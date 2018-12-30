import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

public class OpenAPI {
	/**********************************************
	 * Variables.
	 * 깔끔하게 Vector<PoliceOffice>로 하려 했으나 알 수없는 로직 에러 때문에 바꿈.
	 ***********************************************/
	public static Vector<String> name = new Vector<String>();
	public static Vector<String> reli = new Vector<String>();
	public static Vector<String> reli2 = new Vector<String>();
	public static Vector<String> reli3 = new Vector<String>();
	public static Vector<String> addr = new Vector<String>();
	public static Vector<String> lat = new Vector<String>();
	public static Vector<String> lon = new Vector<String>();
	
	/**********************************************
	 * Function - 엑셀파일을 읽어오는 함수
	 * 전국 경찰서의 위치 정보가 들어있는 PoliceOffice 파일을 읽어오고 리스트에 저장한다.
	 ***********************************************/
	public static void readEXCEL() throws FileNotFoundException, IOException{
		FileInputStream fis = new FileInputStream("C:\\policeDB.xlsx");
		XSSFWorkbook workbook = new XSSFWorkbook(fis);
		int rowIndex = 0; 
		int columnIndex = 0;
		int num = 0;
		String temp_name = "", temp_religion = "", temp_religion2 = "", temp_religion3 = "", temp_address = "";
		PoliceOffice newoffice = new PoliceOffice();
		
		XSSFSheet sheet = workbook.getSheetAt(0);
		
		int rows = sheet.getPhysicalNumberOfRows();
		for(rowIndex = 1; rowIndex < rows; rowIndex++){
			XSSFRow row  = sheet.getRow(rowIndex);
			if(row != null){
				int cells = row.getPhysicalNumberOfCells();
				for(columnIndex = 1; columnIndex <= cells; columnIndex++){
					XSSFCell cell = row.getCell(columnIndex);
					String value = "";
					if(num == 6){
						num = 0;
					}
					if(cell == null){
						continue;
					}
					else{
						switch(cell.getCellType()){
						case XSSFCell.CELL_TYPE_FORMULA:
							value = cell.getCellFormula();
							break;
						case XSSFCell.CELL_TYPE_NUMERIC:
							value = cell.getNumericCellValue() + "";
							break;
						case XSSFCell.CELL_TYPE_STRING:
							value = cell.getStringCellValue() + "";
							break;
						case XSSFCell.CELL_TYPE_BLANK:
							value = cell.getBooleanCellValue() + "";
							break;
						case XSSFCell.CELL_TYPE_ERROR:
							value = cell.getErrorCellValue() + "";
							break;
						}
					}
					if(num == 0){
						reli.add(value);
					} else if(num == 1){
						reli2.add(value);
					} else if(num == 2){
						reli3.add(value);
					} else if(num == 4){
						name.add(value);
					} else if(num == 5){
						addr.add(value);
					}
					num++;
				}
			}
		}
		//for(int i = 0; i < addr.size(); i++){
		//	System.out.println(addr.elementAt(i));
		//}
	}
	/**********************************************
	 * Function - 경찰서 DB값을 채워주는 함수
	 * 엑셀파일과 지오코딩으로 얻은 모든 값들을 Server에서 사용하기 편하게 모두 DB에 넣는 함수이다.
	 ***********************************************/
	public static void setPoliceDB() throws FileNotFoundException, IOException {
		Connection conn = null;
		Statement stmt = null;
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("드라이버 로딩 성공");
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:XE","hr","hr");
			System.out.println("DB연결 성공");
			
			stmt = conn.createStatement();
			for(int i = 0; i < name.size(); i++){
				stmt.executeQuery("INSERT INTO policeoffice VALUES('" + name.elementAt(i) + "', '" + 
						reli.elementAt(i) + "', '" + reli2.elementAt(i) + "', '" + 
						reli3.elementAt(i) + "', '" + addr.elementAt(i) + "', '"+
						lat.elementAt(i) + "', '" + lon.elementAt(i) + "')");
			}
		}catch(ClassNotFoundException cnfe){
			System.out.println("해당 클래스를 찾을 수 없습니다." + cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	public void callGeo() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException{
		System.out.println("address size : " + addr.size());
		for(int i = 0; i < addr.size(); i++){
			System.out.println("element : "+ addr.elementAt(i));
			GeoCoding(addr.elementAt(i));
		}
	}
	/**********************************************
	 * Function - 주소 => 위도, 경도
	 * "충남 천안시 두정동 1004번지"와 같이 주소로 되어있는 형태를 위도, 경도 값으로 바꿔주는 naver api.
	 * 한국 주소변환은 구글보다 네이버가 정확해서 naver 지도 api를 사용했으며, client-id값과 client-secret값은 
	 * 모든 컴퓨터에서 그대로 써도 상관없다. 단, 무료로 제공되는 이상 하루에 100000번 이상 호출할 수 없다.
	 * Json 데이터로 전달되는 sentence를 파싱해서 사용하기 편하게끔 만들어 놓은 함수이다.
	 ***********************************************/
	public static void GeoCoding(String address) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException{
		//for(int i = 33; i < addr.size(); i++){
			//String address = addr.elementAt(i);
			String url = "";
			//String real_address = URLEncoder.encode(address,"UTF8");
			HttpURLConnection urlConnection = null;
			try{
				url = "https://openapi.naver.com/v1/map/geocode?"  
						 +"clientId=JgEY8HTMtsH1cZUCs6AP" +"&encoding=utf-8&coord=latlng" + "&output=json" + "&query=" + URLEncoder.encode(address,  "utf-8");
				URL obj = new URL(url);
				urlConnection = (HttpURLConnection)obj.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setRequestProperty("X-Naver-Client-Id", "JgEY8HTMtsH1cZUCs6AP");
				urlConnection.setRequestProperty("X-Naver-Client-Secret", "Yz7guS6n1B");
			}catch(UnsupportedEncodingException e){
				System.out.println(e);
			}
			if(urlConnection == null){
				System.out.println("error at address : ");
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
			String inputLine = "";
			String sentence = "";
			int num = 0;
			while((inputLine = in.readLine()) != null){
				sentence += inputLine;
			}
			System.out.println(sentence);
			try{
				JSONParser jsonParser = new JSONParser();
				JSONObject jsonObject = (JSONObject) jsonParser.parse(sentence);
				JSONObject myResponse = (JSONObject) jsonObject.get("result");
				JSONArray myArray = (JSONArray) myResponse.get("items");
			
				JSONObject inArray = (JSONObject) myArray.get(0);
				JSONObject points = (JSONObject) inArray.get("point");
				System.out.println("x : " + points.get("x"));
				System.out.println("y : " + points.get("y"));
				String latitude = points.get("y").toString();	//위도. y값이고, 37~38 사이의 값이다.
				String longitude = points.get("x").toString();	//경도. x값이고, 126~127 사이의 값이다.
				lat.add(latitude);
				lon.add(longitude);
			}catch(ParseException e){
				System.out.println(e);
			}
		//}//addr.size
	}
}
