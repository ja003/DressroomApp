/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroommanager;

import cz.muni.fi.pv168.common.ServiceFailureException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import javax.sql.*;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Vukmir
 */
public class ClosetManagerImpl implements ClosetManager
{
    private DataSource dataSource;
    private final Logger log = LoggerFactory.getLogger(ClosetManagerImpl.class);

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
    @Override                                                               ///////////////CHECK...musí se použít ServiceFailureException???????????
    public void createCloset(Closet closet)throws ServiceFailureException {
        if (closet == null) {
            throw new IllegalArgumentException("grave is null");
        }
        if (closet.getId() != null) {
            throw new IllegalArgumentException("grave id is already set");
        }
        if (closet.getOwner().length() < 1) {
            throw new IllegalArgumentException("owner name is empty");
        }
        if (closet.getName().length() < 1) {
            throw new IllegalArgumentException("closet name is empty");
        }

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO CLOSET (owner,name) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, closet.getOwner());
                st.setString(2, closet.getName());
                int addedRows = st.executeUpdate();
                if (addedRows != 1) {
                    throw new ServiceFailureException("Internal Error: More rows inserted when trying to insert grave " + closet);
                }
                ResultSet keyRS = st.getGeneratedKeys();
                keyRS.next();
                closet.setId(keyRS.getLong(1));
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving the closet", ex);
        }
    }
    
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
    
    @Override
    public void deleteCloset(Closet closet) {
        if (closet == null) {
            throw new IllegalArgumentException("Closet is null");
        }
        if (closet.getId() == null) {
            throw new IllegalArgumentException("Id is null");
        }
        log.info("Attempt to remove airship: "+closet.toString());
        
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement st = connection.prepareStatement("DELETE FROM closet WHERE id = ?")) {
                st.setLong(1, closet.getId());
                st.execute();
            }
        } catch (SQLException ex) {
            log.error(ex.toString());
        }
    }
    
    @Override
    public Closet getClosetById(Long id)throws ServiceFailureException {
        log.info("Attempt to get closet by id with given id: "+id.toString());
        
        if (id == null) {
            throw new IllegalArgumentException("Id is null");
        }
        Closet closet = null;
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement st = connection.prepareStatement("SELECT id,owner,name FROM closet WHERE ID = ?")) {
                st.setLong(1, id);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    closet = resultSetToCloset(rs);
                }
            }
        } catch (SQLException ex) {
            log.error(ex.toString());
        }
        return closet;
    }
    
    @Override
    public List<Closet> getAllClosets() {
        log.info("Attempt to get all airships");
        
        List<Closet> closets = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement st = connection.prepareStatement("SELECT id,owner,name FROM closet")) {
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    closets.add(resultSetToCloset(rs));
                }
            }
        } catch (SQLException ex) {
            log.error(ex.toString());
        }
        return closets;
    }
    
    @Override
    public void updateCloset(Closet closet) {
        if (closet == null) {
            throw new IllegalArgumentException("grave is null");
        }
        if (closet.getId() != null) {
            throw new IllegalArgumentException("grave id is already set");
        }
        if (closet.getOwner().length() < 1) {
            throw new IllegalArgumentException("owner name is empty");
        }
        if (closet.getName().length() < 1) {
            throw new IllegalArgumentException("closet name is empty");
        }
        log.info("Attempt to edit airship: "+closet.toString());
        
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement st = connection.prepareStatement("UPDATE closet SET "
                    + "owner = ?, name = ? WHERE id = ?")) {
                st.setString(1, closet.getOwner());
                st.setString(2, closet.getName());
                st.setLong(3, closet.getId());
                if(st.executeUpdate()!=1) {
                    throw new IllegalArgumentException("cannot update grave "+closet);
                }
            }
        } catch (SQLException ex) {
            log.error(ex.toString());
        }
    }
    
    
    private Closet resultSetToCloset(ResultSet rs) throws SQLException {
        Closet closet = new Closet();
        closet.setId(rs.getLong("id"));
        closet.setOwner(rs.getString("owner"));
        closet.setName(rs.getString("name"));        
               //.setOwner(rs.getString("owner")); nefachá...????

        return closet;
    }
    
}
