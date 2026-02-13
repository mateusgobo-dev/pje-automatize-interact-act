package br.jus.cnj.pje.nucleo.service;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;

import br.jus.cnj.pje.business.dao.TipoProcessoDocumentoPapelDAO;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name("tipoProcessoDocumentoPapelService")
@Scope(ScopeType.EVENT)
@Transactional
public class TipoProcessoDocumentoPapelService {

	@In(create = true)
	private TipoProcessoDocumentoPapelDAO tipoProcessoDocumentoPapelDAO;
	
	/**
	 * Metodo responsvel por verificar se o tipo de documento e com o papel atual precisam assinar os documentos.
	 * 
	 * @param tipoProcessoDocumento
	 * @return Boolean
	 */
	public boolean verificarExigibilidadeNaoAssina(Papel papel, TipoProcessoDocumento tipoProcessoDocumento) {
		return this.tipoProcessoDocumentoPapelDAO.verificarExigibilidadeNaoAssina(papel, tipoProcessoDocumento);
	}
	
	public boolean verificarExigibilidadeAssina(Papel papel, TipoProcessoDocumento tipoProcessoDocumento) {
		return this.tipoProcessoDocumentoPapelDAO.verificarExigibilidadeAssina(papel, tipoProcessoDocumento);
	}

}