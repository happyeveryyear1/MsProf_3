# msProf

### Overview
The microservice application performance problem analysis tool supports to locate performance bottlenecks via proposed intra/inter-trace analysis technique, and it can also display the analysis results visually.

### The environment required for installation

#### 	Basic environment

1.Java-1.8.0

2.Python-3.8: Test execution environment

3.Nginx: front-end operating environment

4.Docker: container service

#### 	Pinpoint environment

1.Hadoop-2.8.5, Hbase-1.4.13: Pinpoint storage service

2.Pinpoint-collector: Pinpoint collector      

3.Pinpoint-web: Pinpoint interface       

4.Pinpoint-agent: Pinpoint probe

#### 	Performance analysis related

msProf: Performance Analysis Service Project

#### 	Front-end related

1.Webflash-vue: front-end interface

2.Webflash: the background of the front end

3.Mysql-8.0: database

#### The application under test

Trainticket: Application under test

### Installation

#### Basic environment installation

1.Python

`pip install locust==2.10.1`

2.Nginx

`sudo apt install nginx`

#### Pinpoint environment installation

1.Hadoop

​	1)Download Hadoop-2.8.5 installation package.

​	2)Modify `core-site.xml` in hadoop.

```
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:9000</value>
    </property>
    <property>
        <name>hadoop.tmp.dir</name>
        <value>/root/softwares/hadoop/hadoop-2.7.1/hadoop_tmp</value>
    </property>
</configuration>

```

​	3)Modify `yarn-site.xml` in hadoop

```
<configuration>
<!-- Site specific YARN configuration properties -->
        <property>
                <name>yarn.nodemanager.aux-services</name>
                <value>mapreduce_shuffle</value>
        </property>
        <property>
                <name>yarn.resourcemanager.hostname</name>
                <value>localhost</value>
        </property>
        <property>
             <name>yarn.nodemanager.resource.memory-mb</name>
             <value>1024</value>
      </property>
      <property>
             <name>yarn.nodemanager.resource.cpu-vcores</name>
             <value>1</value>
      </property>   
</configuration>
```

​	4)Modify `mapred-site.xml` in hadoop

```
<configuration>
        <property>
            <name>mapreduce.framework.name</name>
            <value>yarn</value>
        </property>
</configuration>
```

​	5)Execute the command to initialize the file system

```
hdfs namenode -format
```

​	6)Add environment variables

```
export HADOOP_HOME="/root/softwares/hadoop/hadoop-2.7.1"
```

2.Hbase

​	1)Download Hbase-1.4.13 installation package

​	2)Modify hbase-site.xml in hbase

```
<configuration>
        <property>
                  <name>hbase.rootdir</name>
                  <value>hdfs://localhost:9000/hbase</value>
          </property>
          <property>
                  <name>hbase.master.info.port</name>
                  <value>60010</value>
          </property>
          <property>
                  <name>hbase.zookeeper.property.dataDir</name>
                  <value>/root/softwares/hbase/hbase-1.2.7/zookeeper_data</value>
          </property>
</configuration>
```

​	3)start Hbase

```
hbase/bin/start-hbase.sh
```

​	4)Hbase initialization script

```
hbase shell hbase-create.hbase
```

3.Pinpoint-collector

​	1)Upload `/resource/pinpoint-collector-boot-2.2.0.jar` to the server.

​	2)Start the application using the deployment script `start.sh`.

```
nohup java 
-Dpinpoint.zookeeper.address=localhost -DMsD.span.url=http://127.0.0.1:8347/collect/span 
-DMsD.spanChunk.url=http://127.0.0.1:8347/collect/spanChunk 
-jar pinpoint-collector-boot-2.2.2.jar >nohup.out 2>&1 &
```

4.Pinpoint-web

​	1)Upload `/resource/pinpoint-web-boot-2.2.0.jar` to the server.

​	2)Start the application using the deployment script `start.sh`.

