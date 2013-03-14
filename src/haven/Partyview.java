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

import haven.Party.Member;

import java.util.*;
import java.util.Map.Entry;

public class Partyview extends Widget {
    final static java.awt.image.BufferedImage[] pleave = {
	Resource.loadimg("gfx/hud/pleave"),
	Resource.loadimg("gfx/hud/pleave"),
	Resource.loadimg("gfx/hud/pleave"),
    };
    long ign;
    Party party = ui.sess.glob.party;
    Map<Long, Member> om = null;
    Member ol = null;
    Map<Member, FramedAva> avs = new HashMap<Member, FramedAva>();
    IButton leave = null;
	
    @RName("pv")
    public static class $_ implements Factory {
	public Widget create(Coord c, Widget parent, Object[] args) {
	    return(new Partyview(c, parent, (Integer)args[0]));
	}
    }
	
    Partyview(Coord c, Widget parent, long ign) {
	super(c, new Coord(84, 140), parent);
	this.ign = ign;
    }
	
    public void tick(double dt) {
	if(party.memb != om) {
	    int i = 0;
	    Collection<Member> old = new HashSet<Member>(avs.keySet());
	    for(final Member m : (om = party.memb).values()) {
		if(m.gobid == ign)
		    continue;
		FramedAva w = avs.get(m);
		if(w == null) {
		    w = new FramedAva(Coord.z, new Coord(36, 36), this, m.gobid, "avacam") {
			    private Tex tooltip = null;
			    
			    public Object tooltip(Coord c, Widget prev) {
				Gob gob = m.getgob();
				if(gob == null)
				    return(tooltip);
				KinInfo ki = gob.getattr(KinInfo.class);
				if(ki == null)
				    return(null);
				return(tooltip = ki.rendered());
			    }
			};
		    avs.put(m, w);
		} else {
		    old.remove(m);
		}
	    }
	    for(Member m : old) {
		ui.destroy(avs.get(m));
		avs.remove(m);
	    }
	    List<Map.Entry<Member, FramedAva>> wl = new ArrayList<Map.Entry<Member, FramedAva>>(avs.entrySet());
	    Collections.sort(wl, new Comparator<Map.Entry<Member, FramedAva>>() {
		    public int compare(Entry<Member, FramedAva> a, Entry<Member, FramedAva> b) {
			long aid = a.getKey().gobid, bid = b.getKey().gobid;
			if(aid < bid)
			    return(-1);
			else if(bid > aid)
			    return(1);
			return(0);
		    }
		});
	    for(Map.Entry<Member, FramedAva> e : wl) {
		e.getValue().c = new Coord((i % 2) * 38, (i / 2) * 38);
		i++;
	    }
	    if(avs.size() > 0) {
		if(leave == null) {
		    leave = new IButton(Coord.z, this, pleave[0], pleave[1], pleave[2]);
		    leave.tooltip = Text.render("Leave party");
		}
		leave.c = new Coord((i % 2) * 38, (i / 2) * 38);
	    }
	    if((avs.size() == 0) && (leave != null)) {
		ui.destroy(leave);
		leave = null;
	    }
	}
	for(Map.Entry<Member, FramedAva> e : avs.entrySet()) {
	    e.getValue().color = e.getKey().col;
	}
    }
	
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == leave) {
	    wdgmsg("leave");
	    return;
	}
	for(Member m : avs.keySet()) {
	    if(sender == avs.get(m)) {
		wdgmsg("click", (int)m.gobid, args[0]);
		return;
	    }
	}
	super.wdgmsg(sender, msg, args);
    }
	
    public void draw(GOut g) {
	super.draw(g);
    }
}
