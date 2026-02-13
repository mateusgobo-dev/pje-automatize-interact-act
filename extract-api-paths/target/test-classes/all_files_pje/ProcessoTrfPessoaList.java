package br.com.infox.ibpm.entity;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrf;

@Name(ProcessoTrfPessoaList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class ProcessoTrfPessoaList extends EntityList<ConsultaProcessoTrf> {
	public static final String NAME = "processoTrfPessoaList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select distinct o from ConsultaProcessoTrf o join o.processoTrf.processoParteList ppl ";
	private static final String DEFAULT_ORDER = "o.numeroProcesso";

	private static final String R1 = " ppl.pessoa.idUsuario = #{pessoaHome.instance.idPessoa} and o.processoStatus = 'D' ";

	@Override
	protected void addSearchFields() {
		addSearchField("", SearchCriteria.igual, R1);

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
}
