/**
 * ProcessoTrfParaCabecalhoProcessualConverter.java
 * 
 * Data de criação: 23/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;

import com.google.common.base.MoreObjects;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v222.beans.AssuntoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.CabecalhoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.CadastroIdentificador;
import br.jus.cnj.intercomunicacao.v222.beans.NumeroUnico;
import br.jus.cnj.intercomunicacao.v222.beans.PoloProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.VinculacaoProcessual;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.intercomunicacao.v222.util.MNIParametroUtil;
import br.jus.cnj.pje.intercomunicacao.v222.util.MNIUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.JurisdicaoManager;
import br.jus.cnj.pje.nucleo.manager.MunicipioManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfConexaoManager;
import br.jus.cnj.pje.nucleo.manager.SituacaoProcessualManager;
import br.jus.cnj.pje.ws.AssuntoJudicial;
import br.jus.cnj.pje.ws.ClasseJudicial;
import br.jus.cnj.pje.ws.Competencia;
import br.jus.cnj.pje.ws.Jurisdicao;
import br.jus.cnj.pje.ws.client.ConsultaPJeClient;
import br.jus.pje.je.entidades.ComplementoProcessoJE;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.entidades.SituacaoProcessual;
import br.jus.pje.nucleo.entidades.TipoSituacaoProcessual;
import br.jus.pje.nucleo.enums.PrevencaoEnum;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Conversor de ProcessoTrf para CabecalhoProcessual.
 * 
 * @author Adriano Pamplona
 */
