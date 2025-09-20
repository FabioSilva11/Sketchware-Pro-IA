package pro.sketchware.activities.chat.managers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pro.sketchware.activities.chat.models.ChatSession;
import pro.sketchware.activities.chat.models.ChatMessage;

public class SketchwareFileManager {
    private Activity activity;
    private AttachCallback callback;
    
    public interface AttachCallback {
        void onFileSelected(String filePath, String fileName);
    }
    
    public SketchwareFileManager(Activity activity) {
        this.activity = activity;
    }
    
    public void showAttachOptions(AttachCallback callback) {
        this.callback = callback;
        String[] options = {
            "Attach .sketchware file",
            "Take photo",
            "Select image"
        };
        
        new AlertDialog.Builder(activity)
            .setTitle("Attach File")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        openFilePicker();
                        break;
                    case 1:
                        // Implementar captura de foto
                        Toast.makeText(activity, "Photo capture not implemented yet", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        // Implementar seleção de imagem
                        Toast.makeText(activity, "Image selection not implemented yet", Toast.LENGTH_SHORT).show();
                        break;
                }
            })
            .show();
    }
    
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/octet-stream"});
        activity.startActivityForResult(Intent.createChooser(intent, "Select .sketchware file"), 1001);
    }
    
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String filePath = uri.getPath();
                String fileName = getFileName(uri);
                if (filePath != null && fileName != null) {
                    // Verificar se é um arquivo .sketchware
                    if (fileName.endsWith(".sketchware")) {
                        // Processar arquivo .sketchware
                        processSketchwareFile(filePath, fileName);
                    } else {
                        Toast.makeText(activity, "Please select a .sketchware file", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    
    private String getFileName(Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();
        
        if (scheme != null && scheme.equals("content")) {
            // Para URIs de conteúdo
            String[] projection = {android.provider.MediaStore.MediaColumns.DISPLAY_NAME};
            android.database.Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } else if (scheme != null && scheme.equals("file")) {
            // Para URIs de arquivo
            fileName = new File(uri.getPath()).getName();
        }
        
        return fileName;
    }
    
    private void processSketchwareFile(String filePath, String fileName) {
        // Aqui você pode implementar a lógica para processar o arquivo .sketchware
        // Por exemplo, extrair informações do projeto, analisar estrutura, etc.
        Toast.makeText(activity, "Processing " + fileName, Toast.LENGTH_SHORT).show();
        
        // Por enquanto, apenas notificar que o arquivo foi selecionado
        // Em uma implementação real, você extrairia informações do projeto
    }
    
    public void exportChat(ChatSession session) {
        try {
            String fileName = "chat_export_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".txt";
            
            File exportDir = new File(activity.getExternalFilesDir(null), "chat_exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            File exportFile = new File(exportDir, fileName);
            FileWriter writer = new FileWriter(exportFile);
            
            writer.write("Chat Export - " + session.getTitle() + "\n");
            writer.write("Created: " + new Date(session.getCreatedAt()) + "\n");
            writer.write("Messages: " + session.getMessageCount() + "\n\n");
            writer.write("=".repeat(50) + "\n\n");
            
            for (ChatMessage message : session.getMessages()) {
                String type = message.getType() == ChatMessage.TYPE_USER ? "User" : "AI";
                writer.write("[" + type + "] " + new Date(message.getTimestamp()) + "\n");
                writer.write(message.getContent() + "\n\n");
            }
            
            writer.close();
            
            Toast.makeText(activity, "Chat exported to: " + exportFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            
        } catch (IOException e) {
            Toast.makeText(activity, "Error exporting chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    public String analyzeSketchwareFile(String filePath) {
        // Implementar análise do arquivo .sketchware
        // Retornar informações sobre o projeto
        return "Sketchware project analysis not implemented yet";
    }
}
