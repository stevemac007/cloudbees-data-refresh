package com.fixturetime.data.refresh

class XMLDatabaseRefresh {

	public static void main(String[] args) {
		
		def refreshConfig = new XmlParser().parse("database-refresh.xml")
		
		def engine = new CloudbeesDatabaseRefreshEngine()
		
		def configOK = true
		
		def cloudbeesConfig = refreshConfig.cloudbeesConfig
		def sourceConfig = refreshConfig.source
		def destinationConfig = refreshConfig.destination
		
		if (cloudbeesConfig.isEmpty()) {
			println "Missing <cloudbeesConfig> section, expected"
			println "  <cloudbeesConfig>"
			println "  	  <apiKey></apiKey> -- required"
			println "  	  <secret></secret> -- required"
			println "  </cloudbeesConfig>"
			configOK = false
		}
		else {
			if (cloudbeesConfig.apiKey.text().isEmpty()) {
				println "Missing <cloudbeesConfig> section, expected"
				println "  	  <apiKey></apiKey> -- required"
				configOK = false
			}
			
			if (cloudbeesConfig.apiKey.text().isEmpty()) {
				println "Missing <cloudbeesConfig> section, expected"
				println "  	  <secret></secret> -- required"
				configOK = false
			}
		}
		
		if (sourceConfig.isEmpty()) {
			println "Missing <source> section, expected"
			println "  <source>"
			println "  	  <sourceDBId></sourceDBId> -- required, the ID of the database to clone from"
//			println "  	  <sourceSnapshotId></sourceSnapshotId> -- required, the ID of the database to clone from"
			println "  	  <takeNewSnapshot></takeNewSnapshot> -- optional, if 'true' will snapshot the current DB before the deploy"
			println "  </source>"
			configOK = false
		}
		else {
			if (sourceConfig.sourceDBId.text().isEmpty()) {
				println "Missing <sourceConfig> section, expected"
				println "  	  <sourceDBId></sourceDBId> -- required, the ID of the database to clone from"
				configOK = false
			}
		}

		if (destinationConfig.isEmpty()) {
			println "Missing <destination> section, expected"
			println "  <destination>"
			println "  	  <destinationDBId></destinationDBId> -- required, the ID of the database to deploy to"
			println "  	  <applicationId></applicationId> -- optional, cloudbees application to shutdown during db deploy"
			println "  </destination>"
			configOK = false
		}
		
		if (!configOK) {
			return;
		}
		
		if (!cloudbeesConfig.serverAPIUrl.isEmpty()) {
			engine.serverAPIUrl = cloudbeesConfig.serverAPIUrl.text()
		}
		engine.apiKey = cloudbeesConfig.apiKey.text()
		engine.secret = cloudbeesConfig.secret.text()
		
		engine.sourceDBId = sourceConfig.sourceDBId.text()
		engine.destinationDBId = destinationConfig.destinationDBId.text()
		
		if (!sourceConfig.takeNewSnapshot.text().isEmpty()) {
			engine.takeNewSnapshot = "true".equalsIgnoreCase(sourceConfig.takeNewSnapshot.text())
		}

		if (!destinationConfig.applicationId.isEmpty()) {		
			engine.applicationId = destinationConfig.applicationId.text()
		}
		
		engine.sanatiseSQLStatements = refreshConfig.postDeploySQL.findAll().'@sql'
		
		engine.processRefresh()
	}
}
