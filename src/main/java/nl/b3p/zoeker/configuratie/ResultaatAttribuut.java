/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.zoeker.configuratie;

/**
 *
 * @author Roy
 */
public class ResultaatAttribuut extends Attribuut{

    public ResultaatAttribuut(){
        
    }
    
    public ResultaatAttribuut(Integer id, String naam, String attribuutnaam,
            String label, Integer type, Integer volgorde) {
        
        super(id,naam,attribuutnaam,label,type,volgorde,null,null);
    }
    
    public ResultaatAttribuut(Integer id, String naam, String attribuutnaam,
            String label, Integer type, Integer volgorde, String omschrijving) {
        
        super(id,naam,attribuutnaam,label,type,volgorde,omschrijving,null);
    }
    
    public ResultaatAttribuut(Integer id, String naam, String attribuutnaam,
            String label, Integer type, Integer volgorde, String omschrijving, String dropDownValues) {
        super(id,naam,attribuutnaam,label,type,volgorde,omschrijving,null);
    }

}
