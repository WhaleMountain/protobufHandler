package protobufhandler.view.ui;

import javax.swing.table.AbstractTableModel;

import protobufhandler.model.AppModel;

import java.util.List;
import java.util.ArrayList;

public class AppTableModel extends AbstractTableModel {
    private final String[] columns = {"Enabled", "Scope", "Replace Scope", "Tool Scope", "Message Type", "File",  "Comment"};
    private final List<AppModel> items;

    public AppTableModel() {
        this.items = new ArrayList<>();
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public synchronized int getRowCount()
    {
        return items.size();
    }

    @Override
    public int getColumnCount() { 
        return columns.length;
    }

    @Override
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
        AppModel item = items.get(rowIndex);

        String messageType = "";
        if(item.getDescriptor() != null) {
            messageType = item.getDescriptor().getName();
        }

        String replaceScope = "Response";
        if(item.isRequestHandling()) {
            replaceScope = "Request";
        }

        return switch (columnIndex)
                {
                    case 0 -> item.isEnabled();
                    case 1 -> item.getScope();
                    case 2 -> replaceScope;
                    case 3 -> String.join(", ", item.getToolScope());
                    case 4 -> messageType;
                    case 5 -> item.getProtoDescPath();
                    case 6 -> item.getComment();
                    default -> "";
                };
    }

    @Override
    public synchronized void setValueAt(Object aValue, int row, int column) {
        AppModel item = items.get(row);

        switch (column) {
            case 0:
                item.setEnabled(!item.isEnabled());
                break;

            case 6:
                item.setComment((String)aValue);
                break;
        
            default:
                break;
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) { 
        switch (column) {
            case 0:
                return true;

            case 6:
                return true;
        
            default:
                return false;
        }
    }

    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return Boolean.class;
        
            default:
                return String.class;
        }
    }

    public synchronized void add(AppModel item) {
        int index = items.size();
        items.add(item);
        fireTableRowsInserted(index, index);
    }

    public synchronized void remove(int rowIndex) {
        items.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public synchronized AppModel get(int rowIndex) {
        return items.get(rowIndex);
    }

    public synchronized List<AppModel> getAll() {
        return items;
    }
}
