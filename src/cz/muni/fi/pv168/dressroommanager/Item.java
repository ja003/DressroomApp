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
    
    public Item() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setCloset(Closet closet) {
        this.closet = closet;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Date getAdded() {
        return added;
    }

    public Gender getGender() {
        return gender;
    }

    public String getSize() {
        return size;
    }

    public String getNote() {
        return note;
    }

    public Closet getCloset() {
        return closet;
    }
    
    
    
}
