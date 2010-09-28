/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.zoeker.services;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import nl.b3p.zoeker.configuratie.Attribuut;
import nl.b3p.zoeker.configuratie.Bron;
import nl.b3p.zoeker.configuratie.ResultaatAttribuut;
import nl.b3p.zoeker.configuratie.ZoekAttribuut;
import nl.b3p.zoeker.configuratie.ZoekConfiguratie;
import nl.b3p.zoeker.hibernate.MyEMFDatabase;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.ows.WFSCapabilities;
import org.geotools.data.wfs.v1_0_0.WFS_1_0_0_DataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.FilterCapabilities;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

/**
 *
 * @author Roy
 */
public class Zoeker {

    private static final int topMaxResults = 1000;
    private static final Log log = LogFactory.getLog(Zoeker.class);    
    private static final SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy",new Locale("NL"));

    public List zoek(Integer[] zoekConfiguratieIds, String searchStrings[], Integer maxResults) {        
        List<ZoekResultaat> results = new ArrayList();
        Object identity = null;
        try {
            identity = MyEMFDatabase.createEntityManager(MyEMFDatabase.MAIN_EM);
            EntityManager em = MyEMFDatabase.getEntityManager(MyEMFDatabase.MAIN_EM);
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                String queryString="from ZoekConfiguratie z where z.id IN (";
                for (int i = 0; i < zoekConfiguratieIds.length; i++) {
                    if (i!=0)
                        queryString+=",";
                    queryString+=zoekConfiguratieIds[i];
                }
                queryString+=") order by z.parentBron.volgorde";
                List zoekconfiguraties=em.createQuery(queryString).getResultList();
                for (int i = 0; i < zoekconfiguraties.size(); i++) {
                    ZoekConfiguratie zc = (ZoekConfiguratie) zoekconfiguraties.get(i);
                    results = zoekMetConfiguratie(zc, cleanStringArray(searchStrings), maxResults, results);
                }
                tx.commit();
            } catch (Exception ex) {
                log.error("Exception occured" + (tx.isActive() ? ", rollback" : "tx not active"), ex);
                if (tx.isActive()) {
                    tx.rollback();
                }
            }

        } catch (Throwable e) {
            log.error("Exception occured in search: ", e);
        } finally {
            log.debug("Closing entity manager .....");
            MyEMFDatabase.closeEntityManager(identity, MyEMFDatabase.MAIN_EM);
        }
        if (results != null) {
            Collections.sort(results);
        }
        return results;
    }

    private String[] cleanStringArray(String[] sa) {
        if (sa == null) {
            return null;
        }
        for (int i = 0; i < sa.length; i++) {
            sa[i] = sa[i].trim();
        }
        return sa;
    }

    /**
     * Zoek moet configuratie (search Strings moet gelijk zijn aan aantal zoekvelden in de ZoekConfiguratie:
     * @param zc: ZoekConfiguratie waarmee gezocht moet worden
     * @param searchStrings Een array van gezochte waarden. (moet gelijk zijn aan het aantal geconfigureerde zoekvelden en ook in die volgorde staan)
     * @param maxResults: Het maximaal aantal resultaten dat getoond moeten worden.
     * @param results: De al gevonden resultaten (de nieuwe resultaten worden hier aan toegevoegd.
     */
    public List<ZoekResultaat> zoekMetConfiguratie(ZoekConfiguratie zc, String[] searchStrings, Integer maxResults, List<ZoekResultaat> results) {
        if (maxResults == null || maxResults.intValue() == 0 || maxResults.intValue() > topMaxResults) {
            maxResults = topMaxResults;
        }
        if (zc == null || searchStrings == null) {
            return results;
        }
        if (!zc.isResultListDynamic() && ZoekConfiguratie.isCachedResultListReady()) {
            /**
             * zoekresultaten worden uit cache gehaald.
             */
            throw new NotImplementedException("Cache not implemented yet.");
        }
        Bron bron = zc.getBron();
        List<ZoekResultaat> zoekResultaten = new ArrayList(results);
        DataStore ds = null;
        try {
            ds = getDataStore(bron);
            if (ds != null) {
                FeatureCollection fc = null;
                //FeatureReader reader = null;
                FeatureIterator fi=null;
                try {
                    if (ds instanceof WFS_1_0_0_DataStore) {
                        WFS_1_0_0_DataStore wfs100ds = (WFS_1_0_0_DataStore) ds;
                        WFSCapabilities wfscap = wfs100ds.getCapabilities();
                        FilterCapabilities filterCap = wfscap.getFilterCapabilities();
                        filterCap.addType(FilterCapabilities.FUNCTIONS);
                        wfscap.setFilterCapabilities(filterCap);
                    }
                    FeatureSource fs = ds.getFeatureSource(zc.getFeatureType());
                    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
                    //FilterFactory ff = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());
                    if (zc.getZoekVelden() == null) {
                        throw new Exception("Fout in zoekconfiguratie. Er zijn geen zoekvelden gedefineerd");
                    }
                    if (zc.getZoekVelden().size() != searchStrings.length) {
                        throw new Exception("Fout in zoekconfiguratie. Het aantal zoekvelden (" + zc.getZoekVelden().size() + ") is ongelijk aan het aantal meegegeven strings(" + searchStrings.length + ")");
                    }
                    Iterator it = zc.getZoekVelden().iterator();
                    List filters = new ArrayList();
                    
                    //maak de filters
                    try{
                        fs.getSchema();
                    }catch (NullPointerException npe){
                        log.error("Kan het schema voor de zoekconfiguratie niet ophalen",npe);
                        throw npe;
                    }
                    ArrayList properties = new ArrayList();
                    for (int i = 0; it.hasNext(); i++) {
                        ZoekAttribuut zoekVeld = (ZoekAttribuut) it.next();
                        Filter filter= createFilter(ZoekAttribuut.setToZoekVeldenArray(zc.getZoekVelden()),searchStrings,i,ds,ff,fs.getSchema());
                        if (filter!=null){
                            filters.add(filter);                            
                        }
                    }
                    //maak een and filter of een single filter aan de hand van het aantal filters
                    Filter filter = null;
                    if (filters.size() == 1) {
                        filter = (Filter) filters.get(0);
                    } else {
                        filter = ff.and(filters);
                    }
                    //maak de query met de maxResults
                    DefaultQuery query;
                    if (filters.size() == 0) {
                        query = new DefaultQuery(zc.getFeatureType());
                    } else {
                        query = new DefaultQuery(zc.getFeatureType(), filter);
                    }
                    query.setMaxFeatures(maxResults.intValue());
                    //set de property namen die opgehaald moeten worden.
                    Iterator pit = zc.getResultaatVelden().iterator();
                    if (!pit.hasNext()) {
                        log.error("Geen resultaatvelden geconfigureerd voor zoekconfiguratie: " + zc.getNaam());
                        return null;
                    }
                    while (pit.hasNext()) {
                        ResultaatAttribuut pa = (ResultaatAttribuut) pit.next();
                        if (!properties.contains(pa.getAttribuutLocalnaam())) {
                            properties.add(pa.getAttribuutLocalnaam());
                        }
                    }
                    query.setPropertyNames(properties);
                    //Haal de featureCollection met de query op.
                    fc = fs.getFeatures(query);
                    //Maak de FeatureIterator aan (hier wordt het daad werkelijke verzoek gedaan.
                    fi=fc.features();
                    //doorloop de features en maak de resultaten.
                    while (fi.hasNext()) {
                        Feature f = fi.next();                        
                        ZoekResultaat p = new ZoekResultaat();
                        Iterator rit = zc.getResultaatVelden().iterator();
                        while (rit.hasNext()) {
                            ResultaatAttribuut ra = (ResultaatAttribuut) rit.next();
                            if (f.getProperty(ra.getAttribuutLocalnaam()) != null) {
                                Object value = f.getProperty(ra.getAttribuutLocalnaam()).getValue();
                                ZoekResultaatAttribuut zra = new ZoekResultaatAttribuut(ra);
                                zra.setWaarde(value);
                                p.addAttribuut(zra);
                                p.setZoekConfiguratie(zc);
                            } else {
                                String attrTypes = "";
                                Iterator pi = f.getProperties().iterator();
                                while (pi.hasNext()) {
                                    Property pr = (Property) pi.next();
                                    attrTypes += pr.getType().getName().getLocalPart() + " ";
                                }
                                log.debug("Attribuut: " + ra.toString() + " niet gevonden. Mogelijke attributen: " + attrTypes);
                            }
                        }
                        if (f.getType().getGeometryDescriptor() != null && f.getDefaultGeometryProperty() != null && f.getDefaultGeometryProperty().getBounds() != null) {
                            p.setBbox(f.getDefaultGeometryProperty().getBounds());
                        }else{
                            log.debug("Can't set Bbox for result. No bounds set for feature by the server. And no Geometry given or configured as result in the search configuration");
                        }
                        if (!zoekResultaten.contains(p)) {
                            zoekResultaten.add(p);
                        }
                        
                    }
                } catch (SchemaNotFoundException snfe) {
                    String typenames = "";
                    String[] tn = ds.getTypeNames();
                    for (int i = 0; i < tn.length; i++) {
                        if (typenames.length() != 0) {
                            typenames += "\n";
                        }
                        typenames += tn[i];
                    }
                    log.error("Feature "+zc.getFeatureType()+" niet bekend bij bron, mogelijke features: " + typenames, snfe);
                } catch (Exception e) {
                    log.error("Fout bij laden plannen: ", e);
                } finally {
                    if (fc!=null && fi!=null){
                        fc.close(fi);
                    }

                }
            } else {
                log.error("Kan geen datastore maken van bron");
            }
        } catch (Exception ioe) {
            log.error("Fout bij laden van plannen: ", ioe);
        } finally {
            if (ds != null) {
                ds.dispose();
            }
        }

        if (!zc.isResultListDynamic() && !ZoekConfiguratie.isCachedResultListReady()) {
            /**
             * zoekresultaten worden in cache gezet.
             * per zoekstrings in static map
             */
            log.debug("Cache not implemented yet.");
//            throw new NotImplementedException("Cache not implemented yet.");
        }

        return zoekResultaten;
    }
    public static List getZoekConfiguraties() {
        Object identity = null;
        List returnList=null;
        try {
            identity = MyEMFDatabase.createEntityManager(MyEMFDatabase.MAIN_EM);
            EntityManager em = MyEMFDatabase.getEntityManager(MyEMFDatabase.MAIN_EM);
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                returnList= em.createQuery("from ZoekConfiguratie z").getResultList();
                tx.commit();
            } catch (Exception ex) {
                log.error("Exception occured" + (tx.isActive() ? ", rollback" : "tx not active"), ex);
                if (tx.isActive()) {
                    tx.rollback();
                }
            }

        } catch (Throwable e) {
            log.error("Exception occured in search: ", e);
        } finally {
            log.debug("Closing entity manager .....");
            MyEMFDatabase.closeEntityManager(identity, MyEMFDatabase.MAIN_EM);
        }
        return returnList;
    }

    /**
     * Maakt een datastore dmv de bron
     * @param b de Bron
     * @return een Datastore
     * @throws java.io.IOException
     * @deprecated: use b.toDatastore();
     */
     public static DataStore getDataStore(Bron b) throws IOException, Exception {
        return b.toDatastore();
    }
    /**
     * Maakt het filter voor het zoekveld met als value het zoek criterium.
     * Geef de alle zoekvelden mee en alle ingevulde strings omdat sommige zoekvelden afhankelijk kunnen zijn van elkaar.
     */
    private Filter createFilter(ZoekAttribuut[] zoekVelden, String[] searchStrings, int index, DataStore ds,FilterFactory2 ff, FeatureType ft) throws Exception {
        String searchString=searchStrings[index];
        if (searchString==null || searchString.length()==0)
            return null;
        ZoekAttribuut zoekVeld=zoekVelden[index];
        Filter filter=null;
        if(zoekVeld.getType()==Attribuut.GEOMETRY_TYPE){
            WKTReader wktreader = new WKTReader(new GeometryFactory(new PrecisionModel(), 28992));
            try {
                Geometry geom = wktreader.read(searchString);
                //zijn er nog zoekAttributen ingevuld die betrekking hebben op de geometry (zoals straal)
                for (int i=0; i < zoekVelden.length; i++){
                    //skip voor dit zoekveld
                    if (i==index)
                        continue;
                    //bij straal maak een buffer.
                    if (zoekVelden[i].getType()==Attribuut.STRAAL_TYPE &&searchStrings[i]!=null && searchStrings[i].length()>0){
                        try{
                            double straal=Double.parseDouble(searchStrings[i]);
                            geom=geom.buffer(straal);
                        }catch(NumberFormatException nfe){
                            log.error("Ingevulde zoekopdracht "+zoekVelden[i].getNaam()+ "moet een nummer zijn",nfe);
                        }
                    }
                }
                filter=ff.within(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(geom));
            }catch(Exception e){
                log.error("Fout bij parsen wkt geometry",e);
            }
        }else if (zoekVeld.getType().intValue()==Attribuut.GROTER_DAN_TYPE){            
            filter=ff.greaterOrEqual(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(searchString));
        }else if (zoekVeld.getType().intValue()==Attribuut.KLEINER_DAN_TYPE){            
            filter=ff.lessOrEqual(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(searchString));
        }else if (zoekVeld.getType().intValue()==Attribuut.GELIJK_AAN_TYPE){
            filter=ff.equals(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(searchString));
        }else if (zoekVeld.isFilterMogelijk()){
            String wildeSearchString=null;
            if(propertyIsNumber(ft.getDescriptor(zoekVeld.getAttribuutnaam()))){
                wildeSearchString=searchString;
            }else{
                wildeSearchString="*"+searchString+"*";
            }
            if (ds instanceof WFS_1_0_0_DataStore) {
                if(propertyIsNumber(ft.getDescriptor(zoekVeld.getAttribuutnaam()))){
                    filter=ff.equals(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(wildeSearchString));
                }else{
                    filter=ff.like(ff.property(zoekVeld.getAttribuutnaam()), wildeSearchString);
                }
            } else {
                if (searchString.length() > 0) {
                    if(propertyIsNumber(ft.getDescriptor(zoekVeld.getAttribuutnaam()))){
                        filter=ff.equals(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(wildeSearchString));
                    }else{
                        filter=ff.like( ff.property(zoekVeld.getAttribuutnaam()),wildeSearchString, "*", "?", "\\", false);
                    }
                }
            }
        }
        return filter;
    }

    private boolean propertyIsNumber(PropertyDescriptor descriptor) {
        if (descriptor==null || descriptor.getType()==null || descriptor.getType().getBinding()==null)
            return false;
        if (descriptor.getType().getBinding()==Integer.class || descriptor.getType().getBinding()==Double.class || descriptor.getType().getBinding()==BigInteger.class){
            return true;
        }
        return false;
    }
}
