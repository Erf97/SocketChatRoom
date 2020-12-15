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
 * 所有变量&函数请使用驼峰命名
 */
public class Server {
	
	private ArrayList<Channel> channels; //频道列表
	private ArrayList<MainClientsThread> mainClients; //客户列表
	private ServerSocket mainServerSocket;
	private ServerThread serverThread;
	private int port = 1234;
	private Map<User,String> userChannelMap; //用户-频道表，记录用户在哪个频道，消息转发依此为据
	
	public Server() throws IOException {
		channels = new ArrayList<Channel>();
		mainClients = new ArrayList<MainClientsThread>();
		mainServerSocket = new ServerSocket(port);
		userChannelMap = new HashMap<User,String>();
		serverThread = new ServerThread(mainServerSocket);
		serverThread.start();
	}
	
	/**
	 * 
	 * @return 当前频道列表
	 */
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
	
	/**
	 * 
	 * @param client 新增的客户线程
	 */
	synchronized public void addClients(MainClientsThread client) {
		mainClients.add(client);
	}
	
	/**
	 * 
	 * @param nameString 频道名
	 * @param passwordString 密码
	 * @param max 最大人数
	 * @return 添加成功/失败
	 */
	synchronized public boolean createChannel(String nameString,String passwordString,int max) {
		if(nameString == null) {
			return false;
		}
		if(passwordString == null) {
			channels.add(new Channel(nameString,max));
		}
		else {
			channels.add(new Channel(nameString,max,true,passwordString));
		}
		return true;
	}
	
	/**
	 * ！所有！向客户端发送消息的过程都要通过这个函数来进行，不要直接使用writer
	 * @param writer 
	 * @param msgString 要发送的信息
	 */
	public void sendMsg(PrintWriter writer,String msgString) {
		writer.println(msgString);
		writer.flush();
	}
	
	/**
	 * 将聊天信息发送给频道中的所有客户
	 * @param msgString 聊天信息
	 * @param channelNameString 发送人所在的频道名
	 * @param userNameString 发送人昵称
	 * @return
	 */
	public boolean chat(String msgString,String channelNameString,String userNameString,boolean isAnonymous) {
		if(isAnonymous) {
			userNameString = "匿名用户";
		}
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
		private boolean isAnonymous = false;
			
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
		
		public void setAnonymous(boolean isAnonymous) {
			this.isAnonymous = isAnonymous;
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
				
			case "/list":
				sendMsg(writer, getChannelsList());
				return true;
			
			case "/join": //加入频道
				
				return true;
				
			case "/to": //私聊
				
				return true;
				
			case "/userlist": //显示频道内用户
				
				return true;
				
			case "/anonymous": //匿名
				setAnonymous(true);
				sendMsg(writer, "您已匿名");
				return true;
				
			case "/no-anonymous": //取匿
				setAnonymous(false);
				sendMsg(writer, "您已取消匿名");
				return true;
				
			case "/block": //不接收某用户的消息
				
				return true;
				
			case "/vote": //发起一项投票
				
				return true;
				
			case "/all": //全频道广播
				
				return true;
			
			case "/special": //将频道内某个用户设置为特别关注
				
				return true;
				
			case "/exit": //退出频道
				
				return true; 
				
			default:
				if(userChannelMap.get(user) == null) {
					sendMsg(writer, "您还没有加入频道，请加入后再发言！");
					return true;
				}
				else {
					return chat(messageString, userChannelMap.get(user), user.getName(),isAnonymous);
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
