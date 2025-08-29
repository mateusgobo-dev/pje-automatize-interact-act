package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;

import br.jus.pje.nucleo.entidades.EstabelecimentoPrisional;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

public abstract class IcrAssociarEstabelecimentosPrisionaisAction<T extends InformacaoCriminalRelevante, J extends InformacaoCriminalRelevanteManager<T>>
		extends InformacaoCriminalRelevanteAction<T, J> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4280637665847527099L;
	private IcrPrisaoManager icrPrisaoManager = (IcrPrisaoManager) Component.getInstance(IcrPrisaoManager.class);
	private String uf;
	private String cidade;
	private List<String> ufs = new ArrayList<String>(0);
	private List<String> cidades = new ArrayList<String>(0);
	private List<EstabelecimentoPrisional> estabelecimentos = new ArrayList<EstabelecimentoPrisional>(0);

	protected abstract EstabelecimentoPrisional getEstabelecimentoPrisional(T entity);

	@Override
	public void init() {
		super.init();
		recuperarEstadosEstabelecimentos();
		if (getInstance().getId() != null) {
			setUf(getEstabelecimentoPrisional(getInstance()).getUf());
			recuperarCidadesEstabelecimentos();
			setCidade(getEstabelecimentoPrisional(getInstance()).getDsCidade());
			recuperarEstabelecimentosPrisionais();
		}
	}

	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	public List<String> getUfs() {
		return ufs;
	}

	public void setUfs(List<String> ufs) {
		this.ufs = ufs;
	}

	public List<String> getCidades() {
		return cidades;
	}

	public void setCidades(List<String> cidades) {
		this.cidades = cidades;
	}

	public List<EstabelecimentoPrisional> getEstabelecimentos() {
		return estabelecimentos;
	}

	public void setEstabelecimentos(List<EstabelecimentoPrisional> estabelecimentos) {
		this.estabelecimentos = estabelecimentos;
	}

	/**
	 * Adicionar no init da action que vai utilizar
	 */
	public void recuperarEstadosEstabelecimentos() {
		uf = null;
		cidade = null;
		cidades = null;
		estabelecimentos = null;
		ufs = icrPrisaoManager.recuperarEstadosEstabelecimentosPrisionais();
		cidade = null;
	}

	public void recuperarCidadesEstabelecimentos() {
		cidade = null;
		cidades = null;
		estabelecimentos = null;
		if (getUf() != null) {
			cidades = icrPrisaoManager.recuperarCidadesEstabelecimentosPrisionais(getUf());
		}
	}

	public void recuperarEstabelecimentosPrisionais() {
		estabelecimentos = null;
		if (getCidade() != null) {
			estabelecimentos = icrPrisaoManager.recuperarEstabelecimentosPrisionais(getCidade());
		}
	}
}
