package chatroom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServerThread extends ServerThread {
	
private ArrayList<ChatClientThread> chatClientsList;
	
	public ChatServerThread(ServerSocket chatServerSocket, Channel channel, int port) {
		super(chatServerSocket, channel, port);
		this.chatClientsList = new ArrayList<ChatClientThread>();
	}
	
	public int getClientNum() {
		return chatClientsList.size();
	}
	
	synchronized public void removeClient(ChatClientThread cThread) {
		chatClientsList.remove(cThread);
	}

	public void chat(String userNameString,String msgString) {
		System.out.println(chatClientsList.size());
		for(int i=0;i<chatClientsList.size();i++) {
			System.out.println(userNameString+":"+msgString);
			Server.sendMsg(chatClientsList.get(i).getWriter(), userNameString+":"+msgString);
		}
	}
	
	public void boardCast(String msgString) {
		for(int i=0;i<chatClientsList.size();i++) {
			Server.sendMsg(chatClientsList.get(i).getWriter(),msgString);
		}
	}

	public void run() {
		while(true) {
			try {
				Socket socket = getServerSocket().accept();
				ChatClientThread client = new ChatClientThread(socket);
				chatClientsList.add(client);
				Server.putChatServerThread(client, this);
				client.start();
				System.out.println("channel "+ getChannel().getNameString() + 
						" has "+ getClientNum() + " members");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
