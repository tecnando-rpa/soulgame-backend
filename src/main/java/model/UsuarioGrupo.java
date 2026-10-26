package model;

import java.time.LocalDate;

// Entidade associativa (N:M entre Usuario e Grupo) — chave primária composta
// (id_usuario, id_grupo), exatamente como modelado em TB_USUARIO_GRUPO.
public class UsuarioGrupo {
    private int usuarioId;
    private int grupoId;
    private LocalDate dataEntrada;
    private int pontosNoGrupo;
    private String statusParticipacao;

    public UsuarioGrupo(int usuarioId, int grupoId, LocalDate dataEntrada,
                         int pontosNoGrupo, String statusParticipacao) {
        this.usuarioId = usuarioId;
        this.grupoId = grupoId;
        this.dataEntrada = dataEntrada;
        this.pontosNoGrupo = pontosNoGrupo;
        this.statusParticipacao = statusParticipacao;
    }

    public UsuarioGrupo(int usuarioId, int grupoId) {
        this(usuarioId, grupoId, LocalDate.now(), 0, "ATIVO");
    }

    public boolean validarParticipacao() {
        return usuarioId > 0
                && grupoId > 0
                && pontosNoGrupo >= 0
                && ("ATIVO".equalsIgnoreCase(statusParticipacao)
                || "INATIVO".equalsIgnoreCase(statusParticipacao)
                || "SUSPENSO".equalsIgnoreCase(statusParticipacao));
    }

    public void adicionarPontosNoGrupo(int pontos) {
        if (pontos > 0) {
            pontosNoGrupo += pontos;
        }
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(int grupoId) {
        this.grupoId = grupoId;
    }

    public LocalDate getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDate dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public int getPontosNoGrupo() {
        return pontosNoGrupo;
    }

    public void setPontosNoGrupo(int pontosNoGrupo) {
        this.pontosNoGrupo = pontosNoGrupo;
    }

    public String getStatusParticipacao() {
        return statusParticipacao;
    }

    public void setStatusParticipacao(String statusParticipacao) {
        this.statusParticipacao = statusParticipacao;
    }
}
