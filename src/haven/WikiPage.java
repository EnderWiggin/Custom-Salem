package haven;

import haven.RichText.Foundry;
import haven.Scrollport.Scrollcont;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.ender.wiki.HtmlDraw;
import org.ender.wiki.Item;
import org.ender.wiki.Wiki;
import org.ender.wiki.Request.Callback;

public class WikiPage extends SIWidget implements Callback, HyperlinkListener {
    private static final Foundry wpfnd = new Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 28,
	    TextAttribute.FOREGROUND, Color.WHITE);
    private HtmlDraw hd;
    private String name = "Ore Smelter";
    private long last = 1;
    int count = 0;
    private TexI loading = null;

    public WikiPage(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent);
    }

    @Override
    public void draw(GOut g) {
	long now = System.currentTimeMillis();
	if (hd != null && now - last > 1000 && count < 5) {
	    last = now;
	    count++;
	    redraw();
	}
	super.draw(g);
	if (loading != null) {
	    g.aimage(loading, parent.sz.div(2), 0.5f, 0.5f);
	}
    }

    @Override
    public void draw(BufferedImage buf) {
	if (!visible) { return; }
	if (hd == null) { return; }
	hd.setWidth(sz.x);
	presize();
	hd.get(buf, sz.x, sz.y);
	loading(null);
    }

    private void loading(String name) {
	if (loading != null) {
	    loading.dispose();
	}
	if (name == null) {
	    loading = null;
	} else {
	    BufferedImage img = wpfnd.render(String.format("$b{Loading '%s'...}", name)).img;
	    loading = new TexI(Utils.outline2(img, Color.BLACK, true));
	}
    }

    @Override
    public boolean mousedown(Coord c, int button) {
	if (hd != null) {
	    hd.mouseup(c.x, c.y, button);
	}
	return false;
    }

    @Override
    public void wiki_item_ready(Item item) {
	if (item == null) { return; }
	hd = new HtmlDraw(item.content, this);
	if (parent instanceof Scrollcont) {
	    Scrollcont sc = (Scrollcont) parent;
	    Scrollport sp = (Scrollport) sc.parent;
	    sp.bar.ch(-sp.bar.val);
	}
	presize();
	last = System.currentTimeMillis();
	count = 0;
    }

    public void open(String text, boolean search) {
	if (hd != null) {
	    hd.destroy();
	}
	hd = null;
	name = text;
	loading(name);
	if (search) {
	    Wiki.search(name, this);
	} else {
	    Wiki.get(name, this);
	}
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent ev) {
	try {
	    String path = ev.getURL().getPath();
	    String name = path.substring(path.lastIndexOf("index.php/") + 10);
	    System.out.println(String.format("Link: '%s', name: '%s'", path, name));
	    open(name, false);
	} catch (Exception e) {}
    }

    @Override
    public void presize() {
	int h = (hd == null) ? 100 : hd.getHeight();
	resize(new Coord(parent.sz.x, h));
	if (parent instanceof Scrollport.Scrollcont) {
	    ((Scrollport.Scrollcont) parent).update();
	}
    }

}
