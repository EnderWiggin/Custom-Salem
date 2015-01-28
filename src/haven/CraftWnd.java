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
    private Breadcrumbs breadcrumbs;
    private static Pagina current = null;
    private ItemData data;
    private Resource resd;
    private Pagina senduse = null;

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
	box = new RecipeListBox(new Coord(0, PANEL_H), this, 200, (WND_SZ.y-PANEL_H)/SZ){
	    @Override
	    protected void itemclick(Pagina item, int button) {
		if(button == 1){
		    if(item == menu.bk){
			item = current;
			if(getPaginaChildren(current, null).size()==0){
			    item = menu.getParent(item);
			}
			item = menu.getParent(item);
		    }
		    menu.use(item);
		}
	    }
	};
	box.bgcolor = null;
	CRAFT = paginafor("paginae/act/craft");
	menu = ui.gui.menu;
	breadcrumbs = new Breadcrumbs(new Coord(0, -2), new Coord(WND_SZ.x, SZ), this) {
	    @Override
	    public void selected(Object data) {
		select((Pagina) data, false);
	    }
	};
	Pagina selected = current;
	if(selected == null) {
	    selected = menu.cur;
	    if (selected == null || !menu.isCrafting(selected)) {
		selected = CRAFT;
	    }
	}
	select(selected, true);
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


    public void select(Resource resource, boolean senduse) {
	select(paginafor(resource), senduse);
    }

    public void select(Pagina p, boolean senduse) {
	if (!menu.isCrafting(p)){return;}
	if(box != null){
	    List<Pagina> children = getPaginaChildren(p, null);
	    if(children.size() == 0){
		children = getPaginaChildren(menu.getParent(p), null);
	    } else {
		closemake();
	    }
	    Collections.sort(children, MenuGrid.sorter);
	    if(p != CRAFT){
		children.add(0, menu.bk);
	    }
	    box.setitems(children);
	    box.change(p);
	    setCurrent(p);
	}
	if(senduse){
	    this.senduse = p;
	}
    }

    private void closemake() {
	if(makewnd != null){
	    makewnd.wdgmsg("close");
	}
	senduse = null;
    }

    @Override
    public void cdraw(GOut g) {
	super.cdraw(g);

	if(senduse != null){
	    Pagina p = senduse;
	    closemake();
	    menu.senduse(p);
	}
	drawDescription(g);
    }

    public void drawDescription(GOut g) {
	if(resd == null){return;}
	if(description == null) {
	    if (data != null) {
		try {
		    description = data.longtip(resd);
		}catch (Resource.Loading ignored){}
	    } else {
		description = MenuGrid.rendertt(resd, true, false);
	    }
	}
	if(description != null){
	    g.image(description, new Coord(215, PANEL_H));
	}
    }

    private void setCurrent(Pagina current) {
	CraftWnd.current = current;
	updateBreadcrumbs(current);
	updateDescription(current);
    }

    private void updateBreadcrumbs(Pagina p) {
	List<Breadcrumbs.Crumb> crumbs = new LinkedList<Breadcrumbs.Crumb>();
	List<Pagina> parents = getParents(p);
	Collections.reverse(parents);
	for(Pagina item : parents){
	    BufferedImage img = item.res().layer(Resource.imgc).img;
	    Resource.AButton act = item.act();
	    String name = "...";
	    if(act != null){
		name = act.name;
	    }
	    crumbs.add(new Breadcrumbs.Crumb(img,name, item));
	}
	breadcrumbs.setCrumbs(crumbs);
    }

    private List<Pagina> getParents(Pagina p) {
	List<Pagina> list = new LinkedList<Pagina>();
	if(getPaginaChildren(p, null).size() > 0){
	    list.add(p);
	}
	Pagina parent;
	while((parent = menu.getParent(p)) != null){
	    list.add(parent);
	    p = parent;
	}
	return list;
    }

    private void updateDescription(Pagina p) {
	if(description != null){
	    description.dispose();
	    description = null;
	}

	resd = p.res();
	data = ItemData.get(resd.name);
    }

    public void setMakewindow(Widget widget) {
	makewnd = widget;
    }

    @Override
    public boolean drop(Coord cc, Coord ul, GItem item) {
	ItemData.actualize(item, box.sel);
	updateDescription(current);
	return true;
    }

    @Override
    public boolean iteminteract(Coord cc, Coord ul, GItem item) {
	return false;
    }

    private Pagina paginafor(String name){
	return paginafor(Resource.load(name));
    }

    private Pagina paginafor(Resource res){
	return ui.sess.glob.paginafor(res);
    }

    private static class RecipeListBox extends Listbox<Pagina> {
	private List<Pagina> list;
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

	public void setitems(List<Pagina> list){
	    if(list.equals(this.list)){return;}
	    this.list = list;
	    sb.max = listitems() - h;
	    sb.val = 0;
	}

	@Override
	public void change(Pagina item) {
	    super.change(item);
	    int k = list.indexOf(item);
	    if(k>=0){
		if(k < sb.val){
		    sb.val = k;
		}
		if(k >= sb.val+h){
		    sb.val = Math.min(sb.max, k-h+1);
		}
	    }
	}

	@Override
	protected int listitems() {
	    if(list == null){
		return 0;
	    }
	    return list.size();
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
