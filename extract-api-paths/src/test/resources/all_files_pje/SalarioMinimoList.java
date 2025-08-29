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
import br.jus.pje.jt.entidades.SalarioMinimo;

@Name(SalarioMinimoList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class SalarioMinimoList extends EntityList<SalarioMinimo> {

	public static final String NAME = "salarioMinimoList";
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_EJBQL = "select o from SalarioMinimo o ";
	private static final String DEFAULT_ORDER = "o.dataFimVigencia desc";
	
	private static final String R1 = "lower(to_ascii(trim(o.fundamentacaoLegal))) like concat('%',lower(to_ascii(trim(#{salarioMinimoList.entity.fundamentacaoLegal}))),'%')";
	private static final String R2 = "o.valor >= #{salarioMinimoList.valorInicio}";
	private static final String R3 = "o.valor <= #{salarioMinimoList.valorFim}";
	private static final String R4 = "cast(#{salarioMinimoList.dataPesquisa} as date) >= cast(o.dataInicioVigencia as date)";
	private static final String R5 = "(o.dataFimVigencia is null or cast(#{salarioMinimoList.dataPesquisa} as date) <= cast(o.dataFimVigencia as date))";
	
	private Double valorInicio;
	private Double valorFim;
	
	private Date dataPesquisa;
	
	protected void addSearchFields() {
		addSearchField("fundamentacaoLegal", SearchCriteria.igual, R1);
		addSearchField("valorInicio", SearchCriteria.igual, R2);
		addSearchField("valorFim", SearchCriteria.igual, R3);
		addSearchField("dataPesquisa", SearchCriteria.igual, R4);
		addSearchField("dataPesquisa2", SearchCriteria.igual, R5);
	}

	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	protected String getDefaultEjbql() {
		StringBuffer sql = new StringBuffer(DEFAULT_EJBQL);
		if(getEntity().getDataInicioVigencia() != null && getEntity().getDataFimVigencia() != null){
			sql.append("where (#{salarioMinimoList.entity.dataInicioVigencia} between o.dataInicioVigencia and o.dataFimVigencia ");
			sql.append("or #{salarioMinimoList.entity.dataFimVigencia} between o.dataInicioVigencia and o.dataFimVigencia ");
			sql.append("or o.dataInicioVigencia between #{salarioMinimoList.entity.dataInicioVigencia} and #{salarioMinimoList.entity.dataFimVigencia} ");
			sql.append("or o.dataFimVigencia between #{salarioMinimoList.entity.dataInicioVigencia} and #{salarioMinimoList.entity.dataFimVigencia}) ");
		}
		if(getEntity().getDataInicioVigencia() != null && getEntity().getDataFimVigencia() == null){
			sql.append("where ((cast(o.dataInicioVigencia as date) >= #{salarioMinimoList.entity.dataInicioVigencia} )");
			sql.append(" or (#{salarioMinimoList.entity.dataInicioVigencia} between cast(o.dataInicioVigencia as date) and cast(o.dataFimVigencia as date))");
			sql.append(" or (o.dataFimVigencia is null and cast(o.dataInicioVigencia as date) <= #{salarioMinimoList.entity.dataInicioVigencia})) ");
		}
		
		if(getEntity().getDataInicioVigencia() == null && getEntity().getDataFimVigencia() != null){
			sql.append("where ((cast(o.dataFimVigencia as date) <= #{salarioMinimoList.entity.dataFimVigencia})");
			sql.append(" or (#{salarioMinimoList.entity.dataFimVigencia} between cast(o.dataInicioVigencia as date) and cast(o.dataFimVigencia as date))");
			sql.append(" or (o.dataFimVigencia is null and cast(o.dataInicioVigencia as date) <= #{salarioMinimoList.entity.dataFimVigencia})) ");
		}
		
		return sql.toString();
	}

	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}
	
	@Override
	public List<SalarioMinimo> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}
	
	@Override
	public void newInstance() {
		this.valorInicio = null;
		this.valorFim = null;
		this.dataPesquisa = null;
		super.newInstance();
		getResultList();
	}

	public Double getValorInicio() {
		return valorInicio;
	}

	public void setValorInicio(Double valorInicio) {
		this.valorInicio = valorInicio;
	}

	public Double getValorFim() {
		return valorFim;
	}

	public void setValorFim(Double valorFim) {
		this.valorFim = valorFim;
	}

	public Date getDataPesquisa() {
		return dataPesquisa;
	}

	public void setDataPesquisa(Date dataPesquisa) {
		this.dataPesquisa = dataPesquisa;
	}

}