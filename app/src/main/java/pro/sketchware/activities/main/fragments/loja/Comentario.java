package pro.sketchware.activities.main.fragments.loja;

public class Comentario {
    private String id_comentario;
    private String id_app;
    private String icone_usuario;
    private String nome_autor;
    private String id_autor;
    private int estrelas;
    private String comentario;

    // Construtor padrão para Firebase
    public Comentario() {}

    // Getters e Setters
    public String getIdComentario() { return id_comentario; }
    public void setIdComentario(String id_comentario) { this.id_comentario = id_comentario; }

    public String getIdApp() { return id_app; }
    public void setIdApp(String id_app) { this.id_app = id_app; }

    public String getIconeUsuario() { return icone_usuario; }
    public void setIconeUsuario(String icone_usuario) { this.icone_usuario = icone_usuario; }

    public String getNomeAutor() { return nome_autor; }
    public void setNomeAutor(String nome_autor) { this.nome_autor = nome_autor; }

    public String getIdAutor() { return id_autor; }
    public void setIdAutor(String id_autor) { this.id_autor = id_autor; }

    public int getEstrelas() { return estrelas; }
    public void setEstrelas(int estrelas) { this.estrelas = estrelas; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
