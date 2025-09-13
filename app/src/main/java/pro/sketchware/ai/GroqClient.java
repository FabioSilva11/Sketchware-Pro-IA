package pro.sketchware.ai;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mod.hilal.saif.activities.tools.ConfigActivity;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class GroqClient {

    public static final String SETTINGS_KEY_API_KEY = "ai-groq-api-key";
    public static final String SETTINGS_KEY_MODEL = "ai-groq-model";

    private static final String DEFAULT_MODEL = "llama-3.3-70b-versatile";
    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient;

    public GroqClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public String chat(@NonNull List<Message> messages) throws IOException, JSONException {
        return chatWithRetry(messages, 3); // 3 tentativas
    }
    
    private String chatWithRetry(@NonNull List<Message> messages, int maxRetries) throws IOException, JSONException {
        String apiKey = ConfigActivity.DataStore.getInstance().getString(SETTINGS_KEY_API_KEY, null);
        if (TextUtils.isEmpty(apiKey)) {
            throw new IOException("Missing Groq API key in settings");
        }

        String model = ConfigActivity.DataStore.getInstance().getString(SETTINGS_KEY_MODEL, DEFAULT_MODEL);

        // Validar entrada para evitar erro 400
        if (messages == null || messages.isEmpty()) {
            throw new IOException("Messages cannot be null or empty");
        }

        // Validar tamanho total do conteúdo para evitar erro 400 (limite do Groq)
        int totalContentLength = 0;
        for (Message m : messages) {
            if (m.content != null) {
                totalContentLength += m.content.length();
            }
        }
        
        // Limite aproximado do Groq (ajustar conforme necessário)
        if (totalContentLength > 100000) { // ~100KB
            throw new IOException("Request too large: content exceeds 100KB limit");
        }

        JSONObject payload = new JSONObject();
        payload.put("model", model);
        
        // Adicionar parâmetros para evitar erro 400
        payload.put("temperature", 0.7);
        payload.put("max_tokens", 4000);
        payload.put("top_p", 1.0);

        JSONArray msgs = new JSONArray();
        for (Message m : messages) {
            if (m == null || TextUtils.isEmpty(m.role) || m.content == null) {
                continue; // Pular mensagens inválidas
            }
            
            JSONObject j = new JSONObject();
            j.put("role", m.role.trim());
            j.put("content", m.content.trim());
            msgs.put(j);
        }
        
        if (msgs.length() == 0) {
            throw new IOException("No valid messages to send");
        }
        
        payload.put("messages", msgs);

        Request req = new Request.Builder()
                .url(ENDPOINT)
                .addHeader("Authorization", "Bearer " + apiKey.trim())
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "Sketchware-Pro-IA/1.0")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();

        IOException lastException = null;
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try (Response resp = httpClient.newCall(req).execute()) {
                int responseCode = resp.code();
                
                if (!resp.isSuccessful()) {
                    String errorBody = null;
                    try {
                        if (resp.body() != null) {
                            errorBody = resp.body().string();
                        }
                    } catch (Exception ignored) {}
                    
                    String errorMessage = "Groq API error: HTTP " + responseCode;
                    
                    // Tratamento específico para erro 400
                    if (responseCode == 400) {
                        if (errorBody != null && errorBody.contains("invalid_request_error")) {
                            errorMessage += " - Invalid request format";
                        } else if (errorBody != null && errorBody.contains("content_filter")) {
                            errorMessage += " - Content filtered by safety system";
                        } else if (errorBody != null && errorBody.contains("rate_limit")) {
                            errorMessage += " - Rate limit exceeded";
                        } else {
                            errorMessage += " - Bad request: " + (errorBody != null ? errorBody.substring(0, Math.min(200, errorBody.length())) : "Unknown error");
                        }
                        
                        // Para erro 400, não tentar novamente se for problema de formato
                        if (errorBody != null && (errorBody.contains("invalid_request_error") || errorBody.contains("content_filter"))) {
                            throw new IOException(errorMessage);
                        }
                    } else if (responseCode == 401) {
                        errorMessage += " - Invalid API key";
                        throw new IOException(errorMessage); // Não tentar novamente para erro de autenticação
                    } else if (responseCode == 429) {
                        errorMessage += " - Rate limit exceeded";
                        // Para rate limit, aguardar antes de tentar novamente
                        if (attempt < maxRetries - 1) {
                            try {
                                Thread.sleep(1000 * (attempt + 1)); // Backoff exponencial
                                continue;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new IOException("Request interrupted");
                            }
                        }
                    } else if (responseCode == 500) {
                        errorMessage += " - Server error";
                        // Para erro de servidor, tentar novamente
                        if (attempt < maxRetries - 1) {
                            try {
                                Thread.sleep(1000 * (attempt + 1)); // Backoff exponencial
                                continue;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new IOException("Request interrupted");
                            }
                        }
                    }
                    
                    lastException = new IOException(errorMessage);
                    if (attempt == maxRetries - 1) {
                        throw lastException;
                    }
                    continue;
                }
                
                String body = resp.body() != null ? resp.body().string() : null;
                if (body == null) return null;

                JSONObject json = new JSONObject(body);
                JSONArray choices = json.optJSONArray("choices");
                if (choices == null || choices.length() == 0) return null;
                JSONObject first = choices.getJSONObject(0);
                JSONObject message = first.optJSONObject("message");
                return message != null ? message.optString("content", null) : null;
                
            } catch (IOException e) {
                lastException = e;
                if (attempt < maxRetries - 1) {
                    try {
                        Thread.sleep(1000 * (attempt + 1)); // Backoff exponencial
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Request interrupted");
                    }
                }
            }
        }
        
        if (lastException != null) {
            throw lastException;
        }
        
        throw new IOException("All retry attempts failed");
    }

    public static final class Message {
        public final String role;
        public final String content;

        public Message(@NonNull String role, @NonNull String content) {
            this.role = role;
            this.content = content;
        }

        public static List<Message> of(@NonNull String system, @NonNull String user) {
            List<Message> list = new ArrayList<>();
            list.add(new Message("system", system));
            list.add(new Message("user", user));
            return list;
        }
    }
}


