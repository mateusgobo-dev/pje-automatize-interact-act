package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.PagamentoPericia;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.ProcessoPericia;
import br.jus.pje.nucleo.enums.PagamentoEnum;
import br.jus.pje.nucleo.enums.PericiaStatusEnum;

@Name("pagamentoPericiaHome")
@BypassInterceptors
public class PagamentoPericiaHome extends AbstractPagamentoPericiaHome<PagamentoPericia> {

	private static final long serialVersionUID = 1L;
	private Boolean visualizarAbas = Boolean.FALSE;
	private String processo;
	private String pessoaPericiado;
	private String pessoaPerito;
	private Boolean possuiSolicitacoes;
	private int idProcessoPericia;
	private String useCase;
	private String statusPericia;
	private List<PagamentoPericia> pagamentosSolicitados = new ArrayList<PagamentoPericia>(0);

	public static PagamentoPericiaHome instance() {
		return ComponentUtil.getComponent("pagamentoPericiaHome");
	}

	// Define qual usecase que chama o Home
	public void setUseCase(String useCase) {
		this.useCase = useCase;
	}

	public PericiaStatusEnum[] getPericiaStatusEnumValues() {
		return PericiaStatusEnum.values();
	}

	@Override
	public void setId(Object id) {
		super.setId(id);
		if (useCase.equals("requisicao")) {
			setTab("RequisicaoTab");
			return;
		}
		if (id != null) {
			setTab("RegistroTab");
			setVisualizarAbas(true);
		}

	}

	public void setTab() {
		if (this.useCase.equals("requisicao")) {
			super.setTab("RequisicaoTab");
		} else if (this.useCase.equals("registro")) {
			super.setTab("RegistroTab");
		}
	}

	@Override
	public String persist() {
		int cont = 0;
		Double valor = 0.0;
		if (this.useCase.equals("requisicao")) {
			for (PagamentoPericia pag : pagamentosSolicitados) {
				if (pag.getPagamento().equals(PagamentoEnum.S)) {
					cont++;
				}
			}
			getInstance().setDataSolicitacao(new Date());
			getInstance().setPagamento(PagamentoEnum.S);
			getInstance().getProcessoPericia().setValorPericia(getInstance().getValorPericia());
			valor = getInstance().getValorPercentualRequerido() + getInstance().getTotalSolicitacaoPago();
		}

		String retorno = null;

		Double total = getInstance().getValorPericia();
		if (total == null) {
			total = 0.0;
		}

		if (cont == 0) {
			if (validaRequerimento(total, valor)) {
				retorno = super.persist();
				getEntityManager().refresh(getInstance());
				super.setTab("searchGrid");
				refreshGrid("pagamentosSolicitadosGrid");
				newInstance();
			}
		} else {
			FacesMessages.instance().add(Severity.ERROR, "Existe uma solicitação em aberto para esta perícia");
		}

		return retorno;

	}

	@Override
	public String update() {
		String retorno = null;
		if (getInstance().getDataPagamento().before(getInstance().getDataSolicitacao())) {
			FacesMessages.instance().add(Severity.ERROR,
					"A data de Pagamento não pode ser menor que a data da Solicitação");
			retorno = null;
		} else {
			PessoaServidor servidor = ((PessoaFisica) Authenticator.getUsuarioLogado()).getPessoaServidor();
			getInstance().setPessoaServidor(servidor);
			getInstance().setPagamento(PagamentoEnum.R);
			Double valor = getInstance().getValorPercentualPago() + getInstance().getTotalSolicitacaoPago();

			Double total = getInstance().getValorPericia();
			if (total == null) {
				total = 0.0;
			}

			retorno = null;
			if (validaRequerimento(total, valor)) {
				retorno = super.update();
				getEntityManager().refresh(getInstance());
				super.setTab("searchGrid");
				refreshGrid("pagamentosSolicitadosGrid");
				newInstance();
			} else {
				getInstance().setPagamento(PagamentoEnum.S);
			}

		}
		FacesMessages.instance().clear();
		FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro alterado com sucesso.");
		return retorno;
	}

	public boolean validaRequerimento(double valorTotal, double valorRequerido) {
		valorTotal = valorTotal * 0.3d;
		if (valorRequerido <= valorTotal) {
			return true;
		} else {
			FacesMessages.instance().add(Severity.ERROR, "O valor não pode ser maior que 30% do valor da perícia");
			return false;
		}
	}

