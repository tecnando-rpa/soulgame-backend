package dao;

import conexao.ConexaoOracleProjeto;
import model.Parceiro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParceiroDAO {

    // Sem Statement.RETURN_GENERATED_KEYS (ORA-17132 neste driver/ambiente).
    // cnpj_parceiro é UNIQUE (UK_PARCEIRO_CNPJ), chave natural segura pra
    // buscar o id de volta depois do INSERT.
    public boolean criar(Parceiro parceiro) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "INSERT INTO TB_PARCEIRO "
                + "(nome_parceiro, tipo_parceiro, cnpj_parceiro, cidade_parceiro, status_parceiro) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setString(1, parceiro.getNome());
            statement.setString(2, parceiro.getTipo());
            statement.setString(3, parceiro.getCnpj());
            statement.setString(4, parceiro.getCidade());
            statement.setString(5, parceiro.getStatus());

            if (statement.executeUpdate() != 1) {
                return false;
            }

            Integer idGerado = buscarIdPorCnpj(parceiro.getCnpj());
            if (idGerado != null) {
                parceiro.setId(idGerado);
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao criar parceiro: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private Integer buscarIdPorCnpj(String cnpj) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT id_parceiro FROM TB_PARCEIRO WHERE cnpj_parceiro = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setString(1, cnpj);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id_parceiro");
            }

            return null;
        } catch (SQLException e) {
            System.out.println("Erro ao buscar id do parceiro: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public Parceiro buscarPorId(int id) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT * FROM TB_PARCEIRO WHERE id_parceiro = ?";

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
            System.out.println("Erro ao buscar parceiro: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public List<Parceiro> listarTodos() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Parceiro> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_PARCEIRO ORDER BY id_parceiro";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar parceiros: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    public boolean atualizar(Parceiro parceiro) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "UPDATE TB_PARCEIRO SET "
                + "nome_parceiro = ?, tipo_parceiro = ?, cnpj_parceiro = ?, cidade_parceiro = ?, status_parceiro = ? "
                + "WHERE id_parceiro = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setString(1, parceiro.getNome());
            statement.setString(2, parceiro.getTipo());
            statement.setString(3, parceiro.getCnpj());
            statement.setString(4, parceiro.getCidade());
            statement.setString(5, parceiro.getStatus());
            statement.setInt(6, parceiro.getId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar parceiro: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    public boolean excluir(int id) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "DELETE FROM TB_PARCEIRO WHERE id_parceiro = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir parceiro: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private Parceiro montar(ResultSet resultSet) throws SQLException {
        return new Parceiro(
                resultSet.getInt("id_parceiro"),
                resultSet.getString("nome_parceiro"),
                resultSet.getString("tipo_parceiro"),
                resultSet.getString("cnpj_parceiro"),
                resultSet.getString("cidade_parceiro"),
                resultSet.getString("status_parceiro")
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
