package model;

import interfaces.Calculavel;

public class AcaoSustentavel implements Calculavel {
    private int id;
    private String slug;
    private String nome;
    private String objetivoSustentavel;
    private int pontuacaoBase;
    private String nivelDificuldade;
    private String descricao;
    private int idOds; // 0 = ainda nao ligado a um ODS oficial

    public AcaoSustentavel(int id, String slug, String nome, String objetivoSustentavel, int pontuacaoBase,
                           String nivelDificuldade, String descricao, int idOds) {
        this.id = id;
        this.slug = slug;
        this.nome = nome;
        this.objetivoSustentavel = objetivoSustentavel;
        this.pontuacaoBase = pontuacaoBase;
        this.nivelDificuldade = nivelDificuldade;
        this.descricao = descricao;
        this.idOds = idOds;
    }

    public AcaoSustentavel(String slug, String nome, String objetivoSustentavel, int pontuacaoBase,
                           String nivelDificuldade, String descricao, int idOds) {
        this(0, slug, nome, objetivoSustentavel, pontuacaoBase, nivelDificuldade, descricao, idOds);
    }

    public boolean validarAcao() {
        return nome != null && !nome.isBlank()
                && slug != null && !slug.isBlank()
                && objetivoSustentavel != null && !objetivoSustentavel.isBlank()
                && pontuacaoBase > 0
                && ("BAIXA".equalsIgnoreCase(nivelDificuldade)
                || "MEDIA".equalsIgnoreCase(nivelDificuldade)
                || "ALTA".equalsIgnoreCase(nivelDificuldade));
    }

    // Pontuação = valor fixo cadastrado (pontuacaoBase), sem bônus por
    // dificuldade — alinhado exatamente com a regra do SoulUp MVP, que
    // usa o ponto fixo de cada atividade sem cálculo adicional.
    @Override
    public int calcularPontuacao() {
        return validarAcao() ? pontuacaoBase : 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getObjetivoSustentavel() {
        return objetivoSustentavel;
    }

    public void setObjetivoSustentavel(String objetivoSustentavel) {
        this.objetivoSustentavel = objetivoSustentavel;
    }

    public int getPontuacaoBase() {
        return pontuacaoBase;
    }

    public void setPontuacaoBase(int pontuacaoBase) {
        this.pontuacaoBase = pontuacaoBase;
    }

    public String getNivelDificuldade() {
        return nivelDificuldade;
    }

    public void setNivelDificuldade(String nivelDificuldade) {
        this.nivelDificuldade = nivelDificuldade;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getIdOds() {
        return idOds;
    }

    public void setIdOds(int idOds) {
        this.idOds = idOds;
    }
}
