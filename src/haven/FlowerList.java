package haven;

import java.util.*;

import static haven.Config.AUTOCHOOSE;
import static haven.Config.saveFile;
import static haven.Window.cbtni;

public class FlowerList extends Scrollport {

    private final IBox box;

    public FlowerList(Coord c, Widget parent) {
	super(c, new Coord(200, 250), parent);
	box = new IBox("gfx/hud", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb");

	int i = 0;
	for (Map.Entry<String, Boolean> entry : AUTOCHOOSE.entrySet()) {
	    new Item(new Coord(0, 25 * i++), entry.getKey(), cont);
	}

	update();
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if (msg.equals("changed")) {
	    String name = (String) args[0];
	    boolean val = (Boolean) args[1];
	    synchronized (AUTOCHOOSE) {
		AUTOCHOOSE.put(name, val);
	    }
	    Config.saveAutochoose();
	} else if (msg.equals("delete")) {
	    String name = (String) args[0];
	    synchronized (AUTOCHOOSE) {
		AUTOCHOOSE.remove(name);
	    }
	    Config.saveAutochoose();
	    ui.destroy(sender);
	    update();
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void add(String name) {
	if(name != null && !name.isEmpty() && !AUTOCHOOSE.containsKey(name)) {
	    synchronized (AUTOCHOOSE) {
		AUTOCHOOSE.put(name, true);
	    }
	    Config.saveAutochoose();
	    new Item(new Coord(0, 0), name, cont);
	    update();
	}
    }

    private void update() {
	LinkedList<String> order = new LinkedList<String>(AUTOCHOOSE.keySet());
	Collections.sort(order);
	for(Widget wdg = cont.lchild; wdg != null; wdg = wdg.prev) {
	    int i = order.indexOf(((Item)wdg).name);
	    wdg.c.y = 25 * i;
	}
	cont.update();
    }

    @Override
    public void draw(GOut g) {
	super.draw(g);
	box.draw(g, Coord.z, sz);
    }

    private static class Item extends Widget {

	public final String name;
	private final CheckBox cb;
	private boolean highlight = false;
	private boolean a = false;

	public Item(Coord c, String name, Widget parent) {
	    super(c, new Coord(200, 25), parent);
	    this.name = name;

	    cb = new CheckBox(new Coord(3, 3), this, name);
	    cb.a = AUTOCHOOSE.get(name);
	    cb.canactivate = true;
	    new IButton(new Coord(178, 5), this, cbtni[0], cbtni[1], cbtni[2]);

	}

	@Override
	public void draw(GOut g) {
	    if (highlight) {
		g.chcolor(255, 255, 0, 128);
		g.poly2(Coord.z, Listbox.selr,
			new Coord(0, sz.y), Listbox.selr,
			sz, Listbox.overr,
			new Coord(sz.x, 0), Listbox.overr);
		g.chcolor();
	    }
	    super.draw(g);
	}

	@Override
	public void mousemove(Coord c) {
	    highlight = c.isect(Coord.z, sz);
	    super.mousemove(c);
	}

	@Override
	public boolean mousedown(Coord c, int button) {
	    if(super.mousedown(c, button)){
		return true;
	    }
	    if(button != 1)
		return(false);
	    a = true;
	    ui.grabmouse(this);
	    return(true);
	}

	@Override
	public boolean mouseup(Coord c, int button) {
	    if(a && button == 1) {
		a = false;
		ui.grabmouse(null);
		if(c.isect(new Coord(0, 0), sz))
		    click();
		return(true);
	    }
	    return(false);
	}

	private void click() {
	    cb.a = !cb.a;
	    wdgmsg("changed", name, cb.a);
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
	    if (msg.equals("ch")) {
		wdgmsg("changed", name, args[0]);
	    } else if (msg.equals("activate")) {
		wdgmsg("delete", name);
	    } else {
		super.wdgmsg(sender, msg, args);
	    }
	}
    }
}
