package com.github.thinkerou.demo.helloworld;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.google.protobuf.util.JsonFormat;

/**
 * HelloWorldServer that manages startup/shutdown of a Greeter server.
 *
 * Source from: https://github.com/grpc/grpc-java/tree/master/examples/src/main/java/io/grpc/examples
 *
 * @author thinkerou
 */
public class HelloWorldServer {

    private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

    private Server server;

    private void start() throws IOException {
        // The port on which the server should run.
        int port = 50051;

        String file = System.getProperty("user.dir") + "/src/main/resources/route-guide.json";
        String payloads = readFile(file);

        server = ServerBuilder.forPort(port)
                .addService(new HelloWorldServerImpl(parseFeatures(payloads)))
                .build()
                .start();
        logger.info("Server started listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                logger.info("Shutting down gRPC server since JVM is shutting down");
                HelloWorldServer.this.stop();
                logger.info("Server shut down");
            }
        });
    }

    public String readFile(String file) throws IOException {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            logger.warning(e.getMessage());
        }

        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String payloads = "";
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            payloads += line + "\n";
        }

        bufferedReader.close();

        return payloads;
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Parses the JSON input file containing the list of features.
     */
    public static List<Feature> parseFeatures(String content) throws IOException {
        FeatureDatabase.Builder database = FeatureDatabase.newBuilder();
        JsonFormat.parser().merge(content, database);
        return database.getFeatureList();
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final HelloWorldServer server = new HelloWorldServer();
        server.start();
        // For testing
        if (args.length == 0 || args[0] != "test") {
            server.blockUntilShutdown();
        }
    }

}

