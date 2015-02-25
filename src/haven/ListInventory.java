package haven;

import java.util.LinkedList;
import java.util.List;

public class ListInventory extends Widget {
    static Tex item_bg = ISBox.bg;
    static Coord item_sz = item_bg.sz();
    Listbox<WItem> list;
    Inventory inv;
    long changed = 0;
    List<WItem> items;

    public ListInventory(Coord c, Coord sz, Inventory inventory) {
	super(c, sz, inventory.parent);
	inv = inventory;

	list = new Listbox<WItem>(Coord.z, this, item_sz.x+10, 5, item_sz.y) {
	    @Override
	    protected WItem listitem(int i) {
		return items.get(i);
	    }

	    @Override
	    protected int listitems() {
		return (items != null)?items.size():0;
	    }

	    @Override
	    protected void drawitem(GOut g, WItem item) {
		g.image(item_bg, Coord.z);
		item.draw(g);
		g.text(item.item.name(), new Coord(45, 0));
	    }
	};
	list.bgcolor = null;
    }

    @Override
    public void draw(GOut g) {
	super.draw(g);
	if(changed != inv.changed){
	    changed = inv.changed;
	    invchanged();
	}
    }

    @Override
    public Object tooltip(Coord c, Widget prev) {
	WItem item = list.itemat(c.sub(list.c));
	if(item != null) {
	    return item.tooltip(Coord.z, (prev==this)?item:prev);
	}
	return null;
    }

    private void invchanged() {
	items = new LinkedList<WItem>(inv.wmap.values());
    }

}
