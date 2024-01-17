package protobufhandler;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.logging.Logging;
import protobufhandler.model.AppModel;
import protobufhandler.util.Protobuffer;
import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;
import burp.api.montoya.http.message.requests.HttpRequest;

import java.util.List;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Descriptors.Descriptor;

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

        String bodyToString = requestToBeSent.bodyToString();
        String headerToString = requestToBeSent.toString().substring(0, requestToBeSent.bodyOffset());
        String requestToString = headerToString.concat(bodyToString);

        for (AppModel rule : handlingRules) {
            if(!rule.isEnabled()) { continue; }
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
        return ResponseReceivedAction.continueWith(responseReceived);
    }
}