@Name (ProcessoTrfParaCabecalhoProcessualConverter.NAME)
public class ProcessoTrfParaCabecalhoProcessualConverter extends
		IntercomunicacaoConverterAbstrato<ProcessoTrf, CabecalhoProcessual> {
	
	public static final String NAME = "v222.processoTrfParaCabecalhoProcessualConverter";
	
	private ConsultaPJeClient consultaPJeClient;
	private Integer competenciaConflito;
	
	@In
	private ProcessoTrfConexaoManager processoTrfConexaoManager = (ProcessoTrfConexaoManager) Component.getInstance("processoTrfConexaoManager");

	@In
	private ProcessoTrfManager processoTrfManager;
	
	@In
	private SituacaoProcessualManager situacaoProcessualManager;
	
	private Boolean deveCarregarPartesDeTodasSituacoes = Boolean.TRUE; 
	
	@In (scope = ScopeType.EVENT, required = false)
	@Out (scope = ScopeType.EVENT, required = false)
	private Map<Integer, CabecalhoProcessual> mapaCacheCabecalhoProcessual;
	
	public static ProcessoTrfParaCabecalhoProcessualConverter instance(ConsultaPJeClient consultaPJeClient, Integer competenciaConflito) {
		ProcessoTrfParaCabecalhoProcessualConverter instance = ComponentUtil.getComponent(ProcessoTrfParaCabecalhoProcessualConverter.class);
		instance.setConsultaPJeClient(consultaPJeClient);
		instance.setCompetenciaConflito(competenciaConflito);
		return instance;
	}
	
	/**
	 * Construtor.
	 */
	public ProcessoTrfParaCabecalhoProcessualConverter() {
		super();
	}

	/**
	 * Construtor.
	 */
	public ProcessoTrfParaCabecalhoProcessualConverter(Boolean deveCarregarPartesDeTodasSituacoes) {
		super();
		this.deveCarregarPartesDeTodasSituacoes = deveCarregarPartesDeTodasSituacoes;
	}
	
	/**
	 * Construtor.
	 * 
	 * @param consultaPJeClient
	 */
	public ProcessoTrfParaCabecalhoProcessualConverter(
			ConsultaPJeClient consultaPJeClient, Integer competenciaConflito) {
		super();
		setConsultaPJeClient(consultaPJeClient);
		this.competenciaConflito = competenciaConflito;
	}

	@Override
	public CabecalhoProcessual converter(ProcessoTrf processo) {
		CabecalhoProcessual resultado = null;
		if (isNotNull(processo)) {
			if (!getMapaCacheCabecalhoProcessual().containsKey(processo.getIdProcessoTrf())) {
				Jurisdicao jurisdicao = obterJurisdicao(processo);
				ClasseJudicial classe = obterClasseJudicial(processo, jurisdicao);
				List<AssuntoProcessual> assuntos = consultarColecaoAssuntoProcessual(processo, jurisdicao, classe);
				br.jus.pje.nucleo.entidades.Competencia com = processo.getCompetencia();
	
				resultado = new CabecalhoProcessual();
				
				if (isNotNull(classe) && isNotVazio(classe.getCodigo())) {
					resultado.setClasseProcessual(converterParaInt(classe.getCodigo()));
				}
				resultado.setOrgaoJulgador(obterOrgaoJulgador(processo));
				resultado.setNumero(obterNumero(processo));
				resultado.setNivelSigilo(obterNivelSigilo(processo));
				resultado.setCodigoLocalidade(obterCodigoLocalidade(jurisdicao));
				if(this.competenciaConflito != null){
					resultado.setCompetencia(this.competenciaConflito);
				}else if (isNotNull(com)) {
					resultado.setCompetencia(com.getIdCompetencia());
				}
				resultado.setValorCausa(processo.getValorCausa());
				resultado.setDataAjuizamento(converterParaDataHora(processo.getDataAutuacao()));
				if (!isServicoConsultarAvisosPendentes()) {
					resultado.setIntervencaoMP(processo.getClasseJudicial().getExigeFiscalLei());
					resultado.setTamanhoProcesso(obterTamanhoProcesso(processo));
					resultado.getPolo().addAll(consultarColecaoPolo(processo));
					resultado.getPrioridade().addAll(consultarPrioridade(processo));
					resultado.getMagistradoAtuante().addAll(getMagistradoAtuante(processo));
					resultado.getProcessoVinculado().addAll(consultarProcessoVinculado(processo));
					resultado.unsetOutroParametro();
					adicionarSituacaoProcesso(resultado, processo);
					this.addDadosEleitorais(resultado, processo);
					MNIParametroUtil.adicionar(resultado, MNIParametro.PARAM_LIMINAR_ANTECIPA_TUTELA, processo.getTutelaLiminar().toString());
					
					if (ComponentUtil.getTramitacaoProcessualService().temSituacao(processo, Variaveis.PJE_ATENDIMENTO_PLANTAO)) {
						MNIParametroUtil.adicionar(resultado, MNIParametro.PARAM_ATENDIMENTO_PLANTAO, Boolean.TRUE.toString());
					}
				}
				
				resultado.getAssunto().addAll(assuntos);
								
				if(resultado.getOutroParametro().isEmpty()) {
					resultado.unsetOutroParametro();
				}
				getMapaCacheCabecalhoProcessual().put(processo.getIdProcessoTrf(), resultado);
			} else {
				resultado = getMapaCacheCabecalhoProcessual().get(processo.getIdProcessoTrf());
			}
		}
		return resultado;
	}

	protected void addDadosEleitorais(CabecalhoProcessual resultado, ProcessoTrf processo) {
		ComplementoProcessoJE complementoJE = processo.getComplementoJE();
		
		if(complementoJE != null) {
			Eleicao eleicao = complementoJE.getEleicao();
			if(eleicao != null) {
				MNIParametroUtil.adicionar(resultado, 
						MNIParametro.PARAM_JE_ANO_ELEICAO, eleicao.getAno().toString());
				MNIParametroUtil.adicionar(resultado, 
						MNIParametro.PARAM_JE_ANO_ELEICAO, eleicao.getAno().toString());
				MNIParametroUtil.adicionar(resultado, 
						MNIParametro.PARAM_JE_TIPO_ELEICAO, eleicao.getTipoEleicao().getCodObjeto().toString());
			}
        
			MNIParametroUtil.adicionar(resultado, MNIParametro.PARAM_JE_ESTADO, complementoJE.getEstadoEleicao().getCodEstado());
        
			MunicipioManager municipioManager = (MunicipioManager) ComponentUtil.getComponent(MunicipioManager.NAME);
			Municipio municipio = municipioManager.getMunicipioByCodigoIBGE(complementoJE.getMunicipioEleicao().getCodigoIbge());
        
			MNIParametroUtil.adicionar(resultado, MNIParametro.PARAM_JE_MUNICIPIO, municipio.getCodigoIbge());
		}
	}
	
	/**
	 * Adiciona a situação do processo no mapa de parâmetros do cabeçalho processual.
	 * 
	 * @param resultado
	 * @param processo
	 */
	private void adicionarSituacaoProcesso(CabecalhoProcessual resultado, ProcessoTrf processo) {
		List<SituacaoProcessual> situacoes = getSituacaoProcessualManager().recuperaSituacoesAtuais(processo);
		if (ProjetoUtil.isNotVazio(situacoes)) {
			SituacaoProcessual situacao = situacoes.get(0);
			TipoSituacaoProcessual tipo = situacao.getTipoSituacaoProcessual();
			MNIParametroUtil.adicionar(resultado, MNIParametro.getSituacaoProcesso(), tipo.getNome());
		}
		
	}

	/**
	 * @param processo
	 * @param jurisdicao
	 * @param classe
	 * @param assuntos
	 * @return competência.
	 */
	protected Competencia obterCompetencia(ProcessoTrf processo, Jurisdicao jurisdicao, ClasseJudicial classe, List<AssuntoProcessual> assuntos) {
		Competencia resultado = null;
		if (isNotNull(getCompetenciaConflito())) {
			resultado = new Competencia();
			resultado.setId(getCompetenciaConflito());
		} else {
			if (isNotNull(getConsultaPJeClient(), jurisdicao, classe) && isNotVazio(assuntos)) {
				AssuntoProcessualParaAssuntoJudicialConverter converter = new AssuntoProcessualParaAssuntoJudicialConverter();
				List<AssuntoJudicial> colecaoAssuntoJudicial = converter.converterColecao(assuntos);
				List<Competencia> competencias = getConsultaPJeClient().consultarCompetencias(jurisdicao, classe, colecaoAssuntoJudicial);
				if (isNotVazio(competencias)) {
					resultado = competencias.get(0);
				}
			}
		}
		return resultado;
	}

	/**
	 * @param processo
	 * @return jurisdição.
	 */
	protected Jurisdicao obterJurisdicao(ProcessoTrf processo) {
		Jurisdicao resultado = null;
		if (isNotNull(processo.getJurisdicao())) {
			br.jus.pje.nucleo.entidades.Jurisdicao jurisdicao = processo.getJurisdicao();
			
			Integer id = MoreObjects.firstNonNull(jurisdicao.getNumeroOrigem(), jurisdicao.getIdJurisdicao());
			resultado = new Jurisdicao();
			resultado.setId(id);
			resultado.setDescricao(jurisdicao.getJurisdicao());
		} else {
			if (isNotNull(getConsultaPJeClient())) {
				List<Jurisdicao> jurisdicoes = getConsultaPJeClient().consultarJurisdicoes();
				if (isNotVazio(jurisdicoes)) {
					resultado = jurisdicoes.get(0);
				}
			}
		}
		return resultado;
	}

	/**
	 * @param processo
	 * @return nível de sigilo.
	 */
	protected int obterNivelSigilo(ProcessoTrf processo) {
		return processo.getNivelAcesso();
	}

	/**
	 * @param processo
	 * @return número único.
	 */
	protected NumeroUnico obterNumero(ProcessoTrf processo) {
		String numeroProcesso = processo.getProcesso().getNumeroProcesso();
		NumeroUnico numero = new NumeroUnico();
		numero.setValue(InscricaoMFUtil.retiraMascara(numeroProcesso));
		return numero;
	}

	/**
	 * @param processo
	 * @return orgão julgador.
	 */
	protected br.jus.cnj.intercomunicacao.v222.beans.OrgaoJulgador obterOrgaoJulgador(ProcessoTrf processo) {
		return novoOrgaoJulgadorParaIntercomunicacaoOrgaoJulgadorConverter().
				converter(processo.getOrgaoJulgador(), processo.getNumeroOrgaoJustica());
	}

	/**
	 * @param processo
	 * @param jurisdicao
	 * @return classe processual.
	 */
	protected ClasseJudicial obterClasseJudicial(ProcessoTrf processo, Jurisdicao jurisdicao) {
		ClasseJudicial resultado = null;

		if (isNotNull(processo.getClasseJudicial())) {
			resultado = new ClasseJudicial();
			resultado.setCodigo(processo.getClasseJudicial().getCodClasseJudicial());
			resultado.setDescricao(processo.getClasseJudicial().getClasseJudicial());
		} else {
			if (isNotNull(getConsultaPJeClient(), jurisdicao)) {
				List<ClasseJudicial> classes = getConsultaPJeClient().consultarClassesJudiciais(jurisdicao);
				if (isNotVazio(classes)) {
					resultado = classes.get(0);
				}
			}
		}

		return resultado;
	}

	/**
	 * @param processo
	 * @param jurisdicao
	 * @param classe
	 * @return coleção de assuntos do processo.
	 */
	protected List<AssuntoProcessual> consultarColecaoAssuntoProcessual(ProcessoTrf processo, Jurisdicao jurisdicao, ClasseJudicial classe) {
		List<AssuntoProcessual> resultado = new ArrayList<AssuntoProcessual>();
		if (isNotVazio(processo.getProcessoAssuntoList())) {
			ProcessoAssuntoParaAssuntoProcessualConverter converter = new ProcessoAssuntoParaAssuntoProcessualConverter();
			resultado.addAll(converter.converterColecao(processo.getProcessoAssuntoList()));
		} else {
			if (isNotNull(getConsultaPJeClient(), jurisdicao, classe)) {
				List<AssuntoJudicial> assuntos = getConsultaPJeClient().consultarAssuntosJudiciais(jurisdicao, classe);
				AssuntoJudicialParaAssuntoProcessualConverter converter = new AssuntoJudicialParaAssuntoProcessualConverter();
				resultado.addAll(converter.converterColecao(assuntos));
			}
		}
		return resultado;
	}

	/**
	 * @param processo
	 * @return coleção de polo do processo.
	 */
	protected List<PoloProcessual> consultarColecaoPolo(ProcessoTrf processo) {
		ProcessoTrfParaPoloProcessualConverter converter = new ProcessoTrfParaPoloProcessualConverter(this.deveCarregarPartesDeTodasSituacoes);
		return converter.converter(processo);
	}

	/**
	 * @param processo
	 * @return lista de prioridades
	 */
	protected List<String> consultarPrioridade(ProcessoTrf processo) {
		List<String> prioridades = new ArrayList<String>(0);
		for (PrioridadeProcesso prioridadeProcesso : processo
				.getPrioridadeProcessoList()) {
			prioridades.add(prioridadeProcesso.getPrioridade().toUpperCase());
		}
		return prioridades;
	}

	/**
	 * @return the consultaPJeClient
	 */
	protected ConsultaPJeClient getConsultaPJeClient() {
		return consultaPJeClient;
	}

	/**
	 * @param consultaPJeClient
	 *            the consultaPJeClient to set
	 */
	protected void setConsultaPJeClient(ConsultaPJeClient consultaPJeClient) {
		this.consultaPJeClient = consultaPJeClient;
	}

	/**
	 * @return the competenciaConflito
	 */
	protected Integer getCompetenciaConflito() {
		return competenciaConflito;
	}

	/**
	 * @param competenciaConflito
	 *            the competenciaConflito to set
	 */
	protected void setCompetenciaConflito(Integer competenciaConflito) {
		this.competenciaConflito = competenciaConflito;
	}
	
	
	protected List<CadastroIdentificador> getMagistradoAtuante(ProcessoTrf processo){
		List<CadastroIdentificador> returnValue = new ArrayList<CadastroIdentificador>(0);
		List<Pessoa> magistrados = getProcessoJudicialManager().getMagistradosAtuantes(processo);
		
		for(Pessoa magistrado : magistrados){
			String cpf = magistrado.getDocumentoCpfCnpj();
			CadastroIdentificador ci = MNIUtil.novoCadastroIdentificador(cpf);
			returnValue.add(ci);
		}				
		
		return returnValue;
	}
	
	
	protected ProcessoJudicialManager getProcessoJudicialManager(){
		return (ProcessoJudicialManager) Component.getInstance("processoJudicialManager");
	}

	/**
	 * @return novo OrgaoJulgadorParaIntercomunicacaoOrgaoJulgadorConverter 
	 */
	protected OrgaoJulgadorParaIntercomunicacaoOrgaoJulgadorConverter novoOrgaoJulgadorParaIntercomunicacaoOrgaoJulgadorConverter() {
		return new OrgaoJulgadorParaIntercomunicacaoOrgaoJulgadorConverter();
	}
	
	/**
	 * Consulta os processos vinculados do processo passado por parâmetro.
	 * 
	 * @param processo ProcessoTrf
	 * @return Lista de processos vinculados.
	 */
	protected List<VinculacaoProcessual> consultarProcessoVinculado(ProcessoTrf processo) {
		List<VinculacaoProcessual> resultado = new ArrayList<VinculacaoProcessual>();
		
		TipoConexaoParaModalidadeVinculacaoProcessoConverter conversor = getTipoConexaoParaModalidadeVinculacaoProcessoConverter();
		
		List<ProcessoTrfConexao> listaProcessoTrfConexao = processoTrfConexaoManager.getListProcessosAssociados(processo.getIdProcessoTrf(), Boolean.TRUE);
		
		for(ProcessoTrfConexao processoAssociado : listaProcessoTrfConexao){
			
			if(processoAssociado.getPrevencao().equals(PrevencaoEnum.PR)) {
			
				VinculacaoProcessual vp = new VinculacaoProcessual();
				vp.setNumeroProcesso(obterNumeroUnicoProcessoAssociado(processoAssociado));
				vp.setVinculo(conversor.converter(processoAssociado.getTipoConexao()));
				
				if(NumeroProcessoUtil.numeroProcessoValido(vp.getNumeroProcesso().getValue())) {
					resultado.add(vp);
				}
			}
		}
		return resultado;
	}
	
	/**
	 * Dado um objeto ProcessoTrfConexao (que possui informação de associação entre processos)
	 * retorna o número único do processo associado. 
	 * @param processoAssociado possuindo informações de associação entre processos. 
	 * @return Número Único do processo associado.
	 */
	private NumeroUnico obterNumeroUnicoProcessoAssociado(ProcessoTrfConexao processoAssociado){
		String numeroProcesso = "";
		
		if (processoAssociado.getProcessoTrfConexo() != null){
			if (processoAssociado.getProcessoTrfConexo().getNumeroProcesso() != null) {
				numeroProcesso = processoAssociado.getProcessoTrfConexo().getNumeroProcesso();
			} else if (processoAssociado.getProcessoTrfConexo().getProcessoReferencia() != null){
				numeroProcesso = processoAssociado.getProcessoTrfConexo().getProcessoReferencia().getNumeroProcesso();
			}
		} else if (processoAssociado.getNumeroProcesso() != null){
			numeroProcesso = processoAssociado.getNumeroProcesso();	
		}
		
		NumeroUnico numeroUnico = new NumeroUnico();
		numeroUnico.setValue(StringUtil.removeNaoNumericos(numeroProcesso));
		return numeroUnico;
	}
	
	/**
	 * @return novo TipoConexaoParaModalidadeVinculacaoProcessoConverter.
	 */
	protected TipoConexaoParaModalidadeVinculacaoProcessoConverter getTipoConexaoParaModalidadeVinculacaoProcessoConverter() {
		return new TipoConexaoParaModalidadeVinculacaoProcessoConverter();
	}

	/**
	 * @return the situacaoProcessualManager
	 */
	public SituacaoProcessualManager getSituacaoProcessualManager() {
		if (situacaoProcessualManager == null) {
			situacaoProcessualManager = ComponentUtil.getComponent(SituacaoProcessualManager.class);
		}
		return situacaoProcessualManager;
	}

	/**
	 * Retorna o tamanho do processo, ou seja, a soma do tamanho dos documentos ativos do processo.
	 * 
	 * @param processo ProcessoTrf
	 * @return Long do tamanho do processo.
	 */
	protected int obterTamanhoProcesso(ProcessoTrf processo) {
		Long resultado = getProcessoTrfManager().obterTamanho(processo);
		return resultado.intValue();
	}
	
	protected String obterCodigoLocalidade(Jurisdicao jurisdicao) {
		return String.valueOf(jurisdicao.getId());
	}
	
	/**
	 * @return the processoTrfConexaoManager
	 */
	protected ProcessoTrfConexaoManager getProcessoTrfConexaoManager() {
		if (processoTrfConexaoManager == null) {
			processoTrfConexaoManager = ComponentUtil.getComponent(ProcessoTrfConexaoManager.class);
		}
		return processoTrfConexaoManager;
	}

	/**
	 * @return the processoTrfManager
	 */
	protected ProcessoTrfManager getProcessoTrfManager() {
		if (processoTrfManager == null) {
			processoTrfManager = ComponentUtil.getComponent(ProcessoTrfManager.class);
		}
		return processoTrfManager;
	}

	/**
	 * @return mapaCacheCabecalhoProcessual.
	 */
	protected Map<Integer, CabecalhoProcessual> getMapaCacheCabecalhoProcessual() {
		if (mapaCacheCabecalhoProcessual == null) {
			mapaCacheCabecalhoProcessual = new HashMap<Integer, CabecalhoProcessual>();
		}
		return mapaCacheCabecalhoProcessual;
	}
}