package com.github.hchan.nio.proxy;

import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;

@Slf4j
public class ProxyThread extends Thread {

	private SocketChannel serverSocketChannel;
	private String msg;
	private Socket socket = null;

	public ProxyThread(SocketChannel serverSocketChannel, String msg) {
		this.serverSocketChannel = serverSocketChannel;
		this.msg = msg;
	}
	
	
	private void writeMsgToSocket() throws Exception {
		try {
			RawHttp http = new RawHttp() ;
			logger.info(msg);
			RawHttpRequest request = http.parseRequest(msg);
			//RawHttpResponse<?> response = client.send(request);
			String hostLine = request.getHeaders().getFirst("Host").get();
			String[] hostAndPort = hostLine.split(":");
			socket = new Socket(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
			socket.setKeepAlive(true);
			request.writeTo(socket.getOutputStream());
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
			//Thread.sleep(700);
			
			InputStream dataInputStream = socket.getInputStream();
			byte[] tempBytes = new byte[1024];
			int readBytes = tempBytes.length;
			ByteBuffer byteBuffer = null;
			while ( readBytes == tempBytes.length) {
				readBytes = dataInputStream.read(tempBytes);
				byteBuffer = ByteBuffer.wrap(tempBytes, 0, readBytes);
				serverSocketChannel.write(byteBuffer);
				//logger.info(new String(tempBytes, 0, readBytes));
			}
			serverSocketChannel.close();
			SocketCreatedTimeBean socketCreatedTimeBean = new SocketCreatedTimeBean();
			socketCreatedTimeBean.setSocket(socket);
			socketCreatedTimeBean.setCreatedDate(new Date());
			SocketCloseThread.safeList.add(socketCreatedTimeBean);
			this.socket = null;
			this.serverSocketChannel = null;
			
		} catch (Exception e) {
			logger.error("", e);
		} finally {
		}
	}
	
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
		ByteBuffer byteBuffer = null;
		InputStream dataInputStream = proxyThread.socket.getInputStream();

		while ( readBytes == tempBytes.length) {
			readBytes = dataInputStream.read(tempBytes);
			byteBuffer = ByteBuffer.wrap(tempBytes, 0, readBytes);
			logger.info(new String(tempBytes, 0, readBytes));
		}
		
		Thread.sleep(10000);
	}
}
