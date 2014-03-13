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

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Inspiration extends ItemInfo.Tip {
    public final String[] attrs;
    public final int[] exp;
    public final int[] o;
    
    public Inspiration(Owner owner, String[] attrs, int[] exp) {
	super(owner);
	this.o = CharWnd.sortattrs(attrs);
	this.attrs = attrs;
	this.exp = exp;
    }

    public int total() {
	int ret = 0;
	for(int lp : exp)
	    ret += lp;
	return(ret);
    }
    
    public BufferedImage longtip() {
	StringBuilder buf = new StringBuilder();
	Color[] cs = UI.instance.gui.chrwdg.attrcols(attrs);
	buf.append("When studied:");
	int uses = -1;
	for (int i = 0; i < attrs.length; i++) {
	    String type = attrs[o[i]];
	    if(type.equals("uses")){
		uses = exp[o[i]];
		continue;
	    }
	    String attr = CharWnd.attrnm.get(type);
	    if(attr == null){continue;}
	    Color c = cs[o[i]];
	    buf.append(String.format("\n$col[%d,%d,%d]{%s: %d}",c.getRed(), c.getGreen(), c.getBlue(), attr, exp[o[i]] ));
	}
	buf.append(String.format("   $b{$col[192,192,64]{Inspiration required: %d}}\n", total()));
	if(uses > 0){ buf.append(String.format("$b{$col[192,192,64]{Uses: %d}}\n", uses)); }
	return RichText.stdf.render(buf.toString(), 0).img;
    }
}
