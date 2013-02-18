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

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

public class CPButton extends Button {
    private static final Resource csfx = Resource.load("sfx/confirm");
    public Object cptip = null;
    public boolean s = false;
    private long fst;

    static {
	Widget.addtype("cpbtn", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new CPButton(c, (Integer)args[0], parent, (String)args[1]));
		}
	    });
    }

    public CPButton(Coord c, int w, Widget parent, String text) {
	super(c, w, parent, text);
    }

    public void cpclick() {
	wdgmsg("activate");
    }

    private TexI glowmask = null;
    public void draw(GOut g) {
	super.draw(g);
	if(s) {
	    if(glowmask == null)
		glowmask = new TexI(PUtils.glowmask(PUtils.glowmask(draw().getRaster()), 10, new Color(255, 64, 0)));
	    double ph = (System.currentTimeMillis() - fst) / 1000.0;
	    g.chcolor(255, 255, 255, (int)(128 * ((Math.cos(ph * Math.PI * 2) * -0.5) + 0.5)));
	    GOut g2 = g.reclipl(new Coord(-10, -10), g.sz.add(20, 20));
	    g2.image(glowmask, Coord.z);
	}
    }

    public void click() {
	if(!s) {
	    fst = System.currentTimeMillis();
	    s = true;
	    change(text.text, new Color(255, 64, 0));
	    redraw();
	    Audio.play(csfx);
	} else {
	    if((System.currentTimeMillis() - fst) > 1000) {
		cpclick();
		s = false;
		change(text.text, defcol);
		redraw();
	    }
	}
    }

    public void mousemove(Coord c) {
	super.mousemove(c);
	if(s && !c.isect(Coord.z, sz)) {
	    s = false;
	    change(text.text, defcol);
	    redraw();
	}
    }

    public Object tooltip(Coord c, Widget prev) {
	if(s && (cptip != null))
	    return(cptip);
	return(super.tooltip(c, prev));
    }
}
