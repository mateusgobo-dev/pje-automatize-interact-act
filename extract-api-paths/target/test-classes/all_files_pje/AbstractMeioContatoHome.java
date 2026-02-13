package br.com.infox.cliente.home;

import javax.persistence.Query;

import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.bean.PreCadastroPessoaBean;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.MeioContato;
import br.jus.pje.nucleo.util.StringUtil;

public abstract class AbstractMeioContatoHome<T> extends AbstractHome<MeioContato> {

	private static final long serialVersionUID = 1L;

	public void setMeioContatoIdMeioContato(Integer id) {
		setId(id);
	}

	public Integer getMeioContatoIdMeioContato() {
		return (Integer) getId();
	}

	// @Override
	// protected MeioContato createInstance() {
	// MeioContato meioContato = new MeioContato();
	// UsuarioHome usuarioHome = (UsuarioHome) Component.getInstance(
	// "usuarioHome", false);
	// if (usuarioHome != null) {
	// meioContato.setUsuario(usuarioHome.getDefinedInstance());
	// }
	// TipoContatoHome tipoContatoHome = (TipoContatoHome) Component
	// .getInstance("tipoContatoHome", false);
	// if (tipoContatoHome != null) {
	// meioContato.setTipoContato(tipoContatoHome.getDefinedInstance());
	// }
	// return meioContato;
	// }
	//
	// @Override
	// public String remove() {
	// UsuarioHome usuario = (UsuarioHome) Component.getInstance(
	// "usuarioHome", false);
	// if (usuario != null) {
	// usuario.getInstance().getMeioContatoList().remove(instance);
	// }
	// TipoContatoHome tipoContato = (TipoContatoHome) Component.getInstance(
	// "tipoContatoHome", false);
	// if (tipoContato != null) {
	// tipoContato.getInstance().getMeioContatoList().remove(instance);
	// }
	// return super.remove();
	// }

	@Override
	public String remove(MeioContato obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("meioContatoGrid");
		return ret;
	}

	@Override
	public String persist() {
		if (instance.getPessoa() == null) {
			PreCadastroPessoaBean pbean = (PreCadastroPessoaBean) ComponentUtil.getComponent("preCadastroPessoaBean");
			instance.setPessoa(pbean.getPessoa());
		}
		String action = super.persist();
		// if (getInstance().getUsuario() != null) {
		// List<MeioContato> usuarioList = getInstance().getUsuario()
		// .getMeioContatoList();
		// if (!usuarioList.contains(instance)) {
		// getEntityManager().refresh(getInstance().getUsuario());
		// }
		// }

		// if (getInstance().getTipoContato() != null) {
		// List<MeioContato> tipoContatoList = getInstance().getTipoContato()
		// .getMeioContatoList();
		// if (!tipoContatoList.contains(instance)) {
		// getEntityManager().refresh(getInstance().getTipoContato());
		// }
		// }
		// newInstance();
		return action;
	}

	private boolean verificaMeioContatoDuplicado() {
		if(getInstance().getTipoContato()!=null && StringUtil.isNotEmpty(getInstance().getTipoContato().getTipoContato())
				&& getInstance().getValorMeioContato()!=null && StringUtil.isNotEmpty(getInstance().getValorMeioContato())){
			StringBuilder sb = new StringBuilder();
			sb.append(" select o from MeioContato o ");
			sb.append("where o.pessoa = :pessoa and o.tipoContato = :tipoContato and o.valorMeioContato = :valorMeioContato");

			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("pessoa", getInstance().getPessoa());
			q.setParameter("tipoContato", getInstance().getTipoContato());
			q.setParameter("valorMeioContato", getInstance().getValorMeioContato());

			if (!q.getResultList().isEmpty()) {
				if(q.getResultList().get(0)!=getInstance()){
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public String update() {

		String ret = null;
		if (verificaMeioContatoDuplicado()) {
			FacesMessages.instance().add(Severity.ERROR, "Já existe este Meio de Contato cadastrado.");
			refreshGrid("meioContatoGrid");
			getEntityManager().clear();
			return ret;
		}else{
			return super.update();
		}
	}
}