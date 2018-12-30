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
	 * ����ϰ� Vector<PoliceOffice>�� �Ϸ� ������ �� ������ ���� ���� ������ �ٲ�.
	 ***********************************************/
	public static Vector<String> name = new Vector<String>();
	public static Vector<String> reli = new Vector<String>();
	public static Vector<String> reli2 = new Vector<String>();
	public static Vector<String> reli3 = new Vector<String>();
	public static Vector<String> addr = new Vector<String>();
	public static Vector<String> lat = new Vector<String>();
	public static Vector<String> lon = new Vector<String>();
	
	/**********************************************
	 * Function - ���������� �о���� �Լ�
	 * ���� �������� ��ġ ������ ����ִ� PoliceOffice ������ �о���� ����Ʈ�� �����Ѵ�.
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
	 * Function - ������ DB���� ä���ִ� �Լ�
	 * �������ϰ� �����ڵ����� ���� ��� ������ Server���� ����ϱ� ���ϰ� ��� DB�� �ִ� �Լ��̴�.
	 ***********************************************/
	public static void setPoliceDB() throws FileNotFoundException, IOException {
		Connection conn = null;
		Statement stmt = null;
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("����̹� �ε� ����");
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:XE","hr","hr");
			System.out.println("DB���� ����");
			
			stmt = conn.createStatement();
			for(int i = 0; i < name.size(); i++){
				stmt.executeQuery("INSERT INTO policeoffice VALUES('" + name.elementAt(i) + "', '" + 
						reli.elementAt(i) + "', '" + reli2.elementAt(i) + "', '" + 
						reli3.elementAt(i) + "', '" + addr.elementAt(i) + "', '"+
						lat.elementAt(i) + "', '" + lon.elementAt(i) + "')");
			}
		}catch(ClassNotFoundException cnfe){
			System.out.println("�ش� Ŭ������ ã�� �� �����ϴ�." + cnfe.getMessage());
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
	 * Function - �ּ� => ����, �浵
	 * "�泲 õ�Ƚ� ������ 1004����"�� ���� �ּҷ� �Ǿ��ִ� ���¸� ����, �浵 ������ �ٲ��ִ� naver api.
	 * �ѱ� �ּҺ�ȯ�� ���ۺ��� ���̹��� ��Ȯ�ؼ� naver ���� api�� ���������, client-id���� client-secret���� 
	 * ��� ��ǻ�Ϳ��� �״�� �ᵵ �������. ��, ����� �����Ǵ� �̻� �Ϸ翡 100000�� �̻� ȣ���� �� ����.
	 * Json �����ͷ� ���޵Ǵ� sentence�� �Ľ��ؼ� ����ϱ� ���ϰԲ� ����� ���� �Լ��̴�.
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
				String latitude = points.get("y").toString();	//����. y���̰�, 37~38 ������ ���̴�.
				String longitude = points.get("x").toString();	//�浵. x���̰�, 126~127 ������ ���̴�.
				lat.add(latitude);
				lon.add(longitude);
			}catch(ParseException e){
				System.out.println(e);
			}
		//}//addr.size
	}
}
