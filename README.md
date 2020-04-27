# Remove Unused PdxTypes Function
## Description

This project provides a function that removes unused PdxTypes from the Geode servers.

The **RemoveUnusedPdxTypesFunction**:

- Gets a set of existing PdxTypes
- Iterates each input region's PdxInstance values
- For each PdxInstance value, recursively removes all in use PdxTypes from the set of existing PdxTypes
- Deletes any remaining PdxTypes from the PdxTypes region

In addition, it provides a client from which to load and delete JSON data and to run the function.
## Caveats and Comments
Here are a few caveats and comments:

- The function has been tested mainly with JSON data not Java objects.
- The function should be run on a backup or offline system that has been backed up, not on a live system.
- If Java objects are being iterated, verify PDX read-serialized is true so that only PdxInstances are being checked.
- The function has a simulate parameter so that a dry run can be made.
- The function assumes the region's values are PdxInstances.
- Based on the cases supported by JSONFormatter, this function handles PdxInstances Collections and primitives. It also has been modified to handle Maps. It currently does not handle arrays, but support for those could easily be added.
- The function must be executed on only one server so that unused PdxTypes only in one primary bucket are aren't removed accidentally.
- The in-memory TypeRegistry is invalid after this function runs, so the system needs to be restarted.

Unknown objects are skipped with a warning like:

```
[warn 2020/04/24 16:31:48.959 PDT <ServerConnection on port 54534 Thread 2> tid=0x67] Skipping unknown object with parent=...; parentClass=...; objectFieldName=...; object=...; objectClass=...
```
In this case, either the function needs to be enhanced to handle these object types or it needs to be verified that it is ok to skip them.

If the function needs to handle the object type, the removeInUsePdxTypes method must be modified to handle it.

If the function doesn't need to handle the object type, the shouldSkipRecursing method can be modified to remove the warning message.

## Initialization
Modify the **GEODE** environment variable in the *setenv.sh* script to point to a Geode installation directory.
## Build
Build the Spring Boot Client Application and Geode Server Functions using gradle like:

```
./gradlew clean jar bootJar
```
## Run Example
### Start and Configure Locator and Servers
Start and configure the locator and 3 servers using the *startandconfigure.sh* script like:

```
./startandconfigure.sh
```
### Load and Destroy Entries
Run the client to load N JSON PdxInstances and destroy half of them using the *runclient.sh* script like below.

The parameters are:

- operation (load-destroy)
- number of entries (100)

```
./runclient.sh load-destroy 100
```
### Dump Current PdxTypes
Execute the function to dump the current PdxTypes using the *runclient.sh* script like below.

The parameters are:

- operation (dump-pdxtypes)

```
./runclient.sh dump-pdxtypes
```
### Remove Unused PdxTypes Dry Run
Execute the function to simulate removal of the unused PdxTypes using the *runclient.sh* script like below.

Note: This is where any **Skipping unknown object** warnings should be examined and resolved.

The parameters are:

- operation (remove-unused-pdxtypes)
- Comma-separated list of region names (Customer)
- simulate removal (true)

```
./runclient.sh remove-unused-pdxtypes Customer true
```
### Remove Unused PdxTypes
Execute the function to remove the unused PdxTypes using the *runclient.sh* script like below.

The parameters are:

- operation (remove-unused-pdxtypes)
- Comma-separated list of region names (Customer)
- simulate removal (false)

```
./runclient.sh remove-unused-pdxtypes Customer false
```
### Bounce Locator and Servers
At this point, the in-memory TypeRegistry is invalid. The servers need to be bounced. I think its possible to roll them, but this example stops and restarts them.

Execute the *shutdownall.sh* script to shutdown the servers and locators like:

```
./shutdownall.sh
```
Execute the *startall.sh* script to restart the locator and servers like:

```
./startall.sh
```
### Dump Current PdxTypes
Execute the function to dump the current PdxTypes using the *runclient.sh* script like below.

The parameters are:

- operation (dump-pdxtypes)

```
./runclient.sh dump-pdxtypes
```
### Shutdown Locator and Servers
Execute the *shutdownall.sh* script to shutdown the servers and locators like:

