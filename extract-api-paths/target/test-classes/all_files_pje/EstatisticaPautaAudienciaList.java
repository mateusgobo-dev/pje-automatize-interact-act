package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.pje.bean.EstatisticaPautaAudienciaBean;

@Name(EstatisticaPautaAudienciaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EstatisticaPautaAudienciaList extends EntityList<EstatisticaPautaAudienciaBean> {

	public static final String NAME = "estatisticaPautaAudienciaList";
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_EJBQL = " select distinct new br.com.infox.pje.bean.EstatisticaPautaAudienciaBean(pa.statusAudiencia as statusAudienciaEnum, "
			+ "                                                                         pa.dtInicio as dtInicio, "
			+ "                                                                         pa.processoTrf as processoTrf, "
			+ "                                                                         pa.processoTrf.classeJudicial as classeJudicial, "
			+ "                                                                         pa.tipoAudiencia as tipoAudiencia, "
			+ "                                                                         (select count(a) from ProcessoAudiencia u inner join "
			+ "                                                                                                                 u.processoAudienciaPessoaList a "
			+ "                                                                           where a.testemunha = true "
			+ "                                                                           and a.processoAudiencia.idProcessoAudiencia = pa.idProcessoAudiencia) as totalNumeroDepoimento) "
			+ " from ProcessoAudiencia pa inner join "
			+ "                       pa.processoTrf.processoParteList ppl inner join "
			+ "                       pa.processoTrf.processoAssuntoList pal inner join "
			+ "                       pa.processoTrf.orgaoJulgador.localizacao.usuarioLocalizacaoList ull "
			+ " where 1=1 ";
	private static final String DEFAULT_ORDER = "pa.dtInicio ";
	private static final String DEFAULT_GROUP_BY = "pa.statusAudiencia, pa.dtInicio, pa.processoTrf, pa.processoTrf.classeJudicial, pa.tipoAudiencia, pal.assuntoTrf, pa.idProcessoAudiencia ";

	private static final String R1 = " pa.processoTrf.orgaoJulgador = #{estatisticaPautaAudienciaAction.orgaoJulgador} ";
	private static final String R2 = " exists (select ulms from UsuarioLocalizacaoMagistradoServidor ulms  "
			+ "             where ulms.usuarioLocalizacao.usuario.nome = #{estatisticaPautaAudienciaAction.juiz.nome} "
			+ "               and ulms.orgaoJulgadorCargo.cargo = pa.processoTrf.cargo "
			+ "               and ulms.orgaoJulgador = pa.processoTrf.orgaoJulgador "
			+ "               and ulms.usuarioLocalizacao.usuario.idUsuario in (select pm.idUsuario from PessoaMagistrado pm) "
			+ "               and ulms.dtInicio <= pa.dtInicio "
			+ "               and (ulms.dtFinal is null or (ulms.dtFinal >= pa.dtInicio))) ";
	private static final String R3 = " pa.statusAudiencia = #{estatisticaPautaAudienciaAction.statusAudienciaEnum}";
	private static final String R4 = " to_char(pa.dtMarcacao,'yyyy-MM-dd') >= #{estatisticaPautaAudienciaAction.dataInicioFormatada} ";
	private static final String R5 = " to_char(pa.dtMarcacao,'yyyy-MM-dd') <= #{estatisticaPautaAudienciaAction.dataFimFormatada} ";

	@Override
	protected void addSearchFields() {
		addSearchField("orgaoJulgador", SearchCriteria.igual, R1);
		addSearchField("juiz", SearchCriteria.igual, R2);
		addSearchField("statusAudienciaEnum", SearchCriteria.igual, R3);
		addSearchField("dataInicio", SearchCriteria.igual, R4);
		addSearchField("dataFim", SearchCriteria.igual, R5);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected String getEntityName() {
		return NAME;
	}

	@Override
	public String getGroupBy() {
		return DEFAULT_GROUP_BY;
	}

}