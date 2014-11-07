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

import haven.Tabs.Tab;

import java.awt.Color;
import java.util.*;
import java.util.Map.Entry;
import java.awt.image.BufferedImage;

public class CharWnd extends Window {
    private static final Color selcol = new Color(0xEAEFB4);
    private static final Color defcol = new Color(0xB5B094);
    private static final Coord SZ_FULL = new Coord(640, 360);
    public static final Map<String, String> attrnm;
    public static final List<String> attrorder;
    public final Map<String, Attr> attrs = new HashMap<String, Attr>();
    public final SkillList csk, nsk;
    public final Widget attrwdgs;
    public int tmexp;
    public boolean skavail;
    private final SkillInfo ski;
    private final Label tmexpl;
    
    public static final Color GREEN = new Color(0xaaeeaa);
    public static final Color GRAY = new Color(0xbda3a3);
    
    public static final Color METER_BORDER = new Color(133, 92, 62, 255);
    public static final Color METER_BACK = new Color(28, 28, 28, 255);
    
    public static final Color REQ_ENOUGH = new Color(0x991616);
    public static final Color REQ_NOT_ENOUGH = new Color(0xFF3A3A);
    public static final Color FILL = new Color(0x006AA3);
    public static final Color FILL_ENOUGH = new Color(0x06A8FF);
    public static final Color FILL_FULL = new Color(0x4EA320);
    public static final Color FILL_PRESSED = new Color(0x6E91A3);
    public static final Color GAIN_FULL = new Color(0x6BA050);
    public static final Color GAIN_ENOUGH = new Color(0xA09740);
    public static final Color GAIN_SMALL = new Color(0xA36751);
    
    @RName("chr")
    public static class $_ implements Factory {
	public Widget create(Coord c, Widget parent, Object[] args) {
	    return(new CharWnd(c, parent));
	}
    }

    static {
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
	an.put("wild", "Flora & Fauna");
	an.put("nail", "Hammer & Nail");
	an.put("hung", "Hunting & Hideworking");
	an.put("law", "Law & Lore");
	an.put("mine", "Mines & Mountains");
	an.put("pots", "Herbs & Sprouts");
	an.put("fire", "Sparks & Embers");
	an.put("stock", "Stocks & Cultivars");
	an.put("spice", "Sugar & Spice");
	an.put("thread", "Thread & Needle");
	an.put("natp", "Natural Philosophy");
	an.put("perp", "Perennial Philosophy");
	attrnm = Collections.unmodifiableMap(an);
	attrorder = Collections.unmodifiableList(ao);
    }
    
    public static String attrbyname(String name){
	for(Entry<String, String> entry : attrnm.entrySet()){
	    if(entry.getValue().equals(name)){
		return entry.getKey();
	    }
	}
	return null;
    }
    
    public class Skill {
	public final String nm;
	public final Indir<Resource> res;
	public final String[] costa;
	public final int[] costv;
	private int listidx;
	
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
		if(attrs.get(costa[i]).attr.base * 100 < costv[i])
		    return(3);
		if(attrs.get(costa[i]).exp < costv[i])
		    ret = Math.max(ret, 2);
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
	    GItem.infoUpdated = System.currentTimeMillis();
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
    
