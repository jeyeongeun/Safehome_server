
public class PoliceOffice {
	//===================================================
	// variables
	//===================================================
	private String police_name;
	private String religion;
	private String religion2;
	private String religion3;
	private String address;
	private double latitude;
	private double longitude;
	//===================================================
	// get funcions
	//===================================================
	public String getPoliceName(){
		return police_name;
	}
	public String getReligion(){
		return religion;
	}
	public String getReligion2(){
		return religion2;
	}
	public String getReligion3(){
		return religion3;
	}
	public String getAddress(){
		return address;
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
	public void setInfo(String _name, String _r, String _r2, String _r3, String _address, double _lat, double _long){
		police_name = _name;
		religion = _r;
		religion2 = _r2;
		religion3 = _r3;
		address = _address;
		latitude = _lat;
		longitude = _long;
	}
	
	public void setPoliceName(String _name){
		police_name = _name;
	}
	public void setReligion(String _religion){
		religion = _religion;
	}
	public void setReligion2(String _religion){
		religion2 = _religion;
	}
	public void setReligion3(String _religion){
		religion3 = _religion;
	}
	public void setAddress(String _address){
		address = _address;
	}
	public void setLatitude(double _lat){
		latitude = _lat;
	}
	public void setLongitudde(double _long){
		longitude = _long;
	}
	//public V
}
