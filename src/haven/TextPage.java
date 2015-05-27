package haven;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;

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
	fnd = new RichText.Foundry(
	    TextAttribute.FAMILY, family,
	    TextAttribute.SIZE, 18,
	    TextAttribute.FOREGROUND, Color.BLACK
	);
	fnd.aa = true;
    }

    @RName("textpage")
    public static class $_ implements Factory {
	public Widget create(Coord c, Widget parent, Object[] args) {
	    return new TextPage(c, (Coord) args[0], parent, (String) args[1]);
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
