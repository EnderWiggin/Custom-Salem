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
	float minv = FlatnessTool.minheight;
	float maxv = FlatnessTool.maxheight;
	float delta = maxv - minv;
	
	int o0 = 0;
	for(int i = 0; i < num * 3; i++) {
	    int idx = indb.get(i);
	    int o = idx * 3;
	    //gl.glNormal3f(vert.nrmb.get(o), vert.nrmb.get(o + 1), vert.nrmb.get(o + 2));
	    gl.glNormal3f(0, 0, 1);
	    float v = vert.posb.get(o + 2);
	    v = Math.min((v - minv)/delta, 1.0f);
	    gl.glTexCoord2f(1 - v, 0);
	    gl.glVertex3f(vert.posb.get(o), vert.posb.get(o + 1), vert.posb.get(o + 2));
	    if(i%3 == 0){o0 = o;}
	    if(i%3 == 2){
		v = vert.posb.get(o0 + 2);
		v = Math.min((v - minv)/delta, 1.0f);
		gl.glNormal3f(0, 0, 1);
		gl.glTexCoord2f(1 - v, 0);
		gl.glVertex3f(vert.posb.get(o0), vert.posb.get(o0 + 1), vert.posb.get(o0 + 2));
	    }
	}
	gl.glEnd();
    }

}
