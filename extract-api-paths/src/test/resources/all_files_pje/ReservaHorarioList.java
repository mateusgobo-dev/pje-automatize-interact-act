package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ReservaHorario;

@Name(ReservaHorarioList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ReservaHorarioList extends EntityList<ReservaHorario> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "reservaHorarioList";
	private static final String DEFAULT_EJBQL = "select o from ReservaHorario o ";
	private static final String DEFAULT_ORDER = "o.dsTraducaoExpressaoCron ";
	private static final String R1 = "o.ativo = #{reservaHorarioList.situacao} ";
	private static final String R2 = "lower(o.dsExpressaoCronInicio) like concat('%', lower(#{reservaHorarioList.dsExpressaoCronInicio}), '%') ";
	private static final String R3 = "lower(o.dsExpressaoCronTermino) like concat('%', lower(#{reservaHorarioList.dsExpressaoCronTermino}), '%') ";
	private static final String R4 = "lower(o.dsTraducaoExpressaoCron) like concat('%', lower(#{reservaHorarioList.dsTraducaoExpressaoCron}), '%') ";

	private Boolean situacao;
	private String dsExpressaoCronInicio;
	private String dsExpressaoCronTermino;
	private String dsTraducaoExpressaoCron;

	@Override
	protected void addSearchFields() {
		addSearchField("situacao", SearchCriteria.igual, R1);
		addSearchField("dsExpressaoCronInicio", SearchCriteria.igual, R2);
		addSearchField("dsExpressaoCronTermino", SearchCriteria.igual, R3);
		addSearchField("dsTraducaoExpressaoCron", SearchCriteria.igual, R4);
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
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	public Boolean getSituacao() {
		return situacao;
	}

	public void setSituacao(Boolean situacao) {
		this.situacao = situacao;
	}

	public String getDsExpressaoCronInicio() {
		return dsExpressaoCronInicio;
	}

	public void setDsExpressaoCronInicio(String dsExpressaoCronInicio) {
		this.dsExpressaoCronInicio = dsExpressaoCronInicio;
	}

	public String getDsTraducaoExpressaoCron() {
		return dsTraducaoExpressaoCron;
	}

	public void setDsTraducaoExpressaoCron(String dsTraducaoExpressaoCron) {
		this.dsTraducaoExpressaoCron = dsTraducaoExpressaoCron;
	}

	public String getDsExpressaoCronTermino() {
		return dsExpressaoCronTermino;
	}

	public void setDsExpressaoCronTermino(String dsExpressaoCronTermino) {
		this.dsExpressaoCronTermino = dsExpressaoCronTermino;
	}
}
