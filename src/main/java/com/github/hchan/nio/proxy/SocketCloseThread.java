package com.github.hchan.nio.proxy;

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
	public static int GRACE_TIME_MILLIS = 500;

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(GRACE_TIME_MILLIS);
				Iterator<SocketCreatedTimeBean> iterator = safeList.iterator();

				synchronized (safeList) {
					while (iterator.hasNext()) {
						SocketCreatedTimeBean next = iterator.next();
						Date nowMinusGrace = new Date(new Date().getTime() - GRACE_TIME_MILLIS);

						Date createdDate = next.getCreatedDate();

						if (nowMinusGrace.after(createdDate)) {
							next.getSocket().close();
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
