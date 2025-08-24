package pro.sketchware.activities.main.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirebaseStorageUploader {
    
    private static final String TAG = "FirebaseStorageUploader";
    private final FirebaseStorage storage;
    private final StorageReference storageRef;
    private final ExecutorService executor;
    private long maxFileSize = 50 * 1024 * 1024; // 50MB padrão
    
    public FirebaseStorageUploader() {
        this.storage = FirebaseStorage.getInstance();
        this.storageRef = storage.getReference();
        this.executor = Executors.newCachedThreadPool();
    }
    
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
    
    public void uploadFile(Uri fileUri, Context context, String folder, FileUploadCallback callback) {
        List<Uri> files = new ArrayList<>();
        files.add(fileUri);
        uploadFiles(files, context, folder, callback);
    }
    
    public void uploadFile(File file, String folder, FileUploadCallback callback) {
        List<File> files = new ArrayList<>();
        files.add(file);
        uploadFiles(files, folder, callback);
    }
    
    public void uploadFiles(List<Uri> fileUris, Context context, String folder, FileUploadCallback callback) {
        executor.execute(() -> {
            try {
                List<UploadResult> results = new ArrayList<>();
                
                for (Uri fileUri : fileUris) {
                    UploadResult result = uploadSingleFile(fileUri, context, folder);
                    if (result != null) {
                        results.add(result);
                    }
                }
                
                if (!results.isEmpty()) {
                    callback.onSuccess(results);
                } else {
                    callback.onError("Nenhum arquivo foi enviado com sucesso");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Erro no upload de múltiplos arquivos", e);
                callback.onError("Erro no upload: " + e.getMessage());
            }
        });
    }
    
    public void uploadFiles(List<File> files, String folder, FileUploadCallback callback) {
        executor.execute(() -> {
            try {
                List<UploadResult> results = new ArrayList<>();
                
                for (File file : files) {
                    UploadResult result = uploadSingleFile(file, folder);
                    if (result != null) {
                        results.add(result);
                    }
                }
                
                if (!results.isEmpty()) {
                    callback.onSuccess(results);
                } else {
                    callback.onError("Nenhum arquivo foi enviado com sucesso");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Erro no upload de múltiplos arquivos", e);
                callback.onError("Erro no upload: " + e.getMessage());
            }
        });
    }
    
    private UploadResult uploadSingleFile(Uri fileUri, Context context, String folder) {
        try {
            String fileName = getFileName(context, fileUri);
            String uniqueFileName = generateUniqueFileName(fileName);
            StorageReference fileRef = storageRef.child(folder + "/" + uniqueFileName);
            
            UploadTask uploadTask = fileRef.putFile(fileUri);
            
            // Aguardar o upload
            UploadTask.TaskSnapshot taskSnapshot = Tasks.await(uploadTask);
            
            if (taskSnapshot.getTask().isSuccessful()) {
                // Obter a URL de download
                Uri downloadUrl = Tasks.await(fileRef.getDownloadUrl());
                return new UploadResult(uniqueFileName, downloadUrl.toString());
            } else {
                Log.e(TAG, "Upload falhou para: " + fileUri);
                return null;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao fazer upload do arquivo: " + fileUri, e);
            return null;
        }
    }
    
    private UploadResult uploadSingleFile(File file, String folder) {
        try {
            if (!file.exists()) {
                Log.e(TAG, "Arquivo não existe: " + file.getAbsolutePath());
                return null;
            }
            
            if (file.length() > maxFileSize) {
                Log.e(TAG, "Arquivo muito grande: " + file.length() + " bytes");
                return null;
            }
            
            String uniqueFileName = generateUniqueFileName(file.getName());
            StorageReference fileRef = storageRef.child(folder + "/" + uniqueFileName);
            
            Uri fileUri = Uri.fromFile(file);
            UploadTask uploadTask = fileRef.putFile(fileUri);
            
            // Aguardar o upload
            UploadTask.TaskSnapshot taskSnapshot = Tasks.await(uploadTask);
            
            if (taskSnapshot.getTask().isSuccessful()) {
                // Obter a URL de download
                Uri downloadUrl = Tasks.await(fileRef.getDownloadUrl());
                return new UploadResult(uniqueFileName, downloadUrl.toString());
            } else {
                Log.e(TAG, "Upload falhou para: " + file.getAbsolutePath());
                return null;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao fazer upload do arquivo: " + file.getAbsolutePath(), e);
            return null;
        }
    }
    
    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return baseName + "_" + uniqueId + "." + extension;
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
    
    private String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    
    public interface FileUploadCallback {
        void onSuccess(List<UploadResult> results);
        void onError(String error);
    }
    
    public static class UploadResult {
        private final String filename;
        private final String fileUrl;
        
        public UploadResult(String filename, String fileUrl) {
            this.filename = filename;
            this.fileUrl = fileUrl;
        }
        
        public String getFilename() {
            return filename;
        }
        
        public String getFileUrl() {
            return fileUrl;
        }
    }
}
