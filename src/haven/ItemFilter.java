package haven;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static haven.Tempers.anm;
import static haven.Tempers.rnm;

public abstract class ItemFilter {
    private static final Pattern q = Pattern.compile("(?:(?<tag>\\w+):)?(?<text>[\\w\\*]+)(?:(?<sign>[<>=+~])(?<value>\\d+(?:\\.\\d+)?)?(?<opt>[<>=+~])?)?");
    private static final Pattern float_p = Pattern.compile("(\\d+(?:\\.\\d+)?)");
    public boolean matches(List<ItemInfo> info){
	for(ItemInfo item : info){
	    if(item instanceof ItemInfo.Name){
		if(match((ItemInfo.Name) item)){ return true;}
	    } else if(item instanceof FoodInfo){
		if(match((FoodInfo)item)){return true;}
	    } else if(item instanceof Inspiration){
		if(match((Inspiration)item)){return true;}
	    } else if(item instanceof ItemInfo.Contents){
		if(match((ItemInfo.Contents)item)){return true;}
	    } else if(item instanceof Alchemy){
		if(match((Alchemy)item)){return true;}
	    } else if(item instanceof GobbleInfo){
		if(match((GobbleInfo)item)){return true;}
	    }
	}
	return false;
    }

    protected boolean match(ItemInfo.Contents item) {
	return false;
    }

    protected boolean match(Alchemy item) {
	return false;
    }

    protected boolean match(Inspiration item) {
	return false;
    }

    protected boolean match(GobbleInfo item) {
	return false;
    }

    protected boolean match(FoodInfo item) {
	return false;
    }

    protected boolean match(ItemInfo.Name item) {
	return false;
    }

    public static ItemFilter create(String query){
	Compound result = new Compound();
	Matcher m = q.matcher(query);
	while(m.find()){
	    String tag = m.group("tag");
	    String text = m.group("text").toLowerCase();
	    String sign = m.group("sign");
	    String value = m.group("value");
	    String opt = m.group("opt");

	    ItemFilter filter = null;
	    if(tag == null){
		if(sign != null && text.equals("q")){
		    filter = new Alch(Alchemy.names[0], sign, value, opt);
		} else {
		    filter = new Text(text, false);
		}
	    } else {
		tag = tag.toLowerCase();
		if(tag.equals("heal")){
		    filter = new Heal(text, sign, value, opt);
		} else if(tag.equals("gob")){
		    filter = new Gobble(text, sign, value, opt);
		} else if(tag.equals("txt")){
		    filter = new Text(text, true);
		} else if(tag.equals("xp")){
		    filter = new XP(text, sign, value, opt);
		} else if(tag.equals("has")){
		    filter = new Has(text, sign, value, opt);
		} else if(tag.equals("alch")){
		    filter = new Alch(text, sign, value, opt);
		}
	    }
	    if(filter != null){
		result.add(filter);
	    }
	}
	return result;
    }

    public static class Compound extends ItemFilter{
	List<ItemFilter> filters = new LinkedList<ItemFilter>();
	@Override
	public boolean matches(List<ItemInfo> info) {
	    if(filters.isEmpty()){return false;}
	    for(ItemFilter filter : filters){
		if(!filter.matches(info)){return false;}
	    }
	    return true;
	}

	public void add(ItemFilter filter){
	    filters.add(filter);
	}
    }

    public static class Complex extends ItemFilter{
	protected final String text;
	protected final Sign sign;
	protected final Sign opts;
	protected float value;
	protected final boolean all;
	protected final boolean any;

	public Complex(String text, String sign, String value, String opts){
	    this.text = text.toLowerCase();
	    this.sign = getSign(sign);
	    this.opts = getSign(opts);
	    float tmp = 0;
	    try {
		tmp = Float.parseFloat(value);
	    } catch (Exception ignored){}
	    this.value = tmp;

	    all = text.equals("*") || text.equals("all");
	    any = text.equals("any");
	}

	protected boolean test(double actual, double target){
	    switch(sign){
		case GREATER	: return actual >  target;
		case LESS	: return actual <= target;
		case EQUAL	: return actual == target;
		case GREQUAL	: return actual >= target;
		default		: return actual >  0;
	    }
	}

	protected Sign getSign(String sign){
	    if(sign == null){
		return getaDefaultSign();
	    }
	    if(sign.equals(">")){
		return Sign.GREATER;
	    } else if(sign.equals("<")){
		return Sign.LESS;
	    } else if(sign.equals("=")) {
		return Sign.EQUAL;
	    } else if(sign.equals("+")) {
		return Sign.GREQUAL;
	    } else if(sign.equals("~")) {
		return Sign.WAVE;
	    } else {
		return getaDefaultSign();
	    }
	}

	protected Sign getaDefaultSign() {
	    return Sign.DEFAULT;
	}

	public static enum Sign {GREATER, LESS, EQUAL, GREQUAL, WAVE, DEFAULT}
    }

    public static class Heal extends Complex{

	public Heal(String text, String sign, String value, String opts) {
	    super(text, sign, value, opts);
	    this.value = 1000*this.value;
	}

