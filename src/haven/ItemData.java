package haven;

import java.awt.image.BufferedImage;

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
}
