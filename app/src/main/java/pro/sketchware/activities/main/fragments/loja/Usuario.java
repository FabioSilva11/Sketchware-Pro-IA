package pro.sketchware.activities.main.fragments.loja;

public class Usuario {
    private String id;
    private String nome_completo;
    private String genero;
    private String email;
    private String foto_perfil;
    private String data_cadastro;
    private String status;

    // Construtor padrão para Firebase
    public Usuario() {}

    // Construtor com parâmetros
    public Usuario(String id, String nome_completo, String email) {
        this.id = id;
        this.nome_completo = nome_completo;
        this.email = email;
        this.status = "ativo";
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome_completo() { return nome_completo; }
    public void setNome_completo(String nome_completo) { this.nome_completo = nome_completo; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFoto_perfil() { return foto_perfil; }
    public void setFoto_perfil(String foto_perfil) { this.foto_perfil = foto_perfil; }

    public String getData_cadastro() { return data_cadastro; }
    public void setData_cadastro(String data_cadastro) { this.data_cadastro = data_cadastro; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Métodos utilitários
    public boolean isAtivo() {
        return "ativo".equalsIgnoreCase(status);
    }

    public String getDisplayName() {
        return nome_completo != null && !nome_completo.trim().isEmpty() 
            ? nome_completo 
            : "Usuário";
    }

    public String getInitials() {
        if (nome_completo == null || nome_completo.trim().isEmpty()) {
            return "U";
        }
        
        String[] parts = nome_completo.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        } else {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id='" + id + '\'' +
                ", nome_completo='" + nome_completo + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Usuario usuario = (Usuario) obj;
        return id != null ? id.equals(usuario.id) : usuario.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
