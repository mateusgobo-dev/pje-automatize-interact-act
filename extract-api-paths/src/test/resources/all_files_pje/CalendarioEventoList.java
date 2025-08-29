package br.com.infox.pje.list;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.component.tree.PesquisaFeriadosBean;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.CalendarioEvento;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.JurisdicaoMunicipio;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.enums.AbrangenciaEnum;

@Name(CalendarioEventoList.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class CalendarioEventoList extends EntityList<CalendarioEvento> {
	public static final String NAME = "calendarioEventoList";

	private static final long serialVersionUID = 1L;
	
	private Boolean situacao;
	private String descricao;
	private String ato;
	private Integer dia;
	private Integer mes;
	private Integer ano;
	private Integer diaFim;
	private Integer mesFim;
	private Integer anoFim;
	private AbrangenciaEnum abrangencia;
	private Boolean feriadoJudiciario;
	private Estado estado;
	private Municipio municipio;
	private JurisdicaoMunicipio jurisdicaoMunicipio;
	private OrgaoJulgador orgaoJulgador;
	private Date dataInicial;
	private Date dataFinal;
	
	private Integer idMunicipio;
	private Integer idEstado;
	private Integer idOrgaoJulgador;
	
	private static final String DEFAULT_EJBQL = "select o from CalendarioEvento o ";

	private static final String DEFAULT_ORDER = "dtAno,dtMes,dtDia";

	private static final String R1 = "o.ativo = #{calendarioEventoList.situacao}";
	private static final String R2 = "lower(to_ascii(o.dsEvento)) like concat('%',lower(to_ascii(#{calendarioEventoList.descricao})),'%')";
	private static final String R3 = "lower(to_ascii(o.dsAto)) like concat('%',lower(to_ascii(#{calendarioEventoList.ato})),'%')";
	private static final String R7 = "o.inAbrangencia = #{calendarioEventoList.abrangencia}";
	private static final String R8 = "o.inJudiciario = #{calendarioEventoList.feriadoJudiciario}";
	
	@Override
	protected void addSearchFields() {
		addSearchField("ativo", SearchCriteria.contendo, R1);
		addSearchField("dsEvento", SearchCriteria.contendo, R2);
		addSearchField("dsAto", SearchCriteria.contendo, R3);
		addSearchField("inAbrangencia", SearchCriteria.contendo, R7);
		addSearchField("inJudiciario", SearchCriteria.contendo, R8);
	}

	@Override
	public void newInstance() {
		situacao = null;
		descricao = null;
		ato = null;
		dia = null;
		mes = null;
		ano = null;
		diaFim = null;
		mesFim = null;
		anoFim = null;
		abrangencia = null;
		feriadoJudiciario = null;
		estado = null;
		setMunicipio(null);
		setJurisdicaoMunicipio(null);
		orgaoJulgador = null;
		setIdOrgaoJulgador(null);
		setIdEstado(null);
		setIdMunicipio(null);
		dataInicial = null;
		dataFinal = null;
		super.newInstance();
	}
	
	@Override
	public List<CalendarioEvento> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<JurisdicaoMunicipio> getMunicipiosSedeList(){
		if(this.estado != null){
			StringBuilder sb = new StringBuilder();
			sb.append("select jM from JurisdicaoMunicipio jM ");
			sb.append("where jM.sede = true ");
			sb.append("and jM.jurisdicao.estado = :estado ");
			Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
			q.setParameter("estado", this.estado);
			List<JurisdicaoMunicipio> result = q.getResultList();
			return result;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> orgaoJulgadorItems() {
		if(this.getJurisdicaoMunicipio() != null || this.getMunicipio() != null){
			StringBuilder sql = new StringBuilder("select o from OrgaoJulgador o where o.jurisdicao in( ");
			sql.append("select o1.jurisdicao from JurisdicaoMunicipio o1 where o1.municipio = :municipio " );
			sql.append(													"and o1.sede = true) ");
			sql.append("order by o.orgaoJulgadorOrdemAlfabetica" );
			Query q = getEntityManager().createQuery(sql.toString());
			q.setParameter("municipio", this.municipio);
			return q.getResultList();
		}
		return null;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append(DEFAULT_EJBQL);
		sb.append("where 1=1 ");

		if (getDia() != null) {
			sb.append("and (o.dtDia = ").append(getDia())
				.append(" or (o.dtDiaFinal != null and ").append(getDia()).append(" between o.dtDia and o.dtDiaFinal))");
		}
		if (getMes() != null) {
			sb.append("and (o.dtMes = ").append(getMes())
				.append(" or (o.dtMesFinal != null and ").append(getMes()).append(" between o.dtMes and o.dtMesFinal))");
		}
		if (getAno() != null) {
			sb.append("and (o.dtAno = ").append(getAno())
				.append(" or (o.dtAnoFinal != null and ").append(getAno()).append(" between o.dtAno and o.dtAnoFinal))");
		}
		
		if(getEstado() != null){
			sb.append(" and o.estado.idEstado = " + getEstado().getIdEstado());
		}
		
		if(getMunicipio() != null){
			sb.append(" and o.municipio.idMunicipio = " + getMunicipio().getIdMunicipio());
		}
		
		if(getOrgaoJulgador() != null){
			sb.append(" and o.orgaoJulgador.idOrgaoJulgador = " + getOrgaoJulgador().getIdOrgaoJulgador());
		}
		return sb.toString();
	}
	
	public void carregaDadosDaSessao(){
		if(Contexts.getSessionContext().get("pesquisaFeriadosBeanTemp") != null){
			newInstance();
			
			PesquisaFeriadosBean pesquisaFeriadosBean = (PesquisaFeriadosBean) Contexts.getSessionContext().get("pesquisaFeriadosBeanTemp");
			
			setDataInicial(pesquisaFeriadosBean.getDataInicial());
			setDataFinal(pesquisaFeriadosBean.getDataFinal());
			setIdMunicipio(pesquisaFeriadosBean.getIdMunicipio());
			setIdEstado(pesquisaFeriadosBean.getIdEstado());
			setIdOrgaoJulgador(pesquisaFeriadosBean.getIdOrgaoJulgador());
			
			Contexts.getSessionContext().set("pesquisaFeriadosBeanTemp", null);
		}
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public Boolean getSituacao() {
		return situacao;
	}

	public void setSituacao(Boolean situacao) {
		this.situacao = situacao;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getAto() {
		return ato;
	}

	public void setAto(String ato) {
		this.ato = ato;
	}

	public Integer getDia() {
		return dia;
	}

	public void setDia(Integer dia) {
		this.dia = dia;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public Boolean getFeriadoJudiciario() {
		return feriadoJudiciario;
	}

	public void setFeriadoJudiciario(Boolean feriadoJudiciario) {
		this.feriadoJudiciario = feriadoJudiciario;
	}

	public void setAbrangencia(AbrangenciaEnum abrangencia) {
		this.abrangencia = abrangencia;
	}

	public AbrangenciaEnum getAbrangencia() {
		return abrangencia;
	}

	public void setMes(Integer mes) {
		this.mes = mes;
	}

	public Integer getMes() {
		return mes;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
		
		if(municipio != null && 
				(this.getJurisdicaoMunicipio() == null || !this.getJurisdicaoMunicipio().getMunicipio().equals(municipio))){
			
			String sql = "SELECT jm FROM JurisdicaoMunicipio jm WHERE jm.municipio = :municipio AND jm.sede = true";
			Query query = EntityUtil.createQuery(sql);
			query.setParameter("municipio", municipio);
			
			try {
				this.setJurisdicaoMunicipio((JurisdicaoMunicipio) query.getSingleResult());
			} catch (Exception ex) {
				this.jurisdicaoMunicipio = null;
			}
		}else if(municipio == null){
			this.setJurisdicaoMunicipio(null);
		}
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(Date dataInicial) {
		if(dataInicial != null){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dataInicial);
			setDia(calendar.get(Calendar.DAY_OF_MONTH));
			setMes(calendar.get(Calendar.MONTH)+1);
			setAno(calendar.get(Calendar.YEAR));
		}
		this.dataInicial = dataInicial;
	}

	public Date getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(Date dataFinal) {
		if(dataFinal != null){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dataFinal);
			setDiaFim(calendar.get(Calendar.DAY_OF_MONTH));
			setMesFim(calendar.get(Calendar.MONTH)+1);
			setAnoFim(calendar.get(Calendar.YEAR));
		}
		this.dataFinal = dataFinal;
	}

	public Integer getDiaFim() {
		return diaFim;
	}

	public void setDiaFim(Integer diaFim) {
		this.diaFim = diaFim;
	}

	public Integer getMesFim() {
		return mesFim;
	}

	public void setMesFim(Integer mesFim) {
		this.mesFim = mesFim;
	}

	public Integer getAnoFim() {
		return anoFim;
	}

	public void setAnoFim(Integer anoFim) {
		this.anoFim = anoFim;
	}
	
	
	public Integer getIdMunicipio() {
		return idMunicipio;
	}

	public void setIdMunicipio(Integer idMunicipio) {
		if(idMunicipio != null && !idMunicipio.equals(0)){
			setMunicipio(EntityUtil.find(Municipio.class, idMunicipio));
		}
		this.idMunicipio = idMunicipio;
	}

	public Integer getIdEstado() {
		return idEstado;
	}

	public void setIdEstado(Integer idEstado) {
		if(idEstado != null && !idEstado.equals(0)){
			setEstado(EntityUtil.find(Estado.class, idEstado));
		}
		this.idEstado = idEstado;
	}

	public Integer getIdOrgaoJulgador() {
		return idOrgaoJulgador;
	}

	public void setIdOrgaoJulgador(Integer idOrgaoJulgador) {
		if(idOrgaoJulgador != null && !idOrgaoJulgador.equals(0)){
			setOrgaoJulgador(EntityUtil.find(OrgaoJulgador.class, idOrgaoJulgador));
		}
		this.idOrgaoJulgador = idOrgaoJulgador;
	}

	public JurisdicaoMunicipio getJurisdicaoMunicipio() {
		return jurisdicaoMunicipio;
	}

	public void setJurisdicaoMunicipio(JurisdicaoMunicipio jurisdicaoMunicipio) {
		this.jurisdicaoMunicipio = jurisdicaoMunicipio;
		if(jurisdicaoMunicipio != null && (getMunicipio() == null || !getMunicipio().equals(jurisdicaoMunicipio.getMunicipio()))){
			setMunicipio(jurisdicaoMunicipio.getMunicipio());
		}else if(jurisdicaoMunicipio == null){
			municipio = null;
		}
	}
	
	public void limparCampos() {
		limpaEstado();
		limpaMunicipio();
		limpaOrgaoJulgador();
	}
	
	public void limpaEstado() {
		setEstado(null);
		setIdEstado(null);
	}
	
	public void limpaMunicipio(){
		setJurisdicaoMunicipio(null);
		setMunicipio(null);
		setIdMunicipio(null);
	}
	
	public void limpaOrgaoJulgador(){
		setOrgaoJulgador(null);
		setIdOrgaoJulgador(null);
	}
	
}
