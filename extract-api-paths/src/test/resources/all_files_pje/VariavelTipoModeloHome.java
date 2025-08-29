package br.com.infox.ibpm.home;

import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.TipoModeloDocumento;
import br.jus.pje.nucleo.entidades.Variavel;
import br.jus.pje.nucleo.entidades.VariavelTipoModelo;

@Name("variavelTipoModeloHome")
@BypassInterceptors
public class VariavelTipoModeloHome extends AbstractVariavelTipoModeloHome<VariavelTipoModelo> {

	private static final long serialVersionUID = 1L;

	@Override
	public void newInstance() {
		super.newInstance();
		limparCamposPesquisa();
	}

	public void addVariavelTipoModelo(Variavel obj, String gridId) {
		if (getInstance() != null) {
			getInstance().setVariavel(obj);

			VariavelTipoModelo variavelTipoModelo = getInstance();

			persist();

			VariavelHome.instance().getInstance().getVariavelTipoModeloList().add(variavelTipoModelo);

			FacesMessages.instance().clear();
			refreshGrid("variavelTipoModeloGrid");
			refreshGrid("variavelGrid");
			
			FacesMessages.instance().add(Severity.INFO, "Variável adicionada com sucesso!");
		}
	}

	public void removeVariavelTipoModelo(VariavelTipoModelo obj, String gridId) {
		if (getInstance() != null) {

			Variavel variavel = obj.getVariavel();

			List<VariavelTipoModelo> variavelTipoModeloList = variavel.getVariavelTipoModeloList();
			variavelTipoModeloList.remove(obj);

			getEntityManager().remove(obj);

			getEntityManager().flush();
			EntityUtil.flush(getEntityManager());

			newInstance();
			FacesMessages.instance().clear();
			refreshGrid("variavelTipoModeloGrid");
			refreshGrid("variavelGrid");
			
			FacesMessages.instance().add(Severity.INFO, "Variável removida com sucesso!");
		}
	}

	public void addTipoModeloVariavel(TipoModeloDocumento obj, String gridId) {
		if (getInstance() != null) {
			getInstance().setTipoModeloDocumento(obj);

			VariavelTipoModelo variavelTipoModelo = getInstance();

			persist();

			TipoModeloDocumentoHome.instance().getInstance().getVariavelTipoModeloList().add(variavelTipoModelo);

			FacesMessages.instance().clear();
			refreshGrid("tipoModeloVariavelGrid");
			refreshGrid("tipoModeloDocumentoGrid");
			
			FacesMessages.instance().add(Severity.INFO, "Tipo de Modelo de Documento adicionado com sucesso!");
		}
	}

	public void removeTipoModeloVariavel(VariavelTipoModelo obj, String gridId) {
		if (getInstance() != null) {
			TipoModeloDocumento tipoModeloDocumento = obj.getTipoModeloDocumento();

			List<VariavelTipoModelo> variavelTipoModeloList = tipoModeloDocumento.getVariavelTipoModeloList();
			variavelTipoModeloList.remove(obj);

			getEntityManager().remove(obj);

			getEntityManager().flush();
			EntityUtil.flush(getEntityManager());

			newInstance();
			FacesMessages.instance().clear();
			refreshGrid("tipoModeloVariavelGrid");
			refreshGrid("tipoModeloDocumentoGrid");
			
			FacesMessages.instance().add(Severity.INFO, "Tipo de Modelo de Documento removido com sucesso!");
		}
	}

	public void limparCamposPesquisa() {
		Contexts.removeFromAllContexts("grupoModeloDocumentoSuggest");

	}
}