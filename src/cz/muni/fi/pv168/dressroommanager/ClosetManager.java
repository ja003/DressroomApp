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
 * @author Vukmir
 */
public interface ClosetManager {
    
    public void createCloset(Closet closet) throws ServiceFailureException, ValidationException, IllegalEntityException;
    
    public void deleteCloset(Closet closet) throws ServiceFailureException, IllegalEntityException;
    
    public Closet getClosetById(Long id) throws ServiceFailureException;
    
    public List<Closet> getAllClosets() throws ServiceFailureException;
    
    public void updateCloset(Closet closet) throws ServiceFailureException, ValidationException, IllegalEntityException;
    
}
