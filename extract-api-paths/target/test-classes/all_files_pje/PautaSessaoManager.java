package br.com.jt.pje.manager;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.jt.pje.dao.PautaSessaoDAO;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.jt.entidades.HistoricoSituacaoAnalise;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.entidades.TipoSituacaoPauta;
import br.jus.pje.jt.enums.ClassificacaoTipoSituacaoPautaEnum;
import br.jus.pje.jt.enums.ResultadoVotacaoEnum;
import br.jus.pje.jt.enums.SituacaoAnaliseEnum;
import br.jus.pje.jt.enums.SituacaoSessaoEnum;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.DateUtil;

@Name(PautaSessaoManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class PautaSessaoManager extends GenericManager{


	private static final long serialVersionUID = 1L;

	private static final String MSG_ERRO_INCLUSAO_MESA_DURANTE_SESSAO = "O processo {0} não pode ser incluído em mesa pois já está incluído na pauta da {1} de {2}.";
	
	public static final String NAME = "pautaSessaoManager";
	
	@In
	private PautaSessaoDAO pautaSessaoDAO;
	
	public boolean existePautaSessao(SessaoJT sessao){
		if(sessao == null){
			return false;
		}
		return pautaSessaoDAO.existePautaSessao(sessao);
	}
	
	public Date getDataUltimaSessaoByProcesso(ProcessoTrf processoTrf){
		if(processoTrf == null){
			return null;
		}
		return pautaSessaoDAO.getDataUltimaSessaoByProcesso(processoTrf);
	}
	
	public List<PautaSessao> getProcessosPautaSessaoInclusaoPA(SessaoJT sessao){
		if(sessao == null){
			return null;
		}
		return pautaSessaoDAO.getProcessosPautaSessaoInclusaoPA(sessao);
	}
	
	public Integer quantidadeProcessosEmPauta(Object sessao){
		if(sessao == null){
			return null;
		}
		return pautaSessaoDAO.quantidadeProcessosEmPauta(sessao);
	}
	
	public Integer quantidadeProcessosByOrgaoJulgador(SessaoJT sessao, OrgaoJulgador orgaoJulgador){
		if(orgaoJulgador == null){
			return null;
		}
		return pautaSessaoDAO.quantidadeProcessosByOrgaoJulgador(sessao, orgaoJulgador);
	}
	
	public Integer quantidadeProcessosResultadoVotacao(SessaoJT sessao, ResultadoVotacaoEnum resultadoVotacaoEnum){
		if(sessao == null){
			return null;
		}
		return pautaSessaoDAO.quantidadeProcessosResultadoVotacao(sessao, resultadoVotacaoEnum);
	}
	
	public List<PautaSessao> listaPautaSessaoBySessao(SessaoJT sessao){
		if(sessao == null){
			return null;
		}
		return pautaSessaoDAO.listaPautaSessaoBySessao(sessao);
	}
	
	public PautaSessao getPautaSessaoAbertaByProcesso(ProcessoTrf processoTrf){
		return pautaSessaoDAO.getPautaSessaoAbertaByProcesso(processoTrf);
	}

	public PautaSessao getPautaProcessoApregoadoBySessao(SessaoJT sessao) {
		return pautaSessaoDAO.getPautaProcessoApregoadoBySessao(sessao);
	}
	
	public boolean existeProcessoPendente(SessaoJT sessao){
		if(sessao == null){
			return false;
		}
		return pautaSessaoDAO.existeProcessoPendente(sessao);
	}
	
	public boolean existeProcessoJulgadoSemConclusao(SessaoJT sessao){
		if(sessao == null){
			return false;
		}
		return pautaSessaoDAO.existeProcessoJulgadoSemConclusao(sessao);
	}
	
	public void gravarSustentacaoOral(PautaSessao pautaSessao){
		pautaSessao.setSustentacaoOral(true);
		pautaSessao.setDataPedidoSustentacaoOral(new Date());
		pautaSessaoDAO.update(pautaSessao);
	}
	
	public OrgaoJulgador getOrgaoJulgadorRedatorByProcessoSessao(ProcessoTrf processoTrf, SessaoJT sessao){
		if(processoTrf == null || sessao == null){
			return null;
		}
		return pautaSessaoDAO.getOrgaoJulgadorRedatorByProcessoSessao(processoTrf, sessao);
	}
	
	public List<PautaSessao> getProcessosRetiradoPautaOuDeliberado(SessaoJT sessao){
		if(sessao == null){
			return null;
		}
		return pautaSessaoDAO.getProcessosRetiradoPautaOuDeliberado(sessao);
	}
	
	public List<PautaSessao> getProcessosJulgados(SessaoJT sessao){
		if(sessao == null){
			return null;
		}
		return pautaSessaoDAO.getProcessosJulgados(sessao);
	}
	
	public void atualizarSituacaoPauta(PautaSessao pautaSessao, TipoSituacaoPauta tipoSituacaoPauta, Usuario usuario){
		pautaSessao.setDataSituacaoPauta(new Date());
		pautaSessao.setUsuarioSituacaoPauta(usuario);
		pautaSessao.setTipoSituacaoPauta(tipoSituacaoPauta);
		update(pautaSessao);
	}
	
	//TODO Remover para um Service
	public void atualizarPauta(boolean situacaoAnalise, PautaSessao pautaSessao){
		if(situacaoAnalise && !pautaSessao.getSituacaoAnalise().equals(SituacaoAnaliseEnum.A)){
			if(pautaSessao.getUsuarioSituacaoAnalise() != null){
				gravarHistoricoAnalise(pautaSessao);
			}
			pautaSessao.setSituacaoAnalise(SituacaoAnaliseEnum.A);
			pautaSessao.setDataSituacaoAnalise(new Date());
			pautaSessao.setUsuarioSituacaoAnalise(Authenticator.getUsuarioLogado());
		}else if(!situacaoAnalise && pautaSessao.getSituacaoAnalise().equals(SituacaoAnaliseEnum.A)){
			gravarHistoricoAnalise(pautaSessao);			
			pautaSessao.setSituacaoAnalise(SituacaoAnaliseEnum.R);
			pautaSessao.setDataSituacaoAnalise(new Date());
			pautaSessao.setUsuarioSituacaoAnalise(Authenticator.getUsuarioLogado());
		}
		update(pautaSessao);
	}
	
	//TODO Remover para um Service
	public void gravarHistoricoAnalise(PautaSessao pautaSessao){
		HistoricoSituacaoAnalise hist = new HistoricoSituacaoAnalise();
		hist.setPautaSessao(pautaSessao);
		hist.setSituacaoAnalise(pautaSessao.getSituacaoAnalise());
		hist.setDataSituacaoAnalise(pautaSessao.getDataSituacaoAnalise());
		hist.setUsuarioSituacaoAnalise(pautaSessao.getUsuarioSituacaoAnalise());
		update(hist);
	}
	
	public Long quantidadeProcessoBySessaoClassificacao(SessaoJT sessao, ClassificacaoTipoSituacaoPautaEnum classificacao){
		if(sessao == null || classificacao == null){
			return 0L;
		}
		return pautaSessaoDAO.quantidateProcessoBySessaoClassificacao(sessao, classificacao);
	}
	
	public void lancarMovimentoInclusaoPauta(Processo processo, SessaoJT sessaoJT){
		
		String codMovimentoIncluidoProcessoPauta = CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_INCLUSAO_PAUTA_PRESENCIAL;
		
		String dataHoraLocalSessao = (new SimpleDateFormat("dd/MM/yyyy")).format(sessaoJT.getDataSessao()) + ", " 
										+ sessaoJT.getSalaHorario().getHoraInicial() + ", " 
										+ sessaoJT.getSalaHorario().getSala().getSala(); 
		
		// Código = 417 - Descrição = Incluído em pauta para #{data, hora e local da sessão}
		// **************************************************************************************
		MovimentoAutomaticoService.preencherMovimento().deCodigo(codMovimentoIncluidoProcessoPauta)
		  						  .associarAoProcesso(processo)
		  						  .comProximoComplementoVazio().doTipoLivre().preencherComTexto(dataHoraLocalSessao)
		  						  .lancarMovimento();
	}
	
	//TODO retirar daqui
	public boolean existeDocumentoAcordao(ProcessoTrf processoTrf, SessaoJT sessao){
		if(sessao == null || processoTrf == null){
			return false;
		}
		return pautaSessaoDAO.existeDocumentoAcordao(processoTrf, sessao);
	}
	
	public void podeInserirProcessoEmMesaDuranteSessao(ProcessoTrf processoTrf){
		if(processoTrf == null){
			return;
		}
		PautaSessao ps = getUltimaPautaByProcesso(processoTrf);
		if(ps == null){
			return;
		}
		if(!ps.getSessao().getSituacaoSessao().equals(SituacaoSessaoEnum.F)){
			throw new NegocioException(MessageFormat.format(MSG_ERRO_INCLUSAO_MESA_DURANTE_SESSAO, 
										ps.getProcessoTrf().getNumeroProcesso(), 
										ps.getSessao().getTipoSessao(),
										DateUtil.getDataFormatada(ps.getSessao().getDataSessao(), 
																	"dd/MM/yyyy")));
		}
	}
	
	public PautaSessao getUltimaPautaByProcesso(ProcessoTrf processoTrf){
		if(processoTrf == null){
			return null;
		}
		return pautaSessaoDAO.getUltimaPautaByProcesso(processoTrf);
	}
	
	public List<PautaSessao>recuperarProcessosPorSessao(SessaoJT sessao){
		List<PautaSessao> retorno = null;
		if(sessao != null){
			retorno = pautaSessaoDAO.recuperarProcessosPorSessao(sessao);
		}
		return retorno;
	}
	
	public PautaSessao recuperarProcessosPorId(PautaSessao pautaSessao){
		PautaSessao retorno  = null;
		if(pautaSessao != null){
			retorno = pautaSessaoDAO.find(PautaSessao.class, pautaSessao.getIdPautaSessao());
		}
		return retorno;
	}
	
	/**
	 * [PJEII-4718] Método para verificar se um processo possui um dispositivo a ser minutado
	 */
	public boolean possuiDispositivoParaMinutar() {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		
		if (processoTrf == null) {
			return false;
		}
		
		PautaSessao pautaSessao = this.getUltimaPautaByProcesso(processoTrf);
		
		if (pautaSessao == null) {
			return false;
		}
		
		if (pautaSessao.getSituacaoAnalise() == null) {
			return false;
		}
		
		return !pautaSessao.getSituacaoAnalise().equals(SituacaoAnaliseEnum.A);
	}
	
}
