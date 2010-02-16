/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.zoeker;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import nl.b3p.zoeker.configuratie.Attribuut;
import org.geotools.util.logging.Logging;
import nl.b3p.zoeker.configuratie.Bron;
import nl.b3p.zoeker.configuratie.ResultaatAttribuut;
import nl.b3p.zoeker.configuratie.ZoekAttribuut;
import nl.b3p.zoeker.configuratie.ZoekConfiguratie;
import nl.b3p.zoeker.services.ZoekResultaat;
import nl.b3p.zoeker.services.Zoeker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.chainsaw.Main;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Roy
 */
public class ZoekerTest {
    private static final Log log = LogFactory.getLog(Zoeker.class);
    private static final Logging logging = Logging.ALL;
    private Zoeker zoeker = new Zoeker();
    private static String arnhemWKT="POLYGON((194000 454000,193100 452800,192700 451300,193097.165775462 447129.759357651,193100 447100,193300 446900,193310.365505896 446884.451741156,193500 446600,193600 446300,193600.116346052 446299.650961844,193696.218028945 446011.345913164,193700 446000,193800 445800,194000 445200,194019.1173791 445171.32393135,194400 444600,194405.632255597 444597.183872202,194600 444500,194689.906847841 444455.04657608,195000 444300,195781.476739365 443811.577037897,195800 443800,196400 443100,194800 442800,194315.671426971 442509.402856182,194300 442500,193900 442000,193700 441300,193700 440700,194100 440100,193100 440900,192700 441500,192684.70693828 441494.90231276,192400 441400,192400 440814.985265513,192400 440800,192300 440400,192265.13771735 440400,192100 440400,191521.018234365 440400,191500 440400,191300 440300,191002.068519439 440002.068519439,191000 440000,191080.502950951 439758.491147146,191100 439700,190400 438500,190315.336018801 438528.221327066,190100 438600,189900 438600,189892.678266973 438601.464346605,189400 438700,189118.740790052 438840.629604974,188800 439000,188784.070377251 439009.55777365,188300 439300,187657.78963532 439471.256097248,187054.250211581 439632.199943578,187029.135104073 439638.897305581,186800 439700,186800 439500,185400 439700,185400 440300,185431.547775309 440583.92997778,185443.878785809 440694.909072284,185500 441200,185700 441700,185795.661102272 442273.966613633,185800 442300,185800 442900,186300 442800,187100 442500,187135.500144876 442500,187400 442500,187700 442500,188000 442700,188141.487686964 442982.975373928,188200 443100,188028.02731191 443529.931720224,188000 443600,187802.91744896 444092.706377599,187800 444100,187800 444198.504019583,187800 444445.375021375,187800 444900,187706.023252088 445369.883739559,187700 445400,187200 445400,186600 445600,185600 445700,185575.33333457 445700,185500 445700,185467.761153625 445708.059711594,184700 445900,184653.290041796 445915.569986068,183811.194330857 446196.268556381,183800 446200,183600 446300,183600 446572.513820769,183600 446600,183800 447100,183900 447900,184200 448500,184991.708973282 449687.563459923,185000 449700,185900 451000,186700 451600,186722.561690174 451600,187400 451600,188900 451300,190100 451400,190000 452900,190800 452800,193200 454500,193300 454500,194000 454000))";
    public ZoekerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        
    }

    @Before
    public void setUp() throws Exception {
        Class c = ZoekerTest.class;
        URL log4j_url = c.getResource("log4j.properties");
        if (log4j_url == null) {
            throw new IOException("Unable to locate log4j.properties in package " + Main.class.getPackage().toString());
        }

        Properties p = new Properties();
        p.load(log4j_url.openStream());
        PropertyConfigurator.configure(p);

        //logger for geotools
        try {
            logging.setLoggerFactory("org.geotools.util.logging.Log4JLoggerFactory");
        } catch (ClassNotFoundException log4jException) {
            log.error("error: ",log4jException);
        }
        log.info("logging configured!");

        //owner=MyEMFDatabase.createEntityManager(MyEMFDatabase.MAIN_EM);
    }

    @After
    public void tearDown() {
        //MyEMFDatabase.closeEntityManager(owner, MyEMFDatabase.MAIN_EM);
    }

    @Test
    /**
     */
    public void searchWithGeomDeegree(){
        //test op roonline deegree wfs
        Bron bron = new Bron(1,"ro-online","http://pilot.ruimtelijkeplannen.nl/afnemers/services?Version=1.0.0");
        ZoekConfiguratie zc = new ZoekConfiguratie(null,"planzoeken op geometry","app:Plangebied",bron,null);

        ZoekAttribuut za = new ZoekAttribuut(null,"op geometry","geometrie", "geom",3,null);
        ResultaatAttribuut ra = new ResultaatAttribuut(null,"Identificatie","app:naam","Plannaam",2,null);

        zc.addZoekAttribuut(za);
        zc.addResultaatAttribuut(ra);
        List resultaten= zoeker.zoekMetConfiguratie(zc, new String[]{arnhemWKT}, 10000, new ArrayList());
        printResultsToLog(resultaten);       
    }
    @Test
    public void searchWithGeomMapserver(){        
        //test op kaartenbalie public mapserver wfs
        Bron bron = new Bron(1,"cbs-wfs","http://public-wms.kaartenbalie.nl/wms/nederland?Version=1.0.0");
        ZoekConfiguratie zc = new ZoekConfiguratie(null,"Buurt zoeken op geometry","buurten_2006",bron,null);

        ZoekAttribuut za = new ZoekAttribuut(null,"op geometry","msGeometry", "msGeometry",3,null);
        ResultaatAttribuut ra = new ResultaatAttribuut(null,"bu_naam","bu_naam","bu_naam",2,null);

        zc.addZoekAttribuut(za);
        zc.addResultaatAttribuut(ra);
        List resultaten= zoeker.zoekMetConfiguratie(zc, new String[]{arnhemWKT}, 10000, new ArrayList());
        printResultsToLog(resultaten);        
    }

    @Test
    public void searchWithStringPostgis(){
        Bron bron = new Bron(1,"cbs","jdbc:postgresql://b3p-demoserver:5432/demo_kaartenbalie","postgres","***REMOVED***",null);
        ZoekConfiguratie zc = new ZoekConfiguratie(null,"Buurten op geom","buurt_2006_cbs",bron,null);

        ZoekAttribuut za = new ZoekAttribuut(null,"op geometry","the_geom", "the_geom",3,null);
        ResultaatAttribuut ra = new ResultaatAttribuut(null,"bu_naam","bu_naam","bu_naam",2,null);

        zc.addZoekAttribuut(za);
        zc.addResultaatAttribuut(ra);

        List resultaten3= zoeker.zoekMetConfiguratie(zc, new String[]{"Arnhem"}, 10000, new ArrayList());
        printResultsToLog(resultaten3);
    }
    @Test
    public void searchWithLessGreater(){
        //test op kaartenbalie public mapserver wfs
        Bron bron = new Bron(1,"cbs-wfs","http://public-wms.kaartenbalie.nl/wms/nederland?Version=1.0.0");
        ZoekConfiguratie zc = new ZoekConfiguratie(null,"Buurt zoeken op geometry","gemeenten_2006",bron,null);

        ZoekAttribuut za1= new ZoekAttribuut(null,"aantal inwoners","aant_inw", "aantal inwoners",Attribuut.GROTER_DAN_TYPE,1);
        ZoekAttribuut za2 = new ZoekAttribuut(null,"aantal inwoners","aant_inw", "aantal inwoners",Attribuut.KLEINER_DAN_TYPE,2);
        ResultaatAttribuut ra = new ResultaatAttribuut(null,"Gemeente","gm_naam","Gemeente",2,null);

        zc.addZoekAttribuut(za1);
        zc.addZoekAttribuut(za2);
        zc.addResultaatAttribuut(ra);
        List resultaten= zoeker.zoekMetConfiguratie(zc, new String[]{"10000","15000"}, 25, new ArrayList());
        printResultsToLog(resultaten);
    }
    public void searchWith2LikeFilters(){
        //test op kaartenbalie public mapserver wfs
        Bron bron = new Bron(1,"cbs-wfs","http://public-wms.kaartenbalie.nl/wms/nederland?Version=1.0.0");
        ZoekConfiguratie zc = new ZoekConfiguratie(null,"Buurt zoeken","buurten_2006",bron,null);

        ZoekAttribuut za1= new ZoekAttribuut(null,"aantal inwoners","gm_naam", "aantal inwoners",Attribuut.GEEN_TYPE,1);
        ZoekAttribuut za = new ZoekAttribuut(null,"aantal inwoners","bu_naam", "aantal inwoners",Attribuut.GEEN_TYPE,2);        
        ResultaatAttribuut ra = new ResultaatAttribuut(null,"Gemeente","gm_naam","Gemeente",2,null);

        zc.addZoekAttribuut(za);
        zc.addZoekAttribuut(za1);
        zc.addResultaatAttribuut(ra);
        List resultaten= zoeker.zoekMetConfiguratie(zc, new String[]{"Utrecht","Bedrijventerrein Lageweide"}, 25, new ArrayList());
        printResultsToLog(resultaten);
    }

    private void printResultsToLog(List resultaten){
        log.info("Er zijn "+resultaten.size()+" resultaten gevonden:");
        for (int i=0; i < resultaten.size(); i++){
            ZoekResultaat zr=(ZoekResultaat) resultaten.get(i);
            log.info(zr.getLabel());
        }
    }
       
    public static void main (String[] args) throws Exception{        
        ZoekerTest test = new ZoekerTest();
        test.setUp();
        test.searchWith2LikeFilters();
    }
}