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
	
	public ChatClientThread(Socket socket) {
		super();
		this.socket = socket;
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
			return true;
		
		default:
			chatServer.chat(user.getName(),msgString);
			System.out.println("chatted");
			return true;
		}
	}

	public void run() {
		String msgString = null;
		chatServer = Server.getServerThread(this);
		while(true) {
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
					System.out.println("received user info");
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
			writer.close();
			reader.close();
			socket.close();
			chatServer.removeClient(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
