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
			System.out.println("��˿ں�Ϊ��" + port + ",IP��ַΪ��" + hostIp
					+ "�ķ���������ʧ��!" + "\r\n");
			isConnected = false;// 
			return false;
		}
	}
	
	public void sendMessage(String message) {
		writer.println(message);
		writer.flush();
	}
	
	public static void main(String[] args) {
		System.out.println("�������û����ƣ�");
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
					System.out.println("���������жϣ�");
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
						System.out.println("����Ƶ����/create + Ƶ����  " + "��ѯƵ���б�/list                  " + "����Ƶ����/join + Ƶ����\n" +
										   "˽�ģ�/to                " + "��ʾƵ�����û���/userlist             " + "������/anonymous\n" +
										   "ȥ�䣺/no-anonymous      " + "������ĳ�û�����Ϣ��/block             " + "����һ��ͶƱ��/vote\n" +
										   "ȫƵ���㲥��/all           " + "��Ƶ����ĳ���û�����Ϊ�ر��ע��/special  " + "�˳�Ƶ����/exit\n");
							
					}
				}
				else
					sendMessage(msgString);
			}
			System.out.println("���ѶϿ����ӣ�");
		}
	}
}
