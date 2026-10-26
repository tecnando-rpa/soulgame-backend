package dao;

import conexao.ConexaoOracleProjeto;
import model.UsuarioConquista;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioConquistaDAO {

    // Retorna false se o usuario ja tinha essa conquista (UK_USRCONQ_PAR
    // barra duplicata) — o motor de conquistas usa isso pra saber se foi
    // um desbloqueio novo (e vale mostrar a animacao) ou repetido.
    public boolean desbloquear(UsuarioConquista usuarioConquista) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "INSERT INTO TB_USUARIO_CONQUISTA (id_usuario, id_conquista) VALUES (?, ?)";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, usuarioConquista.getUsuarioId());
            statement.setInt(2, usuarioConquista.getConquistaId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            // Constraint UK_USRCONQ_PAR violada = ja tinha essa conquista,
            // nao e um erro real, so nao desbloqueia de novo.
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    // Usado em dois cenarios: exclusao de conta (LGPD) e limpeza de dados
    // de teste. Remove todas as conquistas desbloqueadas por um usuario.
    public boolean excluirPorUsuario(int usuarioId) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "DELETE FROM TB_USUARIO_CONQUISTA WHERE id_usuario = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, usuarioId);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir conquistas do usuario: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    public boolean jaDesbloqueou(int usuarioId, int conquistaId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT 1 FROM TB_USUARIO_CONQUISTA WHERE id_usuario = ? AND id_conquista = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, usuarioId);
            statement.setInt(2, conquistaId);
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("Erro ao verificar conquista: " + e.getMessage());
            return false;
        } finally {
            fechar(resultSet, statement, connection);
        }
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
