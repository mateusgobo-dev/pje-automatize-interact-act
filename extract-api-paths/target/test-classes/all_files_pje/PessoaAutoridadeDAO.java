package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(PessoaAutoridadeDAO.NAME)
public class PessoaAutoridadeDAO extends AbstractUsuarioDAO<PessoaAutoridade>{

	public static final String NAME = "pessoaAutoridadeDAO";

	@Override
	public Object getId(PessoaAutoridade e) {
		return e.getIdPessoa();
	}
	
	/**
	 * Recupera as autoridades vinculadas ao orgaoVinculacao
	 * @param orgaoVinculacao
	 * @return
	 */
	public List<PessoaAutoridade> findByOrgaoVinculacao(Pessoa orgaoVinculacao) {
		List<PessoaAutoridade> resultado = null;
		try {
			Search search = new Search(PessoaAutoridade.class);
			search.addCriteria(Criteria.equals("orgaoVinculacao.idUsuario", orgaoVinculacao.getIdUsuario()));
			search.addCriteria(Criteria.equals("ativo", Boolean.TRUE));
			resultado = list(search);
		} catch (NoSuchFieldException e) {
			throw PJeDAOExceptionFactory.getDaoException(e);
		}
		return resultado;
	}
	
	/**
  	 * metodo responsavel por recuperar a pessoa autoridade pela id.
  	 * 
  	 * caso nao encontre a pessoa autoridade no banco, nao lança excessao e retorna null.
  	 * 
  	 * @param idPessoaJuridica
  	 * @return 
  	 */
  	public PessoaAutoridade recuperaPessoaAutoridadePelaID(Integer idPessoaAutoridade){
  		PessoaAutoridade resultado = null;
  		Search search = new Search(PessoaAutoridade.class);
		try {
			search.addCriteria(Criteria.equals("idPessoaAutoridade", idPessoaAutoridade));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		if(list(search) != null && !list(search).isEmpty()){
			resultado = (PessoaAutoridade) list(search).get(0);
		}
  		return resultado;
  	}

	/**
	 * Filtra os PessoaAutoridade de acordo com o nome informado.
	 * 
	 * @param nome String nome a ser filtrado.
	 * @return List<PessoaAutoridade> lista com os PessoaAutoridade.
	 */
	@SuppressWarnings("unchecked")
	public List<PessoaAutoridade> filtrarPessoaAutoridade(String nome) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaAutoridade o where ");
		sb.append("o.ativo = true and ");
		sb.append("lower(TO_ASCII(o.nome)) like lower(concat('%',TO_ASCII('");
		sb.append(nome);
		sb.append("'), '%')) order by o.nome");
		Query query = entityManager.createQuery(sb.toString());
		return (List<PessoaAutoridade>)query.getResultList();
	}
	
}
	