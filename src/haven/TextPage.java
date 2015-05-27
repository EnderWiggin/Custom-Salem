package haven;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Map;

public class TextPage extends RichTextBox {
    private static final RichText.Foundry fnd;
    private final Tex paper = Resource.loadtex("gfx/hud/blankpaper");

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
	protected RichText.Part tag(PState s, String tn, String[] args, Map<? extends AttributedCharacterIterator.Attribute, ?> attrs) throws IOException {
	    if(tn.equals("item")) {
		return new RichText.Image("gfx/invobjs/" + args[0], -1);
	    } else if(tn.equals("menu")) {
		return new RichText.Image("paginae/" + args[0], -1);
	    } else {
		Map<AttributedCharacterIterator.Attribute, Object> na = new HashMap<AttributedCharacterIterator.Attribute, Object>(attrs);
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
