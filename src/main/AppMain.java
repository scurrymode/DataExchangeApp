package main;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.SAXException;

public class AppMain extends JFrame implements ActionListener{
	JPanel p_left, p_right;
	JButton bt_open, bt_save, bt_json;
	JTable table;
	JScrollPane sc_table, sc_area;
	JTextArea area;
	JFileChooser chooser;
	Vector<Vector> data = new Vector<Vector>();
	Vector<String> columnName = new Vector<String>();
	String tableName;
	DBManager manager = DBManager.getInstance();
	Connection con;
	
	public AppMain() {
		p_left = new JPanel();
		p_right = new JPanel();
		bt_open = new JButton("xml열기");
		bt_save = new JButton("oracle에 저장");
		bt_json = new JButton("json으로 Export");
		table = new JTable(null);
		area = new JTextArea(25,40);
		sc_table = new JScrollPane(table);
		sc_area = new JScrollPane(area);
		chooser = new JFileChooser("C:/java_workspace2/DataExchangeApp/data");
		
		this.setLayout(new GridLayout(1, 2));

		
		p_left.add(bt_open);
		p_left.add(bt_save);
		p_left.add(sc_table);
		p_right.add(bt_json);
		p_right.add(sc_area);
		
		add(p_left);
		add(p_right);
		
		bt_open.addActionListener(this);
		bt_save.addActionListener(this);
		bt_json.addActionListener(this);
		
		setSize(1000, 600);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if(obj == bt_open){
			//xml파일을 하나 열어서 그걸 가지고 논다.
			//선택해서 오픈하면 그걸 table로 보여준다.
			int result = chooser.showOpenDialog(this);
			if(result == JFileChooser.APPROVE_OPTION){
				File file = chooser.getSelectedFile();//선택한 파일
				String fileName =file.getName();//파일 이름 
				String[] name = fileName.split("\\.");//파일 이름 확장자와 분리!
				tableName=name[0];
				SAXParserFactory factory = SAXParserFactory.newInstance();
				try {
					SAXParser parser = factory.newSAXParser();
					parser.parse(file, new MyHandler(data, columnName,tableName));
					//파스해온걸 가지고 일단 테이블에 뿌려야 한다. 근데 나중에는 이거 DB에도 뿌려줘야 해서 그걸 생각해라~!
					//다 담아온걸 테이블 모델에 넣어주고 그걸 뿌리자	
					MyTableModel myTableModel = new MyTableModel(data, columnName);
					table.setModel(myTableModel);
					table.updateUI();
				} catch (ParserConfigurationException e1) {
					e1.printStackTrace();
				} catch (SAXException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}	
			}
			
		}else if(obj == bt_save){
			save();
			
		}else if(obj == bt_json){
			write();
		}
		
	}
	
