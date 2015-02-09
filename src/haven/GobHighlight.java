package haven;

import java.awt.*;

public class GobHighlight extends GAttrib {
    Material.Colors fx;
    int time = 0;
    int duration = 5000;

    public GobHighlight(Gob gob) {
	super(gob);
	Color c = new Color(64, 255, 64, 100);
	fx = new Material.Colors();
	fx.amb = Utils.c2fa(c);
	fx.dif = Utils.c2fa(c);
	fx.emi = Utils.c2fa(c);
    }

    public void ctick(int dt) {
	time += dt;
	float a = (float) (0.49f * (1 + Math.sin(time/100f)));
	fx.amb[3] = a;
	fx.dif[3] = a;
	fx.emi[3] = a;

	duration -= dt;
    }

    public GLState getfx() {
	return(fx);
    }
}
