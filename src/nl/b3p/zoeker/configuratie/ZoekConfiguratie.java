/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.zoeker.configuratie;

import java.util.Set;
import org.json.JSONObject;

/**
 *
 * @author Roy
 */
public class ZoekConfiguratie {
    /*
     * String[] featureTypes= {"app:Plangebied"};
     * String[] searchPropertys={"app:overheidscode"};
     */
    private Integer id=null;
    // de te tonen search naam.
    private String naam=null;
    // tabel naam/feature type.
    private String featureType=null;
    // kolom waarop gezocht moet worden.
    private Set zoekVelden=null;
    // alle resultaat velden.
    private Set resultaatVelden=null;
    // kolom /attribuut waar het id van het gezochte object in staat.
    private ZoekConfiguratie parentZoekConfiguratie = null;
    // bron
    private Bron parentBron=null;

    public ZoekConfiguratie(){
    
    }

    /*public ZoekConfiguratie(String featureType,Set zoekVelden,String idAttribuut, String toonAttribuut){
        setFeatureType(featureType);
        setZoekVelden(zoekVelden);
        setIdAttribuut(idAttribuut);
        setToonAttribuut(toonAttribuut);
    }*/
    
    //getters and setters
    /**
     * @return the featureType
     */
    public String getFeatureType() {
        return featureType;
    }

    /**
     * @param featureType the featureType to set
     */
    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    /**
     * @return the searchProperty
     */
    public Set getZoekVelden() {
        return zoekVelden;
    }

    /**
     * @param searchProperty the searchProperty to set
     */
    public void setZoekVelden(Set zoekVelden) {
        this.zoekVelden = zoekVelden;
    }
    
    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the parentZoekConfiguratie
     */
    public ZoekConfiguratie getParentZoekConfiguratie() {
        return parentZoekConfiguratie;
    }

    /**
     * @param parentZoekConfiguratie the parentZoekConfiguratie to set
     */
    public void setParentZoekConfiguratie(ZoekConfiguratie parentZoekConfiguratie) {
        this.parentZoekConfiguratie = parentZoekConfiguratie;
    }

    /**
     * @return the parentBron
     */
    public Bron getParentBron() {
        return parentBron;
    }

    /**
     * @param parentBron the parentBron to set
     */
    public void setParentBron(Bron parentBron) {
        this.parentBron = parentBron;
    }

    /**
     * @return the naam
     */
    public String getNaam() {
        return naam;
    }

    /**
     * @param naam the naam to set
     */
    public void setNaam(String naam) {
        this.naam = naam;
    }

    public Bron getBron(){
        if (getParentBron()!=null){
            return getParentBron();
        }else if (getParentZoekConfiguratie()!=null){
            return getParentZoekConfiguratie().getBron();
        }else{
            return null;
        }
    }    

    public JSONObject toJSON(){
        JSONObject json= new JSONObject();
        return json;
    }

    /**
     * @return the resultaatVelden
     */
    public Set getResultaatVelden() {
        return resultaatVelden;
    }

    /**
     * @param resultaatVelden the resultaatVelden to set
     */
    public void setResultaatVelden(Set resultaatVelden) {
        this.resultaatVelden = resultaatVelden;
    }
}
