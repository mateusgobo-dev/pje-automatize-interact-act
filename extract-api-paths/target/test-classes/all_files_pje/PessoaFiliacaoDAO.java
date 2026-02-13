package br.jus.cnj.pje.business.dao;

import java.util.List;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFiliacao;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(PessoaFiliacaoDAO.NAME)
public class PessoaFiliacaoDAO extends BaseDAO<PessoaFiliacao>{

	public static final String NAME = "pessoaFiliacaoDAO";
	
	@Override
	public Object getId(PessoaFiliacao e) {
		return e.getId();
	}

	public List<PessoaFiliacao> recuperaFiliacoesDaPessoa(Pessoa pessoa) {
		List<PessoaFiliacao> resultado = null;
		Search search = new Search(PessoaFiliacao.class);
		try {
			search.addCriteria(Criteria.equals("pessoaFisica.idUsuario", pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		resultado = list(search);
		return resultado;	
	}
	

}
