package br.jus.cnj.pje.list;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoPessoa;



@Name(AnalisePrevencaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AnalisePrevencaoList extends EntityList<ProcessoTrf>
{

	/*
	 * Esta classe foi criada utilizando as mesmas regras definidas 
	 * em analisePrevencaoGrid.component.xml. Esta classe foi criada
	 * como solução de contorno aos erros encontrados no formulário 
	 * de pesquisa do agrupador "Processos sob análise de prevenção".
	 */
	
	public static final String NAME = "analisePrevencaoList";

	private static final long serialVersionUID = -5509525397501668941L;

	private static final String DEFAULT_ORDER = "o.dataAutuacao DESC";

	private String numeroCPF;
	private String numeroCNPJ;
	private boolean cpf = false;
	private ClasseJudicial classeJudicial;
	private String nomeClasseJudicial;
	private AssuntoTrf assuntoTrf;
	private String nomeParte;
	private Date dataInicio;
	private Date dataFim;
	private OrgaoJulgador orgaoJulgador;
	private TipoPessoa tipoPessoa;
	private Long resultadoTotal;
	private String idLocFilhas;
	private boolean processoComParteSemCPFCNPJ;
	
	
	
	@Override
	public void newInstance() {
		super.newInstance();
		this.numeroCNPJ = null;
		this.numeroCPF = null;
		this.cpf = false;
		this.classeJudicial = null;
		this.assuntoTrf = null;
		this.nomeParte = null;
		this.dataInicio = null;
		this.dataFim = null;
		this.orgaoJulgador = null;
		this.tipoPessoa = null;
		this.resultadoTotal = null;
		processoComParteSemCPFCNPJ = false;
		this.idLocFilhas = (String)Contexts.getSessionContext().get(Authenticator.ID_LOCALIZACOES_FILHAS_ATUAIS);
	}
	
	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
        map.put("numeroProcessoPrevencaoCol", "(o.numeroSequencia, o.numeroDigitoVerificador, o.ano, o.numeroOrgaoJustica, o.numeroOrigem)");
        map.put("dataAutuacaoPrevencaoCol", "o.dataAutuacao");

        return map;
        
	}
	
	// Definindo query padrão
	@Override
	protected String getDefaultEjbql() {
		String query = "SELECT o FROM ProcessoTrf o WHERE o.idProcessoTrf in ( "+ 
				"SELECT c.processoTrf.idProcessoTrf FROM ProcessoTrfConexao c "+
				"WHERE c.dtValidaPrevencao is null AND c.tipoConexao = 'PR') ";
		return query;
	}
	
	// Definindo retrições dos campos de busca
	
	private static final String R1 = "exists (" +  
									 "select pp.processoTrf from ProcessoParte pp " + 
									 "where pp.processoTrf.idProcessoTrf = o.idProcessoTrf " + 
									 "and pp.inSituacao = 'A' " +
									 "and pp.pessoa.idUsuario IN (" +
									 		"select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi " + 
									 		"where pdi.tipoDocumento.codTipo = 'CPF' " +
									 		"and pdi.numeroDocumento like '%'|| #{analisePrevencaoList.numeroCPF} ||'%' )) ";
	
	private static final String R2 = "exists (" +  
			 						 "select pp.processoTrf from ProcessoParte pp " + 
			 						 "where pp.processoTrf.idProcessoTrf = o.idProcessoTrf " + 
			 						 	"and pp.inSituacao = 'A' " +
			 						 	"and pp.pessoa.idUsuario IN (" +
			 						 		"select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi " + 
			 						 		"where pdi.tipoDocumento.codTipo = 'CPJ' " +
			 						 			"and pdi.numeroDocumento like '%'|| #{analisePrevencaoList.numeroCNPJ} ||'%' )) ";
	
	private static final String R3 = "exists (select pp.processoTrf from ProcessoParte pp " +
									 "where pp.processoTrf.idProcessoTrf = o.idProcessoTrf " +
									 "and pp.inSituacao = 'A' " + 
									 "and pp.pessoa.tipoPessoa = #{analisePrevencaoList.tipoPessoa} ) ";
	
	private static final String R4 = "exists (select pp.processoTrf from ProcessoParte pp " +
									 "where pp.processoTrf.idProcessoTrf = o.idProcessoTrf "  +
									 "and lower(to_ascii(pp.pessoa.nome)) like " +
									 "'%' || lower(to_ascii(#{analisePrevencaoList.nomeParte})) || '%' ) ";
	
	private static final String R6 = "lower(to_ascii(o.classeJudicial.classeJudicial)) like '%' || lower(to_ascii(#{analisePrevencaoList.nomeClasseJudicial})) || '%' ";
	
	private static final String R7 = "o.orgaoJulgador = #{analisePrevencaoList.orgaoJulgador} ";
	
	private static final String R8 = "cast(o.dataAutuacao as date) >= #{analisePrevencaoList.dataInicio} ";
	
	private static final String R9 = "cast(o.dataAutuacao as date) <= #{analisePrevencaoList.dataFim} ";
		
	private static final String R10 = "(false = #{analisePrevencaoList.processoComParteSemCPFCNPJ} OR exists (" + 
										" SELECT pp.processoTrf FROM ProcessoParte pp" + 
											" WHERE pp.processoTrf.idProcessoTrf = o.idProcessoTrf" + 
											" AND pp.inSituacao = 'A'" +
											" AND pp.pessoa.tipoPessoa.idTipoPessoa != " + ParametroUtil.instance().getIdTipoPessoaAutoridade() + 
											" AND pp.pessoa.idUsuario NOT IN (" +
												"SELECT pdi.pessoa.idUsuario FROM PessoaDocumentoIdentificacao pdi " + 
												"WHERE pdi.tipoDocumento.codTipo IN( 'CPJ' ,'CPF') " +
												"AND pdi.ativo is TRUE ))) ";
	
	// Fim da definição das restrições
	
	@Override
	protected void addSearchFields()
	{
		addSearchField("o.processoTrf1", SearchCriteria.igual, R1);
		addSearchField("o.processoTrf2", SearchCriteria.igual, R2);
		addSearchField("o.processoTrf3", SearchCriteria.igual, R3);
		addSearchField("o.processoTrf4", SearchCriteria.igual, R4);
		addSearchField("o.processoTrf6", SearchCriteria.igual, R6);
		addSearchField("o.processoTrf7", SearchCriteria.igual, R7);
		addSearchField("o.processoTrf8", SearchCriteria.igual, R8);
		addSearchField("o.processoTrf9", SearchCriteria.igual, R9);
		addSearchField("o.processoTrf10", SearchCriteria.igual, R10);
		
	}
	
	public void limparPesquisa() {
		
	}
	
	
	@Override
	public List<ProcessoTrf> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}
	
	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}
	
	public void clearCpfCnpj()
	{
		setNumeroCNPJ(null);
		setNumeroCPF(null);
	}

	public String getNumeroCPF() {
		return numeroCPF;
	}

	public void setNumeroCPF(String numeroCPF) {
		this.numeroCPF = numeroCPF;
	}

	public String getNumeroCNPJ() {
		return numeroCNPJ;
	}

	public void setNumeroCNPJ(String numeroCNPJ) {
		this.numeroCNPJ = numeroCNPJ;
	}

	public boolean isCpf() {
		return cpf;
	}

	public void setCpf(boolean cpf) {
		this.cpf = cpf;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public String getNomeClasseJudicial() {
		return nomeClasseJudicial;
	}

	public void setNomeClasseJudicial(String nomeClasseJudicial) {
		this.nomeClasseJudicial = nomeClasseJudicial;
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

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	public Long getResultadoTotal() {
		if(resultadoTotal != null)
			return resultadoTotal;
		else
			this.resultadoTotal = getResultCount();
		return resultadoTotal;
	}

	public void setResultadoTotal(Long resultadoTotal) {
		this.resultadoTotal = resultadoTotal;
	}

	public String getIdLocFilhas() {
		return idLocFilhas;
	}

	public void setIdLocFilhas(String idLocFilhas) {
		this.idLocFilhas = idLocFilhas;
	}
	
	public void setProcessoComParteSemCPFCNPJ(boolean processoComParteSemCPFCNPJ) {
		this.processoComParteSemCPFCNPJ = processoComParteSemCPFCNPJ;
	}
	
	public boolean isProcessoComParteSemCPFCNPJ() {
		return processoComParteSemCPFCNPJ;
	}
	
	public void refreshTogglePrevencao() {
		setAssuntoTrf(null);
		setClasseJudicial(null);
		setDataFim(null);
		setDataInicio(null);
		setNomeParte(null);
		setNumeroCNPJ(null);
		setNumeroCPF(null);
		setResultadoTotal(null);
		setProcessoComParteSemCPFCNPJ(false);
		super.cleanSearch();
		refresh();
	}
	
}
