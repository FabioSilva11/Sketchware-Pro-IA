package pro.sketchware.ai;

import androidx.annotation.NonNull;

import com.besome.sketch.editor.view.palette.PaletteWidget;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a canonical list of layout and widget types supported by the palette,
 * to be passed as context to the AI when requesting code generation.
 */
public final class LayoutPaletteContext {

    private LayoutPaletteContext() {}

    public static Map<String, List<String>> getSupportedTypes() {
        Map<String, List<String>> map = new LinkedHashMap<>();

        map.put("layouts", Arrays.asList(
                // PaletteWidget.a
                "LinearLayout(horizontal)",
                "LinearLayout(vertical)",
                "HorizontalScrollView",
                "ScrollView",
                // extraWidgetLayout
                "RelativeLayout",
                "CardView",
                "TextInputLayout",
                "TabLayout",
                "BottomNavigationView",
                "CollapsingToolbarLayout",
                "SwipeRefreshLayout",
                "RadioGroup"
        ));

        map.put("widgets", Arrays.asList(
                // PaletteWidget.b
                "Button",
                "TextView",
                "EditText",
                "ImageView",
                "ListView",
                "Spinner",
                "CheckBox",
                "WebView",
                "Switch",
                "SeekBar",
                "CalendarView",
                "AdView",
                "ProgressBar",
                "MapView",
                // extraWidget
                "DatePicker",
                "RatingBar",
                "SearchView",
                "DigitalClock",
                "RadioButton",
                "GridView",
                "AutoCompleteTextView",
                "MultiAutoCompleteTextView",
                "VideoView",
                "TimePicker",
                "AnalogClock",
                "ViewPager",
                "BadgeView",
                "PatternLockView",
                "WaveSideBar",
                "SignInButton",
                "MaterialButton",
                "CircleImageView",
                "LottieAnimation",
                "YoutubePlayer",
                "OTPView",
                "CodeView",
                "RecyclerView"
        ));

        return map;
    }

    public static Map<String, List<String>> getAttributesByType() {
        Map<String, List<String>> m = new LinkedHashMap<>();

        // Common for most views (based on Sketchware Pro properties)
        m.put("_common", Arrays.asList(
                "android:id",
                "android:layout_width (match_parent|wrap_content|dp)",
                "android:layout_height (match_parent|wrap_content|dp)",
                "android:layout_margin, layout_marginLeft, layout_marginTop, layout_marginRight, layout_marginBottom",
                "android:padding, paddingLeft, paddingTop, paddingRight, paddingBottom",
                "android:background (color|@color|@drawable)",
                "android:enabled (true|false)",
                "android:clickable (true|false)",
                "android:alpha (0.0-1.0)",
                "android:translationX (dp)",
                "android:translationY (dp)",
                "android:scaleX, android:scaleY (float)",
                // Parent-relative attributes (for RelativeLayout)
                "android:layout_alignParentTop|Bottom|Start|End (true|false)",
                "android:layout_alignTop|Bottom|Start|End (@id/view)",
                "android:layout_toStartOf|layout_toEndOf (@id/view)",
                "android:layout_centerInParent|CenterHorizontal|CenterVertical (true|false)"
        ));

        // Layouts
        m.put("LinearLayout", Arrays.asList(
                "android:orientation (vertical|horizontal)",
                "android:weightSum (number)",
                "android:gravity",
                "android:layout_gravity",
                "android:layout_weight (child)"
        ));
        m.put("RelativeLayout", Arrays.asList(
                "android:gravity",
                "android:layout_gravity"
        ));
        m.put("ScrollView", Arrays.asList("android:fillViewport (true|false)"));
        m.put("HorizontalScrollView", Arrays.asList("android:fillViewport (true|false)"));
        m.put("CardView", Arrays.asList(
                "android:background (for card background)",
                "android:elevation (for card elevation)",
                "android:padding (for content padding)"
        ));
        m.put("TextInputLayout", Arrays.asList(
                "android:hint (for input hint)",
                "android:textColorHint (for hint color)"
        ));
        m.put("SwipeRefreshLayout", Arrays.asList("android:enabled (true|false)"));

        // Widgets - Propriedades específicas baseadas no ViewBean
        m.put("TextView", Arrays.asList(
                "android:text",
                "android:textSize (sp)",
                "android:textStyle (normal|bold|italic|bold|italic)",
                "android:textColor (@color|#RRGGBB)",
                "android:hint",
                "android:textColorHint",
                "android:singleLine (true|false)",
                "android:lines (int)"
        ));
        m.put("EditText", Arrays.asList(
                "android:text",
                "android:hint",
                "android:inputType",
                "android:imeOptions",
                "android:textColor",
                "android:textSize (sp)"
        ));
        m.put("ImageView", Arrays.asList(
                "android:src (@drawable)",
                "android:scaleType (fitXY|centerCrop|centerInside|fitCenter|fitStart|fitEnd)",
                "android:contentDescription"
        ));
        m.put("Button", Arrays.asList("android:text", "android:textSize", "android:textColor"));
        m.put("CheckBox", Arrays.asList("android:text", "android:checked (true|false)"));
        m.put("RadioButton", Arrays.asList("android:text", "android:checked (true|false)"));
        m.put("Switch", Arrays.asList("android:text", "android:checked (true|false)"));
        m.put("SeekBar", Arrays.asList("android:max (int)", "android:progress (int)"));
        m.put("ProgressBar", Arrays.asList(
                "style (?android:progressBarStyle|?android:progressBarStyleHorizontal)",
                "android:max (int)",
                "android:progress (int)",
                "android:indeterminate (true|false)"
        ));
        m.put("ListView", Arrays.asList(
                "android:dividerHeight (dp)",
                "android:choiceMode (none|single|multiple)"
        ));
        m.put("Spinner", Arrays.asList("android:prompt"));
        m.put("CalendarView", Arrays.asList("android:firstDayOfWeek (1-7)"));
        m.put("WebView", Arrays.asList("android:overScrollMode"));
        m.put("AdView", Arrays.asList("android:layout_width", "android:layout_height"));
        m.put("MapView", Arrays.asList("android:apiKey (if needed via resources)"));

        // Extras
        m.put("RecyclerView", Arrays.asList("android:scrollbars"));
        m.put("ViewPager", Arrays.asList("android:layout_width", "android:layout_height"));
        m.put("TabLayout", Arrays.asList("android:layout_width", "android:layout_height"));
        m.put("BottomNavigationView", Arrays.asList("android:layout_width", "android:layout_height"));
        m.put("CollapsingToolbarLayout", Arrays.asList("android:layout_width", "android:layout_height"));

        return m;
    }
}


