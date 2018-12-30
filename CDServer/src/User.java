import java.util.Vector;

public class User {
	//===================================================
	// variables
	//===================================================
	private Vector<Friend> friendList = new Vector<Friend>();
	private String name;
	private String phoneNumber;
	private int startTime;
	private int endTime;
	//===================================================
	// get funcions
	//===================================================
	public Vector<Friend> getFriendList(){
		return friendList;
	}
	public String getName(){
		return name;
	}
	public String getPhoneNumber(){
		return phoneNumber;
	}
	public int getStartTime(){
		return startTime;
	}
	public int getEndTime(){
		return endTime;
	}
	//====================================================
	// set functions
	//====================================================
	public void setUserInfo(String _number, String _name, int _start, int _end){
		name = _name;
		phoneNumber = _number;
		startTime = _start;
		endTime = _end;
	}
	public void setName(String _name){
		name = _name;
	}
	public void setPhoneNumber(String _number){
		phoneNumber = _number;
	}
	public void setStartTime(int _time){
		startTime = _time;
	}
	public void setEndTime(int _time){
		endTime = _time;
	}
	//===================================================
	// 그 외 기능
	//===================================================
	public void addFriend(Friend newfriend){
		//System.out.println(newfriend.getUserNumber());
		friendList.add(newfriend);
	}
}
