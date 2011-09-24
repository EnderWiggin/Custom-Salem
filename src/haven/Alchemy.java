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
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Alchemy extends GItem.Tip {
    public static final Color[] colors = {
	new Color(255, 0, 0),
	new Color(0, 255, 0),
	new Color(0, 128, 255),
	new Color(255, 255, 0),
    };
    public final int[] a;
    
    public enum Element {
	SALT, MERC, SULF, LEAD
    }

    public Alchemy(GItem item, int salt, int merc, int sulf, int lead) {
	item.super();
	this.a = new int[]{salt, merc, sulf, lead};
    }
    
    public BufferedImage longtip() {
	return(Text.std.renderf("Salt: %.2f, Mercury: %.2f, Sulphur: %.2f, Lead: %.2f", a[0] / 100.0, a[1] / 100.0, a[2] / 100.0, a[3] / 100.0).img);
    }
    
    public BufferedImage smallmeter() {
	BufferedImage buf = TexI.mkbuf(new Coord(50, 12));
	Graphics g = buf.getGraphics();
	for(int i = 0; i < 4; i++) {
	    g.setColor(colors[i]);
	    g.fillRect(0, i * 3, a[i] / 200, 3);
	}
	g.dispose();
	return(buf);
    }
    
    public String toString() {
	return(String.format("%d-%d-%d-%d", a[0], a[1], a[2], a[3]));
    }
}
