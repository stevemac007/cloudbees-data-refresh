package com.fixturetime.data.refresh

import java.sql.SQLException;

import groovy.sql.*

class DBModification {
	
	def db;
	
	public DBModification(String url, String username, String password, String driver) {
		db = [url:url, user:username, password:password, driver:driver]
	}

	public void doUpdate(List<String> sqls) {

		def sql = Sql.newInstance(db.url, db.user, db.password, db.driver)
		
		try {
			sqls.each {
				println "Executing '${it}'."
				sql.executeUpdate it
			}
		}
		catch (SQLException ex) {
			println "ERROR : ${ex.message}"
		}

		sql.close()
	}
}