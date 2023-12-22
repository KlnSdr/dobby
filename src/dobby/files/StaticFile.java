package dobby.files;

/**
 * Static file
 */
public class StaticFile {
    private String contentType = "text/html";
    private byte[] content = new byte[0];
    private long lastAccessed;

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

    /**
     * Gets the last accessed time for the session.
     *
     * @return The last accessed time in milliseconds since the epoch (1970-01-01T00:00:00Z).
     */
    public long getLastAccessed() {
        return lastAccessed;
    }

    /**
     * Sets the last accessed time for the session.
     *
     * @param lastAccessed The last accessed time in milliseconds since the epoch (1970-01-01T00:00:00Z).
     */
    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }
}
