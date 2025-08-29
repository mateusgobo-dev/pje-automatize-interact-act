package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.CentralMandadoLocalizacaoDAO;
import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.CentralMandadoLocalizacao;

@Name("centralMandadoLocalizacaoManager")
public class CentralMandadoLocalizacaoManager extends BaseDAO<CentralMandadoLocalizacao> {

	@In
	CentralMandadoLocalizacaoDAO centralMandadoLocalizacaoDAO;
	
	@Override
	public Object getId(CentralMandadoLocalizacao e) {
		return centralMandadoLocalizacaoDAO.getId(e);
	}
	
	/**
	 * Retorna lista das localizações cadastradas para a central de mandado informada
	 * @param centralMandado
	 * @return
	 */
	public List<CentralMandadoLocalizacao> findByCentralMandado(CentralMandado centralMandado) {
		return centralMandadoLocalizacaoDAO.findByCentralMandado(centralMandado);
	}
}
