import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

//import example.User;

public class Server {
	//======================================================================================================
	// variables
	//======================================================================================================
	private int port = 5555;
	private ServerSocket server_socket;
	private Socket socket;
	//���� ���ӵǾ� �ִ�, �� ���� �����带 ������ �ִ� user���� ���մϴ�.
	private Vector<ServerThread> actual_user = new Vector<ServerThread>();
	private Vector<User> all_user = new Vector<User>();
	private Vector<Friend> all_friend = new Vector<Friend>();
	private Vector<GPS> all_gps = new Vector<GPS>();
	private Vector<Criminal> all_criminal = new Vector<Criminal>();
	private Vector<PoliceOffice> all_police = new Vector<PoliceOffice>();
	
	//======================================================================================================
	// functions
	//======================================================================================================
	/**********************************************
	 * Function - ������ ������.
	 * DB�� �ʱ�ȭ�ϰ�, ��Ʈ��ũ ������ �����ϰ�, ������� ������ ��ٸ���.
	 ***********************************************/
	public Server() throws ParserConfigurationException, SAXException, FileNotFoundException, IOException{
		initiate();
		startNetwork();
		connect();
	}
	/**********************************************
	 * Function - ��Ʈ��ũ ����.
	 * port number�� ���� ������ �����ϰ� ������ ��ٸ���.
	 ***********************************************/
	private void startNetwork(){
		try{
			server_socket = new ServerSocket(port);
			System.out.println("������ �����մϴ�...");
			connect();
		} catch(IOException e){
			System.out.println("�̹� ��� ���� ��Ʈ�Դϴ�.");
		} catch(Exception e) {
			System.out.println("�߸� �Է��Ͽ����ϴ�.");
		}
	}
	/**********************************************
	 * Function - ���� ���
	 * ������� ������ ��ٸ��ٰ� ������ accept�ϸ� �����尡 ���۵ȴ�.
	 * ServerThread Ŭ������ run()�Լ��� �Ѿ��.
	 ***********************************************/
	private void connect(){
		Thread th = new Thread(new Runnable() {
			public void run(){
				while(true){
					try{
						System.out.println("������� ������ ��ٸ��ϴ�..\n");
						socket = server_socket.accept();
						System.out.println("Success!");
						
						ServerThread servThread = new ServerThread(socket);
						servThread.start();
					}catch(IOException e){
						System.out.println("������!! �ٽ� �õ��ϼ���.");
					}
				}
			}
		});
		th.start();
	}
	/**********************************************
	 * Function - DB �ε�
	 * ���α׷� ���� ���� ��� DB�� �ε��ϴ� �Լ��̴�.
	 ***********************************************/
	public void initiate() throws FileNotFoundException, ParserConfigurationException, SAXException, FileNotFoundException, IOException{
		initiate_User();
		initiate_Friend();
		initiate_GPS();
		//set_PoliceOffice();
		initiate_PoliceOffice();
	}
	/**********************************************
	 * Function - ������ ���� �б� �� ����
	 * ���� ���� ���·� �Ǿ��ִ� ������ ������ �о�� policeoffice DB�� �����ϴ� �Լ�.
	 * �� ó�� ������ ���� ���� �����ϸ� �ȴ�.
	 ***********************************************/
	public void set_PoliceOffice() throws FileNotFoundException, ParserConfigurationException, SAXException, FileNotFoundException, IOException{
		OpenAPI newapi = new OpenAPI();
		newapi.readEXCEL();
		newapi.callGeo();
		newapi.setPoliceDB();
	}
	//User DB Loading
	/**********************************************
	 * Function - CDUser DB �ε� 
	 * ����ڿ� ���� DB�� �ε��ϴ� �Լ��̴�. 
	 * CDUser - user_number VARCHAR2(20), user_name VARCHAR2(20), starttime NUMBER(3), endtime NUMBER(3)
	 ***********************************************/
	public void initiate_User(){
		Connection conn = null;
		Statement stmt = null;
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("����̹� �ε� ����");
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:XE","hr","hr");
			System.out.println("DB���� ����");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select user_number, user_name, starttime, endtime from CDUser");
			System.out.println("<<����� DB>>");
			try{
				while(rs.next()){
					String user_number = rs.getString("user_number");
					String user_name = rs.getString("user_name");
					int starttime = rs.getInt("starttime");
					int endtime = rs.getInt("endtime");
					
					User newuser = new User();
					newuser.setUserInfo(user_number, user_name, starttime, endtime);
					all_user.add(newuser);
					
					//���� ����ڿ� ����ϹǷ� �� ����ڸ� �� �� �޼����� ������� �Ѵ�. ���� �� ������� �����带 �ϳ��� ����Ʈ�� �־��ش�.
					Socket newsock = new Socket();
					ServerThread newthread = new ServerThread(newsock);
					newthread.setUser(newuser);
					actual_user.add(newthread);
					
					System.out.println(user_number + "	" + user_name + "	" + starttime + "	" + endtime);
				}
			}catch(Exception ex){}
			
		}catch(ClassNotFoundException cnfe){
			System.out.println("�ش� Ŭ������ ã�� �� �����ϴ�." + cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	/**********************************************
	 * Function - Friend DB �ε� 
	 * ģ���� ���� DB�� �ε��ϴ� �Լ��̴�. 
	 * Friend - index_number NUMBER(10), user_number VARCHAR2(20), friend_number VARCHAR2(20), help NUMBER(5)
	 ***********************************************/
	public void initiate_Friend(){
		Connection conn = null;
		Statement stmt = null;
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("����̹� �ε� ����");
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:XE","hr","hr");
			System.out.println("DB���� ����");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select user_number, friend_number, help from friend");
			System.out.println("<<ģ�� DB>>");
			try{
				while(rs.next()){
					String user_number = rs.getString("user_number");
					String friend_number = rs.getString("friend_number");
					int help = rs.getInt("help");
					
					Friend newfriend = new Friend();
					newfriend.setInfo(user_number, friend_number, help);
					all_friend.add(newfriend);
					
					System.out.println(user_number + "	" + friend_number + "	" + help);
				}
				//��� user�� ģ������Ʈ�� ����
				for(int i = 0; i < all_user.size(); i++){
					for(int j = 0; j < all_friend.size(); j++){
						if(all_user.elementAt(i).getPhoneNumber().equals(all_friend.elementAt(j).getUserNumber())){
							all_user.elementAt(i).addFriend(all_friend.elementAt(j));
						}
					}
				}
			}catch(Exception ex){}
			
		}catch(ClassNotFoundException cnfe){
			System.out.println("�ش� Ŭ������ ã�� �� �����ϴ�." + cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	/**********************************************
	 * Function - GPS DB �ε� 
	 * GPS�� ���� DB�� �ε��ϴ� �Լ��̴�. 
	 * GPS - user_number VARCHAR2(20), latitude VARCHAR2(30), longitude VARCHAR2(30)
	 ***********************************************/
	public void initiate_GPS(){
		Connection conn = null;
		Statement stmt = null;
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("����̹� �ε� ����");
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:XE","hr","hr");
			System.out.println("DB���� ����");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select user_number, latitude, longitude from gps");
			System.out.println("<<GPS DB>>");
			try{
				while(rs.next()){
					String user_number = rs.getString("user_number");
					double latitude = Double.parseDouble(rs.getString("latitude"));
					double longitude = Double.parseDouble(rs.getString("longitude"));
					
					GPS newgps = new GPS();
					newgps.setInfo(user_number, latitude, longitude);
					all_gps.add(newgps);
					System.out.println(user_number + "	" + latitude + "	" + longitude);
				}
			}catch(Exception ex){}
			
		}catch(ClassNotFoundException cnfe){
			System.out.println("�ش� Ŭ������ ã�� �� �����ϴ�." + cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	/**********************************************
	 * Function - policeoffice DB �ε� 
	 * �������� ���� DB�� �ε��ϴ� �Լ��̴�. 
	 * policeoffice - police_name VARCHAR2(20), religion VARCHAR2(20), religion2 VARCHAR2(20),
	 * religion3 VARCHAR2(20), address VARCHAR2(80), latitude VARCHAR2(20), longitude VARCHAR2(20)
	 ***********************************************/
	public void initiate_PoliceOffice(){
		Connection conn = null;
		Statement stmt = null;
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("����̹� �ε� ����");
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:XE","hr","hr");
			System.out.println("DB���� ����");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select police_name, religion, religion2, religion3, address, latitude, longitude from policeoffice");
			System.out.println("<<������ DB>>");
			try{
				while(rs.next()){
					String police_name = rs.getString("police_name");
					String religion = rs.getString("religion");
					String religion2 = rs.getString("religion2");
					String religion3 = rs.getString("religion3");
					String address = rs.getString("address");
					double latitude = Double.parseDouble(rs.getString("latitude"));
					double longitude = Double.parseDouble(rs.getString("longitude"));
					
					System.out.println(police_name + " " + religion + " " + religion2 + " " + religion3 + " " + address
							+ " " + latitude + " " + longitude);
					PoliceOffice newoffice = new PoliceOffice();
					newoffice.setInfo(police_name, religion, religion2, religion3, address, latitude, longitude);
					all_police.add(newoffice);
				}
			}catch(Exception ex){}
			
		}catch(ClassNotFoundException cnfe){
			System.out.println("�ش� Ŭ������ ã�� �� �����ϴ�." + cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	// ���� ��� ����� �����ϰ� �����尡 ���ư��� �Լ�.
	// ���� ��� �� ������ ���Ǽ��� ���� Server�� ����Ŭ������ �ۼ�.
	class ServerThread extends Thread{
		//======================================================================================================
		// variables
		//======================================================================================================
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		private Socket thread_socket;
		Thread thread;
		User myUser = new User();	//���� �� �������� user
		private StringBuffer buf = new StringBuffer(4096);
		
		//======================================================================================================
		// thread functions
		//======================================================================================================
		/**********************************************
		 * Function - ���� ����
		 * ���ο� ������ �����ϴ� �Լ�.
		 ***********************************************/
		public ServerThread(Socket newsock)
		{
			this.thread_socket = newsock;
			thread = this;
			setStream();
		}
		/**********************************************
		 * Function - ���� ����
		 * �� �Լ��� ����ϴ�. �����ε� �Լ�.
		 ***********************************************/
		public ServerThread(){
			this.thread_socket = new Socket();
			thread = this;
			setStream();
		}
		/**********************************************
		 * Function - stream ����
		 * input stream�� output stream�� �����ϴ� �Լ�.
		 ***********************************************/
		private void setStream(){
			try{
				is = thread_socket.getInputStream();
				dis = new DataInputStream(is);
				os = thread_socket.getOutputStream();
				dos = new DataOutputStream(os);
			} catch(IOException e){
				System.out.println("Stream ���� ����!\n");
			}
		}
		/**********************************************
		 * Function - ���� ����� ����
		 * ���� �� �����带 ����ϰ� �ִ� ����ڰ� �������� ����.
		 ***********************************************/
		public User getUser(){
			return myUser;
		}
		/**********************************************
		 * Function  - ���� ����� ����
		 * ���� �� �����带 ����ϴ� ����ڸ� �������ִ� �Լ�.
		 ***********************************************/
		public void setUser(User _user){
			this.myUser = _user;
			System.out.println("���� " + this.myUser.getName() + "�Դϴ�.");
		}
		/**********************************************
		 * Function - thread run
		 * �����尡 �����ϸ� ���ư��� �Լ�
		 * ����ڷκ��� msg�� ���⸦ ��ٷȴٰ�, ���� inmessage���� �޼����� ó���Ѵ�.
		 ***********************************************/
		public void run(){
			try{
				Thread currentThread = Thread.currentThread();
				
				while(currentThread == thread){
					String msg = dis.readUTF();
					System.out.println("received message : " + msg);
					inmessage(msg);
				}
			}catch(IOException e){
				System.out.println("Fail");
			}
		}
		
		//======================================================================================================
		// real functions
		//======================================================================================================
		/**********************************************
		 * Function - ���� �α��� �Լ�
		 * ��� ����� �߿� �ش� ��ȣ�� �̸��� ��ġ�ϴ� ����ڰ� �ִٸ� �� ����ڸ� �������ְ�, �ƴϸ� null����.
		 ***********************************************/
		public User LogIn(String phone_number, String name){
	         for(int i = 0; i < all_user.size(); i++){
	            User newuser = new User();
	            String temp = all_user.elementAt(i).getPhoneNumber();
	            newuser = all_user.elementAt(i);
	            
	            if(newuser.getPhoneNumber().equals(phone_number)){
	               if(newuser.getName().equals(name)){
	                  setUser(newuser);
	            	   return newuser;
	                  }
	               else{
	                  System.out.println("��й�ȣ�� �ٸ��ϴ�. �ٽ� �õ��ϼ���\n");
	                  return null;
	               }
	            }
	         }
	         System.out.println("���� �����Դϴ�.\n");
	         return null;
	      }
		/**********************************************
		 * Function - ���� ȸ������ �Լ�
		 * ���ο� ����ڸ� ����Ʈ�� �߰��ϰ� DB ����.
		 ***********************************************/
		public void registerUser(String phone_number, String name){
			User newuser = new User();
			newuser.setUserInfo(name, phone_number,0,0);
			all_user.add(newuser);
			
			Socket newsock = new Socket();
			ServerThread newthread = new ServerThread(newsock);
			newthread.setUser(newuser);
			actual_user.add(newthread);
			
			System.out.println("ȸ������ �Ϸ�");
		}
		/**********************************************
		 * Function - �α��� ó��
		 * Received : LogIn|����ȣ|�̸�
		 * Send : Confirm|����or����
		 ***********************************************/
		public void getLogIn(String str){
			StringTokenizer t = new StringTokenizer(str, "|");
			String protocol = t.nextToken();
			
			String phone_number = t.nextToken();		
			String name = t.nextToken();
			if(LogIn(phone_number, name).equals(null)){
				for(int i = 0; i < actual_user.size(); i++){
					if(actual_user.elementAt(i).getUser().getPhoneNumber().equals(phone_number)){
						ServerThread rr = actual_user.elementAt(i);
						buf.setLength(0);
						buf.append(phone_number);
						buf.append("|");
						buf.append("ConfirmLogIn");
						buf.append("|����");
						sendMessgage(buf.toString());
					}
				}
			}else{
				User login_user = LogIn(phone_number, name);
				for(int i = 0; i < actual_user.size(); i++){
					if(actual_user.elementAt(i).getUser().getPhoneNumber().equals(login_user.getPhoneNumber())){
						ServerThread rr = actual_user.elementAt(i);
						System.out.println("���� ������ " + rr.getUser().getName());
						buf.setLength(0);
						buf.append(phone_number);
						buf.append("|");
						buf.append("ConfirmLogIn");
						buf.append("|����");
						sendMessgage(buf.toString());
						//sendMessgage(buf.toString());
					}
				}
			}
			System.out.println("Send Message from server : "+ buf.toString());
		}
		/**********************************************
		 * Function - ȸ������ ó��
		 * Received : Register|����ȣ|�̸�
		 * Send : NONE
		 ***********************************************/
		public void getRegister(String str){
			StringTokenizer t = new StringTokenizer(str, "|");
			String protocol = t.nextToken();
			
			String phone_number = t.nextToken();
			String name = t.nextToken();
			
			registerUser(phone_number, name);
			
			String query = "INSERT INTO CDUser VALUES('" + phone_number  + "', '" + name + "', 0, 0)";
			UpdateDB db = new UpdateDB();
			db.register_DB(query);
		}
		//ģ���߰� �Լ�
		/**********************************************
		 * Function - ģ�� �߰� ó��
		 * Received : addFriend|����ȣ|ģ����ȣ|help
		 * Send : ConfirmAddFriend|ģ����ȣ|ģ���̸�|help
		 ***********************************************/
		public void getAddFriend(String str){
			StringTokenizer t = new StringTokenizer(str, "|");
			String protocol = t.nextToken();
			
			String phone_number = t.nextToken();
			String friend_number = t.nextToken();
			String shelp = t.nextToken();
			int help = 0;
			if(shelp.equals("true"))
				help = 1;
			else
				help = 0;
			
			Friend newfriend = new Friend();
			newfriend.setInfo(phone_number, friend_number, help);
			
			for(int i = 0; i < all_user.size(); i++){
				if(all_user.elementAt(i).getPhoneNumber().equals(phone_number)){
					all_user.elementAt(i).addFriend(newfriend);
				}
			}
			
			String query = "INSERT INTO friend VALUES('" + phone_number + "', '" + friend_number +"', " + help + ")";
			UpdateDB db = new UpdateDB();
			db.addFriend_DB(query);
			
			String name = "";
			for(int i = 0; i < all_user.size(); i++){
				if(all_user.elementAt(i).getPhoneNumber().equals(friend_number)){
					name = all_user.elementAt(i).getName();
			}
			}
			buf.setLength(0);
			buf.append(phone_number);
			buf.append("|");
			buf.append("ConfirmAddFriend|" + phone_number + "|" +name + "|" + help);
			sendMessgage(buf.toString());
			
		}
		/**********************************************
		 * Function  - GPS ������Ʈ ó��
		 * Received : UpdateGPS|����ȣ|����|�浵
		 * Send : NONE
		 ***********************************************/
		public void getUpdateGPS(String str){
			StringTokenizer t = new StringTokenizer(str, "|");
			String protocol = t.nextToken();
			
			String phone_number = t.nextToken();
			double latitude = Double.parseDouble(t.nextToken());
			double longitude = Double.parseDouble(t.nextToken());
			boolean check = false;
			
			for(int i = 0; i < all_gps.size(); i++){
				if(all_gps.elementAt(i).getPhoneNumber().equals(phone_number)){
					check = true;
					all_gps.elementAt(i).setInfo(phone_number, latitude, longitude);
				}
			}
			UpdateDB db = new UpdateDB();
			String query = "";
			if(check == false){
				GPS newgps = new GPS();
				newgps.setInfo(phone_number, latitude, longitude);
				all_gps.add(newgps);
				query = "INSERT INTO gps VALUES('" + phone_number + "', '" + latitude + "', '" + 
				longitude + "')";
				db.UpdateGPS(query);
			}
			String lat = Double.toString(latitude);
			String lon = Double.toString(longitude);
			query = "UPDATE gps SET latitude='" + lat + "', longitude='" + lon
					+ "' WHERE user_number='" + phone_number + "'";
			db.UpdateGPS(query);
		}
		/**********************************************
		 * Function - ģ�� ���� ������Ʈ ó��
		 * Received : UpdateFriend|����ȣ|ģ����ȣ1|ģ����ȣ2|...
		 * Send : NONE
		 * ���� ģ�� ��ȣ�� ���� help�� ��� 1�� ����.
		 ***********************************************/
		public void getUpdateFriend(String str){
			StringTokenizer t = new StringTokenizer(str, "|");
			String protocol = t.nextToken();
			
			String phone_number = t.nextToken();
			while(true){
				String friend_number = t.nextToken();
				if(friend_number.equals(null))
					break;
				else{
					for(int i = 0; i < all_friend.size(); i++){
						if(all_friend.elementAt(i).getUserNumber().equals(phone_number) 
								&& all_friend.elementAt(i).getFriendNumber().equals(friend_number)){
							all_friend.elementAt(i).setHelp(1);
						}
					}
				}
				String query = "UPDATE friend SET help=1 WHERE user_number='" + phone_number + "' AND friend_number='" + friend_number 
						+ "'";
				UpdateDB db = new UpdateDB();
				db.UpdateFriend(query);
			}
		}
		/**********************************************
		 * Function - ģ�� ���� �ҷ����� ó��
		 * Received : FriendInfo|����ȣ
		 * Send : FriendList|ģ��1_��ȣ|ģ��1_�̸�|ģ��1_help|...
		 ***********************************************/
		public void getFriendInfo(String str){
			StringTokenizer t = new StringTokenizer(str, "|");
			String protocol = t.nextToken();
			
			String phone_number = t.nextToken();
			String name = "";
			User newuser = new User();
			ServerThread r = new ServerThread();
			
			for(int i = 0; i < actual_user.size(); i++){
				if(actual_user.elementAt(i).getUser().getPhoneNumber().equals(phone_number)){
					newuser = actual_user.elementAt(i).getUser();
					r = actual_user.elementAt(i);
				}
			}
			
			String shelp = "";
			buf.setLength(0);
			buf.append(phone_number);
			buf.append("|");
			buf.append("FriendList");
			//buf.append("|");
			for(int i = 0; i < all_friend.size(); i++){
				buf.append("|");
				if(all_friend.elementAt(i).getUserNumber().equals(phone_number)){
					buf.append(all_friend.elementAt(i).getFriendNumber());
					buf.append("|");
					for(int j = 0; j < all_user.size(); j++){
						if(all_user.elementAt(j).getPhoneNumber().equals(all_friend.elementAt(i).getFriendNumber())){
							name = all_user.elementAt(j).getName();
						}
					}
					buf.append(name);
					buf.append("|");
					if(all_friend.elementAt(i).getHelp() == 0){
						shelp = "0";
					}
					else{
						shelp = "1";
					}
					buf.append(shelp);
				}
			}
			sendMessgage(buf.toString());
		}
		/**********************************************
		 * Function - GPS ���� �ҷ����� ó��
		 * Received : GPSInfo|����ȣ
		 * Send : GPSList|ģ��1_��ȣ|����|�浵|...
		 ***********************************************/
		public void getGPSInfo(String str){
			StringTokenizer t = new StringTokenizer(str, "|");
			String protocol = t.nextToken();
			
			String phone_number = t.nextToken();
			User newuser = new User();
			ServerThread r = new ServerThread();
			
			for(int i = 0; i < actual_user.size(); i++){
				if(actual_user.elementAt(i).getUser().getPhoneNumber().equals(phone_number)){
					newuser = actual_user.elementAt(i).getUser();
					r = actual_user.elementAt(i);
				}
			}
			
			Vector<Friend> temp_friend = newuser.getFriendList();
			Vector<GPS> temp_gps = new Vector<GPS>();
			
			for(int i = 0; i < temp_friend.size(); i++){
				for(int j = 0; j < all_gps.size(); j++){
					if(all_gps.elementAt(j).getPhoneNumber().equals(temp_friend.elementAt(i).getFriendNumber())){
						temp_gps.add(all_gps.elementAt(j));
					}
				}
			}
			
			buf.setLength(0);
			buf.append(phone_number);
			buf.append("|");
			buf.append("GPSList");
			for(int i = 0; i < temp_gps.size(); i++){
				buf.append("|");
				buf.append(temp_gps.elementAt(i).getPhoneNumber());
				buf.append("|");
				buf.append(temp_gps.elementAt(i).getLatitude());
				buf.append("|");
				buf.append(temp_gps.elementAt(i).getLongitude());
			}
			sendMessgage(buf.toString());
		}
		/**********************************************
		 * Function - ������ ���� �ҷ����� ó��
		 * Received : PoliceofficeInfo|����ȣ|����|�浵
		 * Send : PoliceList|������1_�̸�|����|�浵|...
		 ***********************************************/
		public void getPoliceOfficeInfo(String str){
			StringTokenizer t = new StringTokenizer(str, "|");
			String protocol = t.nextToken();
			
			String phone_number = t.nextToken();
			double latitude = Double.parseDouble(t.nextToken());
			double longitude = Double.parseDouble(t.nextToken());
			
			double one_kilo_lat = 0.009009;	//1km�� ����
			double one_kilo_long = 0.011364;	//1km�� �浵
			Vector<PoliceOffice> temp = new Vector<PoliceOffice>();
			
			ServerThread r = new ServerThread();
			for(int i = 0; i < actual_user.size(); i++){
				if(actual_user.elementAt(i).getUser().getPhoneNumber().equals(phone_number)){
					r = actual_user.elementAt(i);
				}
			}
			
			for(int i = 0; i < all_police.size(); i++){
				if((latitude-one_kilo_lat) <= all_police.elementAt(i).getLatitude() &&
						all_police.elementAt(i).getLatitude() <= (latitude+one_kilo_lat)){
					if((longitude-one_kilo_long) <= all_police.elementAt(i).getLongitude() &&
							all_police.elementAt(i).getLongitude() <= (longitude+one_kilo_long)){
						temp.add(all_police.elementAt(i));
						System.out.println(all_police.elementAt(i).getAddress());
					}
				}
			}
			
			buf.setLength(0);
			buf.append(phone_number);
			buf.append("|");
			buf.append("PoliceList");
			for(int i = 0; i < temp.size(); i++){
				buf.append("|");
				buf.append(temp.elementAt(i).getPoliceName());
				buf.append("|");
				buf.append(temp.elementAt(i).getLatitude());
				buf.append("|");
				buf.append(temp.elementAt(i).getLongitude());
			}
			sendMessgage(buf.toString());
		}
		/**********************************************
		 * 
		 * 
		 ***********************************************/
		public void getSetting(String str){
			
		}
		/**********************************************
		 * Function - �޼��� ó��
		 * ����ڿ��� �� �޼����� �����Ͽ� �ش� �Լ��� �ȳ��ϴ� �Լ�.
		 ***********************************************/
		private void inmessage(String str){
			StringTokenizer st = new StringTokenizer(str, "|");
			String protocol = st.nextToken();
			
			System.out.println("Protocol : " + protocol);
			
			if(protocol.equals("LogIn")){
				getLogIn(str);
			}
			
			else if(protocol.equals("Register")){
				getRegister(str);
			}
			
			else if(protocol.equals("addFriend")){
				getAddFriend(str);
			}
			
			else if(protocol.equals("UpdateGPS")){
				getUpdateGPS(str);
			}
			
			else if(protocol.equals("UpdateFriend")){
				getUpdateFriend(str);
			}

			else if(protocol.equals("FriendInfo")){
				getFriendInfo(str);
			}

			else if(protocol.equals("GPSInfo")){
				getGPSInfo(str);
			}

			else if(protocol.equals("PoliceofficeInfo")){
				getPoliceOfficeInfo(str);
			}

			else if(protocol.equals("Setting")){
				getSetting(str);
			}
		}
		/**********************************************
		 * Function - �޼��� ����
		 * ����ڿ��� �޼����� �����ϴ� �Լ�.
		 ***********************************************/
		private void sendMessgage(String msg){
			try{
				dos.writeUTF(msg);
				dos.flush();
				System.out.println("server send �Ϸ�");
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	} //serverThread end
	public static void main(String[] args) throws FileNotFoundException, ParserConfigurationException, SAXException, FileNotFoundException, IOException{
			new Server();
		}
}//server end
