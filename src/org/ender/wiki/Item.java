package org.ender.wiki;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Item {
    public String name;
    public Set<String> required;
    public Set<String> locations;
    public Set<String> tech;
    public Set<String> reqby;
    public Set<String> unlocks;
    public Map<String, Integer> attreq;
    public Map<String, Integer> attgive;
    public String content;
    public Map<String, Integer[]> food_reduce;
    public Map<String, Integer[]> food_restore;
    public Map<String, Float[]> food;
    public int food_full = 0;
    public int food_uses = 1;
    public int cloth_slots = 0;
    public int cloth_pmin = 0;
    public int cloth_pmax = 0;
    public String[] cloth_profs;
    public int art_pmin;
    public int art_pmax;
    public String[] art_profs;
    public Map<String, Integer> art_bonuses;

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
	if(food != null){ xml_food(builder); }
	if(content != null){xml_content(builder);}
	cloth_xml(builder);
	art_xml(builder);
	builder.append("\n</item>");
	return builder.toString();
    }
    
    private void art_xml(StringBuilder builder) {
	if(art_profs == null || art_profs.length == 0){return;}
	String tag = "artifact";
	builder.append(String.format("\n  <%s", tag));
	builder.append(String.format(" difficulty=\"%d to %d\"",100 - art_pmin, 100 - art_pmax));
	builder.append(String.format(" profs=\"%s\"",join(", ", art_profs).replaceAll("&", "&amp;")));
	
	String bonuses = "";
	boolean first = true;
	for(Entry<String, Integer> entry : art_bonuses.entrySet()){
	    if(!first){bonuses += ", ";}
	    bonuses += String.format("%s=%d", entry.getKey(), entry.getValue());
	    first = false;
	}
	builder.append(String.format(" bonuses=\"%s\"", bonuses.replaceAll("&", "&amp;")));
	
	builder.append(String.format(" />"));
    }

    private void cloth_xml(StringBuilder builder) {
	if(cloth_slots == 0){return;}
	String tag = "cloth";
	builder.append(String.format("\n  <%s", tag));
	builder.append(String.format(" slots=\"%d\"",cloth_slots));
	builder.append(String.format(" difficulty=\"%d to %d\"",100 - cloth_pmin, 100 - cloth_pmax));
	builder.append(String.format(" profs=\"%s\"",join(", ",cloth_profs).replaceAll("&", "&amp;")));
	builder.append(String.format(" />"));
    }

    private void xml_content(StringBuilder builder) {
	String tag = "content";
	builder.append(String.format("\n  <%s><![CDATA[%s]]></%s>", tag, content, tag));
    }

    private void xml_food(StringBuilder builder) {
	String tag = "food";
	builder.append(String.format("\n  <%s", tag));
	for (Entry<String , Float[]> e : food.entrySet()){
	    Float[] vals = e.getValue();
	    builder.append(String.format(" %s=\"%s %s %s %s\"", e.getKey(), vals[0], vals[1], vals[2], vals[3]));
	}
	builder.append(String.format(" full=\"%d\" uses=\"%d\"", food_full, food_uses));
	builder.append(String.format(" />"));
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

    public void setClothing(int slots) {
	this.cloth_slots = slots;
	if(slots == 0){return;}
	this.cloth_pmin = 100;
	this.cloth_pmax = 100;
	this.cloth_profs = new String[0];
    }
    
    public void setArtifact(String difficulty, String[] profs, Map<String, Integer> bonuses) {
	String[] ds = difficulty.split(" to ");
	try{
	    this.art_pmin = 100 - Integer.parseInt(ds[0]);
	    this.art_pmax = 100 - Integer.parseInt(ds[1]);
	} catch(Exception ignored){}
	this.art_profs = profs;
	this.art_bonuses = bonuses;
    }
    
    String join(String separator, String[] s) {
      int k=s.length;
      if (k==0)
        return null;
      StringBuilder out=new StringBuilder();
      out.append(s[0]);
      for (int x=1;x<k;++x)
        out.append(separator).append(s[x]);
      return out.toString();
    }

    public Object[] getArtBonuses() {
	if(art_bonuses == null){return new Object[]{0};}
	Object[] ret = new Object[1 + art_bonuses.size() * 2];
	int i = 0;
	ret[i++] = 0;
	for(Entry<String, Integer> entry : art_bonuses.entrySet()){
	    ret[i++] = entry.getKey();
	    ret[i++] = entry.getValue();
	}
	return ret;
    }

}