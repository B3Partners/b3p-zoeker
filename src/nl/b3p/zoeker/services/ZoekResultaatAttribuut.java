/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.zoeker.services;

import nl.b3p.zoeker.configuratie.Attribuut;
import nl.b3p.zoeker.configuratie.ResultaatAttribuut;

/**
 *
 * @author Roy
 */
public class ZoekResultaatAttribuut extends Attribuut{
    private Object waarde=null;

    public ZoekResultaatAttribuut(ResultaatAttribuut ra) {
        super(ra);
    }

    /**
     * @return the waarde
     */
    public Object getWaarde() {
        return waarde;
    }

    /**
     * @param waarde the waarde to set
     */
    public void setWaarde(Object waarde) {
        this.waarde = waarde;
    }
}
