package util.file;

public class FileUtil {
	//확장자를 제외한 파일명만 추출하기 메서드
	//c:/data/test.jpg , mario.png
	public static String getOnlyName(String path){
		int point = path.lastIndexOf(".");
		
		String name = path.substring(0, point);
		
		return name;
	}
//	
//	public static void main(String[] args) {
//		System.out.println(getOnlyName("mar.rio.gon.png"));
//	}
}
