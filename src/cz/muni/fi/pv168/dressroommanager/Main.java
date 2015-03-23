/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroommanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.dbcp.BasicDataSource;

/**
 *
 * @author Vukmir
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException{
        // TODO code application logic here
        /*
        Item item = new Item("shirt",Gender.FEMALE,"s","good");
        System.out.println(item.toString());
        
        ClosetManagerImpl manager = new ClosetManagerImpl();
        Closet closet = new Closet();
        closet.setOwner("Adam");
        closet.setName("my closet");
        System.out.println(closet.toString());
        manager.createCloset(closet);
        System.out.println(manager.getAllClosets().toString());
                */
        
        //setUp();
        
        myJDBCApp();
        
    }
    
    public static void setUp() throws SQLException {
        BasicDataSource bds = new BasicDataSource();
        //bds.setUrl("jdbc:derby:memory:ClosetManagerTest;create=true");
        bds.setUrl("jdbc:derby://localhost:1527/myDB;create=true");
        //this.dataSource = bds;
        //create new empty table before every test
        try (Connection conn = bds.getConnection()) {
            conn.prepareStatement("CREATE TABLE closet ("
                    + "id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "owner VARCHAR(255),"
                    + "name VARCHAR(255),").executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
        
        //manager = new ClosetManagerImpl(bds);
        
    }
    
    public static void myJDBCApp() throws SQLException{
    Connection conn = null;
    String url = "jdbc:derby://localhost:1527/";
    String dbName = "Student";
    String driver = "org.apache.derby.jdbc.ClientDriver";
    String userName = "root";
    String password = "root";
    try{
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url+dbName,userName,password);
        System.out.println("copnnected to db");
        conn.prepareStatement("CREATE TABLE GRAVE ("
                    + "id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "col INT,"
                    + "row INT,"
                    + "capacity INT NOT NULL,"
                    + "note VARCHAR(255))").executeUpdate();
        
        conn.close();
        System.out.println("discopnnected to db");
    } catch(Exception e){
        e.printStackTrace();
    }
}

    
    
    
}
