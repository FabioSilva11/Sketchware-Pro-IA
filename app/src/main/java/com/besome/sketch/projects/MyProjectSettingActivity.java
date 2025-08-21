package com.besome.sketch.projects;

import static mod.hey.studios.util.ProjectFile.getDefaultColor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Bundle;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;


import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.besome.sketch.lib.ui.ColorPickerDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import a.a.a.GB;
import a.a.a.MA;
import a.a.a.VB;
import a.a.a.bB;
import a.a.a.lC;
import a.a.a.mB;
import a.a.a.nB;
import a.a.a.oB;
import a.a.a.wB;
import a.a.a.wq;
import a.a.a.yB;
import mod.hey.studios.project.ProjectSettings;
import mod.hey.studios.util.Helper;
import mod.hey.studios.util.ProjectFile;
import mod.hilal.saif.activities.tools.ConfigActivity;
import pro.sketchware.R;
import pro.sketchware.control.VersionDialog;
import pro.sketchware.databinding.MyprojectSettingBinding;
import pro.sketchware.lib.validator.AppNameValidator;
import pro.sketchware.lib.validator.PackageNameValidator;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.SketchwareUtil;
import pro.sketchware.analytics.SketchwareAnalytics;

public class MyProjectSettingActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private final String[] themeColorKeys = {"color_accent", "color_primary", "color_primary_dark", "color_control_highlight", "color_control_normal"};
    private final String[] themeColorLabels = {"colorAccent", "colorPrimary", "colorPrimaryDark", "colorControlHighlight", "colorControlNormal"};
    private final int[] projectThemeColors = new int[themeColorKeys.length];
    public MyprojectSettingBinding binding;
    private PackageNameValidator projectPackageNameValidator;
    private VB projectNameValidator;
    private AppNameValidator projectAppNameValidator;
    private boolean projectHasCustomIcon = false;
    private boolean updatingExistingProject = false;
    private int projectVersionCode = 1;
    private int projectVersionNameFirstPart;
    private int projectVersionNameSecondPart;
    private boolean shownPackageNameChangeWarning;
    private boolean isIconAdaptive;
    private Bitmap icon;
    private String sc_id;

    private static final int CROP_SHAPE_RECT = 0;
    private static final int CROP_SHAPE_CIRCLE = 1;
    private static final int CROP_SHAPE_OVAL = 2;
    private static final int CROP_SHAPE_SQUIRCLE_LIGHT = 3;
    private static final int CROP_SHAPE_SQUIRCLE_DARK = 4;
    private int currentCropShape = CROP_SHAPE_RECT;

    private ThemePresetAdapter themePresetAdapter;
    
    private static final int REQUEST_CODE_PICK_ICON = 1001;
    private static final int REQUEST_CODE_PICK_ICON_FOR_CROP = 1002;
    private static final int REQUEST_CODE_CROP_ICON = 1003;
    private static final int REQUEST_CODE_SHAPE_CROP = 1004;

    public static void saveBitmapTo(Bitmap bitmap, String path) {
        try {
            // Criar o diretório pai se não existir
            File file = new File(path);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MyprojectSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(arg0 -> onBackPressed());

        if (!isStoragePermissionGranted()) finish();

        sc_id = getIntent().getStringExtra("sc_id");
        updatingExistingProject = getIntent().getBooleanExtra("is_update", false);

        binding.verCode.setSelected(true);
        binding.verName.setSelected(true);


        binding.appIconLayout.setOnClickListener(this);
        binding.verCodeHolder.setOnClickListener(this);
        binding.verNameHolder.setOnClickListener(this);
        binding.imgThemeColorHelp.setOnClickListener(this);
        binding.okButton.setOnClickListener(this);
        binding.cancel.setOnClickListener(this);

        initializeThemePresets();

        binding.tilAppName.setHint(Helper.getResString(R.string.myprojects_settings_hint_enter_application_name));
        binding.tilPackageName.setHint(Helper.getResString(R.string.myprojects_settings_hint_enter_package_name));
        binding.tilProjectName.setHint(Helper.getResString(R.string.myprojects_settings_hint_enter_project_name));

        projectAppNameValidator = new AppNameValidator(getApplicationContext(), binding.tilAppName);
        projectPackageNameValidator = new PackageNameValidator(getApplicationContext(), binding.tilPackageName);
        projectNameValidator = new VB(getApplicationContext(), binding.tilProjectName);
        binding.tilPackageName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (!shownPackageNameChangeWarning && !Helper.getText(((EditText) v)).trim().contains("com.my.newproject")) {
                    showPackageNameChangeWarning();
                }
            }
        });

        projectThemeColors[0] = getDefaultColor(ProjectFile.COLOR_ACCENT);
        projectThemeColors[1] = getDefaultColor(ProjectFile.COLOR_PRIMARY);
        projectThemeColors[2] = getDefaultColor(ProjectFile.COLOR_PRIMARY_DARK);
        projectThemeColors[3] = getDefaultColor(ProjectFile.COLOR_CONTROL_HIGHLIGHT);
        projectThemeColors[4] = getDefaultColor(ProjectFile.COLOR_CONTROL_NORMAL);

        for (int i = 0; i < themeColorKeys.length; i++) {
            ThemeColorView colorView = new ThemeColorView(this, i);
            colorView.name.setText(themeColorLabels[i]);
            colorView.color.setBackgroundColor(Color.WHITE);
            binding.layoutThemeColors.addView(colorView);
            colorView.setOnClickListener(v -> {
                if (!mB.a()) {
                    pickColor(v, (Integer) v.getTag());
                }
            });
        }
        if (updatingExistingProject) {
            /* Set the dialog's title & save button label */
            binding.toolbar.setTitle("Project Settings");
            HashMap<String, Object> metadata = lC.b(sc_id);
            binding.etPackageName.setText(yB.c(metadata, "my_sc_pkg_name"));
            binding.etProjectName.setText(yB.c(metadata, "my_ws_name"));
            binding.etAppName.setText(yB.c(metadata, "my_app_name"));
            binding.okButton.setText("Save changes");
            projectVersionCode = parseInt(yB.c(metadata, "sc_ver_code"), 1);
            parseVersion(yB.c(metadata, "sc_ver_name"));
            binding.verCode.setText(yB.c(metadata, "sc_ver_code"));
            binding.verName.setText(yB.c(metadata, "sc_ver_name"));
            projectHasCustomIcon = yB.a(metadata, "custom_icon");
            if (projectHasCustomIcon) {
                // Usar o ícone padrão para projetos existentes
                binding.appIcon.setImageResource(R.drawable.default_icon);
            }

            for (int i = 0; i < themeColorKeys.length; i++) {
                projectThemeColors[i] = yB.a(metadata, themeColorKeys[i], projectThemeColors[i]);
            }
        } else {
            /* Set the dialog's title & create button label */
            String newProjectName = getIntent().getStringExtra("my_ws_name");
            String newProjectPackageName = getIntent().getStringExtra("my_sc_pkg_name");
            if (sc_id == null || sc_id.isEmpty()) {
                sc_id = lC.b();
                newProjectName = lC.c();
                newProjectPackageName = "com.my." + newProjectName.toLowerCase();
            }
            binding.etPackageName.setText(newProjectPackageName);
            binding.etProjectName.setText(newProjectName);
            binding.etAppName.setText(getIntent().getStringExtra("my_app_name"));

            String newProjectVersionCode = getIntent().getStringExtra("sc_ver_code");
            String newProjectVersionName = getIntent().getStringExtra("sc_ver_name");
            if (newProjectVersionCode == null || newProjectVersionCode.isEmpty()) {
                newProjectVersionCode = "1";
            }
            if (newProjectVersionName == null || newProjectVersionName.isEmpty()) {
                newProjectVersionName = "1.0";
            }
            projectVersionCode = parseInt(newProjectVersionCode, 1);
            parseVersion(newProjectVersionName);
            binding.verCode.setText(newProjectVersionCode);
            binding.verName.setText(newProjectVersionName);
            projectHasCustomIcon = getIntent().getBooleanExtra("custom_icon", false);
            if (projectHasCustomIcon) {
                // Usar o ícone padrão para novos projetos
                binding.appIcon.setImageResource(R.drawable.default_icon);
            }
        }
        syncThemeColors();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_PICK_ICON) {
                // Selecionar imagem da galeria
                handleImageSelection(data.getData());
            } else if (requestCode == REQUEST_CODE_PICK_ICON_FOR_CROP) {
                // Selecionar imagem para cortar
                handleImageSelectionForCrop(data.getData());
            } else if (requestCode == REQUEST_CODE_SHAPE_CROP) {
                Uri resultUri = data.getData();
                if (resultUri != null) {
                    binding.appIcon.setImageURI(resultUri);
                    projectHasCustomIcon = true;
                    saveIconFromUri(resultUri);
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                Uri resultUri = UCrop.getOutput(data);
                if (resultUri != null) {
                    try {
                        if (currentCropShape != CROP_SHAPE_RECT) {
                            Bitmap cropped = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                            Bitmap masked = switch (currentCropShape) {
                                case CROP_SHAPE_CIRCLE -> maskToCircle(cropped);
                                case CROP_SHAPE_OVAL -> maskToOval(cropped);
                                case CROP_SHAPE_SQUIRCLE_LIGHT -> maskToRounded(cropped, 0.4f);
                                case CROP_SHAPE_SQUIRCLE_DARK -> maskToRounded(cropped, 0.25f);
                                default -> cropped;
                            };
                            saveBitmapTo(masked, new File(resultUri.getPath()).getAbsolutePath());
                        }
                    } catch (Exception ignored) {
                    }
                    binding.appIcon.setImageURI(resultUri);
                    projectHasCustomIcon = true;
                    saveIconFromUri(resultUri);
                }
            } else if (requestCode == REQUEST_CODE_CROP_ICON) { // Fallback intents antigos
                // Resultado do crop
                handleCroppedImage(data);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            if (cropError != null) cropError.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.app_icon_layout) {
            showIconOptionsDialog();
        } else if (id == R.id.ok_button) {
            mB.a(v);
            if (isInputValid()) {
                new SaveProjectAsyncTask(getApplicationContext()).execute();
            }
        } else if (id == R.id.cancel) {
            finish();
        } else if (id == R.id.img_theme_color_help) {
            animateLayoutChanges(binding.getRoot());
            if (binding.imgColorGuide.getVisibility() == View.VISIBLE) {
                binding.imgColorGuide.setVisibility(View.GONE);
            } else {
                binding.imgColorGuide.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.ver_code_holder || id == R.id.ver_name_holder) {
            if (ConfigActivity.isSettingEnabled(ConfigActivity.SETTING_USE_NEW_VERSION_CONTROL)) {
                new VersionDialog(this).show();
            } else {
                showOldVersionControlDialog();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isStoragePermissionGranted()) {
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        oB oBVar = new oB();
        oBVar.f(wq.e() + File.separator + sc_id);
        oBVar.f(wq.g() + File.separator + sc_id);
        oBVar.f(wq.t() + File.separator + sc_id);
        oBVar.f(wq.d() + File.separator + sc_id);
        File o = getCustomIcon();
        if (!o.exists()) {
            try {
                // Criar o diretório pai se não existir
                File parentDir = o.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                o.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showIconOptionsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(Helper.getResString(R.string.myprojects_settings_context_menu_title_choose));
        builder.setItems(new String[]{
                Helper.getResString(R.string.myprojects_settings_context_menu_title_choose_gallery),
                Helper.getResString(R.string.myprojects_settings_context_menu_title_choose_gallery_with_crop),
                Helper.getResString(R.string.myprojects_settings_context_menu_title_choose_gallery_default)
        }, (dialog, which) -> {
            switch (which) {
                case 0 -> pickImageFromGallery();
                case 1 -> pickImageForCrop();
                case 2 -> resetToDefaultIcon();
            }
        });
        builder.create().show();
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_ICON);
    }

    private void pickImageForCrop() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_ICON_FOR_CROP);
    }

    private void resetToDefaultIcon() {
        binding.appIcon.setImageResource(R.drawable.default_icon);
        projectHasCustomIcon = false;
        icon = null;
        // Remover o arquivo de ícone personalizado se existir
        File customIconFile = getCustomIcon();
        if (customIconFile.exists()) {
            customIconFile.delete();
        }
    }

    private void handleImageSelection(Uri imageUri) {
        if (imageUri != null) {
            try {
                // Carregar a imagem selecionada
                binding.appIcon.setImageURI(imageUri);
                projectHasCustomIcon = true;
                
                // Salvar o ícone
                saveIconFromUri(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
                SketchwareUtil.toastError("Erro ao carregar imagem");
            }
        }
    }

    private void handleImageSelectionForCrop(Uri imageUri) {
        if (imageUri != null) {
            Intent intent = new Intent(this, pro.sketchware.activities.iconcreator.ShapeCropActivity.class);
            intent.putExtra(pro.sketchware.activities.iconcreator.ShapeCropActivity.EXTRA_IMAGE_URI, imageUri);
            intent.putExtra(pro.sketchware.activities.iconcreator.ShapeCropActivity.EXTRA_SHAPE, 0);
            startActivityForResult(intent, REQUEST_CODE_SHAPE_CROP);
        }
    }

    private void startUCrop(Uri sourceUri) {
        startUCrop(sourceUri, currentCropShape);
    }

    private void startUCrop(Uri sourceUri, int cropShape) {
        try {
            File destination = new File(getCacheDir(), "icon_cropped_" + System.currentTimeMillis() + ".png");
            Uri destinationUri = Uri.fromFile(destination);

            UCrop.Options options = new UCrop.Options();
            options.setFreeStyleCropEnabled(true);
            options.setHideBottomControls(false);
            options.setCompressionFormat(Bitmap.CompressFormat.PNG);
            options.setToolbarTitle("Editar");
            options.setToolbarColor(Color.parseColor("#6D4C41"));
            options.setStatusBarColor(Color.parseColor("#5D4037"));
            options.setActiveControlsWidgetColor(Color.parseColor("#FFA000"));
            options.setCropGridColor(Color.WHITE);
            options.setCropGridStrokeWidth(2);
            options.setDimmedLayerColor(Color.parseColor("#66000000"));
            options.setToolbarWidgetColor(Color.WHITE);
            options.setRootViewBackgroundColor(Color.BLACK);
            options.setShowCropGrid(true);
            if (cropShape == CROP_SHAPE_CIRCLE) {
                options.setCircleDimmedLayer(true);
                options.setShowCropGrid(true);
                options.setShowCropFrame(false);
            }

            UCrop uCrop = UCrop.of(sourceUri, destinationUri)
                    .withMaxResultSize(512, 512)
                    .withOptions(options);

            if (cropShape == CROP_SHAPE_CIRCLE || cropShape == CROP_SHAPE_OVAL || cropShape == CROP_SHAPE_SQUIRCLE_LIGHT || cropShape == CROP_SHAPE_SQUIRCLE_DARK) {
                uCrop = uCrop.withAspectRatio(1, 1);
            }

            // Gestos: escala, rotação e ajuste de proporção
            options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
            uCrop.start(this);
        } catch (Exception e) {
            e.printStackTrace();
            handleImageSelection(sourceUri);
        }
    }

    private void showCropShapeSelector(Uri imageUri) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Formato do recorte");
        String[] items = new String[]{
                "Livre (retângulo)",
                "Círculo",
                "Oval",
                "Squircle claro",
                "Squircle escuro"
        };
        builder.setItems(items, (dialog, which) -> {
            switch (which) {
                case 0 -> currentCropShape = CROP_SHAPE_RECT;
                case 1 -> currentCropShape = CROP_SHAPE_CIRCLE;
                case 2 -> currentCropShape = CROP_SHAPE_OVAL;
                case 3 -> currentCropShape = CROP_SHAPE_SQUIRCLE_LIGHT;
                case 4 -> currentCropShape = CROP_SHAPE_SQUIRCLE_DARK;
            }
            startUCrop(imageUri, currentCropShape);
        });
        builder.show();
    }

    private Bitmap maskToCircle(Bitmap src) {
        int size = Math.min(src.getWidth(), src.getHeight());
        int x = (src.getWidth() - size) / 2;
        int y = (src.getHeight() - size) / 2;
        Bitmap squared = Bitmap.createBitmap(src, x, y, size, size);
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(squared, 0, 0, paint);
        return output;
    }

    private Bitmap maskToOval(Bitmap src) {
        Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        RectF rect = new RectF(0, 0, src.getWidth(), src.getHeight());
        canvas.drawOval(rect, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, 0, 0, paint);
        return output;
    }

    private Bitmap maskToRounded(Bitmap src, float radiusRatio) {
        int w = src.getWidth();
        int h = src.getHeight();
        float r = Math.min(w, h) * radiusRatio;
        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        RectF rect = new RectF(0, 0, w, h);
        canvas.drawRoundRect(rect, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, 0, 0, paint);
        return output;
    }

    private void handleCroppedImage(Intent data) {
        try {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap croppedBitmap = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    croppedBitmap = extras.getParcelable("data", Bitmap.class);
                } else {
                    croppedBitmap = extras.getParcelable("data");
                }
                
                if (croppedBitmap != null) {
                    binding.appIcon.setImageBitmap(croppedBitmap);
                    projectHasCustomIcon = true;
                    icon = croppedBitmap;
                    
                    // Salvar o ícone cortado
                    saveBitmapTo(croppedBitmap, getCustomIconPath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SketchwareUtil.toastError("Erro ao processar imagem cortada");
        }
    }

    private void saveIconFromUri(Uri imageUri) {
        try {
            // Converter URI para Bitmap e salvar
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            if (bitmap != null) {
                icon = bitmap;
                saveBitmapTo(bitmap, getCustomIconPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            SketchwareUtil.toastError("Erro ao salvar ícone");
        }
    }

    private void showOldVersionControlDialog() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
        dialog.setIcon(R.drawable.numbers_48);
        dialog.setTitle(Helper.getResString(R.string.myprojects_settings_version_control_title));
        View view = wB.a(getApplicationContext(), R.layout.property_popup_version_control);
        ((TextView) view.findViewById(R.id.tv_code)).setText(Helper.getResString(R.string.myprojects_settings_version_control_title_code));
        ((TextView) view.findViewById(R.id.tv_name)).setText(Helper.getResString(R.string.myprojects_settings_version_control_title_name));

        NumberPicker versionCodePicker = view.findViewById(R.id.version_code);
        NumberPicker versionNameFirstPartPicker = view.findViewById(R.id.version_name1);
        NumberPicker versionNameSecondPartPicker = view.findViewById(R.id.version_name2);

        versionCodePicker.setWrapSelectorWheel(false);
        versionNameFirstPartPicker.setWrapSelectorWheel(false);
        versionNameSecondPartPicker.setWrapSelectorWheel(false);

        int versionCode = Integer.parseInt(Helper.getText(binding.verCode));
        int versionCodeMinimum = versionCode - 5;
        int versionNameFirstPartMinimum = 1;
        if (versionCodeMinimum <= 0) {
            versionCodeMinimum = 1;
        }
        versionCodePicker.setMinValue(versionCodeMinimum);
        versionCodePicker.setMaxValue(versionCode + 5);
        versionCodePicker.setValue(versionCode);

        String[] split = Helper.getText(binding.verName).split("\\.");
        AtomicInteger projectNewVersionNameFirstPart = new AtomicInteger(parseInt(split[0], 1));
        AtomicInteger projectNewVersionNameSecondPart = new AtomicInteger(parseInt(split[1], 0));
        if (projectNewVersionNameFirstPart.get() - 5 > 0) {
            versionNameFirstPartMinimum = projectNewVersionNameFirstPart.get() - 5;
        }
        versionNameFirstPartPicker.setMinValue(versionNameFirstPartMinimum);
        versionNameFirstPartPicker.setMaxValue(projectNewVersionNameFirstPart.get() + 5);
        versionNameFirstPartPicker.setValue(projectNewVersionNameFirstPart.get());

        versionNameSecondPartPicker.setMinValue(Math.max(projectNewVersionNameSecondPart.get() - 20, 0));
        versionNameSecondPartPicker.setMaxValue(projectNewVersionNameSecondPart.get() + 20);
        versionNameSecondPartPicker.setValue(projectNewVersionNameSecondPart.get());
        dialog.setView(view);

        versionCodePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (oldVal > newVal && newVal < projectVersionCode) {
                picker.setValue(projectVersionCode);
            }
        });
        versionNameFirstPartPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            projectNewVersionNameFirstPart.set(newVal);
            if (oldVal > newVal) {
                if (newVal < projectVersionNameFirstPart) {
                    versionCodePicker.setValue(projectVersionNameFirstPart);
                }
                if (projectNewVersionNameFirstPart.get() == projectVersionNameFirstPart || projectNewVersionNameSecondPart.get() <= projectVersionNameSecondPart) {
                    versionNameSecondPartPicker.setValue(projectVersionNameSecondPart);
                    projectNewVersionNameSecondPart.set(projectVersionNameSecondPart);
                }
            }
        });
        versionNameSecondPartPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            projectNewVersionNameSecondPart.set(newVal);
            if (oldVal > newVal && newVal < projectVersionNameSecondPart && projectNewVersionNameFirstPart.get() < projectVersionNameFirstPart) {
                picker.setValue(projectVersionNameSecondPart);
            }
        });
        dialog.setPositiveButton(Helper.getResString(R.string.common_word_save), (v, which) -> {
            if (!mB.a()) {
                binding.verCode.setText(String.valueOf(versionCodePicker.getValue()));
                binding.verName.setText(projectNewVersionNameFirstPart + "." + projectNewVersionNameSecondPart);
                v.dismiss();
            }
        });
        dialog.setNegativeButton(Helper.getResString(R.string.common_word_cancel), null);
        dialog.show();
    }

    private void syncThemeColors() {
        for (int i = 0; i < projectThemeColors.length; i++) {
            ((ThemeColorView) binding.layoutThemeColors.getChildAt(i)).color.setBackgroundColor(projectThemeColors[i]);
        }
    }

    private void parseVersion(String toParse) {
        try {
            String[] split = toParse.split("\\.");
            projectVersionNameFirstPart = parseInt(split[0], 1);
            projectVersionNameSecondPart = parseInt(split[1], 0);
        } catch (Exception ignored) {
        }
    }

    private void pickColor(View anchorView, int colorIndex) {
        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this, projectThemeColors[colorIndex], false, false);
        colorPickerDialog.a((new ColorPickerDialog.b() {
            @Override
            public void a(int var1) {
                projectThemeColors[colorIndex] = var1;
                syncThemeColors();
                themePresetAdapter.unselectThePreviousTheme(-1);
            }

            @Override
            public void a(String var1, int var2) {
                projectThemeColors[colorIndex] = var2;
                syncThemeColors();
                themePresetAdapter.unselectThePreviousTheme(-1);
            }
        }
        ));
        colorPickerDialog.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
    }

    private void showResetIconConfirmation() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
        dialog.setTitle(Helper.getResString(R.string.common_word_settings));
        dialog.setIcon(R.drawable.default_icon);
        dialog.setMessage(Helper.getResString(R.string.myprojects_settings_confirm_reset_icon));
        dialog.setPositiveButton(Helper.getResString(R.string.common_word_reset), (v, which) -> {
            binding.appIcon.setImageResource(R.drawable.default_icon);
            projectHasCustomIcon = false;
            v.dismiss();
        });
        dialog.setNegativeButton(Helper.getResString(R.string.common_word_cancel), null);
        dialog.show();
    }

    private File getCustomIcon() {
        return new File(getCustomIconPath());
    }

    private String getCustomIconPath() {
        return wq.e() + File.separator + sc_id + File.separator + "icon.png";
    }

    private String getTempIconsFolderPath(String foldername) {
        return wq.e() + File.separator + sc_id + File.separator + foldername;
    }

    private String getIconsFolderPath() {
        return wq.e() + File.separator + sc_id + File.separator + "mipmaps" + File.separator;
    }

    private boolean isInputValid() {
        return projectPackageNameValidator.b() && projectNameValidator.b() && projectAppNameValidator.b();
    }

    private void showPackageNameChangeWarning() {
        shownPackageNameChangeWarning = true;
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
        dialog.setTitle(Helper.getResString(R.string.common_word_warning));
        dialog.setIcon(R.drawable.break_warning_96_red);
        dialog.setMessage(Helper.getResString(R.string.myprojects_settings_message_package_rename));
        dialog.setPositiveButton(Helper.getResString(R.string.common_word_ok), null);
        dialog.show();
    }

    private int parseInt(String input, int fallback) {
        try {
            return Integer.parseInt(input);
        } catch (Exception unused) {
            return fallback;
        }
    }

    private void animateLayoutChanges(View view) {
        var autoTransition = new AutoTransition();
        autoTransition.setDuration((short) 200);
        TransitionManager.beginDelayedTransition((ViewGroup) view, autoTransition);
    }

    private static class ThemeColorView extends LinearLayout {

        private TextView color;
        private TextView name;

        public ThemeColorView(Context context, int tag) {
            super(context);
            initialize(context, tag);
        }

        private void initialize(Context context, int tag) {
            setTag(tag);
            wB.a(context, this, R.layout.myproject_color);
            color = findViewById(R.id.color);
            name = findViewById(R.id.name);
        }
    }

    private class SaveProjectAsyncTask extends MA {

        public SaveProjectAsyncTask(Context context) {
            super(context);
            addTask(this);
            k();
        }

        @Override
        public void a() {
            h();
            
            // Registrar criação de projeto no analytics
            if (!updatingExistingProject) {
                String projectName = Helper.getText(binding.etProjectName);
                String projectType = "android_app";
                SketchwareAnalytics.getInstance(getApplicationContext()).logProjectCreated(sc_id, projectName, projectType);
            }
            
            Intent intent = getIntent();
            intent.putExtra("sc_id", sc_id);
            intent.putExtra("is_new", !updatingExistingProject);
            intent.putExtra("index", intent.getIntExtra("index", -1));
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void b() {
            HashMap<String, Object> data = new HashMap<>();
            data.put("sc_id", sc_id);
            data.put("my_sc_pkg_name", Helper.getText(binding.etPackageName));
            data.put("my_ws_name", Helper.getText(binding.etProjectName));
            data.put("my_app_name", Helper.getText(binding.etAppName));
            if (updatingExistingProject) {
                data.put("custom_icon", projectHasCustomIcon);
                data.put("isIconAdaptive", isIconAdaptive);
                data.put("sc_ver_code", Helper.getText(binding.verCode));
                data.put("sc_ver_name", Helper.getText(binding.verName));
                data.put("sketchware_ver", GB.d(getApplicationContext()));
                for (int i = 0; i < themeColorKeys.length; i++) {
                    data.put(themeColorKeys[i], projectThemeColors[i]);
                }
                lC.b(sc_id, data);
                updateProjectResourcesContents(data);
            } else {
                data.put("my_sc_reg_dt", new nB().a("yyyyMMddHHmmss"));
                data.put("custom_icon", projectHasCustomIcon);
                data.put("isIconAdaptive", isIconAdaptive);
                data.put("sc_ver_code", Helper.getText(binding.verCode));
                data.put("sc_ver_name", Helper.getText(binding.verName));
                data.put("sketchware_ver", GB.d(getApplicationContext()));
                for (int i = 0; i < themeColorKeys.length; i++) {
                    data.put(themeColorKeys[i], projectThemeColors[i]);
                }
                lC.a(sc_id, data);
                updateProjectResourcesContents(data);
                wq.a(getApplicationContext(), sc_id);
                new oB().b(wq.b(sc_id));
                ProjectSettings projectSettings = new ProjectSettings(sc_id);
                projectSettings.setValue(ProjectSettings.SETTING_NEW_XML_COMMAND, ProjectSettings.SETTING_GENERIC_VALUE_TRUE);
                projectSettings.setValue(ProjectSettings.SETTING_ENABLE_VIEWBINDING, ProjectSettings.SETTING_GENERIC_VALUE_TRUE);

            }
            try {
                FileUtil.deleteFile(getTempIconsFolderPath("mipmaps" + File.separator));
                FileUtil.copyDirectory(new File(getTempIconsFolderPath("temp_icons" + File.separator)), new File(getIconsFolderPath()));
                FileUtil.deleteFile(getTempIconsFolderPath("temp_icons" + File.separator));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private void updateProjectResourcesContents(HashMap<String, Object> data) {
            String baseDir = wq.b(sc_id) + "/files/resource/values/";
            String stringsFilePath = baseDir + "strings.xml";
            String colorsFilePath = baseDir + "colors.xml";
            String newAppName = Objects.requireNonNull(data.get("my_app_name")).toString();

            if (FileUtil.isExistFile(stringsFilePath)) {
                String xmlContent = FileUtil.readFile(stringsFilePath);
                xmlContent = xmlContent.replaceAll("(<string\\s+name=\"app_name\">)(.*?)(</string>)", "$1" + newAppName + "$3");
                FileUtil.writeFile(stringsFilePath, xmlContent);
            }

            if (FileUtil.isExistFile(colorsFilePath)) {
                String xmlContent = FileUtil.readFile(colorsFilePath);
                for (int i = 0; i < themeColorKeys.length; i++) {
                    String colorName = themeColorLabels[i];
                    String newColor = String.format("#%06X", (0xFFFFFF & projectThemeColors[i]));
                    xmlContent = xmlContent.replaceAll("(<color\\s+name=\"" + colorName + "\">)(.*?)(</color>)", "$1" + newColor + "$3");
                }
                FileUtil.writeFile(colorsFilePath, xmlContent);
            }

        }

        @Override
        public void a(String str) {
            h();
        }

    }

    private void initializeThemePresets() {
        List<ThemeManager.ThemePreset> themePresets = Arrays.asList(ThemeManager.getThemePresets());

        themePresetAdapter = new ThemePresetAdapter(this, themePresets, (theme, position) -> applyTheme(theme));

        binding.btnGenerateRandomTheme.setOnClickListener(v -> {
            themePresetAdapter.unselectThePreviousTheme(-1);
            generateRandomTheme();
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.layoutThemePresets.setLayoutManager(layoutManager);

        binding.layoutThemePresets.setAdapter(themePresetAdapter);
    }

    private void generateRandomTheme() {
        ThemeManager.ThemePreset randomTheme = ThemeManager.generateRandomTheme();
        applyTheme(randomTheme);

        SketchwareUtil.toast(Helper.getResString(R.string.theme_random_generated));
    }

    private void applyTheme(ThemeManager.ThemePreset theme) {
        projectThemeColors[0] = theme.colorAccent;
        projectThemeColors[1] = theme.colorPrimary;
        projectThemeColors[2] = theme.colorPrimaryDark;
        projectThemeColors[3] = theme.colorControlHighlight;
        projectThemeColors[4] = theme.colorControlNormal;

        syncThemeColors();
    }
}
