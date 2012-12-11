package org.ender.wiki;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ender.wiki.Request.Callback;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Wiki {
    private static final Pattern PAT_CATS = Pattern.compile("\\{\\{([^\\|]*)(|.*?)}}", Pattern.MULTILINE|Pattern.DOTALL);
    private static final Pattern PAT_ARGS = Pattern.compile("\\s*\\|\\s*(.*?)\\s*=\\s*([^\\|]*)", Pattern.MULTILINE|Pattern.DOTALL);
    private static final String CONTENT_URL = "action=query&prop=revisions&titles=%s&rvprop=content&format=json";

    static private final Map<String, String> imap = new HashMap<String, String>(15);
    static private final LinkedBlockingQueue<Request> requests = new LinkedBlockingQueue<Request>();
    static private File folder;
    private static final Map<String, Item> DB = new LinkedHashMap<String, Item>(9, 0.75f, true) {
	private static final long serialVersionUID = 1L;
	@Override
	protected boolean removeEldestEntry(Map.Entry<String, Item> eldest) {
	    return false;
	}
    };

    public static void init(File cfg, int workers){
	imap.put("Arts & Crafts", "arts");
	imap.put("Cloak & Dagger", "cloak");
	imap.put("Faith & Wisdom", "faith");
	imap.put("Frontier & Wilderness", "wild");
	imap.put("Hammer & Nail", "nail");
	imap.put("Hunting & Gathering", "hung");
	imap.put("Law & Lore", "law");
	imap.put("Mines & Mountains", "mine");
	imap.put("Pots & Pans", "pots");
	imap.put("Sparks & Embers", "fire");
	imap.put("Stocks & Cultivars", "stock");
	imap.put("Sugar & Spice", "spice");
	imap.put("Thread & Needle", "thread");
	imap.put("Natural Philosophy", "natp");
	imap.put("Perennial Philosophy", "perp");

	folder = cfg;
	if(!folder.exists()){folder.mkdirs();}

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
    
    public static Item get(String name) {
	return get(name, null);
    }

    public static Item get(String name, Callback callback){
	Item itm = null;
	synchronized (DB) {
	    if(DB.containsKey(name)){
		itm = DB.get(name);
		if(callback != null){callback.wiki_item_ready(itm);}
		return itm;
	    }
	    itm = get_cache(name, true);
	    DB.put(name, itm);
	}
	request(new Request(name, callback));
	return itm;
    }

    private static void request(Request request) {
	try {
	    requests.put(request);
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
	FileWriter fw;
	try {
	    fw = new FileWriter(new File(folder, item.name+".xml"));
	    fw.write(item.toXML());
	    fw.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public static String stream2str(java.io.InputStream is) {
	java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	return s.hasNext() ? s.next() : "";
    }

    private static void load(Request request){
	//System.out.println(String.format("Loading '%s' at '%s'", name, Thread.currentThread().getName()));
	Item item = get_cache(request.name, false);
	if(item == null){
	    item = new Item();
	    item.name = request.name;
	    item.content = get_content(request.name);
	    if(item.content != null){
		parse_content(item);
		item.content = parse_wiki(item);
		cache(item);
	    }
	}
	store(request.name, item);
	if(request.callback != null){request.callback.wiki_item_ready(item);}
	//System.out.println(String.format("Finished '%s' at '%s'", name, Thread.currentThread().getName()));
    }

    private static void parse_content(Item item) {
	Matcher m = PAT_CATS.matcher(item.content);
	while(m.find()){
	    if(m.groupCount() == 2){
		String method = m.group(1).trim();
		String argsline = m.group(2);
		parse(item, method, getargs(argsline));
	    }

	    item.content = m.replaceFirst("");
	    m = PAT_CATS.matcher(item.content);
	}
	m = Pattern.compile("\\{\\|..*?}", Pattern.DOTALL|Pattern.MULTILINE).matcher(item.content);
	while(m.find()){
	    Pattern p = Pattern.compile("\\'\\'\\'([^\\']+)\\'\\'\\'", Pattern.DOTALL|Pattern.MULTILINE);
	    Matcher tabs = p.matcher(m.group());
	    while(tabs.find()){
		System.out.println("Tab: "+tabs.group(1));
	    }
	    p = Pattern.compile("\\|-(.*?)(?=\\|-)", Pattern.DOTALL|Pattern.MULTILINE);
	    Matcher rows = p.matcher(m.group());
	    while(rows.find()){
		System.out.println(rows.group(1));
	    }
	}
    }

    private static void parse(Item item, String method, Map<String, String> args) {
	if(method.equals("Crafted")){
	    String reqs = args.get("Objects required");
	} else if(method.equals("Inspirational")){
	    Map<String, Integer> attrs = new HashMap<String, Integer>();
	    for(Entry<String, String> e : args.entrySet()){
		try {
		    attrs.put(imap.get(e.getKey()), Integer.parseInt(e.getValue()));
		} catch (NumberFormatException ex){}
	    }
	    item.attgive = attrs;
	} else if(method.equals("Food")){
	    Map<String, Float[]> food = new HashMap<String, Float[]>(5);
	    for(String key : args.keySet()){
		String[] svals = args.get(key).split(",");
		Float[] vals = new Float[4];
		int i=0;
		for(String sval : svals){
		    float val = 0;
		    try{
			val = Float.parseFloat(sval);
		    } catch (NumberFormatException ex){}
		    vals[i] = val;
		    i++;
		}
		food.put(key,  vals);
	    }
	    item.food = food;
	} else {
	    System.out.println(String.format("Item '%s': Unknown method '%s', args: %s",item.name, method, args.toString()));
	}
    }

    private static Map<String, String> getargs(String argsline) {
	Map<String, String> args = new HashMap<String, String>();
	Matcher m = PAT_ARGS.matcher(argsline);
	while(m.find()){
	    if(m.groupCount() == 2){
		String name = m.group(1).trim();
		String val = m.group(2).trim();
		args.put(name,  val);
	    }
	}
	return args;
    }

    private static String get_content(String name){
	String content = null;
	try {
	    URI uri = new URI("http", null, "salemwiki.info", -1, "/api.php", String.format(CONTENT_URL, name), null);

	    URL link = uri.toURL();
	    String data = stream2str(link.openStream());
	    JSONObject json = new JSONObject(data);
	    json = json.getJSONObject("query").getJSONObject("pages");
	    String pageid = JSONObject.getNames(json)[0];
	    content = json.getJSONObject(pageid).getJSONArray("revisions").getJSONObject(0).getString("*");
	    if(content.indexOf("#REDIRECT") == 0){
		return get_content(get_redirect(content));
	    }
	    return content;
	} catch (JSONException e) {
	    System.err.println(String.format("Error while parsing '%s':\n%s\nContent:'%s'", name, e.getMessage(), content));
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (URISyntaxException e) {
	    e.printStackTrace();
	}
	return null;
    }
    
    private static String parse_wiki(Item item){
	String content = null;
	try {
	    URI uri = new URI("http", null, "salemwiki.info", -1, "/api.php", null, null);

	    URL link = uri.toURL();
	    HttpURLConnection conn = (HttpURLConnection) link.openConnection();
	    conn.setRequestMethod("POST");
	    conn.setDoOutput(true);
	    conn.setDoInput(true);
	    String data = URLEncoder.encode(item.content.trim(), "UTF-8");
	    String title = URLEncoder.encode(item.name, "UTF-8");
	    String req = String.format("action=parse&format=json&text=%s&title=%s", data, title);
	    DataOutputStream wr = new DataOutputStream(conn.getOutputStream ());
	    wr.writeBytes(req);
	    wr.flush();
	    wr.close();
	    data = stream2str(conn.getInputStream());
	    JSONObject json = new JSONObject(data);
	    json = json.getJSONObject("parse").getJSONObject("text");
	    content = json.getString("*");
	    return content;
	} catch (JSONException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (URISyntaxException e) {
	    e.printStackTrace();
	}
	return null;
    }

    private static String get_redirect(String content) {
	Matcher m = Pattern.compile("#REDIRECT\\s*\\[\\[(.*)\\]\\]").matcher(content);
	if(m.find() && m.groupCount() == 1){
	    return m.group(1);
	}
	return null;
    }

    private static Item get_cache(String name, boolean fast) {
	File f = new File(folder, name+".xml");
	if(!f.exists()){return null;}
	if(!fast && has_update(name, f.lastModified())){
	    return null;
	}
	return load_cache(f);
    }

    private static Item load_cache(File f) {
	try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(f);
	    Item item = new Item();
	    item.name = doc.getDocumentElement().getAttribute("name");

	    item.required = parse_cache(doc, "required");
	    item.locations = parse_cache(doc, "locations");
	    item.reqby = parse_cache(doc, "reqby");
	    item.tech = parse_cache(doc, "tech");
	    item.unlocks = parse_cache(doc, "unlocks");
	    item.attreq = parse_cache_map(doc, "attreq");
	    item.attgive = parse_cache_map(doc, "attgive");
	    item.food = parse_cache_food(doc, "food");
	    item.content = parse_cache_content(doc, "content");

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

    private static String parse_cache_content(Document doc, String tag) {
	NodeList list = doc.getElementsByTagName(tag);
	if(list.getLength() > 0){
	    Node item = list.item(0);
	    return item.getTextContent();
	}
	return null;
    }

    private static Map<String, Float[]> parse_cache_food(Document doc, String tag) {
	NodeList list = doc.getElementsByTagName(tag);
	if(list.getLength() > 0){
	    Node item = list.item(0);
	    Map<String, Float[]> food = new HashMap<String, Float[]>();
	    NamedNodeMap attrs = item.getAttributes();
	    for(int i=0; i< attrs.getLength(); i++){
		Node attr = attrs.item(i);
		String svals[] = attr.getNodeValue().split(" ");
		Float[] vals = new Float[4];
		for(int j=0; j<4; j++){
		    try{
			vals[j] = Float.parseFloat(svals[j]);
		    }catch (NumberFormatException ex){
			vals[j] = 0.0f;
		    }
		}
		food.put(attr.getNodeName(), vals);
	    }
	    return food;
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

    private static boolean has_update(String name, long date) {
	try {
	    if(date < 1354898300927L){return true;}//ignore old cache
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

}
