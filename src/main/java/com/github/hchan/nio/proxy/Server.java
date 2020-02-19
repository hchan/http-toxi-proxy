package com.github.hchan.nio.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
@Slf4j
public class Server {
	public static int MAX_THREAD_COUNT = 300;

	public static void main(String[] args) throws IOException {

		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD_COUNT);
		// Selector: multiplexor of SelectableChannel objects
		Selector selector = Selector.open(); // selector is open here

		// ServerSocketChannel: selectable channel for stream-oriented listening sockets
		ServerSocketChannel crunchifySocket = ServerSocketChannel.open();
		InetSocketAddress crunchifyAddr = new InetSocketAddress("localhost", 28080);

		// Binds the channel's socket to a local address and configures the socket to
		// listen for connections
		crunchifySocket.bind(crunchifyAddr);

		// Adjusts this channel's blocking mode.
		crunchifySocket.configureBlocking(false);

		int ops = crunchifySocket.validOps();
		SelectionKey selectKy = crunchifySocket.register(selector, ops, null);

		
		SocketCloseThread socketCloseThread = new SocketCloseThread();
		socketCloseThread.start();
		
		// Infinite loop..
		// Keep server running
		while (true) {

			logger.info("i'm a server and i'm waiting for new connection and buffer select...");
			// Selects a set of keys whose corresponding channels are ready for I/O
			// operations
			selector.select();

			// token representing the registration of a SelectableChannel with a Selector
			Set<SelectionKey> crunchifyKeys = selector.selectedKeys();
			Iterator<SelectionKey> crunchifyIterator = crunchifyKeys.iterator();

			while (crunchifyIterator.hasNext()) {
				SelectionKey myKey = crunchifyIterator.next();
				try {
					// Tests whether this key's channel is ready to accept a new socket connection
					if (myKey.isAcceptable()) {
						try {
							SocketChannel crunchifyClient = crunchifySocket.accept();
		
							// Adjusts this channel's blocking mode to false
							crunchifyClient.configureBlocking(false);
		
							// Operation-set bit for read operations
							crunchifyClient.register(selector, SelectionKey.OP_READ);
							logger.info("Connection Accepted: " + crunchifyClient.getLocalAddress() + "\n");
						} catch (Exception e) {
							logger.error("", e);
						}
						// Tests whether this key's channel is ready for reading
					} else if (myKey.isReadable()) {
						try {
							SocketChannel serverSocketChannel = (SocketChannel) myKey.channel();
							ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
							serverSocketChannel.read(byteBuffer);
							String result = new String(byteBuffer.array()).trim();
							logger.info("Message received: " + result);
							byteBuffer.clear();
							myKey.cancel();
							ProxyThread proxyThread = new ProxyThread(serverSocketChannel, result);
							executor.execute(proxyThread);
						} catch (Exception e) {
							logger.error("", e);
						} finally {
						}	
					} 
				} catch (Exception e) {
					logger.error("", e);
				}
				crunchifyIterator.remove();
			}
		}
	}
}