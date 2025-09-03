package pro.sketchware.ai;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * High-level helper that asks Groq to generate block code for Sketchware Pro
 * based on the block properties and specifications.
 */
public final class AiBlockCodeGenerator {

    private final GroqClient client;

    public AiBlockCodeGenerator() {
        this.client = new GroqClient();
    }

    public BlockCodeResult generateBlockCode(@NonNull BlockGenerationRequest request) throws IOException, JSONException {
        String system = buildSystemPrompt();
        String userPrompt = buildUserPrompt(request);
        
        List<GroqClient.Message> messages = GroqClient.Message.of(system, userPrompt);
        String response = client.chat(messages);
        
        return parseResponse(response, request);
    }

    private String buildSystemPrompt() {
        StringJoiner sj = new StringJoiner("\n");
        
        sj.add("You are an assistant that generates Java code for Sketchware Pro blocks.");
        sj.add("You will receive a block specification and must generate appropriate Java code.");
        sj.add("The code should be practical, efficient, and follow Android development best practices.");
        sj.add("");
        sj.add("BLOCK TYPES AND THEIR PURPOSES:");
        sj.add("- regular: Standard block that executes code");
        sj.add("- c: Conditional block (if statement)");
        sj.add("- e: If-else block");
        sj.add("- s: String block (returns a string)");
        sj.add("- b: Boolean block (returns true/false)");
        sj.add("- d: Number block (returns a number)");
        sj.add("- v: Variable block (assigns to a variable)");
        sj.add("- a: Map block (works with HashMap)");
        sj.add("- f: Stop block (terminates execution)");
        sj.add("- l: List block (works with ArrayList)");
        sj.add("- p: Component block (works with UI components)");
        sj.add("- h: Header block (for organization)");
        sj.add("");
        sj.add("PARAMETER TYPES:");
        sj.add("- %s: String parameter");
        sj.add("- %b: Boolean parameter");
        sj.add("- %d: Number parameter");
        sj.add("- %m.varMap: Map variable");
        sj.add("- %m.view: View component");
        sj.add("- %m.textview: TextView component");
        sj.add("- %m.edittext: EditText component");
        sj.add("- %m.imageview: ImageView component");
        sj.add("- %m.listview: ListView component");
        sj.add("- %m.list: List variable");
        sj.add("- %m.listMap: List of Maps");
        sj.add("- %m.listStr: List of Strings");
        sj.add("- %m.listInt: List of Numbers");
        sj.add("- %m.intent: Intent object");
        sj.add("- %m.color: Color value");
        sj.add("- %m.activity: Activity reference");
        sj.add("- %m.resource: Resource reference");
        sj.add("- %m.customViews: Custom views");
        sj.add("- %m.layout: Layout reference");
        sj.add("- %m.anim: Animation reference");
        sj.add("- %m.drawable: Drawable reference");
        sj.add("- %m.ResString: String resource");
        sj.add("");
        sj.add("CODE GENERATION RULES:");
        sj.add("- Generate only the Java code, no explanations");
        sj.add("- Use proper variable naming conventions");
        sj.add("- Include necessary null checks");
        sj.add("- Handle exceptions appropriately");
        sj.add("- Use Android best practices");
        sj.add("- Make code readable and maintainable");
        sj.add("- Include proper comments for complex logic");
        sj.add("- Use StringBuilder for string concatenation when appropriate");
        sj.add("- Follow Java coding standards");
        sj.add("");
        sj.add("IMPORTANT:");
        sj.add("- The code will be embedded in a block context");
        sj.add("- Variables are already declared in the scope");
        sj.add("- Focus on the core functionality");
        sj.add("- Don't include class declarations or method signatures");
        sj.add("- The code should be self-contained");
        
        return sj.toString();
    }

    private String buildUserPrompt(BlockGenerationRequest request) {
        StringJoiner sj = new StringJoiner("\n");
        
        sj.add("Generate Java code for a Sketchware Pro block with the following specifications:");
        sj.add("");
        sj.add("BLOCK NAME: " + request.blockName);
        sj.add("BLOCK TYPE: " + request.blockType);
        sj.add("TYPE NAME: " + request.typeName);
        sj.add("SPECIFICATION: " + request.specification);
        sj.add("DESCRIPTION: " + request.description);
        
        if (request.blockType.equals("e")) {
            sj.add("SPECIFICATION 2: " + request.specification2);
        }
        
        if (request.imports != null && !request.imports.isEmpty()) {
            sj.add("REQUIRED IMPORTS: " + String.join(", ", request.imports));
        }
        
        sj.add("");
        sj.add("Please generate appropriate Java code that:");
        sj.add("1. Handles the parameters specified in the specification");
        sj.add("2. Implements the functionality described");
        sj.add("3. Returns appropriate values based on the block type");
        sj.add("4. Uses the provided imports if any");
        sj.add("5. Follows Android development best practices");
        
        return sj.toString();
    }

    private BlockCodeResult parseResponse(String response, BlockGenerationRequest request) {
        // Clean up the response
        String cleanedResponse = response.trim();
        
        // Remove markdown code blocks if present
        if (cleanedResponse.startsWith("```java")) {
            cleanedResponse = cleanedResponse.substring(7);
        }
        if (cleanedResponse.startsWith("```")) {
            cleanedResponse = cleanedResponse.substring(3);
        }
        if (cleanedResponse.endsWith("```")) {
            cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
        }
        
        cleanedResponse = cleanedResponse.trim();
        
        return new BlockCodeResult(cleanedResponse, request);
    }

    public static class BlockGenerationRequest {
        public final String blockName;
        public final String blockType;
        public final String typeName;
        public final String specification;
        public final String specification2;
        public final String description;
        public final List<String> imports;

        public BlockGenerationRequest(String blockName, String blockType, String typeName, 
                                    String specification, String specification2, 
                                    String description, List<String> imports) {
            this.blockName = blockName;
            this.blockType = blockType;
            this.typeName = typeName;
            this.specification = specification;
            this.specification2 = specification2;
            this.description = description;
            this.imports = imports;
        }
    }

    public static class BlockCodeResult {
        public final String code;
        public final BlockGenerationRequest request;
        public final long timestamp;

        public BlockCodeResult(String code, BlockGenerationRequest request) {
            this.code = code;
            this.request = request;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
