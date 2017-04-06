# javac Simulation.java
cd bin
java -XX:+HeapDumpOnOutOfMemoryError -agentlib:hprof=file=snapshot.hprof -classpath .:/usr/share/java/mysql-connector-java.jar no.hiof.mobapp.foxhunt.server.Simulation
