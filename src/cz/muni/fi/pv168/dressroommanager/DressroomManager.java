/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroommanager;

import java.util.List;


/**
 *
 * @author Vukmir
 */
public interface DressroomManager {
    
    public List<Item> getAllItemsFromCloset(Closet closet);
    
    public void putItemInCloset(Item item, Closet closet);
    
    //public void removeItemFromCloset(Item item, Closet closet);
    
    public Closet findClosetWithItem(Item item);
    
    public boolean isItemInCloset(Item item, Closet closet); 
    
    public List<Item> findItemsInClosetByType(Closet closet, String type);
    
    
    
}
