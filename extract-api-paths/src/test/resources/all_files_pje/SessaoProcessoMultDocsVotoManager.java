package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.SessaoProcessoMultDocsVotoDAO;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.SessaoProcessoMultDocsVoto;

/**
 * Classe responsável por gerenciar a entidade SessaoProcessoMultDocsVoto.
 * 
 * @author carlos
 */
@Name("sessaoProcessoMultDocsVotoManager")
public class SessaoProcessoMultDocsVotoManager extends BaseManager<SessaoProcessoMultDocsVoto>{

	@In
	private SessaoProcessoMultDocsVotoDAO sessaoProcessoMultDocsVotoDAO;
	
	@Override
	protected SessaoProcessoMultDocsVotoDAO getDAO() {
		return sessaoProcessoMultDocsVotoDAO;
	}

	/**
	 * Recupera o objeto {@link SessaoProcessoMultDocsVoto} de acordo com o argumento informado.
	 * 
	 * @param procDoc {@link ProcessoDocumento}
	 * @return O objeto {@link SessaoProcessoMultDocsVoto} de acordo com o argumento informado.
	 */
	public SessaoProcessoMultDocsVoto recuperarSessaoProcessoDoc(ProcessoDocumento procDoc) {
		return getDAO().recuperarSessaoProcessoDoc(procDoc);
	}

	/**
	 * Recupera o próximo número da ordem.
	 * 
	 * @param voto {@link SessaoProcessoDocumentoVoto}.
	 * @return O próximo número da ordem. 
	 */
	public Integer recuperarProximoNumeroOrdemDoc(SessaoProcessoDocumentoVoto voto) {
		return getDAO().recuperarUltimoNumeroOrdemDoc(voto) + 1;
	}

	/**
	 * Recupera o último documento do voto.
	 * 
	 * @param sessaoProcessoDocumentoVoto {@link SessaoProcessoDocumentoVoto}
	 * @return O último documento do voto.
	 */
	public SessaoProcessoMultDocsVoto recuperarUltimoDoc(SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto) {
		return getDAO().recuperarUltimoDoc(sessaoProcessoDocumentoVoto);
	}

	/**
	 * Recupera todos os documentos do voto.
	 * 
	 * @param sessaoProcessoDocumentoVoto {@link SessaoProcessoDocumentoVoto}
	 * @return Todos os documentos do voto.
	 */
	public List<SessaoProcessoMultDocsVoto> recuperarDocsVoto(SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto) {
		return getDAO().recuperarDocsVoto(sessaoProcessoDocumentoVoto);
	}
	
	/**
	 * Ao passar um id de um ProcessoDocumento ira apagar todos os SessaoProcessoMultDocsVoto que tenham este
	 * ProcessoDocumento vinculado.
	 * 
	 * @param idProcessoDocumento id do ProcessoDocumento vinculado.
	 */
	public void remover(Integer idProcessoDocumento){
		getDAO().remover(idProcessoDocumento);
	}
}
