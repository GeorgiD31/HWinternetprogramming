package homework;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class Server {
    private static final String FILE_NAME = "students.txt";
    private static Map<String, List<Double>> studentMap;

    public static void main(String[] args) {
        studentMap = loadStudents();
        try (ServerSocket serverSocket = new ServerSocket(1234)) {
            System.out.println("Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())
            ) {
                String request;
                while ((request = (String) in.readObject()) != null) {
                    if (request.equalsIgnoreCase("exit")) {
                        out.writeObject("Goodbye!");
                        break;
                    }
                    String response = handleRequest(request);
                    out.writeObject(response);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error processing client request: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }

        private String handleRequest(String request) {
            try {
                String[] parts = request.split(" ");
                String command = parts[0];
                switch (command) {
                    case "ADD":
                        if (parts.length < 3) {
                            return "Invalid ADD command. Usage: ADD <name> <grade>";
                        }
                        String name = parts[1];
                        double grade = Double.parseDouble(parts[2]);
                        addStudent(name, grade);
                        return "Student added.";
                    case "LIST":
                        return listStudents();
                    case "AVG":
                        if (parts.length < 2) {
                            return "Invalid AVG command. Usage: AVG <name>";
                        }
                        return averageGrade(parts[1]);
                    default:
                        return "Invalid command.";
                }
            } catch (NumberFormatException e) {
                return "Invalid grade format. Please enter a number.";
            } catch (ArrayIndexOutOfBoundsException e) {
                return "Incomplete command. Please check your input.";
            }
        }

        private void addStudent(String name, double grade) {
            List<Double> grades = studentMap.getOrDefault(name, new ArrayList<>());
            grades.add(grade);
            studentMap.put(name, grades);
            saveStudents();
        }

        private String listStudents() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<Double>> entry : studentMap.entrySet()) {
                sb.append(entry.getKey()).append(" - ").append(entry.getValue().toString()).append("\n");
            }
            return sb.toString();
        }

        private String averageGrade(String name) {
            List<Double> grades = studentMap.get(name);
            if (grades != null) {
                double average = grades.stream().mapToDouble(val -> val).average().orElse(0.0);
                return "Average grade for " + name + ": " + average;
            } else {
                return "Student not found.";
            }
        }
    }

    private static Map<String, List<Double>> loadStudents() {
        Map<String, List<Double>> students = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                String name = parts[0];
                String[] gradeStrings = parts[1].split(",");
                List<Double> grades = new ArrayList<>();
                for (String gradeStr : gradeStrings) {
                    grades.add(Double.parseDouble(gradeStr.trim()));
                }
                students.put(name, grades);
            }
        } catch (IOException e) {
            System.out.println("No existing data found, starting fresh.");
        }
        return students;
    }

    private static void saveStudents() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Map.Entry<String, List<Double>> entry : studentMap.entrySet()) {
                pw.print(entry.getKey() + ":");
                List<Double> grades = entry.getValue();
                for (int i = 0; i < grades.size(); i++) {
                    pw.print(grades.get(i));
                    if (i < grades.size() - 1) {
                        pw.print(",");
                    }
                }
                pw.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
