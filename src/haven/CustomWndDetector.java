package haven;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CustomWndDetector {
    private boolean custom_detected = false;
    private boolean canDetect = true;
    private List<String> types = new LinkedList<String>();
    private List<Object[]> args = new LinkedList<Object[]>();
    private List<Widget> children = new LinkedList<Widget>();

    public void add(String type, Object[] cargs, Widget child) {
	types.add(type);
	args.add(cargs);
	children.add(child);
    }

    public boolean canDetect() {
	return canDetect;
    }

    public void detect(Widget target) {
	int n = types.size();
	boolean detected = true;
	if(n >= 2) {
	    for (int i = 0; i < n; i++) {
		String type = types.get(i);
		Object[] arg = args.get(i);
		if(i == 0) {
		    if(type.equals("img")) {
			Indir<Resource> res = target.ui.sess.getres((Integer) arg[0]);
			try {
			    if(!res.get().name.contains("blankpaper")) {
				detected = false;
				canDetect = false;
				break;
			    }
			} catch (Loading ignored) {
			    detected = false;
			}
		    } else {
			detected = false;
			canDetect = false;
			break;
		    }
		} else if(i == 1) {
		    if(!type.equals("lbl")) {
			detected = false;
			canDetect = false;
			break;
		    }
		}
	    }
	} else {
	    detected = false;
	}
	if(detected) {
	    canDetect = false;
	    System.out.println("FOUND page!");
	    children.get(1).show(false);
	    String text = (String) args.get(1)[0];
	    text = "asd $col[128,32,32]{TE$b{XT}} $size[33]{ww} /n second line! woo hoo! /n ahsgdfhagsfd ahgda hagsd agsdahgd fdgsahsgdf ahsgd ahgsdf ahsgdf ahgsdf ";

	    //(new RichTextBox(Coord.z, new Coord(505, 500), target, text, TextAttribute.FAMILY, "SansSerif",TextAttribute.SIZE, 16, TextAttribute.FOREGROUND, Color.BLACK)).bg = null;
	    Window wnd = new Window(Coord.z, Coord.z, target.ui.gui, "Book");
	    wnd.justclose = true;
	    new TextPage(Coord.z, new Coord(485, 500), wnd, text);
	    wnd.pack();
	}
    }

    private static class BookMatcher {

    }
}
