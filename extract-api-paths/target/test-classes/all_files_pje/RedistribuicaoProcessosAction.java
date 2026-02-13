package br.com.infox.pje.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.AssuntoTrfComCompetenciaSuggestBean;
import br.com.infox.cliente.component.suggest.ClasseJudicialComCompetenciaSuggestBean;
import br.com.infox.cliente.component.suggest.MovimentoProcessualSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.manager.JurisdicaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.RedistribuicaoProcessoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.view.FiltroDinamicoFuncionalidade;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(RedistribuicaoProcessosAction.NAME)
@Scope(ScopeType.PAGE)
@AutoCreate
public class RedistribuicaoProcessosAction{
	public static final String NAME = "redistribuicaoProcessosAction";
	
	private final String funcionalidade = FiltroDinamicoFuncionalidade.REDISTRIBUICAO_PROCESSOS.getNomeFuncionalidade();

	@In
	private RedistribuicaoProcessoManager redistribuicaoProcessoManager;
	
	@In
	private JurisdicaoManager jurisdicaoManager;
	
	@In
	protected FacesMessages facesMessages;
	
	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@In
	private ParametroService parametroService;
	
	private List<ProcessoTrf> listaProcessos;
	
	private List<ProcessoTrf> listaComRestricoesBasicas;
	
	private ProcessoTrf selecionado;
	
	private Boolean selecionaTodos = Boolean.FALSE;
	
	private Boolean filtroAvancado = Boolean.FALSE;
	
	private List<ProcessoRedistribuido> processosRedistribuidos;
	
	/**Filtros da Tela**/
	private String nomeParte;
	private String objetoProcesso;
	private String documentoParte;
	private Integer numeroSequencia;
	private Integer digitoVerificador;
	private Integer ano;
	private String ramoJustica;
	private String respectivoTribunal;
	private Integer numeroOrigem;
	private Boolean aplicarMascaraProcessoReferencia = Boolean.TRUE;
	private String numeroProcessoReferencia;
	private AssuntoTrf assuntoTrf;
	private ClasseJudicial classeJudicial;
	private String numeroDocumento;
	private String numeroOAB;
	private String letraOAB;
	private Estado estadoOAB;
	private OrgaoJulgadorColegiado orgaoColegiado;
	private Jurisdicao jurisdicao;
	private OrgaoJulgador orgaoJulgador;
	private Date dataAutuacaoInicio;
	private Date dataAutuacaoFim;
	private Eleicao eleicao;
	private Estado estado;
	private Municipio municipio;
	private Double valorCausaInicial;
	private Double valorCausaFinal;
	private Evento movimentacaoProcessual;
	
	@Create
	public void Init(){
		String numeroOrgaoJustica = parametroService.valueOf("numeroOrgaoJustica");
		if(numeroOrgaoJustica != null){
			this.ramoJustica = numeroOrgaoJustica.substring(0, 1);
			this.respectivoTribunal = numeroOrgaoJustica.substring(1);
		}
	}
	
	public void pesquisar(){
		List<ProcessoTrf> processos;
		try {
			processos = redistribuicaoProcessoManager.consultarProcessos( 
					numeroSequencia, digitoVerificador, ano, numeroOrigem, ramoJustica, 
					respectivoTribunal, nomeParte, documentoParte, estadoOAB, numeroOAB, letraOAB, 
					orgaoJulgador, orgaoColegiado, dataAutuacaoInicio, 
					dataAutuacaoFim, eleicao, estado, municipio, numeroDocumento, valorCausaInicial, 
					valorCausaFinal, numeroProcessoReferencia, objetoProcesso, jurisdicao, movimentacaoProcessual);
		} catch (Exception e) {
			facesMessages.add(Severity.WARN, "A consulta não obteve nenhum resultado.");
			processos = new ArrayList<ProcessoTrf>();
		}
		listaProcessos = processos;
	}
	
	 /**
  	* Método responsável por limpar os filtros de pesquisa
  	*/
  	public void limparCamposPesquisa() {
		this.nomeParte = null;
		this.documentoParte = null;
		this.numeroSequencia = null;
		this.digitoVerificador = null;
		this.ano = null;
		this.numeroOrigem = null;
		this.estadoOAB = null;
		this.numeroOAB = null;
		this.letraOAB = null;
		this.orgaoColegiado = null;
		this.orgaoJulgador = null;
		this.dataAutuacaoInicio = null;
		this.dataAutuacaoFim = null;
		this.eleicao = null;
		this.municipio = null;
		this.estado = null;
		this.valorCausaInicial = null;
		this.valorCausaFinal = null;
		this.objetoProcesso = null;
		this.numeroDocumento = null;
		this.jurisdicao = null;
		limparNumeroProcessoReferencia();
		limparMovimentoProcessual();
		limparAssuntoTrf();
		limparClasseJudicial();
		listaProcessos = null;
	}

