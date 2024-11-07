package protobufhandler.view;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor;
import protobufhandler.AppEditorProvider;
import protobufhandler.util.Protobuffer;

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

import java.util.List;
import java.util.HashMap;
import java.util.Objects;

import java.nio.charset.StandardCharsets;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Descriptors.Descriptor;

public class AppRequestEditorView implements ExtensionProvidedHttpRequestEditor {
    private JPanel mainEditorPanel;
    private JComboBox<String> messageTypeComboBox;
    private HttpRequestResponse requestResponse;
    private HashMap<String, Descriptor> messageTypes;
    private final RawEditor requestEditor;
    private final String editorCaption;
    private final Logging logging;

    public AppRequestEditorView(MontoyaApi api, EditorCreationContext creationContext, String editorCaption) {
        logging = api.logging();
        this.editorCaption = editorCaption;

        requestEditor = api.userInterface().createRawEditor();
        requestEditor.setEditable(false);

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
                    logging.logToError(e.getMessage());
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
                String decodedBody = Protobuffer.decodeRaw(requestResponse.request().body().getBytes());
                requestEditor.setContents(ByteArray.byteArray(decodedBody));
                requestEditor.setEditable(false);

            } catch (Exception e) {
                requestEditor.setContents(requestResponse.request().body());
                requestEditor.setEditable(false);
            }
        });

        jsonDecodeBtn.addActionListener( event -> {
            Descriptor descriptor = messageTypes.get(messageTypeComboBox.getSelectedItem());
            try {
                DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
                builder.mergeFrom(requestResponse.request().body().getBytes());

                String json = Protobuffer.protobufToJson(builder.build());
                requestEditor.setEditable(true);
                requestEditor.setContents(ByteArray.byteArray(json));

            } catch(Exception e) {
                logging.logToError(e.getMessage());
                requestEditor.setContents(ByteArray.byteArray("Failed to parse input."));
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
        mainEditorPanel.add(requestEditor.uiComponent(), BorderLayout.CENTER);
    }

    @Override
    public HttpRequest getRequest() {
        if (requestEditor.isModified()) {
            Descriptor descriptor = messageTypes.get(messageTypeComboBox.getSelectedItem());
            HttpRequest request = requestResponse.request();
            try {
                DynamicMessage message = Protobuffer.jsonToProtobuf(new String(requestEditor.getContents().getBytes(), StandardCharsets.UTF_8), descriptor);
                request = request.withBody(ByteArray.byteArray(message.toByteArray()));

                return request;

            } catch(Exception e) {
                logging.logToError(e.getMessage());
                logging.logToOutput("Protobuf メッセージへの変換に失敗しました。\n");
            }
        }

        return requestResponse.request();
    }

    @Override
    public void setRequestResponse(HttpRequestResponse requestResponse) {
        this.requestResponse = requestResponse;
        Object comboBoxObj = messageTypeComboBox.getSelectedItem();
        if(Objects.isNull(comboBoxObj)) {
            try {
                String decodedBody = Protobuffer.decodeRaw(requestResponse.request().body().getBytes());
                requestEditor.setContents(ByteArray.byteArray(decodedBody));
                requestEditor.setEditable(false);

            } catch (Exception e) {
                requestEditor.setContents(requestResponse.request().body());
                requestEditor.setEditable(false);
            }

        } else { // comboBox で選択されているメッセージタイプでデコードする
            Descriptor descriptor = messageTypes.get(comboBoxObj);
            try {
                DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
                builder.mergeFrom(requestResponse.request().body().getBytes());

                String json = Protobuffer.protobufToJson(builder.build());
                requestEditor.setEditable(true);
                requestEditor.setContents(ByteArray.byteArray(json));

            } catch(Exception e) { // デコードに失敗したら、元のリクエストデータをセットする
                try {
                    String decodedBody = Protobuffer.decodeRaw(requestResponse.request().body().getBytes());
                    requestEditor.setContents(ByteArray.byteArray(decodedBody));
                    requestEditor.setEditable(false);
    
                } catch (Exception err) {
                    requestEditor.setContents(requestResponse.request().body());
                    requestEditor.setEditable(false);
                }
            }
        }
    }

    @Override
    public boolean isEnabledFor(HttpRequestResponse requestResponse) {
        HttpRequest request = requestResponse.request();
        try {
            String contentType = request.headerValue("Content-Type");
            return AppEditorProvider.ENABLE_EDITOR_CONTENT_TYPE.stream().anyMatch(ctype -> contentType.startsWith(ctype));

        } catch(Exception e) {
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
        return requestEditor.selection().get();
    }

    @Override
    public boolean isModified() {
        return requestEditor.isModified();
    }
}
