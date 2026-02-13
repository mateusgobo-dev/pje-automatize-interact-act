package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.pje.dao.SecaoJudiciariaDAO;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;

/**
 * Classe que acessa o DAO e contem a regra de negocios referente a entidade de
 * EstatisticaEventoProcesso
 * 
 * @author Daniel
 * 
 */
@Name(SecaoJudiciariaManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class SecaoJudiciariaManager extends GenericManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "secaoJudiciariaManager";

	@In
	private SecaoJudiciariaDAO secaoJudiciariaDAO;

	public List<SecaoJudiciaria> secaoJudiciariaItems() {
		return secaoJudiciariaDAO.secaoJudiciariaItems();
	}

	public SecaoJudiciaria secaoJudiciaria1Grau() {
		return secaoJudiciariaDAO.secaoJudiciaria1Grau();
	}

	public List<SecaoJudiciaria> listSecaoJudiciaria1Grau() {
		return secaoJudiciariaDAO.listSecaoJudiciaria1Grau();
	}
}