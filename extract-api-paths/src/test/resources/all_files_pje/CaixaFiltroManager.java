package br.com.infox.pje.manager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.pje.dao.CaixaFiltroDAO;
import br.jus.pje.nucleo.entidades.CaixaFiltro;
import br.jus.pje.nucleo.entidades.Tarefa;

/**
 * Classe manager para CaixaFiltro
 * 
 * @author Infox
 * 
 */
@Name(CaixaFiltroManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class CaixaFiltroManager extends GenericManager {

	public static final String NAME = "caixaFiltroManager";

	@In
	private CaixaFiltroDAO caixaFiltroDAO;

	/**
	 * Verifica se existe alguma caixa através dos parametros informados.
	 * 
	 * @param nomeCaixa
	 *            nome da caixa
	 * @param t
	 *            tarefa
	 * @return caixaFiltro
	 */
	public CaixaFiltro existsCaixaByNomeAndTarefa(String nomeCaixa, Tarefa t) {
		CaixaFiltro result = caixaFiltroDAO.countCaixaByNomeAndTarefa(nomeCaixa, t);
		return result;
	}

}