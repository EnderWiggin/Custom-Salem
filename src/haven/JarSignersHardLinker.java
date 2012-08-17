package haven;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
 
 
/**
 * A utility class for working around the java webstart jar signing/security bug 
 * 
 * see http://bugs.sun.com/view_bug.do?bug_id=6967414 and http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6805618
 * @author Scott Chan
  */
public class JarSignersHardLinker {
    
    private static final String JRE_1_6_0 = "1.6.0_";
    
    /**
     * the 1.6.0 update where this problem first occurred
     */
    private static final int PROBLEM_JRE_UPDATE = 19;
    
    public static final List sm_hardRefs = new ArrayList();
    
    protected static void makeHardSignersRef(JarFile jar) throws java.io.IOException { 
        
        System.out.println("Making hard refs for: " + jar );
        
        if(jar != null && jar.getClass().getName().equals("com.sun.deploy.cache.CachedJarFile")) {
 
        	//lets attempt to get at the each of the soft links.
        	//first neet to call the relevant no-arg method to ensure that the soft ref is populated
        	//then we access the private member, resolve the softlink and throw it in a static list.
            
            callNoArgMethod("getSigners", jar);
            makeHardLink("signersRef", jar);
            
            callNoArgMethod("getSignerMap", jar);
            makeHardLink("signerMapRef", jar);
            
//            callNoArgMethod("getCodeSources", jar);
//            makeHardLink("codeSourcesRef", jar);
            
            callNoArgMethod("getCodeSourceCache", jar);
            makeHardLink("codeSourceCacheRef", jar);
        }            
    }
    
    
    /**
     * if the specified field for the given instance is a Softreference
     * That soft reference is resolved and the returned ref is stored in a static list,
     * making it a hard link that should never be garbage collected
     * @param fieldName
     * @param instance
     */
    private static void makeHardLink(String fieldName, Object instance) {
        
        System.out.println("attempting hard ref to " + instance.getClass().getName() + "." + fieldName);
        
        try {
            Field signersRef = instance.getClass().getDeclaredField(fieldName);
            
            signersRef.setAccessible(true);
            
            Object o = signersRef.get(instance);
            
            if(o instanceof SoftReference) {
                SoftReference r = (SoftReference) o;
                Object o2 = r.get();
                sm_hardRefs.add(o2);
            } else {
                System.out.println("noooo!");
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Call the given no-arg method on the given instance
     * @param methodName
     * @param instance
     */
    private static void callNoArgMethod(String methodName, Object instance) {
        System.out.println("calling noarg method hard ref to " + instance.getClass().getName() + "." + methodName + "()");
        try {
            Method m = instance.getClass().getDeclaredMethod(methodName);
            m.setAccessible(true);
            
            m.invoke(instance);
 
        } catch (SecurityException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
 
    
    /**
     * is the preloader enabled. ie: will the preloader run in the current environment
     * @return
     */
    public static boolean isHardLinkerEnabled() {
    	
    	boolean isHardLinkerDisabled = false;  //change this to use whatever mechanism you use to enable or disable the preloader
        
        return !isHardLinkerDisabled && /*isRunningOnJre1_6_0_19OrHigher() &&*/ isRunningOnWebstart();
    }
    
    /**
     * is the application currently running on webstart
     * 
     * detect the presence of a JNLPclassloader
     * 
     * @return
     */
    public static boolean isRunningOnWebstart() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        while(cl != null) {
            if(cl.getClass().getName().equals("com.sun.jnlp.JNLPClassLoader")) {
                return true;
            }
            cl = cl.getParent();
        }
        
        return false;
 
    }
    
    /**
     * Is the JRE 1.6.0_19 or higher?
     * @return
     */
    public static boolean isRunningOnJre1_6_0_19OrHigher() {
        String javaVersion = System.getProperty("java.version");
        
        if(javaVersion.startsWith(JRE_1_6_0)) {
            //then lets figure out what update we are on
            String updateStr = javaVersion.substring(JRE_1_6_0.length());
            
            try {
                return Integer.parseInt(updateStr) >= PROBLEM_JRE_UPDATE;
            } catch (NumberFormatException e) {
                //then unable to determine updatedate level
                return false;
            }
        } 
        
        //all other cases
        return false;
        
    }
    
    
	/**
	 * get all the JarFile objects for all of the jars in the classpath
	 * @return
	 */
	public static Set<JarFile> getAllJarsFilesInClassPath() {
	
		Set<JarFile> jars = new LinkedHashSet<JarFile> (); 
	    
	    for (URL url : getAllJarUrls()) {
	        try {
	            jars.add(getJarFile(url));
	        } catch(IOException e) {
	        	System.out.println("unable to retrieve jar at URL: " + url);
	        }
	    }
	    
	    return jars;
	}
	
    /**
     * Returns set of URLS for the jars in the classpath.
     * URLS will have the protocol of jar eg: jar:http://HOST/PATH/JARNAME.jar!/META-INF/MANIFEST.MF
     */
    static Set<URL> getAllJarUrls() {
        try {
            Set<URL> urls = new LinkedHashSet<URL>();
            Enumeration<URL> mfUrls = Thread.currentThread().getContextClassLoader().getResources("META-INF/MANIFEST.MF");
            while(mfUrls.hasMoreElements()) {
                URL jarUrl = mfUrls.nextElement();
//                System.out.println(jarUrl);
                if(!jarUrl.getProtocol().equals("jar")) continue;
                urls.add(jarUrl);
            }
            return urls;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * get the jarFile object for the given url
     * @param jarUrl
     * @return
     * @throws IOException
     */
    public static JarFile getJarFile(URL jarUrl) throws IOException {
        URLConnection urlConnnection = jarUrl.openConnection();
        if(urlConnnection instanceof JarURLConnection) {
            // Using a JarURLConnection will load the JAR from the cache when using Webstart 1.6
            // In Webstart 1.5, the URL will point to the cached JAR on the local filesystem
            JarURLConnection jcon = (JarURLConnection) urlConnnection;
            return jcon.getJarFile();
        } else {
            throw new AssertionError("Expected JarURLConnection");
        }
    }
    
    
    /**
     * Spawn a new thread to run through each jar in the classpath and create a hardlink
     * to the jars softly referenced signers infomation.
     */
    public static void go() {
        if(!isHardLinkerEnabled()) {
            return;
        }
        
        System.out.println("Starting Resource Preloader Hardlinker");
        
        Thread t = new Thread(new Runnable() {
 
            public void run() {
                
                try {
                    Set<JarFile> jars = getAllJarsFilesInClassPath();
                    
                    for (JarFile jar : jars) {
                        makeHardSignersRef(jar);
                    }
 
                } catch (Exception e) {
                    System.out.println("Problem preloading resources");
                    e.printStackTrace();
                } catch (Error e) {
                	System.out.println("Error preloading resources");
                	e.printStackTrace();
                }
            }
            
        });
        
        t.start();
        
    }
}
