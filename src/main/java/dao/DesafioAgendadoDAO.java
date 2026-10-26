package dao;

import conexao.ConexaoOracleProjeto;
import model.DesafioAgendado;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DesafioAgendadoDAO {

    // Sem Statement.RETURN_GENERATED_KEYS (ORA-17132 neste driver/ambiente).
    // UK_DESAF_AGEND_USUARIO_ACAO garante que (usuario, acao) é único,
    // então essa dupla é chave natural suficiente para buscar o id de volta.
    public boolean criar(DesafioAgendado agendamento) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "INSERT INTO TB_DESAFIO_AGENDADO (id_usuario, id_acao, prazo) VALUES (?, ?, ?)";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, agendamento.getUsuarioId());
            statement.setInt(2, agendamento.getAcaoId());
            statement.setDate(3, Date.valueOf(agendamento.getPrazo()));

            if (statement.executeUpdate() != 1) {
                return false;
            }

            Integer idGerado = buscarIdPorUsuarioEAcao(agendamento.getUsuarioId(), agendamento.getAcaoId());
            if (idGerado != null) {
                agendamento.setId(idGerado);
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao criar desafio agendado: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private Integer buscarIdPorUsuarioEAcao(int usuarioId, int acaoId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT id_desafio_agendado FROM TB_DESAFIO_AGENDADO "
                + "WHERE id_usuario = ? AND id_acao = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, usuarioId);
            statement.setInt(2, acaoId);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id_desafio_agendado");
            }

            return null;
        } catch (SQLException e) {
            System.out.println("Erro ao buscar id do desafio agendado: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public List<DesafioAgendado> listarPorUsuario(int usuarioId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<DesafioAgendado> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_DESAFIO_AGENDADO WHERE id_usuario = ? ORDER BY prazo";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, usuarioId);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar desafios agendados: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    public boolean atualizarPrazo(int usuarioId, int acaoId, java.time.LocalDate novoPrazo) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "UPDATE TB_DESAFIO_AGENDADO SET prazo = ? WHERE id_usuario = ? AND id_acao = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setDate(1, Date.valueOf(novoPrazo));
            statement.setInt(2, usuarioId);
            statement.setInt(3, acaoId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar prazo: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    public boolean excluir(int usuarioId, int acaoId) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "DELETE FROM TB_DESAFIO_AGENDADO WHERE id_usuario = ? AND id_acao = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, usuarioId);
            statement.setInt(2, acaoId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir desafio agendado: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private DesafioAgendado montar(ResultSet resultSet) throws SQLException {
        return new DesafioAgendado(
                resultSet.getInt("id_desafio_agendado"),
                resultSet.getInt("id_usuario"),
                resultSet.getInt("id_acao"),
                resultSet.getDate("prazo").toLocalDate()
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
