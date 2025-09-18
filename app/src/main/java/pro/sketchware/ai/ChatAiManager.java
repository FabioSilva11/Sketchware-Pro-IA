package pro.sketchware.ai;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mod.hilal.saif.activities.tools.ConfigActivity;

/**
 * Manager para integrar todas as IAs disponíveis no chat
 */
public class ChatAiManager {
    
    private final OpenAIClient openAIClient;
    private final ClaudeClient claudeClient;
    private final GroqClient groqClient;
    
    public ChatAiManager() {
        this.openAIClient = new OpenAIClient();
        this.claudeClient = new ClaudeClient();
        this.groqClient = new GroqClient();
    }
    
    public interface ChatCallback {
        void onSuccess(String response, String provider);
        void onError(String error);
    }
    
    public void sendMessage(String userMessage, ChatCallback callback) {
        // Determinar qual provedor usar baseado nas configurações
        String selectedProvider = getSelectedProvider();
        sendMessage(userMessage, selectedProvider, callback);
    }
    
    public void sendMessage(String userMessage, String provider, ChatCallback callback) {
        // Criar mensagens para o chat
        List<Object> messages = createMessages(userMessage);
        
        // Enviar para o provedor selecionado
        switch (provider.toLowerCase()) {
            case "openai":
                sendToOpenAI(messages, callback);
                break;
            case "claude":
                sendToClaude(messages, callback);
                break;
            case "groq":
                sendToGroq(messages, callback);
                break;
            default:
                // Fallback para Groq se nenhum provedor estiver configurado
                sendToGroq(messages, callback);
                break;
        }
    }
    
    private String getSelectedProvider() {
        // Verificar se há chaves de API configuradas para cada provedor
        String openaiKey = ConfigActivity.DataStore.getInstance().getString(OpenAIClient.SETTINGS_KEY_API_KEY, null);
        String claudeKey = ConfigActivity.DataStore.getInstance().getString(ClaudeClient.SETTINGS_KEY_API_KEY, null);
        String groqKey = ConfigActivity.DataStore.getInstance().getString(GroqClient.SETTINGS_KEY_API_KEY, null);
        
        // Prioridade: OpenAI > Claude > Groq
        if (openaiKey != null && !openaiKey.trim().isEmpty()) {
            return "openai";
        } else if (claudeKey != null && !claudeKey.trim().isEmpty()) {
            return "claude";
        } else if (groqKey != null && !groqKey.trim().isEmpty()) {
            return "groq";
        }
        
        // Fallback para Groq
        return "groq";
    }
    
    private List<Object> createMessages(String userMessage) {
        // Criar mensagens básicas para chat
        // Pode ser expandido para incluir histórico de conversa
        return List.of(
            Map.of("role", "system", "content", "Você é um assistente de IA útil e amigável. Responda de forma clara e concisa em português."),
            Map.of("role", "user", "content", userMessage)
        );
    }
    
    private void sendToOpenAI(List<Object> messages, ChatCallback callback) {
        new Thread(() -> {
            try {
                List<OpenAIClient.Message> openAIMessages = convertToOpenAIMessages(messages);
                String response = openAIClient.chat(openAIMessages);
                callback.onSuccess(response, "openai");
            } catch (IOException | JSONException e) {
                callback.onError("Erro OpenAI: " + e.getMessage());
            }
        }).start();
    }
    
    private void sendToClaude(List<Object> messages, ChatCallback callback) {
        new Thread(() -> {
            try {
                List<ClaudeClient.Message> claudeMessages = convertToClaudeMessages(messages);
                String response = claudeClient.chat(claudeMessages);
                callback.onSuccess(response, "claude");
            } catch (IOException | JSONException e) {
                callback.onError("Erro Claude: " + e.getMessage());
            }
        }).start();
    }
    
    private void sendToGroq(List<Object> messages, ChatCallback callback) {
        new Thread(() -> {
            try {
                List<GroqClient.Message> groqMessages = convertToGroqMessages(messages);
                String response = groqClient.chat(groqMessages);
                callback.onSuccess(response, "groq");
            } catch (IOException | JSONException e) {
                callback.onError("Erro Groq: " + e.getMessage());
            }
        }).start();
    }
    
    private List<OpenAIClient.Message> convertToOpenAIMessages(List<Object> messages) {
        List<OpenAIClient.Message> result = new ArrayList<>();
        for (Object msg : messages) {
            if (msg instanceof Map) {
                Map<String, String> msgMap = (Map<String, String>) msg;
                result.add(new OpenAIClient.Message(msgMap.get("role"), msgMap.get("content")));
            }
        }
        return result;
    }
    
    private List<ClaudeClient.Message> convertToClaudeMessages(List<Object> messages) {
        List<ClaudeClient.Message> result = new ArrayList<>();
        for (Object msg : messages) {
            if (msg instanceof Map) {
                Map<String, String> msgMap = (Map<String, String>) msg;
                result.add(new ClaudeClient.Message(msgMap.get("role"), msgMap.get("content")));
            }
        }
        return result;
    }
    
    private List<GroqClient.Message> convertToGroqMessages(List<Object> messages) {
        List<GroqClient.Message> result = new ArrayList<>();
        for (Object msg : messages) {
            if (msg instanceof Map) {
                Map<String, String> msgMap = (Map<String, String>) msg;
                result.add(new GroqClient.Message(msgMap.get("role"), msgMap.get("content")));
            }
        }
        return result;
    }
    
    public boolean hasAnyApiKey() {
        String openaiKey = ConfigActivity.DataStore.getInstance().getString(OpenAIClient.SETTINGS_KEY_API_KEY, null);
        String claudeKey = ConfigActivity.DataStore.getInstance().getString(ClaudeClient.SETTINGS_KEY_API_KEY, null);
        String groqKey = ConfigActivity.DataStore.getInstance().getString(GroqClient.SETTINGS_KEY_API_KEY, null);
        
        return (openaiKey != null && !openaiKey.trim().isEmpty()) ||
               (claudeKey != null && !claudeKey.trim().isEmpty()) ||
               (groqKey != null && !groqKey.trim().isEmpty());
    }
    
    public String getCurrentProvider() {
        return getSelectedProvider();
    }
    
    public boolean hasOpenAiKey() {
        String key = ConfigActivity.DataStore.getInstance().getString(OpenAIClient.SETTINGS_KEY_API_KEY, null);
        return key != null && !key.trim().isEmpty();
    }
    
    public boolean hasClaudeKey() {
        String key = ConfigActivity.DataStore.getInstance().getString(ClaudeClient.SETTINGS_KEY_API_KEY, null);
        return key != null && !key.trim().isEmpty();
    }
    
    public boolean hasGroqKey() {
        String key = ConfigActivity.DataStore.getInstance().getString(GroqClient.SETTINGS_KEY_API_KEY, null);
        return key != null && !key.trim().isEmpty();
    }
}
