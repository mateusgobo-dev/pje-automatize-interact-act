package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.pje.action.EstatisticaAudienciaAction;

@Name(EstatisticaAudienciaProcessoClasseList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaAudienciaProcessoClasseList extends EntityList<Map<String, Object>> {

	public static final String NAME = "estatisticaAudienciaProcessoClasseList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_ORDER = "o.processoTrf.classeJudicial.codClasseJudicial";

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select new Map(o.processoTrf.classeJudicial as classe, o.processoTrf.classeJudicial as nome) from ProcessoAudiencia o where 1=1 ");
		EstatisticaAudienciaAction ea = EstatisticaAudienciaAction.intance();
		if (ea.getDataInicio() != null && ea.getDataFim() != null) {
			sb.append(" and to_char(o.dtMarcacao,'yyyy-MM-dd') < #{estatisticaAudienciaAction.dataFimFormatada} ");
			sb.append(" and (o.dtCancelamento is null or ");
			sb.append(" to_char(o.dtCancelamento,'yyyy-MM-dd') >= #{estatisticaAudienciaAction.dataInicioFormatada}) ");
			sb.append(" and o.processoTrf.orgaoJulgador = #{estatisticaAudienciaAction.orgaoJulgador} ");
			if (ea.getJuiz() != null) {
				sb.append(" and exists (select ulms from UsuarioLocalizacaoMagistradoServidor ulms ");
				sb.append("             where ulms.usuarioLocalizacao.usuario.nome = #{estatisticaAudienciaAction.juiz.nome} ");
				sb.append("               and ulms.orgaoJulgadorCargo.cargo = o.processoTrf.cargo ");
				sb.append("               and ulms.orgaoJulgador = o.processoTrf.orgaoJulgador ");
				sb.append("               and ulms.usuarioLocalizacao.usuario.idUsuario in (select pm.idUsuario from PessoaMagistrado pm) ");
				sb.append("               and ulms.dtInicio <= o.dtInicio ");
				sb.append("               and (ulms.dtFinal is null or (ulms.dtFinal >= o.dtInicio))) ");
			}
		}

		return sb.toString();
	}

	@Override
	protected String getEntityName() {
		return NAME;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	public List<Map<String, Object>> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	@Override
	protected void addSearchFields() {
		// TODO Auto-generated method stub

	}

}