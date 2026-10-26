package dao;

import conexao.ConexaoOracleProjeto;
import model.Validacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ValidacaoDAO {

    // Sem Statement.RETURN_GENERATED_KEYS (ORA-17132 neste driver/ambiente).
    // id_registro é UNIQUE (UK_VALIDACAO_REGISTRO — 1 validação por
    // registro), chave natural segura pra buscar o id de volta.
    public boolean criar(Validacao validacao) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "INSERT INTO TB_VALIDACAO "
                + "(id_parceiro, id_registro, resultado_validacao, observacao_validacao) "
                + "VALUES (?, ?, ?, ?)";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, validacao.getParceiroId());
            statement.setInt(2, validacao.getRegistroId());
            statement.setString(3, validacao.getResultado());
            statement.setString(4, validacao.getObservacao());

            if (statement.executeUpdate() != 1) {
                return false;
            }

            Integer idGerado = buscarIdPorRegistro(validacao.getRegistroId());
            if (idGerado != null) {
                validacao.setId(idGerado);
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao criar validacao: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private Integer buscarIdPorRegistro(int registroId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT id_validacao FROM TB_VALIDACAO WHERE id_registro = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, registroId);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id_validacao");
            }

            return null;
        } catch (SQLException e) {
            System.out.println("Erro ao buscar id da validacao: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public Validacao buscarPorId(int id) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT * FROM TB_VALIDACAO WHERE id_validacao = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return montar(resultSet);
            }

            return null;
        } catch (SQLException e) {
            System.out.println("Erro ao buscar validacao: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public List<Validacao> listarTodas() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Validacao> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_VALIDACAO ORDER BY id_validacao";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar validacoes: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    public boolean atualizar(Validacao validacao) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "UPDATE TB_VALIDACAO SET resultado_validacao = ?, observacao_validacao = ? "
                + "WHERE id_validacao = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setString(1, validacao.getResultado());
            statement.setString(2, validacao.getObservacao());
            statement.setInt(3, validacao.getId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar validacao: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    public boolean excluir(int id) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "DELETE FROM TB_VALIDACAO WHERE id_validacao = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir validacao: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private Validacao montar(ResultSet resultSet) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp("data_hora_validacao");
        return new Validacao(
                resultSet.getInt("id_validacao"),
                resultSet.getInt("id_parceiro"),
                resultSet.getInt("id_registro"),
                timestamp != null ? timestamp.toLocalDateTime() : null,
                resultSet.getString("resultado_validacao"),
                resultSet.getString("observacao_validacao")
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
