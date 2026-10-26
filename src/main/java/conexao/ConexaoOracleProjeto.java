package conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexao destinada ao banco Oracle fisico oficial do Challenge.
 * O modelo recebido da disciplina de Banco de Dados foi usado na versao final
 * e os DAOs seguem os nomes reais TB_USUARIO, TB_ACAO_SUSTENTAVEL e TB_REGISTRO_ACAO.
 * A conexao utiliza o banco Oracle oficial da FIAP.
 * O driver Oracle JDBC ja esta declarado no pom.xml.
 * Quem chamar conectar() deve fechar a Connection explicitamente ao terminar.
 */
public class ConexaoOracleProjeto {
    // Prioridade: variáveis de ambiente (ORACLE_URL, ORACLE_USUARIO,
    // ORACLE_SENHA) — usadas no deploy; se não existirem, usa o padrão local.
    private static final String URL = env("ORACLE_URL", "jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL");
    private static final String USUARIO = env("ORACLE_USUARIO", "rm570196");
    private static final String SENHA = env("ORACLE_SENHA", "250587");

    private static String env(String nome, String padrao) {
        String valor = System.getenv(nome);
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }

    private ConexaoOracleProjeto() {
    }

    public static Connection conectar() throws SQLException {
        if (URL.startsWith("COLOCAR_")
                || USUARIO.startsWith("COLOCAR_")
                || SENHA.startsWith("COLOCAR_")) {
            throw new SQLException(
                    "Preencha URL, USUARIO e SENHA do Oracle em ConexaoOracleProjeto.java."
            );
        }

        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    public static boolean testarConexao() {
        Connection connection = null;

        try {
            connection = conectar();
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.out.println("Erro ao testar Oracle: " + e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.out.println("Erro ao fechar Oracle: " + e.getMessage());
                }
            }
        }
    }
}
