package pro.sketchware.ai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Exemplo de uso do AiBlockCodeGenerator com o novo prompt em português.
 * Demonstra como gerar código para diferentes tipos de blocos do Sketchware Pro.
 */
public class BlockCodeGeneratorExample {

    public static void main(String[] args) {
        AiBlockCodeGenerator generator = new AiBlockCodeGenerator();
        
        // Exemplo 1: Bloco de verificação de conexão
        testConnectionBlock(generator);
        
        // Exemplo 2: Bloco de verificação de arquivo
        testFileCheckBlock(generator);
        
        // Exemplo 3: Bloco de Toast
        testToastBlock(generator);
        
        // Exemplo 4: Bloco de loop while
        testWhileLoopBlock(generator);
        
        // Exemplo 5: Bloco de retorno de string
        testStringReturnBlock(generator);
    }
    
    private static void testConnectionBlock(AiBlockCodeGenerator generator) {
        try {
            List<String> imports = new ArrayList<>();
            imports.add("android.content.Context");
            imports.add("android.net.ConnectivityManager");
            
            var request = new AiBlockCodeGenerator.BlockGenerationRequest(
                "Verificar Conexão",
                "e", // If-Else
                "if_else",
                "Verificar se o dispositivo está conectado à internet",
                "Executar código se conectado",
                "Verifica se há conexão com a internet usando ConnectivityManager",
                imports
            );
            
            var result = generator.generateBlockCode(request);
            System.out.println("=== BLOCO DE VERIFICAÇÃO DE CONEXÃO ===");
            System.out.println(result.code);
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar bloco de conexão: " + e.getMessage());
        }
    }
    
    private static void testFileCheckBlock(AiBlockCodeGenerator generator) {
        try {
            var request = new AiBlockCodeGenerator.BlockGenerationRequest(
                "Arquivo Existe",
                "e", // If-Else
                "if_else",
                "Verificar se arquivo existe",
                "Executar se arquivo existe",
                "Verifica se um arquivo existe no sistema de arquivos",
                null
            );
            
            var result = generator.generateBlockCode(request);
            System.out.println("=== BLOCO DE VERIFICAÇÃO DE ARQUIVO ===");
            System.out.println(result.code);
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar bloco de arquivo: " + e.getMessage());
        }
    }
    
    private static void testToastBlock(AiBlockCodeGenerator generator) {
        try {
            var request = new AiBlockCodeGenerator.BlockGenerationRequest(
                "Mostrar Toast",
                "regular", // Regular
                "regular",
                "Mostrar mensagem toast",
                "",
                "Exibe uma mensagem toast na tela",
                null
            );
            
            var result = generator.generateBlockCode(request);
            System.out.println("=== BLOCO DE TOAST ===");
            System.out.println(result.code);
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar bloco de toast: " + e.getMessage());
        }
    }
    
    private static void testWhileLoopBlock(AiBlockCodeGenerator generator) {
        try {
            var request = new AiBlockCodeGenerator.BlockGenerationRequest(
                "Loop While",
                "c", // Conditional
                "while",
                "Executar enquanto condição for verdadeira",
                "",
                "Executa um loop while baseado em uma condição booleana",
                null
            );
            
            var result = generator.generateBlockCode(request);
            System.out.println("=== BLOCO DE LOOP WHILE ===");
            System.out.println(result.code);
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar bloco de loop: " + e.getMessage());
        }
    }
    
    private static void testStringReturnBlock(AiBlockCodeGenerator generator) {
        try {
            var request = new AiBlockCodeGenerator.BlockGenerationRequest(
                "Retornar String",
                "s", // String return
                "string",
                "Retornar uma string",
                "",
                "Retorna uma string baseada nos parâmetros fornecidos",
                null
            );
            
            var result = generator.generateBlockCode(request);
            System.out.println("=== BLOCO DE RETORNO DE STRING ===");
            System.out.println(result.code);
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar bloco de string: " + e.getMessage());
        }
    }
}
