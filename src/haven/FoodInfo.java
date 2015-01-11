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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class FoodInfo extends ItemInfo.Tip {
    public final int[] tempers;
    public FoodInfo(Owner owner, int[] tempers) {
	super(owner);
	this.tempers = tempers;
    }
    
    public BufferedImage longtip() {
	StringBuilder buf = new StringBuilder();
	buf.append("Heals: ");
	for(int i = 0; i < 4; i++) {
	    if(i > 0)
		buf.append(", ");
	    buf.append(String.format("$col[%s]{%s}", Tempers.tcolors[i], Utils.fpformat(tempers[i], 3, 1)));
	}
	return(RichText.render(buf.toString(), 0).img);
    }

    public static class Data implements ItemData.ITipData {
	public int[] tempers;

	public Data(){ }
	public Data(FoodInfo info, double mult){
	    if(mult == 1) {
		tempers = info.tempers;
	    } else {
		tempers = fixMult(mult, info.tempers);
	    }
	}

	public static int[] fixMult(double mult, int[] from) {
	    int[] res = new int[from.length];
	    for(int i = 0; i< from.length; i++){
		double a = from[i] / (100.0*mult);
		res[i] = (int) (100*Math.round(a));
	    }
	    return  res;
	}

	@Override
	public ItemInfo.Tip create()
	{
	    return new FoodInfo(null, tempers);
	}
	
	public static class DataAdapter extends TypeAdapter<Data>{

	    @Override
	    public Data read(JsonReader reader) throws IOException {
		Data data = new Data();
		List<Integer> vals = new LinkedList<Integer>();
		
		reader.beginArray();
		while(reader.hasNext()){
		    vals.add(reader.nextInt());
		}
		reader.endArray();
		
		data.tempers = new int[vals.size()];
		for(int i = 0;i < data.tempers.length; i++) {
		    data.tempers[i] = vals.get(i);
		}
		return data;
	    }

	    @Override
	    public void write(JsonWriter writer, Data data) throws IOException {
		writer.beginArray();
		int n = data.tempers.length;
		for(int i = 0; i < n; i++){
		    writer.value(data.tempers[i]);
		}
		writer.endArray();
	    }
	    
	}
    }
}
