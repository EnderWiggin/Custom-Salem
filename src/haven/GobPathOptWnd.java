package haven;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GobPathOptWnd extends Window {
    private static Window instance;

    public static void toggle() {
	if (instance == null) {
	    instance = new GobPathOptWnd(Coord.z, UI.instance.gui);
	} else {
	    UI.instance.destroy(instance);
	}
    }

    public GobPathOptWnd(Coord c, Widget parent) {
	super(c, Coord.z, parent, "Actor Path Options");

	justclose = true;

	List<String> animals = new LinkedList<String>(Config.gobPathCfg.keySet());
	Collections.sort(animals);
	int k = 0;
	for (String animal : animals) {
	    new Element(new Coord(0, 25 * k++), this, animal);
	}

	pack();
    }

    @Override
    public void destroy() {
	instance = null;
	super.destroy();
	Config.saveGobPathCfg();
    }

    private static class Element extends Widget {

	public Element(Coord c, Widget parent, String name) {
	    super(c, new Coord(300, 20), parent);
	    final GobPath.Cfg cfg = Config.getGobPathCfg(name);
	    if(cfg.name != null){
		name = cfg.name;
	    }
	    (new CheckBox(Coord.z, this, name) {
		@Override
		public void changed(boolean val) {
		    cfg.show = val;
		}
	    }).a = cfg.show;
	}
    }
}
