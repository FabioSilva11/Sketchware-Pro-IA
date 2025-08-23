package pro.sketchware.activities.main.fragments.loja;

import java.util.List;
import java.util.Map;
import pro.sketchware.activities.main.fragments.loja.CategoryManager;

public class AppItem {
    // Novos campos do formato JSON atualizado
    private String id;
    private String package_name;
    private String nome_app;
    private String versao;
    private String descricao_curta;
    private String descricao_longa;
    private String categoria;
    private String icone;
    private int downloads;
    private double avaliacao_media;
    private int numero_avaliacoes;
    private Map<String, String> screenshots;
    private String location_download;
    private String data_publicacao;
    private Autor autor;
    
    // Campos de compatibilidade com formato antigo
    private String appId;
    private String nome;
    private String descricaoCurta;
    private String descricaoLonga;
    private String urlDownload;
    private String dataPublicacao;
    private Publisher publisher;
    private Estatisticas estatisticas;
    private Map<String, Boolean> likes;
    private Map<String, Comentario> comentarios;

    // Construtor padrão para Firebase
    public AppItem() {}

    // Getters e Setters para novos campos
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPackageName() { return package_name; }
    public void setPackageName(String package_name) { this.package_name = package_name; }

    public String getNomeApp() { return nome_app; }
    public void setNomeApp(String nome_app) { this.nome_app = nome_app; }

    public String getVersao() { return versao; }
    public void setVersao(String versao) { this.versao = versao; }

    public String getDescricaoCurta() { return descricao_curta; }
    public void setDescricaoCurta(String descricao_curta) { this.descricao_curta = descricao_curta; }

    public String getDescricaoLonga() { return descricao_longa; }
    public void setDescricaoLonga(String descricao_longa) { this.descricao_longa = descricao_longa; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }

    public int getDownloads() { return downloads; }
    public void setDownloads(int downloads) { this.downloads = downloads; }

    public double getAvaliacaoMedia() { return avaliacao_media; }
    public void setAvaliacaoMedia(double avaliacao_media) { this.avaliacao_media = avaliacao_media; }

    public int getNumeroAvaliacoes() { return numero_avaliacoes; }
    public void setNumeroAvaliacoes(int numero_avaliacoes) { this.numero_avaliacoes = numero_avaliacoes; }

    public Map<String, String> getScreenshots() { return screenshots; }
    public void setScreenshots(Map<String, String> screenshots) { this.screenshots = screenshots; }

    public String getLocationDownload() { return location_download; }
    public void setLocationDownload(String location_download) { this.location_download = location_download; }

    public String getDataPublicacao() { return data_publicacao; }
    public void setDataPublicacao(String data_publicacao) { this.data_publicacao = data_publicacao; }

    public Autor getAutor() { return autor; }
    public void setAutor(Autor autor) { this.autor = autor; }

    // Additional getter/setter methods with underscore naming for Firebase compatibility
    public String getNome_app() { return nome_app; }
    public void setNome_app(String nome_app) { this.nome_app = nome_app; }

    public String getDescricao_curta() { return descricao_curta; }
    public void setDescricao_curta(String descricao_curta) { this.descricao_curta = descricao_curta; }

    public String getDescricao_longa() { return descricao_longa; }
    public void setDescricao_longa(String descricao_longa) { this.descricao_longa = descricao_longa; }

    public String getData_publicacao() { return data_publicacao; }
    public void setData_publicacao(String data_publicacao) { this.data_publicacao = data_publicacao; }

    public String getLocation_download() { return location_download; }
    public void setLocation_download(String location_download) { this.location_download = location_download; }

    public double getAvaliacao_media() { return avaliacao_media; }
    public void setAvaliacao_media(double avaliacao_media) { this.avaliacao_media = avaliacao_media; }

    public int getNumero_avaliacoes() { return numero_avaliacoes; }
    public void setNumero_avaliacoes(int numero_avaliacoes) { this.numero_avaliacoes = numero_avaliacoes; }

    // Status field and methods
    private String status;
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Tipo download field and methods  
    private String tipo_download;
    public String getTipo_download() { return tipo_download; }
    public void setTipo_download(String tipo_download) { this.tipo_download = tipo_download; }

    // Getters e Setters de compatibilidade (delegam para novos campos)
    public String getAppId() { 
        return id != null ? id : appId; 
    }
    public void setAppId(String appId) { 
        this.appId = appId;
        if (id == null) this.id = appId;
    }

    public String getNome() { 
        return nome_app != null ? nome_app : nome; 
    }
    public void setNome(String nome) { 
        this.nome = nome;
        if (nome_app == null) this.nome_app = nome;
    }

    public String getUrlDownload() { 
        return location_download != null ? location_download : urlDownload; 
    }
    public void setUrlDownload(String urlDownload) { 
        this.urlDownload = urlDownload;
        if (location_download == null) this.location_download = urlDownload;
    }

    public Publisher getPublisher() { return publisher; }
    public void setPublisher(Publisher publisher) { this.publisher = publisher; }

    public Estatisticas getEstatisticas() { return estatisticas; }
    public void setEstatisticas(Estatisticas estatisticas) { this.estatisticas = estatisticas; }

    public Map<String, Boolean> getLikes() { return likes; }
    public void setLikes(Map<String, Boolean> likes) { this.likes = likes; }

