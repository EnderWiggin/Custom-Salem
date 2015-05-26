package haven;

public class PageDetector {
    private boolean canDetect = true;
    private boolean detected = false;

    private Indir<Resource> res = null;
    private Widget img = null;
    private Label label = null;
    TextPage page = null;

    public void add(String type, Object[] cargs, Widget child) {
	if(!detected) {
	    if(res == null) {
		if(type.equals("img")) {
		    res = child.ui.sess.getres((Integer) cargs[0]);
		    img = child;
		} else {
		    canDetect = false;
		}
	    }
	}
	if((canDetect || detected) && type.equals("lbl")) {
	    label = (Label) child;
	    if(detected) {
		updatetext();
	    }
	}

    }

    private void updatetext() {
	if(label != null) {
	    label.visible = false;
	    page.settext(label.texts);
	}
    }

    public boolean canDetect() {
	return canDetect;
    }

    public boolean detected() {
	return detected;
    }

    public void detect(Widget target) {
	if(res != null) {
	    try {
		if(res.get().name.equals("gfx/hud/blankpaper")) {
		    detected = true;
		    canDetect = false;
		    page = new TextPage(Coord.z, new Coord(485, 500), target, "");
		    img.visible = false;
		    updatetext();
		}
	    } catch(Resource.Loading ignored) {
	    }
	}
    }
}
