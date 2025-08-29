/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import br.com.itx.util.EntityUtil;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.Procuradoria;

/**
 * Componente de acesso aos dados pertinentes à entidade {@link PessoaProcurador}.
 * 
 * @author cristof
 *
 */
@Name("procuradorDAO")
public class ProcuradorDAO extends BaseDAO<PessoaProcurador> {

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(PessoaProcurador e) {
		return e.getIdUsuario();
	}
	
	/**
	 * Inclui um perfil de procurador na pessoa física dada.
	 * 
	 * @param pessoa a pessoa física a quem se pretende dar o perfil de procurador
	 * @return a {@link PessoaProcurador} vinculada à pessoa física indicada.
	 */
	public PessoaProcurador especializa(PessoaFisica pessoa){
		if(!entityManager.contains(pessoa)){
			entityManager.persist(pessoa);
		}
		entityManager.flush();
		String query = "INSERT INTO tb_pessoa_procurador (id,in_procurador_mp_sessao) VALUES (?1,false)";
		Query q = EntityUtil.createNativeQuery(entityManager, query, "tb_pessoa_procurador");
		q.setParameter(1, pessoa.getIdUsuario());
		if(q.executeUpdate() > 0) {
			return entityManager.find(PessoaProcurador.class, pessoa.getIdUsuario());
		} else {
			return null;
		}
	}
	
	/**
	 * Suprime de uma pessoa física o perfil de procurador
	 * @param pessoa
	 * @return
	 */
	public PessoaProcurador desespecializa(PessoaFisica pessoa){
		PessoaProcurador pro = null;
		pro = (PessoaProcurador)entityManager.find(PessoaProcurador.class, pessoa.getIdPessoa());
		if(pro != null){
			pro.getPessoa().suprimePessoaEspecializada(pro);
			entityManager.flush();
			return pro;
		}
		
		return null;
	}	
	
	/**
	 * Recupera a lista de pessoas representadas por um procurador vinculado a uma dada procuradoria.
	 * 
	 * @param procuradoria a procuradoria a que está vinculado o procurador
	 * @param procurador o procurador a respeito de quem se pretende identificar as pessoas representadas
	 * @return a lista de pessoas representadas.
	 */
	@SuppressWarnings("unchecked")
	public List<Pessoa> getPessoasRepresentadas(Procuradoria procuradoria, Pessoa procurador){
		String query = "SELECT DISTINCT ppp.pessoaProcuradoriaEntidade.pessoa " +
				"	FROM PessoaProcuradorProcuradoria AS ppp " +
				"	WHERE ppp.pessoaProcurador.id = :idProcurador " +
				"	AND ppp.pessoaProcuradoriaEntidade.procuradoria = :procuradoria";
		Query q = entityManager.createQuery(query);
		q.setParameter("idProcurador", ((PessoaFisica) procurador).getPessoaProcurador().getIdUsuario());
		q.setParameter("procuradoria", procuradoria);
		return (List<Pessoa>) q.getResultList();
	}
	
}
