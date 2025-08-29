package br.jus.csjt.pje.view.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.component.UIComponent;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.visao.beans.DebitoTrabalhistaBean;
import br.jus.csjt.pje.business.service.DebitoTrabalhistaService;
import br.jus.pje.jt.entidades.DebitoTrabalhista;
import br.jus.pje.jt.entidades.DebitoTrabalhistaHistorico;
import br.jus.pje.jt.entidades.SituacaoDebitoTrabalhista;
import br.jus.pje.jt.entidades.TipoOperacaoEnum;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

/**
 * Componente Action usado para interface entre a View e o
 * LancadorMovimentosService.
 * 
 * @author David, Borges
 */
@Name(DebitoTrabalhistaAction.NAME)
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.APPLICATION)
@BypassInterceptors
public class DebitoTrabalhistaAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -994333487163144834L;
	public static final String NAME = "debitoTrabalhistaAction";
	private ConsultaDebitoTrabalhista instance = new ConsultaDebitoTrabalhista();

	// Lista das partes que ainda não possuem debitos trabalhistas (Já
	// encapsuladas em um ojbeto Debito Trabalhista)
	private List<DebitoTrabalhistaBean> listaDebitoTrabalhistaSemSituacao;

	// Lista das partes que já possuem débitos trabalhistas (Já encapsuladas em
	// um ojbeto Debito Trabalhista)
	private List<DebitoTrabalhista> listaDebitoTrabalhistaCadastrado;

	private List<SituacaoDebitoTrabalhista> listaSituacoes;
	private String situacaoSelecionada;

	private boolean ocorreuErro = false;
	
	public Boolean checkAllDebitoTrabalhista = Boolean.FALSE;

	@Create
	public void init() {
		listaDebitoTrabalhistaSemSituacao();
		listaDebitoTrabalhistaCadastrados();
		listaSituacoes();
	}
	
	/**
	 * Método que insere um débito trabalhista e insere o aperação realizada no
	 * histórico.gravarDebitoTrabalhista
	 * 
	 * @author Estevão Mognatto
	 */
	public void gravarDebitoTrabalhista() {

		ocorreuErro = false;

		if (!peloMenosUmSelecionado()) {

			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Por favor selecione pelo menos uma parte.");
			return;
		}

		DebitoTrabalhistaService debitoTrabalhistaService = ComponentUtil.getComponent(DebitoTrabalhistaService.NAME);
		SituacaoDebitoTrabalhista situacaoDebitoTrabalhista = debitoTrabalhistaService
				.obterSituacaoDebitoTrabalhistaPorDescricao(situacaoSelecionada);
		Pessoa pessoaLogada = (Pessoa) ProcessoHome.instance().getUsuarioLogado();

		String retornoEnvioBndt = "";

		List<DebitoTrabalhistaBean> listaDebitosSeremRemovidos = new ArrayList<DebitoTrabalhistaBean>();

		for (DebitoTrabalhistaBean debitoTrabalhistaBean : listaDebitoTrabalhistaSemSituacao) {

			if (debitoTrabalhistaBean.getSelected()) {

				// Obtem o Debito Trabalhista
				DebitoTrabalhista debitoTrabalhista = debitoTrabalhistaBean.getDebitoTrabalhista();
				debitoTrabalhista.setSituacaoDebitoTrabalhista(situacaoDebitoTrabalhista);

				// Obtem Debito Trabalhista Historico
				DebitoTrabalhistaHistorico debitoTrabalhistaHistorico = new DebitoTrabalhistaHistorico();
				ProcessoParte processoParte = debitoTrabalhista.getProcessoParte();

				debitoTrabalhistaHistorico.setDataAlteracao(new Date());
				debitoTrabalhistaHistorico.setOperacao(TipoOperacaoEnum.I);
				debitoTrabalhistaHistorico.setProcessoParte(processoParte);
				debitoTrabalhistaHistorico.setSituacaoDebitoTrabalhista(situacaoDebitoTrabalhista);
				debitoTrabalhistaHistorico.setUsuarioResponsavel(pessoaLogada);

				// Envia para BNDT
				retornoEnvioBndt = debitoTrabalhistaService
						.enviarXMLDebitoTrabalhistaOnLine(debitoTrabalhistaHistorico);

				if (retornoEnvioBndt.equals("")) {

					// Grava a situação Atual
					debitoTrabalhistaService.gravarDebitoTrabalhista(debitoTrabalhista);
					listaDebitosSeremRemovidos.add(debitoTrabalhistaBean);

					// Grava no Banco de Historico
					debitoTrabalhistaService.gravarDebitoTrabalhistaHistorico(debitoTrabalhistaHistorico);

					// Lança o moviemnto Correspondente
					debitoTrabalhistaService.lancarMovimentosDebitoTrabalhista(debitoTrabalhistaHistorico);
				} else {

					ocorreuErro = true;
					FacesMessages.instance().add(Severity.ERROR, retornoEnvioBndt);
				}

			}
		}

		// Remove partes já adicionadas do grid "Partes"
		listaDebitoTrabalhistaSemSituacao.removeAll(listaDebitosSeremRemovidos);

		// Limpa e recarega registros do grid "Registros"
		listaDebitoTrabalhistaCadastrado = null;
		situacaoSelecionada = null;
		listaDebitoTrabalhistaCadastrados();

		if (!ocorreuErro) {

			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Débito Trabalhista inserido com sucesso!");
		}
	}

	public boolean peloMenosUmSelecionado() {

		boolean retorno = false;

		if (listaDebitoTrabalhistaSemSituacao != null && listaDebitoTrabalhistaSemSituacao.size() > 0) {

			for (DebitoTrabalhistaBean debitoTrabalhistaBean : listaDebitoTrabalhistaSemSituacao) {

				if (debitoTrabalhistaBean.getSelected()) {

					retorno = true;
				}
			}
		}

		return retorno;
	}

	/**
	 * Método que dado uma parte, verifica se é réu ou autor
	 * 
	 * @param debitoTrabalhista
	 * 
	 * @author Estevão Mognatto
	 */
	public Boolean isProcessoParteAutor(DebitoTrabalhista debitoTrabalhista) {

		ProcessoParte processoParte = debitoTrabalhista.getProcessoParte();
		if (processoParte.getPartePrincipal() && processoParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.A)) {
			return true;
		} 
		return false;
	}

	/**
	 * Dados uma parte (encapsulada em um objeto DebitoTrabalhista) obtem o
	 * CPF/CNPJ
	 * 
	 * @param debitoTrabalhista
	 * 
	 * @author Estevão Mognatto
	 */
	public String obterCPFouCNPJParte(DebitoTrabalhista debitoTrabalhista) {

		ProcessoParte processoParte = debitoTrabalhista.getProcessoParte();

		String documentoCpfCnpj = "";

		if (processoParte.getPessoa() != null) {

			if (processoParte.getPessoa().getDocumentoCpfCnpj() != null) {

				documentoCpfCnpj = processoParte.getPessoa().getDocumentoCpfCnpj();
			}
		}

		return documentoCpfCnpj;
	}

	/**
	 * Lista das partes que ainda não possuem debitos trabalhistas (Já
	 * encapsuladas em um ojbeto Debito Trabalhista)
	 * 
	 * @author Estevão Mognatto
	 */
	public void listaDebitoTrabalhistaSemSituacao() {

		DebitoTrabalhistaService debitoTrabalhistaService = ComponentUtil.getComponent(DebitoTrabalhistaService.NAME);
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();

		if ((this.listaDebitoTrabalhistaSemSituacao != null) 
				|| (processoTrf == null) 
				|| (processoTrf.getIdProcessoTrf() == 0)) {
			return;
		}

		this.listaDebitoTrabalhistaSemSituacao = debitoTrabalhistaService.obterListaDebitoTrabalhistaSemSituacao();

	}

	/**
	 * Lista das partes que já possuem débitos trabalhistas (Já encapsuladas em
	 * um ojbeto Debito Trabalhista)
	 * 
	 * @author Estevão Mognatto
	 */
	public void listaDebitoTrabalhistaCadastrados() {

		DebitoTrabalhistaService debitoTrabalhistaService = ComponentUtil.getComponent(DebitoTrabalhistaService.NAME);

		if (this.listaDebitoTrabalhistaCadastrado != null) {
			return;
		}

		this.listaDebitoTrabalhistaCadastrado = debitoTrabalhistaService.obterListaDebitoTrabalhistaCadastrados();

	}

	/**
	 * Obtem todas as situações cadastradas no Banco de Dados.
	 * 
	 * @author Estevão Mognatto
	 */
	public void listaSituacoes() {

		DebitoTrabalhistaService debitoTrabalhistaService = ComponentUtil.getComponent(DebitoTrabalhistaService.NAME);

		if (this.listaSituacoes != null) {
			return;
		}

		this.listaSituacoes = debitoTrabalhistaService.obterListaSituacoes();
	}

	/**
	 * Obtem todas as situações cadastradas no Banco de Dados como uma String
	 * separada por vírgulas
	 * 
	 * @author Estevão Mognatto
	 */
	public String getListaSituacoesString() {

		StringBuilder listaSituacoesString = new StringBuilder();

		for (SituacaoDebitoTrabalhista situacaoDebitoTrabalhista : listaSituacoes) {

			listaSituacoesString.append(situacaoDebitoTrabalhista.getDescricao());
			listaSituacoesString.append(",");
		}

		String subStringSemUltimaVirgula = listaSituacoesString.substring(0, listaSituacoesString.length() - 1);

		return subStringSemUltimaVirgula;
	}

	public void setListaDebitoTrabalhistaSemSituacao(List<DebitoTrabalhistaBean> listaDebitoTrabalhistaSemSituacao) {
		this.listaDebitoTrabalhistaSemSituacao = listaDebitoTrabalhistaSemSituacao;
	}

	public List<DebitoTrabalhistaBean> getListaDebitoTrabalhistaSemSituacao() {
		return listaDebitoTrabalhistaSemSituacao;
	}

	public void setListaDebitoTrabalhistaCadastrado(List<DebitoTrabalhista> listaDebitoTrabalhistaCadastrado) {
		this.listaDebitoTrabalhistaCadastrado = listaDebitoTrabalhistaCadastrado;
	}

	public List<DebitoTrabalhista> getListaDebitoTrabalhistaCadastrado() {
		return listaDebitoTrabalhistaCadastrado;
	}

	public void setListaSituacoes(List<SituacaoDebitoTrabalhista> listaSituacoes) {
		this.listaSituacoes = listaSituacoes;
	}

	public List<SituacaoDebitoTrabalhista> getListaSituacoes() {
		return listaSituacoes;
	}

	public void setSituacaoSelecionada(String situacaoSelecionada) {
		this.situacaoSelecionada = situacaoSelecionada;
	}

	public String getSituacaoSelecionada() {
		return situacaoSelecionada;
	}

	public boolean isEditable() {
		return true;
	}

	public String getHomeName() {
		return NAME;
	}

	public ConsultaDebitoTrabalhista getInstance() {
		return instance;
	}

	public void setInstance(ConsultaDebitoTrabalhista instance) {
		this.instance = instance;
	}

	public void limparTela(String obj) {
		instance = new ConsultaDebitoTrabalhista();
		UIComponent form = ComponentUtil.getUIComponent(obj);
		ComponentUtil.clearChildren(form);
	}

	public void setOcorreuErro(boolean ocorreuErro) {
		this.ocorreuErro = ocorreuErro;
	}

	public boolean isOcorreuErro() {
		return ocorreuErro;
	}
	
	public Boolean getCheckAllDebitoTrabalhista(){
		return checkAllDebitoTrabalhista;
	}
	
	public void setCheckAllDebitoTrabalhista(Boolean checkAllDebitoTrabalhista){
		this.checkAllDebitoTrabalhista = checkAllDebitoTrabalhista;
	}	
	
}
