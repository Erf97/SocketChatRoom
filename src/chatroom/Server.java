package chatroom;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 
 * @author Erfan
 * ���б���&������ʹ���շ�����
 * �����Ҫ�����߳��иı乫������������ʹ�ù��������������ṩ�ķ������Ҹ÷���������synchronized���Σ�����ֱ���޸�
 */
public class Server {
	
	private static int port = 1000;
	private ServerSocket mainServerSocket;
	private ArrayList<ServerThread> channeList;
	
	public static void main(String[] args) throws IOException {
		new Server();
	}
	
	public Server() throws IOException {
		this.mainServerSocket = new ServerSocket(getPort());
		this.channeList = new ArrayList<ServerThread>();
		MainServerThread mainServerThread = new MainServerThread(mainServerSocket);
		mainServerThread.start();
	}
	
	synchronized public static int getPort() {
		return port++;
	}
	
	public static void sendMsg(PrintWriter writer,String msgString) {
		writer.println(msgString);
		writer.flush();
	}
	
	synchronized public boolean createChannel(String nameString,String passwordString,String type) {
		if(nameString == null || (type == null || (!type.equals("chat") && !type.equals("file")))){
			return false;
		}
		ServerSocket serverSocket;
		try {
			int port = getPort();
			serverSocket = new ServerSocket(port);
			Channel channel;
			if(passwordString == null) {
				channel = new Channel(nameString, type);
			}
			else {
				channel = new Channel(nameString,true,passwordString,type);
			}
			if(type.equals("chat")) {
				ChatServerThread chatServerThread = new ChatServerThread(serverSocket, channel, port);
				channeList.add(chatServerThread);
				chatServerThread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public String getChannelList(boolean isShow) {
		String listString = null;
		if(isShow) {
			listString = "#channel&";
		}
		else {
			listString = "#channel-not-show&";
		}
		for(int i=0;i<channeList.size();i++) {
			ServerThread serverThread = channeList.get(i);
			Channel channel = serverThread.getChannel();
			listString += channel.getNameString() + "\t"
						+ serverThread.getClientNum() + "\t"
						+ channel.isEncrypted() + "\t"
						+ channel.getType() + "\t"
						+ serverThread.getPort() + "\n";
		}
		System.out.println(listString);
		return listString;
	}
	
	class MainServerThread extends Thread {
		
		private ServerSocket mainServerSocket;

		public MainServerThread(ServerSocket mainServerSocket) {
			super();
			this.mainServerSocket = mainServerSocket;
		}
		
		public void run() {
			while(true) {
				try {
					Socket socket = mainServerSocket.accept();
					MainClientThread client = new MainClientThread(socket);
					client.start();
					sendMsg(client.getWriter(), getChannelList(true));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	
	class MainClientThread extends Thread {
		
		private Socket socket;
		private BufferedReader reader;
		private PrintWriter writer;
		private User user;
		
		public MainClientThread(Socket socket) {
			super();
			this.socket = socket;
			try {
				this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				this.writer = new PrintWriter(socket.getOutputStream());
				String userInfoString = reader.readLine();
				StringTokenizer sTokenizer = new StringTokenizer(userInfoString);
				this.user = new User(sTokenizer.nextToken(), sTokenizer.nextToken());
				System.out.println(userInfoString);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public PrintWriter getWriter() {
			return writer;
		}

		public boolean prase(String messageString) {
			StringTokenizer sTokenizer = new StringTokenizer(messageString);
			if(!sTokenizer.hasMoreTokens()) {
				sendMsg(writer, "��������Ϊ��");
				return false;
			}
			String cmdString = sTokenizer.nextToken();
			switch (cmdString) {
			case "/create":
				String nameString = sTokenizer.hasMoreTokens()?sTokenizer.nextToken():null;
				String type = sTokenizer.hasMoreTokens()?sTokenizer.nextToken():null;
				String passwordString = sTokenizer.hasMoreTokens()?sTokenizer.nextToken():null;
				if(createChannel(nameString,passwordString,type)) {
					sendMsg(writer, "������ȡƵ����Ϣ...");
					sendMsg(writer, getChannelList(true));
					return true;
				}
				else {
					sendMsg(writer, "����Ƶ��ʧ��");
					return false;
				}
				
			case "/list":
				sendMsg(writer, "������ȡƵ����Ϣ...");
				sendMsg(writer, getChannelList(true));
				return true;
				
			case "/update_channel":
				sendMsg(writer, getChannelList(false));
				return true;
			
			default:
				//Ӧ�ò����ߵ���
				return true;
			}
		}
				
		public void run() {
			String msgString = null;
			while(true) {
				try {
					msgString = reader.readLine();
					if(!prase(msgString)) {
						sendMsg(writer, "����������");
					}
				} catch (IOException e) {
					break;
				}
			}
		}
	}
	
}
