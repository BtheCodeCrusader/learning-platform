package de.learning.platform.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import de.learning.platform.model.Certificate;
import de.learning.platform.model.Course;
import de.learning.platform.model.Enrollment;
import de.learning.platform.model.Professor;
import de.learning.platform.model.Student;
import de.learning.platform.model.Submission;
import org.bson.Document;
import org.hibernate.Session;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MongoLearningRepository implements AutoCloseable {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    private final MongoClient client;
    private final MongoCollection<Document> profiles;

    public MongoLearningRepository(String uri, String databaseName, String collectionName) {
        this.client = MongoClients.create(uri);
        MongoDatabase database = client.getDatabase(databaseName);
        this.profiles = database.getCollection(collectionName);
    }

    public static MongoLearningRepository createDefault() {
        String uri = System.getProperty("mongo.uri", "mongodb://localhost:27017");
        String databaseName = System.getProperty("mongo.db", "learning_platform");
        String collectionName = System.getProperty("mongo.collection", "student_learning_profiles");
        return new MongoLearningRepository(uri, databaseName, collectionName);
    }

    public void seedFromSql(Session session) {
        List<Student> students = session.createQuery(
                        "FROM Student s ORDER BY s.id",
                        Student.class)
                .getResultList();

        Map<Long, List<Enrollment>> enrollmentsByStudent = session.createQuery(
                        "SELECT e FROM Enrollment e JOIN FETCH e.course c ORDER BY e.id",
                        Enrollment.class)
                .getResultList()
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getStudent().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        Map<Long, List<Submission>> submissionsByStudent = session.createQuery(
                        "SELECT s FROM Submission s JOIN FETCH s.assignment a JOIN FETCH a.course c ORDER BY s.submissionDate DESC",
                        Submission.class)
                .getResultList()
                .stream()
                .collect(Collectors.groupingBy(
                        s -> s.getStudent().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        Map<Long, List<Certificate>> certificatesByStudent = session.createQuery(
                        "SELECT cert FROM Certificate cert JOIN FETCH cert.course c ORDER BY cert.issueDate DESC",
                        Certificate.class)
                .getResultList()
                .stream()
                .collect(Collectors.groupingBy(
                        c -> c.getStudent().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        for (Student student : students) {
            Document profile = buildProfile(
                    student,
                    enrollmentsByStudent.getOrDefault(student.getId(), List.of()),
                    submissionsByStudent.getOrDefault(student.getId(), List.of()),
                    certificatesByStudent.getOrDefault(student.getId(), List.of()));

            profiles.replaceOne(
                    new Document("_id", profile.getString("_id")),
                    profile,
                    new ReplaceOptions().upsert(true));
        }
    }

    public List<Document> findHighPerformingStudents(String courseTitle, double minimumAverageGrade) {
        List<Document> pipeline = List.of(
                new Document("$unwind", "$courses"),
                new Document("$match", new Document("courses.courseTitle", courseTitle)
                        .append("courses.averageGrade", new Document("$gte", minimumAverageGrade))
                        .append("courses.submissionCount", new Document("$gt", 0))),
                new Document("$project", new Document("_id", 0)
                        .append("studentDbId", 1)
                        .append("studentCode", 1)
                        .append("studentName", 1)
                        .append("email", 1)
                        .append("course", "$courses")));
        return profiles.aggregate(pipeline).into(new ArrayList<>());
    }

    public List<Document> findInactiveStudents(String courseTitle, int inactivityDays) {
        Date cutoff = Date.from(
                LocalDate.now()
                        .minusDays(inactivityDays)
                        .atStartOfDay(ZONE_ID)
                        .toInstant());

        List<Document> pipeline = List.of(
                new Document("$unwind", "$courses"),
                new Document("$match", new Document("courses.courseTitle", courseTitle)
                        .append("$or", List.of(
                                new Document("courses.lastSubmissionDate", new Document("$lt", cutoff)),
                                new Document("courses.lastSubmissionDate", new Document("$exists", false))))),
                new Document("$project", new Document("_id", 0)
                        .append("studentDbId", 1)
                        .append("studentCode", 1)
                        .append("studentName", 1)
                        .append("course", "$courses")));
        return profiles.aggregate(pipeline).into(new ArrayList<>());
    }

    public List<Document> findCertificateHolders(String courseTitle) {
        List<Document> pipeline = List.of(
                new Document("$unwind", "$courses"),
                new Document("$match", new Document("courses.courseTitle", courseTitle)
                        .append("courses.certificateTitles.0", new Document("$exists", true))),
                new Document("$project", new Document("_id", 0)
                        .append("studentDbId", 1)
                        .append("studentCode", 1)
                        .append("studentName", 1)
                        .append("course", "$courses")));
        return profiles.aggregate(pipeline).into(new ArrayList<>());
    }

    public List<Document> findProfilesByStudentDbIds(List<Long> studentDbIds) {
        if (studentDbIds == null || studentDbIds.isEmpty()) {
            return List.of();
        }

        return profiles.find(new Document("studentDbId", new Document("$in", studentDbIds)))
                .into(new ArrayList<>());
    }

    private Document buildProfile(
            Student student,
            List<Enrollment> enrollments,
            List<Submission> submissions,
            List<Certificate> certificates) {

        Map<Long, List<Submission>> submissionsByCourse = submissions.stream()
                .collect(Collectors.groupingBy(
                        submission -> submission.getAssignment().getCourse().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        Map<Long, List<Certificate>> certificatesByCourse = certificates.stream()
                .collect(Collectors.groupingBy(
                        certificate -> certificate.getCourse().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<Document> courseSnapshots = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            Course course = enrollment.getCourse();
            Professor professor = course.getProfessor();
            List<Submission> courseSubmissions = submissionsByCourse.getOrDefault(course.getId(), List.of());
            List<Certificate> courseCertificates = certificatesByCourse.getOrDefault(course.getId(), List.of());

            double averageGrade = courseSubmissions.stream()
                    .mapToInt(Submission::getGrade)
                    .average()
                    .orElse(0.0);

            Date lastSubmissionDate = courseSubmissions.stream()
                    .map(Submission::getSubmissionDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .map(MongoLearningRepository::toDate)
                    .orElse(null);

            List<Document> recentSubmissions = courseSubmissions.stream()
                    .sorted(Comparator.comparing(
                            Submission::getSubmissionDate,
                            Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                    .limit(3)
                    .map(submission -> new Document("assignmentTitle", submission.getAssignment().getTitle())
                            .append("grade", submission.getGrade())
                            .append("submittedOn", toDate(submission.getSubmissionDate())))
                    .toList();

            List<String> certificateTitles = courseCertificates.stream()
                    .map(Certificate::getTitle)
                    .toList();

            int engagementScore = courseSubmissions.size() * 10
                    + certificateTitles.size() * 25
                    + (int) Math.round(averageGrade);

            Document courseSnapshot = new Document("courseId", course.getId())
                    .append("courseTitle", course.getTitle())
                    .append("professorName", professor != null ? professor.getName() : null)
                    .append("enrollmentDate", enrollment.getEnrollmentDate())
                    .append("submissionCount", courseSubmissions.size())
                    .append("averageGrade", averageGrade)
                    .append("lastSubmissionDate", lastSubmissionDate)
                    .append("certificateTitles", certificateTitles)
                    .append("recentSubmissions", recentSubmissions)
                    .append("engagementScore", engagementScore);

            courseSnapshots.add(courseSnapshot);
        }

        return new Document("_id", "student:" + student.getStudentId())
                .append("studentDbId", student.getId())
                .append("studentCode", student.getStudentId())
                .append("studentName", student.getName())
                .append("email", student.getEmail())
                .append("courses", courseSnapshots)
                .append("profileUpdatedAt", Date.from(Instant.now()));
    }

    private static Date toDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return Date.from(date.atStartOfDay(ZONE_ID).toInstant());
    }

    @Override
    public void close() {
        client.close();
    }
}
