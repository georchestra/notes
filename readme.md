# Note mapfishapp addon

This addon add a feature in mapfishapp. This allow user to report geolocalized 'note' in mapfishapp.
 
## Build

### Create JAR file
 
 In order to build this addon, you just need to launch :
 
 ```bash
mvn clean jar:jar install 
 ```
 This command will create a jar file and copy it in your local maven repo (`~/.m2`).  

### Add jar file to mapfishapp

 In order to link this addon to mapfishapp, you will need to add following dependency in `pom.xml` file of mapfishapp 
 module :
 
 ```xml
<dependency>
  <groupId>org.georchestra</groupId>
  <artifactId>notes</artifactId>
  <version>15.12</version>
</dependency>
 ```
 Then, just follow build instruction of mapfishapp georchestra submodule. 

## Configuration

 Configuration of this addon is done in mapfishapp properties files in datadir : `mapfishapp/mapfishapp.properties`. You 
 need, at least, to define one backend to be enable to store notes in database. You can define as many backend as you 
 want. Each backend must have a unique identifier under `id` key. For each backend, you need to define following keys :
  
 * id : alphanumeric identifier of backend, Must be unique across backends.
 * table : target table in configured database, may contains database schema
 * srid : numeric identifier of projection used. For example : 4326
 * jdbcUrl : jdbc URL to connect to database. For example : `jdbc:postgresql://localhost:5432/georchestra?user=www-data&password=www-data`
 
 Each previous keys must be prefixed with `note.x.` where x is sequential integer starting from 0. For example, if you 
 want to define first backend identifier as `foo`, you need to define following property in `mapfishapp.properties` file in 
 datadir : 
  
 ```properties
note.0.id=foo
 ```
  
  Example of definition
 of 3 backends :
 
 ```properties
note.0.id=backend1
note.0.table=mapfishapp.note
note.0.srid=4326
note.0.jdbcUrl=jdbc:postgresql://localhost:5432/georchestra?user=www-data&password=www-data

note.1.id=backend2
note.1.table=main_note
note.1.srid=3857
note.1.jdbcUrl=jdbc:postgresql://localhost:5432/georchestra?user=www-data&password=www-data

note.2.id=internal_backend
note.2.table=main_note
note.2.srid=3857
note.2.jdbcUrl=jdbc:postgresql://database.internal:5432/intranet?user=intranet&password=e3Mdf6esd_ 
 ```
 