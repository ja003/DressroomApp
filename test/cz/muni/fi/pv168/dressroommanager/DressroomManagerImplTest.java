/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroommanager;

import cz.muni.fi.pv168.common.DBUtils;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Vukmir
 */
public class DressroomManagerImplTest {
    private DressroomManagerImpl manager;
    private ItemsManagerImpl itemsManager;
    private ClosetManagerImpl closetManager;
    private DataSource ds;
            
    /*
    public DressroomManagerImplTest() {
    }
    */
    
    private static DataSource prepareDataSource() throws SQLException{
        BasicDataSource ds = new BasicDataSource();
        //we will use in memory database
        ds.setUrl("jdbc:derby:memory:dressroom-test;create=true");
        //ds.setUrl("jdbc:derby://localhost:1527/test");
        return ds;
    }
    
    private Closet c1, c2, c3, closetWithNullId, closetWithNoItem, fakeCloset;
    private Item i1, i2, i3, i4;
    
    
    private void prepareTestData(){
        c1 = newCloset("Anna", "Closet_01");
        c2 = newCloset("Adam", "Closet_02");
        c3 = newCloset("Tomas", "Closet_03");        
        fakeCloset = newCloset("fake", "fake");
        closetWithNoItem = newCloset("Petr", "Closet_05");

        closetWithNullId = newCloset("Pavel", "Closet_04");
        
        i1 = newItem("shirt 1", Gender.BOTH, "M", null, fakeCloset);
        i2 = newItem("shirt 2", Gender.FEMALE, null, "beautiful", fakeCloset);
        i3 = newItem("shirt 3", Gender.MALE, "XXl", "with awesome unicorn", fakeCloset);
        i4 = newItem("shirt 1", Gender.MALE, null, null, fakeCloset);
        
        closetManager.createCloset(fakeCloset);

        
        itemsManager.createItem(i1);
        itemsManager.createItem(i2);
        itemsManager.createItem(i3);
        itemsManager.createItem(i4);
        
        closetManager.createCloset(c1);
        closetManager.createCloset(c2);
        closetManager.createCloset(c3);
        closetManager.createCloset(closetWithNoItem);
        
        //a dalsi s nullId nebo notInDB
        /*
        graveWithNullId = newGrave(1,1,1,"Grave with null id");
        graveNotInDB = newGrave(1,1,1,"Grave not in DB");
        graveNotInDB.setId(g3.getId() + 100);
        bodyWithNullId = newBody("Body with null id", null, null, true);
        bodyNotInDB = newBody("Body not in DB", null, null, true);
        bodyNotInDB.setId(b5.getId() + 100);
        */
    }
    
    @Before
    public void setUpClass() throws SQLException {
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds, ClosetManager.class.getResource("createTables.sql"));
        manager = new DressroomManagerImpl();
        manager.setDataSource(ds);
        itemsManager = new ItemsManagerImpl();
        itemsManager.setDataSource(ds);
        closetManager = new ClosetManagerImpl();
        closetManager.setDataSource(ds);
        prepareTestData();
    }
    
    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(ds, ClosetManager.class.getResource("dropTables.sql"));
    }

    //
    @Test
    public void testGetAllItemsFromCloset(){
        assertTrue(manager.getAllItemsFromCloset(closetWithNoItem).isEmpty());
        manager.putItemInCloset(i1, closetWithNoItem);
        manager.putItemInCloset(i2, closetWithNoItem);
        
        List<Item> expected = Arrays.asList(i1, i2);
        List<Item> actual = manager.getAllItemsFromCloset(closetWithNoItem);
        Collections.sort(actual, idItemComparator);
        Collections.sort(expected, idItemComparator);

        assertEquals(expected, actual);
//        assertDeepEquals(expected, actual);
    }
    
    @Test
    public void putItemInCloset(){
        
        manager.putItemInCloset(i1, c1);
        manager.putItemInCloset(i2, c1);
        
        assertTrue(manager.isItemInCloset(i1, c1));
        assertTrue(manager.isItemInCloset(i2, c1));
    }
    
    /*
    @Test
    public void removeItemFromCloset(){
        manager.putItemInCloset(i1, c1);
        manager.putItemInCloset(i2, c1);
        manager.putItemInCloset(i3, c1);
        manager.putItemInCloset(i4, c2);
        
        manager.removeItemFromCloset(i1, c1);
        manager.removeItemFromCloset(i2, c1);
        manager.removeItemFromCloset(i3, c1);
        
        assertTrue(manager.getAllItemsFromCloset(c1).isEmpty());
        manager.removeItemFromCloset(i4, c2);
        assertTrue(manager.getAllItemsFromCloset(c2).isEmpty());
    }
    */
    @Test
    public void findClosetWithItem(){
        manager.putItemInCloset(i1, c1);
        manager.putItemInCloset(i2, c1);
        manager.putItemInCloset(i3, c1);
        manager.putItemInCloset(i4, c1);
        
        assertEquals(manager.findClosetWithItem(i1), c1);
        assertEquals(manager.findClosetWithItem(i4), c1);
        
        
        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void findClosetWithItemThatIsNull(){
        manager.findClosetWithItem(null);
    }
    
    
    @Test
    public void findItemsInClosetByType(){
        manager.putItemInCloset(i1, c1);
        manager.putItemInCloset(i2, c1);
        manager.putItemInCloset(i3, c1);
        manager.putItemInCloset(i4, c1);
                
        List<Item> expected = Arrays.asList(i1, i4);
        List<Item> actual = manager.findItemsInClosetByType(c1, "shirt 1");
        Collections.sort(actual, idItemComparator);
        Collections.sort(expected, idItemComparator);

        assertEquals(expected, actual);
//        assertDeepEquals(expected, actual);
    }
    
    @Test
    public void isItemInCloset(){
        assertFalse(manager.isItemInCloset(i1, c1));
        
        manager.putItemInCloset(i1, c1);
        
        assertTrue(manager.isItemInCloset(i1, c1));

    }
    
    
    private void assertDeepEquals(Closet expected, Closet actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getOwner(), actual.getOwner());
        assertEquals(expected.getName(), actual.getName());
    }
    
    private void assertDeepEquals(Item expected, Item actual) {
        assertEquals(expected.getId(), actual.getId());
        //assertEquals(expected.getCloset(), actual.getCloset());
        assertEquals(expected.getGender(), actual.getGender());
        assertEquals(expected.getAdded(), actual.getAdded());
        assertEquals(expected.getNote(), actual.getNote());
        assertEquals(expected.getSize(), actual.getSize());
        assertEquals(expected.getType(), actual.getType());
        
    }

    private void assertDeepEquals(List<Item> expectedList, List<Item> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Item expected = expectedList.get(i);
            Item actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }
    
    private static final Comparator<Item> idItemComparator = new Comparator<Item>() {

        @Override
        public int compare(Item o1, Item o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };

    private static Closet newCloset(String owner, String name){
        Closet closet = new Closet();
        closet.setOwner(owner);
        closet.setName(name);
        return closet; 
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
    
    
}
