package haven.minimap;

import java.awt.Color;
import java.util.*;

import haven.*;

public class Radar {
    private final MarkerFactory factory;
    private final Map<Long, Marker> markers = new HashMap<Long, Marker>();
    private final Map<Long, GobRes> undefined = new HashMap<Long, GobRes>();
    private final Map<Long, Color> colors = new HashMap<Long, Color>();
    private final Object markerLock = new Object();
    private final Object colorLock = new Object();

    public Radar() {
        factory = new MarkerFactory();
    }

    public void add(Gob g, Indir<Resource> res) {
        synchronized (markerLock) {
            if (this.contains(g))
                return;
            boolean added = false;
            try {
                Resource r = res.get();
                if (r != null && r.name != null && r.name.length() != 0) {
                    add(r.name, g);
                    added = true;
                }
            } catch (Session.LoadingIndir ignored) {
            } catch (Resource.Loading ignored) {
            }
            if (!added) {
                // resource isn't loaded yet?
                undefined.put(g.id, new GobRes(g, res));
            }
        }
    }

    public void update() {
        synchronized (markerLock) {
            checkUndefined();
        }
    }

    private void add(String name, Gob gob) {
        Marker m = factory.makeMarker(name, gob);
        if (m != null) {
            markers.put(gob.id, m);
            //System.out.println("Marker added: " + name);
        }
    }

    private void checkUndefined() {
        if (undefined.size() == 0)
            return;
        GobRes[] gs = undefined.values().toArray(new GobRes[undefined.size()]);
        for (GobRes gr : gs) {
            try {
                Resource r = gr.res.get();
                if (r != null && r.name != null && r.name.length() != 0) {
                    add(r.name, gr.gob);
                    undefined.remove(gr.gob.id);
                }
            } catch (Session.LoadingIndir ignored) {
            } catch (Resource.Loading ignored) {
            }
        }
    }

    private boolean contains(Gob g) {
        return undefined.containsKey(g.id) || markers.containsKey(g.id);
    }

    public Marker[] getMarkers() {
        synchronized (markerLock) {
            checkUndefined();
            return markers.values().toArray(new Marker[markers.size()]);
        }
    }

    public void remove(Long gobid) {
        synchronized (markerLock) {
            markers.remove(gobid);
            undefined.remove(gobid);
        }
    }

    public void reload() {
        synchronized (markerLock) {
            undefined.clear();
            factory.setConfig(new RadarConfig("radar.xml"));
            Marker[] ms = markers.values().toArray(new Marker[markers.size()]);
            markers.clear();
            for (Marker m : ms) {
                add(m.name, m.gob);
            }
        }
    }
    
    private static class GobRes {
        public final Gob gob;
        public final Indir<Resource> res;
        
        public GobRes(Gob gob, Indir<Resource> res) {
            this.gob = gob;
            this.res = res;
        }
    }
}
