
public class Criminal {
	//===================================================
	// variables
	//===================================================
	private String type;
	private String religion;
	private int religion_count;
	private String age;
	private int age_count;
	//===================================================
	// get funcions
	//===================================================
	public void setInfo(String _type, String _religion, int _count, String _age, int _age_count){
		type = _type;
		religion = _religion;
		religion_count = _count;
		age = _age;
		age_count = _age_count;
	}
	
	public String getType(){
		return type;
	}
	public String getReligion(){
		return religion;
	}
	public int getReligionCount(){
		return religion_count;
	}
	public String getAge(){
		return age;
	}
	public int getAgeCount(){
		return age_count;
	}
	//===================================================
	// set functions
	//===================================================
	public void setType(String _type){
		type = _type;
	}
	public void setReligion(String _religion){
		religion = _religion;
	}
	public void setReligionCount(int _count){
		religion_count = _count;
	}
	public void setAge(String _age){
		age = _age;
	}
	public void setAgeCount(int _count){
		age_count = _count;
	}
}
