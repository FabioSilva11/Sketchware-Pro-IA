package pro.sketchware.activities.main.fragments.templates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.button.MaterialButton;
import pro.sketchware.R;

public class TemplateUseDialogFragment extends DialogFragment {

    private static final String ARG_SC_ID = "sc_id";
    private String scId;

    public static TemplateUseDialogFragment newInstance(String scId) {
        TemplateUseDialogFragment fragment = new TemplateUseDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SC_ID, scId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            scId = getArguments().getString(ARG_SC_ID);
        }
        
        // Configurar estilo do diálogo
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.template_use_dialog, container, false);
        
        // Configurar o diálogo para tela cheia
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Configurar os botões
        MaterialButton btnCopy = view.findViewById(R.id.btn_copy);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel);
        
        // Ação do botão Copiar
        btnCopy.setOnClickListener(v -> {
            if (getParentFragment() instanceof TemplatesFragment) {
                ((TemplatesFragment) getParentFragment()).useTemplate(scId);
            }
            dismiss();
        });
        
        // Ação do botão Cancelar
        btnCancel.setOnClickListener(v -> dismiss());
    }
}
