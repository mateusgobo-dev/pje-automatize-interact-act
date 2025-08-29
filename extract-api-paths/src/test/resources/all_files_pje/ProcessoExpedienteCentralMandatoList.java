package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;

@Name(ProcessoExpedienteCentralMandatoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoExpedienteCentralMandatoList extends EntityList<ProcessoExpedienteCentralMandado> {

	public static final String NAME = "processoExpedienteCentralMandatoList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.urgencia desc, o.dtDistribuicaoExpediente asc";

	@Override
	protected void addSearchFields() {
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("processoTrf", "o.processoExpediente.processoTrf");
		map.put("tipoProcessoDocumento", "o.processoExpediente.tipoProcessoDocumento");
		map.put("pessoaGrupoOficialJustica", "o.pessoaGrupoOficialJustica.pessoa");
		map.put("grupoOficialJustica", "o.pessoaGrupoOficialJustica.grupoOficialJustica");
		map.put("dtDistribuicaoExpediente", "o.dtDistribuicaoExpediente");
		return map;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoExpedienteCentralMandado o ");
		sb.append("where (o.pessoaGrupoOficialJustica is not null ");
		sb.append("and o.statusExpedienteCentral = 'R' ");
		sb.append("and o.processoExpedienteCentralMandadoAnterior is not null) ");
		sb.append("and o.idProcessoExpedienteCentralMandado != o.processoExpedienteCentralMandadoAnterior ");
		sb.append("and o.centralMandado.idCentralMandado in ( ");
		sb.append("select cml.centralMandado.idCentralMandado from CentralMandadoLocalizacao cml ");
		sb.append("where cml.localizacao.idLocalizacao = #{usuarioLogadoLocalizacaoAtual.localizacaoFisica.idLocalizacao} ");
		sb.append("OR cml.localizacao IN (#{localizacaoService.getTree(usuarioLogadoLocalizacaoAtual.localizacaoFisica)})) ");
		return sb.toString();	
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

}
