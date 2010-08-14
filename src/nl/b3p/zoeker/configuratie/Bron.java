/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.zoeker.configuratie;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.oracle.OracleDataStoreFactory;
import org.geotools.data.ows.WFSCapabilities;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.v1_0_0.WFS_1_0_0_DataStore;
import org.geotools.filter.FilterCapabilities;
import org.geotools.xml.DocumentFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

/**
 *
 * @author Roy
 */
public class Bron {

    private Integer id = null;
    private String naam = null;
    private String url = null;
    private String gebruikersnaam = null;
    private String wachtwoord = null;
    private Integer volgorde = null;
    private static final int TIMEOUT = 60000;

    public static final String TYPE_JDBC = "jdbc";
    public static final String TYPE_ORACLE = "oracle";
    public static final String TYPE_WFS = "wfs";

    public Bron() {
    }

    public Bron(Integer id, String naam, String url, String gebruikersnaam, String wachtwoord, Integer volgorde) {
        this.id = id;
        this.naam = naam;
        this.url = url;
        this.gebruikersnaam = gebruikersnaam;
        this.wachtwoord = wachtwoord;
        this.volgorde = volgorde;
    }

    public Bron(Integer id, String naam, String url) {
        this(id, naam, url, null, null, null);
    }

//getters and setters
    /**
     * @return the Id
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

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("naam", getNaam());
        json.put("url", getUrl());
        json.put("volgorde", getVolgorde());
        return json;
    }

    public boolean checkType(String type) {
        if (this.getUrl() == null || type == null || type.length() == 0) {
            return false;
        }
        if (this.getUrl().toLowerCase().startsWith("jdbc:")) {
            if (this.getUrl().toLowerCase().startsWith("jdbc:oracle:")) {
                if (type.equals(TYPE_ORACLE)) {
                    return true;
                }
                return false;
            }
            if (type.equals(TYPE_JDBC)) {
                return true;
            }
        } else if (type.equals(TYPE_WFS)) {
            return true;
        }
        return false;
    }

    public DataStore toDatastore() throws IOException {

        if (this.getUrl() == null) {
            return null;
        }
        HashMap params = new HashMap();
        if (checkType(TYPE_ORACLE)) {
            //jdbc:oracle:thin:@b3p-demoserver:1521:ORCL
            int firstIndex;
            int lastIndex;
            firstIndex = this.getUrl().indexOf("@") + 1;
            lastIndex = this.getUrl().indexOf(":", firstIndex);
            String host = this.getUrl().substring(firstIndex, lastIndex);
            firstIndex = lastIndex + 1;
            lastIndex = this.getUrl().indexOf(":", firstIndex);
            String port = this.getUrl().substring(firstIndex, lastIndex);
            firstIndex = lastIndex + 1;
            lastIndex = this.getUrl().indexOf(".", firstIndex);
            String schema = null;
            if (lastIndex == -1) {
                lastIndex = this.getUrl().length();
            } else {
                schema = this.getUrl().substring(lastIndex + 1, this.getUrl().length());
            }
            String instance = this.getUrl().substring(firstIndex, lastIndex);
            params.put("host", host);
            params.put("port", port);
            if (schema != null) {
                params.put("schema", schema);
            }
            params.put("instance", instance);
            params.put("user", this.getGebruikersnaam());
            params.put("passwd", this.getWachtwoord());
            params.put("dbtype", "oracle");
            return (new OracleDataStoreFactory()).createDataStore(params);
        }
        if (checkType(TYPE_JDBC)) {
            //jdbc:postgresql://localhost:5432/edamvolendam_gis
            int firstIndex;
            int lastIndex;
            firstIndex = this.getUrl().indexOf("//") + 2;
            lastIndex = this.getUrl().indexOf(":", firstIndex);
            String host = this.getUrl().substring(firstIndex, lastIndex);
            firstIndex = lastIndex + 1;
            lastIndex = this.getUrl().indexOf("/", firstIndex);
            String port = this.getUrl().substring(firstIndex, lastIndex);
            firstIndex = lastIndex + 1;
            String database = this.getUrl().substring(firstIndex, this.getUrl().length());
            String schema = "public";
            if (database.indexOf(".") >= 0) {
                String[] tokens = database.split("\\.");
                if (tokens.length == 2) {
                    schema = tokens[1];
                    database = tokens[0];
                }
            }
            params.put(PostgisDataStoreFactory.DBTYPE.key, "postgis");
            params.put(PostgisDataStoreFactory.HOST.key, host);
            params.put(PostgisDataStoreFactory.PORT.key, port);
            params.put(PostgisDataStoreFactory.SCHEMA.key, schema);
            params.put(PostgisDataStoreFactory.DATABASE.key, database);
            if (this.getGebruikersnaam() != null) {
                params.put(PostgisDataStoreFactory.USER.key, this.getGebruikersnaam());
            }
            if (this.getWachtwoord() != null) {
                params.put(PostgisDataStoreFactory.PASSWD.key, this.getWachtwoord());
            }
        }
        if (checkType(TYPE_WFS)) {
            String url = this.getUrl();
            if (this.getUrl().toLowerCase().indexOf("request=") == -1) {
                if (url.indexOf("?") > 0) {
                    url += "&";
                } else {
                    url += "?";
                }
                url += "request=GetCapabilities&service=WFS";
                //temp hack: default use version 1.0.0
                if (url.toLowerCase().indexOf("version") == -1) {
                    url += "&Version=1.0.0";
                }
            }
            params.put(WFSDataStoreFactory.URL.key, url);
            if (this.getGebruikersnaam() != null) {
                params.put(WFSDataStoreFactory.USERNAME.key, this.getGebruikersnaam());
            }
            if (this.getWachtwoord() != null) {
                params.put(WFSDataStoreFactory.PASSWORD.key, this.getWachtwoord());
            }
            params.put(WFSDataStoreFactory.TIMEOUT.key, TIMEOUT);
        }
        DataStore ds = DataStoreFinder.getDataStore(params);
        
        /*omdat de WFS_1_0_0_Datastore niet met de opengis filters werkt even toevoegen dat
        er simpelle vergelijkingen kunnen worden gedaan. (de meeste servers kunnen dit natuurlijk);*/
        if (ds instanceof WFS_1_0_0_DataStore) {
            WFS_1_0_0_DataStore wfs100ds = (WFS_1_0_0_DataStore) ds;
            WFSCapabilities wfscap = wfs100ds.getCapabilities();
            FilterCapabilities filterCap = wfscap.getFilterCapabilities();
            filterCap.addAll(FilterCapabilities.SIMPLE_COMPARISONS_OPENGIS);
            boolean b=filterCap.supports(FilterCapabilities.SIMPLE_COMPARISONS_OPENGIS);
            wfscap.setFilterCapabilities(filterCap);
        }
        return ds;
    }

    public String toString() {
        String returnValue = "";
        if (getNaam() != null) {
            returnValue += getNaam() + " ";
        }
        if (getUrl() != null) {
            returnValue += "(" + getUrl() + ")";
        }
        return returnValue;
    }


    public static void main(String [] args) throws UnsupportedEncodingException, IOException {
//debug stukje
        String wfsCapabilitiesRawData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><WFS_Capabilities xmlns=\"http://www.opengis.net/wfs\" xmlns:app=\"http://www.deegree.org/app\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengeospatial.net/wfs/1.0.0/WFS-capabilities.xsd\" version=\"1.0.0\" updateSequence=\"0\"><Service><Name>Ruimtelijke ordeningsplannen (RO-Online WFS)</Name><Title>RO-Online: De landelijke voorziening voor digitale ruimtelijke ordeningsplannen</Title><Keywords>ruimtelijke ordening planologie bestemmingsplannen structuurvisies AMvB provinciale verordeningen wet ruimtelijke ordening besluit ruimtelijke ordening IMRO</Keywords><OnlineResource xsi:type=\"java:java.lang.String\">http://localhost:8084/kaartenbalie/services/</OnlineResource><Fees>none</Fees><AccessConstraints>NONE</AccessConstraints></Service><Capability><Request><GetFeature><ResultFormat><GML2/></ResultFormat><DCPType><HTTP><Get onlineResource=\"http://localhost:8084/kaartenbalie/services/\"/><Post onlineResource=\"http://localhost:8084/kaartenbalie/services/\"/></HTTP></DCPType></GetFeature><DescribeFeatureType><SchemaDescriptionLanguage><XMLSCHEMA/></SchemaDescriptionLanguage><DCPType><HTTP><Get onlineResource=\"http://localhost:8084/kaartenbalie/services/\"/><Post onlineResource=\"http://localhost:8084/kaartenbalie/services/\"/></HTTP></DCPType></DescribeFeatureType><GetCapabilities><DCPType><HTTP><Get onlineResource=\"http://localhost:8084/kaartenbalie/services/\"/><Post onlineResource=\"http://localhost:8084/kaartenbalie/services/\"/></HTTP></DCPType></GetCapabilities></Request></Capability><FeatureTypeList><FeatureType><Name>app:roowfs_Gebiedsaanduiding</Name><Title>app:Gebiedsaanduiding</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_ProvinciaalComplex</Name><Title>app:ProvinciaalComplex</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_ProvinciaalVerbinding</Name><Title>app:ProvinciaalVerbinding</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisiecomplex_R</Name><Title>app:Structuurvisiecomplex_R</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisiecomplex_P</Name><Title>app:Structuurvisiecomplex_P</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Plangebied</Name><Title>app:Plangebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Enkelbestemming</Name><Title>app:Enkelbestemming</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitgebied_P</Name><Title>app:Besluitgebied_P</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_NationaalGebied</Name><Title>app:NationaalGebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Figuur</Name><Title>app:Figuur</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitgebied_X</Name><Title>app:Besluitgebied_X</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Plangebied_PCP</Name><Title>app:Plangebied_PCP</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitvlak_A</Name><Title>app:Besluitvlak_A</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_ProvinciaalPlangebied</Name><Title>app:ProvinciaalPlangebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisiecomplex_G</Name><Title>app:Structuurvisiecomplex_G</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Dubbelbestemming</Name><Title>app:Dubbelbestemming</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitgebied</Name><Title>app:Besluitgebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_PlangebiedDigitaalWaarmerk</Name><Title>app:PlangebiedDigitaalWaarmerk</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitsubvlak_X</Name><Title>app:Besluitsubvlak_X</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisiegebied_G</Name><Title>app:Structuurvisiegebied_G</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Bouwvlak</Name><Title>app:Bouwvlak</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Bestemmingsplangebied</Name><Title>app:Bestemmingsplangebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitvlak_X</Name><Title>app:Besluitvlak_X</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisiegebied_R</Name><Title>app:Structuurvisiegebied_R</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisiegebied_P</Name><Title>app:Structuurvisiegebied_P</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitsubvlak_P</Name><Title>app:Besluitsubvlak_P</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_NationaalVerbinding</Name><Title>app:NationaalVerbinding</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_OnderdelenDigitaalWaarmerk</Name><Title>app:OnderdelenDigitaalWaarmerk</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Onthoudingsgebied</Name><Title>app:Onthoudingsgebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Functieaanduiding</Name><Title>app:Functieaanduiding</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisieverklaring_P</Name><Title>app:Structuurvisieverklaring_P</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitvlak_P</Name><Title>app:Besluitvlak_P</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_NationaalComplex</Name><Title>app:NationaalComplex</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Maatvoering</Name><Title>app:Maatvoering</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_NationaalPlangebied</Name><Title>app:NationaalPlangebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Bouwaanduiding</Name><Title>app:Bouwaanduiding</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitsubvlak_A</Name><Title>app:Besluitsubvlak_A</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisieplangebied_G</Name><Title>app:Structuurvisieplangebied_G</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Lettertekenaanduiding</Name><Title>app:Lettertekenaanduiding</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisieplangebied_R</Name><Title>app:Structuurvisieplangebied_R</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_ProvinciaalGebied</Name><Title>app:ProvinciaalGebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisieplangebied_P</Name><Title>app:Structuurvisieplangebied_P</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitgebied_A</Name><Title>app:Besluitgebied_A</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>demowfs_bebouwdekom_nl</Name><Title>bebouwdekom_nl</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"3.22989\" miny=\"50.709\" maxx=\"7.27394\" maxy=\"53.5672\"/></FeatureType><FeatureType><Name>demowfs_bebouwdekom nl</Name><Title>bebouwdekom nl</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"3.22989\" miny=\"50.709\" maxx=\"7.27394\" maxy=\"53.5672\"/></FeatureType><FeatureType><Name>demowfs_basis_nl</Name><Title>basis_nl</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType><FeatureType><Name>demowfs_autowegen_nl</Name><Title>autowegen_nl</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType><FeatureType><Name>demowfs_autowegen_elabels</Name><Title>autowegen_elabels</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType><FeatureType><Name>demowfs_rivieren_nl</Name><Title>rivieren_nl</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType><FeatureType><Name>demowfs_gemeenten_2006</Name><Title>gemeenten_2006</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"3.22989\" miny=\"50.709\" maxx=\"7.27394\" maxy=\"53.5672\"/></FeatureType><FeatureType><Name>demowfs_wijken_2006</Name><Title>wijken_2006</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType><FeatureType><Name>demowfs_buurten_2006</Name><Title>buurten_2006</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType><FeatureType><Name>demowfs_plan_lijnen</Name><Title>plan_lijnen</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType><FeatureType><Name>demowfs_plan_polygonen</Name><Title>plan_polygonen</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType></FeatureTypeList><ogc:Filter_Capabilities><ogc:Spatial_Capabilities><ogc:Spatial_Operators><ogc:BBOX/></ogc:Spatial_Operators></ogc:Spatial_Capabilities><ogc:Scalar_Capabilities><ogc:Comparison_Operators><ogc:Simple_Comparisons/><ogc:Like/><ogc:Between/><ogc:NullCheck/></ogc:Comparison_Operators></ogc:Scalar_Capabilities></ogc:Filter_Capabilities></WFS_Capabilities>";
        String wfsCapabilitiesRawData2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><WFS_Capabilities xmlns=\"http://www.opengis.net/wfs\" xmlns:app=\"http://www.deegree.org/app\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.0.0\" updateSequence=\"0\"><Service><Name>Ruimtelijke ordeningsplannen (RO-Online WFS)</Name><Title>RO-Online: De landelijke voorziening voor digitale ruimtelijke ordeningsplannen</Title><Keywords>ruimtelijke ordening planologie bestemmingsplannen structuurvisies AMvB provinciale verordeningen wet ruimtelijke ordening besluit ruimtelijke ordening IMRO</Keywords><OnlineResource xsi:type=\"java:java.lang.String\">http://localhost:8084/kaartenbalie/services/</OnlineResource><Fees>none</Fees><AccessConstraints>NONE</AccessConstraints></Service><Capability><Request><GetFeature><ResultFormat><GML2/></ResultFormat><DCPType><HTTP><Get onlineResource=\"http://localhost:8084/kaartenbalie/services/\"/><Post onlineResource=\"http://localhost:8084/kaartenbalie/services/\"/></HTTP></DCPType></GetFeature><DescribeFeatureType><SchemaDescriptionLanguage><XMLSCHEMA/></SchemaDescriptionLanguage><DCPType><HTTP><Get onlineResource=\"http://localhost:8084/kaartenbalie/services/\"/><Post onlineResource=\"http://localhost:8084/kaartenbalie/services/\"/></HTTP></DCPType></DescribeFeatureType><GetCapabilities><DCPType><HTTP><Get onlineResource=\"http://localhost:8084/kaartenbalie/services/\"/><Post onlineResource=\"http://localhost:8084/kaartenbalie/services/\"/></HTTP></DCPType></GetCapabilities></Request></Capability><FeatureTypeList><FeatureType><Name>app:roowfs_Gebiedsaanduiding</Name><Title>app:Gebiedsaanduiding</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_ProvinciaalComplex</Name><Title>app:ProvinciaalComplex</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_ProvinciaalVerbinding</Name><Title>app:ProvinciaalVerbinding</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisiecomplex_R</Name><Title>app:Structuurvisiecomplex_R</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisiecomplex_P</Name><Title>app:Structuurvisiecomplex_P</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Plangebied</Name><Title>app:Plangebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Enkelbestemming</Name><Title>app:Enkelbestemming</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitgebied_P</Name><Title>app:Besluitgebied_P</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_NationaalGebied</Name><Title>app:NationaalGebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Figuur</Name><Title>app:Figuur</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitgebied_X</Name><Title>app:Besluitgebied_X</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Plangebied_PCP</Name><Title>app:Plangebied_PCP</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitvlak_A</Name><Title>app:Besluitvlak_A</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_ProvinciaalPlangebied</Name><Title>app:ProvinciaalPlangebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisiecomplex_G</Name><Title>app:Structuurvisiecomplex_G</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Dubbelbestemming</Name><Title>app:Dubbelbestemming</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitgebied</Name><Title>app:Besluitgebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_PlangebiedDigitaalWaarmerk</Name><Title>app:PlangebiedDigitaalWaarmerk</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitsubvlak_X</Name><Title>app:Besluitsubvlak_X</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisiegebied_G</Name><Title>app:Structuurvisiegebied_G</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Bouwvlak</Name><Title>app:Bouwvlak</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Bestemmingsplangebied</Name><Title>app:Bestemmingsplangebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitvlak_X</Name><Title>app:Besluitvlak_X</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisiegebied_R</Name><Title>app:Structuurvisiegebied_R</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisiegebied_P</Name><Title>app:Structuurvisiegebied_P</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitsubvlak_P</Name><Title>app:Besluitsubvlak_P</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_NationaalVerbinding</Name><Title>app:NationaalVerbinding</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_OnderdelenDigitaalWaarmerk</Name><Title>app:OnderdelenDigitaalWaarmerk</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Onthoudingsgebied</Name><Title>app:Onthoudingsgebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Functieaanduiding</Name><Title>app:Functieaanduiding</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisieverklaring_P</Name><Title>app:Structuurvisieverklaring_P</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitvlak_P</Name><Title>app:Besluitvlak_P</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_NationaalComplex</Name><Title>app:NationaalComplex</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Maatvoering</Name><Title>app:Maatvoering</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_NationaalPlangebied</Name><Title>app:NationaalPlangebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Bouwaanduiding</Name><Title>app:Bouwaanduiding</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitsubvlak_A</Name><Title>app:Besluitsubvlak_A</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisieplangebied_G</Name><Title>app:Structuurvisieplangebied_G</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Lettertekenaanduiding</Name><Title>app:Lettertekenaanduiding</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisieplangebied_R</Name><Title>app:Structuurvisieplangebied_R</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_ProvinciaalGebied</Name><Title>app:ProvinciaalGebied</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Structuurvisieplangebied_P</Name><Title>app:Structuurvisieplangebied_P</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>app:roowfs_Besluitgebied_A</Name><Title>app:Besluitgebied_A</Title><SRS>urn:ogc:def:crs:EPSG::28992</SRS><LatLongBoundingBox minx=\"-180.0\" miny=\"-90.0\" maxx=\"180.0\" maxy=\"90.0\"/><MetadataURL type=\"TC211\" format=\"XML\">http://afnemers.ruimtelijkeplannen.nl:80/afnemers/metadata.xml</MetadataURL></FeatureType><FeatureType><Name>demowfs_bebouwdekom_nl</Name><Title>bebouwdekom_nl</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"3.22989\" miny=\"50.709\" maxx=\"7.27394\" maxy=\"53.5672\"/></FeatureType><FeatureType><Name>demowfs_bebouwdekom nl</Name><Title>bebouwdekom nl</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"3.22989\" miny=\"50.709\" maxx=\"7.27394\" maxy=\"53.5672\"/></FeatureType><FeatureType><Name>demowfs_basis_nl</Name><Title>basis_nl</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType><FeatureType><Name>demowfs_autowegen_nl</Name><Title>autowegen_nl</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType><FeatureType><Name>demowfs_autowegen_elabels</Name><Title>autowegen_elabels</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType><FeatureType><Name>demowfs_rivieren_nl</Name><Title>rivieren_nl</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType><FeatureType><Name>demowfs_gemeenten_2006</Name><Title>gemeenten_2006</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"3.22989\" miny=\"50.709\" maxx=\"7.27394\" maxy=\"53.5672\"/></FeatureType><FeatureType><Name>demowfs_wijken_2006</Name><Title>wijken_2006</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType><FeatureType><Name>demowfs_buurten_2006</Name><Title>buurten_2006</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType><FeatureType><Name>demowfs_plan_lijnen</Name><Title>plan_lijnen</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType><FeatureType><Name>demowfs_plan_polygonen</Name><Title>plan_polygonen</Title><SRS>EPSG:28992</SRS><LatLongBoundingBox minx=\"-179.156\" miny=\"-74.7705\" maxx=\"179.909\" maxy=\"2.64457\"/></FeatureType></FeatureTypeList><ogc:Filter_Capabilities><ogc:Spatial_Capabilities><ogc:Spatial_Operators><ogc:BBOX/></ogc:Spatial_Operators></ogc:Spatial_Capabilities><ogc:Scalar_Capabilities><ogc:Comparison_Operators><ogc:Simple_Comparisons/><ogc:Like/><ogc:Between/><ogc:NullCheck/></ogc:Comparison_Operators></ogc:Scalar_Capabilities></ogc:Filter_Capabilities></WFS_Capabilities>";
        ByteArrayInputStream capabilitiesReader = new ByteArrayInputStream(wfsCapabilitiesRawData.getBytes("UTF-8"));
               Map hints = new HashMap();
        hints.put(DocumentFactory.VALIDATION_HINT, Boolean.FALSE);

        Object parsed;
        try {
            parsed = DocumentFactory.getInstance(capabilitiesReader, hints, null);
        } catch (Exception e) {
            throw new IOException("Error parsing WFS 1.0.0 capabilities", e);
        }
// eind debug stukje

    }
}
