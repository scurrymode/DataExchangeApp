package util.file;

public class FileUtil {
	//Ȯ���ڸ� ������ ���ϸ� �����ϱ� �޼���
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
