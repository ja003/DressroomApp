/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.dressroommanager;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ValidationException;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for ItemsManagerImpl methods
 * @author Anna
 */
public class ItemsManagerImplTest {
    private ItemsManagerImpl manager;
    private DataSource ds;
    private Closet closet; 
    
    private static DataSource prepareDataSource() throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        //we will use in memory database
        ds.setUrl("jdbc:derby:memory:dressroom-test;create=true");
        return ds;
    }
    
    @Before
    public void setUp() throws SQLException{
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds, ItemsManager.class.getResource("createTables.sql"));
        manager = new ItemsManagerImpl();
        manager.setDataSource(ds);
        
        ClosetManagerImpl cm = new ClosetManagerImpl();
        cm.setDataSource(ds);

        Closet closet = new Closet();
        closet.setOwner("Thomas");
        closet.setName("pants");
        cm.createCloset(closet);
        this.closet = closet;
    }
    
    @After
    public void tearDown() throws SQLException{
        DBUtils.executeSqlScript(ds, ItemsManager.class.getResource("dropTables.sql"));
    }
    
    /**
     * Test of createItem method, of class ItemsManagerImpl.
     */
    @Test
    public void createItem(){
        //test if is in database
        System.out.println("test createItem");
        
        Item item = newItem("shirt", Gender.FEMALE, "XS", " ", closet);
        manager.createItem(item);
        
        Long itemId = item.getId();
        assertNotNull(item.getId());
        Item result = manager.getItemById(itemId);
        assertEquals(item, result);
        assertNotSame(item, result);
    }
    
    /**
     * Another tests of createItem. Testing wrong attributes
     */
    @Test(expected = IllegalArgumentException.class)
    public void createItemWithWrongAttributeNullItem(){
        manager.createItem(null);
    }
    
    @Test(expected = IllegalEntityException.class)
    public void createItemWithWrongAttributeNotNullId(){
        Item item = newItem("socks", Gender.MALE, "39", "", closet);
        item.setId(2L);
        manager.createItem(item);
    }
    
    @Test(expected = ValidationException.class)
    public void createItemWithWrongAttributesAllNull(){
        Item item = newItem(null, null, null, null, null);
        manager.createItem(item);
    }
    
    @Test(expected = ValidationException.class)
    public void createItemWithWrongAttributeNullType(){
        Item item = newItem(null, Gender.BOTH, "S", "nice", closet);
        manager.createItem(item);
    }
    
    @Test(expected = ValidationException.class)
    public void createItemWithWrongAttributeType(){
        Item item = newItem("", Gender.BOTH, "S", "nice", closet);
        manager.createItem(item);
    }
    
    @Test(expected = ValidationException.class)
    public void updateItemWithNullType(){
        Item item1 = newItem("shirt", Gender.FEMALE, "XS", " ", closet);
        manager.createItem(item1);
        Long item1Id = item1.getId();
        item1 = manager.getItemById(item1Id);
        item1.setType(null);
        manager.updateItem(item1);
    }
    /**
     * Another test of createItem. Testing wrong attributes
     */
    @Test
    public void createItemWithWrongAttributes(){
        // these variants should be ok
        Item item = newItem("socks", Gender.MALE, "39", "", closet);
        manager.createItem(item);
        Item result = manager.getItemById(item.getId());
        assertNotNull(result);
        
        item = newItem("socks", Gender.MALE, "39", null, closet);
        manager.createItem(item);
        result = manager.getItemById(item.getId());
        assertNotNull(result);
        assertNull(result.getNote());
        
        item = newItem("socks", Gender.FEMALE, null, "nice", closet);
        manager.createItem(item);
        result = manager.getItemById(item.getId());
        assertNotNull(result);
        assertNull(result.getSize());
    }
    
    /**
     * Test of updateItem method of ItemsManagerImpl class
     */
    @Test
    public void updateItem(){
        System.out.println("test updateItem");
        
        Item item1 = newItem("shirt", Gender.FEMALE, "XS", " ", closet);
        Item item2 = newItem("skirt", Gender.FEMALE, "S", "knee length, formal, black", closet);
        manager.createItem(item1);
        manager.createItem(item2);
        Long item1Id = item1.getId();
        
        //change of gendre
        item1 = manager.getItemById(item1Id);
        item1.setGender(Gender.MALE);
        manager.updateItem(item1);
        assertEquals(item1.getGender(), Gender.MALE);  

        //change of type
        item1 = manager.getItemById(item1Id);
        item1.setType("jacket");
        manager.updateItem(item1);
        assertEquals("jacket", item1.getType());
        
        //change of note
        item1 = manager.getItemById(item1Id);
        item1.setNote("very nice shirt with Superman logo");
        manager.updateItem(item1);
        assertNotNull(item1.getNote());
        assertEquals("very nice shirt with Superman logo", item1.getNote());
        
        //change of size
        item1 = manager.getItemById(item1Id);
        item1.setSize("L");
        manager.updateItem(item1);
        assertNotNull(item1);
        assertEquals("L", item1.getSize());
        
        // Check if updates didn't affected other records
        assertDeepEquals(item2, manager.getItemById(item2.getId()));
    }

    /**
     * Test of getItemById method of ItemsManagerImpl class
     */
    @Test
    public void getItemById(){
        assertNull(manager.getItemById(1l));
        
        Item item  = newItem("shirt", Gender.FEMALE, "XS", " ", closet);
        manager.createItem(item);
        Long itemId = item.getId();

        Item result = manager.getItemById(itemId);
        assertEquals(item, result);
        //assertItemDeepEquals(item, result);
        /*
        System.out.println("test getItemById");
        
        Long id = null;
        ItemsManagerImpl instance = new ItemsManagerImpl();
        Item expResult = null;
        Item result = instance.getItemById(id);
        assertEquals(expResult, result);  
        */
    }
    
    /**
     * Test of deleteItem method of ItemsManagerImpl class.
     */
    @Test
    public void deleteItem(){
        System.out.println("test deleteItem");
        
        Item item1 = newItem("shirt", Gender.FEMALE, "XS", " ", closet);
        Item item2 = newItem("skirt", Gender.FEMALE, "S", "popis", closet);
        manager.createItem(item1);
        manager.createItem(item2);
        
        assertNotNull(manager.getItemById(item1.getId()));
        assertNotNull(manager.getItemById(item2.getId()));
        
        manager.deleteItem(item1);
        
        assertNull(manager.getItemById(item1.getId()));
        assertNotNull(manager.getItemById(item2.getId()));
    }
        
    private static Item newItem(String type, Gender gender, String size, String note, Closet closet){
        Item item = new Item();
        item.setType(type);
        item.setGender(gender);
        item.setSize(size);
        item.setNote(note);
        item.setCloset(closet);
        return item; 
    }
    
    private void assertDeepEquals(Item item, Item result) {
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getGender(), result.getGender());
        assertEquals(item.getType(), result.getType());
        assertEquals(item.getSize(), result.getSize());
        assertEquals(item.getNote(), result.getNote());
        
        //throw new UnsupportedOperationException("Not supported yet."); 
    }
}
