package chatroom;
import chatroom.Client.*;

public class Utils {
	
	public static boolean isMainCommand(String commString) {
		for(int i=0;i<Client.mainMenu.size();i++) {
			if(commString.contains(Client.mainMenu.get(i))) {
				return true;
			}
		}
		return false;
	}
	
	public static String getMainCommand(String msgString) {
		for(int i=0;i<Client.mainMenu.size();i++) {
			if(msgString.contains(Client.mainMenu.get(i))) {
				return Client.mainMenu.get(i);
			}
		}
		return null;
	}
	
	public static boolean isLocalCommand(String commString) {
		for(int i=0;i<Client.localMenu.size();i++) {
			if(commString.contains(Client.localMenu.get(i))) {
				return true;
			}
		}
		return false;
	}
	
	public static String getLocalCommand(String msgString) {
		for(int i=0;i<Client.localMenu.size();i++) {
			if(msgString.contains(Client.localMenu.get(i))) {
				return Client.localMenu.get(i);
			}
		}
		return null;
	}
	
	public static boolean isChatCommand(String commString) {
		for(int i=0;i<Client.chatMenu.size();i++) {
			if(commString.contains(Client.chatMenu.get(i))) {
				return true;
			}
		}
		return false;
	}
	
	// TODO getChatCommand
	
	public static boolean isFileCommand() {
		// TODO
		return true;
	}
	
	// TODO getFileCommand
}