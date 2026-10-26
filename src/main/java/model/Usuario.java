package model;

import java.time.LocalDate;
import java.util.Set;

public class Usuario {
    // 7 tipos de conta reais do SoulUp + admin — mesmo conjunto do
    // CHECK constraint CK_USUARIO_TIPO no banco.
    public static final Set<String> TIPOS_VALIDOS = Set.of(
            "soulgamer", "soulfluencer", "soulhealth", "soulsport",
            "soulbrain", "educasoul", "ecosoul", "admin"
    );

    // Tipos que emitem chave de validação para outras contas (papel
    // "validador" no fluxo de comprovação de atividades).
    public static final Set<String> TIPOS_VALIDADORES = Set.of(
            "soulhealth", "soulsport", "educasoul", "ecosoul"
    );

    // Tipos que têm código de indicação próprio e recebem pontos em
    // dobro de quem indicarem (papel "multiplicador").
    public static final Set<String> TIPOS_MULTIPLICADORES = Set.of(
            "soulfluencer", "soulsport", "soulbrain", "ecosoul"
    );

    private int id;
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private LocalDate dataNascimento;
    private String cidade;
    private String estado;
    private String tipo;
    private int totalPontos;
    private int nivel;
    private int streakDias;
    private String codigoIndicacao;
    private String indicadoPorCodigo;
    private String avatarTipo;
    private String avatarPersonagemId;
    private String avatarFotoBase64;

    // Campos legados do Sprint de Banco de Dados (opcionais no SoulUp,
    // mantidos por compatibilidade com o schema já entregue).
    private String cpf;
    private String bairro;

    public Usuario(
            int id, String nome, String email, String senha, String telefone,
            LocalDate dataNascimento, String cidade, String estado, String tipo,
            int totalPontos, int nivel, int streakDias, String codigoIndicacao,
            String indicadoPorCodigo, String avatarTipo, String avatarPersonagemId,
            String avatarFotoBase64, String cpf, String bairro
    ) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
        this.cidade = cidade;
        this.estado = estado;
        this.tipo = tipo;
        this.totalPontos = totalPontos;
        this.nivel = nivel;
        this.streakDias = streakDias;
        this.codigoIndicacao = codigoIndicacao;
        this.indicadoPorCodigo = indicadoPorCodigo;
        this.avatarTipo = avatarTipo;
        this.avatarPersonagemId = avatarPersonagemId;
        this.avatarFotoBase64 = avatarFotoBase64;
        this.cpf = cpf;
        this.bairro = bairro;
    }

    // Construtor simplificado para cadastro novo (sem id, pontos, nivel,
    // streak — todos nascem zerados/no valor inicial).
    public Usuario(
            String nome, String email, String senha, String telefone,
            LocalDate dataNascimento, String cidade, String estado, String tipo
    ) {
        this(
                0, nome, email, senha, telefone, dataNascimento, cidade, estado,
                tipo, 0, 1, 0, null, null, "personagem", "explorador", null, null, null
        );
    }

    public boolean validarCadastro() {
        return nome != null && !nome.isBlank()
                && email != null && email.contains("@")
                && senha != null && !senha.isBlank()
                && cidade != null && !cidade.isBlank()
                && estado != null && estado.length() == 2
                && tipo != null && TIPOS_VALIDOS.contains(tipo)
                && totalPontos >= 0
                && nivel >= 1
                && streakDias >= 0;
    }

    public boolean ehValidador() {
        return TIPOS_VALIDADORES.contains(tipo);
    }

    public boolean ehMultiplicador() {
        return TIPOS_MULTIPLICADORES.contains(tipo);
    }

    public void adicionarPontos(int pontos) {
        if (pontos > 0) {
            totalPontos += pontos;
            recalcularNivel();
        }
    }

    // Regra de negócio: a cada 500 pontos acumulados, sobe um nível
    // (mesma regra já usada no front-end do SoulUp).
    private void recalcularNivel() {
        int novoNivel = (totalPontos / 500) + 1;
        if (novoNivel > nivel) {
            nivel = novoNivel;
        }
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getTotalPontos() {
        return totalPontos;
    }

    public void setTotalPontos(int totalPontos) {
        this.totalPontos = totalPontos;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public int getStreakDias() {
        return streakDias;
    }

    public void setStreakDias(int streakDias) {
        this.streakDias = streakDias;
    }

    public String getCodigoIndicacao() {
        return codigoIndicacao;
    }

    public void setCodigoIndicacao(String codigoIndicacao) {
        this.codigoIndicacao = codigoIndicacao;
    }

    public String getIndicadoPorCodigo() {
        return indicadoPorCodigo;
    }

    public void setIndicadoPorCodigo(String indicadoPorCodigo) {
        this.indicadoPorCodigo = indicadoPorCodigo;
    }

    public String getAvatarTipo() {
        return avatarTipo;
    }

    public void setAvatarTipo(String avatarTipo) {
        this.avatarTipo = avatarTipo;
    }

    public String getAvatarPersonagemId() {
        return avatarPersonagemId;
    }

    public void setAvatarPersonagemId(String avatarPersonagemId) {
        this.avatarPersonagemId = avatarPersonagemId;
    }

    public String getAvatarFotoBase64() {
        return avatarFotoBase64;
    }

    public void setAvatarFotoBase64(String avatarFotoBase64) {
        this.avatarFotoBase64 = avatarFotoBase64;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }
}
