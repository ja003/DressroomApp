/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.dressroomAppGui;
import cz.muni.fi.pv168.dressroommanager.Closet;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 *
 * @author Vukmir
 */
public class ClosetsComboBoxModel extends AbstractListModel implements ComboBoxModel{
    private String selectedClosetString;
    private List<Closet> closets;
    
    public ClosetsComboBoxModel(List closets){
        this.closets = closets;
    }
    
    @Override
    public String getSelectedItem(){
        return selectedClosetString;
    }
    
    ////////////////////////////////////*********************OPRAVIT*********************//////////////////////////////////////
    @Override 
    public void setSelectedItem(Object newValue) {
            for (Closet c: closets){
                //if (newValue.equals(c)){
                if (newValue.equals(c) || newValue.toString().equals(c.getOwner())){
                    selectedClosetString = c.getOwner();
                    break;
                }
            }
      }
    
    
    @Override
      public int getSize() {
        return closets.size();
      }

    @Override
    public String getElementAt(int i) {
      return closets.get(i).getOwner();
    }
    
    
      
    
    
    
}
