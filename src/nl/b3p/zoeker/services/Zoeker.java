/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.zoeker.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import nl.b3p.zoeker.configuratie.Bron;
import nl.b3p.zoeker.configuratie.ResultaatAttribuut;
import nl.b3p.zoeker.configuratie.ZoekAttribuut;
import nl.b3p.zoeker.configuratie.ZoekConfiguratie;
import nl.b3p.zoeker.hibernate.MyEMFDatabase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
/**
 *
 * @author Roy
 */
public class Zoeker {
    private static final int topMaxResults=1000;
    private static final Log log = LogFactory.getLog(Zoeker.class);

    public List zoek(Integer[] zoekConfiguratieIds, String searchStrings[], Integer maxResults){
        if (maxResults==null || maxResults.intValue() == 0 || maxResults.intValue() > topMaxResults){
            maxResults=topMaxResults;
        }
        List results = new ArrayList();
        Object identity = null;
        try {
            identity = MyEMFDatabase.createEntityManager(MyEMFDatabase.MAIN_EM);
            EntityManager em = MyEMFDatabase.getEntityManager(MyEMFDatabase.MAIN_EM);
            EntityTransaction tx = em.getTransaction();
            tx.begin();                
            for (int i=0; i < zoekConfiguratieIds.length; i++){
                ZoekConfiguratie zc=  (ZoekConfiguratie) em.createQuery("from ZoekConfiguratie z where z.id = :id").setParameter("id", zoekConfiguratieIds[i]).getSingleResult();
                List zcResult= zoekMetConfiguratie(zc,searchStrings, maxResults);
                results.addAll(zcResult);
            }
            
        } catch (Throwable e) {
            log.error("Exception occured in search: ", e);
        } finally {
            log.debug("Closing entity manager .....");
            MyEMFDatabase.closeEntityManager(identity, MyEMFDatabase.MAIN_EM);
        }
        return results;
    }

