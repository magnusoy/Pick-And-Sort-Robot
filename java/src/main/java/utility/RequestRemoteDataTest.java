package main.java.utility;


import java.io.IOException;

public class RequestRemoteDataTest {
    public static void main(String[] args) throws IOException {
        RequestRemoteData requestRemoteData = new RequestRemoteData();
        requestRemoteData.update();
        System.out.println(requestRemoteData.getAll().get(0).toString());
    }
}
