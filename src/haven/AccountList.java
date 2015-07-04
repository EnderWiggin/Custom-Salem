package haven;

import java.util.*;

import static haven.Utils.setpref;

public class AccountList extends Widget {
    private static final Coord SZ = new Coord(230, 30);
    private static final Comparator<Account> accountComparator = new Comparator<Account>() {
	@Override
	public int compare(Account o1, Account o2) {
	    return o1.name.compareTo(o2.name);
	}
    };

    public int height, y;
    public final List<Account> accounts = new ArrayList<Account>();

    public static class Account {
	public String name, token;
	Button plb, del;

	public Account(String name, String token) {
	    this.name = name;
	    this.token = token;
	}
    }

    public AccountList(Coord c, Widget parent, int height) {
	super(c, new Coord(SZ.x, SZ.y * height), parent);
	this.height = height;
	y = 0;

	for(Map.Entry<String, String> entry :Config.accounts.entrySet()){
	    add(entry.getKey(), entry.getValue());
	}
	Collections.sort(accounts, accountComparator);

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
	Coord cc = new Coord(5, 5);
	synchronized (accounts) {
	    for (Account account : accounts) {
		account.plb.hide();
		account.del.hide();
	    }
	    for (int i = 0; (i < height) && (i + this.y < accounts.size()); i++) {
		Account account = accounts.get(i + this.y);
		account.plb.show();
		account.plb.c = cc;
		account.del.show();
		account.del.c = cc.add(account.plb.sz.x + 5, 0);
		cc = cc.add(0, SZ.y);
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
			break;
		    } else if(sender == account.del){
			remove(account);
			break;
		    }
		}
	    }
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }

    public void add(String name, String token){
	Account c = new Account(name, token);
	c.plb = new Button(Coord.z, 200, this, name);
	c.plb.hide();
	c.del = new Button(Coord.z, 20, this, "X");
	c.del.hide();
	synchronized(accounts) {
	    accounts.add(c);
	}
    }

    public void remove(Account account) {
	synchronized(accounts) {
	    accounts.remove(account);
	}
	scroll(0);
	Config.removeAccount(account.name);
	ui.destroy(account.plb);
	ui.destroy(account.del);
    }
}