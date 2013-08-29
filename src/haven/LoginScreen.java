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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoginScreen extends Widget {
    Login cur;
    Text error;
    Window log;
    IButton btn;
    static final Text.Furnace textf, texte, textfs;
    static final Tex bg = Resource.loadtex("gfx/loginscr");
    static final Tex cbox = Resource.loadtex("gfx/hud/login/cbox");
    static final Coord cboxc = new Coord((bg.sz().x - cbox.sz().x) / 2, 310);
    Text progress = null;
	
    static {
	textf = new Text.Foundry(new Font("Sans", Font.BOLD, 16), Color.BLACK).aa(true);
	texte = new Text.Foundry(new Font("Sans", Font.BOLD, 18), new Color(255, 0, 0)).aa(true);
	textfs = new Text.Foundry(new Font("Sans", Font.BOLD, 14), Color.BLACK).aa(true);
    }
	
    public LoginScreen(Widget parent) {
	super(parent.sz.div(2).sub(bg.sz().div(2)), bg.sz(), parent);
	setfocustab(true);
	parent.setfocus(this);
	new Img(Coord.z, bg, this);
	new Img(cboxc, cbox, this);
	
	if(Config.isUpdate == true){
	    showChangelog();
	}
    }
    
    private void showChangelog() {
	log = new Window(new Coord(100,50), new Coord(50,50), ui.root, "Changelog");
	log.justclose = true;
	Textlog txt= new Textlog(Coord.z, new Coord(450,500), log);
	txt.quote = false;
	int maxlines = txt.maxLines = 200;
	log.pack();
	try {
	    InputStream in = LoginScreen.class.getResourceAsStream("/changelog.txt");
	    BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
	    File f = Config.getFile("changelog.txt");
	    FileOutputStream out = new FileOutputStream(f);
	    String strLine;
	    int count = 0;
	    while ((count<maxlines)&&(strLine = br.readLine()) != null)   {
		txt.append(strLine);
		out.write((strLine+"\n").getBytes());
		count++;
	    }
	    br.close();
	    out.close();
	    in.close();
	} catch (FileNotFoundException e) {
	} catch (IOException e) {
	}
	txt.setprog(0);
	
	//WikiBrowser.toggle();
    }

    private static abstract class Login extends Widget {
	private Login(Coord c, Coord sz, Widget parent) {
	    super(c, sz, parent);
	}
	
	abstract Object[] data();
	abstract boolean enter();
    }

    private abstract class PwCommon extends Login {
	TextEntry user, pass;
	CheckBox savepass;

	private PwCommon(String username, boolean save) {
	    super(cboxc, cbox.sz(), LoginScreen.this);
	    setfocustab(true);
	    new Img(new Coord(35, 30), textf.render("User name").tex(), this);
	    user = new TextEntry(new Coord(150, 30), new Coord(150, 20), this, username);
	    new Img(new Coord(35, 60), textf.render("Password").tex(), this);
	    pass = new TextEntry(new Coord(150, 60), new Coord(150, 20), this, "");
	    pass.pw = true;
	    savepass = new CheckBox(new Coord(150, 90), this, "Remember me");
	    savepass.a = save;
	    if(user.text.equals(""))
		setfocus(user);
	    else
		setfocus(pass);
	}
	
	public void wdgmsg(Widget sender, String name, Object... args) {
	}
		
	boolean enter() {
	    if(user.text.equals("")) {
		setfocus(user);
		return(false);
	    } else if(pass.text.equals("")) {
		setfocus(pass);
		return(false);
	    } else {
		return(true);
	    }
	}

	public boolean globtype(char k, KeyEvent ev) {
	    if((k == 'r') && ((ev.getModifiersEx() & (KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) != 0)) {
		savepass.set(!savepass.a);
		return(true);
	    }
	    return(false);
	}
    }

    private class Pwbox extends PwCommon {
	private Pwbox(String username, boolean save) {
	    super(username, save);
	    if(Config.regurl != null) {
		final RichText text = RichText.render("If you don't have an account, $col[64,64,255]{$u{register one here}}.", 0, java.awt.font.TextAttribute.FOREGROUND, Color.BLACK);
		new Widget(new Coord(35, 115), text.sz(), this) {
		    public void draw(GOut g) {
			g.image(text.tex(), Coord.z);
		    }

		    public boolean mousedown(Coord c, int btn) {
			if(btn == 1) {
			    Number ul = (Number)text.attrat(c, java.awt.font.TextAttribute.UNDERLINE);
			    if((ul != null) && (ul.intValue() == java.awt.font.TextAttribute.UNDERLINE_ON)) {
				try {
				    WebBrowser.sshow(Config.regurl);
				} catch(WebBrowser.BrowserException e) {
				    error("Could not launch browser");
				}
			    }
			}
			return(true);
		    }
		};
	    }
	}
		
	Object[] data() {
	    return(new Object[] {new AuthClient.NativeCred(user.text, pass.text), savepass.a});
	}
    }
		
    private class Pdxbox extends PwCommon {
	private Pdxbox(String username, boolean save) {
	    super(username, save);
	    }

	Object[] data() {
	    return(new Object[] {new ParadoxCreds(user.text, pass.text), savepass.a});
	    }
	}

    private abstract class WebCommon extends Login {
	private WebCommon() {
	    super(cboxc, cbox.sz(), LoginScreen.this);
	}

	boolean enter() {
	    return(true);
	}
    }

    private class Amazonbox extends WebCommon {
	Object[] data() {
	    return(new Object[] {new BrowserAuth() {
		    public String method() {return("amz");}
		    public String name() {return("Amazon user");}
		}, false});
	}
    }
	
    private class Tokenbox extends Login {
	Text label;
	Button btn;
		
	private Tokenbox(String username) {
	    super(cboxc, cbox.sz(), LoginScreen.this);
	    label = textfs.render("Identity is saved for " + username);
	    btn = new Button(new Coord((sz.x - 100) / 2, 55), 100, this, "Forget me");
	}
		
	Object[] data() {
	    return(new Object[0]);
	}
		
	boolean enter() {
	    return(true);
	}
		
	public void wdgmsg(Widget sender, String name, Object... args) {
	    if(sender == btn) {
		LoginScreen.this.wdgmsg("forget");
		return;
	    }
	    super.wdgmsg(sender, name, args);
	}
		
	public void draw(GOut g) {
	    g.image(label.tex(), new Coord((sz.x - label.sz().x) / 2, 30));
	    super.draw(g);
	}
	
	public boolean globtype(char k, KeyEvent ev) {
	    if((k == 'f') && ((ev.getModifiersEx() & (KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) != 0)) {
		LoginScreen.this.wdgmsg("forget");
		return(true);
	    }
	    return(false);
	}
    }

    static final BufferedImage[] loginb = {
	Resource.loadimg("gfx/hud/buttons/loginu"),
	Resource.loadimg("gfx/hud/buttons/logind"),
	Resource.loadimg("gfx/hud/buttons/loginh"),
    };
    private void mklogin() {
	synchronized(ui) {
	    btn = new IButton(cboxc.add((cbox.sz().x - loginb[0].getWidth()) / 2, 140), this, loginb[0], loginb[1], loginb[2]);
	    progress(null);
	}
    }
	
    private void error(String error) {
	synchronized(ui) {
	    if(this.error != null)
		this.error = null;
	    if(error != null)
		this.error = texte.render(error);
	}
    }
    
    private void progress(String p) {
	synchronized(ui) {
	    if(progress != null)
		progress = null;
	    if(p != null)
		progress = textf.render(p);
	}
    }
    
    private void clear() {
	if(cur != null) {
	    ui.destroy(cur);
	    cur = null;
	    ui.destroy(btn);
	    btn = null;
	}
	progress(null);
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == btn) {
	    if(cur.enter())
		super.wdgmsg("login", cur.data());
	    return;
	}
	super.wdgmsg(sender, msg, args);
    }
	
    public void uimsg(String msg, Object... args) {
	synchronized(ui) {
	    if(msg == "passwd") {
		clear();
		if(Config.authmech.equals("native")) {
		    cur = new Pwbox((String)args[0], (Boolean)args[1]);
		} else if(Config.authmech.equals("paradox")) {
		    cur = new Pdxbox((String)args[0], (Boolean)args[1]);
		} else if(Config.authmech.equals("amz")) {
		    cur = new Amazonbox();
		} else {
		    throw(new RuntimeException("Unknown authmech `" + Config.authmech + "' specified"));
		}
		mklogin();
	    } else if(msg == "token") {
		clear();
		cur = new Tokenbox((String)args[0]);
		mklogin();
	    } else if(msg == "error") {
		error((String)args[0]);
	    } else if(msg == "prg") {
		error(null);
		clear();
		progress((String)args[0]);
	    }
	}
    }
    
    public void presize() {
	c = parent.sz.div(2).sub(sz.div(2));
    }
	
    public void draw(GOut g) {
	super.draw(g);
	if(error != null) {
	    Coord c = new Coord((sz.x - error.sz().x) / 2, 290);
	    g.chcolor(0, 0, 0, 224);
	    g.frect(c.sub(4, 2), error.sz().add(8, 4));
	    g.chcolor();
	    g.image(error.tex(), c);
	}
	if(progress != null)
	    g.image(progress.tex(), new Coord((sz.x - progress.sz().x) / 2, cboxc.y + ((cbox.sz().y - progress.sz().y) / 2)));
    }
	
    public boolean type(char k, KeyEvent ev) {
	if(k == 10) {
	    if((cur != null) && cur.enter())
		wdgmsg("login", cur.data());
	    return(true);
	}
	return(super.type(k, ev));
    }

    @Override
    public void destroy() {
	if(log != null){
	    ui.destroy(log);
	    log = null;
	}
	super.destroy();
    }
}
