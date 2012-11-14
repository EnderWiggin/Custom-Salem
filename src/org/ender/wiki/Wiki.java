package org.ender.wiki;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Wiki {
    private static final String RDF_URL = "http://salemwiki.info/index.php/Special:ExportRDF/";

    static private final Map<String, String> gmap = new HashMap<String, String>(15);
    static private final Map<String, String> rmap = new HashMap<String, String>(15);
    private static Properties props;
    static private final LinkedBlockingQueue<String> requests = new LinkedBlockingQueue<String>();
    static private File config, folder;
    private static final Map<String, Item> DB = new LinkedHashMap<String, Item>(9, 0.75f, true) {
	private static final long serialVersionUID = 1L;
	@Override
	protected boolean removeEldestEntry(Map.Entry<String, Item> eldest) {
	    return false;
	}
    };

    public static void init(File cfg, int workers){
	gmap.put("property:Gives_Arts_-26_Crafts", "arts");
	gmap.put("property:Gives_Cloak_-26_Dagger", "cloak");
	gmap.put("property:Gives_Faith_-26_Wisdom", "faith");
	gmap.put("property:Gives_Frontier_-26_Wilderness", "wild");
	gmap.put("property:Gives_Hammer_-26_Nail", "nail");
	gmap.put("property:Gives_Hunting_-26_Gathering", "hung");
	gmap.put("property:Gives_Law_-26_Lore", "law");
	gmap.put("property:Gives_Mines_-26_Mountains", "mine");
	gmap.put("property:Gives_Pots_-26_Pans", "pots");
	gmap.put("property:Gives_Sparks_-26_Embers", "fire");
	gmap.put("property:Gives_Stocks_-26_Cultivars", "stock");
	gmap.put("property:Gives_Sugar_-26_Spice", "spice");
	gmap.put("property:Gives_Thread_-26_Needle", "thread");
	gmap.put("property:Gives_Natural_Philosophy", "natp");
	gmap.put("property:Gives_Perennial_Philosophy", "perp");

	rmap.put("property:Requires_Arts_-26_Crafts", "arts");
	rmap.put("property:Requires_Cloak_-26_Dagger", "cloak");
	rmap.put("property:Requires_Faith_-26_Wisdom", "faith");
	rmap.put("property:Requires_Frontier_-26_Wilderness", "wild");
	rmap.put("property:Requires_Hammer_-26_Nail", "nail");
	rmap.put("property:Requires_Hunting_-26_Gathering", "hung");
	rmap.put("property:Requires_Law_-26_Lore", "law");
	rmap.put("property:Requires_Mines_-26_Mountains", "mine");
	rmap.put("property:Requires_Pots_-26_Pans", "pots");
	rmap.put("property:Requires_Sparks_-26_Embers", "fire");
	rmap.put("property:Requires_Stocks_-26_Cultivars", "stock");
	rmap.put("property:Requires_Sugar_-26_Spice", "spice");
	rmap.put("property:Requires_Thread_-26_Needle", "thread");
	rmap.put("property:Requires_Natural_Philosophy", "natp");
	rmap.put("property:Requires_Perennial_Philosophy", "perp");

	folder = cfg;
	if(!folder.exists()){folder.mkdirs();}
	config = new File(folder, "cache.cfg");
	loadProps();

	for(int i=0; i<workers; i++){
	    Thread t = new Thread(new Runnable() {

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
	    }, "Wiki loader "+i);
	    t.setDaemon(true);
	    t.start();
	}
    }

    public static Item get(String name){
	Item itm = null;
	synchronized (DB) {
	    if(DB.containsKey(name)){
		return DB.get(name);
	    }
	    itm = get_cache(name, true);
	    DB.put(name, itm);
	}
	request(name);
	return itm;
    }

    private static void request(String name) {
	try {
	    requests.put(name);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    private static void store(String label, Item item) {
	synchronized (DB) {
	    DB.put(label, item);
	}
	//System.out.println(item.toString());
    }

    private static void cache(Item item) {
	synchronized (props) {
	    props.setProperty(item.name, item.url);
	}
	saveProps();
	FileWriter fw;
	try {
	    fw = new FileWriter(new File(folder, item.url+".xml"));
	    fw.write(item.toXML());
	    fw.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private static void load(String name){
	//System.out.println(String.format("Loading '%s' at '%s'", name, Thread.currentThread().getName()));
	try {
	    Item item = get_cache(name, false);
	    if(item == null){
		String url = String.format("%s%s", RDF_URL, name);

		//parse
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(url);

		item = new Item();
		item.name = name;

		NodeList objs = doc.getElementsByTagName("swivt:Subject");
		parseItem(objs.item(0), item);
		for (int i = 1; i < objs.getLength(); i++) {
		    parseSubject(objs.item(i), item);
		}
		cache(item);
	    }
	    store(name, item);
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (ParserConfigurationException e) {
	    e.printStackTrace();
	}
	//System.out.println(String.format("Finished '%s' at '%s'", name, Thread.currentThread().getName()));
    }

    private static Item get_cache(String name, boolean fast) {
	String fn;
	synchronized (props) {
	    fn = props.getProperty(name, null);
	}
	if(fn != null){
	    File f = new File(folder, fn+".xml");
	    if(!f.exists()){return null;}
	    if(!fast && !has_update(name, f.lastModified()));
	    return load_cache(f, fn);
	}
	return null;
    }

    private static Item load_cache(File f, String fn) {
	try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(f);
	    Item item = new Item();
	    item.name = doc.getDocumentElement().getAttribute("name");
	    item.url = fn;

	    item.required = parse_cache(doc, "required");
	    item.locations = parse_cache(doc, "locations");
	    item.reqby = parse_cache(doc, "reqby");
	    item.tech = parse_cache(doc, "tech");
	    item.unlocks = parse_cache(doc, "unlocks");
	    item.attreq = parse_cache_map(doc, "attreq");
	    item.attgive = parse_cache_map(doc, "attgive");

	    return item;
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (ParserConfigurationException e) {
	    e.printStackTrace();
	}
	return null;
    }

    private static Set<String> parse_cache(Document doc, String tag) {
	NodeList list = doc.getElementsByTagName(tag);
	if(list.getLength() > 0){
	    Set<String> items = new HashSet<String>(list.getLength());
	    for(int i=0; i< list.getLength(); i++){
		items.add(list.item(i).getAttributes().getNamedItem("name").getNodeValue());
	    }
	    return items;
	}
	return null;
    }

    private static Map<String, Integer> parse_cache_map(Document doc, String tag) {
	NodeList list = doc.getElementsByTagName(tag);
	if(list.getLength() > 0){
	    Node item = list.item(0);
	    Map<String, Integer> items = new HashMap<String, Integer>();
	    NamedNodeMap attrs = item.getAttributes();
	    for(int i=0; i< attrs.getLength(); i++){
		Node attr = attrs.item(i);
		items.put(attr.getNodeName(), Integer.decode(attr.getNodeValue()));
	    }
	    return items;
	}
	return null;
    }

    private static void parseSubject(Node node, Item item) {
	if (node.getNodeType() != Node.ELEMENT_NODE){return;}
	Element el = (Element) node;
	String name = el.getElementsByTagName("rdfs:label").item(0).getTextContent();
	if(name.indexOf("Help:")>=0){return;}
	if(el.getElementsByTagName("property:Unlocks").getLength() > 0){
	    if(item.tech == null){
		item.tech = new HashSet<String>();
	    }
	    item.tech.add(name);
	} else if(el.getElementsByTagName("property:Requires").getLength() > 0){
	    if(item.reqby == null){
		item.reqby = new HashSet<String>();
	    }
	    item.reqby.add(name);
	} else if(el.getElementsByTagName("property:RequiresTech").getLength() > 0){
	    if(item.unlocks == null){
		item.unlocks = new HashSet<String>();
	    }
	    item.unlocks.add(name);
	}
    }

    private static void parseItem(Node node, Item item) {
	if (node.getNodeType() != Node.ELEMENT_NODE){return;}
	Element el = (Element) node;
	node = el.getElementsByTagName("rdfs:isDefinedBy").item(0);
	item.url = last(node.getAttributes().getNamedItem("rdf:resource").getNodeValue());
	NodeList types = el.getElementsByTagName("rdf:type");
	for(int i=0; i<types.getLength(); i++){
	    String type = last(types.item(i).getAttributes().getNamedItem("rdf:resource").getNodeValue());
	    if(type.equals("Category-3AObjects")){
		parseObjects(el, item);
	    } else if (type.equals("Category-3ACrafted")){
		parseCrafted(el, item);
	    } else if (type.equals("Category-3AForageables")){
		parseForaged(el, item);
	    } else if (type.equals("Category-3ASkills")){
		parseSkill(el, item);
	    } else if (type.equals("Category-3AInspirationals")){
		parseCurio(el, item);
	    } else {
		System.out.println(type);
	    }
	}
    }

    private static void parseCurio(Element el, Item item) {
	Map<String, Integer> props = null;
	NodeList nodes = el.getChildNodes();
	for(int i=0; i<nodes.getLength(); i++){
	    Node node = nodes.item(i);
	    String name = node.getNodeName();
	    if(gmap.containsKey(name)){
		if(props == null)
		    props = new HashMap<String, Integer>(15);
		props.put(gmap.get(name), Integer.parseInt(node.getTextContent()));
	    }
	}
	item.attgive = props;
    }

    private static void parseSkill(Element el, Item item) {
	NodeList locations = el.getElementsByTagName("property:Unlocks");
	item.unlocks = new HashSet<String>(locations.getLength()); 
	for(int i=0; i<locations.getLength(); i++){
	    Node node = locations.item(i).getAttributes().getNamedItem("rdf:resource");
	    item.unlocks.add(get_name(node.getNodeValue()));
	}
	Map<String, Integer> props = null;
	NodeList nodes = el.getChildNodes();
	for(int i=0; i<nodes.getLength(); i++){
	    Node node = nodes.item(i);
	    String name = node.getNodeName();
	    if(rmap.containsKey(name)){
		if(props == null)
		    props = new HashMap<String, Integer>(15);
		props.put(rmap.get(name), Integer.parseInt(node.getTextContent()));
	    }
	}
	item.attreq = props;
    }

    private static void parseForaged(Element el, Item item) {
	NodeList locations = el.getElementsByTagName("property:Location");
	item.locations = new HashSet<String>(locations.getLength()); 
	for(int i=0; i<locations.getLength(); i++){
	    Node node = locations.item(i).getAttributes().getNamedItem("rdf:resource");
	    item.locations.add(get_name(node.getNodeValue()));
	}
    }

    private static void parseCrafted(Element el, Item item) {
	NodeList required = el.getElementsByTagName("property:Requires");
	item.required = new HashSet<String>(required.getLength()); 
	for(int i=0; i<required.getLength(); i++){
	    Node node = required.item(i).getAttributes().getNamedItem("rdf:resource");
	    item.required.add(get_name(node.getNodeValue()));
	}
    }

    private static void parseObjects(Element el, Item item) {
	item.name = el.getElementsByTagName("rdfs:label").item(0).getTextContent();
    }

    private static String get_name(String link){
	try {
	    String url = String.format("%s%s?recursive=0&backlinks=0", RDF_URL, last(link));
	    //parse
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(url);

	    NodeList objs = doc.getElementsByTagName("swivt:Subject");
	    Element el = (Element) objs.item(0);
	    return el.getElementsByTagName("rdfs:label").item(0).getTextContent();

	} catch (MalformedURLException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (ParserConfigurationException e) {
	    e.printStackTrace();
	}
	return last(link);
    }

    private static boolean has_update(String name, long date) {
	try {
	    //String p = String.format("%s%s", WIKI_URL, name);
	    URI uri = new URI("http","salemwiki.info","/index.php/"+name, null);
	    URL  url = uri.toURL();
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod("HEAD");
	    conn.setIfModifiedSince(date);
	    //conn.disconnect();
	    if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
		return true;
	    }
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (URISyntaxException e1) {
	    e1.printStackTrace();
	}
	return false;
    }

    private static void loadProps(){
	props = new Properties();
	if (!config.exists()) {
	    try {
		config.createNewFile();
	    } catch (IOException e) {
		return;
	    }
	}
	try {
	    props.load(new FileInputStream(config));
	}
	catch (IOException e) {
	    System.out.println(e);
	}
    }

    private static void saveProps(){
	synchronized (props) {
	    try {
		props.store(new FileOutputStream(config), "cache file names");
	    } catch (IOException e) {
		System.out.println(e);
	    }
	}
    }

    private static String last(String url){
	int k = url.lastIndexOf("/");
	return url.substring(k + 1);
    }
}
