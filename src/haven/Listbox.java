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
import static haven.Window.fbox;

public abstract class Listbox<T> extends Widget {
    public final int h, itemh;
    public final Scrollbar sb;
    public final Color sell = new Color(52, 35, 36);
    public final Color selr = new Color(178, 93, 91);
    public T sel;

    public Listbox(Coord c, Widget parent, int w, int h, int itemh) {
	super(c, new Coord(w, h * itemh).add(fbox.bisz()), parent);
	this.h = h;
	this.itemh = itemh;
	this.sb = new Scrollbar(new Coord(sz.x - fbox.br.sz().x, fbox.bt.sz().y), sz.y - fbox.bt.sz().y - fbox.bb.sz().y, this, 0, 0);
    }

    protected abstract T listitem(int i);
    protected abstract int listitems();
    protected abstract void drawitem(GOut g, T item);

    protected void drawsel(GOut g) {
	g.chcolor(255, 255, 0, 128);
	g.poly2(Coord.z, sell,
		new Coord(0, g.sz.y), sell,
		g.sz, selr,
		new Coord(g.sz.x, 0), selr);
	g.chcolor();
    }

    public void draw(GOut g) {
	sb.max = listitems() - h;
	g.chcolor(Color.BLACK);
	g.frect(Coord.z, sz);
	g.chcolor();
	fbox.draw(g, Coord.z, sz);
	Coord off = fbox.btloff();
	int n = listitems();
	for(int i = 0; i < h; i++) {
	    int idx = i + sb.val;
	    if(idx >= n)
		break;
	    T item = listitem(idx);
	    GOut ig = g.reclip(off.add(0, i * itemh), new Coord(sz.x, itemh).sub(fbox.bl.sz().x + fbox.br.sz().x + sb.sz.x, 0));
	    if(item == sel)
		drawsel(ig);
	    drawitem(ig, item);
	}
	super.draw(g);
    }

    public boolean mousewheel(Coord c, int amount) {
	sb.ch(amount);
	return(true);
    }

    public void change(T item) {
	this.sel = item;
    }

    protected void itemclick(T item, int button) {
	if(button == 1)
	    change(item);
    }

    public T itemat(Coord c) {
	int idx = (c.y / itemh) + sb.val;
	if(idx >= listitems())
	    return(null);
	return(listitem(idx));
    }

    public boolean mousedown(Coord c, int button) {
	if(super.mousedown(c, button))
	    return(true);
	T item = itemat(c);
	if((item == null) && (button == 1))
	    change(null);
	else if(item != null)
	    itemclick(item, button);
	return(true);
    }
}
