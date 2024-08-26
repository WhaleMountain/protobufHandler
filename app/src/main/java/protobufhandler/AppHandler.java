package protobufhandler;

import java.util.List;
import java.nio.charset.StandardCharsets;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.logging.Logging;
import protobufhandler.model.AppModel;
import protobufhandler.util.Protobuffer;

public class AppHandler implements HttpHandler {
    private final Logging logging;
    private final List<AppModel> handlingRules;

    public AppHandler(MontoyaApi api, List<AppModel> rules) {
        this.logging = api.logging();
        this.handlingRules = rules;
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        // Protobuf Handler がサポートするツールか判定
        if (!requestToBeSent.toolSource().isFromTool(AppModel.RULE_TARGE_TOOL_TYPE)) {
            return RequestToBeSentAction.continueWith(requestToBeSent);
        }

        if (!requestToBeSent.isInScope()) {
            return RequestToBeSentAction.continueWith(requestToBeSent);
        }

        String bodyToString = new String(requestToBeSent.body().getBytes(), StandardCharsets.UTF_8);
        String headerToString = requestToBeSent.toString().substring(0, requestToBeSent.bodyOffset());
        String requestToString = headerToString.concat(bodyToString);

        for (AppModel rule : handlingRules) {
            if(!rule.isEnabled()) { continue; }
            if(!rule.isReplaceIsRequest()) { continue; }
            if(!rule.getToolScope().contains(requestToBeSent.toolSource().toolType().toolName())) { continue; }
            if(!requestToString.contains(rule.getScope())) {
                logging.logToOutput("リクエストがスコープとマッチしませんでした。");
                logging.logToOutput("Scope: %s\n".formatted(rule.getScope()));
                continue;
            }

            try {
                Descriptor descriptor = rule.getDescriptor();
                DynamicMessage message = Protobuffer.jsonToProtobuf(bodyToString, descriptor);
                HttpRequest request = requestToBeSent.withBody(ByteArray.byteArray(message.toByteArray()));
                return RequestToBeSentAction.continueWith(request);

            } catch (Exception e) {
                logging.logToError(e.getMessage());
                logging.logToOutput("Protobufメッセージに変換することができませんでした。");
                logging.logToOutput("Scope: %s".formatted(rule.getScope()));
                logging.logToOutput("Message Type: %s\n".formatted(rule.getDescriptor().getName()));
            }
        }

        return RequestToBeSentAction.continueWith(requestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        HttpRequest initiatingRequest = responseReceived.initiatingRequest();
        if(!initiatingRequest.isInScope()) {
            return ResponseReceivedAction.continueWith(responseReceived);
        }

        for (AppModel rule : handlingRules) {
            if(!rule.isEnabled()) { continue; }
            if(rule.isReplaceIsRequest()) { continue; }
            if(!rule.getToolScope().contains(responseReceived.toolSource().toolType().toolName())) { continue; }
            if(!initiatingRequest.contains(rule.getScope(), false)) {
                logging.logToOutput("ベースリクエストがスコープとマッチしませんでした。");
                logging.logToOutput("Scope: %s\n".formatted(rule.getScope()));
                continue;
            }

            try {
                Descriptor descriptor = rule.getDescriptor();
                DynamicMessage message = Protobuffer.jsonToProtobuf(new String(responseReceived.body().getBytes(), StandardCharsets.UTF_8), descriptor);
                HttpResponse response = responseReceived.withBody(ByteArray.byteArray(message.toByteArray()));
                return ResponseReceivedAction.continueWith(response);

            } catch (Exception e) {
                logging.logToError(e.getMessage());
                logging.logToOutput("Protobufメッセージに変換することができませんでした。");
                logging.logToOutput("Scope: %s".formatted(rule.getScope()));
                logging.logToOutput("Message Type: %s\n".formatted(rule.getDescriptor().getName()));
            }
        }

        return ResponseReceivedAction.continueWith(responseReceived);
    }
}
