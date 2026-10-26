package dao;

import conexao.ConexaoOracleProjeto;
import model.Evidencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EvidenciaDAO {

    // Sem Statement.RETURN_GENERATED_KEYS (ORA-17132 neste driver/ambiente).
    // id_registro tem FK única de fato por fluxo de uso (1 registro gera no
    // máximo 1 evidência em sequência imediata), então buscamos de volta
    // pelo id_registro + mais recente, que é preciso o suficiente aqui.
    public boolean criar(Evidencia evidencia) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "INSERT INTO TB_EVIDENCIA "
                + "(id_registro, tipo_evidencia, url_arquivo, descricao_evidencia, hash_evidencia) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, evidencia.getRegistroId());
            statement.setString(2, evidencia.getTipo());
            statement.setString(3, evidencia.getUrlArquivo());
            statement.setString(4, evidencia.getDescricao());
            statement.setString(5, evidencia.getHashEvidencia());

            if (statement.executeUpdate() != 1) {
                return false;
            }

            Integer idGerado = buscarUltimoIdPorRegistro(evidencia.getRegistroId());
            if (idGerado != null) {
                evidencia.setId(idGerado);
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao criar evidencia: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private Integer buscarUltimoIdPorRegistro(int registroId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT id_evidencia FROM TB_EVIDENCIA WHERE id_registro = ? "
                + "ORDER BY id_evidencia DESC FETCH FIRST 1 ROW ONLY";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, registroId);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id_evidencia");
            }

            return null;
        } catch (SQLException e) {
            System.out.println("Erro ao buscar id da evidencia: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    // Usado ANTES de chamar a IA: se o hash já existir, é a mesma
    // imagem sendo reenviada — bloqueia sem gastar nem tempo nem
    // custo de IA analisando de novo algo que já foi analisado.
    public Evidencia buscarPorHash(String hash) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT * FROM TB_EVIDENCIA WHERE hash_evidencia = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setString(1, hash);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return montar(resultSet);
            }

            return null;
        } catch (SQLException e) {
            System.out.println("Erro ao buscar evidencia por hash: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public Evidencia buscarPorId(int id) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT * FROM TB_EVIDENCIA WHERE id_evidencia = ?";

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
            System.out.println("Erro ao buscar evidencia: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public List<Evidencia> listarTodas() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Evidencia> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_EVIDENCIA ORDER BY id_evidencia";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar evidencias: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    public boolean atualizar(Evidencia evidencia) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "UPDATE TB_EVIDENCIA SET tipo_evidencia = ?, url_arquivo = ?, "
                + "descricao_evidencia = ?, hash_evidencia = ? WHERE id_evidencia = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setString(1, evidencia.getTipo());
            statement.setString(2, evidencia.getUrlArquivo());
            statement.setString(3, evidencia.getDescricao());
            statement.setString(4, evidencia.getHashEvidencia());
            statement.setInt(5, evidencia.getId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar evidencia: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    public boolean excluir(int id) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "DELETE FROM TB_EVIDENCIA WHERE id_evidencia = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir evidencia: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private Evidencia montar(ResultSet resultSet) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp("data_hora_envio");
        return new Evidencia(
                resultSet.getInt("id_evidencia"),
                resultSet.getInt("id_registro"),
                resultSet.getString("tipo_evidencia"),
                resultSet.getString("url_arquivo"),
                timestamp != null ? timestamp.toLocalDateTime() : null,
                resultSet.getString("descricao_evidencia"),
                resultSet.getString("hash_evidencia")
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
