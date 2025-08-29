package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.component.tree.AssuntoTrfTreeHandler;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.IcrFuga;
import br.jus.pje.nucleo.entidades.IcrMedidaCautelarDiversa;
import br.jus.pje.nucleo.entidades.IcrMedidaProtetivaUrgencia;
import br.jus.pje.nucleo.entidades.IcrPrisao;
import br.jus.pje.nucleo.entidades.IcrSentencaCondenatoria;
import br.jus.pje.nucleo.entidades.IcrSentencaExtincaoPunibilidade;
import br.jus.pje.nucleo.entidades.IcrSoltura;
import br.jus.pje.nucleo.entidades.IcrSuspensao;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.MedidaCautelarDiversa;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.TipoMedidaCautelarDiversaEnum;

@Name("informacaoCriminalRelevanteHome")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.DEPLOYMENT)
@BypassInterceptors
public class InformacaoCriminalRelevanteHome extends
		AbstractHome<InformacaoCriminalRelevante>{

	private static final long serialVersionUID = 1L;
	private static final String NAME = "informacaoCriminalRelevanteHome";
	@Logger
	private Log log;
	public static final String TAB_PROCESSO_ID = "search";
	public static final String TAB_PESQUISA_ID = "tabConsultaICR";
	public static final String TAB_FORMULARIO_ID = "form";
	public static final String TAB_TIPIFICACAO_DELITO_ID = "tipificacaoIcr";
	private static final String TAB_MEDIDAS_CAUTELARES_DIVERSAS = "medidasCautelaresDiversasTab";
	private static final String TAB_ACOMPANHAR_MEDIDAS_CAUTELARES = "acompanharMedidasCautelaresTab";
	private static final String TAB_MEDIDAS_PROTETIVAS_URGENCIA = "medidasProtetivasUrgenciaTab";
	private ProcessoTrf processoTrf;
	private List<ProcessoParte> reusSelecionados = new ArrayList<ProcessoParte>(
			0);
	private Date dataInicioPesquisaMovimentacao;
	private Date dataFimPesquisaMovimentacao;
	private Date dataInicioPesquisaIcr;
	private Date dataFimPesquisaIcr;
	private Evento movimentacaoSelecionadaPesquisaMovimentacao;
	private List<TipoInformacaoCriminalRelevante> tipoInformacaoCriminalRelevanteList;
	private List<ProcessoEvento> processoEventoList;

	public InformacaoCriminalRelevanteHome(){
		newInstance();
		init();
	}

	public List<String> getPolos(){
		List<String> ret = new ArrayList<String>();
		for (ProcessoParte pp : getPartesTodosOsPolos()){
			if (pp.getInParticipacao().equals(ProcessoParteParticipacaoEnum.A) && !ret.contains("Polo Ativo"))
				ret.add("Polo Ativo");
			if (pp.getInParticipacao().equals(ProcessoParteParticipacaoEnum.P) && !ret.contains("Polo Passivo"))
				ret.add("Polo Passivo");
			if (pp.getInParticipacao().equals(ProcessoParteParticipacaoEnum.T) && !ret.contains("Terceiros"))
				ret.add("Terceiros");
		}
		return ret;
	}

	public List<ProcessoParteRepresentante> getChildren(ProcessoParte pp){
		return pp.getProcessoParteRepresentanteList();
	}

	public List<ProcessoParteRepresentante> getChildren(
			ProcessoParteRepresentante pp){
		return pp.getParteRepresentante().getProcessoParteRepresentanteList();
	}

	public List<ProcessoParte> getPartesTodosOsPolos(){
		return getProcessoTrf().getListaPartePoloObj(ProcessoParteParticipacaoEnum.values());
	}

	public List<ProcessoParte> getPartesPoloAtivo(){
		return getProcessoTrf().getListaPartePoloObj(ProcessoParteParticipacaoEnum.A);
	}

	public List<ProcessoParte> getPartesPoloPassivo(){
		return getProcessoTrf().getListaPartePoloObj(ProcessoParteParticipacaoEnum.P);
	}

	public List<ProcessoParte> getPartesPoloTerceiros(){
		return getProcessoTrf().getListaPartePoloObj(ProcessoParteParticipacaoEnum.T);
	}

	public static InformacaoCriminalRelevanteHome getHomeInstance(){
		return (InformacaoCriminalRelevanteHome) Component.getInstance(NAME);
	}

	private InformacaoCriminalRelevanteService getService(){
		return (InformacaoCriminalRelevanteService) Component
				.getInstance(InformacaoCriminalRelevanteService.NAME);
	}

	public void init(){
		tipoInformacaoCriminalRelevanteList = getService()
				.getTipoInformacaoCriminalRelevanteList();
	}

	public void clearProcesso(){
		setProcessoTrf(null);
		clear();
	}

	public void clear(){
		/***
		 * limpar entityManager necessário para limpar referencias de icr sujas e não salvas (o método "getEntityManager().refresh(getInstance())" não
		 * funciona da maneira esperada, ocorre erros de assertion do hibernate)
		 ***/
		getEntityManager().clear();
		// lista de processo
		refreshGrid("consultaProcessoViewGrid");
		// lista de icr
		refreshGrid("informacaoCriminalRelevanteGridRefactory");
		// substituir referencia de processoTrf para a nova entityManager
		if (getProcessoTrf() != null){
			setProcessoTrf(getEntityManager().find(ProcessoTrf.class,
					getProcessoTrf().getIdProcessoTrf()));
		}
		/*** fim limpeza entityManager ***/
		newInstance();
		endNestedConversation();
		reusSelecionados.clear();
		dataInicioPesquisaMovimentacao = null;
		dataFimPesquisaMovimentacao = null;
		movimentacaoSelecionadaPesquisaMovimentacao = null;
		tipoInformacaoCriminalRelevanteList = new ArrayList<TipoInformacaoCriminalRelevante>(
				0);
		processoEventoList = new ArrayList<ProcessoEvento>(0);
		if (getProcessoTrf() != null){
			pesquisarMovimentacao();
		}
	}

	/*
	 * Fronteiras: inicio: quando o usuario selecionar o tipo de icr, editar uma icr, fim: clique nas abas "Processo" e "Pesquisa", botão novo e
	 * clique no menu
	 */
	public void beginNestedConversation(){
		Conversation conversation = Conversation.instance();
		if (conversation.isNested()){
			endNestedConversation();
			conversation.root();
			Conversation.instance().beginNested();
		}
		else if (conversation.isLongRunning()){
			conversation.beginNested();
		}
	}

	public void endNestedConversation(){
		reusSelecionados.clear();
		Conversation conversation = Conversation.instance();
		if (conversation.isNested()){
			conversation.end();
		}
	}

	private void addMessage(Severity severity, String key, Throwable e){
		FacesMessages.instance().addFromResourceBundle(severity, key);
		if (e != null){
			log.error(e.getMessage(), e);
		}
	}

	public ProcessoTrf getProcessoTrf(){
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf){
		this.processoTrf = processoTrf;
		if (processoTrf != null){
			pesquisarMovimentacao();
		}
	}

	@Override
	public InformacaoCriminalRelevante getInstance(){
		return instance;
	}

	@Override
	public void setInstance(InformacaoCriminalRelevante instance){
		super.setInstance(instance);
		if (instance != null){
			pesquisarMovimentacao();
			init();
			if (instance.getId() != null){
				beginNestedConversation();
			}
		}
	}

	public List<ProcessoParte> getReusSelecionados(){
		return reusSelecionados;
	}

	public void setReusSelecionados(List<ProcessoParte> reus){
		this.reusSelecionados = reus;
	}

	public void setDataInicioPesquisaMovimentacao(
			Date dataInicioPesquisaMovimentacao){
		this.dataInicioPesquisaMovimentacao = dataInicioPesquisaMovimentacao;
	}

	public Date getDataInicioPesquisaMovimentacao(){
		return dataInicioPesquisaMovimentacao;
	}

	public void setDataFimPesquisaMovimentacao(Date dataFimPesquisaMovimentacao){
		this.dataFimPesquisaMovimentacao = dataFimPesquisaMovimentacao;
	}

	public Date getDataFimPesquisaMovimentacao(){
		return dataFimPesquisaMovimentacao;
	}

	public Date getDataInicioPesquisaIcr(){
		return dataInicioPesquisaIcr;
	}

	public void setDataInicioPesquisaIcr(Date dataInicioPesquisaIcr){
		this.dataInicioPesquisaIcr = dataInicioPesquisaIcr;
	}

	public Date getDataFimPesquisaIcr(){
		return dataFimPesquisaIcr;
	}

	public void setDataFimPesquisaIcr(Date dataFimPesquisaIcr){
		this.dataFimPesquisaIcr = dataFimPesquisaIcr;
	}

	public Evento getMovimentacaoSelecionadaPesquisaMovimentacao(){
		return movimentacaoSelecionadaPesquisaMovimentacao;
	}

	public void setMovimentacaoSelecionadaPesquisaMovimentacao(
			Evento movimentacaoSelecionadaPesquisaMovimentacao){
		this.movimentacaoSelecionadaPesquisaMovimentacao = movimentacaoSelecionadaPesquisaMovimentacao;
	}

	public List<TipoInformacaoCriminalRelevante> getTipoInformacaoCriminalRelevanteList(){
		return tipoInformacaoCriminalRelevanteList;
	}

	public void setTipoInformacaoCriminalRelevanteList(
			List<TipoInformacaoCriminalRelevante> tipoInformacaoCriminalRelevanteList){
		this.tipoInformacaoCriminalRelevanteList = tipoInformacaoCriminalRelevanteList;
	}

	public List<ProcessoEvento> getProcessoEventoList(){
		return processoEventoList;
	}

	public void setProcessoEventoList(List<ProcessoEvento> processoEventoList){
		this.processoEventoList = processoEventoList;
	}

	public void pesquisarMovimentacao(){
		try{
			setProcessoEventoList(getService().getMovimentacoes(
					getProcessoTrf(), getDataInicioPesquisaMovimentacao(),
					dataFimPesquisaMovimentacao,
					movimentacaoSelecionadaPesquisaMovimentacao));
			getProcessoEventoList().removeAll(
					getInstance().getProcessoEventoList());
		} catch (IcrValidationException e){
			addMessage(Severity.WARN, e.getMessage(), null);
		} catch (Exception e){
			addMessage(Severity.ERROR, "Erro ao pesquisar as movimentações: "
				+ e.getMessage(), e);
		}
	}

	public void limparCamposPesquisaMovimentacao(){
		dataInicioPesquisaMovimentacao = null;
		dataFimPesquisaMovimentacao = null;
		movimentacaoSelecionadaPesquisaMovimentacao = null;
	}

	public void limparCamposPesquisaProcesso(){
		AssuntoTrfTreeHandler assuntoTrfProcessoTree = (AssuntoTrfTreeHandler) Contexts
				.getConversationContext().get("assuntoTrfProcessoTree");
		ClasseJudicialTreeHandler classeJudicialTree = (ClasseJudicialTreeHandler) Contexts
				.getConversationContext().get("classeJudicialTree");
		assuntoTrfProcessoTree.setSelected(null);
		classeJudicialTree.setSelected(null);
	}

	public void desvincularMovimentacao(ProcessoEvento pe){
		getInstance().getProcessoEventoList().remove(pe);
		getProcessoEventoList().add(pe);
		if (getInstance().getProcessoEventoList().isEmpty()){
			clear();
		}
	}

	public void vincularMovimentacao(ProcessoEvento pe){
		getInstance().getProcessoEventoList().add(pe);
		getProcessoEventoList().remove(pe);
	}

	private InformacaoCriminalRelevante icrParaExcluir = null;
	private String mensagemDeConfirmacaoDeExclusao = "";

	public InformacaoCriminalRelevante getIcrParaExcluir(){
		return icrParaExcluir;
	}

	public void setIcrParaExcluir(InformacaoCriminalRelevante icrParaExcluir){
		this.icrParaExcluir = icrParaExcluir;
		if (icrParaExcluir != null){
			setMensagemDeConfirmacaoDeExclusao("Deseja excluir?");
			if (icrParaExcluir instanceof IcrFuga){
				setMensagemDeConfirmacaoDeExclusao("Ao excluir uma fuga, a prisão retornará ao estado 'em aberto'.\n Deseja realmente excluir a fuga?");
			}
			else if (icrParaExcluir instanceof IcrSoltura){
				setMensagemDeConfirmacaoDeExclusao("Ao excluir uma soltura, a prisão retornará ao estado 'em aberto'.\n Deseja realmente excluir a soltura?");
			}
			else if (icrParaExcluir instanceof IcrPrisao
				&& ((IcrPrisao) icrParaExcluir).getPrisaoEncerrada() != null){
				setMensagemDeConfirmacaoDeExclusao("Ao excluir uma coversão de prisão, a prisão anterior retornará ao estado 'em aberto'.\n Deseja realmente excluir?");
			}
		}
		else{
			setMensagemDeConfirmacaoDeExclusao("");
		}
	}

	@Override
	public String remove(InformacaoCriminalRelevante icr){
		this.icrParaExcluir = null;
		try{
			getService().inactive(icr);
			refreshGrid("informacaoCriminalRelevanteGridRefactory");
			addMessage(Severity.INFO, "InformacaoCriminalRelevante_deleted",
					null);
			return "updated";
		} catch (IcrValidationException e){
			addMessage(Severity.ERROR, e.getMessage(), null);
		}
		return null;
	}

	public boolean exibirComboTipoInformacaoCriminalRelevante(){
		return !isManaged() && !getInstance().getProcessoEventoList().isEmpty();
	}

	public boolean exibirPainelIcr(){
		return isManaged();
	}

	@SuppressWarnings("unchecked")
	public boolean exibirTabTipificacaoDelito(){
		if (isManaged()){
			return exigeTipificacaoDelito();
		}
		if (getConversationContext().get("icrList") != null
			&& !((List<InformacaoCriminalRelevante>) getConversationContext()
					.get("icrList")).isEmpty()){
			return exigeTipificacaoDelito();
		}
		return false;
	}

	public boolean exigeTipificacaoDelito(){
		if (getInstance() instanceof IcrSentencaExtincaoPunibilidade){
			IcrSentencaExtincaoPunibilidade extincaoPunibilidade = (IcrSentencaExtincaoPunibilidade) getInstance();
			if (extincaoPunibilidade.getTipoExtincao() != null){
				return extincaoPunibilidade.getTipoExtincao()
						.exigeTipificacaoDelito();
			}
			else{
				return false;
			}
		}
		if (getInstance().getTipo() != null){
			return getInstance().getTipo().exigeTipificacaoDelito();
		}
		else{
			return false;
		}
	}

	public boolean exibirTabCadastroPena(){
		if (exibirTabTipificacaoDelito()){
			return exigeCadastroPena();
		}
		return false;
	}

	private boolean exigeCadastroPena(){
		return getInstance() instanceof IcrSentencaCondenatoria;
	}

	public boolean exibirTabAcompanhamentoCondicaoSuspensao(){
		if (getInstance() == null)
			return false;
		boolean retorno = false;
		if (isManaged()){
			retorno = getInstance() instanceof IcrSuspensao
				&& ((IcrSuspensao) getInstance()).getTipoSuspensao() != null
				&& ((IcrSuspensao) getInstance()).getTipoSuspensao().getAcompanhamentoCondicao();
		}
		return retorno;
	}

	@SuppressWarnings("unchecked")
	public boolean exibirTabMedidasCautelaresDiversas(){
		if (isManaged()){
			return getInstance() instanceof IcrMedidaCautelarDiversa;
		}
		if (getConversationContext().get("icrList") != null
			&& !((List<InformacaoCriminalRelevante>) getConversationContext()
					.get("icrList")).isEmpty()){
			return getInstance() instanceof IcrMedidaCautelarDiversa;
		}
		return false;
	}
	
	public boolean exibirTabAcompanharMedidasCautelares(){
		if(getInstance() instanceof IcrMedidaCautelarDiversa && isManaged()){
			IcrMedidaCautelarDiversa icrMedidaCautelarDiversa = (IcrMedidaCautelarDiversa) getInstance();
			for (MedidaCautelarDiversa medida : icrMedidaCautelarDiversa.getMedidasCautelaresDiversas()){
				if (TipoMedidaCautelarDiversaEnum.CPP319I.equals(medida.getTipo())){
					return true;
				}
					
			}
		}
		
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean exibirTabMedidasProtetivasUrgencia(){
		if (isManaged()){
			return getInstance() instanceof IcrMedidaProtetivaUrgencia;
		}
		if (getConversationContext().get("icrList") != null
			&& !((List<InformacaoCriminalRelevante>) getConversationContext()
					.get("icrList")).isEmpty()){
			return getInstance() instanceof IcrMedidaProtetivaUrgencia;
		}
		return false;
	}
	
	

	public void showTabProcesso(){
		clear();
		setTab(TAB_PROCESSO_ID);
	}

	public void showTabPesquisa(){
		clear();
		refreshGrid("informacaoCriminalRelevanteGridRefactory");
		setTab(TAB_PESQUISA_ID);
	}

	public void showTabFormulario(){
		setTab(TAB_FORMULARIO_ID);
	}

	public void clearAndShowTabFormulario(){
		clear();
		setTab(TAB_FORMULARIO_ID);
	}

	public void showTabTipificacaoDelito(){
		setTab(TAB_TIPIFICACAO_DELITO_ID);
	}

	public void showTabMedidasCautelaresDiversas(){
		setTab(TAB_MEDIDAS_CAUTELARES_DIVERSAS);
	}
	
	public void showTabAcompanharMedidasCautelares(){
		setTab(TAB_ACOMPANHAR_MEDIDAS_CAUTELARES);
	}

	public void showTabMedidasProtetivasUrgencia(){
		setTab(TAB_MEDIDAS_PROTETIVAS_URGENCIA);
	}

	public String getMensagemDeConfirmacaoDeExclusao(){
		return mensagemDeConfirmacaoDeExclusao;
	}

	public void setMensagemDeConfirmacaoDeExclusao(
			String mensagemDeConfirmacaoDeExclusao){
		this.mensagemDeConfirmacaoDeExclusao = mensagemDeConfirmacaoDeExclusao;
	}
	
	public String getEventosSelecionados() {
		String retorno = "0";
		if (getInstance().getProcessoEventoList().size() > 0) {
			StringBuilder eventos = new StringBuilder("");
			for (ProcessoEvento processoEvento : getInstance().getProcessoEventoList()) {
				eventos.append(processoEvento.getEvento().getIdEvento());
				eventos.append(", ");
			}
			retorno = eventos.toString().trim(); // retira os espaços em branco do início e fim da String
			retorno = retorno.substring(0, retorno.length() - 1); // retira a última vírgula
		}
		return retorno;
	}
}
