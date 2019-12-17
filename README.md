# Java-Scrap
Repository for Avalith's Java Scrap mini-challenges
https://trello.com/b/mwq8RB1q/allan-java-scrap

## Web Scraping
Web scraping es una técnica utilizada mediante programas de software para extraer información de sitios web

2 formas de scraping:
- Frontend (HTML - DOM)
- Backend (endpoints)

Si se hacen muchas peticiones te pueden banear. Solucion: VPN

https://sitelabs.es/web-scraping-introduccion-y-herramientas/

## JSOUP
jsoup is a Java library for working with real-world HTML. It provides a very convenient API for extracting and manipulating data, using the best of DOM, CSS, and jquery-like methods.

To use it, we have to add the dependency to the POM.XML 

https://jsoup.org/
https://riptutorial.com/es/jsoup

## JACKSON
JACKSON is a library for JSON objects mapping

https://elbauldelprogramador.com/como-mapear-json-a-objetos-java-con-jackson-objectmapper/

Gson --> https://jarroba.com/gson-json-java-ejemplos/


## MongoDB

instalacion para ubuntu --> https://docs.mongodb.com/manual/tutorial/install-mongodb-on-ubuntu/

sudo apt-get install -y mongodb ---> para instalar el mongodb en ubuntu
sudo service mongodb start ---> para iniciar la base de datos
sudo service mongodb status ----> para comprobar el estado de la bd
sudo service mongodb stop ----> para detener la base de datos
sudo service mongodb restart ----> para reiniciar la bd
mongo -----> para acceder al shell de mongo

uso de mongodb en java----> http://zetcode.com/springboot/mongodb/ (para hacerlo con mongorepository)
https://spring.io/guides/gs/accessing-data-mongodb/

Con MongoRepository, se conecta automaticamente a la base de datos que especifiquemos en el archivo properties, y nos proporciona ya metodos para guardar,borrar , buscar, etc.

https://www.tutorialspoint.com/mongodb/mongodb_java.htm // para hacerlo manualmente (no lo use)

- Coleccion: equivalente a una tabla en sql
- Documento: un registro de la bd


## ROBO3T

Herramienta para administrar visualmente la base de datos MongoDB (GUI)

Para instalar: En ubuntu: desde la store, en windows: desde la pagina.

Tutorial para usar: https://victorroblesweb.es/2017/10/07/trabajar-con-mongodb-visualmente-gui-robo3t/

## HTTPCLIENT (libreria)

Libreria de JAVA para realizar peticiones REST

https://www.arquitecturajava.com/java-httpclient-invocando-un-servicio-rest/

