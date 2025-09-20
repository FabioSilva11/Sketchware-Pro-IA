package pro.sketchware.activities.chat.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatSession {
    private String id;
    private String title;
    private long createdAt;
    private long updatedAt;
    private List<ChatMessage> messages;
    private String projectPath;
    
    public ChatSession() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.messages = new ArrayList<>();
    }
    
    public ChatSession(String id, String title, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.messages = new ArrayList<>();
    }
    
    // Getters and Setters
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
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<ChatMessage> getMessages() {
        return messages;
    }
    
    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }
    
    public String getProjectPath() {
        return projectPath;
    }
    
    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }
    
    // Helper methods
    public void addMessage(ChatMessage message) {
        messages.add(message);
        updatedAt = System.currentTimeMillis();
    }
    
    public void clearMessages() {
        messages.clear();
        updatedAt = System.currentTimeMillis();
    }
    
    public int getMessageCount() {
        return messages.size();
    }
    
    public String getLastMessagePreview() {
        if (messages.isEmpty()) {
            return "No messages";
        }
        String lastMessage = messages.get(messages.size() - 1).getContent();
        return lastMessage.length() > 50 ? lastMessage.substring(0, 50) + "..." : lastMessage;
    }
}
