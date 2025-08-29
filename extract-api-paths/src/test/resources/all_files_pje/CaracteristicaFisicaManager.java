package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.CaracteristicaFisicaDAO;
import br.jus.pje.nucleo.entidades.CaracteristicaFisica;
import br.jus.pje.nucleo.entidades.Pessoa;

/**
 * 
 * @author luiz.mendes
 *
 */
@Name(CaracteristicaFisicaManager.NAME)
public class CaracteristicaFisicaManager extends BaseManager<CaracteristicaFisica>{
	
	public static final String NAME = "caracteristicaFisicaManager";
	
	@In
	private CaracteristicaFisicaDAO caracteristicaFisicaDAO;

	@Override
	protected CaracteristicaFisicaDAO getDAO() {
		return caracteristicaFisicaDAO;
	}
	
	/**
	 * metodo responsavel por recuperar todas as caracteristicas fisicas da pessoa passada em parametro
	 * @param _pessoa
	 * @return
	 */
	public List<CaracteristicaFisica> recuperaCaracteristicasFisicas(Pessoa _pessoa) {
		return caracteristicaFisicaDAO.recuperaCaracteristicaFisica(_pessoa);
	}
}