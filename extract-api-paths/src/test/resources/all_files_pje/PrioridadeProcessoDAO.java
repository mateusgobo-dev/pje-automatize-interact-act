/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ProjetoUtil;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Componente de acesso a dados da entidade {@link PrioridadeProcesso}.
 * 
 * @author cristof
 * @since 1.4.6.2.RC4
 */
@Name("prioridadeProcessoDAO")
public class PrioridadeProcessoDAO extends BaseDAO<PrioridadeProcesso> {

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(PrioridadeProcesso p) {
		return p.getIdPrioridadeProcesso();
	}

	/**
	 * Recupera o PrioridadeProcesso pelo ID.
	 * 
	 * @param id Identificador.
	 * @return PrioridadeProcesso
	 */
	public PrioridadeProcesso findById(String id) {
		PrioridadeProcesso resultado = null;
		
		if (StringUtils.isNotBlank(id)) {
			resultado = getEntityManager().find(PrioridadeProcesso.class, new Integer(id));
		}
		return resultado;
	}

	/**
	 * Recupera o PrioridadeProcesso pela descrição.
	 * 
	 * @param descricao Descrição da prioridade.
	 * @return PrioridadeProcesso
	 */
	@SuppressWarnings("unchecked")
	public PrioridadeProcesso findByDescricao(String descricao) {
		PrioridadeProcesso resultado = null;
		
		if (StringUtils.isNotBlank(descricao)) {
			String hql = "from PrioridadeProcesso pp where upper(to_ascii(pp.prioridade)) = :prioridade";
			Query query = getEntityManager().createQuery(hql);
			query.setParameter("prioridade", StringUtil.getUsAscii(descricao.toUpperCase()));
			
			List<PrioridadeProcesso> prioridades = query.getResultList();
			resultado = ProjetoUtil.isNotVazio(prioridades) ? prioridades.get(0) : null;
		}
		return resultado;
	}
}
