package com.github.hchan.http.toxi.proxy;

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
/**
 * 
 * @author henry.chan
 * Starting point
 * To monitor traffic :
 * lsof -nP -i4TCP:8080 | grep "\->127.0.0.1:8080" | wc -l
 */
@SuppressWarnings("unused")
@Slf4j
public class Server {
	public static final int MAX_THREAD_COUNT = 300;
	public static int SERVER_PORT = 28080;
	public static void main(String[] args) throws IOException {

		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD_COUNT);
		// Selector: multiplexor of SelectableChannel objects
		Selector selector = Selector.open(); // selector is open here

		// ServerSocketChannel: selectable channel for stream-oriented listening sockets
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", SERVER_PORT);

		// Binds the channel's socket to a local address and configures the socket to
		// listen for connections
		serverSocketChannel.bind(inetSocketAddress);

		// Adjusts this channel's blocking mode.
		serverSocketChannel.configureBlocking(false);

		int ops = serverSocketChannel.validOps();
		SelectionKey selectKy = serverSocketChannel.register(selector, ops, null);
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
			Set<SelectionKey> selectionKeySet = selector.selectedKeys();
			Iterator<SelectionKey> selectionKeyIterator = selectionKeySet.iterator();

			while (selectionKeyIterator.hasNext()) {
				SelectionKey myKey = selectionKeyIterator.next();
				try {
					// Tests whether this key's channel is ready to accept a new socket connection
					if (myKey.isAcceptable()) {
						try {
							SocketChannel socketChannel = serverSocketChannel.accept();
		
							// Adjusts this channel's blocking mode to false
							socketChannel.configureBlocking(false);
		
							// Operation-set bit for read operations
							socketChannel.register(selector, SelectionKey.OP_READ);
							logger.info("Connection Accepted: " + socketChannel.getLocalAddress() + "\n");
						} catch (Exception e) {
							logger.error("", e);
						}
						// Tests whether this key's channel is ready for reading
					} else if (myKey.isReadable()) {
						try {
							SocketChannel socketChannel = (SocketChannel) myKey.channel();
							ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
							socketChannel.read(byteBuffer);
							String requestMsg = new String(byteBuffer.array()).trim();
							logger.info("Message received: " + requestMsg);
							byteBuffer.clear();
							myKey.cancel();
							ProxyThread proxyThread = new ProxyThread(socketChannel, requestMsg);
							executor.execute(proxyThread);
						} catch (Exception e) {
							logger.error("", e);
						} finally {
						}	
					} 
				} catch (Exception e) {
					logger.error("", e);
				}
				selectionKeyIterator.remove();
			}
		}
	}
}