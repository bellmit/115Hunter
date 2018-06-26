package com.sap.cisp.xhna.data.storage;

public class ColumnType {
	private String column_name;
	private int column_type;
	private String type_name;
	
	public String getColumn_name() {
		return column_name;
	}
	public int getColumn_type() {
		return column_type;
	}
	public String getType_name() {
		return type_name;
	}
	
	public ColumnType(String column_name, int column_type, String type_name){
		this.column_name = column_name;
		this.column_type = column_type;
		this.type_name = type_name;
	}
	
	@Override
	public String toString() {
		return "DBColumnType [column_name=" + column_name + ", column_type="
				+ column_type + ", type_name=" + type_name + "]";
	}
	
}
