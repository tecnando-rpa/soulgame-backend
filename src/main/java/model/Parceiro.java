package model;

public class Parceiro {
    private int id;
    private String nome;
    private String tipo;
    private String cnpj;
    private String cidade;
    private String status;

    public Parceiro(int id, String nome, String tipo, String cnpj, String cidade, String status) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.cnpj = cnpj;
        this.cidade = cidade;
        this.status = status;
    }

    public Parceiro(String nome, String tipo, String cnpj, String cidade) {
        this(0, nome, tipo, cnpj, cidade, "ATIVO");
    }

    public boolean validarParceiro() {
        return nome != null && !nome.isBlank()
                && cnpj != null && cnpj.length() == 14
                && cidade != null && !cidade.isBlank()
                && ("ATIVO".equalsIgnoreCase(status)
                || "INATIVO".equalsIgnoreCase(status)
                || "EM_ANALISE".equalsIgnoreCase(status));
    }

    public boolean podeValidarAcoes() {
        return "ATIVO".equalsIgnoreCase(status);
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

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
