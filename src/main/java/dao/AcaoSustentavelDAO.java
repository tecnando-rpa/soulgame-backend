package dao;

import conexao.ConexaoOracleProjeto;
import model.AcaoSustentavel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class AcaoSustentavelDAO {

    // Sem Statement.RETURN_GENERATED_KEYS (ORA-17132 neste driver/ambiente).
    // slug_acao é único, então buscamos o id de volta por ele.
    public boolean criar(AcaoSustentavel acao) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "INSERT INTO TB_ACAO_SUSTENTAVEL "
                + "(slug_acao, nome_acao, objetivo_sustentavel, pontuacao_base, nivel_dificuldade, descricao_acao, id_ods) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setString(1, acao.getSlug());
            statement.setString(2, acao.getNome());
            statement.setString(3, acao.getObjetivoSustentavel());
            statement.setInt(4, acao.getPontuacaoBase());
            statement.setString(5, acao.getNivelDificuldade().toUpperCase());
            statement.setString(6, acao.getDescricao());

            if (acao.getIdOds() > 0) {
                statement.setInt(7, acao.getIdOds());
            } else {
                statement.setNull(7, Types.INTEGER);
            }

            if (statement.executeUpdate() != 1) {
                return false;
            }

            AcaoSustentavel criada = buscarPorSlug(acao.getSlug());
            if (criada != null) {
                acao.setId(criada.getId());
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao criar acao: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    public AcaoSustentavel buscarPorId(int id) {
        return buscarPorCampo("id_acao = ?", id);
    }

    public AcaoSustentavel buscarPorSlug(String slug) {
        return buscarPorCampo("slug_acao = ?", slug);
    }

    private AcaoSustentavel buscarPorCampo(String condicaoWhere, Object valor) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "SELECT * FROM TB_ACAO_SUSTENTAVEL WHERE " + condicaoWhere;

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);

            if (valor instanceof Integer numero) {
                statement.setInt(1, numero);
            } else {
                statement.setString(1, (String) valor);
            }

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return montar(resultSet);
            }

            return null;
        } catch (SQLException e) {
            System.out.println("Erro ao buscar acao: " + e.getMessage());
            return null;
        } finally {
            fechar(resultSet, statement, connection);
        }
    }

    public List<AcaoSustentavel> listarTodas() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<AcaoSustentavel> lista = new ArrayList<>();

        String sql = "SELECT * FROM TB_ACAO_SUSTENTAVEL ORDER BY id_acao";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                lista.add(montar(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar acoes: " + e.getMessage());
        } finally {
            fechar(resultSet, statement, connection);
        }

        return lista;
    }

    public boolean atualizar(AcaoSustentavel acao) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "UPDATE TB_ACAO_SUSTENTAVEL SET "
                + "slug_acao = ?, nome_acao = ?, objetivo_sustentavel = ?, pontuacao_base = ?, "
                + "nivel_dificuldade = ?, descricao_acao = ?, id_ods = ? WHERE id_acao = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setString(1, acao.getSlug());
            statement.setString(2, acao.getNome());
            statement.setString(3, acao.getObjetivoSustentavel());
            statement.setInt(4, acao.getPontuacaoBase());
            statement.setString(5, acao.getNivelDificuldade().toUpperCase());
            statement.setString(6, acao.getDescricao());

            if (acao.getIdOds() > 0) {
                statement.setInt(7, acao.getIdOds());
            } else {
                statement.setNull(7, Types.INTEGER);
            }

            statement.setInt(8, acao.getId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar acao: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    public boolean excluir(int id) {
        Connection connection = null;
        PreparedStatement statement = null;

        String sql = "DELETE FROM TB_ACAO_SUSTENTAVEL WHERE id_acao = ?";

        try {
            connection = ConexaoOracleProjeto.conectar();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir acao: " + e.getMessage());
            return false;
        } finally {
            fechar(null, statement, connection);
        }
    }

    private AcaoSustentavel montar(ResultSet resultSet) throws SQLException {
        int idOdsValor = resultSet.getInt("id_ods");
        int idOds = resultSet.wasNull() ? 0 : idOdsValor;

        return new AcaoSustentavel(
                resultSet.getInt("id_acao"),
                resultSet.getString("slug_acao"),
                resultSet.getString("nome_acao"),
                resultSet.getString("objetivo_sustentavel"),
                resultSet.getInt("pontuacao_base"),
                resultSet.getString("nivel_dificuldade"),
                resultSet.getString("descricao_acao"),
                idOds
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
