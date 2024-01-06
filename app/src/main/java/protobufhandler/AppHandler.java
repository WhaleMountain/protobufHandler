package protobufhandler;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import protobufhandler.model.ProtobufModel;
import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.core.ByteArray;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;

import java.util.List;

public class AppHandler implements HttpHandler {
    private final MontoyaApi api;
    private final Logging logging;
    private final ProtobufModel pModel;
    private final String protoDescPath = System.getProperty("user.dir")+ "/hello/hello.desc";
	private final String messageTypeName = "HelloRequest";

    public AppHandler(MontoyaApi api) {
        this.api = api;
        this.logging = api.logging();
        this.pModel = new ProtobufModel(protoDescPath);
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        if (!requestToBeSent.isInScope()) {
            return RequestToBeSentAction.continueWith(requestToBeSent);
        }

        String bodyToString = requestToBeSent.bodyToString();

        try {
            DynamicMessage message = pModel.jsonToProtobuf(bodyToString, messageTypeName);
            logging.logToOutput(JsonFormat.printer().print(message));

            HttpRequest request = requestToBeSent.withBody(ByteArray.byteArray(message.toByteArray()));
            return RequestToBeSentAction.continueWith(request);

        } catch (Exception e) {
            logging.logToError(e);
        }

        return RequestToBeSentAction.continueWith(requestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        return ResponseReceivedAction.continueWith(responseReceived);
    }
}