    private List zoekMetConfiguratie(ZoekConfiguratie zc,String[] searchStrings, Integer maxResults){
        Bron bron = zc.getBron();
        ArrayList zoekResultaten = new ArrayList();
        DataStore ds=null;
        try{
            ds=getDataStore(bron);
            if (ds!=null){
                FeatureCollection fc=null;
                Iterator fi=null;
                try{
                    FeatureSource fs= ds.getFeatureSource(zc.getFeatureType());
                    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
                    if (zc.getZoekVelden()==null){
                        throw new Exception("Fout in zoekconfiguratie. Er zijn geen zoekvelden gedefineerd");
                    }
                    if (zc.getZoekVelden().size()!=searchStrings.length){
                        throw new Exception("Fout in zoekconfiguratie. Het aantal zoekvelden ("+zc.getZoekVelden().size()+") is ongelijk aan het aantal meegegeven strings("+searchStrings.length+")");
                    }
                    Iterator it=zc.getZoekVelden().iterator();
                    List filters= new ArrayList();
                    Filter filter=null;
                    for(int i=0; it.hasNext(); i++){
                        ZoekAttribuut zoekVeld= (ZoekAttribuut) it.next();
                        filters.add(ff.like(ff.property(zoekVeld.getAttribuutLocalnaam()), "*"+searchStrings[i]+"*"));
                    }
                    if (filters.size()==1){
                        filter= (Filter) filters.get(0);
                    }else{
                        filter = ff.and(filters);
                    }
                    DefaultQuery query = new DefaultQuery(zc.getFeatureType(),filter);
                    query.setMaxFeatures(maxResults.intValue());
                    fc=fs.getFeatures(query);
                    //fc=fs.getFeatures(filter);
                    fi=fc.iterator();
                    while(fi.hasNext()){
                        Feature f=(Feature) fi.next();
                        ZoekResultaat p = new ZoekResultaat();
                        Iterator rit=zc.getResultaatVelden().iterator();
                        if (!rit.hasNext()){
                            log.error("Geen resultaatvelden geconfigureerd voor zoekconfiguratie: "+zc.getNaam());
                        }
                        while (rit.hasNext()){
                            ResultaatAttribuut ra= (ResultaatAttribuut) rit.next();
                            if (f.getAttribute(ra.getAttribuutLocalnaam())!=null){
                                String value=f.getAttribute(ra.getAttribuutLocalnaam()).toString();
                                ZoekResultaatAttribuut zra= new ZoekResultaatAttribuut(ra);
                                zra.setWaarde(value);
                                p.addAttribuut(zra);
                                p.setZoekConfigId(zc.getId());
                            }else{
                                String attrTypes="";
                                for (int i=0; i < f.getFeatureType().getAttributeTypes().length; i++){
                                    attrTypes+=f.getFeatureType().getAttributeType(i).getLocalName()+" ";
                                }
                                log.debug("Attribuut: "+ra.toString()+ " niet gevonden. Mogelijke attributen: "+attrTypes);
                            }
                        }
                        if (f.getDefaultGeometry()!=null && f.getDefaultGeometry().getEnvelopeInternal()!=null){
                            p.setBbox(f.getDefaultGeometry().getEnvelopeInternal());
                        }
                        zoekResultaten.add(p);
                    }
                }catch (Exception e){
                    log.error("Fout bij laden plannen, mogelijk is de feature: ",e);
                }
                finally{
                    if (fc!=null && fi!=null)
                        fc.close(fi);
                }
            }else{
                log.error("Kan geen datastore maken van bron");
            }
        }catch(Exception ioe){
            log.error("Fout bij laden van plannen: ",ioe);
        }finally{
            if (ds!=null)
                ds.dispose();
        }
        return zoekResultaten;
    }
     /**
     * Maakt een datastore dmv de bron
     * @param b de Bron
     * @return een Datastore
     * @throws java.io.IOException
     */
    //jdbc:postgresql://localhost:5432/edamvolendam_gis
    public DataStore getDataStore(Bron b) throws IOException{
        HashMap params = new HashMap();
        if (b.getUrl().toLowerCase().startsWith("jdbc:")){
            //jdbc:postgresql://localhost:5432/edamvolendam_gis
            int firstIndex;
            int lastIndex;
            firstIndex=b.getUrl().indexOf("//")+2;
            lastIndex=b.getUrl().indexOf(":", firstIndex);
            String host=b.getUrl().substring(firstIndex, lastIndex);
            firstIndex=lastIndex+1;
            lastIndex=b.getUrl().indexOf("/",firstIndex);
            String port= b.getUrl().substring(firstIndex,lastIndex);
            firstIndex=lastIndex+1;
            String database=b.getUrl().substring(firstIndex,b.getUrl().length());
            String schema="public";
            if (database.indexOf(".")>=0){
                String[] tokens=database.split("\\.");
                if(tokens.length==2){
                    schema=tokens[0];
                    database=tokens[1];
                }
            }
            params.put(PostgisDataStoreFactory.DBTYPE.key,"postgis");
            params.put(PostgisDataStoreFactory.HOST.key,host);
            params.put(PostgisDataStoreFactory.PORT.key,port);
            params.put(PostgisDataStoreFactory.SCHEMA.key,schema);
            params.put(PostgisDataStoreFactory.DATABASE.key,database);
            if (b.getGebruikersnaam()!=null)
                params.put(PostgisDataStoreFactory.USER.key,b.getGebruikersnaam());
            if (b.getWachtwoord()!=null)
                params.put(PostgisDataStoreFactory.PASSWD.key,b.getWachtwoord());
        }else{
            params.put(WFSDataStoreFactory.URL.key,b.getUrl());
            if (b.getGebruikersnaam()!=null)
                params.put(WFSDataStoreFactory.USERNAME.key,b.getGebruikersnaam());
            if (b.getWachtwoord()!=null)
                params.put(WFSDataStoreFactory.PASSWORD.key,b.getWachtwoord());
            
        }
        return DataStoreFinder.getDataStore(params);
    }   
    
}