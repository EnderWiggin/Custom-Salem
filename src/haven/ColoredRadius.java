package haven;

import haven.States.ColState;
import haven.VertexBuf.NormalArray;
import haven.VertexBuf.VertexArray;
import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ColoredRadius extends Sprite {
    static final GLState smat = new ColState(new Color(13, 186, 192, 128));
    static final GLState emat = new ColState(new Color(255, 251, 86));
    final VertexArray posa;
    final NormalArray nrma;
    final ShortBuffer sidx;
    final ShortBuffer eidx;
    private Coord lc;

    public ColoredRadius(Owner owner, float r) {
	super(owner, null);
	int sections = Math.max(24, (int)(2*Math.PI * (double)r / 11.0D));
	FloatBuffer var6 = Utils.mkfbuf(sections * 3 * 2);
	FloatBuffer var7 = Utils.mkfbuf(sections * 3 * 2);
	ShortBuffer var8 = Utils.mksbuf(sections * 6);
	ShortBuffer var9 = Utils.mksbuf(sections);

	for(int section = 0; section < sections; ++section) {
	    float var11 = (float)Math.sin(2*Math.PI * (double)section / (double)sections);
	    float var12 = (float)Math.cos(2*Math.PI * (double)section / (double)sections);
	    var6.put(section * 3 + 0, var12 * r).put(section * 3 + 1, var11 * r).put(section * 3 + 2, 10.0F);
	    var6.put((sections + section) * 3 + 0, var12 * r).put((sections + section) * 3 + 1, var11 * r).put((sections + section) * 3 + 2, -10.0F);
	    var7.put(section * 3 + 0, var12).put(section * 3 + 1, var11).put(section * 3 + 2, 0.0F);
	    var7.put((sections + section) * 3 + 0, var12).put((sections + section) * 3 + 1, var11).put((sections + section) * 3 + 2, 0.0F);
	    int var13 = section * 6;
	    var8.put(var13 + 0, (short)section).put(var13 + 1, (short)(section + sections)).put(var13 + 2, (short)((section + 1) % sections));
	    var8.put(var13 + 3, (short)(section + sections)).put(var13 + 4, (short)((section + 1) % sections + sections)).put(var13 + 5, (short)((section + 1) % sections));
	    var9.put(section, (short)section);
	}

	VertexArray var14 = new VertexArray(var6);
	NormalArray var15 = new NormalArray(var7);
	this.posa = var14;
	this.nrma = var15;
	this.sidx = var8;
	this.eidx = var9;
    }

    private void setz(Glob var1, Coord var2) {
	FloatBuffer var3 = this.posa.data;
	int var4 = this.posa.size() / 2;

	try {
	    float var5 = var1.map.getcz((float)var2.x, (float)var2.y);

	    for(int var6 = 0; var6 < var4; ++var6) {
		float var7 = var1.map.getcz((float)var2.x + var3.get(var6 * 3), (float)var2.y - var3.get(var6 * 3 + 1)) - var5;
		var3.put(var6 * 3 + 2, var7 + 10.0F);
		var3.put((var4 + var6) * 3 + 2, var7 - 10.0F);
	    }
	} catch (Loading ignored) {
	}

    }

    public boolean tick(int var1) {
	Coord var2 = ((Gob)this.owner).rc;
	if(this.lc == null || !this.lc.equals(var2)) {
	    this.setz(this.owner.glob(), var2);
	    this.lc = var2;
	}

	return false;
    }

    public boolean setup(RenderList var1) {
	var1.prepo(Rendered.eyesort);
	var1.prepo(Material.nofacecull);
	var1.prepo(Location.onlyxl);
	var1.state().put(States.color, null);
	return true;
    }

    public void draw(GOut var1) {
	var1.state(smat);
	var1.apply();
	this.posa.bind(var1);
	this.nrma.bind(var1);
	this.sidx.rewind();
	var1.gl.glDrawElements(4, this.sidx.capacity(), 5123, this.sidx);
	var1.state(emat);
	var1.apply();
	this.eidx.rewind();
	var1.gl.glLineWidth(3.0F);
	var1.gl.glDrawElements(2, this.eidx.capacity(), 5123, this.eidx);
	this.posa.unbind(var1);
	this.nrma.unbind(var1);
    }
}
