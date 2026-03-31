package de.learning.platform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
@Inheritance(strategy = InheritanceType.JOINED)
public class Student extends Person {
    private String studentId;

    // Getter/Setter
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
}