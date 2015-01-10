package haven;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GobbleEventInfo extends ItemInfo.Tip {
    public static final Color undebuff = new Color(192, 255, 192);
    public static final Color debuff = new Color(255, 192, 192);

    public int value;
    public Indir<Resource> res;

    public GobbleEventInfo(Owner owner, int value, Indir<Resource> res) {
	super(owner);
	this.value = value;
	this.res = res;
    }

    @Override
    public BufferedImage longtip() {
	int i = 16;
	BufferedImage head = getHead();
	BufferedImage icon = PUtils.convolvedown(res.get().layer(Resource.imgc).img, new Coord(i, i), GobIcon.filter);
	String name = res.get().layer(Resource.tooltip).t;
	BufferedImage tail = RichText.render(name).img;
	return catimgsh(3, head, icon, tail);
    }

    private BufferedImage getHead() {
	String format = value < 0?"%d%%":"+%d%%";
	Color color = value < 0?debuff:undebuff;
	return RichText.render(String.format(format, value), color).img;
    }
}
