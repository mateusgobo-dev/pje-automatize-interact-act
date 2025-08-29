package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.util.Strings;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ConsultaProcessoIbpm;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;

@Name(ConsultaProcessoIbpmDetalheList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ConsultaProcessoIbpmDetalheList extends EntityList<ConsultaProcessoIbpm> {

	public static final String NAME = "consultaProcessoIbpmDetalheList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from ConsultaProcessoIbpm o";
	private static final String DEFAULT_ORDER = "idProcessoLocalizacao";

	private static final String R1 = "o.idOrgaoJulgador = #{consultaProcessoIbpmDetalheList.orgaoJulgador.idOrgaoJulgador}";
	private static final String R2 = "o.idOrgaoJulgadorColegiado = #{consultaProcessoIbpmDetalheList.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado}";
	private static final String R3 = "o.idProcesso = #{consultaProcessoSimplesList.idProcesso}";

	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;

	@Override
	protected void addSearchFields() {
		addSearchField("numeroProcesso", SearchCriteria.contendo);
		addSearchField("nomeTask", SearchCriteria.contendo);
		addSearchField("nomeProcessDefinition", SearchCriteria.contendo);
		addSearchField("idOrgaoJulgador", SearchCriteria.igual, R1);
		addSearchField("idOrgaoJulgadorColegiado", SearchCriteria.igual, R2);
		addSearchField("idProcesso", SearchCriteria.igual, R3);
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

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	public boolean showGrid() {
		return !Strings.isEmpty(getEntity().getNumeroProcesso()) || orgaoJulgador != null
				|| orgaoJulgadorColegiado != null;
	}
}