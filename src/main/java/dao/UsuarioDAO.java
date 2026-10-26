package dao;

import conexao.ConexaoOracleProjeto;
import model.Usuario;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public boolean criar(Usuario usuario) {
        Connection connection = null;
        PreparedStatement statement = null;

        // IMPORTANTE: NÃO usar Statement.RETURN_GENERATED_KEYS aqui.
        // O driver JDBC do Oracle (ojdbc11) tem um bug conhecido (ORA-17132)
        // quando a tabela tem uma coluna CLOB (avatar_foto_usuario) e o
        // INSERT tenta retornar a chave gerada ao mesmo tempo. A correção
        // é inserir normalmente e, depois, buscar o ID com um SELECT separado.
        String sql = "INSERT INTO TB_USUARIO "
                + "(nome_usuario, email_usuario, senha_usuario, telefone_usuario, data_nascimento_usuario, "
                + "cidade_usuario, estado_usuario, tipo_usuario, total_pontos_usuario, nivel_usuario, "
                + "streak_dias_usuario, codigo_indicacao_usuario, indicado_por_codigo_usuario, "
                + "avatar_tipo_usuario, avatar_personagem_id_usuario, avatar_foto_usuario, cpf_usuario, bairro_usuario) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            preencher(statement, usuario);

            if (statement.executeUpdate() != 1) {
                return false;
            }

            Integer idGerado = buscarIdPorEmail(usuario.getEmail());
            if (idGerado != null) {
                usuario.setId(idGerado);
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao criar usuario: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private Integer buscarIdPorEmail(String email) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setString(1, email);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id_usuario");
            }

            return null;
        } catch (SQLException e) {
            System.out.println("Erro ao buscar id do usuario: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public Usuario buscarPorId(int id) {
        return buscarPorCampo("id_usuario = ?", id);
    }

    public Usuario buscarPorEmail(String email) {
        return buscarPorCampo("email_usuario = ?", email);
    }

    public Usuario buscarPorCodigoIndicacao(String codigo) {
        return buscarPorCampo("codigo_indicacao_usuario = ?", codigo);
    }

    private Usuario buscarPorCampo(String condicaoWhere, Object valor) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT * FROM TB_USUARIO WHERE " + condicaoWhere;

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);

            if (valor instanceof Integer numero) {
                statement.setInt(1, numero);
            } else {
                statement.setString(1, (String) valor);
            }

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return montar(resultSet);
            }

            return null;
        } catch (SQLException e) {
            System.out.println("Erro ao buscar usuario: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public List<Usuario> listarTodos() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Usuario> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_USUARIO ORDER BY total_pontos_usuario DESC";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar usuarios: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    public boolean atualizar(Usuario usuario) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "UPDATE TB_USUARIO SET "
                + "nome_usuario = ?, email_usuario = ?, senha_usuario = ?, telefone_usuario = ?, "
                + "data_nascimento_usuario = ?, cidade_usuario = ?, estado_usuario = ?, tipo_usuario = ?, "
                + "total_pontos_usuario = ?, nivel_usuario = ?, streak_dias_usuario = ?, "
                + "codigo_indicacao_usuario = ?, indicado_por_codigo_usuario = ?, avatar_tipo_usuario = ?, "
                + "avatar_personagem_id_usuario = ?, avatar_foto_usuario = ?, cpf_usuario = ?, bairro_usuario = ? "
                + "WHERE id_usuario = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            preencher(statement, usuario);
            statement.setInt(19, usuario.getId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar usuario: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    public boolean excluir(int id) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "DELETE FROM TB_USUARIO WHERE id_usuario = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir usuario: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private void preencher(PreparedStatement statement, Usuario usuario) throws SQLException {
        statement.setString(1, usuario.getNome());
        statement.setString(2, usuario.getEmail());
        statement.setString(3, usuario.getSenha());
        statement.setString(4, usuario.getTelefone());
        statement.setDate(5, usuario.getDataNascimento() != null ? Date.valueOf(usuario.getDataNascimento()) : null);
        statement.setString(6, usuario.getCidade());
        statement.setString(7, usuario.getEstado());
        statement.setString(8, usuario.getTipo());
        statement.setInt(9, usuario.getTotalPontos());
        statement.setInt(10, usuario.getNivel());
        statement.setInt(11, usuario.getStreakDias());
        statement.setString(12, usuario.getCodigoIndicacao());
        statement.setString(13, usuario.getIndicadoPorCodigo());
        statement.setString(14, usuario.getAvatarTipo());
        statement.setString(15, usuario.getAvatarPersonagemId());

        // Coluna avatar_foto_usuario é CLOB. Se vier nula, precisa setNull
        // com Types.CLOB explicitamente — statement.setString(16, null)
        // sozinho é o que dispara o ORA-17132 nesse driver.
        String fotoBase64 = usuario.getAvatarFotoBase64();
        if (fotoBase64 == null || fotoBase64.isBlank()) {
            statement.setNull(16, Types.CLOB);
        } else {
            statement.setString(16, fotoBase64);
        }

        statement.setString(17, usuario.getCpf());
        statement.setString(18, usuario.getBairro());
    }

    private Usuario montar(ResultSet resultSet) throws SQLException {
        Date dataNascimento = resultSet.getDate("data_nascimento_usuario");

        // avatar_foto_usuario é CLOB: ler com getString() direto também
        // pode causar ORA-17132 dependendo do driver/estado da coluna.
        // getClob() + getSubString() é a forma segura de ler.
        String avatarFoto = null;
        java.sql.Clob clobFoto = resultSet.getClob("avatar_foto_usuario");
        if (clobFoto != null) {
            avatarFoto = clobFoto.getSubString(1, (int) clobFoto.length());
        }

        return new Usuario(
                resultSet.getInt("id_usuario"),
                resultSet.getString("nome_usuario"),
                resultSet.getString("email_usuario"),
                resultSet.getString("senha_usuario"),
                resultSet.getString("telefone_usuario"),
                dataNascimento != null ? dataNascimento.toLocalDate() : null,
                resultSet.getString("cidade_usuario"),
                resultSet.getString("estado_usuario"),
                resultSet.getString("tipo_usuario"),
                resultSet.getInt("total_pontos_usuario"),
                resultSet.getInt("nivel_usuario"),
                resultSet.getInt("streak_dias_usuario"),
                resultSet.getString("codigo_indicacao_usuario"),
                resultSet.getString("indicado_por_codigo_usuario"),
                resultSet.getString("avatar_tipo_usuario"),
                resultSet.getString("avatar_personagem_id_usuario"),
                avatarFoto,
                resultSet.getString("cpf_usuario"),
                resultSet.getString("bairro_usuario")
        );
    }

    private void fechar(ResultSet resultSet, PreparedStatement statement, Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.out.println("Erro ao fechar ResultSet: " + e.getMessage());
            }
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.out.println("Erro ao fechar PreparedStatement: " + e.getMessage());
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("Erro ao fechar Connection: " + e.getMessage());
            }
        }
    }
}
