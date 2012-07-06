package haven;

import static haven.Inventory.invsq;
import static haven.WItem.missing;

import haven.Resource.AButton;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class ToolBeltWdg extends Window implements DropTarget{
    private static final Coord invsz = invsq.sz();
    private static final int COUNT = 12;
    
    private static final BufferedImage ilockc = Resource.loadimg("gfx/hud/lockc");
    private static final BufferedImage ilockch = Resource.loadimg("gfx/hud/lockch");
    private static final BufferedImage ilocko = Resource.loadimg("gfx/hud/locko");
    private static final BufferedImage ilockoh = Resource.loadimg("gfx/hud/lockoh");
    
    GameUI gui;
    private int curbelt = 0;
    boolean locked = false, flipped = false;
    private Resource pressed, dragging;
    private int preslot;
    private IButton lockbtn, flipbtn;
    public final int beltkeys[];
    private Tex[] nums;
    private final String name;
    
    public ToolBeltWdg(GameUI parent, String name, int beltn, final int[] keys) {
	super(new Coord(5, 500), Coord.z, parent, null);
	gui = parent;
	curbelt = beltn;
	this.name = name;
	beltkeys = keys;
	mrgn = new Coord(0,0);
	cbtn.visible = false;
	justclose = true;
	
	init();
    }

    private void init() {
	loadOpts();
	lockbtn = new IButton(Coord.z, this, locked?ilockc:ilocko, locked?ilocko:ilockc, locked?ilockch:ilockoh) {
	    public void click() {
		locked = !locked;
		if(locked) {
		    up = ilockc;
		    down = ilocko;
		    hover = ilockch;
		} else {
		    up = ilocko;
		    down = ilockc;
		    hover = ilockoh;
		}
		Config.setWindowOpt(name+"_locked", locked);
	    }
	};
	lockbtn.recthit = true;
	
	flipbtn = new IButton(Coord.z, this, Resource.loadimg("gfx/hud/flip"), Resource.loadimg("gfx/hud/flip"), Resource.loadimg("gfx/hud/flipo")) {
		public void click() {
		    flip();
		}
	};
	flipbtn.recthit = true;
	
	resize(beltc(COUNT-1).add(invsz));
	/* Text rendering is slow, so pre-cache the hotbar numbers. */
	nums = new Tex[COUNT];
	for(int i = 0; i < COUNT; i++) {
	    String key = KeyEvent.getKeyText(beltkeys[i]);
	    nums[i] = new TexI(Utils.outline2(Text.render(key).img, Color.BLACK, true));
	}
    }
    
    private void loadOpts() {
	synchronized (Config.window_props) {
	    if(Config.window_props.getProperty(name+"_locked", "false").equals("true")) {
		locked = true;
	    }
	    if(Config.window_props.getProperty(name+"_flipped", "false").equals("true")) {
		flip();
	    }
	    c = new Coord(Config.window_props.getProperty(name+"_pos", c.toString()));
	}
    }
    
    private void flip() {
	flipped = !flipped;
	Config.setWindowOpt(name+"_flipped", flipped);
	resize(beltc(COUNT-1).add(invsz));
    }

    @Override
    protected void placecbtn() {
	if(flipbtn != null){
	    if(flipped){
		flipbtn.c = new Coord(asz.x - flipbtn.sz.x,0);
	    } else {
		flipbtn.c = new Coord(0, asz.y - flipbtn.sz.y);
	    }
	}
    }

    @Override
    public void cdraw(GOut g) {
	super.cdraw(g);
	for(int i = 0; i < COUNT; i++) {
		int slot = i + (curbelt * COUNT);
		Coord c = beltc(i);
		g.image(invsq, beltc(i));
		Tex tex = null;
		try {
		    if(gui.belt[slot] != null)
			tex = gui.belt[slot].get().layer(Resource.imgc).tex();
		    g.image(tex, c.add(1, 1));
		} catch(Loading e) {
		    missing.loadwait();
		    tex = missing.layer(Resource.imgc).tex();
		    g.image(tex, c, invsz);
		}
		g.chcolor(200, 220, 200, 255);
		g.aimage(nums[i], c.add(invsz), 1, 1);
		g.chcolor();
	    }
    }
    
    @Override
    public void draw(GOut og) {
	super.draw(og);
	if(dragging != null){
	    Tex tex = dragging.layer(Resource.imgc).tex();
	    og.root().aimage(tex, ui.mc, 0.5f, 0.5f);
	}
    }
    
    @Override
    public boolean mousedown(Coord c, int button) {
	int slot = beltslot(c);
	if (button == 1) {
	    pressed = beltres(slot);
	    preslot = slot;
	    if (pressed != null) {
		ui.grabmouse(this);
	    } else {
		super.mousedown(c, button);
		if(locked){canceldm();}
	    }
	} else if((button == 3)&&(!locked)){
	    clearslot(slot);
	}
	return (true);
    }
    
    @Override
    public boolean mouseup(Coord c, int button) {
	int slot = beltslot(c);
	if (button == 1) {
	    if(dragging != null) {
		ui.dropthing(ui.root, ui.mc, dragging);
		dragging = pressed = null;
	    } else if (pressed != null) {
		if (pressed == beltres(slot))
		    use(preslot);
		pressed = null;
		preslot = -1;
	    }
	    ui.grabmouse(null);
	}
	if(dm) {
	    Config.setWindowOpt(name+"_pos", this.c.toString());
	}
	super.mouseup(c, button);
	
	return (true);
    }
    
    @Override
    public void mousemove(Coord c) {
	if ((!locked)&&(dragging == null) && (pressed != null)) {
	    dragging = pressed;
	    clearslot(beltslot(c));
	    pressed = null;
	    preslot = -1;
	} else {
	    super.mousemove(c);
	}
    }
    
    public boolean key(KeyEvent ev) {
	boolean M = (ev.getModifiersEx() & (KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) != 0;
	for(int i = 0; i < beltkeys.length; i++) {
	    if(ev.getKeyCode() == beltkeys[i]) {
		if(M) {
		    curbelt = i;
		    return(true);
		} else {
		    keyact(i);
		    return(true);
		}
	    }
	}
	return false;
    }
    
    public boolean globtype(char ch, KeyEvent ev) {
	if(!key(ev))
	    return(super.globtype(ch, ev));
	else
	    return true;
    }
    
    public boolean type(char key, KeyEvent ev) {
	if(key == 27) {
	    return(false);
	}
	if(!key(ev))
	    return(super.type(key, ev));
	else
	    return true;
    }
    
    private boolean checkmenu(int slot) {
	Resource res = beltres(slot);
	if(res == null){return false;}
	AButton ab = res.layer(AButton.class);
	if(ab != null && ((ab.ad.length == 0) || ab.ad[0].equals("@"))){
	    ui.mnu.useres(res);
	}
	return false;
    }
    
    private void use(int slot) {
	if(slot == -1){return;}
	if(checkmenu(slot)){return;}
	slot += curbelt*COUNT;
	ui.gui.wdgmsg("belt", slot, 1, ui.modflags());
    }

    private void keyact(int index) {
	if(index == -1){return;}
	if(checkmenu(index)){return;}
	final int slot = index + curbelt*COUNT;
	MapView map = ui.gui.map;
	if(map != null) {
	    Coord mvc = map.rootxlate(ui.mc);
	    if(mvc.isect(Coord.z, map.sz)) {
		map.delay(map.new Hittest(mvc) {
		    protected void hit(Coord pc, Coord mc, Gob gob, Rendered tgt) {
			if(gob == null)
			    ui.gui.wdgmsg("belt", slot, 1, ui.modflags(), mc);
			else
			    ui.gui.wdgmsg("belt", slot, 1, ui.modflags(), mc, (int)gob.id, gob.rc);
		    }

		    protected void nohit(Coord pc) {
			ui.gui.wdgmsg("belt", slot, 1, ui.modflags());
		    }
		});
	    }
	}
	}
    
    private void clearslot(int slot) {
	if(slot == -1){return;}
	ui.gui.wdgmsg("setbelt", (curbelt*COUNT)+slot, 1);
    }
    
    private Coord beltc(int i) {
	if(flipped){
	    return(new Coord(0, 
		    ((invsz.y + 2) * i)
		    + (10 * (i / 4)) + ilockc.getWidth() + 2));
	} else {
	    
	    return(new Coord(((invsz.x + 2) * i)
		    + (10 * (i / 4)) + ilockc.getWidth() + 2,
		    0));
	}
    }
    
    public int beltslot(Coord c){
	c = c.sub(ac);
	for(int i = 0; i<COUNT; i++){
	    if(c.isect(beltc(i), invsz)){
		return i;
	    }
	}
	return -1;
    }
    
    public Resource beltres(int slot){
	if(slot == -1){return null;}
	slot += curbelt*COUNT;
	Resource res = null;
	try {
	    if(gui.belt[slot] != null)
		res = gui.belt[slot].get();
	} catch (Loading e){}
	return res;
    }
    
    @Override
    public Object tooltip(Coord c, boolean again) {
	int slot = beltslot(c);
	if(slot  != -1){
	    slot += (curbelt * COUNT);
	    try {
		if(gui.belt[slot] != null){
		    Resource res = gui.belt[slot].get();
		    Resource.AButton ad = res.layer(Resource.action);
		    if(ad != null){
			return ad.name;
		    }
		}
	    }catch(Loading e){return "...";}
	}
	return null;
    }

    @Override
    public boolean dropthing(Coord cc, Object thing) {
	int slot = beltslot(cc);
	if(slot != -1) {
	    slot += (curbelt * COUNT);
	    if(thing instanceof Resource) {
		Resource res = (Resource)thing;
		if(res.layer(Resource.action) != null) {
		    gui.wdgmsg("setbelt", slot, res.name);
		    return(true);
		}
	    }
	}
	return false;
    }

}