	@Override
	protected boolean match(FoodInfo item) {
	    int[] tempers = item.tempers;
	    if(all){
		for(int k = 0; k < anm.length; k++){
		    if(!test(tempers[k], value)){return false;}
		}
	    } else {
		for(int k = 0; k < anm.length; k++){
		    boolean enough = test(tempers[k], value);
		    if((any || anm[k].equals(text)) && enough){return true;}
		    if((any || rnm[k].toLowerCase().contains(text)) && enough){return true;}
		}
	    }
	    return false;
	}
    }

    public static class Gobble extends Complex {

	public Gobble(String text, String sign, String value, String opts) {
	    super(text, sign, value, opts);
	    this.value = 1000*this.value;
	}

	@Override
	protected boolean match(GobbleInfo item) {
	    if (all) {
		for (int k = 0; k < anm.length; k++) {
		    if (!test(getBile(item, k), value)) {
			return false;
		    }
		}
	    } else {
		for (int k = 0; k < anm.length; k++) {
		    boolean enough = test(getBile(item, k), value);
		    if ((any || anm[k].equals(text)) && enough) {
			return true;
		    }
		    if ((any || rnm[k].toLowerCase().contains(text)) && enough) {
			return true;
		    }
		}
	    }
	    return false;
	}

	private int getBile(GobbleInfo item, int k) {
	    int result;
	    switch (opts) {
		case EQUAL:
		    result = (item.h[k] + item.l[k]) / 2;
		    break;
		case LESS:
		    result = item.l[k];
		    break;
		case GREATER:
		default:
		    result = item.h[k];
	    }
	    return 100*(result/100);
	}
    }

    public  static class Has extends Complex {
	public Has(String text, String sign, String value, String opts) {
	    super(text, sign, value, opts);
	}

	@Override
	protected boolean match(ItemInfo.Contents item) {
	    String name = this.name(item.sub).toLowerCase();
	    float num = count(name);
	    return name.contains(text) && test(num, value);
	}

	@Override
	protected Sign getaDefaultSign() {
	    return Sign.GREQUAL;
	}

	private float count(String txt){
	    float n = 0;
	    if (txt != null) {
		try {
		    Matcher matcher = float_p.matcher(txt);
		    if(matcher.find()) {
			n = Float.parseFloat(matcher.group(1));
		    }
		} catch (Exception ignored){}
	    }
	    return n;
	}

	private String name(List<ItemInfo> sub) {
	    String txt = null;
	    for(ItemInfo subInfo : sub){
		if(subInfo instanceof ItemInfo.Name){
		    ItemInfo.Name name = (ItemInfo.Name) subInfo;
		    txt = name.str.text;
		}
	    }
	    return txt;
	}
    }

    public static class XP extends Complex {

	public XP(String text, String sign, String value, String opts) {
	    super(text, sign, value, opts);
	}

	@Override
	protected boolean match(Inspiration item) {
	    for(int k = 0; k<item.attrs.length; k++){
		boolean enough = test(item.exp[k], value);
		if((any || item.attrs[k].equals(text)) && enough){return true;}
		if((any || CharWnd.attrnm.get(item.attrs[k]).toLowerCase().contains(text)) && enough){return true;}
	    }
	    return false;
	}
    }

    public static class Alch extends Complex {

	public Alch(String text, String sign, String value, String opts) {
	    super(text, sign, value, opts);
	    this.value = (int)(100*this.value);
	}

	@Override
	protected boolean match(Alchemy item) {
	    for(int k = 0; k<item.a.length; k++){
		boolean enough = test((int)(10000*item.a[k]), value);
		if((any || Alchemy.names[k].toLowerCase().equals(text)) && enough){return true;}
	    }
	    return false;
	}
    }

    public static class Text extends ItemFilter{
	private String text;
	private final boolean full;

	public  Text(String text, boolean full){
	    this.full = full;
	    this.text = text.toLowerCase();
	}

	public void update(String text){
	    this.text = text.toLowerCase();
	}

	@Override
	protected boolean match(ItemInfo.Name item) {
	    return item.str.text.toLowerCase().contains(text);
	}

	@Override
	protected boolean match(FoodInfo item) {
	    if(!full){return false;}
	    for(int k = 0; k < anm.length; k++){
		boolean notEmpty = item.tempers[k] > 0;
		if(anm[k].equals(text) && notEmpty){return true;}
		if(rnm[k].toLowerCase().contains(text) && notEmpty){return true;}
	    }
	    return false;
	}

	@Override
	protected boolean match(GobbleInfo item) {
	    if(!full){return false;}
	    for(int k = 0; k < anm.length; k++){
		if(anm[k].equals(text) && item.h[k] > 0){return true;}
	    }
	    for(int k = 0; k < rnm.length; k++){
		if(rnm[k].toLowerCase().contains(text) && item.h[k] > 0){return true;}
	    }
	    return false;
	}

	@Override
	protected boolean match(Inspiration item) {
	    if(!full){return false;}
	    for(String attr : item.attrs){
		if(attr.equals(text)){return true;}
		if(CharWnd.attrnm.get(attr).toLowerCase().contains(text)){return true;}
	    }
	    return false;
	}
    }
}
