package pro.sketchware.activities.chat.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pro.sketchware.R;
import pro.sketchware.activities.chat.models.ChatSession;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ChatSessionViewHolder> {
    
    private List<ChatSession> chatSessions;
    private OnChatSessionClickListener clickListener;
    private SimpleDateFormat dateFormat;
    
    public interface OnChatSessionClickListener {
        void onChatSessionClick(ChatSession session);
    }
    
    public ChatHistoryAdapter(List<ChatSession> chatSessions, OnChatSessionClickListener clickListener) {
        this.chatSessions = chatSessions;
        this.clickListener = clickListener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }
    
    public void setOnChatSessionClickListener(OnChatSessionClickListener listener) {
        this.clickListener = listener;
    }
    
    @NonNull
    @Override
    public ChatSessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_chat_session, parent, false);
        return new ChatSessionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ChatSessionViewHolder holder, int position) {
        ChatSession session = chatSessions.get(position);
        holder.bind(session);
    }
    
    @Override
    public int getItemCount() {
        return chatSessions.size();
    }
    
    class ChatSessionViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText;
        private TextView previewText;
        private TextView timeText;
        private TextView messageCountText;
        
        public ChatSessionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.session_title);
            previewText = itemView.findViewById(R.id.session_preview);
            timeText = itemView.findViewById(R.id.session_time);
            messageCountText = itemView.findViewById(R.id.message_count);
            
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener.onChatSessionClick(chatSessions.get(position));
                    }
                }
            });
        }
        
        public void bind(ChatSession session) {
            titleText.setText(session.getTitle().isEmpty() ? "New Chat" : session.getTitle());
            previewText.setText(session.getLastMessagePreview());
            timeText.setText(dateFormat.format(new Date(session.getUpdatedAt())));
            messageCountText.setText(session.getMessageCount() + " messages");
        }
    }
}
