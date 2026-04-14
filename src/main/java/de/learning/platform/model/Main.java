package de.learning.platform;

import de.learning.platform.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.time.LocalDate;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Person.class)
                .addAnnotatedClass(Student.class)
                .addAnnotatedClass(Professor.class)
                .addAnnotatedClass(Course.class)
                .addAnnotatedClass(Enrollment.class)
                .addAnnotatedClass(de.learning.platform.model.Module.class)
                .addAnnotatedClass(Assignment.class)
                .addAnnotatedClass(Submission.class)
                .addAnnotatedClass(SubmissionId.class)
                .addAnnotatedClass(Certificate.class)
                .addAnnotatedClass(Category.class)
                .buildSessionFactory();

        try (Session session = factory.openSession()) {
            session.beginTransaction();

            Professor prof = new Professor();
            prof.setName("Prof. Mustermann");
            prof.setEmail("prof@uni.de");
            prof.setTitle("Dr.");
            session.persist(prof);

            Student student = new Student();
            student.setName("Anna Studentin");
            student.setEmail("anna@student.de");
            student.setStudentId("S12345");
            session.persist(student);

            Course course = new Course();
            course.setTitle("Java Programmierung");
            course.setDescription("Einführung in Java und ORM");
            course.setProfessor(prof);
            session.persist(course);

            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setCourse(course);
            enrollment.setEnrollmentDate(new java.util.Date());
            session.persist(enrollment);

            de.learning.platform.model.Module module = new de.learning.platform.model.Module();
            module.setTitle("Modul 1: Grundlagen");
            module.setDescription("Variablen, Methoden, Klassen");
            module.setCourse(course);
            session.persist(module);

            Assignment assignment = new Assignment();
            assignment.setTitle("Erste Hausaufgabe");
            assignment.setDescription("Hello World Programm");
            assignment.setDueDate(LocalDate.now().plusDays(7));
            assignment.setCourse(course);
            session.persist(assignment);

            Submission submission = new Submission();
            submission.setId(new SubmissionId(student.getId(), assignment.getId()));
            submission.setStudent(student);
            submission.setAssignment(assignment);
            submission.setGrade(95);
            submission.setSubmissionDate(LocalDate.now());
            session.persist(submission);

            Category category = new Category();
            category.setName("Programmierung");
            session.persist(category);

            if (course.getCategories() == null) {
                course.setCategories(new ArrayList<>());
            }
            course.getCategories().add(category);

            Certificate cert = new Certificate();
            cert.setTitle("Java Grundlagen Zertifikat");
            cert.setIssueDate(LocalDate.now());
            cert.setStudent(student);
            cert.setCourse(course);
            session.persist(cert);

            session.getTransaction().commit();
            System.out.println("Daten erfolgreich gespeichert!");
        } finally {
            factory.close();
        }
    }
}