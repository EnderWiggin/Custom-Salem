/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.awt.event.KeyEvent;

public class RootWidget extends ConsoleHost {
    public static Resource defcurs = Resource.load("gfx/hud/curs/arw");
    Logout logout = null;
    Profile gprof;
    boolean afk = false;

    public RootWidget(UI ui, Coord sz) {
	super(ui, new Coord(0, 0), sz);
	setfocusctl(true);
	cursor = defcurs;
    }

    public boolean globtype(char key, KeyEvent ev) {
	int code = ev.getKeyCode();
	boolean ctrl = ev.isControlDown();
	boolean alt = ev.isAltDown();
	boolean hasGUI = ui.gui != null;
	if(!super.globtype(key, ev)) {
	    if(key == 0){return false;}
	    if(Config.profile && (key == '`')) {
		new Profwnd(new Coord(100, 100), this, gprof, "Glob prof");
	    } else if(Config.profile && (key == '~')) {
		GameUI gi = ui.gui;
		if((gi != null) && (gi.map != null))
		    new Profwnd(new Coord(100, 100), this, gi.map.prof, "MV prof");
	    } else if(key == ':') {
		entercmd();
	    }else if((code == KeyEvent.VK_L || code == KeyEvent.VK_F) && ctrl){
		FlatnessTool.instance(ui).toggle();
	    }else if(code == KeyEvent.VK_D && ctrl){
		DarknessWnd.toggle();
	    }else if(code == KeyEvent.VK_C && alt && hasGUI){
		ui.gui.toggleCraftWnd();
	    }else if(code == KeyEvent.VK_F && alt && hasGUI){
		ui.gui.toggleFilterWnd();
	    }else if(code == KeyEvent.VK_Z && ctrl){
		Config.center = !Config.center;
		ui.message(String.format("Tile centering in turned %s", Config.center?"ON":"OFF"), GameUI.MsgType.INFO);
	    } else if(key != 0) {
		wdgmsg("gk", (int)key);
	    }
	}
	return(true);
    }

    @Override
    public boolean keyup(KeyEvent ev) {
	if(ev.getKeyCode() == KeyEvent.VK_PRINTSCREEN){
	    Screenshooter.take(ui.gui, Config.screenurl);
	    return true;
	}
	return super.keyup(ev);
    }

    public void draw(GOut g) {
	super.draw(g);
	drawcmd(g, new Coord(20, sz.y - 20));
    }

    public void error(String msg) {
    }
}
