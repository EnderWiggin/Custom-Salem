/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import haven.glsl.*;
import javax.media.opengl.*;
import static haven.glsl.Cons.*;

public class DropSky implements Rendered {
    public final TexCube tex;

    public DropSky(TexCube tex) {
	this.tex = tex;
    }

    private void vertex(GOut g, GL2 gl, Matrix4f ixf, float[] oc, float x, float y) {
	float[] cc = {x, y, 0.99999f, 1};
	float[] vc = ixf.mul4(cc);
	float iw = 1 / vc[3];
	for(int i = 0; i < 4; i++) vc[i] *= iw;
	if(g.st.prog == null)
	    gl.glMultiTexCoord3f(GL.GL_TEXTURE0 + tsky.id, vc[0] - oc[0], vc[1] - oc[1], vc[2] - oc[2]);
	else
	    gl.glTexCoord3f(vc[0] - oc[0], vc[1] - oc[1], vc[2] - oc[2]);
	gl.glVertex4f(vc[0], vc[1], vc[2], vc[3]);
    }

    public void draw(GOut g) {
	g.apply();
	GL2 gl = g.gl;
	Matrix4f mvxf = new Matrix4f(g.st.cam).mul1(g.st.wxf);
	Matrix4f pmvxf = g.st.cur(PView.proj).fin(Matrix4f.id).mul(mvxf);
	Matrix4f ixf = pmvxf.invert();
	float[] oc = mvxf.invert().mul4(new float[] {0, 0, 0, 1});
	float iw = 1 / oc[3];
	for(int i = 0; i < 4; i++) oc[i] *= iw;
	gl.glBegin(GL2.GL_QUADS);
	vertex(g, gl, ixf, oc, -1.01f, -1.01f);
	vertex(g, gl, ixf, oc,  1.01f, -1.01f);
	vertex(g, gl, ixf, oc,  1.01f,  1.01f);
	vertex(g, gl, ixf, oc, -1.01f,  1.01f);
	gl.glEnd();
    }

    private static final Uniform ssky = new Uniform(Type.SAMPLERCUBE);
    private static final ShaderMacro[] shaders = {
	new ShaderMacro() {
	    AutoVarying texcoord = new AutoVarying(Type.VEC3) {
		    protected Expression root(VertexContext vctx) {
			return(pick(vctx.gl_MultiTexCoord[0].ref(), "stp"));
		    }
		};
	    public void modify(ProgramContext prog) {
		prog.fctx.fragcol.mod(new Macro1<Expression>() {
			public Expression expand(Expression in) {
			    return(mul(in, textureCube(ssky.ref(), texcoord.ref())));
			}
		    }, 0);
	    }
	}
    };
    private GLState.TexUnit tsky;
    private final GLState st = new States.AdHoc(shaders) {
	    public boolean reqshaders() {return(false);}

	    public void reapply(GOut g) {
		g.gl.glUniform1i(g.st.prog.uniform(ssky), tsky.id);
	    }

	    private void papply(GOut g) {
		reapply(g);
	    }

	    private void fapply(GOut g) {
		g.gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
		g.gl.glEnable(GL.GL_TEXTURE_CUBE_MAP);
	    }

	    public void apply(GOut g) {
		(tsky = g.st.texalloc()).act();
		g.gl.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, tex.glid(g));
		if(g.st.prog == null)
		    fapply(g);
		else
		    papply(g);
	    }

	    private void funapply(GOut g) {
		g.gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
	    }

	    public void unapply(GOut g) {
		tsky.act();
		if(!g.st.usedprog)
		    funapply(g);
		g.gl.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, 0);
		tsky.free(); tsky = null;
	    }
	};

    public boolean setup(RenderList rl) {
	rl.prepo(st);
	rl.prepo(States.presdepth);
	return(true);
    }

    public static class ResSky implements Rendered {
	private DropSky sky;
	private Indir<Resource> res;
	public double alpha = 1.0;

	public ResSky(Indir<Resource> res) {
	    this.res = res;
	}

	public void update(Indir<Resource> res) {
	    synchronized(this) {
		if(this.res != res) {
		    this.sky = null;
		    this.res = res;
		}
	    }
	}

	public void draw(GOut g) {
	}

	public boolean setup(RenderList rl) {
	    DropSky sky = this.sky;
	    if(sky == null) {
		synchronized(this) {
		    if(res != null) {
			try {
			    this.sky = sky = new DropSky(new TexCube(res.get().layer(Resource.imgc).img));
			} catch(Loading l) {
			}
		    }
		}
	    }
	    if(sky != null) {
		GLState blend = null;
		if(alpha < 1.0)
		    blend = new States.ColState(255, 255, 255, (int)(255 * alpha));
		rl.add(sky, blend);
	    }
	    return(false);
	}
    }
}
