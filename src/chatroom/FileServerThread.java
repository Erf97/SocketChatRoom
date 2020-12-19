package chatroom;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class FileServerThread extends ServerThread {
	private ArrayList<FileClientThread> fileClientsList; // 用户
	private String serverFilePath = "D:\\ProjectFile\\test\\"; // 上传的文件保存的位置，最好每个频道一个文件夹
	private ArrayList<String> fileList;
	private ServerSocket fileServerSocket;
	private ServerSocket messageServerSocket;
	private Channel channel;
	private int filePort;
	private int messagePort;

	public FileServerThread(ServerSocket fileServerSocket, ServerSocket messageServerSocket, Channel channel,
			int filePort, int messagePort) {
		super(fileServerSocket, channel, filePort);
		this.fileClientsList = new ArrayList<FileClientThread>();
		this.fileList = new ArrayList<String>();
		this.messageServerSocket = messageServerSocket;
		this.messagePort = messagePort;
	}

	public int getClientNum() {
		return fileClientsList.size();
	}

	synchronized public void removeClient(FileClientThread fThread) {
		fileClientsList.remove(fThread);
	}

	public void createFilePath() {
		// to do 为每个文件频道创建一个服务器存储地址,文件夹名为端口号
		serverFilePath += String.valueOf(filePort);
		File file = new File(serverFilePath);
		if (!file.exists())
			file.mkdir();
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
		return this.messageServerSocket;
	}

	public int getMessagePort() {
		return this.messagePort;
	}

	public void run() {
		while (true) {
			try {
				Socket messageSocket = messageServerSocket.accept();
				Socket fileSocket = fileServerSocket.accept();
				FileClientThread fileClient = new FileClientThread(fileSocket, messageSocket);
				fileClientsList.add(fileClient);
				fileClient.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class ServerFileTransfer extends Thread {
		private DataOutputStream out = null;
		private DataOutputStream fileOut = null;
		private DataInputStream fileIn = null;
		private Socket fileSocket;
		private Socket messageSocket;
		private BufferedReader reader;
		private PrintWriter writer;
		private boolean isConnect;

		public ServerFileTransfer(Socket fileSocket,Socket messageSocket) {
			this.fileSocket = fileSocket;
			this.messageSocket = messageSocket;
			try {
				this.reader = new BufferedReader(new InputStreamReader(messageSocket.getInputStream()));
				this.writer = new PrintWriter(messageSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public DataInputStream getInputStream() throws Exception {
			try {
				fileIn = new DataInputStream(new BufferedInputStream(fileSocket.getInputStream()));
				return fileIn;
			} catch (Exception e) {
				e.printStackTrace();
				if (fileIn == null)
					fileIn.close();
				throw e;
			} finally {
			}
		}

		public DataOutputStream getOutputStream() throws Exception {
			try {
				fileOut = new DataOutputStream(fileSocket.getOutputStream());
				return fileOut;
			} catch (Exception e) {
				e.printStackTrace();
				if (fileOut == null)
					fileOut.close();
				throw e;
			} finally {
			}
		}
		
		public void upload() {

		}

		public void download(String fileDownName) throws Exception {
			try {
				fileIn = getInputStream();
				out = new DataOutputStream(fileSocket.getOutputStream());
				out.writeByte(0x1);
				out.flush();
			} catch (Exception e) {
				e.printStackTrace();
				if (out != null)
					out.close();
			}
			try {
				int bufferSize = 8192;
				byte[] buf = new byte[bufferSize];
				String savePath = serverFilePath + fileDownName;
				fileIn = getInputStream();
				fileOut = new DataOutputStream(
						new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(savePath))));
				long fileLength = fileIn.readLong();
				long len = 0;
				Server.sendMsg(writer, "开始下载文件！");
				while (true) {
					int read = 0;
					if (fileIn != null)
						read = fileIn.read(buf);
					len += read;
					if (read == -1)
						break;
					fileOut.write(buf,0,read);
				}
				Server.sendMsg(writer, "文件接收了" + (len * 100 / fileLength) + "%");
				out.close();
				fileOut.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("接收消息错误" + "\n");
				return;
			}

		}
		
		public void run() {
			
			
		}

	}

}
