package model;

public class CampeonatoParticipante {
    private int id;
    private int campeonatoId;
    private int usuarioId;
    private int pontosCampeonato;

    public CampeonatoParticipante(int id, int campeonatoId, int usuarioId, int pontosCampeonato) {
        this.id = id;
        this.campeonatoId = campeonatoId;
        this.usuarioId = usuarioId;
        this.pontosCampeonato = pontosCampeonato;
    }

    public CampeonatoParticipante(int campeonatoId, int usuarioId) {
        this(0, campeonatoId, usuarioId, 0);
    }

    public void adicionarPontos(int pontos) {
        if (pontos > 0) {
            pontosCampeonato += pontos;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCampeonatoId() {
        return campeonatoId;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public int getPontosCampeonato() {
        return pontosCampeonato;
    }

    public void setPontosCampeonato(int pontosCampeonato) {
        this.pontosCampeonato = pontosCampeonato;
    }
}
