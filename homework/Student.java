package homework;

import java.io.*;
import java.net.*;
import java.util.*;

class Student {
    String name;
    List<Double> grades;

    public Student(String name) {
        this.name = name;
        this.grades = new ArrayList<>();
    }

    public void addGrade(double grade) {
        grades.add(grade);
    }

    public double getAverageGrade() {
        return grades.stream().mapToDouble(val -> val).average().orElse(0.0);
    }

    @Override
    public String toString() {
        return name + " - " + grades.toString();
    }
}

