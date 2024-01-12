docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.12-management
docker run --name=mysql1 -d mysql/mysql-server:latest
docker run --name=zipkin1 -p 9411:9411 -d openzipkin/zipkin