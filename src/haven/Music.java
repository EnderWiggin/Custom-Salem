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

import java.util.*;

public class Music {
    public static double volume = 1.0;
    private static Resource curres = null;
    private static boolean curloop;
    private static Audio.CS clip = null;
    
    static {
	volume = Double.parseDouble(Utils.getpref("bgmvol", "1.0"));
    }

    public static class Jukebox implements Audio.CS {
	public final Resource res;
	private int state;
	private Audio.DataClip cur = null;

	public Jukebox(Resource res, boolean loop) {
	    this.res = res;
	    this.state = loop?0:1;
	}

	public int get(double[][] buf) {
	    int ns = buf[0].length;
	    int nch = buf.length;
	    for(int i = 0; i < nch; i++) {
		for(int o = 0; o < ns; o++) {
		    buf[i][o] = 0;
		}
	    }
	    if(cur == null) {
		if(state == 2)
		    return(-1);
		try {
		    List<Resource.Audio> clips = new ArrayList<Resource.Audio>(res.layers(Resource.audio));
		    cur = new Audio.DataClip(clips.get((int)(Math.random() * clips.size())).pcmstream());
		    if(state == 1)
			state = 2;
		} catch(Loading l) {
		    return(ns);
		}
	    }
	    int ret = cur.get(buf);
	    double vol = volume;
	    if(ret < 0) {
		cur = null;
	    } else {
		for(int i = 0; i < nch; i++) {
		    for(int o = 0; o < ret; o++)
			buf[i][o] *= vol;
		}
	    }
	    return(ns);
	}
    }

    public static void play(Resource res, boolean loop) {
	synchronized(Music.class) {
	    curres = res;
	    curloop = loop;
	    stop();
	    if(volume >= 0.01 && res != null) {
		Audio.play(clip = new Jukebox(res, loop));
	    }
	}
    }

    private static void stop() {
	if(clip != null) {
	    Audio.stop(clip);
	    clip = null;
	}
    }

    public static void setvolume(double vol) {
	synchronized(Music.class) {
	    boolean off = vol < 0.01;
	    boolean prevoff = volume < 0.01;
	    Music.volume = vol;
	    Utils.setpref("bgmvol", Double.toString(Music.volume));
	    if(off && !prevoff) {
		stop();
	    } else if(!off && prevoff) {
		play(curres, curloop);
	    }
	}
    }

    static {
	Console.setscmd("bgm", new Console.Command() {
		public void run(Console cons, String[] args) {
		    int i = 1;
		    String opt;
		    boolean loop = false;
		    if(i < args.length) {
			while((opt = args[i]).charAt(0) == '-') {
			    i++;
			    if(opt.equals("-l"))
				loop = true;
			}
			String resnm = args[i++];
			int ver = -1;
			if(i < args.length)
			    ver = Integer.parseInt(args[i++]);
			Music.play(Resource.load(resnm, ver), loop);
		    } else {
			Music.play(null, false);
		    }		
		}
	    });
	Console.setscmd("bgmvol", new Console.Command() {
		public void run(Console cons, String[] args) {
		    setvolume(Double.parseDouble(args[1]));
		}
	    });
    }
}
