package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.SolicitacaoNoDesvio;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("solicitacaoNoDesvioDAO")
public class SolicitacaoNoDesvioDAO extends BaseDAO<SolicitacaoNoDesvio>{

	@Override
	public Object getId(SolicitacaoNoDesvio e) {
		return e.getIdSolicitacaoNoDesvio();
	}

	/**
	 * metodo responsavel por recuperar todas as solicitacoes para no de desvio da pessoa passada em parametro.
	 * @param _pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<SolicitacaoNoDesvio> recuperarSolicitacoesNoDesvio(Pessoa _pessoa) throws Exception {
		List<SolicitacaoNoDesvio> resultado = null;
		Search search = new Search(SolicitacaoNoDesvio.class);
		try {
			search.addCriteria(Criteria.equals("usuario.idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			resultado = list(search);
		} catch (EntityNotFoundException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ocorreu um erro ao tentar recuperar as solicitaçoes de nó de desvio da pessoa ");
			sb.append(_pessoa.getNome());
			sb.append(". Por favor, contacte o suporte do tribunal.");
			
			throw new Exception(sb.toString());
		}
		return resultado;
	}
}