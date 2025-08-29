package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.jboss.seam.annotations.Name;
import br.jus.pje.nucleo.entidades.ModeloProclamacaoJulgamento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("modeloProclamacaoJulgamentoDAO")
public class ModeloProclamacaoJulgamentoDAO extends BaseDAO<ModeloProclamacaoJulgamento>{

	@Override
	public Object getId(ModeloProclamacaoJulgamento e) {
		return e.getId();
	}

	/**
	 * metodo responsavel por recuperar todas os modelos de proclamacao de julgamento cadastrados pela pessoa passda em parametro.
	 * @param _pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<ModeloProclamacaoJulgamento> recuperarModelos(Pessoa _pessoa) throws Exception {
		List<ModeloProclamacaoJulgamento> resultado = null;
		Search search = new Search(ModeloProclamacaoJulgamento.class);
		try {
			search.addCriteria(Criteria.equals("usuario.idUsuario", _pessoa.getIdPessoa()));			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			resultado = list(search);
		} catch (EntityNotFoundException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ocorreu um erro ao tentar recuperar os modelos de proclamação de julgamento da pessoa ");
			sb.append(_pessoa.getNome());
			sb.append(". Por favor, contacte o suporte do tribunal.");
			
			throw new Exception(sb.toString());
		}
		return resultado;
	}
}