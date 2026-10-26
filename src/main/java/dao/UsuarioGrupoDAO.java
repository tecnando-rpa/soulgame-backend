package dao;

import conexao.ConexaoOracleProjeto;
import model.UsuarioGrupo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// DAO da entidade associativa (chave composta id_usuario + id_grupo) —
// não tem "id" próprio gerado, por isso criar/buscar/atualizar/excluir
// trabalham sempre com o par de chaves.
public class UsuarioGrupoDAO {

    public boolean criar(UsuarioGrupo usuarioGrupo) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "INSERT INTO TB_USUARIO_GRUPO "
                + "(id_usuario, id_grupo, data_entrada, pontos_no_grupo, status_participacao) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, usuarioGrupo.getUsuarioId());
            statement.setInt(2, usuarioGrupo.getGrupoId());
            statement.setDate(3, Date.valueOf(usuarioGrupo.getDataEntrada()));
            statement.setInt(4, usuarioGrupo.getPontosNoGrupo());
            statement.setString(5, usuarioGrupo.getStatusParticipacao());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao criar participacao: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    public UsuarioGrupo buscarPorChave(int usuarioId, int grupoId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT * FROM TB_USUARIO_GRUPO WHERE id_usuario = ? AND id_grupo = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, usuarioId);
            statement.setInt(2, grupoId);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return montar(resultSet);
            }

            return null;
        } catch (SQLException e) {
            System.out.println("Erro ao buscar participacao: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public List<UsuarioGrupo> listarTodos() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<UsuarioGrupo> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_USUARIO_GRUPO ORDER BY id_grupo, id_usuario";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar participacoes: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    public boolean atualizar(UsuarioGrupo usuarioGrupo) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "UPDATE TB_USUARIO_GRUPO SET pontos_no_grupo = ?, status_participacao = ? "
                + "WHERE id_usuario = ? AND id_grupo = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, usuarioGrupo.getPontosNoGrupo());
            statement.setString(2, usuarioGrupo.getStatusParticipacao());
            statement.setInt(3, usuarioGrupo.getUsuarioId());
            statement.setInt(4, usuarioGrupo.getGrupoId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar participacao: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    public boolean excluir(int usuarioId, int grupoId) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "DELETE FROM TB_USUARIO_GRUPO WHERE id_usuario = ? AND id_grupo = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, usuarioId);
            statement.setInt(2, grupoId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir participacao: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private UsuarioGrupo montar(ResultSet resultSet) throws SQLException {
        return new UsuarioGrupo(
                resultSet.getInt("id_usuario"),
                resultSet.getInt("id_grupo"),
                resultSet.getDate("data_entrada").toLocalDate(),
                resultSet.getInt("pontos_no_grupo"),
                resultSet.getString("status_participacao")
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
