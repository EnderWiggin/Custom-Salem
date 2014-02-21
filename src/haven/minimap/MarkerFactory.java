package haven.minimap;

import java.awt.Color;
import java.util.*;

import haven.*;
import haven.minimap.Marker.Shape;

class MarkerFactory {
    private final Set<String> unknownNameCache;
    private final Map<String, MarkerTemplate> templateCache;
    private final Map<String, ConfigMarker> matches;
    private final List<ConfigMarker> patterns;

    public MarkerFactory() {
        unknownNameCache = new HashSet<String>();
        templateCache = new HashMap<String, MarkerTemplate>();
        matches = new HashMap<String, ConfigMarker>();
        patterns = new ArrayList<ConfigMarker>();

        setConfig(new RadarConfig());
    }

    public Marker makeMarker(String resname, Gob gob) {
	MarkerTemplate mt = findTemplate(resname);
	if ((mt == null) && Config.radar_icons && (gob.getattr(GobIcon.class) != null)){
	    mt = new MarkerTemplate(Color.WHITE, true, resname, true, Shape.CIRCLE);
	    templateCache.put(resname, mt);
	}
	if(mt != null){
	    return new Marker(resname, gob, mt);
	}
	return null;
    }

    private MarkerTemplate findTemplate(String resname) {
        if (unknownNameCache.contains(resname))
            return null;

        if (templateCache.containsKey(resname))
            return templateCache.get(resname);

        // now check if we have match defined in the config
        ConfigMarker marker = matches.get(resname);
        if (marker == null) {
            // try to match resource name with known patterns
            for (ConfigMarker cm : patterns)
                if (resname.matches(cm.match)) {
                    marker = cm;
                    break;
                }
        }

        if (marker != null) {
            MarkerTemplate template = createTemplate(resname, marker);
            templateCache.put(resname, template);
            return template;
        } else {
            unknownNameCache.add(resname);
            return null;
        }
    }

    private MarkerTemplate createTemplate(String resname, ConfigMarker cm) {
        return new MarkerTemplate(cm.color, cm.show, cm.hastext() ? cm.text : resname, cm.tooltip, cm.shape);
    }

    public void setConfig(RadarConfig config) {
        unknownNameCache.clear();
        templateCache.clear();
        matches.clear();
        patterns.clear();

        for (ConfigGroup group : config.getGroups()) {
            for (ConfigMarker marker : group.markers) {
                if (marker.ispattern)
                    patterns.add(marker);
                else
                    matches.put(marker.match, marker);
            }
        }
    }
}
