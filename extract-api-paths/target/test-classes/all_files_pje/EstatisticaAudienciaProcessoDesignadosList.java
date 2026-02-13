package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.pje.action.EstatisticaAudienciaAction;

@Name(EstatisticaAudienciaProcessoDesignadosList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaAudienciaProcessoDesignadosList extends EntityList<Map<String, Object>> {

	public static final String NAME = "estatisticaAudienciaProcessoDesignadosList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_ORDER = "o.processoTrf.classeJudicial";

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct new Map(o.processoTrf as processo, o.processoTrf.classeJudicial as classe) from ProcessoAudiencia o where 1=1 ");
		EstatisticaAudienciaAction ea = EstatisticaAudienciaAction.intance();
		if (ea.getDataInicioFormatada() != null && ea.getDataFimFormatada() != null) {
			sb.append(" and to_char(o.dtMarcacao,'yyyy-MM-dd') between #{estatisticaAudienciaAction.dataInicioFormatada} ");
			sb.append(" and #{estatisticaAudienciaAction.dataFimFormatada} ");
			sb.append(" and o.processoTrf.orgaoJulgador = #{estatisticaAudienciaAction.orgaoJulgador} ");
			if (ea.getJuiz() != null) {
				sb.append("and exists (select ulms from UsuarioLocalizacaoMagistradoServidor ulms ");
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