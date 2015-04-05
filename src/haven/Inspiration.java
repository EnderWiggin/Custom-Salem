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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Inspiration extends ItemInfo.Tip {
    public final int xc;
    public final String[] attrs;
    public final int[] exp;
    public final int[] o;
    
    public Inspiration(Owner owner, int xc, String[] attrs, int[] exp) {
	super(owner);
	this.xc = (xc >= 0)?xc:total();
	this.o = CharWnd.sortattrs(attrs);
	this.attrs = attrs;
	this.exp = exp;
    }

    public Inspiration(Owner o, String[] attrs, int[] exp) {
	this(o, -1, attrs, exp);
    }

    public int total() {
	int ret = 0;
	int n = attrs.length;
	for(int i =0; i<n; i++) {
	    if(attrs[i].equals("uses")){continue;}
	    ret += exp[i];
	}
	return(ret);
    }

    public BufferedImage longtip() {
	StringBuilder buf = new StringBuilder();
	Color[] cs = UI.instance.gui.chrwdg.attrcols(attrs);
	buf.append("When studied:");
	int uses = -1;
	for (int i = 0; i < attrs.length; i++) {
	    int k = o[i];
	    String type = attrs[k];
	    if(type.equals("uses")){
		uses = exp[k];
		continue;
	    }
	    String attr = CharWnd.attrnm.get(type);
	    if(attr == null){continue;}
	    Color c = cs[k];
	    buf.append(String.format("\n$col[%d,%d,%d]{%s: %d}",c.getRed(), c.getGreen(), c.getBlue(), attr, exp[k] ));
	}
	buf.append(String.format("   $b{$col[192,192,64]{Inspiration required: %d}}\n", xc));
	if(uses > 0){ buf.append(String.format("$b{$col[192,192,64]{Uses: %d}}\n", uses)); }
	return RichText.stdf.render(buf.toString(), 0).img;
    }

    public static class Data implements ItemData.ITipData {
	String[] attrs;
	int[] exp;

	public Data(){}
	public Data(Inspiration info){
	    attrs = info.attrs;
	    exp = info.exp;
	}

	@Override
	public Tip create() {
	    return new Inspiration(null, attrs, exp);
	}

	public static class DataAdapter extends TypeAdapter<Data>
	{

	    @Override
	    public void write(JsonWriter writer, Data data) throws IOException {
		writer.beginObject();
		int n = data.attrs.length;
		for(int i=0; i < n; i++){
		    writer.name(data.attrs[i]).value(data.exp[i]);
		}
		writer.endObject();
	    }

	    @Override
	    public Data read(JsonReader reader) throws IOException {
		List<String> names = new LinkedList<String>();
		List<Integer> vals = new LinkedList<Integer>();

		reader.beginObject();
		while(reader.hasNext()){
		    names.add(reader.nextName());
		    vals.add(reader.nextInt());
		}
		reader.endObject();

		Data data = new Data();
		data.attrs = names.toArray(new String[names.size()]);

		data.exp = new int[vals.size()];
		for(int i = 0;i < data.exp.length;i++) {
		    data.exp[i] = vals.get(i);
		}

		return data;
	    }
	}
    }
}
