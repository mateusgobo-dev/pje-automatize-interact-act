package br.com.infox.pje.list;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.util.Strings;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.component.NumeroProcesso;
import br.com.infox.cliente.component.tree.JurisdicaoTreeHandler;
import br.com.infox.cliente.home.PainelUsuarioAdvogadoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrf;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;

@Name(ProcessoTrfInicialAdvogadoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoTrfInicialAdvogadoList extends EntityList<ConsultaProcessoTrf> {

	public static final String NAME = "processoTrfInicialAdvogadoList";

	private static final long serialVersionUID = 1L;

	private NumeroProcesso numeroProcesso = new NumeroProcesso();
	private AssuntoTrf assuntoTrf;
	private OrgaoJulgador orgaoJulgador;
	private ClasseJudicial classeJudicial;
	private PrioridadeProcesso prioridadeProcesso;
	private Date dataDistribuicaoFinal;
	private Date dataDistribuicaoInicial;
	private Date dataAutuacaoInicialParte;
	private Date dataAutuacaoFinalParte;
	private Date nascimentoFinalParte;
	private Date nascimentoInicialParte;
	private String processoParte;
	private String situacaoProcesso;
	private String numeroOAB;
	private String letraOAB;
	private Estado ufOAB;
	private String numeroCNPJ;
	private String numeroCPF;
	private boolean checkCpfCnpj;
	private boolean caixaPendentes = true;
	private boolean cpf = false;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;

	private static final String DEFAULT_ORDER = "o.idProcessoTrf";

	private static final String R1 = "o.processoTrf.numeroSequencia = #{processoTrfInicialAdvogadoList.numeroProcesso.numeroSequencia}";
	private static final String R2 = "o.processoTrf.ano = #{processoTrfInicialAdvogadoList.numeroProcesso.ano}";
	private static final String R3 = "o.processoTrf.numeroDigitoVerificador = #{processoTrfInicialAdvogadoList.numeroProcesso.numeroDigitoVerificador}";
	private static final String R4 = "o.processoTrf.numeroOrgaoJustica = #{processoTrfInicialAdvogadoList.numeroProcesso.numeroOrgaoJustica}";
	private static final String R5 = "o.processoTrf.numeroOrigem = #{processoTrfInicialAdvogadoList.numeroProcesso.numeroOrigem}";

	private static final String R6 = "o.processoTrf.idProcessoTrf in (select distinct p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.assuntoTrfList a "
			+ "where o = p and "
			+ "a = #{processoTrfInicialAdvogadoList.assuntoTrf})";

	private static final String R7 = "o.processoTrf.classeJudicial = #{processoTrfInicialAdvogadoList.classeJudicial}";

	private static final String R8 = "o.processoTrf.idProcessoTrf in (select distinct p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.prioridadeProcessoList pp "
			+ "where o = p and "
			+ "pp = #{processoTrfInicialAdvogadoList.prioridadeProcesso})";

	private static final String R9 = "cast(o.dataDistribuicao as date) >= #{processoTrfInicialAdvogadoList.dataDistribuicaoInicial}";
	private static final String R10 = "cast(o.dataDistribuicao as date) <= #{processoTrfInicialAdvogadoList.dataDistribuicaoFinal}";

	private static final String R11 = "o.processoTrf in (select pp.processoTrf from ProcessoParte pp "
			+ "where pp.pessoa in (select pd.pessoa from PessoaDocumentoIdentificacao pd "
			+ "where pd.tipoDocumento.tipoDocumento = 'CPF' "
			+ "and pd.numeroDocumento = #{processoTrfInicialAdvogadoList.numeroCPF}))";

	private static final String R12 = "o.processoTrf in (select pp.processoTrf from ProcessoParte pp "
			+ "where pp.pessoa in (select pd.pessoa from PessoaDocumentoIdentificacao pd "
			+ "where pd.tipoDocumento.tipoDocumento = 'CNPJ' "
			+ "and pd.numeroDocumento = #{processoTrfInicialAdvogadoList.numeroCNPJ}))";

	private static final String R13 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "
			+ "and lower(to_ascii(pp.pessoa.nome)) like "
			+ "'%' || lower(to_ascii(#{processoTrfInicialAdvogadoList.processoParte})) || '%')";

	private static final String R14 = "o.processoTrf in (select pp.processoTrf from ProcessoParte pp "
			+ "where pp.pessoa.idUsuario in (select pf.idUsuario from PessoaFisica pf "
			+ "where pf.dataNascimento <= #{processoTrfInicialAdvogadoList.nascimentoFinalParte}))";

	private static final String R15 = "o.processoTrf in (select pp.processoTrf from ProcessoParte pp "
			+ "where pp.pessoa.idUsuario in (select pf.idUsuario from PessoaFisica pf "
			+ "where pf.dataNascimento >= #{processoTrfInicialAdvogadoList.nascimentoInicialParte}))";

	private static final String R17 = "o.jurisdicao = #{painelUsuarioAdvogadoHome.jurisdicao} ";
	private static final String R18 = "o.idProcessoTrf in (select pcap.processoTrf.idProcessoTrf from ProcessoCaixaAdvogadoProcurador pcap "
			+ "where pcap.caixaAdvogadoProcurador.idCaixaAdvogadoProcurador = #{painelUsuarioAdvogadoHome.idCaixaAdvogadoProcurador}) ";

	/*
	 * [PJEII-3543] : Fernando Barreira (31/10/2012)
	 * Alteração na filtragem da lista de processos do nó raiz, para excluir aqueles que estiverem em caixas 
	 * da respectiva localização do advogado/procurador. 
	 */
	private static final String R20 = "(#{empty painelUsuarioAdvogadoHome.idCaixaAdvogadoProcurador} = false "
			+ "or o.idProcessoTrf not in (select pcap.processoTrf.idProcessoTrf from ProcessoCaixaAdvogadoProcurador pcap  "
			+ "where pcap.caixaAdvogadoProcurador.localizacao.idLocalizacao = " + Authenticator.getLocalizacaoAtual().getIdLocalizacao() + "))";

	private static final String R22 = "o.idProcessoTrf in (select p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.processoParteList ppList "
			+ "inner join ppList.processoParteAdvogadoList ppaList "
			+ "inner join ppaList.pessoaAdvogado pa "
			+ "where p = o and pa.numeroOAB like concat('%',lower(to_ascii(#{processoTrfInicialAdvogadoList.numeroOAB})),'%'))";
	private static final String R23 = "o.idProcessoTrf in (select p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.processoParteList ppList "
			+ "inner join ppList.processoParteAdvogadoList ppaList "
			+ "inner join ppaList.pessoaAdvogado pa "
			+ "where p = o and pa.letraOAB like concat('%',lower(to_ascii(#{processoTrfInicialAdvogadoList.letraOAB})),'%'))";
	private static final String R24 = "o.idProcessoTrf in (select p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.processoParteList ppList " + "inner join ppList.processoParteAdvogadoList ppaList "
			+ "inner join ppaList.pessoaAdvogado pa "
			+ "where p = o and pa.ufOAB = #{processoTrfInicialAdvogadoList.ufOAB})";

	private static final String R25 = "cast(o.dataDistribuicao as date) >= #{processoTrfInicialAdvogadoList.dataAutuacaoInicialParte}";
	private static final String R26 = "cast(o.dataDistribuicao as date) <= #{processoTrfInicialAdvogadoList.dataAutuacaoFinalParte}";
	private static final String R27 = "o.orgaoJulgadorColegiado = #{processoTrfInicialAdvogadoList.orgaoJulgadorColegiado.orgaoJulgadorColegiado}";
	private static final String R28 = "o.orgaoJulgador = #{processoTrfInicialAdvogadoList.orgaoJulgador.orgaoJulgador}";

	public static ProcessoTrfInicialAdvogadoList instance() {
		return ComponentUtil.getComponent(ProcessoTrfInicialAdvogadoList.NAME);
	}

	@Override
	protected void addSearchFields() {
		addSearchField("processoTrf.numeroSequencia", SearchCriteria.igual, R1);
		addSearchField("processoTrf.ano", SearchCriteria.igual, R2);
		addSearchField("processoTrf.numeroDigitoVerificador", SearchCriteria.igual, R3);
		addSearchField("processoTrf.numeroOrgaoJustica", SearchCriteria.igual, R4);
		addSearchField("processoTrf.numeroOrigem", SearchCriteria.igual, R5);
		addSearchField("processoTrf.assuntoTrfList", SearchCriteria.igual, R6);
		addSearchField("processoTrf.classeJudicial", SearchCriteria.igual, R7);
		addSearchField("processoTrf.prioridadeProcessoList", SearchCriteria.igual, R8);
		addSearchField("dataDistribuicaoInicial", SearchCriteria.igual, R9);
		addSearchField("dataDistribuicaoFinal", SearchCriteria.igual, R10);
		addSearchField("instance.numeroCPF", SearchCriteria.contendo, R11);
		addSearchField("instance.numeroCNPJ", SearchCriteria.contendo, R12);
		addSearchField("processoTrf.pessoaMarcouPauta", SearchCriteria.contendo, R13);
		addSearchField("nascimentoFinalParte", SearchCriteria.igual, R14);
		addSearchField("nascimentoInicialParte", SearchCriteria.igual, R15);
		addSearchField("jurisdicao", SearchCriteria.igual, R17);
		addSearchField("idCaixaAdvogadoProcurador", SearchCriteria.igual, R18);
		addSearchField("painelUsuarioAdvogado", SearchCriteria.igual, R20);
		addSearchField("numeroOABParte", SearchCriteria.igual, R22);
		addSearchField("letraOABParte", SearchCriteria.igual, R23);
		addSearchField("ufOABParte", SearchCriteria.igual, R24);
		addSearchField("autuacaoInicialParte", SearchCriteria.igual, R25);
		addSearchField("autuacaoFinalParte", SearchCriteria.igual, R26);
		addSearchField("orgaoJulgadorColegiado", SearchCriteria.igual, R27);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R28);		
	}

	@Override
	public List<ConsultaProcessoTrf> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ConsultaProcessoTrf o where o.processoStatus = 'D' ");
		if (getSituacaoProcesso() != null || getCaixaPendentes()) {
			if (!Strings.isEmpty(getSituacaoProcesso()) && getSituacaoProcesso().equals("Pendentes de Recebimento")) {
				sb.append(getEjbqlFiltroPendenteRecebimento());
			} else {
				sb.append(getEjbqlFiltroPendentes());
			}
		}
		return sb.toString();
	}

	public String getEjbqlFiltroPendentes() {
		String SQL_NAO_RESPONDIDO = "AND o.idProcessoTrf IN (SELECT DISTINCT proc.idProcessoTrf FROM ProcessoParteExpediente AS ppe " +
				"	INNER JOIN ppe.processoJudicial proc " +
				"	INNER JOIN proc.processoParteList parte " +
				"	LEFT OUTER JOIN parte.processoParteRepresentanteList rep LEFT JOIN rep.representante pes WITH pes.idUsuario = #{usuarioLogado.idUsuario} " +
				"	WHERE proc.processoStatus = 'D' AND ppe.tipoPrazo NOT IN ('S', 'C') " +
				"	AND ppe.resposta IS NULL AND (ppe.fechado = false AND ppe.dtCienciaParte IS NOT NULL AND ppe.pendenteManifestacao = true) AND ppe.dtPrazoLegal >= current_date " +
				"	AND (" +
				"		(parte.processoParteRepresentanteList IS NOT EMPTY " +
				"			AND ppe.pessoaParte = parte.pessoa " +
				"			AND rep.representante.idUsuario = #{usuarioLogado.idUsuario}) " +
				"		OR " +
				"			ppe.pessoaParte IN (#{pessoaService.getRepresentados(usuarioLogado)})" +
				"	)) ";
		return SQL_NAO_RESPONDIDO;
//		StringBuilder sb = new StringBuilder();
//		sb.append("and o.idProcessoTrf in (select ppe.processoJudicial.idProcessoTrf from ProcessoParteExpediente ppe ");
//		sb.append("where ppe.dtCienciaParte is not null and ");
//		sb.append("(ppe.pendenteManifestacao = true or ppe.resposta IS NULL) AND ");
//		sb.append("ppe.pessoaParte in (#{pessoaAdvogadoHome.pessoaAdvogadoProcurador})) ");
//		return sb.toString();
	}

	public String getEjbqlFiltroPendenteRecebimento() {
		StringBuilder sb = new StringBuilder();
		sb.append("and o.idProcessoTrf in (select ppe.processoJudicial.idProcessoTrf from ProcessoParteExpediente ppe ");
		sb.append("where ppe.dtCienciaParte is null and ");
		sb.append("ppe.pessoaParte in (#{pessoaAdvogadoHome.pessoaAdvogadoProcurador})) ");
		return sb.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	/**
	 * Método que limpa a instância da aba Caixas e da aba Pendência de
	 * Manifestação no Painel do Advogado / Procurador
	 */
	public void limparCaixaPendentes() {
		numeroProcesso.setNumeroProcesso(null);
		clearCpfCnpj();
		setAssuntoTrf(null);
		setOrgaoJulgadorColegiado(null);
		setOrgaoJulgador(null);
		setClasseJudicial(null);
		setPrioridadeProcesso(null);
		setDataDistribuicaoFinal(null);
		setDataDistribuicaoInicial(null);
		setDataAutuacaoInicialParte(null);
		setDataAutuacaoFinalParte(null);
		setNascimentoFinalParte(null);
		setNascimentoInicialParte(null);
		setProcessoParte(null);
		setSituacaoProcesso(null);
		setNumeroOAB(null);
		setLetraOAB(null);
		setUfOAB(null);
		setCpf(false);
	}

	public void clearCpfCnpj() {
		setNumeroCPF(null);
		setNumeroCNPJ(null);
	}

	public boolean getCaixaPedente() {
		return caixaPendentes;
	}

	public void setCaixaPedente(boolean caixaPendentes) {
		this.caixaPendentes = caixaPendentes;

		// Reconstruindo a tree
		JurisdicaoTreeHandler.instance().clearTree();
		PainelUsuarioAdvogadoHome.instance().onJurisdicaoSelected(new HashMap<String, Object>());
	}

	/**
	 * Método que retorna uma string para popular a combo de Pendências de
	 * Manifestação. Foi criado o método porque o TRF enviou uma imagem para
	 * deixar claro que era para ser uma combo e não um check
	 * 
	 * @return list - Retorna uma lista com com um único valor
	 */
	public List<String> pendenciaManifestacao() {
		List<String> list = new ArrayList<String>();
		list.add("Pendentes de Manifestação");
		list.add("Pendentes de Recebimento");
		return list;
	}

	public NumeroProcesso getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(NumeroProcesso numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public PrioridadeProcesso getPrioridadeProcesso() {
		return prioridadeProcesso;
	}

	public void setPrioridadeProcesso(PrioridadeProcesso prioridadeProcesso) {
		this.prioridadeProcesso = prioridadeProcesso;
	}

	public Date getDataDistribuicaoFinal() {
		return dataDistribuicaoFinal;
	}

	public void setDataDistribuicaoFinal(Date dataDistribuicaoFinal) {
		this.dataDistribuicaoFinal = dataDistribuicaoFinal;
	}

	public Date getDataDistribuicaoInicial() {
		return dataDistribuicaoInicial;
	}

	public void setDataDistribuicaoInicial(Date dataDistribuicaoInicial) {
		this.dataDistribuicaoInicial = dataDistribuicaoInicial;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public String getProcessoParte() {
		return processoParte;
	}

	public void setProcessoParte(String processoParte) {
		this.processoParte = processoParte;
	}

	public Date getNascimentoInicialParte() {
		return nascimentoInicialParte;
	}

	public void setNascimentoInicialParte(Date nascimentoInicialParte) {
		this.nascimentoInicialParte = nascimentoInicialParte;
	}

	public Date getNascimentoFinalParte() {
		return nascimentoFinalParte;
	}

	public void setNascimentoFinalParte(Date nascimentoFinalParte) {
		this.nascimentoFinalParte = nascimentoFinalParte;
	}

	public String getSituacaoProcesso() {
		return situacaoProcesso;
	}

	public void setSituacaoProcesso(String situacaoProcesso) {
		this.situacaoProcesso = situacaoProcesso;
	}

	public String getNumeroOAB() {
		return numeroOAB;
	}

	public void setNumeroOAB(String numeroOAB) {
		this.numeroOAB = numeroOAB;
	}

	public String getLetraOAB() {
		return letraOAB;
	}

	public void setLetraOAB(String letraOAB) {
		this.letraOAB = letraOAB;
	}

	public Estado getUfOAB() {
		return ufOAB;
	}

	public void setUfOAB(Estado ufOAB) {
		this.ufOAB = ufOAB;
	}

	public Boolean getCaixaPendentes() {
		return caixaPendentes;
	}

	public void setCaixaPendentes(boolean caixaPendentes) {
		this.caixaPendentes = caixaPendentes;
	}

	public String getNumeroCNPJ() {
		return numeroCNPJ;
	}

	public void setNumeroCNPJ(String numeroCNPJ) {
		this.numeroCNPJ = numeroCNPJ;
	}

	public String getNumeroCPF() {
		return numeroCPF;
	}

	public void setNumeroCPF(String numeroCPF) {
		this.numeroCPF = numeroCPF;
	}

	public boolean getCheckCpfCnpj() {
		return checkCpfCnpj;
	}

	public void setCheckCpfCnpj(boolean checkCpfCnpj) {
		this.checkCpfCnpj = checkCpfCnpj;
	}

	public Date getDataAutuacaoInicialParte() {
		return dataAutuacaoInicialParte;
	}

	public void setDataAutuacaoInicialParte(Date dataAutuacaoInicialParte) {
		this.dataAutuacaoInicialParte = dataAutuacaoInicialParte;
	}

	public Date getDataAutuacaoFinalParte() {
		return dataAutuacaoFinalParte;
	}

	public void setDataAutuacaoFinalParte(Date dataAutuacaoFinalParte) {
		this.dataAutuacaoFinalParte = dataAutuacaoFinalParte;
	}

	public Date dataExpediente(ConsultaProcessoTrf obj) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(ProcessoExpediente.class);
		criteria.add(Restrictions.eq("processoTrf", obj.getProcessoTrf()));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		ProcessoExpediente classe = (ProcessoExpediente)criteria.uniqueResult();
		return classe != null ? classe.getDtCriacao() : null;
	}

	public void setCpf(boolean cpf) {
		this.cpf = cpf;
	}

	public boolean isCpf() {
		return cpf;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}	
}