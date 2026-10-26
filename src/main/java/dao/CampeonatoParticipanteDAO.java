package dao;

import conexao.ConexaoOracleProjeto;
import model.CampeonatoParticipante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CampeonatoParticipanteDAO {

    public boolean inscrever(CampeonatoParticipante participante) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "INSERT INTO TB_CAMPEONATO_PARTICIPANTE (id_campeonato, id_usuario, pontos_campeonato) "
                + "VALUES (?, ?, 0)";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, participante.getCampeonatoId());
            statement.setInt(2, participante.getUsuarioId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            // UK_CAMPPART_PAR violada = ja estava inscrito, nao e erro real.
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    // Soma pontos na pontuacao do participante dentro de um campeonato
    // especifico — chamado toda vez que ele conclui uma acao do ODS
    // relacionado enquanto o campeonato esta em andamento.
    public boolean somarPontos(int campeonatoId, int usuarioId, int pontos) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "UPDATE TB_CAMPEONATO_PARTICIPANTE SET pontos_campeonato = pontos_campeonato + ? "
                + "WHERE id_campeonato = ? AND id_usuario = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, pontos);
            statement.setInt(2, campeonatoId);
            statement.setInt(3, usuarioId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao somar pontos no campeonato: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    public List<CampeonatoParticipante> listarRankingPorCampeonato(int campeonatoId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<CampeonatoParticipante> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_CAMPEONATO_PARTICIPANTE WHERE id_campeonato = ? "
                + "ORDER BY pontos_campeonato DESC";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, campeonatoId);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar ranking do campeonato: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    public boolean estaInscrito(int campeonatoId, int usuarioId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT 1 FROM TB_CAMPEONATO_PARTICIPANTE WHERE id_campeonato = ? AND id_usuario = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, campeonatoId);
            statement.setInt(2, usuarioId);
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("Erro ao verificar inscricao: " + e.getMessage());
            return false;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    // Completa o CRUD: cancelar inscrição (ex: usuário que se inscreveu
    // por engano, ou saiu do campeonato antes dele começar).
    public boolean excluir(int campeonatoId, int usuarioId) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "DELETE FROM TB_CAMPEONATO_PARTICIPANTE WHERE id_campeonato = ? AND id_usuario = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, campeonatoId);
            statement.setInt(2, usuarioId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir participacao: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private CampeonatoParticipante montar(ResultSet resultSet) throws SQLException {
        return new CampeonatoParticipante(
                resultSet.getInt("id_participacao"),
                resultSet.getInt("id_campeonato"),
                resultSet.getInt("id_usuario"),
                resultSet.getInt("pontos_campeonato")
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
