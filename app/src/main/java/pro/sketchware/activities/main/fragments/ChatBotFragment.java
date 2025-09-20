package pro.sketchware.activities.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import pro.sketchware.R;
import pro.sketchware.activities.chat.ChatActivity;
import pro.sketchware.activities.chat.adapters.ChatHistoryAdapter;
import pro.sketchware.activities.chat.managers.ChatManager;
import pro.sketchware.activities.chat.models.ChatSession;

public class ChatBotFragment extends Fragment implements ChatHistoryAdapter.OnChatSessionClickListener {

    private RecyclerView chatHistoryRecyclerView;
    private FloatingActionButton newChatButton;
    private ChatHistoryAdapter chatHistoryAdapter;
    private ChatManager chatManager;
    private List<ChatSession> chatSessions;

    public ChatBotFragment() {
        // Default empty constructor
    }

    public static ChatBotFragment newInstance() {
        return new ChatBotFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatManager = new ChatManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadChatHistory();
        
        return view;
    }

    private void initializeViews(View view) {
        chatHistoryRecyclerView = view.findViewById(R.id.chat_history_recycler_view);
        newChatButton = view.findViewById(R.id.new_chat_button);
    }

    private void setupRecyclerView() {
        chatSessions = chatManager.getAllSessions();
        chatHistoryAdapter = new ChatHistoryAdapter(chatSessions, this);
        chatHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatHistoryRecyclerView.setAdapter(chatHistoryAdapter);
    }

    private void setupClickListeners() {
        newChatButton.setOnClickListener(v -> startNewChat());
        
        chatHistoryAdapter.setOnChatSessionClickListener(session -> openChatSession(session));
    }

    private void startNewChat() {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        startActivity(intent);
    }

    private void openChatSession(ChatSession session) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("session_id", session.getId());
        startActivity(intent);
    }

    private void loadChatHistory() {
        chatSessions.clear();
        chatSessions.addAll(chatManager.getAllSessions());
        chatHistoryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recarregar histórico quando voltar para o fragment
        loadChatHistory();
    }
    
    @Override
    public void onChatSessionClick(ChatSession session) {
        openChatSession(session);
    }
}


