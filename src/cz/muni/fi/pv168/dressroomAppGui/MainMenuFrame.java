/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroomAppGui;

import javax.sql.DataSource;
import javax.swing.JOptionPane;
import cz.muni.fi.pv168.dressroommanager.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;

/**
 *
 * @author Vukmir
 */
public class MainMenuFrame extends javax.swing.JFrame {

    private DataSource dataSource;
    private DressroomManagerImpl dressroomManager;
    private ClosetManagerImpl closetManager;
    private ItemsManagerImpl itemsManager;
    private Closet currentCloset;
    private Long updateId;
    private boolean updateC;
    private boolean updateI;
    
    
    
    public MainMenuFrame() {
        initComponents();
        new AllItemsFromClosetSwingWorker().execute();
        new AllClosetsSwingWorker().execute();
    }
    
    public static DataSource prepareDataSource() throws SQLException {
        BasicDataSource dataSource = new BasicDataSource();
        //dataSource.setUrl("jdbc:derby:memory:dressroom-gui;create=true");
        dataSource.setUrl(java.util.ResourceBundle.getBundle("cz.muni.fi.pv168.dressroomappgui/settings").getString("url"));
        dataSource.setUsername(java.util.ResourceBundle.getBundle("cz.muni.fi.pv168.dressroomappgui/settings").getString("user"));
        dataSource.setPassword(java.util.ResourceBundle.getBundle("cz.muni.fi.pv168.dressroomappgui/settings").getString("password"));
        return dataSource;
    }
    
    ///////////////////////////////////*********************************///////////////////////////////////
    private AddItemSwingWorker addItemSwingWorker;
    private class AddItemSwingWorker extends SwingWorker<Item, Void> {
        private String type;
        private String size;
        private String genderString;
        private Gender gender;
        private String note;

        private Item item;

        public void setType(String type) {
            this.type = type;
        }

        public void setSize(String size) {
            this.size = size;
        }
        
        public void setGender(String gender) {
            this.size = gender;
        }
        
        public void setNote(String note) {
            this.note = note;
        }

        public void setItem(Item item) {
            this.item = item;
        }

        @Override
        protected Item doInBackground() throws Exception {

            dataSource = prepareDataSource();
            itemsManager = new ItemsManagerImpl();
            itemsManager.setDataSource(dataSource);
            dressroomManager = new DressroomManagerImpl();
            dressroomManager.setDataSource(dataSource);
            gender = Gender.BOTH;                   ///////////*******************FIX
            item = new Item(type, gender, size, note);
            itemsManager.createItem(item);
            dressroomManager.putItemInCloset(item, currentCloset);
            return item;
        }

        @Override
        protected void done() {
            ItemsTableModel model = new ItemsTableModel();
            model.addItem(item);
            
            itemsTable.setModel(model);
        }
    }
    

     ///////////////////////////////////*********************************///////////////////////////////////
    private DeleteClosetSwingWorker deleteClosetSwingWorker;
    private class DeleteClosetSwingWorker extends SwingWorker<Closet, Void> {
        private Closet deletedCloset;

        @Override
        protected Closet doInBackground() throws Exception {

            dataSource = prepareDataSource();
            closetManager = new ClosetManagerImpl();
            closetManager.setDataSource(dataSource);
            deletedCloset = (Closet)closetsComboBox.getSelectedItem();
                closetManager.deleteCloset(deletedCloset);
            return deletedCloset;
        }

        @Override
        protected void done() {
            ClosetsComboBoxModel model = new ClosetsComboBoxModel(closetManager.getAllClosets());
            model.removeCloset(deletedCloset);
            System.out.println("deleted: " + deletedCloset);
            closetsComboBox.setModel(model);
        }
    }
    
    ///////////////////////////////////*********************************///////////////////////////////////
    private AddClosetSwingWorker addClosetSwingWorker;
    private class AddClosetSwingWorker extends SwingWorker<Closet, Void> {
        private String name;
        private String owner;
        private Closet closet;

