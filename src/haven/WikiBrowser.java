package haven;

import haven.Resource.AButton;
import haven.Resource.Tooltip;
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

public class WikiBrowser extends Window implements DTarget2, DropTarget{
    private static final int SEARCH_H = 20;
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

    private static final Coord gzsz = new Coord(15,15);
    private static final Coord minsz = new Coord(200, 150);
    private static final String OPT_SZ = "_sz";
    private static WikiBrowser instance;

    private Scrollport sp;
    private TextEntry search;
    private Button back;
    private WikiPage page;
    boolean rsm = false;
    public WikiBrowser(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent, "Wiki");
	justclose = true;
	search = new TextEntry(Coord.z, new Coord(asz.x-30, SEARCH_H), this, "");
	search.canactivate = true;
	back = new Button(new Coord(asz.x - 20, 0), 20, this, "\u2190"){
	    @Override
	    public Object tooltip(Coord c, Widget prev) {
		return "Back";
	    }
	};
	sp = new Scrollport(new Coord(0, SEARCH_H+3), asz.sub(0, SEARCH_H+3), this);
	pack();
	page = new WikiPage(Coord.z, sp.cont.sz, sp.cont);
    }

    @Override
    protected void loadOpts() {
	super.loadOpts();
	resize(getOptCoord(OPT_SZ, sz));
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(msg.equals("activate")){
	    if(sender == search){
		page.open(search.text, true);
		return;
	    } else if(sender == back){
		page.back();
		return;
	    }
	}
	super.wdgmsg(sender, msg, args);
    }

    @Override
    public void resize(Coord sz) {
	super.resize(sz);
	if(sp != null){sp.resize(sz.sub(0, SEARCH_H+3));}
	if(search!= null){search.resize(new Coord(sz.x - 25, SEARCH_H));}
	if(back!= null){back.c.x = sz.x - 20;}
    }

    @Override
    public boolean mousedown(Coord c, int button) {
	if (button == 1) {
	    ui.grabmouse(this);
	    doff = c;
	    if(c.isect(sz.sub(gzsz), gzsz)) {
		rsm = true;
		return true;
	    }
	}
	return super.mousedown(c, button);
    }

    @Override
    public boolean mouseup(Coord c, int button) {
	if (rsm){
	    ui.grabmouse(null);
	    rsm = false;
	    storeOpt(OPT_SZ, asz);
	    return true;
	}
	return super.mouseup(c, button);
    }

    @Override
    public void mousemove(Coord c) {
	if (rsm){
	    Coord d = c.sub(doff);
	    asz = asz.add(d);
	    asz.x = Math.max(minsz.x, asz.x);
	    asz.y = Math.max(minsz.y, asz.y);
	    doff = c;
	    resize(asz);
	} else {
	    super.mousemove(c);
	}
    }

    public static void toggle() {
	if(instance == null){
	    instance = new WikiBrowser(new Coord(300, 200), minsz, UI.instance.gui);
	} else {
	    close();
	}
    }

    @Override
    public void destroy() {
	instance = null;
	super.destroy();
    }

    public static void close() {
	if(instance != null){
	    UI ui = UI.instance;
	    ui.destroy(instance);
	}
    }

    @Override
    public boolean dropthing(Coord cc, Object thing) {
	if (thing instanceof Resource) {
	    Resource res = (Resource)thing;
	    String name = null;
	    Tooltip tt = res.layer(Resource.tooltip);
	    if(tt!=null){
		name = tt.t;
	    } else {
		AButton ad = res.layer(Resource.action);
		if(ad != null) {
		    name = ad.name;
		}
	    }
	    if(name!=null)
		page.open(name, true);
	    return true;
	}
	return false;
    }

    @Override
    public boolean drop(Coord cc, Coord ul, GItem item) {
	String name = item.name();
	if(name != null){
	    page.open(name, true);
	}
	return true;
    }

    @Override
    public boolean iteminteract(Coord cc, Coord ul, GItem item) {
	return false;
    }

}
