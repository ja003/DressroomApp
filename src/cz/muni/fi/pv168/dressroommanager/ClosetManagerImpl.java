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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.*;
import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author Vukmir
 */
public class ClosetManagerImpl implements ClosetManager
{
    private DataSource dataSource;
    
    private static final Logger logger = Logger.getLogger(
            ClosetManagerImpl.class.getName());

    public ClosetManagerImpl() {}
    
    public ClosetManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }
    
    //na primary key use big int (místo long)
    //názvy sloupcu dát do uvozovek, pokud se shodují s sql příkazem
    @Override                                                             
    public void createCloset(Closet closet) {
        validate(closet);
        if (closet.getId() != null) {
            throw new IllegalArgumentException("closet id is already set");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement("INSERT INTO CLOSET (owner,name) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                st.setString(1, closet.getOwner());
                st.setString(2, closet.getName());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, closet, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            closet.setId(id);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting closet into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }
    
    
    
    @Override
    public void deleteCloset(Closet closet) {
        if (closet == null) {
            throw new IllegalArgumentException("Closet is null");
        }
        if (closet.getId() == null) {
            throw new IllegalArgumentException("Id is null");
        }
        //log.info("Attempt to remove airship: "+closet.toString());
        
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement st = connection.prepareStatement("DELETE FROM closet WHERE id = ?")) {
                st.setLong(1, closet.getId());
                st.execute();
            }
        } catch (SQLException ex) {
            //log.error(ex.toString());
        }
    }
    
    @Override
    public Closet getClosetById(Long id) {
        
        checkDataSource();
        
        if (id == null) {
            throw new IllegalArgumentException("Id is null");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id,owner,name FROM closet WHERE ID = ?");
            st.setLong(1, id);
            return executeQueryForSingleCloset(st);
        } catch (SQLException ex) {
            String msg = "Error when getting closet with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
        
    }
    
    
    @Override
    public List<Closet> getAllClosets() {
        
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id,owner,name FROM closet");
            return executeQueryForMultipleClosets(st);
        } catch (SQLException ex) {
            String msg = "Error when getting all closets from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }  
    }
    
    @Override
    public void updateCloset(Closet closet) {
        validate(closet);
        if (closet.getId() == null) {
            throw new IllegalEntityException("closet with null id cannot be updated");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement("UPDATE closet SET "
                    + "owner=?, name=? WHERE id=?");
                st.setString(1, closet.getOwner());
                st.setString(2, closet.getName());
                st.setLong(3, closet.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, closet, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating grave in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }
    
    
    static Closet executeQueryForSingleCloset(PreparedStatement st) throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Closet result = rowToCloset(rs);                
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more graves with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }
    
    static List<Closet> executeQueryForMultipleClosets(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Closet> result = new ArrayList<Closet>();
        while (rs.next()) {
            result.add(rowToCloset(rs));
        }
        return result;
    }
    
    private static Closet rowToCloset(ResultSet rs) throws SQLException {
        Closet result = new Closet();
        result.setId(rs.getLong("id"));
        result.setOwner(rs.getString("owner"));
        result.setName(rs.getString("name"));
        return result;
    }
    
    private static void validate(Closet closet) {
        if (closet == null) {
            throw new IllegalArgumentException("closet is null");
        }
        if (closet.getOwner().length() < 1) {
            throw new ValidationException("owner name is empty");
        }
        if (closet.getName().length() < 1) {
            throw new ValidationException("closet name is empty");
        }
        if(closet.getOwner().matches(".*\\d.*")){
            throw new ValidationException("owner contains number");
        }     
    }
    
    //not sure if needed
    /*
    private Long getKey(ResultSet keyRS, Closet closet) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + closet
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + closet
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert grave " + closet
                    + " - no key found");
        }
    }
    */
    
}
