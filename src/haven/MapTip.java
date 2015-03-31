import haven.Coord;
import haven.ItemInfo.Owner;
import haven.ItemInfo.Tip;
import haven.PUtils;
import haven.PUtils.Convolution;
import haven.PUtils.Lanczos;
import haven.TexI;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class MapTip extends ItemInfo.Tip
{
    public static final PUtils.Convolution filter = new PUtils.Lanczos(2.0D);
    public final BufferedImage img;
    public final Coord loc;

    public MapTip(ItemInfo.Owner paramOwner, BufferedImage paramBufferedImage, Coord paramCoord)
    {
	super(paramOwner);
	this.img = paramBufferedImage;
	this.loc = paramCoord;
    }

    public BufferedImage longtip() {
	BufferedImage localBufferedImage1 = this.img;
	Coord localCoord = PUtils.imgsz(localBufferedImage1);
	if (localCoord.y > 128) {
	    localCoord = new Coord(128 * localCoord.x / localCoord.y, 128);
	    localBufferedImage1 = PUtils.convolvedown(localBufferedImage1, localCoord, filter);
	}
	BufferedImage localBufferedImage2 = TexI.mkbuf(localCoord.add(10, 20));
	Graphics localGraphics = localBufferedImage2.getGraphics();
	localGraphics.drawImage(localBufferedImage1, 10, 10, null);
	localGraphics.drawImage(imagedx, 10+loc.x, 10+loc.y, null);
	localGraphics.dispose();
	return localBufferedImage2;
    }
}