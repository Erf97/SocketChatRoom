package chatroom;

import java.net.ServerSocket;
import java.util.ArrayList;

public class FileServerThread extends ServerThread{
	
	private ArrayList<FileClientThread> fileClientsList;
	private String filePath; // 上传的文件保存的位置，最好每个频道一个文件夹
	
	public FileServerThread(ServerSocket fileServerSocket, Channel channel, int port) {
		super(fileServerSocket, channel, port);
		this.fileClientsList = new ArrayList<FileClientThread>();
	}
	
	// TODO 文件线程
}
