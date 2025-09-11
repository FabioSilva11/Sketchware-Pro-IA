package pro.sketchware.activities.main.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import pro.sketchware.R;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment; // 변경

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// AppCompatActivity এর পরিবর্তে Fragment ব্যবহার করা হয়েছে
public class ZabbarFragment extends Fragment {

    private ListView listView;
    private final ArrayList<HashMap<String, Object>> projectList = new ArrayList<>();
    private String currentScId = "";
    private String currentBasePath = "";
    private int pendingAction = 0;
    private int pendingType = 0;
    private byte[] exportBytes = new byte[0];
    private String exportFileName = "";
    private static final int TYPE_ACTIVITY = 0;
    private static final int TYPE_CUSTOM   = 1;

    private static final String EXPORT_AES_MODE = "AES/CBC/PKCS5Padding";
    private static final byte[] EXPORT_KEY = Arrays.copyOf("ZabbarFileImport".getBytes(StandardCharsets.UTF_8), 16);
    private static final String SW_AES_MODE = "AES/CBC/PKCS5Padding";
    private static final byte[] SW_KEY = Arrays.copyOf("sketchwaresecure".getBytes(StandardCharsets.UTF_8), 16);
    private static final IvParameterSpec SW_IV = new IvParameterSpec(SW_KEY);

    private ActivityResultLauncher<Intent> exportLauncher;
    private ActivityResultLauncher<Intent> importLauncher;

