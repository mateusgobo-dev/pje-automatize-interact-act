package br.com.infox.bpm.taskPage.remessacnj;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRequisicaoDTO;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;


/**
 * Classe responsável por remeter de manifestações processuais ao STF. 
 * Caso tenha algum problema ou dúvida, favor
 * encaminhar um e-mail para G_INTEGRACAO@stf.jus.br. 
 * 
 * Endpoint Homologação :https://wsh.stf.jus.br/servico-intercomunicacao-2.2/intercomunicacao?wsdl 
 *
 * Manual Envio, Reenvio, Envio Complementar: http://www.stf.jus.br/arquivo/cms/processoIntegracaoInformacaoTecnica/anexo/Documentacao_Tecnica__WS_Operacao_Envio_e_Reenvio_de_Processos__Envio_complementar_de_pecas.pdf
 *
 * Códigos para De-Paras de Peçaas Processuais:
 * http://www.stf.jus.br/ARQUIVO/NORMA/RESOLUCAO490-2012.PDF
 * 
 * @author rodrigoar
 *
 */
@Name(RemeterManifestacaoProcessualSTFTaskPageAction.NAME)
@Scope(ScopeType.EVENT)
public class RemeterManifestacaoProcessualSTFTaskPageAction extends
		RemeterManifestacaoProcessualTaskPageAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "remeterManifestacaoProcessualTaskPageAction";
	private static final String NUMERO_STF = "100";

	private int tamanhoProcesso;

	@Override
	protected String getWsdl() {
		return parametroService.valueOf(Parametros.WSDL_REMESSA_STF);
	}
	
	@Override
	protected String getDestino() {
		return "Supremo Tribunal Federal";
	}

	/**
	 * Identificador do tribunal consultante. Campo do tipo string;
	 */
	@Override
	protected String getIdManifestante() {
		return parametroService.valueOf(Parametros.ID_MANIFESTANTE_REMESSA_STF);
	}

	/**
	 * Senha previamente cadastrada pela Secretaria Judiciária do STF. Campo do
	 * tipo string;
	 */
	@Override
	protected String getSenhaManifestante() {
		return parametroService
				.valueOf(Parametros.SENHA_MANIFESTANTE_REMESSA_STF);
	}

	/**
	 * Montar documentos de acordo com o manual do STF
	 */
	protected void montarDocumentos(
			ManifestacaoProcessualRequisicaoDTO manifestacaoProcessual,
			ManifestacaoProcessualMetaData remessaManifestacaoProcessual)
			throws Exception {
		for (ProcessoDocumento documentoPJE : remessaManifestacaoProcessual
				.getDocumentos()) {
			if (((ProcessoDocumentoRemessa) documentoPJE).getSelecionado()) {
				ProcessoDocumentoRemessa pdr = (ProcessoDocumentoRemessa) documentoPJE;
				TipoProcessoDocumento tipoProcessoDocumento = pdr.getTipoDocumentoRemessa();
				tipoProcessoDocumento.setCodigoDocumento(tipoProcessoDocumento.getCodigoDocumento().replace(NUMERO_STF, ""));
			}
		}

		// Volume, em bytes, dos documentos existentes no processo judicial.
		manifestacaoProcessual.setTamanhoProcesso(
				tamanhoProcesso);
	}

}
