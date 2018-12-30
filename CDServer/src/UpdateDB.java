import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateDB {
	public void register_DB(String query){
		Connection conn = null;
		Statement stmt = null;
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("드라이버 로딩 성공");
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:XE","hr","hr");
			System.out.println("DB연결 성공");
			
			stmt = conn.createStatement();
			stmt.executeQuery(query);
		}catch(ClassNotFoundException cnfe){
			System.out.println("해당 클래스를 찾을 수 없습니다." + cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	public void addFriend_DB(String query){
		Connection conn = null;
		Statement stmt = null;
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("드라이버 로딩 성공");
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:XE","hr","hr");
			System.out.println("DB연결 성공");
			
			stmt = conn.createStatement();
			stmt.executeQuery(query);
		}catch(ClassNotFoundException cnfe){
			System.out.println("해당 클래스를 찾을 수 없습니다." + cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	public void UpdateGPS(String query){
		Connection conn = null;
		Statement stmt = null;
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("드라이버 로딩 성공");
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:XE","hr","hr");
			System.out.println("DB연결 성공");
			
			stmt = conn.createStatement();
			stmt.executeQuery(query);
		}catch(ClassNotFoundException cnfe){
			System.out.println("해당 클래스를 찾을 수 없습니다." + cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	public void UpdateFriend(String query){
		Connection conn = null;
		Statement stmt = null;
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("드라이버 로딩 성공");
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:XE","hr","hr");
			System.out.println("DB연결 성공");
			
			stmt = conn.createStatement();
			stmt.executeQuery(query);
		}catch(ClassNotFoundException cnfe){
			System.out.println("해당 클래스를 찾을 수 없습니다." + cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	
}
