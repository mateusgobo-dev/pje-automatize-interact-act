package br.com.jt.pje.manager;

import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.dao.VotoDAO;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.jt.entidades.HistoricoTipoVoto;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.entidades.TipoVotoJT;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.jt.enums.ConclusaoEnum;
import br.jus.pje.jt.enums.MotivoRecebimentoEnum;
import br.jus.pje.jt.enums.ResultadoVotacaoEnum;
import br.jus.pje.jt.enums.TipoResponsavelEnum;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.Usuario;


@Name(VotoManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class VotoManager extends GenericManager {
    private static final long serialVersionUID = 1L;
    public static final String NAME = "votoManager";
    
    @In
    private VotoDAO votoDAO;
    
    @In
    private DocumentoVotoManager documentoVotoManager;
   
    @Logger
	private Log logger;

    public List<Voto> getVotosByProcesso(ProcessoTrf processoTrf) {
        if (processoTrf == null) {
            return null;
        }

        return votoDAO.getVotosByProcesso(processoTrf);
    }

    public List<Voto> getVotosByProcessoSessao(ProcessoTrf processoTrf,
        SessaoJT sessao) {
        if ((sessao == null) || (processoTrf == null)) {
            return null;
        }

        return votoDAO.getVotosByProcessoSessao(processoTrf, sessao);
    }

    public List<Voto> getVotosComposicaoProcessoByProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        if ((sessao == null) || (processoTrf == null)) {
            return null;
        }

        return votoDAO.getVotosComposicaoProcessoByProcessoSessao(processoTrf,
            sessao);
    }

    public List<OrgaoJulgador> getOrgaoJulgadorComVotoLiberado(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        if ((sessao == null) || (processoTrf == null)) {
            return null;
        }

        return votoDAO.getOrgaoJulgadorComVotoLiberado(processoTrf, sessao);
    }

    public List<Voto> getVotosProcessoSemSessaoByOrgaoJugador(SessaoJT sessao,
        ProcessoTrf processoTrf) {
        if ((sessao == null) || (processoTrf == null)) {
            return null;
        }

        return votoDAO.getVotosProcessoSemSessaoByOrgaoJugador(sessao,
            processoTrf);
    }

    public Voto getVotoProcessoSemSessaoByOrgaoJulgador(
        ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador) {
        if ((processoTrf == null) || (orgaoJulgador == null)) {
            return null;
        }

        return votoDAO.getVotoProcessoSemSessaoByOrgaoJulgador(processoTrf,
            orgaoJulgador);
    }

    public Voto getVotoProcessoByOrgaoJulgadorSessao(ProcessoTrf processoTrf,
        OrgaoJulgador orgaoJulgador, SessaoJT sessao) {
        if ((processoTrf == null) || (orgaoJulgador == null) ||
                (sessao == null)) {
            return null;
        }

        return votoDAO.getVotoProcessoByOrgaoJulgadorSessao(processoTrf,
            orgaoJulgador, sessao);
    }

    public Voto getUltimoVotoMagistradoByProcessoOrgaoJulgador(
        ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador) {
        if ((processoTrf == null) || (orgaoJulgador == null)) {
            return null;
        }

        return votoDAO.getUltimoVotoMagistradoByProcessoOrgaoJulgador(processoTrf,
            orgaoJulgador);
    }

    public Voto getUltimoVotoByOrgaoJulgadorProcessoSessao(
        OrgaoJulgador orgaoJulgador, ProcessoTrf processoTrf, SessaoJT sessao) {
        if ((orgaoJulgador == null) || (processoTrf == null) ||
                (sessao == null)) {
            return null;
        }

        return votoDAO.getUltimoVotoByOrgaoJulgadorProcessoSessao(orgaoJulgador,
            processoTrf, sessao);
    }

    public Long quantidadeVotosAcompanhamRelatorByProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        if ((sessao == null) || (processoTrf == null)) {
            return null;
        }

        return votoDAO.quantidadeVotosAcompanhamRelatorByProcessoSessao(processoTrf,
            sessao);
    }

    public Long quantidadeVotosDivergentesByProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        if ((sessao == null) || (processoTrf == null)) {
            return null;
        }

        return votoDAO.quantidadeVotosDivergentesByProcessoSessao(processoTrf,
            sessao);
    }

    public Long quantidadeVotosSemConclusaoByProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        if ((sessao == null) || (processoTrf == null)) {
            return null;
        }

        return votoDAO.quantidadeVotosSemConclusaoByProcessoSessao(processoTrf,
            sessao);
    }

    public Long quantidadeVotosNaoConhecidosByProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        if ((sessao == null) || (processoTrf == null)) {
            return null;
        }

        return votoDAO.quantidadeVotosNaoConhecidosByProcessoSessao(processoTrf,
            sessao);
    }

    public List<Voto> getVotosNaoLiberadosByProcessoSessao(
        ProcessoTrf processoTrf, SessaoJT sessao) {
        if ((sessao == null) || (processoTrf == null)) {
            return null;
        }

        return votoDAO.getVotosNaoLiberadosByProcessoSessao(processoTrf, sessao);
    }

    public boolean existeVotoComDivergencia(ProcessoTrf processoTrf,
        SessaoJT sessao) {
        if ((processoTrf == null) || (sessao == null)) {
            return false;
        }

        return votoDAO.existeVotoComDivergencia(processoTrf, sessao);
    }

    public boolean existeVotoComDestaque(ProcessoTrf processoTrf,
        SessaoJT sessao) {
        if ((processoTrf == null) || (sessao == null)) {
            return false;
        }

        return votoDAO.existeVotoComDestaque(processoTrf, sessao);
    }

    public boolean existeVotoComObservacao(ProcessoTrf processoTrf,
        SessaoJT sessao) {
        if ((processoTrf == null) || (sessao == null)) {
            return false;
        }

        return votoDAO.existeVotoComObservacao(processoTrf, sessao);
    }

    public void atualizaPautaResultadoVotacao(ProcessoTrf processoTrf,
        PautaSessao pautaSessao, List<OrgaoJulgador> orgaosDaComposicao) {
        Boolean alguemDivergiu = Boolean.FALSE;
        Boolean alguemNaoVotou = Boolean.FALSE;
        ResultadoVotacaoEnum resultadoVotacao = null;
        List<Voto> votosDoProcesso = getVotosByProcesso(processoTrf);

        //Se existem votos
        if (votosDoProcesso != null) {
            //Se já existe algum voto do relator e ele já está liberado
            if (existeVotoRelatorLiberado(votosDoProcesso)) {
                //Contabiliza apenas os votos dos outros magistrados e apenas os que estão liberados
                for (Voto voto : votosDoProcesso) {
                    if (voto.getLiberacao()) {
                        if ((voto.getTipoVoto() != null) &&
                                (voto.getTipoVoto().getTipoResponsavel() != TipoResponsavelEnum.R) &&
                                ((voto.getTipoVoto().getConclusao() == ConclusaoEnum.DP) ||
                                (voto.getTipoVoto().getConclusao() == ConclusaoEnum.DR))) {
                            alguemDivergiu = Boolean.TRUE;
                        }

                        if (voto.getTipoVoto() == null) {
                            alguemNaoVotou = Boolean.TRUE;
                        }
                    } else {
                        alguemNaoVotou = Boolean.TRUE;
                    }
                }

                if (votosDoProcesso.size() != orgaosDaComposicao.size()) {
                    alguemNaoVotou = Boolean.TRUE;
                }

                //Se todos votaram e ninguem divergiu
                if (!alguemNaoVotou && !alguemDivergiu) {
                    resultadoVotacao = ResultadoVotacaoEnum.UN;
                }
                //Se todos votaram e alguem divergiu
                else if (!alguemNaoVotou && alguemDivergiu) {
                    resultadoVotacao = ResultadoVotacaoEnum.MA;
                }
                //Se algum não votou e ninguem divergiu
                else if (alguemNaoVotou && !alguemDivergiu) {
                    resultadoVotacao = ResultadoVotacaoEnum.UP;
                }
                //Se alguem não votou e alguém divergiu
                else if (alguemNaoVotou && alguemDivergiu) {
                    resultadoVotacao = ResultadoVotacaoEnum.MP;
                }

                pautaSessao.setResultadoVotacao(resultadoVotacao);
                this.update(pautaSessao);
            }
        }
    }

    private boolean existeVotoRelatorLiberado(List<Voto> votosDoProcesso) {
        for (Voto voto : votosDoProcesso) {
            if ((voto.getTipoVoto() != null) &&
                    (voto.getTipoVoto().getTipoResponsavel() == TipoResponsavelEnum.R) &&
                    voto.getLiberacao()) {
                return true;
            }
        }

        return false;
    }

    public boolean existeVotoRelatorLiberado(ProcessoTrf processoTrf,
        SessaoJT sessao) {
        Voto votoRelator = getVotoProcessoByOrgaoJulgadorSessao(processoTrf,
                processoTrf.getOrgaoJulgador(), sessao);

        return (votoRelator != null) && votoRelator.getLiberacao();
    }

    //TODO Remover para um Service
    public void atualizarVoto(Voto voto, TipoVotoJT tipoVoto, Usuario usuario) {
        if (((voto.getTipoVoto() != null) &&
                !voto.getTipoVoto().equals(tipoVoto)) ||
                ((voto.getTipoVoto() == null) && (tipoVoto != null))) {
            if (voto.getUsuarioTipoVoto() != null) {
                gravarHistorico(voto);
            }

            voto.setUsuarioTipoVoto(usuario);
            voto.setDataTipoVoto(new Date());
            voto.setTipoVoto(tipoVoto);
        }

        voto.setDataAlteracao(new Date());
        update(voto);
    }

    private void gravarHistorico(Voto voto) {
        HistoricoTipoVoto hist = new HistoricoTipoVoto();
        hist.setDataTipoVoto(voto.getDataTipoVoto());
        hist.setUsuarioTipoVoto(voto.getUsuarioTipoVoto());
        hist.setVoto(voto);
        hist.setTipoVoto(voto.getTipoVoto());
        persist(hist);
    }

    public void persistVoto(Voto voto, TipoVotoJT tipoVoto, Usuario usuario) {
        if (tipoVoto != null) {
            voto.setTipoVoto(tipoVoto);
            voto.setUsuarioTipoVoto(usuario);
            voto.setDataTipoVoto(new Date());
        }

        voto.setDataInclusao(new Date());
        persist(voto);
    }

    public void liberarPauta(ProcessoTrf processoTrf) {
        if (processoTrf.getClasseJudicial().getPauta()) {
            processoTrf.setSelecionadoPauta(true);
        } else {
            processoTrf.setSelecionadoJulgamento(true);
        }

        update(processoTrf);
    }

    public void liberarVotos(ProcessoTrf processoTrf, SessaoJT sessao) {
        List<Voto> listaVotos = getVotosNaoLiberadosByProcessoSessao(processoTrf,
                sessao);

        for (Voto voto : listaVotos) {
            voto.setLiberacao(true);
            update(voto);
        }
    }

    //TODO Remover para um service
    public void copiarVotos(ProcessoTrf processoTrf, SessaoJT sessao)
        throws InstantiationException, IllegalAccessException {
        for (Voto voto : getVotosByProcessoSessao(processoTrf, sessao)) {
            Voto novoVoto = EntityUtil.cloneEntity(voto, false);
            novoVoto.setSessao(null);
            persist(novoVoto);

            documentoVotoManager.copiarDocumentos(voto, novoVoto);
        }
    }

    public void lancarMovimentoAptoParaSessao(Processo processo) {
        String codMovimentoRecebimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_COMUNICACAO_RECEBIMENTO;

        // Código = 132 - Descrição = Recebidos os autos #{motivo do recebimento}
        // **************************************************************************************
        MovimentoAutomaticoService.preencherMovimento()
                                  .deCodigo(codMovimentoRecebimento)
                                  .associarAoProcesso(processo)
                                  .comProximoComplementoVazio()
                                  .preencherComCodigo(MotivoRecebimentoEnum.PIP.getCodigo())
                                  .preencherComTexto(MotivoRecebimentoEnum.PIP.getLabel())
                                  .lancarMovimento();
    }
    
	/**
	 * Retorna o voto do orgao julgador atual (logado) em relação ao procesos pautado em questão 
	 * 
	 * @param processoPautado objeto representando um processo pautado em uma sessão de julgamento
	 * @return SessaoProcessoDocumentoVoto o voto do orgao julgador atual
	 */
	public SessaoProcessoDocumentoVoto getVotoProprio(SessaoPautaProcessoTrf processoPautado){
		OrgaoJulgador orgaoAtual = Authenticator.getOrgaoJulgadorAtual();
		if (orgaoAtual == null){ 
			return null;
		}			
		return getVoto(processoPautado.getSessao(), processoPautado.getProcessoTrf(), orgaoAtual);
	}
	
	/**
	 * Retorna o voto do orgao julgador, para uma determinada Sessao e Processo
	 * 
	 * @param sessao Sessao 
	 * @param processo ProcessoTrf
	 * @param julgador OrgaoJulgador votante
	 * @return Voto do orgao julgador
	 */
	public SessaoProcessoDocumentoVoto getVoto(Sessao sessao, ProcessoTrf processo, OrgaoJulgador julgador){
		SessaoProcessoDocumentoVotoManager spdvm = (SessaoProcessoDocumentoVotoManager) Component.getInstance("sessaoProcessoDocumentoVotoManager");
		return spdvm.recuperarVoto(sessao, processo, julgador);
	}
	
	/**
	 * Retorna o voto do relator para a sessao pauta processo.
	 * 
	 * @param processoPautado
	 * @return Retorna o voto do relator
	 */
	public SessaoProcessoDocumentoVoto getVotoRelator(SessaoPautaProcessoTrf processoPautado){
		return getVoto(processoPautado.getSessao(), processoPautado.getProcessoTrf(), processoPautado.getProcessoTrf().getOrgaoJulgador());
	}
	
}
