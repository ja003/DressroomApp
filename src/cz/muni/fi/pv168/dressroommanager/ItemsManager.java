/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroommanager;

import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import cz.muni.fi.pv168.common.ValidationException;
import java.util.List;

/**
 *
 * @author Anna
 */
public interface ItemsManager{
    
    public void createItem(Item item) throws ServiceFailureException, ValidationException, IllegalEntityException;
    
    public void deleteItem(Item item)throws ServiceFailureException, IllegalEntityException;
    
    public Item getItemById(Long id)throws ServiceFailureException;
    
    public void updateItem(Item item)throws ServiceFailureException, ValidationException, IllegalEntityException;
    
    public List<Item> getAllItems() throws ServiceFailureException;
    
}
