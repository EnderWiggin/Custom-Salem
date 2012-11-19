package haven;

public class DarknessWnd extends Window {
    Label lbl;
    public static DarknessWnd instance;
    public DarknessWnd(Widget parent) {
	super(new Coord(300, 200), Coord.z, parent, "Darkness");
	justclose = true;
	lbl = new Label(Coord.z, this, "");
	instance = this;
	update();
	pack();
    }
    public static void update() {
	if(instance == null){return;}
	Glob g = instance.ui.sess.glob;
	instance.lbl.settext(String.format("Angle: %.2f°, Elevation: %.2f°, color: (%d, %d, %d)",
				180*g.lightang/Math.PI,
				180*g.lightelev/Math.PI,
				g.lightamb.getRed(),g.lightamb.getGreen(),g.lightamb.getBlue()));
    }

}
