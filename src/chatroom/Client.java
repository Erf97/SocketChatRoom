package chatroom;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client {
	
	private Socket mainSocket;
	private PrintWriter mainWriter;
	private BufferedReader mainReader;
	private Socket chatSocket;
	private PrintWriter chatWriter;
	private BufferedReader chatReader;
	private String hostIpString = "127.0.0.1";
	private String userNameString;
	
	private Map<String, Integer> namePortMap;
	private Map<String, String> nameTypeMap;
	
	private static Scanner sc = new Scanner(System.in);
	public static ArrayList<String> mainMenu= new ArrayList<String>(Arrays.asList("/create","/list"));
	public static ArrayList<String> localMenu= new ArrayList<String>(Arrays.asList("/help","/join"));
	// TODO 加入聊天命令菜单和文件命令菜单
	
	private boolean isConnected = false;
	private boolean isInChatChannel = false;
	private boolean isInFileChannel = false;
	
	
	public Client(int port,String hostIp,String name) {
		namePortMap = new HashMap<String,Integer>();
		nameTypeMap = new HashMap<String, String>();
		userNameString = name;
		if(connectMainServer(port, hostIp, name)) {
			new MainMsgReceiveThread().start();
			new MsgSendThread().start();
		}
	}
	
	synchronized public void addNamePortMap(String name,int port) {
		namePortMap.put(name, port);
	}
	
	synchronized public void addNameTypeMap(String name,String type) {
		nameTypeMap.put(name, type);
	}
	
	public boolean connectMainServer(int port, String hostIp, String name) {
		try {
			mainSocket = new Socket(hostIp, port);
			mainWriter = new PrintWriter(mainSocket.getOutputStream());
			mainReader = new BufferedReader(new InputStreamReader(mainSocket
					.getInputStream()));
			sendMessage(mainWriter,name + " " + mainSocket.getLocalAddress().toString());
			isConnected = true;
			return true;
		} catch (Exception e) {
			System.out.println("与端口号为：" + port + ",IP地址为：" + hostIp
					+ "的服务器连接失败!" + "\r\n");
			isConnected = false;// 
			return false;
		}
	}
	
	public boolean connectChannel(int port,String type) {
		if(type.equals("chat")) {
			try {
				chatSocket = new Socket(hostIpString,port);
				chatReader = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));
				chatWriter = new PrintWriter(chatSocket.getOutputStream());
				isInChatChannel = true;
				new ChatMsgReceiveThread().start();
				sendMessage(chatWriter, "#info");
				sendMessage(chatWriter,userNameString + " " + mainSocket.getLocalAddress().toString());
				return true;
			}catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		// TODO: 连接文件频道
		return true;
	}
	
	public double getChannelPing(int port) {
		try {
			double begintime = System.nanoTime();
			Socket testSocket = new Socket(hostIpString,port);
			PrintWriter testWriter = new PrintWriter(testSocket.getOutputStream());
			BufferedReader testReader = new BufferedReader(new InputStreamReader(testSocket
					.getInputStream()));
			sendMessage(testWriter, "#ping");
			System.out.println(testReader.readLine());
			double endtime = System.nanoTime();
			double costTime = (endtime - begintime)/1000000;
			testWriter.close();
			testReader.close();
			testSocket.close();
			return costTime;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public String getChannelList(String msgString) {
		StringTokenizer sTokenizer = new StringTokenizer(msgString,"&");
		String headString = sTokenizer.nextToken();
		String listString = "频道名\t在线人数\t是否加密\t类型\t延迟\n";
//		System.out.println("105:" + sTokenizer.hasMoreTokens());
		while(sTokenizer.hasMoreTokens()) {
			StringTokenizer sTokenizer2 = new StringTokenizer(sTokenizer.nextToken(),"\t");
			String nameString = sTokenizer2.nextToken();
			String onlineNum = sTokenizer2.nextToken();
			String isEnString = sTokenizer2.nextToken().equals("true")?"是":"否";
			String typeString = sTokenizer2.nextToken();
			int port = Integer.valueOf(sTokenizer2.nextToken());
			double ping = getChannelPing(port);
			addNamePortMap(nameString, port);
			addNameTypeMap(nameString, typeString);
			listString += nameString + "\t"
					+ onlineNum + "\t"
					+ isEnString + "\t"
					+ typeString + "\t"
					+ String.format("%.1f", ping) + "ms\n";
//			System.out.println(118);
		}
//		System.out.println("120:"+listString);
		return listString;
	}
	
	public void sendMessage(PrintWriter writer,String message) {
		writer.println(message);
		writer.flush();
	}
	
	public static void main(String[] args) {
		System.out.println("请输入用户名称：");
		String nameString = sc.nextLine();
		
		new Client(1000, "127.0.0.1", nameString);
	}

	class MainMsgReceiveThread extends Thread {
		
		public MainMsgReceiveThread() {
			super();
		}

		public void run() {
			String msgString = "";
			while(isConnected) {
				try {
					msgString = mainReader.readLine();
//					System.out.println("134:" + msgString);
					if(msgString.contains("#channel&")) {
						System.out.println(getChannelList(msgString));
						continue;
					}
					System.out.println(msgString);
				} catch (IOException e) {
					System.out.println("连接意外中断！");
					isConnected = false;
					e.printStackTrace();
				}
			}
		}
	}
	
	class ChatMsgReceiveThread extends Thread {
			
			public ChatMsgReceiveThread() {
				super();
			}
			
			public void run() {
				String msgString = "";
				while(isInChatChannel) {
					try {
						msgString = chatReader.readLine();
						System.out.println(msgString);
					} catch (IOException e) {
						System.out.println("连接意外中断！");
						isInChatChannel = false;
						e.printStackTrace();
					}
				}
			}
		}
	
	class MsgSendThread extends Thread {

		public MsgSendThread() {
			super();
		}

		public void run() {
			while(isConnected) {
				String msgString = sc.nextLine();
				if(Utils.isLocalCommand(msgString)){
					StringTokenizer sTokenizer = new StringTokenizer(msgString);
					String headString = sTokenizer.nextToken();
					switch(headString){
					case "/help":
						System.out.println("创建频道：/create + 频道名" + "\t\t查询频道列表：/list" + "\t\t\t\t加入频道：/join + 频道名\n" +
										   "私聊：/to" + "\t\t\t\t显示频道内用户：/userlist" + "\t\t\t匿名：/anonymous\n" +
										   "去匿：/no-anonymous" + "\t\t\t不接收某用户的消息：/block" + "\t\t\t发起一项投票：/vote\n" +
										   "全频道广播：/all" + "\t\t\t\t将频道内某个用户设置为特别关注：/special  " + "\t退出频道：/exit\n");
						break;
					case "/join":
						String nameString;
						if(!sTokenizer.hasMoreTokens()) {
							System.out.println("缺失频道名，请重新输入");
						}
						else {
							nameString = sTokenizer.nextToken();
							sendMessage(mainWriter, "/list");
							if(namePortMap.get(nameString) == null) {
								System.out.println("没有找到该频道，请重新输入");
							}
							else {
								int port = namePortMap.get(nameString);
								String type = nameTypeMap.get(nameString);
								connectChannel(port, type);
							}
						}
						break;
					}
				}
				else if(Utils.isMainCommand(msgString)) {
					sendMessage(mainWriter,msgString);
				}
				else {
					if(isInChatChannel) {
						System.out.println("sent to chat channel");
						sendMessage(chatWriter, msgString);
					}
					else if(isInFileChannel) {
						// TODO 文件发送逻辑
						
					}
					else
						sendMessage(mainWriter,msgString);
				}
			}
			System.out.println("您已断开连接！");
		}
	}
}
