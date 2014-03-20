import haven.Coord;
import haven.GobIcon;
import haven.Indir;
import haven.ItemInfo;
import haven.PUtils;
import haven.Resource;
import haven.RichText;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Undebuff
implements ItemInfo.InfoFactory
{
    public static final Color undebuff = new Color(192, 255, 192);
    public ItemInfo build(ItemInfo.Owner owner, Object... params)
    {
	final Indir<Resource> res = owner.glob().sess.getres(((Integer)params[1]).intValue());
	final double m = ((Integer)params[2]).intValue() / 100.0D;
	return new ItemInfo.Tip(owner) {
	    public BufferedImage longtip() {
		int val = (int)Math.round(100.0D * m);
		int i = 16;
		BufferedImage head = RichText.render(String.format("+%d%%", val), undebuff).img;
		BufferedImage icon = PUtils.convolvedown(res.get().layer(Resource.imgc).img, new Coord(i, i), GobIcon.filter);
		String name = res.get().layer(Resource.tooltip).t;
		BufferedImage tail = RichText.render(name).img;
		return catimgsh(3, new BufferedImage[] { head, icon, tail });
	    }
	};
    }
}