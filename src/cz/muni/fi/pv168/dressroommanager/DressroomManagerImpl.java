/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroommanager;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author Vukmir
 */
public class DressroomManagerImpl implements DressroomManager
{
    private static final Logger logger = Logger.getLogger(
            ClosetManagerImpl.class.getName());

    

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }    

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }
    
    
    
    public List<Item> getAllItemsFromCloset(Closet closet) throws ServiceFailureException{
        checkDataSource();
        if (closet == null) {
            throw new IllegalArgumentException("closet is null");
        }        
        if (closet.getId() == null) {
            throw new IllegalEntityException("closet id is null");
        }  
        
        Connection conn = null;
        PreparedStatement findSt = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            //checkIfGraveHasSpace(conn, closet);
            
            findSt = conn.prepareStatement(
                    "SELECT * FROM Item WHERE closet = ?");
            findSt.setLong(1, closet.getId());
            return ItemsManagerImpl.executeQueryForMultipleItems(findSt);
        } catch (SQLException ex) {
            String msg = "Error when getting all items from closet";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, findSt);
        }
        
    }
    
    public void putItemInCloset(Item item, Closet closet)throws ServiceFailureException, IllegalEntityException {
        checkDataSource();
        if (closet == null) {
            throw new IllegalArgumentException("closet is null");
        }        
        if (closet.getId() == null) {
            throw new IllegalEntityException("closet id is null");
        }        
        if (item == null) {
            throw new IllegalArgumentException("item is null");
        }        
        if (item.getId() == null) {
            throw new IllegalEntityException("item id is null");
        }        
        Connection conn = null;
        PreparedStatement updateSt = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            //checkIfGraveHasSpace(conn, closet);
            
            updateSt = conn.prepareStatement(
                    "UPDATE Item SET closet = ? WHERE id = ?");
                updateSt.setLong(1, closet.getId());
                updateSt.setLong(2, item.getId());
            int count = updateSt.executeUpdate();
            if (count == 0) {
                throw new IllegalEntityException("Item " + item + " not found or it is already placed in some closet");
            }
            DBUtils.checkUpdatesCount(count, item, false);            
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when putting item into closet";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, updateSt);
        }
        
    }
    
    /*// we don't allow item to exists without closet, so this make no sense
    public void removeItemFromCloset(Item item, Closet closet)throws ServiceFailureException, IllegalEntityException {
        checkDataSource();
        if (closet == null) {
            throw new IllegalArgumentException("closet is null");
        }        
        if (closet.getId() == null) {
            throw new IllegalEntityException("closet id is null");
        }        
        if (item == null) {
            throw new IllegalArgumentException("item is null");
        }        
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
                    "UPDATE Item SET closetId = NULL WHERE id = ? AND closetId = ?");
                    //"UPDATE Body SET graveId = NULL WHERE id = ? AND graveId = ?");
            st.setLong(1, item.getId());
            st.setLong(2, closet.getId());
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, item, false);            
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when putting item from closet";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }   
    }
    */
    public Closet findClosetWithItem(Item item)throws ServiceFailureException, IllegalEntityException{
        checkDataSource();
        if(item == null){
            throw new IllegalArgumentException("item is null");
        }
        if(item.getId() ==  null){
            throw new IllegalArgumentException("item id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT * "
                    + " FROM Closet JOIN Item ON Closet.id = Item.closet"
                    + " WHERE Item.id = ?");
                st.setLong(1, item.getId());
                return ClosetManagerImpl.executeQueryForSingleCloset(st);
                
        } catch (SQLException ex){
            String msg = "Error when trying to find closet with item " + item;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        } 
    }
    
    
    public boolean isItemInCloset(Item item, Closet closet) {
        checkDataSource();
        if (closet == null) {
            throw new IllegalArgumentException("closet is null");
        }        
        if (closet.getId() == null) {
            throw new IllegalEntityException("closet id is null");
        }  
        if (item == null) {
            throw new IllegalArgumentException("item is null");
        }        
        if (item.getId() == null) {
            throw new IllegalEntityException("item id is null");        
        }  
        Connection conn = null;
        PreparedStatement findSt = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            //checkIfGraveHasSpace(conn, closet);
            
            findSt = conn.prepareStatement(
                    "SELECT * FROM Item WHERE closet = ? AND id = ?");
            findSt.setLong(1, closet.getId());
            findSt.setLong(2, item.getId());
             
            return ItemsManagerImpl.executeQueryForSingleItem(findSt) != null;
        } catch (SQLException ex) {
            String msg = "Error when finding item in closet";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, findSt);
        }
    }
    //typ prevest na kapitaly
    public List<Item> findItemsInClosetByType(Closet closet, String type)throws ServiceFailureException {
        checkDataSource();
        if (closet == null) {
            throw new IllegalArgumentException("closet is null");
        }        
        if (closet.getId() == null) {
            throw new IllegalEntityException("closet id is null");
        }  
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("item type is null or empty");
        }        
         
        Connection conn = null;
        PreparedStatement findSt = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            //checkIfGraveHasSpace(conn, closet);
            
            findSt = conn.prepareStatement(
                    "SELECT * FROM Item WHERE closet = ? AND type = ?");
            findSt.setLong(1, closet.getId());
            findSt.setString(2, type);
             
            return ItemsManagerImpl.executeQueryForMultipleItems(findSt);
        } catch (SQLException ex) {
            String msg = "Error when finding item in closet by type";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, findSt);
        }
        
        
    }


}
