package br.jus.csjt.pje.business.service;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.BaseService;
import br.jus.csjt.pje.view.action.TipoProcessoDocumentoAction;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

@Name(TipoProcessoDocumentoService.NAME)
public class TipoProcessoDocumentoService extends BaseService {
	
	public static final String NAME = "tipoProcessoDocumentoService"; 
	
	
	/**
	 * Se estiverem definidos os tipos de documento no fluxo, os retorna.
	 * <br/><br/>
	 * Senão, retorna todos os tipos de documentos ativos.
	 * 
	 * @param variavel
	 *            Nome do campo de formulário do fluxo. Ex: Minuta
	 * @return Lista de tipos de documentos, de acordo com o comportamento
	 *         descrito acima.
	 * @see TipoProcessoDocumentoAction#set(String, int...) Método que define os tipos de documentos no fluxo
	 */
	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumento> getTipoDocumentoItems(String variavel) {
		String[] tokens = variavel.split("-");
		String listaTipos = (String) TaskInstanceUtil.instance().getVariable(tokens[0] 
				+ TipoProcessoDocumentoAction.SUFIXO_VARIAVEL_TIPO_DOCUMENTO);
		List<TipoProcessoDocumento> list = null;
		
		EntityManager em = EntityUtil.getEntityManager();
		
		if (listaTipos != null) {
			list = em.createQuery(
					"select o from TipoProcessoDocumento o where o.ativo = true "
							+ "and o.idTipoProcessoDocumento in (" + listaTipos + ") order by tipoProcessoDocumento")
					.getResultList();
		} else {
			list = em.createQuery(
					"select o from TipoProcessoDocumento o where o.ativo = true " + "order by tipoProcessoDocumento")
					.getResultList();

		}
		return list;
	}
	
}