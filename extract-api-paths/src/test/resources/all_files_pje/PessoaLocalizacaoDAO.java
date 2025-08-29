/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaLocalizacao;

/**
 * Componente de acesso a dados da entidade {@link PessoaLocalizacao}.
 * 
 * @author cristof
 *
 */
@Name("pessoaLocalizacaoDAO")
public class PessoaLocalizacaoDAO extends BaseDAO<PessoaLocalizacao> {

	@Override
	public Object getId(PessoaLocalizacao e) {
		return e.getIdPessoaLocalizacao();
	}

	public PessoaLocalizacao recuperaUnivoca(Pessoa p, Localizacao l) {
		String query = "SELECT pl FROM PessoaLocalizacao AS pl WHERE pl.pessoa = :pessoa AND pl.localizacao = :localizacao";
		Query q = entityManager.createQuery(query);
		q.setParameter("pessoa", p);
		q.setParameter("localizacao", l);
		try {
			return (PessoaLocalizacao) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
}
