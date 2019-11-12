package main.java.utility;


import java.io.IOException;

public class RequestRemoteDataTest {
    public static void main(String[] args) throws IOException {
        RequestRemoteData requestRemoteData = new RequestRemoteData();
        while (true) {
            long startTime = System.nanoTime();
            requestRemoteData.update();
            String lst = requestRemoteData.getAll().toList().toString();
            long endTime = System.nanoTime();
            System.out.println(endTime-startTime);
        }
    }
}
