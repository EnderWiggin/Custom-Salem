package haven;

import java.awt.Color;

public class DarknessWnd extends Window {
    Label lbl;
    public static DarknessWnd instance;
    public DarknessWnd(Widget parent) {
	super(new Coord(300, 200), Coord.z, parent, "Darkness");
	close();
	instance = this;
	justclose = true;
	lbl = new Label(Coord.z, this, "darknesss tool");
	visible = false;
	update();
    }
    
    
    @Override
    public void destroy() {
	instance = null;
	super.destroy();
    }

    public static void close(){
	if(instance != null) {
	    instance.destroy();
	}
    }

    public static void update() {
	if(instance == null){return;}
	Glob g = instance.ui.sess.glob;
	float b = Color.RGBtoHSB(g.lightamb.getRed(),g.lightamb.getGreen(),g.lightamb.getBlue(), null)[2]*100;
	instance.lbl.settext(String.format("Angle: %.2f�, Elevation: %.2f�, color: (%d, %d, %d), b: %.2f",
				180*g.lightang/Math.PI,
				180*g.lightelev/Math.PI,
				g.lightamb.getRed(),g.lightamb.getGreen(),g.lightamb.getBlue(), b));
	instance.pack();
    }

    public static DarknessWnd getInstance(){
	if(instance == null){
	    return new DarknessWnd(UI.instance.gui);
	}
	return instance;
    }
    
    public static void toggle(){
	DarknessWnd wnd = getInstance();
	wnd.visible = !wnd.visible;
    }
    
}
