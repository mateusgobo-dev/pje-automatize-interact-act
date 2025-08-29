package br.com.infox.cliente.home;

import java.util.List;

import org.hibernate.AssertionFailure;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoTipoPessoa;

@Name("tipoProcessoDocumentoTipoPessoaHome")
@BypassInterceptors
public class TipoProcessoDocumentoTipoPessoaHome extends
		AbstractTipoProcessoDocumentoTipoPessoaHome<TipoProcessoDocumentoTipoPessoa> {

	private static final long serialVersionUID = 1L;

	public void addTipoProcessoDocumentoTipoPessoa(TipoPessoa obj, String gridId) {
		if (getInstance() != null) {
			getInstance().setTipoPessoa(obj);
			getInstance().setTipoProcessoDocumento(TipoProcessoDocumentoTrfHome.instance().getInstance());

			persist();

			refreshGrid("tipoProcessoDocumentoTipoPessoaGrid");
			refreshGrid("tipoProcessoDocumentoTipoPessoaRightGrid");
			FacesMessages.instance().clear();
		}
	}

	public void removeTipoProcessoDocumentoTipoPessoa(TipoProcessoDocumentoTipoPessoa obj, String gridId) {
		if (getInstance() != null) {

			TipoPessoa tipoPessoa = obj.getTipoPessoa();

			List<TipoProcessoDocumentoTipoPessoa> tipoProcessoDocumentoTipoPessoaList = tipoPessoa
					.getTipoProcessoDocumentoTipoPessoaList();
			tipoProcessoDocumentoTipoPessoaList.remove(obj);

			getEntityManager().remove(obj);

			try {
				getEntityManager().flush();
				FacesMessages.instance().add(Severity.INFO, "Registro inativado com sucesso.");
			} catch (AssertionFailure e) {
				System.out.println(e.getMessage());
			}

			newInstance();
			refreshGrid("tipoProcessoDocumentoTipoPessoaGrid");
			refreshGrid("tipoProcessoDocumentoTipoPessoaRightGrid");
			FacesMessages.instance().clear();
		}
	}

}