        public void setName(String name) {
            this.name = name;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public void setCloset(Closet closet) {
            this.closet = closet;
        }

        @Override
        protected Closet doInBackground() throws Exception {

            dataSource = prepareDataSource();
            closetManager = new ClosetManagerImpl();
            closetManager.setDataSource(dataSource);
            closet = new Closet(name, owner);
            if(!updateC)
                closetManager.createCloset(closet);
            else
                closetManager.updateCloset(closet);
            return closet;
        }

        @Override
        protected void done() {
            ClosetsComboBoxModel model = new ClosetsComboBoxModel(closetManager.getAllClosets());
            model.addCloset(closet);
            
            closetsComboBox.setModel(model);
        }
    }
    ///////////////////////////////////*********************************///////////////////////////////////
    public AllClosetsSwingWorker allClosetsSwingWorker;
    public class AllClosetsSwingWorker extends SwingWorker<List<Closet>, Void> {

        private List<Closet> closets;

        @Override
        protected List<Closet> doInBackground() throws Exception {
            dataSource = prepareDataSource();
            closetManager = new ClosetManagerImpl();
            closetManager.setDataSource(dataSource);
            closets = new ArrayList<>();
            try{
                closets = closetManager.getAllClosets();
            }catch(Exception e){
                System.out.println("no closets available");
                Closet closet = new Closet("XX","YY");
                closets.add(closet);
            }
            return closets;
        }

        @Override
        protected void done() {
            List<Closet> closets = new ArrayList<Closet>();
            try{
                closets.addAll(closetManager.getAllClosets());
            }catch(Exception e){
                closets.add(new Closet("DD","FF"));
                closets.add(new Closet("PP","LL"));
            }
            ClosetsComboBoxModel model = new ClosetsComboBoxModel(closets);
            closetsComboBox.setModel(model);
            //ClosetsComboBoxModel model = (ClosetsComboBoxModel) closetsComboBox.getModel();

        }
    }
    
    
    ///////////////////////////////////*********************************///////////////////////////////////
    private class AllItemsFromClosetSwingWorker extends SwingWorker<List<Item>, Void> {

        private List<Item> items;

        @Override
        protected List<Item> doInBackground() throws Exception {
            dataSource = prepareDataSource();
            dressroomManager = new DressroomManagerImpl();
            dressroomManager.setDataSource(dataSource);
            items = new ArrayList<>();
            currentCloset = (Closet)closetsComboBox.getSelectedItem();
            System.out.println(" current = " + currentCloset);
            
            try{
                items = dressroomManager.getAllItemsFromCloset(currentCloset);
            }catch(Exception e){
                System.out.println("no closet selected");
                items.add(new Item("xx",Gender.BOTH,"S","XX"));
                items.add(new Item("jj",Gender.BOTH,"J","DD"));
            }
            return items;
        }

        @Override
        protected void done() {

            ItemsTableModel model = (ItemsTableModel) itemsTable.getModel();
            try {
                model.removeAllItem();
                for (Item item : get()) {
                    model.addItem(item);
                }
            } catch (ExecutionException ex) {
                Logger.getLogger(MainMenuFrame.class.getName()).log(Level.SEVERE, "null!", ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(MainMenuFrame.class.getName()).log(Level.SEVERE, "null!!", ex);
            }
        }
    }
    
    
    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        newClosetFrame = new javax.swing.JFrame();
        closetOwnerTextField = new javax.swing.JTextField();
        newClosetLabel = new javax.swing.JLabel();
        closetOwnerLabel = new javax.swing.JLabel();
        closetNameLabel = new javax.swing.JLabel();
        closetNameTextField = new javax.swing.JTextField();
        addClosetButton = new javax.swing.JButton();
        newItemFrame = new javax.swing.JFrame();
        newItemLabel = new javax.swing.JLabel();
        itemTypeLabel = new javax.swing.JLabel();
        itemTypeTextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        itemGenderTextField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        itemSizeTextField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        itemNoteTextField = new javax.swing.JTextField();
        addItemButton = new javax.swing.JButton();
        updateClosetFrame = new javax.swing.JFrame();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        updateClosetOwnerTextField = new javax.swing.JTextField();
        updateClosetNameTextField = new javax.swing.JTextField();
        updateClosetConfirmButton = new javax.swing.JButton();
        updateItemFrame = new javax.swing.JFrame();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jTextField12 = new javax.swing.JTextField();
        jButton10 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        closetsComboBox = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        newItemButton = new javax.swing.JButton();
        updateItemButton = new javax.swing.JButton();
        deleteItemButton = new javax.swing.JButton();
        updateClosetButton = new javax.swing.JButton();
        deleteClosetButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        itemsTable = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();

