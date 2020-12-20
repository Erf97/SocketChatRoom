package chatroom;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileClientThread extends Thread {
	
	private ServerSocket fileStreamServerSocket;
	private Socket messageSocket;
	private Socket fileSocket;
	private int filePort;
	private BufferedReader msgReader;
	private PrintWriter msgWriter;
	private FileServerThread fileServerThread;
	private User user;
	private boolean isConnect;
	private String fileDownloadPath;
	private String fileUploadPath;
	private FileInOutThread fileInOutThread;

	public FileClientThread(Socket messageSocket,FileServerThread fileServerThread) throws Exception {
		super();
		this.messageSocket = messageSocket;
		this.fileServerThread = fileServerThread;
		this.filePort = Server.getPort();
		this.isConnect = true;
		try {
			this.msgReader = new BufferedReader(new InputStreamReader(messageSocket.getInputStream()));
			this.msgWriter = new PrintWriter(messageSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public PrintWriter getMsgWriter() {
		return msgWriter;
	}
	
	public User getUser() {
		return user;
	}

	public boolean prase(String msgString) throws IOException {
		StringTokenizer sTokenizer = new StringTokenizer(msgString);
		if (!sTokenizer.hasMoreTokens()) {
			Server.sendMsg(msgWriter, "您的输入为空");
			return false;
		}
		String cmdString = sTokenizer.nextToken();
		switch (cmdString) {
		case "/filelist":
			String liString = fileServerThread.getFileList();
			StringTokenizer sTokenizer1 = new StringTokenizer(liString, "\t");
			while (sTokenizer1.hasMoreTokens()) {
				Server.sendMsg(msgWriter, sTokenizer1.nextToken());
			}
			return true;

		case "/fileup":
			try {
				fileSocket = fileStreamServerSocket.accept();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			String fileName = msgReader.readLine();
			System.out.println("准备接收文件"+ fileName);
			fileUploadPath = fileServerThread.getFilePath() + fileName;
			this.fileInOutThread = new FileInOutThread(fileSocket,0);
			fileInOutThread.start();
			return true;

		case "/filedown":
			try {
				fileSocket = fileStreamServerSocket.accept();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			fileDownloadPath = msgReader.readLine();
			this.fileInOutThread = new FileInOutThread(fileSocket,1);
			fileInOutThread.start();
			return true;
		
		case "/exit":
			try {
				Server.sendMsg(msgWriter, "exit000");
				if(fileInOutThread != null) {
					fileInOutThread.fileWriter.close();
					fileInOutThread.fileReader.close();
					fileInOutThread.fileSocket.close();
				}
				fileStreamServerSocket.close();
				isConnect = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		default:
			Server.sendMsg(msgWriter, msgString);
			return true;
		}
	}
	
	public void run() {
		String msgString = null;
		while (isConnect) {
			try {
				msgString = msgReader.readLine();
				if (msgString.equals("#ping")) {
					Server.sendMsg(msgWriter, msgString);
					break;
				} else if (msgString.equals("#info")) {
					msgString = msgReader.readLine();
					StringTokenizer sTokenizer = new StringTokenizer(msgString);
					user = new User(sTokenizer.nextToken(), sTokenizer.nextToken());
					Server.sendMsg(msgWriter, String.valueOf(filePort));
					try {
						fileStreamServerSocket = new ServerSocket(filePort);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else {
					if (!prase(msgString)) {
						Server.sendMsg(msgWriter, "请重新输入");
					}
				}
			} catch (IOException e) {
				isConnect = false;
				break;
			}
		}
		try {
			msgWriter.close();
			msgReader.close();
			messageSocket.close();
			fileServerThread.removeClient(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	class FileInOutThread extends Thread {
		
		private Socket fileSocket;
		private DataOutputStream fileWriter = null;
		private DataInputStream fileReader = null;
		private int inOrOut;

		public FileInOutThread(Socket fileSocket,int inOrOut) {
			this.fileSocket = fileSocket;
			this.inOrOut = inOrOut;
			try {
				fileReader = new DataInputStream(new BufferedInputStream(fileSocket.getInputStream()));
				fileWriter = new DataOutputStream(fileSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void sendFileToCilent() {
			try {
				DataInputStream localfileReader = new DataInputStream(new BufferedInputStream(new FileInputStream(fileDownloadPath)));
//				fileWriter.writeLong((long) file.length());
//				fileWriter.flush();
				int bufferSize = 8192;
				byte[] buf = new byte[bufferSize];
				while (true) {
					int read = 0;
					if (localfileReader != null) {
						read = localfileReader.read(buf);
					}
					if (read == -1) {
						break;
					}
					fileWriter.write(buf, 0, read);
				}
				localfileReader.close();
				fileWriter.flush();
				fileWriter.close();
			} catch (Exception e) {
				e.printStackTrace();
				Server.sendMsg(msgWriter, "传输意外终止\n");
				return;
			}
			
		}

		public void getFileFromCilent() throws Exception {
			try {
				int bufferSize = 8192;
				byte[] buf = new byte[bufferSize];
				DataOutputStream localFileWriter = new DataOutputStream(
						new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(fileUploadPath))));
				Server.sendMsg(msgWriter, "文件开始上传");
				while (true) {
					int read = 0;
					if (fileReader != null)
						read = fileReader.read(buf);
					if (read == -1)
						break;
					localFileWriter.write(buf,0,read);
				}
				Server.sendMsg(msgWriter, "文件上传完成");
				fileServerThread.boardCast(user.getName()+"上传了"+fileUploadPath);
				fileReader.close();
				localFileWriter.flush();
				localFileWriter.close();
			} catch (Exception e) {
				e.printStackTrace();
				Server.sendMsg(msgWriter, "传输意外终止" + "\n");
				return;
			}
		}
		
		public void run() {
			try {
				if(inOrOut == 0) { // in
					getFileFromCilent();
				}
				else if(inOrOut == 1) { // out
					sendFileToCilent();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
