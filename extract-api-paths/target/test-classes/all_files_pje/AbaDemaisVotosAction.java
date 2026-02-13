package br.com.infox.pje.action;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.ModeloDocumentoLocalManager;
import br.com.infox.pje.service.SessaoJulgamentoService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;

@Name("abaDemaisVotosAction")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AbaDemaisVotosAction extends AbstractInteiroTeorProcesso implements Serializable {

	private static final long serialVersionUID = 1L;
	private SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto;
	private ProcessoDocumento processoDocumentoVoto;
	private boolean visualizarProcesso = false;

	@In
	private transient SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager;

	@In
	private transient SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager;

	@In
	private transient ModeloDocumentoLocalManager modeloDocumentoLocalManager;

	@In
	private transient ProcessoDocumentoManager processoDocumentoManager;

	@In
	private TipoVotoManager tipoVotoManager;

	private static final TipoProcessoDocumento TIPO_PROCESSO_DOCUMENTO_VOTO = ParametroUtil.instance()
			.getTipoProcessoDocumentoVoto();

	@In
	private SessaoJulgamentoService sessaoJulgamentoService;

	public ProcessoDocumento getProcessoDocumentoVoto() {
		return processoDocumentoVoto;
	}

	public void setProcessoDocumentoVoto(ProcessoDocumento processoDocumentoVoto) {
		this.processoDocumentoVoto = processoDocumentoVoto;
	}

	public List<ModeloDocumento> getModeloDocumentoList() {
		return getModeloDocumentoList(ParametroUtil.instance().getTipoProcessoDocumentoVoto());
	}

	public void setSessaoProcessoDocumentoVoto(SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto) {
		this.sessaoProcessoDocumentoVoto = sessaoProcessoDocumentoVoto;
	}

	public SessaoProcessoDocumentoVoto getSessaoProcessoDocumentoVoto() {
		if (sessaoProcessoDocumentoVoto == null) {

			sessaoProcessoDocumentoVoto = this.getSessaoProcessoDocumentoVotoByTipoOj(Authenticator.getOrgaoJulgadorAtual());

			// Se não existe nenhum processodocumento na sessão persistido
			// cria-se um novo
			if (sessaoProcessoDocumentoVoto == null) {
				sessaoProcessoDocumentoVoto = new SessaoProcessoDocumentoVoto();
				ProcessoDocumento processoDocumento = new ProcessoDocumento();
				ProcessoDocumentoBin processoDocumentoBin = new ProcessoDocumentoBin();
				processoDocumento.setProcessoDocumentoBin(processoDocumentoBin);
				sessaoProcessoDocumentoVoto.setProcessoDocumento(processoDocumento);
				sessaoProcessoDocumentoVoto.getProcessoDocumento().setTipoProcessoDocumento(
						TIPO_PROCESSO_DOCUMENTO_VOTO);
			}
		}
		return sessaoProcessoDocumentoVoto;
	}

	/**
	 * Ao clicar em uma lupa, altera o voto sendo exibido na tela
	 * 
	 * @param sessaoProcessoDocumentoVoto
	 *            voto sendo exibido atualmente
	 */
	public void alteraProcessoExibido(SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto) {
		this.setSessaoProcessoDocumentoVoto(sessaoProcessoDocumentoVoto);

		// TODO: Verificar porque foi necessário alterar diretamente
		// ProcessoDocumentoBin e não simplesmente alterar o objeto
		// SessaoProcessoDocumentoVoto
		this.getSessaoProcessoDocumentoVoto().getProcessoDocumento()
				.setProcessoDocumentoBin(sessaoProcessoDocumentoVoto.getProcessoDocumento().getProcessoDocumentoBin());

		// O usuário poderá visualizar o processo, caso relator, apenas após
		// clicar na lupa
		setVisualizarProcesso(true);
	}

	public List<TipoVoto> getTipoVotoList() {
		if (this.tipoVotoList == null) {
			this.tipoVotoList = sessaoJulgamentoService.listTipoVotoAtivoSemRelator();
		}
		return tipoVotoList;
	}

	public void setVisualizarProcesso(Boolean visualizarProcesso) {
		this.visualizarProcesso = visualizarProcesso;
	}

	public Boolean getVisualizarProcesso() {
		return visualizarProcesso;
	}

	@Override
	public SessaoProcessoDocumento getSessaoProcessoDocumento() {
		if (sessaoProcessoDocumentoVoto == null) {
			sessaoProcessoDocumentoVoto = (SessaoProcessoDocumentoVoto) this
					.getSessaoProcessoDocumentoByTipo(TIPO_PROCESSO_DOCUMENTO_VOTO);
		}
		return sessaoProcessoDocumentoVoto;
	}

	@Override
	public void setSessaoProcessoDocumento(SessaoProcessoDocumento sessaoProcessoDocumento) {
		this.sessaoProcessoDocumentoVoto = (SessaoProcessoDocumentoVoto) sessaoProcessoDocumento;
	}

	@Override
	public TipoVotoManager getTipoVotoManager() {
		return tipoVotoManager;
	}

}