	public void limparNumeroProcessoReferencia() {
		this.numeroProcessoReferencia = null;
	}
	
	/**
	 * Limpa o campo suggest de movimento processual.
	 */
	private void limparMovimentoProcessual() {
		this.movimentacaoProcessual = null;
		MovimentoProcessualSuggestBean.instance().setDefaultValue(null);
		MovimentoProcessualSuggestBean.instance().setInstance(null);
	}

	/**
	 * Limpa as informações de assunto do campo suggest box.
	 */
	public void limparAssuntoTrf() {
		assuntoTrf = null;
		AssuntoTrfComCompetenciaSuggestBean.instance().setDefaultValue(null);
		AssuntoTrfComCompetenciaSuggestBean.instance().setInstance(null);
	}

	/**
	 * Limpa as informações de classe judicial do campo suggest box.
	 */
	public void limparClasseJudicial() {
		classeJudicial = null;
		ClasseJudicialComCompetenciaSuggestBean.instance().setDefaultValue(null);
		ClasseJudicialComCompetenciaSuggestBean.instance().setInstance(null);
	}
	
	@SuppressWarnings("unchecked")
	public List<Estado> getEstadoList() {

		StringBuilder sb = new StringBuilder();
		sb.append("select o from Estado o where o.ativo = true ");

		return EntityUtil.getEntityManager().createQuery(sb.toString()).getResultList();

	}
	
	@SuppressWarnings("unchecked")
	public List<Eleicao> getEleicaoList() {

		StringBuilder sb = new StringBuilder();
		sb.append("select o from Eleicao o where o.ativo = true ");

		return EntityUtil.getEntityManager().createQuery(sb.toString()).getResultList();

	}
	
	/**
	* Método responsável por retornar uma lista de Órgãos Julgadores filtrados por OrgaoJulgadorColegiado e Jurisdicao.
	*  
	* @param  ojc			Objeto da classe OrgaoJulgadorColegiado. Caso seja ojcnulo, é ignorado.
	* @param  jurisdicao	Objeto da classe Jurisdicao. Caso seja nulo, é ignorado.
	* @return Lista de OrgaoJulgador filtrados conforme os valores informados nos parâmetros.
	*/
	public List<OrgaoJulgador> getOrgaoJulgadorListPorOjcJurisdicao(OrgaoJulgadorColegiado ojc, Jurisdicao jurisdicao) {
		return processoJudicialManager.getOrgaoJulgadorListPorOjcJurisdicao(ojc, jurisdicao);
	}
	
	/**
	* Método responsável por retornar uma lista com a totalidade Jurisdicoes ativas.
	*  
	* @return Lista de Jurisdicao filtrados com a propriedade "ativo" = true.
	*/
	public List<Jurisdicao> getJurisdicaoList() {
		return jurisdicaoManager.getJurisdicoesAtivas();
	}
	
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getOrgaoJulgadorColegiadoList() {
		
		String papel = Authenticator.getPapelAtual().getIdentificador();
		boolean isAdmin = papel.equalsIgnoreCase("admin") || papel.equalsIgnoreCase("administrador");
		
		OrgaoJulgadorColegiado ojc = Authenticator.getOrgaoJulgadorColegiadoAtual();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o FROM OrgaoJulgadorColegiado o WHERE o.ativo = true ");
		
		if (!isAdmin){
			if (ojc != null) {
				sb.append("AND o.idOrgaoJulgadorColegiado = ");
				sb.append(ojc.getIdOrgaoJulgadorColegiado());
			} else {
				sb.append("AND 1 != 1 ");
			}
		}

		sb.append(" ORDER BY CASE WHEN o.orgaoJulgadorColegiado >= 'A' THEN upper(to_ascii(o.orgaoJulgadorColegiado)) ELSE to_char(to_number(o.orgaoJulgadorColegiado, '999'),'000') END, upper(to_ascii(o.orgaoJulgadorColegiado)) ");
		
		return EntityUtil.getEntityManager().createQuery(sb.toString()).getResultList();
	}

