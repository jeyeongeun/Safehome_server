
public class GPS {
	//===================================================
	// variables
	//===================================================
	private String phone_number;
	private double latitude;
	private double longitude;
	//===================================================
	// get funcions
	//===================================================
	public String getPhoneNumber(){
		return phone_number;
	}
	
	public double getLatitude(){
		return latitude;
	}
	public double getLongitude(){
		return longitude;
	}
	//===================================================
	// set functions
	//===================================================
	public void setInfo(String _number, double _latitude, double _longitude){
		phone_number = _number;
		latitude = _latitude;
		longitude = _longitude;
	}
	public void setPhoneNumber(String _number){
		phone_number = _number;
	}
	
	public void setLatitude(double _latitude){
		latitude = _latitude;
	}
	public void setLongitude(double _longitude){
		longitude = _longitude;
	}
}
