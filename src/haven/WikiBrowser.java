package haven;

import haven.RichText.Part;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WikiBrowser extends Window {
    public static final RichText.Foundry fnd = new RichText.Foundry(new WikiParser(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 12, TextAttribute.FOREGROUND, Color.WHITE));

    private static class WikiParser extends RichText.Parser {

	public WikiParser(Object... args) {
	    super(args);
	}

	@Override
	protected Part tag(PState s, String tn, String[] args, Map<? extends Attribute, ?> attrs) throws IOException {
	    if(tn.equals("table")){
		return new Table(args, attrs);
	    }
	    return super.tag(s, tn, args, attrs);
	}

    }

    private static class Table extends RichText.Part {
	private static final int PAD_W = 5;
	private static final int PAD_H = 3;
	private int tabc, w=1, h=0, lh = 0;
	private int[] twidth;
	private BufferedImage[] tnames;
	private List<BufferedImage[]> rows = new ArrayList<BufferedImage[]>();
	public Table(String[] args, Map<? extends Attribute, ?> attrs) {
	    super();
	    tabc = Integer.parseInt(args[0]);
	    tnames = new BufferedImage[tabc];
	    twidth = new int[tabc];
	    int i=0;
	    for(i=0; i<tabc; i++){
		tnames[i] = fnd.render(args[i+1]).img;
		twidth[i] = tnames[i].getWidth() + 2*PAD_W;
		lh = Math.max(h, tnames[i].getHeight()+2*PAD_H);
	    }
	    i = tabc+1;
	    while(i < args.length){
		BufferedImage[] cols = new BufferedImage[tabc];
		for(int k=0; k<tabc; k++, i++){
		    cols[k] = fnd.render(args[i]).img;
		    twidth[k] = Math.max(twidth[k], cols[k].getWidth() + 2*PAD_W);
		    lh = Math.max(h, cols[k].getHeight()+2*PAD_H);
		}
		rows.add(cols);
	    }
	    for(i=0; i<tabc; i++){w += twidth[i];}
	    h = lh*(rows.size()+1);
	}

	@Override
	public int height() {return h;}

	@Override
	public int width() {return w;}

	@Override
	public int baseline() {return h-1;}

	@Override
	public void render(Graphics2D g) {
	    g.setColor(Color.WHITE);
	    int cx=x, cy=y, cw, i;
	    for(i=0; i<tabc; i++){
		cw = twidth[i];
		g.drawImage(tnames[i], cx+PAD_W, cy+PAD_H, null);
		g.drawRect(cx, cy, cw, lh);
		cx += cw;
	    }
	    i=1;
	    for(BufferedImage[] cols : rows){
		cx = x;
		cy = y+lh*i;
		for(int j=0; j<tabc; j++){
		    cw = twidth[j];
		    g.drawImage(cols[j], cx+PAD_W, cy+PAD_H, null);
		    g.drawRect(cx, cy, cw, lh);
		    cx += cw;
		}
		i++;
	    }
	}

    }

    private Scrollport sp;
    private TextEntry search;
    private WikiPage page;
    public WikiBrowser(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent, "Wiki");
	justclose = true;
	search = new TextEntry(Coord.z, new Coord(sz.x, 20), this, "");
	search.canactivate = true;
	sp = new Scrollport(new Coord(0, 20), sz, this);
	pack();
	page = new WikiPage(Coord.z, sz, sp.cont);
    }
    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(msg.equals("activate")){
	    if(sender == search){
		page.open(search.text, true);
		return;
	    }
	}
	super.wdgmsg(sender, msg, args);
    }

}
