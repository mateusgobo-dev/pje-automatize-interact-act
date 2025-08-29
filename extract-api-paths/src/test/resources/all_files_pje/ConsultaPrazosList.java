package br.com.infox.pje.list;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.ibpm.component.tree.AssuntoTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.SimNaoEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name(ConsultaPrazosList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class ConsultaPrazosList extends EntityList<ProcessoParteExpediente> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "consultaPrazosList";

	private String numeroProcesso;
	private TipoProcessoDocumento tipoDocumentoAtoMagistrado;
	private OrgaoJulgador orgaoJulgador;
	private String destinatario;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private PrioridadeProcesso prioridadeProcesso;
	private Date dataInicio;
	private Date dataFim;
	private SimNaoEnum situacaoPrazoVencido = null;
	private SimNaoEnum situacaoExpedienteFechado = null;
	private Tarefa tarefa;
	private boolean filtroPreenchido = Boolean.FALSE;
	private boolean filtroInformado = Boolean.FALSE;

	private static final String DEFAULT_EJBQL = "select distinct ppe from ProcessoParteExpediente ppe inner join ppe.processoExpediente o "
			+ " where o.inTemporario = false and o.dtExclusao is null and o.processoTrf.processoStatus = 'D'";
			
			
	private static final String DEFAULT_ORDER = "ppe";

	private static final String R1 = "o.processoTrf.processo.numeroProcesso like concat('%', #{consultaPrazosList.numeroProcesso}, '%')";
	private static final String R2 = "exists(select pd from ProcessoDocumentoExpediente pd where pd.processoExpediente = o and pd.anexo = false and pd.processoDocumentoAto.tipoProcessoDocumento = #{consultaPrazosList.tipoDocumentoAtoMagistrado})";
	private static final String R3 = "lower(to_ascii(ppe.pessoaParte.nome)) like concat('%', lower(to_ascii(#{consultaPrazosList.destinatario})), '%')";
	private static final String R4 = "o.processoTrf.classeJudicial = #{consultaPrazosList.classeJudicial}";
	private static final String R5 = "exists(select pa from ProcessoAssunto pa where pa.processoTrf = o.processoTrf and pa.assuntoTrf = #{consultaPrazosList.assuntoTrf})";
	private static final String R6 = "exists(select ppp from ProcessoPrioridadeProcesso ppp where ppp.processoTrf = o.processoTrf and ppp.prioridadeProcesso = #{consultaPrazosList.prioridadeProcesso})";
	private static final String R7 = "exists(select sp from SituacaoProcesso sp where sp.idProcesso = o.processoTrf.idProcessoTrf and sp.idTarefa = #{consultaPrazosList.tarefa.idTarefa})";

	public ConsultaPrazosList() {
		super();
		String hql = DEFAULT_EJBQL;
		
		setEjbql(hql);
		setOrder(DEFAULT_ORDER);
		setMaxResults(DEFAULT_MAX_RESULT);
		setMostraConsultaSemFiltro(Boolean.FALSE);
		newInstance();
	}

	@Override
	protected void addSearchFields() {
		addSearchField("numeroProcesso", SearchCriteria.igual, R1);
		addSearchField("tipoExpediente", SearchCriteria.contendo, R2);
		addSearchField("destinatario", SearchCriteria.igual, R3);
		addSearchField("classeJudicial", SearchCriteria.igual, R4);
		addSearchField("assuntoTrf", SearchCriteria.contendo, R5);
		addSearchField("prioridadeProcesso", SearchCriteria.contendo, R6);
		addSearchField("tarefa", SearchCriteria.igual, R7);
	}

	@Override
	public void newInstance() {
		setNumeroProcesso(null);
		setClasseJudicial(null);
		setAssuntoTrf(null);
		setOrgaoJulgador(null);
		setDataInicio(null);
		setDataFim(null);
		setTipoDocumentoAtoMagistrado(null);
		setPrioridadeProcesso(null);
		setDestinatario(null);
		setSituacaoPrazoVencido(null);
		setSituacaoExpedienteFechado(null);
		setTarefa(null);
		filtroPreenchido = Boolean.FALSE;
		super.newInstance();
	}

	@Override
	public List<ProcessoParteExpediente> getResultList() {
		setEjbql(getDefaultEjbql());
		List<ProcessoParteExpediente> expedientes = super.getResultList();
		if(expedientes != null){
			for (ProcessoParteExpediente ppe : expedientes) {
				ppe.setCheck(ppe.getFechado());
			}
		}
		return expedientes;
	}

	public void limparCache() {
		getEntityManager().clear();
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("orgaoJulgador", "o.processoTrf.orgaoJulgador");
		map.put("assunto", "o.processoTrf.assuntoTrf");
		map.put("classeJudicial", "o.processoTrf.classeJudicial");
		return map;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append(DEFAULT_EJBQL);
		sb.append(getRestrictionOrgaoJulgadorOrgaoJulgadorColegiado());
        if (dataInicio != null || dataFim != null) {
            sb.append(getRestrictionPeriodoPrazo());
        }
		sb.append(getRestrictionSituacaoPrazo());
		sb.append(getRestrictionSituacaoExpediente());
		return sb.toString();
	}
	
	@Override
	public boolean verificarFiltroPreenchido() {
		return filtroPreenchido || super.verificarFiltroPreenchido();
	}
	
	private String getRestrictionOrgaoJulgadorOrgaoJulgadorColegiado() {
		StringBuilder result = new StringBuilder();
		if (!Authenticator.isPapelAdministrador()) {
			if (Authenticator.getOrgaoJulgadorColegiadoAtual() != null) {
				result.append(" and o.processoTrf.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = ")
					.append(Authenticator.getOrgaoJulgadorColegiadoAtual().getIdOrgaoJulgadorColegiado());
			}
			if (Authenticator.getOrgaoJulgadorAtual() != null) {
				result.append(" and o.processoTrf.orgaoJulgador.idOrgaoJulgador = ")
					.append(Authenticator.getOrgaoJulgadorAtual().getIdOrgaoJulgador());				
			}
		}
		return result.toString();
	}

	/**
	 * Método retorna trecho de query que restringe a consulta de expedientes ao prazo informado como parâmetro
	 * @return trecho de query montado com as restrições  
	 */
	private String getRestrictionPeriodoPrazo() {
		StringBuilder sbAux = new StringBuilder();
		if (dataInicio != null && dataFim != null){
			setDataInicio(dataInicio);
			setDataFim(dataFim);
			sbAux.append("between #{consultaPrazosList.dataInicio} and #{consultaPrazosList.dataFim}");			
		} else if (dataInicio != null && dataFim == null){
			setDataInicio(dataInicio);
			sbAux.append(">= #{consultaPrazosList.dataInicio}");
		} else {
			setDataFim(dataFim);
			sbAux.append("<= #{consultaPrazosList.dataFim}");
		}
		filtroPreenchido = Boolean.TRUE;
		return " and ppe.dtPrazoLegal " + sbAux.toString();
	}	

	/**
	 * Método retorna trecho de query que restringe a consulta de expedientes à situação informada como parâmetro, ou seja, fechado ou não
	 * @return trecho de query montado com as restrições  
	 */
	private String getRestrictionSituacaoExpediente() {
		StringBuilder sb = new StringBuilder();
		if (situacaoExpedienteFechado != null) {
			if(situacaoExpedienteFechado == SimNaoEnum.S) {
				sb.append(" and ppe.fechado = true");
			} else {
				sb.append(" and ppe.fechado = false");
			}
			filtroPreenchido = Boolean.TRUE;
		}
		return sb.toString();
	}

	/**
	 * Método retorna trecho de query que restringe a consulta de expedientes à situação do prazo informada como parâmetro, ou seja, vencido ou não
	 * @return trecho de query montado com as restrições  
	 */
	private String getRestrictionSituacaoPrazo() {
	    StringBuilder sb = new StringBuilder();
	    if (situacaoPrazoVencido != null ) {
	    	if (situacaoPrazoVencido == SimNaoEnum.S) {
				sb.append(" and (ppe.dtPrazoLegal is not null and ppe.dtPrazoLegal < current_date )");
			} else {
				sb.append(" and (ppe.dtPrazoLegal is null or ppe.dtPrazoLegal >= current_date )");
			}
	    	filtroPreenchido = Boolean.TRUE;
	    }
	    return sb.toString();
	}
	
	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public static ConsultaPrazosList instance() {
		return ComponentUtil.getComponent(NAME);
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setTipoDocumentoAtoMagistrado(TipoProcessoDocumento tipoDocumentoAtoMagistrado) {
		this.tipoDocumentoAtoMagistrado = tipoDocumentoAtoMagistrado;
	}

	public TipoProcessoDocumento getTipoDocumentoAtoMagistrado() {
		return tipoDocumentoAtoMagistrado;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setDestinatario(String destinatario) {
		this.destinatario = destinatario;
	}

	public String getDestinatario() {
		return destinatario;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setPrioridadeProcesso(PrioridadeProcesso prioridadeProcesso) {
		this.prioridadeProcesso = prioridadeProcesso;
	}

	public PrioridadeProcesso getPrioridadeProcesso() {
		return prioridadeProcesso;
	}

	public void setSituacaoPrazoVencido(SimNaoEnum situacaoPrazo) {
		this.situacaoPrazoVencido = situacaoPrazo;
	}

	public SimNaoEnum getSituacaoPrazoVencido() {
		return situacaoPrazoVencido;
	}

	public void setSituacaoExpedienteFechado(SimNaoEnum situacaoExpediente) {
		this.situacaoExpedienteFechado = situacaoExpediente;
	}

	public SimNaoEnum getSituacaoExpedienteFechado() {
		return situacaoExpedienteFechado;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = DateUtil.getBeginningOfDay(dataInicio);
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = DateUtil.getEndOfDay(dataFim);
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setTarefa(Tarefa tarefa) {
		this.tarefa = tarefa;
	}

	public Tarefa getTarefa() {
		return tarefa;
	}

	/**
	 * @return instância de ClasseJudicialTreeHandler
	 */
	protected ClasseJudicialTreeHandler getClasseJudicialTreeHandler() {
		return ComponentUtil.getComponent(ClasseJudicialTreeHandler.class);
	}
	
	/**
	 * @return instância de AssuntoTreeHandler
	 */
	protected AssuntoTreeHandler getAssuntoTreeHandler() {
		return ComponentUtil.getComponent(AssuntoTreeHandler.class);
	}
	
	public void verificarFiltrosInformados() {
		if(getSituacaoPrazoVencido() != null || getSituacaoExpedienteFechado() != null 
				|| getTipoDocumentoAtoMagistrado() != null || getPrioridadeProcesso() != null) {
			
			this.filtroInformado = true;
		} else{
			this.filtroInformado = false;
		}
	}

	public boolean isFiltroInformado() {
		return this.filtroInformado;
	}

	public void setFiltroInformado(boolean filtroInformado) {
		this.filtroInformado = filtroInformado;
	}
	
	@Override
	public List<ProcessoParteExpediente> list() {
		if (isFiltroInformado()) {
			return super.list(DEFAULT_MAX_RESULT);
		} else {
			return null;
		}
	}
	
	@Override
	public Integer getPage() {
		return super.getPage();
	}
}
