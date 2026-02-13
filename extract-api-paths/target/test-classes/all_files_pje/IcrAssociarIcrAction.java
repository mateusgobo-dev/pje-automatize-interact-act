package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.international.StatusMessage.Severity;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.ProcessoParte;

public abstract class IcrAssociarIcrAction<T extends InformacaoCriminalRelevante, J extends IcrAssociarIcrManager<T>>
		extends InformacaoCriminalRelevanteAction<T, J> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1047110958749482001L;
	private List<InformacaoCriminalRelevante> listaIcr;
	private List<ProcessoParte> listaReusNoProcesso = new ArrayList<ProcessoParte>(0);
	private Integer page;

	@Override
	public void init() {
		super.init();
		listarReusNoProcesso();
		listarIcrPorParteETipo();
	}

	public Boolean getExibirModal() {
		return getReuSelecionado() != null && getIcrAfetada() == null;
	}

	public ProcessoParte getReuSelecionado() {
		return getInstance().getProcessoParte();
	}

	public void setReuSelecionado(ProcessoParte reuSelecionado) {
		if (reuSelecionado == null) {
			getListaIcr().clear();
			setIcrAfetada(null);
		}
		boolean changed = reuSelecionado != null && !reuSelecionado.equals(getInstance().getProcessoParte());
		if (changed) {
			setIcrAfetada(null);
			setListaIcr(getIcrPorParte(reuSelecionado));
		}
		getInstance().setProcessoParte(reuSelecionado);
	}

	/**
	 * recarrega a lista de réus conforme os tipos aceitos
	 */
	public void listarReusNoProcesso() {
		List<ProcessoParte> reus = getManager().recuperarReusNoProcesso(getHome().getProcessoTrf());
		if (reus.size() == 1) {
			getInstance().setProcessoParte(reus.get(0));
		}
		setListaReusNoProcesso(reus);
		// return reus;
	}

	/**
	 * recupera a lista de icrs do réu
	 */
	public List<InformacaoCriminalRelevante> getIcrPorParte(ProcessoParte pp) {
		List<InformacaoCriminalRelevante> icrs = getManager().recuperarIcrPorParteEtipo(pp);
		if (icrs != null && (icrs.size() == 1)) {
			setIcrAfetada(icrs.get(0));
		}
		return icrs;
	}

	/**
	 * recarrega a lista de icrs do réu selecionado
	 */
	public void listarIcrPorParteETipo() {
		if (getInstance() != null && getInstance().getProcessoParte() != null) {
			setListaIcr(getIcrPorParte(getInstance().getProcessoParte()));
		}
	}

	/**
	 * Icr escolhida pelo usuário que será vinculada à Decisão em Instância
	 * Superior
	 * 
	 * @param icrAfetada
	 */
	public abstract void setIcrAfetada(InformacaoCriminalRelevante icrAfetada);

	/**
	 * Icr escolhida pelo usuário que será vinculada à Decisão em Instância
	 * Superior
	 */
	public abstract InformacaoCriminalRelevante getIcrAfetada();

	/**
	 * mensagem para retornar quando getListaReusNoProcesso() não retornar
	 * nenhum réu
	 * 
	 * @return
	 */
	public abstract String getTextNaoHaReusComSentencaParaAssociar();

	@Override
	public void insert() {
		try {
			inicializaParaGravacao();
			getManager().persist(getInstance());
			getIcrList().clear();
			addMessage(Severity.INFO, "InformacaoCriminalRelevante_created", null);
			postInsertNavigation();
		} catch (Exception e) {
			addMessage(Severity.ERROR, e.getMessage(), e);
		}
	}

	@Override
	public void next() {
		try {
			getManager().validate(getInstance());
			getIcrList().add(getInstance());
			getHome().showTabTipificacaoDelito();
		} catch (Exception e) {
			addMessage(Severity.ERROR, e.getMessage(), e);
		}
	}

	public List<InformacaoCriminalRelevante> getListaIcr() {
		return listaIcr;
	}

	public void setListaIcr(List<InformacaoCriminalRelevante> listaIcr) {
		this.listaIcr = listaIcr;
	}

	public void setListaReusNoProcesso(List<ProcessoParte> listaReusNoProcesso) {
		this.listaReusNoProcesso = listaReusNoProcesso;
	}

	public List<ProcessoParte> getListaReusNoProcesso() {
		return listaReusNoProcesso;
	}

	public Integer getNumeroDeReusNoProcesso() {
		if (getListaReusNoProcesso() != null)
			return getListaReusNoProcesso().size();
		return 0;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getPage() {
		return page;
	}
}
