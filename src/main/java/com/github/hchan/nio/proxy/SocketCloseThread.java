package com.github.hchan.nio.proxy;

import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author henry.chan Holds sockets and periodically frees them ... toxicity ;)
 */
@Slf4j
public class SocketCloseThread extends Thread {
	public static List<SocketCreatedTimeBean> safeList = Collections.synchronizedList(new ArrayList<>());
	public static int GRACE_TIME_MILLIS = 1000; // holds socket for this long before evicting

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(2);
				Iterator<SocketCreatedTimeBean> iterator = safeList.iterator();
				synchronized (safeList) {
					while (iterator.hasNext()) {
						SocketCreatedTimeBean next = iterator.next();
						Date nowMinusGrace = new Date(new Date().getTime() - GRACE_TIME_MILLIS);
						Date createdDate = next.getCreatedDate();
						if (nowMinusGrace.after(createdDate)) {
							Socket socket = next.getSocket();
							SocketChannel socketChannel = next.getSocketChannel();
							InputStream dataInputStream = socket.getInputStream();
							byte[] tempBytes = new byte[1024];
							int readBytes = tempBytes.length;
							ByteBuffer byteBuffer = null;
							while ( readBytes == tempBytes.length) {
								readBytes = dataInputStream.read(tempBytes);
								byteBuffer = ByteBuffer.wrap(tempBytes, 0, readBytes);
								socketChannel.write(byteBuffer);
								//logger.info(new String(tempBytes, 0, readBytes));
							}
							socketChannel.close();
							socket.close();
							iterator.remove();
						}
					}
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}
}
