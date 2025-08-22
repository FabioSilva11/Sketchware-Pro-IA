package pro.sketchware.activities.main.fragments.loja;

import java.util.Map;

public class AppItem {
    private String appId;
    private String nome;
    private String descricaoCurta;
    private String descricaoLonga;
    private String icone;
    private String urlDownload;
    private String dataPublicacao;
    private Publisher publisher;
    private Estatisticas estatisticas;
    private Map<String, Boolean> likes;
    private Map<String, Comentario> comentarios;

    // Construtor padrão para Firebase
    public AppItem() {}

    // Getters e Setters
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricaoCurta() { return descricaoCurta; }
    public void setDescricaoCurta(String descricaoCurta) { this.descricaoCurta = descricaoCurta; }

    public String getDescricaoLonga() { return descricaoLonga; }
    public void setDescricaoLonga(String descricaoLonga) { this.descricaoLonga = descricaoLonga; }

    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }

    public String getUrlDownload() { return urlDownload; }
    public void setUrlDownload(String urlDownload) { this.urlDownload = urlDownload; }

    public String getDataPublicacao() { return dataPublicacao; }
    public void setDataPublicacao(String dataPublicacao) { this.dataPublicacao = dataPublicacao; }

    public Publisher getPublisher() { return publisher; }
    public void setPublisher(Publisher publisher) { this.publisher = publisher; }

    public Estatisticas getEstatisticas() { return estatisticas; }
    public void setEstatisticas(Estatisticas estatisticas) { this.estatisticas = estatisticas; }

    public Map<String, Boolean> getLikes() { return likes; }
    public void setLikes(Map<String, Boolean> likes) { this.likes = likes; }



    public Map<String, Comentario> getComentarios() { return comentarios; }
    public void setComentarios(Map<String, Comentario> comentarios) { this.comentarios = comentarios; }

    // Classe interna para Publisher
    public static class Publisher {
        private String usuarioId;

        public Publisher() {}

        public String getUsuarioId() { return usuarioId; }
        public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    }

    // Classe interna para Estatisticas
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



    // Classe interna para Comentario
    public static class Comentario {
        private String comentario;
        private long timestamp;
        private String usuarioId;

        public Comentario() {}

        public String getComentario() { return comentario; }
        public void setComentario(String comentario) { this.comentario = comentario; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public String getUsuarioId() { return usuarioId; }
        public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    }
}
