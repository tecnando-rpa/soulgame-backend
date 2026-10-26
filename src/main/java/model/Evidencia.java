package model;

import java.time.LocalDateTime;

public class Evidencia {
    private int id;
    private int registroId;
    private String tipo;
    private String urlArquivo;
    private LocalDateTime dataHoraEnvio;
    private String descricao;
    private String hashEvidencia;

    public Evidencia(int id, int registroId, String tipo, String urlArquivo,
                      LocalDateTime dataHoraEnvio, String descricao, String hashEvidencia) {
        this.id = id;
        this.registroId = registroId;
        this.tipo = tipo;
        this.urlArquivo = urlArquivo;
        this.dataHoraEnvio = dataHoraEnvio;
        this.descricao = descricao;
        this.hashEvidencia = hashEvidencia;
    }

    public Evidencia(int registroId, String tipo, String urlArquivo, String descricao, String hashEvidencia) {
        this(0, registroId, tipo, urlArquivo, LocalDateTime.now(), descricao, hashEvidencia);
    }

    public boolean validarEvidencia() {
        return registroId > 0
                && urlArquivo != null && !urlArquivo.isBlank()
                && ("FOTO".equalsIgnoreCase(tipo)
                || "VIDEO".equalsIgnoreCase(tipo)
                || "DOCUMENTO".equalsIgnoreCase(tipo)
                || "QR_CODE".equalsIgnoreCase(tipo));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRegistroId() {
        return registroId;
    }

    public void setRegistroId(int registroId) {
        this.registroId = registroId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getUrlArquivo() {
        return urlArquivo;
    }

    public void setUrlArquivo(String urlArquivo) {
        this.urlArquivo = urlArquivo;
    }

    public LocalDateTime getDataHoraEnvio() {
        return dataHoraEnvio;
    }

    public void setDataHoraEnvio(LocalDateTime dataHoraEnvio) {
        this.dataHoraEnvio = dataHoraEnvio;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getHashEvidencia() {
        return hashEvidencia;
    }

    public void setHashEvidencia(String hashEvidencia) {
        this.hashEvidencia = hashEvidencia;
    }
}
