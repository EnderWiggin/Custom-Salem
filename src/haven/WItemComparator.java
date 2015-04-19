package haven;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WItemComparator implements Comparator<WItem> {
    static final Pattern count_patt = Pattern.compile("([0-9]*\\.?[0-9]+)");

    @Override
    public int compare(WItem o1, WItem o2) {
	int carats = carats(o1, o2);

	if(carats == 0) {
	    int alchemy = alchemy(o1, o2);
	    if(alchemy == 0){
		return number(o1, o2);
	    } else {
		return alchemy;
	    }
	} else {
	    return carats;
	}
    }

    public int alchemy(WItem o1, WItem o2) {
	Alchemy a = o1.alch.get();
	double q1 = (a == null) ? 0 : a.purity();

	a = o2.alch.get();
	double q2 = (a == null) ? 0 : a.purity();

	if(q1 == q2) {
	    return 0;
	} else if(q1 > q2) {
	    return 1;
	} else {
	    return -1;
	}
    }

    private int carats(WItem o1, WItem o2) {
	float c1 = (o1 != null && o1.carats != null)?o1.carats.get():0;
	float c2 = (o2 != null && o2.carats != null)?o2.carats.get():0;

	if(c1 > c2){
	    return 1;
	} else if(c2 > c1){
	    return -1;
	} else {
	    return 0;
	}
    }

    private int number(WItem o1, WItem o2) {
	float n1 = getCount(o1);
	float n2 = getCount(o2);
	if(n1 > n2){
	    return 1;
	} else if(n2 > n1){
	    return -1;
	} else {
	    return 0;
	}
    }

    private float getCount(WItem wItem) {
	float num = wItem.item.num;
	try {
	    if (num < 0) {
		GItem.NumberInfo ninf = ItemInfo.find(GItem.NumberInfo.class, wItem.item.info());
		if (ninf != null) {
		    num = ninf.itemnum();
		} else {
		    String snum = ItemInfo.getCount(wItem.item.info());
		    if (snum != null) {
			Matcher m = count_patt.matcher(snum);
			if (m.find()) {
			    num = Float.parseFloat(m.group(1));
			}
		    }
		}
	    }
	} catch(Exception ignore){}
	return num;
    }
}
