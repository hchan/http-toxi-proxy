package com.github.hchan.http.toxi.proxy;

import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
/**
 * 
 * @author henry.chan
 * Just a simple bean to hold a socket and the createdDate
 */
@Getter @Setter
public class SocketCreatedTimeBean {
	private Socket socket;
	private Date createdDate;
	private SocketChannel socketChannel;
}
