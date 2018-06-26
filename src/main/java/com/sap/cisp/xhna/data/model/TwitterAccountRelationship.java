package com.sap.cisp.xhna.data.model;

public class TwitterAccountRelationship {
	private String userid;
	private String follower_id;
	
	public TwitterAccountRelationship(String line)
	{
		String[] lines = line.split(";");
		this.userid = lines[0];
		this.follower_id = lines[1];
	}
	
	public String toString(){
		return String
				.format("%s\t%s", userid, follower_id);
	}
}
