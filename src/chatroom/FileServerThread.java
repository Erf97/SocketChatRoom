package chatroom;

import java.net.ServerSocket;
import java.util.ArrayList;

public class FileServerThread extends ServerThread{
	
	private ArrayList<FileClientThread> fileClientsList;
	
	public FileServerThread(ServerSocket fileServerSocket, Channel channel, int port) {
		super(fileServerSocket, channel, port);
		this.fileClientsList = new ArrayList<FileClientThread>();
	}
	
	// TODO 文件线程
}