    public Map<String, Comentario> getComentarios() { return comentarios; }
    public void setComentarios(Map<String, Comentario> comentarios) { this.comentarios = comentarios; }

    // Método para obter estatísticas adaptadas (compatibilidade)
    public Estatisticas getEstatisticasAdaptadas() {
        if (estatisticas != null) {
            return estatisticas;
        }
        
        // Criar estatísticas a partir dos novos campos
        Estatisticas adaptadas = new Estatisticas();
        adaptadas.setDownloads(downloads);
        adaptadas.setComentarios(numero_avaliacoes);
        adaptadas.setLikes(0); // Não temos likes no novo formato
        return adaptadas;
    }

    // Método para obter o nome do desenvolvedor com fallback
    public String getDeveloperName() {
        if (autor != null && autor.getNome() != null && !autor.getNome().trim().isEmpty()) {
            return autor.getNome();
        }
        if (publisher != null && publisher.getUsuarioId() != null && !publisher.getUsuarioId().trim().isEmpty()) {
            return publisher.getUsuarioId();
        }
        return "Desenvolvedor Desconhecido";
    }

    // Método para obter o ID do desenvolvedor
    public String getDeveloperId() {
        if (autor != null && autor.getId() != null && !autor.getId().trim().isEmpty()) {
            return autor.getId();
        }
        if (publisher != null && publisher.getUsuarioId() != null && !publisher.getUsuarioId().trim().isEmpty()) {
            return publisher.getUsuarioId();
        }
        return null;
    }

    // Método para obter a categoria com fallback
    public String getCategoryDisplay() {
        // Primeiro, tentar usar a categoria definida
        if (categoria != null && !categoria.trim().isEmpty()) {
            return CategoryManager.getCategoryDisplay(categoria);
        }
        
        // Se não há categoria definida, tentar inferir da descrição
        String descricao = descricao_curta != null ? descricao_curta : descricaoCurta;
        if (descricao != null && !descricao.trim().isEmpty()) {
            String inferredCategory = CategoryManager.getCategoryFromDescription(descricao);
            if (inferredCategory != null) {
                return CategoryManager.getCategoryDisplay(inferredCategory);
            }
        }
        
        // Se não há descrição curta, tentar da descrição longa
        String descricaoLonga = this.descricao_longa != null ? this.descricao_longa : this.descricaoLonga;
        if (descricaoLonga != null && !descricaoLonga.trim().isEmpty()) {
            String inferredCategory = CategoryManager.getCategoryFromDescription(descricaoLonga);
            if (inferredCategory != null) {
                return CategoryManager.getCategoryDisplay(inferredCategory);
            }
        }
        
        return "Categoria não definida";
    }

    // Classe interna para Autor (novo formato)
    public static class Autor {
        private String id;
        private String nome;
        private String icone;

        public Autor() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }

        public String getIcone() { return icone; }
        public void setIcone(String icone) { this.icone = icone; }
    }

    // Classe interna para Publisher (formato antigo - compatibilidade)
    public static class Publisher {
        private String usuarioId;

        public Publisher() {}

        public String getUsuarioId() { return usuarioId; }
        public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    }

    // Classe interna para Estatisticas (formato antigo - compatibilidade)
    public static class Estatisticas {
        private int comentarios;
        private int downloads;
        private int likes;

        public Estatisticas() {}

        public int getComentarios() { return comentarios; }
        public void setComentarios(int comentarios) { this.comentarios = comentarios; }

        public int getDownloads() { return downloads; }
        public void setDownloads(int downloads) { this.downloads = downloads; }

        public int getLikes() { return likes; }
        public void setLikes(int likes) { this.likes = likes; }
    }

    // Classe interna para Comentario (formato antigo - compatibilidade)
    public static class Comentario {
        private String comentario;
        private long timestamp;
        private String usuarioId;
        private String id_comentario;
        private String id_app;
        private String id_autor;
        private String nome_autor;
        private int estrelas;

        public Comentario() {}

        public String getComentario() { return comentario; }
        public void setComentario(String comentario) { this.comentario = comentario; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public String getUsuarioId() { return usuarioId; }
        public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

        public String getId_comentario() { return id_comentario; }
        public void setId_comentario(String id_comentario) { this.id_comentario = id_comentario; }

        public String getId_app() { return id_app; }
        public void setId_app(String id_app) { this.id_app = id_app; }

        public String getId_autor() { return id_autor; }
        public void setId_autor(String id_autor) { this.id_autor = id_autor; }

        public String getNome_autor() { return nome_autor; }
        public void setNome_autor(String nome_autor) { this.nome_autor = nome_autor; }

        public int getEstrelas() { return estrelas; }
        public void setEstrelas(int estrelas) { this.estrelas = estrelas; }

        // Additional getter/setter methods for consistency
        public String getIdComentario() { return id_comentario; }
        public void setIdComentario(String idComentario) { this.id_comentario = idComentario; }

        public String getIdApp() { return id_app; }
        public void setIdApp(String idApp) { this.id_app = idApp; }

        public String getIdAutor() { return id_autor; }
        public void setIdAutor(String idAutor) { this.id_autor = idAutor; }

        public String getNomeAutor() { return nome_autor; }
        public void setNomeAutor(String nomeAutor) { this.nome_autor = nomeAutor; }
    }
}
