package model;

import java.time.LocalDateTime;

public class Validacao {
    private int id;
    private int parceiroId;
    private int registroId;
    private LocalDateTime dataHoraValidacao;
    private String resultado;
    private String observacao;

    public Validacao(int id, int parceiroId, int registroId, LocalDateTime dataHoraValidacao,
                      String resultado, String observacao) {
        this.id = id;
        this.parceiroId = parceiroId;
        this.registroId = registroId;
        this.dataHoraValidacao = dataHoraValidacao;
        this.resultado = resultado;
        this.observacao = observacao;
    }

    public Validacao(int parceiroId, int registroId, String resultado, String observacao) {
        this(0, parceiroId, registroId, LocalDateTime.now(), resultado, observacao);
    }

    public boolean validarValidacao() {
        return parceiroId > 0
                && registroId > 0
                && ("APROVADO".equalsIgnoreCase(resultado) || "REPROVADO".equalsIgnoreCase(resultado));
    }

    public boolean foiAprovado() {
        return "APROVADO".equalsIgnoreCase(resultado);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParceiroId() {
        return parceiroId;
    }

    public void setParceiroId(int parceiroId) {
        this.parceiroId = parceiroId;
    }

    public int getRegistroId() {
        return registroId;
    }

    public void setRegistroId(int registroId) {
        this.registroId = registroId;
    }

    public LocalDateTime getDataHoraValidacao() {
        return dataHoraValidacao;
    }

    public void setDataHoraValidacao(LocalDateTime dataHoraValidacao) {
        this.dataHoraValidacao = dataHoraValidacao;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
