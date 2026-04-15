package protobufhandler.view;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor;

import java.awt.Component;

public class AppResponseEditorView extends AbstractEditorView implements ExtensionProvidedHttpResponseEditor {

    public AppResponseEditorView(MontoyaApi api, EditorCreationContext creationContext, String editorCaption) {
        super(api, creationContext, editorCaption);
    }

    @Override
    protected byte[] getBodyBytes() {
        return requestResponse.response().body().getBytes();
    }

    @Override
    protected void onDecodeSuccess(String json) {
        // Response editorはread-only
        editor.setContents(ByteArray.byteArray(json));
    }

    @Override
    protected String getContentType() {
        return requestResponse.response().headerValue("Content-Type");
    }

    @Override
    public HttpResponse getResponse() {
        return requestResponse.response();
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
