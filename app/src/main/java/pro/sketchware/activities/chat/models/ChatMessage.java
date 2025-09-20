package pro.sketchware.activities.chat.models;

public class ChatMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;
    public static final int TYPE_SYSTEM = 2;
    
    private int type;
    private String content;
    private long timestamp;
    private boolean loading;
    private boolean error;
    private String attachment;
    
    public ChatMessage(int type, String content, long timestamp) {
        this.type = type;
        this.content = content;
        this.timestamp = timestamp;
        this.loading = false;
        this.error = false;
    }
    
    // Getters and Setters
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isLoading() {
        return loading;
    }
    
    public void setLoading(boolean loading) {
        this.loading = loading;
    }
    
    public boolean isError() {
        return error;
    }
    
    public void setError(boolean error) {
        this.error = error;
    }
    
    public String getAttachment() {
        return attachment;
    }
    
    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }
}
