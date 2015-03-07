/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroommanager;

/**
 *
 * @author Vukmir
 */
public interface ItemsManager{
    
    public void createItem(Item item);
    
    public void deleteItem(Item item);
    
    public Item getItemById(Long id);
    
    public void updateItem(Item item);
    
}
