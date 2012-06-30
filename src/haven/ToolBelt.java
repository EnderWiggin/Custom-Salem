package haven;

import java.awt.event.KeyEvent;

public class ToolBelt {
    GameUI ui;
    ToolBeltWdg wdg;
    public ToolBelt(GameUI gameUI) {
	ui = gameUI;
	wdg = new ToolBeltWdg(Coord.z, new Coord(100,32), ui);
    }

    public int draw(GOut g, int by) {
	return 0;
    }

    public boolean click(Coord c, int button) {
	return false;//wdg.mousedown(c, button);
    }

    public boolean key(KeyEvent ev) {
	return false;
    }

    public boolean item(Coord c) {
	return false;
    }

    public boolean thing(Coord c, Object thing) {
	return false;
    }
    
}
