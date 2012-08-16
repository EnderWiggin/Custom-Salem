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

import static haven.ItemInfo.find;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

public class WItem extends Widget implements DTarget {
    public static final Resource missing = Resource.load("gfx/invobjs/missing");
    public final GItem item;
    private Tex ltex = null;
    private Tex mask = null;
    private Resource cmask = null;
    private long ts = 0;
    
    public WItem(Coord c, Widget parent, GItem item) {
	super(c, Inventory.sqsz, parent);
	this.item = item;
    }
    
    public void drawmain(GOut g, Tex tex) {
	g.image(tex, Coord.z);
	if(tex != ltex) {
	    resize(tex.sz());
	    ltex = tex;
	}
    }

    public static BufferedImage rendershort(List<ItemInfo> info) {
	ItemInfo.Name nm = find(ItemInfo.Name.class, info);
	if(nm == null)
	    return(null);
	BufferedImage img = nm.str.img;
	Alchemy ch = find(Alchemy.class, info);
	if(ch != null)
	    img = ItemInfo.catimgsh(5, img, ch.smallmeter(),
				    Text.std.renderf("(%d%% pure)", (int)(ch.purity() * 100)).img);
	return(img);
    }

    public static BufferedImage shorttip(List<ItemInfo> info) {
	BufferedImage img = rendershort(info);
	ItemInfo.Contents cont = find(ItemInfo.Contents.class, info);
	if(cont != null) {
	    BufferedImage rc = rendershort(cont.sub);
	    if((img != null) && (rc != null))
		img = ItemInfo.catimgs(0, img, rc);
	    else if((img == null) && (rc != null))
		img = rc;
	}
	if(img == null)
	    return(null);
	return(img);
    }
    
    public static BufferedImage longtip(GItem item, List<ItemInfo> info) {
	BufferedImage img = ItemInfo.longtip(info);
	Resource.Pagina pg = item.res.get().layer(Resource.pagina);
	if(pg != null)
	    img = ItemInfo.catimgs(5, img, RichText.render(pg.text, 200).img);
	return(img);
    }
    
    public BufferedImage longtip(List<ItemInfo> info) {
	return(longtip(item, info));
    }
    
    public class ItemTip implements Indir<Tex> {
	private final TexI tex;
	
	public ItemTip(BufferedImage img) {
	    if(img == null)
		throw(new Loading());
	    tex = new TexI(img);
	}
	
	public GItem item() {
	    return(item);
	}
	
	public Tex get() {
	    return(tex);
	}
    }
    
    public class ShortTip extends ItemTip {
	public ShortTip(List<ItemInfo> info) {super(shorttip(info));}
    }
    
    public class LongTip extends ItemTip {
	public LongTip(List<ItemInfo> info) {super(longtip(info));}
    }

    private long hoverstart;
    private ItemTip shorttip = null, longtip = null;
    private List<ItemInfo> ttinfo = null;
    public Object tooltip(Coord c, Widget prev) {
	long now = System.currentTimeMillis();
	if(prev == this) {
	} else if(prev instanceof WItem) {
	    hoverstart = ((WItem)prev).hoverstart;
	} else {
	    hoverstart = now;
	}
	try {
	    List<ItemInfo> info = item.info();
	    if(info.size() < 1)
		return(null);
	    if(info != ttinfo) {
		shorttip = longtip = null;
		ttinfo = info;
	    }
	    if(now - hoverstart < 1000) {
		if(shorttip == null)
		    shorttip = new ShortTip(info);
		return(shorttip);
	    } else {
		if((longtip == null) || ts < GItem.infoUpdated){
		    ts = GItem.infoUpdated;
		    longtip = new LongTip(info);
		}
		return(longtip);
	    }
	} catch(Loading e) {
	    return("...");
	}
    }

    public abstract class AttrCache<T> {
	private List<ItemInfo> forinfo = null;
	private T save = null;
	
	public T get() {
	    try {
		List<ItemInfo> info = item.info();
		if(info != forinfo) {
		    save = find(info);
		    forinfo = info;
		}
	    } catch(Loading e) {
		return(null);
	    }
	    return(save);
	}
	
	protected abstract T find(List<ItemInfo> info);
    }
    
