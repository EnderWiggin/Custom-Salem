import haven.*;
import haven.BuddyWnd.GroupSelector;
import haven.Button;
import haven.Label;
import haven.MCache.Overlay;
import haven.Window;

import java.awt.*;

public class Landwindow extends Window {
    Widget bn, be, bs, bw;
    Widget buy;
    Widget reset;
    Widget dst;
    GroupSelector group;
    Label area, cost, lbl_upkeep;
    int auth;
    int acap;
    int adrain;
    boolean offline;
    Coord c1;
    Coord c2;
    Coord cc1;
    Coord cc2;
    Overlay ol;
    MCache map;
    int[] bflags = new int[8];
    PermBox[] perms = new PermBox[3];
    CheckBox homeck;
    private Tex rauth = null;
    private float upkeep = 0;

    @SuppressWarnings("deprecation")
    public static class Maker implements WidgetFactory {
	public Widget create(Coord var1, Widget var2, Object[] args) {
	    Coord var4 = (Coord)args[0];
	    Coord var5 = (Coord)args[1];
	    boolean var6 = (Integer) args[2] != 0;
	    return new Landwindow(var1, var2, var4, var5, var6);
	}
    }

    private class PermBox extends CheckBox {
	int fl;

	PermBox(Coord var2, Widget var3, String var4, int var5) {
	    super(var2, var3, var4);
	    this.fl = var5;
	}

    public void changed(boolean var1) {
	int var2 = 0;

	for (PermBox perm : Landwindow.this.perms) {
	    if (perm.a) {
		var2 |= perm.fl;
	    }
	}

	Landwindow.this.wdgmsg("shared", Landwindow.this.group.group, var2);
    }
}

    private void fmtarea() {
	int area = (c2.x - c1.x + 1) * (c2.y - c1.y + 1);
	this.area.settext(String.format("Area: %d m²", area));
	upkeep = 4 + area/300f;
	updupkeep();
    }

    private void updupkeep() {
	float days = auth/upkeep;
	lbl_upkeep.settext(String.format("Upkeep: %.2f/day, enough for %.1f days", upkeep, days));
    }

    private void updatecost() {
	int cost = (cc2.x - cc1.x + 1) * (cc2.y - cc1.y + 1) - (c2.x - c1.x + 1) * (c2.y - c1.y + 1);
	this.cost.settext(String.format("Cost: %d", cost));
    }

    private void updflags() {
	int var1 = this.bflags[this.group.group];

	for (PermBox perm : this.perms) {
	    perm.a = (var1 & perm.fl) != 0;
	}

    }

