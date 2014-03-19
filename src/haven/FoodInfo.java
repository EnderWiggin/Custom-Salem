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

public class FoodInfo extends ItemInfo.Tip {
    public static final String colors[] = {"250,96,96", "128,224,96", "224,224,96", "144,128,224"};
    public final int[] tempers;
    public FoodInfo(Owner owner, int[] tempers) {
	super(owner);
	this.tempers = tempers;
    }
    
    public BufferedImage longtip() {
	String text = String.format("Heals: $b{$col[%s]{%s} : $col[%s]{%s} : $col[%s]{%s} : $col[%s]{%s}}", 
		colors[0], Utils.fpformat(tempers[0], 3, 1), 
		colors[1], Utils.fpformat(tempers[1], 3, 1),
		colors[2], Utils.fpformat(tempers[2], 3, 1), 
		colors[3], Utils.fpformat(tempers[3], 3, 1));
	return(RichText.stdf.render(text).img);
    }
}
