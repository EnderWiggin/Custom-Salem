package haven;

import haven.ChatUI.ChatAttribute;
import haven.ChatUI.FuckMeGentlyWithAChainsaw;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.HashMap;
import java.util.Map;

public class TextPage extends RichTextBox {
    private static final RichText.Foundry fnd;
    public static final String WIKI_BASE_URL = "http://www.salem-wiki.com/mediawiki/index.php?title=";
    private final Tex paper = Resource.loadtex("gfx/hud/blankpaper");
    private RichText.Part ttpart = null;
    private Tex tt = null;

    static {
	Font font = null;
	try {
	    font = Font.createFont(Font.TRUETYPE_FONT, Config.getFile("mordred.regular.ttf"));
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    ge.registerFont(font.deriveFont(Font.PLAIN, 18));
	} catch (FontFormatException ignored) {
	} catch (IOException ignored) {
	}
	String family = font != null ? font.getFamily() : "Serif";
	fnd = new RichText.Foundry(new Parser(
	    TextAttribute.FAMILY, family,
	    TextAttribute.SIZE, 18,
	    TextAttribute.FOREGROUND, Color.BLACK
	));
	fnd.aa = true;
    }

    @RName("textpage")
    public static class $_ implements Factory {
	public Widget create(Coord c, Widget parent, Object[] args) {
	    return new TextPage(c, (Coord) args[0], parent, (String) args[1]);
	}
    }

    private static class Parser extends RichText.Parser {
	@Override
	protected RichText.Part tag(PState s, String tn, String[] args,  Map<? extends Attribute, ?> attrs) throws IOException {
	    if(tn.equals("img")) {
		int id = -1;
		if(args.length > 1)
		    id = Integer.parseInt(args[1]);
		return (new Image(args[0], id));
	    } else if(tn.equals("item")) {
		Image img = new Image("gfx/invobjs/" + args[0], -1);
		String name = img.res.layer(Resource.tooltip).t;
		try {
		    img.url = new URL(WIKI_BASE_URL + URLEncoder.encode(name, "UTF-8"));
		} catch (java.net.MalformedURLException ignored) {}
		return img;
	    } else if(tn.equals("menu")) {
		return new Image("paginae/" + args[0], -1);
	    } else {
		Map<Attribute, Object> na = new HashMap<Attribute, Object>(attrs);
		boolean found = false;
		if(tn.equals("c")) {
		    na.put(TextAttribute.FOREGROUND, new Color(Integer.parseInt(args[0], 16)));
		    found = true;
		} else if(tn.equals("h1")) {
		    float sz = a2float(attrs.get(TextAttribute.SIZE));
		    if(sz > 0) {
			na.put(TextAttribute.SIZE, Math.round(1.6 * sz));
			na.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
			found = true;
		    }
		} else if(tn.equals("h2")) {
		    float sz = a2float(attrs.get(TextAttribute.SIZE));
		    if(sz > 0) {
			na.put(TextAttribute.SIZE, Math.round(1.2 * sz));
			na.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
			found = true;
		    }
		} else if(tn.equals("url")) {
		    try {
			if(args[0].indexOf(':') < 0)
			    args[0] = "http://" + args[0];
			URL url = new URL(args[0]);
			na.putAll(ChatUI.ChatParser.urlstyle);
			na.put(ChatAttribute.HYPERLINK, new FuckMeGentlyWithAChainsaw(url));
			found = true;
		    }catch(Exception ignored){
			ignored.printStackTrace();
		    }
		}
		if(found) {
		    if(s.in.peek(true) != '{')
			throw (new RichText.FormatException("Expected `{', got `" + (char) s.in.peek() + "'"));
		    s.in.read();
		    return (text(s, na));
		} else {
		    return super.tag(s, tn, args, attrs);
		}
	    }
	}

	public Parser(Object... attrs) {
	    super(attrs);
	}

	protected float a2float(Object val) {
	    float res = 0;
	    if(val instanceof String) {
		try {
		    res = Float.parseFloat((String) val);
		} catch (Exception ignored) {
		}
	    } else {
		try {
		    res = (Float) val;
		} catch (Exception ignored) {
		}
	    }
	    return res;
	}
    }

    public static class Image extends RichText.Part {
	public BufferedImage img;
	public Resource res;
	public URL url;

