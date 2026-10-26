package dao;

import conexao.ConexaoOracleProjeto;
import model.RegistroAcao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class RegistroAcaoDAO {

    public boolean criar(RegistroAcao registro) {
        Connection connection = null;
        PreparedStatement statement = null;

        // Mesmo motivo do UsuarioDAO: não usar Statement.RETURN_GENERATED_KEYS
        // (driver Oracle gera ORA-17132 nesse ambiente). Insere e depois busca
        // o ID com uma query separada, pegando o registro mais recente desse
        // par usuario+acao.
        String sql = "INSERT INTO TB_REGISTRO_ACAO "
                + "(id_acao, id_usuario, data_hora_registro, status_validacao, pontos_obtidos, latitude, longitude) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            preencher(statement, registro, false);

            if (statement.executeUpdate() != 1) {
                return false;
            }

            Integer idGerado = buscarUltimoIdPorUsuarioEAcao(registro.getUsuarioId(), registro.getAcaoId());
            if (idGerado != null) {
                registro.setId(idGerado);
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao criar registro: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private Integer buscarUltimoIdPorUsuarioEAcao(int usuarioId, int acaoId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT id_registro FROM TB_REGISTRO_ACAO "
                + "WHERE id_usuario = ? AND id_acao = ? "
                + "ORDER BY id_registro DESC "
                + "FETCH FIRST 1 ROW ONLY";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, usuarioId);
            statement.setInt(2, acaoId);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id_registro");
            }

            return null;
        } catch (SQLException e) {
            System.out.println("Erro ao buscar id do registro: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public RegistroAcao buscarPorId(int id) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT * FROM TB_REGISTRO_ACAO WHERE id_registro = ?";

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
            System.out.println("Erro ao buscar registro: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public List<RegistroAcao> listarTodos() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<RegistroAcao> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_REGISTRO_ACAO ORDER BY id_registro";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar registros: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    // Deriva o "histórico de conclusões" de um usuário a partir dos
    // registros reais — em vez de duplicar essa lista como campo solto
    // no usuário (que poderia ficar dessincronizada). Só traz registros
    // já VALIDADOs, que é a definição real de "atividade concluída".
    /**
     * Já existe um registro PENDENTE (aguardando validação) desse usuário
     * para essa ação? Evita que o mesmo desafio seja enviado várias vezes
     * enquanto a primeira evidência ainda está em análise.
     */
    public boolean existePendente(int usuarioId, int acaoId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT COUNT(*) FROM TB_REGISTRO_ACAO WHERE id_usuario = ? AND id_acao = ? "
                + "AND status_validacao = 'PENDENTE'";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, usuarioId);
            statement.setInt(2, acaoId);
            resultSet = statement.executeQuery();
            return resultSet.next() && resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao verificar registro pendente: " + e.getMessage());
            return false;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public List<RegistroAcao> listarConcluidasPorUsuario(int usuarioId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<RegistroAcao> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_REGISTRO_ACAO WHERE id_usuario = ? AND status_validacao = 'VALIDADO' "
                + "ORDER BY data_hora_registro DESC";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, usuarioId);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar concluidas do usuario: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    public boolean atualizar(RegistroAcao registro) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "UPDATE TB_REGISTRO_ACAO SET "
                + "id_acao = ?, id_usuario = ?, data_hora_registro = ?, status_validacao = ?, "
                + "pontos_obtidos = ?, latitude = ?, longitude = ? WHERE id_registro = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            preencher(statement, registro, true);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar registro: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    public boolean excluir(int id) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "DELETE FROM TB_REGISTRO_ACAO WHERE id_registro = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir registro: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private void preencher(PreparedStatement statement, RegistroAcao registro, boolean incluirId) throws SQLException {
        statement.setInt(1, registro.getAcaoId());
        statement.setInt(2, registro.getUsuarioId());
        statement.setTimestamp(3, Timestamp.valueOf(registro.getDataHoraRegistro()));
        statement.setString(4, registro.getStatusValidacao().toUpperCase());
        statement.setInt(5, registro.getPontosObtidos());

        if (registro.getLatitude() == null) {
            statement.setNull(6, Types.DOUBLE);
        } else {
            statement.setDouble(6, registro.getLatitude());
        }

        if (registro.getLongitude() == null) {
            statement.setNull(7, Types.DOUBLE);
        } else {
            statement.setDouble(7, registro.getLongitude());
        }

        if (incluirId) {
            statement.setInt(8, registro.getId());
        }
    }

    private RegistroAcao montar(ResultSet resultSet) throws SQLException {
        Double latitude = (Double) resultSet.getObject("latitude");
        Double longitude = (Double) resultSet.getObject("longitude");

        return new RegistroAcao(
                resultSet.getInt("id_registro"),
                resultSet.getInt("id_acao"),
                resultSet.getInt("id_usuario"),
                resultSet.getTimestamp("data_hora_registro").toLocalDateTime(),
                resultSet.getString("status_validacao"),
                resultSet.getInt("pontos_obtidos"),
                latitude,
                longitude
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