```
./shutdownall.sh
```
### Remove Locator and Server Files
Execute the *cleanupfiles.sh* script to remove the server and locator files like:

```
./cleanupfiles.sh
```
## Example Sample Output
### Start and Configure Locator and Servers
Sample output from the *startandconfigure.sh* script is:

```
./startandconfigure.sh 
1. Executing - start locator --name=locator

....
Locator in <working-directory>/locator on xxx.xxx.x.x[10334] as locator is currently online.
Process ID: 26656
Uptime: 5 seconds
Geode Version: 1.9.2
Java Version: 1.8.0_121
Log File: <working-directory>/locator/locator.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

Successfully connected to: JMX Manager [host=xxx.xxx.x.x, port=1099]

Cluster configuration service is up and running.

2. Executing - set variable --name=APP_RESULT_VIEWER --value=any

Value for variable APP_RESULT_VIEWER is now: any.

3. Executing - configure pdx --read-serialized=true --disk-store=pdx_store

read-serialized = true
ignore-unread-fields = false
persistent = true
disk-store = pdx_store
Changes to configuration for group 'cluster' are persisted.

4. Executing - start server --name=server-1 --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

...
Server in <working-directory>/server-1 on xxx.xxx.x.x[53890] as server-1 is currently online.
Process ID: 26674
Uptime: 4 seconds
Geode Version: 1.9.2
Java Version: 1.8.0_121
Log File: <working-directory>/server-1/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

5. Executing - start server --name=server-2 --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

...
Server in <working-directory>/server-2 on xxx.xxx.x.x[53912] as server-2 is currently online.
Process ID: 26676
Uptime: 4 seconds
Geode Version: 1.9.2
Java Version: 1.8.0_121
Log File: <working-directory>/server-2/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

6. Executing - start server --name=server-3 --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

...
Server in <working-directory>/server-3 on xxx.xxx.x.x[53938] as server-3 is currently online.
Process ID: 26685
Uptime: 4 seconds
Geode Version: 1.9.2
Java Version: 1.8.0_121
Log File: <working-directory>/server-3/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

7. Executing - list members

  Name   | Id
-------- | --------------------------------------------------------------
locator  | xxx.xxx.x.x(locator:26656:locator)<ec><v0>:41000 [Coordinator]
server-1 | xxx.xxx.x.x(server-1:26674)<v1>:41001
server-2 | xxx.xxx.x.x(server-2:26676)<v2>:41002
server-3 | xxx.xxx.x.x(server-3:26685)<v3>:41003

8. Executing - create disk-store --name=pdx_store --dir=. --max-oplog-size=10

 Member  | Status | Message
-------- | ------ | ----------------------------
server-1 | OK     | Created disk store pdx_store
server-2 | OK     | Created disk store pdx_store
server-3 | OK     | Created disk store pdx_store

9. Executing - create disk-store --name=data_store --dir=. --max-oplog-size=10

 Member  | Status | Message
-------- | ------ | -----------------------------
server-1 | OK     | Created disk store data_store
server-2 | OK     | Created disk store data_store
server-3 | OK     | Created disk store data_store

10. Executing - deploy --jar=server/build/libs/server-0.0.1-SNAPSHOT.jar

 Member  |       Deployed JAR        | Deployed JAR Location
-------- | ------------------------- | --------------------------------------------------------------------------------------------------
server-1 | server-0.0.1-SNAPSHOT.jar | <working-directory>/server-1/server-0.0.1-SNAPSHOT.v1.jar
server-2 | server-0.0.1-SNAPSHOT.jar | <working-directory>/server-2/server-0.0.1-SNAPSHOT.v1.jar
server-3 | server-0.0.1-SNAPSHOT.jar | <working-directory>/server-3/server-0.0.1-SNAPSHOT.v1.jar

11. Executing - list functions

 Member  | Function
-------- | ----------------------------
server-1 | DumpPdxTypesFunction
server-1 | RemoveUnusedPdxTypesFunction
server-2 | DumpPdxTypesFunction
server-2 | RemoveUnusedPdxTypesFunction
server-3 | DumpPdxTypesFunction
server-3 | RemoveUnusedPdxTypesFunction

12. Executing - sleep --time=3

13. Executing - create region --name=Customer --type=PARTITION_REDUNDANT_PERSISTENT --disk-store=data_store

 Member  | Status | Message
-------- | ------ | ----------------------------------------
server-1 | OK     | Region "/Customer" created on "server-1"
server-2 | OK     | Region "/Customer" created on "server-2"
server-3 | OK     | Region "/Customer" created on "server-3"

14. Executing - list regions

List of regions
---------------
Customer
```
### Load and Destroy Entries
Sample output from the *runclient.sh* script is:

