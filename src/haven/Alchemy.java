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

public class Alchemy extends ItemInfo.Tip {
    public static final Color[] colors = {
	new Color(255, 0, 0),
	new Color(0, 255, 0),
	new Color(0, 128, 255),
	new Color(255, 255, 0),
    };
    public static final String[] names = {"Salt", "Mercury", "Sulphur", "Lead"};
    public static final String[] tcolors;
    public final int[] a;
    
    static {
	String[] buf = new String[colors.length];
	for(int i = 0; i < colors.length; i++)
	    buf[i] = String.format("%d,%d,%d", colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue());
	tcolors = buf;
    }
    
    public enum Element {
	SALT, MERC, SULF, LEAD
    }

    public Alchemy(Owner owner, int salt, int merc, int sulf, int lead) {
	super(owner);
	this.a = new int[]{salt, merc, sulf, lead};
    }
    
    public BufferedImage longtip() {
	StringBuilder buf = new StringBuilder();
	for(int i = 0; i < 4; i++) {
	    if(i > 0)
		buf.append(", ");
	    buf.append(String.format("%s: $col[%s]{%.2f}", names[i], tcolors[i], a[i] / 100.0));
	}
	buf.append(String.format("\nMultiplier: %.2f", mult()));
	buf.append(String.format("   (%d%% pure)", (int)(purity() * 100)));
	return(RichText.render(buf.toString(), 0).img);
    }
    
    public BufferedImage smallmeter() {
	int max = 0;
	for(int i = 0; i < 4; i++)
	    max = Math.max(a[i], max);
	BufferedImage buf = TexI.mkbuf(new Coord(max / 200, 12));
	Graphics g = buf.getGraphics();
	for(int i = 0; i < 4; i++) {
	    g.setColor(colors[i]);
	    g.fillRect(0, i * 3, a[i] / 200, 3);
	}
	g.dispose();
	return(buf);
    }

    public double purity() {
	double p = 0.0;
	for(int e : a)
	    p += Math.pow(e / 10000.0, 2);
	return(((p - 0.25) * 4.0) / 3.0);
    }
    
    private double mult() {
	double p = 0.0;
	for(int e : a)
	    p += Math.pow(e / 10000.0, 2);
	return 12*p - 2;
    }
    
    public String toString() {
	return(String.format("%d-%d-%d-%d", a[0], a[1], a[2], a[3]));
    }
}
