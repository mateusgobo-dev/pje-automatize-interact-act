package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.pje.dao.HistoricoEstatisticaEventoProcessoDAO;

/**
 * Classe que acessa o DAO e contem a regra de negocios referente a entidade de
 * EstatisticaEventoProcesso
 * 
 * @author Daniel
 * 
 */
@Name(HistoricoEstatisticaEventoProcessoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class HistoricoEstatisticaEventoProcessoManager extends GenericManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "historicoEstatisticaEventoProcessoManager";

	@In
	private HistoricoEstatisticaEventoProcessoDAO historicoEstatisticaEventoProcessoDAO;

	public List<Object[]> listSecaoNaoAtualizada() {
		return historicoEstatisticaEventoProcessoDAO.listSecaoNaoAtualizada();
	}

	public String getDataAtualizacaoSessao(String estado) {
		return historicoEstatisticaEventoProcessoDAO.getDataAtualizacaoSessao(estado);
	}
}