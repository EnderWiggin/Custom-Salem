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

import java.util.*;
import java.awt.Color;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import org.w3c.dom.*;
import java.io.*;
import java.net.*;

public class Screenshooter extends Window {
    public static final ComponentColorModel outcm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
    public final URL tgt;
    public final Shot shot;
    private final TextEntry comment;
    private final CheckBox decobox, pub;
    private final int w, h;
    private Label prog;
    private Label progSave;
    private Coord btnc, btns;
    private Button btn, btnSave;
    
    public Screenshooter(Coord c, Widget parent, URL tgt, Shot shot) {
	super(c, Coord.z, parent, "Screenshot");
	this.tgt = tgt;
	this.shot = shot;
	this.w = Math.min(200 * shot.sz.x / shot.sz.y, 150);
	this.h = w * shot.sz.y / shot.sz.x;
	this.decobox = new CheckBox(new Coord(w, (h - CheckBox.box.sz().y) / 2), this, "Include interface");
	this.decobox.a = Config.ss_ui;
	Label clbl = new Label(new Coord(0, h + 5), this, "If you wish, leave a comment:");
	this.comment = new TextEntry(new Coord(0, clbl.c.y + clbl.sz.y + 5), w + 130, this, "") {
		public void activate(String text) {
		    upload();
		}
	    };
	this.pub = new CheckBox(new Coord(0, comment.c.y + comment.sz.y + 5), this, "Make public");
	pub.a = true;
	btnc = new Coord((comment.sz.x - 125) / 2, pub.c.y + pub.sz.y + 20);
	btns = btnc.add(130, 0);
	btn = new Button(btnc, 125, this, "Upload") {
		public void click() {
		    upload();
		}
	    };
	btnSave = new Button(btns, 125, this, "Save"){
	    public void click(){
		save();
	    }
	};
	pack();
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if((sender == this) && (msg == "close")) {
	    ui.destroy(this);
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
    
    public void cdraw(GOut g) {
	TexI tex = this.decobox.a?shot.ui:shot.map;
	g.image(tex, Coord.z, new Coord(w, h));
    }
    
    public static class Shot {
	public final TexI map, ui;
	public final Coord sz;
	public String comment;
	public boolean fsaa, fl, sdw;

	public Shot(TexI map, TexI ui) {
	    this.map = map;
	    this.ui = ui;
	    this.sz = map.sz();
	}
    }

    public static interface ImageFormat {
	public String ctype();
	public void write(OutputStream out, BufferedImage img, Shot info) throws IOException;
    }

    public static final ImageFormat png = new ImageFormat() {
	    public String ctype() {return("image/png");}

	    void cmt(Node tlist, String key, String val) {
		Element cmt = new IIOMetadataNode("TextEntry");
		cmt.setAttribute("keyword", key);
		cmt.setAttribute("value", val);
		cmt.setAttribute("encoding", "utf-8");
		cmt.setAttribute("language", "");
		cmt.setAttribute("compression", "none");
		tlist.appendChild(cmt);
	    }

	    public void write(OutputStream out, BufferedImage img, Shot info) throws IOException {
		ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(img);
		ImageWriter wr = ImageIO.getImageWriters(type, "PNG").next();
		IIOMetadata dat = wr.getDefaultImageMetadata(type, null);
		Node root = dat.getAsTree("javax_imageio_1.0");
		Node tlist = new IIOMetadataNode("Text");
		if(info.comment != null)
		    cmt(tlist, "Comment", info.comment);
		cmt(tlist, "haven.fsaa", info.fsaa?"y":"n");
		cmt(tlist, "haven.flight", info.fl?"y":"n");
		cmt(tlist, "haven.sdw", info.sdw?"y":"n");
		cmt(tlist, "haven.conf", Config.confid);
		root.appendChild(tlist);
		dat.setFromTree("javax_imageio_1.0", root);
		ImageOutputStream iout = ImageIO.createImageOutputStream(out);
		wr.setOutput(iout);
		wr.write(new IIOImage(img, null, dat));
	    }
	};

    public static final ImageFormat jpeg = new ImageFormat() {
	    public String ctype() {return("image/jpeg");}

	    public void write(OutputStream out, BufferedImage img, Shot info) throws IOException {
		ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(img);
		ImageWriter wr = ImageIO.getImageWriters(type, "JPEG").next();
		IIOMetadata dat = wr.getDefaultImageMetadata(type, null);
		
		Node root = dat.getAsTree("javax_imageio_jpeg_image_1.0");
		Node mseq;
		for(mseq = root.getFirstChild(); (mseq != null) && !mseq.getLocalName().equals("markerSequence"); mseq = mseq.getNextSibling());
		if(mseq == null) {
		    mseq = new IIOMetadataNode("markerSequence");
		    root.appendChild(mseq);
		}
		if(info.comment != null) {
		    IIOMetadataNode cmt = new IIOMetadataNode("com");
		    cmt.setUserObject(info.comment.getBytes("utf-8"));
		    mseq.appendChild(cmt);
		}

		Message hdat = new Message(0);
		hdat.addstring2("HSSI1");
		hdat.addstring("fsaa");
		hdat.addstring(info.fsaa?"y":"n");
		hdat.addstring("flight");
		hdat.addstring(info.fl?"y":"n");
		hdat.addstring("sdw");
		hdat.addstring(info.sdw?"y":"n");
		hdat.addstring("conf");
		hdat.addstring(Config.confid);
		IIOMetadataNode app4 = new IIOMetadataNode("unknown");
		app4.setAttribute("MarkerTag", "228");
		app4.setUserObject(hdat.blob);
		mseq.appendChild(app4);
		
		dat.setFromTree("javax_imageio_jpeg_image_1.0", root);
		
		ImageOutputStream iout = ImageIO.createImageOutputStream(out);
		wr.setOutput(iout);
		wr.write(new IIOImage(img, null, dat));
	    }
	};

    public class Uploader extends HackThread {
	private final TexI img;
	private final Shot info;
	private final ImageFormat fmt;
	
	public Uploader(TexI img, Shot info, ImageFormat fmt) {
	    super("Screenshot uploader");
	    this.img = img;
	    this.info = info;
	    this.fmt = fmt;
	}
	
	public void run() {
	    try {
		upload(img, info, fmt);
	    } catch(InterruptedIOException e) {
		setstate("Cancelled");
		synchronized(ui) {
		    ui.destroy(btn);
		    btn = new Button(btnc, 125, Screenshooter.this, "Retry") {
			    public void click() {
				Screenshooter.this.upload();
			    }
			};
		}
	    } catch(IOException e) {
		setstate("Could not upload image");
		synchronized(ui) {
		    ui.destroy(btn);
		    btn = new Button(btnc, 125, Screenshooter.this, "Retry") {
			    public void click() {
				Screenshooter.this.upload();
			    }
			};
		}
	    }
	}

	private void setstate(String t) {
	    synchronized(ui) {
		if(prog != null)
		    ui.destroy(prog);
		prog = new Label(btnc.sub(0, 15), Screenshooter.this, t);
	    }
	}

	private BufferedImage convert(BufferedImage img) {
	    WritableRaster buf = PUtils.byteraster(PUtils.imgsz(img), 3);
	    BufferedImage ret = new BufferedImage(outcm, buf, false, null);
	    java.awt.Graphics g = ret.getGraphics();
	    g.drawImage(img, 0, 0, null);
	    g.dispose();
	    return(ret);
	}

	public void upload(TexI ss, Shot info, ImageFormat fmt) throws IOException {
	    setstate("Preparing image...");
	    ByteArrayOutputStream buf = new ByteArrayOutputStream();
	    
	    fmt.write(buf, convert(ss.back), info);
	    
	    byte[] data = buf.toByteArray();
	    buf = null;
	    setstate("Connecting...");
	    URL pared = Utils.urlparam(tgt, "p", pub.a?"y":"n");
	    HttpURLConnection conn = (HttpURLConnection)pared.openConnection();
	    conn.setDoOutput(true);
	    conn.setFixedLengthStreamingMode(data.length);
	    conn.addRequestProperty("Content-Type", fmt.ctype());
	    Message auth = new Message(0);
	    auth.addstring2(ui.sess.username + "/");
	    auth.addbytes(ui.sess.sesskey);
	    conn.addRequestProperty("Authorization", "Haven " + Utils.base64enc(auth.blob));
	    conn.connect();
	    OutputStream out = conn.getOutputStream();
	    try {
		int off = 0;
		while(off < data.length) {
		    setstate(String.format("Uploading (%d%%)...", (off * 100) / data.length));
		    int len = Math.min(1024, data.length - off);
		    out.write(data, off, len);
		    off += len;
		}
	    } finally {
		out.close();
	    }
	    setstate("Awaiting response...");
	    InputStream in = conn.getInputStream();
	    final URL result;
	    try {
		if(!conn.getContentType().equals("text/x-target-url"))
		    throw(new IOException("Unexpected type of reply from server"));
		byte[] b = Utils.readall(in);
		try {
		    result = new URL(new String(b, "utf-8"));
		} catch(MalformedURLException e) {
		    throw((IOException)new IOException("Unexpected reply from server").initCause(e));
		}
	    } finally {
		in.close();
	    }
	    setstate("Done");
	    synchronized(ui) {
		ui.destroy(btn);
		btn = new Button(btnc, 125, Screenshooter.this, "Open in browser") {
			public void click() {
			    if(WebBrowser.self != null)
				WebBrowser.self.show(result);
			}
		    };
	    }
	}
    }
    
    public void upload() {
	shot.comment = comment.text;
	final Uploader th = new Uploader(decobox.a?shot.ui:shot.map, shot, Config.ss_compress?jpeg:png);
	th.start();
	ui.destroy(btn);
	btn = new Button(btnc, 125, this, "Cancel") {
		public void click() {
		    th.interrupt();
		}
	    };
    }
    
    public class Saver extends HackThread {
	private final TexI img;
	private final Shot info;
	private final ImageFormat fmt;
	
	public Saver(TexI img, Shot info, ImageFormat fmt) {
	    super("Screenshot saver");
	    this.img = img;
	    this.info = info;
	    this.fmt = fmt;
	}
	
	public void run() {
	    try {
		save(img, info, fmt);
	    } catch(IOException e) {
		setstate("Could not save image");
		synchronized(ui) {
		    ui.destroy(btn);
		    btn = new Button(btns, 125, Screenshooter.this, "Retry") {
			    public void click() {
				Screenshooter.this.save();
			    }
			};
		}
	    }
	}

	private void setstate(String t) {
	    synchronized(ui) {
		if(progSave != null)
		    ui.destroy(progSave);
		progSave = new Label(btns.sub(0, 15), Screenshooter.this, t);
	    }
	}

	private BufferedImage convert(BufferedImage img) {
	    WritableRaster buf = PUtils.byteraster(PUtils.imgsz(img), 3);
	    BufferedImage ret = new BufferedImage(outcm, buf, false, null);
	    java.awt.Graphics g = ret.getGraphics();
	    g.drawImage(img, 0, 0, null);
	    g.dispose();
	    return(ret);
	}

	public void save(TexI ss, Shot info, ImageFormat fmt) throws IOException {
	    setstate("Preparing image...");
	    ByteArrayOutputStream buf = new ByteArrayOutputStream();
	    
	    fmt.write(buf, convert(ss.back), info);
	    
	    byte[] data = buf.toByteArray();
	    buf = null;
	    
	    File ssfolder = Config.getFile("screenshots");
	    if(!ssfolder.exists()){
		ssfolder.mkdirs();
	    }
	    String fname = String.format("shot_%s_%d.%s", 
		    Utils.current_date(), 
		    System.currentTimeMillis(), 
		    Config.ss_compress?"jpeg":"png");
	    File f = new File(ssfolder, fname );
	    FileOutputStream fos = new FileOutputStream(f);
	    fos.write(data);
	    fos.close();
	    setstate("Done");
	    final URL result = f.toURI().toURL();
	    synchronized(ui) {
		ui.destroy(btnSave);
		btnSave = new Button(btns, 125, Screenshooter.this, "Open") {
		    public void click() {
			if(WebBrowser.self != null)
			    WebBrowser.self.show(result);
		    }
		};
		ui.message("Screenshot saved");
	    }
	}
    }
    
    protected void save() {
	final Saver th = new Saver(decobox.a?shot.ui:shot.map, shot, Config.ss_compress?jpeg:png);
	th.start();
	ui.destroy(btnSave);
    }

    public static void take(final GameUI gameui, final URL tgt) {
	if(gameui == null){return;}
	new Object() {
	    TexI[] ss = {null, null};
	    {
		gameui.map.delay2(new MapView.Delayed() {
			public void run(GOut g) {
			    ss[0] = new TexI(g.getimage(Coord.z, g.sz));
			    ss[0].minfilter = javax.media.opengl.GL.GL_LINEAR;
			    checkcomplete(g);
			}
		    });
		gameui.ui.drawaftertt(new UI.AfterDraw() {
			public void draw(GOut g) {
			    ss[1] = new TexI(g.getimage(Coord.z, g.sz));
			    checkcomplete(g);
			}
		    });
	    }
	    
	    private void checkcomplete(GOut g) {
		if((ss[0] != null) && (ss[1] != null)) {
		    Shot shot = new Shot(ss[0], ss[1]);
		    shot.fl = g.gc.pref.flight.val;
		    shot.sdw = g.gc.pref.lshadow.val;
		    shot.fsaa = g.gc.pref.fsaa.val;
		    Screenshooter s = new Screenshooter(new Coord(100, 100), gameui, tgt, shot);
		    if(Config.ss_silent){
			s.visible = false;
			s.save();
			s.wdgmsg(s, "close", (Object[])null);
		    }
		}
	    }
	};
    }
}
