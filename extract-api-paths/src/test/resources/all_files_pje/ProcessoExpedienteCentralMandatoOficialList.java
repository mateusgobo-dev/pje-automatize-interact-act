package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;

@Name(ProcessoExpedienteCentralMandatoOficialList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoExpedienteCentralMandatoOficialList extends EntityList<ProcessoExpedienteCentralMandado>{
	
	public static final String NAME = "processoExpedienteCentralMandatoOficialList";
	private static final long serialVersionUID = 1L;	
			
	private static final String DEFAULT_ORDER = "o.urgencia desc, o.dtDistribuicaoExpediente asc";
	
	private static final String R1 = "o.processoExpediente.processoTrf IN (select pp.processoTrf from ProcessoParte pp "+
									"where pp.processoTrf.idProcessoTrf = o.processoExpediente.processoTrf.idProcessoTrf "+
									"and pp.pessoa.nome like '%' || #{processoExpedienteCentralMandadoHome.nomeParte} || '%')";
	
	private static final String R2 = "o.pessoaGrupoOficialJustica.grupoOficialJustica = #{processoExpedienteCentralMandadoHome.grupoOficial}";
	
	private static final String R3 = "o.processoExpediente.tipoProcessoDocumento = #{processoExpedienteCentralMandadoHome.tpProcessoDocumento}";
	
	
		
	protected void addSearchFields() {	
		addSearchField("processoExpediente.processoTrf.nomeParte", SearchCriteria.igual, R1);
		addSearchField("pessoaGrupoOficialJustica.grupoOficialJustica", SearchCriteria.igual, R2);
		addSearchField("processoExpediente.tipoProcessoDocumento", SearchCriteria.igual, R3);	
	}

	protected Map<String, String> getCustomColumnsOrder() {	
		Map<String, String> map = new HashMap<String, String>();
		map.put("processoTrf", "o.processoExpediente.processoTrf");
		map.put("tipoProcessoDocumento", "o.processoExpediente.tipoProcessoDocumento");
		map.put("pessoaGrupoOficialJustica", "o.pessoaGrupoOficialJustica.pessoa");
		map.put("grupoOficialJustica", "o.pessoaGrupoOficialJustica.grupoOficialJustica");
		map.put("dtDistribuicaoExpediente", "o.dtDistribuicaoExpediente");
		return map;
	}

	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoExpedienteCentralMandado o ");
		sb.append("where o.statusExpedienteCentral = 'A' ");
		sb.append("and o.pessoaGrupoOficialJustica.pessoa.idUsuario = #{usuarioLogado.idUsuario} ");
		if (ParametroUtil.instance().isPrimeiroGrau() && Authenticator.getOrgaoJulgadorAtual() != null) {
			sb.append("and o.processoExpediente.processoTrf.orgaoJulgador = #{authenticator.getOrgaoJulgadorAtual()} ");
		} else if (Authenticator.getOrgaoJulgadorColegiadoAtual() != null) {
			sb.append("and o.processoExpediente.processoTrf.orgaoJulgadorColegiado = #{authenticator.getOrgaoJulgadorColegiadoAtual()} ");
		}
		return sb.toString();
	}

	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}
