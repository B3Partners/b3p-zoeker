/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.zoeker.services;

import java.util.Comparator;

/**
 *
 * @author Roy Braam
 */
public class ResultaatLabelComparator implements Comparator{

    public int compare(Object o1, Object o2) {
        if (o1 instanceof ZoekResultaat && o2 instanceof ZoekResultaat){
            ZoekResultaat z1 = (ZoekResultaat)o1;
            ZoekResultaat z2 = (ZoekResultaat)o2;
            int compare = z1.getLabel().compareToIgnoreCase(z2.getLabel());
            if (compare==0){
                return z1.compareTo(o2);
            }
            return compare;
        }
        return 0;
    }
    
}
