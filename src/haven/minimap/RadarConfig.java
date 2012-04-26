package haven.minimap;

import haven.Config;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.xml.parsers.*;

import org.w3c.dom.*;

public class RadarConfig {
    public static final String def_config = Config.userhome+"/radar.xml";
    private final File file;
    private List<ConfigGroup> groups = new ArrayList<ConfigGroup>();

    public RadarConfig(String configfile) {
        this(new File(configfile));
    }

    public RadarConfig(File configfile) {
        this.file = configfile;
        try {
            load();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public RadarConfig() {
	this(new File(def_config));
    }

    public Iterable<ConfigGroup> getGroups() {
        return groups;
    }

    /** Loads marker configuration. */
    private void load() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);

        groups.clear();

        NodeList groupNodes = doc.getElementsByTagName("group");
        for (int i = 0; i < groupNodes.getLength(); i++) {
            ConfigGroup g = parseGroup(groupNodes.item(i));
            if (g != null)
                groups.add(g);
        }
    }

    private static ConfigGroup parseGroup(Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE)
            return null;
        Element el = (Element) node;

        ConfigGroup group = new ConfigGroup();
        group.name = el.getAttribute("name");
        group.show = !el.getAttribute("show").equals("false");
        group.color = Utils.parseColor(el.getAttribute("color"));

        NodeList markerNodes = el.getElementsByTagName("marker");
        for (int i = 0; i < markerNodes.getLength(); i++) {
            ConfigMarker m = parseMarker(group, markerNodes.item(i));
            if (m != null)
                group.markers.add(m);
        }

        return group;
    }

    private static ConfigMarker parseMarker(ConfigGroup group, Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE)
            return null;
        Element el = (Element) node;

        ConfigMarker marker = new ConfigMarker();
        marker.text = el.getAttribute("text");
        marker.show = el.hasAttribute("show")
                ? el.getAttribute("show").equals("true")
                : group.show;
        marker.tooltip = el.hasAttribute("tooltip") && el.getAttribute("tooltip").equals("true");

        if (el.hasAttribute("match")) {
            marker.match = el.getAttribute("match");
        } else if (el.hasAttribute("pattern")) {
            marker.match = el.getAttribute("pattern");
            marker.ispattern = true;
        }
        
        Color c = Utils.parseColor(el.getAttribute("color"));
        if (c == null) c = group.color;
        if (c == null) c = Color.WHITE;
        marker.color = c;

        return marker;
    }
}
