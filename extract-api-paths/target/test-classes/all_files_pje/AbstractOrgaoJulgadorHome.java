package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

public abstract class AbstractOrgaoJulgadorHome<T> extends AbstractHome<OrgaoJulgador> {

	private static final long serialVersionUID = 1L;

	public void setOrgaoJulgadorIdOrgaoJulgador(Integer id) {
		setId(id);
	}

	public Integer getOrgaoJulgadorIdOrgaoJulgador() {
		return (Integer) getId();
	}

	@Override
	protected OrgaoJulgador createInstance() {
		OrgaoJulgador orgaoJulgador = new OrgaoJulgador();
		// EnderecoHome enderecoHome = (EnderecoHome) Component.getInstance(
		// "enderecoHome", false);
		// if (enderecoHome != null){
		// orgaoJulgador.setEndereco(enderecoHome.getDefinedInstance());
		// }
		return orgaoJulgador;
	}

	@Override
	public String remove(OrgaoJulgador obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("orgaoJulgadorGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		/*
		 * if (getInstance().getEndereco() != null) { List<OrgaoJulgador>
		 * enderecoList = getInstance().getEndereco().getOrgaoJulgadorList(); if
		 * (!enderecoList.contains(instance)) {
		 * getEntityManager().refresh(getInstance().getEndereco()); } }
		 */
		// newInstance();
		return action;
	}

}