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

import java.awt.image.*;
import static haven.PUtils.*;

public class Progress extends SIWidget {
    public static final BufferedImage bg = Resource.loadimg("gfx/hud/prog/bg");
    public static final BufferedImage fg = Resource.loadimg("gfx/hud/prog/fg");
    public static final BufferedImage cap = Resource.loadimg("gfx/hud/prog/cap");
    public static final Coord fgc = new Coord(2, 2);
    public int prog;

    public Progress(Coord c, Widget parent) {
	super(c, imgsz(bg), parent);
    }

    public void draw(BufferedImage buf) {
	WritableRaster dst = buf.getRaster();
	blit(dst, bg.getRaster(), Coord.z);

	int w = (prog * fg.getWidth()) / 100;
	WritableRaster bar = copy(fg.getRaster());
	gayblit(bar, 3, new Coord(w - cap.getWidth(), 0), cap.getRaster(), 0, Coord.z);
	for(int y = 0; y < bar.getHeight(); y++) {
	    for(int x = w; x < bar.getWidth(); x++)
		bar.setSample(x, y, 3, 0);
	}

	alphablit(dst, bar, fgc);
    }

    public void ch(int prog) {
	this.prog = prog;
	redraw();
    }

    public void uimsg(String msg, Object... args) {
	if(msg == "p") {
	    ch((Integer)args[0]);
	} else {
	    super.uimsg(msg, args);
	}
    }
}
