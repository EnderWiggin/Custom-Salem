package haven;

public class CustomWndDetector {
    private boolean canDetect = true;
    private boolean detected = false;

    private Indir<Resource> img = null;
    private Label label = null;
    TextPage page = null;

    public void add(String type, Object[] cargs, Widget child) {
	if(!detected) {
	    if(img == null) {
		if(type.equals("img")) {
		    img = child.ui.sess.getres((Integer) cargs[0]);
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
	if(img != null) {
	    try {
		if(img.get().name.contains("blankpaper")) {
		    detected = true;
		    page = new TextPage(Coord.z, new Coord(485, 500), target, "");
		    updatetext();
		}
	    } catch(Resource.Loading ignored) {
	    }
	}
    }
}
