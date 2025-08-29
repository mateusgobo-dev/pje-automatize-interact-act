package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.ProcessoHome;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ProcessoDocumentoAcessoExternoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoDocumentoAcessoExternoList extends EntityList<ProcessoDocumento> {

	public static final String NAME = "processoDocumentoAcessoExternoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL =	
			"select o from ProcessoDocumento o "
			+ "where o.dataJuntada is not null "
			+ "and o.documentoSigiloso is false "
			+ "and o.ativo is true "
			+ "and o.processoTrf.segredoJustica is false "
			+ "and not exists "
			+ " ( "
			+ "   select 1 "
			+ "   from ProcessoExpediente pe "
			+ "   where pe.processoDocumento = o "
			+ "   and pe.documentoExistente = true  "
			+ " ) ";
	private static final String DEFAULT_ORDER = "to_char(o.dataInclusao, 'yyyy-mm-dd') desc";

	private static final String R1 = "o.processo.idProcesso = #{processoTrfHome.instance.idProcessoTrf} ";

	@Override
	protected void addSearchFields() {
		addSearchField("processo", SearchCriteria.igual, R1);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		String sql = DEFAULT_EJBQL;
		if (!verificaClasseJudicialPublica()){
			sql += " and o.tipoProcessoDocumento.publico = true ";
		}
		return sql;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}
	
	/**
	 * Metodo responsavel por verificar se a classe judicial do processo permite visualizar os 
	 * documentos com tipo não publico na consulta publica de processos
	 */
	private boolean verificaClasseJudicialPublica(){
		boolean retorno = false;
		ProcessoHome processoHome = (ProcessoHome) Component.getInstance("processoHome");
		
		ProcessoTrf processoTrf = getEntityManager().find(ProcessoTrf.class, processoHome.getInstance().getIdProcesso());
		if (processoTrf!=null && processoTrf.getClasseJudicial()!=null && processoTrf.getClasseJudicial().getPublico()){
			retorno = true;
		}
		return retorno;
	}

}
