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
        String apiKey = ConfigActivity.DataStore.getInstance().getString(SETTINGS_KEY_API_KEY, null);
        if (TextUtils.isEmpty(apiKey)) {
            throw new IOException("Missing Groq API key in settings");
        }

        String model = ConfigActivity.DataStore.getInstance().getString(SETTINGS_KEY_MODEL, DEFAULT_MODEL);

        JSONObject payload = new JSONObject();
        payload.put("model", model);

        JSONArray msgs = new JSONArray();
        for (Message m : messages) {
            JSONObject j = new JSONObject();
            j.put("role", m.role);
            j.put("content", m.content);
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
                throw new IOException("Groq API error: HTTP " + resp.code());
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