    // Fragment এর জন্য একটি খালি constructor দরকার
    public ZabbarFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ActivityResultLauncher গুলোকে onCreate এ রেজিস্টার করা হয়
        exportLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
            if (res.getResultCode() == Activity.RESULT_OK && res.getData() != null) {
                Uri uri = res.getData().getData();
                if (uri == null) return;
                try (OutputStream os = requireActivity().getContentResolver().openOutputStream(uri)) {
                    if (os == null) { toast("Failed to open output stream."); return; }
                    os.write(exportBytes);
                    os.flush();
                    toast("Exported: " + exportFileName);
                } catch (Exception e) {
                    toast("Write error: " + e.getMessage());
                }
            }
        });

        importLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
            if (res.getResultCode() == Activity.RESULT_OK && res.getData() != null) {
                Uri uri = res.getData().getData();
                if (uri == null) return;
                try (BufferedInputStream bis = new BufferedInputStream(requireActivity().getContentResolver().openInputStream(uri))) {
                    byte[] all = readAllBytes(bis);
                    String plain = decryptImported(all);
                    if (plain.startsWith("Decrypt Error:")) { toast(plain); return; }
                    applyImport(plain, pendingType);
                } catch (Exception e) {
                    toast("Import read error: " + e.getMessage());
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // UI এলিমেন্টগুলো এখানে তৈরি করা হবে এবং root view রিটার্ন করা হবে
        // 'this' এর পরিবর্তে 'requireContext()' ব্যবহার করা হয়েছে
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);

        
        TextView hint = new TextView(requireContext());
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        hint.setTextColor(0xFF555555);
        hint.setPadding(dp(0), dp(0), dp(0), dp(0));
        root.addView(hint);

        listView = new ListView(requireContext());
        listView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(listView);

        // setContentView এর পরিবর্তে view রিটার্ন করা হয়েছে
        return root;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // View তৈরি হয়ে যাওয়ার পর এই মেথড কল হয়
        // এখানে View সম্পর্কিত কাজগুলো করা হয়
        loadProjects();
        listView.setAdapter(new ProjectAdapter());
        listView.setOnItemClickListener((parent, v, position, id) -> openProjectActions(projectList.get(position)));
    }


    private void loadProjects() {
        projectList.clear();
        try {
            String listRoot = FileUtil.getExternalStorageDir() + "/.sketchware/mysc/list/";
            ArrayList<String> dirs = new ArrayList<>();
            FileUtil.listDir(listRoot, dirs);
            Collections.sort(dirs, String.CASE_INSENSITIVE_ORDER);

            for (String p : dirs) {
                String scId = lastName(p);
                if (TextUtils.isEmpty(scId)) continue;

                String projectFile = listRoot + scId + "/project";
                if (!FileUtil.isExistFile(projectFile)) continue;

                HashMap<String, Object> map = tryReadProject(projectFile);
                if (map != null) {
                    map.putIfAbsent("sc_id", scId);
                    boolean useCustomIcon = "true".equals(String.valueOf(map.get("custom_icon")));
                    // 'this' এর পরিবর্তে 'requireContext()' ব্যবহার
                    Drawable iconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.app_ico);

                    if (useCustomIcon) {
                        String iconPath = FileUtil.getExternalStorageDir() + "/.sketchware/resources/icons/" + scId + "/icon.png";
                        Bitmap bmp = null;
                        if (FileUtil.isExistFile(iconPath)) {
                            try { bmp = BitmapFactory.decodeFile(iconPath); } catch (Exception ignored) {}
                        }
                        if (bmp != null) iconDrawable = new BitmapDrawable(requireContext().getResources(), bmp);
                    }
                    map.put("icon", iconDrawable);
                    projectList.add(map);
                }
            }
            Collections.reverse(projectList);
        } catch (Exception e) {
            toast("Load error: " + e.getMessage());
        }
    }

    private HashMap<String, Object> tryReadProject(String path) {
        try {
            Cipher dec = Cipher.getInstance(SW_AES_MODE);
            dec.init(Cipher.DECRYPT_MODE, new SecretKeySpec(SW_KEY, "AES"), SW_IV);
            byte[] all = FileUtil.readAll(new File(path));
            byte[] plain = dec.doFinal(all);
            return Json.readMap(new String(plain, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            try { return Json.readMap(FileUtil.readFile(path)); } catch (Exception ignored2) { return null; }
        }
    }

    private class ProjectAdapter extends BaseAdapter {
        @Override public int getCount() { return projectList.size(); }
        @Override public Object getItem(int i) { return projectList.get(i); }
        @Override public long getItemId(int i) { return i; }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // এখানে parent.getContext() ব্যবহার করা সঠিক
                LinearLayout row = new LinearLayout(parent.getContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(dp(14), dp(12), dp(14), dp(12));
                row.setBackground(makeRipple());

                ImageView icon = new ImageView(parent.getContext());
                LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(48), dp(48));
                ip.rightMargin = dp(12);
                icon.setLayoutParams(ip);
                icon.setId(101);
                icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                row.addView(icon);

                LinearLayout col = new LinearLayout(parent.getContext());
                col.setOrientation(LinearLayout.VERTICAL);
                col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                TextView title = makeText(16, 0xFF111111, true); title.setId(102);
                TextView sub = makeText(13, 0xFF666666, false); sub.setId(103);
                TextView sc = makeText(12, 0xFF888888, false); sc.setId(104);

                col.addView(title); col.addView(sub); col.addView(sc);
                row.addView(col);
                convertView = row;
            }

            HashMap<String, Object> m = projectList.get(pos);
            ((TextView) convertView.findViewById(102)).setText(String.valueOf(m.getOrDefault("my_app_name", "(no name)")));
            ((TextView) convertView.findViewById(103)).setText(String.valueOf(m.getOrDefault("my_sc_pkg_name", "")));
            ((TextView) convertView.findViewById(104)).setText("sc_id: " + m.getOrDefault("sc_id", ""));
            ((ImageView) convertView.findViewById(101)).setImageDrawable((Drawable) m.get("icon"));

            return convertView;
        }
    }

    private void openProjectActions(HashMap<String, Object> item) {
        currentScId = String.valueOf(item.get("sc_id"));
        currentBasePath = FileUtil.getExternalStorageDir() + "/.sketchware/data/" + currentScId;

        // 'this' এর পরিবর্তে 'requireContext()' ব্যবহার
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Project " + currentScId)
            .setItems(new String[]{"Import", "Export"}, (dialog, which) -> {
                pendingAction = (which == 0) ? 2 : 1;
                pickTypeThenContinue();
            })
            .setNegativeButton("Close", null)
            .show();
    }

    private void pickTypeThenContinue() {
        // 'this' এর পরিবর্তে 'requireContext()' ব্যবহার
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle((pendingAction == 2) ? "Import: Choose type" : "Export: Choose type")
            .setItems(new String[]{"Activity", "Custom"}, (dialog, which) -> {
                pendingType = (which == 0) ? TYPE_ACTIVITY : TYPE_CUSTOM;

                if (pendingAction == 1) { // Export
                    ArrayList<String> names = listScreenNamesFromFile(pendingType == TYPE_ACTIVITY);
                    if (names.isEmpty()) {
                        toast("No " + ((pendingType == TYPE_ACTIVITY) ? "Activity" : "Custom") + " found.");
                        return;
                    }

                    // 'ZabbarActivity.this' এর পরিবর্তে 'requireContext()' ব্যবহার
                    new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Select " + ((pendingType == TYPE_ACTIVITY) ? "Activity" : "Custom"))
                        .setItems(names.toArray(new CharSequence[0]), (dialog2, idx) -> doExport(names.get(idx)))
                        .setNegativeButton("Cancel", null)
                        .show();

                } else { // Import
                    Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .setType("*/*")
                        .putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                            "application/octet-stream", "application/zip", "text/plain", "application/json"})
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    importLauncher.launch(Intent.createChooser(i, "Select a file to import"));
                }
            })
            .setNegativeButton("Back", null)
            .show();
    }
    
    // বাকি মেথডগুলো অপরিবর্তিত থাকবে, শুধু যেখানে Context এর প্রয়োজন সেখানে পরিবর্তন করা হয়েছে
    
    private void doExport(String selectedName) {
    try {
        ArrayList<String> allLines = readAllSW(currentBasePath + "/view", currentBasePath + "/logic");

        String h1 = "@" + selectedName + ".xml";
        String h2 = "@" + selectedName + ".xml_";
        String h3 = "@" + selectedName + ".xml_fab";

        ArrayList<String> out = new ArrayList<>();
        addSectionIfExists(allLines, h1, out);
        addSectionIfExists(allLines, h2, out);
        addSectionIfExists(allLines, h3, out);

        if (pendingType == TYPE_ACTIVITY) {
            // XML base → Java base (e.g. main → MainActivity.java)
            String javaFileName = toJavaFromXmlBase(selectedName);        // MainActivity.java
            String javaBaseNoExt = javaFileName.substring(0, javaFileName.length() - 5); // MainActivity

            boolean anyFound = collectAllLogicSubsections(allLines, "@" + javaBaseNoExt + ".java", out);

            // Fallback: single block
            if (!anyFound) {
                for (String line : allLines) {
                    if (line.startsWith("@" + javaBaseNoExt + ".java")) {
                        int idx = allLines.indexOf(line);
                        if (idx != -1) {
                            StringBuilder body = new StringBuilder();
                            for (int i = idx + 1; i < allLines.size() && !allLines.get(i).startsWith("@"); i++) {
                                body.append(allLines.get(i)).append("\n");
                            }
                            out.add(line);
                            out.add(body.toString().trim());
                            anyFound = true;
                        }
                    }
                }
            }

            if (!anyFound) {
                out.add("@" + javaBaseNoExt + ".java");
                out.add("// ⚠ No logic sections found for " + javaBaseNoExt);
            }
        }

        // ---- Embed referenced images from this view (base64) ----
        try {
            // শুধু current screen-এর view সেকশনগুলো নিয়ে কাজ
            ArrayList<String> viewOnly = new ArrayList<>();
            addSectionIfExists(allLines, h1, viewOnly);
            addSectionIfExists(allLines, h2, viewOnly);
            addSectionIfExists(allLines, h3, viewOnly);

            ArrayList<String> imgNames = extractImageNames(viewOnly); // আপনার বিদ্যমান হেল্পার
            if (!imgNames.isEmpty()) {
                // ডুপ্লিকেট বাদ
                java.util.LinkedHashSet<String> unique = new java.util.LinkedHashSet<>(imgNames);
                String imagesRoot = FileUtil.getExternalStorageDir() + "/.sketchware/resources/images/" + currentScId + "/";

                for (String n : unique) {
                    String fname = n.endsWith(".png") ? n : (n + ".png");
                    File f = new File(imagesRoot + fname);
                    if (!f.exists()) continue; // না পেলে স্কিপ

                    byte[] bytes = FileUtil.readAll(f);
                    String b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);

                    // সেকশন হেডার: @image:NAME.png
                    out.add("@image:" + fname);
                    // সেকশন বডি: base64
                    out.add(b64);
                }
            }
        } catch (Exception e) {
            toast("Image embed failed: " + e.getMessage());
        }
        // ---------------------------------------------------------

        if (out.isEmpty()) {
            toast("Nothing to export for: " + selectedName);
            return;
        }

        String plain = join(out);
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        Cipher c = Cipher.getInstance(EXPORT_AES_MODE);
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(EXPORT_KEY, "AES"), new IvParameterSpec(iv));
        byte[] ct = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));

        exportBytes = new byte[iv.length + ct.length];
        System.arraycopy(iv, 0, exportBytes, 0, iv.length);
        System.arraycopy(ct, 0, exportBytes, iv.length, ct.length);

        exportFileName = selectedName + ".ziz";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, exportFileName);
        exportLauncher.launch(intent);

    } catch (Exception e) {
        toast("Export error: " + e.getMessage());
    }
}

    private void applyImport(String plain, int targetType) {
    try {
        String viewPath  = currentBasePath + "/view";
        String logicPath = currentBasePath + "/logic";
        String filePath  = currentBasePath + "/file";

        ArrayList<String> viewLines  = readSW(viewPath);
        ArrayList<String> logicLines = readSW(logicPath);
        ArrayList<String> fileLines  = readSW(filePath);

        ArrayList<Section> sections = parseSections(plain);

        // Collect all view names
        ArrayList<String> names = new ArrayList<>();
        for (Section s : sections) {
            if (isViewHeader(s.header)) {
                String name = getBaseFromViewHeader(s.header);
                if (!TextUtils.isEmpty(name) && !names.contains(name)) {
                    names.add(name);
                }
            }
        }

        if (names.isEmpty()) {
            toast("No view section found in file.");
            return;
        }

        boolean forActivity = (targetType == TYPE_ACTIVITY);

        // Ensure file.json updated for screens
        for (String n : names) {
            removeNameFromSection(fileLines, !forActivity, n);
            ensureFileJsonEntry(fileLines, forActivity, n);
        }

        int appliedView = 0, appliedLogic = 0;
        boolean logicSkipped = false;

        // ---- First, restore view/logic blocks ----
        for (Section s : sections) {
            if (isViewHeader(s.header)) {
                replaceOrAppend(viewLines, s.header, s.body);
                appliedView++;
            } else if (isLogicHeader(s.header)) {
                if (forActivity) {
                    replaceOrAppend(logicLines, s.header, s.body);
                    appliedLogic++;
                } else {
                    logicSkipped = true;
                }
            }
        }

        // Fallback: If no logic imported but Activity chosen, try to match by java name
        if (forActivity && appliedLogic == 0 && !names.isEmpty()) {
            String base = names.get(0); // e.g. "main"
            String javaFile = toJavaFromXmlBase(base); // e.g. "MainActivity.java"

            for (Section s : sections) {
                if (s.header != null && s.header.startsWith("@" + javaFile)) {
                    replaceOrAppend(logicLines, s.header, s.body);
                    appliedLogic++;
                }
            }
        }

        // ---- Now handle embedded images ----
        ArrayList<String> resLines = readSW(currentBasePath + "/resource"); // project resource index
        boolean anyImageWritten = false;

        for (Section s : sections) {
            if (s.header != null && s.header.startsWith("@image:")) {
                try {
                    String fname = s.header.substring("@image:".length()).trim(); // e.g. "aaa.png"
                    if (TextUtils.isEmpty(fname)) continue;

                    // Base64 বডি (একাধিক লাইন থাকলেও join করা হবে)
                    StringBuilder sb = new StringBuilder();
                    for (String line : s.body) sb.append(line.trim());
                    if (sb.length() == 0) continue;

                    byte[] bytes = Base64.decode(sb.toString(), Base64.DEFAULT);

                    // গন্তব্য: গ্লোবাল ইমেজ স্টোর (Sketchware যা দেখে)
                    String imagesRoot = FileUtil.getExternalStorageDir() + "/.sketchware/resources/images/" + currentScId + "/";
                    new File(imagesRoot).mkdirs();

                    File outFile = new File(imagesRoot + fname);
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        fos.write(bytes);
                        fos.flush();
                    }

                    // resource তালিকায় @images এন্ট্রি নিশ্চিত করুন
                    updateResourceFile(resLines, fname);
                    anyImageWritten = true;

                } catch (Exception e) {
                    toast("Image import failed: " + e.getMessage());
                }
            }
        }

        // Write back
        writeSW(viewPath,  viewLines);
        writeSW(logicPath, logicLines);
        writeSW(filePath,  fileLines);

        if (anyImageWritten) {
            try {
                writeSW(currentBasePath + "/resource", resLines);
            } catch (Exception e) {
                toast("Resource list write failed: " + e.getMessage());
            }
        }

        String msg = "Imported ✓ view: " + appliedView + " | logic: " + appliedLogic;
        if (logicSkipped) msg += " (logic skipped for Custom)";
        if (anyImageWritten) msg += " | images: updated";
        toast(msg);

    } catch (Exception e) {
        toast("Import error: " + e.getMessage());
    }
}
    
    // ****** Helper মেথডগুলো নিচে দেওয়া হলো (কোনো পরিবর্তন ছাড়াই) *****
    // শুধুমাত্র যেখানে Context দরকার সেখানে requireContext() ব্যবহার করা হয়েছে

    private void toast(String s) {
        // 'this' এর পরিবর্তে 'getContext()' বা 'requireContext()'
        Toast.makeText(requireContext(), s, Toast.LENGTH_LONG).show();
    }

    private int dp(int v) {
        // 'getResources()' এর জন্য context দরকার
        return Math.round(requireContext().getResources().getDisplayMetrics().density * v);
    }

    private int resolveColor(int attr) {
        TypedValue tv = new TypedValue();
        // 'getTheme()' এর জন্য context দরকার
        requireActivity().getTheme().resolveAttribute(attr, tv, true);
        return tv.data;
    }

    private TextView makeText(int sp, int color, boolean bold) {
        // 'new TextView(this)' এর পরিবর্তে context ব্যবহার
        TextView t = new TextView(requireContext());
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private Drawable makeRipple() {
        TypedValue outValue = new TypedValue();
        // 'getTheme()' এর জন্য context দরকার
        requireActivity().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        return ContextCompat.getDrawable(requireContext(), outValue.resourceId);
    }
    
    // --- The rest of the helper methods remain the same ---

    private boolean collectAllLogicSubsections(ArrayList<String> all, String prefix, ArrayList<String> out) {
        boolean found = false;
        for (String line : all) {
            if (line.trim().startsWith(prefix) && line.trim().startsWith("@")) {
                addSectionIfExists(all, line.trim(), out);
                found = true;
            }
        }
        return found;
    }

    private boolean isLogicHeader(String header) {
        if (header == null) return false;
        return header.endsWith(".java") || header.contains(".java_");
    }

    private static class Section {
        String header;
        ArrayList<String> body = new ArrayList<>();
    }

    private ArrayList<Section> parseSections(String text) {
        ArrayList<Section> list = new ArrayList<>();
        String[] lines = text.split("\n", -1);
        Section cur = null;
        for (String raw : lines) {
            if (raw.startsWith("@")) {
                cur = new Section();
                cur.header = raw.trim();
                list.add(cur);
            } else {
                if (cur != null) cur.body.add(raw);
            }
        }
        return list;
    }

    private boolean isViewHeader(String h) {
        return h != null && (h.endsWith(".xml") || h.endsWith(".xml_") || h.endsWith(".xml_fab"));
    }

    private String getBaseFromViewHeader(String header) {
        if (TextUtils.isEmpty(header) || !header.startsWith("@")) return "";
        String h = header.substring(1);
        if (h.endsWith(".xml_fab")) return h.substring(0, h.length() - 8);
        if (h.endsWith(".xml_"))    return h.substring(0, h.length() - 5);
        if (h.endsWith(".xml"))     return h.substring(0, h.length() - 4);
        return "";
    }

    private String toJavaFromXmlBase(String xmlBase) {
        StringBuilder sb = new StringBuilder();
        for (String p : xmlBase.split("_")) {
            if (p.length()==0) continue;
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1));
        }
        return sb.toString() + "Activity.java";
    }

    private void replaceOrAppend(ArrayList<String> lines, String header, ArrayList<String> body) {
        int s = -1, e = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().equals(header)) {
                s = i;
                for (int j = i + 1; j < lines.size(); j++) {
                    if (lines.get(j).startsWith("@")) { e = j; break; }
                }
                break;
            }
        }
        if (s == -1) {
            lines.add(header);
            lines.addAll(body);
        } else {
            ArrayList<String> newLines = new ArrayList<>(lines.subList(0, s));
            newLines.add(header);
            newLines.addAll(body);
            newLines.addAll(lines.subList(e, lines.size()));
            lines.clear();
            lines.addAll(newLines);
        }
    }

    private boolean addSectionIfExists(ArrayList<String> all, String header, ArrayList<String> out) {
        int idx = indexOfHeader(all, header);
        if (idx == -1) return false;
        out.add(all.get(idx));
        for (int j = idx + 1; j < all.size(); j++) {
            if (all.get(j).startsWith("@")) break;
            out.add(all.get(j));
        }
        return true;
    }

    private int indexOfHeader(ArrayList<String> all, String header) {
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).trim().equals(header)) return i;
        }
        return -1;
    }

    private ArrayList<String> listScreenNamesFromFile(boolean activity) {
        ArrayList<String> out = new ArrayList<>();
        try {
            ArrayList<String> lines = readSW(currentBasePath + "/file");
            boolean inAct = false, inCus = false;
            for (String raw : lines) {
                String t = raw.trim();
                if (t.equals("@activity")) { inAct = true; inCus = false; continue; }
                if (t.equals("@customview")) { inCus = true; inAct = false; continue; }
                if ((inAct || inCus) && t.startsWith("{") && t.endsWith("}")) {
                    String n = new JSONObject(t).optString("fileName", "");
                    if (!TextUtils.isEmpty(n) && ((activity && inAct) || (!activity && inCus)) && !out.contains(n)) {
                        out.add(n);
                    }
                }
            }
        } catch (Exception ignored) { }
        Collections.sort(out, String.CASE_INSENSITIVE_ORDER);
        return out;
    }

    private void removeNameFromSection(ArrayList<String> fileLines, boolean fromActivity, String name) {
        String header = fromActivity ? "@activity" : "@customview";
        int s = -1, e = fileLines.size();
        for (int i = 0; i < fileLines.size(); i++) if (fileLines.get(i).trim().equals(header)) { s = i; break; }
        if (s == -1) return;
        for (int j = s + 1; j < fileLines.size(); j++) {
            if (fileLines.get(j).startsWith("@")) { e = j; break; }
        }
        fileLines.removeIf(line -> {
            if (line.trim().startsWith("{") && line.trim().endsWith("}")) {
                try { return name.equals(new JSONObject(line.trim()).optString("fileName")); } catch (Exception ignored) {}
            }
            return false;
        });
    }

    private void ensureFileJsonEntry(ArrayList<String> fileLines, boolean toActivity, String fileName) {
        String header = toActivity ? "@activity" : "@customview";
        int s = -1, e = fileLines.size();
        for (int i = 0; i < fileLines.size(); i++) if (fileLines.get(i).trim().equals(header)) { s = i; break; }
        if (s == -1) { fileLines.add(header); s = fileLines.size() - 1; }
        for (int j = s + 1; j < fileLines.size(); j++) {
            if (fileLines.get(j).startsWith("@")) { e = j; break; }
        }
        for (int k = s + 1; k < e; k++) {
            String t = fileLines.get(k).trim();
            if (t.startsWith("{") && t.endsWith("}")) {
                try { if (fileName.equals(new JSONObject(t).optString("fileName"))) return; } catch (Exception ignored) {}
            }
        }
        try {
            JSONObject obj = new JSONObject();
            obj.put("fileName", fileName); obj.put("fileType", 0); obj.put("keyboardSetting", 0);
            obj.put("options", 1); obj.put("orientation", 0); obj.put("theme", -1);
            fileLines.add(e, obj.toString());
        } catch (Exception ignored) {}
    }

    private ArrayList<String> readSW(String path) throws Exception {
        ArrayList<String> out = new ArrayList<>();
        if (!FileUtil.isExistFile(path)) return out;
        Cipher dec = Cipher.getInstance(SW_AES_MODE);
        dec.init(Cipher.DECRYPT_MODE, new SecretKeySpec(SW_KEY, "AES"), SW_IV);
        try (FileInputStream fis = new FileInputStream(path);
             CipherInputStream cis = new CipherInputStream(fis, dec);
             BufferedReader br = new BufferedReader(new InputStreamReader(cis, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) out.add(line);
        }
        return out;
    }

    private void writeSW(String path, ArrayList<String> lines) throws Exception {
        String data = String.join("\n", lines);
        byte[] plain = data.getBytes(StandardCharsets.UTF_8);
        Cipher enc = Cipher.getInstance(SW_AES_MODE);
        enc.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(SW_KEY, "AES"), SW_IV);
        try (FileOutputStream fos = new FileOutputStream(path);
             CipherOutputStream cos = new CipherOutputStream(fos, enc)) {
            cos.write(plain);
            cos.flush();
        }
    }

    private ArrayList<String> readAllSW(String viewPath, String logicPath) throws Exception {
        ArrayList<String> all = new ArrayList<>(readSW(viewPath));
        all.addAll(readSW(logicPath));
        return all;
    }

    private String decryptImported(byte[] fileData) {
        try {
            if (fileData == null || fileData.length < 17) return "Decrypt Error: File too small";
            byte[] iv = Arrays.copyOfRange(fileData, 0, 16);
            byte[] ct = Arrays.copyOfRange(fileData, 16, fileData.length);
            Cipher dec = Cipher.getInstance(EXPORT_AES_MODE);
            dec.init(Cipher.DECRYPT_MODE, new SecretKeySpec(EXPORT_KEY, "AES"), new IvParameterSpec(iv));
            return new String(dec.doFinal(ct), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Decrypt Error: " + e.getMessage();
        }
    }

    private static class Json {
        static HashMap<String, Object> readMap(String s) throws Exception {
            JSONObject o = new JSONObject(s);
            HashMap<String, Object> m = new HashMap<>();
            Iterator<String> it = o.keys();
            while (it.hasNext()) {
                String k = it.next();
                m.put(k, o.get(k));
            }
            return m;
        }
    }

    private static class FileUtil {
        static String getExternalStorageDir() { return Environment.getExternalStorageDirectory().getAbsolutePath(); }
        static boolean isExistFile(String path) { return new File(path).exists(); }
        static void listDir(String path, ArrayList<String> out) {
            out.clear();
            File f = new File(path);
            if (!f.exists() || !f.isDirectory()) return;
            File[] list = f.listFiles();
            if (list == null) return;
            for (File child : list) if (child.isDirectory()) out.add(child.getAbsolutePath());
        }
        static String readFile(String path) throws Exception {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }
            return sb.toString();
        }
        static byte[] readAll(File f) throws Exception {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (InputStream is = new FileInputStream(f)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) >= 0) bos.write(buf, 0, n);
            }
            return bos.toByteArray();
        }
    }

    private String lastName(String path) {
        if (TextUtils.isEmpty(path)) return "";
        int i = path.lastIndexOf('/');
        return (i >= 0) ? path.substring(i + 1) : path;
    }

    private static String join(ArrayList<String> arr) {
        return String.join("\n", arr) + "\n";
    }

    private static byte[] readAllBytes(InputStream is) throws java.io.IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) >= 0) bos.write(buf, 0, n);
        return bos.toByteArray();
    }
    
    private ArrayList<String> extractImageNames(ArrayList<String> lines) {
        ArrayList<String> names = new ArrayList<>();
        for (String line : lines) {
            if (line.contains("\"resName\":\"")) {
                int i = line.indexOf("\"resName\":\"") + 11;
                int j = line.indexOf("\"", i);
                if (i > 0 && j > i) {
                    String name = line.substring(i, j);
                    if (!TextUtils.isEmpty(name) && !names.contains(name)) {
                        names.add(name);
                    }
                }
            }
            if (line.contains("@drawable/")) {
                int i = line.indexOf("@drawable/") + 10;
                int j = line.indexOf("\"", i);
                if (j == -1) j = line.length();
                String name = line.substring(i, j);
                if (!TextUtils.isEmpty(name) && !names.contains(name)) {
                    names.add(name);
                }
            }
        }
        return names;
    }
 
    private void updateResourceFile(ArrayList<String> resLines, String fname) {
        try {
            String baseName = fname;
            if (baseName.endsWith(".png")) {
                baseName = baseName.substring(0, baseName.length() - 4);
            }

            boolean imagesSectionFound = false;
            boolean alreadyExists = false;
            int insertIndex = -1;

            for (int i = 0; i < resLines.size(); i++) {
                String line = resLines.get(i);
                if (line.equals("@images")) {
                    imagesSectionFound = true;
                    insertIndex = i + 1;
                } else if (line.contains("\"resFullName\":\"" + fname + "\"")) {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {
                String entry = "{\"resFullName\":\"" + fname
                        + "\",\"resName\":\"" + baseName
                        + "\",\"resType\":1}";
                if (imagesSectionFound) {
                    resLines.add(insertIndex, entry);
                } else {
                    resLines.add("@images");
                    resLines.add(entry);
                }
            }
        } catch (Exception e) {
            toast("Resource update failed: " + e.getMessage());
        }
    }
    
   public static ZabbarFragment newInstance() {
    return new ZabbarFragment();
    
  }

}