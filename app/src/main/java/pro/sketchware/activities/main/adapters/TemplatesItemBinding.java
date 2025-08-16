package pro.sketchware.activities.main.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import pro.sketchware.R;

public class TemplatesItemBinding {
    private final View rootView;
    public final ImageView imgIcon;
    public final TextView projectName;
    public final TextView appName;
    public final TextView packageName;

    private TemplatesItemBinding(@NonNull View rootView) {
        this.rootView = rootView;
        this.imgIcon = rootView.findViewById(R.id.img_icon);
        this.projectName = rootView.findViewById(R.id.project_name);
        this.appName = rootView.findViewById(R.id.app_name);
        this.packageName = rootView.findViewById(R.id.package_name);
    }

    @NonNull
    public View getRoot() {
        return rootView;
    }

    @NonNull
    public static TemplatesItemBinding inflate(@NonNull LayoutInflater inflater, ViewGroup parent, boolean attachToParent) {
        View root = inflater.inflate(R.layout.itens_templates, parent, attachToParent);
        return new TemplatesItemBinding(root);
    }

    @NonNull
    public static TemplatesItemBinding bind(@NonNull View rootView) {
        return new TemplatesItemBinding(rootView);
    }
}
