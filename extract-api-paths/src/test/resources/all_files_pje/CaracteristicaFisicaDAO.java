package br.jus.cnj.pje.business.dao;


import java.util.List;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.CaracteristicaFisica;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * 
 * @author luiz.mendes
 *
 */
@Name(CaracteristicaFisicaDAO.NAME)
public class CaracteristicaFisicaDAO extends BaseDAO<CaracteristicaFisica>{

	public static final String NAME = "caracteristicaFisicaDAO";

	@Override
	public Object getId(CaracteristicaFisica e) {
		return e.getId();
	}
	
	/**
	 * recupera todos as caracteristicas fisicas da pessoa passada em parametro
	 * @param _pessoa
	 * @return
	 */
	public List<CaracteristicaFisica> recuperaCaracteristicaFisica(Pessoa _pessoa) {
		List<CaracteristicaFisica> resultado = null;
		Search search = new Search(CaracteristicaFisica.class);
		try {
			search.addCriteria(Criteria.equals("pessoaFisica.idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		resultado = list(search);
		return resultado;
	}
}