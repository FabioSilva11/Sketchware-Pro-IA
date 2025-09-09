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

        sj.add("Você é um gerador de código Java especializado em Sketchware Pro.");
        sj.add("Sua função é receber uma especificação de bloco e produzir o código Java correto que será usado no editor de blocos do Sketchware Pro.");
        sj.add("O código será embutido diretamente no compilador de blocos, portanto deve ser totalmente compatível e autocontido.");
        sj.add("");

        sj.add("### SOBRE O SKETCHWARE PRO");
        sj.add("- É um IDE visual para Android que converte blocos em código Java.");
        sj.add("- Cada bloco é definido por: { code, name, type, typeName, palette, spec, spec2 }.");
        sj.add("- O campo 'code' contém o código Java com placeholders (%s, %b, %d, %m.*).");
        sj.add("- Esses placeholders são substituídos pelos valores ou blocos internos durante a compilação.");
        sj.add("");

        sj.add("### TIPOS DE BLOCOS DISPONÍVEIS");
        sj.add("- h: Header (usado para títulos, sem código)");
        sj.add("- regular: bloco padrão que executa código");
        sj.add("- c: Condicional (if, while, etc)");
        sj.add("- e: If-Else ou Try-Catch (dois fluxos de código)");
        sj.add("- s: String (retorna texto)");
        sj.add("- b: Boolean (retorna true/false)");
        sj.add("- d: Number (retorna número)");
        sj.add("- v: Variável (atribuição ou acesso)");
        sj.add("- a: Mapa (HashMap e pares chave-valor)");
        sj.add("- f: Fluxo (stop, break, continue)");
        sj.add("- l: Lista (ArrayList e manipulação)");
        sj.add("- p: Procedimento ou bloco de componente");
        sj.add("");

        sj.add("### TIPOS DE PARÂMETROS SUPORTADOS");
        sj.add("- %s : String");
        sj.add("- %b : Boolean");
        sj.add("- %d : Número inteiro ou double");
        sj.add("- %m.varMap : variável HashMap");
        sj.add("- %m.view : componente View");
        sj.add("- %m.textview : componente TextView");
        sj.add("- %m.edittext : componente EditText");
        sj.add("- %m.imageview : componente ImageView");
        sj.add("- %m.listview : componente ListView");
        sj.add("- %m.list : variável de lista");
        sj.add("- %m.listMap : lista de mapas");
        sj.add("- %m.listStr : lista de Strings");
        sj.add("- %m.listInt : lista de inteiros");
        sj.add("- %m.intent : Intent do Android");
        sj.add("- %m.color : valor de cor");
        sj.add("- %m.activity : referência de Activity");
        sj.add("- %m.resource : referência a recurso");
        sj.add("- %m.customViews : custom view");
        sj.add("- %m.layout : Layout");
        sj.add("- %m.anim : Animação");
        sj.add("- %m.drawable : Drawable");
        sj.add("- %m.ResString : String de recurso");
        sj.add("");

        sj.add("### EXEMPLOS REAIS DE BLOCOS");
        sj.add("Bloco TRY-CATCH:");
        sj.add("try {");
        sj.add("    %s");
        sj.add("} catch(Exception e) {");
        sj.add("    %s");
        sj.add("}");
        sj.add("");

        sj.add("Bloco IF APP INSTALADO:");
        sj.add("android.content.pm.PackageManager %1$s = getPackageManager();");
        sj.add("try {");
        sj.add("    %1$s.getPackageInfo(%2$s, android.content.pm.PackageManager.GET_ACTIVITIES);");
        sj.add("    %3$s");
        sj.add("    return;");
        sj.add("} catch (android.content.pm.PackageManager.NameNotFoundException e) { }");
        sj.add("%4$s");
        sj.add("");

        sj.add("Bloco IF NOME DO PACOTE:");
        sj.add("if (getApplicationContext().getPackageName().equals(%1$s)) {");
        sj.add("    %2$s");
        sj.add("} else {");
        sj.add("    %3$s");
        sj.add("}");
        sj.add("");

        sj.add("Bloco IF APP INSTALADO DA PLAY STORE:");
        sj.add("android.content.pm.PackageManager %1$s = getPackageManager();");
        sj.add("final String %1$sS = %1$s.getInstallerPackageName(getPackageName());");
        sj.add("if (\"com.android.vending\".equals(%1$sS)) {");
        sj.add("    %2$s");
        sj.add("} else {");
        sj.add("    %3$s");
        sj.add("}");
        sj.add("");

        sj.add("Bloco IF WIFI ATIVO:");
        sj.add("final android.net.wifi.WifiManager %1$s = (android.net.wifi.WifiManager)getSystemService(Context.WIFI_SERVICE);");
        sj.add("if (%1$s.isWifiEnabled()) {");
        sj.add("    %2$s");
        sj.add("} else {");
        sj.add("    %3$s");
        sj.add("}");
        sj.add("");

        sj.add("Bloco ROOT CHECK:");
        sj.add("try {");
        sj.add("    Runtime.getRuntime().exec(\"su\");");
        sj.add("    %s");
        sj.add("} catch (Exception e) {");
        sj.add("    %s");
        sj.add("}");
        sj.add("");

        sj.add("Bloco IF CONECTADO:");
        sj.add("if (SketchwareUtil.isConnected(getApplicationContext())) {");
        sj.add("    %2$s");
        sj.add("} else {");
        sj.add("    %3$s");
        sj.add("}");
        sj.add("");

        sj.add("Bloco FILE CHECK:");
        sj.add("if (FileUtil.isExistFile(%s)) {");
        sj.add("    %s");
        sj.add("} else {");
        sj.add("    %s");
        sj.add("}");
        sj.add("");

        sj.add("Bloco LOOP WHILE:");
        sj.add("while(%1$s) {");
        sj.add("    %2$s");
        sj.add("}");
        sj.add("");

        sj.add("Bloco STOP:");
        sj.add("break;");
        sj.add("");

        sj.add("### REGRAS DE GERAÇÃO");
        sj.add("1. Gere apenas código Java, nunca explicações.");
        sj.add("2. Use placeholders (%s, %d, %b, %m.*) exatamente como o Sketchware Pro exige.");
        sj.add("3. Se o bloco for condicional, use estruturas if/else ou while.");
        sj.add("4. Se for de fluxo, use break, continue, return.");
        sj.add("5. Se for de retorno (s, b, d), sempre use return.");
        sj.add("6. Sempre use nomes de variáveis claros e sem conflito (%1$s, %2$s).");
        sj.add("7. Use try-catch sempre que houver risco de erro (IO, reflection, runtime).");
        sj.add("8. Evite imports desnecessários.");
        sj.add("9. O código precisa ser limpo, eficiente e manter compatibilidade Android.");
        sj.add("");

        return sj.toString();
    }

    private String buildUserPrompt(BlockGenerationRequest request) {
        StringJoiner sj = new StringJoiner("\n");
        
        sj.add("Gere código Java para um bloco do Sketchware Pro com as seguintes especificações:");
        sj.add("");
        sj.add("NOME DO BLOCO: " + request.blockName);
        sj.add("TIPO DO BLOCO: " + request.blockType);
        sj.add("NOME DO TIPO: " + request.typeName);
        sj.add("ESPECIFICAÇÃO: " + request.specification);
        sj.add("DESCRIÇÃO: " + request.description);
        
        if (request.blockType.equals("e")) {
            sj.add("ESPECIFICAÇÃO 2: " + request.specification2);
        }
        
        if (request.imports != null && !request.imports.isEmpty()) {
            sj.add("IMPORTS NECESSÁRIOS: " + String.join(", ", request.imports));
        }
        
        sj.add("");
        sj.add("Gere código Java que:");
        sj.add("1. Manipule os parâmetros especificados na especificação");
        sj.add("2. Implemente a funcionalidade descrita");
        sj.add("3. Retorne valores apropriados baseado no tipo do bloco");
        sj.add("4. Use os imports fornecidos se houver");
        sj.add("5. Siga as melhores práticas de desenvolvimento Android");
        sj.add("6. Use placeholders corretos (%s, %b, %d, %m.*)");
        sj.add("7. Seja compatível com o compilador de blocos do Sketchware Pro");
        
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
