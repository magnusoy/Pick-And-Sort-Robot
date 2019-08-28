package main.java.communication;

import java.util.Scanner;

public class ArduinoHandlerTest {

    public static void main(String[] args) {
        ArduinoHandler arduinoHandler = new ArduinoHandler();
        arduinoHandler.run();

        System.out.println("Started");


        boolean quit= false;
        while(!quit){
            System.out.println("enter a number");
            Scanner reader = new Scanner(System.in);
            int menuSelection = reader.nextInt();
            String hei = "halla";
            switch (menuSelection){
                case 1:
                    arduinoHandler.sendData(hei.getBytes());
                    break;
                case 2:
                    arduinoHandler.sendData(hei.getBytes());
                    break;

                case 3:
                    System.out.println("quit");
                    arduinoHandler.close();
                    quit=true;
                    break;
            }
        }
    }
}
