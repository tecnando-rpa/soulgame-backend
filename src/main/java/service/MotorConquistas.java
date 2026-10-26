package service;

import dao.AcaoSustentavelDAO;
import dao.ConquistaDAO;
import dao.RegistroAcaoDAO;
import dao.UsuarioConquistaDAO;
import model.AcaoSustentavel;
import model.Conquista;
import model.RegistroAcao;
import model.Usuario;
import model.UsuarioConquista;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Verifica, depois de cada ação concluída, se o usuário desbloqueou
 * alguma conquista nova — e desbloqueia automaticamente se sim.
 *
 * Chamado pelo HandlerRegistros logo após um registro ser gravado com
 * sucesso. Não bloqueia o fluxo principal se algo falhar aqui — pontos
 * e o registro em si já foram salvos antes desta etapa rodar.
 */
public class MotorConquistas {

    private final ConquistaDAO conquistaDAO = new ConquistaDAO();
    private final UsuarioConquistaDAO usuarioConquistaDAO = new UsuarioConquistaDAO();
    private final RegistroAcaoDAO registroAcaoDAO = new RegistroAcaoDAO();
    private final AcaoSustentavelDAO acaoSustentavelDAO = new AcaoSustentavelDAO();

    /**
     * @return lista de conquistas desbloqueadas AGORA (nesta chamada) —
     * o chamador usa isso pra decidir se mostra a animação de
     * "nova conquista" na tela.
     */
    public List<Conquista> verificarEDesbloquear(Usuario usuario) {
        List<RegistroAcao> registrosValidados = registroAcaoDAO.listarConcluidasPorUsuario(usuario.getId());

        return conquistaDAO.listarTodas().stream()
                .filter(conquista -> !usuarioConquistaDAO.jaDesbloqueou(usuario.getId(), conquista.getId()))
                .filter(conquista -> criterioAtendido(conquista, usuario, registrosValidados))
                .filter(conquista -> usuarioConquistaDAO.desbloquear(new UsuarioConquista(usuario.getId(), conquista.getId())))
                .toList();
    }

    private boolean criterioAtendido(Conquista conquista, Usuario usuario, List<RegistroAcao> registros) {
        return switch (conquista.getTipoCriterio()) {
            case Conquista.CONTAGEM_TOTAL -> contarRegistrosRelevantes(conquista, registros) >= conquista.getValorCriterio();
            case Conquista.STREAK_DIAS -> usuario.getStreakDias() >= conquista.getValorCriterio();
            case Conquista.ODS_DIFERENTES -> contarOdsDiferentes(registros) >= conquista.getValorCriterio();
            // CAMPEAO_CAMPEONATO é desbloqueada em outro fluxo (quando um
            // campeonato encerra), não a cada registro de ação individual.
            case Conquista.CAMPEAO_CAMPEONATO -> false;
            default -> false;
        };
    }

    private long contarRegistrosRelevantes(Conquista conquista, List<RegistroAcao> registros) {
        return registros.stream()
                .map(registro -> acaoSustentavelDAO.buscarPorId(registro.getAcaoId()))
                .filter(acao -> acao != null)
                .filter(acao -> correspondeAoFiltro(conquista, acao))
                .count();
    }

    private boolean correspondeAoFiltro(Conquista conquista, AcaoSustentavel acao) {
        if (conquista.getSlugAcaoRelacionada() != null) {
            return conquista.getSlugAcaoRelacionada().equals(acao.getSlug());
        }
        if (conquista.getOdsRelacionadoId() != null) {
            return conquista.getOdsRelacionadoId() == acao.getIdOds();
        }
        return true; // conquista geral, conta qualquer acao concluida
    }

    private long contarOdsDiferentes(List<RegistroAcao> registros) {
        Set<Integer> odsDistintos = new HashSet<>();
        for (RegistroAcao registro : registros) {
            AcaoSustentavel acao = acaoSustentavelDAO.buscarPorId(registro.getAcaoId());
            if (acao != null) {
                odsDistintos.add(acao.getIdOds());
            }
        }
        return odsDistintos.size();
    }
}
