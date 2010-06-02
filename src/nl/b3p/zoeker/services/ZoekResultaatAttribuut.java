/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.zoeker.services;

import com.vividsolutions.jts.geom.Geometry;
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
        /* Als de waarde van het type geometry is dan omzetten naar een string.
         * Dit is omdat anders DWR het niet snapt zonder converter te defineren en dus een NULL object
         * terug geeft aan het javascript.
        */
        if (waarde instanceof Geometry){
            this.waarde=((Geometry)waarde).toText();
        }else{
            this.waarde = waarde;
        }
    }
}
