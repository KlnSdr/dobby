package dobby.files;

public class StaticFile {
    private String contentType = "text/html";
    private byte[] content = new byte[0];

    public StaticFile() {
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