    public final AttrCache<Color> olcol = new AttrCache<Color>() {
	protected Color find(List<ItemInfo> info) {
	    GItem.ColorInfo cinf = ItemInfo.find(GItem.ColorInfo.class, info);
	    return((cinf == null)?null:cinf.olcol());
	}
    };
    
    public final AttrCache<Tex> itemnum = new AttrCache<Tex>() {
	protected Tex find(List<ItemInfo> info) {
	    GItem.NumberInfo ninf = ItemInfo.find(GItem.NumberInfo.class, info);
	    if(ninf == null) return(null);
	    return(new TexI(Utils.outline2(Text.render(Integer.toString(ninf.itemnum()), Color.WHITE).img, Utils.contrast(Color.WHITE))));
	}
    };
    
    public void draw(GOut g) {
	try {
	    Resource res = item.res.get();
	    Tex tex = res.layer(Resource.imgc).tex();
	    drawmain(g, tex);
	    if(item.num >= 0) {
		g.atext(Integer.toString(item.num), tex.sz(), 1, 1);
	    } else if(itemnum.get() != null) {
		g.aimage(itemnum.get(), tex.sz(), 1, 1);
	    }
	    if(item.meter > 0) {
		double a = ((double)item.meter) / 100.0;
		int r = (int) ((1-a)*255);
		int gr = (int) (a*255);
		Coord s2 = sz.add(2, 2);
		GOut g2 = g.reclipl(Coord.z, s2);
		g2.chcolor(r, gr, 0, 255);
		Coord bsz = new Coord(4, (int) (a*s2.y));
		g2.frect(s2.sub(bsz), bsz);
		g2.chcolor();
	    }
	    heuristics(g);
	    if(olcol.get() != null) {
		if(cmask != res) {
		    mask = null;
		    if(tex instanceof TexI)
			mask = ((TexI)tex).mkmask();
		    cmask = res;
		}
		if(mask != null) {
		    g.chcolor(olcol.get());
		    g.image(mask, Coord.z);
		    g.chcolor();
		}
	    }
	} catch(Loading e) {
	    missing.loadwait();
	    g.image(missing.layer(Resource.imgc).tex(), Coord.z, sz);
	}
    }
    
    long last_heur = 0;
    boolean heuristic = false, heur_checked = false;
    List<Integer> heur_meters;
    private void heuristics(GOut g) {
	if(!heur_checked){
	    heuristic = isheuristic();
	    heur_checked = true;
	}
	if(!heuristic) {return;}
	
	long now = System.currentTimeMillis();
	if(now - last_heur > 2500){
	    last_heur = now;
	    heur_meters = ItemInfo.getMeters(item.info());
	}
	if(heur_meters == null){return;}

	int k = 0;
	Coord s2 = sz.add(2, 2);
	for (Integer meter : heur_meters){
	    double a = ((double)meter) / 100.0;
	    int r = (int) ((1-a)*255);
	    int gr = (int) (a*255);
	    g.chcolor(r, gr, 0, 255);
	    Coord bsz = new Coord(4, (int) (a*s2.y));
	    g.frect(new Coord(bsz.x*k, s2.y - bsz.y), bsz);
	    g.chcolor();
	    k++;
	}
	
    }

    private boolean isheuristic() {
	if(ItemInfo.getMeters(item.info()).size() > 0){
	    return true;
	}
        return false;
    }

    public boolean mousedown(Coord c, int btn) {
	if(btn == 1) {
	    if(ui.modshift){
		if(ui.modmeta){
		    wdgmsg("transfer-same", item.resname());
		} else {
		    item.wdgmsg("transfer", c);
		}
	    } else if(ui.modctrl) {
		if(ui.modmeta){
		    wdgmsg("drop-same", item.resname());
		} else {
		    item.wdgmsg("drop", c);
		}
	    } else {
		item.wdgmsg("take", c);
	    }
	    return(true);
	} else if(btn == 3) {
	    item.wdgmsg("iact", c);
	    return(true);
	}
	return(false);
    }

    public boolean drop(Coord cc, Coord ul) {
	return(false);
    }
	
    public boolean iteminteract(Coord cc, Coord ul) {
	item.wdgmsg("itemact", ui.modflags());
	return(true);
    }
}
