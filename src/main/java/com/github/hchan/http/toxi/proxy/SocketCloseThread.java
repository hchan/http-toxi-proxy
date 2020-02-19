package com.github.hchan.http.toxi.proxy;

import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author henry.chan Holds sockets and periodically frees them ... toxicity ;)
 */
@Slf4j
public class SocketCloseThread extends Thread {
	public static List<SocketCreatedTimeBean> safeList = new CopyOnWriteArrayList<SocketCreatedTimeBean>();
	public static int GRACE_TIME_MILLIS = 800; // holds socket for this long before evicting

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1);
				Iterator<SocketCreatedTimeBean> iterator = safeList.iterator();
				synchronized (safeList) {
					while (iterator.hasNext()) {
						ByteBuffer byteBuffer = ByteBuffer.wrap("Hello World".getBytes());
						SocketCreatedTimeBean next = iterator.next();
						Date nowMinusGrace = new Date(new Date().getTime() - GRACE_TIME_MILLIS);
						Socket socket = next.getSocket();
						SocketChannel socketChannel = next.getSocketChannel();
						//socketChannel.write(byteBuffer);
						socketChannel.close();
						Date createdDate = next.getCreatedDate();
						if (nowMinusGrace.after(createdDate)) {
							InputStream inputStream = socket.getInputStream();
							byte[] tempBytes = new byte[1024];
							int readBytes = tempBytes.length;
							
							while ( readBytes == tempBytes.length) {
								readBytes = inputStream.read(tempBytes);
								byteBuffer = ByteBuffer.wrap(tempBytes, 0, readBytes);
								//socketChannel.write(byteBuffer);
								//logger.info(new String(tempBytes, 0, readBytes));
							}
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
