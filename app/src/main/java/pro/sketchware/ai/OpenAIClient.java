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

public final class OpenAIClient {

    public static final String SETTINGS_KEY_API_KEY = "ai-openai-api-key";
    public static final String SETTINGS_KEY_MODEL = "ai-openai-model";

    private static final String DEFAULT_MODEL = "gpt-5";
    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient;

    public OpenAIClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public String chat(@NonNull List<Message> messages) throws IOException, JSONException {
        return chat(messages, null);
    }

    public String chat(@NonNull List<Message> messages, String imageBase64) throws IOException, JSONException {
        String apiKey = ConfigActivity.DataStore.getInstance().getString(SETTINGS_KEY_API_KEY, null);
        if (TextUtils.isEmpty(apiKey)) {
            throw new IOException("Missing OpenAI API key in settings");
        }

        String model = ConfigActivity.DataStore.getInstance().getString(SETTINGS_KEY_MODEL, DEFAULT_MODEL);

        JSONObject payload = new JSONObject();
        payload.put("model", model);

        JSONArray msgs = new JSONArray();
        for (Message m : messages) {
            JSONObject j = new JSONObject();
            j.put("role", m.role);
            j.put("content", m.content);
            
            // Add image support for vision models
            if (imageBase64 != null && !imageBase64.isEmpty() && m.role.equals("user")) {
                JSONArray contentArray = new JSONArray();
                
                // Add text content
                JSONObject textContent = new JSONObject();
                textContent.put("type", "text");
                textContent.put("text", m.content);
                contentArray.put(textContent);
                
                // Add image content
                JSONObject imageContent = new JSONObject();
                imageContent.put("type", "image_url");
                JSONObject imageUrl = new JSONObject();
                imageUrl.put("url", "data:image/jpeg;base64," + imageBase64);
                imageContent.put("image_url", imageUrl);
                contentArray.put(imageContent);
                
                j.put("content", contentArray);
            }
            
            msgs.put(j);
        }
        payload.put("messages", msgs);

        Request req = new Request.Builder()
                .url(ENDPOINT)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();

        try (Response resp = httpClient.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                String errorBody = resp.body() != null ? resp.body().string() : null;
                throw new IOException("OpenAI API error: HTTP " + resp.code() + (errorBody != null ? " - " + errorBody : ""));
            }
            String body = resp.body() != null ? resp.body().string() : null;
            if (body == null) return null;

            JSONObject json = new JSONObject(body);
            JSONArray choices = json.optJSONArray("choices");
            if (choices == null || choices.length() == 0) return null;
            JSONObject first = choices.getJSONObject(0);
            JSONObject message = first.optJSONObject("message");
            return message != null ? message.optString("content", null) : null;
        }
    }

    public boolean testConnection() throws IOException, JSONException {
        List<Message> testMessages = Message.of(
                "You are a helpful assistant. Respond with 'OK' if you receive this message.",
                "Test message"
        );
        
        String response = chat(testMessages);
        return response != null && response.contains("OK");
    }

    public static String imageToBase64(byte[] imageBytes) {
        if (imageBytes == null) return null;
        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP);
    }

    public static final class Message {
        public final String role;
        public final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public static List<Message> of(String systemPrompt, String userPrompt) {
            List<Message> messages = new ArrayList<>();
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                messages.add(new Message("system", systemPrompt));
            }
            messages.add(new Message("user", userPrompt));
            return messages;
        }

        public static List<Message> of(String userPrompt) {
            List<Message> messages = new ArrayList<>();
            messages.add(new Message("user", userPrompt));
            return messages;
        }
    }
}
