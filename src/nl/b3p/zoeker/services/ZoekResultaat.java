/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.zoeker.services;

import java.util.ArrayList;
import nl.b3p.zoeker.configuratie.Attribuut;
import nl.b3p.zoeker.configuratie.ZoekConfiguratie;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opengis.geometry.BoundingBox;

/**
 * @author Roy
 */
public class ZoekResultaat implements Comparable {

    private static final Log log = LogFactory.getLog(ZoekResultaat.class);
    private ArrayList<ZoekResultaatAttribuut> attributen = null;
    private Integer zoekConfigId = null;
    private ZoekConfiguratie zoekConfiguratie = null;
    private double maxx;
    private double maxy;
    private double minx;
    private double miny;

    /**
     * @return the maxX
     */
    public double getMaxx() {
        return maxx;
    }

    /**
     * @param maxX the maxX to set
     */
    public void setMaxx(double maxx) {
        this.maxx = maxx;
    }

    /**
     * @return the maxY
     */
    public double getMaxy() {
        return maxy;
    }

    /**
     * @param maxY the maxY to set
     */
    public void setMaxy(double maxy) {
        this.maxy = maxy;
    }

    /**
     * @return the minX
     */
    public double getMinx() {
        return minx;
    }

    /**
     * @param minX the minX to set
     */
    public void setMinx(double minx) {
        this.minx = minx;
    }

    /**
     * @return the minY
     */
    public double getMiny() {
        return miny;
    }

    /**
     * @param minY the minY to set
     */
    public void setMiny(double miny) {
        this.miny = miny;
    }

    /**
     * @return the zoekConfigId
     */
    public Integer getZoekConfigId() {
        if (getZoekConfiguratie() != null) {
            return getZoekConfiguratie().getId();
        }
        return null;
    }

    /**
     * @return the attributen
     */
    public ArrayList<ZoekResultaatAttribuut> getAttributen() {
        return attributen;
    }

    /**
     * @param attributen the attributen to set
     */
    public void setAttributen(ArrayList attributen) {
        this.attributen = attributen;
    }

    /** Voeg een attribuut toe
     */
    void addAttribuut(ZoekResultaatAttribuut o) {
        if (getAttributen() == null) {
            setAttributen(new ArrayList());
        }
        attributen.add(o);
    }

    /**
    Set the bbox
     */
    void setBbox(BoundingBox bounds) {
        this.setMaxx(bounds.getMaxX());
        this.setMaxy(bounds.getMaxY());
        this.setMinx(bounds.getMinX());
        this.setMiny(bounds.getMinY());
    }

    /**
     * @return de waarde van het zoekresultaatattribuut met type id (mag er maar 1 zijn)
     */
    public String getId() {
        ArrayList waarden = getWaarden(Attribuut.ID_TYPE);
        if (waarden.size() == 0) {
            return null;
        } else if (waarden.size() > 1) {
            log.error("Meerdere resultaat attributen geconfigureerd met type ID, dit mag er maar 1 zijn. De eerste wordt gebruikt");
        }
        return waarden.get(0).toString();

    }

    public ArrayList getExtraAttribuutWaarden() {
        return getWaarden(Attribuut.GEEN_TYPE);
    }

    /**
     * @return alle waarden van de attributen met TOON_TYPE aan elkaar gescheiden door een " "
     */
    public String getLabel() {
        String resultValue = "";
        ArrayList waarden = getWaarden(Attribuut.TOON_TYPE);
        for (int i = 0; i < waarden.size(); i++) {
            if (resultValue.length() > 0) {
                resultValue += " ";
            }
            if (waarden.get(i) != null) {
                resultValue += waarden.get(i).toString();
            }
        }
        return resultValue;
    }

    /**
     * Geeft een list met waarden(string) terug van alle zoekresultaatattributen met het meegegeven type.
     */
    private ArrayList getWaarden(int type) {
        ArrayList returnValue = new ArrayList();
        if (getAttributen() != null) {
            for (int i = 0; i < getAttributen().size(); i++) {
                ZoekResultaatAttribuut a = (ZoekResultaatAttribuut) getAttributen().get(i);
                if (a.getType() != null && a.getType() == type) {
                    returnValue.add(a.getWaarde());
                }
            }
        }
        return returnValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ZoekResultaat) {
            ZoekResultaat z = (ZoekResultaat) o;
            if (this.getId() != null && z.getId() != null) {
                return this.getId().equals(z.getId());
            }
        }
        return false;
    }
    /**
     * Compares o with this zoekresultaat
     * @param o see compare interface
     * @return see compare interface
     */
    public int compareTo(Object o) {
        //if not instance of zoekresultaat this wins.
        if (!(o instanceof ZoekResultaat)) {
            return 1;
        } else {            
            ZoekResultaat zoekResultaat = (ZoekResultaat) o;
            ArrayList<ZoekResultaatAttribuut> zAttributen = zoekResultaat.getAttributen();
            int compareResult = 0;
            //walk over all attributes and compare the attributes with type = 'TOON_TYPE';
            for (int i = 0; i < this.attributen.size() && compareResult == 0; i++) {
                ZoekResultaatAttribuut thisZra = this.attributen.get(i);
                //only compare if type = TOON_TYPE (these will be displayed)
                if (thisZra.getType().equals(Attribuut.TOON_TYPE)) {
                    for (int z = 0; z < zAttributen.size(); z++) {
                        ZoekResultaatAttribuut zra = zAttributen.get(z);
                        //if the names are the same do a compare
                        if (thisZra.getAttribuutnaam() != null && zra.getAttribuutnaam() != null && thisZra.getAttribuutnaam().equals(zra.getAttribuutnaam())) {
                            if (thisZra.getWaarde() == null) {
                                compareResult = -1;
                            } else if (zra.getWaarde() == null) {
                                compareResult = 1;
                            } else if (thisZra.getWaarde() instanceof Comparable) {
                                //most services give the numbers in string format. Try to numberFormat the strings and then compare
                                boolean NaN = false;
                                if (thisZra.getWaarde() instanceof String && zra.getWaarde() instanceof String){
                                    try {
                                        Double thisD = new Double(thisZra.getWaarde().toString());
                                        Double d = new Double(zra.getWaarde().toString());
                                        compareResult = thisD.compareTo(d);
                                    } catch (NumberFormatException nfe) {
                                        NaN = true;
                                    }
                                }
                                if (NaN) {
                                    compareResult = ((Comparable) thisZra.getWaarde()).compareTo(zra.getWaarde());
                                }
                            } else {
                                compareResult = thisZra.getWaarde().toString().compareToIgnoreCase(zra.getWaarde().toString());
                            }
                            continue;
                        }
                    }
                }
            }
            return compareResult;
        }
    }

    public ZoekConfiguratie getZoekConfiguratie() {
        return zoekConfiguratie;
    }

    public void setZoekConfiguratie(ZoekConfiguratie zoekConfiguratie) {
        this.zoekConfiguratie = zoekConfiguratie;
    }
}
