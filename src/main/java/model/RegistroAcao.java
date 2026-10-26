package model;

import java.time.LocalDateTime;

public class RegistroAcao {
    private int id;
    private int acaoId;
    private int usuarioId;
    private LocalDateTime dataHoraRegistro;
    private String statusValidacao;
    private int pontosObtidos;
    private Double latitude;
    private Double longitude;

    public RegistroAcao(int id, int acaoId, int usuarioId, LocalDateTime dataHoraRegistro,
                        String statusValidacao, int pontosObtidos, Double latitude, Double longitude) {
        this.id = id;
        this.acaoId = acaoId;
        this.usuarioId = usuarioId;
        this.dataHoraRegistro = dataHoraRegistro;
        this.statusValidacao = statusValidacao;
        this.pontosObtidos = pontosObtidos;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public RegistroAcao(int acaoId, int usuarioId, String statusValidacao, int pontosObtidos) {
        this(0, acaoId, usuarioId, LocalDateTime.now(), statusValidacao, pontosObtidos, null, null);
    }

    public boolean validarRegistro() {
        return acaoId > 0
                && usuarioId > 0
                && pontosObtidos >= 0
                && ("PENDENTE".equalsIgnoreCase(statusValidacao)
                || "VALIDADO".equalsIgnoreCase(statusValidacao)
                || "REJEITADO".equalsIgnoreCase(statusValidacao));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAcaoId() {
        return acaoId;
    }

    public void setAcaoId(int acaoId) {
        this.acaoId = acaoId;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getDataHoraRegistro() {
        return dataHoraRegistro;
    }

    public void setDataHoraRegistro(LocalDateTime dataHoraRegistro) {
        this.dataHoraRegistro = dataHoraRegistro;
    }

    public String getStatusValidacao() {
        return statusValidacao;
    }

    public void setStatusValidacao(String statusValidacao) {
        this.statusValidacao = statusValidacao;
    }

    public int getPontosObtidos() {
        return pontosObtidos;
    }

    public void setPontosObtidos(int pontosObtidos) {
        this.pontosObtidos = pontosObtidos;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
