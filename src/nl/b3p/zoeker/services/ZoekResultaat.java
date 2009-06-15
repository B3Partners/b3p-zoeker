/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.zoeker.services;

import java.util.ArrayList;
import nl.b3p.zoeker.configuratie.Attribuut;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opengis.geometry.BoundingBox;

/**
 * @author Roy
 */
public class ZoekResultaat {
    private static final Log log = LogFactory.getLog(ZoekResultaat.class);
    private ArrayList attributen=null;
    private Integer zoekConfigId=null;
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
        return zoekConfigId;
    }

    /**
     * @param zoekConfigId the zoekConfigId to set
     */
    public void setZoekConfigId(Integer zoekConfigId) {
        this.zoekConfigId = zoekConfigId;
    } 

    /**
     * @return the attributen
     */
    public ArrayList getAttributen() {
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
        if (getAttributen()==null){
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
        ArrayList waarden=getWaarden(Attribuut.ID_TYPE);
        if (waarden.size()==0){
            log.error("Geen resultaat attribuut geconfigureerd met type ID");
            return null;
        }else if (waarden.size()>1){
            log.error("Meerdere resultaat attributen geconfigureerd met type ID, dit mag er maar 1 zijn. De eerste wordt gebruikt");
        }
        return (String) waarden.get(0);
        
    }
    public ArrayList getExtraAttribuutWaarden(){
        return getWaarden(Attribuut.GEEN_TYPE);
    }
    /**
     * @return alle waarden van de attributen met TOON_TYPE aan elkaar gescheiden door een " "
     */
    public String getLabel(){
        String resultValue="";
        ArrayList waarden=getWaarden(Attribuut.TOON_TYPE);
        for (int i=0; i < waarden.size(); i++){
            if (resultValue.length()>0)
                    resultValue+=" ";
            resultValue+=waarden.get(i);
        }       
        return resultValue;
    }
    /**
     * Geeft een list met waarden(string) terug van alle zoekresultaatattributen met het meegegeven type.
     */
    private ArrayList getWaarden(int type){
        ArrayList returnValue=new ArrayList();
        if (getAttributen()!=null){            
            for (int i=0; i < getAttributen().size(); i++){
                ZoekResultaatAttribuut a=(ZoekResultaatAttribuut) getAttributen().get(i);
                if (a.getType()==type)
                    returnValue.add(a.getWaarde());
            }
        }
        return returnValue;
    }
}
