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
	public static List<SocketCreatedTimeBean> safeList = new CopyOnWriteArrayList<SocketCreatedTimeBean>();// Collections.synchronizedList(new
																											// ArrayList<>());
	public static int GRACE_TIME_MILLIS = 800; // holds socket for this long before evicting

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
				// Iterator<SocketCreatedTimeBean> iterator = safeList.iterator();
				// synchronized (safeList) {
				// while (iterator.hasNext()) {
				safeList.forEach(socketCreatedTimeBean -> {
					ByteBuffer byteBuffer = null;
					Date nowMinusGrace = new Date(new Date().getTime() - GRACE_TIME_MILLIS);
					Socket socket = socketCreatedTimeBean.getSocket();
					SocketChannel socketChannel = socketCreatedTimeBean.getSocketChannel();
					// socketChannel.write(byteBuffer);

					Date createdDate = socketCreatedTimeBean.getCreatedDate();
					if (nowMinusGrace.after(createdDate)) {
						try {
							safeList.remove(socketCreatedTimeBean);
							InputStream inputStream = socket.getInputStream();
							byte[] tempBytes = new byte[1024];
							int readBytes = tempBytes.length;
							while (readBytes == tempBytes.length) {
								readBytes = inputStream.read(tempBytes);
								byteBuffer = ByteBuffer.wrap(tempBytes, 0, readBytes);
								if (socketChannel.isOpen()) {
									socketChannel.write(byteBuffer);
									socketChannel.close();
								}
								// logger.info(new String(tempBytes, 0, readBytes));
							}
							socket.close();
						} catch (Exception e) {
							logger.error("", e);
						} finally {
							try {
								socket.close();
							} catch (Exception e) {
							}
							try {
								socketChannel.close();
							} catch (Exception e) {
							}
						}
					}
				}
				);
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}
}
