package de.learning.platform.mongodb;

import de.learning.platform.model.Course;
import de.learning.platform.model.Student;
import org.bson.Document;
import org.hibernate.Session;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolyglotAnalyticsService {

    private final MongoLearningRepository mongoRepository;

    public PolyglotAnalyticsService(MongoLearningRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    public void printCourseEngagementReport(Session session, String courseTitle) {
        Course course = session.createQuery(
                        "SELECT c FROM Course c JOIN FETCH c.professor WHERE c.title = :title",
                        Course.class)
                .setParameter("title", courseTitle)
                .uniqueResult();

        if (course == null) {
            System.out.println("❌ Course not found for polyglot report: " + courseTitle);
            return;
        }

        List<Student> enrolledStudents = session.createQuery(
                        "SELECT e.student FROM Enrollment e JOIN e.course c WHERE c.title = :title",
                        Student.class)
                .setParameter("title", courseTitle)
                .getResultList();

        List<Document> mongoProfiles = mongoRepository.findHighPerformingStudents(courseTitle, 85.0);

        System.out.println("\nPolyglot Query 1: Course engagement report");
        System.out.println("Course: " + course.getTitle());
        System.out.println("Professor: " + course.getProfessor().getName());
        System.out.println("Enrolled students: " + enrolledStudents.size());
        System.out.println("High performers from MongoDB:");

        mongoProfiles.stream()
                .sorted(Comparator.comparingDouble((Document doc) -> doc.get("course", Document.class).getDouble("averageGrade")).reversed())
                .forEach(doc -> {
                    Document courseSnapshot = doc.get("course", Document.class);
                    System.out.println(
                            " - " + doc.getString("studentName")
                                    + " (" + doc.getString("studentCode") + ")"
                                    + " | avg grade " + String.format("%.1f", courseSnapshot.getDouble("averageGrade"))
                                    + " | engagement " + courseSnapshot.getInteger("engagementScore"));
                });
    }

    public void printAtRiskSupportReport(Session session, String courseTitle, int inactivityDays) {
        Course course = session.createQuery(
                        "SELECT c FROM Course c JOIN FETCH c.professor WHERE c.title = :title",
                        Course.class)
                .setParameter("title", courseTitle)
                .uniqueResult();

        if (course == null) {
            System.out.println("❌ Course not found for support report: " + courseTitle);
            return;
        }

        List<Document> inactiveProfiles = mongoRepository.findInactiveStudents(courseTitle, inactivityDays);
        List<Long> studentDbIds = inactiveProfiles.stream()
                .map(doc -> doc.getLong("studentDbId"))
                .toList();

        List<Object[]> averageGrades = session.createQuery(
                        "SELECT s.student.id, AVG(s.grade) " +
                                "FROM Submission s " +
                                "JOIN s.assignment a " +
                                "WHERE s.student.id IN :ids AND a.course.title = :title " +
                                "GROUP BY s.student.id",
                        Object[].class)
                .setParameter("ids", studentDbIds.isEmpty() ? List.of(-1L) : studentDbIds)
                .setParameter("title", courseTitle)
                .getResultList();

        List<Object[]> certificateCounts = session.createQuery(
                        "SELECT cert.student.id, COUNT(cert) " +
                                "FROM Certificate cert " +
                                "JOIN cert.course c " +
                                "WHERE cert.student.id IN :ids AND c.title = :title " +
                                "GROUP BY cert.student.id",
                        Object[].class)
                .setParameter("ids", studentDbIds.isEmpty() ? List.of(-1L) : studentDbIds)
                .setParameter("title", courseTitle)
                .getResultList();

        Map<Long, Double> avgGradeByStudent = new HashMap<>();
        for (Object[] row : averageGrades) {
            avgGradeByStudent.put((Long) row[0], (Double) row[1]);
        }

        Map<Long, Long> certificatesByStudent = new HashMap<>();
        for (Object[] row : certificateCounts) {
            certificatesByStudent.put((Long) row[0], (Long) row[1]);
        }

        System.out.println("\nPolyglot Query 2: Support list for inactive students");
        System.out.println("Course: " + course.getTitle());
        System.out.println("Inactivity threshold: " + inactivityDays + " days");
        System.out.println("Inactive students:");

        for (Document profile : inactiveProfiles) {
            Long studentDbId = profile.getLong("studentDbId");
            Document courseSnapshot = profile.get("course", Document.class);

            System.out.println(
                    " - " + profile.getString("studentName")
                            + " (" + profile.getString("studentCode") + ")"
                            + " | last activity "
                            + courseSnapshot.getDate("lastSubmissionDate")
                            + " | avg grade "
                            + String.format("%.1f", avgGradeByStudent.getOrDefault(studentDbId, 0.0))
                            + " | certificates "
                            + certificatesByStudent.getOrDefault(studentDbId, 0L));
        }
    }
}
