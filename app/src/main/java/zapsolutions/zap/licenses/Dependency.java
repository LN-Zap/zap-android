package zapsolutions.zap.licenses;

import com.google.gson.Gson;

public class Dependency {

    private String project;
    private String url;
    private String description;
    private String version;
    private String[] developers;
    private License[] licenses;
    private String dependency;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String[] getDevelopers() {
        return developers;
    }

    public void setDevelopers(String[] developers) {
        this.developers = developers;
    }

    public License[] getLicenses() {
        return licenses;
    }

    public void setLicenses(License[] licenses) {
        this.licenses = licenses;
    }

    public String getDependency() {
        return dependency;
    }

    public void setDependency(String dependency) {
        this.dependency = dependency;
    }

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
