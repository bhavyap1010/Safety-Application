package com.example.b07demosummer2024;

public class Item {

    private String id;
    private String title;
    private String description;
    private String date;
    private String govId;
    private String courtOrder;
    private String name;
    private String relationship;
    private String phone;
    private String address;
    private String notes;
    private String medName;
    private String dosage;
    private String imageUrl;
    private String pdfUrl;

    public Item() {}

    public Item(String id, String title, String description, String date) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getGovId() { return govId; }
    public void setGovId(String govId) { this.govId = govId; }
    public String getCourtOrder() { return courtOrder; }
    public void setCourtOrder(String courtOrder) { this.courtOrder = courtOrder; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getMedName() { return medName; }
    public void setMedName(String medName) { this.medName = medName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getFileUrl() { return pdfUrl; }
    public void setFileUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

}
