package br.jus.cnj.pje.business.dao;

import java.util.List;
import org.jboss.seam.annotations.Name;
import br.jus.pje.nucleo.entidades.MeioContato;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("meioContatoDAO")
public class MeioContatoDAO extends BaseDAO<MeioContato>{

	@Override
	public Object getId(MeioContato e) {
		return e.getIdMeioContato();
	}

	/**
	 * recupera todos os meios de contato cadastrados pela pessoa passada em parametro
	 * @param _pessoa
	 * @return
	 */
	public List<MeioContato> recuperaMeioContatoCadastrados(Pessoa _pessoa) {
		List<MeioContato> resultado = null;
		Search search = new Search(MeioContato.class);
		try {
			search.addCriteria(Criteria.equals("usuarioCadastrador.idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		resultado = list(search);
		return resultado;
	}

	/**
	 * recupera todos os meios de contato onde a pessoa passada em parametro é a proprietária
	 * @param _pessoa
	 * @return
	 */
	public List<MeioContato> recuperaMeioContatoProprietarios(Pessoa _pessoa) {
		List<MeioContato> resultado = null;
		Search search = new Search(MeioContato.class);
		try {
			search.addCriteria(Criteria.equals("pessoa.idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		resultado = list(search);
		return resultado;
	}
}