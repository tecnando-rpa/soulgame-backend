package model;

import java.time.LocalDate;

public class Grupo {
    private int id;
    private String nome;
    private String tipo;
    private String cidade;
    private String bairro;
    private int pontosMinimosEntrada;
    private LocalDate dataCriacao;

    public Grupo(int id, String nome, String tipo, String cidade, String bairro,
                 int pontosMinimosEntrada, LocalDate dataCriacao) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.cidade = cidade;
        this.bairro = bairro;
        this.pontosMinimosEntrada = pontosMinimosEntrada;
        this.dataCriacao = dataCriacao;
    }

    public Grupo(String nome, String tipo, String cidade, String bairro, int pontosMinimosEntrada) {
        this(0, nome, tipo, cidade, bairro, pontosMinimosEntrada, LocalDate.now());
    }

    public boolean validarGrupo() {
        return nome != null && !nome.isBlank()
                && ("ABERTO".equalsIgnoreCase(tipo) || "PRIVADO".equalsIgnoreCase(tipo))
                && pontosMinimosEntrada >= 0;
    }

    public boolean permiteEntrada(int pontosUsuario) {
        return "ABERTO".equalsIgnoreCase(tipo) && pontosUsuario >= pontosMinimosEntrada;
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

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public int getPontosMinimosEntrada() {
        return pontosMinimosEntrada;
    }

    public void setPontosMinimosEntrada(int pontosMinimosEntrada) {
        this.pontosMinimosEntrada = pontosMinimosEntrada;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