        closetOwnerTextField.setText("jTextField1");
        closetOwnerTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closetOwnerTextFieldActionPerformed(evt);
            }
        });

        newClosetLabel.setText("New Closet");

        closetOwnerLabel.setText("closet owner");

        closetNameLabel.setText("closet name");

        closetNameTextField.setText("jTextField1");
        closetNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closetNameTextFieldActionPerformed(evt);
            }
        });

        addClosetButton.setText("ADD CLOSET");
        addClosetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addClosetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout newClosetFrameLayout = new javax.swing.GroupLayout(newClosetFrame.getContentPane());
        newClosetFrame.getContentPane().setLayout(newClosetFrameLayout);
        newClosetFrameLayout.setHorizontalGroup(
            newClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newClosetFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(newClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addClosetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, newClosetFrameLayout.createSequentialGroup()
                        .addComponent(closetOwnerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(closetOwnerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(newClosetLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(newClosetFrameLayout.createSequentialGroup()
                        .addComponent(closetNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(closetNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        newClosetFrameLayout.setVerticalGroup(
            newClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newClosetFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(newClosetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(newClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(closetOwnerTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                    .addComponent(closetOwnerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(newClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(closetNameTextField)
                    .addComponent(closetNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(addClosetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        newItemLabel.setText("New Item");

        itemTypeLabel.setText("type");

        itemTypeTextField.setText("jTextField3");

        jLabel9.setText("gender");

        itemGenderTextField.setText("jTextField3");

        jLabel10.setText("size");

        itemSizeTextField.setText("jTextField3");

        jLabel11.setText("note");

        itemNoteTextField.setText("jTextField3");

        addItemButton.setText("ADD ITEM");
        addItemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addItemButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout newItemFrameLayout = new javax.swing.GroupLayout(newItemFrame.getContentPane());
        newItemFrame.getContentPane().setLayout(newItemFrameLayout);
        newItemFrameLayout.setHorizontalGroup(
            newItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newItemFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(newItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addItemButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(newItemFrameLayout.createSequentialGroup()
                        .addComponent(itemTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(itemTypeTextField))
                    .addComponent(newItemLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, newItemFrameLayout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(itemNoteTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, newItemFrameLayout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(itemSizeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, newItemFrameLayout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(itemGenderTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)))
                .addContainerGap())
        );
        newItemFrameLayout.setVerticalGroup(
            newItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newItemFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(newItemLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(newItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(itemTypeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(itemTypeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(newItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(itemGenderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(newItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(itemSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(newItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(itemNoteTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(addItemButton, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel12.setText("Update Closet");

        jLabel13.setText("owner");

        jLabel14.setText("name");

        updateClosetOwnerTextField.setText("jTextField7");

        updateClosetNameTextField.setText("jTextField7");

        updateClosetConfirmButton.setText("UPDATE CLOSET");
        updateClosetConfirmButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateClosetConfirmButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout updateClosetFrameLayout = new javax.swing.GroupLayout(updateClosetFrame.getContentPane());
        updateClosetFrame.getContentPane().setLayout(updateClosetFrameLayout);
        updateClosetFrameLayout.setHorizontalGroup(
            updateClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updateClosetFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(updateClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(updateClosetFrameLayout.createSequentialGroup()
                        .addGroup(updateClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(updateClosetConfirmButton, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(updateClosetFrameLayout.createSequentialGroup()
                                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(updateClosetNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(updateClosetFrameLayout.createSequentialGroup()
                                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(updateClosetOwnerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 10, Short.MAX_VALUE)))
                .addContainerGap())
        );
        updateClosetFrameLayout.setVerticalGroup(
            updateClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updateClosetFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(updateClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(updateClosetOwnerTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(updateClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateClosetNameTextField))
                .addGap(18, 18, 18)
                .addComponent(updateClosetConfirmButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jLabel15.setText("Update Item");

        jLabel16.setText("type");

        jTextField9.setText("jTextField9");

        jLabel17.setText("gender");

        jTextField10.setText("jTextField9");

        jLabel18.setText("size");

        jTextField11.setText("jTextField9");

        jLabel19.setText("note");

        jTextField12.setText("jTextField9");

        jButton10.setText("jButton10");

        javax.swing.GroupLayout updateItemFrameLayout = new javax.swing.GroupLayout(updateItemFrame.getContentPane());
        updateItemFrame.getContentPane().setLayout(updateItemFrameLayout);
        updateItemFrameLayout.setHorizontalGroup(
            updateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updateItemFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(updateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(updateItemFrameLayout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(jTextField9))
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(updateItemFrameLayout.createSequentialGroup()
                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(jTextField10, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, updateItemFrameLayout.createSequentialGroup()
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(jTextField11, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE))
                    .addGroup(updateItemFrameLayout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(jTextField12, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)))
                .addContainerGap())
        );
        updateItemFrameLayout.setVerticalGroup(
            updateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updateItemFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(updateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField9, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(updateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(updateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(updateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setResizable(false);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Dressroom");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        closetsComboBox.setName(""); // NOI18N
        closetsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closetsComboBoxActionPerformed(evt);
            }
        });

        jButton1.setText("New Closet");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newClosetButtonActionPerformed(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Closet content");
        jLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        newItemButton.setText("New Item");
        newItemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newItemButtonActionPerformed(evt);
            }
        });

        updateItemButton.setText("Update Item");
        updateItemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateItemButtonActionPerformed(evt);
            }
        });

        deleteItemButton.setText("Delete Item");
        deleteItemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteItemButtonActionPerformed(evt);
            }
        });

        updateClosetButton.setText("Update Closet");
        updateClosetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateClosetActionPerformed(evt);
            }
        });

        deleteClosetButton.setText("Delete Closet");
        deleteClosetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteClosetActionPerformed(evt);
            }
        });

        itemsTable.setModel(new ItemsTableModel());
        jScrollPane4.setViewportView(itemsTable);

        jLabel3.setText("Choose your Closet:");

        refreshButton.setText("REFRESH");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newItemButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(updateItemButton)
                        .addGap(37, 37, 37)
                        .addComponent(deleteItemButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 371, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(closetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(updateClosetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(deleteClosetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(150, 150, 150)
                                .addComponent(refreshButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(refreshButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(updateClosetButton)
                    .addComponent(deleteClosetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(updateItemButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(deleteItemButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(newItemButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closetsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closetsComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_closetsComboBoxActionPerformed

    private void newClosetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newClosetButtonActionPerformed
        newClosetFrame.setSize(400, 400);
        newClosetFrame.setLocationRelativeTo(null);
        closetOwnerTextField.setText("");
        closetNameTextField.setText("");
        
        newClosetFrame.setVisible(true);
    }//GEN-LAST:event_newClosetButtonActionPerformed

    private void newItemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newItemButtonActionPerformed
        newItemFrame.setSize(400, 400);
        newItemFrame.setLocationRelativeTo(null);
        itemNoteTextField.setText("");
        itemGenderTextField.setText("");
        itemTypeTextField.setText("");
        itemSizeTextField.setText("");
        
        newItemFrame.setVisible(true);
    }//GEN-LAST:event_newItemButtonActionPerformed

    private void deleteClosetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteClosetActionPerformed
       
       deleteClosetSwingWorker = new DeleteClosetSwingWorker();
       deleteClosetSwingWorker.execute();
       
       new AllClosetsSwingWorker().execute();
    }//GEN-LAST:event_deleteClosetActionPerformed

    private void updateItemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateItemButtonActionPerformed
        int selectedRow = itemsTable.getSelectedRow();
        updateClosetFrame.setSize(400, 400);
        updateClosetFrame.setLocationRelativeTo(null);

        Object idValue = itemsTable.getValueAt(selectedRow, 0);
        Closet closet = null;
        //try {
        closet = closetManager.getClosetById((Long) idValue);
        /*} catch (SQLException ex) {
            Logger.getLogger(MainMenuFrame.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        updateId = closet.getId();
        closetNameTextField.setText(closet.getName());
        closetOwnerTextField.setText(closet.getOwner());
        updateItemFrame.setVisible(true);
    }//GEN-LAST:event_updateItemButtonActionPerformed

    private void deleteItemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteItemButtonActionPerformed
        int selectedRow = itemsTable.getSelectedRow();
        try{
             dataSource = prepareDataSource();
        } catch(Exception e)
        {
            System.out.println("no datasource set");
        }
        itemsManager = new ItemsManagerImpl();
        itemsManager.setDataSource(dataSource);
        Object idValue = itemsTable.getValueAt(selectedRow, 0);
        Item item = null;
        
        System.out.println();
        try {
            item = itemsManager.getItemById((Long) idValue);
        } catch (Exception ex) {
            Logger.getLogger(MainMenuFrame.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
        //Item item = itemsManager.getItemById((Long) idValue);
        boolean remove = false;
        if (selectedRow == -1) {
            return;
        }
        remove = true;
        for (Item i : dressroomManager.getAllItemsFromCloset(currentCloset)) {
            if (i.equals(item)) {
                remove = false;
                break;
            }
        }
        dressroomManager.removeItemFromCloset(item, currentCloset);
        
        
        new AllItemsFromClosetSwingWorker().execute();
        
    }//GEN-LAST:event_deleteItemButtonActionPerformed

    private void updateClosetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateClosetActionPerformed
        
        updateClosetFrame.setSize(400, 400);
        updateClosetFrame.setLocationRelativeTo(null);

        Closet closet = null;
        //try {
        closet = (Closet)closetsComboBox.getSelectedItem();
        
        updateClosetNameTextField.setText(closet.getName());
        updateClosetOwnerTextField.setText(closet.getOwner());
        updateClosetFrame.setVisible(true);
    }//GEN-LAST:event_updateClosetActionPerformed

    private void closetOwnerTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closetOwnerTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_closetOwnerTextFieldActionPerformed

    private void closetNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closetNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_closetNameTextFieldActionPerformed

    private void addClosetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClosetButtonActionPerformed
        String name = closetNameTextField.getText();
        String owner = closetNameTextField.getText();
        
        
       addClosetSwingWorker = new AddClosetSwingWorker();
       addClosetSwingWorker.setName(name);
       addClosetSwingWorker.setOwner(owner);
       addClosetSwingWorker.execute();
       newClosetFrame.dispose();
       
       new AllClosetsSwingWorker().execute();
    }//GEN-LAST:event_addClosetButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        new AllItemsFromClosetSwingWorker().execute();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void addItemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addItemButtonActionPerformed
        String type = itemTypeTextField.getText();
        String size = itemSizeTextField.getText();
        String gender = itemGenderTextField.getText();
        String note = itemNoteTextField.getText();
        
        
       addItemSwingWorker = new AddItemSwingWorker();
       addItemSwingWorker.setType(type);
       addItemSwingWorker.setSize(size);
       addItemSwingWorker.setGender(gender);
       addItemSwingWorker.setNote(note);

       addItemSwingWorker.execute();
       newItemFrame.dispose();
       
       new AllItemsFromClosetSwingWorker().execute();
    }//GEN-LAST:event_addItemButtonActionPerformed

    private void updateClosetConfirmButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateClosetConfirmButtonActionPerformed
       String name = updateClosetNameTextField.getText();
       String owner = updateClosetOwnerTextField.getText();
        
       updateC = true;
       addClosetSwingWorker = new AddClosetSwingWorker();
       addClosetSwingWorker.setName(name);
       addClosetSwingWorker.setOwner(owner);
       addClosetSwingWorker.execute();
       newClosetFrame.dispose();
       
       new AllClosetsSwingWorker().execute();
        
        
        
    }//GEN-LAST:event_updateClosetConfirmButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainMenuFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainMenuFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainMenuFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainMenuFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainMenuFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addClosetButton;
    private javax.swing.JButton addItemButton;
    private javax.swing.JLabel closetNameLabel;
    private javax.swing.JTextField closetNameTextField;
    private javax.swing.JLabel closetOwnerLabel;
    private javax.swing.JTextField closetOwnerTextField;
    public static javax.swing.JComboBox closetsComboBox;
    private javax.swing.JButton deleteClosetButton;
    private javax.swing.JButton deleteItemButton;
    private javax.swing.JTextField itemGenderTextField;
    private javax.swing.JTextField itemNoteTextField;
    private javax.swing.JTextField itemSizeTextField;
    private javax.swing.JLabel itemTypeLabel;
    private javax.swing.JTextField itemTypeTextField;
    private javax.swing.JTable itemsTable;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JFrame newClosetFrame;
    private javax.swing.JLabel newClosetLabel;
    private javax.swing.JButton newItemButton;
    private javax.swing.JFrame newItemFrame;
    private javax.swing.JLabel newItemLabel;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton updateClosetButton;
    private javax.swing.JButton updateClosetConfirmButton;
    private javax.swing.JFrame updateClosetFrame;
    private javax.swing.JTextField updateClosetNameTextField;
    private javax.swing.JTextField updateClosetOwnerTextField;
    private javax.swing.JButton updateItemButton;
    private javax.swing.JFrame updateItemFrame;
    // End of variables declaration//GEN-END:variables
}
