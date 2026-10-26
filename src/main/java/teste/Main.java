package teste;

import conexao.ConexaoOracleProjeto;
import dao.AcaoSustentavelDAO;
import dao.CampeonatoDAO;
import dao.CampeonatoParticipanteDAO;
import dao.ConquistaDAO;
import dao.DesafioAgendadoDAO;
import dao.EvidenciaDAO;
import dao.GrupoDAO;
import dao.OdsDAO;
import dao.ParceiroDAO;
import dao.RegistroAcaoDAO;
import dao.UsuarioConquistaDAO;
import dao.UsuarioDAO;
import dao.UsuarioGrupoDAO;
import dao.ValidacaoDAO;
import model.AcaoSustentavel;
import model.Campeonato;
import model.CampeonatoParticipante;
import model.Conquista;
import model.DesafioAgendado;
import model.Evidencia;
import model.Grupo;
import model.Ods;
import model.Parceiro;
import model.RegistroAcao;
import model.Usuario;
import model.UsuarioGrupo;
import model.Validacao;
import service.MotorConquistas;
import service.Ranking;
import service.SistemaGamificacao;

import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Conexao Oracle: " + ConexaoOracleProjeto.testarConexao());

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        AcaoSustentavelDAO acaoDAO = new AcaoSustentavelDAO();
        RegistroAcaoDAO registroDAO = new RegistroAcaoDAO();
        GrupoDAO grupoDAO = new GrupoDAO();
        ParceiroDAO parceiroDAO = new ParceiroDAO();
        UsuarioGrupoDAO usuarioGrupoDAO = new UsuarioGrupoDAO();
        EvidenciaDAO evidenciaDAO = new EvidenciaDAO();
        ValidacaoDAO validacaoDAO = new ValidacaoDAO();

        Usuario usuario = new Usuario(
                "Pedro Silva",
                "pedro.tm@teste.com",
                "1234",
                "(11) 90000-0000",
                java.time.LocalDate.of(1998, 5, 20),
                "Sao Paulo",
                "SP",
                "soulgamer"
        );

        AcaoSustentavel acao = new AcaoSustentavel(
                "reciclagem-teste",
                "Reciclagem de materiais",
                "Consumo responsavel",
                40,
                "MEDIA",
                "Separacao de plastico e papel",
                12
        );

        System.out.println("=== REGRAS DE NEGOCIO ===");
        System.out.println("Usuario valido: " + usuario.validarCadastro());
        System.out.println("Acao valida: " + acao.validarAcao());
        System.out.println("Pontuacao calculada: " + acao.calcularPontuacao());

        System.out.println("\n=== CRUD USUARIO ===");
        System.out.println("CREATE: " + usuarioDAO.criar(usuario));
        System.out.println("READ: " + (usuarioDAO.buscarPorId(usuario.getId()) != null));
        usuario.setBairro("Paulista");
        System.out.println("UPDATE: " + usuarioDAO.atualizar(usuario));
        System.out.println("LISTAR: " + usuarioDAO.listarTodos().size());

        System.out.println("\n=== CRUD ACAO ===");
        System.out.println("CREATE: " + acaoDAO.criar(acao));
        System.out.println("READ: " + (acaoDAO.buscarPorId(acao.getId()) != null));
        acao.setDescricao("Reciclagem de plastico, papel e vidro");
        System.out.println("UPDATE: " + acaoDAO.atualizar(acao));
        System.out.println("LISTAR: " + acaoDAO.listarTodas().size());

        SistemaGamificacao sistema = new SistemaGamificacao();
        RegistroAcao registro = sistema.registrarAcao(usuario, acao);

        System.out.println("\n=== CRUD REGISTRO DE ACAO ===");
        System.out.println("CREATE: " + registroDAO.criar(registro));
        System.out.println("READ: " + (registroDAO.buscarPorId(registro.getId()) != null));
        registro.setStatusValidacao("VALIDADO");
        System.out.println("UPDATE: " + registroDAO.atualizar(registro));
        System.out.println("LISTAR: " + registroDAO.listarTodos().size());

        Grupo grupo = new Grupo("Reciclagem Vila Mariana", "ABERTO", "Sao Paulo", "Vila Mariana", 0);

        System.out.println("\n=== CRUD GRUPO ===");
        System.out.println("Grupo valido: " + grupo.validarGrupo());
        System.out.println("CREATE: " + grupoDAO.criar(grupo));
        System.out.println("READ: " + (grupoDAO.buscarPorId(grupo.getId()) != null));
        grupo.setPontosMinimosEntrada(10);
        System.out.println("UPDATE: " + grupoDAO.atualizar(grupo));
        System.out.println("LISTAR: " + grupoDAO.listarTodos().size());
        System.out.println("Permite entrada com 50 pts: " + grupo.permiteEntrada(50));

        Parceiro parceiro = new Parceiro("EcoPonto Reciclagem LTDA", "COOPERATIVA", "12345678000199", "Sao Paulo");

        System.out.println("\n=== CRUD PARCEIRO ===");
        System.out.println("Parceiro valido: " + parceiro.validarParceiro());
        System.out.println("CREATE: " + parceiroDAO.criar(parceiro));
        System.out.println("READ: " + (parceiroDAO.buscarPorId(parceiro.getId()) != null));
        parceiro.setStatus("EM_ANALISE");
        System.out.println("UPDATE: " + parceiroDAO.atualizar(parceiro));
        System.out.println("LISTAR: " + parceiroDAO.listarTodos().size());

        UsuarioGrupo participacao = new UsuarioGrupo(usuario.getId(), grupo.getId());

        System.out.println("\n=== CRUD USUARIO_GRUPO (associativa) ===");
        System.out.println("Participacao valida: " + participacao.validarParticipacao());
        System.out.println("CREATE: " + usuarioGrupoDAO.criar(participacao));
        System.out.println("READ: " + (usuarioGrupoDAO.buscarPorChave(usuario.getId(), grupo.getId()) != null));
        participacao.adicionarPontosNoGrupo(15);
        System.out.println("UPDATE: " + usuarioGrupoDAO.atualizar(participacao));
        System.out.println("LISTAR: " + usuarioGrupoDAO.listarTodos().size());

        Evidencia evidencia = new Evidencia(
                registro.getId(), "FOTO", "https://storage.tmconsulting.com/evidencias/foto1.jpg",
                "Foto da entrega dos materiais reciclaveis",
                utils.HashImagem.calcularSha256("foto-teste-main".getBytes())
        );

        System.out.println("\n=== CRUD EVIDENCIA ===");
        System.out.println("Evidencia valida: " + evidencia.validarEvidencia());
        System.out.println("CREATE: " + evidenciaDAO.criar(evidencia));
        System.out.println("READ: " + (evidenciaDAO.buscarPorId(evidencia.getId()) != null));
        evidencia.setDescricao("Foto da entrega, conferida pelo parceiro");
        System.out.println("UPDATE: " + evidenciaDAO.atualizar(evidencia));
        System.out.println("LISTAR: " + evidenciaDAO.listarTodas().size());

        Validacao validacao = new Validacao(
                parceiro.getId(), registro.getId(), "APROVADO", "Material conferido e pesado corretamente"
        );

        System.out.println("\n=== CRUD VALIDACAO ===");
        System.out.println("Validacao valida: " + validacao.validarValidacao());
        System.out.println("Foi aprovado: " + validacao.foiAprovado());
        System.out.println("CREATE: " + validacaoDAO.criar(validacao));
        System.out.println("READ: " + (validacaoDAO.buscarPorId(validacao.getId()) != null));
        validacao.setObservacao("Material conferido, pesado e aprovado pelo parceiro");
        System.out.println("UPDATE: " + validacaoDAO.atualizar(validacao));
        System.out.println("LISTAR: " + validacaoDAO.listarTodas().size());

        Ranking ranking = new Ranking();
        ranking.adicionarUsuario(usuario);

        System.out.println("\n=== RANKING ===");
        ranking.exibirRanking();

        // =================================================================
        // ECOSSISTEMA ODS — tabelas e regras de negocio novas (conquistas,
        // campeonatos, mapa de ODS, desafios agendados). Mesma logica de
        // antes: instancia, valida regra de negocio, executa CRUD real.
        // =================================================================
        OdsDAO odsDAO = new OdsDAO();
        ConquistaDAO conquistaDAO = new ConquistaDAO();
        UsuarioConquistaDAO usuarioConquistaDAO = new UsuarioConquistaDAO();
        CampeonatoDAO campeonatoDAO = new CampeonatoDAO();
        CampeonatoParticipanteDAO participanteDAO = new CampeonatoParticipanteDAO();
        DesafioAgendadoDAO desafioAgendadoDAO = new DesafioAgendadoDAO();

        System.out.println("\n=== ODS (catalogo oficial ONU, somente leitura) ===");
        List<Ods> todasOds = odsDAO.listarTodos();
        System.out.println("Total de ODS cadastradas: " + todasOds.size());
        Ods odsSaude = odsDAO.buscarPorId(3);
        System.out.println("ODS 3: " + (odsSaude != null ? odsSaude.getNome() : "nao encontrada"));

        System.out.println("\n=== CONQUISTAS (catalogo curado, somente leitura) ===");
        List<Conquista> todasConquistas = conquistaDAO.listarTodas();
        System.out.println("Total de conquistas cadastradas: " + todasConquistas.size());
        List<Conquista> conquistasPorContagem = conquistaDAO.listarPorTipo(Conquista.CONTAGEM_TOTAL);
        System.out.println("Conquistas do tipo CONTAGEM_TOTAL: " + conquistasPorContagem.size());

        System.out.println("\n=== MOTOR DE CONQUISTAS (regra de negocio) ===");
        MotorConquistas motor = new MotorConquistas();
        List<Conquista> desbloqueadasAgora = motor.verificarEDesbloquear(usuario);
        System.out.println("Conquistas desbloqueadas nesta simulacao: " + desbloqueadasAgora.size());
        desbloqueadasAgora.forEach(c -> System.out.println(" -> " + c.getNome()));

        System.out.println("\n=== CRUD DESAFIO AGENDADO ===");
        DesafioAgendado desafio = new DesafioAgendado(usuario.getId(), acao.getId(), LocalDate.now().plusDays(7));
        System.out.println("CREATE: " + desafioAgendadoDAO.criar(desafio));
        System.out.println("LISTAR POR USUARIO: " + desafioAgendadoDAO.listarPorUsuario(usuario.getId()).size());
        System.out.println("UPDATE (novo prazo): "
                + desafioAgendadoDAO.atualizarPrazo(usuario.getId(), acao.getId(), LocalDate.now().plusDays(14)));

        System.out.println("\n=== CRUD CAMPEONATO (criado por ADM) ===");
        Campeonato campeonato = new Campeonato(
                "Campeonato Teste Reciclagem SP", "ESTADUAL", "SP", 3,
                LocalDate.now(), LocalDate.now().plusDays(30), "100 pontos de bonus", usuario.getId()
        );
        System.out.println("Campeonato valido: " + campeonato.validarCampeonato());
        System.out.println("CREATE: " + campeonatoDAO.criar(campeonato));
        System.out.println("LISTAR TODOS: " + campeonatoDAO.listarTodos().size());
        System.out.println("LISTAR POR STATUS (AGENDADO): " + campeonatoDAO.listarPorStatus("AGENDADO").size());
        System.out.println("UPDATE (status): " + campeonatoDAO.atualizarStatus(campeonato.getId(), "EM_ANDAMENTO"));

        System.out.println("\n=== CRUD CAMPEONATO_PARTICIPANTE ===");
        CampeonatoParticipante participante = new CampeonatoParticipante(campeonato.getId(), usuario.getId());
        System.out.println("CREATE (inscrever): " + participanteDAO.inscrever(participante));
        System.out.println("ESTA INSCRITO: " + participanteDAO.estaInscrito(campeonato.getId(), usuario.getId()));
        System.out.println("UPDATE (somar pontos): "
                + participanteDAO.somarPontos(campeonato.getId(), usuario.getId(), 40));
        System.out.println("RANKING DO CAMPEONATO: " + participanteDAO.listarRankingPorCampeonato(campeonato.getId()).size());

        System.out.println("\n=== DELETE (ordem respeita as chaves estrangeiras) ===");
        System.out.println("CAMPEONATO_PARTICIPANTE: " + participanteDAO.excluir(campeonato.getId(), usuario.getId()));
        System.out.println("CAMPEONATO: " + campeonatoDAO.excluir(campeonato.getId()));
        System.out.println("DESAFIO_AGENDADO: " + desafioAgendadoDAO.excluir(usuario.getId(), acao.getId()));
        System.out.println("USUARIO_CONQUISTA (se alguma foi desbloqueada): "
                + usuarioConquistaDAO.excluirPorUsuario(usuario.getId()));
        System.out.println("VALIDACAO: " + validacaoDAO.excluir(validacao.getId()));
        System.out.println("EVIDENCIA: " + evidenciaDAO.excluir(evidencia.getId()));
        System.out.println("USUARIO_GRUPO: " + usuarioGrupoDAO.excluir(usuario.getId(), grupo.getId()));
        System.out.println("REGISTRO: " + registroDAO.excluir(registro.getId()));
        System.out.println("PARCEIRO: " + parceiroDAO.excluir(parceiro.getId()));
        System.out.println("GRUPO: " + grupoDAO.excluir(grupo.getId()));
        System.out.println("ACAO: " + acaoDAO.excluir(acao.getId()));
        System.out.println("USUARIO: " + usuarioDAO.excluir(usuario.getId()));
        System.out.println("\nTeste finalizado.");
    }
}
