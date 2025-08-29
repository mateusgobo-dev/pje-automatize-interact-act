package br.com.infox.ibpm.service;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

@Name(AssinaturaDocumentoService.NAME)
@Install(precedence = Install.FRAMEWORK)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AssinaturaDocumentoService extends GenericManager {

	public final static String NAME = "assinaturaDocumentoService";

	public Boolean isDocumentoAssinado(ProcessoDocumento processoDocumento) {
		return !processoDocumento.getProcessoDocumentoBin().getSignatarios().isEmpty();
	}

	public Boolean isDocumentoAssinado(Integer idDoc) {
		ProcessoDocumento processoDocumento = EntityUtil.find(ProcessoDocumento.class, idDoc);
		return processoDocumento != null && isDocumentoAssinado(processoDocumento);
	}
}