```
./runclient.sh load-destroy 100

> Task :client:bootRun

2020-04-24 15:34:07.584  INFO 27196 --- [           main] example.client.Client                    : Starting Client on localhost with PID 27196
2020-04-24 15:34:09.392  INFO 27196 --- [           main] example.client.Client                    : Started Client in 2.055 seconds (JVM running for 2.329)
2020-04-24 15:34:09.489  INFO 27196 --- [           main] o.a.geode.pdx.internal.TypeRegistry      : Caching PdxType[dsid=0, typenum=8173280
        name=__GEMFIRE_JSON
        fields=[
        streetAddress:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1
        city:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1
        state:Object:2:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2
        postalCode:Object:3:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3]]
2020-04-24 15:34:09.500  INFO 27196 --- [           main] o.a.geode.pdx.internal.TypeRegistry      : Caching PdxType[dsid=0, typenum=14064690
        name=__GEMFIRE_JSON
        fields=[
        type:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1
        number:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1]]
2020-04-24 15:34:09.505  INFO 27196 --- [           main] o.a.geode.pdx.internal.TypeRegistry      : Caching PdxType[dsid=0, typenum=5603787
        name=__GEMFIRE_JSON
        fields=[
        firstName0:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1
        lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1
        age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2
        timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2
        donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2
        accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2
        address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3
        phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4]]
2020-04-24 15:34:09.571  INFO 27196 --- [           main] example.client.service.CustomerService   : Loaded PDX[5603787,__GEMFIRE_JSON]{accounts=[acc1, acc2, acc3], address=PDX[8173280,__GEMFIRE_JSON]{city=New York, postalCode=10021, state=NY, streetAddress=123 Main Street}, age=25, donor=true, firstName0=John, lastName=Johnson, phoneNumber=[PDX[14064690,__GEMFIRE_JSON]{number=212-555-1234, type=home}, PDX[14064690,__GEMFIRE_JSON]{number=646-555-1234, type=fax}], timestamp=1586811161843}
2020-04-24 15:34:09.583  INFO 27196 --- [           main] example.client.service.CustomerService   : Destroyed PDX[5603787,__GEMFIRE_JSON]{accounts=[acc1, acc2, acc3], address=PDX[8173280,__GEMFIRE_JSON]{city=New York, postalCode=10021, state=NY, streetAddress=123 Main Street}, age=25, donor=true, firstName0=John, lastName=Johnson, phoneNumber=[PDX[14064690,__GEMFIRE_JSON]{number=212-555-1234, type=home}, PDX[14064690,__GEMFIRE_JSON]{number=646-555-1234, type=fax}], timestamp=1586811161843}
2020-04-24 15:34:09.588  INFO 27196 --- [           main] o.a.geode.pdx.internal.TypeRegistry      : Caching PdxType[dsid=0, typenum=10088652
        name=__GEMFIRE_JSON
        fields=[
        firstName1:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1
        lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1
        age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2
        timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2
        donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2
        accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2
        address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3
        phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4]]
2020-04-24 15:34:09.636  INFO 27196 --- [           main] example.client.service.CustomerService   : Loaded PDX[10088652,__GEMFIRE_JSON]{accounts=[acc1, acc2, acc3], address=PDX[8173280,__GEMFIRE_JSON]{city=New York, postalCode=10021, state=NY, streetAddress=123 Main Street}, age=25, donor=true, firstName1=John, lastName=Johnson, phoneNumber=[PDX[14064690,__GEMFIRE_JSON]{number=212-555-1234, type=home}, PDX[14064690,__GEMFIRE_JSON]{number=646-555-1234, type=fax}], timestamp=1586811161843}
2020-04-24 15:34:09.641  INFO 27196 --- [           main] o.a.geode.pdx.internal.TypeRegistry      : Caching PdxType[dsid=0, typenum=14573517
        name=__GEMFIRE_JSON
        fields=[
        firstName2:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1
        lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1
        age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2
        timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2
        donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2
        accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2
        address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3
        phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4]]
2020-04-24 15:34:09.670  INFO 27196 --- [           main] example.client.service.CustomerService   : Loaded PDX[14573517,__GEMFIRE_JSON]{accounts=[acc1, acc2, acc3], address=PDX[8173280,__GEMFIRE_JSON]{city=New York, postalCode=10021, state=NY, streetAddress=123 Main Street}, age=25, donor=true, firstName2=John, lastName=Johnson, phoneNumber=[PDX[14064690,__GEMFIRE_JSON]{number=212-555-1234, type=home}, PDX[14064690,__GEMFIRE_JSON]{number=646-555-1234, type=fax}], timestamp=1586811161843}
2020-04-24 15:34:09.678  INFO 27196 --- [           main] example.client.service.CustomerService   : Destroyed PDX[14573517,__GEMFIRE_JSON]{accounts=[acc1, acc2, acc3], address=PDX[8173280,__GEMFIRE_JSON]{city=New York, postalCode=10021, state=NY, streetAddress=123 Main Street}, age=25, donor=true, firstName2=John, lastName=Johnson, phoneNumber=[PDX[14064690,__GEMFIRE_JSON]{number=212-555-1234, type=home}, PDX[14064690,__GEMFIRE_JSON]{number=646-555-1234, type=fax}], timestamp=1586811161843}
2020-04-24 15:34:09.725  INFO 27196 --- [           main] o.a.geode.pdx.internal.TypeRegistry      : Caching PdxType[dsid=0, typenum=13072947
        name=__GEMFIRE_JSON
        fields=[
        firstName3:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1
        lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1
        age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2
        timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2
        donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2
        accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2
        address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3
        phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4]]
2020-04-24 15:34:09.751  INFO 27196 --- [           main] example.client.service.CustomerService   : Loaded PDX[13072947,__GEMFIRE_JSON]{accounts=[acc1, acc2, acc3], address=PDX[8173280,__GEMFIRE_JSON]{city=New York, postalCode=10021, state=NY, streetAddress=123 Main Street}, age=25, donor=true, firstName3=John, lastName=Johnson, phoneNumber=[PDX[14064690,__GEMFIRE_JSON]{number=212-555-1234, type=home}, PDX[14064690,__GEMFIRE_JSON]{number=646-555-1234, type=fax}], timestamp=1586811161843}
2020-04-24 15:34:09.785  INFO 27196 ...
2020-04-24 15:34:11.534  INFO 27196 --- [           main] example.client.service.CustomerService   : Loaded 100 PdxInstances in 2141 ms
```
### Dump Current PdxTypes
Sample output from the *runclient.sh* script is:

