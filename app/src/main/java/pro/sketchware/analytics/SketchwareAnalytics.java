package pro.sketchware.analytics;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe de analytics personalizada para o Sketchware Pro IA
 * Rastreia eventos importantes do app para análise de uso
 */
public class SketchwareAnalytics {
    
    private static SketchwareAnalytics instance;
    private FirebaseAnalytics firebaseAnalytics;
    private Context context;
    
    // Eventos principais
    public static final String EVENT_APP_OPENED = "app_opened";
    public static final String EVENT_PROJECT_CREATED = "project_created";
    public static final String EVENT_PROJECT_OPENED = "project_opened";
    public static final String EVENT_PROJECT_BUILT = "project_built";
    public static final String EVENT_PROJECT_EXPORTED = "project_exported";
    public static final String EVENT_AI_FEATURE_USED = "ai_feature_used";
    public static final String EVENT_EDITOR_OPENED = "editor_opened";
    public static final String EVENT_LOGIC_EDITOR_USED = "logic_editor_used";
    public static final String EVENT_DESIGN_EDITOR_USED = "design_editor_used";
    public static final String EVENT_TEMPLATE_USED = "template_used";
    public static final String EVENT_BACKUP_CREATED = "backup_created";
    public static final String EVENT_BACKUP_RESTORED = "backup_restored";
    public static final String EVENT_USER_LOGIN = "user_login";
    public static final String EVENT_USER_LOGOUT = "user_logout";
    public static final String EVENT_SETTINGS_CHANGED = "settings_changed";
    public static final String EVENT_ERROR_OCCURRED = "error_occurred";
    public static final String EVENT_FEATURE_DISCOVERED = "feature_discovered";
    
    // Parâmetros
    public static final String PARAM_PROJECT_ID = "project_id";
    public static final String PARAM_PROJECT_NAME = "project_name";
    public static final String PARAM_PROJECT_TYPE = "project_type";
    public static final String PARAM_AI_MODEL = "ai_model";
    public static final String PARAM_AI_FEATURE = "ai_feature";
    public static final String PARAM_EDITOR_TYPE = "editor_type";
    public static final String PARAM_TEMPLATE_ID = "template_id";
    public static final String PARAM_TEMPLATE_CATEGORY = "template_category";
    public static final String PARAM_ERROR_TYPE = "error_type";
    public static final String PARAM_ERROR_MESSAGE = "error_message";
    public static final String PARAM_FEATURE_NAME = "feature_name";
    public static final String PARAM_SETTING_NAME = "setting_name";
    public static final String PARAM_SETTING_VALUE = "setting_value";
    public static final String PARAM_BUILD_TYPE = "build_type";
    public static final String PARAM_BUILD_DURATION = "build_duration";
    public static final String PARAM_BUILD_SUCCESS = "build_success";
    public static final String PARAM_USER_ID = "user_id";
    public static final String PARAM_USER_TYPE = "user_type";
    public static final String PARAM_SESSION_DURATION = "session_duration";
    public static final String PARAM_SCREEN_NAME = "screen_name";
    public static final String PARAM_ACTION_NAME = "action_name";
    
    private SketchwareAnalytics(Context context) {
        this.context = context.getApplicationContext();
        try {
            this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        } catch (Exception e) {
            // Firebase não configurado
            this.firebaseAnalytics = null;
        }
    }
    
    public static SketchwareAnalytics getInstance(Context context) {
        if (instance == null) {
            instance = new SketchwareAnalytics(context);
        }
        return instance;
    }
    
    /**
     * Registra a abertura do app
     */
    public void logAppOpened() {
        logEvent(EVENT_APP_OPENED, null);
    }
    
