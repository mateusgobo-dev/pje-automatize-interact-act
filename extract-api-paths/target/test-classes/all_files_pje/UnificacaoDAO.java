package br.jus.cnj.pje.business.dao;


import javax.persistence.EntityManager;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Unificacao;

@Name(UnificacaoDAO.NAME)
public class UnificacaoDAO extends BaseDAO<Unificacao>{

	public static final String NAME = "unificacaoDAO";

	@Override
	public Object getId(Unificacao e) {
		return e.getIdUnificacao();
	}
	
	/**
	 * Metodo responsavel por salvar a unificacao e suas dependencias
	 * @param unificacao
	 * @throws Exception
	 */
	public void persisteAlteracoesUnificacao(Unificacao unificacao) throws Exception{
		EntityManager em = EntityUtil.getEntityManager();
		if(em.contains(unificacao)) {
			em.merge(unificacao);
		} else {
			em.persist(unificacao);
		}
		em.flush();
	}
}