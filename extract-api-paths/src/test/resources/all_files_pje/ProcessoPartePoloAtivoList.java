package br.com.jt.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoParte;

@Name(ProcessoPartePoloAtivoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoPartePoloAtivoList extends EntityList<ProcessoParte> {

	public static final String NAME = "processoPartePoloAtivoList";
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_EJBQL = "select ppa "+
												"from ProcessoParte ppa "+ 
												"where ppa.inParticipacao = 'A'  ";
	
	private static final String R1 = "ppa.processoTrf = #{votoAction.processoTrf} ";
	private static final String R2 = "ppa.processoTrf = #{secretarioSessaoJulgamentoAction.processoTrf} ";
	private static final String R3 = "ppa.processoTrf = #{magistradoSessaoJulgamentoAction.processoTrf} ";
	private static final String R4 = "ppa.processoTrf = #{procuradorSessaoJulgamentoAction.processoTrf} ";
	
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}


	@Override
	protected void addSearchFields() {
		addSearchField("processoTrf", SearchCriteria.igual, R1);
		addSearchField("processoTrfSecretario", SearchCriteria.igual, R2);
		addSearchField("processoTrfMagistrado", SearchCriteria.igual, R3);
		addSearchField("processoTrfProcurador", SearchCriteria.igual, R4);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("participante", "pessoa.nome");
		map.put("tipoParte", "tipoParte.tipoParte");
		return map;
	}


	@Override
	protected String getDefaultOrder() {
		// TODO Auto-generated method stub
		return null;
	}

}