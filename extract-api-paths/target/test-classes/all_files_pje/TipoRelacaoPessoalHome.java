package br.com.infox.cliente.home;

import static org.jboss.seam.faces.FacesMessages.instance;

import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoRelacaoPessoal;
import br.jus.pje.nucleo.enums.TipoPessoaRelacaoEnum;

@Name(TipoRelacaoPessoalHome.NAME)
@BypassInterceptors
public class TipoRelacaoPessoalHome extends AbstractHome<TipoRelacaoPessoal> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "tipoRelacaoPessoalHome";

	public void setTipoRelacaoPessoalCodigoTipoRelacaoPessoal(String id) {
		setId(id);
	}

	public String getTipoRelacaoPessoalCodigoTipoRelacaoPessoal() {
		return (String) getId();
	}

	@Override
	public String persist() {
		String ret = super.persist();
		refreshGrid("tipoRelacaoPessoalGrid");
		return ret;
	}

	@Override
	public String update() {
		String ret = null;

		try{
			getEntityManager().merge(getInstance());
			getEntityManager().flush();
			ret = getUpdatedMessage().getValue().toString();
			instance().add(StatusMessage.Severity.ERROR, "Registro alterado com sucesso");
		} catch (Exception e){
			Throwable cause = e.getCause();
			if (cause instanceof ConstraintViolationException){
				instance().add(StatusMessage.Severity.ERROR, "Registro informado já cadastrado no sistema.");
			}
		}
		return ret;
	}

	public TipoPessoaRelacaoEnum[] getTipoPessoaRelacaoValues() {
		return TipoPessoaRelacaoEnum.values();
	}

	@Override
	public void newInstance() {
		super.newInstance();
	}

}
