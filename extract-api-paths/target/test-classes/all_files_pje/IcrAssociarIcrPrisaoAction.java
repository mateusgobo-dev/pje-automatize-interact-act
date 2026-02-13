package br.com.infox.cliente.home.icrrefactory;

import java.util.List;

import org.jboss.seam.Component;

import br.jus.pje.nucleo.entidades.IcrPrisao;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

public abstract class IcrAssociarIcrPrisaoAction<T extends InformacaoCriminalRelevante, J extends InformacaoCriminalRelevanteManager<T>>
		extends IcrAssociarEstabelecimentosPrisionaisAction<T, J> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7240832401613014883L;

	private List<IcrPrisao> prisoesEmAberto;

	private IcrPrisaoManager icrPrisaoManager = (IcrPrisaoManager) Component.getInstance(IcrPrisaoManager.class);

	@Override
	public void init() {
		recuperarPrisoesEmAberto();
		super.init();
	}

	public List<IcrPrisao> getPrisoesEmAberto() {
		return prisoesEmAberto;
	}

	public void setPrisoesEmAberto(List<IcrPrisao> prisoesEmAberto) {
		this.prisoesEmAberto = prisoesEmAberto;
	}

	/**
	 * Adicionar no init da action que vai utilizar
	 */
	public void recuperarPrisoesEmAberto() {
		prisoesEmAberto = icrPrisaoManager.recuperarPrisoesEmAberto(InformacaoCriminalRelevanteHome.getHomeInstance()
				.getProcessoTrf());
	}
}
