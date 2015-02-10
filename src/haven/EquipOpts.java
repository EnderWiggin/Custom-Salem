package haven;

import java.util.*;

class EquipOpts extends GameUI.Hidewnd {
    private static final Map<Integer, String> slotNames;
    private static final List<Integer> slotOrder;
    static {
	final List<Integer> ao = new ArrayList<Integer>();
	Map<Integer, String> an = new HashMap<Integer, String>() {
	    public String put(Integer k, String v) {
		ao.add(k);
		return(super.put(k, v));
	    }
	};
	an.put(0, "Head");
	an.put(6, "Left hand");
	an.put(7, "Right hand");
	an.put(9, "Purse");
	an.put(14, "Back");
	an.put(5, "Belt");
	an.put(4, "Keys");
	slotNames = Collections.unmodifiableMap(an);
	slotOrder = Collections.unmodifiableList(ao);
    }

    private Map<CheckBox, Integer> checkSlots = new HashBMap<CheckBox, Integer>();
    private List<Integer> selected;

    public EquipOpts(Coord c, Widget parent) {
	super(c, Coord.z, parent, "Proxy CFG");
	int k = 0;
	read();
	for(int slot : slotOrder){
	    CheckBox checkBox = new CheckBox(new Coord(0, 20 * k++), this, slotNames.get(slot)) {
		@Override
		public void changed(boolean val) {
		    setSlotState(this, val);
		}
	    };
	    checkBox.a = selected.contains(slot);
	    checkSlots.put(checkBox, slot);
	}
	pack();
	update();
    }

    @Override
    public Coord contentsz() {
	Coord sz = super.contentsz();
	sz.x = Math.max(sz.x, 100);
	return sz;
    }

    public void toggle(){
	show(!visible);
	if(visible){
	    raise();
	}
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {

	super.wdgmsg(sender, msg, args);
    }

    private void read() {
	selected = new LinkedList<Integer>();
	String[] slots =  Utils.getpref("equip_proxy_slots", "6;7;9;14;5;4").split(";");
	for(String slot : slots){
	    try {
		selected.add(Integer.parseInt(slot));
	    } catch (NumberFormatException ignored){}
	}
    }

    private void setSlotState(CheckBox check, boolean val) {
	int slot = checkSlots.get(check);
	int k = selected.indexOf(slot);
	if(!val && k>=0){
	    selected.remove(k);
	} else if(val && k<0){
	    selected.add(slot);
	}
	store();
	update();
    }

    private void store() {
	String buf = "";
	int n = selected.size();
	for(int i=0; i< n; i++){
	    buf += selected.get(i);
	    if(i < n-1 ){
		buf+=";";
	    }
	}
	Utils.setpref("equip_proxy_slots", buf);
    }

    private void update() {
	int[] slots = new int[selected.size()];
	int k = 0;
	for(int slot : slotOrder) {
	    if (selected.contains(slot)) {
		slots[k++] = slot;
	    }
	}
	ui.gui.equipProxy.setSlots(slots);
    }
}
