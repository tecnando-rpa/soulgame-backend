package dao;

import conexao.ConexaoOracleProjeto;
import model.Campeonato;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CampeonatoDAO {

    public boolean criar(Campeonato campeonato) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "INSERT INTO TB_CAMPEONATO "
                + "(nome_campeonato, escopo, estado_campeonato, id_ods_relacionado, "
                + "data_inicio, data_fim, premiacao, id_usuario_criador, status_campeonato) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            preencher(statement, campeonato);

            if (statement.executeUpdate() != 1) {
                return false;
            }

            Integer idGerado = buscarUltimoIdPorNomeECriador(campeonato.getNome(), campeonato.getUsuarioCriadorId());
            if (idGerado != null) {
                campeonato.setId(idGerado);
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao criar campeonato: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    public List<Campeonato> listarTodos() {
        return listarPorCondicao("1=1");
    }

    public List<Campeonato> listarPorStatus(String status) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Campeonato> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_CAMPEONATO WHERE status_campeonato = ? ORDER BY data_inicio";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setString(1, status);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar campeonatos por status: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    // Campeonatos EM_ANDAMENTO ligados a um ODS especifico — usado logo
    // apos um registro de acao pra saber em quais campeonatos o usuario
    // deveria ganhar pontos, se estiver inscrito.
    public List<Campeonato> listarEmAndamentoPorOds(int idOds) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Campeonato> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_CAMPEONATO WHERE status_campeonato = 'EM_ANDAMENTO' "
                + "AND id_ods_relacionado = ? "
                + "AND CURRENT_DATE BETWEEN data_inicio AND data_fim";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, idOds);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar campeonatos em andamento por ODS: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    public boolean atualizarStatus(int id, String novoStatus) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "UPDATE TB_CAMPEONATO SET status_campeonato = ? WHERE id_campeonato = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setString(1, novoStatus);
            statement.setInt(2, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar status do campeonato: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    // Completa o CRUD (Create/Read/Update já existiam). Exclusão real só
    // deve ser usada em campeonatos AGENDADOs que nunca rodaram — em
    // produção, o fluxo normal é encerrar (atualizarStatus) e manter o
    // histórico, não apagar.
    public boolean excluir(int id) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "DELETE FROM TB_CAMPEONATO WHERE id_campeonato = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir campeonato: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private List<Campeonato> listarPorCondicao(String condicaoWhere) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Campeonato> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_CAMPEONATO WHERE " + condicaoWhere + " ORDER BY data_inicio";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar campeonatos: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    private Integer buscarUltimoIdPorNomeECriador(String nome, int criadorId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT id_campeonato FROM TB_CAMPEONATO "
                + "WHERE nome_campeonato = ? AND id_usuario_criador = ? "
                + "ORDER BY id_campeonato DESC FETCH FIRST 1 ROW ONLY";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setString(1, nome);
            statement.setInt(2, criadorId);
            resultSet = statement.executeQuery();
            return resultSet.next() ? resultSet.getInt(1) : null;
        } catch (SQLException e) {
            System.out.println("Erro ao buscar id do campeonato: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    private void preencher(PreparedStatement statement, Campeonato campeonato) throws SQLException {
        statement.setString(1, campeonato.getNome());
        statement.setString(2, campeonato.getEscopo());
        statement.setString(3, campeonato.getEstado());

        if (campeonato.getOdsRelacionadoId() != null) {
            statement.setInt(4, campeonato.getOdsRelacionadoId());
        } else {
            statement.setNull(4, Types.INTEGER);
        }

        statement.setDate(5, Date.valueOf(campeonato.getDataInicio()));
        statement.setDate(6, Date.valueOf(campeonato.getDataFim()));
        statement.setString(7, campeonato.getPremiacao());
        statement.setInt(8, campeonato.getUsuarioCriadorId());
        statement.setString(9, campeonato.getStatus());
    }

    private Campeonato montar(ResultSet resultSet) throws SQLException {
        int odsRelacionadoValor = resultSet.getInt("id_ods_relacionado");
        Integer odsRelacionado = resultSet.wasNull() ? null : odsRelacionadoValor;

        return new Campeonato(
                resultSet.getInt("id_campeonato"),
                resultSet.getString("nome_campeonato"),
                resultSet.getString("escopo"),
                resultSet.getString("estado_campeonato"),
                odsRelacionado,
                resultSet.getDate("data_inicio").toLocalDate(),
                resultSet.getDate("data_fim").toLocalDate(),
                resultSet.getString("premiacao"),
                resultSet.getInt("id_usuario_criador"),
                resultSet.getString("status_campeonato")
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
