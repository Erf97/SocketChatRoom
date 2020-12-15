import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	private static Scanner sc = new Scanner(System.in);
	private static String[] menu= {"/help"};
	
	private boolean isConnected = false;
	
	
	public Client(int port,String hostIp,String name) {
		if(connectServer(port, hostIp, name)) {
			new MsgReceiveThread(reader).start();
			new MsgSendThread(writer).start();
		}
	}
	
	public boolean connectServer(int port, String hostIp, String name) {
		try {
			socket = new Socket(hostIp, port);
			writer = new PrintWriter(socket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
			sendMessage(name + " " + socket.getLocalAddress().toString());
			isConnected = true;
			return true;
		} catch (Exception e) {
			System.out.println("与端口号为：" + port + ",IP地址为：" + hostIp
					+ "的服务器连接失败!" + "\r\n");
			isConnected = false;// 
			return false;
		}
	}
	
	public void sendMessage(String message) {
		writer.println(message);
		writer.flush();
	}
	
	public static void main(String[] args) {
		System.out.println("请输入用户名称：");
		String nameString = sc.nextLine();
		
		new Client(1234, "127.0.0.1", nameString);
	}

	class MsgReceiveThread extends Thread {
		
		private BufferedReader reader;
		
		public MsgReceiveThread(BufferedReader reader) {
			super();
			this.reader = reader;
		}
		
		public void run() {
			String msgString = "";
			while(isConnected) {
				try {
					msgString = reader.readLine();
					System.out.println(msgString);
				} catch (IOException e) {
					System.out.println("连接意外中断！");
					isConnected = false;
				}
			}
		}
	}
	
	class MsgSendThread extends Thread {
		
		private PrintWriter writer;

		public MsgSendThread(PrintWriter writer) {
			super();
			this.writer = writer;
		}
		
		public void run() {
			while(isConnected) {
				String msgString = sc.nextLine();
				boolean flag = false;
				for(int i=0;i<menu.length;i++){
					if(menu[i].equals(msgString)){
						flag = true;
						break;
					}
				}
				if(flag){
					switch(msgString){
					case "/help":
						System.out.println("创建频道：/create + 频道名  " + "查询频道列表：/list                  " + "加入频道：/join + 频道名\n" +
										   "私聊：/to                " + "显示频道内用户：/userlist             " + "匿名：/anonymous\n" +
										   "去匿：/no-anonymous      " + "不接收某用户的消息：/block             " + "发起一项投票：/vote\n" +
										   "全频道广播：/all           " + "将频道内某个用户设置为特别关注：/special  " + "退出频道：/exit\n");
							
					}
				}
				else
					sendMessage(msgString);
			}
			System.out.println("您已断开连接！");
		}
	}
}
