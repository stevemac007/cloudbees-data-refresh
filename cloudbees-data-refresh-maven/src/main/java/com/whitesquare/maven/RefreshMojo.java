package com.whitesquare.maven;

//Based on org.codehaus.mojo.tomcat.RunMojo (Copyright 2006 Mark Hobson), which was licensed
//under the Apache License, Version 2.0. You may obtain a copy of the License at
//       http://www.apache.org/licenses/LICENSE-2.0 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.whitesquare.db.refresh.CloudbeesDatabaseRefreshEngine;

/**
 * Refresh a cloudbees database from another cloudbees database
 * 
 * @goal refresh
 * @execute phase = "package"
 * @requiresDependencyResolution runtime
 * 
 */
public class RefreshMojo extends AbstractMojo {

	// ----------------------------------------------------------------------
	// Mojo Parameters
	// ----------------------------------------------------------------------
	/**
	 * The packaging of the Maven project that this goal operates upon.
	 * 
	 * @parameter expression = "${project.packaging}"
	 * @required
	 * @readonly
	 */
	private String packaging;

	/**
	 * The id of the stax application.
	 * 
	 * @parameter expression="${bees.sourceDb}"
	 */
	private String sourceDbid;

	/**
	 * The id of the stax application.
	 * 
	 * @parameter expression="${bees.destinationDb}"
	 */
	private String destinationDbid;

	/**
	 * The id of the stax application.
	 * 
	 * @parameter expression="${bees.destinationDb}"
	 */
	private String applicationId;
	
	/**
	 * Bees api key.
	 * 
	 * @parameter expression="${bees.key}"
	 */
	private String apikey;

	/**
	 * Bees api secret.
	 * 
	 * @parameter expression="${bees.secret}"
	 */
	private String secret;

	/**
	 * Bees deployment server.
	 * 
	 * @parameter expression="${bees.apiurl}" default-value =
	 *            "https://api.cloudbees.com/api"
	 * @required
	 */
	private String apiurl;

	/**
	 * The set of dependencies for the web application being run.
	 * 
	 * @parameter default-value = "${basedir}"
	 * @required
	 * @readonly
	 */
	private String baseDir;
	
	/**
	 * The set of dependencies for the web application being run.
	 * 
	 * @parameter expression="${bees.takeNewSnapshot}" default-value = "false"
	 * @required
	 * @readonly
	 */
	private boolean takeNewSnapshot;

	/*
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		// Read SDK config file
		Properties properties = getConfigProperties();
		// Initialize the parameter values (to allow system property overrides
		// from the command line)
		initParameters(properties);

		CloudbeesDatabaseRefreshEngine engine = new CloudbeesDatabaseRefreshEngine();

		// deploy the application to the server
		try {
			initCredentials();

			initManualConfig();
			
			engine.setApiKey(apikey);
			engine.setSecret(secret);
			
			engine.setSourceDBId(sourceDbid);
			engine.setDestinationDBId(destinationDbid);
			
			engine.setTakeNewSnapshot(takeNewSnapshot);

			engine.setApplicationId(applicationId);
			
//			engine.setSanatiseSQLStatements(postDeploySQL);
			
			engine.processRefresh();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoFailureException(this, "Database refresh failed.", e.getMessage());
		}
	}

	private void initManualConfig() throws IOException {

		if (sourceDbid == null || sourceDbid.isEmpty()) {
			sourceDbid = promptFor("Enter source database name: ");
		}

		if (destinationDbid == null || destinationDbid.isEmpty()) {
			destinationDbid = promptFor("Enter destination database name: ");
		}

		if (applicationId == null || applicationId.isEmpty()) {
			applicationId = promptFor("Enter application name: ");
		}
	}

	private Properties getConfigProperties() {
		Properties properties = new Properties();
		
		File userConfigFile = new File(System.getProperty("user.home"),".bees/bees.config");
		
		if (userConfigFile.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(userConfigFile);
				properties.load(fis);
				fis.close();
			} catch (IOException e) {
				getLog().error(e);
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException ignored) {
					}
				}
			}
		}
		return properties;
	}

	private String getSysProperty(String parameterName, String defaultValue) {
		String value = System.getProperty(parameterName);
		if (value != null)
			return value;
		else
			return defaultValue;
	}

	/**
	 * Initialize the parameter values (to allow system property overrides)
	 */
	private void initParameters(Properties properties) {
		sourceDbid = getSysProperty("bees.sourceDb", sourceDbid);
		destinationDbid = getSysProperty("bees.destinationDb", destinationDbid);

		apikey = getSysProperty("bees.apikey", apikey);
		if (apikey == null)
			apikey = properties.getProperty("bees.api.key");

		secret = getSysProperty("bees.secret", secret);
		if (secret == null)
			secret = properties.getProperty("bees.api.secret");

		apiurl = getSysProperty("bees.api.url", apiurl);
		if (apiurl == null)
			apiurl = properties.getProperty("bees.api.url");

		applicationId = getSysProperty("bees.applicationId", applicationId);
	}

	private void initCredentials() throws IOException {
		boolean promptForApiKey = apikey == null;
		boolean promptForApiSecret = apikey == null || secret == null;
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
		if (promptForApiKey) {
			System.out.print("Enter your CloudBees API key: ");
			apikey = inputReader.readLine();
		}

		if (promptForApiSecret) {
			System.out.print("Enter your CloudBees Api secret: ");
			secret = inputReader.readLine();
		}
	}
	
	private String promptFor(String message) throws IOException {
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
		System.out.print(message);
		String appId = inputReader.readLine();
		return appId;
	}
}