	public void save(){
		con=manager.getConnection();
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			//같은 이름 테이블 있는지 부터 확인
			String sql0="select table_name from user_tables";
			pstmt=con.prepareStatement(sql0);
			rs=pstmt.executeQuery();
			
			ArrayList<String> tables = new ArrayList<String>();
			while(rs.next()){
				tables.add(rs.getString(1));
			}
			
			boolean flag =false;
			for(int i=0;i<tables.size();i++){
				if(tableName.equalsIgnoreCase(tables.get(i))){
					flag=true; //같은게 있다.
					if(flag){
						String sql_drop_seq="drop sequence seq_"+tableName;
						pstmt=con.prepareStatement(sql_drop_seq);
						pstmt.executeUpdate();
					}
				}
			}
			if(flag){
				String sql_drop="drop table "+tableName;
				pstmt=con.prepareStatement(sql_drop);
				pstmt.executeUpdate();
				System.out.println("여기?");
			} 
			String sql_drop_create = "create table "+tableName+" ("+columnName.get(0)+" varchar2(20), "+columnName.get(1)+" varchar2(20), "+columnName.get(2)+" varchar2(20), "+columnName.get(3)+" varchar2(20))";
			pstmt=con.prepareStatement(sql_drop_create);
			pstmt.executeUpdate();
			
//			String sql = "create table ? (? varchar2(20), ? varchar2(20), ? varchar2(20), ? varchar2(20) )";
//			StringBuffer sb_create = new StringBuffer();
//			sb_create.append("create table ? (? varchar2(20), ? varchar2(20),");
//			sb_create.append(" ? varchar2(20), ? varchar2(20) )");
//			pstmt=con.prepareStatement(sql);
//			pstmt.setString(1, tableName);
//			for(int i=0;i<columnName.size();i++){
//				pstmt.setString(i+2, (String)columnName.get(i));
//				System.out.println(columnName.get(i));
//			}
//			pstmt.executeUpdate();
			
			String sql_seq = "create sequence seq_"+tableName+" increment by 1 start with 1";
			pstmt=con.prepareStatement(sql_seq);
			pstmt.executeUpdate();
			
			
			for(int j=0; j<data.size(); j++){
			StringBuffer sb_insert = new StringBuffer();	
			sb_insert.append("insert into "+tableName+" ("+columnName.get(0)+", "+columnName.get(1)+", "+columnName.get(2)+", "+columnName.get(3)+")");
			sb_insert.append(" values ('"+(String)data.get(j).get(0)+"', '"+(String)data.get(j).get(1)+"', '"+(String)data.get(j).get(2)+"','"+(String)data.get(j).get(3)+"')");
			pstmt=con.prepareStatement(sb_insert.toString());
			pstmt.executeUpdate();
			}
//			
//			for(int j=0;j<data.size();j++){
//				pstmt.setString(1, tableName);			
//				for(int i=0;i<columnName.size();i++){
//					pstmt.setString(i+2, columnName.get(i));
//				}
//				for(int i=0;i<columnName.size();i++){
//					pstmt.setString(i+6, (String)data.get(j).get(i));
//				}
//				pstmt.executeUpdate();
//			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if(pstmt!=null){
				try {
					pstmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void write(){
		//일단 데이터 가져오기!
		
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		
		String sql = "select * from "+tableName;
		Vector<String> columnName1 = new Vector<>();
		Vector<Vector> data1 = new Vector<Vector>();
		try {
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			System.out.println(meta.getColumnCount());
			for(int i=0; i<meta.getColumnCount();i++){
				columnName1.add(meta.getColumnName(i+1));
			}
			while(rs.next()){
				Vector<String> vec = new Vector<String>();
				for(int i =0; i<meta.getColumnCount();i++){
					vec.add(rs.getString(i+1));
				}
				data1.add(vec);
			} 
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		JSONObject info = new JSONObject();
		JSONArray array = new JSONArray();
		
		for(int j=0;j<data.size();j++){
			for(int i=0; i<columnName1.size();i++){
				info.put(columnName1.get(i), data1.get(j).get(i));
				array.add(info);
			}
		}
		JSONObject arrayObj = new JSONObject();
		arrayObj.put(tableName+"s",array);
		
		
//		StringBuffer sb = new StringBuffer();
//		sb.append("[");
//		for(int j = 0;j<data1.size();j++){
//			sb.append("{");
//			for(int i = 0;i<columnName1.size();i++){
//				sb.append("\""+columnName1.get(i)+"\":\""+data1.get(j).get(i)+"\"");
//				if(i==columnName1.size()-1){
//					sb.append("}\n");
//				}else{
//					sb.append(",\n");
//				}
//			}
//			if(j==data1.size()-1){
//				sb.append("\n");
//			}else{
//				sb.append(",\n");
//			}
//		}
//		sb.append("]");
//		area.append(sb.toString());
		area.setText(arrayObj.toJSONString());
		area.updateUI();
	}
	
	public static void main(String[] args){
		new AppMain();
		
	}

}
