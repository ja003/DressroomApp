/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.dressroommanager;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Anna
 */
public class ItemsManagerImplTest {
    private ItemsManagerImpl manager;
    
    @Before
    public void setUp() {
        manager = new ItemsManagerImpl();
    }
    
    @Test
    public void createItem(){
        System.out.println("test createItem");
        
        Item item = newItem(1L, "shirt", Gender.FEMALE, "XS", " ");
        manager.createItem(item);
        
        Long itemId = item.getId();
        assertNotNull(itemId);
        Item result = manager.getItemById(itemId);
        assertEquals(item, result);
        assertNotSame(item, result);
    }
    
    // test for generating Id
    @Test
    public void createItemWithNullId(){
        System.out.println("test createItemWithNullId");
        
        Item item = newItem("shirt", Gender.FEMALE, "XS", " ");
        manager.createItem(item);
        
        assertNotNull(item.getId());
        Item result = manager.getItemById(item.getId());
        assertEquals(item, result);
        assertNotSame(item, result);
    }
    
    @Test
    public void updateItem(){
        System.out.println("test updateItem");
        
        Item item1 = newItem(1L, "shirt", Gender.FEMALE, "XS", " ");
        Item item2 = newItem(1L, "skirt", Gender.FEMALE, "S", "popis");
        manager.createItem(item1);
        manager.createItem(item2);
    
        //finish this code...
    }

    @Test
    public void deleteItem(){
        System.out.println("test deleteItem");
        
        Item item1 = newItem(1L, "shirt", Gender.FEMALE, "XS", " ");
        Item item2 = newItem(1L, "skirt", Gender.FEMALE, "S", "popis");
        manager.createItem(item1);
        manager.createItem(item2);
        
        assertNotNull(manager.getItemById(item1.getId()));
        assertNotNull(manager.getItemById(item2.getId()));
        
        manager.deleteItem(item1);
        
        assertNull(manager.getItemById(item1.getId()));
        assertNotNull(manager.getItemById(item2.getId()));
    }
        
    private static Item newItem(Long id, String type, Gender gender, String size, String note){
        Item item = new Item();
        item.setId(id);
        item.setType(type);
        item.setGender(gender);
        item.setSize(size);
        item.setNote(note);
        return item; 
    }
    
    private static Item newItem(String type, Gender gender, String size, String note){
        Item item = new Item();
        item.setType(type);
        item.setGender(gender);
        item.setSize(size);
        item.setNote(note);
        return item; 
    }

    private void assertDeepEquals(Item item, Item result) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
