package model;

public class Conquista {
    public static final String CONTAGEM_TOTAL = "CONTAGEM_TOTAL";
    public static final String STREAK_DIAS = "STREAK_DIAS";
    public static final String ODS_DIFERENTES = "ODS_DIFERENTES";
    public static final String CAMPEAO_CAMPEONATO = "CAMPEAO_CAMPEONATO";

    private int id;
    private String nome;
    private String descricao;
    private String tipoCriterio;
    private int valorCriterio;
    private Integer odsRelacionadoId; // pode ser null (conquista geral)
    private String slugAcaoRelacionada; // pode ser null (conta o ODS inteiro, nao so 1 acao)
    private String icone;

    public Conquista(int id, String nome, String descricao, String tipoCriterio,
                      int valorCriterio, Integer odsRelacionadoId, String slugAcaoRelacionada, String icone) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.tipoCriterio = tipoCriterio;
        this.valorCriterio = valorCriterio;
        this.odsRelacionadoId = odsRelacionadoId;
        this.slugAcaoRelacionada = slugAcaoRelacionada;
        this.icone = icone;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getTipoCriterio() {
        return tipoCriterio;
    }

    public int getValorCriterio() {
        return valorCriterio;
    }

    public Integer getOdsRelacionadoId() {
        return odsRelacionadoId;
    }

    public String getSlugAcaoRelacionada() {
        return slugAcaoRelacionada;
    }

    public String getIcone() {
        return icone;
    }
}
