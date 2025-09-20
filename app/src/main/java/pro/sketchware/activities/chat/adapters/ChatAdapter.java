package pro.sketchware.activities.chat.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pro.sketchware.R;
import pro.sketchware.activities.chat.models.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_AI = 1;
    
    private List<ChatMessage> messages;
    private Context context;
    private SimpleDateFormat timeFormat;
    
    public ChatAdapter(List<ChatMessage> messages, Context context) {
        this.messages = messages;
        this.context = context;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        return message.getType() == ChatMessage.TYPE_USER ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_ai, parent, false);
            return new AIMessageViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof AIMessageViewHolder) {
            ((AIMessageViewHolder) holder).bind(message);
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    private class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView messageTime;
        
        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
        }
        
        public void bind(ChatMessage message) {
            messageText.setText(message.getContent());
            messageTime.setText(timeFormat.format(new Date(message.getTimestamp())));
        }
    }
    
    private class AIMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView messageTime;
        private MaterialButton copyButton;
        
        public AIMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
            copyButton = itemView.findViewById(R.id.copy_button);
            
            copyButton.setOnClickListener(v -> copyToClipboard(messageText.getText().toString()));
        }
        
        public void bind(ChatMessage message) {
            messageText.setText(message.getContent());
            messageTime.setText(timeFormat.format(new Date(message.getTimestamp())));
            
            if (message.isLoading()) {
                messageText.setText("Thinking...");
                copyButton.setVisibility(View.GONE);
            } else if (message.isError()) {
                messageText.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                copyButton.setVisibility(View.VISIBLE);
            } else {
                messageText.setTextColor(context.getResources().getColor(android.R.color.black));
                copyButton.setVisibility(View.VISIBLE);
            }
        }
        
        private void copyToClipboard(String text) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("AI Response", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }
}
