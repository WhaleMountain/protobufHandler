package protobufhandler.view;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
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
import javax.swing.filechooser.FileNameExtensionFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Descriptors.Descriptor;

/**
 * Request/Response EditorView の共通ロジックを持つ基底クラス。
 * descファイルの読み込み、メッセージタイプ選択、decode_raw表示を共通化する。
 */
public abstract class AbstractEditorView {
    protected final JPanel mainEditorPanel;
    protected final JComboBox<String> messageTypeComboBox;
    protected final RawEditor editor;
    protected final String editorCaption;
    protected final Logging logging;
    protected HttpRequestResponse requestResponse;
    protected Map<String, Descriptor> messageTypes;

    protected AbstractEditorView(MontoyaApi api, EditorCreationContext creationContext, String editorCaption) {
        this.logging = api.logging();
        this.editorCaption = editorCaption;
        this.messageTypes = new HashMap<>();

        editor = api.userInterface().createRawEditor();
        editor.setEditable(false);

        mainEditorPanel = new JPanel(new BorderLayout());
        JLabel selectedProtoPathLabel = new JLabel("選択されていません");
        selectedProtoPathLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        messageTypeComboBox = new JComboBox<>();
        messageTypeComboBox.setEnabled(false);

        JButton protoChooseBtn = new JButton("Choose");
        JButton resetBtn = new JButton("Reset");
        JButton jsonDecodeBtn = new JButton("Decode");
        jsonDecodeBtn.setEnabled(false);
        resetBtn.setBackground(new Color(251, 180, 196));

        JFileChooser protoChooser = new JFileChooser();
        protoChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        protoChooser.setFileFilter(new FileNameExtensionFilter("Proto Descriptor files (*.desc)", "desc"));

        protoChooseBtn.addActionListener(event -> {
            int actionAns = protoChooser.showOpenDialog(null);
            if (actionAns == JFileChooser.APPROVE_OPTION) {
                messageTypes = new HashMap<>();
                messageTypeComboBox.removeAllItems();
                String selectedPath = protoChooser.getSelectedFile().getAbsolutePath();
                try {
                    List<Descriptor> descriptors = Protobuffer.getMessageTypesFromProtoFile(selectedPath);
                    for (Descriptor descriptor : descriptors) {
                        messageTypes.put(descriptor.getName(), descriptor);
                        messageTypeComboBox.addItem(descriptor.getName());
                    }
                } catch (Exception e) {
                    logging.logToError(e);
                    logging.logToOutput("Protobuf file の読み込みに失敗しました。File: %s".formatted(selectedPath));
                }

                jsonDecodeBtn.setEnabled(true);
                messageTypeComboBox.setEnabled(true);
                selectedProtoPathLabel.setText(selectedPath);
            }
        });

        resetBtn.addActionListener(event -> {
            jsonDecodeBtn.setEnabled(false);
            messageTypeComboBox.removeAllItems();
            messageTypeComboBox.setEnabled(false);
            selectedProtoPathLabel.setText("選択されていません");
            showRawBody();
        });

        jsonDecodeBtn.addActionListener(event -> {
            Descriptor descriptor = messageTypes.get(messageTypeComboBox.getSelectedItem());
            try {
                String json = Protobuffer.protobufToJson(getBodyBytes(), descriptor);
                onDecodeSuccess(json);
            } catch (Exception e) {
                logging.logToError(e);
                editor.setContents(ByteArray.byteArray("Failed to parse input."));
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
        mainEditorPanel.add(editor.uiComponent(), BorderLayout.CENTER);
    }

    /** bodyのバイト列を取得する（Request/Responseで異なる） */
    protected abstract byte[] getBodyBytes();

    /** Decode成功時のeditor設定（Requestはeditable、Responseはread-only） */
    protected abstract void onDecodeSuccess(String json);

    /** Content-Typeヘッダの値を取得する */
    protected abstract String getContentType();

    /** raw decode表示 */
    protected void showRawBody() {
        try {
            String decodedBody = Protobuffer.decodeRaw(getBodyBytes());
            editor.setContents(ByteArray.byteArray(decodedBody));
            editor.setEditable(false);
        } catch (Exception e) {
            editor.setContents(ByteArray.byteArray(getBodyBytes()));
            editor.setEditable(false);
        }
    }

    /** メッセージタイプが選択済みなら型付きデコード、未選択ならraw decode */
    protected void decodeAndDisplay() {
        Object comboBoxObj = messageTypeComboBox.getSelectedItem();
        if (comboBoxObj == null) {
            showRawBody();
        } else {
            Descriptor descriptor = messageTypes.get(comboBoxObj);
            try {
                String json = Protobuffer.protobufToJson(getBodyBytes(), descriptor);
                onDecodeSuccess(json);
            } catch (Exception e) {
                showRawBody();
            }
        }
    }

    /** Content-Typeがprotobuf系か判定 */
    protected boolean isProtobufContentType() {
        try {
            String contentType = getContentType();
            return AppEditorProvider.ENABLE_EDITOR_CONTENT_TYPE.stream()
                    .anyMatch(ctype -> contentType.startsWith(ctype));
        } catch (Exception e) {
            return false;
        }
    }

    public String caption() {
        return editorCaption;
    }

    public Component uiComponent() {
        return mainEditorPanel;
    }

    public Selection selectedData() {
        return editor.selection().get();
    }

    public boolean isModified() {
        return editor.isModified();
    }
}
