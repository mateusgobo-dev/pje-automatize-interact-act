package br.com.infox.pje.list;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.util.Strings;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.bean.ConsultaProcesso;
import br.com.infox.cliente.home.ConsultaProcessoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ParametroHome;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.StringUtil;

@Name(ConsultaProcessoTerceirosList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ConsultaProcessoTerceirosList extends EntityList<ProcessoTrf> {
	public static final String NAME = "consultaProcessoTerceirosList";

	private static final long serialVersionUID = 1L;

	private String numeroProcesso;
	private String nomeParte;
	private AssuntoTrf assuntoTrf;
	private ClasseJudicial classeJudicial;
	private Date dataInicio;
	private Date dataFim;
	private Boolean cpf = Boolean.TRUE;
	private String numCpf;
	private String numCnpj;
	private String numeroOAB;
	private String letraOAB;
	private Estado ufOAB;
	
	/**
	 * Variável responsável por garantir que a ação do botão Limpar além de realizar tal ação nos
	 * campos do formulário, traga também uma tabela sem tuplas retornadas pela EJBQL padrão da classe.
	 */
	private Boolean validarAcaoBotao;
	/**
	 * Variável responsável por armazenar o número completo da OAB a ser pesquisada,
	 * tendo sido inserida na classe para que se realize a verificação do preenchimento
	 * completo e correto deste campo.
	 */
	private String numeroCompletoOAB;
	/**
	 * Variável responsável por armazenar a mensagem de erro na validação dos campos
	 * de pesquisa para que a mesma possa ser exibida posteriormente.
	 */
	private String mensagemErroValidacao;
	
	private PessoaProcurador procurador;

	@In
	private FacesMessages facesMessages;

	private static final String DEFAULT_ORDER = "idProcessoTrf";

	private static final String R1 = "o.numeroProcesso like concat('%', #{consultaProcessoTerceirosList.numeroProcesso}, '%')";
	private static final String R2 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ " where pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf " 
			+ " and pp.inSituacao = 'A' "			
			+ " and lower(to_ascii(pp.pessoa.nome)) like "
			+ " '%' || lower(to_ascii(#{consultaProcessoTerceirosList.nomeParte})) || '%')";

	private static final String R3 = " exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ " where pp.processoTrf.idProcessoTrf = o.idProcessoTrf "
			+ " and pp.inSituacao = 'A' "			
			+ " and pp.pessoa.idUsuario IN  "
			+ " (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ " where pdi.tipoDocumento.codTipo = 'CPF' and pdi.numeroDocumento like '%' || #{consultaProcessoHome.instance.numeroCPF} || '%'))";

	private static final String R4 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ " where pp.processoTrf.idProcessoTrf = o.idProcessoTrf "
			+ " and pp.inSituacao = 'A' "			
			+ " and pp.pessoa.idUsuario IN  "
			+ " (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ " where pdi.tipoDocumento.codTipo = 'CPJ' and pdi.numeroDocumento like '%' || #{consultaProcessoHome.instance.numeroCNPJ} || '%'))";

	private static final String R5 = "exists (select 1 from ProcessoTrf p inner join p.assuntoTrfList a where o = p and "
			+ "a.assuntoCompleto like concat(#{consultaProcessoTerceirosList.assuntoTrf.assuntoCompleto}, '%'))";

	private static final String R6 = "o.processoTrf.classeJudicial.classeJudicialCompleto like concat(#{consultaProcessoTerceirosList.classeJudicial.classeJudicialCompleto}, '%')";

	private static final String R7 = "cast(o.processoTrf.dataDistribuicao as date) >= #{consultaProcessoTerceirosList.dataInicio}";
	private static final String R8 = "cast(o.processoTrf.dataDistribuicao as date) <= #{consultaProcessoTerceirosList.dataFim}";

	private static final String R9 = "o.idProcessoTrf in (select p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.processoParteList ppList  inner join ppList.processoParteAdvogadoList ppaList "
			+ "inner join ppaList.pessoaAdvogado pa "
			+ "where p = o " 
			+ " and ppList.inSituacao = 'A' "			
			+ " and pa.ufOAB = #{consultaProcessoTerceirosList.ufOAB})";

	private static final String R10 = "o.idProcessoTrf in (select p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.processoParteList ppList inner join ppList.processoParteAdvogadoList ppaList "
			+ "inner join ppaList.pessoaAdvogado pa "
			+ "where p = o " 
			+ " and ppList.inSituacao = 'A' "			
			+ " and pa.numeroOAB like concat('%',lower(to_ascii(#{consultaProcessoTerceirosList.numeroOAB})),'%'))";

	private static final String R11 = "o.idProcessoTrf in (select p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.processoParteList ppList "
			+ "inner join ppList.processoParteAdvogadoList ppaList "
			+ "inner join ppaList.pessoaAdvogado pa "
			+ "where p = o "
			+ " and ppList.inSituacao = 'A' "			
			+ " and pa.letraOAB like concat('%',lower(to_ascii(#{consultaProcessoTerceirosList.letraOAB})),'%'))";

	private static final String R12 = "o.idProcessoTrf not in (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "inner join pp.processoParteRepresentanteList ppr "
			+ "where ppr.parteRepresentante.pessoa.idUsuario = #{authenticator.getUsuarioLogado().getIdUsuario()} "
			+ " and pp.inSituacao = 'A' and  ppr.parteRepresentante.inSituacao = 'A') "			
			;
	
	private static final String R13 = "false = #{consultaProcessoTerceirosList.validarAcaoBotao or not util.ajaxRequest}";

	@Override
	protected void addSearchFields() {
		addSearchField("numeroProcesso", SearchCriteria.contendo, R1);
		addSearchField("processoTrf.nomeParte", SearchCriteria.igual, R2);
		addSearchField("processoTrf.orgaoJulgador.orgaoJulgador", SearchCriteria.contendo, R3);
		addSearchField("processoTrf.orgaoJulgador.localizacao", SearchCriteria.contendo, R4);
		addSearchField("processoTrf.assuntoTrfList", SearchCriteria.igual, R5);
		addSearchField("processoTrf.classeJudicial", SearchCriteria.igual, R6);
		addSearchField("processoTrf.dataDistribuicao", SearchCriteria.igual, R7);
		addSearchField("processoTrf.dataAutuacao", SearchCriteria.igual, R8);
		addSearchField("ufOAB", SearchCriteria.igual, R9);
		addSearchField("numeroOAB", SearchCriteria.igual, R10);
		addSearchField("letraOAB", SearchCriteria.igual, R11);
		addSearchField("usuario", SearchCriteria.igual, R12);
		addSearchField("validarAcaoBotao", SearchCriteria.igual, R13);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	public List<ProcessoTrf> getResultList() {
		setEjbql(getDefaultEjbql());
				
		return super.getResultList();
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ConsultaProcessoTrfSemFiltro o where o.processoStatus in ('V','D') ").append(
				"and o.segredoJustica = false ");
		if (Authenticator.getPapelAtual().equals(ParametroUtil.instance().getPapelAdvogado())) {
			sb.append("and not exists (select prp.processoTrf.idProcessoTrf from ProcessoParte prp ")
					.append("where prp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf ")
					.append("and prp.inSituacao = 'A' ")
					.append("and prp.pessoa.idUsuario = #{authenticator.getUsuarioLogado().getIdUsuario()}) ");
		} else {
			Usuario usuTemp = Authenticator.getUsuarioLogado();
			PessoaProcurador pessoaProcTemp = EntityUtil.find(PessoaProcurador.class, usuTemp.getIdUsuario());
			if (pessoaProcTemp != null) {
				sb.append("and not exists (select prp.processoTrf.idProcessoTrf from ProcessoParte prp ")
						.append("where prp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf ")
						.append("and prp.inSituacao = 'A' ")
						.append("and prp.pessoa.idUsuario in (select ppe.pessoa.idUsuario from PessoaProcuradoriaEntidade ppe ")
						.append("where ppe.procuradoria.idProcuradoria in (select ppp.pessoaProcuradoriaEntidade.procuradoria.idProcuradoria from PessoaProcuradorProcuradoria ppp ")
						.append("where ppp.pessoaProcurador.idUsuario = #{authenticator.getUsuarioLogado().getIdUsuario()}) ")
						.append("and ppe.procuradoria.idProcuradoria = #{consultaProcessoTerceirosList.procurador.procuradoria.idProcuradoria} ")
						.append(")) ");
			}
		}
		return sb.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	public void newInstance() {
		numeroProcesso = null;
		nomeParte = null;
		classeJudicial = null;
		assuntoTrf = null;
		setCpf(Boolean.TRUE);
		setNumCpf(null);
		setNumCnpj(null);
		dataInicio = null;
		dataFim = null;
		numeroOAB = null;
		letraOAB = null;
		ufOAB = null;
		ConsultaProcessoHome.instance().newInstance();
		list();
		super.newInstance();
	}

	public void clearCpfCnpj() {
		setNumCpf(null);
		setNumCnpj(null);
	}

	@Override
	public List<ProcessoTrf> list(int maxResult) {
		if (!getShowGrid()) {
			return null;
		}
		return super.list(maxResult);
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setCpf(Boolean cpf) {
		this.cpf = cpf;
	}

	public Boolean getCpf() {
		return cpf;
	}

	public void setNumCpf(String numCpf) {
		this.numCpf = numCpf;
	}

	public String getNumCpf() {
		return numCpf;
	}

	public void setNumCnpj(String numCnpj) {
		this.numCnpj = numCnpj;
	}

	public String getNumCnpj() {
		return numCnpj;
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

	public void setNumeroOAB(String numeroOAB) {
		this.numeroOAB = numeroOAB;
	}

	public String getNumeroOAB() {
		return numeroOAB;
	}

	public void setLetraOAB(String letraOAB) {
		this.letraOAB = letraOAB;
	}

	public String getLetraOAB() {
		return letraOAB;
	}

	public void setUfOAB(Estado ufOAB) {
		this.ufOAB = ufOAB;
	}

	public Estado getUfOAB() {
		return ufOAB;
	}

	private boolean isNumeroVazio() {
		if (getNumeroProcesso() == null) {
			return true;
		}
		return StringUtil.limparCharsNaoNumericos(getNumeroProcesso()).equals(
				ParametroHome.getFromContext("numeroOrgaoJustica", true));
	}

	public Boolean getShowGrid() {
		ConsultaProcesso consultaProcesso = ConsultaProcessoHome.instance().getInstance();
		return (getNomeParte() != null || getAssuntoTrf() != null || getClasseJudicial() != null || getNumCpf() != null
				|| getNumCnpj() != null || !Strings.isEmpty(getNumeroOAB()) || getUfOAB() != null
				|| getLetraOAB() != null || getDataInicio() != null || getDataFim() != null || !isNumeroVazio()
				|| !Strings.isEmpty(consultaProcesso.getNumeroCPF()) || !Strings.isEmpty(consultaProcesso
				.getNumeroCNPJ()));
	}

	public PessoaProcurador getProcurador() {
		Usuario usuTemp = Authenticator.getUsuarioLogado();
		setProcurador(EntityUtil.find(PessoaProcurador.class, usuTemp.getIdUsuario()));
		return procurador;
	}

	public void setProcurador(PessoaProcurador procurador) {
		this.procurador = procurador;
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

	public String getNumeroCompletoOAB() {		
		numeroCompletoOAB = null;
		String letraOAB = ProcessoTrfInicialAdvogadoList.instance().getLetraOAB();
		if((this.ufOAB != null ) && (this.numeroOAB != null) && (letraOAB != null)){
			this.numeroCompletoOAB = this.ufOAB.getIdEstado() + "-" + this.numeroOAB.toLowerCase() + "-" + letraOAB.toLowerCase();
		}
		return this.numeroCompletoOAB;
	}

}
