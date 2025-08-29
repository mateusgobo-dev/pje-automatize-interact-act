package br.com.infox.cliente.home.icrrefactory;

import java.util.List;

import br.jus.pje.nucleo.entidades.IcrTransacaoPenal;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.ProcessoParte;

public abstract class IcrAssociarTransacaoPenalAction<T extends InformacaoCriminalRelevante, J extends IcrAssociarTransacaoPenalManager<T>>
		extends InformacaoCriminalRelevanteAction<T, J> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7583311059416007006L;
	private List<ProcessoParte> listaReusNoProcessoComTransacao;
	private List<IcrTransacaoPenal> listaTransacaoPorParte;

	@Override
	public void init() {
		super.init();
		listaReusNoProcessoComTransacao();
	}

	public void setProcessoParte(ProcessoParte processoParte) {
		getInstance().setProcessoParte(processoParte);
		listaTransacaoPorParte();
	}

	public ProcessoParte getProcessoParte() {
		return getInstance().getProcessoParte();
	}

	public abstract IcrTransacaoPenal getIcrTransacaoPenalSelecionada();

	public abstract void setIcrTransacaoPenalSelecionada(IcrTransacaoPenal icr);

	public void listaReusNoProcessoComTransacao() {
		if (listaReusNoProcessoComTransacao == null || listaReusNoProcessoComTransacao.isEmpty()) {
			listaReusNoProcessoComTransacao = getManager().listarReusNoProcessoComTransacao(getHome().getProcessoTrf());
		}
		if (listaReusNoProcessoComTransacao.size() == 1) {
			setProcessoParte(listaReusNoProcessoComTransacao.iterator().next());
		}
	}

	public void listaTransacaoPorParte() {
		listaTransacaoPorParte = listaTransacaoPorParte(getProcessoParte());
		if (listaTransacaoPorParte.size() == 1) {
			setIcrTransacaoPenalSelecionada(listaTransacaoPorParte.iterator().next());
		}
	}

	public List<IcrTransacaoPenal> listaTransacaoPorParte(ProcessoParte pp) {
		return getManager().listarTransacaoPorParte(pp);
	}

	public List<ProcessoParte> getListaReusNoProcessoComTransacao() {
		return listaReusNoProcessoComTransacao;
	}

	public void setListaReusNoProcessoComTransacao(List<ProcessoParte> listaReusNoProcessoComTransacao) {
		this.listaReusNoProcessoComTransacao = listaReusNoProcessoComTransacao;
	}

	public List<IcrTransacaoPenal> getListaTransacaoPorParte() {
		return listaTransacaoPorParte;
	}

	public void setListaTransacaoPorParte(List<IcrTransacaoPenal> listaTransacaoPorParte) {
		this.listaTransacaoPorParte = listaTransacaoPorParte;
	}
}
