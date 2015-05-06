/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroomAppGui;

import cz.muni.fi.pv168.common.ServiceFailureException;
import cz.muni.fi.pv168.dressroommanager.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.apache.commons.dbcp.BasicDataSource;

/**
 *
 * @author Vukmir
 */
public class MainMenuFrame extends javax.swing.JFrame {

    private DataSource dataSource;
    private DressroomManagerImpl dressroomManager;
    private ClosetManagerImpl closetManager;
    private ItemsManagerImpl itemsManager = new ItemsManagerImpl();
    private Closet currentCloset;
    private Long updateId;
    private boolean updateC;
    private boolean updateI;
    
    
    
    public MainMenuFrame() {
        initComponents();
        
        new AllClosetsSwingWorker().execute();
        new AllItemsFromClosetSwingWorker().execute();
        
        
        refreshButton.doClick();
        System.out.println("RERESH");
    }
    
    public static DataSource prepareDataSource() throws SQLException {
        BasicDataSource dataSource = new BasicDataSource();
        //dataSource.setUrl("jdbc:derby:memory:dressroom-gui;create=true");
        dataSource.setUrl(java.util.ResourceBundle.getBundle("cz.muni.fi.pv168.dressroomappgui/settings").getString("url"));
        dataSource.setUsername(java.util.ResourceBundle.getBundle("cz.muni.fi.pv168.dressroomappgui/settings").getString("user"));
        dataSource.setPassword(java.util.ResourceBundle.getBundle("cz.muni.fi.pv168.dressroomappgui/settings").getString("password"));
        return dataSource;
    }
    
