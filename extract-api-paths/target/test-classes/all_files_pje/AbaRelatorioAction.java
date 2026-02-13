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

@Name("abaRelatorioAction")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AbaRelatorioAction extends AbstractInteiroTeorProcesso implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final TipoProcessoDocumento TIPO_PROCESSO_DOCUMENTO_RELATORIO = ParametroUtil.instance()
			.getTipoProcessoDocumentoRelatorio();
	private SessaoProcessoDocumento sessaoProcessoDocumentoRelatorio;

	public List<ModeloDocumento> getModeloDocumentoList() {
		return getModeloDocumentoList(TIPO_PROCESSO_DOCUMENTO_RELATORIO);
	}

	@Override
	public SessaoProcessoDocumento getSessaoProcessoDocumento() {
		if (sessaoProcessoDocumentoRelatorio == null) {
			sessaoProcessoDocumentoRelatorio = getSessaoProcessoDocumentoByTipo(TIPO_PROCESSO_DOCUMENTO_RELATORIO);

			// Se não existe nenhum processodocumento na sessão persistido
			// cria-se um novo
			if (sessaoProcessoDocumentoRelatorio != null
					&& sessaoProcessoDocumentoManager.documentoInclusoAposProcessoJulgado(
							sessaoProcessoDocumentoRelatorio.getProcessoDocumento(), getProcessoTrf().getProcesso())) {
				return sessaoProcessoDocumentoRelatorio;
			} else {
				criaNovoRelatorio();
			}
		}
		return sessaoProcessoDocumentoRelatorio;
	}

	private void criaNovoRelatorio() {
		sessaoProcessoDocumentoRelatorio = new SessaoProcessoDocumento();
		ProcessoDocumento processoDocumento = new ProcessoDocumento();
		processoDocumento.setProcessoDocumentoBin(new ProcessoDocumentoBin());
		sessaoProcessoDocumentoRelatorio.setProcessoDocumento(processoDocumento);
		sessaoProcessoDocumentoRelatorio.getProcessoDocumento().setTipoProcessoDocumento(
				TIPO_PROCESSO_DOCUMENTO_RELATORIO);
	}

	@Override
	public void setSessaoProcessoDocumento(SessaoProcessoDocumento sessaoProcessoDocumento) {
		this.sessaoProcessoDocumentoRelatorio = sessaoProcessoDocumento;
	}

	@Override
	public TipoVotoManager getTipoVotoManager() {
		return tipoVotoManager;
	}

}
