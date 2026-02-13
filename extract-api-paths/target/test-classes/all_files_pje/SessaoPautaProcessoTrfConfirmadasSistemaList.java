package br.com.infox.pje.list;

import java.util.Date;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.bean.ConsultaProcesso;
import br.com.infox.cliente.component.NumeroProcesso;
import br.com.infox.cliente.home.ConsultaProcessoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoSessao;
import br.jus.pje.nucleo.entidades.Usuario;

@Name(SessaoPautaProcessoTrfConfirmadasSistemaList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class SessaoPautaProcessoTrfConfirmadasSistemaList extends EntityList<SessaoPautaProcessoTrf> {

	public static final String NAME = "sessaoPautaProcessoTrfConfirmadasSistemaList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.processoJudicial";

	private NumeroProcesso numeroProcesso = new NumeroProcesso();
	private String classeJudicial;
	private String assuntoTrf;
	private String nomeParte;
	private boolean relatorio;
	private Date dtDistribuicaoInicio;
	private Date dtDistribuicaoFim;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private OrgaoJulgador orgaoJulgador;
	private TipoSessao tipoSessao;

	private static final String R1 = "proc.numeroSequencia = #{sessaoPautaProcessoTrfConfirmadasSistemaList.numeroProcesso.numeroSequencia}";
	private static final String R2 = "proc.numeroDigitoVerificador = #{sessaoPautaProcessoTrfConfirmadasSistemaList.numeroProcesso.numeroDigitoVerificador}";
	private static final String R3 = "proc.ano = #{sessaoPautaProcessoTrfConfirmadasSistemaList.numeroProcesso.ano}";
	private static final String R4 = "proc.numeroOrigem = #{sessaoPautaProcessoTrfConfirmadasSistemaList.numeroProcesso.numeroOrigem}";
	private static final String R5 = "proc.numeroOrgaoJustica = #{sessaoPautaProcessoTrfConfirmadasSistemaList.numeroProcesso.numeroOrgaoJustica}";
	private static final String R6 = "proc.orgaoJulgador = #{sessaoPautaProcessoTrfConfirmadasSistemaList.orgaoJulgador}";
	private static final String R7 = "lower(to_ascii(proc.classeJudicial.classeJudicial)) like '%' || lower(to_ascii(#{sessaoPautaProcessoTrfIntimacoesList.classeJudicial})) || '%'";
	private static final String R8 = "proc in (select pa.processoTrf from ProcessoAssunto pa where "
			+ "lower(to_ascii(pa.assuntoTrf.assuntoTrf)) like '%' || lower(to_ascii(#{sessaoPautaProcessoTrfIntimacoesList.assuntoTrf})) || '%')";
	private static final String R9 = "proc in (select pp.processoTrf from ProcessoParte pp where pp.processoTrf = proc and "
			+ "lower(to_ascii(pp.pessoa.nome)) like '%' || lower(to_ascii(#{sessaoPautaProcessoTrfConfirmadasSistemaList.nomeParte})) || '%')";
	private static final String R10 = "proc in (select pp.processoTrf from ProcessoParte pp where pp.processoTrf = proc "
			+ "and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPF' and pdi.numeroDocumento like concat('%', #{consultaProcessoHome.instance.numeroCPF} ,'%')))";
	private static final String R11 = "proc in (select pp.processoTrf from ProcessoParte pp "
			+ "where pp.processoTrf = proc and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPJ' and pdi.numeroDocumento like concat('%', #{consultaProcessoHome.instance.numeroCNPJ} ,'%')))";
	private static final String R12 = "proc.idProcessoTrf in (select c.processo.idProcesso from ProcessoDocumento c where c.processo.idProcesso = proc.idProcessoTrf and "
			+ "c.tipoProcessoDocumento.tipoProcessoDocumento like #{sessaoPautaProcessoTrfConfirmadasSistemaList.relatorio ? 'Relatório' : null})";
	private static final String R13 = "o.processoExpediente.sessao.tipoSessao =  #{sessaoPautaProcessoTrfConfirmadasSistemaList.tipoSessao}";
	private static final String R14 = "cast(proc.dataDistribuicao as date) >= #{sessaoPautaProcessoTrfConfirmadasSistemaList.dtDistribuicaoInicio}";
	private static final String R15 = "cast(proc.dataDistribuicao as date) <= #{sessaoPautaProcessoTrfConfirmadasSistemaList.dtDistribuicaoFim}";
	private static final String R16 = "proc.orgaoJulgadorColegiado = #{sessaoPautaProcessoTrfConfirmadasSistemaList.orgaoJulgadorColegiado}";
	
	@Override
	protected void addSearchFields() {
		addSearchField("processoTrf.numeroSequencia", SearchCriteria.contendo, R1);
		addSearchField("processoTrf.numeroDigitoVerificador", SearchCriteria.contendo, R2);
		addSearchField("processoTrf.ano", SearchCriteria.contendo, R3);
		addSearchField("processoTrf.numeroOrigem", SearchCriteria.contendo, R4);
		addSearchField("processoTrf.numeroOrgaoJustica", SearchCriteria.igual, R5);
		addSearchField("processoTrf.orgaoJulgador", SearchCriteria.igual, R6);
		addSearchField("processoTrf.classeJudicial", SearchCriteria.igual, R7);
		addSearchField("processoTrf.assuntoTrf", SearchCriteria.contendo, R8);
		addSearchField("processoTrf.nomeParte", SearchCriteria.contendo, R9);
		addSearchField("processoTrf.jurisdicao", SearchCriteria.igual, R10);
		addSearchField("processoTrf.valorCausa", SearchCriteria.igual, R11);
		addSearchField("processoTrf.idProcessoTrf", SearchCriteria.contendo, R12);
		addSearchField("sessao.tipoSessao", SearchCriteria.igual, R13);
		addSearchField("processoTrf.listaPartePassivo", SearchCriteria.igual, R14);
		addSearchField("processoTrf.dataDistribuicao", SearchCriteria.igual, R15);
		addSearchField("processoTrf.orgaoJulgadorColegiado", SearchCriteria.igual, R16);		
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoParteExpediente o ");
		sb.append("INNER JOIN o.processoJudicial proc  ");
		sb.append("INNER JOIN proc.processoParteList parte "); 
		sb.append("LEFT OUTER JOIN parte.processoParteRepresentanteList rep LEFT JOIN rep.representante pes WITH pes.idUsuario = #{usuarioLogado.idUsuario} ");
		sb.append("where o.dtCienciaParte is not null ");
		sb.append("and o.cienciaSistema = true ");
		sb.append("AND ((parte.processoParteRepresentanteList IS NOT EMPTY AND o.pessoaParte = parte.pessoa AND rep.representante.idUsuario = #{usuarioLogado.idUsuario}) OR o.pessoaParte IN (#{pessoaService.getRepresentados(usuarioLogado)})) ");
		sb.append("and o.processoExpediente.meioExpedicaoExpediente = 'E' ");
		sb.append("and o.pendencia is null ");
		sb.append("and o.processoExpediente.sessao.dataFechamentoPauta is not null ");
		sb.append("and cast(current_date as date) <= cast(o.dtPrazoLegal as date) ");
		return sb.toString();
	}

	@Override
	public String getGroupBy() {
		return "o";
	}
	
	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	public void setNumeroProcesso(NumeroProcesso numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public NumeroProcesso getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public String getClasseJudicial() {
		return classeJudicial;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setAssuntoTrf(String assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public String getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setRelatorio(boolean relatorio) {
		this.relatorio = relatorio;
	}

	public boolean isRelatorio() {
		return relatorio;
	}

	public void setDtDistribuicaoInicio(Date dtDistribuicaoInicio) {
		this.dtDistribuicaoInicio = dtDistribuicaoInicio;
	}

	public Date getDtDistribuicaoInicio() {
		return dtDistribuicaoInicio;
	}

	public void setDtDistribuicaoFim(Date dtDistribuicaoFim) {
		this.dtDistribuicaoFim = dtDistribuicaoFim;
	}

	public Date getDtDistribuicaoFim() {
		return dtDistribuicaoFim;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setTipoSessao(TipoSessao tipoSessao) {
		this.tipoSessao = tipoSessao;
	}

	public TipoSessao getTipoSessao() {
		return tipoSessao;
	}

	public String getIdProcessoParteExpedienteUsuarioLogado(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		ProcessoTrf processoTrf = sessaoPautaProcessoTrf.getProcessoTrf();
		Usuario usuario = Authenticator.getUsuarioLogado();
		String sql = "select o from ProcessoParteExpediente o "
				+ "where o.processoJudicial = :processoTrf and o.pessoaParte = :pessoa";
		Query query = EntityUtil.createQuery(sql);
		query.setParameter("processoTrf", processoTrf);
		query.setParameter("pessoa", usuario);
		ProcessoParteExpediente ppe = EntityUtil.getSingleResult(query);
		return Integer.toString(ppe.getIdProcessoParteExpediente());
	}

	@Override
	public void newInstance() {
		ConsultaProcesso instanceCP = ConsultaProcessoHome.instance().getInstance();
		instanceCP.setNumeroCPF(null);
		instanceCP.setNumeroCNPJ(null);
		this.assuntoTrf = null;
		this.classeJudicial = null;
		  setOrgaoJulgadorColegiado(null);
		setOrgaoJulgador(null);
		super.newInstance();
	}
	
	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}


	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}	
}
