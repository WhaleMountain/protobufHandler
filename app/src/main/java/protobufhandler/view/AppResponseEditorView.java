package protobufhandler.view;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JFileChooser;
import javax.swing.BoxLayout;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.protobuf.Descriptors.Descriptor;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor;
import protobufhandler.AppEditorProvider;
import protobufhandler.util.Protobuffer;

public class AppResponseEditorView implements ExtensionProvidedHttpResponseEditor {
    private JPanel mainEditorPanel;
    private JComboBox<String> messageTypeComboBox;
    private HttpRequestResponse requestResponse;
    private HashMap<String, Descriptor> messageTypes;
    private final RawEditor responseEditor;
    private final String editorCaption;
    private final Logging logging;

    public AppResponseEditorView(MontoyaApi api, EditorCreationContext creationContext, String editorCaption) {
        logging = api.logging();
        this.editorCaption = editorCaption;

        responseEditor = api.userInterface().createRawEditor();
        responseEditor.setEditable(false);

        mainEditorPanel = new JPanel(new BorderLayout());
        JLabel selectedProtoPathLabel = new JLabel("選択されていません");
        selectedProtoPathLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        messageTypeComboBox = new JComboBox<String>();
        messageTypeComboBox.setEnabled(false);

        JButton protoChooseBtn  = new JButton("Choose");
        JButton resetBtn        = new JButton("Reset");
        JButton jsonDecodeBtn   = new JButton("Decode");
        jsonDecodeBtn.setEnabled(false);
        resetBtn.setBackground(new Color(251, 180, 196));

        JFileChooser protoChooser = new JFileChooser();
        protoChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter descFilter = new FileNameExtensionFilter("Proto Descriptor files (*.desc)", "desc");
        protoChooser.setFileFilter(descFilter);

        protoChooseBtn.addActionListener( event -> {
            int actionAns = protoChooser.showOpenDialog(null);
            if(actionAns == JFileChooser.APPROVE_OPTION) {
                messageTypes = new HashMap<String, Descriptor>();
                messageTypeComboBox.removeAllItems();
                String selectedPath = protoChooser.getSelectedFile().getAbsolutePath();
                try {
                    List<Descriptor> descriptors = Protobuffer.getMessageTypesFromProtoFile(selectedPath);
                    for (Descriptor descriptor : descriptors) {
                        messageTypes.put(descriptor.getName(), descriptor);
                        messageTypeComboBox.addItem(descriptor.getName());
                    }

                } catch(Exception e) {
                    logging.logToError(e);
                    logging.logToOutput("Protobuf file の読み込みに失敗しました。");
                    logging.logToOutput("File: %s\n".formatted(selectedPath));
                }

                jsonDecodeBtn.setEnabled(true);
                messageTypeComboBox.setEnabled(true);
                selectedProtoPathLabel.setText(selectedPath);
            }
        });

        resetBtn.addActionListener(event -> {
            // reset ui
            jsonDecodeBtn.setEnabled(false);
            messageTypeComboBox.removeAllItems();
            messageTypeComboBox.setEnabled(false);
            selectedProtoPathLabel.setText("選択されていません");

            // set raw format
            try {
                String decodedBody = Protobuffer.decodeRaw(requestResponse.response().body().getBytes());
                responseEditor.setContents(ByteArray.byteArray(decodedBody));

            } catch(Exception e) {
                responseEditor.setContents(requestResponse.response().body());
            }
        });

        jsonDecodeBtn.addActionListener( event -> {
            Descriptor descriptor = messageTypes.get(messageTypeComboBox.getSelectedItem());
            try {
                String json = Protobuffer.protobufToJson(requestResponse.response().body().getBytes(), descriptor);
                responseEditor.setContents(ByteArray.byteArray(json));

            } catch(Exception e) {
                logging.logToError(e);
                responseEditor.setContents(ByteArray.byteArray("Failed to parse input."));
            }
        });

        JPanel protoPathPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        protoPathPanel.add(selectedProtoPathLabel);
        protoPathPanel.add(protoChooseBtn);
        protoPathPanel.add(resetBtn);

        JPanel protoDecodePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        protoDecodePanel.add(messageTypeComboBox);
        protoDecodePanel.add(jsonDecodeBtn);

        JPanel protobufFormPanel = new JPanel();
        protobufFormPanel.setLayout(new BoxLayout(protobufFormPanel, BoxLayout.Y_AXIS));
        protobufFormPanel.add(protoPathPanel);
        protobufFormPanel.add(protoDecodePanel);

        mainEditorPanel.add(protobufFormPanel, BorderLayout.NORTH);
        mainEditorPanel.add(responseEditor.uiComponent(), BorderLayout.CENTER);
    }

    @Override
    public HttpResponse getResponse() {
        return requestResponse.response();
    }

    @Override
    public void setRequestResponse(HttpRequestResponse requestResponse) {
        this.requestResponse = requestResponse;
        Object comboBoxObj = messageTypeComboBox.getSelectedItem();
        if(Objects.isNull(comboBoxObj)) {
            try {
                String decodedBody = Protobuffer.decodeRaw(requestResponse.response().body().getBytes());
                responseEditor.setContents(ByteArray.byteArray(decodedBody));

            } catch(Exception e) {
                responseEditor.setContents(requestResponse.response().body());
            }

        } else { // comboBox で選択されているメッセージタイプでデコードする
            Descriptor descriptor = messageTypes.get(comboBoxObj);
            try {
                String json = Protobuffer.protobufToJson(requestResponse.response().body().getBytes(), descriptor);
                responseEditor.setContents(ByteArray.byteArray(json));

            } catch(Exception e) { // デコードに失敗したら、元のリクエストデータをセットする
                try {
                    String decodedBody = Protobuffer.decodeRaw(requestResponse.response().body().getBytes());
                    responseEditor.setContents(ByteArray.byteArray(decodedBody));
    
                } catch(Exception err) {
                    responseEditor.setContents(requestResponse.response().body());
                }
            }
        }
    }

    @Override
    public boolean isEnabledFor(HttpRequestResponse requestResponse) {
        HttpResponse response = requestResponse.response();
        try {
            String contentType = response.headerValue("Content-Type");
            return AppEditorProvider.ENABLE_EDITOR_CONTENT_TYPE.stream().anyMatch(ctype -> contentType.startsWith(ctype));
            
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String caption() {
        return editorCaption;
    }

    @Override
    public Component uiComponent() {
        return mainEditorPanel;
    }

    @Override
    public Selection selectedData()
    {
        return responseEditor.selection().get();
    }

    @Override
    public boolean isModified() {
        return responseEditor.isModified();
    }
}