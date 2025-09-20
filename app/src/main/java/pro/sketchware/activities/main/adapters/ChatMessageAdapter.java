package pro.sketchware.activities.main.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.noties.markwon.Markwon;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pro.sketchware.R;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_USER_MESSAGE = 0;
    private static final int TYPE_AI_MESSAGE = 1;
    
    private final List<ChatMessage> messages = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private Markwon markwon;
    
    public static class ChatMessage {
        public final String content;
        public final long timestamp;
        public final boolean isUser;
        public final String aiProvider;
        
        public ChatMessage(String content, boolean isUser, String aiProvider) {
            this.content = content;
            this.timestamp = System.currentTimeMillis();
            this.isUser = isUser;
            this.aiProvider = aiProvider;
        }
        
        public ChatMessage(String content, boolean isUser) {
            this(content, isUser, null);
        }
    }
    
    public void setMarkwon(Markwon markwon) {
        this.markwon = markwon;
    }
    
    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
    
    public void addUserMessage(String content) {
        addMessage(new ChatMessage(content, true));
    }
    
    public void addAiMessage(String content, String aiProvider) {
        addMessage(new ChatMessage(content, false, aiProvider));
    }
    
    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }
    
    public void removeLastMessage() {
        if (!messages.isEmpty()) {
            int lastIndex = messages.size() - 1;
            messages.remove(lastIndex);
            notifyItemRemoved(lastIndex);
        }
    }
    
    public void updateLastMessage(String newContent, String aiProvider) {
        if (!messages.isEmpty()) {
            int lastIndex = messages.size() - 1;
            ChatMessage lastMessage = messages.get(lastIndex);
            if (!lastMessage.isUser) {
                messages.set(lastIndex, new ChatMessage(newContent, false, aiProvider));
                notifyItemChanged(lastIndex);
            }
        }
    }
    
    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser ? TYPE_USER_MESSAGE : TYPE_AI_MESSAGE;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        if (viewType == TYPE_USER_MESSAGE) {
            View view = inflater.inflate(R.layout.item_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_ai, parent, false);
            return new AiMessageViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof AiMessageViewHolder) {
            ((AiMessageViewHolder) holder).bind(message);
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    private class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView messageTime;
        
        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
        }
        
        public void bind(ChatMessage message) {
            messageText.setText(message.content);
            messageTime.setText(timeFormat.format(new Date(message.timestamp)));
        }
    }
    
    private class AiMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView aiName;
        private final TextView messageText;
        private final TextView messageTime;
        
        public AiMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            aiName = itemView.findViewById(R.id.ai_name);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
        }
        
        public void bind(ChatMessage message) {
            // Usar Markwon para formatação de markdown
            if (markwon != null) {
                markwon.setMarkdown(messageText, message.content);
            } else {
                messageText.setText(message.content);
            }
            messageTime.setText(timeFormat.format(new Date(message.timestamp)));
            
            // Set AI provider name
            if (message.aiProvider != null) {
                switch (message.aiProvider.toLowerCase()) {
                    case "openai":
                        aiName.setText("GPT");
                        break;
                    case "claude":
                        aiName.setText("Claude");
                        break;
                    case "groq":
                        aiName.setText("Groq");
                        break;
                    default:
                        aiName.setText("IA Assistant");
                        break;
                }
            } else {
                aiName.setText("IA Assistant");
            }
        }
    }
}
