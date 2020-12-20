package chatroom;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.File;

public class FileServerThread extends ServerThread {
	private ArrayList<FileClientThread> fileClientsList; 
	private String serverFilePath = "D:/ProjectFile/test/"; 
	private ArrayList<String> fileList;

	public FileServerThread(ServerSocket fileServerSocket, Channel channel,int msgPort) {
		super(fileServerSocket, channel, msgPort);
		this.fileClientsList = new ArrayList<FileClientThread>();
		this.fileList = new ArrayList<String>();
		this.serverFilePath += String.valueOf(msgPort) + "/";
		File file = new File(serverFilePath);
		System.out.println(serverFilePath);
		if (!file.exists()) {
			System.out.println("创建文件夹");
			file.mkdirs();
		}
	}

	public int getClientNum() {
		return fileClientsList.size();
	}

	synchronized public void removeClient(FileClientThread fThread) {
		fileClientsList.remove(fThread);
	}

	public String getFilePath() {
		return serverFilePath;
	}

	public String getFileList() {
		String listString = null;
		for (int i = 0; i < fileList.size(); i++) {
			listString += fileList.get(i) + "\t";
		}
		listString += "\n";
		return listString;
	}
	
	public void addFile(String fileName) {
		fileList.add(fileName);
	}

	public ServerSocket getMessageServerSocket() {
		return getServerSocket();
	}
	
	public void boardCast(String msgString) {
		for(int i=0;i<fileClientsList.size();i++) {
			Server.sendMsg(fileClientsList.get(i).getMsgWriter(),msgString);
		}
	}

	public void run() {
		while (true) {
			try {
				Socket messageSocket = getServerSocket().accept();
				FileClientThread fileClient = new FileClientThread(messageSocket,this);
				fileClientsList.add(fileClient);
				fileClient.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	

}



