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

package haven.resutil;

import java.awt.Color;
import java.util.*;
import javax.media.opengl.*;
import haven.*;

public class GlowEffect extends GLState implements GLState.GlobalState {
    public static final Slot<GlowEffect> slot = new Slot<GlowEffect>(Slot.Type.DRAW, GlowEffect.class, PView.wnd);
    private static final Map<PView.RenderState, GlowBuf> bufs = new WeakHashMap<PView.RenderState, GlowBuf>();
    public final States.ColState col;
    
    public GlowEffect(Color col) {
	this.col = new States.ColState(col);
    }
    
    public static class GlowBuf implements Global {
	public final PView.RenderState wnd;
	private TexE buf;
	private FBView tgt;
	private Coord lsz;
	private GLState.Delegate proj = new GLState.Delegate(null);
	private GLState.Delegate cam = new GLState.Delegate(null);
	private final List<RenderList.Slot> parts = new ArrayList<RenderList.Slot>();
	
	private final GLState draw = new States.AdHoc(new GLShader[] {
		GLShader.VertexShader.load(TexGL.class, "glsl/tex2d.vert"),
		GLShader.FragmentShader.load(GlowEffect.class, "glowblur.frag"),
	    }) {
		public void apply(GOut g) {
		    GL gl = g.gl;
		    g.st.texunit(0);
		    gl.glBindTexture(GL.GL_TEXTURE_2D, buf.glid(g));
		    reapply(g);
		}
		
		public void reapply(GOut g) {
		    GL gl = g.gl;
		    gl.glUniform1i(g.st.prog.uniform("glowtex"), 0);
		    gl.glUniform1f(g.st.prog.uniform("glowtex_xs"), 1.25f / lsz.x);
		    gl.glUniform1f(g.st.prog.uniform("glowtex_ys"), 1.25f / lsz.y);
		}
	    };
	
	private static final GLState clearblend = new States.AdHoc(null) {
		private final States.ColState col = new States.ColState(new Color(0, 0, 0, 0));
		
		public void apply(GOut g) {
		    GL gl = g.gl;
		    gl.glBlendColor(0.0f, 0.0f, 0.0f, 0.1f);
		    gl.glBlendFunc(GL.GL_CONSTANT_ALPHA, GL.GL_ONE_MINUS_CONSTANT_ALPHA);
		}

		public void unapply(GOut g) {
		    g.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		}
		
		public void prep(Buffer buf) {
		    super.prep(buf);
		    col.prep(buf);
		    Rendered.first.prep(buf);
		}
	    };

	private static Coord res(Coord sz) {
	    return(new Coord(Math.max(1, Tex.nextp2(sz.x) / 2), Math.max(1, Tex.nextp2(sz.y) / 2)));
	}

	private void consfb() {
	    lsz = wnd.sz();
	    if(tgt != null)
		tgt.dispose();
	    if(buf != null)
		buf.dispose();
	    buf = new TexE(res(lsz), GL.GL_RGBA, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE);
	    buf.magfilter(GL.GL_LINEAR);
	    tgt = new FBView(new GLFrameBuffer(buf, null), GLState.compose(proj, cam)) {
		    boolean cleared = false;
		    protected void clear(GOut g) {
			if(!cleared) {
			    super.clear(g);
			    cleared = true;
			} else {
			    g.gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
			}
		    }
		};
	}

	public GlowBuf(PView.RenderState wnd) {
	    this.wnd = wnd;
	    consfb();
	}
	
	public void postsetup(RenderList rl) {
	    rl.prepc(Rendered.last);
	    rl.add(new Rendered.ScreenQuad(true), draw);
	}
	
	public void prerender(RenderList rl, GOut g) {
	    parts.clear();
	    PView.RenderState wnd = null;
	    for(RenderList.Slot s : rl.slots()) {
		if(!s.d)
		    continue;
		GlowEffect glow = s.os.get(slot);
		if(glow != null) {
		    if(wnd == null) {
			wnd = s.os.get(PView.wnd);
			proj.del = s.os.get(PView.proj);
			cam.del = s.os.get(PView.cam);
		    }
		    parts.add(s);
		}
	    }
	    if(!wnd.sz().equals(lsz))
		consfb();
	    g.gl.glBlendFunc(GL.GL_ONE, GL.GL_ZERO);
	    tgt.render(new Rendered() {
		    public void draw(GOut g) {}
		    public boolean setup(RenderList irl) {
			irl.add(new Rendered.ScreenQuad(false), clearblend);
			Buffer buf = new GLState.Buffer(irl.cfg);
			for(RenderList.Slot s : parts) {
			    irl.state().copy(buf);
			    s.os.copy(buf, Slot.Type.GEOM);
			    s.os.get(slot).col.prep(buf);
			    irl.add2(s.r, buf);
			}
			return(false);
		    }
		}, g);
	    g.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
	}
	public void postrender(RenderList rl, GOut g) {}
    }
    
    public Global global(RenderList rl, Buffer ctx) {
	PView.RenderState wnd = ctx.get(PView.wnd);
	synchronized(bufs) {
	    GlowBuf ret = bufs.get(wnd);
	    if(ret == null)
		bufs.put(wnd, ret = new GlowBuf(wnd));
	    return(ret);
	}
    }
    
    public void apply(GOut g) {}
    public void unapply(GOut g) {}

    public void prep(Buffer buf) {
	buf.put(slot, this);
    }
}
