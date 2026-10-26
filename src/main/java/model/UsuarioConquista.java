package model;

import java.time.LocalDateTime;

public class UsuarioConquista {
    private int id;
    private int usuarioId;
    private int conquistaId;
    private LocalDateTime dataDesbloqueio;

    public UsuarioConquista(int id, int usuarioId, int conquistaId, LocalDateTime dataDesbloqueio) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.conquistaId = conquistaId;
        this.dataDesbloqueio = dataDesbloqueio;
    }

    public UsuarioConquista(int usuarioId, int conquistaId) {
        this(0, usuarioId, conquistaId, LocalDateTime.now());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public int getConquistaId() {
        return conquistaId;
    }

    public LocalDateTime getDataDesbloqueio() {
        return dataDesbloqueio;
    }
}
