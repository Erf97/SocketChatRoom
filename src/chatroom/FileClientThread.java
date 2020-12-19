package chatroom;

import java.net.Socket;
import java.util.StringTokenizer;

import javax.sound.midi.VoiceStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileClientThread extends Thread {

	private Socket messageSocket;
	private Socket fileSocket;
	private BufferedReader reader;
	private PrintWriter writer;
	private FileServerThread fileServerThread;
	private User user;
	private boolean isConnect;
	private String fileUpName;
	private String fileDownName;
	private String fileUpPath;
	private String fileDownPath;
	private ClientFileTransfer clientFileTransfer;

	public FileClientThread(Socket fileSocket, Socket messageSocket) throws Exception {
		super();
		this.fileSocket = fileSocket;
		this.messageSocket = messageSocket;
		this.fileServerThread = fileServerThread;
		this.fileUpName = fileUpName;
		this.fileDownName = fileDownName;
		this.fileUpPath = fileUpPath;
		this.fileDownPath = fileDownPath;
		this.isConnect = true;
		try {
			this.reader = new BufferedReader(new InputStreamReader(messageSocket.getInputStream()));
			this.writer = new PrintWriter(messageSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	

	public void run() {
		while (this.isConnect) {
			clientFileTransfer.start();
		}

	}

	class ClientFileTransfer extends Thread {
//		private static String serverPath = "D:\\ProjectFile\\test\\";
		private DataOutputStream out = null;
		private DataOutputStream fileOut = null;
		private DataInputStream fileIn = null;

		public ClientFileTransfer() {
			try {
				fileIn = getInputStream();
			} catch (Exception e) {
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
			File file = new File(fileUpPath);
			try {
				fileIn = new DataInputStream(new BufferedInputStream(new FileInputStream(fileUpPath)));
				fileOut = getOutputStream();
				fileOut.writeLong((long) file.length());
				fileOut.flush();
				int bufferSize = 8192;
				byte[] buf = new byte[bufferSize];
				while (true) {
					int read = 0;
					if (fileIn != null) {
						read = fileIn.read(buf);
					}
					if (read == -1) {
						break;
					}
					fileOut.write(buf, 0, read);
				}
				fileOut.flush();
				fileIn.close();
				fileOut.close();
				Server.sendMsg(writer, "�ļ��������" + "\n");
			} catch (Exception e) {
				e.printStackTrace();
				Server.sendMsg(writer, "�����ļ�����" + "\n");
				return;
			}
			
		}

		public void download() throws Exception {
			try {
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
				fileIn = getInputStream();
				fileOut = new DataOutputStream(
						new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(fileDownPath))));
				long fileLength = fileIn.readLong();
				long len = 0;
				Server.sendMsg(writer, "��ʼ�����ļ���");
				while (true) {
					int read = 0;
					if (fileIn != null)
						read = fileIn.read(buf);
					len += read;
					if (read == -1)
						break;
					fileOut.write(buf,0,read);
				}
				Server.sendMsg(writer, "�ļ�������" + (len * 100 / fileLength) + "%");
				out.close();
				fileOut.close();
			} catch (Exception e) {
				e.printStackTrace();
				Server.sendMsg(writer, "�����ļ�����" + "\n");
				return;
			}
		}
	}
	
	public boolean prase(String msgString) throws IOException {
		StringTokenizer sTokenizer = new StringTokenizer(msgString);
		if (!sTokenizer.hasMoreTokens()) {
			Server.sendMsg(writer, "��������Ϊ��");
			return false;
		}
		String cmdString = sTokenizer.nextToken();
		switch (cmdString) {
		case "/filelist":
			// չʾ�ļ�����
			String liString = fileServerThread.getFileList();
			StringTokenizer sTokenizer1 = new StringTokenizer(liString, "\t");
			while (sTokenizer1.hasMoreTokens()) {
				Server.sendMsg(writer, sTokenizer1.nextToken());
			}
			return true;

		case "/fileUp":
			// �ļ��ϴ���Ϣ���ļ������ļ�����·��
			String fileInformation1 = reader.readLine();
			StringTokenizer sTokenizer2 = new StringTokenizer(fileInformation1);
			if (sTokenizer2.countTokens() == 2) {
				fileUpName = sTokenizer2.nextToken();
				fileUpPath = sTokenizer2.nextToken();
				Server.sendMsg(writer, "#fileUp");
				

			} else {
				Server.sendMsg(writer, "�������ļ������ļ�����·����");
			}

			return true;

		case "/fileDown":
			// �ļ�������Ϣ���ļ������ļ����ص�ַ
			String fileInformation2 = reader.readLine();
			StringTokenizer sTokenizer3 = new StringTokenizer(fileInformation2);
			if (sTokenizer3.countTokens() == 2) {
				fileDownName = sTokenizer3.nextToken();
				fileDownPath = sTokenizer3.nextToken();
				Server.sendMsg(writer, "#fileDown");
				try {
					clientFileTransfer.download();
				} catch (Exception e) {
					Server.sendMsg(writer, "����ʧ�ܣ�");
					e.printStackTrace();
				}
			} else {
				Server.sendMsg(writer, "�������ļ������ļ�����·����");
			}
			return true;

		default:
			Server.sendMsg(writer, "��Ч�����");
			return false;
		}
	}
	
	public void run() {
		String msgString = null;
		while (this.isConnect) {
			try {
				msgString = reader.readLine();
				if (msgString.equals("#ping")) {
					Server.sendMsg(writer, msgString);
					break;
				} else if (msgString.equals("#info")) {
					msgString = reader.readLine();
					StringTokenizer sTokenizer = new StringTokenizer(msgString);
					this.user = new User(sTokenizer.nextToken(), sTokenizer.nextToken());
				} else {
					if (!prase(msgString)) {
						Server.sendMsg(writer, "����������");
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
			messageSocket.close();
			fileSocket.close();
			clientFileTransfer.fileOut.close();
			clientFileTransfer.fileIn.close();
			fileServerThread.removeClient(this);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
