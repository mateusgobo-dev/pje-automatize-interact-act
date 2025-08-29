package br.com.infox.cliente.home;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.home.api.IProcessoDocumentoBinHome;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrfLocal;

@Name(ProcessoDocumentoTrfLocalHome.NAME)
@BypassInterceptors
public class ProcessoDocumentoTrfLocalHome extends AbstractHome<ProcessoDocumentoTrfLocal> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "processoDocumentoTrfLocalHome";

	public static ProcessoDocumentoTrfLocalHome instance() {
		return ComponentUtil.getComponent(ProcessoDocumentoTrfLocalHome.NAME);
	}

	@Observer(ProcessoHome.AFTER_UPDATE_PD_FLUXO_EVENT)
	public void updateProcessoDocumentoTrf(int i) {
		if (getInstance().getDecisaoTerminativa() != null) {
			if (i != 0) {
				ProcessoDocumentoTrfLocal pdtl = EntityUtil.find(ProcessoDocumentoTrfLocal.class, i);
				if (pdtl == null) {
					ProcessoDocumento pd = EntityUtil.find(ProcessoDocumento.class, i);
					if (pd != null && pd.getIdProcessoDocumento() != 0) {
							
					ProcessoDocumentoTrfLocal oldInstance  = getInstance();
					newInstance();
					try {
						PropertyUtils.copyProperties(getInstance(), oldInstance);
					} catch (Exception e) {
						FacesMessages.instance().clear();
						FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro não gravado!");
					}	
					getInstance().setProcessoDocumento(pd);
					getInstance().setIdProcessoDocumentoTrf(pd.getIdProcessoDocumento());
					super.persist();
					FacesMessages.instance().clear();
					FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro gravado com sucesso!");
					}
				} else {
					boolean decisao = getInstance().getDecisaoTerminativa();
					boolean visivel = getInstance().getExibirDocMinuta();
					setInstance(pdtl);
					getInstance().setDecisaoTerminativa(decisao);
					getInstance().setExibirDocMinuta(visivel);
					super.update();
					FacesMessages.instance().clear();
					FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro gravado com sucesso!");
				}
			}
		}
	}

	@Override
	public ProcessoDocumentoTrfLocal getInstance() {
		ProcessoDocumento pd = ProcessoHome.instance().getPdFluxo();
		if (!(pd == null || pd.getIdProcessoDocumento() == 0)) {
			ProcessoDocumentoTrfLocal pdtl = EntityUtil.find(ProcessoDocumentoTrfLocal.class,
					pd.getIdProcessoDocumento());
			/*
			 * Incluído este teste pois nem sempre existe um ProcessoDocumentoTrfLocal com 
			 * o id do ProcessoDocumento do fluxo (ProcessoHome.instance().getPdFluxo()).
			 */
			if (pdtl != null) {
				setInstance(pdtl);
			}
		} else {
			IProcessoDocumentoBinHome binHome = getComponent("processoDocumentoBinHome");
			pd = binHome.getProcessoDocumento();
			if (!(pd == null || pd.getIdProcessoDocumento() == 0)) {
				ProcessoDocumentoTrfLocal pdtl = EntityUtil.find(ProcessoDocumentoTrfLocal.class,
						pd.getIdProcessoDocumento());
				/*
				 * Incluído este teste pois nem sempre existe um ProcessoDocumentoTrfLocal com 
				 * o id do ProcessoDocumento do fluxo (ProcessoHome.instance().getPdFluxo()).
				 */
				if (pdtl != null) {
					setInstance(pdtl);
				}
			}
		}
		return super.getInstance();
	}
}
