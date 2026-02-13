package br.jus.csjt.pje.view.action;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.list.ResultadoSentencaParteList;
import br.jus.cnj.pje.visao.beans.ResultadoSentencaParteBean;
import br.jus.csjt.pje.business.service.ResultadoSentencaService;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ResultadoSentenca;
import br.jus.pje.nucleo.entidades.ResultadoSentencaParte;
import br.jus.pje.nucleo.entidades.SolucaoSentenca;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

/**
 * Componente Action usado para interface entre a View e o
 * LancadorMovimentosService.
 * 
 * @author David, Borges
 */
/**
 * @author admin-tst
 *
 */
@Name(ResultadoSentencaAction.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class ResultadoSentencaAction implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2311127744265261087L;

	public static final String NAME = "resultadoSentencaAction";

	private ResultadoSentenca resultadoSentenca;
	private ResultadoSentencaService resultadoSentencaService = ComponentUtil
			.getComponent(ResultadoSentencaService.NAME);

	private SolucaoSentenca solucaoSentencaSelecionada;
	private BigDecimal valorCondenacao = new BigDecimal("0");
	private BigDecimal custasDispensadas = new BigDecimal("0");
	private BigDecimal custasArrecadar = new BigDecimal("0");
	private Boolean assistenciaJudiciariaGratuita;
	private Boolean modalAberto = Boolean.FALSE;

	ResultadoSentencaParteList resultadoSentencaParteList = ComponentUtil.getComponent(ResultadoSentencaParteList.NAME);

	public ResultadoSentencaService getResultadoSentencaService() {
		return resultadoSentencaService;
	}

	public void setResultadoSentencaService(ResultadoSentencaService resultadoSentencaService) {
		this.resultadoSentencaService = resultadoSentencaService;
	}

	public Boolean getModalAberto() {
		return modalAberto;
	}

	public void setModalAberto(Boolean modalAberto) {
		this.modalAberto = modalAberto;
	}

	private List<ResultadoSentencaParteBean> resultadoSentencaParteBeans;

	/**
	 * Método responsável pela lógica de renderização do botão de
	 * "registrar resultado de sentença" no nó análise de acessoria para a
	 * justiça do trabalho no momento de criação de uma sentença.
	 * 
	 * @return Boolean : true se for para aparecer o botão e false para não
	 *         aparecer.
	 */
	public Boolean getRenderedRegistrarResultadoSentenca() {
		return resultadoSentencaService.getRenderedRegistrarResultadoSentenca();
	}

	/**
	 * Método responsável pela lógica de renderização do fragmento de
	 * "próxima ação" no nó análise de acessoria para a justiça do trabalho no
	 * momento de criação de uma sentença.
	 * 
	 * @return Boolean : true se for para aparecer o fragmento e false para não
	 *         aparecer.
	 */
	public Boolean getRenderedProximaAcao() {
		return resultadoSentencaService.getRenderedProximaAcao();
	}

	/**
	 * Método que verifica se todos os autores do processo têm resultado de
	 * sentença cadastrado.
	 * 
	 * @return Boolean : true se todos os autores têm sentenças
	 */
	public Boolean getQuantidadeSentencaAutorCorreta() {
		return resultadoSentencaService.getQuantidadeSentencaAutorCorreta();
	}

	/**
	 * Método que verifica se o processo possui uma sentença não homologada.
	 * 
	 * @return Boolean : true se tiver e false caso contrário.
	 */
	public Boolean getPossuiSentencaNaoHomologada() {
		return resultadoSentencaService.getPossuiSentencaNaoHomologada();
	}

	/**
	 * Método responsável por mostrar uma mensagem ao magistrado, caso não haja
	 * sentenças para todas as partes autoras.
	 * 
	 */
	public void mostrarMensagemQuantidadeSentencas() {
		if (!getQuantidadeSentencaAutorCorreta()) {
			FacesMessages.instance().add(Severity.INFO,
					"Não há soluções cadastradas para todas as partes autoras do processo.");
		}
	}

	public Boolean getRenderedSolucaoDiferenciada() {
		if (resultadoSentenca == null) {
			return Boolean.FALSE;
		}
		if (resultadoSentenca.getSolucaoUnica()) {
			return resultadoSentenca.getResultados().size() == 0;
		} else {
			if (getResultadoSentencaParteBeanList() == null) {
				return Boolean.TRUE;
			}
			return getResultadoSentencaParteBeanList().size() > 0;
		}
	}

	public void carregarResultadoSentenca() {
		ResultadoSentenca resultadoSentenca = resultadoSentencaService.getResultadoSentenca(ProcessoTrfHome.instance()
				.getInstance());

		if (resultadoSentenca == null) {
			ResultadoSentenca resultadoSentencaTemp = new ResultadoSentenca();
			resultadoSentencaTemp.setProcessoTrf(ProcessoTrfHome.instance().getInstance());
			setResultadoSentenca(resultadoSentencaTemp);
			resultadoSentencaTemp.setDataSentenca(new Date());
			resultadoSentencaTemp.setSolucaoUnica(Boolean.TRUE);
		} else {
			setResultadoSentenca(resultadoSentenca);
		}

		setModalAberto(Boolean.TRUE);
	}

	public void setSolucaoDiferenciada(Boolean solucaoDiferenciada) {
		this.resultadoSentenca.setSolucaoUnica(!solucaoDiferenciada);
	}

	public Boolean getSolucaoDiferenciada() {
		return (resultadoSentenca == null) ? false : !resultadoSentenca.getSolucaoUnica();
	}

	public String getObrigacaoesFazer() {
		ObrigacaoFazerHome obrigacaoFazerHome = ComponentUtil.getComponent(ObrigacaoFazerHome.NAME);
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		boolean possuiObrigacaoFazer = obrigacaoFazerHome.possuiObrigacaoFazer(processoTrf);

		return possuiObrigacaoFazer ? "Sim" : "Não";
	}

	public void gravarResultadoSentenca() {
		setarBigDecimalNuloComoZero();
		resultadoSentencaService.gravarResultadoSentenca(resultadoSentenca);
		FacesMessages.instance().add(Severity.INFO, "Resultado do Julgamento inserido com sucesso!");
	}

	
	/**
	 * PJEII-3000 - PJEII-3001
	 * Inclusão da chamada do método setMostrarSentencasRevisarMinuta para recuperação da lista de resultado de 
	 * sentença parte list na página de revisarMinuta.xhtml
	 */
	public void gravarRegistroSolucaoUnica() {
		if (!validarCustas()) {
			FacesMessages.instance().add(Severity.ERROR,
					"Por favor informar ao menos uma das custas (dispensadas ou a arrecadar).");
			return;
		}
		setarBigDecimalNuloComoZero();
		resultadoSentencaService.gravarResultadoSentencaParteUnico(resultadoSentenca, solucaoSentencaSelecionada,
				valorCondenacao, custasDispensadas, custasArrecadar, assistenciaJudiciariaGratuita);

		resultadoSentencaParteList.setMostraSentencas();
		resultadoSentencaParteList.setMostrarSentencasRevisarMinuta();
		limpaGrid();

		FacesMessages.instance().add(Severity.INFO, "Registro de Solução do Processo inserido com sucesso!");
	}

	private void limpaGrid() {

		solucaoSentencaSelecionada = null;
		assistenciaJudiciariaGratuita = false;
		valorCondenacao = new BigDecimal("0");
		custasDispensadas = new BigDecimal("0");
		custasArrecadar = new BigDecimal("0");

	}

	public void gravarRegistroSolucaoDiferenciada() {
		if (!validarCustas()) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR,
					"Por favor informar ao menos uma das custas (dispensadas ou a arrecadar).");
			return;
		}
		setarBigDecimalNuloComoZero();
		if (resultadoSentencaParteBeans == null) {
			resultadoSentencaParteBeans = getResultadoSentencaParteBeanList();
		}
		boolean isParteSelecionada = false;
		for (ResultadoSentencaParteBean resultadoSentencaParteBean : resultadoSentencaParteBeans) {

			if (resultadoSentencaParteBean.getSelected()) {
				ResultadoSentencaParte resultadoSentencaParte = resultadoSentencaParteBean.getResultadoSentencaParte();
				resultadoSentencaParte.setSolucaoSentenca(solucaoSentencaSelecionada);
				resultadoSentencaParte.setValorCondenacao(valorCondenacao);
				resultadoSentencaParte.setValorCustasArrecadar(custasArrecadar);
				resultadoSentencaParte.setValorCustasDispensadas(custasDispensadas);
				resultadoSentencaParte.setResultadoSentenca(resultadoSentenca);
				resultadoSentencaService.gravarResultadoSentencaParteDiferenciado(resultadoSentencaParte);

				resultadoSentenca.getResultados().add(resultadoSentencaParte);

			}
		}

		this.resultadoSentencaParteBeans = null;

		if (!isParteSelecionada) {  
			FacesMessages.instance().clear();  
			FacesMessages.instance().add(Severity.ERROR,  
					"Por favor selecione ao menos uma parte.");  
		  	return;  
		}
		
		resultadoSentencaParteList.setMostraSentencas();
		resultadoSentencaParteList.setMostrarSentencasRevisarMinuta();
		limpaGrid();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro de Solução Diferenciada inserido com sucesso!");
	}

	public List<SolucaoSentenca> getListSolucaoSentencaUnica() {
		return resultadoSentencaService.getListSolucaoSentencaUnica();
	}

	public List<SolucaoSentenca> getListSolucaoSentencaDiferenciada() {
		return resultadoSentencaService.getListSolucaoSentencaDiferenciada();
	}

	public void setSolucaoSentencaSelecionada(SolucaoSentenca solucaoSentencaSelecionada) {
		this.solucaoSentencaSelecionada = solucaoSentencaSelecionada;
	}

	public SolucaoSentenca getSolucaoSentencaSelecionada() {
		return solucaoSentencaSelecionada;
	}

	public void setValorCondenacao(BigDecimal valorCondenacao) {
		this.valorCondenacao = valorCondenacao;
	}

	public BigDecimal getValorCondenacao() {
		return valorCondenacao;
	}

	public void setCustasDispensadas(BigDecimal custasDispensadas) {
		this.custasDispensadas = custasDispensadas;
	}

	public BigDecimal getCustasDispensadas() {
		return custasDispensadas;
	}

	public void setCustasArrecadar(BigDecimal custasArrecadar) {
		this.custasArrecadar = custasArrecadar;
	}

	public BigDecimal getCustasArrecadar() {
		return custasArrecadar;
	}

	public void setAssistenciaJudiciariaGratuita(Boolean assistenciaJudiciariaGratuita) {
		this.assistenciaJudiciariaGratuita = assistenciaJudiciariaGratuita;
	}

	public Boolean getAssistenciaJudiciariaGratuita() {
		return assistenciaJudiciariaGratuita;
	}

	public ResultadoSentenca getResultadoSentenca() {
		return resultadoSentenca;
	}

	public void setResultadoSentenca(ResultadoSentenca resultadoSentenca) {
		this.resultadoSentenca = resultadoSentenca;
	}

	public List<ResultadoSentencaParteBean> getResultadoSentencaParteBeanList() {

		if (this.resultadoSentencaParteBeans != null) {
			return this.resultadoSentencaParteBeans;
		}

		if (resultadoSentenca == null) {
			return new ArrayList<ResultadoSentencaParteBean>();
		}
		List<ProcessoParte> listaPartes = resultadoSentenca.getProcessoTrf().getListaAutor();
		listaPartes.addAll(resultadoSentenca.getProcessoTrf().getListaReu());

		StringBuilder hql = new StringBuilder("select ");
		hql.append("			new br.jus.cnj.pje.visao.beans.ResultadoSentencaParteBean(pp) ");
		hql.append("	  from ProcessoParte pp ");
		hql.append("	  where pp in (:listaPartes) ");
		hql.append("		and pp not in (select rsp.processoParte from ResultadoSentencaParte rsp where rsp.resultadoSentenca = :resultadoSentenca)");

		Query q = EntityUtil.createQuery(hql.toString()).setParameter("listaPartes", Util.isEmpty(listaPartes)?null:listaPartes)
				.setParameter("resultadoSentenca", resultadoSentenca);

		@SuppressWarnings("unchecked")
		List<ResultadoSentencaParteBean> resultadoSentencaParteBeans = q
				.getResultList();

		for (ResultadoSentencaParteBean resultadoSentencaParteBean : resultadoSentencaParteBeans) {
			resultadoSentencaParteBean.setEhAutor(isProcessoParteAutor(resultadoSentencaParteBean
					.getResultadoSentencaParte()));

			if (!resultadoSentencaParteBean.getEhAutor()) {
				resultadoSentencaParteBean.getResultadoSentencaParte().setBeneficioOrdem(1);
			}
		}

		this.resultadoSentencaParteBeans = resultadoSentencaParteBeans;

		return resultadoSentencaParteBeans;

	}

	// senao será reu
	public Boolean isProcessoParteAutor(ResultadoSentencaParte resultadoSentencaParte) {
		ProcessoParte processoParte = resultadoSentencaParte.getProcessoParte();
		if (processoParte.getPartePrincipal()
				&& processoParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.A)) {
			return true;
		}
		return false;
	}

	public void excluirResultadoSentencaParteDiferenciado(ResultadoSentencaParte resultadoSentencaParte) {
		resultadoSentencaService.excluirResultadoSentencaParteDiferenciado(resultadoSentencaParte);
		this.resultadoSentencaParteBeans = null;
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro de Solução Diferenciada excluído com sucesso!");
	}

	public void excluirTodosResultadoSentencaParte() {
		resultadoSentencaService.excluirTodosResultadoSentencaParte(resultadoSentenca);
		this.resultadoSentencaParteBeans = null;
		resultadoSentencaParteList.setMostraSentencas();
		resultadoSentencaParteList.setMostrarSentencasRevisarMinuta();
		FacesMessages.instance().add(Severity.INFO, "Registro da Solução do Processo excluído com sucesso!");
	}

	public void setResultadoSentencaParteBeans(List<ResultadoSentencaParteBean> resultadoSentencaParteBeans) {
		this.resultadoSentencaParteBeans = resultadoSentencaParteBeans;
	}

	public List<ResultadoSentencaParteBean> getResultadoSentencaParteBeans() {
		return resultadoSentencaParteBeans;
	}

	private void setarBigDecimalNuloComoZero() {
		if (valorCondenacao == null) {
			valorCondenacao = new BigDecimal("0");
		}
		if (custasDispensadas == null) {
			custasDispensadas = new BigDecimal("0");
		}
		if (custasArrecadar == null) {
			custasArrecadar = new BigDecimal("0");
		}
	}

	private boolean validarCustas() {
		if ((custasArrecadar == null || custasArrecadar.compareTo(new BigDecimal("0")) == 0)
				&& (custasDispensadas == null || custasDispensadas.compareTo(new BigDecimal("0")) == 0)) {
			return false;
		}
		return true;
	}
}
