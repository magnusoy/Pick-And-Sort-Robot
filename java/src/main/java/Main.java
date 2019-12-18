package main.java;

import main.java.communication.SerialHandler;
import main.java.communication.Server;
import main.java.utility.Database;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Test experiment too see if code works
 * as expected.
 */
public class Main {

    public static void main(String[] args) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Database database = new Database();
        Server server = new Server(5056, database);
        SerialHandler serialHandler = new SerialHandler(database);

        serialHandler.run();
        executor.scheduleAtFixedRate(() -> {
            serialHandler.sendData(database.getJsonToTeensy());
        }, 2000, 10, TimeUnit.MILLISECONDS);

        server.start();
    }
}
