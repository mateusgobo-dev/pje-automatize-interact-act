package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoCKManager;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;
import br.jus.pje.nucleo.entidades.AplicacaoClasseTipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.ModeloDocumentoCK;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoTrf;
import br.jus.pje.nucleo.enums.TipoEditorEnum;

@Name(AplicacaoClasseTipoProcessoDocumentoHome.NAME)
public class AplicacaoClasseTipoProcessoDocumentoHome extends
		AbstractAplicacaoClasseTipoProcessoDocumentoHome<AplicacaoClasseTipoProcessoDocumento> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "aplicacaoClasseTipoProcessoDocumentoHome";

	@Logger
	private Log logger;
	
	@In
	private ModeloDocumentoCKManager modeloDocumentoCKManager;

	public static AplicacaoClasseTipoProcessoDocumentoHome instance() {
		return ComponentUtil.getComponent(AplicacaoClasseTipoProcessoDocumentoHome.NAME);
	}

	public void addAplicacaoClasseTipoProcessoDocumento(AplicacaoClasse obj, String gridId) {
		if (getInstance() != null) {
			TipoProcessoDocumentoTrf tipoProcessoDocTrf = TipoProcessoDocumentoTrfHome.instance().getInstance();
			
			getInstance().setAplicacaoClasse(obj);
			getInstance().setTipoProcessoDocumento(tipoProcessoDocTrf);

			String persisted = persist();
			String mensagem = "";
			Severity severidade = Severity.INFO;
			
			if( persisted != null && persisted.equals("persisted")) {
				mensagem = "Aplicação de classe adicionada com sucesso!";
				severidade = Severity.INFO;
				if (ParametroUtil.instance().isParametrosCKEditorDefinidos() && obj == ParametroUtil.instance().getAplicacaoSistema()) {
					ModeloDocumentoCK modeloCK = modeloDocumentoCKManager.montaModeloDocumentoCK(tipoProcessoDocTrf);
					try {
						modeloDocumentoCKManager.persistAndFlush(modeloCK);
					} catch (PJeBusinessException e) {
						logger.error(e.getCause(), e);
						mensagem = "ModeloDocumentoCK.erroGravarModelo";
						severidade = Severity.ERROR;
					}
				}
			} else {
				mensagem = "Houve erro na adição de aplicação de classe!";
				severidade = Severity.ERROR;
			}
			refreshGrid("aplicacaoClasseTipoProcessoDocumentoGrid");
			refreshGrid("aplicacaoClasseGrid");

			FacesMessages.instance().clear();
			FacesMessages.instance().add(severidade, mensagem);
		}
	}

	public void removeAplicacaoClasseTipoProcessoDocumento(AplicacaoClasseTipoProcessoDocumento obj, String gridId) {
		if (getInstance() != null) {

			AplicacaoClasse aplicacaoClasse = obj.getAplicacaoClasse();

			List<AplicacaoClasseTipoProcessoDocumento> aplicacaoClasseTipoProcessoDocumentoList = aplicacaoClasse
					.getAplicacaoClasseTipoProcessoDocumentoList();
			aplicacaoClasseTipoProcessoDocumentoList.remove(obj);

			EntityManager em = getEntityManager();
			em.remove(obj);

			EntityUtil.flush(em);

			newInstance();
			refreshGrid("aplicacaoClasseTipoProcessoDocumentoGrid");
			refreshGrid("aplicacaoClasseGrid");
			FacesMessages.instance().clear();
			
			FacesMessages.instance().add(Severity.INFO, "Aplicação de classe removida com sucesso!");
		}
	}
}