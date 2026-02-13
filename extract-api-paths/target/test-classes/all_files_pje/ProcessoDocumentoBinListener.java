package br.jus.cnj.pje.entidades.listeners;

import javax.persistence.FlushModeType;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.util.Crypto;

/**
 * Classe Listener para os eventos ocorridos com o objeto ProcessoDocumentoBin no sistema. 
 */
public class ProcessoDocumentoBinListener {

	/**
	 * Antes de persistir um documento, faz o devido tratamento afim de evitar que o conte�do nulo de um documento que 
	 * n�o seja bin�rio seja persistido.
	 * @param processoDocumentoBin	documento a ser tratado
	 */
	public void prePersist(ProcessoDocumentoBin processoDocumentoBin){
		inicializarConteudoDocumentoNulo(processoDocumentoBin);
		atualizarMD5Documento(processoDocumentoBin);
	}

	/**
	 * Antes de atualizar um documento, faz o devido tratamento afim de evitar que o conte�do nulo de um documento que 
	 * n�o seja bin�rio seja persistido.
	 * @param processoDocumentoBin	documento a ser tratado
	 */
	public void preUpdate(ProcessoDocumentoBin processoDocumentoBin) throws PJeBusinessException {
		verificarAlteracaoConteudoDocumentoAssinado(processoDocumentoBin);
		inicializarConteudoDocumentoNulo(processoDocumentoBin);
		atualizarMD5Documento(processoDocumentoBin);
	}
	
	private void atualizarMD5Documento(ProcessoDocumentoBin processoDocumentoBin) {
		if (processoDocumentoBin != null && processoDocumentoBin.getModeloDocumento() != null){
			String md5Calculado = 	Crypto.encodeMD5(processoDocumentoBin.getModeloDocumento());
			if(!md5Calculado.equals(processoDocumentoBin.getMd5Documento()))
			{
				processoDocumentoBin.setMd5Documento(md5Calculado);
			}
		}	
	}

	/**
	 * M�todo respons�vel por evitar a altera��o do conte�do de um documento que consta como assinado.
	 * 
	 * @param processoDocumentoBin {@link ProcessoDocumentoBin}
	 * @throws PJeBusinessException Caso haja tentativa de altera��o do conte�do de um documento assinado � lan�ada a exce��o.
	 */
	private void verificarAlteracaoConteudoDocumentoAssinado(ProcessoDocumentoBin processoDocumentoBin) throws PJeBusinessException {
		if (processoDocumentoBin != null && !processoDocumentoBin.isBinario()) {
			/* Esta query precisa ser nativa para que o hibernate n�o utilize a entidade gerenciada e 
			 * sim verifique no banco qual � o conte�do anterior � alteracao. */
			Query query = EntityUtil.createNativeQuery("select ds_md5_documento "
					+ "from tb_processo_documento_bin "
					+ "where id_processo_documento_bin = :id and "
					+ "exists (select 1 from tb_proc_doc_bin_pess_assin a where a.id_processo_documento_bin = :id) "
					, "");

			query.setParameter("id", processoDocumentoBin.getIdProcessoDocumentoBin());
			query.setFlushMode(FlushModeType.COMMIT);

			String hashConteudoDocumento = (String) EntityUtil.getSingleResult(query); //  Conte�do do documento antes de ser alterado.
			
			if (StringUtils.isNotBlank(hashConteudoDocumento) && !hashConteudoDocumento.equals(Crypto.encodeMD5(processoDocumentoBin.getModeloDocumento()))) {
				throw new PJeBusinessException(String.format("DocumentoBin (%d) consta como assinado e n�o pode ser alterado.", processoDocumentoBin.getIdProcessoDocumentoBin()));
			}
		}
	}
	
	/**
	 * Dado um documento, seta um conte�do vazio (espa�o em branco) para o mesmo caso ele 
	 * esteja nulo e n�o seja um documento bin�rio.
	 * @param processoDocumentoBin documento a ser tratado. 
	 */
	private void inicializarConteudoDocumentoNulo(ProcessoDocumentoBin processoDocumentoBin){
		if (processoDocumentoBin != null && processoDocumentoBin.getModeloDocumento() == null){
			if (!processoDocumentoBin.isBinario()){
				processoDocumentoBin.setModeloDocumento(" ");
			}	
		}
	}
}