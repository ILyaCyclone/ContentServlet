Для запуска приложения необходимо настроить конфигурационный файл content.properties. Он находится в папке src/main/resources и в нем есть два пункта - это использование кэша и имя jndi datasource для подключения к бд:

	ru.unisuite.contentservlet.usecache=false
	ru.unisuite.contentservlet.jndi.datasource.name=jdbc/ds_basic
