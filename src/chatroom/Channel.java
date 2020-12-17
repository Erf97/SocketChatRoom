package chatroom;
public class Channel{
		
		public boolean isEncrypted() {
			return isEncrypted;
		}

		public String getPasswordString() {
			return passwordString;
		}

		public String getNameString() {
			return nameString;
		}
		
		public String getType() {
			return type;
		}

		private String nameString;
		private boolean isEncrypted;
		private String passwordString;
		private String type;
		private int onlineNum;
		private int port;
		
		public Channel(String nameString,boolean isEncrypted,String passwordString,String type) {
			this.nameString = nameString;
			this.isEncrypted = isEncrypted;
			this.passwordString = passwordString;
			this.type = type;
		}
		
		public Channel(String nameString,String type) {
			this(nameString,false,"",type);
		}
		
	}
