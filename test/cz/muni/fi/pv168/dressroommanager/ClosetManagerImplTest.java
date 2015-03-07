/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroommanager;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vukmir
 */
public class ClosetManagerImplTest {
    
    public ClosetManagerImplTest() {
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

    /**
     * Test of createCloset method, of class ClosetManagerImpl.
     */
    @Test
    public void testCreateCloset() {
        System.out.println("createCloset");
        Closet closet = null;
        ClosetManagerImpl instance = new ClosetManagerImpl();
        instance.createCloset(closet);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteCloset method, of class ClosetManagerImpl.
     */
    @Test
    public void testDeleteCloset() {
        System.out.println("deleteCloset");
        Closet closet = null;
        ClosetManagerImpl instance = new ClosetManagerImpl();
        instance.deleteCloset(closet);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getClosetById method, of class ClosetManagerImpl.
     */
    @Test
    public void testGetClosetById() {
        System.out.println("getClosetById");
        Long id = null;
        ClosetManagerImpl instance = new ClosetManagerImpl();
        Closet expResult = null;
        Closet result = instance.getClosetById(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllClosets method, of class ClosetManagerImpl.
     */
    @Test
    public void testGetAllClosets() {
        System.out.println("getAllClosets");
        ClosetManagerImpl instance = new ClosetManagerImpl();
        List<Closet> expResult = null;
        List<Closet> result = instance.getAllClosets();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
