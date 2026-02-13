package br.com.jt.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.security.Identity;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;
import br.jus.pje.jt.entidades.SessaoJT;

@Name(PainelSessaoJulgamentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PainelSessaoJulgamentoList extends EntityList<SessaoJT> {

	public static final String NAME = "painelSessaoJulgamentoList";
	
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL_JT = "select o from SessaoJT o ";
	private static final String DEFAULT_EJBQL = "select o from Sessao o ";

	private static final String DEFAULT_ORDER = "idSessao";
	
	private static final String R1 = "cast(o.dataSessao as date) = #{agendaSessaoJT.currentDate}";
	private static final String R2 = "o.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual} ";
	private static final String R3 = "exists (select cs from ComposicaoSessao cs "+
									 		" where cs.orgaoJulgador = #{orgaoJulgadorAtual}" +
									 		" and cs.sessao = o)";
	private static final String R4 = "o.pessoaProcurador.idUsuario = #{usuarioLogado.idUsuario} ";
	private static final String R5 = "cast(o.dataSessao as date) = #{agendaSessao.currentDate}";
	
	protected void addSearchFields() {
		if("JT".equalsIgnoreCase(ParametroUtil.instance().getTipoJustica())){
			addSearchField("dataSessao", SearchCriteria.igual, R1);
		}else{
			addSearchField("dataSessao", SearchCriteria.igual, R5);
		}
		addSearchField("orgaoJulgadorColegiado", SearchCriteria.igual, R2);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R3);
		if(Identity.instance().hasRole("procurador")){
			addSearchField("procurador", SearchCriteria.igual, R4);
		}
	}

	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("sala", "o.orgaoJulgadorColegiadoSalaHorario.sala");
		map.put("tipoSessao", "o.tipoSessao");
		return map;
	}

	protected String getDefaultEjbql() {
		if("JT".equalsIgnoreCase(ParametroUtil.instance().getTipoJustica())){
			return DEFAULT_EJBQL_JT;
		}else{
			return DEFAULT_EJBQL;
		}
	}

	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}
	
}