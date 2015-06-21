package haven;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static haven.Utils.setpref;

public class Accountlist extends Widget {
    private static final Coord SZ = new Coord(350, 55);
    public static final int margin = 1;
    public static final int bmargin = 46;

    public int height, y;
    public IButton sau, sad;
    public final List<Account> accounts = new ArrayList<Account>();

    public static class Account {
	static Text.Furnace tf = new Text.Imager(new Text.Foundry(new java.awt.Font("Serif", java.awt.Font.PLAIN, 20), java.awt.Color.WHITE).aa(true)) {
	    protected BufferedImage proc(Text text) {
		return(PUtils.rasterimg(PUtils.blurmask2(text.img.getRaster(), 1, 1, java.awt.Color.BLACK)));
	    }
	};
	public String name, token;
	Text nt;
	Button plb;

	public Account(String name, String token) {
	    this.name = name;
	    this.token = token;
	    nt = tf.render(name);
	}
    }

    public Accountlist(Coord c, Widget parent, int height) {
	super(c, new Coord(SZ.x, (bmargin * 2) + (SZ.y * height) + (margin * (height - 1))), parent);
	this.height = height;
	y = 0;

	for(Map.Entry<String, String> entry :Config.accounts.entrySet()){
	    add(entry.getKey(), entry.getValue());
	}

    }

    public void scroll(int amount) {
	y += amount;
	synchronized(accounts) {
	    if(y > accounts.size() - height)
		y = accounts.size() - height;
	}
	if(y < 0)
	    y = 0;
    }

    public void draw(GOut g) {
	Coord cc = new Coord(5, 0);
	synchronized(accounts) {
	    for(Account c : accounts) {
		c.plb.hide();
	    }
	    for(int i = 0; (i < height) && (i + this.y < accounts.size()); i++) {
		Account a = accounts.get(i + this.y);
		a.plb.show();
		a.plb.c = cc.add(0, SZ.y -25);
		g.image(a.nt.tex(), cc);
		cc = cc.add(0, SZ.y + margin);
	    }
	}
	super.draw(g);
    }

    public boolean mousewheel(Coord c, int amount) {
	scroll(amount);
	return(true);
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender instanceof Button) {
	    synchronized(accounts) {
		for(Account account : accounts) {
		    if(sender == account.plb){
			setpref("savedtoken", account.token);
			setpref("tokenname", account.name);
			setpref("tokenname", account.name);
			super.wdgmsg("account", account.name, account.token);
		    }
		}
	    }
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }

    public void add(String name, String token){
	Account c = new Account(name, token);
	c.plb = new Button(new Coord(0, 0), 50, this, "Log in");
	c.plb.hide();
	synchronized(accounts) {
	    accounts.add(c);
	    if(accounts.size() > height) {
		sau.show();
		sad.show();
	    }
	}
    }
}