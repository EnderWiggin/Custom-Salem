package haven;

import haven.Glob.Pagina;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CraftWnd extends Window implements DTarget2{
    private static final int SZ = 20;
    private static final int PANEL_H = 24;
    private static final Coord WND_SZ = new Coord(635, 360+PANEL_H);
    private static final Coord ICON_SZ = new Coord(SZ, SZ);
    private static final Coord TEXT_POS = new Coord(SZ+2, SZ/2);
    private RecipeListBox box;
    private Tex description;
    private Widget makewnd;
    private MenuGrid menu;
    private Pagina CRAFT;
    private Pagina current;

    public CraftWnd(Coord c, Widget parent) {
	super(c, WND_SZ.add(0,5), parent, "Craft window");
	ui.gui.craftwnd = this;
	init();
    }

    @Override
    public void destroy() {
	box.destroy();
	super.destroy();
    }

    private void init() {
	box = new RecipeListBox(new Coord(0, PANEL_H), this, 200, (WND_SZ.y-PANEL_H)/SZ);
	box.bgcolor = null;
	CRAFT = paginafor("paginae/act/craft");
	menu = ui.gui.menu;
	Pagina selected = menu.cur;
	if(selected == null || !menu.isCrafting(selected)){
	    selected = CRAFT;
	}
	select(selected);

	List<Breadcrumbs.Crumb> items = new LinkedList<Breadcrumbs.Crumb>();
	items.add(new Breadcrumbs.Crumb(CRAFT.res().layer(Resource.imgc).img, "Craft", CRAFT));

	Breadcrumbs breadcrumbs = new Breadcrumbs(new Coord(0,-2), new Coord(560, 20), this);
	breadcrumbs.setSteps(items);

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

    private List<Pagina> getPaginaChildren(Pagina parent, List<Pagina> buf) {
	if(buf == null){buf = new LinkedList<Pagina>();}
	menu.cons(parent, buf);
	return buf;
    }


    public void select(Resource resource) {
	select(paginafor(resource));
    }

    public void select(Pagina p) {
	if (!menu.isCrafting(p)){return;}
	if(box != null){
	    List<Pagina> children = getPaginaChildren(p, null);
	    if(children.size() == 0){
		children = getPaginaChildren(menu.getParent(p), null);
	    } else {
		if(makewnd != null){
		    makewnd.wdgmsg("close");
		}
	    }
	    Collections.sort(children, MenuGrid.sorter);
	    if(p != CRAFT){
		children.add(0, menu.bk);
	    }
	    box.list = children;
	    box.change(p);
	    setCurrent(p);
	}
	Resource res = p.res();
	ItemData data = Config.item_data.get(res.name);
	if(data != null){
	    setDescription(data.longtip(p.res()));
	} else {
	    //setDescription(null);
	    setDescription(MenuGrid.rendertt(p.res(), true, false));
	}
    }

    private void setCurrent(Pagina p) {
	current = p;
    }

    @Override
    public void cdraw(GOut g) {
	super.cdraw(g);

	//drawBreadcrumbs(g);

	if(description != null){
	    g.image(description, new Coord(215, PANEL_H));
	}
    }

    private void drawBreadcrumbs(GOut g) {
	if(current != null){
	    List<Pagina> parents = getParents(current);
	    Collections.reverse(parents);
	    Coord text_pos = new Coord(TEXT_POS);
	    for(Pagina item : parents){
		g.image(item.img.tex(), text_pos.sub(TEXT_POS), ICON_SZ);
		Resource.AButton act = item.act();
		String name = "...";
		if(act != null){
		    name = act.name;
		}
		Coord tsz = g.atext(name+" > ", text_pos, 0, 0.5);
		text_pos.x += tsz.x+SZ;
	    }
	}
    }

    private List<Pagina> getParents(Pagina p) {
	List<Pagina> list = new LinkedList<Pagina>();
	if(p != CRAFT && getPaginaChildren(p, null).size() > 0){
	    list.add(p);
	}
	Pagina parent;
	while((parent = menu.getParent(p)) != CRAFT && parent != null){
	    list.add(parent);
	    p = parent;
	}
	return list;
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

    @Override
    public boolean drop(Coord cc, Coord ul, GItem item) {
	ItemData.actualize(item, box.sel);
	return true;
    }

    @Override
    public boolean iteminteract(Coord cc, Coord ul, GItem item) {
	// TODO Auto-generated method stub
	return false;
    }

    private Pagina paginafor(String name){
	return paginafor(Resource.load(name));
    }

    private Pagina paginafor(Resource res){
	return ui.sess.glob.paginafor(res);
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
	    Resource.AButton act = item.act();
	    String name = "...";
	    if(act != null){
		name = act.name;
	    } else if(item == ui.gui.menu.bk){
		name = "Back";
	    }
	    g.atext(name, TEXT_POS, 0, 0.5);
	}
    }
}
