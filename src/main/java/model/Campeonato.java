package model;

import java.time.LocalDate;
import java.util.Set;

public class Campeonato {
    public static final Set<String> ESCOPOS_VALIDOS = Set.of("REGIONAL", "ESTADUAL", "NACIONAL");
    public static final Set<String> STATUS_VALIDOS = Set.of("AGENDADO", "EM_ANDAMENTO", "ENCERRADO");

    private int id;
    private String nome;
    private String escopo;
    private String estado; // null quando escopo == NACIONAL
    private Integer odsRelacionadoId;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String premiacao;
    private int usuarioCriadorId;
    private String status;

    public Campeonato(int id, String nome, String escopo, String estado, Integer odsRelacionadoId,
                       LocalDate dataInicio, LocalDate dataFim, String premiacao,
                       int usuarioCriadorId, String status) {
        this.id = id;
        this.nome = nome;
        this.escopo = escopo;
        this.estado = estado;
        this.odsRelacionadoId = odsRelacionadoId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.premiacao = premiacao;
        this.usuarioCriadorId = usuarioCriadorId;
        this.status = status;
    }

    public Campeonato(String nome, String escopo, String estado, Integer odsRelacionadoId,
                       LocalDate dataInicio, LocalDate dataFim, String premiacao, int usuarioCriadorId) {
        this(0, nome, escopo, estado, odsRelacionadoId, dataInicio, dataFim, premiacao,
                usuarioCriadorId, "AGENDADO");
    }

    // Regra de negocio central: so quem e ADMIN pode criar campeonato —
    // verificado pelo chamador (o Handler) antes de sequer montar este
    // objeto, mas a validacao de dados do campeonato em si mora aqui.
    public boolean validarCampeonato() {
        if (nome == null || nome.isBlank()) return false;
        if (escopo == null || !ESCOPOS_VALIDOS.contains(escopo)) return false;
        if (dataInicio == null || dataFim == null || !dataFim.isAfter(dataInicio)) return false;

        boolean precisaDeEstado = !"NACIONAL".equals(escopo);
        boolean temEstado = estado != null && estado.length() == 2;

        if (precisaDeEstado && !temEstado) return false;
        if (!precisaDeEstado && temEstado) return false;

        return true;
    }

    public boolean estaEmAndamento() {
        LocalDate hoje = LocalDate.now();
        return !hoje.isBefore(dataInicio) && !hoje.isAfter(dataFim);
    }

    public boolean jaEncerrou() {
        return LocalDate.now().isAfter(dataFim);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getEscopo() {
        return escopo;
    }

    public String getEstado() {
        return estado;
    }

    public Integer getOdsRelacionadoId() {
        return odsRelacionadoId;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public String getPremiacao() {
        return premiacao;
    }

    public int getUsuarioCriadorId() {
        return usuarioCriadorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
