package haven;

import static haven.Inventory.invsq;
import static haven.Inventory.invsz;
import static haven.Inventory.sqoff;
import static haven.Inventory.sqroff;

public class EquipProxyWdg extends Widget implements DTarget {
    private Coord slotsz;
    private int slots[];
    public EquipProxyWdg(Coord c, int[] slots, Widget parent) {
	super(c, Coord.z, parent);
	setSlots(slots);
    }

    public void setSlots(int[] slots){
	this.slots = slots;
	slotsz = new Coord(slots.length, 1);
	sz = invsz(slotsz);
    }

    private int slot(Coord c){
	int slot = sqroff(c).x;
	if(slot < 0){slot = 0;}
	if(slot >= slots.length){slot = slots.length -1;}
	return slots[slot];
    }
    
    @Override
    public boolean mousedown(Coord c, int button) {
	Equipory e = ui.gui.getEquipory();
	if(e != null){
	    WItem w = e.slots[slot(c)];
		if(w != null){
		    w.mousedown(Coord.z, button);
		    return true;
		}
	}
	return false;
    }
    
    @Override
    public void draw(GOut g) {
	super.draw(g);
	Equipory e = ui.gui.getEquipory();
	if(e != null){
	    int k = 0;
	    invsq(g, Coord.z, slotsz);
	    Coord c0 = new Coord(0, 0);
	    for (int slot : slots){
		c0.x = k;
		WItem w = e.slots[slot];
		if(w != null){
		    w.draw(g.reclipl(sqoff(c0), g.sz));
		} else {
		    Tex ebg = Equipory.ebgs[slot];
		    if(ebg != null){
			g.image(ebg,sqoff(c0));
		    }
		}
		k++;
	    }
	}
    }
    
    @Override
    public Object tooltip(Coord c, Widget prev) {
	Equipory e = ui.gui.getEquipory();
	if(e != null){
	    WItem w = e.slots[slot(c)];
	    if(w != null){
		return w.tooltip(c, (prev == this)?w:prev);
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
	    WItem w = e.slots[slot(cc)];
	    if(w != null){
		return w.iteminteract(cc, ul);
	    }
	}
	return false;
    }

}