	public List<ProcessoRedistribuido> getProcessosRedistribuidos() {
		if(processosRedistribuidos == null || processosRedistribuidos.isEmpty() || !listaProcessos.containsAll(processosRedistribuidos)){
			processosRedistribuidos = new ArrayList<ProcessoRedistribuido>();
			if(listaProcessos != null){
				for (ProcessoTrf processoTrf : listaProcessos) {
					processosRedistribuidos.add(new ProcessoRedistribuido(processoTrf));
				}
			}
		}
		return processosRedistribuidos;
	}
	public void setProcessosRedistribuidos(List<ProcessoRedistribuido> processosRedistribuidos) {
		this.processosRedistribuidos = processosRedistribuidos;
	}
	
	public void showFiltroAvancado(Boolean show){
		processosRedistribuidos = null;
		listaProcessos = null;
		filtroAvancado = show;
	}
	
	/**
	 * @Description método de controle do componente <h:selectBooleanCheckbox>
	 */
	public void selectAll(){
		if(selecionaTodos == Boolean.FALSE){
			setAll(Boolean.FALSE);
		}else{
			setAll(Boolean.TRUE);			
		}
	}
	
	/**
	 * @Description método de controle do componente <h:selectBooleanCheckbox>
	 */
	private void setAll(Boolean isSelect){
		for (ProcessoRedistribuido processo : processosRedistribuidos) {
			processo.setSelecionado(isSelect);
		}
	}
	
 	/**
	 * @Description método de controle do componente <h:selectBooleanCheckbox>
	 */
 	public void selectOne(ProcessoTrf processo){
 		selecionaTodos = Boolean.FALSE;
 		for (ProcessoRedistribuido processoRedistribuido : processosRedistribuidos) {
			if(processoRedistribuido.getProcesso().equals(processo)){
				if(processoRedistribuido.getSelecionado()){
					processoRedistribuido.setSelecionado(Boolean.FALSE);
				}else{
					processoRedistribuido.setSelecionado(Boolean.TRUE);
				}
				return;
			}
		}
 	}
 	
 	
 	public String getFuncionalidade() {
		return funcionalidade;
	}
	public List<ProcessoTrf> getListaProcessos() {
		if(listaProcessos == null || listaProcessos.isEmpty()){
			listaProcessos = new ArrayList<ProcessoTrf>();
			processosRedistribuidos = new ArrayList<ProcessoRedistribuido>();
		}
		return listaProcessos;
	}
	public void setListaProcessos(List<ProcessoTrf> listaProcessos) {
		this.listaProcessos = listaProcessos;
	}
	public List<ProcessoTrf> getListaComRestricoesBasicas() {
		if(listaComRestricoesBasicas == null){
			listaComRestricoesBasicas = redistribuicaoProcessoManager.obtemListaComRestricoesBasicas();
		}
		return listaComRestricoesBasicas;
	}
	public void setListaComRestricoesBasicas(List<ProcessoTrf> listaComRestricoesBasicas) {
		this.listaComRestricoesBasicas = listaComRestricoesBasicas;
	}
	public ProcessoTrf getSelecionado() {
		return selecionado;
	}
	public void setSelecionado(ProcessoTrf selecionado) {
		this.selecionado = selecionado;
	}
	public Boolean getSelecionaTodos() {
		return selecionaTodos;
	}
	public void setSelecionaTodos(Boolean selecionaTodos) {
		this.selecionaTodos = selecionaTodos;
	}
	public Boolean getFiltroAvancado() {
		return filtroAvancado;
	}
	public void setFiltroAvancado(Boolean filtroAvancado) {
		this.filtroAvancado = filtroAvancado;
	}
 	public RedistribuicaoProcessoManager getRedistribuicaoProcessoManager() {
		return redistribuicaoProcessoManager;
	}

	public void setRedistribuicaoProcessoManager(RedistribuicaoProcessoManager redistribuicaoProcessoManager) {
		this.redistribuicaoProcessoManager = redistribuicaoProcessoManager;
	}

	public JurisdicaoManager getJurisdicaoManager() {
		return jurisdicaoManager;
	}

	public void setJurisdicaoManager(JurisdicaoManager jurisdicaoManager) {
		this.jurisdicaoManager = jurisdicaoManager;
	}

	public ProcessoJudicialManager getProcessoJudicialManager() {
		return processoJudicialManager;
	}

