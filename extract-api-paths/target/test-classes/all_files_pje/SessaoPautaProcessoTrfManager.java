package br.jus.cnj.pje.nucleo.manager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.core.Events;
import org.jboss.seam.util.Strings;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.component.tree.EventsHomologarMovimentosTreeHandler;
import br.com.infox.ibpm.component.tree.EventsTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.manager.ModeloDocumentoLocalManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.com.jt.pje.manager.SessaoManager;
import br.jus.cnj.pje.business.dao.SessaoPautaProcessoTrfDAO;
import br.jus.cnj.pje.business.dao.SessaoProcessoDocumentoDAO;
import br.jus.cnj.pje.business.dao.SessaoProcessoDocumentoVotoDAO;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.SessaoJulgamentoService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualImpl;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.servicos.AtividadesLoteService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.fluxo.AcordaoModelo;
import br.jus.cnj.pje.vo.AcordaoCompilacao;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService.MovimentoBuilder;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.dto.SessaoJulgamentoFiltroDTO;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.HistoricoMovimentacaoLote;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.PublicacaoDiarioEletronico;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoComposicao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.enums.JulgamentoEnum;
import br.jus.pje.nucleo.enums.SituacaoProcessoSessaoEnum;
import br.jus.pje.nucleo.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.nucleo.util.Utf8ParaIso88591Util;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componente de controle negocial da entidade {@link SessaoPautaProcessoTrf}.
 * @author cristof
 * @author rafaelmatos
 */
@Name(SessaoPautaProcessoTrfManager.NAME)
public class SessaoPautaProcessoTrfManager extends BaseManager<SessaoPautaProcessoTrf> {
	
	public static final String NAME = "sessaoPautaProcessoTrfManager";
	public static final ScopeType ESCOPO = ScopeType.EVENT;
	public static final String EVENT_SITUACAO_JULGAMENTO = "sessaoPautaProcessoTrfManager:alteraSituacao:emJulgamento";
	
	private Boolean permiteAlterarAcordao;

	private List<SituacaoProcessoSessaoEnum> getListaSituacaoPermitidaParaCerticao(){
		return SituacaoProcessoSessaoEnum.getListaSituacaoPermitidaParaCerticao(ParametroUtil.instance().getListaSituacaoJulgamento());
	}
	
	/**
	 * Método responsável por retornar os tipos permitidos para emissão da certidão de julgamento
	 * conforme o parametro de sistema pje:lista:situacaoJulgamento
	 * @return Array de SituacaoProcessoSessaoEnum
	 */
	public SituacaoProcessoSessaoEnum[] getSituacaoProcessoSessao(){
		List<SituacaoProcessoSessaoEnum> listaPermitidos = getListaSituacaoPermitidaParaCerticao();
		return listaPermitidos.toArray(new SituacaoProcessoSessaoEnum[listaPermitidos.size()]);
	}

