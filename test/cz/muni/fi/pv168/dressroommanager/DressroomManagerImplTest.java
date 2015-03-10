/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroommanager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
    
    public DressroomManagerImplTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testSomeMethod() {
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    //
    private void testGetAllItemsFromCloset(Closet closet){
        
        assertTrue(manager.getAllItemsFromCloset(closet).isEmpty());

        
        Item item = new Item("shirt",Gender.BOTH,"s","my shirt");
        Item item2 = new Item("pants",Gender.MALE,"42","my pants");

        manager.putItemInCloset(item, closet);
        manager.putItemInCloset(item2, closet);
        
        List<Item> expected = Arrays.asList(item, item2);
        List<Item> actual = manager.getAllItemsFromCloset(closet);
        Collections.sort(actual, idItemComparator);
        Collections.sort(expected, idItemComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }
    
    //private void putItemInCloset(Item item, Closet closet);
    
    //private void removeItemFromCloset(Item item, Closet closet);
    
    //private Closet findClosetWithItem(Item item);
    
    //private List<Item> findItemsInClosetByType(Closet closet, String type);
    
    
    
    
    private void assertDeepEquals(Closet expected, Closet actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getOwner(), actual.getOwner());
        assertEquals(expected.getName(), actual.getName());
    }
    
    private void assertDeepEquals(Item expected, Item actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getCloset(), actual.getCloset());
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
    
    
    
}
