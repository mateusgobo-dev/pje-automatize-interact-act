package br.com.infox.pje.list;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Query;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.DAO.EntityListBeta;
import br.com.infox.cliente.component.NumeroProcesso;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ProcessoComAudienciaNaoMarcadaList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class ProcessoComAudienciaNaoMarcadaList extends EntityListBeta<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "processoComAudienciaNaoMarcadaList";
	private static final String DEFAULT_ORDER = "o.processoDocumento.dataInclusao";
	private NumeroProcesso numeroProcesso = new NumeroProcesso();
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private String nomeParte;
	private String usuarioInclusao;
	private Date dataInicio;
	private Date dataFim;
	private List<ProcessoTrf> selectedItens = new ArrayList<ProcessoTrf>();
	private Long resultadoTotal;

	@Override
	public void newInstance() {
		super.newInstance();
		this.nomeParte = null;
		this.numeroProcesso = new NumeroProcesso();
		this.classeJudicial = null;
		this.assuntoTrf = null;
		this.nomeParte = null;
		this.usuarioInclusao = null;
		this.dataInicio = null;
		this.dataFim = null;
		this.resultadoTotal = null;		
		
	}

	@Override
	public List<ProcessoTrf> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}
	
	@Override
	protected String getDefaultEjbql() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrf o where o.orgaoJulgador.idOrgaoJulgador = #{orgaoJulgadorAtual.idOrgaoJulgador}"); 
		sb.append(" and o.processoAudienciaList.size=0	and o.numeroSequencia is not null");	
		return sb.toString();
	}

	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
        map.put("numeroProcesso", "o.processo.numeroProcesso");
        map.put("classeJudicial", "o.classeJudicial");
//        map.put("assunto", "o.processoDocumento.tipoProcessoDocumento");
        map.put("dataDeProtocoloDoDocumento", "o.processoDocumento.dataDistribuicao");
//        map.put("tarefa", "o.tarefa");
		return map;
	}	
	
	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}


	@Override
	protected void addSearchFields() {
	}


	public NumeroProcesso getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(NumeroProcesso numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getUsuarioInclusao() {
		return usuarioInclusao;
	}

	public void setUsuarioInclusao(String usuarioInclusao) {
		this.usuarioInclusao = usuarioInclusao;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public List<ProcessoTrf> getSelectedItens() {
		return selectedItens;
	}

	public void setSelectedItens(List<ProcessoTrf> selectedItens) {
		this.selectedItens = selectedItens;
	}

	public Long getResultadoGeral() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("select count(o) from ProcessoTrf o where o.orgaoJulgador.idOrgaoJulgador = #{orgaoJulgadorAtual.idOrgaoJulgador}"); 
		sb.append(" and o.processoAudienciaList.size=0	and o.numeroSequencia is not null and  o.deveMarcarAudiencia = true");	

		Query q = getEntityManager().createQuery(sb.toString());
		return (Long) q.getSingleResult();
	}

	public Long getResultadoTotal(){
		if (resultadoTotal != null) {
			return resultadoTotal;
		}
		this.resultadoTotal = getResultadoGeral();
		return resultadoTotal;
	}
	public void refreshResultadoTotal(){
		this.resultadoTotal = null;
		this.resultadoTotal = getResultadoGeral();
		this.refresh();
	}

}