	/**
	 * Metodo que retorna uma lista de processos para validar a emissao da Certidao de Julgamento
	 * @param List<SessaoPautaProcessoTrf>
	 * @return List<ProcessoTrf> 
	 */
	public List<ProcessoTrf> recuperaListaProcessosParaValidarCertidaoJulgamento(
			List<SessaoPautaProcessoTrf> sessaoPautaProcessoList) {
		List<ProcessoTrf> listaProcessoVerificar = new ArrayList<ProcessoTrf>(sessaoPautaProcessoList.size());
		List<SituacaoProcessoSessaoEnum> listaProcessosOutrasSituacoes = getListaSituacaoPermitidaParaCerticao();
		for (SessaoPautaProcessoTrf spp : sessaoPautaProcessoList) {
			if (spp.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.JG) || !listaProcessosOutrasSituacoes.isEmpty()){
				listaProcessoVerificar.add(spp.getProcessoTrf());
			}
		}
		return listaProcessoVerificar;
	}

	@Override
	protected SessaoPautaProcessoTrfDAO getDAO() {
		return ComponentUtil.getComponent(SessaoPautaProcessoTrfDAO.class);
	}
	
	/**
	 * Recupera o número total de processos incluídos na sessão dada, independentemente
	 * do tipo de inclusão.
	 *  
	 * @param sessao a sessão de referência.
	 * @return o número total de processos incluídos e não excluídos na sessão dada.
	 */
	public long totalIncluidos(Sessao sessao){
		Search s = new Search(SessaoPautaProcessoTrf.class);
		addCriteria(s, 
				Criteria.equals("sessao", sessao),
				Criteria.isNull("dataExclusaoProcessoTrf"));
		return count(s);
	}
	
	/**
	 * Recupera o número total de processos julgados na sessão dada.
	 * 
	 * @param sessao a sessão de referência
	 * @return o número total de processos julgados na sessão dada.
	 */
	public long totalJulgados(Sessao sessao){
		Search s = new Search(SessaoPautaProcessoTrf.class);
		addCriteria(s, 
				Criteria.equals("sessao", sessao),
				Criteria.isNull("dataExclusaoProcessoTrf"),
				Criteria.equals("situacaoJulgamento", TipoSituacaoPautaEnum.JG));
		return count(s);
	}
	
	/**
	 * Recupera o número total de processos que receberam pedido de vista na sessão dada.
	 * 
	 * @param sessao a sessão de referência
	 * @return o número total de processos que tiveram pedido de vista na sessão dada.
	 */
	public long totalComVista(Sessao sessao){
		return totalNaoJulgados(sessao, AdiadoVistaEnum.PV, false);
	}
	
	/**
	 * Recupera o número total de processos que receberam pedido de adiamento de julgamento
	 * na sessão dada.
	 * 
	 * @param sessao a sessão de referência
	 * @return o número total de processos que tiveram pedido de adiamento
	 */
	public long totalAdiados(Sessao sessao){
		return totalNaoJulgados(sessao, AdiadoVistaEnum.AD, false);
	}
	
	/**
	 * Recupera o número total de processos que foram retirados de pauta na sessão dada.
	 * 
	 * @param sessao a sessão de referência.
	 * @return o número total de processos que foram retirados de pauta.
	 */
	public long totalRetirados(Sessao sessao){
		return totalNaoJulgados(sessao, AdiadoVistaEnum.AD, true);
	}
    
    /**
     * Indica se um processo teve ou não algum julgamento.
     * 
     * Criada na solicitacao [PJEII-4330]
     * 
     * @param processoTrf processo a ser verificado
     * @return Flag indicando se o processo já foi julgado em Sessao
     */
    public Boolean isProcessoJulgado(ProcessoTrf processoTrf) {
        SessaoPautaProcessoTrf sppt = getSessaoPautaProcessoTrfJulgado(processoTrf);
        if (sppt != null) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 
     * @param processoTrf processo para verificação se foi julgado na última sessão, caso já tenha sido pautado
     * @return true caso tenha sido julgado na última sessão em que foi pautado ou false caso não tenha sido julgado
     * ou ainda não tenha sido pautado em nenhuma sessão.
     */
    public Boolean isProcessoJulgadoUltimaSessao(ProcessoTrf processoTrf) {
    	Search s = new Search(SessaoPautaProcessoTrf.class);
    	Criteria sessaoEncerrada = Criteria.not(Criteria.isNull("sessao.dataRealizacaoSessao"));
    	Criteria julgamentoFinalizado = Criteria.equals("julgamentoFinalizado", true);
        addCriteria(s, 
        		Criteria.equals("processoTrf.idProcessoTrf", processoTrf.getIdProcessoTrf()),
        		Criteria.isNull("dataExclusaoProcessoTrf"),
        		Criteria.or(sessaoEncerrada,julgamentoFinalizado));
        s.addOrder("o.sessao.dataRealizacaoSessao", Order.DESC);
    	s.setMax(1);
    	List<SessaoPautaProcessoTrf> ret = list(s);
        return ProjetoUtil.isNotVazio(ret) 
        		&& ret.get(0).getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.JG)
        		&& ret.get(0).getAdiadoVista() == null;
    }
    
    /**
     * Recupera a mais recente sessão de julgamento de um dado processo judicial.
     * 
     * Criada na solicitacao [PJEII-4330]
     * 
     * @param processoTrf O processo
     * @return A sessao do processo julgado.
     */
    public SessaoPautaProcessoTrf getSessaoPautaProcessoTrfJulgado(ProcessoTrf processoTrf) {
    	return getSessaoPautaProcessoTrf(processoTrf, TipoSituacaoPautaEnum.JG);
    }
    
    public SessaoPautaProcessoTrf getSessaoPautaProcessoTrfByID(Integer idSessaoPautaProcessoTrf) {
    	return getDAO().find(idSessaoPautaProcessoTrf);
    }
    
    public SessaoPautaProcessoTrf getSessaoPautaProcessoTrfNaoJulgado(ProcessoTrf processoTrf) {
        return getSessaoPautaProcessoTrf(processoTrf, TipoSituacaoPautaEnum.NJ);
    }
    
    
    private SessaoPautaProcessoTrf getSessaoPautaProcessoTrf(ProcessoTrf processoTrf, TipoSituacaoPautaEnum situacaoJulgamento){
    	Criteria sessaoEncerrada = Criteria.not(Criteria.isNull("sessao.dataRealizacaoSessao"));
    	Criteria julgamentoFinalizado = Criteria.equals("julgamentoFinalizado", true);
    	Search s = new Search(SessaoPautaProcessoTrf.class);
        addCriteria(s, 
        		Criteria.equals("processoTrf", processoTrf),
        		Criteria.equals("situacaoJulgamento", situacaoJulgamento),
        		Criteria.or(sessaoEncerrada,julgamentoFinalizado));
        s.addOrder("o.sessao.dataRealizacaoSessao", Order.DESC);
    	s.setMax(1);
    	List<SessaoPautaProcessoTrf> ret = list(s);
        return ret.isEmpty() ? null : ret.get(0);
    }
    
    
	/**
	 * Método responsável por retornar a última {@link SessaoPautaProcessoTrf}
	 * do processo caso sua situação de julgamento seja
	 * {@link TipoSituacaoPautaEnum.AJ} ou {@link TipoSituacaoPautaEnum.EJ},
	 * essas situações consideram que o processo está pautado numa sessão.
	 * 
	 * @param processoTrf
	 *            o processo que se deseja retornar a última sessão
	 * @return <code>SessaoPautaProcessoTrf</code> a última sessão do processo
	 *         pautado
	 */
    public SessaoPautaProcessoTrf getSessaoPautaProcessoPautado(ProcessoTrf processoTrf){
    	Search s = new Search(SessaoPautaProcessoTrf.class);
        addCriteria(s, 
        		Criteria.equals("processoTrf", processoTrf),
        		Criteria.in("situacaoJulgamento", 
        				new TipoSituacaoPautaEnum[]{TipoSituacaoPautaEnum.AJ, TipoSituacaoPautaEnum.EJ}));
        s.addOrder("o.sessao.dataSessao", Order.DESC);
    	s.setMax(1);
    	List<SessaoPautaProcessoTrf> ret = list(s);
        return ret.isEmpty() ? null : ret.get(0);
    }
    
    /**
     * Recupera SessaoPautaProcessoTrf pelo processo e sessao
     * @author rafaelmatos
     * @since 23/07/2015
     * @param processoTrf
     * @param sessao
     * @return SessaoPautaProcessoTrf
     */
    public SessaoPautaProcessoTrf getSessaoPautaProcessoTrf(ProcessoTrf processoTrf, Sessao sessao){
    	Search s = new Search(SessaoPautaProcessoTrf.class);
        addCriteria(s, 
        		Criteria.equals("processoTrf", processoTrf),
        		Criteria.equals("sessao", sessao));
        s.addOrder("o.sessao.dataRealizacaoSessao", Order.DESC);
    	s.setMax(1);
    	List<SessaoPautaProcessoTrf> ret = list(s);
        return ret.isEmpty() ? null : ret.get(0);
    }
    
	/**
	 * Recupera o número total de processos não julgados em uma dada sessão, deixando
	 * de incluir na contagem os processos que tenham sido dela excluídos e seguindo
	 * os parâmetros indicados.
	 * 
	 * @param sessao a sessão de julgamento na qual deverá ser feita a contagem
	 * @param tipoRetirada o tipo de adiamento definido
	 * @param retiradoJulgamento marca indicativa quanto ao tipo de retirada de julgamento, a ser
	 * aplicado quando tipoRetirada for diferente de {@link AdiadoVistaEnum#PV}
	 * @return o número total de processos não julgados.
	 */
	private long totalNaoJulgados(Sessao sessao, AdiadoVistaEnum tipoRetirada, boolean retiradoJulgamento){
		Search s = new Search(SessaoPautaProcessoTrf.class);
		addCriteria(s, 
				Criteria.equals("sessao", sessao),
				Criteria.isNull("dataExclusaoProcessoTrf"),
				Criteria.equals("adiadoVista", tipoRetirada));
		if(tipoRetirada != AdiadoVistaEnum.PV){
			addCriteria(s, Criteria.equals("retiradaJulgamento", retiradoJulgamento));
		}
		return count(s);
	}
	
	
	public long totalEmSituacao(Sessao sessao, TipoSituacaoPautaEnum tipoSituacaoEnum){
		Search s = new Search(SessaoPautaProcessoTrf.class);
		addCriteria(s, 
				Criteria.equals("sessao", sessao),
				Criteria.isNull("dataExclusaoProcessoTrf"),
				Criteria.equals("situacaoJulgamento", tipoSituacaoEnum));
		return count(s);
	}

	public List<SessaoPautaProcessoTrf> getProcessoSessao(Sessao sessao, TipoSituacaoPautaEnum... situacoes) {
		if(situacoes == null || situacoes.length == 0){
			return Collections.emptyList();
		}
		Search s = new Search(SessaoPautaProcessoTrf.class);
		s.addOrder("numeroOrdem", Order.ASC);
		addCriteria(s, 
				Criteria.equals("sessao", sessao),
				Criteria.isNull("dataExclusaoProcessoTrf"),
				Criteria.in("situacaoJulgamento", situacoes));
		s.addOrder("o.numeroOrdem", Order.ASC);
		return list(s);
	}

	public List<SessaoPautaProcessoTrf> recuperarPautaSessaoOrdenada(Sessao sessao) {
		if (sessao == null) {
			return Collections.emptyList();
		}
		return recuperarPautaSessaoOrdenada(sessao.getIdSessao());
	}
	
	public List<SessaoPautaProcessoTrf> recuperarPautaSessaoOrdenada(Integer idSessao) {
		if (idSessao == null) {
			return Collections.emptyList();
		}
		Search s = new Search(SessaoPautaProcessoTrf.class);
		s.addOrder("numeroOrdem", Order.ASC);
		addCriteria(s, 
			Criteria.equals("sessao.idSessao", idSessao), 
			Criteria.isNull("dataExclusaoProcessoTrf"));
		
		return list(s);
	}

	public void atualizaOrdem(SessaoPautaProcessoTrf processoPautaNovo) throws PJeBusinessException {
		int numeroOrdemNovo = processoPautaNovo.getNumeroOrdem();

		List<SessaoPautaProcessoTrf> listaPauta = new ArrayList<SessaoPautaProcessoTrf>(
				recuperarPautaSessaoOrdenada(processoPautaNovo.getSessao()));
		
		listaPauta.remove(processoPautaNovo);
		listaPauta.add(numeroOrdemNovo-1, processoPautaNovo);

		int numeroOrdem = 0;
		for (SessaoPautaProcessoTrf processoPauta: listaPauta) {
			processoPauta.setNumeroOrdem(++numeroOrdem);
			persist(processoPauta);
		}
		flush();
	}
	
	/**
	 * Retorna uma lista dos anos das sessões de julgamento.
	 * 
	 * @return Lista dos anos das sessões de julgamento.
	 */
	public List<Integer> getAnosSessaoJulgamento() {
		return this.getDAO().getAnosSessaoJulgamento();
	}
	
	public SessaoPautaProcessoTrf pautarProcesso(Sessao sessao, ProcessoTrf processoTrf) throws Exception {
		return pautarProcesso(sessao, processoTrf, TipoInclusaoEnum.PA);
	}
	
	public SessaoPautaProcessoTrf pautarProcessoIncluidoEmMesa(Sessao sessao, ProcessoTrf processoTrf) throws Exception {
		return pautarProcesso(sessao, processoTrf, TipoInclusaoEnum.ME);
	}
	
	public SessaoPautaProcessoTrf pautarProcesso(Sessao sessao, ProcessoTrf processoTrf, TipoInclusaoEnum tipoInclusao) throws Exception {
		SessaoPautaProcessoTrf sessaoPautaProcesso = criarSessaoPautaProcessoTrf(sessao, processoTrf, tipoInclusao);

		atualizaSessaoProcessoDocumentos(processoTrf, sessao);		
		
		getDAO().persist(sessaoPautaProcesso);
		getDAO().flush();
		getDAO().refresh(sessaoPautaProcesso);
		
		return sessaoPautaProcesso;
	}
		

	public SessaoPautaProcessoTrf pautarProcessoQueFoiAdiadoOuTevePedidoVista(Sessao sessao, ProcessoTrf processoTrf, TipoInclusaoEnum tipoInclusao, Boolean retiradoJulgamento) throws Exception {
		SessaoPautaProcessoTrf sessaoPautaProcesso = criarSessaoPautaProcessoTrf(sessao, processoTrf, tipoInclusao);
		atualizaSessaoProcessoDocumentos(processoTrf, sessao);	
		if(retiradoJulgamento){
			sessaoPautaProcesso.setIntimavel(true);
		}
		
		getDAO().persist(sessaoPautaProcesso);
		getDAO().flush();
		
		return sessaoPautaProcesso;
	}
	
	public SessaoPautaProcessoTrf criarSessaoPautaProcessoTrf(Sessao sessao, ProcessoTrf processoTrf, TipoInclusaoEnum tipoInclusao) throws Exception {
		
		SessaoPautaProcessoTrf sessaoPautaProcesso = new SessaoPautaProcessoTrf();
		
		sessaoPautaProcesso.setSessao(sessao);
		sessaoPautaProcesso.setProcessoTrf(processoTrf);
		sessaoPautaProcesso.setUsuarioInclusao(Authenticator.getUsuarioLogado());
		sessaoPautaProcesso.setOrgaoJulgadorUsuarioInclusao(Authenticator.getOrgaoJulgadorAtual());
		sessaoPautaProcesso.setDataInclusaoProcessoTrf(new Date());
		sessaoPautaProcesso.setTipoInclusao(tipoInclusao);
		sessaoPautaProcesso.setSustentacaoOral(Boolean.FALSE);
		sessaoPautaProcesso.setPreferencia(Boolean.FALSE);
		sessaoPautaProcesso.setRetiradaJulgamento(Boolean.FALSE);
		sessaoPautaProcesso.setPresidente(sessao.getPresidenteSessao());
		if(tipoInclusao.equals(TipoInclusaoEnum.ME) && sessao.getContinua()){
			sessaoPautaProcesso.setSituacaoJulgamento(TipoSituacaoPautaEnum.EJ);
		}else{
			sessaoPautaProcesso.setSituacaoJulgamento(TipoSituacaoPautaEnum.AJ);
		}
		ComponentUtil.getComponent(SessaoPautaProcessoComposicaoManager.class).criarComposicaoJulgamento(sessaoPautaProcesso);
		
		SessaoProcessoDocumentoVoto votoDoRelator = ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class).recuperarVotoAntecipado(processoTrf, processoTrf.getOrgaoJulgador());
		if (votoDoRelator != null && StringUtils.isNotBlank(votoDoRelator.getTextoProclamacaoJulgamento())) {
			sessaoPautaProcesso.setProclamacaoDecisao(Utf8ParaIso88591Util.converter(StringUtil.removeHtmlTags(votoDoRelator.getTextoProclamacaoJulgamento())));
		}
		
		return sessaoPautaProcesso;
	}

	public SessaoPautaProcessoTrf marcarPreferencia(SessaoPautaProcessoTrf sppt) throws Exception {

		if (sppt.getPreferencia() == null) {
			sppt.setPreferencia(Boolean.TRUE);
		}
		else {
			sppt.setPreferencia(!sppt.getPreferencia());
		}	
		
		return alterar(sppt);
	}
	
	public SessaoPautaProcessoTrf marcarPautaRapida(SessaoPautaProcessoTrf sppt) throws Exception {
		
		if (sppt.getMaioriaDetectada() == null){
			sppt.setMaioriaDetectada(Boolean.TRUE);
		}
		else {
			sppt.setMaioriaDetectada(!sppt.getMaioriaDetectada());
		}
		
		return alterar(sppt);
	}	

	public SessaoPautaProcessoTrf adiar(SessaoPautaProcessoTrf sppt) throws Exception {
		
		sppt.setAdiadoVista(AdiadoVistaEnum.AD);
		sppt.setSituacaoJulgamento(TipoSituacaoPautaEnum.NJ);
		sppt.setRetiradaJulgamento(Boolean.FALSE);
		
		return alterar(sppt);		
	}

	public SessaoPautaProcessoTrf retirarDePauta(SessaoPautaProcessoTrf sppt) throws Exception {
		
		sppt.setAdiadoVista(AdiadoVistaEnum.AD);
		sppt.setRetiradaJulgamento(Boolean.TRUE);
		sppt.setSituacaoJulgamento(TipoSituacaoPautaEnum.NJ);
		
		return alterar(sppt);
	}
	
	@Observer(EVENT_SITUACAO_JULGAMENTO)
	public SessaoPautaProcessoTrf alterarSituacaoParaEmJulgamento(Integer id) throws Exception {
		SessaoPautaProcessoTrf sppt = findById(id);
		if(sppt != null) {
			return alterarSituacaoParaEmJulgamento(sppt);
		}
		return null;
	}


	public SessaoPautaProcessoTrf alterarSituacaoParaEmJulgamento(SessaoPautaProcessoTrf sppt) throws Exception {

		sppt.setSituacaoJulgamento(TipoSituacaoPautaEnum.EJ);
		sppt.setAdiadoVista(null);
		sppt.setRetiradaJulgamento(false);
	
		if (sppt.getOrgaoJulgadorVencedor() == null) {			
			sppt.setOrgaoJulgadorVencedor(sppt.getProcessoTrf().getOrgaoJulgador());
		}
		// PJEII-19619
		// Verifica se esta definido o parâmetro pje:fluxo:sessao:processoEmJulgamento
		// e caso positivo adicionar o processo neste fluxo ao colocado em julgamento
		String codigoFluxo = ComponentUtil.getComponent(ParametroService.class).valueOf(Parametros.PARAMETRO_FLUXO_PROCESSO_EM_JULGAMENTO);

		// Verifica se o código do fluxo foi configurado
		if (codigoFluxo != null && !codigoFluxo.trim().isEmpty()) {
			Fluxo fluxo = ComponentUtil.getComponent(FluxoManager.class).findByCodigo(codigoFluxo);
			if(fluxo == null) {
				throw new IllegalArgumentException("Não foi encontrado o fluxo " + codigoFluxo + " definido para ser iniciado ao colocar o processo em julgamento");
			}
			Integer idProcesso = Integer.valueOf(sppt.getProcessoTrf().getIdProcessoTrf());
			boolean existeFluxoJulgamentoAtivo = ComponentUtil.getComponent(FluxoManager.class).existeProcessoNoFluxo(idProcesso, fluxo.getFluxo());
			if(!existeFluxoJulgamentoAtivo) {
				ProcessoJudicialService processoJudicialService = ComponentUtil.getComponent(ProcessoJudicialService.class);
				processoJudicialService.incluirNovoFluxo(sppt.getProcessoTrf(), codigoFluxo);
			}
		}
		
		return alterar(sppt);
	}
	
	public SessaoPautaProcessoTrf alterarSituacaoParaAguardandoJulgamento(SessaoPautaProcessoTrf sppt) throws Exception {
	
		sppt.setOrgaoJulgadorVencedor(null);
		sppt.setSituacaoJulgamento(TipoSituacaoPautaEnum.AJ);
		sppt.setRetiradaJulgamento(Boolean.FALSE);
		sppt.setAdiadoVista(null);
		
		return alterar(sppt);		
	}

	public SessaoPautaProcessoTrf registrarJulgamento(SessaoPautaProcessoTrf sppt) throws Exception {
		validarOrgaoJulgadorVencedor(sppt);		
		sppt.setSituacaoJulgamento(TipoSituacaoPautaEnum.JG);
		sppt.setAdiadoVista(null);
		return alterar(sppt);		
	}
	
	/**
	 * Dado um processo pautado, verifica se foi registrado um órgão julgador vencedor válido e,
	 * casso não tenha sido, retorna uma exceção.
	 * @param sppt processo pautado
	 * @throws Exception caso não tenha sido registrado um órgão julgador vencedor válido
	 */
	private void validarOrgaoJulgadorVencedor(SessaoPautaProcessoTrf sppt) throws Exception {
		OrgaoJulgador ojVencedor = sppt.getOrgaoJulgadorVencedor();
		
		if (ojVencedor == null){
			throw new Exception(MessageFormat.format("Não foi informado um órgão julgador vencedor para o processo: {0} !", 
					sppt.getProcessoTrf().getNumeroProcesso()
			));
		}
		
		if (!sppt.getParticipaVotacao(ojVencedor)) {
			throw new Exception(MessageFormat.format("O órgão julgador: {0} vencedor não está cadastrado como votante na composição do processo: {1}!", 
					sppt.getOrgaoJulgadorVencedor().getOrgaoJulgador(),
					sppt.getProcessoTrf().getNumeroProcesso()
			));
		}
	}
	
	public SessaoPautaProcessoTrf registrarPedidoVista(SessaoPautaProcessoTrf sppt, OrgaoJulgador orgaoJulgadorPediuVista) throws Exception {
		return registrarPedidoVista(sppt, orgaoJulgadorPediuVista, null);
	}
	
	public SessaoPautaProcessoTrf registrarPedidoVista(SessaoPautaProcessoTrf sppt, OrgaoJulgador orgaoJulgadorPediuVista, OrgaoJulgadorCargo orgaoJulgadorCargo) throws Exception {
		
		// Verifica se o órgão julgador escolhido e votante
		if (!sppt.getParticipaVotacao(orgaoJulgadorPediuVista)) {
			throw new Exception(MessageFormat.format("O órgão julgador: {0} que pediu vista não esta cadastrado como votante na composiçãoo do processo: {1}!", 
					orgaoJulgadorPediuVista.getOrgaoJulgador(),
					sppt.getProcessoTrf().getNumeroProcesso()
			));
		}
		
		sppt.setAdiadoVista(AdiadoVistaEnum.PV);
		sppt.setSituacaoJulgamento(TipoSituacaoPautaEnum.NJ);
		sppt.setOrgaoJulgadorPedidoVista(orgaoJulgadorPediuVista);
		sppt.setOrgaoJulgadorCargoPedidoVista(orgaoJulgadorCargo);
		
		return alterar(sppt);
	}
	
	/**
	 * Retira o pedido de vista e retorna para aguardando julgamento
	 * @author rafaelmatos
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-20149
	 * @since 26/05/2015
	 * @param SessaoPautaProcessoTrf dados do processo na sessão
	 */
	public SessaoPautaProcessoTrf retiraPedidoVista(SessaoPautaProcessoTrf sppt) throws Exception {
		sppt.setAdiadoVista(null);
		if(sppt.getSessao().getContinua() != null && sppt.getSessao().getContinua()) {
			sppt.setSituacaoJulgamento(TipoSituacaoPautaEnum.EJ);
		}
		else {
			sppt.setSituacaoJulgamento(TipoSituacaoPautaEnum.AJ);
		}
		sppt.setOrgaoJulgadorPedidoVista(null);
		return alterar(sppt);
	}
	
	/**
	 * Arruma os registros de SessaoProcessoDocumento e SessaoProcessoDocumentoVoto que sao orfaos de uma sessao,
	 * faz isso por causa dos registros legados, pois na inclusao do processo em pauta ele executa o mesmo procedimento
	 * 
	 * @param processoTrf
	 * @param sessao
	 * @throws Exception
	 */
	public void atualizaSessaoProcessoDocumentos(ProcessoTrf processoTrf, Sessao sessao) throws Exception {
		getSessaoProcessoDocumentoManager().atualizarSessaoProcessoDocumentos(processoTrf, sessao);		
		getSessaoProcessoDocumentoVotoManager().atualizarSessaoProcessoDocumentosVotos(processoTrf, sessao);		
	}

	public AcordaoCompilacao recuperarAcordaoCompilacao(SessaoPautaProcessoTrf sppt, TaskInstance taskInstance) {
		

		AcordaoCompilacao acordaoCompilacao = new AcordaoCompilacao();
		acordaoCompilacao.setSessaoPautaProcessoTrf(sppt);
		acordaoCompilacao.setTaskInstance(taskInstance);
		acordaoCompilacao.setComposicaoSessaoJulgamento(ComponentUtil.getComponent(SessaoJulgamentoService.class).getPresentesPorOrgaoJulgador(sppt));

		// Relatorio
		SessaoProcessoDocumento spdRelatorio = ComponentUtil.getComponent(SessaoProcessoDocumentoDAO.class).recuperarRelatorioAtivoPorSessaoEhProcesso(sppt.getSessao(), sppt.getProcessoTrf(), sppt.getOrgaoJulgadorRelator());
		
		// Ementa
		SessaoProcessoDocumento spdEmentaRelatorAcordao = recuperarEmentaAtivaPorSessaoEhProcessoEhOrgaoJulgador(sppt.getSessao(), sppt.getProcessoTrf(), sppt.getOrgaoJulgadorVencedor());

		// Notas orais
		SessaoProcessoDocumento spdNotasOrais = ComponentUtil.getComponent(SessaoProcessoDocumentoDAO.class).recuperarNotasOraisAtivaPorSessaoEhProcessoEhOrgaoJulgador(sppt.getSessao(), sppt.getProcessoTrf(), sppt.getOrgaoJulgadorVencedor());
		
		acordaoCompilacao.setEmentaRelatorDoAcordao(spdEmentaRelatorAcordao);
		acordaoCompilacao.setRelatorio(spdRelatorio);
		acordaoCompilacao.setNotasOrais(spdNotasOrais);
		
		// Votos
		acordaoCompilacao.setVotos(ComponentUtil.getComponent(SessaoProcessoDocumentoVotoDAO.class).recuperarSessaoVotosComDocumentosPorSessaoEhProcesso(sppt.getSessao(), sppt.getProcessoTrf()));
				
		// Acordao
		try {
			acordaoCompilacao.setAcordao(recuperarProcessoDocumentoAcordaoSeNaoExistirCriarUmNovoPorAcordaoCompilacao(acordaoCompilacao));
		} catch (PJeBusinessException ex) {
			throw new PJeRuntimeException(ex);
		}

		atualizarEstadoAssinaturasDocumentos(acordaoCompilacao);
		
		return acordaoCompilacao;
	}

	/**
	 * Método responsável por atualizar o estado das assinaturas dos documentos vinculados ao acórdão especificado
	 * @param acordaoCompilacao
	 */
	public void atualizarEstadoAssinaturasDocumentos(AcordaoCompilacao acordaoCompilacao) {
		DocumentoJudicialService documentoJudicialService = ComponentUtil.getComponent(DocumentoJudicialService.class);
		
		// Verificar se o relatorio existe e foi assinado
		if (acordaoCompilacao.getRelatorio() == null || !documentoJudicialService.temAssinatura(acordaoCompilacao.getRelatorio().getProcessoDocumento())) {
			acordaoCompilacao.setAssinadoRelatorio(false);
		} else {
			acordaoCompilacao.setAssinadoRelatorio(true);
		}
		
		// Verificar se a ementa existe e foi assinada
		if (acordaoCompilacao.getEmentaRelatorDoAcordao() == null || !documentoJudicialService.temAssinatura(acordaoCompilacao.getEmentaRelatorDoAcordao().getProcessoDocumento())) {
			acordaoCompilacao.setAssinadoEmenta(false);
		} else {
			acordaoCompilacao.setAssinadoEmenta(true);
		}
		
		// Verificar se o voto do relator existe e foi assinado
		if (acordaoCompilacao.getVotoRelatorDoProcesso() == null || !documentoJudicialService.temAssinatura(acordaoCompilacao.getVotoRelatorDoProcesso().getProcessoDocumento())) {
			acordaoCompilacao.setAssinadoVotoRelatorProcesso(false);
		} else {
			acordaoCompilacao.setAssinadoVotoRelatorProcesso(true);
		}
		
		// Verificar se o voto do relator do acórdão existe e foi assinado
		if (acordaoCompilacao.getVotoRelatorDoAcordao() == null || !documentoJudicialService.temAssinatura(acordaoCompilacao.getVotoRelatorDoAcordao().getProcessoDocumento())) {
			acordaoCompilacao.setAssinadoVotoRelatorAcordao(false);
		} else {
			acordaoCompilacao.setAssinadoVotoRelatorAcordao(true);
		}
		
		// Verificar se a nota oral existe e foi assinada
		if (acordaoCompilacao.getNotasOrais() == null || !documentoJudicialService.temAssinatura(acordaoCompilacao.getNotasOrais().getProcessoDocumento())) {
			acordaoCompilacao.setAssinadoNotasOrais(false);
		} else {
			acordaoCompilacao.setAssinadoNotasOrais(true);
		}
	}

	public AcordaoCompilacao recuperarAcordaoCompilacaoParaElaboracao(SessaoPautaProcessoTrf sppt, TaskInstance taskInstance) {		
		SessaoJulgamentoService sessaoJulgamentoService = ComponentUtil.getSessaoJulgamentoService();

		AcordaoCompilacao acordaoCompilacao = new AcordaoCompilacao();
		acordaoCompilacao.setSessaoPautaProcessoTrf(sppt);
		acordaoCompilacao.setTaskInstance(taskInstance);
		acordaoCompilacao.setComposicaoSessaoJulgamento(sessaoJulgamentoService.getPresentesPorOrgaoJulgador(sppt));
		acordaoCompilacao.setAssinadoRelatorio(true);

		if (sppt != null) {
			atribuirDocumentosComSessaoJulgamento(sppt, acordaoCompilacao);

		} else {
			atribuirDocumentoSemSessaoJulgamento(acordaoCompilacao);
		}
		
		// Verifica se o relatorio existe e foi assinado
		DocumentoJudicialService documentoJudicialService = ComponentUtil.getComponent(DocumentoJudicialService.class);
		if (acordaoCompilacao.getRelatorio() == null
				|| documentoJudicialService.validaAssinaturasDocumento(acordaoCompilacao.getRelatorio().getProcessoDocumento(), true, false).isEmpty()) {
			acordaoCompilacao.setAssinadoRelatorio(false);
		}
		
		// Acordao
		try {
			acordaoCompilacao.setAcordao(recuperarProcessoDocumentoAcordaoSeNaoExistirCriarUmNovoPorAcordaoCompilacao(acordaoCompilacao));
		} catch (PJeBusinessException ex) {
			throw new PJeRuntimeException(ex);
		}
		
		return acordaoCompilacao;
	}

	private void atribuirDocumentoSemSessaoJulgamento(AcordaoCompilacao acordaoCompilacao) {
		DocumentoJudicialService documentoJudicialService = ComponentUtil.getDocumentoJudicialService();

		// Recupera Relatório, Ementa e Voto Relator que foram cadastradas antes do julgamento acontecer...
		Map<TipoProcessoDocumento, SessaoProcessoDocumento> hashDocumentos = documentoJudicialService.recuperaDocumentosSemSessaoJulgamento(ProcessoJbpmUtil.getProcessoTrf());
		SessaoProcessoDocumento spdRelatorio = hashDocumentos.get(ParametroUtil.instance().getTipoProcessoDocumentoRelatorio());
		SessaoProcessoDocumento spdEmentaRelatorAcordao = hashDocumentos.get(ParametroUtil.instance().getTipoProcessoDocumentoEmenta());

		acordaoCompilacao.setRelatorio(spdRelatorio);
		acordaoCompilacao.setEmentaRelatorDoAcordao(spdEmentaRelatorAcordao);

		try {
			// Recupera as Notas Orais que foram cadastradas antes do julgamento acontecer...
			TipoProcessoDocumento tipoDocNotasOrais = ParametroUtil.instance().getTipoProcessoDocumentoNotasOrais();

			Integer[] tiposDocumento = new Integer[] {tipoDocNotasOrais.getIdTipoProcessoDocumento()};

			List<ProcessoDocumento> listProcessoDocumento = documentoJudicialService.getDocumentosPorTipos(ProcessoJbpmUtil.getProcessoTrf(), tiposDocumento);
			for (ProcessoDocumento procDoc : listProcessoDocumento) {
				if (procDoc.getTipoProcessoDocumento().equals(tipoDocNotasOrais)) {
					SessaoProcessoDocumento spdNotasOrais = new SessaoProcessoDocumento();
					spdNotasOrais.setProcessoDocumento(procDoc);

					acordaoCompilacao.setNotasOrais(spdNotasOrais);
				}
			}

			// Recupera os documentos do tipo Voto
			TipoProcessoDocumento tipoDocVoto = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
			listProcessoDocumento = documentoJudicialService.getDocumentosPorTipos(ProcessoJbpmUtil.getProcessoTrf(), tipoDocVoto.getIdTipoProcessoDocumento());
			List<SessaoProcessoDocumentoVoto> listVotos = new ArrayList<SessaoProcessoDocumentoVoto>();
			if (!CollectionUtilsPje.isEmpty(listProcessoDocumento)) {
				listVotos = new ArrayList<SessaoProcessoDocumentoVoto>(listProcessoDocumento.size());
				for (ProcessoDocumento procDoc : listProcessoDocumento) {
					// Verifica se foi cadastrado antecipadamente à sessão de julgamento
					SessaoProcessoDocumento documentoAntecipado = getSessaoProcessoDocumentoManager().recuperaPorProcessoDocumento(procDoc);
					if (documentoAntecipado != null && documentoAntecipado.getIdSessaoProcessoDocumento() != 0) {
						listVotos.add((SessaoProcessoDocumentoVoto) documentoAntecipado);
					}
				}
			}
			acordaoCompilacao.setVotos(listVotos);
		} catch (PJeBusinessException e) {
			logger.error("Não foi possível recuperar os documentos vinculados ao Acórdão ou Resolução: {0}", e.getLocalizedMessage());
		}
		
	}

	private void atribuirDocumentosComSessaoJulgamento(SessaoPautaProcessoTrf sppt, AcordaoCompilacao acordaoCompilacao) {
		// Relatorio
		SessaoProcessoDocumentoDAO sessaoProcessoDocumentoDAO = ComponentUtil.getSessaoProcessoDocumentoDAO();
		SessaoProcessoDocumento spdRelatorio = sessaoProcessoDocumentoDAO.recuperarRelatorioAtivoPorSessaoEhProcesso(sppt.getSessao(), sppt.getProcessoTrf(), sppt.getOrgaoJulgadorRelator());
		acordaoCompilacao.setRelatorio(spdRelatorio);

		// Ementa
		OrgaoJulgador oj = sppt.getOrgaoJulgadorRelator();
		if (sppt.getOrgaoJulgadorVencedor() != null && sppt.getOrgaoJulgadorVencedor().getIdOrgaoJulgador() != oj.getIdOrgaoJulgador()) {
			oj = sppt.getOrgaoJulgadorVencedor();
		}
		SessaoProcessoDocumento spdEmentaRelatorAcordao = recuperarEmentaAtivaPorSessaoEhProcessoEhOrgaoJulgador(sppt.getSessao(), sppt.getProcessoTrf(), oj);
		acordaoCompilacao.setEmentaRelatorDoAcordao(spdEmentaRelatorAcordao);

		// Notas orais
		SessaoProcessoDocumento spdNotasOrais = sessaoProcessoDocumentoDAO.recuperarNotasOraisAtivaPorSessaoEhProcessoEhOrgaoJulgador(sppt.getSessao(), sppt.getProcessoTrf(), sppt.getOrgaoJulgadorRelator());
		acordaoCompilacao.setNotasOrais(spdNotasOrais);

		// Votos
		SessaoProcessoDocumentoVotoDAO sessaoProcessoDocumentoVotoDAO = ComponentUtil.getSessaoProcessoDocumentoVotoDAO();
		List<SessaoProcessoDocumentoVoto> listVotos = sessaoProcessoDocumentoVotoDAO.recuperarSessaoVotosComDocumentosPorSessaoEhProcesso(sppt.getSessao(), sppt.getProcessoTrf());
		acordaoCompilacao.setVotos(listVotos);
	}

	public ProcessoDocumento recuperarProcessoDocumentoAcordaoSeNaoExistirCriarUmNovoPorAcordaoCompilacao(AcordaoCompilacao acordaoCompilacao) throws PJeBusinessException {
		ProcessoDocumento acordao = null;
        
		//Tenta recuperar primeiramente o documento acordao pela
		//associacao entre documentos e sessao
		if(acordaoCompilacao.getSessaoPautaProcessoTrf() != null){
	        SessaoProcessoDocumento sessaoDocAcordao = getSessaoProcessoDocumentoManager()
	    			.getSessaoProcessoDocumentoByTipo(acordaoCompilacao.getSessaoPautaProcessoTrf().getSessao(), 
	    					ParametroUtil.instance().getTipoProcessoDocumentoAcordao(), 
	    					acordaoCompilacao.getSessaoPautaProcessoTrf().getProcessoTrf().getProcesso());	            	

	    	if(sessaoDocAcordao != null) {
	    		acordao = sessaoDocAcordao.getProcessoDocumento();
	    		
	    		//Verifica se a variavel de "minutaEmElaboracao" esta com o valor do documento
	    		//acordao recuperado pela sessao
	    		//Se nao houver variavel ou ela estiver com valor diferente do documento recuperado
	    		//Setta com o documento recuperado, pois as tarefas de assinatura utilizam esta variavel
	    		//no processo de assinatura
	    		if(acordao.getDataJuntada() == null && ComponentUtil.getSessaoProcessoDocumentoManager().recuperaPorProcessoDocumento(acordao) != null 
				   && ( ComponentUtil.getComponent(TramitacaoProcessualImpl.class).recuperaVariavel(Variaveis.MINUTA_EM_ELABORACAO) == null 
				   		|| !ComponentUtil.getComponent(TramitacaoProcessualImpl.class).recuperaVariavel(Variaveis.MINUTA_EM_ELABORACAO).equals(acordao.getIdProcessoDocumento()))
				   ) {
	    				    		
	    			ComponentUtil.getComponent(TramitacaoProcessualImpl.class).gravaVariavel(Variaveis.MINUTA_EM_ELABORACAO, acordao.getIdProcessoDocumento());
				}
	    	}
		}
		
		if(acordao == null) {
			acordao = recuperarProcessoDocumentoAcordaoPorTaskInstance(acordaoCompilacao.getTaskInstance());
		}
        
		if(acordao == null) {
			acordao = ComponentUtil.getComponent(DocumentoJudicialService.class).getDocumento();
			acordao.setTipoProcessoDocumento(ProcessoDocumentoHome.instance().getTipoDocumentoAcordao());
			acordao.getProcessoDocumentoBin().setModeloDocumento(" ");
			acordao.setProcesso(acordaoCompilacao.getSessaoPautaProcessoTrf().getProcessoTrf().getProcesso());
			acordao.setProcessoTrf(acordaoCompilacao.getSessaoPautaProcessoTrf().getProcessoTrf());
			acordao.setProcessoDocumento("Acórdão");
			Long taskId = BusinessProcess.instance().getTaskId();
			if(taskId == null) {
				taskId = TaskInstanceHome.instance().getTaskId();
			}
			
			acordao.setIdJbpmTask(taskId);
			acordao.setExclusivoAtividadeEspecifica(Boolean.TRUE);

			atualizarConteudoAcordao(acordao, acordaoCompilacao);
			if(acordao.getDataJuntada() == null) {
				ComponentUtil.getComponent(TramitacaoProcessualImpl.class).gravaVariavel(Variaveis.MINUTA_EM_ELABORACAO, acordao.getIdProcessoDocumento());
			}
			if(acordaoCompilacao.getSessaoPautaProcessoTrf() != null) {
				ComponentUtil.getProcessoDocumentoManager().persist(acordao);
				SessaoProcessoDocumento acordaoSessao = new SessaoProcessoDocumento();
				acordaoSessao.setLiberacao(true);
				acordaoSessao.setOrgaoJulgador(acordaoCompilacao.getSessaoPautaProcessoTrf().getOrgaoJulgadorVencedor());
				acordaoSessao.setSessao(acordaoCompilacao.getSessaoPautaProcessoTrf().getSessao());
				acordaoSessao.setProcessoDocumento(acordao);
				ComponentUtil.getSessaoProcessoDocumentoManager().persist(acordaoSessao);
			}
		}

		List<ProcessoDocumento> listDocumentosAssinatura = acordaoCompilacao.getProcessoDocumentosParaAssinatura();
		if (acordaoCompilacao.getSessaoPautaProcessoTrf() != null) {
			listDocumentosAssinatura.addAll(recuperaVotosComDocumentosPreenchidos(acordaoCompilacao));
		}
		ComponentUtil.getComponent(ProcessoDocumentoManager.class).vincularDocumentos(acordao, listDocumentosAssinatura);
		return acordao;
	}

	private List<ProcessoDocumento> recuperaVotosComDocumentosPreenchidos(AcordaoCompilacao acordaoCompilacao) {
		List<ProcessoDocumento> result = new ArrayList<ProcessoDocumento>();

		List<ProcessoTrf> listProcessos = new ArrayList<ProcessoTrf>(1);
		listProcessos.add(acordaoCompilacao.getSessaoPautaProcessoTrf().getProcessoTrf());
		List<SessaoProcessoDocumentoVoto> votos = 
				ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class).recuperarVotosPorSessaoEhProcessos(acordaoCompilacao.getSessaoPautaProcessoTrf().getSessao(), listProcessos);
		for (SessaoProcessoDocumentoVoto spdVoto : votos) {
			if (Util.isDocumentoPreenchido(spdVoto.getProcessoDocumento())) {
				result.add(spdVoto.getProcessoDocumento());
			}
		}
		
		return result;
	}

	public ProcessoDocumento recuperarProcessoDocumentoAcordaoPorTaskInstance(TaskInstance taskInstance) {
		
		TipoProcessoDocumento tipoAcordao = ParametroUtil.instance().getTipoProcessoDocumentoAcordao();

		Integer minutaAcordaoId = (Integer) ComponentUtil.getComponent(TramitacaoProcessualImpl.class).recuperaVariavel(Variaveis.MINUTA_EM_ELABORACAO);
		
		if(minutaAcordaoId == null){
			minutaAcordaoId = (Integer) ComponentUtil.getComponent(TramitacaoProcessualImpl.class).recuperaVariavel(Variaveis.VARIAVEL_FLUXO_COLEGIADO_MINUTA_ACORDAO);
		}
		
		if(minutaAcordaoId != null){
			try {
				ProcessoDocumento processoDocumento = ComponentUtil.getComponent(DocumentoJudicialService.class).getDocumento(minutaAcordaoId);
				//Verificação criada para atender a http://www.cnj.jus.br/jira/browse/PJEII-21020
				//Caso exista acórdão assinado o sistema deve criar um novo
				if (processoDocumento !=null 
						&& ((processoDocumento.getProcessoDocumentoBin() !=null && processoDocumento.getProcessoDocumentoBin().isAssinado()) || !processoDocumento.getTipoProcessoDocumento().equals(tipoAcordao))){
					return null;
				}
				return processoDocumento;
			} 
			catch (PJeBusinessException e) {
				logger.error("Não foi possível recuperar o acórdão: {0}", e.getLocalizedMessage());
			}
		}

		return null;
	}
	
	/**
	 * Verifica se existe a variavel de fluxo "pje:fluxo:elaborarAcordao:acordao:permiteAlterar", 
	 * que no caso de estar definida no fluxo com valor "true", permite 
	 * a escolha de modelos de acórdão bem como a sua edição.
	 *  
	 * @return true caso seja permitido editar e alterar o modelo do acórdão, e false caso contrario.
	 */
	public Boolean getPermiteAlterarAcordao() {
		if (permiteAlterarAcordao == null) {
			Boolean existeVariavelFluxoDefinida = (Boolean) ComponentUtil.getComponent(TramitacaoProcessualImpl.class).recuperaVariavelTarefa(
					Variaveis.VARIAVEL_PERMITE_ALTERAR_ACORDAO);
			
			permiteAlterarAcordao = existeVariavelFluxoDefinida != null ? existeVariavelFluxoDefinida : false;
		}
		return permiteAlterarAcordao;
	}
	
	/**
	 * Atualiza o conteúdo do acórdão pegando o modelo padrão de documento de acórdão 
	 * definido no parâmetro Parametros.ID_MODELO_DOCUMENTO_ACORDAO. Caso a variável de fluxo 
	 * Variaveis.VARIAVEL_PERMITE_ALTERAR_ACORDAO esteja definida como true, 
	 * o método nada faz, pois o modelo de acórdão neste caso pode ser alterado.
	 * 
	 * @param acordao 
	 * @param acordaoCompilacao
	 */
	public void atualizarConteudoAcordao(ProcessoDocumento acordao, AcordaoCompilacao acordaoCompilacao) {
		try {
				ComponentUtil.getComponent(AcordaoModelo.class).setAcordaoCompilacao(acordaoCompilacao);
				ModeloDocumentoLocal modeloAcordao = getModeloDocumentoAcordao();
				acordao.getProcessoDocumentoBin().setModeloDocumento(ComponentUtil.getComponent(ModeloDocumentoManager.class).obtemConteudo(modeloAcordao));
				acordao.setTipoProcessoDocumento(modeloAcordao.getTipoProcessoDocumento());
				ComponentUtil.getComponent(DocumentoJudicialService.class).persist(acordao, true);
				ComponentUtil.getComponent(DocumentoJudicialService.class).flush();
		} catch (PJeBusinessException e){
			String msgErro = FacesUtil.getMessage("erro.acordao.atualizarConteudoAcordao", e.getMessage());
			logger.error(msgErro);
			throw new IllegalStateException(msgErro);
		}
	}

	/**
	 * Recupera o modelo do acordao, baseado no parâmetro do sistema e se encontra um modeloDocumento
	 * 
	 * @return ModeloDocumento
	 * @throws PJeBusinessException
	 */
	public ModeloDocumentoLocal getModeloDocumentoAcordao() throws PJeBusinessException {
		String idModelo = ComponentUtil.getComponent(ParametroService.class).valueOf(Parametros.ID_MODELO_DOCUMENTO_ACORDAO);	        
		if (idModelo == null || idModelo.isEmpty()) {
			throw new PJeBusinessException(
					"A aplicação não tem modelo adequado de acórdão configurado. Parâmetro " + Parametros.ID_MODELO_DOCUMENTO_ACORDAO);
		}
		ModeloDocumentoLocal modeloAcordao = ComponentUtil.getComponent(ModeloDocumentoLocalManager.class).findById(new Integer(idModelo));
		if (modeloAcordao == null) {
			throw new PJeBusinessException(
					"Modelo de acórdão não encontrado. idModelo: " + Parametros.ID_MODELO_DOCUMENTO_ACORDAO);
		}
		return modeloAcordao;
	}

	/**
	 * Método responsável por compilar o acórdão e realizar as validações de obrigatoriedade 
	 * dos documentos que compõem o acórdão
	 * 
	 * @param acordaoCompilacao
	 * @throws Exception
	 */
	public void compilarAcordao(AcordaoCompilacao acordaoCompilacao) throws Exception {
		
			Calendar cal = new GregorianCalendar();
			List<ProcessoDocumento> processoDocumentos = acordaoCompilacao.getProcessoDocumentosParaAssinatura();
			ProcessoDocumento pdAcordao = acordaoCompilacao.getAcordao();
					
			if(acordaoCompilacao.getRelatorio() != null 
					&& Util.isDocumentoPreenchido(acordaoCompilacao.getRelatorio().getProcessoDocumento())){
	
				if (!processoDocumentos.contains(acordaoCompilacao.getRelatorio().getProcessoDocumento())) {
		   			processoDocumentos.add(acordaoCompilacao.getRelatorio().getProcessoDocumento());
		   		}
	
			}
	  		
			if(acordaoCompilacao.getVotoRelator() != null 
					&& Util.isDocumentoPreenchido(acordaoCompilacao.getVotoRelator().getProcessoDocumento())){
				
				pdAcordao.setDataJuntada(cal.getTime());
		   		cal.add(Calendar.MILLISECOND, 10);
		   		ProcessoDocumentoManager processoDocumentoManager = ComponentUtil.getComponent(ProcessoDocumentoManager.class);
		   		for(ProcessoDocumento pd: processoDocumentos){
		   			if( pd != null
			   			&& !(pd.getIdProcessoDocumento() == pdAcordao.getIdProcessoDocumento())){
		   	    		pd.setDocumentoPrincipal(pdAcordao);
		   	    		pd.setDataJuntada(cal.getTime());
		   	    		pd.setDocumentoSigiloso(false);
		   	    		pd.setNomeUsuarioJuntada(pdAcordao.getNomeUsuarioJuntada());
		   	    		processoDocumentoManager.mergeAndFlush(pd);
		   			}
		   		}
			}
			
			SessaoProcessoDocumento spdAcordao = getSessaoProcessoDocumentoManager()
	    			.getSessaoProcessoDocumentoByTipo(acordaoCompilacao.getSessaoPautaProcessoTrf().getSessao(), 
	    					ProcessoDocumentoHome.instance().getTipoDocumentoAcordao(), 
	    					acordaoCompilacao.getSessaoPautaProcessoTrf().getProcessoTrf().getProcesso());	      

			if(spdAcordao == null){
		   		spdAcordao = new SessaoProcessoDocumento();
		   		spdAcordao.setProcessoDocumento(pdAcordao);
		   		spdAcordao.setLiberacao(true);
		   		spdAcordao.setSessao(acordaoCompilacao.getSessaoPautaProcessoTrf().getSessao());
		   		spdAcordao.setOrgaoJulgador(acordaoCompilacao.getSessaoPautaProcessoTrf().getOrgaoJulgadorVencedor());
		   		getSessaoProcessoDocumentoManager().persistAndFlush(spdAcordao);
			}
			
	   		TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(acordaoCompilacao.getTaskInstance().getId());
	   		taskInstance.setActorId(Authenticator.getPessoaLogada().getLogin(), true);
	   		
	   		ProcessInstance processInstance = this.getProcessInstance(acordaoCompilacao);
	   		processInstance.getContextInstance().setVariable(Variaveis.ATO_PROFERIDO, pdAcordao.getIdProcessoDocumento());
	   		processInstance.getContextInstance().setVariable(Variaveis.ULTIMO_DOCUMENTO_JUNTADO_NESTE_FLUXO, pdAcordao.getIdProcessoDocumento());
	   		processInstance.getContextInstance().deleteVariable(Variaveis.MINUTA_EM_ELABORACAO);
	   		this.lancarMovimentosTemporarios(processInstance, pdAcordao);
	   		
	   		ProcessoHome.instance().setIdProcessoDocumento(null);
	   		
	   		Transition transicaoPadrao = ComponentUtil.getComponent(TramitacaoProcessualImpl.class).recuperarTransicaoPadrao(taskInstance);
	   		
	   		if (transicaoPadrao == null) {
	   			throw new Exception(FacesUtil.getMessage("FluxoTransisaoFluxo.erroRecuperacaoTransisaoDoFluxo"));
	   		}
	   		taskInstance.end(transicaoPadrao);
	}
	
    private void lancarMovimentosTemporarios(ProcessInstance processInstance, ProcessoDocumento documentoVinculado) {
		LancadorMovimentosService.instance().lancarMovimentosTemporariosAssociandoAoDocumento(processInstance, documentoVinculado);

		EventsHomologarMovimentosTreeHandler.instance().clearList();
		EventsHomologarMovimentosTreeHandler.instance().clearTree();
		
		EventsTreeHandler.instance().clearList();
		EventsTreeHandler.instance().clearTree();    	
    }
    
    private ProcessInstance getProcessInstance(AcordaoCompilacao acordaoCompilacao) {
   		TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(acordaoCompilacao.getTaskInstance().getId());
   		taskInstance.setActorId(Authenticator.getPessoaLogada().getLogin(), true);
   		
   		return taskInstance.getProcessInstance();
    }
	
	/**
	 * Verifica os documentos do acórdão baseados nas variáveis de tarefas configuradas.
	 * 
	 * @param acordaoCompilacao
	 * @return uma lista de string com os erros encontrados
	 */
	public ArrayList<String> verificaVariaveisDocumentoAcordao(AcordaoCompilacao acordaoCompilacao) {
		
		ArrayList<String> msgErros = new ArrayList<String>();
		TramitacaoProcessualService tramitacaoProcessualService = ComponentUtil.getComponent(TramitacaoProcessualImpl.class);
		Boolean variavelRelatorio = (Boolean)tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.ACORDAO_RELATORIO_NAO_OBRIGATORIO);
		Boolean variavelVotoVencedor = (Boolean)tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.ACORDAO_VOTO_VENCEDOR_NAO_OBRIGATORIO);
		Boolean variavelVotoRelatorProcesso = (Boolean)tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.ACORDAO_VOTO_RELATOR_PROCESSO_NAO_OBRIGATORIO);
		Boolean variavelEmenta = (Boolean)tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.ACORDAO_EMENTA_NAO_OBRIGATORIO);
		Boolean movimentoAcordaoObrigatorio = false;
		Boolean movimentoAcordaoPendente = false;
		
		TipoProcessoDocumento tipoAcordao = ParametroUtil.instance().getTipoProcessoDocumentoAcordao();
		if(tipoAcordao != null && tipoAcordao.getAgrupamento() != null) {
			movimentoAcordaoObrigatorio = true;
			movimentoAcordaoPendente = true;
			
			Integer idAgrupamentoMovimentosTemporarios = LancadorMovimentosService.instance().getAgrupamentoDeMovimentosTemporarios(this.getProcessInstance(acordaoCompilacao));
			Integer idAgrupamentoTipoDocumentoAcordao = new Integer(tipoAcordao.getAgrupamento().getIdAgrupamento());
			List<ProcessoEvento> movimentosTemporarios = LancadorMovimentosService.instance().getProcessoEventoListTemporario(this.getProcessInstance(acordaoCompilacao));
			if(idAgrupamentoMovimentosTemporarios != null 
						&& idAgrupamentoMovimentosTemporarios.equals(idAgrupamentoTipoDocumentoAcordao) 
						&& CollectionUtilsPje.isNotEmpty(movimentosTemporarios)) {
				movimentoAcordaoPendente = false;
			}
		}
		
		if(movimentoAcordaoObrigatorio && movimentoAcordaoPendente) {
			msgErros.add(FacesUtil.getMessage("conclusaoAcordao.acordaoSemMovimentos"));
		}

		
		if(variavelRelatorio == null || !Boolean.TRUE.equals(variavelRelatorio)){
			if (acordaoCompilacao.getRelatorio() == null 
					|| !Util.isDocumentoPreenchido(acordaoCompilacao.getRelatorio().getProcessoDocumento())) {
				msgErros.add(FacesUtil.getMessage("conclusaoAcordao.relatorioNaoEncontrato"));
	   		}
		}
		
		if(variavelEmenta == null || !Boolean.TRUE.equals(variavelEmenta)){
   			if (acordaoCompilacao.getEmentaRelatorDoAcordao() == null
   					|| !Util.isDocumentoPreenchido(acordaoCompilacao.getEmentaRelatorDoAcordao().getProcessoDocumento())) {
   				msgErros.add(FacesUtil.getMessage("conclusaoAcordao.ementaNaoEncontrado"));
   			} 
		}
		
		if((variavelVotoVencedor == null || !Boolean.TRUE.equals(variavelVotoVencedor))) {
			if (acordaoCompilacao.getSessaoPautaProcessoTrf() == null){
				msgErros.add(FacesUtil.getMessage("conclusaoAcordao.votoVencedorNaoEncontrado"));
				msgErros.add(FacesUtil.getMessage("conclusaoAcordao.votoVencedorRelatorNaoEncontrado"));

			} else {
				/* caso 1 - vencedor <> relatorProcesso - verifica o voto do relator do acÃ³rdÃ£o */
				if(acordaoCompilacao.isRelatorParaAcordaoDiferenteRelatorOriginario()){
					if (acordaoCompilacao.getVotoVencedor() == null || !Util.isDocumentoPreenchido(acordaoCompilacao.getVotoVencedor().getProcessoDocumento())) {						
			   			msgErros.add(FacesUtil.getMessage("conclusaoAcordao.votoVencedorNaoEncontrado"));
			   		} 			
				}else{
				/* caso 2 - vencedor = relatorProcesso  - verifica o voto do relator do processo */
			   		if (acordaoCompilacao.getVotoRelatorDoProcesso() == null 
			   				|| !Util.isDocumentoPreenchido(acordaoCompilacao.getVotoRelatorDoProcesso().getProcessoDocumento())) {
			   			msgErros.add(FacesUtil.getMessage("conclusaoAcordao.votoVencedorRelatorNaoEncontrado"));
			   		}
				}	
			}
		}

		if(variavelVotoRelatorProcesso == null || !Boolean.TRUE.equals(variavelVotoRelatorProcesso)){
			if (acordaoCompilacao.getSessaoPautaProcessoTrf() == null || acordaoCompilacao.getVotoRelatorDoProcesso() == null 
	   				|| !Util.isDocumentoPreenchido(acordaoCompilacao.getVotoRelatorDoProcesso().getProcessoDocumento())) {
	   			if(!msgErros.contains(FacesUtil.getMessage("conclusaoAcordao.votoVencedorRelatorNaoEncontrado"))){
	   				msgErros.add(FacesUtil.getMessage("conclusaoAcordao.votoRelatorProcessoNaoEncontrado"));	   				
	   			}
	   		} 
		}		

		return msgErros;
	}

	public void compilarAcordaoEmLote(AcordaoCompilacao acordaoCompilacao, HistoricoMovimentacaoLote historico, List<ArquivoAssinadoHash> arquivosAssinados, Set<ProcessoDocumento> documentosParaAssinatura) throws Exception {
		ComponentUtil.getComponent(DocumentoJudicialService.class).gravarAssinaturaDeProcessoDocumento(arquivosAssinados, documentosParaAssinatura);
		
		compilarAcordao(acordaoCompilacao);

		ComponentUtil.getComponent(AtividadesLoteService.class).salvaHistoricoProcessoMovimentacao(acordaoCompilacao.getTaskInstance().getId(), acordaoCompilacao.getSessaoPautaProcessoTrf().getProcessoTrf(), historico);		
	}
	
	/**
	 * Retorna uma lista dos processos publicados de uma sessao
	 * @param processo O processo(SessaoPautaProcessoTrf) que sera verificado as datas
	 * em que foi publicado
	 * @return Lista dos processos publicados
	 */
	public ProcessoParteExpediente recuperarExpedientePublicadoDje(SessaoPautaProcessoTrf sessaoPautaProcesso) {
		return this.getDAO().recuperarExpedientePublicadoDje(sessaoPautaProcesso);
	}
	
	/**
	 * Recupera a data da publicação do processo no DJE.
	 * @param ppe
	 * @return Data da publicação
	 * @throws PontoExtensaoException
	 * @throws PJeBusinessException 
	 */
	public Date recuperarDataPublicacaoDje(ProcessoParteExpediente ppe) throws PontoExtensaoException, PJeBusinessException  {
		Date dataPublicacaoDje = null;
		PublicacaoDiarioEletronicoManager publicacaoDiarioEletronicoManager = ComponentUtil.getComponent(PublicacaoDiarioEletronicoManager.class);
		if (ppe!=null) {
			PublicacaoDiarioEletronico publicacaoDJE = publicacaoDiarioEletronicoManager.getPublicacao(ppe);
			if(publicacaoDJE != null) {
				if(publicacaoDJE.getDtPublicacao() != null) {
					dataPublicacaoDje = publicacaoDJE.getDtPublicacao();
				}
				if(dataPublicacaoDje == null && StringUtils.isNotEmpty(publicacaoDJE.getReciboPublicacaoDiarioEletronico())) {
					Calendar dataRetornoPublicacaoDJE = ComponentUtil.getComponent(AtoComunicacaoService.class).recuperarDataPublicacao(publicacaoDJE.getReciboPublicacaoDiarioEletronico());
					if(dataRetornoPublicacaoDJE != null) {
						dataPublicacaoDje = dataRetornoPublicacaoDJE.getTime();
						publicacaoDJE.setDtPublicacao(dataPublicacaoDje);
						
						publicacaoDiarioEletronicoManager.merge(publicacaoDJE);
						publicacaoDiarioEletronicoManager.flush();

					}
				}
			}
		}
		return dataPublicacaoDje;
	}
	
	/**
	 * Obtem o nome do órgão julgador do processo que está em pauta na sessão.
	 * @param sessaoPautaProcessoTrf - objeto que contém os processos em pauta na sessao. 
	 * @param orgaoJulgador - órgão julgador do qual será obtido o nome.
	 * @return String - nome do órgão julgador do processo em pauta na sessão.
	 */
	public String obterNomeOrgaoJulgador(SessaoPautaProcessoTrf sessaoPautaProcessoTrf, OrgaoJulgador orgaoJulgador) {
		StringBuilder gabineteVencedor = new StringBuilder();
		if(orgaoJulgador != null && StringUtils.isNotBlank(orgaoJulgador.getOrgaoJulgador())) {
			gabineteVencedor.append(orgaoJulgador.getOrgaoJulgador());
			if(isSessaoPautaProcessoValida(sessaoPautaProcessoTrf)
					&& sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador().equals(orgaoJulgador)) {
				gabineteVencedor.append(" (Relator) ");
			}
		}
		return gabineteVencedor.toString();
	}
	
	/**
	 * Verifica se a sessão, o processo e o órgão julgador não estão nulos.
	 * @param sessaoPautaProcessoTrf - Objeto SessaoPautaProcessoTrf que contém os atributos a serem validados
	 * @return true se os atributos não estiverem nulos
	 */
	public boolean isSessaoPautaProcessoValida(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		return sessaoPautaProcessoTrf != null 
				&& sessaoPautaProcessoTrf.getSessao() != null 
				&& sessaoPautaProcessoTrf.getProcessoTrf() != null 
				&& sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador() != null;
	}

	/**
	 *
	 * Retorna uma lista de sessões de julgamento não julgadas de um processo
	 * filtrando por Situações de julgamento e adiamento
	 * 
	 * @param tipoAdiamento
	 *            tipo do adiamento do julgamento
	 * @return Lista de sessões de julgamento.
	 */
	public List<SessaoPautaProcessoTrf> getSessoesNaoJulgadasPautadas(ProcessoTrf processoTrf,
			AdiadoVistaEnum tipoAdiamento ) {
		return this.getDAO().getSessoesNaoJulgadasPautadas(tipoAdiamento,
				processoTrf);
	}

	/**
	 * Retorna uma lista de sessões de julgamento de um processo filtrando por
	 * Situações de julgamento
	 * 
	 * @param situacao
	 *            Situações do julgamento
	 * @return Lista de sessões de julgamento.
	 */
	public List<SessaoPautaProcessoTrf> getSessoesJulgamentoPautados(ProcessoTrf processoTrf,
			TipoSituacaoPautaEnum situacao ) {
		return this.getDAO().getSessoesJulgamentoPautados(situacao,
				processoTrf);
	}
	
	/**
	 * Retorna uma lista de sessões de julgamento de um processo.
	 * 
	 * @param processoTrf
	 *            Processo
	 * @return Lista de sessões de julgamento.
	 */
	public List<SessaoPautaProcessoTrf> getSessoesJulgamentoPautados(ProcessoTrf processoTrf) {
		return this.getDAO().getSessoesJulgamentoPautados(null, processoTrf);
	}
	
	/**
	 * Metodo responsável por recuperara ordem dos processos na sessao
	 * 
	 * @param sessao Sessao.
	 * @return List<Integer>
	 */
	public List<Integer> recuperarOrdemProcessoSessaoSelectItem(Sessao sessao) {
		return this.getDAO().buscaOrdemProcessoSessao(sessao);
	}
	
	/**
	 * Verifica se é possível a exibição do conteúdo dos votos antecipados para
	 * os usuário que não são magistrados.
	 * 
	 * Quando o parâmetro de sistema
	 * pje:sessao:ocultarVotosAntecipadosNaoMagistrado estiver setado o sistema
	 * irá liberar apenas se o usuário for magistrado ou então se o documento de
	 * acórdão já estiver juntado aos autos
	 */
	public boolean podeExibirConteudoVotosAntecipados(Sessao sessao, ProcessoTrf processo) {
		if (ParametroUtil.instance().isOcultarVotosAntecipadosNaoMagistrado()
				&& !Authenticator.isPapelAtualMagistrado()
				&& !getSessaoProcessoDocumentoManager().existeAcordaoJuntado(sessao, processo)) {
			return false;
		} 
		return true;
	}	

	public SessaoPautaProcessoTrf alterar(SessaoPautaProcessoTrf sppt) throws Exception{
		sppt=merge(sppt);
		flush();
		return sppt;
	}
	
	public SessaoPautaProcessoTrf retirarDePauta(SessaoPautaProcessoTrf sppt, OrgaoJulgador oj) throws Exception{
		sppt.setAdiadoVista(AdiadoVistaEnum.AD);
		sppt.setRetiradaJulgamento(Boolean.TRUE);
		sppt.setSituacaoJulgamento(TipoSituacaoPautaEnum.NJ);
		sppt.setOrgaoJulgadorRetiradaJulgamento(oj);
		return alterar(sppt);
	}
	
	public Boolean verificarBloqueioRegistroJulgamentoSemVoto(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {

		boolean isProcessoBloqueado = false;
		if (Boolean.TRUE.toString()
				.equals(ParametroUtil.getParametro(Parametros.BLOQUEIO_REGISTO_JULGAMENTO_VOTACAO_IMCOMPLETA))) {

			if (!isTodosMagistradosVotantesComVotoRegistrado(sessaoPautaProcessoTrf)) {
				isProcessoBloqueado = true;

			}
		}

		return isProcessoBloqueado;

	}
	
	
	
	public String verificarBloqueioRegistroJulgamentoSemVoto(List<SessaoPautaProcessoTrf> processosPautados) {

		StringBuilder sb = new StringBuilder();

		for (SessaoPautaProcessoTrf sessaoPautaProcessoTrf : processosPautados) {
			if (verificarBloqueioRegistroJulgamentoSemVoto(sessaoPautaProcessoTrf)) {

				sb.append(sessaoPautaProcessoTrf.getProcessoTrf().getNumeroProcesso());
				sb.append("\n");

			}
		}
		if (sb.length() > 0) {
			sb.insert(0, "No foram registrados todos os votos dos magistrados da composio para o(s) processo(s) : \n");

		}
		return sb.toString();
	}

	/**
	 * @param processoTrf
	 * @param limitaAdiadosOuVista
	 * @return retorna a última pauta de um processo ADIADO (solicitação de adiamento para a próxima sessão) ou com pedido de vista
	 */
	public SessaoPautaProcessoTrf recuperaUltimaPautaProcesso(ProcessoTrf processoTrf, boolean limitaAdiadosOuVista){
		Search s = new Search(SessaoPautaProcessoTrf.class);
		addCriteria(s,Criteria.equals("processoTrf", processoTrf));
		if(limitaAdiadosOuVista){
			addCriteria(s,Criteria.or
							(Criteria.and(Criteria.equals("adiadoVista", AdiadoVistaEnum.AD), Criteria.not(Criteria.isNull("processoTrf.pautaVirtual"))),
							 Criteria.equals("adiadoVista", AdiadoVistaEnum.PV)));
		}
		s.addOrder("o.idSessaoPautaProcessoTrf", Order.DESC);
    	s.setMax(1);
    	List<SessaoPautaProcessoTrf> ret = list(s);
        return ret.isEmpty() ? null : ret.get(0);
	}
	
	public SessaoPautaProcessoTrf recuperaUltimaPautaProcesso(ProcessoTrf processoTrf){
		return recuperaUltimaPautaProcesso(processoTrf, false);
	}
	
	public void retirarParaReexame(SessaoPautaProcessoTrf julgamento) throws PJeBusinessException{	
		julgamento.getProcessoTrf().setPautaVirtual(null);
		julgamento.getProcessoTrf().setSelecionadoPauta(false);
		julgamento.setRetiradaJulgamento(Boolean.TRUE);
		julgamento.setAdiadoVista(AdiadoVistaEnum.AD);
		julgamento.setSituacaoJulgamento(TipoSituacaoPautaEnum.NJ);
		julgamento.setOrgaoJulgadorRetiradaJulgamento(Authenticator.getOrgaoJulgadorAtual());
		persistAndFlush(julgamento);
	}
	
	/**
	 * Método responsável por recuperar a ementa ativa de um processo em uma
	 * determinada sessão
	 */
	public SessaoProcessoDocumento recuperarEmentaAtivaPorSessaoEhProcessoEhOrgaoJulgador(Sessao sessao,
			ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador) {
		return ComponentUtil.getComponent(SessaoProcessoDocumentoDAO.class).recuperarEmentaAtivaPorSessaoEhProcessoEhOrgaoJulgador(sessao, processoTrf, orgaoJulgador);
	}

	public List<SessaoPautaProcessoTrf> recuperarSessaoPautaProcessosTrf(Integer idSessao, SessaoJulgamentoFiltroDTO sessaoJulgamentoFiltroDTO) throws Exception {
		return recuperarSessaoPautaProcessosTrf(idSessao, sessaoJulgamentoFiltroDTO, false, false, false);
	}
	
	public List<SessaoPautaProcessoTrf> recuperarSessaoPautaProcessosTrf(Integer idSessao, SessaoJulgamentoFiltroDTO sessaoJulgamentoFiltroDTO, boolean excluirprocessosbloco, boolean especificarJulgados, boolean julgados) throws Exception {
		return getDAO().recuperarSessaoPautaProcessosTrf(idSessao, sessaoJulgamentoFiltroDTO, excluirprocessosbloco, especificarJulgados, julgados);
	}
	
	/**
	 * método responsável por recuperar todos os SessaoPautaProcessoTrf da sessao passada em parâmetro, sem a aplicacao de filtros de pesquisa.
	 * @param idSessao
	 * @return List<SessaoPautaProcessoTrf>
	 * @throws Exception
	 */
	public List<SessaoPautaProcessoTrf> recuperarTodosSessaoPautaProcessosTrf(Integer idSessao) throws Exception {
		return this.recuperarSessaoPautaProcessosTrf(idSessao, new SessaoJulgamentoFiltroDTO(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null));
	}
	
	/**
	 * método responsável por recuperar todos os SessaoPautaProcessoTrf da sessao passada em parâmetro que ja foram julgados na sessao.
	 * 
	 * @param sessaoIdSessao
	 * @return
	 * @throws Exception 
	 */
	public List<SessaoPautaProcessoTrf> recuperarTodosSessaoPautaProcessosTrfJulgados(Integer idSessao) throws Exception {
		return this.recuperarSessaoPautaProcessosTrf(idSessao, new SessaoJulgamentoFiltroDTO(SituacaoProcessoSessaoEnum.JG));
	}


	public TipoSituacaoPautaEnum buscarSituacaoJulgamento(ProcessoTrf processoTrf, Sessao sessao) {
		return this.getDAO().recuperarTipoSituacaoEnum(processoTrf.getIdProcessoTrf(), sessao.getIdSessao());
	}

	/**
	 * Retorna a situação do julgamento para cada processo de uma dada sessão.
	 * @param sessao A sessão que se deseja saber a situação do processo.
	 * @return Um mapa cuja chave é o id do processo e o valor é a situação do 
	 * julgamento.
	 */
	public Map<Integer,TipoSituacaoPautaEnum> buscarSituacaoJulgamento(Sessao sessao) {
		return this.getDAO().recuperarTipoSituacaoEnum(sessao.getIdSessao());
	}

	/**
	 * Retorna a situação de adiamento de cada processo de uma dada sessão.
	 * @param sessao A sessão que se deseja saber a situação de adiamento do processo.
	 * @return Um mapa cuja chave é o id do processo e o valor é a situação do 
	 * adiamento.
	 */
	public Map<Integer,AdiadoVistaEnum> buscarAdiadoVista(Sessao sessao) {
		return this.getDAO().buscarAdiadoVista(sessao.getIdSessao());
	}

	public AdiadoVistaEnum buscarAdiadoVista(ProcessoTrf processoTrf, Sessao sessao) {
		return this.getDAO().buscarAdiadoVista(processoTrf.getIdProcessoTrf(), sessao.getIdSessao());
	}

	/**
	 * Retorna a situação de retirada de julgamento de cada processo de uma dada sessão.
	 * @param sessao A sessão que se deseja saber a situação de adiamento do processo.
	 * @return Um mapa cuja chave é o id do processo e o valor indicando se o processo foi retirado de julgamento ou não.
	 */
	public Map<Integer,Boolean> buscarRetiradaJulgamento(Sessao sessao) {
		return this.getDAO().buscarRetiradaJulgamento(sessao.getIdSessao());
	}

	public boolean buscarRetiradaJulgamento(ProcessoTrf processoTrf, Sessao sessao) {
		return this.getDAO().buscarRetiradaJulgamento(processoTrf.getIdProcessoTrf(), sessao.getIdSessao());
	}

	public OrgaoJulgador buscarOrgaoJulgadorRetiradaJulgamento(ProcessoTrf processoTrf, Sessao sessao) {
		return this.getDAO().buscarOrgaoJulgadorRetiradaJulgamento(processoTrf.getIdProcessoTrf(), sessao.getIdSessao());
	}
	
	public Map<Integer,OrgaoJulgador> buscarOrgaoJulgadorRetiradaJulgamento(Sessao sessao) {
		return this.getDAO().buscarOrgaoJulgadorRetiradaJulgamento(sessao.getIdSessao());
	}
	
	public OrgaoJulgador buscarOrgaoJulgadorVencedorJulgamento(ProcessoTrf processoTrf, Sessao sessao) {
		return this.getDAO().buscarOrgaoJulgadorVencedorJulgamento(processoTrf.getIdProcessoTrf(), sessao.getIdSessao());
	}

	public Map<Integer,OrgaoJulgador> buscarOrgaoJulgadorVencedorJulgamento(Sessao sessao) {
		return this.getDAO().buscarOrgaoJulgadorVencedorJulgamento(sessao.getIdSessao());
	}

	public Boolean buscarPreferencia(ProcessoTrf processoTrf, Sessao sessao) {
		return this.getDAO().buscarPreferencia(processoTrf.getIdProcessoTrf(), sessao.getIdSessao());
	}

	public Map<Integer,Boolean> buscarPreferencia(Sessao sessao) {
		return this.getDAO().buscarPreferencia(sessao.getIdSessao());
	}

	public Boolean buscarMaioriaDetectada(ProcessoTrf processoTrf, Sessao sessao) {
		return this.getDAO().buscarMaioriaDetectada(processoTrf.getIdProcessoTrf(), sessao.getIdSessao());
	}

	public Map<Integer,Boolean> buscarMaioriaDetectada(Sessao sessao) {
		return this.getDAO().buscarMaioriaDetectada(sessao.getIdSessao());
	}

	public Boolean buscarSustentacaoOral(ProcessoTrf processoTrf, Sessao sessao) {
		return this.getDAO().buscarSustentacaoOral(processoTrf.getIdProcessoTrf(), sessao.getIdSessao());
	}

	public Map<Integer,Boolean> buscarSustentacaoOral(Sessao sessao) {
		return this.getDAO().buscarSustentacaoOral(sessao.getIdSessao());
	}

	public String buscarAdvogadoSustentacaoOral(ProcessoTrf processoTrf, Sessao sessao) {
		return this.getDAO().buscarAdvogadoSustentacaoOral(processoTrf.getIdProcessoTrf(), sessao.getIdSessao());
	}

	public Map<Integer,String> buscarAdvogadoSustentacaoOral(Sessao sessao) {
		return this.getDAO().buscarAdvogadoSustentacaoOral(sessao.getIdSessao());
	}

	public String buscarProclamacaoDecisaoOral(ProcessoTrf processoTrf, Sessao sessao) {
		return this.getDAO().buscarProclamacaoDecisaoOral(processoTrf.getIdProcessoTrf(), sessao.getIdSessao());
	}

	public Map<Integer,String> buscarProclamacaoDecisaoOral(Sessao sessao) {
		return this.getDAO().buscarProclamacaoDecisaoOral(sessao.getIdSessao());
	}

	public void salvarProclamacaoJulgamento(SessaoPautaProcessoTrf sessaoPautaProcessoTrf, String proclamacaoDecisao, JulgamentoEnum julgamento) throws PJeBusinessException {
		sessaoPautaProcessoTrf.setProclamacaoDecisao(proclamacaoDecisao);
		sessaoPautaProcessoTrf.setJulgamentoEnum(julgamento);
		flush();
	}
	
	public void salvarProclamacaoJulgamento(SessaoPautaProcessoTrf sessaoPautaProcessoTrf, String proclamacaoDecisao) throws PJeBusinessException {
		salvarProclamacaoJulgamento(sessaoPautaProcessoTrf,proclamacaoDecisao,null);
	}
	
	/**
	 * método responsável por recuperar todos os sessaoPautaProcessoTrf onde a pessoa passada em parâmetro  a pessoa inclusora
	 * @param pessoaInclusora
	 * @return
	 * @throws Exception 
	 */
	public List<SessaoPautaProcessoTrf> recuperarSessaoPautaProcessoPessoaInclusora(Pessoa pessoaInclusora) throws Exception {
		return this.getDAO().recuperarSessaoPautaProcesso(pessoaInclusora, true);
	}

	/**
	 * método responsável por recuperar todos os sessaoPautaProcessoTrf onde a pessoa passada em parâmetro  a pessoa exclusora.
	 * @param pessoaSecundaria
	 * @return
	 * @throws Exception 
	 */
	public List<SessaoPautaProcessoTrf> recuperarSessaoPautaProcessoPessoaExclusora(Pessoa pessoaExclusora) throws Exception {
		return this.getDAO().recuperarSessaoPautaProcesso(pessoaExclusora, false);
	}

	public SessaoPautaProcessoTrf recuperaSessaoPautaProcessoTrf(Integer sessPautProcId) {
		return this.getDAO().find(sessPautProcId);
	}

	/**
	 * Método responsável por atualizar o Presidente da sessão de julgamento para os processos pautados nela
	 * @param sessao Dados da Sessão de Julgamento
	 * @param apenasPendentesJulgamento Informa se o sistema deve atualizar o Presidente apenas para os processos pendentes de julgamento
	 * @throws Exception 
	 */
	public void atualizarPresidenteSessaoJulgamento(Sessao sessao, boolean apenasPendentesJulgamento) throws Exception {
		List<SessaoPautaProcessoTrf> sessaoPautaProcessoTrfList = recuperarTodosSessaoPautaProcessosTrf(sessao.getIdSessao());
		
		for (SessaoPautaProcessoTrf sessaoPautaProcessoTrf : sessaoPautaProcessoTrfList) {
			if (apenasPendentesJulgamento &&  !sessaoPautaProcessoTrf.isProcessoPendenteJulgamento()) {
				continue;
			}
			
			sessaoPautaProcessoTrf.setPresidente(sessao.getPresidenteSessao());
			persist(sessaoPautaProcessoTrf);
		}
		
		flush();
	}	

	/** 
	 * Filtra uma lista dos processos pautados em sessao de acordo com uma lista de objetos Criteria. 
	 * @param criterias
	 * @return  List<SessaoPautaProcessoTrf>
	 */
	public List<SessaoPautaProcessoTrf> filtrar(List<Criteria> criterias){
		Search s = new Search(SessaoPautaProcessoTrf.class);
        List<SessaoPautaProcessoTrf> retorno = new ArrayList<SessaoPautaProcessoTrf>();
		if(criterias != null && criterias.size() > 0){
			for (Criteria criteria : criterias) {
				addCriteria(s,criteria);
			}
			s.setDistinct(Boolean.TRUE);
			s.addOrder("o.numeroOrdem", Order.ASC);
			retorno = list(s);
		}
		return retorno.isEmpty() ? null : retorno;
	}

	public List<SessaoPautaProcessoTrf> recuperar(BlocoJulgamento bloco) {
		return getDAO().recuperar(bloco);
	}
	
	public void atualizarVencedor(SessaoPautaProcessoTrf sessaoPautaProcessoTrf, OrgaoJulgador orgaoJulgador) throws Exception{
		sessaoPautaProcessoTrf.setOrgaoJulgadorVencedor(orgaoJulgador);
		String textoProclamacaoJulgamento = getSessaoProcessoDocumentoVotoManager().recuperarTextoProclamacaoJulgamentoAntecipada(sessaoPautaProcessoTrf);
		sessaoPautaProcessoTrf.setProclamacaoDecisao(textoProclamacaoJulgamento);
		ComponentUtil.getSessaoPautaProcessoTrfManager().alterar(sessaoPautaProcessoTrf);
	}
	
	public SessaoPautaProcessoTrf alterarSituacaoJulgamento(String acao, SessaoPautaProcessoTrf sppt) throws Exception {
		SessaoPautaProcessoTrf retorno = sppt;
		if (acao.equals("aguardando")) {
			retorno = alterarSituacaoParaAguardandoJulgamento(sppt);
			ComponentUtil.getProcessoJudicialService().sinalizarFluxo(sppt.getProcessoTrf(), Variaveis.PJE_FLUXO_COLEGIADO_TORNAR_PENDENTE_JULGAMENTO, Boolean.TRUE, false, false);
		}else if (acao.equals("preferencia")) {				
			retorno = marcarPreferencia(sppt);
		}
		else if (acao.equals("pautarapida")) {
			retorno = marcarPautaRapida(sppt);				
		} 
		else if (acao.equals("adiado")) {
			retorno = adiar(sppt);
		} 
		else if (acao.equals("retirado")) {
			ComponentUtil.getProcessoJudicialManager().aptidaoParaJulgamento(sppt.getProcessoTrf().getIdProcessoTrf(), false, null);
			retorno = retirarDePauta(sppt);				
		} 
		else if(acao.equals("emjulgamento")){
			retorno = alterarSituacaoParaEmJulgamento(sppt);
			ComponentUtil.getProcessoJudicialService().sinalizarFluxo(sppt.getProcessoTrf(), Variaveis.PJE_FLUXO_COLEGIADO_COLOCAR_EM_JULGAMENTO, Boolean.TRUE, false, false);
		} else if (acao.equals("julgado")) {
			retorno = registrarJulgamento(sppt);
		}
		return retorno;
	}

	public SessaoPautaProcessoTrf recuperaUltimaPautaProcessoNaoExcluido(ProcessoTrf processoTrf){
		return getDAO().recuperaUltimaPautaProcessoNaoExcluido(processoTrf);
	}

	public SessaoProcessoDocumentoManager getSessaoProcessoDocumentoManager() {
		return ComponentUtil.getSessaoProcessoDocumentoManager();
	}

	public SessaoProcessoDocumentoVotoManager getSessaoProcessoDocumentoVotoManager() {
		return ComponentUtil.getSessaoProcessoDocumentoVotoManager();
	}
	
	public List<ProcessoTrf> recuperarRemovidos(Sessao sessao) {
		return ComponentUtil.getSessaoPautaProcessoTrfDAO().recuperarRemovidos(sessao);
	}
	
	public List<ProcessoTrf> recuperarProcessos(List<SessaoPautaProcessoTrf> processosPautados) {
		List<ProcessoTrf> retorno = new ArrayList<ProcessoTrf>();
		for (SessaoPautaProcessoTrf processoPautado : processosPautados){
			retorno.add(processoPautado.getProcessoTrf());
		}
		return retorno;
	}

	public boolean podeFinalizarJulgamento(SessaoPautaProcessoTrf sessaoPauta) {
		boolean retorno = false;
		if(!sessaoPauta.isJulgamentoFinalizado() && sessaoPauta.getSessao().getIniciar() && TipoSituacaoPautaEnum.verificarSituacaoJulgado(sessaoPauta.getSituacaoJulgamento()) && 
			(TipoSituacaoPautaEnum.NJ.equals(sessaoPauta.getSituacaoJulgamento()) || StringUtil.isNotEmpty(sessaoPauta.getProclamacaoDecisao())) ) {
			retorno = true;
		}
		return retorno;
	}
	
	private void tratarProcessoNaoJulgado(SessaoPautaProcessoTrf sessaoPauta) throws Exception {
		if(AdiadoVistaEnum.AD.equals(sessaoPauta.getAdiadoVista()) && Boolean.TRUE.equals(sessaoPauta.getRetiradaJulgamento())) {
			ComponentUtil.getSessaoPautaProcessoTrfManager().retirarDePauta(sessaoPauta);
		} else {
			if (AdiadoVistaEnum.PV.equals(sessaoPauta.getAdiadoVista())) {
				tratarProcessoComPedidoDeVista(sessaoPauta);
			}
		}
	}
	
	private void tratarProcessoJulgado(SessaoPautaProcessoTrf sessaoPauta) { 
		if (sessaoPauta.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.JG)) {
			Events.instance().raiseEvent(Eventos.EVENTO_ENCERRA_SESSAO, sessaoPauta.getProcessoTrf());
			List<Integer> idsTipos = new ArrayList<Integer>();
			idsTipos.add(sessaoPauta.getProcessoTrf().getIdProcessoTrf());
		}
	}
	
	public void encerrarJulgamento(SessaoPautaProcessoTrf sessaoPauta, boolean eventoAssincrono, boolean julgamentoIndividual) throws Exception {
		if (!ComponentUtil.getComponent(SessaoManager.class).isEventosDeliberacaoSessaoConfigurados()) {
			throw new PJeBusinessException("Os movimentos da sessão não estão devidamente configurados. "
					+ "Por favor, contacte o suporte do tribunal.");
		}
		
		if(!verificarPendenteProclamacaoJulgamento(sessaoPauta)) {
			if(julgamentoIndividual) {
				sessaoPauta.setJulgamentoFinalizado(true);
				alterar(sessaoPauta);
			}
			if (sessaoPauta.getSituacaoJulgamento() == TipoSituacaoPautaEnum.NJ) {
				tratarProcessoNaoJulgado(sessaoPauta);
			} else {
				tratarProcessoJulgado(sessaoPauta);
			}
			registrarMovimento(sessaoPauta, new Date(), Authenticator.getUsuarioLogado(), eventoAssincrono);
			ComponentUtil.getProcessoTrfManager().gravarSugestaoSessao(sessaoPauta.getProcessoTrf(), null);
		} else {
			throw new PJeBusinessException("O processo está pendente de proclamação de julgamento.");
		}
	}
	
	public boolean verificarPendenteProclamacaoJulgamento(SessaoPautaProcessoTrf sppt) {
		boolean retorno = false;
		if (null != sppt && TipoSituacaoPautaEnum.JG.equals(sppt.getSituacaoJulgamento()) && Strings.isEmpty(sppt.getProclamacaoDecisao())) {
			retorno = true;
		}
		return retorno;
	}
	
	public void tratarProcessoComPedidoDeVista(SessaoPautaProcessoTrf sppt) throws PJeBusinessException {
		if (ComponentUtil.getSessaoManager().getFluxoPedidoVista() != null) {
			if( sppt.getOrgaoJulgadorPedidoVista() != null ) {
				
				List <Integer> idsLocalizacoes = new ArrayList<Integer>();
				idsLocalizacoes.add(sppt.getOrgaoJulgadorPedidoVista().getLocalizacao().getIdLocalizacao());
				boolean existeFluxoVistaAtivo = ComponentUtil.getComponent(FluxoManager.class).existeProcessoNoFluxoEmExecucao(sppt.getProcessoTrf().getIdProcessoTrf(), idsLocalizacoes, ComponentUtil.getSessaoManager().getFluxoPedidoVista().getFluxo());
				if (!existeFluxoVistaAtivo && isProcessoPedidoVista(sppt)) {
					try {
						inicializaFluxoPedidoVista(sppt, ComponentUtil.getSessaoManager().getFluxoPedidoVista().getCodFluxo());
					} catch (PJeBusinessException e) {
						throw new PJeBusinessException("Houve erro na inicializao do fluxo de pedido de vista.");
					}
				} else {
					throw new PJeBusinessException("O fluxo de pedido de vista configurado no parmetro no  vlido.");
				}
			}
		} else {
			throw new PJeBusinessException("No h fluxo de pedido de vista configurado no parmetro.");
		}
	}
	
	/**
	* Verifica se houve pedido de vista no processo informado.
	* 
	* @param sppt
	*            SessaoPautaProcessoTrf
	* @return true se tiver marcado pedido de vista e o OrgaoJulgador
	*         solicitante estiver selecionado
	*/
	private boolean isProcessoPedidoVista(SessaoPautaProcessoTrf sppt) {
		return sppt.getAdiadoVista() != null && sppt.getAdiadoVista().equals(AdiadoVistaEnum.PV) && sppt.getOrgaoJulgadorPedidoVista() != null;
	}
	
	/**
	* Inicializa o fluxo de pedido de vista para o rgo julgador que registrou
	* o pedido
	* 
	* @param julgamento
	*            representao do processo na sesso
	* @param codFluxo
	*            cdigo do fluxo BPM
	* @throws PJeBusinessException
	*/
	private void inicializaFluxoPedidoVista(SessaoPautaProcessoTrf julgamento, String codFluxo)
		throws PJeBusinessException {
		ProcessoTrf processoTrf = julgamento.getProcessoTrf();
		OrgaoJulgador orgaoJulgador = julgamento.getOrgaoJulgadorPedidoVista();
		OrgaoJulgadorCargo orgaoJulgadorCargo = ComponentUtil.getOrgaoJulgadorCargoManager()
			.getOrgaoJulgadorCargoEmExercicio(orgaoJulgador);
		OrgaoJulgadorColegiado orgaoJulgadorColegiado = processoTrf.getOrgaoJulgadorColegiado();
		ComponentUtil.getProcessoJudicialService().incluirNovoFluxo(processoTrf, codFluxo, orgaoJulgador.getIdOrgaoJulgador(), orgaoJulgadorCargo.getIdOrgaoJulgadorCargo(), 
			orgaoJulgadorColegiado.getIdOrgaoJulgadorColegiado(), true);
	}
	
	/**
	 * Grava o movimento referente ao julgamento.
	 * 
	 * @param sessaoPautaProcesso
	 *            Processo pautado na sesso
	 * @param dataRegistroMovimento
	 *            Data de registro do movimento.
	 * @param usuario
	 *            Usurio que estregistrando o movimento.
	 * @return id do movimento gerado.
	 * @throws PJeBusinessException
	 */
	public void registrarMovimento(SessaoPautaProcessoTrf sessaoPautaProcesso, Date dataRegistroMovimento, Usuario usuario, boolean eventoAssincrono) throws PJeBusinessException {

		String codigoMovimento = null;

		if (sessaoPautaProcesso.getSituacaoJulgamento() == TipoSituacaoPautaEnum.JG) {
			sessaoPautaProcesso.setOrgaoJulgadorRelator(sessaoPautaProcesso.getProcessoTrf().getOrgaoJulgador());
			ComponentUtil.getProcessoJudicialManager().removerAptidaoParaJulgamento(sessaoPautaProcesso.getProcessoTrf().getIdProcessoTrf());
			switch (sessaoPautaProcesso.getJulgamentoEnum()) {
			case M:
				codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_DELIBERACAO_MERITO;
				break;
				
			case P:
				codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_DELIBERACAO_LIMINAR;
				break;
				
			case O:
				codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_DELIBERACAO_QUESTAO_ORDEM;
				break;
				
			default:
				codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_DELIBERACAO_MERITO;
				break;
			}	
		}

		if (sessaoPautaProcesso.getAdiadoVista() == AdiadoVistaEnum.AD) {
			codigoMovimento = ComponentUtil.getParametroService().valueOf(CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_ADIADO);
		}

		if (sessaoPautaProcesso.getAdiadoVista() == AdiadoVistaEnum.PV) {
			codigoMovimento = ComponentUtil.getParametroService().valueOf(CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_PEDIDO_VISTA);
		}

		if (sessaoPautaProcesso.getRetiradaJulgamento()) {
			codigoMovimento = ComponentUtil.getParametroService().valueOf(CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_RETIRADO_PAUTA);
			if (codigoMovimento == null) {
				codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_RETIRADO_PAUTA;
			}
		}

		if (codigoMovimento != null) {
			MovimentoBuilder movimentoBuilder = MovimentoAutomaticoService.preencherMovimento().deCodigo(codigoMovimento)
					.associarAoProcesso(sessaoPautaProcesso.getProcessoTrf().getProcesso())					
					.associarADataAtualizacao(dataRegistroMovimento);
			if (usuario != null) {
				movimentoBuilder.associarAoUsuario(usuario);
			}
			movimentoBuilder.lancarMovimento();
		}
		antecipaDocumentosProcessosAdiados(sessaoPautaProcesso);
		if(eventoAssincrono) {
			Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_PROCESSO_JULGADO_COLEGIADO, sessaoPautaProcesso.getIdSessaoPautaProcessoTrf());
		} else {
			Events.instance().raiseEvent(Eventos.EVENTO_PROCESSO_JULGADO_COLEGIADO, sessaoPautaProcesso.getIdSessaoPautaProcessoTrf());
		}
	}

	private void antecipaDocumentosProcessosAdiados(SessaoPautaProcessoTrf sessaoPautaProcesso)
			throws PJeBusinessException {

		if (sessaoPautaProcesso.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.NJ)) {
			List<SessaoProcessoDocumento> listaDocumentosSessao = ComponentUtil.getSessaoProcessoDocumentoManager().listaDocumentosAptosAntecipacao(sessaoPautaProcesso);
			for (SessaoProcessoDocumento spd : listaDocumentosSessao) {
				if(spd.getProcessoDocumento() != null && !spd.getProcessoDocumento().getTipoProcessoDocumento().equals(ParametroUtil.instance().getTipoProcessoDocumentoNotasOrais()) && 
						!spd.getProcessoDocumento().getTipoProcessoDocumento().equals(ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento())) {
					spd.setSessao(null);
					ComponentUtil.getSessaoProcessoDocumentoManager().merge(spd);
				}
			}
		}
	}
	
	public boolean verificarRegistroMovimento(SessaoPautaProcessoTrf sessaoPauta) throws PJeBusinessException {
		boolean retorno = false;
		List<String> eventosDeliberacao = ComponentUtil.getSessaoManager().listarEventosDeliberacaoSessao();
		for(String evento: eventosDeliberacao) {
			Evento movimento = ComponentUtil.getComponent(EventoManager.class).findByCodigoCNJ(evento);
			if(ComponentUtil.getComponent(ProcessoEventoManager.class).temMovimento(sessaoPauta.getProcessoTrf(), movimento, sessaoPauta.getSessao().getDataAberturaSessao())) {
				retorno = true;
				break;
			}
		}
		return retorno;
	}
	public void atualizarProclamacao(Sessao sessao, ProcessoTrf processoTrf, String proclamacao) {
		if (sessao != null) {
			SessaoPautaProcessoTrf sessaoPautaProcessoTrf = getSessaoPautaProcessoTrf(processoTrf, sessao);
			if (sessaoPautaProcessoTrf != null) {
				sessaoPautaProcessoTrf.setProclamacaoDecisao(StringUtil.removeHtmlTags(proclamacao));
				mergeAndFlush(sessaoPautaProcessoTrf);
			}
		}
	}
	

	public boolean isTodosMagistradosVotantesComVotoRegistrado(SessaoPautaProcessoTrf sessaoPauta) {
		List<SessaoPautaProcessoComposicao> composicoesVotantes = sessaoPauta.getComposicoesVotantes();
		List<Integer> listaOJ = new ArrayList<Integer>(composicoesVotantes.size());
		for (SessaoPautaProcessoComposicao element : composicoesVotantes) {
			listaOJ.add(element.getOrgaoJulgador().getIdOrgaoJulgador());
		}
		if (listaOJ.size() > ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class).getVotosCount(sessaoPauta.getSessao(), sessaoPauta.getProcessoTrf(), listaOJ)) {
			return false;					
		}		
		return true;		
	}
	public SessaoPautaProcessoTrf getUltimaSessaoPautaProcessoTrf(ProcessoTrf processoTrf) {
		Search s = new Search(SessaoPautaProcessoTrf.class);
		addCriteria(s,Criteria.equals("processoTrf", processoTrf),Criteria.isNull("dataExclusaoProcessoTrf"));
		s.addOrder("o.dataInclusaoProcessoTrf", Order.DESC);
		s.setMax(1);
		List<SessaoPautaProcessoTrf> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);

	}
}