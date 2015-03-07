/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroommanager;

import java.util.Date;

/**
 *
 * @author Vukmir
 */
public class Item  {
    private Long id;
    private String type;
    private Date added;
    private Gender gender;
    private String size;
    private String note;
    private Closet closet;

    public Item(Long id, String type, Gender gender, String size, String note ) {
        this.id = id;
        this.type = type;
        this.gender = gender;
        this.size = size;
        this.note = note;
    }
    
    
    
    
    
}
