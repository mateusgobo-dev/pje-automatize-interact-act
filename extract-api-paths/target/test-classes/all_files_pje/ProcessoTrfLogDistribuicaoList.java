package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;
import br.jus.pje.nucleo.entidades.ProcessoTrfLogDistribuicao;

@Name(ProcessoTrfLogDistribuicaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoTrfLogDistribuicaoList extends EntityList<ProcessoTrfLogDistribuicao> {

	public static final String NAME = "processoTrfLogDistribuicaoList";

	private static final long serialVersionUID = 1L;

	private String numeroProcesso;

	private static final String DEFAULT_EJBQL = "select o from ProcessoTrfLogDistribuicao o";
	private static final String DEFAULT_ORDER = "o.dataLog DESC";

	private static final String R1 = "o.processoTrf.processo.numeroProcesso like concat('%', #{processoTrfLogDistribuicaoList.numeroProcesso}, '%')";

	@Override
	protected void addSearchFields() {
		if(!ParametroUtil.instance().isPrimeiroGrau()) {
			addSearchField("orgaoJulgadorColegiado", SearchCriteria.igual);
		}
		addSearchField("orgaoJulgador", SearchCriteria.igual);
		addSearchField("inTipoDistribuicao", SearchCriteria.igual);
		addSearchField("processoTrf", SearchCriteria.igual, R1);
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

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}
	
	/**
	 * Limpa os campos da tela de pesquisa Log da Distribuição.
	 */
	public void limparCampos(){
		this.numeroProcesso = null;
		getEntity().setOrgaoJulgador(null);
		getEntity().setOrgaoJulgadorColegiado(null);
	}
}