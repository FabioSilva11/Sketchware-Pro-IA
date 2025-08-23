package pro.sketchware.activities.main.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class FileUploadAPI {
    
    private static final String TAG = "FileUploadAPI";
    private final String uploadUrl;
    private final OkHttpClient client;
    private final ExecutorService executor;
    private long maxFileSize = 50 * 1024 * 1024; // 50MB padrão
    
    public FileUploadAPI(String uploadUrl) {
        this.uploadUrl = uploadUrl;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.executor = Executors.newCachedThreadPool();
    }
    
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
    
    public void uploadFile(Uri fileUri, Context context, FileUploadCallback callback) {
        List<Uri> files = new ArrayList<>();
        files.add(fileUri);
        uploadFiles(files, context, callback);
    }
    
    public void uploadFile(File file, FileUploadCallback callback) {
        List<File> files = new ArrayList<>();
        files.add(file);
        uploadFiles(files, callback);
    }
    
    public void uploadFiles(List<Uri> fileUris, Context context, FileUploadCallback callback) {
        executor.execute(() -> {
            try {
                List<UploadResult> results = new ArrayList<>();
                
                for (Uri fileUri : fileUris) {
                    UploadResult result = uploadSingleFile(fileUri, context);
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
    
    public void uploadFiles(List<File> files, FileUploadCallback callback) {
        executor.execute(() -> {
            try {
                List<UploadResult> results = new ArrayList<>();
                
                for (File file : files) {
                    UploadResult result = uploadSingleFile(file);
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
    
    private UploadResult uploadSingleFile(Uri fileUri, Context context) {
        try {
            String fileName = getFileName(context, fileUri);
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            
            if (inputStream == null) {
                Log.e(TAG, "Não foi possível abrir o arquivo: " + fileUri);
                return null;
            }
            
            return uploadInputStream(inputStream, fileName);
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao fazer upload do arquivo: " + fileUri, e);
            return null;
        }
    }
    
    private UploadResult uploadSingleFile(File file) {
        try {
            if (!file.exists()) {
                Log.e(TAG, "Arquivo não existe: " + file.getAbsolutePath());
                return null;
            }
            
            if (file.length() > maxFileSize) {
                Log.e(TAG, "Arquivo muito grande: " + file.length() + " bytes");
                return null;
            }
            
            FileInputStream inputStream = new FileInputStream(file);
            return uploadInputStream(inputStream, file.getName());
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao fazer upload do arquivo: " + file.getAbsolutePath(), e);
            return null;
        }
    }
    
    private UploadResult uploadInputStream(InputStream inputStream, String fileName) throws IOException {
        MediaType mediaType = getMediaType(fileName);
        
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName,
                        RequestBody.create(mediaType, readInputStream(inputStream)));
        
        RequestBody requestBody = builder.build();
        
        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                return parseUploadResponse(responseBody, fileName);
            } else {
                Log.e(TAG, "Erro na resposta do servidor: " + response.code());
                return null;
            }
        }
    }
    
    private MediaType getMediaType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return MediaType.parse("image/jpeg");
            case "png":
                return MediaType.parse("image/png");
            case "gif":
                return MediaType.parse("image/gif");
            case "webp":
                return MediaType.parse("image/webp");
            case "apk":
                return MediaType.parse("application/vnd.android.package-archive");
            case "swb":
                return MediaType.parse("application/octet-stream");
            case "zip":
                return MediaType.parse("application/zip");
            default:
                return MediaType.parse("application/octet-stream");
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
    
    private byte[] readInputStream(InputStream inputStream) throws IOException {
        List<Byte> bytes = new ArrayList<>();
        int b;
        while ((b = inputStream.read()) != -1) {
            bytes.add((byte) b);
        }
        
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i);
        }
        
        return result;
    }
    
    private UploadResult parseUploadResponse(String responseBody, String originalFileName) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            
            if (jsonResponse.has("success") && jsonResponse.getBoolean("success")) {
                String fileUrl = jsonResponse.optString("file_url", "");
                String filename = jsonResponse.optString("filename", originalFileName);
                
                return new UploadResult(filename, fileUrl);
            } else {
                String error = jsonResponse.optString("error", "Erro desconhecido no upload");
                Log.e(TAG, "Erro no upload: " + error);
                return null;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao fazer parse da resposta", e);
            return null;
        }
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
