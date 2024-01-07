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
    public static final List<String> ENABLE_EDITOR_CONTENT_TYPE = new ArrayList<String>() {
        {
            add("application/x-protobuf");
            add("application/protobuf");
            add("application/grpc-web+proto");
        }
    };
    public static final String EDITOR_CAPTION = "Protobuf to Json Decoder";

    public AppEditorProvider(MontoyaApi api) {
        this.api = api;
    }

    public HttpRequestEditorProvider getRequestProvider() {
        return new AppRequestEditorProvider(api);
    }

    public HttpResponseEditorProvider getResponseProvider() {
        return new AppResponseEditorProvider(api);
    }
}

// Request message Editor
final class AppRequestEditorProvider implements HttpRequestEditorProvider {
    private final MontoyaApi api;

    public AppRequestEditorProvider(MontoyaApi api) {
        this.api = api;
    }

    @Override
    public ExtensionProvidedHttpRequestEditor provideHttpRequestEditor(EditorCreationContext creationContext) {
        return new AppRequestEditorView(api, creationContext);
    }
}

// Response message Editor
final class AppResponseEditorProvider implements HttpResponseEditorProvider {
    private final MontoyaApi api;

    public AppResponseEditorProvider(MontoyaApi api) {
        this.api = api;
    }

    @Override
    public ExtensionProvidedHttpResponseEditor provideHttpResponseEditor(EditorCreationContext creationContext) {
        return new AppResponseEditorView(api, creationContext);
    }
}