package pro.sketchware.activities.main.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pro.sketchware.R;
import pro.sketchware.activities.main.adapters.ChatMessageAdapter;
import pro.sketchware.ai.ChatAiManager;

import io.noties.markwon.Markwon;

public class ChatBotFragment extends Fragment {
    
    private RecyclerView recyclerMessages;
    private EditText editMessage;
    private ImageButton buttonSend;
    private Spinner spinnerAiProvider;
    private TextView textAiStatus;
    private LinearLayout inputBarContainer;
    private ChatMessageAdapter messageAdapter;
    private ChatAiManager aiManager;
    private boolean isProcessing = false;

    public ChatBotFragment() {
        // Default empty constructor
    }

    public static ChatBotFragment newInstance() {
        return new ChatBotFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aiManager = new ChatAiManager();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupAiProviderSpinner();
        setupListeners();
        setupSystemWindowInsets();
        
        // Adicionar mensagem de boas-vindas
        addWelcomeMessage();
        
        return view;
    }
    
    private void initViews(View view) {
        recyclerMessages = view.findViewById(R.id.recycler_messages);
        editMessage = view.findViewById(R.id.edit_message);
        buttonSend = view.findViewById(R.id.button_send);
        spinnerAiProvider = view.findViewById(R.id.spinner_ai_provider);
        inputBarContainer = view.findViewById(R.id.input_bar_container);
    }
    
    private void setupRecyclerView() {
        messageAdapter = new ChatMessageAdapter();
        
        // Inicializar Markwon para formatação de markdown
        Markwon markwon = Markwon.create(getContext());
        messageAdapter.setMarkwon(markwon);
        
        recyclerMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMessages.setAdapter(messageAdapter);
    }
    
    private void setupAiProviderSpinner() {
        String[] providers = {"OpenAI (GPT)", "Claude", "Groq"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, providers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAiProvider.setAdapter(adapter);
        
        // Selecionar o provedor atual
        String currentProvider = aiManager.getCurrentProvider();
        int position = 0;
        switch (currentProvider.toLowerCase()) {
            case "openai":
                position = 0;
                break;
            case "claude":
                position = 1;
                break;
            case "groq":
                position = 2;
                break;
        }
        spinnerAiProvider.setSelection(position);
        
        // Listener para mudança de provedor
        spinnerAiProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedProvider = providers[position].split(" ")[0].toLowerCase();
                updateAiStatus(selectedProvider);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Não fazer nada
            }
        });
        
        // Atualizar status inicial
        updateAiStatus(currentProvider);
    }
    
    private void updateAiStatus(String provider) {
        boolean hasApiKey = false;
        
        switch (provider.toLowerCase()) {
            case "openai":
                hasApiKey = aiManager.hasOpenAiKey();
                break;
            case "claude":
                hasApiKey = aiManager.hasClaudeKey();
                break;
            case "groq":
                hasApiKey = aiManager.hasGroqKey();
                break;
        }
        

    }
    
    private void setupListeners() {
        buttonSend.setOnClickListener(v -> sendMessage());
        
        editMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                sendMessage();
                return true;
            }
            return false;
        });
    }
    
    private void setupSystemWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(inputBarContainer, (v, insets) -> {
            // Obter a altura dos botões de navegação
            int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            
            // Adicionar padding inferior para ficar acima dos botões de navegação
            v.setPadding(
                v.getPaddingLeft(),
                v.getPaddingTop(),
                v.getPaddingRight(),
                Math.max(16, navigationBarHeight + 8) // Mínimo de 16dp, ou altura dos botões + 8dp
            );
            
            return insets;
        });
    }
    
    private void addWelcomeMessage() {
        String welcomeText = "Olá! Sou seu assistente de IA. Como posso ajudá-lo hoje?";
        messageAdapter.addAiMessage(welcomeText, aiManager.getCurrentProvider());
    }
    
    private void sendMessage() {
        if (isProcessing) {
            return;
        }
        
        String message = editMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        
        // Obter provedor selecionado
        String selectedProvider = getSelectedProvider();
        
        // Verificar se há chave de API configurada para o provedor selecionado
        boolean hasApiKey = false;
        switch (selectedProvider.toLowerCase()) {
            case "openai":
                hasApiKey = aiManager.hasOpenAiKey();
                break;
            case "claude":
                hasApiKey = aiManager.hasClaudeKey();
                break;
            case "groq":
                hasApiKey = aiManager.hasGroqKey();
                break;
        }
        
        if (!hasApiKey) {
            Toast.makeText(getContext(), "Configure uma chave de API para " + selectedProvider + " nas configurações primeiro", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Adicionar mensagem do usuário
        messageAdapter.addUserMessage(message);
        editMessage.setText("");
        
        // Mostrar indicador de processamento
        isProcessing = true;
        buttonSend.setEnabled(false);
        buttonSend.setAlpha(0.5f);
        
        // Adicionar mensagem de "digitando..."
        messageAdapter.addAiMessage("Digitando...", selectedProvider);
        scrollToBottom();
        
        // Enviar para IA
        aiManager.sendMessage(message, selectedProvider, new ChatAiManager.ChatCallback() {
            @Override
            public void onSuccess(String response, String provider) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Atualizar a mensagem de "digitando..." com a resposta real
                        messageAdapter.updateLastMessage(response, provider);
                        
                        // Restaurar estado
                        isProcessing = false;
                        buttonSend.setEnabled(true);
                        buttonSend.setAlpha(1.0f);
                        
                        scrollToBottom();
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Atualizar a mensagem de "digitando..." com a mensagem de erro
                        messageAdapter.updateLastMessage("Desculpe, ocorreu um erro: " + error, selectedProvider);
                        
                        // Restaurar estado
                        isProcessing = false;
                        buttonSend.setEnabled(true);
                        buttonSend.setAlpha(1.0f);
                        
                        scrollToBottom();
                        
                        Toast.makeText(getContext(), "Erro: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
    
    private String getSelectedProvider() {
        String selected = (String) spinnerAiProvider.getSelectedItem();
        if (selected != null) {
            return selected.split(" ")[0].toLowerCase();
        }
        return "groq"; // fallback
    }
    
    private void scrollToBottom() {
        if (recyclerMessages.getAdapter() != null) {
            int itemCount = recyclerMessages.getAdapter().getItemCount();
            if (itemCount > 0) {
                recyclerMessages.smoothScrollToPosition(itemCount - 1);
            }
        }
    }
}