```
./runclient.sh dump-pdxtypes

> Task :client:bootRun
2020-04-24 15:34:57.621  INFO 27257 --- [           main] example.client.Client                    : Starting Client on localhost with PID 27257
2020-04-24 15:34:59.241  INFO 27257 --- [           main] example.client.Client                    : Started Client in 1.89 seconds (JVM running for 2.166)
2020-04-24 15:34:59.272  INFO 27257 --- [           main] example.client.service.CustomerService   : Dumped PdxTypes result=PdxTypeStatus{numExisting=102, numInUse=-1, numUnused=-1}
```
One of the server's log files will log a message like:

```
[info 2020/04/24 15:34:59.262 PDT <ServerConnection on port 53890 Thread 2> tid=0x71] The cache contains 102 PdxTypes:
	key=15834864; id=15834864; num=15834864; pdxType=PdxType[dsid=0,typenum=15834864,name=__GEMFIRE_JSON,fields=[firstName86:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=13574861; id=13574861; num=13574861; pdxType=PdxType[dsid=0,typenum=13574861,name=__GEMFIRE_JSON,fields=[firstName51:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=16227571; id=16227571; num=16227571; pdxType=PdxType[dsid=0,typenum=16227571,name=__GEMFIRE_JSON,fields=[firstName68:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=8105330; id=8105330; num=8105330; pdxType=PdxType[dsid=0,typenum=8105330,name=__GEMFIRE_JSON,fields=[firstName23:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=12389046; id=12389046; num=12389046; pdxType=PdxType[dsid=0,typenum=12389046,name=__GEMFIRE_JSON,fields=[firstName99:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	...
```
### Remove Unused PdxTypes Dry Run
Sample output from the *runclient.sh* script is:

