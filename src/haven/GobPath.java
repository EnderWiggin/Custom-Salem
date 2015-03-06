package haven;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import haven.States.ColState;

import javax.media.opengl.GL2;
import java.awt.*;
import java.io.IOException;

public class GobPath extends Sprite
{
    private LinMove move;
    private Gob gob;
    float d = 0.0F;

    public GobPath(Gob gob, LinMove m) {
	super(gob, null);
	move = m;
	this.gob = gob;
    }

    private String resname() {
	Drawable drawable = gob.getattr(Drawable.class);
	try {
	    if(drawable != null && drawable instanceof Composite) {
		Composite composite = (Composite) drawable;
		return composite.base.get().name;
	    }
	} catch (Resource.Loading ignored){ }
	return "<unknown>";
    }

    public void draw(GOut g) {
	if(move == null){return;}
	boolean good = false;
	Coord td = Coord.z;
	int tz = 0;
	try {
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
	Cfg cfg = Config.getGobPathCfg(resname());
	if(!cfg.show){ return false;}
	Color color = cfg.color;
	if(Config.gobpath_color) {
	    KinInfo ki = gob.getattr(KinInfo.class);
	    if(ki != null) {
		color = BuddyWnd.gc[ki.group];
	    }
	}
	if(color == null){
	    color = Cfg.def.color;
	}
	list.prepo(new ColState(color));
	return true;
    }

    public void move(LinMove m) {
	move = m;
    }

    public static class Cfg {
	public static Cfg def = new Cfg(Color.WHITE, true);
	public Color color;
	public boolean show;

	public Cfg(){}
	public Cfg(Color color, boolean show) {
	    this.color = color;
	    this.show = show;
	}

	public static class Adapter extends TypeAdapter<Cfg> {

	    @Override
	    public void write(JsonWriter writer, Cfg cfg) throws IOException {
		writer.beginObject();
		writer.name("show").value(cfg.show);
		String color = Utils.color2hex(cfg.color);
		if(color != null){
		    writer.name("color").value(color);
		}
		writer.endArray();
	    }

	    @Override
	    public Cfg read(JsonReader reader) throws IOException {
		Cfg cfg = new Cfg(null, true);
		reader.beginObject();
		while(reader.hasNext()){
		    String name = reader.nextName();
		    if(name.equals("show")){
			cfg.show = reader.nextBoolean();
		    } else if(name.equals("color")){
			cfg.color = Utils.hex2color(reader.nextString(), null);
		    }
		}
		reader.endObject();
		return cfg;
	    }
	}
    }
}