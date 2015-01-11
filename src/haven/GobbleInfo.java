/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class GobbleInfo extends ItemInfo.Tip {
    public final int[] l, h;
    public final int[] types;
    public final int ft;
    public final List<Event> evs;
    
    public static class Event {
	public final List<ItemInfo> info;
	public final double p;
	private BufferedImage rinf, rp;
	public Event(List<ItemInfo> info, double p) {this.info = info; this.p = p;}
    }

    public GobbleInfo(Owner owner, int[] l, int[] h, int[] types, int ft, List<Event> evs) {
	super(owner);
	this.l = l;
	this.h = h;
	this.types = types;
	this.ft = ft;
	for(Event ev : this.evs = evs) {
	    ev.rinf = ItemInfo.longtip(ev.info);
	    if(ev.p < 1)
		ev.rp = RichText.render(String.format("[%d%%]", (int)Math.round(ev.p * 100)), Color.LIGHT_GRAY).img;
	}
    }
    
    private static final Text.Line head = Text.render("When gobbled:");
    public BufferedImage longtip() {
	StringBuilder buf = new StringBuilder();
	buf.append(String.format("Points: $b{%s : %s : %s : %s}\n", point(0), point(1), point(2), point(3)));
	int min = (ft + 30) / 60;
	buf.append(String.format("Full and Fed Up for %02d:%02d\n", min / 60, min % 60));
	BufferedImage gi = RichText.render(buf.toString(), 0).img;
	Coord sz = PUtils.imgsz(gi);
	for(Event ev : evs) {
	    int w = ev.rinf.getWidth();
	    if(ev.rp != null)
		w += 5 + ev.rp.getWidth();
	    sz.x = Math.max(sz.x, w);
	    sz.y += ev.rinf.getHeight();
	}
	BufferedImage img = TexI.mkbuf(sz.add(10, head.sz().y + 2));
	Graphics g = img.getGraphics();
	int y = 0;
	g.drawImage(head.img, 0, y, null);
	y += head.sz().y + 2;
	g.drawImage(gi, 10, y, null);
	y += gi.getHeight();
	for(Event ev : evs) {
	    g.drawImage(ev.rinf, 10, y, null);
	    if(ev.rp != null)
		g.drawImage(ev.rp, 10 + ev.rinf.getWidth() + 5, y, null);
	    y += ev.rinf.getHeight();
	}
	g.dispose();
	return(img);
    }
    
    private String point(int i) {
	return String.format("$col[%s]{%s} - $col[%s]{%s}",
		Tempers.tcolors[i], Utils.fpformat(l[i], 3, 1), 
		Tempers.tcolors[i], Utils.fpformat(h[i], 3, 1));
    }

    public static class Data implements ItemData.ITipData {
	public int[] l, h;
	public int ft;
	public List<Event> evs;

	public Data(){ }
	public Data(GobbleInfo info, double mult){
	    if(mult == 1) {
		l = info.l;
		h = info.h;
	    } else {
		l = FoodInfo.Data.fixMult(mult, info.l);
		h = FoodInfo.Data.fixMult(mult, info.h);
	    }
	    ft = info.ft;

	    evs = info.evs;
	}

	@Override
	public Tip create() {
	    return new GobbleInfo(null, l, h, new int[]{}, ft, evs);
	}

	public static class DataAdapter extends TypeAdapter<Data> {

	    @Override
	    public void write(JsonWriter writer, Data data) throws IOException {
		writer.beginObject();

		writer.name("fed-up_time").value(data.ft);
		writeArray(writer, "high", data.h);
		writeArray(writer, "low", data.l);

		writeEvents(writer, data.evs);

		writer.endObject();
	    }

	    @Override
	    public Data read(JsonReader reader) throws IOException {
		Data data = new Data();

		reader.beginObject();
		while(reader.hasNext()){
		    String name = reader.nextName();
		    if(name.equals("fed-up_time")){
			data.ft = reader.nextInt();
		    } else if(name.equals("low")){
			data.l = readArray(reader);
		    } else if(name.equals("high")){
			data.h = readArray(reader);
		    } else if (name.equals("events")){
			data.evs = readEvents(reader);
		    }
		}
		reader.endObject();

		return data;
	    }

	    private List<Event> readEvents(JsonReader reader) throws IOException {
		List<Event> events = new LinkedList<Event>();
		reader.beginArray();
		while(reader.hasNext()) {
		    reader.beginObject();
		    double p = 0;
		    int value = 0;
		    String type = null;
		    while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("chance")) {
			    p = reader.nextDouble();
			} else if (name.equals("value")) {
			    value = reader.nextInt();
			} else if (name.equals("type")) {
			    type = reader.nextString();
			}
		    }
		    reader.endObject();

		    Indir<Resource> res = Resource.load(type).indir();
		    LinkedList<ItemInfo> itemInfos = new LinkedList<ItemInfo>();
		    itemInfos.add(new GobbleEventInfo(null, value, res));
		    events.add(new Event(itemInfos,p));
		}
		reader.endArray();
		return events;
	    }

	    private static void writeEvents(JsonWriter writer, List<Event> events) throws IOException {
		writer.name("events");
		writer.beginArray();
		for(Event event : events){
		    writer.beginObject();
		    writer.name("chance").value(event.p);
		    GobbleEventInfo info = (GobbleEventInfo) event.info.get(0);

		    writer.name("value").value(info.value);
		    writer.name("type").value(info.res.get().name);

		    writer.endObject();
		}
		writer.endArray();
	    }

	    private static void writeArray(JsonWriter writer, String name, int[] values) throws IOException {
		writer.name(name);
		writer.beginArray();
		for(int h : values){
		    writer.value(h);
		}
		writer.endArray();
	    }

	    private static int[] readArray(JsonReader reader) throws IOException {
		List<Integer> tmp = new LinkedList<Integer>();
		int[] values;

		reader.beginArray();
		while(reader.hasNext()){
		    tmp.add(reader.nextInt());
		}
		reader.endArray();

		values = new int[tmp.size()];
		for(int i = 0;i < values.length; i++) {
		    values[i] = tmp.get(i);
		}
		return values;
	    }
	}
    }
}
