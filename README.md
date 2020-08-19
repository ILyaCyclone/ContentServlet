Приложение для отдачи бинарных файлов из схемы контента

# Endpoints

/get - отдача контента

/help - инструкция и информация о сборке

/metrics - метрики

/config - текущий конфиг

# Настройка

## Соединение с БД
Внешний datasource по JNDI-имени
> contentservlet.datasource.jndi-name=jdbc/ds_basic

Или внутренний HikariCP
> contentservlet.datasource.url=jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=gdb1.miit.ru)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=cgeneral.miit.ru)))  
> contentservlet.datasource.username=wlapp_u1t  
> contentservlet.datasource.password=

## Общие настройки
Значение HTTP заголовка cache-control в ответе по умолчанию  
Значение: число будет преобразовано в public, max-age=<число>  
Строка будет использована в качестве значения без изменений.
> contentservlet.cachecontrol=public, max-age=86400

Imageprocessor по умолчанию
Значения: "db", "th" - thumbnailator, "im" - imagemagick, "imaginary"
> contentservlet.resizer-type=db

Качество изображения по умолчанию (не поддерживается при contentservlet.resizer-type=db)
Значение между 0 и 100.
> contentservlet.image-quality=80

Включены ли метрики.
Значения: true, false
> contentservlet.enable-metrics=true


URL-паттерн, по которому слушаются контент-запросы
> contentservlet.content-url-pattern=/get/*

(не реализовано) URL-паттерн, по которому слушаются контент-запросы, требующие аутентификацию
> contentservlet.content-secure-url-pattern=/get/private/*

адрес отдельного конфиг-файла приложения в файловой системе
> contentservlet.properties=/somewhere/content/content.properties

адрес отдельного файла конфигурации Logback
> logback.configurationFile=/somewhere/content/logback.xml


## Настройки imageprocessor'ов

Thumbnailator
Antialiasing
Значения: on, off, default
> imageprocessor.thumbnailator.antialiasing=

Rendering
Значения: quality, speed, default
> imageprocessor.thumbnailator.rendering=

ImageMagick
Путь в файловой системе к ImageMagick
> imageprocessor.im4java.toolpath=C:\\Program Files\\ImageMagick-7.0.10-Q16

Imaginary
URL сервера Imaginary
> imageprocessor.imaginary.base-url=http://10.242.101.40:9080
>