package haven.res.lib;

import haven.Config;
import haven.Coord;
import haven.Coord3f;
import haven.FastMesh;
import haven.GLState;
import haven.GOut;
import haven.Gob;
import haven.Location;
import haven.Material;
import haven.Material.Colors;
import haven.RenderList;
import haven.Rendered;
import haven.Resource;
import haven.Sprite;
import haven.UI;
import haven.Widget;

import java.awt.Color;

public class HomeTrackerFX extends Sprite {
    private static final Location SCALE = Location.scale(new Coord3f(1.2f, 1.2f ,1));
    private static final Colors COLORS = new Material.Colors(Color.GREEN);
    private static final Location XLATE = Location.xlate(new Coord3f(0,0,2.5f));
    static Resource sres = Resource.load("gfx/fx/arrow", 1);
    Rendered fx = null;
    double ca = 0;
    Gob.Overlay curol = null;
    public Coord c = null;

    public HomeTrackerFX(Owner owner) {
	super(owner, sres);
	((Gob)owner).ols.add(curol = new Gob.Overlay(this));
    }

    
    
    @Override
    public boolean setup(RenderList d) {
	if(!Config.hptr || !UI.instance.gui.mainmenu.hpv){
	    return false;
	}
	if(fx == null){
	    FastMesh.MeshRes mres = (FastMesh.MeshRes)sres.layer(FastMesh.MeshRes.class);
	    this.fx = mres.mat.get().apply(mres.m);
	}
	if(c != null && ((Gob)owner).rc != null){
	    Location rot = Location.rot(Coord3f.zu, (float)(((Gob)this.owner).a - this.ca));
	    d.add(this.fx, GLState.compose(XLATE, SCALE, COLORS, rot));
	}
	return false;
    }
    
    @Override
    public boolean tick(int dt) {
	if(c != null && ((Gob)owner).rc != null){
	    ca = ((Gob)owner).rc.angle(c);
	}
	
	return false;
    }
    
    @Override
    public void dispose() {
	super.dispose();
	((Gob)owner).ols.remove(curol);
    }

    public static class HTrackWdg extends Widget{
	private Widget ptr;
	private HomeTrackerFX fx;
	private Gob player;
	private Coord hc;

	public HTrackWdg(Widget parent, Widget ptr) {
	    super(Coord.z, Coord.z, parent);
	    this.ptr = ptr;
	    player = ui.sess.glob.oc.getgob(ui.gui.plid);
	    fx = new HomeTrackerFX(player);
	}

	@Override
	public void uimsg(String msg, Object... args) {
	    if(msg.equals("upd")){
		Coord hc = (Coord) args[0];
		this.hc = hc;
		if(fx != null){fx.c = hc;}
	    }
	    ptr.uimsg(msg, args);
	}

	@Override
	public void draw(GOut g) {
	    super.draw(g);
	    
	    Gob gob = ui.sess.glob.oc.getgob(ui.gui.plid);
	    if(gob != player){
		player = gob;
		if(fx != null){
		    fx.dispose();
		    fx = null;
		}
		if(player != null){
		    fx = new HomeTrackerFX(player);
		    fx.c = hc;
		}
	    }
	}

    }

}
