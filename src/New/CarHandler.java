package New;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CarHandler extends DefaultHandler{
	Map<String,String>	cols;
	Vector<Vector> data;
	
	//���� �̺�Ʈ�� �߻���Ű�� ������� ��ġ�� �˱� ���� üũ ����
	boolean cars;
	boolean car;
	boolean brand;
	boolean name;
	boolean price;
	boolean color;
	Vector vec; //vo, dto����
	
	public void startDocument() throws SAXException {
		data = new Vector<Vector>();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		//dto ����� ���͸� �غ�����~!(JTabl�� dto�� �ν� ���ϴϱ�~~!)
		if(qName.equalsIgnoreCase("cars")){
			cars=true;
		}else if(qName.equalsIgnoreCase("car")){
			car=true;
			cols = new HashMap<String,String>();
		}else if(qName.equalsIgnoreCase("brand")){
			cols.put(qName, "varchar2(20)");
			brand=true;
		}else if(qName.equalsIgnoreCase("name")){
			cols.put(qName, "varchar2(30)");
			name=true;
		}else if(qName.equalsIgnoreCase("price")){
			cols.put(qName, "number");
			price=true;
		}else if(qName.equalsIgnoreCase("color")){
			cols.put(qName, "varchar2(20)");
			color=true;
		}
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		if(car){
			car=false;
			vec = new  Vector();
		}else if(brand){
			brand=false;
			vec.add(new String(ch, start, length));
		}else if(name){
			name=false;
			vec.add(new String(ch, start, length));
		}else if(price){
			price=false;
			vec.add(new String(ch, start, length));
		}else if(color){
			color=false;
			vec.add(new String(ch, start, length));
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		//�� car���� ������ ���Ϳ� ����!!
		if(qName.equalsIgnoreCase("car")){
			data.add(vec);
		}
	}
	
	public void endDocument() throws SAXException {
		System.out.println("�� ����� ����?" + data.size());
	}
	
	
	
	
	
}
