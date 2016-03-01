package com.lairplay.telnet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TelnetClient {
	final Logger logger = LoggerFactory.getLogger(TelnetClient.class);
	private final Socket socket;
	private final long millisStart;

	/**
	 * 
	 * @param host
	 * @param port
	 * @param holder
	 * @throws IOException
	 */
	public TelnetClient(String host, int port) throws IOException {
		Socket temp = null;
		try {
			temp = new Socket(host, port);
			temp.setKeepAlive(true);
		} catch (IOException e) {
			System.out.println("couldn't connect to '" + host + "', is it turned on?");
			throw e;
		}

		socket = temp;
		millisStart = System.currentTimeMillis();
	}

	public long ageSecs() {
		return (System.currentTimeMillis() - millisStart) / 1000L;
	}
	/**
	 * 
	 * @param string
	 * @throws IOException
	 */
	public void writeln(String string) throws IOException {
		OutputStream oStream = socket.getOutputStream();
		string += "\r";
		byte[] raw = string.getBytes("UTF-8");
		oStream.write(raw);
		oStream.flush();
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String readln() throws IOException {
		InputStream iStream = socket.getInputStream();			
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		while (true) {
			int c = iStream.read();
			if (c == -1 || c == '\n') {
				break;
			}
			bytes.write(c);
		}
		
		return bytes.toString("UTF-8");
	}
	
	public void close() throws IOException {
		OutputStream oStream = socket.getOutputStream();
		InputStream iStream = socket.getInputStream();			
		oStream.close();
		iStream.close();
		socket.close();
	}
}
