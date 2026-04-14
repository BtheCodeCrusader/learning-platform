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