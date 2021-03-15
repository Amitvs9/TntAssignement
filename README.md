# TntAssignement

How to run
If you have docker installed and, you do not need to be super user to run docker command simply run the following command

./run.sh

If you do not have docker installed, run the following commands in the provided order:

./mvn clean install
java -jar target/target/tntAggregator-0.0.1-SNAPSHOT.jar


How to stop
If you run the application with docker then simply run following command:

./stop.sh

Aggregator endpoint.
/http://localhost:port/aggregation?pricing=&track=&shipments=

# Tech Stack used
Java 8
spring-boot
maven

# improvement that can be done but due to time not done.
  There are some reducndent code that can be optimize .
  Junit test cases .

