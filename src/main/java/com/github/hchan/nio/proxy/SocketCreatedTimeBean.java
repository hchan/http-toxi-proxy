package com.github.hchan.nio.proxy;

import java.net.Socket;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
/**
 * 
 * @author henry.chan
 * Just a simple bean
 */
@Getter @Setter
public class SocketCreatedTimeBean {
	private Socket socket;
	private Date createdDate;
}
