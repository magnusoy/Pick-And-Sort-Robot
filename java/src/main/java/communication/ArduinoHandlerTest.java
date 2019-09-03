package main.java.communication;

import org.json.simple.JSONObject;

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

            switch (menuSelection){
                case 1:
                    JSONObject obj = new JSONObject();

                    obj.put("state",new Integer(1));
                    obj.put("PetterSucks",new Boolean(false));
                    arduinoHandler.sendData(obj);
                    break;
                case 2:
                    JSONObject obj1 = new JSONObject();

                    obj1.put("name","Petter");
                    obj1.put("num",new Integer(1337));
                    obj1.put("balance",new Double(100));
                    obj1.put("is_vip",new Boolean(false));
                    arduinoHandler.sendData(obj1);
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