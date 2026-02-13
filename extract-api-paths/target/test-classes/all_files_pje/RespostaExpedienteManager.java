/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.RespostaExpedienteDAO;
import br.jus.pje.nucleo.entidades.RespostaExpediente;

/**
 * @author cristof
 *
 */
@Name("respostaExpedienteManager")
public class RespostaExpedienteManager extends BaseManager<RespostaExpediente> {
	
	@In
	private RespostaExpedienteDAO respostaExpedienteDAO;

	@Override
	protected BaseDAO<RespostaExpediente> getDAO() {
		return respostaExpedienteDAO;
	}

	/**
	 * Retorna a lista de objetos RespostaExpediente que possui, como documento,
	 * o id informado como parâmetro.
	 *
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-20734
	 * @param idProcessoDocumento
	 * @return List<RespostaExpediente>
	 */
	public List<RespostaExpediente> findByDocumento(int idProcessoDocumento){
		List<RespostaExpediente> lista = respostaExpedienteDAO.findByDocumento(idProcessoDocumento);
		return lista;
	}
}
