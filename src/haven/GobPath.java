package haven;

import haven.States.ColState;

import javax.media.opengl.GL2;
import java.awt.*;

public class GobPath extends Sprite
{
    private LinMove move;
    float d = 0.0F;

    public GobPath(Gob gob, LinMove m) {
	super(gob, null);
	move = m;
    }

    public void draw(GOut g) {
	if(move == null){return;}
	boolean good = false;
	Coord td = Coord.z;
	int tz = 0;
	try {
	    Gob gob = (Gob) owner;
	    Coord ss = new Coord((int) (move.t.x - gob.loc.c.x), (int) (move.t.y + gob.loc.c.y));
	    td = ss.rotate(-gob.a);
	    tz = (int) (gob.glob.map.getcz(move.t) - gob.glob.map.getcz(gob.rc))+1;
	    good = true;
	} catch (Exception ignored) { }
	if(!good) { return; }

	g.apply();
	GL2 gl = g.gl;
	gl.glLineWidth(3.0F);
	gl.glBegin(1);
	gl.glVertex3i(0, 0, 3);
	gl.glVertex3i(td.x, td.y, tz);
	gl.glEnd();
	GOut.checkerr(gl);
    }

    public boolean setup(RenderList list) {
	Gob gob = (Gob) owner;
	Color color = Color.WHITE;
	if(Config.gobpath_color) {
	    KinInfo ki = gob.getattr(KinInfo.class);
	    if(ki != null) {
		color = BuddyWnd.gc[ki.group];
	    }
	}
	list.prepo(new ColState(color));
	return true;
    }

    public void move(LinMove m) {
	move = m;
    }
}