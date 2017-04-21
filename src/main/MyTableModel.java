package main;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class MyTableModel extends AbstractTableModel{
	Vector<Vector> data;
	Vector<String> columnName;
	
	public MyTableModel(Vector<Vector> data,	Vector<String> columnName) {
		this.data=data;
		this.columnName=columnName;
	}
	
	public String getColumnName(int column) {
		return columnName.get(column);
	}

	public int getColumnCount() {
		return columnName.size();
	}

	public int getRowCount() {
		return data.size();
	}

	public Object getValueAt(int row, int col) {
		return data.get(row).get(col);
	}
	

}
