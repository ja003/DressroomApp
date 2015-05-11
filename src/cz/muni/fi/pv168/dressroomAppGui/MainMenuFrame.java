/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroomAppGui;

import cz.muni.fi.pv168.common.ServiceFailureException;
import cz.muni.fi.pv168.dressroommanager.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.apache.commons.dbcp.BasicDataSource;
//import org. ;

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
        
        
        //not working
        chooseClosetLabel.setBackground(Color.red);
        
        new AllClosetsSwingWorker().execute();
        new AllItemsFromClosetSwingWorker().execute();
        
        
        
        System.out.println("language = " + local);
        
    }
    
    private Locale local = Locale.getDefault();
    
    private String localeDirectory = "cz.muni.fi.pv168.dressroomAppGui.localization_" + local;
    
    ResourceBundle bundle = ResourceBundle.getBundle(localeDirectory,local);
    
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
        
        public void setGender(Gender gender) {
            this.gender = gender;
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
            //gender = Gender.BOTH;                   ///////////*******************FIX
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
            if(model.getElementAt(0) != null)
                model.setSelectedItem(model.getElementAt(0));
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
            
            currentCloset = closet;
            closetsComboBox.setModel(model);
            
            new AllClosetsSwingWorker().execute();
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
            
            System.out.println("current! = " + currentCloset);   
            if(currentCloset != null){
                model.setSelectedItem(currentCloset);
                System.out.println("selecting current closet: " + currentCloset);
            }else if (model.getSelectedItem() == null && model.getElementAt(0) != null)
            {
                System.out.println("no closet selected");
                model.setSelectedItem(model.getElementAt(0));
                currentCloset = model.getElementAt(0);
                System.out.println("selecting closet: " + model.getElementAt(0));
                
            }
            
            refreshButton.doClick();
            
            //refresh on each action
            closetsComboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    refreshButton.doClick();
                }
            });
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
        jLabel10 = new javax.swing.JLabel();
        itemSizeTextField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        itemNoteTextField = new javax.swing.JTextField();
        addUpdateItemButton = new javax.swing.JButton();
        genderComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        closetsComboBox = new javax.swing.JComboBox();
        newClosetBtn = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        newItemButton = new javax.swing.JButton();
        updateItemButton = new javax.swing.JButton();
        deleteItemButton = new javax.swing.JButton();
        updateClosetButton = new javax.swing.JButton();
        deleteClosetButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        itemsTable = new javax.swing.JTable();
        chooseClosetLabel = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();

        newUpdateClosetFrame.setPreferredSize(new java.awt.Dimension(410, 270));

        closetOwnerTextField.setText("jTextField1");
        closetOwnerTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closetOwnerTextFieldActionPerformed(evt);
            }
        });

        newUpdateClosetLabel.setBackground(new java.awt.Color(255, 0, 102));
        newUpdateClosetLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        newUpdateClosetLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        newUpdateClosetLabel.setText("New Closet");

        closetOwnerLabel.setText(bundle.getString("closetOwnerLabel"));

        closetNameLabel.setText(bundle.getString("closetNameLabel"));

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(newUpdateClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(closetOwnerTextField)
                    .addComponent(closetOwnerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(newUpdateClosetFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(closetNameTextField)
                    .addComponent(closetNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(addUpdateClosetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        newUpdateItemFrame.setPreferredSize(new java.awt.Dimension(400, 400));

        newUpdateItemLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        newUpdateItemLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        newUpdateItemLabel.setText("New Item");

        itemTypeLabel.setText(bundle.getString("itemTypeLabel"));

        itemTypeTextField.setText("jTextField3");

        jLabel9.setText(bundle.getString("itemGenderLabel"));

        jLabel10.setText(bundle.getString("itemSizeLabel"));

        itemSizeTextField.setText("jTextField3");

        jLabel11.setText(bundle.getString("itemNoteLabel"));

        itemNoteTextField.setText("jTextField3");

        addUpdateItemButton.setText("ADD ITEM");
        addUpdateItemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addUpdateItemButtonActionPerformed(evt);
            }
        });

        genderComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] {}));

        javax.swing.GroupLayout newUpdateItemFrameLayout = new javax.swing.GroupLayout(newUpdateItemFrame.getContentPane());
        newUpdateItemFrame.getContentPane().setLayout(newUpdateItemFrameLayout);
        newUpdateItemFrameLayout.setHorizontalGroup(
            newUpdateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newUpdateItemFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(newUpdateItemFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(newUpdateItemLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addUpdateItemButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(newUpdateItemFrameLayout.createSequentialGroup()
                        .addComponent(itemTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(itemTypeTextField))
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
                        .addComponent(genderComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(genderComboBox))
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
        setBackground(new java.awt.Color(153, 255, 102));
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(bundle.getString("dressroomLabel"));
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        closetsComboBox.setName(""); // NOI18N
        closetsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closetsComboBoxActionPerformed(evt);
            }
        });

        newClosetBtn.setText(bundle.getString("newClosetBtn"));
        newClosetBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newClosetButtonActionPerformed(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(bundle.getString("closetContentLabel"));
        jLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        newItemButton.setText(bundle.getString("newItemBtn"));
        newItemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newItemButtonActionPerformed(evt);
            }
        });

        updateItemButton.setText(bundle.getString("updateItemBtn"));
        updateItemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateItemButtonActionPerformed(evt);
            }
        });

        deleteItemButton.setText(bundle.getString("deleteItemBtn"));
        deleteItemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteItemButtonActionPerformed(evt);
            }
        });

        updateClosetButton.setText(bundle.getString("updateClosetBtn"));
        updateClosetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateClosetActionPerformed(evt);
            }
        });

        deleteClosetButton.setText(bundle.getString("deleteClosetBtn"));
        deleteClosetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteClosetActionPerformed(evt);
            }
        });

        itemsTable.setModel(new ItemsTableModel());
        jScrollPane4.setViewportView(itemsTable);

        chooseClosetLabel.setFont(new java.awt.Font("Tahoma", 2, 14)); // NOI18N
        chooseClosetLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chooseClosetLabel.setText(bundle.getString("chooseClosetLabel"));
        chooseClosetLabel.setAlignmentY(0.0F);
        chooseClosetLabel.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        chooseClosetLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        refreshButton.setText(bundle.getString("refreshBtn"));
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
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newItemButton)
                        .addGap(18, 18, 18)
                        .addComponent(updateItemButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(deleteItemButton, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 371, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(updateClosetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(deleteClosetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(closetsComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(chooseClosetLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(newClosetBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(refreshButton)))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(refreshButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chooseClosetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newClosetBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(updateClosetButton)
                    .addComponent(deleteClosetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        newUpdateClosetFrame.setSize(newUpdateClosetFrame.getPreferredSize());
        newUpdateClosetFrame.setLocationRelativeTo(null);
        updateC = false;
        newUpdateClosetLabel.setText("New Closet");
        
        closetOwnerTextField.setText("");
        closetNameTextField.setText("");
        
        newUpdateClosetFrame.setVisible(true);
    }//GEN-LAST:event_newClosetButtonActionPerformed

    private void newItemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newItemButtonActionPerformed
        newUpdateItemFrame.setSize(newUpdateItemFrame.getPreferredSize());
        newUpdateItemFrame.setLocationRelativeTo(null);
        updateI = false;
        newUpdateItemLabel.setText("New Item");
        addUpdateItemButton.setText("ADD ITEM");
        
        itemNoteTextField.setText("");
        itemTypeTextField.setText("");
        itemSizeTextField.setText("");
        
        genderComboBox.removeAllItems();
        for(Gender g: Gender.values()){
            genderComboBox.addItem(g);
        }
        
        newUpdateItemFrame.setVisible(true);
    }//GEN-LAST:event_newItemButtonActionPerformed

    private void deleteClosetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteClosetActionPerformed
       Closet closet = null;
        closet = (Closet)closetsComboBox.getSelectedItem();
        if(closet == null){
            String err = bundle.getBundle(localeDirectory).getString("noSelectedCloset");
            JOptionPane.showMessageDialog(this, err);
        }
        else{ 
            boolean remove = false;
            String removeClosetMsg = bundle.getBundle(localeDirectory).getString("removeClosetMsg");
            String title = java.util.ResourceBundle.getBundle(localeDirectory).getString("removeClosetTitle");
            int popUp = JOptionPane.showConfirmDialog(this, removeClosetMsg, title,
                        JOptionPane.YES_NO_OPTION);
            
            remove = true;
            for (Closet c : closetManager.getAllClosets()) {
                if (c.equals(closet)) {
                    remove = false;
                    break;
                }
            }
            
            if (!remove) {
                if (popUp == JOptionPane.YES_OPTION) {
                    deleteClosetSwingWorker = new DeleteClosetSwingWorker();
                    deleteClosetSwingWorker.execute();
                    currentCloset = null;
                    new AllClosetsSwingWorker().execute();
                }
            } else {
                String removingItemMsg = bundle.getBundle(localeDirectory).getString("removingClosetMsg");
                JOptionPane.showMessageDialog(this, removingItemMsg);
            }
            
            
            
        }
    }//GEN-LAST:event_deleteClosetActionPerformed

    private void updateItemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateItemButtonActionPerformed
      
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow == -1) {
            String err = bundle.getBundle(localeDirectory).getString("noSelectedItem");
            JOptionPane.showMessageDialog(this, err);
        } else {
            newUpdateItemFrame.setSize(newUpdateItemFrame.getPreferredSize());
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

            try{
                item = itemsManager.getItemById((Long) idValue);
            }catch(Exception ex){
                Logger.getLogger(MainMenuFrame.class.getName()).log(Level.SEVERE, null, ex);
                throw ex;
            }

            updateId = item.getId();
            
            genderComboBox.removeAllItems();
            for(Gender g: Gender.values()){
                genderComboBox.addItem(g);
            }
        
        
            itemTypeTextField.setText(item.getType());
            itemSizeTextField.setText(item.getSize());
            itemNoteTextField.setText(item.getNote());
            newUpdateItemFrame.setVisible(true);

        }
        
        
    }//GEN-LAST:event_updateItemButtonActionPerformed

    private void deleteItemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteItemButtonActionPerformed
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow == -1) {
            String err = bundle.getBundle(localeDirectory).getString("noSelectedItem");
            JOptionPane.showMessageDialog(this, err);
        } else {
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
            
            boolean remove = false;
            String removeItemMsg = bundle.getBundle(localeDirectory).getString("removeItemMsg");
            String title = bundle.getBundle(localeDirectory).getString("removeItemTitle");
            int popUp = JOptionPane.showConfirmDialog(this, removeItemMsg, title,
                        JOptionPane.YES_NO_OPTION);

            remove = true;
            for (Item i : dressroomManager.getAllItemsFromCloset(currentCloset)) {
                if (i.equals(item)) {
                    remove = false;
                    break;
                }
            }
            if (!remove) {
                if (popUp == JOptionPane.YES_OPTION) {
                    dressroomManager.removeItemFromCloset(item, currentCloset);
                }
            } else {
                String removingItemMsg = bundle.getBundle(localeDirectory).getString("removingItemMsg");
                JOptionPane.showMessageDialog(this, removingItemMsg);
            }
            
            new AllItemsFromClosetSwingWorker().execute();
        }
        
    }//GEN-LAST:event_deleteItemButtonActionPerformed

    private void updateClosetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateClosetActionPerformed
        Closet closet = null;
        closet = (Closet)closetsComboBox.getSelectedItem();
        if(closet == null){
            String err = bundle.getBundle(localeDirectory).getString("noSelectedCloset");
            JOptionPane.showMessageDialog(this, err);
        }
        else{ 
            newUpdateClosetFrame.setSize(newUpdateClosetFrame.getPreferredSize());
            newUpdateClosetFrame.setLocationRelativeTo(null);

            updateC = true;
            String updateClosetLabel = bundle.getBundle(localeDirectory).getString("updateClosetLabel");
            newUpdateClosetLabel.setText(updateClosetLabel);
            String updateClosetBtn = bundle.getBundle(localeDirectory).getString("updateClosetBtn");
            addUpdateClosetButton.setText(updateClosetBtn);


            updateId = closet.getId();


            closetNameTextField.setText(closet.getName());
            closetOwnerTextField.setText(closet.getOwner());
            newUpdateClosetFrame.setVisible(true);
        }
    }//GEN-LAST:event_updateClosetActionPerformed

    private void closetOwnerTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closetOwnerTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_closetOwnerTextFieldActionPerformed

    private void closetNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closetNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_closetNameTextFieldActionPerformed

    private void addUpdateClosetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addUpdateClosetButtonActionPerformed
        boolean nameOk = false;
        boolean ownerOk = false;
        String name = closetNameTextField.getText();
        String owner = closetOwnerTextField.getText();
        
        if(name.length() != 0)
            nameOk = true;
        if(owner.length() != 0)
            ownerOk = true;
        
        if(nameOk && ownerOk){
           addClosetSwingWorker = new AddClosetSwingWorker();
           addClosetSwingWorker.setName(name);
           addClosetSwingWorker.setOwner(owner);
           addClosetSwingWorker.execute();
           newUpdateClosetFrame.dispose();
           
           System.out.println("current=" + currentCloset);
           closetsComboBox.setSelectedItem(currentCloset);
            System.out.println("get="+closetsComboBox.getSelectedItem());
           
           new AllClosetsSwingWorker().execute();
        }
        else{
            if(!nameOk){
                String err = bundle.getBundle(localeDirectory).getString("wrongNameMsg");
                JOptionPane.showMessageDialog(this, err);
                closetNameTextField.setBackground(Color.PINK);
            }else{
                closetNameTextField.setBackground(Color.WHITE);
            }
            if(!ownerOk){
                String err = bundle.getBundle(localeDirectory).getString("wrongOwnerMsg");
                JOptionPane.showMessageDialog(this, err);
                closetOwnerTextField.setBackground(Color.PINK);
            }else{
                closetOwnerTextField.setBackground(Color.WHITE);
            }
        }
        
    }//GEN-LAST:event_addUpdateClosetButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        new AllItemsFromClosetSwingWorker().execute();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void addUpdateItemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addUpdateItemButtonActionPerformed
        Date added = getSqlDate(Calendar.getInstance().getTime(), 0);
        boolean typeOk = false;
        boolean sizeOk = false;
        boolean genderOk = false;
        boolean noteOk = true;

        String type = itemTypeTextField.getText();
        String size = itemSizeTextField.getText();
        Gender gender = (Gender)genderComboBox.getSelectedItem();

        String note = itemNoteTextField.getText();

        if(type.length() != 0)
        typeOk = true;
        if(size.length() != 0)
        sizeOk = true;
        if(gender != null)
        genderOk = true;
        if(note.length() != 0)
        noteOk = true;

        if(typeOk && sizeOk && genderOk && noteOk){
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
        }else{
            if(!typeOk){
                String typeMsg = bundle.getBundle(localeDirectory).getString("wrongTypeMsg");
                JOptionPane.showMessageDialog(this, typeMsg);
                itemTypeTextField.setBackground(Color.PINK);
            }
            else{
                itemTypeTextField.setBackground(Color.WHITE);
            }
            if(!sizeOk){
                String sizeMsg = bundle.getBundle(localeDirectory).getString("wrongSizeMsg");
                JOptionPane.showMessageDialog(this, sizeMsg);
                itemSizeTextField.setBackground(Color.PINK);
            }
            else{
                itemSizeTextField.setBackground(Color.WHITE);
            }
            if(!genderOk){
                String genderMsg = bundle.getBundle("cz.muni.fi.pv168.dressroomAppGui/localization_" + local).getString("wrongGenderMsg");
                JOptionPane.showMessageDialog(this, genderMsg);
            }

            if(!noteOk){
                String noteMsg = bundle.getBundle("cz.muni.fi.pv168.dressroomAppGui/localization_" + local).getString("wrongNoteMsg");
                JOptionPane.showMessageDialog(this, noteMsg);
                itemNoteTextField.setBackground(Color.PINK);
            }
            else{
                itemNoteTextField.setBackground(Color.WHITE);
            }

        }

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
    private javax.swing.JLabel chooseClosetLabel;
    private javax.swing.JLabel closetNameLabel;
    private javax.swing.JTextField closetNameTextField;
    private javax.swing.JLabel closetOwnerLabel;
    private javax.swing.JTextField closetOwnerTextField;
    public static javax.swing.JComboBox closetsComboBox;
    private javax.swing.JButton deleteClosetButton;
    private javax.swing.JButton deleteItemButton;
    private javax.swing.JComboBox genderComboBox;
    private javax.swing.JTextField itemNoteTextField;
    private javax.swing.JTextField itemSizeTextField;
    private javax.swing.JLabel itemTypeLabel;
    private javax.swing.JTextField itemTypeTextField;
    private javax.swing.JTable itemsTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton newClosetBtn;
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
