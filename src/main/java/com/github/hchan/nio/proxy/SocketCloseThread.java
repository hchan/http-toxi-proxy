package com.github.hchan.nio.proxy;

import java.net.Socket;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class SocketCloseThread extends Thread {
	private Socket socket;
	public SocketCloseThread(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(800);
			socket.close();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}
