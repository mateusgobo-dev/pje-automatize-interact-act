/**
 * pje-comum
 * Copyright (C) 2009-2015 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.view;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.ClientErrorException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jbpm.graph.exe.ProcessInstance;
import org.richfaces.component.UITree;
import org.richfaces.component.state.TreeState;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.home.TipoDocumentoIdentificacaoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.PessoaFisicaService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.OrgaoDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.domain.StatusDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.domain.TipoDocumentoDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.DadosGeraisPessoaDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.DataNascimentoDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.DocumentoDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.EnderecoDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.NomeMaeDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.NomePaiDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.OutrasAlcunhasDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.OutrosNomesDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.PessoaDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.PessoaListDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.PessoaResponseDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.SexoDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.dto.temp.SinaisMarcasDTO;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.exception.DataNascimentoException;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.filter.PessoaFilter;
import br.jus.cnj.pje.webservice.client.bnmp.ManipulaPecaService;
import br.jus.cnj.pje.webservice.client.bnmp.TipoProcessoDocumentoBNMP;
import br.jus.cnj.pje.webservice.client.bnmp.dto.PecaDTO;
import br.jus.pje.nucleo.dto.EntityPageDTO;
import br.jus.pje.nucleo.dto.PJeServiceApiDTO;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.SexoEnum;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe de controle da integração das partes do processo com o BNMPII
 * @author João Felippe de Oliveira Ferreira (BASIS)
 *
 */
