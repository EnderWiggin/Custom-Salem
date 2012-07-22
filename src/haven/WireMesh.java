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
    public void sdraw(GOut g) {
	GL gl = g.gl;
	VertexBuf.GLArray[] data = new VertexBuf.GLArray[vert.bufs.length];
	VertexBuf.VertexArray vbuf = null;
	int n = 0;
	for(int i = 0; i < vert.bufs.length; i++) {
	    if(vert.bufs[i] instanceof VertexBuf.VertexArray)
		vbuf = (VertexBuf.VertexArray)vert.bufs[i];
	    else if(vert.bufs[i] instanceof VertexBuf.GLArray)
		data[n++] = (VertexBuf.GLArray)vert.bufs[i];
	}
	gl.glLineWidth(5);
	gl.glBegin(GL.GL_LINES);
	for(int i = 0; i < num * 3; i++) {
	    int idx = indb.get(i);
	    for(int o = 0; o < n; o++)
		data[o].set(g, idx);
	    vbuf.set(g, idx);
	}
	gl.glEnd();
	
    }
}
