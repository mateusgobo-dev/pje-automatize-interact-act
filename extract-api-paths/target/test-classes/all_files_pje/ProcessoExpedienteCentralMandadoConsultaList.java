package br.com.infox.pje.list;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.component.NumeroProcesso;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.CentralMandadoManager;
import br.jus.cnj.pje.nucleo.manager.GrupoOficialJusticaManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.service.LocalizacaoService;
import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.GrupoOficialJustica;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaOficialJustica;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ProcessoExpedienteCentralMandadoStatusEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name(ProcessoExpedienteCentralMandadoConsultaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoExpedienteCentralMandadoConsultaList extends EntityList<ProcessoExpedienteCentralMandado> {

	private static final long serialVersionUID = -2187467536995918068L;
	
	public static final String                         NAME             = "processoExpedienteCentralMandadoConsultaList";
    private static final String                        DEFAULT_ORDER    = "o.urgencia desc, o.dtDistribuicaoExpediente asc";
    private static final String                        R2               = "o.pessoaGrupoOficialJustica.grupoOficialJustica = #{processoExpedienteCentralMandadoConsultaList.grupoOficial}";
    private static final String                        R3               = "o.processoExpediente.tipoProcessoDocumento = #{processoExpedienteCentralMandadoConsultaList.tpProcessoDocumento}";
    private static final String                        R4               = "o.processoExpediente.processoTrf.orgaoJulgadorColegiado.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = ";
    private static final String                        R5               = "o.processoExpediente.processoTrf.orgaoJulgador.idOrgaoJulgador = (#{processoExpedienteCentralMandadoConsultaList.orgaoJulgador.idOrgaoJulgador})";
    private static final String                        R6_1             = "o.processoExpediente.processoTrf.numeroSequencia = #{processoExpedienteCentralMandadoConsultaList.numeroProcesso.numeroSequencia}";
    private static final String                        R6_2             = "o.processoExpediente.processoTrf.ano = #{processoExpedienteCentralMandadoConsultaList.numeroProcesso.ano}";
    private static final String                        R6_3             = "o.processoExpediente.processoTrf.numeroDigitoVerificador = #{processoExpedienteCentralMandadoConsultaList.numeroProcesso.numeroDigitoVerificador}";
    private static final String                        R6_4             = "o.processoExpediente.processoTrf.numeroOrgaoJustica = #{processoExpedienteCentralMandadoConsultaList.numeroProcesso.numeroOrgaoJustica}";
    private static final String                        R6_5             = "o.processoExpediente.processoTrf.numeroOrigem = #{processoExpedienteCentralMandadoConsultaList.numeroProcesso.numeroOrigem}";
    private static final String                        R6_6             = "o.processoExpediente.processoTrf.classeJudicial = #{processoExpedienteCentralMandadoConsultaList.classeJudicial}";
    private static final String                        R7               = "o.pessoaGrupoOficialJustica.pessoa.idUsuario = (#{processoExpedienteCentralMandadoConsultaList.pessoaOficialJustica.idUsuario})";
    private static final String                        R8               = "o.processoExpediente.processoTrf IN (select ppe.processoJudicial from ProcessoParteExpediente ppe "
                                                                                + "WHERE ppe.processoJudicial.idProcessoTrf = o.processoExpediente.processoTrf.idProcessoTrf and "
                                                                                + "ppe.processoExpediente.idProcessoExpediente = o.processoExpediente.idProcessoExpediente and "
                                                                                + "lower(ppe.nomePessoaParte) like lower('%' || #{processoExpedienteCentralMandadoConsultaList.nomeDestinatario} || '%') )";
    private static final String                        R9               = "o.processoExpediente.processoTrf.classeJudicial.idClasseJudicial = (#{processoExpedienteCentralMandadoConsultaList.classeJudicial.idClasseJudicial})";
    private static final String                        R11              = "(#{processoExpedienteCentralMandadoConsultaList.prioridadeProcesso}) member of o.processoExpediente.processoTrf.prioridadeProcessoList";
    private static final String                        R12                     = "cast(o.dtDistribuicaoExpediente as date) >= cast(#{processoExpedienteCentralMandadoConsultaList.dataInicio} as date)";
    private static final String                        R13                     = "cast(o.dtDistribuicaoExpediente as date) <= cast(#{processoExpedienteCentralMandadoConsultaList.dataFim} as date)";
    private static final String                        R14                     = "#{processoExpedienteCentralMandadoConsultaList.statusExpedienteCentral} = o.statusExpedienteCentral";
    private static final String                        R15              = "o.centralMandado = #{processoExpedienteCentralMandadoConsultaList.centralMandado}";

    private NumeroProcesso                             numeroProcesso   = new NumeroProcesso();
    private String                                     nomeDestinatario;
    private GrupoOficialJustica                        grupoOficial;
    private CentralMandado                             centralMandado;
    private TipoProcessoDocumento                      tpProcessoDocumento;
    private Date                                       dataInicio;
    private Date                                       dataFim;
    private OrgaoJulgador                              orgaoJulgador;
    private OrgaoJulgadorColegiado 					   orgaoColegiado = Authenticator.getOrgaoJulgadorColegiadoAtual() != null ? Authenticator.getOrgaoJulgadorColegiadoAtual() : null;
    private PessoaOficialJustica                       pessoaOficialJustica;
    private ClasseJudicial                             classeJudicial;
    private PrioridadeProcesso                         prioridadeProcesso;
    private ProcessoTrf                                processoTrf;
    private ProcessoExpedienteCentralMandadoStatusEnum statusExpedienteCentral = ProcessoExpedienteCentralMandadoStatusEnum.A;
    
    private List<CentralMandado>                       centraisMandado;
    
    /**
     * Retorna a EL para pegar o orgao julgador colegiado
     * de acordo com perfil.
     * 
     * Caso o perfil atual tenha OrgaoJulgadorColegiado, a pesquisa irá levar em consideração o mesmo,
     * caso não tenha, a pesquisa levará em consideração o OrgaoJulgadorColegiado selecionado na busca.
     * 
     * @return String
     */
    private String obterElOrgaoJulgadorColegiado() {
		OrgaoJulgadorColegiado ojc = Authenticator.getOrgaoJulgadorColegiadoAtual();

		if (ojc != null) {
			return "( #{authenticator.getOrgaoJulgadorAtual().getIdOrgaoJulgador()}) ";
		}

		return "( #{ processoExpedienteCentralMandadoConsultaList.orgaoColegiado.idOrgaoJulgadorColegiado } )" ;
    }
    
    protected void addSearchFields() {
        addSearchField("pessoaGrupoOficialJustica.grupoOficialJustica", SearchCriteria.igual, R2);
        addSearchField("processoExpediente.tipoProcessoDocumento", SearchCriteria.igual, R3);
        if (!ParametroUtil.instance().isPrimeiroGrau()) {
            addSearchField("processoExpediente.processoTrf.orgaoJulgadorColegiado", 
            		SearchCriteria.igual, R4 + obterElOrgaoJulgadorColegiado());
        } 
        addSearchField("processoExpediente.processoTrf.orgaoJulgador", SearchCriteria.igual, R5);
        
        addSearchField("processoTrf.numeroSequencia", SearchCriteria.igual, R6_1);
        addSearchField("processoTrf.ano", SearchCriteria.igual, R6_2);
        addSearchField("processoTrf.numeroDigitoVerificador", SearchCriteria.igual, R6_3);
        addSearchField("processoTrf.numeroOrgaoJustica", SearchCriteria.igual, R6_4);
        addSearchField("processoTrf.numeroOrigem", SearchCriteria.igual, R6_5);
        addSearchField("processoTrf.classeJudicial", SearchCriteria.igual, R6_6);
        
        addSearchField("pessoaGrupoOficialJustica.pessoa.idUsuario", SearchCriteria.igual, R7);
        addSearchField("processoExpediente.processoTrf.numeroProcesso", SearchCriteria.igual, R8);
        addSearchField("processoExpediente.processoTrf.classeJudicial.idClasseJudicial", SearchCriteria.igual, R9);
        addSearchField("processoExpediente.processoTrf.prioridadeProcessoList", SearchCriteria.contendo, R11);
        addSearchField("dataInicio", SearchCriteria.menorIgual, R12);
        addSearchField("dataFim", SearchCriteria.maiorIgual, R13);
        addSearchField("statusExpedienteCentral", SearchCriteria.igual, R14);
        addSearchField("centralMandado", SearchCriteria.igual, R15);
    }

    protected Map<String, String> getCustomColumnsOrder() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("pessoaGrupoOficialJustica", "o.pessoaGrupoOficialJustica.pessoa");
        map.put("statusExpedienteCentral", "o.statusExpedienteCentral");
        return map;
    }

    @Override
    protected String getDefaultEjbql() {
    	LocalizacaoService localizacaoService = ComponentUtil.getComponent("localizacaoService");
		List<Integer> idsAncestrais = localizacaoService
					.obterIdsAncestrais(Authenticator.getUsuarioLocalizacaoAtual().getLocalizacaoFisica().getIdLocalizacao());
		List<Integer> idsLocalizacoesFilhas = 
				localizacaoService.getTreeIdsList(Authenticator.getUsuarioLocalizacaoAtual().getLocalizacaoFisica());
    	String idsAncestraisFormatado = StringUtils.join(idsAncestrais, ",");
    	String idsFilhasFormatado = StringUtils.join(idsLocalizacoesFilhas, ",");

    	StringBuilder query = new StringBuilder();
    	query.append(" SELECT o FROM ProcessoExpedienteCentralMandado o ");
    	query.append("   JOIN o.centralMandado centralMandado");
    	query.append("   JOIN centralMandado.centralMandadoLocalizacaoList centraisMandadoLocalizacao ");
    	query.append("   JOIN o.processoExpediente processoExpediente ");
    	query.append("   JOIN processoExpediente.processoTrf processoTrf ");
    	query.append("   JOIN processoTrf.orgaoJulgador orgaoJulgador ");
    	query.append("   JOIN orgaoJulgador.localizacao localizacaoOrgaoJulgador ");
    	query.append("   LEFT JOIN processoTrf.orgaoJulgadorColegiado orgaoJulgadorColegiado");
    	query.append("   LEFT JOIN orgaoJulgadorColegiado.localizacao localizacaoOrgaoJulgadorColegiado ");
    	query.append("   WHERE (localizacaoOrgaoJulgador.idLocalizacao "); 
    	query.append("       	  IN ( ");
    	query.append(        		idsFilhasFormatado);
    	query.append("       	  ) ");
    	query.append("   		OR localizacaoOrgaoJulgadorColegiado.idLocalizacao");
    	query.append("       	  IN ( ");
    	query.append(        		idsFilhasFormatado);
    	query.append("       	  ) ");
    	query.append("       	) ");
    	query.append("   AND centraisMandadoLocalizacao.localizacao.idLocalizacao IN (");
    	query.append(	 	idsAncestraisFormatado);
    	query.append("   )");
    	if(Authenticator.isPapelOficialJustica()){
    		query.append("   AND o.pessoaGrupoOficialJustica.grupoOficialJustica IN (");
    		query.append(	 	StringUtils.join(obterIdsGrupoOficialJustica(), ","));
    		query.append("   )");
    	}
    	
    	return query.toString();
    }

    /**
     * Metodo que limpa todos os campos referentes aos filtros de pesquisa.
     */
    public void limparCampos(){
    	this.numeroProcesso = new NumeroProcesso();
    	this.nomeDestinatario = null;  
    	this.grupoOficial = null;
    	this.centralMandado = null;
    	this.tpProcessoDocumento = null;
    	this.dataInicio = null;
    	this.dataFim = null;
    	this.orgaoJulgador = null;
    	this.orgaoColegiado = Authenticator.getOrgaoJulgadorColegiadoAtual() != null ? Authenticator.getOrgaoJulgadorColegiadoAtual() : null;
    	this.pessoaOficialJustica = null;
    	this.classeJudicial = null;
    	this.prioridadeProcesso = null;
    	this.processoTrf = null;
    	this.statusExpedienteCentral = null;
    }
    
    /**
     * Metodo que retorna a lista de ID dos grupos de oficiais de justica.
     * 
     * @return retorna uma lista de inteiros, contendo os id's dos grupos de oficiais de justica.
     */
    private List<Integer> obterIdsGrupoOficialJustica(){
    	List<Integer> ids = new ArrayList<Integer>();
    	for (GrupoOficialJustica grupoOficial : obterGruposOficiaisJustica()) {
			ids.add(grupoOficial.getIdGrupoOficialJustica());
		}
    	return ids;
    }    
    
    protected String getDefaultOrder() {
        return DEFAULT_ORDER;
    }

    @SuppressWarnings("unchecked")
    public List<OrgaoJulgador> getOrgaoJulgadorItems() {
        StringBuilder sb = new StringBuilder();
        sb.append("select o from OrgaoJulgador o ");
        sb.append("where o.ativo = true");
        return getEntityManager().createQuery(sb.toString()).getResultList();
    }
	
	/**
	 * Retorna os Orgaos Julgador Colegiado
     * 
     * Caso o perfil atual tenha OrgaoJulgadorColegiado, retorna uma lista apenas com esse OrgaoJulgadorColegiado,
     * caso não tenha, retorna uma lista com todos o OrgaoJulgadorColegiado ativos.
	 * 
	 * @return List<OrgaoJulgadorColegiado>
	 */
	public List<OrgaoJulgadorColegiado> obterOJColegiadosAtivosPorPerfilLogado() {
		OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager = ComponentUtil.getComponent(OrgaoJulgadorColegiadoManager.NAME);
		
		return orgaoJulgadorColegiadoManager.obterOJColegiadosAtivosPorPerfilLogado();
	}
	
	/**
	 * Retorna os orgaos julgadores de acordo com o colegiado.
	 * Caso seja nulo, retorna todos os orgaos julgaores ativos.
	 * 
	 * @param orgaoJulgadorColegiado
	 * @return List<OrgaoJulgador>
	 */
	public List<OrgaoJulgador> obterOrgaosJulgadoresPorOJC(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent(OrgaoJulgadorManager.NAME);
		
		return orgaoJulgadorManager.obterOrgaosJulgadoresPorColegiado(orgaoJulgadorColegiado);
	}

	/**
	 * Retorna as centrais de mandados de acordo com o perfil que esta selecionado.
	 * 
	 * @return List<CentralMandado> por perfil do usuario logado.
	 */
	public List<CentralMandado> getCentraisMandado() {
		if(centraisMandado == null){
			CentralMandadoManager centralMandadoManager = CentralMandadoManager.instance();
			centraisMandado = centralMandadoManager.obterCentraisMandadosPorPerfil();
		}
		return centraisMandado;
	}
	
	/**
	 * Metodo que retorna a lista de GrupoOficialJustica, se preocupando com a peculiaridades dos perfis de oficial de justica
	 * e dos demais.
	 * 
	 * @return List GrupoOficialJustica por perfil do usuario logado
	 */
    public List<GrupoOficialJustica> obterGruposOficiaisJustica() {
    	GrupoOficialJusticaManager grupoOficialJusticaManager = GrupoOficialJusticaManager.instance();
    	return grupoOficialJusticaManager.obter(centralMandado);
    }

    public String getNomeDestinatario() {
        return nomeDestinatario;
    }

    public void setNomeDestinatario(String nomeDestinatario) {
        this.nomeDestinatario = nomeDestinatario;
    }

    public GrupoOficialJustica getGrupoOficial() {
        return grupoOficial;
    }

    public void setGrupoOficial(GrupoOficialJustica grupoOficial) {
        this.grupoOficial = grupoOficial;
    }

    public TipoProcessoDocumento getTpProcessoDocumento() {
        return tpProcessoDocumento;
    }

    public void setTpProcessoDocumento(TipoProcessoDocumento tpProcessoDocumento) {
        this.tpProcessoDocumento = tpProcessoDocumento;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = DateUtil.getBeginningOfDay(dataInicio);
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = DateUtil.getEndOfDay(dataFim);
    }

    public OrgaoJulgador getOrgaoJulgador() {
        return orgaoJulgador;
    }

    public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
        this.orgaoJulgador = orgaoJulgador;
    }

    public PessoaOficialJustica getPessoaOficialJustica() {
        return pessoaOficialJustica;
    }

    public void setPessoaOficialJustica(
            PessoaOficialJustica pessoaOficialJustica) {
        this.pessoaOficialJustica = pessoaOficialJustica;
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

    public ProcessoTrf getProcessoTrf() {
        return processoTrf;
    }

    public void setProcessoTrf(ProcessoTrf processoTrf) {
        this.processoTrf = processoTrf;
    }

    public NumeroProcesso getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(NumeroProcesso numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }

    public ProcessoExpedienteCentralMandadoStatusEnum getStatusExpedienteCentral() {
        return statusExpedienteCentral;
    }

    public void setStatusExpedienteCentral(ProcessoExpedienteCentralMandadoStatusEnum statusExpedienteCentral) {
        this.statusExpedienteCentral = statusExpedienteCentral;
    }

	public OrgaoJulgadorColegiado getOrgaoColegiado() {
		return orgaoColegiado;
	}

	public void setOrgaoColegiado(OrgaoJulgadorColegiado orgaoColegiado) {
		this.orgaoColegiado = orgaoColegiado;
	}

	public CentralMandado getCentralMandado() {
		return centralMandado;
	}

	public void setCentralMandado(CentralMandado centralMandado) {
		this.centralMandado = centralMandado;
	}
}
