package protobufhandler.view;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor;
import protobufhandler.util.Protobuffer;

import java.awt.Component;
import java.nio.charset.StandardCharsets;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Descriptors.Descriptor;

public class AppRequestEditorView extends AbstractEditorView implements ExtensionProvidedHttpRequestEditor {

    public AppRequestEditorView(MontoyaApi api, EditorCreationContext creationContext, String editorCaption) {
        super(api, creationContext, editorCaption);
    }

    @Override
    protected byte[] getBodyBytes() {
        return requestResponse.request().body().getBytes();
    }

    @Override
    protected void onDecodeSuccess(String json) {
        editor.setEditable(true);
        editor.setContents(ByteArray.byteArray(json));
    }

    @Override
    protected String getContentType() {
        return requestResponse.request().headerValue("Content-Type");
    }

    @Override
    public HttpRequest getRequest() {
        if (editor.isModified()) {
            Descriptor descriptor = messageTypes.get(messageTypeComboBox.getSelectedItem());
            try {
                DynamicMessage message = Protobuffer.jsonToProtobuf(
                        new String(editor.getContents().getBytes(), StandardCharsets.UTF_8), descriptor);
                return requestResponse.request().withBody(ByteArray.byteArray(message.toByteArray()));
            } catch (Exception e) {
                logging.logToError(e);
                logging.logToOutput("Protobuf メッセージへの変換に失敗しました。\n");
            }
        }
        return requestResponse.request();
    }

    @Override
    public void setRequestResponse(HttpRequestResponse requestResponse) {
        this.requestResponse = requestResponse;
        decodeAndDisplay();
    }

    @Override
    public boolean isEnabledFor(HttpRequestResponse requestResponse) {
        this.requestResponse = requestResponse;
        return isProtobufContentType();
    }

    @Override
    public String caption() {
        return super.caption();
    }

    @Override
    public Component uiComponent() {
        return super.uiComponent();
    }

    @Override
    public Selection selectedData() {
        return super.selectedData();
    }

    @Override
    public boolean isModified() {
        return super.isModified();
    }
}
