package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import haven.States.ColState;

import javax.media.opengl.GL2;
import java.awt.*;
import java.io.IOException;

public class GobPath extends Sprite
{
    private Moving move = null;
    private Gob gob;
    float d = 0.0F;

    public GobPath(Gob gob) {
	super(gob, null);
	this.gob = gob;
    }

    private String resname() {
	Drawable drawable = gob.getattr(Drawable.class);
	try {
	    if(drawable != null && drawable instanceof Composite) {
		Composite composite = (Composite) drawable;
		return composite.base.get().name;
	    } else if(drawable != null && drawable instanceof ResDrawable) {
		ResDrawable resdraw = (ResDrawable) drawable;
		return resdraw.res.get().name;
	    }
	} catch (Resource.Loading ignored){ }
	return "<unknown>";
    }

    public void draw(GOut g) {
	Coord t = target();
	if(t == null){return;}
	boolean good = false;
	Coord td = Coord.z;
	int tz = 0;
	try {
	    Coord ss = new Coord((int) (t.x - gob.loc.c.x), (int) (t.y + gob.loc.c.y));
	    td = ss.rotate(-gob.a);
	    tz = (int) (gob.glob.map.getcz(t) - gob.glob.map.getcz(gob.rc))+1;
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

    private Coord target(){
	if(move != null){
	    Class<? extends GAttrib> aClass = move.getClass();
	    if(aClass == LinMove.class){
		return ((LinMove)move).t;
	    } else if(aClass == Homing.class) {
		return getGobCoords(((Homing)move).tgt());
	    } else if(aClass == Following.class){
		return getGobCoords(((Following)move).tgt());
	    }
	}
	return null;
    }

    private Coord getGobCoords(Gob gob){
	if(gob != null) {
	    Gob.GobLocation loc = gob.loc;
	    if (loc != null) {
		Coord3f c = loc.c;
		if( c != null){
		    return new Coord((int)c.x, -(int)c.y);
		}
	    }
	}
	return null;
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

    public void move(Moving m) {
	move = m;
    }

    public void stop(){
	move = null;
    }

    public static class Cfg {
	public static Cfg def = new Cfg(Color.WHITE, true);
	public Color color;
	public boolean show;
	public String name;

	public Cfg(Color color, boolean show) {
	    this.color = color;
	    this.show = show;
	}

	public static Gson getGson() {
	    GsonBuilder builder = new GsonBuilder();
	    builder.setPrettyPrinting();
	    builder.registerTypeAdapter(GobPath.Cfg.class, new GobPath.Cfg.Adapter().nullSafe());
	    return builder.create();
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
		if(cfg.name != null){
		    writer.name("name").value(cfg.name);
		}
		writer.endObject();
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
		    } else if(name.equals("name")){
			cfg.name = reader.nextString();
		    }
		}
		reader.endObject();
		return cfg;
	    }
	}
    }
}