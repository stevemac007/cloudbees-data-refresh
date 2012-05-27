package com.fixturetime.data.refresh

import java.util.List;

import com.cloudbees.api.BeesClient;
import com.cloudbees.api.BeesClientConfiguration;
import com.cloudbees.api.DatabaseSnapshotInfo;
import com.cloudbees.api.DatabaseSnapshotListResponse;
import com.mysql.jdbc.Driver;

class CloudbeesDatabaseRefreshEngine {

	String serverAPIUrl = "https://api.cloudbees.com/api";
	String apiKey = "";
	String secret = "";
	String format = "json";
	String version = "1";
	
	boolean takeNewSnapshot = false;
	
	String sourceDBId = null;
	String destinationDBId = null;
	
	String applicationId = null;
	List<String> sanatiseSQLStatements = null;
	
	public void processRefresh() throws Exception {
		def config = new BeesClientConfiguration(serverAPIUrl, apiKey, secret, format, version);
		def client = new BeesClient(config);
		
		if (takeNewSnapshot) {
			println "Creating new database snapshot for source database '${sourceDBId}'."
			client.databaseSnapshotCreate(sourceDBId, "Snapshot for refresh to " + destinationDBId);
		}
		
		println "Listing database snapshots for source database '${sourceDBId}'."
		DatabaseSnapshotListResponse databaseList = client.databaseSnapshotList(sourceDBId);
		
		databaseList.snapshots.each {
			println "  - found ${it.id} - ${it.created} (${it.title})"
		}
	
		def database = databaseList.snapshots[0]
		println "Using database ID ${database.id} for the refresh."
		
		if (applicationId != null) {
			println "Stopping application '${applicationId}' before database refresh."
			client.applicationStop(applicationId)
		}
		
		println "Deploying database snapshot '${database.id}' to database '${destinationDBId}'."
		client.databaseSnapshotDeploy(destinationDBId, database.getId());
		
		if (sanatiseSQLStatements != null && !sanatiseSQLStatements.isEmpty()) {
			def dbInfo = client.databaseInfo(destinationDBId, true)
			
			def url = "jdbc:mysql://${dbInfo.master}/${dbInfo.name}"
			println "Connecting to database '${destinationDBId}' via URL '${url}'."
			DBModification mod = new DBModification(url, dbInfo.username, dbInfo.password, Driver.class.getName());
			
			println "Executing SQL statements : '${sanatiseSQLStatements}'."
			mod.doUpdate(sanatiseSQLStatements);
		}
		
		if (applicationId != null) {
			println "Restarting application '${applicationId}' before database refresh."
			client.applicationStart(applicationId);
		}
	}
}
