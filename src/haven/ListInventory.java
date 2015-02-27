package haven;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ListInventory extends Widget {
    static Tex item_bg = ISBox.bg;
    static Coord item_sz = item_bg.sz();
    Listbox<StackedItem> list;
    Inventory inv;
    long changed = 0;
    List<StackedItem> items;

    public ListInventory(Coord c, Coord sz, Inventory inventory) {
	super(c, sz, inventory.parent);
	inv = inventory;

	list = new Listbox<StackedItem>(Coord.z, this, item_sz.x+10, 5, item_sz.y) {
	    @Override
	    protected StackedItem listitem(int i) {
		return items.get(i);
	    }

	    @Override
	    protected int listitems() {
		return (items != null)?items.size():0;
	    }

	    @Override
	    protected void drawitem(GOut g, StackedItem item) {
		try {
		    g.image(item_bg, Coord.z);
		    item.draw(g);
		    g.text(item.name(), new Coord(45, 0));
		}catch (Resource.Loading ignored){}
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
	StackedItem item = list.itemat(c.sub(list.c));
	if(item != null) {
	    //return item.tooltip(Coord.z, (prev==this)?item:prev);
	}
	return null;
    }

    private void invchanged() {
	Collection<WItem> values = inv.wmap.values();
	items = new LinkedList<StackedItem>();
	for(WItem item : values){
	    boolean found = false;
	    for(StackedItem sitem : items){
		if(sitem.put(item)){
		    found = true;
		    break;
		}
	    }
	    if(!found){
		items.add(new StackedItem(item));
	    }

	}
    }

    public static class StackedItem {
	public List<WItem> list;
	public StackedItem(WItem item) {
	    list = new LinkedList<WItem>();
	    list.add(item);
	}

	public boolean put(WItem item){
	    if(list.get(0).item.name().equals(item.item.name())){
		list.add(item);
		return true;
	    }
	    return false;
	}

	public String name(){
	    return String.format("%s x%d", list.get(0).item.name(), list.size());
	}

	public void draw(GOut g){
	    try {
		list.get(0).draw(g);
	    }catch (Resource.Loading ignored){}
	}

	public Object tooltip(Coord c, Widget prev){
	    return list.get(0).tooltip(c, prev);
	}
    }

}
