package com.github.hchan.nio.proxy;

import java.net.Socket;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SocketCreatedTimeBean {
	private Socket socket;
	private Date createdDate;
}
