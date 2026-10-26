package model;

import java.time.LocalDate;

public class DesafioAgendado {
    private int id;
    private int usuarioId;
    private int acaoId;
    private LocalDate prazo;

    public DesafioAgendado(int id, int usuarioId, int acaoId, LocalDate prazo) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.acaoId = acaoId;
        this.prazo = prazo;
    }

    public DesafioAgendado(int usuarioId, int acaoId, LocalDate prazo) {
        this(0, usuarioId, acaoId, prazo);
    }

    public boolean validarAgendamento() {
        return usuarioId > 0
                && acaoId > 0
                && prazo != null
                && !prazo.isBefore(LocalDate.now());
    }

    public boolean estaVencido() {
        return prazo != null && prazo.isBefore(LocalDate.now());
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

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getAcaoId() {
        return acaoId;
    }

    public void setAcaoId(int acaoId) {
        this.acaoId = acaoId;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public void setPrazo(LocalDate prazo) {
        this.prazo = prazo;
    }
}
