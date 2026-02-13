package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.PagamentoPericia;
import br.jus.pje.nucleo.enums.PericiaStatusEnum;

@Name(PagamentoPericiaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PagamentoPericiaList extends EntityList<PagamentoPericia> {

	public static final String NAME = "pagamentoPericiaList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from PagamentoPericia o ";

	private static final String DEFAULT_ORDER = "o.dataSolicitacao";

	private PericiaStatusEnum statusPericia;
	private String perito;
	private String numeroProcesso;
	private String pessoaPericiado;
	private Especialidade especialidade;

	private static final String R1 = "o.processoPericia.status = #{pagamentoPericiaList.statusPericia}";
	private static final String R2 = "lower(to_ascii(o.processoPericia.pessoaPerito.nome)) like concat('%',lower(to_ascii(#{pagamentoPericiaList.perito})), '%')";
	private static final String R3 = "lower(to_ascii(o.processoPericia.processoTrf.processo.numeroProcesso)) like lower(to_ascii(#{pagamentoPericiaList.numeroProcesso}))";
	private static final String R4 = "lower(to_ascii(o.processoPericia.pessoaPericiado.nome)) like lower(to_ascii(#{pagamentoPericiaList.pessoaPericiado}))";
	private static final String R5 = "lower(to_ascii(o.processoPericia.especialidade.especialidade)) like lower(to_ascii(#{pagamentoPericiaList.especialidade.especialidade}))";

	@Override
	protected void addSearchFields() {
		addSearchField("processoPericia.status", SearchCriteria.igual, R1);
		addSearchField("processoPericia.pessoaPerito.nome", SearchCriteria.igual, R2);
		addSearchField("processoPericia.processoTrf.processo.numeroProcesso", SearchCriteria.igual, R3);
		addSearchField("processoPericia.pessoaPericiado.nome", SearchCriteria.igual, R4);
		addSearchField("processoPericia.especialidade.especialidade", SearchCriteria.igual, R5);

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

	public PericiaStatusEnum getStatusPericia() {
		return statusPericia;
	}

	public void setStatusPericia(PericiaStatusEnum statusPericia) {
		this.statusPericia = statusPericia;
	}

	public String getPerito() {
		return perito;
	}

	public void setPerito(String perito) {
		this.perito = perito;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getPessoaPericiado() {
		return pessoaPericiado;
	}

	public void setPessoaPericiado(String pessoaPericiado) {
		this.pessoaPericiado = pessoaPericiado;
	}

	public void setEspecialidade(Especialidade especialidade) {
		this.especialidade = especialidade;
	}

	public Especialidade getEspecialidade() {
		return especialidade;
	}

	@Override
	public void newInstance() {
		numeroProcesso = null;
		perito = null;
		pessoaPericiado = null;
		statusPericia = null;
		especialidade = null;
		super.newInstance();
	}

}