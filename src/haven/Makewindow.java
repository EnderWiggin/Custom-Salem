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

import haven.Glob.Pagina;

import java.awt.Color;
import java.awt.Font;
import java.util.LinkedList;

public class Makewindow extends Widget {
    Widget obtn, cbtn, bbtn;
    Spec[] inputs = {};
    Spec[] outputs = {};
    static LinkedList<String> list = new LinkedList<String>();
    static Coord boff = new Coord(7, 9);
    final int xoff = 40, yoff = 60;
    public static final Text.Foundry nmf = new Text.Foundry(new Font("Serif", Font.PLAIN, 20));

    static {
	Widget.addtype("make", new WidgetFactory() {
	    public Widget create(Coord c, Widget parent, Object[] args) {
		return(new Makewindow(c, parent, (String)args[0]));
	    }
	});
    }

    public static class Spec {
	public Indir<Resource> res;
	public Tex num;

	public Spec(Indir<Resource> res, int num) {
	    this.res = res;
	    if(num >= 0)
		this.num = new TexI(Utils.outline2(Text.render(Integer.toString(num), Color.WHITE).img, Utils.contrast(Color.WHITE)));
	    else
		this.num = null;
	}
    }

    public Makewindow(Coord c, Widget parent, String rcpnm) {
	super(c, Coord.z, parent);
	Label nm = new Label(new Coord(0, 0), this, rcpnm, nmf);
	nm.c = new Coord(sz.x - nm.sz.x, 0);
	new Label(new Coord(0, 20), this, "Input:");
	new Label(new Coord(0, 80), this, "Result:");
	obtn = new Button(new Coord(290, 93), 60, this, "Craft");
	cbtn = new Button(new Coord(360, 93), 60, this, "Craft All");
	if(list.size() > 0){
	    bbtn = new Button(new Coord(220, 93), 60, this, list.peek());
	    bbtn.pack();
	    bbtn.c.x = 280 - bbtn.sz.x;
	}
	pack();
    }

    public void uimsg(String msg, Object... args) {
	if(msg == "inpop") {
	    Spec[] inputs = new Spec[args.length / 2];
	    for(int i = 0, a = 0; a < args.length; i++, a += 2)
		inputs[i] = new Spec(ui.sess.getres((Integer)args[a]), (Integer)args[a + 1]);
	    this.inputs = inputs;
	} else if(msg == "opop") {
	    Spec[] outputs = new Spec[args.length / 2];
	    for(int i = 0, a = 0; a < args.length; i++, a += 2)
		outputs[i] = new Spec(ui.sess.getres((Integer)args[a]), (Integer)args[a + 1]);
	    this.outputs = outputs;
	}
    }

    public void draw(GOut g) {
	Coord c = new Coord(xoff, 0);
	Inventory.invsq(g, c, new Coord(inputs.length, 1));
	for(int i = 0; i < inputs.length; i++) {
	    Coord ic = c.add(Inventory.sqoff(new Coord(i, 0)));
	    Spec s = inputs[i];
	    try {
		g.image(s.res.get().layer(Resource.imgc).tex(), ic);
	    } catch(Loading e) {
	    }
	    if(s.num != null)
		g.aimage(s.num, ic.add(Inventory.isqsz), 1.0, 1.0);
	}
	c = new Coord(xoff, yoff);
	Inventory.invsq(g, c, new Coord(outputs.length, 1));
	for(int i = 0; i < outputs.length; i++) {
	    Coord ic = c.add(Inventory.sqoff(new Coord(i, 0)));
	    Spec s = outputs[i];
	    try {
		g.image(s.res.get().layer(Resource.imgc).tex(), ic);
	    } catch(Loading e) {
	    }
	    if(s.num != null)
		g.aimage(s.num, ic.add(Inventory.isqsz), 1.0, 1.0);
	}
	super.draw(g);
    }

    private long hoverstart;
    private Resource lasttip;
    private Object stip, ltip;
    public Object tooltip(Coord mc, Widget prev) {
	return tooltip(mc, prev, true);
    }

    public Object tooltip(Coord mc, Widget prev, boolean full) {
	Resource tres = null;
	Coord c = new Coord(xoff, 0);
	find: {
	    for(int i = 0; i < inputs.length; i++) {
		if(mc.isect(c.add(Inventory.sqoff(new Coord(i, 0))), Inventory.isqsz)) {
		    tres = inputs[i].res.get();
		    break find;
		}
	    }
	    c = new Coord(xoff, yoff);
	    for(int i = 0; i < outputs.length; i++) {
		if(mc.isect(c.add(Inventory.sqoff(new Coord(i, 0))), Inventory.isqsz))
		    tres = outputs[i].res.get();
		break find;
	    }
	}
	if(!full){return null;}
	if(tres == null)
	    return(null);
	if(lasttip != tres) {
	    lasttip = tres;
	    stip = ltip = null;
	}
	long now = System.currentTimeMillis();
	boolean sh = true;
	if(prev != this)
	    hoverstart = now;
	else if(now - hoverstart > 1000)
	    sh = false;
	if(sh) {
	    if(stip == null)
		stip = Text.render(tres.layer(Resource.tooltip).t);
	    return(stip);
	} else {
	    if(ltip == null) {
		String t = tres.layer(Resource.tooltip).t;
		Resource.Pagina p = tres.layer(Resource.pagina);
		if(p != null)
		    t += "\n\n" + tres.layer(Resource.pagina).text;
		ltip = RichText.render(t, 300);
	    }
	    return(ltip);
	}
    }

    @Override
    public boolean mousedown(Coord c, int button) {
	Object tt = tooltip(c, null, false);
	if (tt != null || tt instanceof String){
	    Pagina p = ui.mnu.paginafor((String)tt);
	    if(p != null){
		store();
		ui.mnu.use(p);
		return true;
	    }
	}
	return super.mousedown(c, button);
    }

    private void store() {
	try {
	    list.push(outputs[0].res.get().layer(Resource.tooltip).t);
	} catch (Exception e){e.printStackTrace();}
    }

    private void restore(){
	try{
	    String name = list.pop();
	    Pagina p = ui.mnu.paginafor(name);
	    ui.mnu.use(p);
	} catch (Exception e){e.printStackTrace();}
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == obtn) {
	    if(msg == "activate")
		wdgmsg("make", 0);
	    return;
	}
	if(sender == cbtn) {
	    if(msg == "activate")
		wdgmsg("make", 1);
	    return;
	}
	if(sender == bbtn) {
	    restore();
	    return;
	}
	super.wdgmsg(sender, msg, args);
    }

    public boolean globtype(char ch, java.awt.event.KeyEvent ev) {
	if(ch == '\n') {
	    wdgmsg("make", ui.modctrl?1:0);
	    return(true);
	}
	return(super.globtype(ch, ev));
    }

    public static class MakePrep extends ItemInfo implements GItem.ColorInfo {
	private final static Color olcol = new Color(0, 255, 0, 64);
	public MakePrep(Owner owner) {
	    super(owner);
	}

	public Color olcol() {
	    return(olcol);
	}
    }
}
