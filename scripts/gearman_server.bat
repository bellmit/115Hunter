@ECHO OFF
echo Start Gearman server at port 4731
@title Start Gearman server at port 6666
echo "server information: server port = 4731   web port = 8080   serverAdmin = http://localhost:8080"

java -jar gearman-server-0.6.2.jar --port=4731
pause & exit