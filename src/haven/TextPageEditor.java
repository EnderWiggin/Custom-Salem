package haven;

import java.awt.*;
import java.awt.event.KeyEvent;

public class TextPageEditor extends Widget {
    private static final int MAX_CHARS = 1000;
    TextPage page;
    TextEntry text;
    Label symbols;

    @RName("textpageedit")
    public static class $_ implements Factory {
	public Widget create(Coord c, Widget parent, Object[] args) {
	    return new TextPageEditor(c, parent);
	}
    }

    public TextPageEditor(Coord c, Widget parent) {
	super(c, new Coord(485, 560), parent);

	page = new TextPage(Coord.z, new Coord(485, 500), this, "");
	text = new TextEntry(new Coord(0, 505), 485, this, "") {
	    @Override
	    public void activate(String text) {
		if(!ui.modctrl) {
		    buf.key('/', KeyEvent.VK_SLASH, 0);
		    buf.key('n', KeyEvent.VK_N, 0);
		} else {
		    ui.message("SUBMIT!", GameUI.MsgType.INFO);
		    //super.activate(text);
		}
	    }

	    @Override
	    protected void changed() {
		textchanged(text);
	    }
	};
	symbols = new Label(new Coord(0, 530), this, "");
	textchanged("");
    }

    private void textchanged(String text) {
	try {
	    page.settext(text);
	} catch (Exception error) {
	    page.settext(RichText.Parser.quote(text));
	}
	int length = text.length();
	boolean ok = length < MAX_CHARS;
	symbols.col = ok ? Color.WHITE : Color.RED;
	symbols.settext(String.format("Symbols left: %d/%d   Press CTRL+Enter to submit.", MAX_CHARS - length, MAX_CHARS));
    }
}
