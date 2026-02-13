package br.com.infox.cliente.home;

import javax.persistence.EntityExistsException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.TipoContato;
import br.jus.pje.nucleo.enums.TipoPessoaContatoEnum;

@Name("tipoContatoHome")
@BypassInterceptors
public class TipoContatoHome extends AbstractTipoContatoHome<TipoContato> {

	private static final long serialVersionUID = 1L;

	@Override
	public String persist() {
		String ret = null;
		try {
			ret = super.persist();
		} catch (EntityExistsException e) {

		} catch (Exception e) {

		}

		return ret;

	}

	public TipoPessoaContatoEnum[] getTipoPessoaContatoValues() {
		return TipoPessoaContatoEnum.values();
	}

	@Override
	public String remove(TipoContato obj) {
		obj.setAtivo(Boolean.FALSE);
		return super.remove(obj);
	}

}