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
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class GobbleInfo extends GItem.Tip {
    private static final String[] colors;
    static {
	String[] c = new String[Alchemy.colors.length];
	for(int i = 0; i < c.length; i++) {
	    Color col = Alchemy.colors[i];
	    c[i] = String.format("%d,%d,%d", col.getRed(), col.getGreen(), col.getBlue());
	}
	colors = c;
    };
    public final int[][] evs;
    
    public GobbleInfo(GItem item, int[][] evs) {
	item.super();
	this.evs = evs;
    }
    
    public BufferedImage longtip() {
	StringBuilder buf = new StringBuilder();
	buf.append("Events:\n");
	for(int i = 0; i < 4; i++) {
	    buf.append(String.format("  $col[%s]{%s, %s, %s, %s}\n", colors[i], 
				     Utils.fpformat(evs[i][0], 3, 1), Utils.fpformat(evs[i][1], 3, 1),
				     Utils.fpformat(evs[i][2], 3, 1), Utils.fpformat(evs[i][3], 3, 1)));
	}
	return(RichText.render(buf.toString(), 0).img);
    }
}
