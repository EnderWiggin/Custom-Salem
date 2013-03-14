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
import java.util.*;

public class FramedAva extends Widget {
    public static final IBox box = Window.swbox;
    public Color color = new Color(133, 92, 62);
    public final Avaview view;

    @RName("av")
    public static class $_ implements Factory {
	public Widget create(Coord c, Widget parent, Object[] args) {
	    return(new Avaview(c, Avaview.dasz, parent, (Integer)args[0], "avacam"));
	}
    }

    public FramedAva(Coord c, Coord sz, Widget parent, long avagob, String camnm) {
	super(c, sz, parent);
	this.view = new Avaview(box.btloff(), sz.sub(box.bisz()), this, avagob, camnm);
    }

    public void uimsg(String msg, Object... args) {
	if(msg == "upd") {
	    view.avagob = (long)(Integer)args[0];
	    return;
	}
	super.uimsg(msg, args);
    }

    public void draw(GOut g) {
	super.draw(g);
	g.chcolor(color);
	box.draw(g, Coord.z, sz);
    }

    public boolean mousedown(Coord c, int button) {
	wdgmsg("click", button);
	return(true);
    }
}
