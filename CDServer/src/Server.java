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
	//현재 접속되어 있는, 즉 현재 쓰레드를 돌리고 있는 user들의 집합니다.
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
	 * Function - 서버의 생성자.
	 * DB를 초기화하고, 네트워크 연결을 시작하고, 사용자의 접속을 기다린다.
	 ***********************************************/
	public Server() throws ParserConfigurationException, SAXException, FileNotFoundException, IOException{
		initiate();
		startNetwork();
		connect();
	}
	/**********************************************
	 * Function - 네트워크 시작.
	 * port number를 갖는 소켓을 생성하고 접속을 기다린다.
	 ***********************************************/
	private void startNetwork(){
		try{
			server_socket = new ServerSocket(port);
			System.out.println("서버를 시작합니다...");
			connect();
		} catch(IOException e){
			System.out.println("이미 사용 중인 포트입니다.");
		} catch(Exception e) {
			System.out.println("잘못 입력하였습니다.");
		}
	}
	/**********************************************
	 * Function - 접속 대기
	 * 사용자의 접속을 기다리다가 소켓을 accept하면 쓰레드가 시작된다.
	 * ServerThread 클래스의 run()함수로 넘어간다.
	 ***********************************************/
	private void connect(){
		Thread th = new Thread(new Runnable() {
			public void run(){
				while(true){
					try{
						System.out.println("사용자의 접속을 기다립니다..\n");
						socket = server_socket.accept();
						System.out.println("Success!");
						
						ServerThread servThread = new ServerThread(socket);
						servThread.start();
					}catch(IOException e){
						System.out.println("에헤이!! 다시 시도하세요.");
					}
				}
			}
		});
		th.start();
	}
	/**********************************************
	 * Function - DB 로딩
	 * 프로그램 시작 전에 모든 DB를 로딩하는 함수이다.
	 ***********************************************/
	public void initiate() throws FileNotFoundException, ParserConfigurationException, SAXException, FileNotFoundException, IOException{
		initiate_User();
		initiate_Friend();
		initiate_GPS();
		//set_PoliceOffice();
		initiate_PoliceOffice();
	}
	/**********************************************
	 * Function - 경찰서 정보 읽기 및 저장
	 * 엑셀 파일 형태로 되어있는 경찰서 정보를 읽어와 policeoffice DB에 저장하는 함수.
	 * 맨 처음 서버를 돌릴 때만 수행하면 된다.
	 ***********************************************/
	public void set_PoliceOffice() throws FileNotFoundException, ParserConfigurationException, SAXException, FileNotFoundException, IOException{
		OpenAPI newapi = new OpenAPI();
		newapi.readEXCEL();
		newapi.callGeo();
		newapi.setPoliceDB();
	}
	//User DB Loading
	/**********************************************
	 * Function - CDUser DB 로딩 
	 * 사용자에 대한 DB를 로드하는 함수이다. 
	 * CDUser - user_number VARCHAR2(20), user_name VARCHAR2(20), starttime NUMBER(3), endtime NUMBER(3)
	 ***********************************************/
	public void initiate_User(){
		Connection conn = null;
		Statement stmt = null;
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("드라이버 로딩 성공");
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:XE","hr","hr");
			System.out.println("DB연결 성공");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select user_number, user_name, starttime, endtime from CDUser");
			System.out.println("<<사용자 DB>>");
			try{
				while(rs.next()){
					String user_number = rs.getString("user_number");
					String user_name = rs.getString("user_name");
					int starttime = rs.getInt("starttime");
					int endtime = rs.getInt("endtime");
					
					User newuser = new User();
					newuser.setUserInfo(user_number, user_name, starttime, endtime);
					all_user.add(newuser);
					
					//여러 사용자와 통신하므로 각 사용자를 콕 찝어서 메세지를 보내줘야 한다. 따라서 각 사용자의 쓰레드를 하나의 리스트에 넣어준다.
					Socket newsock = new Socket();
					ServerThread newthread = new ServerThread(newsock);
					newthread.setUser(newuser);
					actual_user.add(newthread);
					
					System.out.println(user_number + "	" + user_name + "	" + starttime + "	" + endtime);
				}
			}catch(Exception ex){}
			
		}catch(ClassNotFoundException cnfe){
			System.out.println("해당 클래스를 찾을 수 없습니다." + cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	/**********************************************
	 * Function - Friend DB 로딩 
	 * 친구에 대한 DB를 로드하는 함수이다. 
	 * Friend - index_number NUMBER(10), user_number VARCHAR2(20), friend_number VARCHAR2(20), help NUMBER(5)
	 ***********************************************/
	public void initiate_Friend(){
		Connection conn = null;
		Statement stmt = null;
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("드라이버 로딩 성공");
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:XE","hr","hr");
			System.out.println("DB연결 성공");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select user_number, friend_number, help from friend");
			System.out.println("<<친구 DB>>");
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
				//모든 user의 친구리스트를 갱신
				for(int i = 0; i < all_user.size(); i++){
					for(int j = 0; j < all_friend.size(); j++){
						if(all_user.elementAt(i).getPhoneNumber().equals(all_friend.elementAt(j).getUserNumber())){
							all_user.elementAt(i).addFriend(all_friend.elementAt(j));
						}
					}
				}
			}catch(Exception ex){}
			
		}catch(ClassNotFoundException cnfe){
			System.out.println("해당 클래스를 찾을 수 없습니다." + cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	/**********************************************
	 * Function - GPS DB 로딩 
	 * GPS에 대한 DB를 로드하는 함수이다. 
	 * GPS - user_number VARCHAR2(20), latitude VARCHAR2(30), longitude VARCHAR2(30)
	 ***********************************************/
	public void initiate_GPS(){
		Connection conn = null;
		Statement stmt = null;
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("드라이버 로딩 성공");
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:XE","hr","hr");
			System.out.println("DB연결 성공");
			
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
			System.out.println("해당 클래스를 찾을 수 없습니다." + cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	/**********************************************
	 * Function - policeoffice DB 로딩 
	 * 경찰서에 대한 DB를 로드하는 함수이다. 
	 * policeoffice - police_name VARCHAR2(20), religion VARCHAR2(20), religion2 VARCHAR2(20),
	 * religion3 VARCHAR2(20), address VARCHAR2(80), latitude VARCHAR2(20), longitude VARCHAR2(20)
	 ***********************************************/
	public void initiate_PoliceOffice(){
		Connection conn = null;
		Statement stmt = null;
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("드라이버 로딩 성공");
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:XE","hr","hr");
			System.out.println("DB연결 성공");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select police_name, religion, religion2, religion3, address, latitude, longitude from policeoffice");
			System.out.println("<<경찰서 DB>>");
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
			System.out.println("해당 클래스를 찾을 수 없습니다." + cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	// 실제 모든 기능을 수행하고 쓰레드가 돌아가는 함수.
	// 변수 사용 및 구현의 편의성을 위해 Server의 내부클래스로 작성.
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
		User myUser = new User();	//현재 이 쓰레드의 user
		private StringBuffer buf = new StringBuffer(4096);
		
		//======================================================================================================
		// thread functions
		//======================================================================================================
		/**********************************************
		 * Function - 소켓 생성
		 * 새로운 소켓을 생성하는 함수.
		 ***********************************************/
		public ServerThread(Socket newsock)
		{
			this.thread_socket = newsock;
			thread = this;
			setStream();
		}
		/**********************************************
		 * Function - 소켓 생성
		 * 위 함수와 비슷하다. 오버로딩 함수.
		 ***********************************************/
		public ServerThread(){
			this.thread_socket = new Socket();
			thread = this;
			setStream();
		}
		/**********************************************
		 * Function - stream 설정
		 * input stream과 output stream을 설정하는 함수.
		 ***********************************************/
		private void setStream(){
			try{
				is = thread_socket.getInputStream();
				dis = new DataInputStream(is);
				os = thread_socket.getOutputStream();
				dos = new DataOutputStream(os);
			} catch(IOException e){
				System.out.println("Stream 설정 에러!\n");
			}
		}
		/**********************************************
		 * Function - 현재 사용자 리턴
		 * 현재 이 쓰레드를 사용하고 있는 사용자가 누구인지 리턴.
		 ***********************************************/
		public User getUser(){
			return myUser;
		}
		/**********************************************
		 * Function  - 현재 사용자 설정
		 * 현재 이 쓰레드를 사용하는 사용자를 설정해주는 함수.
		 ***********************************************/
		public void setUser(User _user){
			this.myUser = _user;
			System.out.println("나는 " + this.myUser.getName() + "입니다.");
		}
		/**********************************************
		 * Function - thread run
		 * 쓰레드가 시작하면 돌아가는 함수
		 * 사용자로부터 msg가 오기를 기다렸다가, 오면 inmessage에서 메세지를 처리한다.
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
		 * Function - 실제 로그인 함수
		 * 모든 사용자 중에 해당 번호와 이름이 일치하는 사용자가 있다면 그 사용자를 리턴해주고, 아니면 null리턴.
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
	                  System.out.println("비밀번호가 다릅니다. 다시 시도하세요\n");
	                  return null;
	               }
	            }
	         }
	         System.out.println("없는 계정입니다.\n");
	         return null;
	      }
		/**********************************************
		 * Function - 실제 회원가입 함수
		 * 새로운 사용자를 리스트에 추가하고 DB 갱신.
		 ***********************************************/
		public void registerUser(String phone_number, String name){
			User newuser = new User();
			newuser.setUserInfo(name, phone_number,0,0);
			all_user.add(newuser);
			
			Socket newsock = new Socket();
			ServerThread newthread = new ServerThread(newsock);
			newthread.setUser(newuser);
			actual_user.add(newthread);
			
			System.out.println("회원가입 완료");
		}
		/**********************************************
		 * Function - 로그인 처리
		 * Received : LogIn|폰번호|이름
		 * Send : Confirm|성공or실패
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
						buf.append("|실패");
						sendMessgage(buf.toString());
					}
				}
			}else{
				User login_user = LogIn(phone_number, name);
				for(int i = 0; i < actual_user.size(); i++){
					if(actual_user.elementAt(i).getUser().getPhoneNumber().equals(login_user.getPhoneNumber())){
						ServerThread rr = actual_user.elementAt(i);
						System.out.println("현재 서버는 " + rr.getUser().getName());
						buf.setLength(0);
						buf.append(phone_number);
						buf.append("|");
						buf.append("ConfirmLogIn");
						buf.append("|성공");
						sendMessgage(buf.toString());
						//sendMessgage(buf.toString());
					}
				}
			}
			System.out.println("Send Message from server : "+ buf.toString());
		}
		/**********************************************
		 * Function - 회원가입 처리
		 * Received : Register|폰번호|이름
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
		//친구추가 함수
		/**********************************************
		 * Function - 친구 추가 처리
		 * Received : addFriend|폰번호|친구번호|help
		 * Send : ConfirmAddFriend|친구번호|친구이름|help
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
		 * Function  - GPS 업데이트 처리
		 * Received : UpdateGPS|폰번호|위도|경도
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
		 * Function - 친구 정보 업데이트 처리
		 * Received : UpdateFriend|폰번호|친구번호1|친구번호2|...
		 * Send : NONE
		 * 받은 친구 번호에 대한 help는 모두 1로 변경.
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
		 * Function - 친구 정보 불러오기 처리
		 * Received : FriendInfo|폰번호
		 * Send : FriendList|친구1_번호|친구1_이름|친구1_help|...
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
		 * Function - GPS 정보 불러오기 처리
		 * Received : GPSInfo|폰번호
		 * Send : GPSList|친구1_번호|위도|경도|...
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
		 * Function - 경찰서 정보 불러오기 처리
		 * Received : PoliceofficeInfo|폰번호|위도|경도
		 * Send : PoliceList|경찰서1_이름|위도|경도|...
		 ***********************************************/
		public void getPoliceOfficeInfo(String str){
			StringTokenizer t = new StringTokenizer(str, "|");
			String protocol = t.nextToken();
			
			String phone_number = t.nextToken();
			double latitude = Double.parseDouble(t.nextToken());
			double longitude = Double.parseDouble(t.nextToken());
			
			double one_kilo_lat = 0.009009;	//1km당 위도
			double one_kilo_long = 0.011364;	//1km당 경도
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
		 * Function - 메세지 처리
		 * 사용자에게 온 메세지를 구분하여 해당 함수를 안내하는 함수.
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
		 * Function - 메세지 전송
		 * 사용자에게 메세지를 전송하는 함수.
		 ***********************************************/
		private void sendMessgage(String msg){
			try{
				dos.writeUTF(msg);
				dos.flush();
				System.out.println("server send 완료");
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	} //serverThread end
	public static void main(String[] args) throws FileNotFoundException, ParserConfigurationException, SAXException, FileNotFoundException, IOException{
			new Server();
		}
}//server end
