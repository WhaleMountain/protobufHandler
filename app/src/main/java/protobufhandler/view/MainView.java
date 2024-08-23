package protobufhandler.view;

import javax.swing.*;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import protobufhandler.model.AppModel;
import protobufhandler.util.Protobuffer;
import protobufhandler.view.ui.AppTableModel;
import burp.api.montoya.core.ToolType;

import java.awt.*;
import javax.swing.filechooser.*;

import org.checkerframework.framework.qual.JavaExpression;

import com.google.protobuf.Descriptors.Descriptor;

import java.util.List;

public class MainView {
    private JSplitPane mainPanel;
    private AppTableModel itemModel;

    public MainView(MontoyaApi api) {
        Logging logging = api.logging();
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

        JLabel replaceScopeLabel = new JLabel("Replace Scope");
        replaceScopeLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JLabel commentLabel = new JLabel("Comment");
        commentLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));


        JTextField scopeTextField   = new JTextField(35);
        JTextArea  commentTextArea  = new JTextArea(8, 1);
        JScrollPane commentScrollPane = new JScrollPane(commentTextArea);
        scopeTextField.setEditable(false);
        scopeTextField.setFocusable(false);
        commentTextArea.setEnabled(false);

        JComboBox<String> messageTypeComboBox = new JComboBox<String>();
        messageTypeComboBox.setEnabled(false);

        JPanel toolScopePanel                   = new JPanel();
        JCheckBox toolScopeRepeaterCheckBox     = new JCheckBox("Repeater", false); 
        JCheckBox toolScopeIntruderCheckBox     = new JCheckBox("Intruder", false);
        JCheckBox toolScopeScannerCheckBox      = new JCheckBox("Scanner", false);
        JCheckBox toolScopeExtensionsCheckBox   = new JCheckBox("Extensions", false);
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

        JPanel replaceScopePanel = new JPanel();
        ButtonGroup replaceScopeBtnGroup = new ButtonGroup();
        JRadioButton replaceIsRequestBtn = new JRadioButton("Request", true);
        JRadioButton replaceIsResponseBtn = new JRadioButton("Response");
        replaceIsRequestBtn.setEnabled(false);
        replaceIsResponseBtn.setEnabled(false);

        replaceScopeBtnGroup.add(replaceIsRequestBtn);
        replaceScopeBtnGroup.add(replaceIsResponseBtn);
        
        replaceScopePanel.add(replaceIsRequestBtn);
        replaceScopePanel.add(replaceIsResponseBtn);

        // Scope Component
        GridBagConstraints constraints = baseConstraints();
        constraints.gridy = 1;
        itemFormPanel.add(scopeLabel, constraints);
        constraints.gridx = 1;
        itemFormPanel.add(scopeTextField, constraints);

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
        toolScopePanel.add(toolScopeRepeaterCheckBox);
        toolScopePanel.add(toolScopeIntruderCheckBox);
        toolScopePanel.add(toolScopeScannerCheckBox);
        toolScopePanel.add(toolScopeExtensionsCheckBox);
        constraints.gridx = 1;
        itemFormPanel.add(toolScopePanel, constraints);

        // Replace Scope Component
        constraints.gridx = 0; constraints.gridy = 6;
        itemFormPanel.add(replaceScopeLabel, constraints);
        constraints.gridx = 1;
        itemFormPanel.add(replaceScopePanel, constraints);

        // Comment Component
        constraints.gridx = 0; constraints.gridy = 8;
        itemFormPanel.add(commentLabel, constraints);
        constraints.gridx = 1;
        itemFormPanel.add(commentScrollPane, constraints);

        // Buttons Component
        constraints = baseConstraints();
        itemBtnPanel.add(itemNewBtn);
        itemBtnPanel.add(itemSaveBtn);
        itemBtnPanel.add(itemRemoveBtn);
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
                commentTextArea.setEnabled(true);
                toolScopeRepeaterCheckBox.setEnabled(true);
                toolScopeIntruderCheckBox.setEnabled(true);
                toolScopeScannerCheckBox.setEnabled(true);
                toolScopeExtensionsCheckBox.setEnabled(true);
                replaceIsRequestBtn.setEnabled(true);
                replaceIsResponseBtn.setEnabled(true);

                // Setup view
                scopeTextField.setText(item.getScope());
                if(item.getProtoDescPath().isEmpty()) {
                    selectedProtoPathLabel.setText("選択されていません");
                } else {
                    selectedProtoPathLabel.setText(item.getProtoDescPath());
                }
                
                commentTextArea.setText(item.getComment());

                List<String> messageTypes = item.getCachedMessageTypes();
                for(int i = 0; i < messageTypes.size(); i++) {
                    messageTypeComboBox.addItem(messageTypes.get(i));
                    if(messageTypes.get(i).equals(item.getDescriptor().getName())) {
                        messageTypeComboBox.setSelectedIndex(i);
                    }
                }

                for (String toolName : item.getToolScope()) {
                    if (toolName == ToolType.REPEATER.toolName()) {
                        toolScopeRepeaterCheckBox.setSelected(true);
                    } else if (toolName == ToolType.INTRUDER.toolName()) {
                        toolScopeIntruderCheckBox.setSelected(true);
                    } else if (toolName == ToolType.SCANNER.toolName()) {
                        toolScopeScannerCheckBox.setSelected(true);
                    } else if (toolName == ToolType.EXTENSIONS.toolName()) {
                        toolScopeExtensionsCheckBox.setSelected(true);
                    }
                }

                if(item.isReplaceIsRequest()) {
                    replaceScopeBtnGroup.setSelected(replaceIsRequestBtn.getModel(), true);
                } else {
                    replaceScopeBtnGroup.setSelected(replaceIsResponseBtn.getModel(), true);
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
                    logging.logToError(e.getMessage());
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
            item.setProtoDescPath(selectedProtoPathLabel.getText());
            item.setReplaceScope(replaceIsRequestBtn.isSelected());
            item.setComment(commentTextArea.getText());

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
                logging.logToError(e.getMessage());
                logging.logToOutput("Protobuf file の読み込みに失敗しました。");
                logging.logToOutput("File: %s\n".formatted(selectedProtoPathLabel.getText()));
            }

            itemModel.fireTableRowsUpdated(selectedRow, selectedRow);
        });

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
                commentTextArea.setText("");
                selectedProtoPathLabel.setText("選択されていません");
                toolScopeRepeaterCheckBox.setSelected(false);
                toolScopeIntruderCheckBox.setSelected(false);
                toolScopeScannerCheckBox.setSelected(false);
                toolScopeExtensionsCheckBox.setSelected(false);
                replaceScopeBtnGroup.setSelected(replaceIsRequestBtn.getModel(), true);

                // Disable Component
                scopeTextField.setEditable(false);
                scopeTextField.setFocusable(false);
                commentTextArea.setEnabled(false);
                messageTypeComboBox.setEnabled(false);
                itemSaveBtn.setEnabled(false);
                itemRemoveBtn.setEnabled(false);
                protoChooseBtn.setEnabled(false);
                toolScopeRepeaterCheckBox.setEnabled(false);
                toolScopeIntruderCheckBox.setEnabled(false);
                toolScopeScannerCheckBox.setEnabled(false);
                toolScopeExtensionsCheckBox.setEnabled(false);
                replaceIsRequestBtn.setEnabled(false);
                replaceIsResponseBtn.setEnabled(false);
            }
        });

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
