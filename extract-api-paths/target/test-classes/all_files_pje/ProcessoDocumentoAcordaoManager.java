package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.ProcessoDocumentoAcordaoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoAcordao;

/**
 * Classe que realiza as operações de negócio para a entidade ProcessoDocumentoAcordao
 * [PJEII-21041]
 * @author Thiago Nascimento Figueiredo
 *
 */
@Name(ProcessoDocumentoAcordaoManager.NAME)
public class ProcessoDocumentoAcordaoManager extends BaseManager<ProcessoDocumentoAcordao> {

	public static final String NAME = "processoDocumentoAcordaoManager";
	
	@In
	private ProcessoDocumentoAcordaoDAO processoDocumentoAcordaoDAO;
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected ProcessoDocumentoAcordaoDAO getDAO() {
		return this.processoDocumentoAcordaoDAO;
	}

	/**
	 * Recupera a lista de documentos que estão associados a um processo para participar da composição do acórdão.
	 * @param idProcesso
	 * @return lista de documentos
	 */
	public List<ProcessoDocumentoAcordao> recuperarDocumentosParaAcordaoEmAberto(Integer idProcesso){
		return getDAO().recuperarDocumentosParaAcordaoEmAberto(idProcesso);
	}
	
	/**
	 * Operação que recebe uma lista de documentos e vincula eles a um processo para tratar a composição do
	 * acórdão.
	 * Caso existam documentos para composição do acórdão em aberto, esses serão desconsiderados para gravar 
	 * a nova relação de documentos.
	 * @param documentos
	 * @throws PJeBusinessException 
	 */
	@SuppressWarnings({ "boxing"})
	public void gravarDocumentosComposicaoAcordao(List<ProcessoDocumento> documentos) throws PJeBusinessException{
		
		Integer idProcesso;
		if(documentos != null && !documentos.isEmpty()){
			
			idProcesso = documentos.get(0).getProcesso().getIdProcesso();
			List<ProcessoDocumentoAcordao> documentosEmAberto = recuperarDocumentosParaAcordaoEmAberto(idProcesso);
			for (ProcessoDocumentoAcordao processoDocumentoAcordao : documentosEmAberto) {
				remove(processoDocumentoAcordao);
			}
			
			ProcessoDocumentoAcordao processoDocumentoAcordao;
			for (int indice = 0; indice < documentos.size(); indice++) {
				processoDocumentoAcordao = new ProcessoDocumentoAcordao();
				processoDocumentoAcordao.setProcessoDocumento(documentos.get(indice));
				processoDocumentoAcordao.setOrdemDocumento(indice);
				persistAndFlush(processoDocumentoAcordao);
			}
			
		}
		
	}
	
	/**
	 * Para um determinado processo, fecha lista existente de documentos em aberto e que poderiam participar
	 * da composição de um acórdão.
	 * 
	 * @param idProcesso - processo do qual se quer recuperar os documentos em aberto.
	 * @param pdAcordao - acórdão que será utilizado para fechar a relação de docuemntos em aberto.
	 * @throws PJeBusinessException
	 */
	public void fecharDocumentosParaAcordaoEmAberto(Integer idProcesso, ProcessoDocumento pdAcordao) throws PJeBusinessException{
		
		List<ProcessoDocumentoAcordao> documentosEmAberto = recuperarDocumentosParaAcordaoEmAberto(idProcesso);
		
		for (ProcessoDocumentoAcordao processoDocumentoAcordao : documentosEmAberto) {
			processoDocumentoAcordao.setProcessoDocumentoAcordao(pdAcordao);
			processoDocumentoAcordao.setDataJuntadaAcordao(pdAcordao.getDataJuntada());
			persistAndFlush(processoDocumentoAcordao);
		}
		
	}
	
	/**
	 * Ao passar um id de um ProcessoDocumento ira apagar todos os processos de elaboracao de acordao que tenham este
	 * ProcessoDocumento vinculado.
	 * 
	 * @param idProcessoDocumento id do ProcessoDocumento vinculado.
	 */
	public void remover(Integer idProcessoDocumento){
		getDAO().remover(idProcessoDocumento);
	}
}
