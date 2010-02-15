/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.zoeker.configuratie;

import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Roy
 */
public class ZoekAttribuut extends Attribuut{

    public ZoekAttribuut(){
        
    }
    public ZoekAttribuut(Integer id, String naam, String attribuutnaam, String label, Integer type, Integer volgorde) {
        super(id,naam,attribuutnaam,label,type,volgorde);
    }

    public static ZoekAttribuut[] setToZoekVeldenArray(Set set){
        ZoekAttribuut[] zoekattributen= new ZoekAttribuut[set.size()];
        Iterator it= set.iterator();
        for (int i=0; it.hasNext(); i++){
            zoekattributen[i]=(ZoekAttribuut) it.next();
        }
        return zoekattributen;
    }

    public boolean isFilterMogelijk(){
        if (this.getType()==null){
            return true;
        }
        return this.getType()<100;
    }
   
}
