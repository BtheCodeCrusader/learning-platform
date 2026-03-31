package de.learning.platform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "professors")
@Inheritance(strategy = InheritanceType.JOINED)
public class Professor extends Person {
    private String title;

    // Getter/Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}