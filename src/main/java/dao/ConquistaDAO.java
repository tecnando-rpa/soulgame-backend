package dao;

import conexao.ConexaoOracleProjeto;
import model.Conquista;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ConquistaDAO {

    public List<Conquista> listarTodas() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Conquista> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_CONQUISTA ORDER BY id_conquista";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar conquistas: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    // Todas as conquistas de um tipo especifico — usado pelo motor de
    // conquistas pra saber quais checar depois de uma acao concluida.
    public List<Conquista> listarPorTipo(String tipoCriterio) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Conquista> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_CONQUISTA WHERE tipo_criterio = ? ORDER BY valor_criterio";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setString(1, tipoCriterio);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar conquistas por tipo: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    public List<Conquista> listarDesbloqueadasPorUsuario(int usuarioId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Conquista> lista = new ArrayList<>();

        String sql = "SELECT c.* FROM TB_CONQUISTA c "
                + "JOIN TB_USUARIO_CONQUISTA uc ON uc.id_conquista = c.id_conquista "
                + "WHERE uc.id_usuario = ? ORDER BY uc.data_desbloqueio DESC";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, usuarioId);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar conquistas do usuario: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    private Conquista montar(ResultSet resultSet) throws SQLException {
        int odsRelacionadoValor = resultSet.getInt("id_ods_relacionado");
        Integer odsRelacionado = resultSet.wasNull() ? null : odsRelacionadoValor;

        return new Conquista(
                resultSet.getInt("id_conquista"),
                resultSet.getString("nome_conquista"),
                resultSet.getString("descricao_conquista"),
                resultSet.getString("tipo_criterio"),
                resultSet.getInt("valor_criterio"),
                odsRelacionado,
                resultSet.getString("slug_acao_relacionada"),
                resultSet.getString("icone_conquista")
        );
    }

    private void fechar(ResultSet resultSet, PreparedStatement statement, Connection connection) {
        if (resultSet != null) {
            try { resultSet.close(); } catch (SQLException e) { System.out.println("Erro ao fechar ResultSet: " + e.getMessage()); }
        }
        if (statement != null) {
            try { statement.close(); } catch (SQLException e) { System.out.println("Erro ao fechar PreparedStatement: " + e.getMessage()); }
        }
        if (connection != null) {
            try { connection.close(); } catch (SQLException e) { System.out.println("Erro ao fechar Connection: " + e.getMessage()); }
        }
    }
}
