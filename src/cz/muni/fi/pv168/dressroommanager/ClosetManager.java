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
public interface ClosetManager {
    
    public void createCloset(Closet closet);
    
    public void deleteCloset(Closet closet);
    
    public Closet getClosetById(Long id);
    
    public List<Closet> getAllClosets();
    
    public void updateCloset(Closet closet);
    
}
