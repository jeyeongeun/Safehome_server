
public class Friend {
	//===================================================
	// variables
	//===================================================
	private String user_number;
	private String friend_number;
	private int help;
	//===================================================
	// get funcions
	//===================================================
	public String getUserNumber(){
		return user_number;
	}
	
	public String getFriendNumber(){
		return friend_number;
	}
	
	public int getHelp(){
		return help;
	}
	//====================================================
	// set functions
	//====================================================
	public void setInfo(String _number, String _number2, int _help){
		user_number = _number;
		friend_number = _number2;
		help = _help;
	}
	
	public void setUserNumber(String _number){
		user_number = _number;
	}
	
	public void setFriendNumber(String _number){
		friend_number = _number;
	}
	
	public void setHelp(int _help){
		help = _help;
	}
}
