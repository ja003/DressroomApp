/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.dressroomAppGui;

import cz.muni.fi.pv168.dressroommanager.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

    Locale local = Locale.getDefault();
    
    String localeDirectory = "cz.muni.fi.pv168.dressroomAppGui.localization_" + local;
    
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return java.util.ResourceBundle.getBundle(localeDirectory).getString("columnId");
            case 1:
                return java.util.ResourceBundle.getBundle(localeDirectory).getString("columnType");
            case 2:
                return java.util.ResourceBundle.getBundle(localeDirectory).getString("columnAdded");
            case 3:
                return java.util.ResourceBundle.getBundle(localeDirectory).getString("columnGender");
            case 4:
                return java.util.ResourceBundle.getBundle(localeDirectory).getString("columnSize");
            case 5:
                return java.util.ResourceBundle.getBundle(localeDirectory).getString("columnNote");
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
}