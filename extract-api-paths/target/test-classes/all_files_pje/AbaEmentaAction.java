package br.com.infox.pje.action;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

@Name("abaEmentaAction")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AbaEmentaAction extends AbstractInteiroTeorProcesso implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final TipoProcessoDocumento TIPO_PROCESSO_DOCUMENTO_EMENTA = ParametroUtil.instance()
			.getTipoProcessoDocumentoEmenta();
	private SessaoProcessoDocumento sessaoProcessoDocumentoEmenta;

	@Override
	public SessaoProcessoDocumento getSessaoProcessoDocumento() {
		if (sessaoProcessoDocumentoEmenta == null) {

			sessaoProcessoDocumentoEmenta = super.getSessaoProcessoDocumentoByTipo(TIPO_PROCESSO_DOCUMENTO_EMENTA);

			// Se não existe nenhum processodocumento na sessão persistido
			// cria-se um novo
			if (sessaoProcessoDocumentoEmenta != null
					&& sessaoProcessoDocumentoManager.documentoInclusoAposProcessoJulgado(
							sessaoProcessoDocumentoEmenta.getProcessoDocumento(), getProcessoTrf().getProcesso())) {
				return sessaoProcessoDocumentoEmenta;
			} else {
				criaNovaEmenta();
			}
		}
		return sessaoProcessoDocumentoEmenta;
	}

	private void criaNovaEmenta() {
		sessaoProcessoDocumentoEmenta = new SessaoProcessoDocumento();
		ProcessoDocumento processoDocumento = new ProcessoDocumento();
		processoDocumento.setProcessoDocumentoBin(new ProcessoDocumentoBin());
		sessaoProcessoDocumentoEmenta.setProcessoDocumento(processoDocumento);
		sessaoProcessoDocumentoEmenta.getProcessoDocumento().setTipoProcessoDocumento(TIPO_PROCESSO_DOCUMENTO_EMENTA);
	}

	public List<ModeloDocumento> getModeloDocumentoList() {
		return getModeloDocumentoList(TIPO_PROCESSO_DOCUMENTO_EMENTA);
	}

	@Override
	public void setSessaoProcessoDocumento(SessaoProcessoDocumento sessaoProcessoDocumento) {
		this.sessaoProcessoDocumentoEmenta = sessaoProcessoDocumento;
	}

	@Override
	public TipoVotoManager getTipoVotoManager() {
		return tipoVotoManager;
	}

}
