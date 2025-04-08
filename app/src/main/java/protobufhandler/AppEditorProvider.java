package protobufhandler;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor;
import burp.api.montoya.ui.editor.extension.HttpRequestEditorProvider;
import burp.api.montoya.ui.editor.extension.HttpResponseEditorProvider;
import protobufhandler.view.AppRequestEditorView;
import protobufhandler.view.AppResponseEditorView;

import java.util.List;
import java.util.ArrayList;

public class AppEditorProvider {
    private final MontoyaApi api;
    private final String editorCaption;
    public static final List<String> ENABLE_EDITOR_CONTENT_TYPE = new ArrayList<String>() {
        {
            add("application/x-protobuf");
            add("application/protobuf");
            add("application/grpc-web+proto");
            add("application/grpc");
        }
    };

    public AppEditorProvider(MontoyaApi api, String editorCaption) {
        this.api = api;
        this.editorCaption = editorCaption;
    }

    public HttpRequestEditorProvider getRequestProvider() {
        return new AppRequestEditorProvider(api, editorCaption);
    }

    public HttpResponseEditorProvider getResponseProvider() {
        return new AppResponseEditorProvider(api, editorCaption);
    }
}

// Request message Editor
final class AppRequestEditorProvider implements HttpRequestEditorProvider {
    private final MontoyaApi api;
    private final String editorCaption;

    public AppRequestEditorProvider(MontoyaApi api, String editorCaption) {
        this.api = api;
        this.editorCaption = editorCaption;
    }

    @Override
    public ExtensionProvidedHttpRequestEditor provideHttpRequestEditor(EditorCreationContext creationContext) {
        return new AppRequestEditorView(api, creationContext, editorCaption);
    }
}

// Response message Editor
final class AppResponseEditorProvider implements HttpResponseEditorProvider {
    private final MontoyaApi api;
    private final String editorCaption;

    public AppResponseEditorProvider(MontoyaApi api, String editorCaption) {
        this.api = api;
        this.editorCaption = editorCaption;
    }

    @Override
    public ExtensionProvidedHttpResponseEditor provideHttpResponseEditor(EditorCreationContext creationContext) {
        return new AppResponseEditorView(api, creationContext, editorCaption);
    }
}