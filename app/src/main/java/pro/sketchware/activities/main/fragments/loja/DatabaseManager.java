package pro.sketchware.activities.main.fragments.loja;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static DatabaseManager instance;
    private DatabaseReference databaseReference;
    private Map<String, Usuario> usuariosCache = new HashMap<>();
    private List<AppItem.Comentario> comentariosCache = new ArrayList<>();

    private DatabaseManager() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public interface AppsLoadCallback {
        void onAppsLoaded(List<AppItem> apps);
        void onError(String error);
    }

    public interface UsuariosLoadCallback {
        void onUsuariosLoaded(Map<String, Usuario> usuarios);
        void onError(String error);
    }

    public interface ComentariosLoadCallback {
        void onComentariosLoaded(List<AppItem.Comentario> comentarios);
        void onError(String error);
    }

    // Carrega dados completos do novo formato JSON
    public void loadCompleteData(AppsLoadCallback callback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    List<AppItem> apps = new ArrayList<>();
                    Map<String, Usuario> usuarios = new HashMap<>();
                    List<AppItem.Comentario> comentarios = new ArrayList<>();

                    // Carregar usuários primeiro
                    DataSnapshot usuariosSnapshot = dataSnapshot.child("usuarios");
                    for (DataSnapshot userSnapshot : usuariosSnapshot.getChildren()) {
                        Usuario usuario = userSnapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            usuario.setId(userSnapshot.getKey());
                            usuarios.put(userSnapshot.getKey(), usuario);
                        }
                    }
                    usuariosCache = usuarios;

                    // Carregar comentários
                    DataSnapshot comentariosSnapshot = dataSnapshot.child("comentarios");
                    for (DataSnapshot comentarioSnapshot : comentariosSnapshot.getChildren()) {
                        AppItem.Comentario comentario = comentarioSnapshot.getValue(AppItem.Comentario.class);
                        if (comentario != null) {
                            comentarios.add(comentario);
                        }
                    }
                    comentariosCache = comentarios;

                    // Carregar apps
                    DataSnapshot appsSnapshot = dataSnapshot.child("apps");
                    for (DataSnapshot appSnapshot : appsSnapshot.getChildren()) {
                        AppItem app = convertToAppItem(appSnapshot, usuarios, comentarios);
                        if (app != null) {
                            app.setAppId(appSnapshot.getKey());
                            apps.add(app);
                        }
                    }

                    callback.onAppsLoaded(apps);
                } catch (Exception e) {
                    callback.onError("Erro ao processar dados: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Erro do Firebase: " + databaseError.getMessage());
            }
        });
    }

    private AppItem convertToAppItem(DataSnapshot appSnapshot, Map<String, Usuario> usuarios, List<AppItem.Comentario> comentarios) {
        try {
            AppItem app = new AppItem();
            
            // Carregar dados básicos do app
            app.setAppId(appSnapshot.getKey());
            app.setNome_app(appSnapshot.child("nome_app").getValue(String.class));
            app.setDescricao_curta(appSnapshot.child("descricao_curta").getValue(String.class));
            app.setDescricao_longa(appSnapshot.child("descricao_longa").getValue(String.class));
            app.setCategoria(appSnapshot.child("categoria").getValue(String.class));
            app.setIcone(appSnapshot.child("icone").getValue(String.class));
            app.setVersao(appSnapshot.child("versao").getValue(String.class));
            app.setData_publicacao(appSnapshot.child("data_publicacao").getValue(String.class));
            app.setStatus(appSnapshot.child("status").getValue(String.class));
            app.setLocation_download(appSnapshot.child("location_download").getValue(String.class));
            app.setTipo_download(appSnapshot.child("tipo_download").getValue(String.class));

            // Carregar valores numéricos com verificação de tipo
            Long downloads = appSnapshot.child("downloads").getValue(Long.class);
            app.setDownloads(downloads != null ? downloads.intValue() : 0);

            Double avaliacaoMedia = appSnapshot.child("avaliacao_media").getValue(Double.class);
            app.setAvaliacao_media(avaliacaoMedia != null ? avaliacaoMedia : 0.0);

            Long numeroAvaliacoes = appSnapshot.child("numero_avaliacoes").getValue(Long.class);
            app.setNumero_avaliacoes(numeroAvaliacoes != null ? numeroAvaliacoes.intValue() : 0);

            // Carregar screenshots como Map
            Map<String, String> screenshots = new HashMap<>();
            DataSnapshot screenshotsSnapshot = appSnapshot.child("screenshots");
            for (DataSnapshot screenshotSnapshot : screenshotsSnapshot.getChildren()) {
                String screenshot = screenshotSnapshot.getValue(String.class);
                if (screenshot != null) {
                    screenshots.put(screenshotSnapshot.getKey(), screenshot);
                }
            }
            app.setScreenshots(screenshots);

            // Carregar autor
            DataSnapshot autorSnapshot = appSnapshot.child("autor");
            if (autorSnapshot.exists()) {
                AppItem.Autor autor = new AppItem.Autor();
                autor.setId(autorSnapshot.child("id").getValue(String.class));
                autor.setNome(autorSnapshot.child("nome").getValue(String.class));
                app.setAutor(autor);
            }

            // Filtrar comentários deste app
            Map<String, AppItem.Comentario> comentariosApp = new HashMap<>();
            for (AppItem.Comentario comentario : comentarios) {
                if (appSnapshot.getKey().equals(comentario.getId_app())) {
                    comentariosApp.put(comentario.getId_comentario(), comentario);
                }
            }
            app.setComentarios(comentariosApp);

            return app;
        } catch (Exception e) {
            System.err.println("Erro ao converter app: " + appSnapshot.getKey() + " - " + e.getMessage());
            return null;
        }
    }

    // Carregar apenas apps (formato antigo para compatibilidade)
    public void loadAppsOnly(AppsLoadCallback callback) {
        databaseReference.child("apps").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<AppItem> apps = new ArrayList<>();
                
                for (DataSnapshot appSnapshot : dataSnapshot.getChildren()) {
                    try {
                        AppItem app = appSnapshot.getValue(AppItem.class);
                        if (app != null) {
                            app.setAppId(appSnapshot.getKey());
                            apps.add(app);
                        }
                    } catch (Exception e) {
                        System.err.println("Erro ao carregar app: " + e.getMessage());
                    }
                }
                
                callback.onAppsLoaded(apps);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void loadUsuarios(UsuariosLoadCallback callback) {
        databaseReference.child("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Usuario> usuarios = new HashMap<>();
                
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Usuario usuario = userSnapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            usuario.setId(userSnapshot.getKey());
                            usuarios.put(userSnapshot.getKey(), usuario);
                        }
                    } catch (Exception e) {
                        System.err.println("Erro ao carregar usuário: " + e.getMessage());
                    }
                }
                
                usuariosCache = usuarios;
                callback.onUsuariosLoaded(usuarios);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void loadComentarios(ComentariosLoadCallback callback) {
        databaseReference.child("comentarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<AppItem.Comentario> comentarios = new ArrayList<>();
                
                for (DataSnapshot comentarioSnapshot : dataSnapshot.getChildren()) {
                    try {
                        AppItem.Comentario comentario = comentarioSnapshot.getValue(AppItem.Comentario.class);
                        if (comentario != null) {
                            comentarios.add(comentario);
                        }
                    } catch (Exception e) {
                        System.err.println("Erro ao carregar comentário: " + e.getMessage());
                    }
                }
                
                comentariosCache = comentarios;
                callback.onComentariosLoaded(comentarios);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public Usuario getUsuarioFromCache(String userId) {
        return usuariosCache.get(userId);
    }

    public List<AppItem.Comentario> getComentariosForApp(String appId) {
        List<AppItem.Comentario> comentariosApp = new ArrayList<>();
        for (AppItem.Comentario comentario : comentariosCache) {
            if (appId.equals(comentario.getId_app())) {
                comentariosApp.add(comentario);
            }
        }
        return comentariosApp;
    }
}
