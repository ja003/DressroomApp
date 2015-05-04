/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.dressroomAppGui;

import cz.muni.fi.pv168.dressroommanager.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class ItemsTableModel extends AbstractTableModel {

    private List<Item> items = new ArrayList<>();

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Item item = items.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return item.getId();
            case 1:
                return item.getType();
            case 2:
                return item.getAdded();
            case 3:
                return item.getGender();
            case 4:
                return item.getSize();
            case 5:
                return item.getNote();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Long.class;
            case 1:
                return String.class;
            case 2:
                return Date.class;
            case 3:
                return Gender.class;
            case 4:
                return String.class;
            case 5:
                return String.class;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

   public void addItem(Item item) {
        items.add(item);
        int lastRow = items.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }
    
    public Item getItem(int rowIndex){
        Item item = items.get(rowIndex);
        return item;
    }
    
    public void removeItem(Item item) {
        items.remove(item);
        int lastRow = items.size() - 1;
        fireTableRowsDeleted(lastRow, lastRow);
    }
    
    public void removeAllItem(){
         items.clear();
         fireTableDataChanged();
    }


    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Item item = items.get(rowIndex);
        switch (columnIndex) {
            case 0:
                item.setId((Long) aValue);
                break;
            case 1:
                item.setType((String) aValue);
                break;
            case 2:
                item.setAdded((java.sql.Date) aValue);
                break;
            case 3:
                item.setGender((Gender) aValue);
                break;
            case 4:
                item.setSize((String) aValue);
                break;
            case 5:
                item.setNote((String) aValue);
                break;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "id";
                //return java.util.ResourceBundle.getBundle("cz.muni.fi.pv168.dressroomappgui/localization").getString("id");
            case 1:
                return "type";
                //return java.util.ResourceBundle.getBundle("cz.muni.fi.pv168.dressroomappgui/localization").getString("type");
            case 2:
                return "added";
                //return java.util.ResourceBundle.getBundle("cz.muni.fi.pv168.dressroomappgui/localization").getString("added");
            case 3:
                return "gender";
                //return java.util.ResourceBundle.getBundle("cz.muni.fi.pv168.dressroomappgui/localization").getString("gender");
            case 4:
                return "size";
                //return java.util.ResourceBundle.getBundle("cz.muni.fi.pv168.dressroomappgui/localization").getString("size");
            case 5:
                return "note";
                //return java.util.ResourceBundle.getBundle("cz.muni.fi.pv168.dressroomappgui/localization").getString("note");
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
}