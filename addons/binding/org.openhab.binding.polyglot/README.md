# Polyglot Binding

This binding manages the lifecycle of docker containers.  Polyglot can start and stop containers when Openhab starts and stops or can run containers on-demand.  Additionally, the binding can provide environment variables to the docker container to support the development of bindings that communicate to Openhab using communications mechanism such as [the homie convention](https://homieiot.github.io/) over [MQTT](https://www.openhab.org/addons/bindings/mqtt.generic/#supported-things).  Utilizing the homie convention and the MQTT protocol, automation bindings can be written in any language and integrated into Openhab. 


## Bridge Configuration
 
Optional configuration parameters are:

* __mqttHostname__: Hostname of IP address of MQTT server to supply to containers in the MQTT_HOSTNAME environment variable.
* __mqttPort__: Network port of the MQTT server to supply to containers in the MQTT_PORT environment variable.  Default is 1883.
* __mqttServer__: URL for MQTT Server to supply to containers in format tcp://host:port and supplied to the container in the MQTT_SERVER environment variable.  
* __mqttUsername__: Username to connect to MQTT Server. Supplied to containers in the MQTT_USERNAME environment variable.
* __mqttPassword__: Password to connect to MQTT Server. Supplied to containers in the MQTT_PASSWORD environment variable.
* __polygotEnvPrefix__: Prefix to use for all Polygot generated environment variables.  Default is no prefix. 

The binding expects to be able to discover your docker configuration information from the environment.  To enable the binding to access docker, the Openhab user needs to be added to the docker group and Openhab needs to be restarted.  See [here](https://docs.docker.com/install/linux/linux-postinstall/#manage-docker-as-a-non-root-user) for more details. 


## Supported Things

| Things           | Description                                                                  | Thing Type |
|------------------|------------------------------------------------------------------------------|------------|
| Container        | Manages the lifecycle of docker containers                                   | container  |



## Discovery

This binding does not support discovery.



## Thing Configuration

| Configuration Parameter | Type    | Description                                                                       | Required | Default |
|-------------------------|---------|-----------------------------------------------------------------------------------|----------|---------|
| image                   | text    | Container image to manage                                                         | yes      |         |
| tag                     | text    | Container tag to append to image name                                             | no       | "latest"|
| mqttClientID            | text    | Client ID to supply to container in the MQTT_CLIENT_ID environment variable       | no       | thingUID|
| logRegex                | text    | [Regular expression for enhanced logs processing](#logging-regex)               | no       |         |
| cmd                     | text    | Docker command to supply to container (formatted as a JSON array)                 | no       | []      |
| env                     | text    | Docker environment variables (formatted as a JSON map)                            | no       | []      |
| runOnStart              | boolean | Start container when binding starts                                               | no       | yes     |
| restart                 | boolean | Restart container automatically (only used if runOnStart is true)                 | no       | yes     |
| skipPull                | boolean | Don't pull image from registry                                                    | no       | no      |


### Logging Regex
Requires named capture groups 
Square brackets [] in the string must be escaped

 - Group "level" maps to log level
 - Group "strip" is removed.


## Channels


| Channel Name            | Type    | Description                                                                       | Read Only |
|-------------------------|---------|-----------------------------------------------------------------------------------|-----------|
| name                    | string  | Container image + tag                                                             | yes       |
| id                      | string  | Container tag to append to image name                                             | yes       |
| created                 | datetime| Time the container was created                                                    | yes       | 
| restart_count           | number  | Number of times container has been automatically restarted                        | yes       |      
| restarting              | switch  | Indicates if the container is currently restarting                                | yes       | 
| running                 | switch  | Indicates and controls if the container is running                                | no        | 
| paused                  | switch  | Pause or resume the container                                                     | no        | 



## Full Example

demo.things:
```
Bridge polyglot:containers:home [ mqttServer="tcp://mosquitto:1883", mqttUsername="openhab", mqttPassword="password" ] {
  Thing container nuvo [ image="boctothefuture/nuvo-homie", tag="1.0.7", logRegex="^(?<strip>\\[(?<level>.*)\\]\\s).*", env="{'nuvo.host':'10.0.0.70','LOG_LEVEL'='DEBUG'}" ]
}
```

demo.items:
```
String Container_Image "Container Image [%s]" { channel = "polyglot:container:nuvo:image" }
String Container_ID "Container ID [%s]" { channel = "polyglot:container:nuvo:id" }
DateTime Container_Created "Created [%s]" { channel = "polyglot:container:nuvo:created" }
Number Container_Restart_Count "Restart Count [%s]" { channel = "polyglot:container:nuvo:restart_count" }
Switch Container_Restarting "Container Restarting [%s]" { channel = "polyglot:container:nuvo:restarting" }
Switch Container_Running "Container Running [%s]" { channel = "polyglot:container:nuvo:running" }
Switch Container_Paused "Container Paused [%s]" { channel = "polyglot:container:nuvo:paused" }
```

demo.sitemap:
```
sitemap demo label="Main Menu"
{
   Frame label="Polyglot" {
      Text item=Container_Image
      Text item=Container_ID
      Text item=Container_Created
      Text item=Container_Restart_Count
      Switch item=Container_Restarting
      Switch item=Container_Running
      Switch item=Container_Paused      
   }
}
```


