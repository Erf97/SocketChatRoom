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

public class Server {
	
	private ArrayList<Channel> channels;
	private ArrayList<MainClientsThread> mainClients;
	private ServerSocket mainServerSocket;
	private ServerThread serverThread;
	private int port = 1234;
	private Map<User,String> userChannelMap;
	
	public Server() throws IOException {
		channels = new ArrayList<Channel>();
		mainClients = new ArrayList<MainClientsThread>();
		mainServerSocket = new ServerSocket(port);
		userChannelMap = new HashMap<User,String>();
		serverThread = new ServerThread(mainServerSocket);
		serverThread.start();
	}
	
	public String getChannelsList() {
		if(channels.size() == 0) {
			return "大厅中当前没有频道！";
		}
		String listString = "频道名\t人数\t是否加密\n";
		for(int i=0;i<channels.size();i++) {
			Server.Channel c = channels.get(i);
			String channelInfoString = c.getNameString() + "\t"
					+c.getCurrentClientNum() + "/" + c.getMax() + "\t"
					+(c.isEncrypted()?"是":"否") + "\n";
			listString += channelInfoString;
 		}
		return listString;
	}
	
	synchronized public void addClients(MainClientsThread client) {
		mainClients.add(client);
	}
	
	synchronized public boolean createChannel(String nameString,String passwordString,int max) {
		if(nameString == null) {
			return false;
		}
		if(passwordString == null) {
			channels.add(new Channel(nameString,max));
			return true;
		}
		else {
			channels.add(new Channel(nameString,max,true,passwordString));
			return true;
		}
	}
	
	public void sendMsg(PrintWriter writer,String msgString) {
		writer.println(msgString);
		writer.flush();
	}
	
	public boolean send(String msgString,String channelNameString,String userNameString) {
		for(int i=0;i<mainClients.size();i++) {
			if(userChannelMap.get(mainClients.get(i).getUser()) == channelNameString) {
				sendMsg(mainClients.get(i).getWriter(),(userNameString + "说：" + msgString));
			}
		}
		return true;
	}
	
	public static void main(String[] args) {
		try {
			new Server();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class MainClientsThread extends Thread {
		
		private Socket socket;
		private User user;
		private BufferedReader reader;
		private PrintWriter writer;
			
		public MainClientsThread(Socket socket) {
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
		
		public User getUser() {
			return user;
		}

		public BufferedReader getReader() {
			return reader;
		}

		public PrintWriter getWriter() {
			return writer;
		}
		
		public boolean prase(String messageString) {
			StringTokenizer sTokenizer = new StringTokenizer(messageString);
			String cmdString = sTokenizer.nextToken();
			switch (cmdString) {
			case "/create":
				String nameString = sTokenizer.hasMoreTokens()?sTokenizer.nextToken():null;
				String passwordString = sTokenizer.hasMoreTokens()?sTokenizer.nextToken():null;
//				int max = sTokenizer.hasMoreTokens()?Integer.valueOf(sTokenizer.nextToken()):null;
				int max = 10;
				return createChannel(nameString,passwordString,max);

			default:
				if(userChannelMap.get(user) == null) {
					sendMsg(writer, "您还没有加入频道，请加入后再发言！");
					return true;
				}
				else {
					return send(messageString, userChannelMap.get(user), user.getName());
				}	
			}
		}

		public void run() {
			String messageString = null;
			while(true) {
				try {
					messageString = reader.readLine();
					if(!prase(messageString)) {
						sendMsg(writer, "输入命令有误！");
					}
				} catch (IOException e) {
					break;
				}
			}
			
		}
	}
	
	class ServerThread extends Thread {
		
		private ServerSocket serverSocket;
		
		public ServerThread(ServerSocket serverSocket) {
			this.serverSocket = serverSocket;
		}
		
		public void run() {
			while(true) {
				try {
					Socket socket = serverSocket.accept();
					MainClientsThread client = new MainClientsThread(socket); 
					client.start();
					sendMsg(client.getWriter(), getChannelsList());
					System.out.println(getChannelsList());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class Channel{
		
		public int getMax() {
			return max;
		}

		public boolean isEncrypted() {
			return isEncrypted;
		}

		public String getPasswordString() {
			return passwordString;
		}

		public String getNameString() {
			return nameString;
		}
		
		public int getCurrentClientNum() {
			return clients.size();
		}
		
		public void addClientToChannel(User client) {
			clients.add(client);
		}
		
		private String nameString;
//		private ServerSocket serverSocket;
		private int max;
		private boolean isEncrypted;
		private String passwordString;
		private ArrayList<User> clients;
		
		public Channel(String nameString,int max,boolean isEncrypted,String passwordString) {
			this.nameString = nameString;
			this.max = max;
			this.isEncrypted = isEncrypted;
			this.passwordString = passwordString;
			this.clients = new ArrayList<User>();
		}
		
		public Channel(String nameString,int max) {
			this(nameString,max,false,"");
		}
		
		public Channel(String nameString) {
			this(nameString,10);
		}
		
	}
}
