package model;

public class Ods {
    private int id;
    private String nome;
    private String cor;
    private String icone;

    public Ods(int id, String nome, String cor, String icone) {
        this.id = id;
        this.nome = nome;
        this.cor = cor;
        this.icone = icone;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCor() {
        return cor;
    }

    public String getIcone() {
        return icone;
    }
}
