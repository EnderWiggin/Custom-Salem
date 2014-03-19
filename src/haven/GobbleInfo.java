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

import java.util.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class GobbleInfo extends ItemInfo.Tip {
    public final int[] l, h;
    public final int[] types;
    public final int ft;
    public final List<Event> evs;
    
    public static class Event {
	public final List<ItemInfo> info;
	public final double p;
	private BufferedImage rinf, rp;
	public Event(List<ItemInfo> info, double p) {this.info = info; this.p = p;}
    }

    public GobbleInfo(Owner owner, int[] l, int[] h, int[] types, int ft, List<Event> evs) {
	super(owner);
	this.l = l;
	this.h = h;
	this.types = types;
	this.ft = ft;
	for(Event ev : this.evs = evs) {
	    ev.rinf = ItemInfo.longtip(ev.info);
	    if(ev.p < 1)
		ev.rp = RichText.render(String.format("[%d%%]", (int)Math.round(ev.p * 100)), Color.LIGHT_GRAY).img;
	}
    }
    
    private static final Text.Line head = Text.render("When gobbled:");
    public BufferedImage longtip() {
	StringBuilder buf = new StringBuilder();
	buf.append(String.format("Points: $b{%s : %s : %s : %s}\n", point(0), point(1), point(2), point(3)));
	int min = (ft + 30) / 60;
	buf.append(String.format("Full and Fed Up for %02d:%02d\n", min / 60, min % 60));
	BufferedImage gi = RichText.render(buf.toString(), 0).img;
	Coord sz = PUtils.imgsz(gi);
	for(Event ev : evs) {
	    int w = ev.rinf.getWidth();
	    if(ev.rp != null)
		w += 5 + ev.rp.getWidth();
	    sz.x = Math.max(sz.x, w);
	    sz.y += ev.rinf.getHeight();
	}
	BufferedImage img = TexI.mkbuf(sz.add(10, head.sz().y + 2));
	Graphics g = img.getGraphics();
	int y = 0;
	g.drawImage(head.img, 0, y, null);
	y += head.sz().y + 2;
	g.drawImage(gi, 10, y, null);
	y += gi.getHeight();
	for(Event ev : evs) {
	    g.drawImage(ev.rinf, 10, y, null);
	    if(ev.rp != null)
		g.drawImage(ev.rp, 10 + ev.rinf.getWidth() + 5, y, null);
	    y += ev.rinf.getHeight();
	}
	g.dispose();
	return(img);
    }
    
    private String point(int i) {
	return String.format("$col[%s]{%s} - $col[%s]{%s}",
		FoodInfo.colors[i], Utils.fpformat(l[i], 3, 1), 
		FoodInfo.colors[i], Utils.fpformat(h[i], 3, 1));
    }
}
