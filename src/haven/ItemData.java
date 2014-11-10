package haven;

import haven.Glob.Pagina;

import java.awt.image.BufferedImage;
import java.util.List;

public class ItemData {
    public FoodInfo.Data food;
    public Inspiration.Data inspiration;
    
    public Tex longtip(Resource res) {
	Resource.AButton ad = res.layer(Resource.action);
	Resource.Pagina pg = res.layer(Resource.pagina);
	String tt = ad.name;
	BufferedImage xp = null, food = null;//, slots = null, art = null;
	if(pg != null){tt += "\n\n" + pg.text;}
	
	if(this.food != null){
	    food = this.food.create().longtip();
	}
	if(this.inspiration != null){
	    xp = this.inspiration.create().longtip();
	}
	
	BufferedImage img = MenuGrid.ttfnd.render(tt, 300).img;
	if(food != null){
	    img = ItemInfo.catimgs(3, img, food);
	}
	if(xp != null){
	    img = ItemInfo.catimgs(3, img, xp);
	}
	return new TexI(img);
    }
    
    public static interface ITipData {
	ItemInfo.Tip create();
    }
    
    public static void actualize(GItem item, Pagina pagina) {
	String name = item.name();
	if(name == null){ return; }
	
	List<ItemInfo> info = item.info();
	ItemData data = new ItemData();
	for(ItemInfo ii : info){
	    if(ii instanceof FoodInfo){
		data.food = new FoodInfo.Data((FoodInfo) ii);
	    } else if(ii instanceof Inspiration){
		data.inspiration = new Inspiration.Data((Inspiration) ii);
	    }
	}
	name = pagina.res().name;
	Config.item_data.put(name, data);
	
    }
}
