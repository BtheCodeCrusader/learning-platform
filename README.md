# Online Learning Platform

## 📖 Projektbeschreibung

Dieses Projekt modelliert eine Online-Lernplattform, auf der Nutzer Kurse erstellen, verwalten und belegen können. Ziel ist es, eine realitätsnahe Anwendung abzubilden, die typische Abläufe wie Kursbuchungen, Bewertungen und Zahlungsprozesse unterstützt.

Dabei liegt der Fokus auf der strukturierten Abbildung von Daten und Beziehungen innerhalb des Systems sowie auf der effizienten Speicherung und Verarbeitung dieser Informationen in einer relationalen Datenbank.

## 🛠️ Technologien & Tools

Dieses Projekt basiert auf folgenden Technologien:

- **PostgreSQL** – Relationale Datenbank zur persistenten Speicherung der Anwendungsdaten
- **Hibernate (JPA)** – ORM-Framework zur Abbildung von Java-Klassen auf Datenbanktabellen
- **Maven** – Build- und Dependency-Management-Tool
- **DBeaver** – Datenbank-Client zur Verwaltung und Analyse der Datenbank

## 🛠 **Setup (5 Minuten - auch ohne Vorkenntnisse)**

### **1) Klonen**
```bash
git clone https://github.com/BtheCodeCrusader/learning-platform.git
cd learning-platform
```

### **2) Java + Maven (Ubuntu)**
```bash
sudo apt update && sudo apt install openjdk-21-jdk maven -y
```

### **3) PostgreSQL Datenbank**
```bash
sudo -u postgres psql
```
```sql
CREATE DATABASE learning_platform_$(whoami);
CREATE USER $(whoami) WITH PASSWORD 'hibernate123';
GRANT ALL PRIVILEGES ON DATABASE learning_platform_$(whoami) TO $(whoami);
\q
```

### **4) Starten & Testen**
```bash
mvn clean compile exec:java -Dexec.mainClass="de.learning.platform.Main"
```
**Erwartet:** `Daten erfolgreich gespeichert!`

### **5) DBeaver verbinden**
```
Host: localhost:5432
Database: learning_platform_DEINNAME  
Username: DEINNAME
Password: hibernate123
```

## 🔄 **Täglicher Workflow**
```bash
git checkout main                       # Wechsel auf den main-Branch (Hauptbranch)
git pull origin main                    # Hole die neuesten Änderungen vom Remote-Repository

git checkout -b feature/deine-aufgabe   # Erstelle einen neuen Branch für deine Aufgabe

# Code ändern                           # Hier entwickelst du deine Funktionalität

mvn clean compile exec:java             # Projekt bauen und ausführen (Testen, ob alles funktioniert)

git add .                               # Alle Änderungen zum Commit vormerken
git commit -m "feat: Änderung"          # Änderungen lokal speichern (Commit erstellen)

git push origin feature/deine-aufgabe   # Branch auf GitHub hochladen
```

## ⚙️ **Konfiguration**
**`hibernate.cfg.xml` anpassen:**
```xml
hibernate.connection.url=jdbc:postgresql://localhost:5432/learning_platform_DEINNAME
hibernate.connection.username=DEINNAME
hibernate.connection.password=hibernate123
```

## ✅ **Erfolg prüfen**
**DBeaver Tabellen:** `professors`, `students`, `courses`, `enrollments`

## 🚀 **Fertig!**
**Clone → Setup → Code → Commit → Push** 

## 📁 File-based Data Storage (Exercise 2)

Die Anwendung wurde um file-basierten Datenaustausch mit JSON erweitert. Neue Daten können als
Einzelobjekt, als komplex verschachtelte Struktur oder als Liste mehrerer Datensätze importiert
und über Hibernate in PostgreSQL gespeichert werden. Zusätzlich können einzelne Entitäten sowie
aggregierte Query-Ergebnisse wieder als JSON-Dateien für den Datentransfer exportiert werden.

### Input Szenarien:
1. **Single Student** → `input1_single_student.json`
2. **Complex Course** (mit Professor + Module) → `input2_complex_course.json`
3. **Studenten-Liste** → `input3_students_list.json`

### Output Szenarien:
1. **Single Student** → `output1_student_single.json`
2. **Single Professor** → `output2_professor_single.json`
3. **Query: Students/Kurs** → `output3_students_per_course.json`
4. **Query: Durchschnittsnoten** → `output4_avg_grades.json`
5. **Query: Zertifikate** → `output5_certificates.json`

### Ausführen:
```bash
mvn clean compile exec:java -Dexec.mainClass="de.learning.platform.model.Main"
``` 

## 🧩 Polyglot Persistence (MongoDB)

Für die Erweiterung wurde neben PostgreSQL eine MongoDB eingebunden. Die relationale Datenbank bleibt das System of Record für `Student`, `Course`, `Enrollment`, `Submission` und `Certificate`. MongoDB speichert ergänzend halbstrukturierte Lernprofile pro Student, also Daten mit stark variabler Form und verschachtelten Details.

### Use Case
Die neue Sammlung `student_learning_profiles` hält pro Student ein Dokument mit eingebetteten Kurs-Snapshots. Jeder Snapshot enthält unter anderem:
- Kursreferenz und Dozent
- Einschreibedatum
- Anzahl Abgaben
- Durchschnittsnote
- letzte Aktivität
- Zertifikats-Titel
- letzte Abgaben als eingebettete Liste

### Figur der Datenstruktur
```text
PostgreSQL
  students ---- enrollments ---- courses ---- professors
      |              |                |
      |              |                +---- assignments ---- submissions
      |              |
      |              +---- certificates
      |
      +---- student_learning_profiles (MongoDB)
               |
               +-- _id: "student:S12345"
               +-- studentDbId
               +-- studentCode
               +-- studentName
               +-- email
               +-- courses[]
                     +-- courseId
                     +-- courseTitle
                     +-- professorName
                     +-- enrollmentDate
                     +-- submissionCount
                     +-- averageGrade
                     +-- lastSubmissionDate
                     +-- certificateTitles[]
                     +-- recentSubmissions[]
```

### MongoDB-Queries
1. `findHighPerformingStudents(courseTitle, minimumAverageGrade)`  
   Return: alle Studenten mit einem Kurs-Snapshot, dessen Durchschnittsnote über dem Schwellwert liegt.
2. `findInactiveStudents(courseTitle, inactivityDays)`  
   Return: Studenten, deren letzter Kursbeitrag älter als die angegebene Anzahl Tage ist.
3. `findCertificateHolders(courseTitle)`  
   Return: Studenten mit mindestens einem Zertifikat in dem Kurs.

### Polyglot Query Cases
1. `printCourseEngagementReport(...)`  
   SQL liefert Kurs, Dozent und eingeschriebene Studenten. MongoDB ergänzt die High-Performer-Snapshots.  
   Return: kombinierter Kursbericht mit relationalen Stammdaten und NoSQL-Engagementdaten.
2. `printAtRiskSupportReport(...)`  
   MongoDB liefert inaktive Studenten im Kurs. SQL ergänzt Durchschnittsnoten und Zertifikatsanzahl.  
   Return: Support-Liste für gefährdete Studenten mit beiden Datenquellen.

### MongoDB Setup
Standardmäßig wird auf `mongodb://localhost:27017` verbunden. Die Werte können bei Bedarf über System-Properties überschrieben werden:
```bash
mvn clean compile exec:java \
  -Dexec.mainClass="de.learning.platform.model.Main" \
  -Dmongo.uri="mongodb://localhost:27017" \
  -Dmongo.db="learning_platform" \
  -Dmongo.collection="student_learning_profiles"
```
