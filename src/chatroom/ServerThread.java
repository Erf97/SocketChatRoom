package chatroom;

import java.net.ServerSocket;

public class ServerThread extends Thread {
	private ServerSocket serverSocket;
	private Channel channel;
	private int port;
	
	public ServerThread(ServerSocket serverSocket, Channel channel, int port) {
		super();
		this.serverSocket = serverSocket;
		this.channel = channel;
		this.port = port;
	}

	public int getClientNum() {
		
		return 100;
	}
	
	public void chat(String name,String msgString) {
		
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
