package haven;

import static haven.Inventory.invsq;
import static haven.WItem.missing;

public class ToolBeltWdg extends Window {
    private static final Coord invsz = invsq.sz();
    GameUI gui;
    public ToolBeltWdg(Coord c, Coord sz, GameUI parent) {
	super(c, sz, parent, null);
	gui = parent;
	mrgn = new Coord(0,0);
	cbtn.visible = false;
	justclose = true;
	resize(beltc(11).add(invsz));
    }

    @Override
    public void cdraw(GOut g) {
	super.cdraw(g);
	for(int i = 0; i < 12; i++) {
		int slot = i + (0 * 12);
		Coord c = beltc(i);
		g.image(invsq, beltc(i));
		Tex tex = null;
		try {
		    if(gui.belt[slot] != null)
			tex = gui.belt[slot].get().layer(Resource.imgc).tex();
		    g.image(tex, c.add(1, 1));
		} catch(Loading e) {
		    missing.loadwait();
		    tex = missing.layer(Resource.imgc).tex();
		    g.image(tex, c, invsz);
		}
		g.chcolor(200, 220, 200, 255);
		FastText.aprintf(g, c.add(invsz), 1, 1, "F%d", i + 1);
		g.chcolor();
	    }
    }
    
    
    
    private Coord beltc(int i) {
	return(new Coord(((invsz.x + 2) * i)
		+ (10 * (i / 4)),
		0));
    }
    
    public int beltslot(Coord c){
	c = c.sub(ac);
	for(int i = 0; i<12; i++){
	    if(c.isect(beltc(i), invsz)){
		return i;
	    }
	}
	return -1;
    }
    
    @Override
    public Object tooltip(Coord c, boolean again) {
	int slot = beltslot(c);
	if(slot  != -1){
	    slot += (0 * 12);
	    try {
		if(gui.belt[slot] != null){
		    Resource res = gui.belt[slot].get();
		    Resource.AButton ad = res.layer(Resource.action);
		    if(ad != null){
			return ad.name;
		    }
		}
	    }catch(Loading e){return "...";}
	}
	return null;
    }

}
