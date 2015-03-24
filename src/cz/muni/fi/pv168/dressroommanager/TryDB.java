/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroommanager;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;

/**
 *
 * @author Vukmir
 */
public class TryDB {
    
private ClosetManagerImpl manager;


public TryDB(){
    
    
}
    
public void start() throws SQLException{
    setUp();


    Closet closet = new Closet();
    closet.setOwner("Adam3");
    closet.setName("my closet3");
    System.out.println(closet.toString());
    manager.createCloset(closet);
    System.out.println("get by id " + manager.getClosetById(closet.getId()));
    
    Closet closet2 = new Closet();
    closet2.setOwner("Anca");
    closet2.setName("my closet2");
    System.out.println(closet2.toString());
    manager.createCloset(closet2);
    
    //manager.getClosetById(Long.MIN_VALUE)
    System.out.println("get all closets" + manager.getAllClosets().toString());

}

public  void setUp() throws SQLException {
    BasicDataSource bds = new BasicDataSource();
    bds.setUrl("jdbc:derby://localhost:1527/DressroomDB;create=true");
    //bds.setUrl("jdbc:derby:memory;create=true");                                      //not sure how this works
    //this.dataSource = bds;
    //create new empty table before every test
    try (Connection conn = bds.getConnection()) {
        conn.prepareStatement("CREATE TABLE CLOSET ("
                + "id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                + "owner VARCHAR(255),"
                + "name VARCHAR(255))").executeUpdate();
    }catch(Exception e){
        e.printStackTrace();
    }
    manager = new ClosetManagerImpl(bds);

}
    
}
