package pro.sketchware.activities.chat.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pro.sketchware.activities.chat.models.ChatMessage;
import pro.sketchware.activities.chat.models.ChatSession;
import pro.sketchware.ai.OpenAIClient;
import pro.sketchware.ai.ClaudeClient;
import pro.sketchware.ai.GroqClient;

public class ChatManager {
    private static final String TAG = "ChatManager";
    private static final String PREFS_NAME = "chat_sessions";
    private static final String KEY_SESSIONS = "sessions";
    
    private Context context;
    private SharedPreferences prefs;
    private OpenAIClient openAIClient;
    private ClaudeClient claudeClient;
    private GroqClient groqClient;
    
    public interface ChatCallback {
        void onResponse(String response);
        void onError(String error);
    }
    
    public ChatManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.openAIClient = new OpenAIClient();
        this.claudeClient = new ClaudeClient();
        this.groqClient = new GroqClient();
    }
    
    public ChatSession createNewSession() {
        return new ChatSession();
    }
    
    public ChatSession loadSession(String sessionId) {
        try {
            String sessionsJson = prefs.getString(KEY_SESSIONS, "{}");
            JSONObject sessions = new JSONObject(sessionsJson);
            
            if (sessions.has(sessionId)) {
                JSONObject sessionJson = sessions.getJSONObject(sessionId);
                return parseSession(sessionJson);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error loading session", e);
        }
        return null;
    }
    
    public void saveSession(ChatSession session) {
        try {
            String sessionsJson = prefs.getString(KEY_SESSIONS, "{}");
            JSONObject sessions = new JSONObject(sessionsJson);
            sessions.put(session.getId(), sessionToJson(session));
            prefs.edit().putString(KEY_SESSIONS, sessions.toString()).apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error saving session", e);
        }
    }
    
    public List<ChatSession> getAllSessions() {
        List<ChatSession> sessions = new ArrayList<>();
        try {
            String sessionsJson = prefs.getString(KEY_SESSIONS, "{}");
            JSONObject sessionsObj = new JSONObject(sessionsJson);
            
            for (int i = 0; i < sessionsObj.length(); i++) {
                String key = sessionsObj.names().getString(i);
                JSONObject sessionJson = sessionsObj.getJSONObject(key);
                sessions.add(parseSession(sessionJson));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error loading all sessions", e);
        }
        return sessions;
    }
    
    public void deleteSession(String sessionId) {
        try {
            String sessionsJson = prefs.getString(KEY_SESSIONS, "{}");
            JSONObject sessions = new JSONObject(sessionsJson);
            sessions.remove(sessionId);
            prefs.edit().putString(KEY_SESSIONS, sessions.toString()).apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error deleting session", e);
        }
    }
    
    public void sendMessage(String message, ChatSession session, ChatCallback callback) {
        // Determinar qual provedor usar baseado nas configurações
        String provider = getPreferredProvider();
        
        try {
            List<OpenAIClient.Message> messages = buildMessageList(session, message);
            
            switch (provider) {
                case "openai":
                    sendWithOpenAI(messages, callback);
                    break;
                case "claude":
                    sendWithClaude(messages, callback);
                    break;
                case "groq":
                    sendWithGroq(messages, callback);
                    break;
                default:
                    callback.onError("No AI provider configured");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
            callback.onError(e.getMessage());
        }
    }
    
    private void sendWithOpenAI(List<OpenAIClient.Message> messages, ChatCallback callback) {
        new Thread(() -> {
            try {
                String response = openAIClient.chat(messages);
                callback.onResponse(response);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }
    
    private void sendWithClaude(List<OpenAIClient.Message> messages, ChatCallback callback) {
        new Thread(() -> {
            try {
                // Converter para formato Claude
                List<ClaudeClient.Message> claudeMessages = new ArrayList<>();
                for (OpenAIClient.Message msg : messages) {
                    claudeMessages.add(new ClaudeClient.Message(msg.role, msg.content));
                }
                
                String response = claudeClient.chat(claudeMessages);
                callback.onResponse(response);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }
    
    private void sendWithGroq(List<OpenAIClient.Message> messages, ChatCallback callback) {
        new Thread(() -> {
            try {
                // Converter para formato Groq
                List<GroqClient.Message> groqMessages = new ArrayList<>();
                for (OpenAIClient.Message msg : messages) {
                    groqMessages.add(new GroqClient.Message(msg.role, msg.content));
                }
                
                String response = groqClient.chat(groqMessages);
                callback.onResponse(response);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }
    
    private List<OpenAIClient.Message> buildMessageList(ChatSession session, String newMessage) {
        List<OpenAIClient.Message> messages = new ArrayList<>();
        
        // Adicionar prompt do sistema
        messages.add(new OpenAIClient.Message("system", getSystemPrompt()));
        
        // Adicionar histórico da conversa
        for (ChatMessage msg : session.getMessages()) {
            String role = msg.getType() == ChatMessage.TYPE_USER ? "user" : "assistant";
            messages.add(new OpenAIClient.Message(role, msg.getContent()));
        }
        
        // Adicionar nova mensagem
        messages.add(new OpenAIClient.Message("user", newMessage));
        
        return messages;
    }
    
    private String getSystemPrompt() {
        return "You are an AI assistant specialized in Android development and Sketchware. " +
               "You help users with:\n" +
               "- Android app development\n" +
               "- Sketchware project assistance\n" +
               "- Code generation and debugging\n" +
               "- UI/UX design suggestions\n" +
               "- Project structure optimization\n\n" +
               "Always provide helpful, accurate, and practical advice for Android development.";
    }
    
    private String getPreferredProvider() {
        // Por padrão, usar OpenAI, mas pode ser configurado
        return "openai";
    }
    
    private ChatSession parseSession(JSONObject sessionJson) throws JSONException {
        ChatSession session = new ChatSession(
            sessionJson.getString("id"),
            sessionJson.getString("title"),
            sessionJson.getLong("createdAt"),
            sessionJson.getLong("updatedAt")
        );
        
        if (sessionJson.has("projectPath")) {
            session.setProjectPath(sessionJson.getString("projectPath"));
        }
        
        JSONArray messagesArray = sessionJson.getJSONArray("messages");
        for (int i = 0; i < messagesArray.length(); i++) {
            JSONObject messageJson = messagesArray.getJSONObject(i);
            ChatMessage message = new ChatMessage(
                messageJson.getInt("type"),
                messageJson.getString("content"),
                messageJson.getLong("timestamp")
            );
            session.addMessage(message);
        }
        
        return session;
    }
    
    private JSONObject sessionToJson(ChatSession session) throws JSONException {
        JSONObject sessionJson = new JSONObject();
        sessionJson.put("id", session.getId());
        sessionJson.put("title", session.getTitle());
        sessionJson.put("createdAt", session.getCreatedAt());
        sessionJson.put("updatedAt", session.getUpdatedAt());
        
        if (session.getProjectPath() != null) {
            sessionJson.put("projectPath", session.getProjectPath());
        }
        
        JSONArray messagesArray = new JSONArray();
        for (ChatMessage message : session.getMessages()) {
            JSONObject messageJson = new JSONObject();
            messageJson.put("type", message.getType());
            messageJson.put("content", message.getContent());
            messageJson.put("timestamp", message.getTimestamp());
            messagesArray.put(messageJson);
        }
        sessionJson.put("messages", messagesArray);
        
        return sessionJson;
    }
}
