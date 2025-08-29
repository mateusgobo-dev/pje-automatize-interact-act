package br.com.jt.pje.list;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.jt.entidades.LimiteDepositoRecursal;
import br.jus.pje.nucleo.util.DateUtil;

@Name(LimiteDepositoRecursalList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class LimiteDepositoRecursalList extends EntityList<LimiteDepositoRecursal> {

	public static final String NAME = "limiteDepositoRecursalList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from LimiteDepositoRecursal o ";
	private static final String DEFAULT_ORDER = "o.dataFimVigencia desc";

	private static final String R1 = "lower(to_ascii(trim(o.fundamentacaoLegal))) like concat('%',lower(to_ascii(trim(#{limiteDepositoRecursalList.entity.fundamentacaoLegal}))),'%')";
	private static final String R2 = "o.valor1Grau >= #{limiteDepositoRecursalList.valorInicio1Grau}";
	private static final String R3 = "o.valor1Grau <= #{limiteDepositoRecursalList.valorFim1Grau}";
	private static final String R4 = "o.valor2Grau >= #{limiteDepositoRecursalList.valorInicio2Grau}";
	private static final String R5 = "o.valor2Grau <= #{limiteDepositoRecursalList.valorFim2Grau}";
	private static final String R6 = "cast(#{limiteDepositoRecursalList.dataPesquisa} as date) >= cast(o.dataInicioVigencia as date)";
	private static final String R7 = "(o.dataFimVigencia is null or cast(#{limiteDepositoRecursalList.dataPesquisa} as date) <= cast(o.dataFimVigencia as date))";

	private Double valorInicio1Grau;
	private Double valorFim1Grau;
	private Double valorInicio2Grau;
	private Double valorFim2Grau;
	
	private Date dataPesquisa;
	
	private String dataPesquisaString;

	protected void addSearchFields() {
		addSearchField("fundamentacaoLegal", SearchCriteria.igual, R1);
		addSearchField("valorInicio1Grau", SearchCriteria.igual, R2);
		addSearchField("valorFim1Grau", SearchCriteria.igual, R3);
		addSearchField("valorInicio2Grau", SearchCriteria.igual, R4);
		addSearchField("valorFim2Grau", SearchCriteria.igual, R5);
		addSearchField("dataPesquisa", SearchCriteria.igual, R6);
		addSearchField("dataPesquisa2", SearchCriteria.igual, R7);
	}
	
	@Override
	public void newInstance() {
		this.valorInicio1Grau = null;
		this.valorFim1Grau = null;
		this.valorInicio2Grau = null;
		this.valorFim2Grau = null;
		this.dataPesquisa = null;
		setDataPesquisaString(null);
		super.newInstance();
		getResultList();
	}

	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	protected String getDefaultEjbql() {
		StringBuffer sql = new StringBuffer(DEFAULT_EJBQL);
		if(getEntity().getDataInicioVigencia() != null && getEntity().getDataFimVigencia() != null){
			sql.append("where (#{limiteDepositoRecursalList.entity.dataInicioVigencia} between o.dataInicioVigencia and o.dataFimVigencia ");
			sql.append("or #{limiteDepositoRecursalList.entity.dataFimVigencia} between o.dataInicioVigencia and o.dataFimVigencia ");
			sql.append("or o.dataInicioVigencia between #{limiteDepositoRecursalList.entity.dataInicioVigencia} and #{limiteDepositoRecursalList.entity.dataFimVigencia} ");
			sql.append("or o.dataFimVigencia between #{limiteDepositoRecursalList.entity.dataInicioVigencia} and #{limiteDepositoRecursalList.entity.dataFimVigencia}) ");
		}
		if(getEntity().getDataInicioVigencia() != null && getEntity().getDataFimVigencia() == null){
			sql.append("where ((cast(o.dataInicioVigencia as date) >= #{limiteDepositoRecursalList.entity.dataInicioVigencia})");
			sql.append(" or (#{limiteDepositoRecursalList.entity.dataInicioVigencia} between cast(o.dataInicioVigencia as date) and cast(o.dataFimVigencia as date))"); 
			sql.append(" or (o.dataFimVigencia is null and cast(o.dataInicioVigencia as date) <= #{limiteDepositoRecursalList.entity.dataInicioVigencia})) ");
		}
		if(getEntity().getDataInicioVigencia() == null && getEntity().getDataFimVigencia() != null){
			sql.append("where ((cast(o.dataFimVigencia as date) <= #{limiteDepositoRecursalList.entity.dataFimVigencia}) ");
			sql.append(" or (#{limiteDepositoRecursalList.entity.dataFimVigencia} between cast(o.dataInicioVigencia as date)and cast(o.dataFimVigencia as date))");
			sql.append(" or (o.dataFimVigencia is null and cast(o.dataInicioVigencia as date) <= #{limiteDepositoRecursalList.entity.dataFimVigencia})) ");
		}
		return sql.toString();
	}

	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}
	
	@Override
	public List<LimiteDepositoRecursal> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	public Double getValorInicio1Grau() {
		return valorInicio1Grau;
	}

	public void setValorInicio1Grau(Double valorInicio1Grau) {
		this.valorInicio1Grau = valorInicio1Grau;
	}

	public Double getValorFim1Grau() {
		return valorFim1Grau;
	}

	public void setValorFim1Grau(Double valorFim1Grau) {
		this.valorFim1Grau = valorFim1Grau;
	}

	public Double getValorInicio2Grau() {
		return valorInicio2Grau;
	}

	public void setValorInicio2Grau(Double valorInicio2Grau) {
		this.valorInicio2Grau = valorInicio2Grau;
	}

	public Double getValorFim2Grau() {
		return valorFim2Grau;
	}

	public void setValorFim2Grau(Double valorFim2Grau) {
		this.valorFim2Grau = valorFim2Grau;
	}

	public Date getDataPesquisa() {
		return dataPesquisa;
	}

	public void setDataPesquisa(Date dataPesquisa) {
		this.dataPesquisa = dataPesquisa;
	}

	public String getDataPesquisaString() {
		return dataPesquisaString;
	}

	public void setDataPesquisaString(String dataPesquisaString) {
		if(dataPesquisaString != null && !dataPesquisaString.isEmpty()){
			setDataPesquisa(DateUtil.stringToDate(dataPesquisaString, "dd/MM/yyyy"));
		}
		this.dataPesquisaString = dataPesquisaString;
	}

}