package haven;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public class WireMesh extends FastMesh {

    public WireMesh(VertexBuf vert, short[] ind) {
	super(vert, ind);
    }

    public WireMesh(FastMesh from, VertexBuf vert) {
	super(from, vert);
    }

    @Override
    public void draw(GOut g) {
	g.apply();
	GL2 gl = g.gl;
	VertexBuf.VertexArray vbuf = null;
	for(int i = 0; i < vert.bufs.length; i++) {
	    if(vert.bufs[i] instanceof VertexBuf.VertexArray)
		vbuf = (VertexBuf.VertexArray)vert.bufs[i];
	}
	gl.glLineWidth(5);
	gl.glBegin(GL.GL_LINES);
	int o0 = 0;
	for(int i = 0; i < num * 3; i++) {
	    int idx = indb.get(i);
	    int o = idx * 3;
	    vertex(gl, o, vbuf);
	    if(i%3 == 0){o0 = o;}
	    if(i%3 == 2){vertex(gl, o0, vbuf);}
	}
	gl.glEnd();

    }

    private void vertex(GL2 gl, int o, VertexBuf.VertexArray vbuf) {
	float minv = FlatnessTool.minheight;
	float delta = FlatnessTool.maxheight - minv;
	float v = vbuf.data.get(o + 2);
	v = (v - minv)/delta;
	if(v >= 1){v = 0.999f;}
	if(v <= 0){v = 0.001f;}
	gl.glNormal3f(0, 0, 1);
	gl.glTexCoord2f(1 - v, 0);
	gl.glVertex3f(vbuf.data.get(o), vbuf.data.get(o + 1), vbuf.data.get(o + 2));
    }
}
