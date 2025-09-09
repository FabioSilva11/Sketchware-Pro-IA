package pro.sketchware.ai;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.besome.sketch.beans.ViewBean;
import mod.agus.jcoderz.beans.ViewBeans;

/**
 * High-level helper that asks AI models to generate Android XML layout code
 * constrained to the components available in the Sketchware palette.
 * Supports multiple AI providers: Groq, OpenAI, and Claude.
 */
public final class AiCodeGenerator {

    private final GroqClient groqClient;
    private final OpenAIClient openAIClient;
    private final ClaudeClient claudeClient;

    public AiCodeGenerator() {
        this.groqClient = new GroqClient();
        this.openAIClient = new OpenAIClient();
        this.claudeClient = new ClaudeClient();
    }

    public String generateLayoutXml(@NonNull String userPrompt) throws IOException, JSONException {
        return generateLayoutXml(userPrompt, null, null);
    }

    public String generateLayoutXml(@NonNull String userPrompt, String currentLayoutXml, Map<String, String> stylePreferences) throws IOException, JSONException {
        String system = buildSystemPrompt(currentLayoutXml, stylePreferences);
        
        // Determine which provider to use based on configured model
        String selectedModel = getSelectedModel();
        String provider = getProviderFromModel(selectedModel);
        
        switch (provider) {
            case "openai":
                List<OpenAIClient.Message> openAIMessages = OpenAIClient.Message.of(system, userPrompt);
                return openAIClient.chat(openAIMessages);
                
            case "claude":
                List<ClaudeClient.Message> claudeMessages = ClaudeClient.Message.of(system, userPrompt);
                return claudeClient.chat(claudeMessages);
                
            case "groq":
            default:
                List<GroqClient.Message> groqMessages = GroqClient.Message.of(system, userPrompt);
                return groqClient.chat(groqMessages);
        }
    }

    private String buildSystemPrompt() {
        return buildSystemPrompt(null, null);
    }

