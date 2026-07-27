package protobufhandler.view;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.persistence.PersistedObject;
import protobufhandler.model.AppModel;
import protobufhandler.util.Protobuffer;
import protobufhandler.util.ProjectStore;
import protobufhandler.view.ui.AppTableModel;
import burp.api.montoya.core.ToolType;

import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.ButtonGroup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.protobuf.Descriptors.Descriptor;

import java.util.List;

public class MainView {
    private JSplitPane mainPanel;
    private AppTableModel itemModel;

    public MainView(MontoyaApi api) {
        Logging logging = api.logging();
        // ルール一覧を Burp のユーザープロジェクトへ永続化するストア
        ProjectStore projectStore = new ProjectStore(api.persistence().extensionData(), logging);
        mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainPanel.setDividerLocation(0.5);
        
        JPanel itemFormPanel = new JPanel(new GridBagLayout());

        JLabel scopeLabel = new JLabel("Scope");
        scopeLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JLabel messageTypeLabel = new JLabel("Message Type");
        messageTypeLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JLabel toolScopeLabel = new JLabel("Tool Scope");
        toolScopeLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JLabel protoDescLabel = new JLabel("Protobuf File");
        protoDescLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JLabel selectedProtoPathLabel = new JLabel("選択されていません");
        selectedProtoPathLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JLabel handlingScopeLabel = new JLabel("Handling Scope");
        handlingScopeLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JLabel responseBodyLabel = new JLabel("(Optional) Replaced Response Body");
        responseBodyLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));


        JTextField scopeTextField   = new JTextField(40);
        JTextArea  responseBodyTextArea  = new JTextArea(8, 1);
        JScrollPane responseBodyScrollPane = new JScrollPane(responseBodyTextArea);
        scopeTextField.setEditable(false);
        scopeTextField.setFocusable(false);
        responseBodyTextArea.setEnabled(false);

        JCheckBox scopeRegexCheckBox = new JCheckBox("Regex", false);
        scopeRegexCheckBox.setEnabled(false);

        JComboBox<String> messageTypeComboBox = new JComboBox<String>();
        messageTypeComboBox.setEnabled(false);

        JPanel toolScopePanel                   = new JPanel();
        JCheckBox toolScopeProxyCheckBox        = new JCheckBox("Proxy", false); 
        JCheckBox toolScopeRepeaterCheckBox     = new JCheckBox("Repeater", false); 
        JCheckBox toolScopeIntruderCheckBox     = new JCheckBox("Intruder", false);
        JCheckBox toolScopeScannerCheckBox      = new JCheckBox("Scanner", false);
        JCheckBox toolScopeExtensionsCheckBox   = new JCheckBox("Extensions", false);
        toolScopeProxyCheckBox.setEnabled(false);
        toolScopeRepeaterCheckBox.setEnabled(false);
        toolScopeIntruderCheckBox.setEnabled(false);
        toolScopeScannerCheckBox.setEnabled(false);
        toolScopeExtensionsCheckBox.setEnabled(false);

        JPanel itemBtnPanel      = new JPanel();
        JButton itemNewBtn       = new JButton("New");
        JButton itemSaveBtn      = new JButton("Save");
        JButton itemRemoveBtn    = new JButton("Remove");
        JButton protoChooseBtn   = new JButton("Choose File");

        itemSaveBtn.setEnabled(false);
        itemRemoveBtn.setEnabled(false);
        protoChooseBtn.setEnabled(false);

        JFileChooser protoChooser = new JFileChooser();
        protoChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter descFilter = new FileNameExtensionFilter("Proto Descriptor files (*.desc)", "desc");
        protoChooser.setFileFilter(descFilter);

        // ルール一覧の保存/読み込み用 FileChooser (*.pbh)
        JFileChooser ruleFileChooser = new JFileChooser();
        ruleFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter ruleFilter = new FileNameExtensionFilter("Protobuf Handler files (*.pbh)", "pbh");
        ruleFileChooser.setFileFilter(ruleFilter);

        // ルール一覧の保存/読み込みボタン（New/Save/Remove と同じ行に並べる）
        JButton exportBtn = new JButton("Export");
        JButton importBtn = new JButton("Import");

        JPanel handlingScopePanel = new JPanel();
        ButtonGroup handlingScopeBtnGroup = new ButtonGroup();
        JRadioButton requestHandlingBtn = new JRadioButton("Request", true);
        JRadioButton responseHandlingBtn = new JRadioButton("Response");
        requestHandlingBtn.setEnabled(false);
        responseHandlingBtn.setEnabled(false);
        requestHandlingBtn.setText("Request");
        responseHandlingBtn.setText("Response");

        handlingScopeBtnGroup.add(requestHandlingBtn);
        handlingScopeBtnGroup.add(responseHandlingBtn);
        
        handlingScopePanel.add(requestHandlingBtn);
        handlingScopePanel.add(responseHandlingBtn);

        // Scope Component
        GridBagConstraints constraints = baseConstraints();
        constraints.gridy = 1;
        itemFormPanel.add(scopeLabel, constraints);
        constraints.gridx = 1;
        itemFormPanel.add(scopeTextField, constraints);
        constraints.gridx = 2;
        itemFormPanel.add(scopeRegexCheckBox, constraints);

        // Proto file Component
        constraints.gridx = 0; constraints.gridy = 2;
        itemFormPanel.add(protoDescLabel, constraints);
        constraints.gridx = 1;
        itemFormPanel.add(protoChooseBtn, constraints);
        constraints.gridx = 2;
        itemFormPanel.add(selectedProtoPathLabel, constraints);

        // Message Type Component
        constraints.gridx = 0; constraints.gridy = 3;
        itemFormPanel.add(messageTypeLabel, constraints);
        constraints.gridx = 1;
        itemFormPanel.add(messageTypeComboBox, constraints);

        // Tool Scope Component
        constraints.gridx = 0; constraints.gridy = 4;
        itemFormPanel.add(toolScopeLabel, constraints);
        toolScopePanel.add(toolScopeProxyCheckBox);
        toolScopePanel.add(toolScopeRepeaterCheckBox);
        toolScopePanel.add(toolScopeIntruderCheckBox);
        toolScopePanel.add(toolScopeScannerCheckBox);
        toolScopePanel.add(toolScopeExtensionsCheckBox);
        constraints.gridx = 1;
        itemFormPanel.add(toolScopePanel, constraints);

        // Handling Scope Component
        constraints.gridx = 0; constraints.gridy = 6;
        itemFormPanel.add(handlingScopeLabel, constraints);
        constraints.gridx = 1;
        itemFormPanel.add(handlingScopePanel, constraints);

        // Optional Replace Response Body Component
        constraints.gridx = 0; constraints.gridy = 8;
        itemFormPanel.add(responseBodyLabel, constraints);
        constraints.gridx = 1;
        // テキストエリアが縦に潰れないよう余白を吸収させる
        constraints.fill    = GridBagConstraints.BOTH;
        constraints.weighty = 1.0;
        itemFormPanel.add(responseBodyScrollPane, constraints);
        constraints.weighty = 0;
        constraints.fill    = GridBagConstraints.HORIZONTAL;

        // Buttons Component (New / Save / Remove / Export / Import を1行に並べる)
        constraints = baseConstraints();
        itemBtnPanel.add(itemNewBtn);
        itemBtnPanel.add(itemSaveBtn);
        itemBtnPanel.add(itemRemoveBtn);
        itemBtnPanel.add(exportBtn);
        itemBtnPanel.add(importBtn);
        constraints.gridx = 1;
        itemFormPanel.add(itemBtnPanel, constraints);

        itemModel = new AppTableModel();
        JTable itemTable = new JTable(itemModel) {
            // アイテムを選択した時の動作
            @Override
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend)
            {
                AppModel item = itemModel.get(rowIndex);

                // Clear view
                messageTypeComboBox.removeAllItems();
                toolScopeProxyCheckBox.setSelected(false);
                toolScopeRepeaterCheckBox.setSelected(false);
                toolScopeIntruderCheckBox.setSelected(false);
                toolScopeScannerCheckBox.setSelected(false);
                toolScopeExtensionsCheckBox.setSelected(false);

                // Enabled Component
                itemSaveBtn.setEnabled(true);
                itemRemoveBtn.setEnabled(true);
                protoChooseBtn.setEnabled(true);
                messageTypeComboBox.setEnabled(true);
                scopeTextField.setEditable(true);
                scopeTextField.setFocusable(true);
                scopeRegexCheckBox.setEnabled(true);
                toolScopeProxyCheckBox.setEnabled(true);
                toolScopeRepeaterCheckBox.setEnabled(true);
                toolScopeIntruderCheckBox.setEnabled(true);
                toolScopeScannerCheckBox.setEnabled(true);
                toolScopeExtensionsCheckBox.setEnabled(true);
                requestHandlingBtn.setEnabled(true);
                responseHandlingBtn.setEnabled(true);

                // Setup view
                scopeTextField.setText(item.getScope());
                scopeRegexCheckBox.setSelected(item.isScopeRegex());
                if(item.getProtoDescPath().isEmpty()) {
                    selectedProtoPathLabel.setText("選択されていません");
                } else {
                    selectedProtoPathLabel.setText(item.getProtoDescPath());
                }
                
                responseBodyTextArea.setText(item.getReplaceResponseBody());

                List<String> messageTypes = item.getCachedMessageTypes();
                for(int i = 0; i < messageTypes.size(); i++) {
                    messageTypeComboBox.addItem(messageTypes.get(i));
                    // descriptor が未設定(null)のケースがあるため null ガードする
                    if(item.getDescriptor() != null && messageTypes.get(i).equals(item.getDescriptor().getName())) {
                        messageTypeComboBox.setSelectedIndex(i);
                    }
                }

                for (String toolName : item.getToolScope()) {
                    if (toolName.equals(ToolType.PROXY.toolName())) {
                        toolScopeProxyCheckBox.setSelected(true);
                    } else if (toolName.equals(ToolType.REPEATER.toolName())) {
                        toolScopeRepeaterCheckBox.setSelected(true);
                    } else if (toolName.equals(ToolType.INTRUDER.toolName())) {
                        toolScopeIntruderCheckBox.setSelected(true);
                    } else if (toolName.equals(ToolType.SCANNER.toolName())) {
                        toolScopeScannerCheckBox.setSelected(true);
                    } else if (toolName.equals(ToolType.EXTENSIONS.toolName())) {
                        toolScopeExtensionsCheckBox.setSelected(true);
                    }
                }

                if(item.isRequestHandling()) { // Requestを選択
                    handlingScopeBtnGroup.setSelected(requestHandlingBtn.getModel(), true);
                    responseBodyTextArea.setEnabled(false);

                } else { // Responseを選択
                    handlingScopeBtnGroup.setSelected(responseHandlingBtn.getModel(), true);
                    responseBodyTextArea.setEnabled(true);
                }

                super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }
        };

        protoChooseBtn.addActionListener( event -> {
            int actionAns = protoChooser.showOpenDialog(null);
            if(actionAns == JFileChooser.APPROVE_OPTION) {
                messageTypeComboBox.removeAllItems();
                String selectedPath = protoChooser.getSelectedFile().getAbsolutePath();
                try {
                    List<Descriptor> descriptors = Protobuffer.getMessageTypesFromProtoFile(selectedPath);
                    for (Descriptor descriptor : descriptors) {
                        messageTypeComboBox.addItem(descriptor.getName());
                    }
                    
                } catch(Exception e) {
                    logging.logToError(e);
                    logging.logToOutput("Protobuf file の読み込みに失敗しました。");
                    logging.logToOutput("File: %s\n".formatted(selectedPath));
                }

                selectedProtoPathLabel.setText(selectedPath);
            }
        });

        itemNewBtn.addActionListener( event -> {
            AppModel item = new AppModel();
            item.setComment("選択して編集して下さい!!");
            itemModel.add(item);
        });

        // 選択したアイテムの保存
        itemSaveBtn.addActionListener( event -> {
            int selectedRow = itemTable.getSelectedRow();
            if (selectedRow < 0) { return; }
            // Proto file、 Scope が指定されていない場合は保存しない
            if (selectedProtoPathLabel.getText().equals("選択されていません")) {
                protoChooseBtn.requestFocus();
                return;
            }
            if (scopeTextField.getText().isBlank()) {
                scopeTextField.requestFocus();
                return;
            }

            AppModel item = itemModel.get(selectedRow);
            item.clearToolScope();
            item.clearCachedMessageType();

            if(toolScopeProxyCheckBox.isSelected()) {
                item.setToolScope(ToolType.PROXY.toolName());
            } else {
                item.removeToolScope(ToolType.PROXY.toolName());
            }

            if(toolScopeRepeaterCheckBox.isSelected()) {
                item.setToolScope(ToolType.REPEATER.toolName());
            } else {
                item.removeToolScope(ToolType.REPEATER.toolName());
            }

            if(toolScopeIntruderCheckBox.isSelected()) {
                item.setToolScope(ToolType.INTRUDER.toolName());
            } else {
                item.removeToolScope(ToolType.INTRUDER.toolName());
            }
            if(toolScopeScannerCheckBox.isSelected()) {
                item.setToolScope(ToolType.SCANNER.toolName());
            } else {
                item.removeToolScope(ToolType.SCANNER.toolName());
            }

            if(toolScopeExtensionsCheckBox.isSelected()) {
                item.setToolScope(ToolType.EXTENSIONS.toolName());
            } else {
                item.removeToolScope(ToolType.EXTENSIONS.toolName());
            }

            item.setScope(scopeTextField.getText());
            item.setScopeRegex(scopeRegexCheckBox.isSelected());
            item.setProtoDescPath(selectedProtoPathLabel.getText());
            item.setRequestHandling(requestHandlingBtn.isSelected());
            if(responseHandlingBtn.isSelected()) { // Replaceが選択されているなら保存
                item.setReplaceResponseBody(responseBodyTextArea.getText());
            }

            try {
                List<Descriptor> descriptors = Protobuffer.getMessageTypesFromProtoFile(selectedProtoPathLabel.getText());
                Object comboBoxObj = messageTypeComboBox.getSelectedItem();
                for (Descriptor descriptor : descriptors) {
                    item.setCachedMessageType(descriptor.getName());
                    if(descriptor.getName().equals(String.valueOf(comboBoxObj))) {
                        item.setDescriptor(descriptor);
                    }
                }

            } catch(Exception e) {
                logging.logToError(e);
                logging.logToOutput("Protobuf file の読み込みに失敗しました。");
                logging.logToOutput("File: %s\n".formatted(selectedProtoPathLabel.getText()));
            }

            itemModel.fireTableRowsUpdated(selectedRow, selectedRow);
        });

        // ラジオボタンのイベント
        ActionListener radioAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JRadioButton selecteRadioButton = (JRadioButton) e.getSource();
                if(selecteRadioButton.getText().equals("Request")) {
                    responseBodyTextArea.setEnabled(false);

                } else if(selecteRadioButton.getText().equals("Response")) {
                    responseBodyTextArea.setEnabled(true);
                }
            }
        };
        requestHandlingBtn.addActionListener(radioAction);
        responseHandlingBtn.addActionListener(radioAction);

        // 選択したアイテムの削除
        itemRemoveBtn.addActionListener( e -> {
            int selectedRow = itemTable.getSelectedRow();
            if (selectedRow < 0) { return; }
            itemModel.remove(selectedRow);

            if (selectedRow - 1 >= 0) {
                itemTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
                itemTable.changeSelection(selectedRow - 1, selectedRow - 1, false, false);

            } else if (selectedRow - 1 < 0 && itemTable.getRowCount() > 0) {
                itemTable.setRowSelectionInterval(0, 0);
                itemTable.changeSelection(0, 0, false, false);

            } else {
                // Clear view
                messageTypeComboBox.removeAllItems();
                scopeTextField.setText("");
                scopeRegexCheckBox.setSelected(false);
                responseBodyTextArea.setText("");
                selectedProtoPathLabel.setText("選択されていません");
                toolScopeProxyCheckBox.setSelected(false);
                toolScopeRepeaterCheckBox.setSelected(false);
                toolScopeIntruderCheckBox.setSelected(false);
                toolScopeScannerCheckBox.setSelected(false);
                toolScopeExtensionsCheckBox.setSelected(false);
                handlingScopeBtnGroup.setSelected(requestHandlingBtn.getModel(), true);

                // Disable Component
                scopeTextField.setEditable(false);
                scopeTextField.setFocusable(false);
                scopeRegexCheckBox.setEnabled(false);
                responseBodyTextArea.setEnabled(false);
                messageTypeComboBox.setEnabled(false);
                itemSaveBtn.setEnabled(false);
                itemRemoveBtn.setEnabled(false);
                protoChooseBtn.setEnabled(false);
                toolScopeProxyCheckBox.setEnabled(false);
                toolScopeRepeaterCheckBox.setEnabled(false);
                toolScopeIntruderCheckBox.setEnabled(false);
                toolScopeScannerCheckBox.setEnabled(false);
                toolScopeExtensionsCheckBox.setEnabled(false);
                requestHandlingBtn.setEnabled(false);
                responseHandlingBtn.setEnabled(false);
            }
        });

        // ルール一覧をファイルに保存
        exportBtn.addActionListener( event -> {
            String saveFilePath = "";
            if(ruleFileChooser.showSaveDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
                File ruleSaveFile = ruleFileChooser.getSelectedFile();
                // .pbh 拡張子をつける
                if(!ruleSaveFile.getName().toLowerCase().endsWith(".pbh")) {
                    ruleSaveFile = new File(ruleSaveFile.getAbsoluteFile() + ".pbh");
                }

                // ファイルが既に存在していたら確認する
                if(ruleSaveFile.exists()) {
                    int result = JOptionPane.showConfirmDialog(mainPanel, "ファイルが既に存在します。上書きしますか？", "Already exists", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    // NO が選択された場合は保存処理を中止
                    if (result != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                // 保存パスの取得
                saveFilePath = ruleSaveFile.getAbsolutePath();
            } else {
                return;
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFilePath))) {
                oos.writeObject(new ArrayList<>(itemModel.getAll()));
            } catch (IOException err) {
                logging.logToError(err);
                JOptionPane.showMessageDialog(mainPanel, "ファイルの保存に失敗しました。", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ファイルからルール一覧を読み込み
        importBtn.addActionListener( event -> {
            String loadFilePath = "";
            if(ruleFileChooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
                File ruleLoadFile = ruleFileChooser.getSelectedFile();
                if(ruleLoadFile.isFile()) {
                    loadFilePath = ruleLoadFile.getAbsolutePath();
                } else {
                    return;
                }
            } else {
                return;
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(loadFilePath))) {
                // 信頼できないファイルの読み込みによる任意コード実行を防ぐため、
                // 復元を許可するクラスを AppModel と標準コレクション/文字列等に限定する。
                ois.setObjectInputFilter(ObjectInputFilter.Config.createFilter(
                    "protobufhandler.model.AppModel;java.util.*;java.lang.*;!*"));

                Object obj = ois.readObject();
                if(obj instanceof ArrayList<?> al) {
                    // 参照先の Protobuf file (.desc) が存在しないルールを記録する
                    List<String> missingProtoFiles = new ArrayList<>();
                    for(Object element : al) {
                        if(element instanceof AppModel item) {
                            String protoPath = item.getProtoDescPath();
                            if(protoPath != null && !protoPath.isBlank() && !new File(protoPath).isFile()) {
                                // 参照先の .desc が存在しない → proto 情報をクリアして未選択状態に戻す
                                if(!missingProtoFiles.contains(protoPath)) {
                                    missingProtoFiles.add(protoPath);
                                }
                                item.setProtoDescPath("");
                                item.clearCachedMessageType();
                            } else {
                                // descriptor は transient で復元されないため、proto ファイルから再構築する
                                reconstructDescriptor(item, logging);
                            }
                            itemModel.add(item);
                        }
                    }

                    // 見つからなかった Protobuf file があればユーザーに通知する
                    if(!missingProtoFiles.isEmpty()) {
                        String message = "以下の Protobuf file (.desc) が見つかりませんでした。\n"
                                + "該当ルールの Message Type は復元されていません。\n\n"
                                + String.join("\n", missingProtoFiles);
                        JOptionPane.showMessageDialog(mainPanel, message, "Protobuf file not found", JOptionPane.WARNING_MESSAGE);
                    }
                }
            } catch (IOException | ClassNotFoundException err) {
                logging.logToError(err);
                JOptionPane.showMessageDialog(mainPanel, "ファイルの読み込みに失敗しました。", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ユーザープロジェクトに保存済みのルールを復元する
        for (AppModel item : projectStore.load()) {
            itemModel.add(item);
        }

        // 以降のルール変更（追加/編集/削除/有効化トグル/コメント編集）を
        // 自動でユーザープロジェクトへ保存する。
        itemModel.addTableModelListener(event -> projectStore.save(itemModel.getAll()));

        JScrollPane itemTableScrollPane = new JScrollPane(itemTable);
        mainPanel.setLeftComponent(itemTableScrollPane);
        mainPanel.setRightComponent(itemFormPanel);
    }

    public Component getUiComponent() {
        return mainPanel;
    }

    public List<AppModel> getHandlingRules() {
        return itemModel.getAll();
    }

    // 読み込んだ AppModel の descriptor を proto ファイルから再構築する。
    // proto ファイルが無い / 該当 message type が見つからない場合は descriptor は null のまま。
    private void reconstructDescriptor(AppModel item, Logging logging) {
        String path = item.getProtoDescPath();
        String name = item.getDescriptorName();
        if (path == null || path.isBlank() || name == null || name.isBlank()) {
            return;
        }
        try {
            List<Descriptor> descriptors = Protobuffer.getMessageTypesFromProtoFile(path);
            for (Descriptor descriptor : descriptors) {
                if (descriptor.getName().equals(name)) {
                    item.setDescriptor(descriptor);
                    break;
                }
            }
        } catch (Exception e) {
            logging.logToError(e);
            logging.logToOutput("Protobuf file の読み込みに失敗しました。");
            logging.logToOutput("File: %s\n".formatted(path));
        }
    }

    private GridBagConstraints baseConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets             = new Insets(10, 4, 2, 10);
        constraints.anchor             = GridBagConstraints.NORTHWEST;
        constraints.fill               = GridBagConstraints.HORIZONTAL;
        constraints.gridx              = 0;
        constraints.gridy              = 0;
        return constraints;
    }
}
