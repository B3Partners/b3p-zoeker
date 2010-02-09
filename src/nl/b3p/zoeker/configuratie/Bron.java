/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.zoeker.configuratie;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Roy
 */
public class Bron {

    private Integer id=null;
    private String naam = null;
    private String url = null;
    private String gebruikersnaam=null;
    private String wachtwoord=null;
    private Integer volgorde=null;

    public Bron() {
    }
    public Bron(Integer id, String naam, String url, String gebruikersnaam, String wachtwoord, Integer volgorde){
        this.id=id;
        this.naam=naam;
        this.url=url;
        this.gebruikersnaam=gebruikersnaam;
        this.wachtwoord=wachtwoord;
        this.volgorde=volgorde;
    }
    public Bron(Integer id, String naam, String url){
        this(id,naam,url,null,null,null);
    }

//getters and setters
    /**
     * @return the Id
     */
    public Integer getId(){
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id){
        this.id=id;
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

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the volgorde
     */
    public Integer getVolgorde() {
        return volgorde;
    }

    /**
     * @param volgorde the volgorde to set
     */
    public void setVolgorde(Integer volgorde) {
        this.volgorde = volgorde;
    }

    /**
     * @return the gebruikersnaam
     */
    public String getGebruikersnaam() {
        return gebruikersnaam;
    }

    /**
     * @param gebruikersnaam the gebruikersnaam to set
     */
    public void setGebruikersnaam(String gebruikersnaam) {
        this.gebruikersnaam = gebruikersnaam;
    }

    /**
     * @return the wachtwoord
     */
    public String getWachtwoord() {
        return wachtwoord;
    }

    /**
     * @param wachtwoord the wachtwoord to set
     */
    public void setWachtwoord(String wachtwoord) {
        this.wachtwoord = wachtwoord;
    }

    public JSONObject toJSON() throws JSONException{
        JSONObject json= new JSONObject();
        json.put("id", id);
        json.put("naam", getNaam());
        json.put("url", getUrl());
        json.put("volgorde", getVolgorde());
        return json;
    }
}
