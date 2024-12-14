package projet.gestionabsence.model;

public class ScheduleItem {

    private String name;
    private String url;
    private String content;

    public ScheduleItem(String name, String url, String content) {
        this.name = name;
        this.url = url;
        this.content = content;
    }
    public ScheduleItem() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
