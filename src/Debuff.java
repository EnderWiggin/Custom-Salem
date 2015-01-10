import haven.*;

public class Debuff
implements ItemInfo.InfoFactory
{
    public ItemInfo build(ItemInfo.Owner owner, final Object... params)
    {
	final double m = ((Integer) params[2] / 100.0D);
	int val = (int)Math.round(100.0D * (m - 1.0D));
	Indir<Resource> res = owner.glob().sess.getres((Integer) params[1]);
	return new GobbleEventInfo(owner, val, res);
    }
}