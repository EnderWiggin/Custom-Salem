package haven;

import haven.Glob.Pagina;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CraftWnd extends Window {
    private static final int SZ = 20;
    private static final Coord WND_SZ = new Coord(635, 360);
    private static final Coord ICON_SZ = new Coord(SZ, SZ);
    private static final Coord TEXT_POS = new Coord(SZ+2, SZ/2);
    private RecipeListBox box;
    private Tex description;
    private Widget makewnd;

    public CraftWnd(Coord c, Widget parent) {
	super(c, WND_SZ, parent, "Craft window");
	ui.gui.craftwnd = this;
	init();
    }

    @Override
    public void destroy() {
	box.destroy();
	super.destroy();
    }

    private void init() {
	List<Pagina> children = getPaginaChilds("paginae/craft/gmt");
	box = new RecipeListBox(Coord.z, this, 200, WND_SZ.y/SZ);
	//box.bgcolor = null;
	box.list = children;
	Collections.sort(children, MenuGrid.sorter);
	box.itemclick(children.get(1), 1);
    }

    @Override
    public void cdestroy(Widget w) {
	if(w == makewnd){
	    makewnd = null;
	}
	super.cdestroy(w);
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if ((sender == this) && msg.equals("close")) {
	    if(makewnd != null){
		makewnd.wdgmsg("close");
		makewnd = null;
	    }
	    ui.destroy(this);
	    ui.gui.craftwnd = null;
	    return;
	}
	super.wdgmsg(sender, msg, args);
    }

    private List<Pagina> getPaginaChilds(String parent) {
	return getPaginaChilds(parent, null);
    }

    private List<Pagina> getPaginaChilds(String parent, List<Pagina> buf) {
	if(buf == null){buf = new LinkedList<Pagina>();}

	ui.gui.menu.cons(ui.sess.glob.paginafor(Resource.load(parent)), buf);
	return buf;
    }

    public void select(Pagina r) {
	if(box != null){
	    box.change(r);
	}
	ItemData data = Config.item_data.get(r.res().name);
	if(data != null){
	    setDescription(data.longtip(r.res()));
	} else {
	    setDescription(null);
	}
//	setDescription(MenuGrid.rendertt(r.res(), true, false));
    }

    @Override
    public void cdraw(GOut g) {
	super.cdraw(g);
	if(description != null){
	    g.image(description, new Coord(215, 0));
	}
    }

    private void setDescription(Tex text) {
	if(description != null){
	    description.dispose();
	}
	description = text;
    }

    public void setMakewindow(Widget widget) {
	makewnd = widget;
    }

    private static class RecipeListBox extends Listbox<Pagina> {
	public List<Pagina> list;
	public RecipeListBox(Coord c, Widget parent, int w, int h) {
	    super(c, parent, w, h, SZ);
	}

	@Override
	protected Pagina listitem(int i) {
	    if(list == null){
		return null;
	    }
	    return list.get(i);
	}

	@Override
	protected int listitems() {
	    if(list == null){
		return 0;
	    }
	    return list.size();
	}

	@Override
	protected void itemclick(Pagina item, int button) {
	    if(button == 1){
		ui.gui.menu.use(item);
	    }
	}

	@Override
	protected void drawitem(GOut g, Pagina item) {
	    if(item == null){
		return;
	    }
	    g.image(item.img.tex(), Coord.z, ICON_SZ);
	    g.atext(item.act().name, TEXT_POS, 0, 0.5);
	}
    }
}
