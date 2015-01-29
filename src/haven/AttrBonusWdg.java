package haven;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class AttrBonusWdg extends Widget {
    private static final Coord SZ = new Coord(175, 255);
    private static final String[] order = new String[]{
	    "Blunt power",
	    "Concussive power",
	    "Impact power",
	    "Feral power",
	    "Piercing power",
	    "Common combat power",
	    "Blunt defence",
	    "Concussive defence",
	    "Impact defence",
	    "Feral defence",
	    "Piercing defence",
	    "Common combat defence",
	    "Culinary",
	    "Mining",
	    "Soil digging",
	    "Weaving",
	    "Woodworking",
	    "Productivity",
	    "Affluence",
	    "Criminality",
	    "Spellcraft",
    };

    private BufferedImage bonusImg;
    private static Coord bonusc = new Coord(5, 20);
    private boolean needUpdate;
    private WItem[] witems;
    private Scrollbar bar;

    public AttrBonusWdg(Equipory equip, Coord c) {
	super(c, SZ, equip);
	bar = new Scrollbar(new Coord(170, bonusc.y), SZ.y-bonusc.y, this, 0, 1);
	bar.visible = false;
	visible = Utils.getprefb("artifice_bonuses", true);
	new Label(new Coord(5, 0), this, "Artifice bonuses:", new Text.Foundry(new Font("SansSerif", Font.PLAIN, 12)));
    }

    @Override
    public void draw(GOut g) {
	super.draw(g);
	if(needUpdate){
	    doUpdate();
	}
	if (bonusImg != null) {
	    Coord c = bonusc;
	    if(bar.visible){
		c = bonusc.sub(0, bar.val);
	    }
	    g.image(bonusImg, c);
	}
    }

    @Override
    public boolean mousewheel(Coord c, int amount) {
	bar.ch(amount * 15);
	return(true);
    }

    public void update(WItem[] witems) {
	this.witems = witems;
	needUpdate = true;
    }

    public void toggle(){
	visible = !visible;
	Utils.setprefb("artifice_bonuses", visible);
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
	    for (String name : order) {
		if(map.containsKey(name)){
		    bonuses[1 + 2 * k] = name;
		    bonuses[2 + 2 * k] = map.remove(name);
		    k++;
		}
	    }
	    for (Map.Entry<String, Integer> entry : map.entrySet()) {
		bonuses[1 + 2 * k] = entry.getKey();
		bonuses[2 + 2 * k] = entry.getValue();
		k++;
	    }
	    LinkedList<ItemInfo> list = new LinkedList<ItemInfo>();
	    list.add(f.build(null, bonuses));
	    bonusImg = ItemInfo.longtip(list);
	} else {
	    bonusImg = null;
	}
	int delta = 0;
	if(bonusImg != null) {
	    delta = bonusImg.getHeight() - SZ.y + bonusc.y;
	}
	bar.visible = delta > 0;
	bar.max = delta;
	bar.ch(0);
    }
}
