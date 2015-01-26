package haven;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VariantsInfo extends ItemInfo.Tip {
    public Map<String, float[]> variants;
    public VariantsInfo(Owner owner, Map<String, float[]> variants) {
	super(owner);
	this.variants = variants;
    }

    @Override
    public BufferedImage longtip() {
	BufferedImage img = null;
	int k = 0;
	int n = (variants == null)?0:variants.size();
	if(n > 0) {
	    BufferedImage names[] = new BufferedImage[n];
	    BufferedImage vals[] = new BufferedImage[n];
	    int namew = 0, valuew = 0, totalh = 0;
	    for (Map.Entry<String, float[]> variant : variants.entrySet()) {
		String resn = variant.getKey();
		float[] mults = variant.getValue();

		Resource.Tooltip tt = Resource.load(resn).layer(Resource.tooltip);
		names[k] = RichText.render(((tt != null)? tt.t:resn) + ":", 0).img;
		String buf = "";
		for (int i = 0; i < 4; i++) {
		    if (i > 0)
			buf += ", ";
		    buf += String.format("$col[%s]{x%.2f}", Tempers.tcolors[i], mults[i]);
		}
		vals[k] = RichText.render(buf, 0).img;

		namew = Math.max(namew, names[k].getWidth());
		valuew = Math.max(valuew, vals[k].getWidth());
		totalh += names[k].getHeight();
		k++;
	    }
	    img = TexI.mkbuf(new Coord(namew + 5 + valuew, totalh));
	    Graphics g = img.getGraphics();
	    int ch = 0;

	    for(int i = 0; i < names.length; i++) {
		g.drawImage(names[i], 0, ch, null);
		g.drawImage(vals[i], namew + 5, ch, null);
		ch += names[i].getHeight();
	    }
	}
	return img;
    }

    public static class Data implements ItemData.ITipData {

	private Map<String, float[]> vars;

	@Override
	public Tip create() {
	    return new VariantsInfo(null, vars);
	}

	public static class DataAdapter extends TypeAdapter<Data> {

	    @Override
	    public void write(JsonWriter writer, Data data) throws IOException {

	    }

	    @Override
	    public Data read(JsonReader reader) throws IOException {
		Data data = new Data();
		Map<String,float[]> vars = new HashMap<String, float[]>();
		data.vars = vars;
		reader.beginObject();
		while(reader.hasNext()){
		    String name = reader.nextName();
		    float[] mults = new float[4];
		    int k = 0;
		    reader.beginArray();
		    while(reader.hasNext()){
			mults[k++] = (float) reader.nextDouble();
		    }
		    reader.endArray();
		    vars.put(name, mults);
		}
		reader.endObject();
		return data;
	    }
	}
    }
}
