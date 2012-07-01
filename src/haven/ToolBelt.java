package haven;

import java.awt.event.KeyEvent;

public class ToolBelt {
    GameUI ui;
    ToolBeltWdg wdg;
    public ToolBelt(GameUI gameUI) {
	ui = gameUI;
	wdg = new ToolBeltWdg(ui);
    }

    public int draw(GOut g, int by) {
	return 0;
    }
    
    private int beltslot(Coord c){
	return wdg.beltslot(c.sub(wdg.c));
    }
    
    public boolean click(Coord c, int button) {
//	int slot = beltslot(c);
//	if(slot != -1) {
//	    if(button == 1)
//		ui.wdgmsg("belt", slot, 1, ui.ui.modflags());
//	    if(button == 3)
//		ui.wdgmsg("setbelt", slot, 1);
//	    return(true);
//	}
	return false;
    }

    public boolean key(KeyEvent ev) {
	return false;//wdg.type(ev.getKeyChar(), ev);
    }

    public boolean item(Coord c) {
	return false;
    }

    public boolean thing(Coord c, Object thing) {
//	int slot = beltslot(c);
//	if(slot != -1) {
//	    if(thing instanceof Resource) {
//		Resource res = (Resource)thing;
//		if(res.layer(Resource.action) != null) {
//		    ui.wdgmsg("setbelt", slot, res.name);
//		    return(true);
//		}
//	    }
//	}
	return(false);
    }
    
}
