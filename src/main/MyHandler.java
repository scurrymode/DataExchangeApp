package main;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MyHandler extends DefaultHandler{
	String name;
	Vector<Vector> data;
	Vector<String> columnName;
	boolean flag_q = false;
	boolean flag_ch	= false;
	Vector<String> vec;
	
	public MyHandler(Vector<Vector> data, Vector<String> columnName, String name) {
		this.data=data;
		this.columnName=columnName;
		this.name=name;
	}
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if(qName.equalsIgnoreCase(name)){
			//파일 명이랑 같은 태그 나오면 그때부터 1차원 벡터만들어서 담기
			vec = new Vector<String>();
			columnName.removeAllElements();
			if(columnName.size()==0){
				flag_q=true;
			}
		}
		if(flag_q&&!qName.equalsIgnoreCase(name)){
			//컬럼 제목도 넣기
			columnName.add(qName);
			flag_ch=true;
		}
	}	

	public void characters(char[] ch, int start, int length) throws SAXException {
		if(flag_ch){
			String str = new String(ch, start, length);
			vec.add(str);
			flag_ch=false;
		}
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equalsIgnoreCase(name)){
			data.add(vec);
			flag_q=false;
		}
	}
	

}
