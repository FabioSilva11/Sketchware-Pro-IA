package pro.sketchware.activities.main.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ChatBotFragment extends Fragment {

    private static final String TELEGRAM_URL = "https://t.me/sketcware_ia";

    public ChatBotFragment() {
        // Default empty constructor
    }

    public static ChatBotFragment newInstance() {
        return new ChatBotFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openTelegram();
    }

    private void openTelegram() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_URL));
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(requireContext(), "No app can open Telegram link", Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Toast.makeText(requireContext(), "Failed to open Telegram", Toast.LENGTH_SHORT).show();
        }
    }
}


