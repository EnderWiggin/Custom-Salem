package haven;

import java.util.List;

import static haven.Tempers.anm;
import static haven.Tempers.rnm;

public abstract class ItemFilter {
    public boolean matches(List<ItemInfo> info){
	for(ItemInfo item : info){
	    String className = item.getClass().getCanonicalName();
	    if(item instanceof ItemInfo.Name){
		if(match((ItemInfo.Name) item)){ return true;}
	    } else if(item instanceof FoodInfo){
		if(match((FoodInfo)item)){return true;}
	    } else if(item instanceof Inspiration){
		if(match((Inspiration)item)){return true;}
	    } else if(item instanceof GobbleInfo){
		if(match((GobbleInfo)item)){return true;}
	    } else if(className.equals("Slotted")){
	    }
	}
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

    public static class Text extends ItemFilter{
	private String text;

	public  Text(String text){
	    this.text = text.toLowerCase();
	}

	@Override
	protected boolean match(ItemInfo.Name item) {
	    return item.str.text.toLowerCase().contains(text);
	}

	@Override
	protected boolean match(FoodInfo item) {
	    for(int k = 0; k < anm.length; k++){
		if(anm[k].equals(text) && item.tempers[k] > 0){return true;}
	    }
	    for(int k = 0; k < rnm.length; k++){
		if(rnm[k].toLowerCase().contains(text) && item.tempers[k] > 0){return true;}
	    }
	    return false;
	}

	@Override
	protected boolean match(GobbleInfo item) {
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
	    for(String attr : item.attrs){
		if(attr.equals(text)){return true;}
		if(CharWnd.attrnm.get(attr).toLowerCase().contains(text)){return true;}
	    }
	    return false;
	}
    }
}
