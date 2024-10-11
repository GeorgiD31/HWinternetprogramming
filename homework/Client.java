package homework;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 1234);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Enter a command (ADD <name> <grade> | LIST | AVG <name>):");
                String request = scanner.nextLine();
                out.writeObject(request);

                String response = (String) in.readObject();
                System.out.println("Response from server: " + response);

                if (request.equalsIgnoreCase("exit")) {
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