```
./runclient.sh remove-unused-pdxtypes Customer true

> Task :client:bootRun
2020-04-24 16:31:47.159  INFO 31051 --- [           main] example.client.Client                    : Starting Client on localhost with PID 31051
2020-04-24 16:31:48.668  INFO 31051 --- [           main] example.client.Client                    : Started Client in 1.729 seconds (JVM running for 1.984)
2020-04-24 16:31:48.969  INFO 31051 --- [           main] example.client.service.CustomerService   : Removed unused PdxTypes in 300 ms with result=PdxTypeStatus{numExisting=102, numInUse=52, numUnused=50}
```
One of the server's log files will log messages like:

```
[info 2020/04/24 16:31:48.683 PDT <ServerConnection on port 54534 Thread 2> tid=0x67] Executing function=RemoveUnusedPdxTypesFunction; regionNames=[Customer]; simulate=true

[info 2020/04/24 16:31:48.688 PDT <ServerConnection on port 54534 Thread 2> tid=0x67] The cache contains 102 existing PdxTypes:
	key=13578702; id=13578702; num=13578702; pdxType=PdxType[dsid=0,typenum=13578702,name=__GEMFIRE_JSON,fields=[firstName31:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=7310702; id=7310702; num=7310702; pdxType=PdxType[dsid=0,typenum=7310702,name=__GEMFIRE_JSON,fields=[firstName80:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=11986612; id=11986612; num=11986612; pdxType=PdxType[dsid=0,typenum=11986612,name=__GEMFIRE_JSON,fields=[firstName20:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=13437128; id=13437128; num=13437128; pdxType=PdxType[dsid=0,typenum=13437128,name=__GEMFIRE_JSON,fields=[firstName45:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=3331637; id=3331637; num=3331637; pdxType=PdxType[dsid=0,typenum=3331637,name=__GEMFIRE_JSON,fields=[firstName65:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
  ...

[info 2020/04/24 16:31:48.959 PDT <ServerConnection on port 54534 Thread 2> tid=0x67] The cache contains 50 unused PdxTypes:
	key=7310702; id=7310702; num=7310702; pdxType=PdxType[dsid=0,typenum=7310702,name=__GEMFIRE_JSON,fields=[firstName80:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=11986612; id=11986612; num=11986612; pdxType=PdxType[dsid=0,typenum=11986612,name=__GEMFIRE_JSON,fields=[firstName20:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=638977; id=638977; num=638977; pdxType=PdxType[dsid=0,typenum=638977,name=__GEMFIRE_JSON,fields=[firstName60:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=14927594; id=14927594; num=14927594; pdxType=PdxType[dsid=0,typenum=14927594,name=__GEMFIRE_JSON,fields=[firstName58:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=1841171; id=1841171; num=1841171; pdxType=PdxType[dsid=0,typenum=1841171,name=__GEMFIRE_JSON,fields=[firstName52:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
  ...
```
### Remove Unused PdxTypes
Sample output from the *runclient.sh* script is:

