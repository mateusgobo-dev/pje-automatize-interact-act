package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

@Name("visualizarDocumentosDigitalizadosAction")
@Scope(ScopeType.EVENT)
public class VisualizarDocumentosDigitalizadosAction implements Serializable {

	private static final long serialVersionUID = 8802876019051984423L;

	private ProcessoDocumento documentoPrincipal;
	private List<ProcessoDocumento> documentosDigitalizados = new ArrayList<ProcessoDocumento>(0);

	@In
	private TramitacaoProcessualService tramitacaoProcessualService;

	@In
	private ProcessoDocumentoManager processoDocumentoManager;


	@SuppressWarnings("unchecked")
	@Create
	public void init() {
		if (tramitacaoProcessualService != null) {

			Integer valor = (Integer) tramitacaoProcessualService.recuperaVariavel(DigitalizarDocumentosAction.DOCUMENTO_PRINCIPAL_FINALIZADO);
			if (valor != null) {
				Map<Integer, Integer> arquivosAnexos = (Map<Integer, Integer>) tramitacaoProcessualService.recuperaVariavel(DigitalizarDocumentosAction.DOCUMENTOS_ANEXOS_FINALIZADOS);
				try {
					this.documentoPrincipal = processoDocumentoManager.findById(valor);
					for (Map.Entry<Integer, Integer> entry : arquivosAnexos.entrySet()) {
						documentosDigitalizados.add(processoDocumentoManager.findById(entry.getValue()));
					}
				} catch (PJeBusinessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

	}
	
	public List<ProcessoDocumento> getDocumentoPrincipalList() {
		return Arrays.asList(documentoPrincipal);
	}
	

	public List<ProcessoDocumento> getDocumentosDigitalizados() {
		return documentosDigitalizados;
	}
}