    /**
     * Registra criação de projeto
     */
    public void logProjectCreated(String projectId, String projectName, String projectType) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_PROJECT_ID, projectId);
        params.put(PARAM_PROJECT_NAME, projectName);
        params.put(PARAM_PROJECT_TYPE, projectType);
        logEvent(EVENT_PROJECT_CREATED, params);
    }
    
    /**
     * Registra abertura de projeto
     */
    public void logProjectOpened(String projectId, String projectName) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_PROJECT_ID, projectId);
        params.put(PARAM_PROJECT_NAME, projectName);
        logEvent(EVENT_PROJECT_OPENED, params);
    }
    
    /**
     * Registra compilação de projeto
     */
    public void logProjectBuilt(String projectId, String buildType, long buildDuration, boolean success) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_PROJECT_ID, projectId);
        params.put(PARAM_BUILD_TYPE, buildType);
        params.put(PARAM_BUILD_DURATION, buildDuration);
        params.put(PARAM_BUILD_SUCCESS, success);
        logEvent(EVENT_PROJECT_BUILT, params);
    }
    
    /**
     * Registra exportação de projeto
     */
    public void logProjectExported(String projectId, String exportType) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_PROJECT_ID, projectId);
        params.put(PARAM_BUILD_TYPE, exportType);
        logEvent(EVENT_PROJECT_EXPORTED, params);
    }
    
    /**
     * Registra uso de recursos de IA
     */
    public void logAiFeatureUsed(String aiFeature, String aiModel) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_AI_FEATURE, aiFeature);
        params.put(PARAM_AI_MODEL, aiModel);
        logEvent(EVENT_AI_FEATURE_USED, params);
    }
    
    /**
     * Registra abertura de editor
     */
    public void logEditorOpened(String editorType) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_EDITOR_TYPE, editorType);
        logEvent(EVENT_EDITOR_OPENED, params);
    }
    
    /**
     * Registra uso do editor de lógica
     */
    public void logLogicEditorUsed(String projectId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_PROJECT_ID, projectId);
        params.put(PARAM_EDITOR_TYPE, "logic");
        logEvent(EVENT_LOGIC_EDITOR_USED, params);
    }
    
    /**
     * Registra uso do editor de design
     */
    public void logDesignEditorUsed(String projectId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_PROJECT_ID, projectId);
        params.put(PARAM_EDITOR_TYPE, "design");
        logEvent(EVENT_DESIGN_EDITOR_USED, params);
    }
    
    /**
     * Registra uso de template
     */
    public void logTemplateUsed(String templateId, String templateCategory) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_TEMPLATE_ID, templateId);
        params.put(PARAM_TEMPLATE_CATEGORY, templateCategory);
        logEvent(EVENT_TEMPLATE_USED, params);
    }
    
    /**
     * Registra criação de backup
     */
    public void logBackupCreated(String projectId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_PROJECT_ID, projectId);
        logEvent(EVENT_BACKUP_CREATED, params);
    }
    
    /**
     * Registra restauração de backup
     */
    public void logBackupRestored(String projectId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_PROJECT_ID, projectId);
        logEvent(EVENT_BACKUP_RESTORED, params);
    }
    
    /**
     * Registra login do usuário
     */
    public void logUserLogin(String userId, String userType) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_USER_ID, userId);
        params.put(PARAM_USER_TYPE, userType);
        logEvent(EVENT_USER_LOGIN, params);
    }
    
    /**
     * Registra logout do usuário
     */
    public void logUserLogout(String userId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_USER_ID, userId);
        logEvent(EVENT_USER_LOGOUT, params);
    }
    
    /**
     * Registra mudança de configuração
     */
    public void logSettingsChanged(String settingName, String settingValue) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_SETTING_NAME, settingName);
        params.put(PARAM_SETTING_VALUE, settingValue);
        logEvent(EVENT_SETTINGS_CHANGED, params);
    }
    
    /**
     * Registra erro ocorrido
     */
    public void logErrorOccurred(String errorType, String errorMessage) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_ERROR_TYPE, errorType);
        params.put(PARAM_ERROR_MESSAGE, errorMessage);
        logEvent(EVENT_ERROR_OCCURRED, params);
    }
    
    /**
     * Registra descoberta de funcionalidade
     */
    public void logFeatureDiscovered(String featureName) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_FEATURE_NAME, featureName);
        logEvent(EVENT_FEATURE_DISCOVERED, params);
    }
    
    /**
     * Registra visualização de tela
     */
    public void logScreenView(String screenName) {
        if (firebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
            bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
        }
    }
    
    /**
     * Registra evento customizado
     */
    public void logEvent(String eventName, Map<String, Object> parameters) {
        if (firebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            if (parameters != null) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        bundle.putString(entry.getKey(), (String) value);
                    } else if (value instanceof Integer) {
                        bundle.putInt(entry.getKey(), (Integer) value);
                    } else if (value instanceof Long) {
                        bundle.putLong(entry.getKey(), (Long) value);
                    } else if (value instanceof Boolean) {
                        bundle.putBoolean(entry.getKey(), (Boolean) value);
                    } else if (value instanceof Double) {
                        bundle.putDouble(entry.getKey(), (Double) value);
                    } else if (value instanceof Float) {
                        bundle.putFloat(entry.getKey(), (Float) value);
                    }
                }
            }
            firebaseAnalytics.logEvent(eventName, bundle);
        }
    }
    
    /**
     * Define propriedades do usuário
     */
    public void setUserProperty(String propertyName, String propertyValue) {
        if (firebaseAnalytics != null) {
            firebaseAnalytics.setUserProperty(propertyName, propertyValue);
        }
    }
    
    /**
     * Define ID do usuário
     */
    public void setUserId(String userId) {
        if (firebaseAnalytics != null) {
            firebaseAnalytics.setUserId(userId);
        }
    }
    
    /**
     * Verifica se o analytics está disponível
     */
    public boolean isAvailable() {
        return firebaseAnalytics != null;
    }
}

