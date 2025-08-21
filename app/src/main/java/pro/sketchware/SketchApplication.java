package pro.sketchware;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import com.besome.sketch.tools.CollectErrorActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import pro.sketchware.utility.theme.ThemeManager;

public class SketchApplication extends Application {
    private static Context mApplicationContext;

    public static Context getContext() {
        return mApplicationContext;
    }

    @Override
    public void onCreate() {
        mApplicationContext = getApplicationContext();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                Intent intent = new Intent(getApplicationContext(), CollectErrorActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("error", Log.getStackTraceString(throwable));
                startActivity(intent);
                Process.killProcess(Process.myPid());
                System.exit(1);
            }
        });
        super.onCreate();
        try {
            if (FirebaseApp.getApps(this) == null || FirebaseApp.getApps(this).isEmpty()) {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApplicationId("1:655995167352:android:29a0031e1c75901c30838d")
                        .setApiKey("AIzaSyBAljf_5JAAJfPHMQYtgCqqxYOGo2N4PI0")
                        .setProjectId("sketchware-pro-ia")
                        .setDatabaseUrl("https://sketchware-pro-ia-default-rtdb.firebaseio.com")
                        .setStorageBucket("sketchware-pro-ia.firebasestorage.app")
                        .setGcmSenderId("655995167352")
                        .build();
                FirebaseApp.initializeApp(this, options);
            }
        } catch (Throwable ignored) {}
        ThemeManager.applyTheme(this, ThemeManager.getCurrentTheme(this));
    }
}