@Name(ProcessoBNMPAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ProcessoBNMPAction<E extends PJeServiceApiDTO> extends BaseAction<ProcessoParte>{

	private static final long serialVersionUID = -7183658558678114446L;

	public static final String NAME = "processoBNMPAction";

	private static final String SIGLA_SISTEMA = "pje";

	private EntityDataModel<ProcessoParte> model;

	private ProcessoTrf processoJudicial;

	private Set<ProcessoParte> partesSelecionadas;

	private Set<ProcessoParte> partesMandadoCadastrado;

	private String urlFrameBnmp;

	private boolean exibirPartesInativas = false;

	private ProcessoParte parteIntegracao;

	private EntityPageDTO<PessoaListDTO> page;

	private List<ModeloDocumento> modelosDocumentos;

	private ProcessoDocumentoBin processoDocumentoBin;

	private ModeloDocumento modeloDocumento;

	private List<TipoProcessoDocumento> tiposProcessoDocumento;

	private TipoProcessoDocumento tipoProcessoDocumento;
	
	private Boolean browserEstaAutenticadoBNMP=false;
	
	private String senhaBnmp;

	@In(create = true)
	private ProcessoParteManager processoParteManager;

	@In(create = true, required = true)
	private transient TaskInstanceUtil taskInstanceUtil;

	@In( value="partesTree",required=false, create=true)
	@Out(value="partesTree",required=false)
	private UITree richTree;

	@In( value="pecasTree",required=false, create=true)
	@Out(value="pecasTree",required=false)
	private UITree richTreePecas;

	@In( value="pecasGeradasTree",required=false, create=true)
	@Out(value="pecasGeradasTree",required=false)
	private UITree richTreePecasGeradas;

	@In(create=true)
	private AjaxDataUtil ajaxDataUtil;

	private ManipulaPecaService manipulaPecaService;
	

	private Integer passo;

	private String idPeca;

	private String urlWebBnmp;
	
	public static final String NAO_INFORMADO = "Não informado";

	private static final int LISTA_PAGE_PADRAO = 0;

	private static final int LISTA_SIZE_PADRAO = 10;

	private static final int PAGINA_PARTES_PASSIVAS = 0;

	private static final int PAGINA_DOCUMENTO_BNMPII = 1;

	private static final int PAGINA_ELABORAR_MINUTA = 2;

	@Create
	public void init(){
		setUrlWebBnmp(ConfiguracaoIntegracaoCloud.getBnmpWebUrl());
		setPasso(PAGINA_PARTES_PASSIVAS);
		limparModalSelecionarRji();
		setPartesSelecionadas(new HashSet<ProcessoParte>());
		setPartesMandadoCadastrado(new HashSet<ProcessoParte>());
		setManipulaPecaService(ComponentUtil.getComponent(ManipulaPecaService.class));
		setTipoProcessoDocumentoViaFluxo();
		try {
			setTiposProcessoDocumento(Arrays.asList(getTipoProcessoDocumento()));
			setProcessoJudicial(loadProcessoJudicial());
			setModelosDocumentos(loadModeloDocumento());
			incluiPartesJaInicalizadas();
		} catch (PJeBusinessException e){
			facesMessages.add(Severity.ERROR, "Houve um erro ao carregar as informações iniciais.");
		} catch (PJeDAOException e){
			facesMessages.add(Severity.ERROR, "Houve um erro de banco de dados ao carregar as informações iniciais.");
		} 
	}

	/**
	 * Carrega o processo judicial.
	 * 
	 * @return o processo judicial vinculado a esta atividade.
	 * @throws PJeBusinessException 
	 */
	private ProcessoTrf loadProcessoJudicial() throws PJeBusinessException{
		ProcessoJudicialManager processoJudicialManager = ComponentUtil.getComponent(ProcessoJudicialManager.class);
		return processoJudicialManager.findByProcessInstance(taskInstanceUtil.getProcessInstance());
	}

	/**
	 * Carrega o tipo de modelo do documento
	 * 
	 * @return List<TipoProcessoDocumento>
	 * @throws PJeBusinessException 
	 * @throws Exception 
	 */
	private List<ModeloDocumento> loadModeloDocumento() throws PJeBusinessException {
		String parametroTipoDocumento = ParametroUtil.getParametroPor("pje:bnmpii:tipoDocumento:", getTipoProcessoDocumento().getIdTipoProcessoDocumento()+"");	
		String parametroModelo = TipoProcessoDocumentoBNMP.getParametroModeloPor(parametroTipoDocumento);
		String idModeloDocumento = ParametroUtil.getParametro(parametroModelo);
		return Arrays.asList(ModeloDocumentoManager.instance().findById(Integer.valueOf(idModeloDocumento)));
	}

	/**
	 * Informa se existe partes selecionadas na tela de escolha de partes.
	 * 
	 * @return verdadeiro se existe partes selecionadas.
	 */
	public boolean existePartes(){
		return !getPartesSelecionadas().isEmpty();
	}

	/**
	 * Metodo para retorno do cpf/cnpj na coluna de destinatários do PAC
	 * 
	 * @param pessoa
	 */
	public String obtemCpfCnpj(Pessoa pessoa) {
		PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
		return pessoaService.obtemCpfCnpj(pessoa);
	}

	/**
	 * Metodo para remover uma parte selecionada
	 * 
	 * @param processoParte
	 */
	public void removeParte(ProcessoParte processoParte){
		ProcessInstance pi = taskInstanceUtil.getProcessInstance();
		try {
			pi.getContextInstance().deleteVariable(geraVariavel(Variaveis.PECA_INICIALIZADA_BNMP, processoParte));
			getPartesSelecionadas().remove(processoParte);
		} catch (Exception e){
			facesMessages.add(Severity.ERROR, "Não foi possível remover peça selecionada.");
			e.printStackTrace();
		}
	}
	/**
	 * Metodo para remover uma parte que tem peca cadastrada
	 * 
	 * @param processoParte
	 */
	public void removePartePecaCadastrada(ProcessoParte processoParte){
		ProcessInstance pi = taskInstanceUtil.getProcessInstance();
		Set<ProcessoParte> listaRetorno = new HashSet<ProcessoParte>(); 
		try {
			String parametroTipoDocumento = ParametroUtil.getParametroPor("pje:bnmpii:tipoDocumento:", getTipoProcessoDocumento().getIdTipoProcessoDocumento()+"");
			TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP = TipoProcessoDocumentoBNMP.buscaPor(parametroTipoDocumento);
			pi.getContextInstance().deleteVariable(geraVariavel(tipoProcessoDocumentoBNMP.getVariavelIdPeca(), processoParte));
			pi.getContextInstance().deleteVariable(geraVariavel(tipoProcessoDocumentoBNMP.getVariavelTipoPeca(), processoParte));
			pi.getContextInstance().deleteVariable(geraVariavel(Variaveis.PECA_INICIALIZADA_BNMP, processoParte));
			pi.getContextInstance().deleteVariable(geraVariavel(Variaveis.MINUTA_EM_ELABORACAO_BNMP, processoParte));
			pi.getContextInstance().deleteVariable(geraVariavel(Variaveis.ID_PECA_BNMP, processoParte));
			for(ProcessoParte parte:getPartesMandadoCadastradoList()) {
				if(parte.getIdPessoa() != processoParte.getIdPessoa()) {
					listaRetorno.add(parte);
				}
			}
			setPartesMandadoCadastrado(listaRetorno);
			getPartesMandadoCadastradoList();
		} catch (Exception e){
			facesMessages.add(Severity.ERROR, "Não foi possível remover peça selecionada.");
			e.printStackTrace();
		}
	}

	/**
	 * Metodo responsável por acrescentar na lista todas as partes do processo.
	 * 
	 */
	public void acrescentaParticipantes(){
		getPartesSelecionadas().addAll(recuperaListaPartePrincipalPassivo());
	}

	/**
	 * Metodo responsável por expandir a arvore de partes.
	 * 
	 */
	public void expandirTodaArvore() {
		TreeState componentState = (TreeState) richTree.getComponentState();
		try {
			componentState.expandAll(richTree);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void expandirTodaArvorePecas() {
		TreeState componentState = (TreeState) richTreePecas.getComponentState();
		try {
			componentState.expandAll(richTreePecas);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void expandirTodaArvorePecasGeradas() {
		TreeState componentState = (TreeState) richTreePecasGeradas.getComponentState();
		try {
			componentState.expandAll(richTreePecasGeradas);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Acrescenta os participantes do polo passivo como destinatários das intimações.
	 * 
	 */
	public void acrescentaPolo(){
		List<ProcessoParte> partesPolo = recuperaListaPartePrincipalPassivo();
		for (ProcessoParte parte : partesPolo){
			incluirParteLista(parte);
		}
	}

	/**
	 * Acrescenta os participantes do polo passivo com peça já inicializadas.
	 * 
	 */
	public void incluiPartesJaInicalizadas(){
		List<ProcessoParte> partesPolo = recuperaListaPartePrincipalPassivo();
		ProcessInstance pi = taskInstanceUtil.getProcessInstance();
		for (ProcessoParte parte : partesPolo){
			Object pecaInicializada = pi.getContextInstance().getVariable(geraVariavel(Variaveis.PECA_INICIALIZADA_BNMP, parte));
			if(parte.getPessoa().buscaNumeroDocumentoIdentificacaoAtivo("RJI") != null && pecaInicializada != null){
				incluirParteLista(parte);
			}
			if(existePeca(parte)) {
				incluirParteMandadoCadastrado(parte);
			}
		}
	}

	/** 
	 * Inclui a parte com peça já cadastrada
	 * 
	 * @param parte
	 */
	public void incluirParteMandadoCadastrado(ProcessoParte parte){
		getPartesMandadoCadastrado().add(parte);
	}

	/** 
	 * Inclui a parte no Set alterando a coluna de integraï¿½ï¿½o
	 * 
	 * @param parte
	 */
	public void incluirParteLista(ProcessoParte parte){
		getPartesSelecionadas().add(parte);
	}

	/**
	 * Metodo para retornar as partes Passivas
	 * 
	 * @return lista de ProcessoParte
	 */
	public List<ProcessoParte> recuperaListaPartePrincipalPassivo() {
		return processoParteManager.recuperaListaPartePrincipalPassivo(getProcessoJudicial(), isExibirPartesInativas()); 
	}

	public void carregaUrlDocumento(){
		try {

			if(getTipoProcessoDocumento()==null)
				throw new Exception("Erro: Não há tipo processo documento para esta peça");

			String parametroTipoDocumento = ParametroUtil.getParametroPor("pje:bnmpii:tipoDocumento:", getTipoProcessoDocumento().getIdTipoProcessoDocumento()+"");
			TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP = TipoProcessoDocumentoBNMP.buscaPor(parametroTipoDocumento);
			if(tipoProcessoDocumentoBNMP==null)
				throw new Exception("Erro: Não há tipo processo documento para esta peça");
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Não há tipo processo documento para este processo");
			e.printStackTrace();
		}
	}

	/**
	 * Metodo para avançar entre as paginas
	 * 
	 */
	public void proximoPasso() throws Exception {
		switch(getPasso()){
		case PAGINA_PARTES_PASSIVAS:
			limparModalSelecionarRji();
			limparUrlFrameBnmp();
			if(existePartes()){
				if(rjisPreenchidos()){
					setPasso(PAGINA_DOCUMENTO_BNMPII);
					salvaInicializacaoPecaBnmp();
				} else{
					facesMessages.add(Severity.ERROR, "Favor selecionar todos os RJIs!");
				}
			} else {
				facesMessages.add(Severity.ERROR, "Favor selecionar ao menos uma parte!");
			}
			break;
		case PAGINA_DOCUMENTO_BNMPII:
			limparUrlFrameBnmp();
			if(variaveisCadastradas()){
				setPasso(PAGINA_ELABORAR_MINUTA);
			} else {
				facesMessages.add(Severity.ERROR, "Favor cadastrar a peça para todas as partes!");
			}
			break;
		}
	}

	/**
	 * Metodo para retornar entre as paginas
	 * 
	 */
	public void passoAnterior(){
		switch(getPasso()){
		case PAGINA_DOCUMENTO_BNMPII:
			limparUrlFrameBnmp();
			setPasso(PAGINA_PARTES_PASSIVAS);
			break;
		case PAGINA_ELABORAR_MINUTA:
			limparElaborarMinuta();
			setPasso(PAGINA_DOCUMENTO_BNMPII);
			break;
		}
	}

	/**
	 * Metodo para salvar inicialização da peça.
	 * 
	 */
	public void salvaInicializacaoPecaBnmp(){
		try {

			if(getTipoProcessoDocumento()==null) throw new Exception("Não há tipo processo documento para este processo");
			ProcessInstance pi = taskInstanceUtil.getProcessInstance();
			Integer tipoProcessoDocumento = getTipoProcessoDocumento().getIdTipoProcessoDocumento();
			for (ProcessoParte parte : getPartesSelecionadas()) {
				pi.getContextInstance().setVariable(geraVariavel(Variaveis.PECA_INICIALIZADA_BNMP, parte), tipoProcessoDocumento);				
			}
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Erro ao salvar inicialização da peça no PJe para o processo {0}", getProcessoJudicial().getNumeroProcesso());
			e.printStackTrace();
		}
	}

	/**
	 * Metodo para conferir se existe uma variavel especifica cadastrada para todas as partes
	 * 
	 * @return boolean
	 * @throws Exception 
	 */
	private boolean variaveisCadastradas(){
		if(getTipoProcessoDocumento()==null) return false;
		
		String parametroTipoDocumento = ParametroUtil.getParametroPor("pje:bnmpii:tipoDocumento:", getTipoProcessoDocumento().getIdTipoProcessoDocumento()+"");
		TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP = TipoProcessoDocumentoBNMP.buscaPor(parametroTipoDocumento);
		if(tipoProcessoDocumentoBNMP==null) return false;

		for(ProcessoParte parte : getPartesSelecionadasList()){
			ProcessInstance pi = taskInstanceUtil.getProcessInstance();
			Object variavelIdPeca = pi.getContextInstance().getVariable(geraVariavel(tipoProcessoDocumentoBNMP.getVariavelIdPeca(), parte));
			Object variavelTipo = pi.getContextInstance().getVariable(geraVariavel(tipoProcessoDocumentoBNMP.getVariavelTipoPeca(), parte));
			if(variavelIdPeca != null && variavelTipo != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Metodo para conferir se todos os RJIs das partes selecionadas foram preenchidos
	 * 
	 * @return boolean
	 */
	private boolean rjisPreenchidos(){
		for(ProcessoParte parte : getPartesSelecionadasList()){
			if(parte.getPessoa().buscaNumeroDocumentoIdentificacaoAtivo("RJI") == null){
				return false;
			}
		}
		return true;
	}

	/**
	 * Metodo para buscas todos os RJIs da parte no BNMPII
	 * 
	 */
	public void buscarRji(ProcessoParte parte){
		try {
			setParteIntegracao(parte);
			setPage(getManipulaPecaService().buscaPorDuplicidade(LISTA_PAGE_PADRAO, LISTA_SIZE_PADRAO, montaPessoaFilter(getParteIntegracao())));
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Erro ao tentar buscar RJI da pessoa no BNMPII.");
			e.printStackTrace(); 
		}
	}

	/**
	 * Metodo para montar o filtro de pessoas duplicadas buscas consumido pelo BNMPII
	 * 
	 */
	private PessoaFilter montaPessoaFilter(ProcessoParte p){
		PessoaFisicaService pessoaFisicaService = ComponentUtil.getComponent(PessoaFisicaService.class);
		PessoaFisica pf = pessoaFisicaService.find(getParteIntegracao().getIdPessoa());
		PessoaFilter filter = new PessoaFilter();
		filter.setBuscaOrgaoRecursivo(false);
		filter.setNome(pf.getNomeParte());
		filter.setNomeMae(pf.getNomeGenitora());
		filter.setAtivo(true);	
		List<DocumentoDTO> listDocumentos = new LinkedList<DocumentoDTO>();
		for (PessoaDocumentoIdentificacao documento: pf.getPessoaDocumentoIdentificacaoList()) {
			DocumentoDTO documentoDTO = new DocumentoDTO();
			documentoDTO.setNumero(documento.getNumeroDocumento());
			listDocumentos.add(documentoDTO);
		}
		filter.setDocumentos(listDocumentos);
		return filter;
	}

	/**
	 * Adiciona o tipo de documento RJI na pessoa
	 * 
	 @ param peca
	 */
	public void salvarRjiParte(String numeroRJI){
		adicionarDocumentoRji(getParteIntegracao(), numeroRJI);
		PessoaManager pessoaManager = ComponentUtil.getComponent(PessoaManager.class);
		pessoaManager.mergeAndFlush(getParteIntegracao().getPessoa());
		//recarregaListas();
		limparModalSelecionarRji();
	}

	/**
	 * Gera um individuo no BNMP a partir dos dados da pessoa selecionada. E salva o RJI gerado na pessoa
	 * 
	 */
	public void gerarRjiParte(){
		try {
			PessoaFisicaService pessoaFisicaService = ComponentUtil.getComponent(PessoaFisicaService.class);
			PessoaFisica pf = pessoaFisicaService.find(getParteIntegracao().getIdPessoa());
			PessoaResponseDTO pessoa = getManipulaPecaService().salvarPessoa(montaDTOPorPessoaFisica(pf));
			adicionarDocumentoRji(getParteIntegracao(), pessoa.getNumeroIndividuoFormatado());
			PessoaManager pessoaManager = ComponentUtil.getComponent(PessoaManager.class);
			pessoaManager.mergeAndFlush(getParteIntegracao().getPessoa());
			//recarregaListas();
			limparModalSelecionarRji();
		} catch (DataNascimentoException dataNascimentoExpection) {
			facesMessages.add(Severity.ERROR, "Erro ao tentar gerar RJI da pessoa no BNMP - Data Nascimento é obrigatório: "+dataNascimentoExpection.getMessage() );

		} catch ( Exception e ) {
			facesMessages.add(Severity.ERROR, "Erro ao tentar gerar RJI da pessoa no BNMP: " + e.getMessage());
			limparModalSelecionarRji();
		}
	}

	/**
	 * Salva um conjunto de dados retornados via Callback do BNMPII , em variaveis de fluxo
	 * 
	 */
	public void salvarPeca() {
		
		
		if (getTipoProcessoDocumento() == null)
			return;

		String parametroTipoDocumento = ParametroUtil.getParametroPor("pje:bnmpii:tipoDocumento:", getTipoProcessoDocumento().getIdTipoProcessoDocumento()+"");
		TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP = TipoProcessoDocumentoBNMP.buscaPor(parametroTipoDocumento);
		if (tipoProcessoDocumentoBNMP == null)
			return;

		ProcessInstance pi = taskInstanceUtil.getProcessInstance();
		tipoProcessoDocumentoBNMP = TipoProcessoDocumentoBNMP.buscaPor(parametroTipoDocumento);
		pi.getContextInstance().setVariable(geraVariavel(tipoProcessoDocumentoBNMP.getVariavelIdPeca(), getParteIntegracao()), getIdPeca());
		pi.getContextInstance().setVariable(geraVariavel(tipoProcessoDocumentoBNMP.getVariavelTipoPeca(), getParteIntegracao()), getTipoProcessoDocumento().getCodigoDocumento());
		limparUrlFrameBnmp();
	}

	/**
	 * Retorna true se já existe o registro daquela peca nas variaveis de fluxo
	 * 
	 * @param parte
	 * @return boolean
	 */
	public boolean existePeca(ProcessoParte parte){
		ProcessInstance pi = taskInstanceUtil.getProcessInstance();
		return verificarExistenciaDePeca(parte, pi);
	}

	/**
	 * Verifica existencia da peca nas varivel de fluxo
	 * 
	 * @param parte
	 * @param pi
	 * @return
	 */
	private boolean verificarExistenciaDePeca(ProcessoParte parte, ProcessInstance pi) {
		
		
		if (getTipoProcessoDocumento()==null)
			return false;
		String parametroTipoDocumento = ParametroUtil.getParametroPor("pje:bnmpii:tipoDocumento:", getTipoProcessoDocumento().getIdTipoProcessoDocumento()+"");
		TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP = TipoProcessoDocumentoBNMP.buscaPor(parametroTipoDocumento);
		if(tipoProcessoDocumentoBNMP == null)
			return false;

		Object variavelIdPeca = pi.getContextInstance().getVariable(geraVariavel(tipoProcessoDocumentoBNMP.getVariavelIdPeca(), parte));
		Object variavelTipo = pi.getContextInstance().getVariable(geraVariavel(tipoProcessoDocumentoBNMP.getVariavelTipoPeca(), parte));
		if(variavelIdPeca != null && variavelTipo != null) {
			return true;
		}
		return false;
	}

	/**
	 * Metodo para salvar a minuta elaborada da parte.
	 * 
	 */
	public void salvarMinuta(){
		try {

			
			if(getTipoProcessoDocumento()==null)
				throw new Exception("Erro: Não há tipo processo documento para esta peça");

			String parametroTipoDocumento = ParametroUtil.getParametroPor("pje:bnmpii:tipoDocumento:", getTipoProcessoDocumento().getIdTipoProcessoDocumento()+"");
			TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP = TipoProcessoDocumentoBNMP.buscaPor(parametroTipoDocumento);
			if(tipoProcessoDocumentoBNMP==null)
				throw new Exception("Erro: Não há tipo processo documento para esta peça");


			ProcessInstance pi = taskInstanceUtil.getProcessInstance();
			Integer tipoProcessoDocumento = getTipoProcessoDocumento().getIdTipoProcessoDocumento();
			String idPecaBNMP = (String)pi.getContextInstance().getVariable(geraVariavel(tipoProcessoDocumentoBNMP.getVariavelIdPeca(), getParteIntegracao()));
			pi.getContextInstance().setVariable(geraVariavel(Variaveis.MINUTA_EM_ELABORACAO_BNMP, getParteIntegracao()), tipoProcessoDocumento);
			pi.getContextInstance().setVariable(geraVariavel(Variaveis.ID_PECA_BNMP, getParteIntegracao()), idPecaBNMP+"");
			limparElaborarMinuta();
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Erro ao salvar minuta de documento para o processo {0}", getProcessoJudicial().getNumeroProcesso());
			e.printStackTrace();
		}
	}

	/**
	 * Retorna true se já existe o registro daquele documento nas variaveis de fluxo
	 * 
	 * @param parte
	 * @return boolean
	 */
	public boolean existeDocumento(ProcessoParte parte){
		ProcessInstance pi = taskInstanceUtil.getProcessInstance();
		Object variavelMinuta = pi.getContextInstance().getVariable(geraVariavel(Variaveis.MINUTA_EM_ELABORACAO_BNMP, parte));
		return variavelMinuta != null;
	}

	/**
	 * Retorna true se já existe o registro de todas as minutas em variaveis de fluxo
	 * 
	 * @param parte
	 * @return boolean
	 */
	public boolean validaDisponibilizarParaAssinatura(){
		ProcessInstance pi = taskInstanceUtil.getProcessInstance();
		Object minutaEmElaboracaoBnmp = null;
		for (ProcessoParte parte : getPartesSelecionadas()) {
			minutaEmElaboracaoBnmp = pi.getContextInstance().getVariable(geraVariavel(Variaveis.MINUTA_EM_ELABORACAO_BNMP, parte));
		}
		return minutaEmElaboracaoBnmp != null;
	}

	/**
	 * Metodo que gera as variaveis de fluxo que são criadas por pessoa
	 * 
	 * @return string
	 * @param variavel, parte
	 */
	private String geraVariavel(String variavel, ProcessoParte parte){
		return variavel + "-" + parte.getPessoa().buscaNumeroDocumentoIdentificacaoAtivo("RJI");
	}

	/**
	 * Metodo para montar o documento HTML
	 * 
	 * @return string
	 * @param md
	 */
	private String montaDocumentoHtml(ModeloDocumento md){
		String documentoHtml = md.getModeloDocumento();
		return ProcessoDocumentoHome.processarModelo(documentoHtml);
	}

	/**
	 * Metodo para recarregar a referï¿½ncia das listas alteradas
	 * 
	 */
	/*	private void recarregaListas(){
		for(ProcessoParte p1 : recuperaListaPartePrincipalPassivo()){
			Set<ProcessoParte> it = getPartesSelecionadas();
			for(ProcessoParte parte : it) {
				System.out.println(p1.getNomeParte());
				if(p1.getIdProcessoParte() != parte.getIdProcessoParte()){
					//getPartesSelecionadas().remove(parte);
					//incluir(p1);
					incluirParteLista(p1);
				}
			}
			while(it.hasNext()){
				ProcessoParte p2 = it.next();
				if(p1.getIdProcessoParte() == p2.getIdProcessoParte()){
					getPartesSelecionadas().remove(p2);
					incluirParteLista(p1);
				}
			}
		}
	}*/

	public void incluir(ProcessoParte parte){
		getPartesSelecionadas().add(parte);
	}

	/**
	 * Metodo para montar o DTO de pessoa a partir das informações da pessoa fisica
	 * 
	 * @return pessoaDTO
	 * @param pf
	 * @throws IOException 
	 * @throws DataNascimentoExpection 
	 */
	@SuppressWarnings({ "deprecation", "unchecked" })
	public PessoaDTO montaDTOPorPessoaFisica(PessoaFisica pf) throws IOException, DataNascimentoException{
		PessoaDTO pessoa = new PessoaDTO();
		DadosGeraisPessoaDTO dadosGerais = new DadosGeraisPessoaDTO();
		String cdOrgao = ConfiguracaoIntegracaoCloud.getBnmpCodigoOrgao();
		pessoa.setEnderecos(Collections.EMPTY_LIST);
		pessoa.getOutrosNomes().add(retornaOutrosNomes(pf));
		pessoa.getDataNascimento().add(retornaDataNascimento(pf.getDataNascimento()));		
		adicionaDocumento(pf, pessoa);	
		pessoa.setFotos(Collections.EMPTY_LIST);
		pessoa.setSinaisMarcas(new SinaisMarcasDTO());
		if(pf.getNomeAlcunha() != null){
			pessoa.getOutrasAlcunhas().add(new OutrasAlcunhasDTO(pf.getNomeAlcunha()));
			dadosGerais.setAlcunha(pf.getNomeAlcunha());
		} else {
			pessoa.getOutrasAlcunhas().add(new OutrasAlcunhasDTO(NAO_INFORMADO));
			dadosGerais.setAlcunha(NAO_INFORMADO);
		}
		if(pf.getNomeGenitor() != null){
			pessoa.getNomePai().add(new NomePaiDTO(pf.getNomeGenitor()));
			dadosGerais.setNomePai(pf.getNomeGenitor());
		} else {
			pessoa.getNomePai().add(new NomePaiDTO(NAO_INFORMADO));
			dadosGerais.setNomePai(NAO_INFORMADO);
		}
		if(pf.getNomeGenitora() != null){
			pessoa.getNomeMae().add(new NomeMaeDTO(pf.getNomeGenitora()));
			dadosGerais.setNomeMae(pf.getNomeGenitora());
		} else {
			pessoa.getNomeMae().add(new NomeMaeDTO(NAO_INFORMADO));
			dadosGerais.setNomeMae(NAO_INFORMADO);
		}
		dadosGerais.setEndereco(new EnderecoDTO());
		dadosGerais.setNome(pf.getNome());
		new DataNascimentoDTO(pf.getDataNascimento());
		dadosGerais.setDataNascimento(retornaDataNascimento(pf.getDataNascimento()).getDataNascimento());
		dadosGerais.setSexo(retornaSexo(pf));
		dadosGerais.setIdTribunal(Long.parseLong(cdOrgao));
		pessoa.setDadosGeraisPessoa(dadosGerais);
		return pessoa;
	}


	private DocumentoDTO adicionaDocumento(PessoaFisica pf, PessoaDTO pessoaDTO) {	
		
		DocumentoDTO documentoDTO = null;		
		if (pf.getDocumentoCpfCnpj()!=null){				
			documentoDTO = new DocumentoDTO();
			documentoDTO.setOutrosNomes(retornaOutrosNomes(pf));
			TipoDocumentoDTO tipoDocumentoDTO = new TipoDocumentoDTO();
			tipoDocumentoDTO.setId(6L);			
			documentoDTO.setTipoDocumento(tipoDocumentoDTO);
			documentoDTO.setNumero(pf.getDocumentoCpfCnpj());			
			pessoaDTO.getDocumentos().add(documentoDTO);		
		} 		
		return documentoDTO;
	}



	private OutrosNomesDTO retornaOutrosNomes(PessoaFisica pf) {
		return new OutrosNomesDTO(pf.getNome());
	}

	private DataNascimentoDTO retornaDataNascimento(Date dataNascimento) throws DataNascimentoException {
		DataNascimentoDTO dataNascimentoDTO;
		if(dataNascimento!=null) {
			dataNascimentoDTO = new DataNascimentoDTO(dataNascimento);
		}
		else {
			throw new DataNascimentoException();
		}
		return dataNascimentoDTO;
	}


	/**
	 * Metodo para criar o DTO de sexo a partir do sexo da pessoa
	 * 
	 * @param pf
	 */
	private SexoDTO retornaSexo(PessoaFisica pf){
		SexoDTO sexo = new SexoDTO();
		if(pf.getSexo()!=null) {
			if(pf.getSexo().equals(SexoEnum.M)){
				sexo.setId(1l);
			}else{
				sexo.setId(2l);
			}
		}else {
			sexo.setId(1L);
		}
		return sexo;
	}

	/**
	 * Metodo para criar o documento de identificação RJI
	 * 
	 * @param parte, rji
	 */
	private void adicionarDocumentoRji(ProcessoParte parte, String rji){
		TipoDocumentoIdentificacao tipoRJI = TipoDocumentoIdentificacaoHome.getHome().getTipoDocumentoIdentificacao(
				TipoDocumentoIdentificacaoHome.TIPORJI);
		PessoaDocumentoIdentificacao docRji = new PessoaDocumentoIdentificacao();
		docRji.setTipoDocumento(tipoRJI);
		docRji.setNumeroDocumento(rji);
		docRji.setNome(parte.getNomeParte());
		docRji.setUsadoFalsamente(false);
		docRji.setAtivo(true);
		docRji.setOrgaoExpedidor("Conselho Nacional de Justiça");
		docRji.setPessoa(parte.getPessoa());
		docRji.setDocumentoPrincipal(false);
		docRji.setTemporario(false);

		parte.getPessoa().getPessoaDocumentoIdentificacaoList().add(docRji);

	}

	/**
	 * Metodo para limpar as informações da modal de busca de RJIs
	 * 
	 */
	public void limparModalSelecionarRji(){
		setPage(new EntityPageDTO<PessoaListDTO>() );
		getPage().setContent(new ArrayList<PessoaListDTO>());
		setParteIntegracao(null);
	}

	/**
	 * Metodo para limpar as informações da pagina que contem o frame do BNMPII
	 * 
	 */
	public void limparUrlFrameBnmp(){
		setUrlFrameBnmp(null);
		setParteIntegracao(null);
		incluiPartesJaInicalizadas();
	}

	/**
	 * Metodo para limpar as informações da pagina de minuta.
	 * 
	 */
	public void limparElaborarMinuta(){
		setModeloDocumento(null);
		setParteIntegracao(null);
		setProcessoDocumentoBin(null);
	}

	/**
	 * Metodo para iniciar o processo de renderização, do frame especifica da parte selecionada
	 * 
	 * CADASTRO DE DOCUMENTO NO BNMP-II
	 * 
	 * PASSO 2
	 * 
	 */
	public void mostraFrameBnmp(ProcessoParte parte){
		setParteIntegracao(parte);
		String rji = StringUtil.removeNaoAlphaNumericos(parte.getPessoa().buscaNumeroDocumentoIdentificacaoAtivo("RJI"));
		String numProcesso = StringUtil.removeNaoAlphaNumericos(processoJudicial.getNumeroProcesso());
		String parametroTipoDocumento = ParametroUtil.getParametroPor("pje:bnmpii:tipoDocumento:", getTipoProcessoDocumento().getIdTipoProcessoDocumento()+"");
		TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP = TipoProcessoDocumentoBNMP.buscaPor(parametroTipoDocumento);
		PecaDTO filtro = new PecaDTO(rji);
		filtro.setOrgao(new OrgaoDTO(Integer.toUnsignedLong(Authenticator.getOrgaoJulgadorAtual().getNumeroVara())));
		EntityPageDTO<PecaDTO> resultado = getManipulaPecaService().getPecasPor(tipoProcessoDocumentoBNMP,filtro);
		if(resultado.getTotalElements()==0) {
			setUrlFrameBnmp(getManipulaPecaService().recuperaURLWebPor(tipoProcessoDocumentoBNMP).concat(getManipulaPecaService().getAcaoCadastro(tipoProcessoDocumentoBNMP)).concat("/").concat(rji).concat("/").concat(numProcesso).concat("/").concat(SIGLA_SISTEMA));
		}else if(resultado.getTotalElements()==1) {
			long idPecaBNMP = resultado.getContent().get(0).getId();
			
			if(resultado.getContent().get(0).getStatus().getId().equals(StatusDTO.ELABORACAO))
				setUrlFrameBnmp(getManipulaPecaService().recuperaURLWebPor(tipoProcessoDocumentoBNMP).concat("editar").concat("/").concat(idPecaBNMP+"").concat("/").concat(numProcesso).concat("/").concat(SIGLA_SISTEMA));
			else {
				setUrlFrameBnmp(getManipulaPecaService().recuperaURLWebSearchPathPor(tipoProcessoDocumentoBNMP).concat(rji).concat("/").concat(numProcesso).concat("/").concat(SIGLA_SISTEMA));
			}

		}else {
			setUrlFrameBnmp(getManipulaPecaService().recuperaURLWebSearchPathPor(tipoProcessoDocumentoBNMP).concat(rji).concat("/").concat(numProcesso).concat("/").concat(SIGLA_SISTEMA));
		}

	}

	/**
	 * 
	 * ELABORAR MINUTA
	 * PASSO 3
	 * 
	 * Metodo para iniciar o processo de renderização, da pagina de minuta especifica da parte selecionada
	 * @throws PJeException 
	 * @throws ClientErrorException 
	 * 
	 */
	public void mostraElaboracaoMinuta(ProcessoParte parte) throws ClientErrorException, PJeException{
		try {

			
			if(getTipoProcessoDocumento()==null)
				throw new Exception("Erro: Não há tipo processo documento para esta peça");

			String parametroTipoDocumento = ParametroUtil.getParametroPor("pje:bnmpii:tipoDocumento:", getTipoProcessoDocumento().getIdTipoProcessoDocumento()+"");
			TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP = TipoProcessoDocumentoBNMP.buscaPor(parametroTipoDocumento);
			if(tipoProcessoDocumentoBNMP==null)
				throw new Exception("Erro: Não há tipo processo documento para esta peça");

			ProcessInstance pi = taskInstanceUtil.getProcessInstance();
			
			String idPecaBNMP = (String)pi.getContextInstance().getVariable(geraVariavel(tipoProcessoDocumentoBNMP.getVariavelIdPeca(), parte));
			manipulaPecaService.populaConteudoPeca(tipoProcessoDocumentoBNMP, Long.valueOf(idPecaBNMP));
			setModeloDocumento(getModelosDocumentos().get(0));
			setProcessoDocumentoBin(new ProcessoDocumentoBin());
			atualizaModelo();
			expandirTodaArvorePecasGeradas();
			setParteIntegracao(parte);
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Não há tipo processo documento para este processo");
			e.printStackTrace();
		}
	}

	/**
	 * Metodo para atualizar o modelo
	 * 
	 */
	public void atualizaModelo(){
		String documentoHtml = montaDocumentoHtml(getModeloDocumento());
		getProcessoDocumentoBin().setModeloDocumento(documentoHtml);
	}

	@Override
	protected BaseManager<ProcessoParte> getManager() {
		return processoParteManager;
	}

	@Override
	public EntityDataModel<ProcessoParte> getModel() {
		return this.model;
	}

	public ProcessoTrf getProcessoJudicial() {
		return processoJudicial;
	}

	public void setProcessoJudicial(ProcessoTrf processoJudicial) {
		this.processoJudicial = processoJudicial;
	}

	public Set<ProcessoParte> getPartesSelecionadas() {
		return partesSelecionadas;
	}

	public void setPartesSelecionadas(Set<ProcessoParte> partesSelecionadas) {
		this.partesSelecionadas = partesSelecionadas;
	}

	public List<ProcessoParte> getPartesSelecionadasList() {
		expandirTodaArvorePecasGeradas();
		return new ArrayList<ProcessoParte>(getPartesSelecionadas());
	}

	public List<ProcessoParte> getPartesMandadoCadastradoList() {

		ArrayList<ProcessoParte> selecionadas = new ArrayList<ProcessoParte>(getPartesSelecionadas());
		ArrayList<ProcessoParte> filtradas = new ArrayList<ProcessoParte>();

		for(ProcessoParte pp:selecionadas) {

			if(existePeca(pp)){
				filtradas.add(pp);
			}
		}
		return filtradas;
	}

	public boolean isExibirPartesInativas() {
		return exibirPartesInativas;
	}

	public void setExibirPartesInativas(boolean exibirPartesInativas) {
		this.exibirPartesInativas = exibirPartesInativas;
	}

	public Set<ProcessoParte> getPartesMandadoCadastrado() {
		return partesMandadoCadastrado;
	}

	public void setPartesMandadoCadastrado(Set<ProcessoParte> partesMandadoCadastrado) {
		this.partesMandadoCadastrado = partesMandadoCadastrado;
	}

	public String geturlFrameBnmp() {
		return urlFrameBnmp;
	}

	public void setUrlFrameBnmp(String urlFrameBnmp) {
		this.urlFrameBnmp = urlFrameBnmp;
	}

	public List<TipoProcessoDocumento> getTiposProcessoDocumento() {
		return tiposProcessoDocumento;
	}

	public void setTiposProcessoDocumento(List<TipoProcessoDocumento> tiposProcessoDocumento) {
		this.tiposProcessoDocumento = tiposProcessoDocumento;
	}

	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumentoViaFluxo() {
		ProcessInstance pi = taskInstanceUtil.getProcessInstance();
		this.tipoProcessoDocumento = (TipoProcessoDocumento) pi.getContextInstance().
				getVariable(Variaveis.TIPO_DOCUMENTO_PAJ);
	}

	public ProcessoParte getParteIntegracao() {
		return parteIntegracao;
	}

	public void setParteIntegracao(ProcessoParte parteIntegracao) {
		this.parteIntegracao = parteIntegracao;
	}

	public EntityPageDTO<PessoaListDTO> getPage() {
		return page;
	}

	public void setPage(EntityPageDTO<PessoaListDTO> page) {
		this.page = page;
	}

	public ManipulaPecaService getManipulaPecaService() {
		return manipulaPecaService;
	}

	public void setManipulaPecaService(ManipulaPecaService manipulaPecaService) {
		this.manipulaPecaService = manipulaPecaService;
	}

	public List<ModeloDocumento> getModelosDocumentos() {
		return modelosDocumentos;
	}

	public void setModelosDocumentos(List<ModeloDocumento> modelosDocumentos) {
		this.modelosDocumentos = modelosDocumentos;
	}

	public ModeloDocumento getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	public Integer getPasso() {
		return passo;
	}

	public void setPasso(Integer passo) {
		this.passo = passo;
	}

	public ProcessoDocumentoBin getProcessoDocumentoBin() {
		return processoDocumentoBin;
	}

	public void setProcessoDocumentoBin(ProcessoDocumentoBin processoDocumentoBin) {
		this.processoDocumentoBin = processoDocumentoBin;
	}

	public String getIdPeca() {
		return idPeca;
	}

	public void setIdPeca(String idPeca) {
		this.idPeca = idPeca;
	}

	public String recuperaURLParaAutenticarIFRAME() {
		String urlComTokenJWT=null;
		try {
			urlComTokenJWT = getManipulaPecaService().recuperaURLComTokenJWT();
			setBrowserEstaAutenticadoBNMP(true);
		}catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Erro ao efetuar autenticao no BNMPII.");
			e.printStackTrace();
		}
		return urlComTokenJWT;
	}

	/**
	 * 
	 * Visualiza PECA NO BNMP-II
	 * 
	 */
	public void visualizaPecaBNMP(ProcessoParte parte){

		try {
			

			if(getTipoProcessoDocumento()==null)	throw new Exception("Erro: Não há tipo processo documento para esta peça");
			
			String parametroTipoDocumento = ParametroUtil.getParametroPor("pje:bnmpii:tipoDocumento:", getTipoProcessoDocumento().getIdTipoProcessoDocumento()+"");
			TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP = TipoProcessoDocumentoBNMP.buscaPor(parametroTipoDocumento);
			if(tipoProcessoDocumentoBNMP==null) 	throw new Exception("Erro: Não há tipo processo documento para esta peça");

			setParteIntegracao(parte);
			ProcessInstance pi = taskInstanceUtil.getProcessInstance();
			String idPecaBNMP = (String)pi.getContextInstance().getVariable(geraVariavel(tipoProcessoDocumentoBNMP.getVariavelIdPeca(), parte));
			String numProcesso = StringUtil.removeNaoAlphaNumericos(processoJudicial.getNumeroProcesso());
			setUrlFrameBnmp(getManipulaPecaService().recuperaURLWebPor(tipoProcessoDocumentoBNMP).concat("visualizar").concat("/").concat(idPecaBNMP+"").concat("/").concat(numProcesso).concat("/").concat(SIGLA_SISTEMA));
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Não há tipo processo documento para este processo");
			e.printStackTrace();
		}

	}
	
	/**
	 * 
	 * Visualiza a Lista de PECA NO BNMP-II
	 * 
	 */
	public void visualizaListaDePecaBNMP(ProcessoParte parte){

		try {
			

			if(getTipoProcessoDocumento()==null)	throw new Exception("Erro: Não há tipo processo documento para esta peça");
			
			String parametroTipoDocumento = ParametroUtil.getParametroPor("pje:bnmpii:tipoDocumento:", getTipoProcessoDocumento().getIdTipoProcessoDocumento()+"");
			TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP = TipoProcessoDocumentoBNMP.buscaPor(parametroTipoDocumento);
			
			if(tipoProcessoDocumentoBNMP==null) 	throw new Exception("Erro: Não há tipo processo documento para esta peça");
			
			String rji = StringUtil.removeNaoAlphaNumericos(parte.getPessoa().buscaNumeroDocumentoIdentificacaoAtivo("RJI"));
			String numProcesso = StringUtil.removeNaoAlphaNumericos(processoJudicial.getNumeroProcesso());
			setUrlFrameBnmp(getManipulaPecaService().recuperaURLWebSearchPathPor(tipoProcessoDocumentoBNMP).concat(rji).concat("/").concat(numProcesso).concat("/").concat(SIGLA_SISTEMA));
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Não há tipo processo documento para este processo");
			e.printStackTrace();
		}

	}

	
	public Boolean getBrowserEstaAutenticadoBNMP() {
		return browserEstaAutenticadoBNMP;
	}

	public void setBrowserEstaAutenticadoBNMP(Boolean browserEstaAutenticadoBNMP_) {
		this.browserEstaAutenticadoBNMP = browserEstaAutenticadoBNMP_;
	}
	
	public void assinaUmaPecaComPapelDeServidorNoBNMP() throws ClientErrorException, PJeException{
		
		String parametroTipoDocumento = ParametroUtil.getParametroPor("pje:bnmpii:tipoDocumento:", getTipoProcessoDocumento().getIdTipoProcessoDocumento()+"");
		
		TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP = TipoProcessoDocumentoBNMP.buscaPor(parametroTipoDocumento);
		manipulaPecaService.assinarServidor(tipoProcessoDocumentoBNMP,
				String.valueOf(tipoProcessoDocumentoBNMP.getVariavelIdPeca()));
	}

	public String getUrlWebBnmp() {
		return urlWebBnmp;
	}

	public void setUrlWebBnmp(String urlWebBnmp) {
		this.urlWebBnmp = urlWebBnmp;
	}

	public String getSenhaBnmp() {
		return senhaBnmp;
	}

	public void setSenhaBnmp(String senhaBnmp) {
		this.senhaBnmp = senhaBnmp;
	}
	
	public void logar() {
		try {
			getManipulaPecaService().autentica(senhaBnmp);
		} catch (ClientErrorException | PJeException e) {
			senhaBnmp = null;
			facesMessages.add(Severity.ERROR, "Usuário ou senha inválidos!");
		}
		
	}
	
	public boolean naoEstaAutenticado() {
		return (Authenticator.getUsuarioBNMPLogado()==null);
	}
	
	public boolean estaAutenticado() {
		return (Authenticator.getUsuarioBNMPLogado()!=null);
	}
	
	private Optional<Long> localizaPecaPorStatus(List<PecaDTO> content) {
		return content.stream()
		.filter( (p) ->  StatusDTO.AGUARDANDO_ASSINATURA.equals(p.getStatus().getId()) || StatusDTO.PENDENTE_CUMPRIMENTO.equals(p.getStatus().getId()))
		.map(x->x.getId())
		.findAny();
	}
	
	public Boolean isMinutaConcluida() {
		if (existePartes()) {
			for (ProcessoParte parte : getPartesSelecionadas()) {
				if (existeDocumento(parte)) {
					return true;
				}
			}
		}
		return false;
	}

}
