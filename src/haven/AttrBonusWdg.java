package haven;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class AttrBonusWdg extends Widget {
    private BufferedImage bonusImg;
    private static Coord bonusc = new Coord(5, 0);
    private boolean needUpdate;
    private WItem[] witems;

    public AttrBonusWdg(Equipory equip, Coord c) {
	super(c, new Coord(175, 255), equip);
    }

    @Override
    public void draw(GOut g) {
	super.draw(g);
	if(needUpdate){
	    doUpdate();
	}
	if (bonusImg != null) {
	    g.image(bonusImg, bonusc);
	}
    }

    public void update(WItem[] witems) {
	this.witems = witems;
	needUpdate = true;
    }

    private void doUpdate() {
	Map<String, Integer> map = new HashMap<String, Integer>();
	needUpdate = false;

	for (WItem wi : witems) {
	    if (wi != null && wi.item != null) {
		try {
		    for (ItemInfo ii : wi.item.info()) {
			if (ii.getClass().getName().equals("ISlots")) {
			    try {
				Object[] slots = (Object[]) Reflect.getFieldValue(ii, "s");
				for (Object slotted : slots) {
				    if (slotted == null) continue;

				    //noinspection unchecked
				    ArrayList<Object> infos = (ArrayList<Object>) Reflect.getFieldValue(slotted, "info");
				    for (Object info : infos) {
					String[] attrs = (String[]) Reflect.getFieldValue(info, "attrs");
					int[] vals = (int[]) Reflect.getFieldValue(info, "vals");
					for (int i = 0; i < attrs.length; i++) {
					    int val = vals[i];
					    if (map.containsKey(attrs[i])) {
						val += map.get(attrs[i]);
					    }
					    map.put(attrs[i], val);
					}
				    }
				}
			    } catch (Exception ignored) { }
			}
		    }
		} catch (Loading e) {
		    needUpdate = true;
		}
	    }
	}
	int n = map.size();

	if (n > 0) {
	    Resource res = Resource.load("ui/tt/dattr");
	    ItemInfo.InfoFactory f = res.layer(Resource.CodeEntry.class).get(ItemInfo.InfoFactory.class);
	    Object[] bonuses = new Object[2 * n + 1];
	    bonuses[0] = null;
	    int k = 0;
	    for (Map.Entry<String, Integer> entry : map.entrySet()) {
		bonuses[1 + 2 * k] = entry.getKey();
		bonuses[2 + 2 * k] = entry.getValue();
		k++;
	    }
	    LinkedList<ItemInfo> list = new LinkedList<ItemInfo>();
	    list.add(f.build(null, bonuses));
	    bonusImg = ItemInfo.longtip(list);
	}
    }
}