```
./runclient.sh remove-unused-pdxtypes Customer false

> Task :client:bootRun

2020-04-24 16:07:05.953  INFO 29294 --- [           main] example.client.Client                    : Starting Client on localhost with PID 29294
2020-04-24 16:07:07.540  INFO 29294 --- [           main] example.client.Client                    : Started Client in 1.829 seconds (JVM running for 2.1)
2020-04-24 16:07:07.871  INFO 29294 --- [           main] example.client.service.CustomerService   : Removed unused PdxTypes in 330 ms with result=PdxTypeStatus{numExisting=102, numInUse=52, numUnused=50}
```
One of the server's log files will log messages like:

```
[info 2020/04/24 16:07:07.552 PDT <ServerConnection on port 53890 Thread 3> tid=0x74] Executing function=RemoveUnusedPdxTypesFunction; regionNames=[Customer]; simulate=false

[info 2020/04/24 16:07:07.555 PDT <ServerConnection on port 53890 Thread 3> tid=0x74] The cache contains 102 existing PdxTypes:
	key=15834864; id=15834864; num=15834864; pdxType=PdxType[dsid=0,typenum=15834864,name=__GEMFIRE_JSON,fields=[firstName86:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=13574861; id=13574861; num=13574861; pdxType=PdxType[dsid=0,typenum=13574861,name=__GEMFIRE_JSON,fields=[firstName51:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=16227571; id=16227571; num=16227571; pdxType=PdxType[dsid=0,typenum=16227571,name=__GEMFIRE_JSON,fields=[firstName68:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=8105330; id=8105330; num=8105330; pdxType=PdxType[dsid=0,typenum=8105330,name=__GEMFIRE_JSON,fields=[firstName23:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=12389046; id=12389046; num=12389046; pdxType=PdxType[dsid=0,typenum=12389046,name=__GEMFIRE_JSON,fields=[firstName99:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
  ...

[info 2020/04/24 16:07:07.827 PDT <ServerConnection on port 53890 Thread 3> tid=0x74] The cache contains 50 unused PdxTypes:
	key=15834864; id=15834864; num=15834864; pdxType=PdxType[dsid=0,typenum=15834864,name=__GEMFIRE_JSON,fields=[firstName86:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=16227571; id=16227571; num=16227571; pdxType=PdxType[dsid=0,typenum=16227571,name=__GEMFIRE_JSON,fields=[firstName68:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=8433552; id=8433552; num=8433552; pdxType=PdxType[dsid=0,typenum=8433552,name=__GEMFIRE_JSON,fields=[firstName32:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=14573517; id=14573517; num=14573517; pdxType=PdxType[dsid=0,typenum=14573517,name=__GEMFIRE_JSON,fields=[firstName2:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=3101198; id=3101198; num=3101198; pdxType=PdxType[dsid=0,typenum=3101198,name=__GEMFIRE_JSON,fields=[firstName74:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
  ...

[info 2020/04/24 16:07:07.864 PDT <ServerConnection on port 53890 Thread 3> tid=0x74] The cache contains 52 in use PdxTypes:
	key=8095226; id=8095226; num=8095226; pdxType=PdxType[dsid=0,typenum=8095226,name=__GEMFIRE_JSON,fields=[firstName37:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=13574861; id=13574861; num=13574861; pdxType=PdxType[dsid=0,typenum=13574861,name=__GEMFIRE_JSON,fields=[firstName51:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=15912816; id=15912816; num=15912816; pdxType=PdxType[dsid=0,typenum=15912816,name=__GEMFIRE_JSON,fields=[firstName21:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=8105330; id=8105330; num=8105330; pdxType=PdxType[dsid=0,typenum=8105330,name=__GEMFIRE_JSON,fields=[firstName23:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=12389046; id=12389046; num=12389046; pdxType=PdxType[dsid=0,typenum=12389046,name=__GEMFIRE_JSON,fields=[firstName99:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
  ...
```
### Bounce Locator and Servers
Sample output from the *shutdownall.sh* script is:

```
./shutdownall.sh 

(1) Executing - connect

Connecting to Locator at [host=localhost, port=10334] ..
Connecting to Manager at [host=xxx.xxx.x.x, port=1099] ..
Successfully connected to: [host=xxx.xxx.x.x, port=1099]


(2) Executing - shutdown --include-locators=true

Shutdown is triggered
```
Sample output from the *startall.sh* script is:

