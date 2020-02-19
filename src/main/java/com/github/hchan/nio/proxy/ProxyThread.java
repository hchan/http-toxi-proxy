package com.github.hchan.nio.proxy;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import lombok.extern.slf4j.Slf4j;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.client.TcpRawHttpClient;

@Slf4j
public class ProxyThread extends Thread {
	private InetSocketAddress inetSocketAddress;
	private SocketChannel serverSocketChannel;
	private String msg;
	private SelectionKey selectionKey;

	public ProxyThread(InetSocketAddress inetSocketAddress, SocketChannel serverSocketChannel, String msg, SelectionKey selectionKey) {
		this.inetSocketAddress = inetSocketAddress;
		this.serverSocketChannel = serverSocketChannel;
		this.msg = msg;
		this.selectionKey = selectionKey;
	}

	@Override
	public void run() {
		try {
			//TcpRawHttpClient client = new TcpRawHttpClient();
			RawHttp http = new RawHttp() ;
			logger.info(msg);
			RawHttpRequest request = http.parseRequest(msg);
			//RawHttpResponse<?> response = client.send(request);
			String hostLine = request.getHeaders().getFirst("Host").get();
			String[] hostAndPort = hostLine.split(":");
			Socket socket = new Socket(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
			request.writeTo(socket.getOutputStream());
			Thread.sleep(700);
			
			InputStream dataInputStream = socket.getInputStream();
			byte[] tempBytes = new byte[1024];
			int readBytes = tempBytes.length;
			ByteBuffer byteBuffer = null;
			while ( readBytes == tempBytes.length) {
				readBytes = dataInputStream.read(tempBytes);
				byteBuffer = ByteBuffer.wrap(tempBytes, 0, readBytes);
				serverSocketChannel.write(byteBuffer);
			}

			/*
			clientSocketChannel.configureBlocking(false);
			byte[] message = new String(msg).getBytes();
			ByteBuffer buffer = ByteBuffer.wrap(message);
			clientSocketChannel.write(buffer);
			ByteBuffer byteBuffer = ByteBuffer.allocate(10000000);
			clientSocketChannel.read(byteBuffer);
			String result = new String(byteBuffer.array()).trim();
			clientSocketChannel.close();
			serverSocketChannel.write(byteBuffer);
			*/
			//selectionKey.cancel();

			
			//dataInputStream.close();
			//socket.getOutputStream().close();
			serverSocketChannel.close();
			//socket.close();
			
			
			this.inetSocketAddress = null;
			this.serverSocketChannel = null;
			this.msg = null;
			this.selectionKey = null;
			
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			logger.info("FINALLY");
		}
	}
}
