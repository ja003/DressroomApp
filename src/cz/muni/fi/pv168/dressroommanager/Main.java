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
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Item item = new Item((long)1,"shirt",Gender.FEMALE,"s","good");
        System.out.println(item.toString());
        
    }
    
}