```
./startall.sh 
Starting locator
....
Locator in <working-directory>/locator on xxx.xxx.x.x[10334] as locator is currently online.
Process ID: 29621
Uptime: 17 seconds
Geode Version: 1.9.2
Java Version: 1.8.0_121
Log File: <working-directory>/locator/locator.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

Successfully connected to: JMX Manager [host=xxx.xxx.x.x, port=1099]

Cluster configuration service is up and running.

Starting server 1
Starting server 2
Starting server 3
............

Server in <working-directory>/server-2 on xxx.xxx.x.x[54336] as server-2 is currently online.
Process ID: 29679
Uptime: 5 seconds
Geode Version: 1.9.2
Java Version: 1.8.0_121
Log File: <working-directory>/server-2/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

Server in <working-directory>/server-1 on xxx.xxx.x.x[54344] as server-1 is currently online.
Process ID: 29680
Uptime: 5 seconds
Geode Version: 1.9.2
Java Version: 1.8.0_121
Log File: <working-directory>/server-1/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

Server in <working-directory>/server-3 on xxx.xxx.x.x[54343] as server-3 is currently online.
Process ID: 29681
Uptime: 5 seconds
Geode Version: 1.9.2
Java Version: 1.8.0_121
Log File: <working-directory>/server-3/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>
```
### Dump Current PdxTypes
Sample output from the *runclient.sh* script is:

```
./runclient.sh dump-pdxtypes

> Task :client:bootRun

2020-04-24 16:25:56.452  INFO 30635 --- [           main] example.client.Client                    : Starting Client on localhost with PID 30635
2020-04-24 16:25:58.111  INFO 30635 --- [           main] example.client.Client                    : Started Client in 1.878 seconds (JVM running for 2.139)
2020-04-24 16:25:58.145  INFO 30635 --- [           main] example.client.service.CustomerService   : Dumped PdxTypes result=PdxTypeStatus{numExisting=52, numInUse=-1, numUnused=-1}
```
One of the server's log files will log a message like:

```
[info 2020/04/24 16:25:58.136 PDT <ServerConnection on port 54343 Thread 1> tid=0xed] The cache contains 52 PdxTypes:
	key=8095226; id=8095226; num=8095226; pdxType=PdxType[dsid=0,typenum=8095226,name=__GEMFIRE_JSON,fields=[firstName37:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=13574861; id=13574861; num=13574861; pdxType=PdxType[dsid=0,typenum=13574861,name=__GEMFIRE_JSON,fields=[firstName51:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=15912816; id=15912816; num=15912816; pdxType=PdxType[dsid=0,typenum=15912816,name=__GEMFIRE_JSON,fields=[firstName21:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=8105330; id=8105330; num=8105330; pdxType=PdxType[dsid=0,typenum=8105330,name=__GEMFIRE_JSON,fields=[firstName23:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	key=12389046; id=12389046; num=12389046; pdxType=PdxType[dsid=0,typenum=12389046,name=__GEMFIRE_JSON,fields=[firstName99:Object:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=-1, lastName:Object:1:1:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=1, age:byte:2:1:idx0(relativeOffset)=-10:idx1(vlfOffsetIndex)=2, timestamp:long:3:1:idx0(relativeOffset)=-9:idx1(vlfOffsetIndex)=2, donor:boolean:4:1:idx0(relativeOffset)=-1:idx1(vlfOffsetIndex)=2, accounts:Object:5:2:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=2, address:Object:6:3:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=3, phoneNumber:Object:7:4:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=4, ]]
	...
```
### Shutdown Locator and Servers
Sample output from the *shutdownall.sh* script is:

```
./shutdownall.sh 

(1) Executing - connect

Connecting to Locator at [host=localhost, port=10334] ..
Connecting to Manager at [host=192.168.1.8, port=1099] ..
Successfully connected to: [host=192.168.1.8, port=1099]


(2) Executing - shutdown --include-locators=true

Shutdown is triggered
```
