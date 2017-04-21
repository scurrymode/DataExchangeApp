package New;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class MyModel extends AbstractTableModel{
	Vector<String> cols;
	Vector<Vector> data;
	
	public MyModel(Vector cols, Vector data) {
		this.cols=cols;
		this.data = data;
	}
	

	public String getColumnName(int column) {
		return cols.get(column);
	}

	public int getColumnCount() {
		return cols.size();
	}


	public int getRowCount() {
		return data.size();
	}

	
	public Object getValueAt(int row, int col) {
		return data.get(row).get(col);
	}

}
