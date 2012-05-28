This library is a work in progress that currently you need to run via an IDE to bring in the dependancies correctly,  
hopefully I will get this updated to work as a standalone command line, and a maven plugin.

To get this working, open the source in your IDE of choice (eclipse for me).

Create the refres definition XML file (structure below)

Execute the XMLDatabaseRefresh.groovy file.

Required XML Structure
-----------------------------------
<database-refresh>
	<cloudbeesConfig>
		<apiKey>1234ABCD</apiKey>
		<secret>SDSAFASFSAFSDFAS/ASDFASFSAFSAFSDFS=</secret>
	</cloudbeesConfig>
	<source>
		<sourceDBId>myapplication-production</sourceDBId>
		<takeNewSnapshot>true</takeNewSnapshot>
	</source>
	<destination>
		<destinationDBId>myapplication-development</destinationDBId>
		<applicationId>stevemac/myapplication</applicationId>
	</destination>
	
	<!-- reset all of the user's email addresses -->
	<postDeploySQL sql="update USER set email = 'noone@example.org' where email is not null" />
	
	<!-- reset all production passwords to 'testpwd' -->
	<postDeploySQL sql="update USER set password = 'testpwd'" />
	
</database-refresh>

[![Build Status](https://buildhive.beescloud.com/job/stevemac007/job/cloudbees-data-refresh/badge/icon)](https://buildhive.beescloud.com/job/stevemac007/job/cloudbees-data-refresh/)