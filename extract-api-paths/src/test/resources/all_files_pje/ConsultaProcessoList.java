package br.com.infox.pje.list;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Expressions.ValueExpression;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrf;
import br.jus.pje.nucleo.entidades.Estado;

/**
 * Classe responsável por realizar as consultas necessárias para a funcionalidade de Consulta Pública. 
 */
@Name(ConsultaProcessoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ConsultaProcessoList extends EntityList<ConsultaProcessoTrf> {

	public static final String NAME = "consultaProcessoList";
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_EJBQL = "select o from ConsultaProcessoTrf o "
			+ " where o.segredoJustica = false "
			+ " and o.processoStatus = 'D' "
			+ " and not exists (select 1 "
			+ "			from  ProcessoParte a "
			+ "			where a.processoTrf.idProcessoTrf = o.idProcessoTrf "
			+ "			and   a.parteSigilosa = 'S') ";
	
	private static final String DEFAULT_ORDER = "numeroProcesso desc";
	
	
	private static final String R1 = "false = #{consultaProcessoList.validarAcaoBotao or not util.ajaxRequest}";
	
	private static final String R2 = " exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "+
			" where pp.processoTrf.idProcessoTrf = o.idProcessoTrf "+
			" and  pp.inSituacao = 'A' "+
			" and  pp.tipoParte.tipoParte = 'ADVOGADO' "+
			" and lower(to_ascii(pp.pessoa.nome)) like "+ 
			" '%' || lower(to_ascii(#{consultaProcessoList.nomeAdvogado.replace(\" \", \"%\")})) || '%') ";
	
	private static final String R3 = " exists (select pp.processoTrf from ProcessoParte pp "+
		      " where pp.processoTrf = o.processoTrf "+ 
		      " and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "+
		      "      where pdi.tipoDocumento.codTipo = 'CPF' "+ 
			  " and pdi.numeroDocumento like concat('%',#{consultaProcessoList.numeroCPF},'%')))";
	
	private static final String R4 = " exists (select pp.processoTrf from ProcessoParte pp "+ 
		      " where pp.processoTrf = o.processoTrf "+ 
		      " and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "+
		      "      where pdi.tipoDocumento.codTipo = 'CPJ' "+ 
			  " and pdi.numeroDocumento like concat('%',#{consultaProcessoList.numeroCNPJ},'%'))) ";
	
	private static final String R5 = " exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "+
			" where pp.processoTrf.idProcessoTrf = o.idProcessoTrf "+ 
			" and lower(to_ascii(pp.pessoa.nome)) like "+ 
			" '%' || lower(to_ascii(#{consultaProcessoList.nomeParte})) || '%')";
	
	private static final String R6 = " o.idProcessoTrf in (select p.idProcessoTrf from ProcessoTrf p "+
			 " inner join p.processoParteList ppList "+
			 " inner join ppList.processoParteAdvogadoList ppaList "+
			 " inner join ppaList.pessoaAdvogado pa "+ 
			 " where p = o and lower(pa.numeroOAB) = lower(to_ascii(#{consultaProcessoList.numeroOABParte})))";
	
	private static final String R7 = " o.idProcessoTrf in (select p.idProcessoTrf from ProcessoTrf p "+
						 " inner join p.processoParteList ppList "+
						 " inner join ppList.processoParteAdvogadoList ppaList "+
						 " inner join ppaList.pessoaAdvogado pa "+ 
            			 " where p = o and pa.letraOAB like concat('%',lower(to_ascii(#{consultaProcessoList.letraOABParte})),'%'))";
	
	private static final String R8 = " o.idProcessoTrf in (select p.idProcessoTrf from ProcessoTrf p "+
						 " inner join p.processoParteList ppList "+
						 " inner join ppList.processoParteAdvogadoList ppaList "+
						 " inner join ppaList.pessoaAdvogado pa "+ 
            			 " where p = o and pa.ufOAB = #{consultaProcessoList.ufOABParte}) ";
	
	private static final String R9 = "o.numeroProcesso like #{consultaProcessoList.numeroProcesso}";
	private static final String R10 = " o.classeJudicialObj.idClasseJudicial = #{consultaProcessoList.entity.classeJudicialObj.idClasseJudicial} ";
		
	/**
	 * Variável responsável por garantir que a ação do botão Limpar além de realizar tal ação nos
	 * campos do formulário, traga também uma tabela sem tuplas retornadas pela EJBQL padrão da classe.
	 */
	private Boolean validarAcaoBotao;
	
	/**
	 * Variável responsável por armazenar a mensagem de erro na validação dos campos
	 * de pesquisa para que a mesma possa ser exibida posteriormente.
	 */
	private String mensagemErroValidacao;
	private String nomeAdvogado;
	private Estado ufOABParte;
	private String numeroOABParte;
	private String letraOABParte;	
	private String numeroCompletoOABParte;
	
	private boolean cpf; 
	
	private String numeroProcesso;
	private String numeroCPF;
	private String numeroCNPJ;
	private String nomeParte;
	
	public ConsultaProcessoList() {
		super();
		setEjbql(DEFAULT_EJBQL);
		setOrder(DEFAULT_ORDER);
		setMaxResults(DEFAULT_MAX_RESULT);
		this.validarAcaoBotao = true;
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getResultList() {
		//Cria lista de resultado
		List listaResultado = Collections.EMPTY_LIST;
		//Variável que identifica se existem restrições
		Boolean possuiRestricoes = false;

		//Chama métodos que seta e recupera restrições
		setRestrictions();
		List<ValueExpression> restricoes = getRestrictions();

		//Verifica se existe restrição
		for (int i = 0; i < getRestrictions().size(); i++) {
			Object parameterValue = restricoes.get(i).getValue();
			if (isRestrictionParameterSet(parameterValue)) {
				possuiRestricoes = true;
				break;
			}
		}
		
		if (possuiRestricoes) {
			listaResultado = super.getResultList();
		}

		return listaResultado;
	}
		
	@Override
	protected void addSearchFields() {
		addSearchField("validarAcaoBotao", SearchCriteria.igual, R1);
		addSearchField("processoTrf.nomeAdvogado", SearchCriteria.igual, R2);
		addSearchField("processoTrf.numeroCPF", SearchCriteria.igual, R3);
		addSearchField("processoTrf.numeroCNPJ", SearchCriteria.igual, R4);
		addSearchField("processoTrf.nomeParte", SearchCriteria.igual, R5);
		addSearchField("numeroOAB", SearchCriteria.igual, R6);
		addSearchField("letraOAB", SearchCriteria.igual, R7);
		addSearchField("ufOAB", SearchCriteria.igual, R8);
		addSearchField("numeroProcesso", SearchCriteria.igual, R9);
		addSearchField("processoTrf.classeJudicial", SearchCriteria.igual, R10);
	}
	
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}
	
	public boolean showGrid() {
		return false;
	}	
	
	public Boolean getValidarAcaoBotao() {
		return validarAcaoBotao;
	}

	public void setValidarAcaoBotao(Boolean validarAcaoBotao) {
		this.validarAcaoBotao = validarAcaoBotao;
	}
	
	public String getMensagemErroValidacao(){
		return mensagemErroValidacao;
	}
	
	public void setMensagemErroValidacao(String mensagemErroValidacao){
		this.mensagemErroValidacao = mensagemErroValidacao;
	}

	public String getNomeAdvogado() {
		return this.nomeAdvogado;
	}

	public void setNomeAdvogado(String nomeAdvogado) {
		this.nomeAdvogado = nomeAdvogado;
	}

	public Estado getUfOABParte() {
		return this.ufOABParte;
	}

	public void setUfOABParte(Estado ufOABParte) {
		this.ufOABParte = ufOABParte;
	}

	public String getNumeroOABParte() {
		return this.numeroOABParte;
	}

	public void setNumeroOABParte(String numeroOABParte) {
		this.numeroOABParte = numeroOABParte;
	}

	public String getLetraOABParte() {
		return this.letraOABParte;
	}

	public void setLetraOABParte(String letraOABParte) {
		this.letraOABParte = letraOABParte;
	}

	public String getNumeroCompletoOABParte() {		
		numeroCompletoOABParte = null;		
		if((this.ufOABParte != null ) && (this.numeroOABParte != null) && (this.letraOABParte != null)){
			this.numeroCompletoOABParte = this.ufOABParte.getIdEstado() + "-" + this.numeroOABParte.toLowerCase() + "-" + this.letraOABParte.toLowerCase();
		}
		return this.numeroCompletoOABParte;
	}
	
	public String getNumeroProcesso() {
		return numeroProcesso;
	}
	
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}
	
	public boolean getCpf(){
		return cpf;
	}
	
	public void setCpf(boolean cpf){
		this.cpf = cpf;
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

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}
	
	public void clearCpfCnpj() {
		setNumeroCPF(null);
		setNumeroCNPJ(null);
	}
        
    @Override
    public void newInstance() {
        super.newInstance();
        setNumeroProcesso(null);
        setNomeParte(null);
        setNomeAdvogado(null);
        setNumeroCPF(null);
        setNumeroCNPJ(null);
        setNumeroOABParte(null);
    }
}