	/**
	 * Método responsável por abilitar a aba do formulário para solicitar a
	 * antecipação do paramento da perícia
	 * 
	 * @param obj
	 *            ProcessoPericia
	 */
	public void setarAba(ProcessoPericia obj) {
		newInstance();
		Double totalPago = 0.0;
		visualizarAbas = Boolean.TRUE;
		ProcessoPericia proP = EntityUtil.getEntityManager().find(ProcessoPericia.class, obj.getIdProcessoPericia());
		PessoaPerito pp = proP.getPessoaPerito();
		Pessoa pparte = proP.getPessoaPericiado();
		Especialidade especialidade = proP.getEspecialidade();

		for (PagamentoPericia pag : proP.getPagamentoPericiaList()) {
			if (pag.getPagamento() == PagamentoEnum.R) {
				totalPago += pag.getValorPercentualPago();
			}
		}

		if (pparte != null) {
			getInstance().setNomePericiado(pparte.getNomeParte());
		}
		getInstance().setNomePerito(pp.getNome());
		getInstance().setNumeroProcesso(proP.getProcessoTrf().getProcesso().getNumeroProcesso());
		getInstance().setEspecialidade(especialidade.getEspecialidade());
		getInstance().setProcessoPericia(proP);
		getInstance().setTotalSolicitacaoPago(totalPago);

		GregorianCalendar dataSolicitacao = new GregorianCalendar();
		getInstance().setDataSolicitacao(dataSolicitacao.getTime());

		setTab();

	}

	/**
	 * Método responsável por abilitar a aba do formulário para registar o
	 * pagamento da perícia
	 * 
	 * @param obj
	 *            PagamentoPericia
	 */
	public void setarAba(PagamentoPericia obj) {
		Double totalPago = 0.0;
		visualizarAbas = Boolean.TRUE;
		ProcessoPericia proP = EntityUtil.getEntityManager().find(ProcessoPericia.class,
				obj.getProcessoPericia().getIdProcessoPericia());
		PessoaPerito pp = proP.getPessoaPerito();
		Pessoa pparte = proP.getPessoaPericiado();
		Especialidade especialidade = proP.getEspecialidade();

		for (PagamentoPericia pag : proP.getPagamentoPericiaList()) {
			if (pag.getPagamento() == PagamentoEnum.S) {
				setInstance(pag);
			}
			if (pag.getPagamento() == PagamentoEnum.R) {
				totalPago += pag.getValorPercentualPago();
			}
		}
		if (pparte != null) {
			getInstance().setNomePericiado(pparte.getNomeParte());
		}
		getInstance().setNomePerito(pp.getNome());
		getInstance().setNumeroProcesso(proP.getProcessoTrf().getProcesso().getNumeroProcesso());
		getInstance().setEspecialidade(especialidade.getEspecialidade());
		getInstance().setProcessoPericia(proP);
		getInstance().setTotalSolicitacaoPago(totalPago);

		setTab();
	}

	public void setVisualizarAbas(Boolean visualizarAbas) {
		this.visualizarAbas = visualizarAbas;
	}

	public Boolean getVisualizarAbas() {
		return visualizarAbas;
	}

	@Override
	public void newInstance() {
		visualizarAbas = Boolean.FALSE;
		super.newInstance();
	}

	public void setProcesso(String processo) {
		this.processo = processo;
	}

	public String getProcesso() {
		return processo;
	}

	public void setPessoaPericiado(String pessoaPericiado) {
		this.pessoaPericiado = pessoaPericiado;
	}

	public String getPessoaPericiado() {
		return pessoaPericiado;
	}

	public Boolean getPossuiSolicitacoes() {
		EntityManager em = EntityUtil.getEntityManager();
		String query = "select count(o) from PagamentoPericia o where o.pagamento = 'R' "
				+ "and processoPericia = :processoPericia";
		Query q = em.createQuery(query);
		q.setParameter("processoPericia", getInstance().getProcessoPericia());
		try {
			Long retorno = (Long) q.getSingleResult();
			possuiSolicitacoes = retorno > 0;
		} catch (NoResultException no) {
			possuiSolicitacoes = Boolean.FALSE;
		}
		
		return possuiSolicitacoes;
	}

	public void setPossuiSolicitacoes(Boolean possuiSolicitacoes) {
		this.possuiSolicitacoes = possuiSolicitacoes;
	}

	public int getIdProcessoPericia() {
		idProcessoPericia = getInstance().getProcessoPericia().getIdProcessoPericia();
		return idProcessoPericia;
	}

	public void setIdProcessoPericia(int idProcessoPericia) {
		this.idProcessoPericia = idProcessoPericia;
	}

	public String getPessoaPerito() {
		return pessoaPerito;
	}

	public void setPessoaPerito(String pessoaPerito) {
		this.pessoaPerito = pessoaPerito;
	}

	public void setStatusPericia(String statusPericia) {
		this.statusPericia = statusPericia;
	}

	public String getStatusPericia() {
		return statusPericia;
	}

	public PericiaStatusEnum[] getStatusPericiaEnumValues() {
		return PericiaStatusEnum.values();

	}

	public void limparSearch() {
		Contexts.removeFromAllContexts("especialidadeTree");
	}
}