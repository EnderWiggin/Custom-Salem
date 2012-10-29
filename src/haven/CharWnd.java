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

public class CharWnd extends Window {
    public static final Map<String, String> attrnm;
    public static final List<String> attrorder;
    public final Map<String, Attr> attrs = new HashMap<String, Attr>();
    public final SkillList csk, nsk;
    public final Widget attrwdgs;
    public int cmod;
    private final SkillInfo ski;
    private final Label cmodl;
    
    static {
	Widget.addtype("chr", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new CharWnd(c, parent));
		}
	    });
	{
	    final List<String> ao = new ArrayList<String>();
	    Map<String, String> an = new HashMap<String, String>() {
		public String put(String k, String v) {
		    ao.add(k);
		    return(super.put(k, v));
		}
	    };
	    an.put("arts", "Arts & Crafts");
	    an.put("cloak", "Cloak & Dagger");
	    an.put("faith", "Faith & Wisdom");
	    an.put("wild", "Frontier & Wilderness");
	    an.put("nail", "Hammer & Nail");
	    an.put("hung", "Hunting & Gathering");
	    an.put("law", "Law & Lore");
	    an.put("mine", "Mines & Mountains");
	    an.put("pots", "Pots & Pans");
	    an.put("fire", "Sparks & Embers");
	    an.put("stock", "Stocks & Cultivars");
	    an.put("spice", "Sugar & Spice");
	    an.put("thread", "Thread & Needle");
	    an.put("natp", "Natural Philosophy");
	    an.put("perp", "Perennial Philosophy");
	    attrnm = Collections.unmodifiableMap(an);
	    attrorder = Collections.unmodifiableList(ao);
	}
    }
    
    public class Skill {
	public final String nm;
	public final Indir<Resource> res;
	public final String[] costa;
	public final int[] costv;
	
	private Skill(String nm, Indir<Resource> res, String[] costa, int[] costv) {
	    this.nm = nm;
	    this.res = res;
	    this.costa = costa;
	    this.costv = costv;
	}
	
	private Skill(String nm, Indir<Resource> res) {
	    this(nm, res, new String[0], new int[0]);
	}
	
	public int afforded() {
	    int ret = 0;
	    for(int i = 0; i < costa.length; i++) {
		if(attrs.get(costa[i]).attr.comp * 100 < costv[i])
		    return(3);
		if(attrs.get(costa[i]).sexp < costv[i])
		    ret = Math.max(ret, 2);
		else if(attrs.get(costa[i]).hexp < costv[i])
		    ret = Math.max(ret, 1);
	    }
	    return(ret);
	}
    }
    
    private static class SkillInfo extends RichTextBox {
	final static RichText.Foundry skbodfnd;
	Skill cur = null;
	boolean d = false;
	
	static {
	    skbodfnd = new RichText.Foundry(java.awt.font.TextAttribute.FAMILY, "SansSerif", java.awt.font.TextAttribute.SIZE, 9);
	    skbodfnd.aa = true;
	}
	
	public SkillInfo(Coord c, Coord sz, Widget parent) {
	    super(c, sz, parent, "", skbodfnd);
	}
	
	public void tick(double dt) {
	    if(d) {
		try {
		    StringBuilder text = new StringBuilder();
		    text.append("$img[" + cur.res.get().name + "]\n\n");
		    text.append("$font[serif,16]{" + cur.res.get().layer(Resource.action).name + "}\n\n");
		    int[] o = sortattrs(cur.costa);
		    if(cur.costa.length > 0) {
			for(int i = 0; i < o.length; i++) {
			    int u = o[i];
			    text.append(attrnm.get(cur.costa[u]) + ": " + cur.costv[u] + "\n");
			}
			text.append("\n");
		    }
		    text.append(cur.res.get().layer(Resource.pagina).text);
		    settext(text.toString());
		    d = false;
		} catch(Loading e) {}
	    }
	}
	
	public void setsk(Skill sk) {
	    d = (sk != null);
	    cur = sk;
	    settext("");
	}
    }
    
    public static int[] sortattrs(final String[] attrs) {
	Integer[] o = new Integer[attrs.length];
	for(int i = 0; i < o.length; i++)
	    o[i] = new Integer(i);
	Arrays.sort(o, new Comparator<Integer>() {
		public int compare(Integer a, Integer b) {
		    return(attrorder.indexOf(attrs[a.intValue()]) - attrorder.indexOf(attrs[b.intValue()]));
		}
	    });
	int[] r = new int[o.length];
	for(int i = 0; i < o.length; i++)
	    r[i] = o[i];
	return(r);
    }

    public static class SkillList extends Listbox<Skill> {
	public Skill[] skills = new Skill[0];
	private boolean loading = false;
	private final Comparator<Skill> skcomp = new Comparator<Skill>() {
	    public int compare(Skill a, Skill b) {
		String an, bn;
		try {
		    an = a.res.get().layer(Resource.action).name;
		} catch(Loading e) {
		    loading = true;
		    an = "\uffff";
		}
		try {
		    bn = b.res.get().layer(Resource.action).name;
		} catch(Loading e) {
		    loading = true;
		    bn = "\uffff";
		}
		return(an.compareTo(bn));
	    }
	};
	
	public SkillList(Coord c, int w, int h, Widget parent) {
	    super(c, parent, w, h, 20);
	}
	
	public void tick(double dt) {
	    if(loading) {
		loading = false;
		Arrays.sort(skills, skcomp);
	    }
	}

	protected Skill listitem(int idx) {return(skills[idx]);}
	protected int listitems() {return(skills.length);}

	protected void drawitem(GOut g, Skill sk) {
	    try {
		g.image(sk.res.get().layer(Resource.imgc).tex(), Coord.z, new Coord(20, 20));
		g.atext(sk.res.get().layer(Resource.action).name, new Coord(25, 10), 0, 0.5);
	    } catch(Loading e) {
		WItem.missing.loadwait();
		g.image(WItem.missing.layer(Resource.imgc).tex(), Coord.z, new Coord(20, 20));
		g.atext("...", new Coord(25, 10), 0, 0.5);
	    }
	}
	
	public void pop(Collection<Skill> nsk) {
	    Skill[] skills = nsk.toArray(new Skill[0]);
	    sb.val = 0;
	    sb.max = skills.length - h;
	    sel = null;
	    this.skills = skills;
	    loading = true;
	}
	
	public void change(Skill sk) {
	    sel = sk;
	}
    }
    
    public class Attr extends Widget {
	public final Coord
	    imgc = new Coord(0, 1),
	    nmc = new Coord(17, 1),
	    vc = new Coord(137, 1),
	    expc = new Coord(162, 0),
	    expsz = new Coord(sz.x - expc.x, sz.y);
	public final String nm;
	public final Resource res;
	public final Glob.CAttr attr;
	public int sexp, hexp;
	public boolean av = false;
	private Text rnm, rv, rexp;
	private int cv;
	
	private Attr(String attr, Coord c, Widget parent) {
	    super(c, new Coord(237, 15), parent);
	    this.nm = attr;
	    this.res = Resource.load("gfx/hud/skills/" + nm);
	    this.res.loadwait();
	    Resource.Pagina pag = this.res.layer(Resource.pagina);
	    if(pag != null)
		this.tooltip = RichText.render(pag.text, 300);
	    this.attr = ui.sess.glob.cattr.get(nm);
	    this.rnm = Text.render(attrnm.get(attr));
	}
	
	public void drawmeter(GOut g, Coord c, Coord sz) {
	    g.chcolor(0, 0, 0, 255);
	    g.frect(c, sz);
	    g.chcolor(64, 64, 64, 255);
	    g.frect(c.add(1, 1), new Coord(((sz.x - 2) * sexp) / (attr.comp * 100), sz.y - 2));
	    if(av)
		g.chcolor(0, (a == 1)?255:128, 0, 255);
	    else
		g.chcolor(0, 0, 128, 255);
	    g.frect(c.add(1, 1), new Coord(((sz.x - 2) * hexp) / (attr.comp * 100), sz.y - 2));
	    if(ui.lasttip instanceof WItem.ItemTip) {
		try {
		    GItem item = ((WItem.ItemTip)ui.lasttip).item();
		    Inspiration insp = ItemInfo.find(Inspiration.class, item.info());
		    if(insp != null) {
			for(int i = 0; i < insp.attrs.length; i++) {
			    if(insp.attrs[i].equals(nm)) {
				int w = Math.min(((sz.x - 2) * insp.exp[i]) / (attr.comp * 100),
						 sz.x - 2);
				if(insp.exp[i] > (attr.comp * 100))
				    g.chcolor(255, 255, 0, 255);
				else
				    g.chcolor(255, 192, 0, 255);
				g.frect(c.add(1, 1), new Coord(w, (sz.y / 2)));
				break;
			    }
			}
		    }
		} catch(Loading e) {}
	    }
	    if(nsk.sel != null) {
		Skill sk = nsk.sel;
		for(int i = 0; i < sk.costa.length; i++) {
		    if(sk.costa[i].equals(nm)) {
			int w = Math.min(((sz.x - 2) * sk.costv[i]) / (attr.comp * 100),
					 sz.x - 2);
			if(sk.costv[i] > (attr.comp * 100))
			    g.chcolor(255, 0, 0, 255);
			else
			    g.chcolor(128, 0, 0, 255);
			g.frect(c.add(1, sz.y / 2), new Coord(w, (sz.y / 2)));
			break;
		    }
		}
	    }
	    g.chcolor();
	    if(rexp == null)
		rexp = Text.render(String.format("%d/%d", sexp, attr.comp * 100));
	    g.aimage(rexp.tex(), c.add(sz.x / 2, 1), 0.5, 0);
	}

	public void draw(GOut g) {
	    g.image(res.layer(Resource.imgc).tex(), imgc);
	    g.image(rnm.tex(), nmc);
	    if(attr.comp != cv)
		rv = null;
	    if(rv == null)
		rv = Text.render(String.format("%d", cv = attr.comp));
	    g.image(rv.tex(), vc);
	    drawmeter(g, expc, expsz);
	}
	
	private int a = 0;
	public boolean mousedown(Coord c, int btn) {
	    if((btn == 1) && c.isect(expc, expsz)) {
		if(av) {
		    a = 1;
		    ui.grabmouse(this);
		}
		return(true);
	    }
	    return(false);
	}
	
	public boolean mouseup(Coord c, int btn) {
	    if((btn == 1) && (a == 1)) {
		a = 0;
		ui.grabmouse(null);
		if(c.isect(expc, expsz))
		    buy();
		return(true);
	    }
	    return(false);
	}
	
	public void buy() {
	    CharWnd.this.wdgmsg("sattr", nm);
	}
    }

    public CharWnd(Coord c, Widget parent) {
	super(c, new Coord(620, 360), parent, "Character");
	new Label(new Coord(0, 0), this, "Proficiencies:");
	attrwdgs = new Widget(new Coord(0, 30), Coord.z, this);
	int y = 0;
	for(String nm : attrorder) {
	    this.attrs.put(nm, new Attr(nm, new Coord(0, y), attrwdgs));
	    y += 20;
	}
	attrwdgs.pack();
	y += attrwdgs.c.y + attrwdgs.c.y + 15;
	cmodl = new Label(new Coord(0, y + 5), this, "Learning Ability: ");
	new Button(new Coord(190, y), 50, this, "Reset") {
	    public void click() {
		CharWnd.this.wdgmsg("lreset");
	    }
	};
	new Label(new Coord(250, 0), this, "Skills:");
	new Label(new Coord(250, 30), this, "Current:");
	this.csk = new SkillList(new Coord(250, 45), 170, 6, this) {
		public void change(Skill sk) {
		    Skill p = sel;
		    super.change(sk);
		    if(sk != null)
			nsk.change(null);
		    if((sk != null) || (p != null))
			ski.setsk(sk);
		}
	    };
	new Label(new Coord(250, 180), this, "Available:");
	this.nsk = new SkillList(new Coord(250, 195), 170, 6, this) {
		protected void drawitem(GOut g, Skill sk) {
		    int astate = sk.afforded();
		    if(astate == 3)
			g.chcolor(255, 128, 128, 255);
		    else if(astate == 2)
			g.chcolor(255, 192, 128, 255);
		    else if(astate == 1)
			g.chcolor(255, 255, 128, 255);
		    super.drawitem(g, sk);
		    g.chcolor();
		}
		
		public void change(Skill sk) {
		    Skill p = sel;
		    super.change(sk);
		    if(sk != null)
			csk.change(null);
		    if((sk != null) || (p != null))
			ski.setsk(sk);
		}
	    };
	new Button(new Coord(250, 340), 50, this, "Buy") {
	    public void click() {
		if(nsk.sel != null) {
		    CharWnd.this.wdgmsg("buy", nsk.sel.nm);
		}
	    }
	};
	this.ski = new SkillInfo(new Coord(430, 45), new Coord(190, 278), this);
    }
    
    private void decsklist(Collection<Skill> buf, Object[] args, int a) {
	while(a < args.length) {
	    String nm = (String)args[a++];
	    Indir<Resource> res = ui.sess.getres((Integer)args[a++]);
	    int n;
	    for(n = 0; !((String)args[a + (n * 2)]).equals(""); n++);
	    String[] costa = new String[n];
	    int[] costv = new int[n];
	    for(int i = 0; i < n; i++) {
		costa[i] = (String)args[a + (i * 2)];
		costv[i] = (Integer)args[a + (i * 2) + 1];
	    }
	    a += (n * 2) + 1;
	    buf.add(new Skill(nm, res, costa, costv));
	}
    }
    
    private Collection<Skill> acccsk, accnsk;
    public void uimsg(String msg, Object... args) {
	if(msg == "exp") {
	    for(int i = 0; i < args.length; i += 4) {
		String nm = (String)args[i];
		int s = (Integer)args[i + 1];
		int h = (Integer)args[i + 2];
		boolean av = ((Integer)args[i + 3]) != 0;
		Attr a = attrs.get(nm);
		a.sexp = s;
		a.hexp = h;
		a.rexp = null;
		a.av = av;
	    }
	} else if(msg == "csk") {
	    /* One could argue that rmessages should have some
	     * built-in fragmentation scheme. */
	    boolean acc = ((Integer)args[0]) != 0;
	    Collection<Skill> buf;
	    if(acccsk != null) {
		buf = acccsk;
		acccsk = null;
	    } else {
		buf = new LinkedList<Skill>();
	    }
	    decsklist(buf, args, 1);
	    if(acc)
		acccsk = buf;
	    else
		csk.pop(buf);
	} else if(msg == "nsk") {
	    boolean acc = ((Integer)args[0]) != 0;
	    Collection<Skill> buf;
	    if(accnsk != null) {
		buf = accnsk;
		accnsk = null;
	    } else {
		buf = new LinkedList<Skill>();
	    }
	    decsklist(buf, args, 1);
	    if(acc)
		accnsk = buf;
	    else
		nsk.pop(buf);
	} else if(msg == "cmod") {
	    cmod = (Integer)args[0];
	    cmodl.settext(String.format("Learning ability: %d%%", cmod));
	}
    }
}
