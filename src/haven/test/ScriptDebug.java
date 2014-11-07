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

package haven.test;

import haven.*;
import java.io.*;
import java.net.*;
import javax.script.*;

public class ScriptDebug {
    private final ScriptEngine eng;

    public ScriptDebug(ScriptEngine eng) {
	this.eng = eng;
    }

    public class Client extends HackThread {
	private final Socket sk;

	public Client(Socket sk) {
	    super("Debug client");
	    this.sk = sk;
	}

	private void run2(Reader in, Writer out) throws IOException {
	    BufferedReader lin = new BufferedReader(in);
	    while(true) {
		out.write("% ");
		out.flush();
		String ln = lin.readLine();
		if(ln == null)
		    break;
		Object ret;
		try {
		    ret = eng.eval(ln);
		} catch(Throwable e) {
		    if(!(e instanceof ScriptException) || (e.getCause() == null))
			e.printStackTrace(new PrintWriter(out));
		    else
			out.write(e.getCause().toString() + "\r\n");
		    continue;
		}
		if(ret != null)
		    out.write(ret.toString() + "\r\n");
	    }
	}

	public void run() {
	    try {
		Reader in;
		Writer out;
		try {
		    out = new OutputStreamWriter(sk.getOutputStream(), "utf-8");
		    in = new InputStreamReader(sk.getInputStream(), "utf-8");
		} catch(IOException e) {
		    throw(new RuntimeException(e));
		}
		try {
		    run2(in, out);
		} catch(IOException e) {
		    return;
		}
	    } finally {
		try {
		    sk.close();
		} catch(IOException e) {
		    throw(new RuntimeException(e));
		}
	    }
	}
    }

    public class Server extends HackThread {
	private final ServerSocket sk;
	private final boolean one;

	public Server(int port, boolean one) throws IOException {
	    super("Debug server");
	    sk = new ServerSocket(port);
	    this.one = one;
	}

	public void run() {
	    try {
		while(true) {
		    Client cl;
		    try {
			cl = new Client(sk.accept());
		    } catch(IOException e) {
			break;
		    }
		    cl.setDaemon(true);
		    cl.start();
		    if(one)
			break;
		}
	    } finally {
		try {
		    sk.close();
		} catch(IOException e) {
		    throw(new RuntimeException(e));
		}
	    }
	}
    }

    public static Server start(String type, int port, boolean one) throws IOException {
	ScriptEngine eng = new ScriptEngineManager().getEngineByName(type);
	if(eng == null)
	    throw(new RuntimeException("No such script engine installed: " + type));
	ScriptDebug db = new ScriptDebug(eng);
	Server srv = db.new Server(port, one);
	srv.setDaemon(true);
	srv.start();
	return(srv);
    }

    public static Client connect(String type, String host, int port) throws IOException {
	ScriptEngine eng = new ScriptEngineManager().getEngineByName(type);
	if(eng == null)
	    throw(new RuntimeException("No such script engine installed: " + type));
	ScriptDebug db = new ScriptDebug(eng);
	Socket sk = new HackSocket();
	try {
	    sk.connect(new InetSocketAddress(host, port));
	    Client cl = db.new Client(sk);
	    cl.setDaemon(true);
	    cl.start();
	    sk = null;
	    return(cl);
	} finally {
	    if(sk != null)
		sk.close();
	}
    }
}