    public Landwindow(final Coord c, final Widget parent, final Coord c1, final Coord c2, final boolean homestead) {
	super(c, new Coord(0, 0), parent, "Stake");
	this.cc1 = this.c1 = c1;
	this.cc2 = this.c2 = c2;
	this.map = this.ui.sess.glob.map;
	ui.gui.map.enol(0, 1, 16);
	this.ol = map.new Overlay(this.cc1, this.cc2, 65536);
	this.area = new Label(new Coord(0, 0), this, "");
	int y = 15;
	lbl_upkeep = new Label(new Coord(0, y), this, "");
	y += 15;
	new Widget(new Coord(0, y), new Coord(220, 20), this){
	    {
		tooltip = RichText.render("Upkeep paid\n\n$i{(Hold silver and Right-Click the Claim Stone to add more.)}", 150);
	    }
	    public void draw(GOut g) {
		int auth = Landwindow.this.auth;
		int acap = Landwindow.this.acap;
		if(acap > 0) {
		    g.chcolor(0, 0, 0, 255);
		    g.frect(Coord.z, this.sz);
		    g.chcolor(128, 0, 0, 255);
		    Coord var4 = this.sz.sub(2, 2);
		    var4.x = auth * var4.x / acap;
		    g.frect(new Coord(1, 1), var4);
		    g.chcolor();
		    if(rauth == null) {
			Color color = offline?Color.RED:Color.WHITE;
			rauth =  new TexI(Utils.outline2(Text.render(String.format("%s/%s", auth, acap), color).img, Utils.contrast(color)));
		    }

		    g.aimage(rauth, this.sz.div(2), 0.5D, 0.5D);
		}

	    }
	};
	y += 25;
	cost = new Label(new Coord(0, y), this, "Cost: 0");
	y += 25;
	fmtarea();
	bn = new Button(new Coord(70, y), 80, this, "Extend North");
	be = new Button(new Coord(140, y + 25), 80, this, "Extend East");
	bs = new Button(new Coord(70, y + 50), 80, this, "Extend South");
	bw = new Button(new Coord(0, y + 25), 80, this, "Extend West");
	y += 80;
	buy = new Button(new Coord(0, y), 60, this, "Buy");
	reset = new Button(new Coord(80, y), 60, this, "Reset");
	dst = new Button(new Coord(160, y), 60, this, "Declaim");
	y += 25;
	new Label(new Coord(0, y), this, "Assign permissions to memorized people:");
	y += 15;
	group = new GroupSelector(new Coord(0, y), this, 0){
	    protected void changed(int group) {
		super.changed(group);
		updflags();
	    }
	};
	y += 20;
	perms[0] = new PermBox(new Coord(10, y), this, "Trespassing", 1);
	y += 20;
	perms[1] = new PermBox(new Coord(10, y), this, "Theft", 2);
	y += 20;
	perms[2] = new PermBox(new Coord(10, y), this, "Vandalism", 4);
	y += 20;
	y += 10;
	homeck = new CheckBox(new Coord(0, y), this, "Use as homestead"){
	    {
		this.a = homestead;
	    }

	    public boolean mousedown(Coord c, int button) {
		if(!this.a) {
		    Landwindow.this.wdgmsg("mkhome");
		    this.set(true);
		}

		return true;
	    }

	    public void changed(boolean val) {
	    }
	};
	this.pack();
    }

    public void destroy() {
	ui.gui.map.disol(0, 1, 16);
	this.ol.destroy();
	super.destroy();
    }

    public void uimsg(String msg, Object... args) {
	if(msg.equals("upd")) {
	    Coord var3 = (Coord)args[0];
	    Coord var4 = (Coord)args[1];
	    this.c1 = var3;
	    this.c2 = var4;
	    this.fmtarea();
	    this.updatecost();
	} else if(msg.equals("shared")) {
	    int var5 = (Integer) args[0];
	    int var6 = (Integer) args[1];
	    this.bflags[var5] = var6;
	    if(var5 == this.group.group) {
		this.updflags();
	    }
	} else if(msg.equals("auth")) {
	    this.auth = (Integer) args[0];
	    this.acap = (Integer) args[1];
	    this.adrain = (Integer) args[2];
	    this.offline = (Integer) args[3] != 0;
	    this.rauth = null;
	    updupkeep();
	}

    }

    public void wdgmsg(Widget sender, String m, Object... args) {
	if(sender == this.bn) {
	    this.cc1 = this.cc1.add(0, -1);
	    this.ol.update(this.cc1, this.cc2);
	    this.updatecost();
	} else if(sender == this.be) {
	    this.cc2 = this.cc2.add(1, 0);
	    this.ol.update(this.cc1, this.cc2);
	    this.updatecost();
	} else if(sender == this.bs) {
	    this.cc2 = this.cc2.add(0, 1);
	    this.ol.update(this.cc1, this.cc2);
	    this.updatecost();
	} else if(sender == this.bw) {
	    this.cc1 = this.cc1.add(-1, 0);
	    this.ol.update(this.cc1, this.cc2);
	    this.updatecost();
	} else if(sender == this.buy) {
	    this.wdgmsg("take", this.cc1, this.cc2);
	} else if(sender == this.reset) {
	    this.ol.update(this.cc1 = this.c1, this.cc2 = this.c2);
	    this.updatecost();
	} else if(sender == this.dst) {
	    this.wdgmsg("declaim");
	} else {
	    super.wdgmsg(sender, m, args);
	}
    }
}
