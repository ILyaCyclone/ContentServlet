Для запуска приложения необходимо в JNDI свойство "contentServlet/configFileLocation", которое указывает на нахождение конфигурационного файла для ContentServlet.
Также потребуется "jdbc/ds_basic" для подключения к бд.

Пример настройки (в файле context.xml для tomcat) для tomcat и oracle:

	<Environment type="java.lang.String"
		value="C:\Users\anotherUser\Desktop\cache\ContentServletConfig.xml"
		name="contentServlet/configFileLocation" />

	<Resource name="jdbc/ds_basic" type="javax.sql.DataSource"
		maxTotal="10"
		url="jdbc:oracle:thin:@ip:port/dbname"
		driverClassName="oracle.jdbc.OracleDriver"
		connectionProperties="oracle.net.READ_TIMEOUT=5000;"
		password="somePassword" username="someUsername" maxWaitMillis="10000"
		maxIdle="300" auth="Container" />
