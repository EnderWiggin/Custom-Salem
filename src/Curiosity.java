import java.awt.image.BufferedImage;

import haven.CharWnd;
import haven.GItem;
import haven.GItem.Info;
import haven.GItem.InfoFactory;
import haven.RichText;
import haven.Text;


public class Curiosity implements InfoFactory {

    public Curiosity() {
    }

    @Override
    public Info build(GItem item, Object... args) {
	final String[] attrs = new String[args.length / 2];
	final int[] exp = new int[args.length / 2];
	int i = 1; for (int j = 0; i < args.length; j++) {
	    attrs[j] = ((String)args[i]);
	    exp[j] = ((Integer)args[(i + 1)]).intValue();
	    i += 2;
	}

	final int[] order = CharWnd.sortattrs(attrs);
	return item.new Tip() {
	    public BufferedImage longtip() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < attrs.length; i++) {
		    if (i > 0)
			sb.append('\n');
		    String attr = CharWnd.attrnm.get(attrs[order[i]]);
		    sb.append(String.format("%s: %d",  attr, exp[order[i]] ));
		}
		return RichText.stdf.render(sb.toString(), 0).img;
	    }
	};
    }

}
