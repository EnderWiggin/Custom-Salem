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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import static haven.ItemInfo.find;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class WItem extends Widget implements DTarget {
    public static final Resource missing = Resource.load("gfx/invobjs/missing");
    private static final Coord hsz = new Coord(24, 24);//Inventory.sqsz.div(2);
    public final GItem item;
    private Tex ltex = null;
    private Tex mask = null;
    private Resource cmask = null;
    private long ts = 0;

    public WItem(Coord c, Widget parent, GItem item) {
	super(c, Inventory.sqsz, parent);
	this.item = item;
    }
    
    private static Coord upsize(Coord sz) {
	int w = sz.x, h = sz.y;
	if((w % Inventory.sqsz.x) != 0)
	    w = Inventory.sqsz.x * ((w / Inventory.sqsz.x) + 1);
	if((h % Inventory.sqsz.y) != 0)
	    h = Inventory.sqsz.y * ((h / Inventory.sqsz.y) + 1);
	return(new Coord(w, h));
    }
    
    public void drawmain(GOut g, Tex tex) {
	g.image(tex, Coord.z);
	if(tex != ltex) {
	    resize(upsize(tex.sz()));
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
		    Text.std.renderf("(%d%% pure)", (int)(ch.a[0] * 100)).img);
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
	    long ps = ((WItem)prev).hoverstart;
	    if(now - ps < 1000)
		hoverstart = now;
	    else
		hoverstart = ps;
	} else {
	    hoverstart = now;
	}
	try {
	    if(item == null){return "...";}
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
	    return(new TexI(Utils.outline2(Text.render(Integer.toString(ninf.itemnum()), Color.WHITE).img, Color.DARK_GRAY)));
	}
    };
    
    public final AttrCache<Tex> heurnum = new AttrCache<Tex>() {
	protected Tex find(List<ItemInfo> info) {
	    String num= ItemInfo.getCount(info);
	    if(num == null) return(null);
	    return(new TexI(Utils.outline2(Text.render(num, Color.WHITE).img, Color.DARK_GRAY)));
	}
    };
    
    public final AttrCache<List<Integer>> heurmeter = new AttrCache<List<Integer>>() {
	protected List<Integer> find(List<ItemInfo> info) {
	    List<Integer> meters = ItemInfo.getMeters(info);
	    return meters;
	}
    };

    public final AttrCache<String> contentName = new AttrCache<String>() {
	protected String find(List<ItemInfo> info) {
	    return ItemInfo.getContent(info);
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
	    } else if(heurnum.get() != null){
		g.aimage(heurnum.get(), tex.sz(), 1, 1);
	    }
	    if(item.meter > 0) {
		double a = ((double)item.meter) / 100.0;
		int r = (int) ((1-a)*255);
		int gr = (int) (a*255);
		Coord s2 = sz.sub(0, 4);
		g.chcolor(r, gr, 0, 255);
		Coord bsz = new Coord(4, (int) (a*s2.y));
		g.frect(s2.sub(bsz).sub(4,0), bsz);
		g.chcolor();
	    }
	    checkContents(g);
	    heurmeters(g);
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
	    drawpurity(g);
	} catch(Loading e) {
	    missing.loadwait();
	    g.image(missing.layer(Resource.imgc).tex(), Coord.z, sz);
	}
    }
    
    public final AttrCache<Tex> purity = new AttrCache<Tex>() {
	protected Tex find(List<ItemInfo> info) {
	    Alchemy alch = ItemInfo.find(Alchemy.class, info);
	    if(alch == null){
		ItemInfo.Contents cont = ItemInfo.find(ItemInfo.Contents.class, info);
		if(cont == null){return null;}
		alch = ItemInfo.find(Alchemy.class, cont.sub);
		if(alch == null){return(null);}
	    }
	    String num = String.format("%.2f%%",100*alch.purity());
	    Color c = tryGetFoodColor(info, alch);
	    return(new TexI(Utils.outline2(Text.render(num, c).img, Color.DARK_GRAY)));
	}
    };
    
    public final AttrCache<Tex> puritymult = new AttrCache<Tex>() {
	protected Tex find(List<ItemInfo> info) {
	    Alchemy alch = ItemInfo.find(Alchemy.class, info);
	    if(alch == null){
		ItemInfo.Contents cont = ItemInfo.find(ItemInfo.Contents.class, info);
		if(cont == null){return null;}
		alch = ItemInfo.find(Alchemy.class, cont.sub);
		if(alch == null){return(null);}
	    }
	    String num = String.format("%.2f",1+alch.purity());
	    Color c = tryGetFoodColor(info, alch);
	    return(new TexI(Utils.outline2(Text.render(num, c).img, Color.DARK_GRAY)));
	}
    };
    
    private Color tryGetFoodColor(List<ItemInfo> info, Alchemy alch)
    {
	GobbleInfo food = ItemInfo.find(GobbleInfo.class, info);
	Color c = alch.color();
	if(food!=null)
	{
	    int[] means = new int[4];
	    int i_highest=-1,i_nexthighest=-1;
	    for(int b = 0;b<4;b++)
	    {
		means[b]=(food.h[b]+food.l[b])/2;
		if(i_highest < 0 || means[i_highest] < means[b])
		{
		    i_nexthighest = i_highest;
		    i_highest = b;
		}
		else if(i_nexthighest < 0 || means[i_nexthighest] < means[b])
		{
		    i_nexthighest = b;
		}
	    }
	    if(means[i_nexthighest] < means[i_highest])
	    {
		c = Tempers.colors[i_highest];
	    }
	}
	return c;
    }
    
    private void drawpurity(GOut g) {
	if(ui.modflags() == 0){return;}//show purity only when any mod key pressed
	Tex img = Config.pure_mult?puritymult.get():purity.get();
	if(img != null){
	    g.aimage(img, new Coord(0, sz.y), 0, 1);
	}
    }

    private void checkContents(GOut g) {
	if(!Config.show_contents_icons){return;}
	String contents = contentName.get();
	if(contents == null){ return; }

	Tex tex = getContentTex(contents);
	if(tex == null){return;}

	g.image(tex, Coord.z,hsz);
    }
    
    private Tex getContentTex(String contents) {
	if(Config.contents_icons == null){ return null;}

	String name = null;
	for(Map.Entry<String, String> entry : Config.contents_icons.entrySet()) {
	    if(contents.contains(entry.getKey())){
	    	name = entry.getValue();
		break;
	    }
	}

	Tex tex = null;
	if(name != null){
	    try {
		//return Resource.loadtex(name);
		Resource res = Resource.load(name);
		tex =  new TexI(Utils.outline2(Utils.outline2(res.layer(Resource.imgc).img, Color.BLACK, true), Color.BLACK, true));
	    } catch (Loading e){
		tex =  missing.layer(Resource.imgc).tex();
	    }
	}
	return tex;
    }
    
    private void heurmeters(GOut g) {
	List<Integer> meters = heurmeter.get();
	if(meters == null){return;}
	
	int k = 0;
	Coord s2 = sz.sub(0, 4);
	for (Integer meter : meters){
	    double a = ((double)meter) / 100.0;
	    int r = (int) ((1-a)*255);
	    int gr = (int) (a*255);
	    g.chcolor(r, gr, 0, 255);
	    Coord bsz = new Coord(4, (int) (a*s2.y));
	    g.frect(new Coord(bsz.x*k+1, s2.y - bsz.y), bsz);
	    g.chcolor();
	    k++;
	}
    }
    
    public boolean mousedown(Coord c, int btn) {
	boolean inv = parent instanceof Inventory;
	if(btn == 1) {
	    if(ui.modshift){
		if(ui.modmeta){
		    if(inv){ wdgmsg("transfer-same", item.resname()); }
		} else {
		    item.wdgmsg("transfer", c);
		}
	    } else if(ui.modctrl) {
		if(ui.modmeta){
		    if(inv){ wdgmsg("drop-same", item.resname()); }
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
