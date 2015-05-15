package nl.b3p.zoeker.hibernate;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MyEMFDatabase{

    private static final Log log = LogFactory.getLog(MyEMFDatabase.class);
    public static final String MAIN_EM = "zoekerMainEM";
    public static final String INIT_EM = "zoekerInitEM";
    public static final String REALM_EM = "zoekerRealmEM";
    private static EntityManagerFactory emf = null;
    private static ThreadLocal tlMap = new ThreadLocal();
    private static String defaultPU = "zoekerPU";

    public static void openEntityManagerFactory(String persistenceUnit) throws Exception {
        log.info("ManagedPersistence.openEntityManagerFactory(" + persistenceUnit + ")");
        if (emf != null) {
            log.warn("EntityManagerFactory already initialized: " + emf.toString());
            return;
        }
        if (persistenceUnit == null || persistenceUnit.trim().length() == 0) {
            throw new Exception("PersistenceUnit cannot be left empty.");
        }
        try {
            emf = Persistence.createEntityManagerFactory(persistenceUnit);
        } catch (Throwable t) {
            log.fatal("Error initializing EntityManagerFactory: ", t);
        }
        if (emf == null) {
            throw new Exception("Cannot initialize EntityManagerFactory");
        }
        log.info("EntityManagerFactory initialized: " + emf.toString());
    }

    public static EntityManagerFactory getEntityManagerFactory() throws Exception {
        if (emf == null) {
            openEntityManagerFactory(defaultPU);
        }
        return emf;
    }   
    /** The constants for describing the ownerships **/
    private static final Owner trueOwner = new Owner(true);
    private static final Owner fakeOwner = new Owner(false);

    /**
     * Internal class , for handling the identity. Hidden for the 
     * developers
     */
    private static class Owner {

        public Owner(boolean identity) {
            this.identity = identity;
        }
        boolean identity = false;
    }

    /**
     * get the hibernate session and set it on the thread local. Returns trueOwner if 
     * it actually opens a session
     */
    public static Object createEntityManager(String emKey) throws Exception {
        EntityManager localEm = (EntityManager) getThreadLocal(emKey);
        if (localEm == null) {
            log.debug("No EntityManager Found - Create and give the identity for key: " + emKey);
            localEm = getEntityManagerFactory().createEntityManager();
            if (localEm == null) {
                throw new Exception("EntityManager could not be initialized for key: " + emKey);
            }
            setThreadLocal(emKey, localEm);
            return trueOwner;
        }
        log.debug("EntityManager Found - Give a Fake identity for key: " + emKey);
        return fakeOwner;
    }

    public static EntityManager getEntityManager(String emKey) throws Exception {
        EntityManager localEm = (EntityManager) getThreadLocal(emKey);
        if (localEm == null) {
            throw new Exception("EntityManager could not be initialized for key: " + emKey);
        }
        return localEm;
    }

    /*
     * The method for closing a session. The close  
     * will be executed only if the session is actually created
     * by this owner.  
     */
    public static void closeEntityManager(Object ownership, String emKey) {
        if (ownership != null && ((Owner) ownership).identity) {
            log.debug("Identity is accepted. Now closing the session for key: " + emKey);
            EntityManager localEm = (EntityManager) getThreadLocal(emKey);
            if (localEm == null) {
                log.warn("EntityManager is missing. Either it's already closed or never initialized for key: " + emKey);
                return;
            }
            clearThreadLocal(emKey);
            localEm.close();
        } else {
            log.debug("Identity is rejected. Ignoring the request for key: " + emKey);
        }
    }

    /*
     * Thread Local Map Management...
     */
    private static void initThreadLocal() {
        tlMap.set(new HashMap());
    }

    private static void clearThreadLocal(String key) {
        Map threadLocalMap = (Map) tlMap.get();
        threadLocalMap.remove(key);
    }

    private static void setThreadLocal(String key, Object object) {
        if (tlMap.get() == null) {
            initThreadLocal();
        }
        Map threadLocalMap = (Map) tlMap.get();
        threadLocalMap.put(key, object);
    }

    private static Object getThreadLocal(String key) {
        if (tlMap.get() == null) {
            initThreadLocal();
            return null;
        }
        Map threadLocalMap = (Map) tlMap.get();
        return threadLocalMap.get(key);
    }  
    
    // </editor-fold>
    
}
