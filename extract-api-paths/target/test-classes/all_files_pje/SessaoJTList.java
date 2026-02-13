package br.com.jt.pje.list;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.jt.entidades.SessaoJT;

@Name(SessaoJTList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoJTList extends EntityList<SessaoJT> {

	public static final String NAME = "sessaoJTList";
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_EJBQL = "select o from SessaoJT o ";
	private static final String DEFAULT_ORDER = "idSessao";
	
	private static final String R1 = "lower(o.salaHorario.sala.sala) like concat('%',lower(#{sessaoJTList.sala}),'%')";
	private static final String R2 = "cast(o.dataSessao as date) >= #{sessaoJTList.dataInicio}";
	private static final String R3 = "cast(o.dataSessao as date) <= #{sessaoJTList.dataFim}";
	private static final String R4 = "o.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual}";
	
	private String sala;
	private Date dataInicio;
	private Date dataFim;
	
	protected void addSearchFields() {
		addSearchField("tipoSessao", SearchCriteria.igual);
		addSearchField("orgaoJulgadorColegiado", SearchCriteria.igual);
		addSearchField("sala", SearchCriteria.igual, R1);
		addSearchField("dataInicio", SearchCriteria.contendo, R2);
		addSearchField("dataFim", SearchCriteria.contendo, R3);
		addSearchField("orgaoJulgadorColegiado", SearchCriteria.igual, R4);		
		addSearchField("situacaoSessao", SearchCriteria.igual);
	}

	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("sala", "salaHorario.sala.sala");
		map.put("horaInicial", "salaHorario.horaInicial");
		map.put("horaFinal", "salaHorario.horaFinal");
		return map;
	}

	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public void setSala(String sala) {
		this.sala = sala;
	}

	public String getSala() {
		return sala;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public Date getDataFim() {
		return dataFim;
	}

}