    private String buildSystemPrompt(String currentLayoutXml, Map<String, String> stylePreferences) {
        Map<String, List<String>> ctx = LayoutPaletteContext.getSupportedTypes();
        Map<String, List<String>> attrs = LayoutPaletteContext.getAttributesByType();
        Map<String, List<String>> parserSupportedTypes = getViewBeanParserSupportedTypes();
        
        StringJoiner sj = new StringJoiner("\n");
        
        sj.add("You are an assistant that writes Android layout XML for Sketchware Pro.");
        sj.add("Only use the following layouts and widgets supported by the palette.");
        sj.add("Output must be a single Android layout XML snippet, no explanations.");
        sj.add("");
        sj.add("IMPORTANT LAYOUT STRUCTURE:");
        sj.add("ALL generated layouts MUST be wrapped in a LinearLayout with these exact attributes:");
        sj.add("<LinearLayout");
        sj.add("    xmlns:android=\"http://schemas.android.com/apk/res/android\"");
        sj.add("    xmlns:app=\"http://schemas.android.com/apk/res-auto\"");
        sj.add("    xmlns:tools=\"http://schemas.android.com/tools\"");
        sj.add("    android:layout_width=\"match_parent\"");
        sj.add("    android:layout_height=\"match_parent\"");
        sj.add("    android:orientation=\"vertical\" >");
        sj.add("");
        sj.add("    <!-- Your generated content goes here -->");
        sj.add("");
        sj.add("</LinearLayout>");
        sj.add("");
        
        // Adicionar contexto da tela atual se disponível
        if (currentLayoutXml != null && !currentLayoutXml.trim().isEmpty()) {
            sj.add("CURRENT LAYOUT CONTEXT:");
            sj.add("The user is currently editing this layout. Preserve existing elements and IDs when possible:");
            sj.add(currentLayoutXml);
            sj.add("");
            sj.add("IMPORTANT: When modifying the layout, try to preserve existing views and their IDs.");
            sj.add("Only add new elements or modify existing ones as requested by the user.");
            sj.add("");
        }
        
        // Adicionar preferências de estilo se disponíveis
        if (stylePreferences != null && !stylePreferences.isEmpty()) {
            sj.add("STYLE PREFERENCES:");
            sj.add("Maintain these style characteristics throughout the layout:");
            for (Map.Entry<String, String> entry : stylePreferences.entrySet()) {
                sj.add("- " + entry.getKey() + ": " + entry.getValue());
            }
            sj.add("");
        }
        
        sj.add("SUPPORTED LAYOUTS (Palette):");
        for (String l : ctx.get("layouts")) sj.add("- " + l);
        sj.add("");
        sj.add("SUPPORTED WIDGETS (Palette):");
        for (String w : ctx.get("widgets")) sj.add("- " + w);
        sj.add("");
        
        sj.add("PARSER-SUPPORTED TYPES (ViewBeanParser can handle these):");
        sj.add("Layouts:");
        for (String l : parserSupportedTypes.get("layouts")) sj.add("- " + l);
        sj.add("Widgets:");
        for (String w : parserSupportedTypes.get("widgets")) sj.add("- " + w);
        sj.add("");
        
        sj.add("SKETCHWARE PRO COMPATIBLE ATTRIBUTES (100% compatible):");
        for (Map.Entry<String, List<String>> e : attrs.entrySet()) {
            sj.add(e.getKey() + ":");
            for (String a : e.getValue()) sj.add("  - " + a);
        }
        sj.add("");
        sj.add("WIDGET-SPECIFIC PROPERTIES (based on ViewBean structure):");
        sj.add("TextView/EditText/Button: text, textSize, textColor, textStyle, hint, singleLine, lines");
        sj.add("ImageView: src, scaleType, contentDescription, rotation");
        sj.add("CheckBox/Switch/RadioButton: text, checked");
        sj.add("ProgressBar/SeekBar: max, progress, indeterminate, style");
        sj.add("ListView: dividerHeight, choiceMode");
        sj.add("Spinner: prompt");
        sj.add("CalendarView: firstDayOfWeek");
        sj.add("All widgets: enabled, clickable, alpha, translationX/Y, scaleX/Y, background");
        sj.add("");
        sj.add("IMPORTANT: Custom attributes (app:, tools:, etc.) are NOT needed here.");
        sj.add("The system will automatically handle Material Design and other custom attributes.");
        sj.add("Focus on standard Android attributes for basic functionality.");
        sj.add("");
        sj.add("Rules:");
        sj.add("- ALWAYS wrap your output in the LinearLayout structure shown above.");
        sj.add("- Use android:id for each view when relevant.");
        sj.add("- Respect Android XML syntax. Do not include code fences.");
        sj.add("- Use ONLY standard Android attributes (android:*) listed above.");
        sj.add("- DO NOT use app: attributes or custom library attributes.");
        sj.add("- Custom attributes will be handled automatically by the system.");
        sj.add("- Maintain consistent spacing and padding throughout the layout.");
        sj.add("- Use Material Design principles when appropriate.");
        sj.add("- Preserve existing view IDs when modifying layouts.");
        sj.add("- The root LinearLayout must have the exact attributes specified above.");
        sj.add("- PREFER types that are supported by both Palette AND Parser for best compatibility.");
        sj.add("- Use properties that match the ViewBean structure for 100% compatibility.");
        sj.add("");
        sj.add("EXAMPLES OF COMPATIBLE WIDGETS:");
        sj.add("TextView: <TextView android:id=\"@+id/textview1\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:text=\"Hello World\" android:textSize=\"16sp\" android:textColor=\"#000000\" />");
        sj.add("EditText: <EditText android:id=\"@+id/edittext1\" android:layout_width=\"match_parent\" android:layout_height=\"wrap_content\" android:hint=\"Enter text\" android:inputType=\"text\" />");
        sj.add("Button: <Button android:id=\"@+id/button1\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:text=\"Click me\" />");
        sj.add("ImageView: <ImageView android:id=\"@+id/imageview1\" android:layout_width=\"100dp\" android:layout_height=\"100dp\" android:src=\"@drawable/ic_launcher\" android:scaleType=\"centerCrop\" />");
        sj.add("CheckBox: <CheckBox android:id=\"@+id/checkbox1\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:text=\"Check me\" android:checked=\"false\" />");
        sj.add("ProgressBar: <ProgressBar android:id=\"@+id/progressbar1\" android:layout_width=\"match_parent\" android:layout_height=\"wrap_content\" android:max=\"100\" android:progress=\"50\" />");
        
        return sj.toString();
    }
    
