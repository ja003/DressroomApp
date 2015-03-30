/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroommanager;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import cz.muni.fi.pv168.common.ValidationException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.sql.*;
import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Anna
 */
public class ItemsManagerImpl implements ItemsManager{
       
    private DataSource dataSource;
    
    private static final Logger logger = Logger.getLogger(
            ItemsManagerImpl.class.getName());

    
    private static ClosetManagerImpl closetManager;
    
    public ItemsManagerImpl( ) {
        }
    
    public ItemsManagerImpl(DataSource dataSource) {
        closetManager = new ClosetManagerImpl();
        this.dataSource = dataSource;
        closetManager.setDataSource(dataSource);
    }
    
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }
    
    
    @Override
    public void createItem(Item item){
        //logger.log(Level.INFO, "Attempt to add an item: {0}", item.toString());
        checkDataSource();
        if(item != null) {
            item.setAdded(new java.sql.Date((new java.util.Date()).getTime()));
        }
        validate(item);
        if (item.getId() != null) {
            throw new IllegalEntityException("item id is already set");
        } 
        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "INSERT INTO ITEM (type,add_date,gender,size,note,closet) "
                    + "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                st.setString(1, item.getType());
                st.setDate(2, item.getAdded());
                st.setString(3, item.getGender().name());
                st.setString(4, item.getSize());
                st.setString(5, item.getNote());
                st.setLong(6, item.getCloset().getId());
            
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, item, true);
            
            Long id = DBUtils.getId(st.getGeneratedKeys());
            item.setId(id);
            conn.commit();
        } catch (SQLException ex){
            String msg = "Error when inserting item into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally{
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }
    
    @Override
    public void deleteItem(Item item) {
        checkDataSource();
        if(item == null){
            throw new IllegalArgumentException("The item is null, can't be deleted");
        }
        if(item.getId() == null){
            throw new IllegalArgumentException("Id of the item is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM item WHERE id = ?");
                st.setLong(1, item.getId());
                
                int count = st.executeUpdate();
                DBUtils.checkUpdatesCount(count, item, false);
                conn.commit();
            
        } catch (SQLException ex) {
            String msg = "Error when deleting item from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }   
    
    @Override
    public Item getItemById(Long id) throws ServiceFailureException{
        
        checkDataSource();
        
        if(id == null){
            throw new IllegalArgumentException("id is null");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT id, type, ADD_DATE, gender, size, note, closet"
                    + " FROM item WHERE ID = ?");
            st.setLong(1, id);
            return executeQueryForSingleItem(st);
        } catch (SQLException ex){
            String msg = "Error when getting item with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }
    
    @Override
    public void updateItem(Item item) {
        checkDataSource();
        validate(item);
        
        if (item.getId() == null) {
            throw new IllegalEntityException("item id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);            
            st = conn.prepareStatement(
                    "UPDATE Item SET type = ?, gender = ?, size = ?, note = ? WHERE id = ?");
                st.setString(1, item.getType());
                st.setString(2, item.getGender().name());
                st.setString(3, item.getSize());
                st.setString(4, item.getNote());
                //st.setLong(5, item.getCloset().getId());
                st.setLong(5, item.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, item, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating item in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }
    
    static Item executeQueryForSingleItem(PreparedStatement st) throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Item result = resultSetToItem(rs);                
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more items with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }
    
    //UPRAVIT!!!!
    static private void validate(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("item is null");
        }
        
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setLenient(false);
        try {
            df.parse(item.getAdded().toString());
        } catch(Exception e) {
            throw new ValidationException("date not in valid format");            
        }
        if (item.getType() == null || item.getType().isEmpty()) {
            throw new ValidationException("item type is empty");
        }
        
        if (item.getGender() == null) {
            throw new ValidationException("item gender is null");
        }
        
        /* // not required, so no reason to validate
        if (item.getNote().isEmpty()) {
            throw new ValidationException("item note is empty");
        }
        
        if (item.getSize().isEmpty()) {
            throw new ValidationException("item note is empty");
        }
        */
        
        // if (item.getCloset() == null) {
        //    throw new ValidationException("item closet is empty");
        // }
    }
    
    static private Item resultSetToItem(ResultSet rs) throws SQLException{
        Item item = new Item();
        item.setId(rs.getLong("id"));
        item.setType(rs.getString("type"));
        item.setAdded(rs.getDate("ADD_DATE"));
        item.setGender(Gender.valueOf(rs.getString("gender")));
        item.setSize(rs.getString("size"));
        item.setNote(rs.getString("note"));
        //item.setCloset(closetManager.getClosetById(rs.getLong("closet")));
       
        
        return item;
    }
}
