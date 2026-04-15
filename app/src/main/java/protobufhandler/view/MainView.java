package protobufhandler.view;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import protobufhandler.model.AppModel;
import protobufhandler.util.Protobuffer;
import protobufhandler.view.ui.AppTableModel;
import burp.api.montoya.core.ToolType;

import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.ButtonGroup;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;

import com.google.protobuf.Descriptors.Descriptor;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class MainView {
    private static final String NO_FILE_SELECTED = "選択されていません";
    private static final Font LABEL_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 13);

    private final JSplitPane mainPanel;
    private final AppTableModel itemModel;
    private final Logging logging;

    // Form components
    private final JTextField scopeTextField;
    private final JLabel selectedProtoPathLabel;
    private final JComboBox<String> messageTypeComboBox;
    private final JTextArea responseBodyTextArea;
    private final JRadioButton requestHandlingBtn;
    private final JRadioButton responseHandlingBtn;
    private final ButtonGroup handlingScopeBtnGroup;
    private final JButton itemSaveBtn;
    private final JButton itemRemoveBtn;
    private final JButton protoChooseBtn;
    private final Map<ToolType, JCheckBox> toolScopeCheckBoxes;

    public MainView(MontoyaApi api) {
        this.logging = api.logging();
        this.itemModel = new AppTableModel();

        mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainPanel.setDividerLocation(0.5);

        // Form components
        scopeTextField = new JTextField(40);
        scopeTextField.setEditable(false);
        scopeTextField.setFocusable(false);

        selectedProtoPathLabel = createLabel(NO_FILE_SELECTED);

        messageTypeComboBox = new JComboBox<>();
        messageTypeComboBox.setEnabled(false);

        responseBodyTextArea = new JTextArea(8, 1);
        responseBodyTextArea.setEnabled(false);

        // Tool scope checkboxes
        toolScopeCheckBoxes = new LinkedHashMap<>();
        for (ToolType toolType : AppModel.RULE_TARGE_TOOL_TYPE) {
            JCheckBox checkBox = new JCheckBox(toolType.toolName(), false);
            checkBox.setEnabled(false);
            toolScopeCheckBoxes.put(toolType, checkBox);
        }

        // Handling scope radio buttons
        requestHandlingBtn = new JRadioButton("Request", true);
        responseHandlingBtn = new JRadioButton("Response");
        requestHandlingBtn.setEnabled(false);
        responseHandlingBtn.setEnabled(false);
        handlingScopeBtnGroup = new ButtonGroup();
        handlingScopeBtnGroup.add(requestHandlingBtn);
        handlingScopeBtnGroup.add(responseHandlingBtn);

        // Buttons
        JButton itemNewBtn = new JButton("New");
        itemSaveBtn = new JButton("Save");
        itemRemoveBtn = new JButton("Remove");
        protoChooseBtn = new JButton("Choose File");
        itemSaveBtn.setEnabled(false);
        itemRemoveBtn.setEnabled(false);
        protoChooseBtn.setEnabled(false);

        // File chooser
        JFileChooser protoChooser = new JFileChooser();
        protoChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        protoChooser.setFileFilter(new FileNameExtensionFilter("Proto Descriptor files (*.desc)", "desc"));

        // Build form panel
        JPanel itemFormPanel = buildFormPanel();

        // Build table
        JTable itemTable = new JTable(itemModel) {
            @Override
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                populateFormFromModel(itemModel.get(rowIndex));
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }
        };

        // Event handlers
        protoChooseBtn.addActionListener(event -> onProtoFileChoose(protoChooser));
        itemNewBtn.addActionListener(event -> onNewItem());
        itemSaveBtn.addActionListener(event -> onSaveItem(itemTable));
        itemRemoveBtn.addActionListener(event -> onRemoveItem(itemTable));

        requestHandlingBtn.addActionListener(event -> responseBodyTextArea.setEnabled(false));
        responseHandlingBtn.addActionListener(event -> responseBodyTextArea.setEnabled(true));

        // Layout
        mainPanel.setLeftComponent(new JScrollPane(itemTable));
        mainPanel.setRightComponent(itemFormPanel);
    }

    public Component getUiComponent() {
        return mainPanel;
    }

    public List<AppModel> getHandlingRules() {
        return itemModel.getAll();
    }

    // --- Form layout ---

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = baseConstraints();

        // Scope
        c.gridy = 1;
        panel.add(createLabel("Scope"), c);
        c.gridx = 1;
        panel.add(scopeTextField, c);

        // Proto file
        c.gridx = 0; c.gridy = 2;
        panel.add(createLabel("Protobuf File"), c);
        c.gridx = 1;
        panel.add(protoChooseBtn, c);
        c.gridx = 2;
        panel.add(selectedProtoPathLabel, c);

        // Message Type
        c.gridx = 0; c.gridy = 3;
        panel.add(createLabel("Message Type"), c);
        c.gridx = 1;
        panel.add(messageTypeComboBox, c);

        // Tool Scope
        c.gridx = 0; c.gridy = 4;
        panel.add(createLabel("Tool Scope"), c);
        JPanel toolScopePanel = new JPanel();
        toolScopeCheckBoxes.values().forEach(toolScopePanel::add);
        c.gridx = 1;
        panel.add(toolScopePanel, c);

        // Handling Scope
        c.gridx = 0; c.gridy = 6;
        panel.add(createLabel("Handling Scope"), c);
        JPanel handlingScopePanel = new JPanel();
        handlingScopePanel.add(requestHandlingBtn);
        handlingScopePanel.add(responseHandlingBtn);
        c.gridx = 1;
        panel.add(handlingScopePanel, c);

        // Response Body
        c.gridx = 0; c.gridy = 8;
        panel.add(createLabel("(Optional) Replaced Response Body"), c);
        c.gridx = 1;
        panel.add(new JScrollPane(responseBodyTextArea), c);

        // Buttons
        c = baseConstraints();
        JPanel btnPanel = new JPanel();
        // itemNewBtn is local, but we need it here — passed via constructor flow
        // Actually, we add it in the constructor. Let's add a placeholder.
        c.gridx = 1;
        panel.add(buildButtonPanel(), c);

        return panel;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        JButton itemNewBtn = new JButton("New");
        itemNewBtn.addActionListener(event -> onNewItem());
        panel.add(itemNewBtn);
        panel.add(itemSaveBtn);
        panel.add(itemRemoveBtn);
        return panel;
    }

    // --- Event handlers ---

    private void onProtoFileChoose(JFileChooser protoChooser) {
        int actionAns = protoChooser.showOpenDialog(null);
        if (actionAns == JFileChooser.APPROVE_OPTION) {
            messageTypeComboBox.removeAllItems();
            String selectedPath = protoChooser.getSelectedFile().getAbsolutePath();
            try {
                List<Descriptor> descriptors = Protobuffer.getMessageTypesFromProtoFile(selectedPath);
                for (Descriptor descriptor : descriptors) {
                    messageTypeComboBox.addItem(descriptor.getName());
                }
            } catch (Exception e) {
                logging.logToError(e);
                logging.logToOutput("Protobuf file の読み込みに失敗しました。File: %s".formatted(selectedPath));
            }
            selectedProtoPathLabel.setText(selectedPath);
        }
    }

    private void onNewItem() {
        AppModel item = new AppModel();
        item.setComment("選択して編集して下さい!!");
        itemModel.add(item);
    }

    private void onSaveItem(JTable itemTable) {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow < 0) { return; }

        if (selectedProtoPathLabel.getText().equals(NO_FILE_SELECTED)) {
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

        // Tool scope の保存
        toolScopeCheckBoxes.forEach((toolType, checkBox) -> {
            if (checkBox.isSelected()) {
                item.setToolScope(toolType.toolName());
            }
        });

        item.setScope(scopeTextField.getText());
        item.setProtoDescPath(selectedProtoPathLabel.getText());
        item.setRequestHandling(requestHandlingBtn.isSelected());
        if (responseHandlingBtn.isSelected()) {
            item.setReplaceResponseBody(responseBodyTextArea.getText());
        }

        try {
            List<Descriptor> descriptors = Protobuffer.getMessageTypesFromProtoFile(selectedProtoPathLabel.getText());
            Object comboBoxObj = messageTypeComboBox.getSelectedItem();
            for (Descriptor descriptor : descriptors) {
                item.setCachedMessageType(descriptor.getName());
                if (descriptor.getName().equals(String.valueOf(comboBoxObj))) {
                    item.setDescriptor(descriptor);
                }
            }
        } catch (Exception e) {
            logging.logToError(e);
            logging.logToOutput("Protobuf file の読み込みに失敗しました。File: %s".formatted(selectedProtoPathLabel.getText()));
        }

        itemModel.fireTableRowsUpdated(selectedRow, selectedRow);
    }

    private void onRemoveItem(JTable itemTable) {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow < 0) { return; }
        itemModel.remove(selectedRow);

        if (selectedRow - 1 >= 0) {
            itemTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
            itemTable.changeSelection(selectedRow - 1, selectedRow - 1, false, false);
        } else if (itemTable.getRowCount() > 0) {
            itemTable.setRowSelectionInterval(0, 0);
            itemTable.changeSelection(0, 0, false, false);
        } else {
            clearForm();
            setFormEnabled(false);
        }
    }

    // --- Form state management ---

    private void populateFormFromModel(AppModel item) {
        // Clear
        messageTypeComboBox.removeAllItems();
        toolScopeCheckBoxes.values().forEach(cb -> cb.setSelected(false));

        // Enable components
        setFormEnabled(true);

        // Populate
        scopeTextField.setText(item.getScope());
        selectedProtoPathLabel.setText(
                item.getProtoDescPath().isEmpty() ? NO_FILE_SELECTED : item.getProtoDescPath());
        responseBodyTextArea.setText(item.getReplaceResponseBody());

        // Message types
        List<String> messageTypes = item.getCachedMessageTypes();
        for (int i = 0; i < messageTypes.size(); i++) {
            messageTypeComboBox.addItem(messageTypes.get(i));
            if (item.getDescriptor() != null && messageTypes.get(i).equals(item.getDescriptor().getName())) {
                messageTypeComboBox.setSelectedIndex(i);
            }
        }

        // Tool scope
        for (String toolName : item.getToolScope()) {
            toolScopeCheckBoxes.forEach((toolType, checkBox) -> {
                if (toolType.toolName().equals(toolName)) {
                    checkBox.setSelected(true);
                }
            });
        }

        // Handling scope
        if (item.isRequestHandling()) {
            handlingScopeBtnGroup.setSelected(requestHandlingBtn.getModel(), true);
            responseBodyTextArea.setEnabled(false);
        } else {
            handlingScopeBtnGroup.setSelected(responseHandlingBtn.getModel(), true);
            responseBodyTextArea.setEnabled(true);
        }
    }

    private void clearForm() {
        messageTypeComboBox.removeAllItems();
        scopeTextField.setText("");
        responseBodyTextArea.setText("");
        selectedProtoPathLabel.setText(NO_FILE_SELECTED);
        toolScopeCheckBoxes.values().forEach(cb -> cb.setSelected(false));
        handlingScopeBtnGroup.setSelected(requestHandlingBtn.getModel(), true);
    }

    private void setFormEnabled(boolean enabled) {
        scopeTextField.setEditable(enabled);
        scopeTextField.setFocusable(enabled);
        responseBodyTextArea.setEnabled(enabled && responseHandlingBtn.isSelected());
        messageTypeComboBox.setEnabled(enabled);
        itemSaveBtn.setEnabled(enabled);
        itemRemoveBtn.setEnabled(enabled);
        protoChooseBtn.setEnabled(enabled);
        requestHandlingBtn.setEnabled(enabled);
        responseHandlingBtn.setEnabled(enabled);
        toolScopeCheckBoxes.values().forEach(cb -> cb.setEnabled(enabled));
    }

    // --- Utility ---

    private static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        return label;
    }

    private static GridBagConstraints baseConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 4, 2, 10);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        return constraints;
    }
}