    /**
     * Extrai os tipos suportados pelo ViewBeanParser para fornecer contexto mais preciso à AI
     */
    private Map<String, List<String>> getViewBeanParserSupportedTypes() {
        Map<String, List<String>> types = new HashMap<>();
        List<String> layouts = new ArrayList<>();
        List<String> widgets = new ArrayList<>();
        
        // Tipos básicos do ViewBean
        layouts.add("LinearLayout");
        layouts.add("RelativeLayout");
        layouts.add("HorizontalScrollView");
        layouts.add("ScrollView");
        
        widgets.add("Button");
        widgets.add("TextView");
        widgets.add("EditText");
        widgets.add("ImageView");
        widgets.add("WebView");
        widgets.add("ProgressBar");
        widgets.add("ListView");
        widgets.add("Spinner");
        widgets.add("CheckBox");
        widgets.add("Switch");
        widgets.add("SeekBar");
        widgets.add("CalendarView");
        widgets.add("AdView");
        widgets.add("MapView");
        
        // Tipos estendidos do ViewBeans
        widgets.add("RadioButton");
        widgets.add("RatingBar");
        widgets.add("VideoView");
        widgets.add("SearchView");
        widgets.add("AutoCompleteTextView");
        widgets.add("MultiAutoCompleteTextView");
        widgets.add("GridView");
        widgets.add("AnalogClock");
        widgets.add("DatePicker");
        widgets.add("TimePicker");
        widgets.add("DigitalClock");
        widgets.add("ViewPager");
        widgets.add("BadgeView");
        widgets.add("PatternLockView");
        widgets.add("WaveSideBar");
        widgets.add("MaterialButton");
        widgets.add("SignInButton");
        widgets.add("CircleImageView");
        widgets.add("LottieAnimationView");
        widgets.add("YoutubePlayerView");
        widgets.add("OTPView");
        widgets.add("CodeView");
        widgets.add("RecyclerView");
        
        layouts.add("TabLayout");
        layouts.add("BottomNavigationView");
        layouts.add("CardView");
        layouts.add("CollapsingToolbarLayout");
        layouts.add("TextInputLayout");
        layouts.add("SwipeRefreshLayout");
        layouts.add("RadioGroup");
        
        // Tipos especiais que o ViewBeanParser pode mapear
        widgets.add("ImageButton");
        layouts.add("NestedScrollView");
        widgets.add("MaterialSwitch");
        widgets.add("TextInputEditText");
        
        types.put("layouts", layouts);
        types.put("widgets", widgets);
        
        return types;
    }
    
    /**
     * Get the currently selected model from settings (only from enabled providers)
     */
    private String getSelectedModel() {
        var ds = mod.hilal.saif.activities.tools.ConfigActivity.DataStore.getInstance();
        
        // Check each provider's settings (only if enabled)
        boolean groqEnabled = ds.getBoolean("ai-groq-enabled", false);
        boolean openAIEnabled = ds.getBoolean("ai-openai-enabled", false);
        boolean claudeEnabled = ds.getBoolean("ai-claude-enabled", false);
        
        // Return the first enabled provider's model
        if (groqEnabled) {
            String groqModel = ds.getString(GroqClient.SETTINGS_KEY_MODEL, "");
            if (!groqModel.isEmpty()) return groqModel;
        }
        
        if (openAIEnabled) {
            String openAIModel = ds.getString(OpenAIClient.SETTINGS_KEY_MODEL, "");
            if (!openAIModel.isEmpty()) return openAIModel;
        }
        
        if (claudeEnabled) {
            String claudeModel = ds.getString(ClaudeClient.SETTINGS_KEY_MODEL, "");
            if (!claudeModel.isEmpty()) return claudeModel;
        }
        
        // Default to Groq if no enabled providers found
        return "llama-3.3-70b-versatile";
    }
    
    /**
     * Determine provider from model name
     */
    private String getProviderFromModel(String model) {
        if (model.startsWith("gpt-") || model.startsWith("chatgpt-")) {
            return "openai";
        } else if (model.startsWith("claude-")) {
            return "claude";
        } else {
            return "groq";
        }
    }
    
}


