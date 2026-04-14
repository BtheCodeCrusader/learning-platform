package de.learning.platform.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "submissions")
public class Submission {

    @EmbeddedId
    private SubmissionId id;

    @ManyToOne
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @MapsId("assignmentId")
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    private Integer grade;
    private LocalDate submissionDate;

    public SubmissionId getId() {
        return id;
    }

    public void setId(SubmissionId id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public LocalDate getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDate submissionDate) {
        this.submissionDate = submissionDate;
    }
}