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

public class SeasonImg extends Widget {
    private static final Tex seasons[] = {Resource.loadtex("gfx/hud/coldsnap"),Resource.loadtex("gfx/hud/everbloom"),Resource.loadtex("gfx/hud/bloodmoon")};
    private int cs = 1;
    private double t = 0;
    
    public SeasonImg(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent);
    }
    
    public void draw(GOut g) {
        switch(cs){
	case 0: 
	    g.chcolor(Color.red);break;
	case 1: 
	    g.chcolor(Color.blue);break;
	case 2: 
	    g.chcolor(Color.green);break;
        }
        g.frect(Coord.z,this.sz);
        g.image(seasons[cs],this.sz.sub(seasons[cs].sz()).div(2));
    }

    public void tick(double dt) {
        t += dt;
        cs = ((int)t/3)%4;
    }
}