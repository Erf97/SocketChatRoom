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
 */
public class Server {
	
	private ArrayList<Channel> channels; //Ƶ���б�
	private ArrayList<MainClientsThread> mainClients; //�ͻ��б�
	private ServerSocket mainServerSocket;
	private ServerThread serverThread;
	private int port = 1234;
	private Map<User,Channel> userChannelMap; //�û�-Ƶ������¼�û����ĸ�Ƶ������Ϣת������Ϊ��
	private Map<String,Channel> nameChannelMap;
	
	public Server() throws IOException {
		channels = new ArrayList<Channel>();
		mainClients = new ArrayList<MainClientsThread>();
		mainServerSocket = new ServerSocket(port);
		userChannelMap = new HashMap<User,Channel>();
		nameChannelMap = new HashMap<String,Channel>();
		serverThread = new ServerThread(mainServerSocket);
		serverThread.start();
	}
	
	/**
	 * 
	 * @return ��ǰƵ���б�
	 */
	public String getChannelsList() {
		if(channels.size() == 0) {
			return "�����е�ǰû��Ƶ����";
		}
		String listString = "Ƶ����\t����\t�Ƿ����\n";
		for(int i=0;i<channels.size();i++) {
			Server.Channel c = channels.get(i);
			String channelInfoString = c.getNameString() + "\t"
					+c.getCurrentClientNum() + "/" + c.getMax() + "\t"
					+(c.isEncrypted()?"��":"��") + "\n";
			listString += channelInfoString;
 		}
		return listString;
	}
	
	/**
	 * 
	 * @param client �����Ŀͻ��߳�
	 */
	synchronized public void addClients(MainClientsThread client) {
		mainClients.add(client);
	}
	
	/**
	 * 
	 * @param nameString Ƶ����
	 * @param passwordString ����
	 * @param max �������
	 * @return ��ӳɹ�/ʧ��
	 */
	synchronized public boolean createChannel(String nameString,String passwordString,int max) {
		if(nameString == null) {
			return false;
		}
		if(passwordString == null) {
			Channel channel = new Channel(nameString,max);
			channels.add(channel);
			nameChannelMap.put(nameString,channel);
		}
		else {
			Channel channel = new Channel(nameString,max,true,passwordString);
			channels.add(channel);
			nameChannelMap.put(nameString,channel);
		}
		return true;
	}
	
	/**
	 * �����У���ͻ��˷�����Ϣ�Ĺ��̶�Ҫͨ��������������У���Ҫֱ��ʹ��writer
	 * @param writer 
	 * @param msgString Ҫ���͵���Ϣ
	 */
	public void sendMsg(PrintWriter writer,String msgString) {
		writer.println(msgString);
		writer.flush();
	}
	
	public void channelBoardCast(Channel channel,String msgString) {
		for(int i=0;i<channel.getClients().size();i++) {
			sendMsg(channel.getClients().get(i).getWriter(),msgString);
		}
	}
	
	/**
	 * ��������Ϣ���͸�Ƶ���е����пͻ�
	 * @param msgString ������Ϣ
	 * @param channelNameString ���������ڵ�Ƶ����
	 * @param userNameString �������ǳ�
	 * @return
	 */
	public boolean chat(String msgString,Channel channel,String userNameString,boolean isAnonymous) {
		if(isAnonymous) {
			userNameString = "�����û�";
		}
		for(int i=0;i<channel.getClients().size();i++) {
			sendMsg(channel.getClients().get(i).getWriter(),(userNameString + "˵��" + msgString));
		}
		return true;
	}
	
	public boolean joinChannel(MainClientsThread client,Channel channel,String passwordString) {
		if(channel.isEncrypted()) {
			if(passwordString != channel.getPasswordString()) {
				sendMsg(client.getWriter(), "Ƶ���������");
				return false;
			}
			else {
				channel.addClientToChannel(client);
				userChannelMap.put(client.getUser(),channel);
				channelBoardCast(channel, "��ӭ"+client.getUser().getName()+"����Ƶ����");
				return true;
			}
		}
		else {
			channel.addClientToChannel(client);
			userChannelMap.put(client.getUser(),channel);
			channelBoardCast(channel, "��ӭ"+client.getUser().getName()+"����Ƶ����");
			return true;
		}
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
				if(createChannel(nameString,passwordString,max)) {
					return true;
				}
				else {
					sendMsg(writer, "����Ƶ��ʧ��");
				}
				
			case "/list":
				sendMsg(writer, getChannelsList());
				return true;
			
			case "/join": //����Ƶ��
				String channelNameString = sTokenizer.hasMoreTokens()?sTokenizer.nextToken():null;
				String channelPasswordString = sTokenizer.hasMoreTokens()?sTokenizer.nextToken():null;
				if(channelNameString == null) {
					return false;
				}
				Channel channel = nameChannelMap.get(channelNameString);
				if(!joinChannel(this,channel,channelPasswordString)) {
					sendMsg(writer, "����Ƶ��ʧ��");
				}
				else {
					return true;
				}
			
			case "/to": //˽��
				
				return true;
				
			case "/userlist": //��ʾƵ�����û�
				
				return true;
				
			case "/anonymous": //����
				setAnonymous(true);
				sendMsg(writer, "��������");
				return true;
				
			case "/no-anonymous": //ȡ��
				setAnonymous(false);
				sendMsg(writer, "����ȡ������");
				return true;
				
			case "/block": //������ĳ�û�����Ϣ
				
				return true;
				
			case "/vote": //����һ��ͶƱ
				
				return true;
				
			case "/all": //ȫƵ���㲥
				
				return true;
			
			case "/special": //��Ƶ����ĳ���û�����Ϊ�ر��ע
				
				return true;
				
			case "/exit": //�˳�Ƶ��
				
				return true; 
				
			default:
				if(userChannelMap.get(user) == null) {
					sendMsg(writer, "����û�м���Ƶ�����������ٷ��ԣ�");
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
						sendMsg(writer, "����������");
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
		
		public ArrayList<MainClientsThread> getClients() {
			return clients;
		}

		public void addClientToChannel(MainClientsThread client) {
			clients.add(client);
		}
		
		private String nameString;
//		private ServerSocket serverSocket;
		private int max;
		private boolean isEncrypted;
		private String passwordString;
		private ArrayList<MainClientsThread> clients;
		
		public Channel(String nameString,int max,boolean isEncrypted,String passwordString) {
			this.nameString = nameString;
			this.max = max;
			this.isEncrypted = isEncrypted;
			this.passwordString = passwordString;
			this.clients = new ArrayList<MainClientsThread>();
		}
		
		public Channel(String nameString,int max) {
			this(nameString,max,false,"");
		}
		
		public Channel(String nameString) {
			this(nameString,10);
		}
		
	}
}