	public void setProcessoJudicialManager(ProcessoJudicialManager processoJudicialManager) {
		this.processoJudicialManager = processoJudicialManager;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getObjetoProcesso() {
		return objetoProcesso;
	}

	public void setObjetoProcesso(String objetoProcesso) {
		this.objetoProcesso = objetoProcesso;
	}

	public String getDocumentoParte() {
		return documentoParte;
	}

	public void setDocumentoParte(String documentoParte) {
		this.documentoParte = documentoParte;
	}

	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	public Integer getDigitoVerificador() {
		return digitoVerificador;
	}

	public void setDigitoVerificador(Integer digitoVerificador) {
		this.digitoVerificador = digitoVerificador;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public String getRamoJustica() {
		return ramoJustica;
	}

	public void setRamoJustica(String ramoJustica) {
		this.ramoJustica = ramoJustica;
	}

	public String getRespectivoTribunal() {
		return respectivoTribunal;
	}

	public void setRespectivoTribunal(String respectivoTribunal) {
		this.respectivoTribunal = respectivoTribunal;
	}

	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}

	public Boolean getAplicarMascaraProcessoReferencia() {
		return aplicarMascaraProcessoReferencia;
	}

	public void setAplicarMascaraProcessoReferencia(Boolean aplicarMascaraProcessoReferencia) {
		this.aplicarMascaraProcessoReferencia = aplicarMascaraProcessoReferencia;
	}

	public String getNumeroProcessoReferencia() {
		return numeroProcessoReferencia;
	}

	public void setNumeroProcessoReferencia(String numeroProcessoReferencia) {
		this.numeroProcessoReferencia = numeroProcessoReferencia;
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

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
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

	public Estado getEstadoOAB() {
		return estadoOAB;
	}

	public void setEstadoOAB(Estado estadoOAB) {
		this.estadoOAB = estadoOAB;
	}

	public OrgaoJulgadorColegiado getOrgaoColegiado() {
		return orgaoColegiado;
	}

	public void setOrgaoColegiado(OrgaoJulgadorColegiado orgaoColegiado) {
		this.orgaoColegiado = orgaoColegiado;
	}

	public Jurisdicao getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public Date getDataAutuacaoInicio() {
		return dataAutuacaoInicio;
	}

	public void setDataAutuacaoInicio(Date dataAutuacaoInicio) {
		this.dataAutuacaoInicio = dataAutuacaoInicio;
	}

	public Date getDataAutuacaoFim() {
		return dataAutuacaoFim;
	}

	public void setDataAutuacaoFim(Date dataAutuacaoFim) {
		this.dataAutuacaoFim = dataAutuacaoFim;
	}

	public Eleicao getEleicao() {
		return eleicao;
	}

	public void setEleicao(Eleicao eleicao) {
		this.eleicao = eleicao;
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
	}

	public Double getValorCausaInicial() {
		return valorCausaInicial;
	}

	public void setValorCausaInicial(Double valorCausaInicial) {
		this.valorCausaInicial = valorCausaInicial;
	}

	public Double getValorCausaFinal() {
		return valorCausaFinal;
	}

	public void setValorCausaFinal(Double valorCausaFinal) {
		this.valorCausaFinal = valorCausaFinal;
	}

	public Evento getMovimentacaoProcessual() {
		return movimentacaoProcessual;
	}

	public void setMovimentacaoProcessual(Evento movimentacaoProcessual) {
		this.movimentacaoProcessual = movimentacaoProcessual;
	}


	public class ProcessoRedistribuido{
 		private ProcessoTrf processo;
 		private Boolean selecionado;
 		
 		public ProcessoRedistribuido(ProcessoTrf processo) {
			this.processo = processo;
			this.selecionado = Boolean.FALSE;
		}
 		
		public ProcessoTrf getProcesso() {
			return processo;
		}
		public void setProcesso(ProcessoTrf processo) {
			this.processo = processo;
		}
		public Boolean getSelecionado() {
			return selecionado;
		}

		public void setSelecionado(Boolean selecionado) {
			this.selecionado = selecionado;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((getProcesso() == null) ? 0 : getProcesso().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if(obj instanceof ProcessoTrf){
				ProcessoTrf otherProc = (ProcessoTrf) obj;
				if (getProcesso() == null && otherProc != null){
					return false;
				} else if (!getProcesso().equals(otherProc))
					return false;
			}else{
				ProcessoRedistribuido other = (ProcessoRedistribuido) obj;
				if (getProcesso() == null && other.getProcesso() != null){
					return false;
				} else if (!getProcesso().equals(other.getProcesso()))
					return false;
			}
			return true;
		}
 	}
}
