package org.ender.wiki;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Item {
    public String name;
    public String url;
    public Set<String> required;
    public Set<String> locations;
    public Set<String> tech;
    public Set<String> reqby;
    public Set<String> unlocks;
    public Map<String, Integer> attreq;
    public Map<String, Integer> attgive;

    public String toXML(){
	StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
	builder.append(String.format("<item name=\"%s\" >", name.replaceAll("&", "&amp;")));
	if(required != null){ xml(builder, required, "required"); }
	if(locations != null){ xml(builder, locations, "locations"); }
	if(tech != null){ xml(builder, tech, "tech"); }
	if(reqby != null){ xml(builder, reqby, "reqby"); }
	if(unlocks != null){ xml(builder, unlocks, "unlocks"); }
	if(attreq != null){ xml(builder, attreq, "attreq"); }
	if(attgive != null){ xml(builder, attgive, "attgive"); }
	builder.append("\n</item>");
	return builder.toString();
    }
    
    private void xml(StringBuilder builder, Map<String, Integer> map, String tag) {
	builder.append(String.format("\n  <%s", tag));
	for (Entry<String , Integer> e : map.entrySet()){
	    builder.append(String.format(" %s=\"%d\"", e.getKey(), e.getValue()));
	}
	builder.append(String.format(" />"));
    }

    private void xml(StringBuilder builder, Set<String> list, String tag) {
	for(String name : list){
	    builder.append(String.format("\n  <%s name=\"%s>\" />", tag, name.replaceAll("&", "&amp;")));
	}
    }

    public String toString(){
	StringBuilder builder = new StringBuilder();

	builder.append(String.format("Wiki Item '%s'", name));

	if(locations != null){
	    append(builder, locations, "Locations");
	}

	if(required != null){
	    append(builder, required, "Requires");
	}

	if(reqby != null){
	    append(builder, reqby, "Used by");
	}

	if(tech != null){
	    append(builder, tech, "Skills needed");
	}

	if(unlocks != null){
	    append(builder, unlocks, "Unlocks");
	}
	
	if(attreq != null){
	    append(builder, attreq, "Profs required");
	}
	
	if(attgive != null){
	    append(builder, attgive, "Profs gain");
	}

	return builder.toString();
    }

    private void append(StringBuilder builder, Map<String, Integer> props, String msg) {
	builder.append(String.format("\n\t%s: ", msg));
	String c = "";
	for(Entry<String, Integer> e : props.entrySet()){
	    builder.append(String.format("%s'%s:%d'", c, e.getKey(), e.getValue()));
	    c = ", ";
	}
	builder.append(';');
    }

    private void append(StringBuilder builder, Set<String> list, String msg){
	builder.append(String.format("\n\t%s: ", msg));
	String c = "";
	for(String name : list){
	    builder.append(String.format("%s'%s'", c, name));
	    c = ", ";
	}
	builder.append(';');
    }
}