	//public Image(Resource res, int id) {
	public Image(String name, int id) {
	    try {
		res = Resource.load(name);
		res.loadwait();
		for (Resource.Image img : res.layers(Resource.imgc)) {
		    if(img.id == id) {
			this.img = img.img;
			break;
		    }
		}
	    } catch (RuntimeException error) {
		this.img = Resource.load("gfx/invobjs/missing").layer(Resource.imgc).img;
	    }
	    if(this.img == null)
		throw (new RuntimeException("Found no image with id " + id + " in " + name));
	}

	public int width() {
	    return (img.getWidth());
	}

	public int height() {
	    return (img.getHeight());
	}

	public int baseline() {
	    return (img.getHeight() - 1);
	}

	public void render(Graphics2D g) {
	    g.drawImage(img, x, y, null);
	}
    }

    public TextPage(Coord c, Coord sz, Widget parent, String text) {
	super(c, sz, parent, "", fnd);
	bg = null;
	drawbox = false;
	settext(text);
    }

    @Override
    public void settext(String text) {
	text = text.replaceAll("/n", "\n");
	text = text.replaceAll("\\$t\\{", "\\$size[30]{");
	try {
	    super.settext(text);
	} catch (Exception error) {
	    super.settext(RichText.Parser.quote(text));
	}
    }

    private URL geturl(Coord c) {
	RichText.Part part = partat(c);
	if(part instanceof Image){
	    return ((Image)part).url;
	} else if(part instanceof RichText.TextPart){
	    RichText.TextPart textPart = (RichText.TextPart) part;
	    int index = textPart.charat(c.sub(textshift())).getCharIndex() + textPart.start;
	    AttributedCharacterIterator inf = textPart.ti();
	    try {
		inf.setIndex(index);
	    }catch(Exception e){
		e.printStackTrace();
	    }
	    FuckMeGentlyWithAChainsaw url = (FuckMeGentlyWithAChainsaw)inf.getAttribute(ChatAttribute.HYPERLINK);
	    if(url != null){
		return url.url;
	    }
	}
	return null;
    }

    private Resource getaction(Coord c){
	RichText.Part part = partat(c);
	if(part instanceof Image){
	    Resource res = ((Image) part).res;
	    if(res != null && res.layer(Resource.action) != null){
		return res;
	    }
	}
	return null;
    }

    @Override
    public boolean mousedown(Coord c, int button) {
	return geturl(c) != null || getaction(c) != null || super.mousedown(c, button);
    }

    @Override
    public boolean mouseup(Coord c, int button) {
	URL url = geturl(c);
	Resource action = getaction(c);
	if(url != null && WebBrowser.self != null) {
	    try {
		WebBrowser.self.show(url);
	    } catch(WebBrowser.BrowserException e) {
		getparent(GameUI.class).error("Could not launch web browser.");
	    }
	    return true;
	} else if(action != null) {
	    ui.gui.menu.useres(action);
	    return true;
	}
	return super.mouseup(c, button);
    }

    @Override
    public Object tooltip(Coord c, Widget prev) {
	RichText.Part p = partat(c);
	URL url = geturl(c);
	if(p == ttpart && tt != null) {
	    return tt;
	} else {
	    tt = null;
	    if(p instanceof Image) {
		Image img = (Image) p;
		Text text = null;
		Resource.Tooltip tip = img.res.layer(Resource.tooltip);
		if(tip != null) {
		    text = Text.render(tip.t);
		} else {
		    Resource.AButton action = img.res.layer(Resource.action);
		    if(action != null) {
			text = Text.render(action.name);
		    }
		}
		Text urltex = null;
		if(url != null){
		    urltex = Text.render(url.toString(), Color.LIGHT_GRAY);
		}
		if(text != null || urltex != null){
		    ttpart = p;
		    if(text != null && urltex != null){
			tt = new TexI(ItemInfo.catimgs(2, text.img, urltex.img));
		    } else if(text != null){
			tt = text.tex();
		    } else {
			tt = urltex.tex();
		    }
		}
	    } else if(p instanceof RichText.TextPart && url != null){
		ttpart = p;
		tt = Text.render(url.toString()).tex();
	    }
	    if(tt != null){
		return tt;
	    }
	}
	return super.tooltip(c, prev);
    }

    @Override
    public void draw(GOut g) {
	g.image(paper, Coord.z);
	super.draw(g);
    }

    @Override
    public void uimsg(String msg, Object... args) {
	if(msg.equals("set")) {
	    settext((String) args[0]);
	} else {
	    super.uimsg(msg, args);
	}
    }
}
