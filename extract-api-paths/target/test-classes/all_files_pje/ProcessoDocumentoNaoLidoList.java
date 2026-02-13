package br.com.infox.pje.list;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.model.SelectItem;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityListBeta;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.component.NumeroProcesso;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.PapelManager;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name(ProcessoDocumentoNaoLidoList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class ProcessoDocumentoNaoLidoList extends EntityListBeta<ProcessoDocumento> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "processoDocumentoNaoLidoList";
	private static final Integer LINHAS = 5;
	private static final String DEFAULT_ORDER = "o.processoDocumentoBin.dataAssinatura";
	private NumeroProcesso numeroProcesso = new NumeroProcesso();
	private Papel papelPesquisa;
	private Papel papelPesquisaAnterior = null;
	private String numeroCPF;
	private String numeroCNPJ;
	private boolean cpf = false;
	private ClasseJudicial classeJudicial;
	private String nomeClasseJudicial;
	private AssuntoTrf assuntoTrf;
	private String nomeParte;
	private String usuarioInclusao;
	private Date dataInicio;
	private Date dataFim;
	private List<ProcessoDocumento> selectedItens = new ArrayList<ProcessoDocumento>();
	private Long resultadoTotal;
	private boolean selecionouTodos = false;
	private List<Papel> papeisPesquisa;	
	private String numeroOab;
	private String letraOab;
	private Estado ufOab;

	@Override
	public void newInstance() {
		super.newInstance();
		this.nomeParte = null;
		this.numeroProcesso = new NumeroProcesso();
		this.papelPesquisa = null;
		this.papelPesquisaAnterior = null;
		this.numeroCPF = null;
		this.numeroCNPJ = null;
		this.cpf = false;
		this.classeJudicial = null;
		this.assuntoTrf = null;
		this.nomeParte = null;
		this.usuarioInclusao = null;
		this.dataInicio = null;
		this.dataFim = null;
		this.resultadoTotal = null;	
		this.papeisPesquisa=null;
		this.ufOab = null;
		this.letraOab = null;
		this.numeroOab = null;
		buscarPapeisDocumentosNaoLidos();
	}
	
	public List<Papel> getPapeisPesquisa() {
		return papeisPesquisa;
	}
	
	public void setPapeisPesquisa(List<Papel> papeisPesquisa) {
		this.papeisPesquisa = papeisPesquisa;
	}
	
	public void addRemoveSelectedItens(ProcessoDocumento procDocTrf) {
		if (selectedItens.contains(procDocTrf)) {
			selectedItens.remove(procDocTrf);
		} else {
			selectedItens.add(procDocTrf);
		}
	}

	public void addRemoveAllSelectedItens(boolean allSelected) {
		// adiciona ou remove todos da lista que realmente sera inserida
		if (allSelected) {
			List<ProcessoDocumento> listaProcDocTrfResult = this.getResultList();
			selectedItens.clear();
			selectedItens.addAll(listaProcDocTrfResult);
			setSelecionouTodos(allSelected);
		} else {
			selectedItens.clear();
			setSelecionouTodos(false);
		}
	}

	public boolean getValueAllSelected() {
		if (this.getResultCount() == selectedItens.size()) {
			return true;
		}
		return false;
	}

	@Override
	public List<ProcessoDocumento> getResultList() {
		boolean isPesquisasDiferentes = false;
		if (this.papelPesquisa == null) {
			isPesquisasDiferentes = this.papelPesquisaAnterior != null;
		} else {
			isPesquisasDiferentes = !this.papelPesquisa.equals(this.papelPesquisaAnterior);
		}
		if (isPesquisasDiferentes) {
			setEjbql(getDefaultEjbql());
			this.papelPesquisaAnterior = this.papelPesquisa;
		}
		return super.getResultList();
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT o FROM ProcessoDocumento o ");
		sb.append("INNER JOIN o.processoTrf p ");
		sb.append("WHERE o.lido = false	");

		buscarPapeisDocumentosNaoLidos();
		
		sb.append("AND (o.papel IN (#{processoDocumentoNaoLidoList.papeisPesquisa})) ");
		sb.append("and o.ativo = true ");
		sb.append("AND o.documentoPrincipal IS null ");
		sb.append("AND o.processoTrf.processoStatus = 'D'");
		sb.append("and o.dataJuntada > o.processoTrf.dataAutuacao ");
		
		if(Authenticator.getOrgaoJulgadorAtual() != null){
			sb.append("AND o.processoTrf.orgaoJulgador.idOrgaoJulgador = #{orgaoJulgadorAtual.idOrgaoJulgador}  ");
		}
		if(Authenticator.getOrgaoJulgadorColegiadoAtual() != null){
			sb.append(" AND o.processoTrf.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual.idOrgaoJulgadorColegiado} ");
		}
		
		if(!Authenticator.isVisualizaSigiloso()) {
			sb.append(" AND (p.segredoJustica = false "
					+ "OR exists (select 1 from "
					+ "ProcessoVisibilidadeSegredo pvs where pvs.pessoa = #{pessoaLogada} and pvs.processo.idProcesso = p.idProcessoTrf) "
					+ ") ");
			
			sb.append("AND (o.documentoSigiloso = false "
					+ "OR( exists(select 1 from ProcessoDocumentoVisibilidadeSegredo pdvs" +
					" where pdvs.processoDocumento.idProcessoDocumento = o.idProcessoDocumento" +
					" and pdvs.pessoa.idUsuario = #{processoDocumentoHome.usuarioLogado.idUsuario})" +
					" or (o.usuarioJuntada.idUsuario = #{usuarioLogado.idUsuario}))) ");
		}
		
		return sb.toString();
	}
	
	/**
	 * Permitir adição de papeis filtrados na list do agrupador "Documentos não lidos"
	 */
	private void buscarPapeisDocumentosNaoLidos() {
		PapelManager p = (PapelManager) Component.getInstance("papelManager");
		papeisPesquisa = p.getPapeisParaDocumentosNaoLidos();
	}

	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
        map.put("numeroProcesso", "o.processoTrf.processo.numeroProcesso");
        map.put("classeJudicial", "o.processoTrf.classeJudicial");
        map.put("tipoDeDocumento", "o.tipoProcessoDocumento");
        map.put("dataDeProtocoloDoDocumento", "o.dataInclusao");
        map.put("tarefa", "o.tarefa");
		return map;
	}	
	
	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	private static final String R1 = "o.processoTrf.numeroSequencia = #{processoDocumentoNaoLidoList.numeroProcesso.numeroSequencia} ";
	private static final String R2 = "o.processoTrf.ano = #{processoDocumentoNaoLidoList.numeroProcesso.ano} ";
	private static final String R3 = "o.processoTrf.numeroDigitoVerificador = #{processoDocumentoNaoLidoList.numeroProcesso.numeroDigitoVerificador} ";
	private static final String R4 = "o.processoTrf.numeroOrgaoJustica = #{processoDocumentoNaoLidoList.numeroProcesso.numeroOrgaoJustica} ";
	private static final String R5 = "o.processoTrf.numeroOrigem = #{processoDocumentoNaoLidoList.numeroProcesso.numeroOrigem} ";
	
	private static final String R6 = "o.processoTrf.idProcessoTrf in (select p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.processoParteList ppList " 
			+ "WHERE ppList.tipoParte = '" + ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte() +"' "
			+ "AND ppList.pessoa.idUsuario IN ("
				+ "SELECT pdi.pessoa.idUsuario FROM PessoaDocumentoIdentificacao pdi " 
				+ "WHERE pdi.tipoDocumento.codTipo = 'OAB' "
				+ "AND pdi.ativo is TRUE "
				+ "AND pdi.numeroDocumento like concat('%',lower(to_ascii(#{processoDocumentoNaoLidoList.numeroOab})),'%')))";
	
	private static final String R7 = "o.processoTrf.idProcessoTrf in (select p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.processoParteList ppList " 
			+ "WHERE ppList.tipoParte = '" + ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte() +"' "
			+ "AND ppList.pessoa.idUsuario IN ("
				+ "SELECT pdi.pessoa.idUsuario FROM PessoaDocumentoIdentificacao pdi " 
				+ "WHERE pdi.tipoDocumento.codTipo = 'OAB' "
				+ "AND pdi.ativo is TRUE "
				+ "AND pdi.numeroDocumento like concat('%',lower(to_ascii(#{processoDocumentoNaoLidoList.letraOab})),'%')))";
	
	private static final String R8 = "o.processoTrf.idProcessoTrf in (select p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.processoParteList ppList " 
			+ "WHERE ppList.tipoParte = '" + ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte() +"' "
			+ "AND ppList.pessoa.idUsuario IN ("
				+ "SELECT pdi.pessoa.idUsuario FROM PessoaDocumentoIdentificacao pdi " 
				+ "WHERE pdi.tipoDocumento.codTipo = 'OAB' "
				+ "AND pdi.ativo is TRUE "
				+ "AND pdi.estado = #{processoDocumentoNaoLidoList.ufOab} ))";

	private static final String R9 = "exists (select pp.processoTrf from ProcessoParte pp "
			+ "where pp.processoTrf = o.processoTrf and "
			+ "pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPF' and "
			+ "pdi.numeroDocumento like concat('%',#{processoDocumentoNaoLidoList.numeroCPF},'%')))";

	private static final String R10 = "exists (select pp.processoTrf from ProcessoParte pp "
			+ "where pp.processoTrf = o.processoTrf and "
			+ "pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPJ' and "
			+ "pdi.numeroDocumento like concat('%',#{processoDocumentoNaoLidoList.numeroCNPJ},'%')))";

	private static final String R11 = "lower(to_ascii(o.processoTrf.classeJudicial.classeJudicial)) like" +
			"'%' || lower(to_ascii(#{processoDocumentoNaoLidoList.nomeClasseJudicial})) || '%'";

	private static final String R13 = "exists (select pp.processoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "
			+ "and lower(to_ascii(pp.pessoa.nome)) like "
			+ "'%' || lower(to_ascii(#{processoDocumentoNaoLidoList.nomeParte})) || '%' )";

	private static final String R14 = "exists (select pp.processo.idProcesso from ProcessoDocumento pp "
			+ "where pp.processo.idProcesso = o.processoTrf.idProcessoTrf "
			+ "and lower(to_ascii(pp.usuarioInclusao.nome)) like "
			+ "'%' || lower(to_ascii(#{processoDocumentoNaoLidoList.usuarioInclusao})) || '%' )";

	private static final String R15 = "cast(o.processoDocumentoBin.dataAssinatura as date) >= #{processoDocumentoNaoLidoList.dataInicio}";

	private static final String R16 = "cast(o.processoDocumentoBin.dataAssinatura as date) <= #{processoDocumentoNaoLidoList.dataFim}";

	private List<SelectItem> listaPapeisItens;

	@Override
	protected void addSearchFields() {
		addSearchField("o.processoTrf1", SearchCriteria.igual, R1);
		addSearchField("o.processoTrf2", SearchCriteria.igual, R2);
		addSearchField("o.processoTrf3", SearchCriteria.igual, R3);
		addSearchField("o.processoTrf4", SearchCriteria.igual, R4);
		addSearchField("o.processoTrf5", SearchCriteria.igual, R5);
		addSearchField("o.processoTrf6", SearchCriteria.igual, R6);
		addSearchField("o.processoTrf7", SearchCriteria.igual, R7);
		addSearchField("o.processoTrf8", SearchCriteria.igual, R8);
		addSearchField("o.processoTrf9", SearchCriteria.igual, R9);
		addSearchField("o.processoTrf10", SearchCriteria.igual, R10);
		addSearchField("o.processoTrf11", SearchCriteria.igual, R11);
		addSearchField("o.processoTrf13", SearchCriteria.igual, R13);
		addSearchField("o.processoTrf14", SearchCriteria.igual, R14);
		addSearchField("o.processoTrf15", SearchCriteria.igual, R15);
		addSearchField("o.processoTrf16", SearchCriteria.igual, R16);
	}
	
	/**
	 * Permitir adição de papeis filtrados na list do agrupador "Documentos não lidos"
	 * 
	 * @return
	 */
	public List<SelectItem> listaDePapeis() {
		if (listaPapeisItens != null) {
			return listaPapeisItens;
		}
		listaPapeisItens = new ArrayList<SelectItem>();
		String papeis = ParametroUtil.getFromContext(Parametros.PJE_AGRUPADOR_DOCS_NAO_LIDOS_PAPEIS, false);
		StringTokenizer token = new StringTokenizer(papeis, ",");
		PapelManager p = (PapelManager) Component.getInstance("papelManager");
		Papel papel = null;
		listaPapeisItens.add(new SelectItem(new Papel(), "- - TODOS - -"));
		while (token.hasMoreElements()) {
			String strToken = token.nextToken();
			strToken = strToken.replaceAll("\n", "").trim();
			try {
				papel = p.findByCodeName(strToken);
				if (papel != null) {
					listaPapeisItens.add(new SelectItem(papel, papel.getNome().toUpperCase()));
				}
			} catch (PJeBusinessException e) {
				System.err.println("Não existe papel com identificador " + strToken);
			}

		}
		return listaPapeisItens;
	}

	public Papel getPapelPesquisa() {
		return papelPesquisa;
	}

	public void setPapelPesquisa(Papel papelPesquisa) {
		this.papelPesquisa = papelPesquisa;
	}

	public NumeroProcesso getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(NumeroProcesso numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
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

	public boolean getCpf() {
		return cpf;
	}

	public void setCpf(boolean cpf) {
		this.cpf = cpf;
	}

	public void limparCamposCPFCNPJ() {
		setNumeroCPF(null);
		setNumeroCNPJ(null);
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

	public List<ProcessoDocumento> getSelectedItens() {
		return selectedItens;
	}

	public void setSelectedItens(List<ProcessoDocumento> selectedItens) {
		this.selectedItens = selectedItens;
	}

	@SuppressWarnings("unchecked")
	public Long resultadoGeral() {
		List<Integer> lista;
		StringBuilder sb = new StringBuilder();

		sb.append("select o.idProcessoTrf from ConsultaProcessoTrf o");
		Query q = getEntityManager().createQuery(sb.toString());
		lista = q.getResultList();
		if (lista.isEmpty()) {
			return 0L;
		}

		sb = new StringBuilder();
		sb.append("select count(o) from ProcessoDocumentoTrf o ");
		sb.append("where o.processoDocumento not in (select pdl.processoDocumento from ProcessoDocumentoLido pdl) ");
		sb.append("and o.processoTrf.idProcessoTrf in (:lista)");
		sb.append("and (o.processoDocumento.papel = #{parametroUtil.papelAdvogado} ");
		sb.append("or o.processoDocumento.papel = #{parametroUtil.papelProcurador} ");
		sb.append("or o.processoDocumento.papel = #{parametroUtil.papelPerito}) ");
		
		// Ignorar documentos ja lidos se for advogado ou procurador
		sb.append("and (o.processoDocumento.papel = #{parametroUtil.papelPerito} OR ((o.processoDocumento.papel = #{parametroUtil.papelAdvogado} " +
						"OR (o.processoDocumento.papel = #{parametroUtil.papelProcurador})  ) " +
					   "AND o.processoDocumento not in (select pdpnl.processoDocumento from ProcessoDocumentoPeticaoNaoLida pdpnl))) ");
		
		// Considerar o sigilo por documento
		sb.append("AND ((o.processoDocumento.documentoSigiloso = true and exists(select 1 from ProcessoDocumentoVisibilidadeSegredo pdvs" +
				" where pdvs.processoDocumento.idProcessoDocumento = o.idProcessoDocumento" +
				" and pdvs.pessoa.idUsuario = #{processoDocumentoHome.usuarioLogado.idUsuario})" +
				" or #{authenticator.isMagistrado()} = true" +
				" or (o.processoDocumento.usuarioInclusao.idUsuario = #{usuarioLogado.idUsuario}))" +
				" or o.processoDocumento.documentoSigiloso = false) ");
		
		// Somente considerar documentos juntados
		sb.append(" AND (");
		sb.append("(o.processoDocumento.processoDocumentoBin.dataJuntada is not null)) ");
		
		// Retirar documentos anexos
		sb.append(" AND o.processoDocumento.documentoPrincipal is null");
		
		q = getEntityManager().createQuery(sb.toString());
		q.setParameter("lista", lista);
		return (Long) q.getSingleResult();
	}

	public Long getResultadoTotal(){
		if (resultadoTotal != null) {
			return resultadoTotal;
		}
		if (papelPesquisa == null) {
			refreshResultadoTotal();
		}

		this.resultadoTotal = getResultCount();
		return resultadoTotal;
	}
	public void refreshResultadoTotal(){
		this.resultadoTotal = null;
		Papel papelTemp = papelPesquisa;
		papelPesquisa = null;
		setEjbql(getDefaultEjbql());
		this.refresh();
		this.resultadoTotal = getResultCount();
		papelPesquisa = papelTemp;
		setEjbql(getDefaultEjbql());
		this.refresh();
	}
	
	public static Integer getLinhas() {
		return LINHAS;
	}
	
	public boolean isSelecionouTodos() {
		return selecionouTodos;
	}
	
	public void setSelecionouTodos(boolean selecionouTodos) {
		this.selecionouTodos = selecionouTodos;
	}

	public String getNumeroOab() {
		return numeroOab;
	}

	public void setNumeroOab(String numeroOab) {
		this.numeroOab = numeroOab;
	}

	public String getLetraOab() {
		return letraOab;
	}

	public void setLetraOab(String letraOab) {
		this.letraOab = letraOab;
	}

	public Estado getUfOab() {
		return ufOab;
	}

	public void setUfOab(Estado ufOab) {
		this.ufOab = ufOab;
	}
}
