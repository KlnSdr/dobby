package dobby.files;

public class StaticFile {
    private String contentType = "text/html";
    private String content = "";

    public StaticFile() {
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