    public java.sql.Date getSqlDate(java.util.Date date, int daysMove) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, daysMove);

        return new java.sql.Date(cal.getTime().getTime());
    }
    
    ///////////////////////////////////**************AddItemSwingWorker*******************///////////////////////////////////
    private AddItemSwingWorker addItemSwingWorker;
    private class AddItemSwingWorker extends SwingWorker<Item, Void> {
        private String type;
        private String size;
        private Date added;
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
        
        public void setAdded(Date added){
            this.added = added;
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
            
            if(!updateI)
                itemsManager.createItem(item);
            else{
                item.setId(updateId);
                item.setAdded(added);
                itemsManager.updateItem(item);                
            }
            
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
    

     ///////////////////////////////////*************DeleteClosetSwingWorker********************///////////////////////////////////
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
    
    ///////////////////////////////////**************AddClosetSwingWorker*******************///////////////////////////////////
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
                closet.setId(updateId);
                closetManager.updateCloset(closet);
            return closet;
        }

        @Override
        protected void done() {
            ClosetsComboBoxModel model = new ClosetsComboBoxModel(closetManager.getAllClosets());
            try {
                if(!updateC)
                    model.addCloset(get());
                
            } catch (Exception ex) {
                Logger.getLogger(MainMenuFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            closetsComboBox.setModel(model);
        }
    }
    ///////////////////////////////////**************AllClosetsSwingWorker*******************///////////////////////////////////
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
            //set default Closet selection
            ClosetsComboBoxModel model = new ClosetsComboBoxModel(closets);
            closetsComboBox.setModel(model);
            if(model.getSelectedItem() == null && model.getElementAt(0) != null)
                model.setSelectedItem(model.getElementAt(0));
            //ClosetsComboBoxModel model = (ClosetsComboBoxModel) closetsComboBox.getModel();

        }
    }
    
    ///////////////////////////////////***************AllItemsFromClosetSwingWorker******************///////////////////////////////////
    private AllItemsFromClosetSwingWorker allItemsFromClosetSwingWorker = new AllItemsFromClosetSwingWorker();
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
                //items.add(new Item("xx",Gender.BOTH,"S","XX"));
                //items.add(new Item("jj",Gender.BOTH,"J","DD"));
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

        newUpdateClosetFrame = new javax.swing.JFrame();
        closetOwnerTextField = new javax.swing.JTextField();
        newUpdateClosetLabel = new javax.swing.JLabel();
        closetOwnerLabel = new javax.swing.JLabel();
        closetNameLabel = new javax.swing.JLabel();
        closetNameTextField = new javax.swing.JTextField();
        addUpdateClosetButton = new javax.swing.JButton();
        newUpdateItemFrame = new javax.swing.JFrame();
        newUpdateItemLabel = new javax.swing.JLabel();
        itemTypeLabel = new javax.swing.JLabel();
        itemTypeTextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        itemGenderTextField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        itemSizeTextField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        itemNoteTextField = new javax.swing.JTextField();
        addUpdateItemButton = new javax.swing.JButton();
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

        newUpdateClosetLabel.setText("New Closet");

        closetOwnerLabel.setText("closet owner");

        closetNameLabel.setText("closet name");

        closetNameTextField.setText("jTextField1");
        closetNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closetNameTextFieldActionPerformed(evt);
            }
        });

        addUpdateClosetButton.setText("ADD CLOSET");
        addUpdateClosetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addUpdateClosetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout newUpdateClosetFrameLayout = new javax.swing.GroupLayout(newUpdateClosetFrame.getContentPane());
        newUpdateClosetFrame.getContentPane().setLayout(newUpdateClosetFrameLayout);
        newUpdateClosetFrameLayout.setHorizontalGroup(
            newUpdateClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newUpdateClosetFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(newUpdateClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addUpdateClosetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, newUpdateClosetFrameLayout.createSequentialGroup()
                        .addComponent(closetOwnerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(closetOwnerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(newUpdateClosetLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(newUpdateClosetFrameLayout.createSequentialGroup()
                        .addComponent(closetNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(closetNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        newUpdateClosetFrameLayout.setVerticalGroup(
            newUpdateClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newUpdateClosetFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(newUpdateClosetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(newUpdateClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(closetOwnerTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                    .addComponent(closetOwnerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(newUpdateClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(closetNameTextField)
                    .addComponent(closetNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(addUpdateClosetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        newUpdateItemLabel.setText("New Item");

        itemTypeLabel.setText("type");

        itemTypeTextField.setText("jTextField3");

        jLabel9.setText("gender");

        itemGenderTextField.setText("jTextField3");

        jLabel10.setText("size");

        itemSizeTextField.setText("jTextField3");

        jLabel11.setText("note");

        itemNoteTextField.setText("jTextField3");

        addUpdateItemButton.setText("ADD ITEM");
        addUpdateItemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addUpdateItemButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout newUpdateItemFrameLayout = new javax.swing.GroupLayout(newUpdateItemFrame.getContentPane());
        newUpdateItemFrame.getContentPane().setLayout(newUpdateItemFrameLayout);
        newUpdateItemFrameLayout.setHorizontalGroup(
            newUpdateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newUpdateItemFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(newUpdateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addUpdateItemButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(newUpdateItemFrameLayout.createSequentialGroup()
                        .addComponent(itemTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(itemTypeTextField))
                    .addComponent(newUpdateItemLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, newUpdateItemFrameLayout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(itemNoteTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, newUpdateItemFrameLayout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(itemSizeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, newUpdateItemFrameLayout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(itemGenderTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)))
                .addContainerGap())
        );
        newUpdateItemFrameLayout.setVerticalGroup(
            newUpdateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newUpdateItemFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(newUpdateItemLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(newUpdateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(itemTypeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(itemTypeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(newUpdateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(itemGenderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(newUpdateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(itemSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(newUpdateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(itemNoteTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(addUpdateItemButton, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
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
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(closetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(updateClosetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteClosetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
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
        newUpdateClosetFrame.setSize(400, 400);
        newUpdateClosetFrame.setLocationRelativeTo(null);
        updateC = false;
        newUpdateClosetLabel.setText("New Closet");
        
        closetOwnerTextField.setText("");
        closetNameTextField.setText("");
        
        newUpdateClosetFrame.setVisible(true);
    }//GEN-LAST:event_newClosetButtonActionPerformed

    private void newItemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newItemButtonActionPerformed
        newUpdateItemFrame.setSize(400, 400);
        newUpdateItemFrame.setLocationRelativeTo(null);
        updateI = false;
        newUpdateItemLabel.setText("New Item");
        addUpdateItemButton.setText("ADD ITEM");
        
        itemNoteTextField.setText("");
        itemGenderTextField.setText("");
        itemTypeTextField.setText("");
        itemSizeTextField.setText("");
        
        newUpdateItemFrame.setVisible(true);
    }//GEN-LAST:event_newItemButtonActionPerformed

    private void deleteClosetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteClosetActionPerformed
       
       deleteClosetSwingWorker = new DeleteClosetSwingWorker();
       deleteClosetSwingWorker.execute();
       
       new AllClosetsSwingWorker().execute();
    }//GEN-LAST:event_deleteClosetActionPerformed

    private void updateItemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateItemButtonActionPerformed
        
        int selectedRow = itemsTable.getSelectedRow();
        newUpdateItemFrame.setSize(400, 400);
        newUpdateItemFrame.setLocationRelativeTo(null);
        updateI = true;
        newUpdateItemLabel.setText("Update Item");
        addUpdateItemButton.setText("UPDATE ITEM");

        itemsManager = new ItemsManagerImpl();
        Object idValue = itemsTable.getValueAt(selectedRow, 0);
        Item item = null;
        try{
        dataSource = prepareDataSource();
        }catch(Exception e){
        }
        itemsManager.setDataSource(dataSource);
        
        item = itemsManager.getItemById((Long) idValue);
        
        updateId = item.getId();
        itemTypeTextField.setText(item.getType());
        itemSizeTextField.setText(item.getSize());
        itemGenderTextField.setText(item.getGender().toString());
        itemNoteTextField.setText(item.getNote());
        newUpdateItemFrame.setVisible(true);
        
        
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
        
        newUpdateClosetFrame.setSize(400, 400);
        newUpdateClosetFrame.setLocationRelativeTo(null);

        updateC = true;
        newUpdateClosetLabel.setText("Update closet");
        
        Closet closet = null;
        //try {
        closet = (Closet)closetsComboBox.getSelectedItem();
        updateId = closet.getId();
        
        
        closetNameTextField.setText(closet.getName());
        closetOwnerTextField.setText(closet.getOwner());
        newUpdateClosetFrame.setVisible(true);
        
        /*updateClosetFrame.setSize(400, 400);
        updateClosetFrame.setLocationRelativeTo(null);

        Closet closet = null;
        //try {
        closet = (Closet)closetsComboBox.getSelectedItem();
        updateId = closet.getId();
        
        updateClosetNameTextField.setText(closet.getName());
        updateClosetOwnerTextField.setText(closet.getOwner());
        updateClosetFrame.setVisible(true);*/
    }//GEN-LAST:event_updateClosetActionPerformed

    private void closetOwnerTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closetOwnerTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_closetOwnerTextFieldActionPerformed

    private void closetNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closetNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_closetNameTextFieldActionPerformed

    private void addUpdateClosetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addUpdateClosetButtonActionPerformed
        String name = closetNameTextField.getText();
        String owner = closetNameTextField.getText();
        
        
       addClosetSwingWorker = new AddClosetSwingWorker();
       addClosetSwingWorker.setName(name);
       addClosetSwingWorker.setOwner(owner);
       addClosetSwingWorker.execute();
       newUpdateClosetFrame.dispose();
       
       new AllClosetsSwingWorker().execute();
    }//GEN-LAST:event_addUpdateClosetButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        new AllItemsFromClosetSwingWorker().execute();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void addUpdateItemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addUpdateItemButtonActionPerformed
        Date added = getSqlDate(Calendar.getInstance().getTime(), 0);
        String type = itemTypeTextField.getText();
        String size = itemSizeTextField.getText();
        String gender = itemGenderTextField.getText();
        String note = itemNoteTextField.getText();
        
        
       addItemSwingWorker = new AddItemSwingWorker();
       addItemSwingWorker.setType(type);
       addItemSwingWorker.setSize(size);
       addItemSwingWorker.setGender(gender);
       addItemSwingWorker.setNote(note);
       addItemSwingWorker.setAdded(added);

       addItemSwingWorker.execute();
       newUpdateItemFrame.dispose();
       
       new AllItemsFromClosetSwingWorker().execute();
       refreshButton.doClick();
    }//GEN-LAST:event_addUpdateItemButtonActionPerformed

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
    private javax.swing.JButton addUpdateClosetButton;
    private javax.swing.JButton addUpdateItemButton;
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton newItemButton;
    private javax.swing.JFrame newUpdateClosetFrame;
    private javax.swing.JLabel newUpdateClosetLabel;
    private javax.swing.JFrame newUpdateItemFrame;
    private javax.swing.JLabel newUpdateItemLabel;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton updateClosetButton;
    private javax.swing.JButton updateItemButton;
    // End of variables declaration//GEN-END:variables
}
