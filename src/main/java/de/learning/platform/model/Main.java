package de.learning.platform.model;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.time.LocalDate;
import java.util.Date;

public class Main {

    public static void main(String[] args) {

        try (SessionFactory factory = buildSessionFactory();
             Session session = factory.openSession()) {

            // =====================
            // TESTDATEN ERSTELLEN
            // =====================
            session.beginTransaction();

            createTestData(session);

            session.getTransaction().commit();
            System.out.println("\n✅ Testdaten gespeichert!");

            // =====================
            // QUERIES AUSFÜHREN
            // =====================
            session.beginTransaction();

            runQueries(session);

            session.getTransaction().commit();
            System.out.println("\n✅ Queries erfolgreich ausgeführt!\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =====================
    // HIBERNATE SETUP
    // =====================
    private static SessionFactory buildSessionFactory() {
        return new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Person.class)
                .addAnnotatedClass(Student.class)
                .addAnnotatedClass(Professor.class)
                .addAnnotatedClass(Course.class)
                .addAnnotatedClass(Enrollment.class)
                .addAnnotatedClass(Module.class)
                .addAnnotatedClass(Assignment.class)
                .addAnnotatedClass(Submission.class)
                .addAnnotatedClass(SubmissionId.class)
                .addAnnotatedClass(Certificate.class)
                .addAnnotatedClass(Category.class)
                .buildSessionFactory();
    }

    // =====================
    // HELPER METHODEN
    // =====================
    private static Professor createProfessor(Session s, String name, String mail, String title) {
        Professor p = new Professor();
        p.setName(name);
        p.setEmail(mail);
        p.setTitle(title);
        s.persist(p);
        return p;
    }

    private static Student createStudent(Session s, String name, String mail, String id) {
        Student st = new Student();
        st.setName(name);
        st.setEmail(mail);
        st.setStudentId(id);
        s.persist(st);
        return st;
    }

    private static Course createCourse(Session s, String title, String desc, Professor p) {
        Course c = new Course();
        c.setTitle(title);
        c.setDescription(desc);
        c.setProfessor(p);
        s.persist(c);
        return c;
    }

    private static void createEnrollment(Session s, Student st, Course c) {
        Enrollment e = new Enrollment();
        e.setStudent(st);
        e.setCourse(c);
        e.setEnrollmentDate(new Date());
        s.persist(e);
    }

    private static Module createModule(Session s, String title, String desc, Course c) {
        Module m = new Module();
        m.setTitle(title);
        m.setDescription(desc);
        m.setCourse(c);
        s.persist(m);
        return m;
    }

    private static Assignment createAssignment(Session s, String title, String desc, int days, Course c) {
        Assignment a = new Assignment();
        a.setTitle(title);
        a.setDescription(desc);
        a.setDueDate(LocalDate.now().plusDays(days));
        a.setCourse(c);
        s.persist(a);
        return a;
    }

    private static void createSubmission(Session s, Student st, Assignment a, int grade) {
        Submission sub = new Submission();
        sub.setId(new SubmissionId(st.getId(), a.getId()));
        sub.setStudent(st);
        sub.setAssignment(a);
        sub.setGrade(grade);
        sub.setSubmissionDate(LocalDate.now());
        s.persist(sub);
    }

    private static Category createCategory(Session s, String name) {
        Category c = new Category();
        c.setName(name);
        s.persist(c);
        return c;
    }

    private static void createCertificate(Session s, String title, Student st, Course c) {
        Certificate cert = new Certificate();
        cert.setTitle(title);
        cert.setIssueDate(LocalDate.now());
        cert.setStudent(st);
        cert.setCourse(c);
        s.persist(cert);
    }

    // =====================
    // TESTDATEN
    // =====================
    private static void createTestData(Session session) {

        // Professoren
        Professor prof1 = createProfessor(session, "Prof. Mustermann", "prof@uni.de", "Dr.");
        Professor prof2 = createProfessor(session, "Prof. Schneider", "schneider@uni.de", "Prof. Dr.");

        // Studenten
        Student s1 = createStudent(session, "Anna Studentin", "anna@student.de", "S12345");
        Student s2 = createStudent(session, "Max Student", "max@student.de", "S12346");
        Student s3 = createStudent(session, "Lisa Lernend", "lisa@student.de", "S12347");
        Student s4 = createStudent(session, "Tom Teilnehmer", "tom@student.de", "S12348");

        // Kurse
        Course c1 = createCourse(session, "Java Programmierung", "Einführung in Java", prof1);
        Course c2 = createCourse(session, "Datenbanken", "SQL und Relationen", prof1);
        Course c3 = createCourse(session, "Web Entwicklung", "Frontend & Backend", prof2);

        // Enrollments
        createEnrollment(session, s1, c1);
        createEnrollment(session, s2, c1);
        createEnrollment(session, s3, c1);
        createEnrollment(session, s1, c2);
        createEnrollment(session, s4, c2);
        createEnrollment(session, s2, c3);
        createEnrollment(session, s3, c3);

        // Module
        Module m1 = createModule(session, "Grundlagen", "Variablen & Klassen", c1);
        Module m2 = createModule(session, "OOP", "Objektorientierung", c1);
        Module m3 = createModule(session, "SQL Basics", "JOIN & GROUP BY", c2);
        Module m4 = createModule(session, "Web Basics", "HTML, CSS, JS", c3);

        // Assignments
        Assignment a1 = createAssignment(session, "Hausaufgabe 1", "Java Variablen", 7, c1);
        Assignment a2 = createAssignment(session, "Hausaufgabe 2", "OOP", 10, c1);
        Assignment a3 = createAssignment(session, "SQL Übung", "JOINs", 5, c2);
        Assignment a4 = createAssignment(session, "Web Projekt", "Mini Webseite", 14, c3);

        session.flush(); // IDs sichern

        // Submissions
        createSubmission(session, s1, a1, 95);
        createSubmission(session, s2, a1, 88);
        createSubmission(session, s3, a2, 91);
        createSubmission(session, s1, a3, 84);
        createSubmission(session, s4, a3, 78);
        createSubmission(session, s2, a4, 92);
        createSubmission(session, s3, a4, 97);

        // Kategorien
        Category cat1 = createCategory(session, "Programmierung");
        Category cat2 = createCategory(session, "Datenbanken");
        Category cat3 = createCategory(session, "Web");

        c1.getCategories().add(cat1);
        c2.getCategories().add(cat2);
        c3.getCategories().add(cat3);

        // Zertifikate
        createCertificate(session, "Java Zertifikat", s1, c1);
        createCertificate(session, "SQL Zertifikat", s4, c2);
        createCertificate(session, "Web Zertifikat", s3, c3);
    }

    // =====================
    // QUERIES
    // =====================
    private static void runQueries(Session session) {

        System.out.println("\nStudenten pro Kurs:");
        session.createQuery(
                        "SELECT c.title, COUNT(e.student) FROM Enrollment e JOIN e.course c GROUP BY c.title",
                        Object[].class)
                .getResultList()
                .forEach(r -> System.out.println(r[0] + ": " + r[1]));

        System.out.println("\nDurchschnittsnote pro Professor:");
        session.createQuery(
                        "SELECT p.name, AVG(s.grade) FROM Submission s JOIN s.assignment.course c JOIN c.professor p GROUP BY p.name",
                        Object[].class)
                .getResultList()
                .forEach(r -> System.out.println(r[0] + ": " + String.format("%.1f", r[1])));

        System.out.println("\nZertifikate pro Kurs:");
        session.createQuery(
                        "SELECT c.title, COUNT(cert) FROM Certificate cert JOIN cert.course c GROUP BY c.title",
                        Object[].class)
                .getResultList()
                .forEach(r -> System.out.println(r[0] + ": " + r[1]));
    }
}
