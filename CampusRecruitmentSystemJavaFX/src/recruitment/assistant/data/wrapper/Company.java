package recruitment.assistant.data.wrapper;

public class Company {
    String id;
    String title;
    String campus;
    String headquarter;
    Boolean isAvail;

    public Company(String id, String title, String campus, String headquarter, Boolean isAvail) {
        this.id = id;
        this.title = title;
        this.campus = campus;
        this.headquarter = headquarter;
        this.isAvail = isAvail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getHeadquarter() {
        return headquarter;
    }

    public void setHeadquarter(String headquarter) {
        this.headquarter = headquarter;
    }

    public Boolean getAvailability() {
        return isAvail;
    }

    public void setIsAvail(Boolean isAvail) {
        this.isAvail = isAvail;
    }
    
    
}
