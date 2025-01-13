/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package protobufhandler;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import protobufhandler.view.MainView;

//Burp will auto-detect and load any class that extends BurpExtension.
public class App implements BurpExtension {
    private final String extensionName = "Protobuf Handler";
    private final String editorCaption = "Protobuf to Json Decode";
    private final String extensionVersion = "v0.0.11";

    @Override
    public void initialize(MontoyaApi api) {
        Logging logging = api.logging();
        api.extension().setName(extensionName);

        MainView view = new MainView(api);
        api.userInterface().registerSuiteTab(extensionName, view.getUiComponent());

        AppHandler handler = new AppHandler(api, view.getHandlingRules());
        api.http().registerHttpHandler(handler);

        AppEditorProvider editorProvider = new AppEditorProvider(api, editorCaption);
        api.userInterface().registerHttpRequestEditorProvider(editorProvider.getRequestProvider());
        api.userInterface().registerHttpResponseEditorProvider(editorProvider.getResponseProvider());

        logging.logToOutput("Successfully loaded %s %s".formatted(extensionName, extensionVersion));
    }
}