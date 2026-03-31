package de.learning.platform;

import de.learning.platform.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Main {
    public static void main(String[] args) {
        SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Person.class)
                .addAnnotatedClass(Student.class)
                .addAnnotatedClass(Professor.class)
                .addAnnotatedClass(Course.class)
                .buildSessionFactory();

        try (Session session = factory.openSession()) {
            session.beginTransaction();

            // Testdaten
            Professor prof = new Professor();
            prof.setName("Prof. Mustermann");
            prof.setEmail("prof@uni.de");
            prof.setTitle("Dr.");
            session.persist(prof);

            Course course = new Course();
            course.setTitle("Java Programmierung");
            course.setDescription("Einführung in Java");
            course.setProfessor(prof);
            session.persist(course);

            session.getTransaction().commit();
            System.out.println("Daten erfolgreich gespeichert!");
        } finally {
            factory.close();
        }
    }
}