    public Color[] attrcols(final String[] attrs){
	Color[] c = new Color[attrs.length];
	int i=0;
	for (String attr : attrs){
	    c[i] = Color.WHITE;
	    if(ski.cur != null){
		for(int j = 0; j<ski.cur.costa.length; j++){
		    String costa = ski.cur.costa[j];
		    int costv = ski.cur.costv[j];
		    if(costa.equals(attr)){
			if(this.attrs.get(costa).exp < costv){
			    c[i] = GREEN;
			} else {
			    c[i] = GRAY;
			}
			break;
		    }
		}
	    }
	    i++;
	}
	return c;
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
		for(int i = 0; i < skills.length; i++)
		    skills[i].listidx = i;
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
    
    private void checkexp() {
	skavail = false;
	for(Skill sk : nsk.skills) {
	    if(sk.afforded() == 0) {
		skavail = true;
		break;
	    }
	}
    }

    private static final BufferedImage[] pbtn = {
	Resource.loadimg("gfx/hud/skills/plusu"),
	Resource.loadimg("gfx/hud/skills/plusd"),
	Resource.loadimg("gfx/hud/skills/plush"),
	PUtils.monochromize(Resource.loadimg("gfx/hud/skills/plusu"), new Color(192, 192, 192)),
	PUtils.glowmask(PUtils.glowmask(Resource.loadimg("gfx/hud/skills/plusu").getRaster()), 4, new Color(32, 255, 32)),
    };
	
    public class Attr extends Widget implements Observer {
	public final Coord
	    imgc = new Coord(0, 1),
	    nmc = new Coord(17, 1),
	    vc = new Coord(137, 1),
	    expc = new Coord(162, 0),
	    expsz = new Coord(sz.x - expc.x - 20, sz.y),
	    btnc = new Coord(sz.x - 17, 0);
	public final String nm;
	public final Resource res;
	public final Glob.CAttr attr;
	public int exp, cap = 500;
	public boolean av = false;
	private Text rnm, rv, rexp;
	private int cv;
	private IButton pb;
	
	private Attr(String attr, Coord c, Widget parent) {
	    super(c, new Coord(257, 15), parent);
	    this.nm = attr;
	    this.res = Resource.load("gfx/hud/skills/" + nm);
	    this.res.loadwait();
	    Resource.Pagina pag = this.res.layer(Resource.pagina);
	    if(pag != null)
		this.tooltip = RichText.render(pag.text, 300);
	    this.attr = ui.sess.glob.cattr.get(nm);
	    this.rnm = Text.render(attrnm.get(attr));
	    this.attr.addObserver(this);
	    this.pb = new IButton(btnc, this, pbtn[0], pbtn[1], pbtn[2]) {
		    public void draw(GOut g) {
			if(av) {
			    super.draw(g);
			    g = g.reclipl(new Coord(-4, -4), g.sz.add(8, 8));
			    double ph = (System.currentTimeMillis() / 1000.0) - (Attr.this.c.y * 0.007);
			    g.chcolor(255, 255, 255, (int)(128 * ((Math.cos(ph * Math.PI * 2) * -0.5) + 0.5)));
			    g.image(pbtn[4], Coord.z);
			} else {
			    g.image(pbtn[3], Coord.z);
			}
		    }

		    public void click() {
			buy();
		    }
		};
	}
	
	public void drawmeter(GOut g, Coord c, Coord sz) {
	    g.chcolor(METER_BORDER);
	    g.frect(c, sz);
	    g.chcolor(METER_BACK);
	    g.frect(c.add(1, 1), sz.sub(2, 2));
	    if(ui.lasttip instanceof WItem.ItemTip) {
		try {
		    GItem item = ((WItem.ItemTip)ui.lasttip).item();
		    Inspiration insp = ItemInfo.find(Inspiration.class, item.info());
		    if(insp != null) {
			for(int i = 0; i < insp.attrs.length; i++) {
			    if(insp.attrs[i].equals(nm)) {
				int xp = insp.exp[i]+exp;
				int w = Math.min(((sz.x - 2) * xp) / cap, sz.x - 2);
				if(xp > cap){
				    g.chcolor(GAIN_ENOUGH);
				} else {
				    g.chcolor(GAIN_SMALL);
				}
				g.frect(c.add(1, 1), new Coord(w, (sz.y / 2)));
				break;
			    }
			}
		    }
		} catch(Loading e) {}
	    }
	    
	    if(av) {
		g.chcolor((a == 1)?FILL_PRESSED:FILL_FULL);
	    } else {
		g.chcolor(FILL);
	    }
	    g.frect(c.add(1, 1), new Coord(((sz.x - 2) * Math.min(exp, cap)) / cap, sz.y - 2));
	    
	    if(nsk.sel != null) {
		Skill sk = nsk.sel;
		for(int i = 0; i < sk.costa.length; i++) {
		    if(sk.costa[i].equals(nm)) {
			int cost = sk.costv[i];
			
			if(!av && exp >= cost){
			    g.chcolor(FILL_ENOUGH);
			    g.frect(c.add(1, 1), new Coord(((sz.x - 2) * Math.min(exp, cap)) / cap, sz.y - 2));
			}
			
			int w = Math.min(((sz.x - 2) * sk.costv[i]) / cap,
					 sz.x - 2);
			if(cost > (attr.base * 100))
			    g.chcolor(REQ_NOT_ENOUGH);
			else
			    g.chcolor(REQ_ENOUGH);

			g.frect(c.add(1, sz.y / 2), new Coord(w, (sz.y / 2)));
			break;
		    }
		}
	    }
	    g.chcolor();
	    if(rexp == null)
		rexp = Text.render(String.format("%d/%d", exp, cap));
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
	    super.draw(g);
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
	    return(super.mousedown(c, btn));
	}
	
	public boolean mouseup(Coord c, int btn) {
	    if((btn == 1) && (a == 1)) {
		a = 0;
		ui.grabmouse(null);
		if(c.isect(expc, expsz))
		    buy();
		return(true);
	    }
	    return(super.mouseup(c, btn));
	}
	
	public void buy() {
	    CharWnd.this.wdgmsg("sattr", nm);
	}

	@Override
	public void update(Observable o, Object arg) {
	    int delta = attr.comp - (Integer) arg;
	    if(delta == 0){return;}
	    rexp = null;
	    ui.message(String.format("Your '%s' profficiency %s to %d (%+d)",
		    attrnm.get(nm),
		    (delta>0?"increased":"decreased"),
		    attr.comp,
		    delta));
	}
    }

    public CharWnd(Coord c, Widget parent) {
	super(c, SZ_FULL, parent, "Character");
	new Label(new Coord(0, 0), this, "Proficiencies:");
	attrwdgs = new Widget(new Coord(0, 30), Coord.z, this);
	int y = 0;
	for(String nm : attrorder) {
	    this.attrs.put(nm, new Attr(nm, new Coord(0, y), attrwdgs));
	    y += 20;
	}
	attrwdgs.pack();
	y = attrwdgs.c.y + attrwdgs.sz.y + 15;
	tmexpl = new Label(new Coord(0, y + 5), this, "Inspiration: ") {
		Glob.CAttr ac = ui.sess.glob.cattr.get("scap"), ar = ui.sess.glob.cattr.get("srate");
		int lc = -1, lr = -1;
		Tex tt = null;
		public Object tooltip(Coord c, Widget prev) {
		    if((tt == null) || (ac.comp != lc) || (ar.comp != lr))
			tt = Text.renderf(Color.WHITE, "Cap: %,d, Rate: %.2f/s", lc = ac.comp, 3 * (lr = ar.comp) / 1000.0).tex();
		    return(tt);
		}
	    };
	new CPButton(new Coord(580, y), 40, this, "Reset") {
	    {tooltip = RichText.render("Discard all currently accumulated proficiency points, and reset learning ability to 100%.", 250).tex();}

	    public void cpclick() {
		CharWnd.this.wdgmsg("lreset");
	    }
	};
	new Label(new Coord(270, 0), this, "Skills:");
	Tabs body = new Tabs(new Coord(270, 10), new Coord(180, 335), this) {
	    public void changed(Tab from, Tab to) {
		from.btn.change(defcol);
		to.btn.change(selcol);
	    }
	};
	Tab tab;
	tab = body.new Tab(new Coord(335, 20), 60, "Learned");
	tab.btn.change(defcol);
	this.csk = new SkillList(new Coord(0, 30), 170, 14, tab) {
		public void change(Skill sk) {
		    Skill p = sel;
		    super.change(sk);
		    if(sk != null)
			nsk.change(null);
		    if((sk != null) || (p != null))
			ski.setsk(sk);
		}
	    };
	tab = body.new Tab(new Coord(270, 20), 60, "Available");
	tab.btn.change(selcol);
	body.showtab(tab);
	this.nsk = new SkillList(new Coord(0, 30), 170, 14, tab) {
		protected void drawitem(GOut g, Skill sk) {
		    int astate = sk.afforded();
		    if(astate == 3) {
			g.chcolor(255, 128, 128, 255);
		    } else if(astate == 2) {
			g.chcolor(255, 192, 128, 255);
		    } else if(astate == 1) {
			g.chcolor(255, 255, 128, 255);
		    } else if(astate == 0) {
			if(sk != sel) {
			    double ph = (System.currentTimeMillis() / 1000.0) - (sk.listidx * 0.15);
			    int c = (int)(128 * ((Math.cos(ph * Math.PI * 2) * -0.5) + 0.5)) + 127;
			    g.chcolor(c, 255, c, 255);
			}
		    }
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
	new Button(new Coord(270, 340), 50, this, "Buy") {
	    Tex glowmask = new TexI(PUtils.glowmask(PUtils.glowmask(draw().getRaster()), 4, new Color(32, 255, 32)));

	    public void click() {
		if(nsk.sel != null) {
		    CharWnd.this.wdgmsg("buy", nsk.sel.nm);
		}
	    }

	    public void draw(GOut g) {
		super.draw(g);
		if((nsk.sel != null) && (nsk.sel.afforded() == 0)) {
		    double ph = System.currentTimeMillis() / 1000.0;
		    g.chcolor(255, 255, 255, (int)(128 * ((Math.cos(ph * Math.PI * 2) * -0.5) + 0.5)));
		    GOut g2 = g.reclipl(new Coord(-4, -4), g.sz.add(8, 8));
		    g2.image(glowmask, Coord.z);
		}
	    }
	};
	this.ski = new SkillInfo(new Coord(450, 45), new Coord(190, 278), this);
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
		int c = (Integer)args[i + 1];
		int e = (Integer)args[i + 2];
		boolean av = ((Integer)args[i + 3]) != 0;
		Attr a = attrs.get(nm);
		a.cap = c;
		a.exp = e;
		a.rexp = null;
		a.av = av;
	    }
	    GItem.infoUpdated = System.currentTimeMillis();
	    checkexp();
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
	} else if (msg == "tmexp") {
	    tmexp = (Integer)args[0];
	    tmexpl.settext(String.format("Inspiration: %,d", tmexp));
	}
    }
}
