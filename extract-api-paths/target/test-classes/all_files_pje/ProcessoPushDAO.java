/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaPush;
import br.jus.pje.nucleo.entidades.ProcessoPush;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Componente de acesso a dados da entidade {@link ProcessoPush}.
 * 
 * @author cristof
 *
 */
@Name("processoPushDAO")
public class ProcessoPushDAO extends BaseDAO<ProcessoPush> {

	@Override
	public String getId(ProcessoPush p) {
		return p.getProcessoTrf().toString() + p.getPessoa().toString();
	}
	
	/**
	 * Método responsável por recuperar um {@link ProcessoPush} associado à {@link Pessoa}.
	 * 
	 * @param pessoa {@link Pessoa}.
	 * @param processoTrf {@link ProcessoPush}.
	 * @return {@link ProcessoPush} associado à {@link Pessoa}.
	 */
	public ProcessoPush recuperarProcessoPush(Pessoa pessoa, ProcessoTrf processoTrf) {
		return this.recuperarProcessoPush(pessoa, null, processoTrf);
	}
	
	/**
	 * Método responsável por recuperar um {@link ProcessoPush} associado à {@link pessoaPush}.
	 * 
	 * @param pessoaPush {@link pessoaPush}.
	 * @param processoTrf {@link ProcessoPush}.
	 * @return {@link ProcessoPush} associado à {@link pessoaPush}.
	 */
	public ProcessoPush recuperarProcessoPush(PessoaPush pessoaPush, ProcessoTrf processoTrf) {
		return this.recuperarProcessoPush(null, pessoaPush, processoTrf);
	}
	
	/**
	 * Método responsável por recuperar uma lista de {@link ProcessoPush} associados à {@link Pessoa}.
	 * 
	 * @param pessoa {@link Pessoa}.
	 * @param ativo Indica que o {@link ProcessoPush} foi (ou não foi) excluído.
	 * @return Lista de {@link ProcessoPush} associados à {@link Pessoa}.
	 */
	public List<ProcessoPush> recuperarProcessosPush(Pessoa pessoa, Boolean ativo) {
		return this.recuperarProcessosPush(pessoa, null, ativo);
	}
	
	/**
	 * Método responsável por recuperar uma lista de {@link ProcessoPush} associados à {@link PessoaPush}.
	 * 
	 * @param pessoaPush {@link PessoaPush}.
	 * @param ativo Indica que o {@link ProcessoPush} foi (ou não foi) excluído.
	 * @return Lista de {@link ProcessoPush} associados à {@link PessoaPush}.
	 */
	public List<ProcessoPush> recuperarProcessosPush(PessoaPush pessoaPush, Boolean ativo) {
		return this.recuperarProcessosPush(null, pessoaPush, ativo);
	}
	
	/**
	 * Método responsável por recuperar uma lista de {@link ProcessoPush}.
	 * 
	 * @param pessoa {@link Pessoa}.
	 * @param pessoaPush {@link PessoaPush}.
	 * @param ativo Indica que o {@link ProcessoPush} foi (ou não foi) excluído.
	 * @return Lista de {@link ProcessoPush}.
	 */
	@SuppressWarnings("unchecked")
	private List<ProcessoPush> recuperarProcessosPush(Pessoa pessoa, PessoaPush pessoaPush, Boolean ativo) {
		StringBuilder sql = new StringBuilder("SELECT o FROM ProcessoPush o WHERE (o.pessoa = :pessoa OR o.pessoaPush = :pessoaPush)");
		
		if (ativo != null) {
			sql.append(" AND o.dtExclusao " + (ativo ? "is" : "is not") + " null");
		}
		
		Query query = getEntityManager().createQuery(sql.toString());
		query.setParameter("pessoa", pessoa);
		query.setParameter("pessoaPush", pessoaPush);

		return query.getResultList();
	}
	
	/**
	 * Método responsável por recuperar um {@link ProcessoPush}.
	 * 
	 * @param pessoa {@link Pessoa}.
	 * @param pessoaPush {@link PessoaPush}.
	 * @param processoTrf {@link ProcessoTrf}.
	 * @return {@link ProcessoPush}.
	 */
	private ProcessoPush recuperarProcessoPush(Pessoa pessoa, PessoaPush pessoaPush, ProcessoTrf processoTrf) {
		Query query = getEntityManager().createQuery(
				"SELECT o FROM ProcessoPush o WHERE (o.pessoa = :pessoa OR o.pessoaPush = :pessoaPush) AND o.processoTrf = :processoTrf");
		
		query.setParameter("pessoa", pessoa);
		query.setParameter("pessoaPush", pessoaPush);
		query.setParameter("processoTrf", processoTrf);
		
		return (ProcessoPush) EntityUtil.getSingleResult(query);
	}

	public List<ProcessoPush> recuperarProcessoPushPorProcesso(ProcessoTrf processoTrf) {
		Query query = getEntityManager().createQuery(
				"SELECT o FROM ProcessoPush o WHERE o.processoTrf = :processoTrf and o.dtExclusao is null");
		
		query.setParameter("processoTrf", processoTrf);
		
		@SuppressWarnings("unchecked")
		List<ProcessoPush> processoPushList = query.getResultList();
		
		return processoPushList;
	}

}
