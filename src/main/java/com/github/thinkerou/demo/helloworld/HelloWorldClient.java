package com.github.thinkerou.demo.helloworld;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

/**
 * HelloWorld is a simple client that requests a greeting from the HelloWorldServerMain.
 *
 * Source from: https://github.com/grpc/grpc-java/tree/master/examples/src/main/java/io/grpc/examples
 *
 * @author thinkerou
 */
public class HelloWorldClient {

    private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    /**
     * Construct client connecting to HelloWorld server at host:port.
     */
    public HelloWorldClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS).
                // For the example we disable TLS to avoid needing certificates.
                .usePlaintext()
                .build());
    }

    /**
     * Construct client for accessing HelloWorld server using the existing channel.
     */
    HelloWorldClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        logger.log(Level.INFO, "shutdown grpc channel");
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Say hello to server.
     */
    public String greet(String payload) {
        HelloRequest.Builder requestBuilder = HelloRequest.newBuilder().setName(payload);
        HelloReply response = null;
        try {
            response = blockingStub.sayHello(requestBuilder.build());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
        String res = null;
        try {
            res = JsonFormat.printer().print(response);
        } catch (InvalidProtocolBufferException e) {
            logger.log(Level.WARNING, "JsonFormat print failed: {}", e);
        }
        return res;
    }

    public String againGreet(String msg, String address) {
        AgainHelloRequest.Builder requestBuilder = AgainHelloRequest.newBuilder().setMessage(msg).setAddress(address);
        AgainHelloReply response = null;
        try {
            response = blockingStub.againSayHello(requestBuilder.build());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
        String res = null;
        try {
            res = JsonFormat.printer().print(response);
        } catch (InvalidProtocolBufferException e) {
            logger.log(Level.WARNING, "JsonFormat print failed: {}", e);
        }
        return res;
    }

    /**
     * Greet server.
     * If provided, the first element of args is the name to use in the greeting.
     */
    public static void main(String[] args) throws Exception {
        HelloWorldClient client = new HelloWorldClient("localhost", 50051);
        try {
            // Access a service running on the local machine on port 50051.
            String user = "world";
            if (args.length > 0) {
                // Use the arg as the name to greet if provided.
                user = args[0];
            }
            client.greet(user);
            client.againGreet(user, "bj");
        } finally {
            client.shutdown();
        }
    }

}
