package haven;

import java.awt.image.BufferedImage;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.ender.wiki.HtmlDraw;
import org.ender.wiki.Item;
import org.ender.wiki.Wiki;
import org.ender.wiki.Request.Callback;


public class WikiPage extends SIWidget implements Callback, HyperlinkListener {
    private HtmlDraw hd;
    private String name = "Ore Smelter";
    BufferedImage img;
    private long last = 1;
    int count = 0;
    public WikiPage(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent);
    }
    @Override
    public void draw(GOut g) {
	long now = System.currentTimeMillis();
	if(now - last > 1000 && count < 10){
	    last = now;
	    count++;
	    redraw();
	}
	super.draw(g);
    }

    @Override
    public void draw(BufferedImage buf){
	if(!visible){return;}
	if(hd == null){return;}
	hd.get(buf, sz.x, sz.y);
	img = buf;
    }

    @Override
    public boolean mousedown(Coord c, int button) {
	if(hd != null){
	    hd.mouseup(c.x, c.y, button);
	}
	return false;
    }
    
    @Override
    public void wiki_item_ready(Item item) {
	if(item == null){return;}
	hd = new HtmlDraw(item.content, this);
	hd.setWidth(sz.x);
	sz.y = hd.getHeight();
	if(parent instanceof Scrollport.Scrollcont){
	    ((Scrollport.Scrollcont)parent).update();
	}
	redraw();
    }
    public void open(String text) {
	if(hd != null){hd.destroy();}
	hd = null;
	name = text;
	Wiki.get(name, this);
    }
    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
	String path = e.getURL().getPath();
	path = path.substring(path.lastIndexOf("/")+1);
	open(path);
    }
}
