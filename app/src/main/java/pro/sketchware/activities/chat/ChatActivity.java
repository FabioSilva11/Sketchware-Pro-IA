package pro.sketchware.activities.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import android.widget.ImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import pro.sketchware.R;
import pro.sketchware.activities.chat.adapters.ChatAdapter;
import pro.sketchware.activities.chat.models.ChatMessage;
import pro.sketchware.activities.chat.models.ChatSession;
import pro.sketchware.activities.chat.managers.ChatManager;
import pro.sketchware.activities.chat.managers.SketchwareFileManager;
import pro.sketchware.utility.UI;

public class ChatActivity extends AppCompatActivity {
    
    private MaterialToolbar toolbar;
    private RecyclerView messagesRecyclerView;
    private TextInputLayout messageInputLayout;
    private TextInputEditText messageInput;
    private ImageView sendButton;
    private ImageView attachButton;
    
    private ChatAdapter chatAdapter;
    private ChatManager chatManager;
    private SketchwareFileManager fileManager;
    private ChatSession currentSession;
    private List<ChatMessage> messages;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupInputHandlers();
        initializeManagers();
        loadChatSession();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        messageInputLayout = findViewById(R.id.message_input_layout);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        attachButton = findViewById(R.id.attach_button);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("AI Chat");
        }
    }
    
    private void setupRecyclerView() {
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages, this);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(chatAdapter);
    }
    
    private void setupInputHandlers() {
        sendButton.setOnClickListener(v -> sendMessage());
        
        attachButton.setOnClickListener(v -> showAttachOptions());
        
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }
    
    private void initializeManagers() {
        chatManager = new ChatManager(this);
        fileManager = new SketchwareFileManager(this);
    }
    
    private void loadChatSession() {
        String sessionId = getIntent().getStringExtra("session_id");
        if (sessionId != null) {
            currentSession = chatManager.loadSession(sessionId);
            if (currentSession != null) {
                messages.addAll(currentSession.getMessages());
                chatAdapter.notifyDataSetChanged();
                scrollToBottom();
            }
        } else {
            currentSession = chatManager.createNewSession();
        }
    }
    
    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }
        
        // Adicionar mensagem do usuário
        ChatMessage userMessage = new ChatMessage(
            ChatMessage.TYPE_USER,
            messageText,
            System.currentTimeMillis()
        );
        messages.add(userMessage);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
        
        // Limpar input
        messageInput.setText("");
        
        // Enviar para IA
        sendToAI(messageText, userMessage);
    }
    
    private void sendToAI(String message, ChatMessage userMessage) {
        // Mostrar indicador de carregamento
        ChatMessage loadingMessage = new ChatMessage(
            ChatMessage.TYPE_AI,
            "Thinking...",
            System.currentTimeMillis()
        );
        loadingMessage.setLoading(true);
        messages.add(loadingMessage);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
        
        // Enviar para IA
        chatManager.sendMessage(message, currentSession, new ChatManager.ChatCallback() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(() -> {
                    // Remover mensagem de carregamento
                    messages.remove(loadingMessage);
                    
                    // Adicionar resposta da IA
                    ChatMessage aiMessage = new ChatMessage(
                        ChatMessage.TYPE_AI,
                        response,
                        System.currentTimeMillis()
                    );
                    messages.add(aiMessage);
                    
                    // Salvar no histórico
                    currentSession.addMessage(userMessage);
                    currentSession.addMessage(aiMessage);
                    chatManager.saveSession(currentSession);
                    
                    chatAdapter.notifyDataSetChanged();
                    scrollToBottom();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Remover mensagem de carregamento
                    messages.remove(loadingMessage);
                    
                    // Adicionar mensagem de erro
                    ChatMessage errorMessage = new ChatMessage(
                        ChatMessage.TYPE_AI,
                        "Error: " + error,
                        System.currentTimeMillis()
                    );
                    errorMessage.setError(true);
                    messages.add(errorMessage);
                    
                    chatAdapter.notifyDataSetChanged();
                    scrollToBottom();
                });
            }
        });
    }
    
    private void showAttachOptions() {
        // Mostrar opções de anexo (arquivos .sketchware, imagens, etc.)
        fileManager.showAttachOptions(new SketchwareFileManager.AttachCallback() {
            @Override
            public void onFileSelected(String filePath, String fileName) {
                // Adicionar arquivo anexado à conversa
                ChatMessage fileMessage = new ChatMessage(
                    ChatMessage.TYPE_USER,
                    "📎 " + fileName,
                    System.currentTimeMillis()
                );
                fileMessage.setAttachment(filePath);
                messages.add(fileMessage);
                chatAdapter.notifyItemInserted(messages.size() - 1);
                scrollToBottom();
            }
        });
    }
    
    private void scrollToBottom() {
        if (messagesRecyclerView.getAdapter() != null) {
            messagesRecyclerView.post(() -> {
                int itemCount = messagesRecyclerView.getAdapter().getItemCount();
                if (itemCount > 0) {
                    messagesRecyclerView.smoothScrollToPosition(itemCount - 1);
                }
            });
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_clear_chat) {
            clearChat();
            return true;
        } else if (id == R.id.action_export_chat) {
            exportChat();
            return true;
        } else if (id == R.id.action_settings) {
            openSettings();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void clearChat() {
        messages.clear();
        chatAdapter.notifyDataSetChanged();
        if (currentSession != null) {
            currentSession.clearMessages();
            chatManager.saveSession(currentSession);
        }
    }
    
    private void exportChat() {
        if (currentSession != null) {
            fileManager.exportChat(currentSession);
            Toast.makeText(this, "Chat exported successfully", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openSettings() {
        // Abrir configurações de IA
        Intent intent = new Intent(this, ChatSettingsActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentSession != null) {
            chatManager.saveSession(currentSession);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (fileManager != null) {
            fileManager.handleActivityResult(requestCode, resultCode, data);
        }
    }
}
