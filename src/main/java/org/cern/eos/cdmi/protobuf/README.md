Protobuf communication
----------------------

Many commands of the EOS server have been moved to a Protobuf implementation.
This means that the encoding and decoding of data is facilitated by the Protobuf library.

In order for our plugin to communicate with the server, we need to provide a valid Protobuf message.
To do this, we must use the exact same Protobuf object definition as in the EOS project.
Therefor, the following files `proto/QoS.proto` and `proto/ConsoleRequest.proto` must have
the same structure.

If the same structure is followed, although cross-language, easy communication is allowed between the plugin
and the EOS server. 

The main disadvantage is the strong coupling of the Protobuf definitions that ensues. If the definitions
were to change within the EOS project, the same changes must be applied here as well.


Request structure
-----------------

In order to successfully construct an EOS command, the following structure must be followed:

1. A Protobuf object corresponding to the desired command is created and filled with desired data
   (in our case, this is the QoS command).
2. A Protobuf `Request` object is created. One of its fields is a command object. We set the command object
   we constructed here.
3. The `Request` object is serialized into bytes and these bytes are Base64 encoded.

```
| client |  ---Base64Encode(request)---> | EOS server |
```

Compiling the Protobuf objects
------------------------------

To be able to use the data abstractions provided by the Protobuf library,
the proto object definitions must be compiled. 

To do this, execute the following command: 
 
```
protoc3 --proto_path=$PRJ_DIR/src/main/java/org/cern/eos/cdmi/protobuf/proto/ --java_out=$PRJ_DIR/src/main/java/ $PRJ_DIR/src/main/java/org/cern/eos/cdmi/protobuf/proto/*.proto
```

The compiled Protobuf classes will be generated according to the specified `java_package` setting.  
In this case, that means under `$PRJ_DIR/src/main/java/org/cern/eos/cdmi/protobuf/generated/`.

_Note: the compiled Protobuf classes are not source-versioned_
