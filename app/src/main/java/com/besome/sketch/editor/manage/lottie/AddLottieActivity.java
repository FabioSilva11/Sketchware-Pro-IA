package com.besome.sketch.editor.manage.lottie;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.besome.sketch.lib.base.BaseDialogActivity;
import com.besome.sketch.lib.ui.EasyDeleteEditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import a.a.a.xB;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.FilePathUtil;
import pro.sketchware.R;

public class AddLottieActivity extends BaseDialogActivity implements View.OnClickListener {

    private static final int REQ_PICK_LOTTIE_JSON = 1001;
    private LottieAnimationView lottiePreview;
    private String pickedJsonContent;
    private String sc_id;
    private com.besome.sketch.lib.ui.EasyDeleteEditText edInputView;
    private android.widget.CheckBox chkCollection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Same styling; update texts for Lottie
        e(xB.b().a(this, R.string.design_manager_lottie_title_add_lottie));
        d(xB.b().a(getApplicationContext(), R.string.common_word_save));
        setContentView(R.layout.manage_lottie_add);

        TextView tvAddLottie = findViewById(R.id.tv_add_lottie);
        lottiePreview = findViewById(R.id.lottie_preview);
        edInputView = findViewById(R.id.ed_input);
        chkCollection = findViewById(R.id.chk_collection);
        sc_id = getIntent().getStringExtra("sc_id");
        if (tvAddLottie != null) {
            tvAddLottie.setText(xB.b().a(getApplicationContext(), R.string.design_manager_lottie_title_add_lottie));
            tvAddLottie.setOnClickListener(v -> openJsonPicker());
        }
        if (lottiePreview != null) {
            lottiePreview.setOnClickListener(v -> openJsonPicker());
        }
        if (edInputView != null) {
            edInputView.setHint(xB.b().a(this, R.string.design_manager_lottie_hint_enter_animation_name));
        }
        if (chkCollection != null) {
            chkCollection.setText(xB.b().a(getApplicationContext(), R.string.design_manager_title_add_to_collection));
        }

        r.setOnClickListener(this);
        s.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.common_dialog_cancel_button || id == R.id.cancel_button) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (id == R.id.common_dialog_ok_button) {
            saveJsonToProject();
        }
    }

    private void openJsonPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        // Fallback to any file type if some pickers don't expose application/json
        String[] mimeTypes = new String[]{"application/json", "text/plain"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, xB.b().a(this, R.string.common_word_select)), REQ_PICK_LOTTIE_JSON);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_LOTTIE_JSON && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    String json = readTextFromUri(uri);
                    pickedJsonContent = json;
                    if (lottiePreview != null) {
                        lottiePreview.setAnimationFromJson(json, null);
                        lottiePreview.setRepeatCount(0);
                        lottiePreview.playAnimation();
                    }
                } catch (IOException ignored) {
                    // Silently ignore invalid files for now
                }
            }
        }
    }

    private String readTextFromUri(Uri uri) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
    }

    private void saveJsonToProject() {
        if (pickedJsonContent == null || pickedJsonContent.isEmpty()) {
            // Nothing picked; just finish
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        String name = edInputView != null && edInputView.getEditText() != null ? edInputView.getEditText().getText().toString().trim() : "";

        // clear previous error
        if (edInputView != null && edInputView.getTextInputLayout() != null) {
            edInputView.getTextInputLayout().setError(null);
        }

        // non-empty validation
        if (name.isEmpty()) {
            if (edInputView != null && edInputView.getTextInputLayout() != null) {
                edInputView.getTextInputLayout().setError(xB.b().a(this, R.string.common_message_name_unavailable));
            }
            return;
        }

        // allow only safe filename characters (letters, digits, _, -, .)
        String baseName = name.endsWith(".json") ? name.substring(0, name.length() - 5) : name;
        if (!baseName.matches("[A-Za-z0-9_.-]+")) {
            if (edInputView != null && edInputView.getTextInputLayout() != null) {
                edInputView.getTextInputLayout().setError(xB.b().a(this, R.string.common_message_name_unavailable));
            }
            return;
        }

        String finalName = baseName + ".json";

        // Destination: assets manager path for this project
        String assetsDir = new FilePathUtil().getPathAssets(sc_id);
        FileUtil.makeDir(assetsDir);
        String destPath = assetsDir + File.separator + finalName;

        // duplicate check in project assets
        File[] existing = new File(assetsDir).listFiles((d, n) -> n != null && n.equalsIgnoreCase(finalName));
        if (existing != null && existing.length > 0) {
            if (edInputView != null && edInputView.getTextInputLayout() != null) {
                edInputView.getTextInputLayout().setError(xB.b().a(this, R.string.common_message_name_unavailable));
            }
            return;
        }

        FileUtil.writeFile(destPath, pickedJsonContent);

        // Optionally also save to My Collection: /.sketchware/collection/lottie/<name>
        if (chkCollection != null && chkCollection.isChecked()) {
            String collectionDir = a.a.a.wq.a() + File.separator + "lottie";
            FileUtil.makeDir(collectionDir);
            String collectionPath = collectionDir + File.separator + finalName;

            // duplicate check in collection
            File[] existingInCollection = new File(collectionDir).listFiles((d, n) -> n != null && n.equalsIgnoreCase(finalName));
            if (existingInCollection != null && existingInCollection.length > 0) {
                if (edInputView != null && edInputView.getTextInputLayout() != null) {
                    edInputView.getTextInputLayout().setError(xB.b().a(this, R.string.common_message_name_unavailable));
                }
                return;
            }
            FileUtil.writeFile(collectionPath, pickedJsonContent);
        }

        setResult(RESULT_OK, new Intent().putExtra("saved_path", destPath));
        finish();
    }
}


