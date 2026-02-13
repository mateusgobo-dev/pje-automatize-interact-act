package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.jboss.seam.annotations.Name;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.QuadroAviso;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("quadroAvisoDAO")
public class QuadroAvisoDAO extends BaseDAO<QuadroAviso>{

	@Override
	public Object getId(QuadroAviso e) {
		return e.getIdQuadroAviso();
	}

	public List<QuadroAviso> recuperarAvisosQuadroAviso(Pessoa _pessoa) throws Exception {
		List<QuadroAviso> resultado = null;
		Search search = new Search(QuadroAviso.class);
		try {
			search.addCriteria(Criteria.equals("usuarioInclusao.idUsuario", _pessoa.getIdPessoa()));			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			resultado = list(search);
		} catch (EntityNotFoundException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ocorreu um erro ao tentar recuperar os avisos do Quadro de Avisos da pessoa ");
			sb.append(_pessoa.getNome());
			sb.append(". Por favor, contacte o suporte do tribunal.");
			
			throw new Exception(sb.toString());
		}
		return resultado;
	}
}