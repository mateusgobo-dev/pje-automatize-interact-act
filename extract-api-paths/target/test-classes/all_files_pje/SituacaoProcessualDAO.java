/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.SituacaoProcessual;

/**
 * Componente de acesso a dados da entidade {@link SituacaoProcessual}
 * @author cristof
 *
 */
@Name("situacaoProcessualDAO")
public class SituacaoProcessualDAO extends BaseDAO<SituacaoProcessual> {

	@Override
	public Long getId(SituacaoProcessual s) {
		return s.getId();
	}

}
