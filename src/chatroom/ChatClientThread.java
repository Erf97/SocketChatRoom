package chatroom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class ChatClientThread extends Thread {
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private User user;
	private ChatServerThread chatServer;
	private boolean isConnect;
	
	public ChatClientThread(Socket socket,ChatServerThread chatServer) {
		super();
		this.socket = socket;
		this.chatServer = chatServer;
		this.isConnect = true;
		try {
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.writer = new PrintWriter(socket.getOutputStream());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public PrintWriter getWriter() {
		return writer;
	}

	public User getUser() {
		return user;
	}
	
	public boolean prase(String msgString) {
		StringTokenizer sTokenizer = new StringTokenizer(msgString);
		if(!sTokenizer.hasMoreTokens()) {
			Server.sendMsg(writer, "您的输入为空");
			return false;
		}
		String cmdString = sTokenizer.nextToken();
		switch (cmdString) {
		case "/exit":
			//TODO 离开频道
			chatServer.boardCast(user.getName()+"已经离开频道");
			this.isConnect = false;
			return true;
		
		default:
			chatServer.chat(user.getName(),msgString);
			System.out.println("chatted");
			return true;
		}
	}

	public void run() {
		String msgString = null;
		while(this.isConnect) {
			try {
				msgString = reader.readLine();
				if(msgString.equals("#ping")) {
					Server.sendMsg(writer, msgString);
					break;
				}
				else if(msgString.equals("#info")) {
					msgString = reader.readLine();
					StringTokenizer sTokenizer = new StringTokenizer(msgString);
					this.user = new User(sTokenizer.nextToken(), sTokenizer.nextToken());
					chatServer.boardCast("欢迎"+user.getName()+"进入频道");
				}
				else {
					if(!prase(msgString)) {
						Server.sendMsg(writer, "请重新输入");
					}
				}
					
			} catch (IOException e) {
				break;
			}
		}
		try {
			Server.sendMsg(writer, "exit000");
			writer.close();
			reader.close();
			socket.close();
			chatServer.removeClient(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
