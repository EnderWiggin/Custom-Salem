import haven.*;

public class Undebuff
implements ItemInfo.InfoFactory
{
    public ItemInfo build(ItemInfo.Owner owner, final Object... params)
    {
	final double m = (Integer) params[2] / 100.0D;
	Indir<Resource> res = owner.glob().sess.getres((Integer) params[1]);
	int val = (int)Math.round(100.0D * m);
	return new GobbleEventInfo(owner,val, res);
    }
}