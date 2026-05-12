package de.learning.platform.model;

import de.learning.platform.util.JsonUtil;
import org.hibernate.Session;

import java.io.File;
import java.util.List;

public class FileProcessor {

    public static void processInputFiles(Session session) {
        try {
            Student student = JsonUtil.readSingle(
                    new File("src/main/resources/input/input1_single_student.json"),
                    Student.class);
            session.persist(student);
            System.out.println("✅ Single Student: " + student.getStudentId());

            List<Student> students = JsonUtil.readList(
                    new File("src/main/resources/input/input3_students_list.json"),
                    Student.class);
            students.forEach(session::persist);
            System.out.println("✅ " + students.size() + " Students Liste");

            Course course = JsonUtil.readSingle(
                    new File("src/main/resources/input/input2_complex_course.json"),
                    Course.class);

            if (course != null) {
                Professor professor = course.getProfessor();
                if (professor != null) {
                    session.persist(professor);
                    course.setProfessor(professor);
                }

                List<Module> modules = course.getModules();
                course.setModules(List.of());
                session.persist(course);

                if (modules != null) {
                    for (Module module : modules) {
                        module.setCourse(course);
                        session.persist(module);
                    }
                    course.setModules(modules);
                }

                System.out.println("✅ Complex Course: " + course.getTitle());
            }

            System.out.println("✅ Input Files verarbeitet!");

        } catch (Exception e) {
            System.out.println("❌ Input Fehler: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void createOutputFiles(Session session) {
        try {
            Student student = session.createQuery(
                            "FROM Student WHERE studentId = 'S12345'", Student.class)
                    .uniqueResult();
            if (student != null) {
                JsonUtil.write(student, new File("output1_student_single.json"));
                System.out.println("✅ output1_student_single.json");
            }

            Professor professor = session.createQuery(
                            "FROM Professor WHERE name = 'Prof. Mustermann'", Professor.class)
                    .uniqueResult();
            if (professor != null) {
                JsonUtil.write(professor, new File("output2_professor_single.json"));
                System.out.println("✅ output2_professor_single.json");
            }

            List<Object[]> studentsPerCourse = session.createQuery(
                    "SELECT c.title, COUNT(e) FROM Enrollment e JOIN e.course c GROUP BY c.title",
                    Object[].class).getResultList();
            JsonUtil.write(studentsPerCourse, new File("output3_students_per_course.json"));
            System.out.println("✅ output3_students_per_course.json");

            List<Object[]> avgGrades = session.createQuery(
                    "SELECT p.name, AVG(s.grade) FROM Submission s " +
                            "JOIN s.assignment.course c JOIN c.professor p GROUP BY p.name",
                    Object[].class).getResultList();
            JsonUtil.write(avgGrades, new File("output4_avg_grades.json"));
            System.out.println("✅ output4_avg_grades.json");

            List<Object[]> certificatesPerCourse = session.createQuery(
                    "SELECT c.title, COUNT(cert) FROM Certificate cert " +
                            "JOIN cert.course c GROUP BY c.title",
                    Object[].class).getResultList();
            JsonUtil.write(certificatesPerCourse, new File("output5_certificates.json"));
            System.out.println("✅ output5_certificates.json");

            System.out.println("✅ Output Files erstellt!");

        } catch (Exception e) {
            System.out.println("❌ Output Fehler: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
