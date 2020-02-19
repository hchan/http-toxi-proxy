package com.github.hchan.http.toxi.proxy;

import java.io.InputStream;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
/**
 * @author henry.chan
 * 
 * Thread for communicating with underlying socket in proxy
 */
@Slf4j
public class ProxyThread extends Thread {

	private SocketChannel socketChannel = null;
	private String requestMsg;
	private Socket socket = null;

	public ProxyThread(SocketChannel socketChannel, String requestMsg) {
		this.socketChannel = socketChannel;
		this.requestMsg = requestMsg;
	}
	
	
	private void writeMsgToSocket() throws Exception {
		try {
			RawHttp http = new RawHttp() ;
			logger.info(requestMsg);
			RawHttpRequest request = http.parseRequest(requestMsg);
			//RawHttpResponse<?> response = client.send(request);
			String hostLine = request.getHeaders().getFirst("Host").get();
			String[] hostAndPort = hostLine.split(":");
			socket = new Socket(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
			socket.setKeepAlive(true);
			request.writeTo(socket.getOutputStream());
			SocketCreatedTimeBean socketCreatedTimeBean = new SocketCreatedTimeBean();
			socketCreatedTimeBean.setSocket(socket);
			socketCreatedTimeBean.setCreatedDate(new Date());
			socketCreatedTimeBean.setSocketChannel(socketChannel);
			SocketCloseThread.safeList.add(socketCreatedTimeBean);
			//sockets.add(socket);
		} catch (Exception e) {
			logger.error("", e);
			throw e;
		}
	}

	@Override
	public void run() {
		try {
			writeMsgToSocket();
			
			this.socket = null;
			this.socketChannel = null;
			
		} catch (Exception e) {
			logger.error("", e);
		} finally {
		}
	}
	
	// simple Test
	public static void main(String[] args) throws Exception {
		ProxyThread proxyThread = new ProxyThread(null, "POST http://localhost:8080/gwtRequest HTTP/1.1\n" + 
				"Cookie: JSESSIONID=ufiTOYYptWkzN-SFAcyhkOSTXTSjhteE5UDMUuqN.c02yr22slvcg\n" + 
				"Host: localhost:8080\n" + 
				"accept: */*\n" + 
				"content-type: application/json; charset=UTF-8\n" + 
				"content-length: 375\n" + 
				"\n" + 
				"{ \"MYRAND\" :73327407,\"F\":\"com.indicee.gwt.assetbrowser.shared.AssetBrowserRequestFactory\",\"I\":[{\"O\":\"5Wnp8jhWSgpH7BhYIEC_Cy16oBQ=\",\"P\":[null,null,{\"R\":\"1\",\"C\":6,\"T\":\"mD8GxHNcgZl_gt$AmX6pz_3Dn0s=\"},null,[],[\"NAME\",\"IQN\"],[true,true],false,0,37,false,false,true]}],\"O\":[{\"O\":\"PERSIST\",\"R\":\"1\",\"C\":6,\"T\":\"mD8GxHNcgZl_gt$AmX6pz_3Dn0s=\",\"P\":{\"id\":null,\"name\":null,\"type\":\"ALL\"}}]}");
		proxyThread.writeMsgToSocket();
		byte[] tempBytes = new byte[1024];
		int readBytes = tempBytes.length;
		InputStream dataInputStream = proxyThread.socket.getInputStream();

		while ( readBytes == tempBytes.length) {
			readBytes = dataInputStream.read(tempBytes);
			logger.info(new String(tempBytes, 0, readBytes));
		}
		
		Thread.sleep(10000);
	}
}
