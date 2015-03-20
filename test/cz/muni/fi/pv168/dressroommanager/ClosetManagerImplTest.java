/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroommanager;

import java.sql.SQLException;
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
public class ClosetManagerImplTest {
    
    private ClosetManager manager;
    
    

    /**
     * Test of createCloset method, of class ClosetManagerImpl.
     */
    @Test
    public void testCreateCloset() {
        Closet closet = newCloset("Adam","Adam - closet");
        manager.createCloset(closet);
        
        Long closetId = closet.getId();
        assertNotNull(closet);
        Closet result = manager.getClosetById(closetId);
        assertEquals(closet, result);
        assertNotSame(closet, result);
        assertDeepEquals(closet, result);
    }
    
    @Test
    public void deleteCloset() {

        Closet c1 = newCloset("Adam", "Adam - closet");
        Closet c2 = newCloset("Another", "Another - closet");
        manager.createCloset(c1);
        manager.createCloset(c2);
        
        assertNotNull(manager.getClosetById(c1.getId()));
        assertNotNull(manager.getClosetById(c2.getId()));

        manager.deleteCloset(c1);
        
        assertNull(manager.getClosetById(c1.getId()));
        assertNotNull(manager.getClosetById(c2.getId()));
                
    }

    /**
     * Test of getClosetById method, of class ClosetManagerImpl.
     */
    @Test
    public void testGetClosetById() {
        System.out.println("getClosetById");
        Long id = null;
        Closet expResult = null;
        Closet result = manager.getClosetById(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllClosets method, of class ClosetManagerImpl.
     */
    @Test
    public void testGetAllClosets() {
        System.out.println("Attempt to test get all airships");
        assertTrue(manager.getAllClosets().isEmpty());

        Closet c1 = newCloset("Adam","Adam - closet");
        Closet c2 = newCloset("Bert","Bert - closet");

        manager.createCloset(c1);
        manager.createCloset(c2);

        List<Closet> expected = Arrays.asList(c1,c2);
        List<Closet> actual = manager.getAllClosets();

        Collections.sort(actual,idComparator);
        Collections.sort(expected,idComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }
    
    
    @Test
    public void updateCloset() {
        Closet closet = newCloset("Adam", "Adam - closet");
        Closet c2 = newCloset("Adam", "Adam - imba closet");
        manager.createCloset(closet);
        manager.createCloset(c2);
        Long closetId = closet.getId();

        closet = manager.getClosetById(closetId);
        closet.setOwner("");
        manager.updateCloset(closet);        
        assertEquals("", closet.getOwner());
        assertEquals("Adam - closet", closet.getName());
        
        closet = manager.getClosetById(closetId);
        closet.setOwner(null);
        manager.updateCloset(closet);
        assertNull(closet.getOwner());
        assertEquals("Adam - closet", closet.getName());
        
        closet = manager.getClosetById(closetId);
        closet.setName("");
        closet.setOwner("Adam");
        manager.updateCloset(closet);        
        assertEquals("Adam", closet.getOwner());
        assertEquals("", closet.getName());

        closet = manager.getClosetById(closetId);
        closet.setName(null);
        manager.updateCloset(closet);        
        assertEquals("Adam", closet.getOwner());
        assertNull(closet.getName());

        // Check if updates haven't affected other records
        assertDeepEquals(c2, manager.getClosetById(c2.getId()));
    }
    
    ///not using try and catch block
    @Test(expected = IllegalArgumentException.class )
    public void addClosetWithNullAttribute() {
        manager.createCloset(null);
    }
    @Test(expected = IllegalArgumentException.class )
    public void addClosetWithWrongOwnerAttribute() {
        Closet closet = newCloset("666", "666 - closet");        
        manager.createCloset(closet);   //owner can not contain number
    }
    @Test(expected = IllegalArgumentException.class )
    public void addClosetWithEmptyOwnerAttribute() {
        Closet closet = newCloset("", "Adam - closet");         
        manager.createCloset(closet);   //owner must be > 0
    }
    @Test(expected = IllegalArgumentException.class )
    public void addClosetWithEmptyNameAttribute() {
        Closet closet = newCloset("Adam", "");         
        manager.createCloset(closet);   //name must be > 0
    }
    @Test(expected = IllegalArgumentException.class )
    public void addClosetWithEmptyOwnerAndNameAttributes() {
        Closet closet = newCloset("",""); 
        manager.createCloset(closet);   //owner and name must be > 0
    }
    @Test       //these should be OK
    public void addClosetWithOKAttributes() {
        Closet closet = newCloset("Adam", "Adam - closet");
        manager.createCloset(closet);
        Closet result = manager.getClosetById(closet.getId()); 
        assertNotNull(result);

        closet = newCloset("Anca", "Anca - closet");
        manager.createCloset(closet);
        result = manager.getClosetById(closet.getId()); 
        assertNotNull(result);

        closet = newCloset("Me", "My closet");
        manager.createCloset(closet);
        result = manager.getClosetById(closet.getId()); 
        assertNotNull(result);
    }

      
    
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteClosetNullCloset() {
        manager.deleteCloset(null);
    }
    
    //    @Test(expected = IllegalArgumentException.class)
//    public void testRemoveAirshipWrongId() {
//        Airship airship = newAirship("AirshipOne", BigDecimal.valueOf(140), 50);
//        airship.setId(1L);
//        manager.removeAirship(airship);
//    }
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteClosetNullId() {
        Closet closet = newCloset("owner 1", "closet number 1");
        closet.setId(null);
        manager.deleteCloset(closet);
    }
    
    
    
    
    
    
    ////////////////////////////////////////////
    
    private static Closet newCloset(String owner, String name){
        Closet closet = new Closet();
        closet.setOwner(owner);
        closet.setName(name);
        return closet; 
    }
    
    private void assertDeepEquals(List<Closet> expectedList, List<Closet> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Closet expected = expectedList.get(i);
            Closet actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }
     private void assertDeepEquals(Closet expected, Closet actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getOwner(), actual.getOwner());
    }
     
    private static Comparator<Closet> idComparator = new Comparator<Closet>() {
        @Override
        public int compare(Closet o1, Closet o2) {
            return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
        }
    };
    
    
    
}
