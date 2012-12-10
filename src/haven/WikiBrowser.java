package haven;

import haven.RichText.Part;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WikiBrowser extends Window {
    public static final RichText.Foundry fnd = new RichText.Foundry(new WikiParser(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 12, TextAttribute.FOREGROUND, Color.WHITE));

    private static class WikiParser extends RichText.Parser {

	public WikiParser(Object... args) {
	    super(args);
	}

	@Override
	protected Part tag(PState s, String tn, String[] args, Map<? extends Attribute, ?> attrs) throws IOException {
	    if(tn.equals("table")){
		return new Table(args, attrs);
	    }
	    return super.tag(s, tn, args, attrs);
	}

    }

    private static class Table extends RichText.Part {
	private static final int PAD_W = 5;
	private static final int PAD_H = 3;
	private int tabc, w=1, h=0, lh = 0;
	private int[] twidth;
	private BufferedImage[] tnames;
	private List<BufferedImage[]> rows = new ArrayList<BufferedImage[]>();
	public Table(String[] args, Map<? extends Attribute, ?> attrs) {
	    super();
	    tabc = Integer.parseInt(args[0]);
	    tnames = new BufferedImage[tabc];
	    twidth = new int[tabc];
	    int i=0;
	    for(i=0; i<tabc; i++){
		tnames[i] = fnd.render(args[i+1]).img;
		twidth[i] = tnames[i].getWidth() + 2*PAD_W;
		lh = Math.max(h, tnames[i].getHeight()+2*PAD_H);
	    }
	    i = tabc+1;
	    while(i < args.length){
		BufferedImage[] cols = new BufferedImage[tabc];
		for(int k=0; k<tabc; k++, i++){
		    cols[k] = fnd.render(args[i]).img;
		    twidth[k] = Math.max(twidth[k], cols[k].getWidth() + 2*PAD_W);
		    lh = Math.max(h, cols[k].getHeight()+2*PAD_H);
		}
		rows.add(cols);
	    }
	    for(i=0; i<tabc; i++){w += twidth[i];}
	    h = lh*(rows.size()+1);
	}

	@Override
	public int height() {return h;}

	@Override
	public int width() {return w;}

	@Override
	public int baseline() {return h-1;}

	@Override
	public void render(Graphics2D g) {
	    g.setColor(Color.WHITE);
	    int cx=x, cy=y, cw, i;
	    for(i=0; i<tabc; i++){
		cw = twidth[i];
		g.drawImage(tnames[i], cx+PAD_W, cy+PAD_H, null);
		g.drawRect(cx, cy, cw, lh);
		cx += cw;
	    }
	    i=1;
	    for(BufferedImage[] cols : rows){
		cx = x;
		cy = y+lh*i;
		for(int j=0; j<tabc; j++){
		    cw = twidth[j];
		    g.drawImage(cols[j], cx+PAD_W, cy+PAD_H, null);
		    g.drawRect(cx, cy, cw, lh);
		    cx += cw;
		}
		i++;
	    }
	}

    }

    public WikiBrowser(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent, "Wiki");
	justclose = true;
	String text = "some text blah-blah...\n$table[6,Ore,Lime,1+ Ingots,2+ Ingots,3+ Ingots,Average Yield,1,24,25.00%,0.00%,0.00%,0.25,2,23,42.24%,5.76%,0.00%,0.48,3,22,54.35%,13.44%,1.22%,0.69,4,21,62.98%,21.22%,3.56%,0.88,5,20,69.23%,28.33%,6.59%,1.05,6,19,73.79%,34.46%,9.89%,1.20,7,18,77.12%,39.56%,13.13%,1.33,8,17,79.56%,43.66%,16.08%,1.44,9,16,81.31%,46.85%,18.61%,1.53,10,15,82.51%,49.20%,20.64%,1.60,11,14,83.27%,50.78%,22.12%,1.65,12,13,83.63%,51.66%,23.03%,1.68,13,12,83.64%,51.86%,23.37%,1.69,14,11,83.30%,51.41%,23.15%,1.68,15,10,82.59%,50.31%,22.38%,1.65,16,9,81.47%,48.53%,21.08%,1.60,17,8,79.88%,46.04%,19.27%,1.53,18,7,77.71%,42.81%,17.02%,1.44,19,6,74.81%,38.79%,14.39%,1.33,20,5,70.99%,33.95%,11.50%,1.20,21,4,65.94%,28.30%,8.49%,1.05,22,3,59.27%,21.92%,5.59%,0.88,23,2,50.37%,15.07%,3.05%,0.69,24,1,38.42%,8.26%,1.18%,0.48,25,0,22.22%,2.58%,0.20%,0.25]\nsome more text tra-la-la";
	new RichTextBox(Coord.z, sz, this, text, fnd);
	pack();
    }

}
