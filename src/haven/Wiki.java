package haven;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.parsers.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Wiki {
    
    static private final Map<String, String> pmap = new HashMap<String, String>(15);
    static private final LinkedBlockingQueue<String> requests = new LinkedBlockingQueue<String>();
    static private Map<String, Map<String, Integer>> DB = new HashMap<String, Map<String,Integer>>();
    
    static {
	pmap.put("property:Gives_Arts_-26_Crafts", "arts");
	pmap.put("property:Gives_Cloak_-26_Dagger", "cloak");
	pmap.put("property:Gives_Faith_-26_Wisdom", "faith");
	pmap.put("property:Gives_Frontier_-26_Wilderness", "wild");
	pmap.put("property:Gives_Hammer_-26_Nail", "nail");
	pmap.put("property:Gives_Hunting_-26_Gathering", "hung");
	pmap.put("property:Gives_Law_-26_Lore", "law");
	pmap.put("property:Gives_Mines_-26_Mountains", "mine");
	pmap.put("property:Gives_Pots_-26_Pans", "pots");
	pmap.put("property:Gives_Sparks_-26_Embers", "fire");
	pmap.put("property:Gives_Stocks_-26_Cultivars", "stock");
	pmap.put("property:Gives_Sugar_-26_Spice", "spice");
	pmap.put("property:Gives_Thread_-26_Needle", "thread");
	pmap.put("property:Gives_Natural_Philosophy", "natp");
	pmap.put("property:Gives_Perennial_Philosophy", "perp");

	Thread t = new HackThread(new Runnable() {
	    
	    @Override
	    public void run() {
		while(true){
		    try {
			load(requests.take());
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}
	    }
	}, "Wiki loader");
	t.setDaemon(true);
	t.start();
    }
    
    public static Map<String, Integer> get(String name){
	synchronized (DB) {
	    if(DB.containsKey(name)){
		return DB.get(name);
	    }
	    DB.put(name, null);
	}
	request(name);
	return null;
    }
    
    private static void request(String name) {
	try {
	    requests.put(name);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    private static void load(String name){
	try {
	    String url = String.format("http://salemwiki.info/index.php/Special:ExportRDF/%s?recursive=0&backlinks=0", name);
	    //parse
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(url);
	    
	    NodeList props = doc.getElementsByTagName("swivt:Subject");
	    for (int i = 0; i < props.getLength(); i++) {
		parseSubject(props.item(i));
	    }
	    
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (ParserConfigurationException e) {
	    e.printStackTrace();
	}
	
    }

    private static void parseSubject(Node node) {
	if (node.getNodeType() != Node.ELEMENT_NODE)
            return;
        Element el = (Element) node;
        NodeList nodes = el.getElementsByTagName("*");
        String label = null;
        Map<String, Integer> props = null;
        for(int i=0; i<nodes.getLength(); i++){
            Node item = nodes.item(i);
            String name = item.getNodeName();
            if(name.equals("rdfs:label")){
        	label = item.getTextContent();
            } else if(pmap.containsKey(name)){
        	if(props == null)
        	    props = new HashMap<String, Integer>(15);
        	props.put(pmap.get(name), Integer.parseInt(item.getTextContent()));
            }
        }
        
        store(label, props);
    }

    private static void store(String label, Map<String, Integer> props) {
	synchronized (DB) {
	    DB.put(label, props);
	}
    }
}
