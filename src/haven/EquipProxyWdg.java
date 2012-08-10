package haven;

import static haven.Inventory.invsq;

public class EquipProxyWdg extends Widget implements DTarget {
    protected static final IBox wbox = new IBox("gfx/hud", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb");
    private static final Coord sqsz = Inventory.sqsz.add(2,1);
    private int slots[];
    private Coord tlo, wbsz;
    public EquipProxyWdg(Coord c, int[] slots, Widget parent) {
	super(c, sqsz.mul(slots.length, 1).sub(1,0), parent);
	this.slots = slots;
	tlo = wbox.tloff().inv();
	wbsz = sz.add(wbox.bisz());
    }
    
    private int slot(Coord c){
	return slots[c.x / sqsz.x];
    }
    
    @Override
    public boolean mousedown(Coord c, int button) {
	Equipory e = ui.gui.getEquipory();
	if(e != null){
	    return e.mousedown(Equipory.ecoords[slot(c)].add(1,1), button);
	}
	return false;
    }
    
    @Override
    public void draw(GOut g) {
	super.draw(g);
	Coord bgc = new Coord();
	for(bgc.y = 0; bgc.y < sz.y; bgc.y += Window.bg.sz().y) {
	    for(bgc.x = 0; bgc.x < sz.x; bgc.x += Window.bg.sz().x)
		g.image(Window.bg, bgc, Coord.z, sz);
	}
	wbox.draw(g.reclipl(tlo, wbsz), Coord.z, wbsz);
	Equipory e = ui.gui.getEquipory();
	if(e != null){
	    int k = 0;
	    for (int slot : slots){
		Coord c0 = sqsz.mul(k,0);
		invsq(g, c0);
		WItem w = e.items.get(slot);
		if(w != null){
		    w.draw(g.reclip(c0.add(1,1), g.sz));
		}
		k++;
	    }
	}
    }
    
    @Override
    public Object tooltip(Coord c, Widget prev) {
	Equipory e = ui.gui.getEquipory();
	if(e != null){
	    WItem w = e.items.get(slot(c));
	    if(w != null){
		return w.tooltip(c, prev);
	    }
	}
	return super.tooltip(c, prev);
    }

    @Override
    public boolean drop(Coord cc, Coord ul) {
	Equipory e = ui.gui.getEquipory();
	if(e != null){
	    e.wdgmsg("drop", slot(cc));
	    return true;
	}
	return false;
    }

    @Override
    public boolean iteminteract(Coord cc, Coord ul) {
	Equipory e = ui.gui.getEquipory();
	if(e != null){
	    WItem w = e.items.get(slot(cc));
	    if(w != null){
		return w.iteminteract(cc, ul);
	    }
	}
	return false;
    }

}