```
nohup java -jar -Dpinpoint.zookeeper.address=localhost pinpoint-web-boot-2.2.2.jar >nohup.out 2>&1 &
```

5.Pinpoint-agent

​	1)Upload `/resource/pinpoint-agent-2.2.0.tar.gz` to the server and decompress it.

​	2)Start the application using the deployment script `start.sh`.

```
java -javaagent:pinpoint-bootstrap-2.2.2.jar 
-Dpinpoint.agentId={The application ID you set} 
-Dpinpoint.applicationName={The application name you set} 
-Dprofiler.collector.ip=collectorIP 
-Dprofiler.transport.grpc.collector.ip= collectorIP 
-Dprofiler.sampling.rate=1 
-jar {Application under test}
```

#### Performance analysis service installation

1.msProf

​	1)Upload `/resource/msProf-1.0.jar` to the server.

​	2)Configure`/msProf/src/main/resources/application.yaml`, set test suite address, locust script address, har file address.

​	3)Configure `/resources/locustExec.py`,Change host to the address of your application under test.

​	4)Start the application using the deployment script `start.sh`.

```
nohup java -jar msProf-1.0.jar 2>&1 &
```

#### Front-end related

1.webflash-vue

​	1)Configure `/etc/nginx/nginx.conf`.

​	2)Start the Nginx service

2.webflash

​	1)Upload `/resource/flash-api.jar` to the server.

​	2)Configure `/resources/application.properties`.

```
SVCDIAGNOSER_URL=http://127.0.0.1:8347
```

​	3)Configure `/resources/application-dev.properties`.

```
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/webflash?useSSL=false&useUnicode=true&characterEncoding=UTF8&
```

​	4)Start the application using the deployment script `start.sh`.

```
nohup java -jar flash-api.jar 2>&1 &
```

3.MySQL8.0

​	1)Upload `/resource/webflash-mysql-8.0.tar` to the server and decompress it.

​	2)Start and mount the directory with docker

```
docker run --name webflash-mysql-8.0 -e MYSQL_ROOT_PASSWORD=root -v /mysql/:/var/lib/mysql  -p 33080:3306 -d webflash-mysql-8.0:pack
```

#### Application under test

1.Trainticket

​	reference link:

​	[https://github.com/FudanSELab/train-ticket/tree/master/deployment/kubernetes-manifests/k8s-with-istio](https://github.com/FudanSELab/train-ticket/tree/master/deployment/kubernetes-manifests/k8s-with-istio)

​	1)Copy `/resources/pinpoint-agent-2.2.2.tar.gz` to each service under the `/train-ticket-master/` directory.

​	2)Modify the start.sh in each service of Trainticket.

```
nohup java -Xmx200m 
-javaagent:/pinpoint/pinpoint-agent-2.2.2/pinpoint-bootstrap-2.2.2.jar 
-Dpinpoint.agentId={The application ID you set}
-Dpinpoint.applicationName={The application name you set} 
-Dprofiler.collector.ip=127.0.0.1 
-Dprofiler.transport.grpc.collector.ip=127.0.0.1 
-Dprofiler.sampling.rate=1 
-jar /app/ts-train-service-1.0.jar > /app/log/nohup.out 2>&1
```

​	3)compile project

```
mvn clean package -Dmaven.test.skip=true
```

​	4)build project

```
docker-compose build
```

​	5)start service

```
kubectl apply -f /root/train-ticket-2/k8s/ts-deployment-part1.yml

kubectl apply -f /root/train-ticket-2/k8s/ts-deployment-part2.yml

kubectl apply -f /root/train-ticket-2/k8s/ts-deployment-part3.yml
```

### **The functionality of msProf**

- #### **Aggregate the execution results of multiple user requests**


![](/resources/static/Interface execution information.png)

- #### **Root cause Analysis**
msProf supports structure analysis, execution time analysis and traffic anomaly analysis.
- ##### **Structure analysis**

- ##### execution time analysis

- ##### traffic anomaly analysis

