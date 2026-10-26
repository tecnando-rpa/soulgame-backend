package dao;

import conexao.ConexaoOracleProjeto;
import model.Grupo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GrupoDAO {

    // Não usar Statement.RETURN_GENERATED_KEYS: o driver Oracle (ojdbc11)
    // usado neste ambiente apresenta ORA-17132 em alguns INSERTs quando
    // a chave gerada é pedida junto. Padrão adotado em todo o projeto:
    // inserir e, depois, buscar o último id gerado com um SELECT separado.
    public boolean criar(Grupo grupo) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "INSERT INTO TB_GRUPO "
                + "(nome_grupo, tipo_grupo, cidade_grupo, bairro_grupo, pontos_minimos_entrada) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setString(1, grupo.getNome());
            statement.setString(2, grupo.getTipo());
            statement.setString(3, grupo.getCidade());
            statement.setString(4, grupo.getBairro());
            statement.setInt(5, grupo.getPontosMinimosEntrada());

            if (statement.executeUpdate() != 1) {
                return false;
            }

            Integer idGerado = buscarUltimoIdGerado();
            if (idGerado != null) {
                grupo.setId(idGerado);
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao criar grupo: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private Integer buscarUltimoIdGerado() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT id_grupo FROM TB_GRUPO ORDER BY id_grupo DESC FETCH FIRST 1 ROW ONLY";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id_grupo");
            }

            return null;
        } catch (SQLException e) {
            System.out.println("Erro ao buscar id do grupo: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public Grupo buscarPorId(int id) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT * FROM TB_GRUPO WHERE id_grupo = ?";

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
            System.out.println("Erro ao buscar grupo: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public List<Grupo> listarTodos() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Grupo> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_GRUPO ORDER BY id_grupo";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar grupos: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    public boolean atualizar(Grupo grupo) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "UPDATE TB_GRUPO SET "
                + "nome_grupo = ?, tipo_grupo = ?, cidade_grupo = ?, bairro_grupo = ?, pontos_minimos_entrada = ? "
                + "WHERE id_grupo = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setString(1, grupo.getNome());
            statement.setString(2, grupo.getTipo());
            statement.setString(3, grupo.getCidade());
            statement.setString(4, grupo.getBairro());
            statement.setInt(5, grupo.getPontosMinimosEntrada());
            statement.setInt(6, grupo.getId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar grupo: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    public boolean excluir(int id) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "DELETE FROM TB_GRUPO WHERE id_grupo = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir grupo: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private Grupo montar(ResultSet resultSet) throws SQLException {
        Date data = resultSet.getDate("data_criacao");
        return new Grupo(
                resultSet.getInt("id_grupo"),
                resultSet.getString("nome_grupo"),
                resultSet.getString("tipo_grupo"),
                resultSet.getString("cidade_grupo"),
                resultSet.getString("bairro_grupo"),
                resultSet.getInt("pontos_minimos_entrada"),
                data != null ? data.toLocalDate() : null
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
