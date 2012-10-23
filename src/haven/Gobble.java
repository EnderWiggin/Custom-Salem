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

import java.awt.Color;

public class Gobble extends Widget {
    static final Tex[] trigi = new Tex[4];
    static final Color softc = new Color(255, 255, 255, 64);
    static final int l = 32;
    static final Color[] cols = {
	new Color(255, 0, 0, 255),
	new Color(255, 255, 255, 255),
	new Color(255, 255, 0, 255),
	new Color(0, 64, 0, 255),
    };
    static final Text.Foundry vf = new Text.Foundry("Serif", 20);
    public int[] lev = new int[4];
    public Text var = vf.render("0");
    Tex ctrig = null;
    long trigt = 0;
    static {
	Text.Foundry f = new Text.Foundry("Serif", 30);
	f.aa = true;
	for(int i = 0; i < 4; i++)
	    trigi[i] = f.render(Integer.toString(i + 1), Color.YELLOW).tex();
    }
    
    public Gobble(Coord c, Widget parent) {
	super(c, Utils.imgsz(Tempers.bg), parent);
    }
    
    public void updt(int[] n) {
	this.lev = n;
    }
    
    public void trig(int a) {
	ctrig = trigi[a];
	trigt = System.currentTimeMillis();
    }
    
    public void updv(int v) {
	this.var = vf.render(Integer.toString(v));
    }
}
