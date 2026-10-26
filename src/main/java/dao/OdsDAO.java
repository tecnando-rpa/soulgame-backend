package dao;

import conexao.ConexaoOracleProjeto;
import model.Ods;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Os 17 ODS sao dado de referencia (seedado na migration, nunca
// alterado pela aplicacao) — essa DAO e so leitura.
public class OdsDAO {

    public List<Ods> listarTodos() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Ods> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_ODS ORDER BY id_ods";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar ODS: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    public Ods buscarPorId(int id) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT * FROM TB_ODS WHERE id_ods = ?";

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
            System.out.println("Erro ao buscar ODS: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    private Ods montar(ResultSet resultSet) throws SQLException {
        return new Ods(
                resultSet.getInt("id_ods"),
                resultSet.getString("nome_ods"),
                resultSet.getString("cor_ods"),
                resultSet.getString("icone_ods")
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
