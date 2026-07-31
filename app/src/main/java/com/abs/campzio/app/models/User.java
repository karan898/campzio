package com.abs.campzio.app.models;

public class User {
    private String enrollment;
    private String name;
    private String email;
    private String id; // phone number
    private String role;
    private String course;
    private String department;
    private String section;
    private String semester;

    public User() {
        // Required for Firebase
    }

    public User(String enrollment, String name, String email, String id, String role, String course, String department, String section, String semester) {
        this.enrollment = enrollment;
        this.name = name;
        this.email = email;
        this.id = id;
        this.role = role;
        this.course = course;
        this.department = department;
        this.section = section;
        this.semester = semester;
    }

    public String getEnrollment() { return enrollment; }
    public void setEnrollment(String enrollment) { this.enrollment = enrollment; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
}
