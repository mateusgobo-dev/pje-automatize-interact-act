package br.com.infox.cliente.home;

import static org.jboss.seam.faces.FacesMessages.instance;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.StatusMessage;

import br.jus.pje.nucleo.entidades.TipoEndereco;

@Name("tipoEnderecoHome")
@BypassInterceptors
public class TipoEnderecoHome extends AbstractTipoEnderecoHome<TipoEndereco> {

	private static final long serialVersionUID = 1L;

	/*
	 * Método createInstance sobrescrito, possibilitando setar valores padrão.
	 * Isso não poderia ser realizado caso o método sobreescrito fosse o
	 * newInstance(non-Javadoc)
	 * 
	 * @see br.com.infox.cliente.home.AbstractTipoEnderecoHome#createInstance()
	 */
	@Override
	protected TipoEndereco createInstance() {
		TipoEndereco tp = super.createInstance();
		return tp;
	}

	@Override
	public String persist() {
		String ret = null;
		try {
			ret = super.persist();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	@SuppressWarnings("finally")
	@Override
	public String update() {
		try {
			refreshGrid("tipoEnderecoGrid");
			return super.update();
		} catch (Exception e) {
			instance().add(StatusMessage.Severity.ERROR, "Registro já existe!");
			e.printStackTrace();
		} finally {
			return "";
		}
	}
}