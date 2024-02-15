cd ./books-parent
mvn clean -DskipTests install
cd ../books-web
mvn jetty:run
cd ..