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
	    page.settext("$h1{       Hello World!}/n/n$h2{  Text formatting is reality!}/nYou can now make text $c[0aaa44]{colored}, $b{bold}, $i{itallic} or $u{underscored} and change its size to be $size[28]{bigger} or $size[14]{smaller}./n/n$h2{  Images can be added!}/nYou can put item icon ( like $item[bar-iron] or $item[rockmaraca]) into texts. It will have tooltip with item name and link to the wiki. If you click it - wiki page will be opened in browser!/nYou can put menu icons ( like $menu[craft/frikadel] or $menu[bld/fishtrap]) into text. It will have tooltip with action name. If you click it - this action will be executed. So now it is possible to make recipe books that list your favorite gluttony foods!/nIt is even possible to add any image from client resources like cursors ($img[gfx/hud/curs/atk]), skills ($img[gfx/hud/skills/arts])./n/n$h2{  Linking websites is possible too!}/nYou can add link to any website. Like $url[forum.salemthegame.com]{forums} or $url[store.salemthegame.com/store]{shop}.");
	    //page.settext(label.texts);
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
