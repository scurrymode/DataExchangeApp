package New;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.xml.sax.SAXException;

import main.DBManager;
import util.file.FileUtil;

public class AppMain extends JFrame implements ActionListener{
	JPanel p_left, p_right;
	JButton bt_open, bt_save, bt_export;
	JTable table;
	JScrollPane sc_table, sc_area;
	JTextArea area;
	File file; //파싱 대상 xml파일
	CarHandler carHandler;
	MyModel model;
	DBManager manager;
	Connection con;
	
	String tableName;
	
	Vector<String> vec;
	
	
	public AppMain() {
		p_left = new JPanel();
		p_right = new JPanel();
		bt_open = new JButton("xml열기");
		bt_save = new JButton("oracle에 저장");
		bt_export = new JButton("json으로 Export");
		table = new JTable(null);
		area = new JTextArea(20,32);
		sc_table = new JScrollPane(table);
		sc_area = new JScrollPane(area);
		
		file = new File("C:/java_workspace2/DataExchangeApp/data/car.xml");
		
		manager = DBManager.getInstance();
		con= manager.getConnection();
		
		this.setLayout(new GridLayout(1, 2));
		
		p_left.add(bt_open);
		p_left.add(bt_save);
		p_left.add(sc_table);
		p_right.add(bt_export);
		p_right.add(sc_area);
		
		add(p_left);
		add(p_right);
		
		//버튼에 리스너 연결
		bt_open.addActionListener(this);
		bt_save.addActionListener(this);
		bt_export.addActionListener(this);
		
		//윈도우 끌때 연결도 끊고 다 끄기~!
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				manager.disConnect(con);
				System.exit(0);
			}
		});
		
		setSize(800, 600);
		setVisible(true);
//		setDefaultCloseOperation(EXIT_ON_CLOSE); 윈도우 리스너에다 달아버림
		
	}
	
	public void open(){
		SAXParserFactory factory=SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(file, carHandler=new CarHandler());
			
			vec=new Vector(carHandler.cols.keySet());
			System.out.println(vec.get(0));
			
			//JTable에 xml 파싱 결과 출력하기!!
			model = new MyModel(vec, carHandler.data);
			table.setModel(model);//테이블모델 적용
			table.updateUI();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void save(){
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		
		tableName = FileUtil.getOnlyName(file.getName());
		
		StringBuffer sql = new StringBuffer();
		//테이블 존재여부를 판단하고, 테이블 생성
		sql.append("select table_name from user_tables");
		sql.append(" where table_name=?");
		
		try {
			pstmt=con.prepareStatement(sql.toString());
			pstmt.setString(1, tableName.toUpperCase());
			rs=pstmt.executeQuery();
			
			//레코드가 있다면...(중복될 테이블이 존재)
			if(rs.next()){
				//이미 존재했던 테이블을 제거(drop)
				sql.delete(0, sql.length());
				sql.append("drop table "+tableName);
				try {
					pstmt=con.prepareStatement(sql.toString());
					pstmt.executeUpdate();
					JOptionPane.showMessageDialog(this, "이미 존재하는 테이블 삭제");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			//테이블 생성!!
			sql.delete(0, sql.length());
			sql.append("create table "+tableName+"(");
			sql.append(tableName+"_id number primary key");
			
			
			Set set =carHandler.cols.keySet();
			Iterator it = set.iterator();
			
			while(it.hasNext()){//있을때까지
				//키 값만 꺼냈다.
				String key = (String)it.next();
				String value = carHandler.cols.get(key);
				sql.append(", "+key+" "+value);
			}
			sql.append(")");
//			내방식
//			for(int i=0; i<carHandler.cols.size();i++){
//				sql.append(", "+vec.get(i)+" "+carHandler.cols.get(vec.get(i)));
//			}
//			sql.append(")");
			
			System.out.println(sql);
			
			
			//생성쿼리 수행
			pstmt=con.prepareStatement(sql.toString());
			pstmt.executeUpdate();
			JOptionPane.showMessageDialog(this, tableName+" 생성 완료!!");
			
			//시퀀스 생성!!
			sql.delete(0, sql.length());
			sql.append("select sequence_name from user_sequences where sequence_name=?");
			pstmt=con.prepareStatement(sql.toString());
			pstmt.setString(1, ("seq_"+tableName).toUpperCase());
			rs=pstmt.executeQuery();
			if(!rs.next()){
				//같은 이름의 시퀀스가 없을때만 시퀀스 생성
				sql.delete(0, sql.length());
				sql.append("create sequence seq_"+tableName);
				sql.append(" increment by 1 start with 1");
				pstmt=con.prepareStatement(sql.toString());
				pstmt.executeUpdate();
				JOptionPane.showMessageDialog(this, "시퀀스 생성 완료");
			}
			
			//insert!
			sql.delete(0, sql.length());
			sql.append("insert into car (car_id, brand, name, price, color)");
			sql.append(" values (seq_car.nextval,?,?,?,? )");
			
			
			pstmt=con.prepareStatement(sql.toString());
			
			//테이블에서 값 가져와서 넣기~!
			for(int a=0; a<table.getRowCount();a++){
				for(int i=0; i<table.getColumnCount();i++){
					String value =(String)table.getValueAt(a, i);
					pstmt.setString(i+1, value);
				}
				pstmt.executeUpdate();
			}
			JOptionPane.showMessageDialog(this, "레코드 등록 완료");
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if(rs!=null){
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}if(pstmt!=null){
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}		
	}
	
	public void export(){
		PreparedStatement pstmt= null;
		ResultSet rs = null;
		
		StringBuffer sql = new StringBuffer();
		sql.append("select * from "+tableName);
		try {
			pstmt=con.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs=pstmt.executeQuery();
			rs.last();
			int total = rs.getRow();
			rs.beforeFirst();
			
			sql.delete(0, sql.length());
			sql.append("{\"cars\":[ \n");
			int count=0;
			while(rs.next()){
				count++;
				sql.append("{\n");
				sql.append("\"brand\":\""+rs.getString("brand")+"\",\n");
				sql.append("\"name\":\""+rs.getString("name")+"\",\n");
				sql.append("\"price\":"+rs.getInt("price")+",\n");
				sql.append("\"color\":\""+rs.getString("color")+"\"\n");				
				if(count<total){
					sql.append("},\n");
				}else{
					sql.append("}\n");
				}
			}
			sql.append("]}");
			
			area.setText(sql.toString());
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	
		
		
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if(obj == bt_open){
			open();
		}else if(obj == bt_save){
			save();
		}else if(obj == bt_export){
			export();
		}
	}
	
	public static void main(String[] args){
		new AppMain();
		
	}

}
