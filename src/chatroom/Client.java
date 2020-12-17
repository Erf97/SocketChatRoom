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
	// TODO ������������˵����ļ�����˵�
	
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
			System.out.println("��˿ں�Ϊ��" + port + ",IP��ַΪ��" + hostIp
					+ "�ķ���������ʧ��!" + "\r\n");
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
		// TODO: �����ļ�Ƶ��
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
		String listString = "Ƶ����\t��������\t�Ƿ����\t����\t�ӳ�\n";
//		System.out.println("105:" + sTokenizer.hasMoreTokens());
		while(sTokenizer.hasMoreTokens()) {
			StringTokenizer sTokenizer2 = new StringTokenizer(sTokenizer.nextToken(),"\t");
			String nameString = sTokenizer2.nextToken();
			String onlineNum = sTokenizer2.nextToken();
			String isEnString = sTokenizer2.nextToken().equals("true")?"��":"��";
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
		System.out.println("�������û����ƣ�");
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
					System.out.println("���������жϣ�");
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
						System.out.println("���������жϣ�");
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
						System.out.println("����Ƶ����/create + Ƶ����" + "\t\t��ѯƵ���б�/list" + "\t\t\t\t����Ƶ����/join + Ƶ����\n" +
										   "˽�ģ�/to" + "\t\t\t\t��ʾƵ�����û���/userlist" + "\t\t\t������/anonymous\n" +
										   "ȥ�䣺/no-anonymous" + "\t\t\t������ĳ�û�����Ϣ��/block" + "\t\t\t����һ��ͶƱ��/vote\n" +
										   "ȫƵ���㲥��/all" + "\t\t\t\t��Ƶ����ĳ���û�����Ϊ�ر��ע��/special  " + "\t�˳�Ƶ����/exit\n");
						break;
					case "/join":
						String nameString;
						if(!sTokenizer.hasMoreTokens()) {
							System.out.println("ȱʧƵ����������������");
						}
						else {
							nameString = sTokenizer.nextToken();
							sendMessage(mainWriter, "/list");
							if(namePortMap.get(nameString) == null) {
								System.out.println("û���ҵ���Ƶ��������������");
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
						// TODO �ļ������߼�
						
					}
					else
						sendMessage(mainWriter,msgString);
				}
			}
			System.out.println("���ѶϿ����ӣ�");
		}
	}
}
