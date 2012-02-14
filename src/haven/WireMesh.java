package haven;

import javax.media.opengl.GL;

public class WireMesh extends FastMesh {

    public WireMesh(VertexBuf vert, short[] ind) {
	super(vert, ind);
    }

    public WireMesh(FastMesh from, VertexBuf vert) {
	super(from, vert);
    }

    @Override
    public void sdraw(GL gl) {
	gl.glLineWidth(5);
	gl.glBegin(GL.GL_LINES);
	FlatnessTool.recalcheight();
	int o0 = 0;
	for(int i = 0; i < num * 3; i++) {
	    int idx = indb.get(i);
	    int o = idx * 3;
	    vertex(gl, o);
	    if(i%3 == 0){o0 = o;}
	    if(i%3 == 2){vertex(gl, o0);}
	}
	gl.glEnd();
    }

    private void vertex(GL gl, int o) {
	float minv = FlatnessTool.minheight;
	float delta = FlatnessTool.maxheight - minv;
	float v = vert.posb.get(o + 2);
	v = (v - minv)/delta;
	if(v >= 1){v = 0.999f;}
	if(v <= 0){v = 0.001f;}
	gl.glNormal3f(0, 0, 1);
	gl.glTexCoord2f(1 - v, 0);
	gl.glVertex3f(vert.posb.get(o), vert.posb.get(o + 1), vert.posb.get(o + 2));
    }

}
