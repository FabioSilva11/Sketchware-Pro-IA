package pro.sketchware.ai;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * High-level helper that asks Groq to generate Android XML layout code
 * constrained to the components available in the Sketchware palette.
 */
public final class AiCodeGenerator {

    private final GroqClient client;

    public AiCodeGenerator() {
        this.client = new GroqClient();
    }

    public String generateLayoutXml(@NonNull String userPrompt) throws IOException, JSONException {
        String system = buildSystemPrompt();
        List<GroqClient.Message> messages = GroqClient.Message.of(system, userPrompt);
        return client.chat(messages);
    }

    private String buildSystemPrompt() {
        Map<String, List<String>> ctx = LayoutPaletteContext.getSupportedTypes();
        Map<String, List<String>> attrs = LayoutPaletteContext.getAttributesByType();
        StringJoiner sj = new StringJoiner("\n");
        sj.add("You are an assistant that writes Android layout XML for Sketchware Pro.");
        sj.add("Only use the following layouts and widgets supported by the palette.");
        sj.add("Output must be a single Android layout XML snippet, no explanations.");
        sj.add("");
        sj.add("Layouts:");
        for (String l : ctx.get("layouts")) sj.add("- " + l);
        sj.add("");
        sj.add("Widgets:");
        for (String w : ctx.get("widgets")) sj.add("- " + w);
        sj.add("");
        sj.add("Allowed attributes by type:");
        for (Map.Entry<String, List<String>> e : attrs.entrySet()) {
            sj.add(e.getKey() + ":");
            for (String a : e.getValue()) sj.add("  - " + a);
        }
        sj.add("");
        sj.add("Rules:");
        sj.add("- Use android:id for each view when relevant.");
        sj.add("- Respect Android XML syntax. Do not include code fences.");
        sj.add("- Prefer simple, valid attributes; avoid unsupported libraries.");
        return sj.toString();
    }
}


