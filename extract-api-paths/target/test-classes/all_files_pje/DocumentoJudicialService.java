package br.jus.cnj.pje.nucleo.manager;

import static br.com.itx.util.ComponentUtil.getSessaoProcessoDocumentoManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.component.ValidacaoAssinaturaProcessoDocumento;
import br.com.infox.cliente.home.AlertaHome;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.ibpm.component.tree.ComplementoBean;
import br.com.infox.ibpm.component.tree.EventoBean;
import br.com.infox.ibpm.component.tree.MovimentoBean;
import br.com.infox.ibpm.component.tree.ValorComplementoBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.UsuarioHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.manager.EventoAgrupamentoManager;
import br.com.infox.pje.manager.ModeloDocumentoLocalManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.infox.pje.webservice.consultaoutrasessao.EncryptionSecurity;
import br.com.infox.utils.Constantes.DESCRICAO_POLO_JUNTADA;
import br.com.infox.utils.ExpressionsUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FileUtil;
import br.jus.cnj.pje.business.dao.ElasticDAO;
import br.jus.cnj.pje.business.dao.ProcessoDocumentoDAO;
import br.jus.cnj.pje.business.dao.SessaoPautaProcessoTrfDAO;
import br.jus.cnj.pje.business.dao.SessaoProcessoDocumentoDAO;
import br.jus.cnj.pje.business.dao.SessaoProcessoDocumentoVotoDAO;
import br.jus.cnj.pje.business.dao.TipoProcessoDocumentoDAO;
import br.jus.cnj.pje.entidades.vo.ConsultaDocumentoIndexadoVO;
import br.jus.cnj.pje.entidades.vo.ResultadoComplexoVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.service.AutomacaoTagService;
import br.jus.cnj.pje.nucleo.service.BaseService;
import br.jus.cnj.pje.nucleo.service.CertificadoDigitalService;
import br.jus.cnj.pje.nucleo.service.LocalizacaoService;
import br.jus.cnj.pje.nucleo.service.MiniPacService;
import br.jus.cnj.pje.nucleo.service.PapelService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualImpl;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.servicos.DateService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.PaginatedDataModel;
import br.jus.cnj.pje.vo.AcordaoCompilacao;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.je.pje.entity.vo.ProcessoDocumentoVO;
import br.jus.pje.indexacao.Indexador;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoLido;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoPapel;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;
import br.jus.pje.nucleo.enums.TipoDocumentoEnum;
import br.jus.pje.nucleo.enums.TipoOrigemAcaoEnum;
import br.jus.pje.nucleo.util.Crypto;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.QrCodeUtil;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(DocumentoJudicialService.NAME)
public class DocumentoJudicialService extends BaseService{

	public static final String NAME = "documentoJudicialService";

	
	public static final String SIG_SEPARATOR = "___SIGNSEP___";

	public static final String PROCESSO_DOCUMENTO_MANAGER_CMAA = "processoDocumentoCMAA";

	@In
	private UsuarioService usuarioService;
	
	@In
	private PapelManager papelManager;
	
	
	@Logger
	private Log logger;

	/**
	 * Recupera um documento vazio, marcado como ativo e não sigiloso e, se disponível, vinculado a uma determinada
	 * tarefa de jBPM. O documento também terá um elemento {@link ProcessoDocumentoBin} novo a ele vinculado.
	 * 
	 * @return o documento novo.
	 */
	public ProcessoDocumento getDocumento(){
		return getNovoDocumento(null);
	}
	
	public ProcessoDocumento getNovoDocumento(String texto) {
		return this.getNovoDocumento(texto, null);
	}
	
	public ProcessoDocumento getNovoDocumento(String texto, Long jbpmTask){
		ProcessoDocumento pd = getProcessoDocumentoManager().getDocumento(jbpmTask);
		pd.setProcessoDocumentoBin(getProcessoDocumentoBinManager().getProcessoDocumentoBin(texto));
		return pd;
	}

	/**
	 * Recupera o documento que tem o identificador dado, ou nulo, se ele não existir.
	 *  
	 * @param identificador o identificador do documento
	 * @return o documento solicitado, ou nulo, caso não exista
	 * @throws PJeBusinessException caso haja algum erro ao tentar recuperar o documento
	 * 
	 * @see #getDocumento(Integer, ProcessoTrf)
	 */
	public ProcessoDocumento getDocumento(Integer identificador) throws PJeBusinessException{
		return getDocumento(identificador, null);
	}
	
	
	public boolean isDocumentoVisivel(Integer identificador) {
		return getDocumento(identificador, null) != null;
	}
	public boolean isDocumentoVisivel(Integer identificador, ProcessoTrf processoJudicial) {
		return getDocumento(identificador, processoJudicial) != null;
	}

	/**
	 * Recupera o documento com o identificador dado, pretensamente pertencente ao processo judicial indicado.
	 * Esse método assegura que o acesso somente seja feito nas seguintes condições, cumulativamente:
	 * <li> o documento exista</li>
	 * <li> caso o documento seja sigiloso, que o usuário logado (ver {@link UsuarioService#getUsuarioLogado()}} seja
	 * o responsável pela inclusão ou pela alteração do documento, magistrado do órgão julgador a que pertence 
	 * o processo em que está o documento, ou pessoa previamente autorizada a ver o documento 
	 * {@link ProcessoDocumentoVisibilidadeSegredoManager#visivel(ProcessoDocumento, Usuario)}.
	 * 
	 * @param identificador o identificador do documento que se pretende recuperar
	 * @param processoJudicial o processo judicial ao qual está vinculado o documento.
	 * @return o documento com o identificador dado, ou nulo, caso não seja possível acessar o documento ou caso ele não exista.
	 */
	public ProcessoDocumento getDocumento(Integer identificador, ProcessoTrf processoJudicial) {
		if (identificador != null){
			try {
				ProcessoDocumento doc = getProcessoDocumentoManager().findById(identificador);
				if(doc == null){
					return doc;
				}
				if (processoJudicial != null && !processoJudicial.equals(doc.getProcessoTrf())) {
					return null;
				}
				Identity identity = Identity.instance();
				UsuarioLocalizacao ul = Authenticator.getUsuarioLocalizacaoAtual();
				if (!visivel(doc, ul, identity)){
					return null;
				} else if (ComponentUtil.getComponent(ProcessoJudicialService.class).visivel(processoJudicial != null ? processoJudicial : doc.getProcessoTrf(), ul, identity)){
					return doc;
				}
			} catch (PJeBusinessException e) {
				logger.error("Erro ao recuperar documento de id [{0}]: {1}", identificador, e.getMessage());
			}
		}
		return null;
	}
	
	public boolean podeVisualizar(ProcessoDocumento doc, UsuarioLocalizacao loc) {
		UsuarioLocalizacaoMagistradoServidor localInterno = loc.getUsuarioLocalizacaoMagistradoServidor();
		boolean papelAutorizado = Authenticator.isVisualizaSigiloso() || Authenticator.isMagistrado();
		boolean processoDeslocado = getProcessoJudicialService().existeFluxoDeslocadoParaLocalizacao(doc.getProcessoTrf());
		boolean pertenceOrgao =
				(	
						doc.getProcessoTrf() != null
						&& localInterno != null 
						&& ( 
									(
											localInterno.getOrgaoJulgador() != null && localInterno.getOrgaoJulgador().equals(doc.getProcessoTrf().getOrgaoJulgador())
									) 
									||
									(
											localInterno.getOrgaoJulgador() == null 
											&& localInterno.getOrgaoJulgadorColegiado() != null 
											&& localInterno.getOrgaoJulgadorColegiado().equals(doc.getProcessoTrf().getOrgaoJulgadorColegiado())
									)
							) 
				);
		return loc.getUsuario().getIdUsuario().equals(doc.getUsuarioInclusao().getIdUsuario())
			// usu�rio que incluiu
			|| (loc.getUsuario().getIdUsuario().equals(doc.getUsuarioAlteracao().getIdUsuario()))
			// usu�rio que alterou
			|| (papelAutorizado && (pertenceOrgao || processoDeslocado));
			// Pode ver e pertence ao mesmo �rg�o ou processo deslocado
	}
	
	private boolean visivel(ProcessoDocumento doc, UsuarioLocalizacao loc, Identity identity){
		if(!doc.getDocumentoSigiloso()){
			return true;
		}
		if(identity == null || loc == null){
			return false;
		}
		if (podeVisualizar(doc, loc)){
			return true;
		}else if (ComponentUtil.getComponent(ProcessoDocumentoVisibilidadeSegredoManager.class).visivel(doc, loc.getUsuario(), Authenticator.getReferenciaProcuradoriaAtualUsuarioLogado())){
			return true;
		}else{
			logger.error("Tentativa de acesso indevido ao documento [{0}]: {1}", doc.getIdProcessoDocumento(), loc.getUsuario().getLogin());
			return false;
		}
	}

	/**
	 * Recupera os modelos de documentos disponíveis para o usuário.
	 * Esses modelos serão aqueles definidos no fluxo, se a chamada for feita em um contexto tal, ou
	 * todos os modelos do sistema, caso contrário.
	 * 
	 * @return a lista de modelos disponíveis.
	 * @throws Exception caso haja algum erro ao acessar a lista.
	 */
	public List<ModeloDocumento> getModelosDisponiveis() throws PJeBusinessException {
		List<Localizacao> localizacoes = obterLocalizacoesUsuario();
		return getModeloDocumentoManager().obterModelosPorLocalizacao(localizacoes);
	}

	public List<ModeloDocumento> getModelosDisponiveisPorTipoDocumento(TipoProcessoDocumento tipoProcessoDocumento) throws PJeBusinessException{
		return getModelosLocais(tipoProcessoDocumento);
	}
	
	public List<ModeloDocumento> getModelosLocais(TipoProcessoDocumento tipo) throws PJeBusinessException{
		List<Localizacao> locais = obterLocalizacoesUsuario();
		return getModeloDocumentoManager().getModelos(tipo, locais);		
	}

	public List<ModeloDocumento> getModelosLocais(List<TipoProcessoDocumento> tipos) throws PJeBusinessException{
		UsuarioLocalizacaoMagistradoServidor ulms = getUsuarioService().getLocalizacaoAtual().getUsuarioLocalizacaoMagistradoServidor();
		Localizacao local =  null;
		if(ulms != null && ulms.getOrgaoJulgador() != null){
			local = ulms.getOrgaoJulgador().getLocalizacao();
		}else if(ulms != null && ulms.getOrgaoJulgadorColegiado() != null){
			local = ulms.getOrgaoJulgadorColegiado().getLocalizacao();
		}else{
			local =  getUsuarioService().getLocalizacaoAtual().getLocalizacaoFisica();
		}
		List<Localizacao> locais = getLocalizacaoService().getLocalizacoesAscendentesDescendentes(local);
		return getModeloDocumentoManager().getModelos(tipos, locais);		
	}
	
	public List<ModeloDocumento> getModelosDisponiveisPorTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento){
		return getModeloDocumentoLocalManager().getModeloDocumentoPorTipo(tipoProcessoDocumento);
	}
	
	public TipoProcessoDocumento getTipoInicial() {
		return ParametroUtil.instance().getTipoProcessoDocumentoPeticaoInicial();
	}

	public List<TipoProcessoDocumento> getTiposDisponiveis() throws Exception{
		return getTipoProcessoDocumentoManager().findDisponiveisInternatemente(null);
	}
	
	public List<TipoProcessoDocumento> getTiposDisponiveis(Papel papel) throws Exception{
		return getTipoProcessoDocumentoManager().findDisponiveisInternatemente(papel);
	}

	public List<TipoProcessoDocumento> getTiposDisponiveis(Integer... ids) throws Exception{
		return getTipoProcessoDocumentoManager().findDisponiveis(ids);
	}

	public List<TipoProcessoDocumento> getTiposDisponiveis(Papel papel, Integer... ids) throws Exception{
		return getTipoProcessoDocumentoManager().findDisponiveis(papel, ids);
	}
	
	public List<TipoProcessoDocumento> getTiposDocumentosMinuta(TaskInstance ti, Papel papel){
		TramitacaoProcessualService tramitacaoProcessualService = ComponentUtil.getComponent(TramitacaoProcessualImpl.class);
		List<Integer> idsList = new ArrayList<Integer>();
		String valor = (String) tramitacaoProcessualService.recuperaVariavelTarefa(ti, Variaveis.VARIAVEL_IDS_TIPOS_DOCUMENTOS_FLUXO);
		if(valor != null && !valor.isEmpty()){
			idsList = CollectionUtilsPje.convertStringToIntegerList(valor);
		}		
		return getTipoProcessoDocumentoManager().findDisponiveis(papel, idsList.toArray(new Integer[]{}));
	}
	
	public List<TipoProcessoDocumento> getTiposDocumentoMinuta() {
		Long taskId = BusinessProcess.instance().getTaskId();
		if(taskId == null) {
			taskId = TaskInstanceHome.instance().getTaskId();
		}
		org.jbpm.taskmgmt.exe.TaskInstance ti = (org.jbpm.taskmgmt.exe.TaskInstance) ManagedJbpmContext.instance()
				.getSession().get(org.jbpm.taskmgmt.exe.TaskInstance.class, taskId);
		Papel papel = Authenticator.getPapelAtual();
		return this.getTiposDocumentosMinuta(ti, papel);
	}

	public void substituirModelo(ProcessoDocumento pd, ModeloDocumento modelo){
		substituirModelo(pd.getProcessoDocumentoBin(), modelo);
	}
	
	public void substituirModelo(ProcessoDocumentoBin pdBin, ModeloDocumento modelo){
		String conteudo = getModeloDocumentoManager().obtemConteudo(modelo);
		pdBin.setModeloDocumento(conteudo);
	}
	
	public void substituirModeloODT(ProcessoDocumentoBin pdBin, ModeloDocumento modelo) throws IOException{
		String conteudoBase64 = getModeloDocumentoManager().obtemConteudoODT(modelo);
		pdBin.setModeloDocumento(conteudoBase64);
	}

	public byte[] getData(ProcessoDocumento pd) throws PJeDAOException, PJeBusinessException{
		return getProcessoDocumentoBinManager().getBinaryData(pd.getProcessoDocumentoBin());
	}
	
	public ProcessoDocumento persist(ProcessoDocumento pd, boolean updateBin) throws PJeBusinessException{
		Usuario u = getUsuarioService().getUsuarioLogado();
		if ( u==null ) u = getUsuarioService().getUsuarioSistema();
		Pessoa p = getPessoaService().findById(u.getIdUsuario());
		return persist(pd, updateBin, p);
	}
	
	public ProcessoDocumento persist(ProcessoDocumento pd, boolean updateBin, Pessoa pessoa) throws PJeBusinessException {
		ProcessoTrf processoTrf = pd.getProcessoTrf();
		Localizacao localizacao = Authenticator.getLocalizacaoAtual();
		Papel papel = Authenticator.getPapelAtual();
		return this.persist(pd, processoTrf, updateBin, pessoa, localizacao, papel, true);
	}
	
	public ProcessoDocumento persist(ProcessoDocumento pd, boolean updateBin, boolean forceConteudoNulo) throws PJeBusinessException {
		ProcessoTrf processoTrf = pd.getProcessoTrf();
		Localizacao localizacao = Authenticator.getLocalizacaoAtual();
		Papel papel = Authenticator.getPapelAtual();
		Usuario u = getUsuarioService().getUsuarioLogado();
		Pessoa pessoa = getPessoaService().findById(u.getIdUsuario());
		return persist(pd, processoTrf, updateBin, pessoa, localizacao, papel, forceConteudoNulo);
	}

	public ProcessoDocumento persist(ProcessoDocumento pd, ProcessoTrf processoTrf, boolean updateBin, Pessoa pessoa, Localizacao localizacao, Papel papel, boolean forceConteudoNulo) throws PJeBusinessException{
		pd.setProcessoTrf(processoTrf);
		pd.setProcesso(processoTrf.getProcesso());
		if(pd.getIdProcessoDocumento() == 0){
			processoTrf.getProcesso().getProcessoDocumentoList().add(pd);
		}

		if (Authenticator.isUsuarioExterno(papel) && pd.getDocumentoSigiloso() && !pd.getProcessoDocumentoBin().getSignatarios().isEmpty()) {
			/*
			 *Marca o processo para ser aprecido pelo juiz por ter sido incluído
			 *documento com pedido de sigilo 
			 */
			processoTrf.setApreciadoSigilo(ProcessoTrfApreciadoEnum.A);
		}
		if(pd.getProcessoDocumento() == null && pd.getTipoProcessoDocumento() != null) {
			pd.setProcessoDocumento(pd.getTipoProcessoDocumento().getTipoProcessoDocumento());
		}
		Date data = new Date();
		processaBinario(pd, data, updateBin, pessoa);
		if (pd.getDataInclusao() == null){
			pd.setDataInclusao(data);			
		} else {
			pd.setDataAlteracao(data);
		}
		
		if(pd.getInstancia() == null){
			pd.setInstancia(ParametroUtil.instance().getInstancia());
		}
		
		Usuario usuario = ComponentUtil.getComponent(UsuarioManager.class).findById(pessoa.getIdPessoa());
		if (pd.getUsuarioInclusao() == null){
			pd.setUsuarioInclusao(pessoa);
			pd.setNomeUsuarioInclusao(UsuarioHome.instance().getNomeUsuarioCompleto(usuario, localizacao, papel));
		} else {
			pd.setUsuarioAlteracao(pessoa);
			pd.setNomeUsuarioAlteracao(UsuarioHome.instance().getNomeUsuarioCompleto(usuario, localizacao, papel));
		}
		
		if (pd.getLocalizacao() == null){
 			pd.setLocalizacao(localizacao);
		}
		
		if (!pd.getProcessoDocumentoBin().getProcessoDocumentoList().contains(pd)) {
			pd.getProcessoDocumentoBin().getProcessoDocumentoList().add(pd);
		}

		boolean isConteudoVazio = (pd.getProcessoDocumentoBin().getModeloDocumento()!=null) 
				? pd.getProcessoDocumentoBin().getModeloDocumento().trim().isEmpty()
				: pd.getProcessoDocumentoBin().getNumeroDocumentoStorage()==null;
		// Impede que seja persistido um processo sem conteúdo caso não tenha sido explicitamente solicitado
		if (!isConteudoVazio || forceConteudoNulo) {
			getProcessoDocumentoBinManager().persist(pd.getProcessoDocumentoBin());
			pd.setProcessoDocumentoBin(pd.getProcessoDocumentoBin());
		}
		return pd;
	}

	public ProcessoDocumento persist(ProcessoDocumento pd, ProcessoTrf processoTrf, boolean updateBin) throws PJeBusinessException{
		Usuario usuarioLogado = getUsuarioService().getUsuarioLogado();
		Pessoa p = getPessoaService().findById(usuarioLogado.getIdUsuario());
		return persist(pd, processoTrf, updateBin, p);
	}

	public ProcessoDocumento persist(ProcessoDocumento pd, ProcessoTrf processoTrf, boolean updateBin, Pessoa p) throws PJeBusinessException{
		if (pd.getProcessoDocumento() == null && pd.getTipoProcessoDocumento() != null){
			pd.setProcessoDocumento(pd.getTipoProcessoDocumento().getTipoProcessoDocumento());
		}
		pd.setProcessoTrf(processoTrf);
		pd.setProcesso(processoTrf.getProcesso());
		return persist(pd, updateBin, p);
	}
	
	public void flush() throws PJeBusinessException{
		try {
			getProcessoDocumentoManager().flush();
			getProcessoDocumentoBinManager().flush();
		} catch (PJeBusinessException e) {
			logger.error("Erro ao tornar definitivas as alterações: {0}", e.getLocalizedMessage());
			throw e;
		}
	}
	
	public boolean hasMinutaEmAberto(Long taskId, ProcessoTrf processo) {
		boolean hasMinutaEmAberto = false;
		
		Integer idMinutaEmElaboracao = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(ManagedJbpmContext.instance().getTaskInstance(taskId));
		if(idMinutaEmElaboracao != null) {
			try {
				ProcessoDocumento minutaEmElaboracao = this.getDocumento(idMinutaEmElaboracao);
				if(minutaEmElaboracao != null && minutaEmElaboracao.getProcessoDocumentoBin() != null) {
					hasMinutaEmAberto = !ComponentUtil.getComponent(ProcessoDocumentoBinManager.class).existemSignatarios(minutaEmElaboracao.getProcessoDocumentoBin().getIdProcessoDocumentoBin());
				}
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}			
		}
		return hasMinutaEmAberto;
	}
	
	/**
	 * Gera uma nova minuta (ou atualiza uma informada) com base nos dados informados, se receber a task, vinculará o documento à task e gerará a variável
	 * minutaEmElaboracao com o ID do documento gerado
	 * - Não atualiza conteúdo de documentos já assinados
	 * - Não atualiza conteúdo de documentos de outros processos
	 * - Se houver agrupador de movimentos relacionado e houver um fluxo relacionado, gravará a variável de fluxo de agrupador de movimentos correspondente - tornando o lançamento de movimentos obrigatório
	 * -- a selecao de qual o movimento temporário relacionado deve ser feita em uma EL posterior
	 * - Se tiver que criar um novo documento, este será criado com o usuário do sistema
	 * 
	 * Retorna o idProcessoDocumento utilizado ou criado
	 * 
	 * @param idProcessoTrf
	 * @param idProcessoDocumento
	 * @param jbpmTask
	 * @param idTipoProcessoDocumento
	 * @param idModeloDocumento
	 * @return idProcessoDocumento
	 */
	public Integer gerarMinuta(Integer idProcessoTrf, Integer idProcessoDocumento, Long jbpmTask, 
			Integer idTipoProcessoDocumento, Integer idModeloDocumento) {
		
		ExpressionsUtil.processoTrfHomeExpressions(idProcessoTrf);
		
		if(jbpmTask != null && jbpmTask == 0) {
			jbpmTask = null;
		}
		
		if(idProcessoDocumento != null && idProcessoDocumento == 0) {
			idProcessoDocumento = null;
		}

		try {
			Usuario usuarioSistema = getUsuarioService().getUsuarioSistema();
			Localizacao localizacaoSistema = ParametroUtil.instance().getLocalizacaoTribunal();
			PapelService papelService = ComponentUtil.getComponent(PapelService.class);
			Papel papelSistema = papelService.findByCodeName(Papeis.SISTEMA);
 
			ProcessoDocumento processoDocumento = this.obterProcessoDocumento(
					idProcessoTrf, idProcessoDocumento, jbpmTask, idTipoProcessoDocumento, 
					usuarioSistema.getIdUsuario(), localizacaoSistema.getIdLocalizacao(), 
					papelSistema.getIdPapel(), idModeloDocumento);
			
			if (processoDocumento != null) {
				idProcessoDocumento = processoDocumento.getIdProcessoDocumento();
				if(jbpmTask == null && processoDocumento.getIdJbpmTask() != null) {
					jbpmTask = processoDocumento.getIdJbpmTask();
				}
				if(jbpmTask != null) {
					org.jbpm.graph.exe.ProcessInstance processInstance = ManagedJbpmContext.instance().getTaskInstance(jbpmTask).getProcessInstance();
					if(processInstance != null) {
						ComponentUtil.getComponent(TramitacaoProcessualImpl.class).gravaVariavel(
								ManagedJbpmContext.instance().getTaskInstance(jbpmTask).getProcessInstance(), 
								Variaveis.MINUTA_EM_ELABORACAO, idProcessoDocumento);
						
						if (processoDocumento.getTipoProcessoDocumento().getAgrupamento() != null) {
							LancadorMovimentosService.instance().setAgrupamentoDeMovimentosTemporarios(processInstance, processoDocumento.getTipoProcessoDocumento().getAgrupamento().getIdAgrupamento());
						}						
					}
				}						
			}
		} catch (Exception e) {
			// verifica se o id do documento informado é da minutaEmElaboracao e se ela esta juntada, se estiver apaga a variavel
			if(jbpmTask != null) {
				Integer idMinutaEmElaboracao = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(ManagedJbpmContext.instance().getTaskInstance(jbpmTask));
				if(idProcessoDocumento != null && idMinutaEmElaboracao == idProcessoDocumento) {
					try {
						ProcessoDocumento documento = this.getProcessoDocumentoManager().findById(idProcessoDocumento);
						if(documento.isJuntado()) {
							JbpmUtil.instance().apagaMinutaEmElaboracao(ManagedJbpmContext.instance().getTaskInstance(jbpmTask));
						}
					} catch (PJeBusinessException e1) {
						e1.printStackTrace();
					}
				}
			}
			
			// não retornar o id do documento minutado, já que houve erro
			idProcessoDocumento = null;
		}
		return idProcessoDocumento;
	}
	
	public Integer gerarMinuta(Integer idProcessoTrf, Integer idProcessoDocumento, Long jbpmTask, 
			Integer idTipoProcessoDocumento, Integer idModeloDocumento, Usuario usuarioSistema) throws Exception { 	
	
		PapelService papelService = ComponentUtil.getComponent(PapelService.class);
		Papel papelSistema = papelService.findByCodeName(Papeis.SISTEMA);
		Localizacao localizacaoSistema = ParametroUtil.instance().getLocalizacaoTribunal();

		ProcessoDocumento processoDocumento = this.obterProcessoDocumento(
				idProcessoTrf, idProcessoDocumento, jbpmTask, idTipoProcessoDocumento, 
				usuarioSistema.getIdUsuario(), localizacaoSistema.getIdLocalizacao(), papelSistema.getIdPapel(), idModeloDocumento);	

		if (processoDocumento != null) {
			idProcessoDocumento = processoDocumento.getIdProcessoDocumento();
			if(jbpmTask == null && processoDocumento.getIdJbpmTask() != null) {
				jbpmTask = processoDocumento.getIdJbpmTask();
			}
			org.jbpm.graph.exe.ProcessInstance processInstance = null;
			if(jbpmTask != null) {
				processInstance = ManagedJbpmContext.instance().getTaskInstance(jbpmTask).getProcessInstance();
			}
			if (processInstance == null) {
				processInstance = org.jboss.seam.bpm.ProcessInstance.instance();
			}
			if(processInstance != null) {
				ComponentUtil.getComponent(TramitacaoProcessualImpl.class).gravaVariavel(
						processInstance, 
						Variaveis.MINUTA_EM_ELABORACAO, idProcessoDocumento);
			
				
				if (processoDocumento.getTipoProcessoDocumento().getAgrupamento() != null) {
					LancadorMovimentosService.instance().setAgrupamentoDeMovimentosTemporarios(processInstance, processoDocumento.getTipoProcessoDocumento().getAgrupamento().getIdAgrupamento());
				}						
			}					
		}
		return idProcessoDocumento;		
	}
	
	private Integer tratarErrosGeracaoMinuta(Long jbpmTask, Integer idProcessoDocumento) {			
		// verifica se o id do documento informado é da minutaEmElaboracao e se ela esta juntada, se estiver apaga a variavel
		if(jbpmTask != null) {
			Integer idMinutaEmElaboracao = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(ManagedJbpmContext.instance().getTaskInstance(jbpmTask));
			if(idProcessoDocumento != null && idMinutaEmElaboracao.equals(idProcessoDocumento)) {
				try {
					ProcessoDocumento documento = this.getProcessoDocumentoManager().findById(idProcessoDocumento);
					if(documento.isJuntado()) {
						JbpmUtil.instance().apagaMinutaEmElaboracao(ManagedJbpmContext.instance().getTaskInstance(jbpmTask));
					}
				} catch (PJeBusinessException e1) {
					e1.printStackTrace();
				}
			}
		}		
		// não retornar o id do documento minutado, já que houve erro
		return null;
	}
	
	/**
	 * 
	 * 
	 * @param idProcessoTrf
	 * @param idProcessoDocumento
	 * @param jbpmTask
	 * @param idTipoProcessoDocumento
	 * @param idModeloDocumento
	 * @return ProcessoDocumento
	 * @throws Exception
	 */
	public ProcessoDocumento obterProcessoDocumento(Integer idProcessoTrf, Integer idProcessoDocumento, Long jbpmTask, 
			Integer idTipoProcessoDocumento, Integer idUsuarioCriacao, Integer idLocalizacaoCriacao, Integer idPapelCriacao, 
			Integer idModeloDocumento) throws Exception {
		
		ProcessoDocumento documento = null;
		
		if (idProcessoTrf != null && idTipoProcessoDocumento != null) {
			if (idProcessoDocumento != null && idProcessoDocumento > 0) {
				documento = this.getProcessoDocumentoManager().findById(idProcessoDocumento);

				if(documento != null) {
					if(this.getProcessoDocumentoManager().isDocumentoAssinado(documento.getProcessoDocumentoBin())) {
						throw new IllegalArgumentException("Documento já assinado, não pode ser alterado.");
					}
					if(documento.getProcessoTrf().getIdProcessoTrf() != idProcessoTrf) {
						throw new IllegalArgumentException("Este documento é de outro processo, por isso não pode ser alterado.");
					}
					if(!documento.getAtivo()) {
						documento = null;
					}
				}
			}
			
			ProcessoTrf processoTrf = ProcessoTrfManager.instance().find(ProcessoTrf.class, idProcessoTrf);
			TipoProcessoDocumento tipoProcessoDocumento = this.getTipoProcessoDocumentoManager().findById(idTipoProcessoDocumento);
			String conteudoDocumento = idModeloDocumento != null ? 
					processaConteudo(this.getModeloDocumentoManager().findById(idModeloDocumento)) : null;
			
			Pessoa pessoaCriacao = getPessoaService().findById(idUsuarioCriacao);
			Localizacao localizacaoCriacao = getLocalizacaoService().findById(idLocalizacaoCriacao);
			PapelService papelService = ComponentUtil.getComponent(PapelService.class);
			Papel papelCriacao = papelService.findById(idPapelCriacao);

			if(documento == null) {
				documento = this.getNovoDocumento(conteudoDocumento, jbpmTask);
			}
			
			if (documento != null) {
				documento = this.atualizaDadosDocumentoGerado(documento, tipoProcessoDocumento, processoTrf, pessoaCriacao, localizacaoCriacao, papelCriacao, jbpmTask);
			}	
		}
		
		return documento;
	}
	
	private ProcessoDocumento atualizaDadosDocumentoGerado(ProcessoDocumento documento,
			TipoProcessoDocumento tipoProcessoDocumento, 
			ProcessoTrf processoTrf,
			Pessoa pessoaCriacao,
			Localizacao localizacaoPessoaCriacao,
			Papel papelPessoaCriacao,
			Long jbpmTask) throws Exception {
		
		documento.setTipoProcessoDocumento(tipoProcessoDocumento);
		documento.setProcessoDocumento(tipoProcessoDocumento.getTipoProcessoDocumento());
		
		processaBinario(documento, new Date(), true, pessoaCriacao);
		finalizaDocumento(documento, processoTrf, jbpmTask, false, false, false, pessoaCriacao, localizacaoPessoaCriacao, papelPessoaCriacao, false);
		Integer idDocumento = null;
		if(documento != null && documento.getIdProcessoDocumento() > 0) {
			idDocumento = documento.getIdProcessoDocumento();
		}
		
		ProcessoDocumento ultimoProcessoDocumento = getProcessoDocumentoManager().getUltimoProcessoDocumentoNaoAssinado(processoTrf.getProcesso());
		if (ultimoProcessoDocumento != null && idDocumento != ultimoProcessoDocumento.getIdProcessoDocumento() 
				&& !getProcessoDocumentoManager().isDocumentoAssinado(ultimoProcessoDocumento.getProcessoDocumentoBin()) && jbpmTask != null) {
			TaskInstance novaInstance = ManagedJbpmContext.instance().getTaskInstance(jbpmTask);
			TaskInstance ultimaInstance = ManagedJbpmContext.instance().getTaskInstance(ultimoProcessoDocumento.getIdJbpmTask());
			Usuario usuarioCriacao = ComponentUtil.getComponent(UsuarioManager.class).findById(pessoaCriacao.getIdPessoa());
			if (novaInstance.getName().equals(ultimaInstance.getName())) {
				getProcessoDocumentoManager().excluirDocumento(ultimoProcessoDocumento, usuarioCriacao, String.format(
						"(%tD) Inserida nova minuta em lote. (%s)", documento.getDataInclusao(), documento.getIdProcessoDocumento()));
			}
		}
		getProcessoDocumentoManager().persistAndFlush(documento);
		
		return documento;
	}
	
	public ProcessoDocumento criarProcessoDocumento(String modeloDocumento, TipoProcessoDocumento tipoProcessoDocumento, 
			ProcessoTrf processoTrf, Long jbpmTask) throws Exception {
		
		ProcessoDocumento documento = this.getNovoDocumento(modeloDocumento, jbpmTask);

		return this.atualizaProcessoDocumento(documento, processoTrf, modeloDocumento, tipoProcessoDocumento, jbpmTask);
	}

	public ProcessoDocumento atualizaProcessoDocumento(ProcessoDocumento documento, ProcessoTrf processoTrf, String modeloDocumento, TipoProcessoDocumento tipoProcessoDocumento, Long jbpmTask) throws Exception {
		Pessoa pessoaLogada = Authenticator.getPessoaLogada();
		Localizacao localizacaoPessoaCriacao = Authenticator.getLocalizacaoAtual();
		Papel papelPessoaCriacao = Authenticator.getPapelAtual();
		
		documento.getProcessoDocumentoBin().setModeloDocumento(modeloDocumento);
		
		return this.atualizaDadosDocumentoGerado(documento, tipoProcessoDocumento, processoTrf, pessoaLogada, localizacaoPessoaCriacao, papelPessoaCriacao, jbpmTask);
	}

	public ProcessoDocumento criaDocumentoPrincipalAssinado(ProcessoTrf processoTrf, String conteudo) throws PJeBusinessException {
		ProcessoDocumento processoDocumento = this.getNovoDocumento(conteudo);
		processoDocumento.setTipoProcessoDocumento(processoTrf.getClasseJudicial().getTipoProcessoDocumentoInicial());
			
		this.persist(processoDocumento, processoTrf, true);
		this.assinaSistema(processoDocumento.getProcessoDocumentoBin());
		
		return processoDocumento;
	}
	
	public ProcessoDocumento criaDocumentoAnexoAssinado(ProcessoDocumento documentoPrincipal, String nome, 
			TipoProcessoDocumento tipo, Integer tamanho, String arquivoBase64, String extensao) throws PJeBusinessException {
		
		ProcessoDocumento processoDocumento = this.getDocumento();
		processoDocumento.setDocumentoPrincipal(documentoPrincipal);
		processoDocumento.setProcessoDocumento(nome);
		processoDocumento.setTipoProcessoDocumento(tipo);
		
		processoDocumento.getProcessoDocumentoBin().setNomeArquivo(nome);
		processoDocumento.getProcessoDocumentoBin().setSize(tamanho);
		processoDocumento.getProcessoDocumentoBin().setFile(FileUtil.createTempFile(Base64.getDecoder().decode(arquivoBase64)));
		processoDocumento.getProcessoDocumentoBin().setMd5Documento(Crypto.encodeMD5(Base64.getDecoder().decode(arquivoBase64)));
		processoDocumento.getProcessoDocumentoBin().setExtensao(extensao);
		processoDocumento.getProcessoDocumentoBin().setBinario(Boolean.TRUE);
		
		this.persist(processoDocumento, documentoPrincipal.getProcessoTrf(), true);
		this.assinaSistema(processoDocumento.getProcessoDocumentoBin());
		
		return processoDocumento;
	}
	
	/**
	 * Configura os dados referentes ao usuário da juntada no objeto processoDocumento.
	 * 
	 * @param processoDocumento
	 * @param usuario
	 * @param papelUsuario
	 * @param procuradoriaUsuario
	 * @throws PJeBusinessException
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-22393
	 */
	public void configurarNomeUsuarioJuntada(ProcessoDocumento processoDocumento, Usuario usuario, Papel papelUsuario, Localizacao localizacaoUsuario) throws PJeBusinessException {
		processoDocumento.setUsuarioJuntada(usuario);
		processoDocumento.setNomeUsuarioJuntada(usuario.getNome().toUpperCase());
		
		TipoOrigemAcaoEnum origemUsuarioJuntada = getOrigemUsuario(processoDocumento.getProcessoTrf(), usuario, papelUsuario, localizacaoUsuario);
		processoDocumento.setLocalizacaoJuntada(getDescricaoLocalizacaoJuntada(origemUsuarioJuntada, papelUsuario, localizacaoUsuario));
		processoDocumento.setInTipoOrigemJuntada(origemUsuarioJuntada);
	}

	/**
	 * Este método retorna a descrição da localçização do usuário logado.<br/>
	 * 
	 * A descrição será preenchida assim:<br/><br/>
	 *   - Para usuários externos: [POLO NO PROCESSO (se estiver em algum)] - [PROCURADORIA/DEFENSORIA (se estiver em algum)] - [PAPEL]<br/>
	 *   - Para usuários internos: [PAPEL]<br/>
	 * 
	 * @param origemUsuarioJuntada
	 * @param papelUsuario
	 * @param procuradoriaAtual
	 * @return
	 * @throws PJeBusinessException
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-22393
	 */
	private String getDescricaoLocalizacaoJuntada(TipoOrigemAcaoEnum origemUsuarioJuntada, Papel papelUsuario, Localizacao localizacaoUsuario) throws PJeBusinessException {
		StringBuilder descricao = new StringBuilder();
		String nomePapel = papelUsuario.getNome();

		// Se não for usuário externo, retorna apenas o nome do papel.
		if(origemUsuarioJuntada != null) {
			if(TipoOrigemAcaoEnum.I == origemUsuarioJuntada) {
				return nomePapel;
			}else if(TipoOrigemAcaoEnum.E != origemUsuarioJuntada) {
				// Polo
				String getDescricaoPoloUsuarioLogado = StringUtils.EMPTY;
				
				if(origemUsuarioJuntada != null) {
					if(TipoOrigemAcaoEnum.PA == origemUsuarioJuntada) {
						getDescricaoPoloUsuarioLogado = DESCRICAO_POLO_JUNTADA.ATIVO;
					}else if(TipoOrigemAcaoEnum.PP == origemUsuarioJuntada) {
						getDescricaoPoloUsuarioLogado = DESCRICAO_POLO_JUNTADA.PASSIVO;
					}else {
						getDescricaoPoloUsuarioLogado = DESCRICAO_POLO_JUNTADA.OUTROS_INTERESSADOS;
					}
				}
				if(!getDescricaoPoloUsuarioLogado.isEmpty()){
					StringUtil.adicionarHifen(descricao);
					descricao.append(getDescricaoPoloUsuarioLogado);
				}
			}
			
			// Procuradoria/Defensoria
			Procuradoria procuradoriaUsuario = null;
			if(localizacaoUsuario != null) {
				procuradoriaUsuario = ComponentUtil.getComponent(ProcuradoriaManager.class).recuperaPorLocalizacao(localizacaoUsuario);
				if (procuradoriaUsuario != null) {
					StringUtil.adicionarHifen(descricao);
					descricao.append(procuradoriaUsuario.getNome().toUpperCase());
				}	
			}
			// Papel
			if(!nomePapel.isEmpty()){
				StringUtil.adicionarHifen(descricao);
				descricao.append(nomePapel);	
			}
		}
		return descricao.toString();
	}
	
	/**
	 * Retorna a origem deste usuário PA / PP / OU / I / E
	 * 
	 * @param processoTrf
	 * @param usuario
	 * @param papelUsuario
	 * @return descricaoPoloUsuarioLogado
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-22393
	 */
	private TipoOrigemAcaoEnum getOrigemUsuario(ProcessoTrf processoTrf, Usuario usuario, Papel papelUsuario, Localizacao localizacaoUsuario) {
		TipoOrigemAcaoEnum retorno = null;
		if(!Authenticator.isUsuarioExterno(papelUsuario)){
			retorno = TipoOrigemAcaoEnum.I;
		}else {
			retorno = this.identificaTipoOrigem(processoTrf, usuario, papelUsuario, localizacaoUsuario);
		}
		
		if(retorno == null) {
			retorno = TipoOrigemAcaoEnum.E;
		}
		return retorno;
	}
	
	

	public ProcessoDocumento finalizaDocumento(
				ProcessoDocumento pd, 
				ProcessoTrf processo, 
				Long idTaskInstance, 
				boolean substituirNomeDocumento, 
				boolean updateBin, 
				boolean marcarComoNaoLido, 
				Pessoa p,
				Localizacao localizacaoUsuario,
				Papel papelUsuario,
				boolean juntar) throws PJeBusinessException{
		if (processo == null){
			throw new IllegalArgumentException("Processo não pode ser nulo.");
		}
		pd.setAtivo(true);
		
		if(pd.getInstancia() == null){
			pd.setInstancia(ParametroUtil.instance().getInstancia());
		}
		if (pd.getDocumentoSigiloso() == null){
			if (processo.getSegredoJustica()){
				pd.setDocumentoSigiloso(true);
			} else {
				pd.setDocumentoSigiloso(false);
			}
		}
		if (idTaskInstance != null && idTaskInstance > 0){
			pd.setIdJbpmTask(idTaskInstance);
			pd.setExclusivoAtividadeEspecifica(Boolean.TRUE);
		}
		
		pd.setUsuarioAlteracao(p);
		pd.setNomeUsuarioAlteracao(p.getNome());
		pd.setDataAlteracao(new Date());

		pd.setLocalizacao(localizacaoUsuario);
		pd.setNomeLocalizacao(localizacaoUsuario.getLocalizacao());
		
		pd.setPapel(papelUsuario);
		pd.setNomePapel(papelUsuario.getNome());

		if (pd.getProcessoDocumento() == null || substituirNomeDocumento){
			pd.setProcessoDocumento(pd.getTipoProcessoDocumento().getTipoProcessoDocumento());
		}
		
		pd = this.persist(pd, processo, updateBin, p, localizacaoUsuario, papelUsuario, true);
		if (updateBin){
			getProcessoDocumentoBinManager().finalizaProcessoDocumentoBin(pd.getProcessoDocumentoBin(), pd.getTipoProcessoDocumento(), p);
		}

		boolean assinado = false;
		if(pd.getProcessoDocumentoBin() != null){
			List<ProcessoDocumentoBinPessoaAssinatura> sigs = pd.getProcessoDocumentoBin().getSignatarios();
			assinado = sigs != null && ! sigs.isEmpty();
		}
		
		if(juntar && processo.getProcessoStatus() == ProcessoStatusEnum.D && pd.getDataJuntada() == null && assinado){
			registrarJuntadaDocumento(pd, p, localizacaoUsuario, papelUsuario);
			getProcessoDocumentoDAO().merge(pd);
		}
		if (assinado && idTaskInstance != null){
		    MiniPacService miniPacService = ComponentUtil.getComponent(MiniPacService.class);
		    miniPacService.processarMiniPac(processo, pd, true);
		}
		
		if (!marcarComoNaoLido) {
			getProcessoDocumentoLidoManager().definirDocumentoComoLido(pd, p);
		}
		
		return pd;
	}
	
	public void registrarJuntadaDocumento(ProcessoDocumento pd) throws PJeBusinessException{
		registrarJuntadaDocumento(pd, getPessoaService().findById(getUsuarioService().getUsuarioLogado().getIdUsuario()), 
				Authenticator.getLocalizacaoAtual(), Authenticator.getPapelAtual());
		registrarJuntadaAnexos(pd);
	}
	
	private void registrarJuntadaAnexos(ProcessoDocumento documentoPrincipal) throws PJeBusinessException {
        for (ProcessoDocumento documentoAnexo : documentoPrincipal.getDocumentosVinculados()) {
            preencheDadosJuntadaAoAnexo(documentoPrincipal, documentoAnexo);
            registrarJuntadaDocumento(documentoAnexo, getPessoaService().findById(getUsuarioService().getUsuarioLogado().getIdUsuario()), 
                    Authenticator.getLocalizacaoAtual(), Authenticator.getPapelAtual());
        }
    }

    private void preencheDadosJuntadaAoAnexo(ProcessoDocumento documentoPrincipal, ProcessoDocumento documentoAnexo) {
        if (documentoPrincipal.getIdJbpmTask() != null && !documentoPrincipal.getIdJbpmTask().equals(0L)) {
            documentoAnexo.setIdJbpmTask(documentoPrincipal.getIdJbpmTask());
            documentoAnexo.setExclusivoAtividadeEspecifica(documentoPrincipal.getExclusivoAtividadeEspecifica());
        }
        documentoAnexo.setUsuarioAlteracao(documentoPrincipal.getUsuarioAlteracao());
        documentoAnexo.setNomeUsuarioAlteracao(documentoPrincipal.getNomeUsuarioAlteracao());
        documentoAnexo.setNomePapel(documentoPrincipal.getNomePapel());
        documentoAnexo.setNomeLocalizacao(documentoPrincipal.getNomeLocalizacao());
        documentoAnexo.setDataAlteracao(documentoPrincipal.getDataAlteracao());
    }
	
	private void registrarJuntadaDocumento(ProcessoDocumento pd, Pessoa p, Localizacao localizacaoUsuario, Papel papelUsuario) throws PJeBusinessException {
		pd.setDataJuntada(new Date());
		configurarNomeUsuarioJuntada(pd, p, papelUsuario, localizacaoUsuario);
	}
	
	public ProcessoDocumento finalizaDocumento(
			ProcessoDocumento pd, 
			ProcessoTrf processo, 
			Long idTaskInstance, 
			boolean substituirNomeDocumento, 
			boolean updateBin, 
			boolean marcarComoNaoLido, 
			Pessoa p, 
			boolean juntar) throws PJeBusinessException{
		
		Localizacao localizacaoUsuarioLogado = Authenticator.getLocalizacaoAtual();
		Papel papelUsuarioLogado = Authenticator.getPapelAtual();
		return this.finalizaDocumento(pd, processo, idTaskInstance, substituirNomeDocumento, updateBin, marcarComoNaoLido, p, localizacaoUsuarioLogado, papelUsuarioLogado, juntar);
	}
	
	public ProcessoDocumento finalizaDocumento(ProcessoDocumento pd, ProcessoTrf processo, Long jbpmTask, boolean substituirNomeDocumento, boolean updateBin, boolean marcarComoNaoLido, Pessoa p) throws PJeBusinessException{
		return finalizaDocumento(pd, processo, jbpmTask, substituirNomeDocumento, updateBin, marcarComoNaoLido, p, true);
	}
	
	public ProcessoDocumento finalizaDocumento(ProcessoDocumento pd, ProcessoTrf processo, Long jbpmTask, boolean substituirNomeDocumento, boolean updateBin, Pessoa p) throws PJeBusinessException{
		return finalizaDocumento(pd, processo, jbpmTask, substituirNomeDocumento, updateBin, true, p);
	}

	public ProcessoDocumento finalizaDocumento(ProcessoDocumento pd, ProcessoTrf processo, Long jbpmTask, boolean substituirNomeDocumento,
			boolean updateBin) throws PJeBusinessException{
		return finalizaDocumento(pd, processo, jbpmTask, substituirNomeDocumento, updateBin, getPessoaService().findById(getUsuarioService().getUsuarioLogado().getIdUsuario()));
	}

	public ProcessoDocumento finalizaDocumento(ProcessoDocumento pd, ProcessoTrf processo, Long jbpmTask,
			boolean substituirNomeDocumento) throws PJeBusinessException, PJeDAOException, CertificadoException{
		return finalizaDocumento(pd, processo, jbpmTask, substituirNomeDocumento, true);
	}
	
	/**
	 * [PJEI-4997] - Gravar documento e assina em modo teste apenas se o parametro passado for verdadeiro 
	 * 
	 */
	public ProcessoDocumento finalizaDocumentoAssinatura(ProcessoDocumento pd, ProcessoTrf processo, Long jbpmTask, boolean substituirNomeDocumento, boolean assinar) throws PJeBusinessException, PJeDAOException, CertificadoException{
		return finalizaDocumento(pd, processo, jbpmTask, substituirNomeDocumento, assinar);
	}

	public boolean exigeAssinatura(ProcessoDocumento pd){
		if (pd.getProcessoDocumentoBin().getValido()){
			return false;
		}
		List<TipoProcessoDocumentoPapel> papeis = pd.getTipoProcessoDocumento().getPapeis();
		if (papeis.size() == 0){
			return false;
		}
		else{
			for (TipoProcessoDocumentoPapel papel : papeis){
				if (papel.getExigibilidade().isSuficiente() || papel.getExigibilidade().isObrigatorio()){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean temAssinatura(ProcessoDocumento doc){
		return getProcessoDocumentoBinManager().temAssinatura(doc.getProcessoDocumentoBin());
	}

	public boolean possivelSignatario(ProcessoDocumento pd, Usuario u){
		List<TipoProcessoDocumentoPapel> papeis = pd.getTipoProcessoDocumento().getPapeis();
		if (papeis.size() == 0){
			return true;
		}
		else{
			Set<Papel> papeisUsuario = u.getPapelSet();
			for (TipoProcessoDocumentoPapel papel : papeis){
				if ((papel.getExigibilidade().isSuficiente() || papel.getExigibilidade().isObrigatorio())
					&& papeisUsuario.contains(papel.getPapel())){
					return true;
				}
			}
		}
		return false;
	}

	private void processaBinario(ProcessoDocumento pd, Date data, boolean update, Pessoa p){
		ProcessoDocumentoBin pdb = pd.getProcessoDocumentoBin();
		if (pd.getIdProcessoDocumento() > 0){
			pdb.getProcessoDocumentoList().add(pd);
		}
		if (update){
			pdb.setDataInclusao(data);
			pdb.setUsuario(p);
			updateMD5(pdb);

			if (StringUtil.isEmpty(pdb.getExtensao())) {
				if (!pdb.isBinario()) {
					pdb.setExtensao("text/html");
				}
			} else {
				if (pdb.isBinario() && pdb.getExtensao().contains("html")) {
					pdb.setBinario(false);
				}
			}
		}
	}

	public void refresh(ProcessoDocumento pd) throws PJeBusinessException{
		getProcessoDocumentoManager().refresh(pd);
		ProcessoDocumentoBin pdb = pd.getProcessoDocumentoBin();
		if (pdb != null){
			getProcessoDocumentoBinManager().refresh(pdb);
		}
	}

	public List<ProcessoDocumento> getDocumentos(ProcessoTrf processoTrf){
		List<ProcessoDocumento> ret = processoTrf.getProcesso().getProcessoDocumentoList();
		return ret;
	}

	public String processaConteudo(ModeloDocumento modelo){
		return getModeloDocumentoManager().obtemConteudo(modelo);
	}

	@Begin(join = true)
	public List<ProcessoDocumento> getDocumentos(ProcessoTrf processoJudicial, int first, int maximo){
		return getProcessoDocumentoManager().findByRange(processoJudicial, first, maximo);
	}

	@Begin(join = true)
	public List<ProcessoDocumento> getDocumentos(ProcessoTrf processoJudicial, int first, int maximo, boolean decrescente, boolean incluirPDF, 
			boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial){
		return this.getDocumentos(processoJudicial, first, maximo, decrescente, incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, false, false, false);
	}

	@Begin(join = true)
	public List<ProcessoDocumento> getDocumentos(ProcessoTrf processoJudicial, int first, int maximo, boolean decrescente, boolean incluirPDF, boolean incluirDocumentoPeticaoInicial, 
			boolean soDocumentosJuntados, boolean incluirDocCopiaExpediente){
		return this.getDocumentos(processoJudicial, first, maximo, decrescente, incluirPDF, false, incluirDocumentoPeticaoInicial, soDocumentosJuntados, incluirDocCopiaExpediente, false);
	}
	
	@Begin(join = true)
	public List<ProcessoDocumento> getDocumentos(ProcessoTrf processoJudicial, int first, int maximo, boolean decrescente, boolean incluirPDF, 
			boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial, boolean soDocumentosJuntados, boolean incluirDocCopiaExpediente, boolean apenasAtosProferidos){
		return this.getDocumentos(processoJudicial, first, maximo, decrescente, incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, soDocumentosJuntados, incluirDocCopiaExpediente, apenasAtosProferidos, null);
	}
	
	@Begin(join = true)
	public List<ProcessoDocumento> getDocumentos(ProcessoTrf processoJudicial, int first, int maximo, boolean decrescente, boolean incluirPDF, 
			boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial, boolean soDocumentosJuntados, boolean incluirDocCopiaExpediente, boolean apenasAtosProferidos, TipoOrigemAcaoEnum tipoOrigemAcao){
		return getProcessoDocumentoManager().findByRange(processoJudicial, first, maximo, false, decrescente, incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, soDocumentosJuntados, incluirDocCopiaExpediente, apenasAtosProferidos, tipoOrigemAcao);
	}

	
	public Integer getCountDocumentos(ProcessoTrf processoJudicial, boolean incluirPDF, boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial){
		return this.getCountDocumentos(processoJudicial, incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, false);
	}

	public Integer getCountDocumentos(ProcessoTrf processoJudicial, boolean incluirPDF, boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial, boolean soDocumentosJuntados){
		return this.getCountDocumentos(processoJudicial, incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, soDocumentosJuntados, false);
	}
	
	public Integer getCountDocumentos(ProcessoTrf processoJudicial, boolean incluirPDF, boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial, boolean soDocumentosJuntados, boolean apenasAtosProferidos){
		return this.getCountDocumentos(processoJudicial, incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, soDocumentosJuntados, apenasAtosProferidos, null);
	}

	public Integer getCountDocumentos(ProcessoTrf processoJudicial, boolean incluirPDF, boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial, boolean soDocumentosJuntados, boolean apenasAtosProferidos, TipoOrigemAcaoEnum tipoOrigemAcao){
		return getProcessoDocumentoManager().getCountDocumentos(processoJudicial, incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, soDocumentosJuntados, apenasAtosProferidos, tipoOrigemAcao);
	}
	
	
	public Integer contagemDocumentos(ProcessoTrf processoJudicial, boolean incluirBinarios, boolean incluirComAssinaturaInvalida, TipoProcessoDocumento...tipos) throws PJeBusinessException{
		return getProcessoDocumentoManager().contagemDocumentos(processoJudicial, incluirBinarios, incluirComAssinaturaInvalida, tipos);
	}
	
	public List<ProcessoDocumento> getPecasProcessuais(ProcessoTrf processoTrf, boolean assinado, boolean valido) throws PJeDAOException{
		List<ProcessoDocumento> docs = processoTrf.getProcesso().getProcessoDocumentoList(false);
		if (!assinado && !valido){
			return docs;
		}
		List<ProcessoDocumento> ret = new ArrayList<ProcessoDocumento>(docs.size());
		for (ProcessoDocumento documento : docs){
			ProcessoDocumentoBin actualDoc = documento.getProcessoDocumentoBin();
			if (valido){
				if (actualDoc.getValido()){
					ret.add(documento);
				}
			}
			else if (assinado){
				if (getProcessoDocumentoBinManager().obtemAssinaturas(actualDoc).size() > 0){
					ret.add(documento);
				}
			}
		}
		return ret;
	}

	/**
	 * Atualiza o MD5 do documento
	 * 
	 * @issue	PJEII-19266
	 * @param	pdb		objeto da entidade ProcessoDocumentoBin
	 */
	public void updateMD5(ProcessoDocumentoBin pdb){
		String md5 = null;

		if (pdb != null) {
			md5 = obterMD5(pdb);
		}
		
		pdb.setMd5Documento(md5);
	}

	/**
	 * Responsável por obter o código MD5 através do processoDocumentoBin. Na primeira tentantiva, usa o File do objeto
	 * passado por parâmetro. Se não, busca o binário num array de bytes, através do modeloDocumento do objeto e em
	 * seguida, gera o MD5.
	 * 
	 * @issue	PJEII-19266
	 * @param 	pdb		objeto da entidade processoDocumentoBin
	 * @return	retorna uma string com o código MD5 gerado.
	 */
	public String obterMD5(ProcessoDocumentoBin pdb) {
		byte[] binario = null;
		String md5 = null;
		
		if(pdb.getFile() != null){
			md5 = Crypto.encodeMD5(FileUtil.readFile(pdb.getFile()));
		} else if(pdb.isBinario()){
			binario = getBinarioProcessoDoc(pdb);
			md5 = Crypto.encodeMD5(binario);
		}
		
		if (md5 == null || md5.isEmpty() || "".equals(md5)) {
			md5 = gerarMD5(pdb.getModeloDocumento(), binario);
		}
		return md5;
	}

	/**
	 * Responsável por gerar o código MD5 do documento, baseado no modeloDocumento de um ProcessoDocumentoBin, ou de um
	 * array de bytes com o binário do ProcessoDocumentoBin
	 * 
	 * @issue	PJEII-19266
	 * @param 	modeloDocumento		String com o modeloDocumento do ProcessoDocumentoBin
	 * @param	data				Array de bytes com o binário do ProcessoDocumentoBin
	 * @return	retorna uma string com o código MD5 gerado
	 */
	private String gerarMD5(String modeloDocumento, byte[] data) {
		return (data == null ? Crypto.encodeMD5(modeloDocumento) : Crypto.encodeMD5(data));
	}

	/**
	 * Método responsável por obter o binário do ProcessoDocumentoBin convertido em array de bytes
	 * 
	 * @issue	PJEII-19266
	 * @param	pdb	objeto ProcessoDocumentoBin
	 * @return	retorna um array de bytes, com o binário do processoDocumentoBin passado no parâmetro
	 */
	private byte[] getBinarioProcessoDoc(ProcessoDocumentoBin pdb) {
		byte[] data = null;
		try {
			data = getProcessoDocumentoBinManager().getBinaryData(pdb);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return data;
	}

	public String[] processaAssinaturas(String assinaturas){
		String[] separado = assinaturas.split(SIG_SEPARATOR);
		return separado;
	}

	@Observer(PROCESSO_DOCUMENTO_MANAGER_CMAA)
	public void criarCertidaoMarcacaoAutomatica(Processo processo, boolean marcada) throws PJeBusinessException{
		ProcessoDocumento documento = getProcessoDocumentoManager().getDocumento();
		ProcessoDocumentoBin documentoBin = documento.getProcessoDocumentoBin();
		ModeloDocumento modelo = null;
		if (marcada){
			modelo = getModeloDocumentoManager().findById(ParametroUtil.getIdModeloDocumentoCMAAMarcada());
		}
		else{
			modelo = getModeloDocumentoManager().findById(ParametroUtil.getIdModeloDocumentoCMAANaoMarcada());
		}
		TipoProcessoDocumento tipo = getTipoProcessoDocumentoManager().findById(ParametroUtil
				.getIdTipoProcessoDocumentoCMAA());
		documentoBin.setDataInclusao(new Date());
		documentoBin.setModeloDocumento(getModeloDocumentoManager().obtemConteudo(modelo));
		documento.setAtivo(true);
		documento.setDataInclusao(new Date());
		documento.setDocumentoSigiloso(false);
		documento.setProcesso(processo);
		documento.setProcessoDocumento(modelo.getTituloModeloDocumento());
		documento.setTipoProcessoDocumento(tipo);
		documento.setProcessoDocumentoBin(documentoBin);
		persist(documento, true, (Pessoa) getUsuarioService().getUsuarioLogado());
	}

	public List<TipoProcessoDocumento> getTiposDocumentosAntigos(boolean inicial, boolean modelo){
		return getTipoProcessoDocumentoManager().findDisponiveisAntigos(inicial, modelo);
	}
	
	public List<ProcessoDocumento> getInicial(ProcessoTrf processoJudicial) throws PJeBusinessException{
		Integer idTipoInicial = processoJudicial.getClasseJudicial().getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento();
		return getProcessoDocumentoManager().getDocumentosPorTipo(processoJudicial, idTipoInicial);
	}

	public List<ProcessoDocumento> getDocumentosPorTipos(ProcessoTrf processoJudicial, Integer... tipos) throws PJeBusinessException{
		return getProcessoDocumentoManager().getDocumentosPorTipo(processoJudicial, tipos);
	}

	public void excluirDocumentosEmBrancoPorTipo(ProcessoTrf processoJudicial, Integer... tipos) throws PJeBusinessException{
		getProcessoDocumentoManager().excluirDocumentosEmBrancoPorTipo(processoJudicial, tipos);
	}

	public static DocumentoJudicialService instance(){
		return ComponentUtil.getComponent(NAME);
	}
	
	public ProcessoDocumento getUltimoAtoJudicial(Processo processoJudicial){
		
		return getUltimoProcessoDocumento(getTipoProcessoDocumentoManager().getTipoDocumentoAtoMagistradoList(), processoJudicial);
	}
	
	public ProcessoDocumento getUltimaComunicacao(Processo processoJudicial){
		TipoProcessoDocumento tipo = ParametroUtil.instance().getTipoProcessoDocumentoComunicacaoEntreInstancias();
		List<TipoProcessoDocumento> tipos = new ArrayList<TipoProcessoDocumento>();
		tipos.add(tipo);
		return getUltimoProcessoDocumento(tipos, processoJudicial);
	}

	public ProcessoDocumento getUltimoProcessoDocumento(List<TipoProcessoDocumento> tipos, Processo processo){
		
		return getProcessoDocumentoManager().getUltimoProcessoDocumento(tipos, processo);
	}

	public ModeloDocumento getModeloDocumento(int id) throws PJeBusinessException{
		return getModeloDocumentoManager().findById(id);
	}
	
	/**
	 * Faz a juntada do documento ao processo com assinatura do sistema e lança as movimentações temporárias relacionadas se for o caso
	 * Se receber a jbpmTask, verifica se há uma variável de minutaEmElaboracao com o mesmo ID do documento que será assinado, se houver, 
	 * ao final da juntada do documento apaga a variável minutaEmElaboraao e lança as variáveis relacionadas ao documento juntado
	 * 
	 * @param idProcessoDocumento
	 * @param jbpmTask
	 * @return ResultadoComplexoVO -- use .getMensagem() para saber a mensagem e .getResultado() para saber se true ou false
	 * @throws PJeBusinessException
	 */
	public ResultadoComplexoVO juntarDocumento(Integer idProcessoDocumento, Long jbpmTask) throws PJeBusinessException {		
		return juntarDocumento(idProcessoDocumento, jbpmTask, null);
	}
	
	/**
	 * Faz a juntada do documento ao processo com assinatura do sistema e lança as movimentações temporárias relacionadas se for o caso
	 * Se receber a jbpmTask, verifica se há uma variável de minutaEmElaboracao com o mesmo ID do documento que será assinado, se houver, 
	 * ao final da juntada do documento apaga a variável minutaEmElaboraao e lança as variáveis relacionadas ao documento juntado
	 * 
	 * @param idProcessoDocumento
	 * @param jbpmTask
	 * @param idProcessInstance
	 * @return ResultadoComplexoVO -- use .getMensagem() para saber a mensagem e .getResultado() para saber se true ou false
	 * @throws PJeBusinessException
	 */
	public ResultadoComplexoVO juntarDocumento(Integer idProcessoDocumento, Long jbpmTask, Long idProcessInstance) throws PJeBusinessException {
		ResultadoComplexoVO retorno = new ResultadoComplexoVO(Boolean.FALSE, "Documento não encontrado");
		if (idProcessoDocumento != null && idProcessoDocumento > 0) {
			ProcessoDocumento processoDocumento = getProcessoDocumentoManager().findById(idProcessoDocumento);

			if (processoDocumento != null) {
				if(jbpmTask != null && jbpmTask == 0) {
					jbpmTask = null;
				}

				if(jbpmTask == null && processoDocumento.getIdJbpmTask() != null) {
					jbpmTask = processoDocumento.getIdJbpmTask();
				}
				org.jbpm.graph.exe.ProcessInstance processInstance = null;
				if (idProcessInstance != null && idProcessInstance != 0) {
					processInstance = ManagedJbpmContext.instance().getProcessInstance(idProcessInstance);
				} else if (jbpmTask != null) {
					processInstance = ManagedJbpmContext.instance().getTaskInstance(jbpmTask).getProcessInstance();
				}
				List<EventoBean> movimentosSelecionados = null;
				if(processInstance != null) {
					movimentosSelecionados = LancadorMovimentosService.instance().getMovimentosTemporarios(processInstance);
				}
				Usuario usuarioSistema = getUsuarioService().getUsuarioSistema();
				PapelService papelService = ComponentUtil.getComponent(PapelService.class);
				Papel papelUsuarioSistema = papelService.findByCodeName(Papeis.SISTEMA);
				Localizacao localizacaoSistema = ParametroUtil.instance().getLocalizacaoTribunal();

				retorno = this.verificaPossibilidadeAssinatura(processoDocumento, usuarioSistema, movimentosSelecionados, papelUsuarioSistema);
				if(retorno.getResultado()) {
					processoDocumento.setProcessoDocumentoBin(this.assinaSistema(processoDocumento.getProcessoDocumentoBin()));
					
					this.finalizaDocumento(processoDocumento, processoDocumento.getProcessoTrf(), jbpmTask, true, true, false, 
							getPessoaService().findById(usuarioSistema.getIdUsuario()), localizacaoSistema, papelUsuarioSistema, true);
					
					if(processInstance != null) {
						if (jbpmTask != null) {
							TaskInstance taskinstance = ManagedJbpmContext.instance().getTaskInstance(jbpmTask);
							
							Integer idMinutaEmElaboracao = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(taskinstance);
							if(idMinutaEmElaboracao == idProcessoDocumento) {
								TaskInstanceHome.instance().trataVariaveisDocumentosPosJuntada(taskinstance, processoDocumento);
							}
						}
						LancadorMovimentosService.instance().lancarMovimentosTemporarios(processInstance);
					}
					if(processoDocumento.isJuntado()) {
						retorno.setResultado(Boolean.TRUE);
						retorno.setMensagem("");
						getProcessoDocumentoManager().persistAndFlush(processoDocumento);
						Events.instance().raiseTransactionSuccessEvent(AutomacaoTagService.EVENTO_AUTOMACAO_TAG, processoDocumento.getProcessoTrf().getIdProcessoTrf());
					}else {
						retorno.setResultado(Boolean.FALSE);
						retorno.setMensagem("Houve uma falha ao juntar o documento");
					}
				}
			}
		}
		return retorno;
	}
	
	/**
	 * Para que seja possível assinar um documento dado:
	 * 1. documento não deve estar juntado e deve estar ativo
	 * 2. documento deve ser binário ou deve ter conteúdo informado
	 * 3. documento não deve ter agrupamento de movimentos relacionados ou se tiver, deve ter movimentos já relacionados
	 * 4. se já houver assinatura para o documento o usuário atual não deve ter assinado ainda
	 * 5. papel do usuário deve ter permissão de exigibilidade para assinar o tipo de documento (suficiente / obrigatória / facultativa)
	 * 
	 * @param documentoVerificacao
	 * @param movimentosSelecionados
	 * @param papelUsuario
	 * @return
	 */
	public ResultadoComplexoVO verificaPossibilidadeAssinatura(ProcessoDocumento documentoVerificacao, Usuario usuarioVerificacao, List<EventoBean> movimentosSelecionados, Papel papelUsuario) {
		ResultadoComplexoVO retorno = new ResultadoComplexoVO(Boolean.TRUE, "");

		retorno = this.verificaPossibilidadeAssinaturaSemMovimentos(documentoVerificacao, usuarioVerificacao, papelUsuario);
		if(retorno.getResultado() && this.verificaPendenciaMovimentacaoParaAssinatura(documentoVerificacao, movimentosSelecionados)) {
			retorno.setResultado(Boolean.FALSE);
			retorno.setMensagem("A seleção de movimentação é obrigatória");
		}
		return retorno;
	}
	
	/**
	 * Verifica se há possibilidade de se assinar o documento sem avaliar a necessidade de movimentos para a assinatura
	 * 
	 * @param documentoVerificacao
	 * @param usuarioVerificacao
	 * @param papelUsuario
	 * @return
	 */
	public ResultadoComplexoVO verificaPossibilidadeAssinaturaSemMovimentos(ProcessoDocumento documentoVerificacao, Usuario usuarioVerificacao, Papel papelUsuario) {
		ResultadoComplexoVO retorno = new ResultadoComplexoVO(Boolean.TRUE, "");
		
		if(documentoVerificacao != null && documentoVerificacao.getAtivo() &&((documentoVerificacao.getProcessoDocumentoBin() != null 
				&& documentoVerificacao.getProcessoDocumentoBin().getModeloDocumento() != null 
				&& !documentoVerificacao.getProcessoDocumentoBin().getModeloDocumento().trim().isEmpty()
			) || documentoVerificacao.getProcessoDocumentoBin().isBinario())) {
			if(!documentoVerificacao.isJuntado()) {
				ProcessoDocumentoBinPessoaAssinaturaManager processoDocBinPessoaAssinaturaManager = ComponentUtil.getComponent(ProcessoDocumentoBinPessoaAssinaturaManager.class);
				
				if(!processoDocBinPessoaAssinaturaManager.verificaUsuarioAssinouDocumento(documentoVerificacao, usuarioVerificacao)) {
					TipoProcessoDocumentoPapelService tipoProcessoDocumentoPapelService = ComponentUtil.getComponent(TipoProcessoDocumentoPapelService.class);
					if(!tipoProcessoDocumentoPapelService.verificarExigibilidadeAssina(papelUsuario, documentoVerificacao.getTipoProcessoDocumento())) {
						retorno.setResultado(Boolean.FALSE);
						retorno.setMensagem("Usuário não tem permissão para assinar o tipo de documento ("+documentoVerificacao.getTipoProcessoDocumento().getTipoProcessoDocumento()+")");
					}
				}else {
					retorno.setResultado(Boolean.FALSE);
					retorno.setMensagem("Usuário já assinou o documento");
				}
			}else {
				retorno.setResultado(Boolean.FALSE);
				retorno.setMensagem("Documento já juntadao");
			}
		}else {
			retorno.setResultado(Boolean.FALSE);
			retorno.setMensagem("Documento não encontrado, excluído ou vazio");
		}

		return retorno;
	}
	
	public boolean verificaPendenciaMovimentacaoParaAssinatura(ProcessoDocumento processoDocumento) {
		boolean result = false;
		Long idJbpmTask = processoDocumento.getIdJbpmTask();

		if (processoDocumento.getTipoProcessoDocumento().getAgrupamento() != null && idJbpmTask != null) {
			ProcessInstance processInstance = ManagedJbpmContext.instance()
					.getTaskInstance(idJbpmTask).getProcessInstance();

			if (processInstance != null) {
				List<EventoBean> movimentosSelecionados = LancadorMovimentosService.instance()
						.getMovimentosTemporarios(processInstance);

				result = this.verificarPendenciaEvento(movimentosSelecionados);
			}
		}

		return result;
	}

	public boolean verificaPendenciaMovimentacaoParaAssinatura(ProcessoDocumento processoDocumento, Long idJbpmTask) {
		boolean result = false;
		if (processoDocumento.getTipoProcessoDocumento().getAgrupamento() != null && idJbpmTask != null) {
			ProcessInstance processInstance = ManagedJbpmContext.instance().getTaskInstance(idJbpmTask)
					.getProcessInstance();

			if (processInstance != null) {
				List<EventoBean> movimentosSelecionados = LancadorMovimentosService.instance()
						.getMovimentosTemporarios(processInstance);

				result = this.verificarPendenciaEvento(movimentosSelecionados);
			}
		}

		return result;
	}
	
	public void validaPendenciaMovimentacaoParaAssinatura(ProcessoDocumento processoDocumento, Long idTarefa) throws PJeBusinessException {
		Long idJbpmTask = null;
		if (idTarefa != null) {
			idJbpmTask = idTarefa;
		} else if (processoDocumento.getIdJbpmTask() != null) {
			idJbpmTask = processoDocumento.getIdJbpmTask();
		}
		
		boolean possuiCondicaoLancamentoMovimentoObrigatorio = LancadorMovimentosService.instance().possuiCondicaoLancamentoMovimentoObrigatorio(ManagedJbpmContext.instance().getTaskInstance(idJbpmTask));
		if (processoDocumento.getDocumentoPrincipal() == null) {
			if (possuiCondicaoLancamentoMovimentoObrigatorio && this.verificaPendenciaMovimentacaoParaAssinatura(processoDocumento, idJbpmTask)) {
				throw new PJeBusinessException(String.format("A assinatura não foi concluída. O tipo de documento %s exige a indicação de pelo menos um movimento / complemento processual.", processoDocumento.getTipoProcessoDocumento().getTipoProcessoDocumento()));
			} else if (!possuiCondicaoLancamentoMovimentoObrigatorio && verificarPendenciaComplemento(processoDocumento, idJbpmTask)) {
				throw new PJeBusinessException("A assinatura não foi concluída. Há movimento sem complemento informado.");
			}
		}
	}

	/**
	 * Verifica se, caso algum movimento foi adicionado, este(s) movimento(s) possuem complemento e foi informado o complemento.
	 * @param processoDocumento
	 * @param idJbpmTask
	 * @return
	 */
	private boolean verificarPendenciaComplemento(ProcessoDocumento processoDocumento, Long idJbpmTask) {
		boolean retorno = false;
		if (processoDocumento.getTipoProcessoDocumento().getAgrupamento() != null && idJbpmTask != null) {
			ProcessInstance processInstance = ManagedJbpmContext.instance().getTaskInstance(idJbpmTask).getProcessInstance();
			if (processInstance != null) {
				List<EventoBean> movimentosSelecionados = LancadorMovimentosService.instance().getMovimentosTemporarios(processInstance);
				if (movimentosSelecionados != null && !movimentosSelecionados.isEmpty()) {
					retorno = verificarPendenciaEventoPossuirComplementoVazio(movimentosSelecionados);
				}
			}
		}
		return retorno;
	}

	private boolean verificarPendenciaEventoPossuirComplementoVazio(List<EventoBean>  eventoBeanList) {
		boolean retorno = false;
		for (EventoBean eventoBean: eventoBeanList) {
			if (Boolean.TRUE.equals(eventoBean.getTemComplemento()) && verificarPendenciaMovimento(eventoBean.getMovimentoBeanList())) {
				retorno = true;
				break;
			}
		}
		return retorno;
	}

	/**
	 * Verifica se o tipo de documento:
	 * 	- não tem agrupamento com movimentos indicados
	 *  - ou já foram selecionados os movimentos relacionados
	 * 
	 * @param documentoVerificacao
	 * @param movimentosSelecionados
	 * @return TRUE se houver pendência de movimentação e FALSE se não houver pendência para assinar
	 */
	public boolean verificaPendenciaMovimentacaoParaAssinatura(ProcessoDocumento documentoVerificacao, List<EventoBean> movimentosSelecionados) {
		boolean haPendencias = false;
		if(documentoVerificacao.getTipoProcessoDocumento().getAgrupamento() != null) {
			if(verificarPendenciaEvento(movimentosSelecionados)) {
				List<Evento> eventoList = ComponentUtil.getComponent(EventoAgrupamentoManager.class)
						.recuperarEventos(documentoVerificacao.getTipoProcessoDocumento().getAgrupamento());
				haPendencias = CollectionUtilsPje.isNotEmpty(eventoList);
			}
		}
		return haPendencias;
	}
	
	private boolean verificarPendenciaEvento(List<EventoBean> eventosBean) {
		boolean resultado = false;
		
		if (CollectionUtilsPje.isEmpty(eventosBean)) {
			resultado = true;
		} else {
			for (EventoBean eventoBean: eventosBean) {
				resultado = eventoBean.getTemComplemento() && verificarPendenciaMovimento(eventoBean.getMovimentoBeanList());
				if (resultado) {
					break;
				}
			}
		}
		
		return resultado;
	}
	
	private boolean verificarPendenciaMovimento(List<MovimentoBean> movimentosBean) {
		boolean resultado = false;
		
		if (CollectionUtilsPje.isEmpty(movimentosBean)) {
			resultado = true;
		} else {
			for (MovimentoBean mb: movimentosBean) {
				resultado = verificarPendenciaComplemento(mb.getComplementoBeanList());
				if (resultado) {
					break;
				}
			}
		}
		
		return resultado;
	}
	
	private boolean verificarPendenciaComplemento(List<ComplementoBean> complementosBean) {
		boolean resultado = false;
		
		if (CollectionUtilsPje.isEmpty(complementosBean)) {
			resultado = true;
		} else {
			for (ComplementoBean complementoBean: complementosBean) {
				resultado = this.verificarPendenciaValorComplemento(complementoBean.getValorComplementoBeanList());
				if (resultado) {
					break;
				}
			}
		}
		
		return resultado;
	}
	
	private boolean verificarPendenciaValorComplemento(List<ValorComplementoBean> valoresComplementoBean) {
		boolean resultado = false;
		
		if (CollectionUtilsPje.isEmpty(valoresComplementoBean)) {
			resultado = true;
		} else {
			for (ValorComplementoBean valorComplementoBean: valoresComplementoBean) {
				if (StringUtils.isEmpty(valorComplementoBean.getValor())) {
					resultado = true;
					break;
				}
			}
		}
		
		return resultado;
	}
	
	private ProcessoDocumentoBin preencheDadosAssinatura(ProcessoDocumentoBin pdb, Usuario assinador) throws PJeBusinessException{
		pdb.setUsuario(assinador);
		pdb.setUsuarioUltimoAssinar(assinador.getNome());
		if(pdb.getDataAssinatura() != null){
			pdb.setDataAssinatura(new Date());
		}
		return pdb;
	}
	
	public ProcessoDocumentoBin assinaSistema(ProcessoDocumentoBin pdb) throws PJeBusinessException{
		try {
			pdb = this.preencheDadosAssinatura(pdb, getUsuarioService().getUsuarioSistema());
			
			CertificadoDigitalService certificadoDigitalService = ComponentUtil.getComponent(CertificadoDigitalService.class);
			pdb = certificadoDigitalService.assinaSistema(pdb);
			
			ProcessoDocumentoBinPessoaAssinatura processoDocumentoBinPessoaAssinatura = new ProcessoDocumentoBinPessoaAssinatura();
			processoDocumentoBinPessoaAssinatura.setProcessoDocumentoBin(pdb);
			processoDocumentoBinPessoaAssinatura.setPessoa(ParametroUtil.instance().getPessoaSistema());
			processoDocumentoBinPessoaAssinatura.setAssinatura(pdb.getSignature());
			processoDocumentoBinPessoaAssinatura.setCertChain(pdb.getCertChain());
			EntityUtil.getEntityManager().persist(processoDocumentoBinPessoaAssinatura);
			
			pdb.getSignatarios().add(processoDocumentoBinPessoaAssinatura);
			
			return pdb;
		} catch (PJeBusinessException e) {
			throw e;
		}
	}
	
	public Future<Boolean> assinaSistemaAsync(ProcessoDocumentoBin pdb) throws PJeBusinessException{
		pdb = this.preencheDadosAssinatura(pdb, getUsuarioService().getUsuarioSistema());
		CertificadoDigitalService certificadoDigitalService = ComponentUtil.getComponent(CertificadoDigitalService.class);
		return certificadoDigitalService.assinaSistemaAsync(pdb);
	}
	
	public List<ProcessoDocumentoBinPessoaAssinatura> validaAssinaturasDocumento(ProcessoDocumento pd){
		return validaAssinaturasDocumento(pd, true);
	}
	
	public List<ProcessoDocumentoBinPessoaAssinatura> validaAssinaturasDocumento(
			ProcessoDocumento pd, boolean validarExigibilidadeAssinatura) {
	
		return validaAssinaturasDocumento(pd, validarExigibilidadeAssinatura, true);
	}
	
	public List<ProcessoDocumentoBinPessoaAssinatura> validaAssinaturasDocumento(
			ProcessoDocumento pd, boolean validarExigibilidadeAssinatura,
			boolean refreshEntity) {
		
		ProcessoDocumentoBin pdb = pd.getProcessoDocumentoBin();
		
		try {
			if (refreshEntity) {
				getProcessoDocumentoBinManager().refresh(pdb);
			}
		} catch (PJeBusinessException e) {
			logger.error("Erro ao tentar recarregar o documento: {0}.",
					pdb.getIdProcessoDocumentoBin());
		}

		if (validarExigibilidadeAssinatura) {
			if (!getProcessoDocumentoBinManager().verificaValidacao(pdb,	pdb.getSignatarios(), pd.getTipoProcessoDocumento())) {
				return Collections.emptyList();
			}
		}

		return getProcessoDocumentoBinManager().verificaAssinaturas(pdb);
	}

	public ProcessoDocumento getDocumentoPendente(ProcessoTrf processoJudicial, Localizacao localizacaoFisica){
		return getDocumentoPendente(processoJudicial, localizacaoFisica, false);
	}

	public ProcessoDocumento getDocumentoPendente(ProcessoTrf processoJudicial, Localizacao localizacaoFisica, boolean abrangerDocumentosDeAtividadeEspecifica){
		return getProcessoDocumentoManager().getDocumentoPendente(processoJudicial, localizacaoFisica, abrangerDocumentosDeAtividadeEspecifica);
	}

	/**
	 * Verifica se há pelo menos um documento não apreciado de um processo, que
	 * este documento seja de um dos tipos requeridos e que tenha sido assinado
	 * após o último documento de um certo tipo.
	 * 
	 * @param idProcesso id do processo que contém os documentos
	 * @param idTipoDocumento id do tipo de documento do último documento
	 * @param tipos tipos dos documentos que serão verificados não apreciação.
	 * @return
	 */
	public boolean haDocumentoNaoApreciado(Integer idProcesso, Integer idTipoDocumento, Integer... tipos) {
		ProcessoTrf processo = null;
		try {
			processo = getProcessoJudicialService().findById(idProcesso);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		boolean existeAlgumNaoApreciado = false;
		try{
			TipoProcessoDocumentoDAO tipoProcessoDocumentoDAO = ComponentUtil.getComponent(TipoProcessoDocumentoDAO.class);
			TipoProcessoDocumento tipoProcessoDocumento = tipoProcessoDocumentoDAO.find(idTipoDocumento);
			
			// Busca o último documento (através de data) do tipo informado
			ProcessoDocumentoDAO processoDocumentoDAO = getProcessoDocumentoDAO();
			ProcessoDocumento ultimo = processoDocumentoDAO.getUltimoProcessoDocumentoByProcessoTipoProcessoDocumento(tipoProcessoDocumento, processo.getProcesso());
			Date dataUltimoDocumentoInserido = ultimo.getProcessoDocumentoBin().getDataAssinatura(); 
			
			if(dataUltimoDocumentoInserido != null) {
				List<ProcessoDocumento> documentos = processoDocumentoDAO.getDocumentosPorTipoAssinadosApos(processo, dataUltimoDocumentoInserido, tipos);
				Map<ProcessoDocumento, Boolean> documentosApreciados = documentosApreciados(getProcessoDocumentoLidoManager().listProcessosDocumentosLidos(documentos));
				
				for(ProcessoDocumento processoDocumento : documentos) {
					if(documentosApreciados.get(processoDocumento) == null) {
						existeAlgumNaoApreciado = true;
						break;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			existeAlgumNaoApreciado = false;
		}
		
		return existeAlgumNaoApreciado;
	}
	
	private Map<ProcessoDocumento, Boolean> documentosApreciados(List<ProcessoDocumentoLido> documentosLidos) {
		Map<ProcessoDocumento, Boolean> mapaDocumentosApreciados = new HashMap<ProcessoDocumento, Boolean>();
		
		for(ProcessoDocumentoLido documentoLido : documentosLidos) {
			if(documentoLido.getDataApreciacao() != null) {
				mapaDocumentosApreciados.put(documentoLido.getProcessoDocumento(), Boolean.TRUE);
			}
		}
		
		return mapaDocumentosApreciados;
	}

	/**
	 * [PJEII-1571] Busca modelos disponíveis conforme a localização informada
	 * @param localizacao localização desejada
	 * @return List<ModeloDocumento> contendo os modelos da localização informada
	 */
	public List<ModeloDocumento> getModelosDisponiveisPorLocalizacao(Localizacao localizacao) {
		List<Localizacao> listaLocalizacoes = getLocalizacaoService().getLocalizacoesAscendentesDescendentesOjsOjcs(localizacao);
		return getModeloDocumentoLocalManager().getModeloDocumentoPorListaLocalizacao(listaLocalizacoes);
	}
	
	/**
	  * [PJEII-5382] Busca modelos disponíveis conforme tipo de documento e localização.
	 * @param tipoDocumento TipoProcessoDocumento cujos modelos são desejados
	 * @param localizacao localização do usuário atual
	 * @return List<ModeloDocumento> contendo todos os modelos daquele tipo de documento que pertençam à localização do usuário,
	 * a qualquer uma de suas localizações superiores ou inferiores.
	 * @throws PJeBusinessException 
	 */
	public List<ModeloDocumento> getModeloDocumentoListPorTipoDocumentoPorLocalizacao(TipoProcessoDocumento tipoDocumento, Localizacao localizacao) throws PJeBusinessException {
		return getModelosLocais(tipoDocumento);
	}
	
	
	/**
	 * Remove um ProcessoDocumento ou ProcessoDocumentoEstruturado a partir de uma variável de fluxo.
	 * Primeiro recupera o id associado a variável de fluxo passada como parametro
	 * Se não tiver nenhum id associado a variável, o método retorna sem fazer nada. 
	 * Depois testa se existe um ProcessoDocumentoEstruturado associado aquele id,
	 * se tiver remove toda a estrutura associada ao ProcessoDocumentoEstruturado.
	 * Se não tiver, tenta recuperar o ProcessoDocumento associado ao id.
	 * se tiver remove toda a estrutura associada ao ProcessoDocumento.
	 * Se não tiver, não faz nada.
	 * 
	 * @author Antonio Lucas
	 * @param var variável de fluxo associada ao id do texto sendo editado.
	 */
	public void removerDocumentoAPartirDeVariavalDeFluxo(String var){
		getProcessoDocumentoManager().removerDocumentoAPartirDeVariavalDeFluxo(var);
	}

	
	public boolean podeAssinar(TipoProcessoDocumento tipoDocumento, Papel...papeis){
		return getProcessoDocumentoBinManager().podeAssinar(tipoDocumento, papeis);
	}
	
	/**
	 * Recupera uma lista com todos os tipos de documentos ativos da instalação.
	 * 
	 * @return a lista dos tipos ativos
	 * @throws PJeBusinessException
	 * @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-1641">PJEII-1641</a>
	 */
	public List<TipoProcessoDocumento> getTiposAtivos() throws PJeBusinessException{
		return getTipoProcessoDocumentoManager().findByRange(null, null, Boolean.TRUE, "ativo");
	}
	
	@Restrict("#{identity.loggedIn}")
	public void remove(ProcessoDocumento doc) throws PJeBusinessException{
		if(doc.getDataJuntada() != null){
			getProcessoDocumentoManager().inactivate(doc);
		}else{
			getProcessoDocumentoManager().remove(doc);
		}
	}

	public TipoProcessoDocumento presuncaoTipoDocumento(String descricao) throws PJeBusinessException {
		return getTipoProcessoDocumentoManager().presuncaoTipoDocumento(descricao, getUsuarioService().getLocalizacaoAtual().getPapel());
	}

	public List<TipoProcessoDocumento> getTiposDisponiveis(Papel papel, TipoDocumentoEnum...tipos) throws PJeBusinessException {
		return getTiposDisponiveis(papel, false, tipos);
	}
	
	public List<TipoProcessoDocumento> getTiposDisponiveis(Papel papel, boolean restringirJuntadaDocumento, TipoDocumentoEnum...tipos) throws PJeBusinessException {
		return getTipoProcessoDocumentoManager().getDisponiveis(papel, restringirJuntadaDocumento, tipos);
	}
	
	/**
	 * Recupera os links de downloads dos documentos para assinatura digital.
	 * 
	 * @par 
	 * @return os links de downloads, separados por vírgula, sem o endereço principal, que deve ser configurado na applet 
	 */
	public String getDownloadLinks(Collection<ProcessoDocumento> documentos){
		return getDownloadLinks(documentos, true);
	}

	/**
	 * Recupera os links de downloads dos documentos para assinatura digital.
	 * 
	 * @par 
	 * @return os links de downloads, separados por vírgula, sem o endereço principal, que deve ser configurado na applet 
	 */
	public String getDownloadLinks(Collection<ProcessoDocumento> documentos, boolean recalculateHash){
		StringBuilder sb = new StringBuilder();
		if(documentos != null && !documentos.isEmpty()){
			int i = 0;
			int size = documentos.size();
			for(ProcessoDocumento pd: documentos){
				if(pd == null){
					continue;
				}
				if(pd.getIdProcessoDocumento() > 0){
					createDownloadLink(pd, sb, recalculateHash);
					if(i < (size - 1)){
						sb.append(",");
					}
				}
				i++;
			}
		}
		return sb.toString();
	}
	
	/**
	 * Recupera o link de download de uma mensagem para assinatura digital.
	 * 
	 * @param mensagem A mensagem que sera assinada digitalmente
	 * @return Uma string com o param md5= hash md5 da mensagem
	 */
	public String getDownloadLink(String mensagem) {
		return "md5=" + Crypto.encodeMD5(mensagem);
	}

	/**
	 * Prepara um link para download do documento pelo aplicativo de assinatura digital.
	 * 
	 * @param pd o documento a ser assinado
	 * @return o conjunto de parâmetros do link de download do documento
	 */
	private void createDownloadLink(ProcessoDocumento pd, StringBuilder sb, boolean recalculateHash) {
		sb.append("id=");
		sb.append(String.valueOf(pd.getIdProcessoDocumento()));
		sb.append("&codIni=");
		sb.append(ProcessoDocumentoHome.instance().getCodData(pd));
		sb.append("&md5=");
		if(recalculateHash && !pd.getProcessoDocumentoBin().isBinario()){
			sb.append(Crypto.encodeMD5(pd.getProcessoDocumentoBin().getModeloDocumento()));
		}else{
			sb.append(pd.getProcessoDocumentoBin().getMd5Documento());
		}
		sb.append("&isBin=");
		sb.append(pd.getProcessoDocumentoBin().getBinario());
	}
	
	
	/**
	 * Verificar se existem determinado tipo de documentos ativos anexados ao
	 * processo. O termo impeditivo não utiliza regra alguma, ficando a critério
	 * do consumidor do método definir quais os tipos de arquivos são
	 * impeditivos.
	 * 
	 * O método adiciona uma mensagem de alerta ao processo caso seja
	 * requisitado.
	 * 
	 * @see #getDocumentosPorTipo(ProcessoTrf, Integer...)
	 * 
	 * @param processoTrf
	 *            Processo para pesquisar os documentos
	 * 
	 * @param adicionarAlerta
	 *            Indicar se irá adicionar uma mensagem de alerta ao processo
	 *            caso os documentos sejam encontrados
	 *            
	 * @see {@link AlertaHome#inserirAlerta(ProcessoTrf, String, CriticidadeAlertaEnum)}
	 * 
	 * @param idsTipoProcessoDocumento
	 *            Ids dos tipos de documentos que serão pesquisado
	 * 
	 * @return <code>true</code> caso sejam encontrados os tipos de documento e
	 *         <code>false</code> caso não sejam encontrados.
	 */
	public boolean existeDocumentosImpeditivosNoProcesso(
			ProcessoTrf processoTrf, boolean adicionarAlerta,
			Integer... idsTipoProcessoDocumento) {
		
		List<ProcessoDocumento> documentosPorTipo = getProcessoDocumentoManager()
				.getDocumentosPorTipo(processoTrf, idsTipoProcessoDocumento);
		
		boolean possuiDocumentoImpeditivo = false;
		
		if (documentosPorTipo != null && !documentosPorTipo.isEmpty()) {
			
			StringBuilder msg = new StringBuilder("Existe(m) documento(s) impeditivo(s) anexados ao processo: ");
			
			List<String> tiposEIds = new ArrayList<String>();			
			for (ProcessoDocumento documento : documentosPorTipo) {
				if (documento.getAtivo()) {
					possuiDocumentoImpeditivo = true;
					tiposEIds.add(documento.getTipoProcessoDocumento().getTipoProcessoDocumento()+" ("+documento.getIdProcessoDocumento()+")" );
				}							
			}	
			
			msg.append(StringUtil.concatList(tiposEIds, ", ", " e "));
			
			if (possuiDocumentoImpeditivo && adicionarAlerta) {
				AlertaHome.instance().inserirAlerta(processoTrf, msg.toString(), CriticidadeAlertaEnum.A);			
				EntityUtil.getEntityManager().flush();				
			}
		}
		
		return possuiDocumentoImpeditivo;
	}
	
	
	/**
	 * Método responsável por verificar se existem documentos adicionados aos
	 * processos pendentes de leitura/apreciação.
	 * 
	 * @see ProcessoDocumentoManager#listDocumentosNaoLidos(ProcessoTrf)
	 * 
	 * @param processoTrf
	 *            {@link ProcessoTrf} a ser pesquisado
	 * 
	 * @param adicionarAlerta
	 *            Indicador para adicionar mensagem de alerta ao processo caso
	 *            exista algum documento pendente de leitura/apreciação.
	 * @see {@link AlertaHome#inserirAlerta(ProcessoTrf, String, CriticidadeAlertaEnum)}
	 * 
	 * @return <code>true</code> caso exista documentos pendentes de leitura
	 *         para o processo.
	 * 
	 *         <code>false</code> caso não exista documentos pendentes de
	 *         leitura para o processo.
	 */
	public boolean existeDocumentosNaoApreciado(ProcessoTrf processoTrf,
			boolean adicionarAlerta) {
		
		List<ProcessoDocumento> documentosNaoApreciados = getProcessoDocumentoManager()
				.listDocumentosNaoLidos(processoTrf);
		
		if (documentosNaoApreciados != null && !documentosNaoApreciados.isEmpty()) {			
			
			if (adicionarAlerta) {
				StringBuilder msg = new StringBuilder("Este processo possui documento(s) pendentes de apreciação. ID's: ");
				
				List<String> idsDocumentos = new ArrayList<String>();
				for (ProcessoDocumento documento : documentosNaoApreciados) {
					idsDocumentos.add(String.valueOf(documento.getIdProcessoDocumento()));
				}			
				msg.append(StringUtil.concatList(idsDocumentos, ", ", " e "));
				
				AlertaHome.instance().inserirAlerta(processoTrf, msg.toString(), CriticidadeAlertaEnum.A);
				EntityUtil.getEntityManager().flush();
			}
			
			return true;
		}
		
		return false;
	}

	/**
	 * [PJEII-18590] Método responsável por recuperar a lista de documentos assinados pelo usuário especificado. 
	 * Pode-se limitar a lista por tipo de documento
	 * @param processoJudicial Dados do processo
	 * @param usuario Dados do usuário
	 * @param tipos Lista de tipos de documento
	 * @return Lista de documentos assinados pelo usuário
	 * @throws PJeBusinessException
	 */
	public List<ProcessoDocumento> getDocumentosAssinadosPorUsuario(
			ProcessoTrf processoJudicial, 
			Usuario usuario, 
			String tipos) 
			throws PJeBusinessException {
		return getDocumentosAssinadosPorUsuario(processoJudicial, usuario, null, tipos);
	}
	
	/**
	 * [PJEII-18590] Método responsável por recuperar a lista de documentos assinados pelo usuário especificado. 
	 * Pode-se limitar a lista por tipo de documento
	 * @param processoJudicial Dados do processo
	 * @param usuario Dados do usuário
	 * @param instancia Instância na qual o documento foi criado
	 * @param tipos Lista de tipos de documento
	 * @return Lista de documentos assinados pelo usuário
	 * @throws PJeBusinessException
	 */
	public List<ProcessoDocumento> getDocumentosAssinadosPorUsuario(
			ProcessoTrf processoJudicial, 
			Usuario usuario, 
			String instancia, 
			String tipos) 
			throws PJeBusinessException {
		
		List<ProcessoDocumento> listaDocumentosAssinados = new ArrayList<ProcessoDocumento>();
		
		if (processoJudicial != null && usuario != null) {
			List<ProcessoDocumento> listaDocumentos = getProcessoDocumentoManager().getDocumentosPorTipo(
					processoJudicial, convertStringToIntegerArray(tipos));
			
			if (listaDocumentos != null) {
				for (ProcessoDocumento processoDocumento : listaDocumentos) {
					// Se a instância não foi informada ou é igual ao do documento atual...
					if (instancia == null || processoDocumento.getInstancia().equalsIgnoreCase(instancia)) {
						ProcessoDocumentoBinPessoaAssinatura signatario = null;
						
						// Se a lista de signatários não está vazia...
						if (processoDocumento.getProcessoDocumentoBin().getSignatarios() != null && 
								processoDocumento.getProcessoDocumentoBin().getSignatarios().size() > 0) {
							signatario = processoDocumento.getProcessoDocumentoBin().getSignatarios().get(0);
						}
						
						// Se o login do usuário especificado for igual ao login do usuário que assinou o documento...
						if (signatario != null && signatario.getPessoa() != null && usuario.getLogin().equals(signatario.getPessoa().getLogin())) {
							listaDocumentosAssinados.add(processoDocumento);
						}
					}
				}
			}			
		}
				
		return listaDocumentosAssinados;
	}
	
	private Integer[] convertStringToIntegerArray(String arg) {
		if (StringUtils.isNotEmpty(arg)) {
			String[] strArray = arg.split("\\,");
			
			if (strArray.length > 0) {
				Integer[] integerArray = new Integer[strArray.length];
				
				for (int i = 0; i < strArray.length; i++) {
					try {
						integerArray[i] = Integer.valueOf(strArray[i].trim());
					} catch (NumberFormatException ex) {
						 continue;
					}
				}
				return integerArray;
			}
		}
		return null;
	}
	
	/**
	 * Método responsável por disparar o fluxo após a assinatura do documento.
	 * 
	 * @param ProcessoDocumento doc
	 */
	public void dispararFluxo(ProcessoDocumento doc){
		if(doc != null && doc.getIdProcessoDocumento() > 0 && doc.getTipoProcessoDocumento() != null && 
		   doc.getTipoProcessoDocumento().getFluxo() != null && Authenticator.isUsuarioExterno()) {
			
			Events.instance().raiseAsynchronousEvent(Eventos.INICIAR_FLUXO_PETICAO_INCIDENTAL, doc.getIdProcessoDocumento());
		}
	}
	
	public List<TipoProcessoDocumento> getTiposAtivos(Integer idTipoProcessoDocumentoExceto) throws PJeBusinessException{
		return getTipoProcessoDocumentoManager().findTiposExceto(idTipoProcessoDocumentoExceto);
	}
	
	/** Método que retorna a lista de tipos de documentos ativos que estejam dentro da lista informada
	 * @param idsTipoDocumento - a lista de ids dos tipos de documento que deverão ser retornados caso estejam ativos
	 * @return a lista de tipos de documentos ativos dentre os informados no parâmetro
	 */
	public List<TipoProcessoDocumento> getTiposAtivosIn(List<Integer> idsTipoDocumento) throws PJeBusinessException{
		return getTipoProcessoDocumentoManager().findTiposIn(idsTipoDocumento);
	}
	
	/** Método que retorna a lista de tipos de documentos ativos que não estejam dentro da lista informada
	 * @param idsTipoDocumento - a lista de ids dos tipos de documento que não deverão ser retornados.
	 * @return a lista de tipos de documentos ativos exceto os informados no parâmetro
	 */
	
	public List<TipoProcessoDocumento> getTiposAtivosNotIn(List<Integer> idsTipoDocumento) throws PJeBusinessException{
		return getTipoProcessoDocumentoManager().findTiposNotIn(idsTipoDocumento);
	}
	
	public List<ProcessoDocumento> getDocumentos(ProcessoTrf processoTrf,Integer idTipoDocumentoExceto){
		List<ProcessoDocumento> resultado = getProcessoDocumentoManager().findAllExceto(processoTrf,idTipoDocumentoExceto);
		return resultado;
	}

	/**
	 * Refatoracao metodo extraido da classe signFile para permitir que seja invocado de outros lugares
	 * Metodo responsavel por validar e gravar um documento assinado digitalmente
	 * 
	 * Ele realiza as seguintes validacoes:
	 *  
	 *  - cadeia de certificado digital
	 *  - assinatura digital
	 *  - se o usuario logado e o mesmo que assinou
	 *  - se o cpf do usuario logado e o mesmo do certificado digital da assinatura 
	 *  - se o usuario tem o papel correto para realizar assinatura
	 *  - se o codIni e o cod correspondente do documento
	 *
	 * @param id
	 * @param codIni
	 * @param md5
	 * @param sign
	 * @param certChain
	 * @param pessoa
	 * @throws Exception
	 */
	public void gravarAssinatura(String id, String codIni, String md5, String sign, String certChain, Pessoa pessoa) throws Exception {
		
		ProcessoDocumento pd = null;
		
		try {			
			pd = EntityUtil.getEntityManager().find(ProcessoDocumento.class, Integer.parseInt(id));
		}
		catch (Exception e) {}	
		
		if (pd == null) {
			throw new Exception("Documento não encontrado: " + id);
		}

		Date dataAssinatura = new Date();

		String erroMsg = ProcessoDocumentoHome.instance().signDocumento(pd.getIdProcessoDocumento(), codIni, md5, sign, certChain, dataAssinatura, pessoa);
		
		if (!StringUtil.isNullOrEmpty(erroMsg)) {
			throw new Exception(erroMsg);
		}
	}
	
	public Date createDataAssinatura(ProcessoDocumento pd, List<ProcessoDocumentoBinPessoaAssinatura> assinaturasAssociadas) {
		DateService dateService = ComponentUtil.getComponent(DateService.NAME);
		if (pd.getProcessoDocumentoBin().isBinario() || assinaturasAssociadas.isEmpty()) {
			return dateService.getDataHoraAtual();
		}
		Date dataAssinatura = getMenorDataAssinatura(assinaturasAssociadas);
		if (dataAssinatura != null) {
			return DateUtil.addMilisegundos(dataAssinatura, -1);
		} else {
			return dateService.getDataHoraAtual();
		}
	}
	
	private Date getMenorDataAssinatura(List<ProcessoDocumentoBinPessoaAssinatura> assinaturasAssociadas) {
		Date resultado = null;
		for (ProcessoDocumentoBinPessoaAssinatura assinatura : assinaturasAssociadas) {
			if (resultado == null || resultado.after(assinatura.getDataAssinatura())) {
				resultado = assinatura.getDataAssinatura();
			}
		}
		return resultado;
	}
	
	/**
	 * Metodo responsavel por gravar a lista de arquivos assinados digitalmente e realizar as validacoes necessarias. 
     *   
	 * @param arquivosAssinados Lista de arquivos assinados
	 * @param documentosParaAssinatura Lista de documentos do processo disponiveis para assinatura
	 * @throws Exception 
	 */
	public void gravarAssinaturaDeProcessoDocumento(Collection<ArquivoAssinadoHash> arquivosAssinados, Collection<ProcessoDocumento> documentosParaAssinatura) throws Exception {

		if (!validarSeDocumentosDisponiveisParaAssinaturaForamAssinadosEhQuantidadeDeAssinaturasConfere(arquivosAssinados, documentosParaAssinatura)) {
			throw new PJeBusinessException("Algum dos documentos não foi assinado ou quantidade de assinatura não confere!");
		}

		for (ArquivoAssinadoHash as : arquivosAssinados) {
			gravarAssinatura(as.getId(), as.getCodIni(), as.getHash(), as.getAssinatura(), as.getCadeiaCertificado(), Authenticator.getPessoaLogada());
		}

	}

	/**
	 * Metodo responsavel por validar se os documentos disponiveis para assinatura foram assinados
	 * e se a quantidade de assinaturas confere com a quantidade de documentos disponiveis para assinar.
	 * @param arquivosAssinados
	 * @return
	 */
	private boolean validarSeDocumentosDisponiveisParaAssinaturaForamAssinadosEhQuantidadeDeAssinaturasConfere(Collection<ArquivoAssinadoHash> arquivosAssinados, Collection<ProcessoDocumento> documentosParaAssinatura) {

		boolean retorno = true;
		
		// Verifica se a quantidade de documentos confere
		if (arquivosAssinados == null || documentosParaAssinatura.size() != arquivosAssinados.size()) {
			retorno = false;
		}
		else { 
			for (ProcessoDocumento documento : documentosParaAssinatura) {
				
				boolean encontrou = false;
				
				for (ArquivoAssinadoHash arquivoAssinadoHash : arquivosAssinados) {
					if (Objects.equals(documento.getIdProcessoDocumento(), arquivoAssinadoHash.getIdEmInteger())) {
						encontrou = true;
						break; 
					}
				}
				
				if (!encontrou) {
					retorno = false;
					break;
				}
			}
		}
		
		return retorno;
	}
	
	/**
	 * Juntar e gravar a assinatura de documentos nao sigilosos de processos 
	 * @param arquivosAssinados
	 * @param documentosParaAssinatura
	 * @throws Exception
	 */
	public void juntarEhGravarAssinaturaDeProcessosDocumentosNaoSigilosos(List<ArquivoAssinadoHash> arquivosAssinados, List<ProcessoDocumento> documentosParaAssinatura) throws Exception {

		gravarAssinaturaDeProcessoDocumento(arquivosAssinados, documentosParaAssinatura);
				
		Date now = new Date();
		
		for(ProcessoDocumento doc: documentosParaAssinatura){			
			doc.setDataJuntada(now);
			doc.setDocumentoSigiloso(false);
		}
		
		flush();		
	}
	
	/**
	 * Juntar e gravar a assinatura de documentos nao sigilosos de processos adicionando assinatura
	 * @param arquivosAssinados
	 * @param documentosParaAssinatura
	 * @throws Exception
	 */
	public void juntarEhGravarAssinaturaDeProcessosDocumentosNaoSigilososComMovimentacao(List<ArquivoAssinadoHash> arquivosAssinados, List<ProcessoDocumento> documentosParaAssinatura) throws Exception {

		gravarAssinaturaDeProcessoDocumento(arquivosAssinados, documentosParaAssinatura);
				
		Date now = new Date();
		
		for(ProcessoDocumento doc: documentosParaAssinatura){			
			doc.setDataJuntada(now);
			doc.setDocumentoSigiloso(false);
			
			MovimentoAutomaticoService.preencherMovimento().
			deCodigo(85).
			comProximoComplementoVazio().
			doTipoLivre().
			preencherComTexto(doc.getTipoProcessoDocumento().toString().toLowerCase()).				
			associarAoProcesso(doc.getProcesso()).
			associarAoDocumento(doc).
			lancarMovimento();
		}
		
		
		flush();		
	}

	private List<Localizacao> obterLocalizacoesUsuario() throws PJeBusinessException {
		UsuarioLocalizacaoMagistradoServidor ulms = getUsuarioService().getLocalizacaoAtual().getUsuarioLocalizacaoMagistradoServidor();
		Localizacao local =  null;
		if(ulms != null && ulms.getOrgaoJulgador() != null){
			local = ulms.getOrgaoJulgador().getLocalizacao();
		}else if(ulms != null && ulms.getOrgaoJulgadorColegiado() != null){
			local = ulms.getOrgaoJulgadorColegiado().getLocalizacao();
		}else{
			local =  getUsuarioService().getLocalizacaoAtual().getLocalizacaoFisica();
		}
		List<Localizacao> locais = getLocalizacaoService().getLocalizacoesAscendentesDescendentes(local);
		return locais;
	}
	
	/**
	 * Operao para filtrar tipos de documento que contm uma descrio informada.
	 * 
	 * @param descricao
	 * @return lista de tipos de documento cuja descrio contm o filtro passado.
	 * @throws PJeBusinessException
	 */
	public List<TipoProcessoDocumento> consultarTipoDocumento(String descricao) throws PJeBusinessException {
		try {
			Search search = new Search(TipoProcessoDocumento.class);
			search.addCriteria(Criteria.equals("ativo", true));
			search.addCriteria(Criteria.contains("tipoProcessoDocumento", descricao)); 
			search.addCriteria(Criteria.in("papeis.papel", new Papel[]{getUsuarioService().getLocalizacaoAtual().getPapel()}));
			return getTipoProcessoDocumentoManager().list(search);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new PJeBusinessException(e);
		}
	}
	
	/**
	 * Consulta documentos indexados em base de documentos replicadas via ferramenta de indexao.
	 * 
	 * @param texto {@link String} de filtragem.
	 * @return {@link List} de {@link JSONObject} com o retorno das informaes que foram consultadas.
	 * 
	 * @throws PJeBusinessException
	 * @throws JSONException 
	 */
	public List<JSONObject> consultarDocumentosIndexados(ConsultaDocumentoIndexadoVO filtros, boolean assinados) throws PJeBusinessException, JSONException{
		try {
			ElasticDAO<ProcessoDocumento> elasticDAO = new ElasticDAO<ProcessoDocumento>() {};
			elasticDAO.setIndexador(getIndexador());
			JSONObject documentospublicos_ = elasticDAO.search(carregarFiltroConsultarDocumentosDecisoesIndexados(filtros,false, assinados));
			JSONObject documentossigilososouminutas_ = elasticDAO.search(carregarFiltroConsultarDocumentosDecisoesIndexados(filtros,true, assinados));

			List<JSONObject> retorno = new ArrayList<JSONObject>();
			try {
				JSONArray jsonPublicos = ((JSONArray) ((JSONObject) documentospublicos_.get("hits")).get("hits"));
				JSONArray jsonSigilosos = ((JSONArray) ((JSONObject) documentossigilososouminutas_.get("hits")).get("hits"));

				retorno = new ArrayList<JSONObject>(jsonPublicos.length() + jsonSigilosos.length());
				
				List<Integer> ids = new ArrayList<Integer>(0);

				obterDocumentosPublicos(retorno, jsonPublicos, ids);
				obterDocumentosSigilosos(retorno, jsonSigilosos, ids);

			} catch (JSONException e) {
				logger.error("Houve um erro ao retornar o JSON", e);
			}
			return retorno;
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new PJeBusinessException(e);
		}
	}
	
	/**
	 * Metodo responsavel por recuperar os documentos pelo JSON de sigilosos
	 * 
	 * @param retorno
	 * @param jsonSigilosos
	 * @param ids
	 * @throws JSONException 
	 */
	private void obterDocumentosSigilosos(List<JSONObject> retorno, JSONArray jsonSigilosos, List<Integer> ids) throws JSONException {
		for(int i = 0; i < jsonSigilosos.length(); i++){
			JSONObject obj = (JSONObject) jsonSigilosos.get(i);
			if(!ids.contains(Integer.parseInt(obj.getString("_id")))){
				ids.add(Integer.parseInt(obj.getString("_id")));
				retorno.add(obj);
			}

		}
	}

	/**
	 * Metodo responsavel por recuperar os documentos pelo JSON de publicos
	 * 
	 * @param retorno
	 * @param jsonPublicos
	 * @param ids
	 * @throws JSONException 
	 */
	private void obterDocumentosPublicos(List<JSONObject> retorno, JSONArray jsonPublicos, List<Integer> ids) throws JSONException {
		for(int i = 0; i < jsonPublicos.length(); i++){
			JSONObject obj = (JSONObject) jsonPublicos.get(i);
			ids.add(Integer.parseInt(obj.getString("_id")));
			retorno.add(obj);
		}
	}
	
	/**
	 * Carrega os filtros de pequisa necessários para consultar documentos de decisões indexadas. 
	 * 
	 * @param filtros {@link String} a ser buscado na filtragem.
	 * @return {@link Search} com os critérios de consulta de decisões indexadas.
	 * 
	 * @throws NoSuchFieldException
	 */
	private Search carregarFiltroConsultarDocumentosDecisoesIndexados(ConsultaDocumentoIndexadoVO filtros, boolean sigilosos, boolean assinados) throws NoSuchFieldException{
		Search search = new Search(ProcessoDocumento.class);
		
		if(filtros.getConteudo() != null && !filtros.getConteudo().isEmpty()){
			search.addCriteria(Criteria.contains("processoDocumentoBin.modeloDocumento", filtros.getConteudo()));
		}
		if(filtros.getNomeUsuarioCriacao() != null && !filtros.getNomeUsuarioCriacao().isEmpty()){
			search.addCriteria(Criteria.contains("nomeUsuarioInclusao", filtros.getNomeUsuarioCriacao()));
		}
		if(filtros.getNomeUsuarioSignatario() != null && !filtros.getNomeUsuarioSignatario().isEmpty()){
			search.addCriteria(Criteria.contains("processoDocumentoBin.signatarios.pessoa.nome", filtros.getNomeUsuarioSignatario()));
		}
		if(filtros.getNrProcesso() != null && !filtros.getNrProcesso().isEmpty()){
			search.addCriteria(Criteria.equals("processo.numeroProcesso", filtros.getNrProcesso()));
		}
		if(filtros.getIdsTipoDocumento() != null && !filtros.getIdsTipoDocumento().isEmpty()){
			search.addCriteria(Criteria.in("tipoProcessoDocumento.idTipoProcessoDocumento", filtros.getIdsTipoDocumento().toArray()));
		}
		if(filtros.getTipoDocumento() != null && !filtros.getTipoDocumento().isEmpty()){
			search.addCriteria(Criteria.equals("tipoProcessoDocumento.tipoProcessoDocumento", filtros.getTipoDocumento()));
		}
		
		SimpleDateFormat formatoInicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
		SimpleDateFormat formatoFim = new SimpleDateFormat("yyyy-MM-dd 23:59:59.999999");
		
		if(filtros.getDataCriacaoInicio() != null && filtros.getDataCriacaoFim() != null){
			search.addCriteria(Criteria.between("dataInclusao", formatoInicio.format(filtros.getDataCriacaoInicio()), formatoFim.format(filtros.getDataCriacaoFim())));
		} else if(filtros.getDataCriacaoInicio() != null){
			search.addCriteria(Criteria.greaterOrEquals("dataInclusao", formatoInicio.format(filtros.getDataCriacaoInicio())));
		} else if(filtros.getDataCriacaoFim() != null){
			search.addCriteria(Criteria.lessOrEquals("dataInclusao", formatoFim.format(filtros.getDataCriacaoFim())));
		}
		
		if(filtros.getDataAssinaturaInicio() != null && filtros.getDataAssinaturaFim() != null){
			search.addCriteria(Criteria.between("processoDocumentoBin.dataAssinatura", formatoInicio.format(filtros.getDataAssinaturaInicio()), formatoFim.format(filtros.getDataAssinaturaFim())));
		} else if(filtros.getDataAssinaturaInicio() != null){
			search.addCriteria(Criteria.greaterOrEquals("processoDocumentoBin.dataAssinatura", formatoInicio.format(filtros.getDataAssinaturaInicio())));
		} else if(filtros.getDataAssinaturaFim() != null){
			search.addCriteria(Criteria.lessOrEquals("processoDocumentoBin.dataAssinatura", formatoFim.format(filtros.getDataAssinaturaFim())));
		}
		
		if(filtros.getOrgaoJulgador() != null && !filtros.getOrgaoJulgador().isEmpty()){
			search.addCriteria(Criteria.equals("processoTrf.orgaoJulgador.orgaoJulgador", filtros.getOrgaoJulgador()));
		}

		if(sigilosos) {
			search.addCriteria(Criteria.equals("documentoSigiloso", true));
			if (Authenticator.getIdOrgaoJulgadorAtual() != null) {
				search.addCriteria(
						Criteria.equals("processoTrf.orgaoJulgador.idOrgaoJulgador", Authenticator.getIdOrgaoJulgadorAtual()));

			} else {
				search.addCriteria(Criteria.equals("processoTrf.orgaoJulgador.idOrgaoJulgador", -1));
			}
		}else{
			search.addCriteria(Criteria.equals("documentoSigiloso",false));
		}
		
		if(assinados) {
			search.addCriteria(Criteria.greaterOrEquals("dataJuntada", formatoInicio.format(new Date(0))));
		}
		
		search.addCriteria(Criteria.equals("ativo", true));
		search.addCriteria(Criteria.exists("processoDocumentoBin.signatarios"));
		return search;
	}
	/**
	 * Operação que avalia se a consulta de documentos indexados está habilitada.
	 * 
	 * @return {@link Boolean} para indicar funcionamento da consulta de documentos indexados.
	 */
	public Boolean isConsultaDocumentosIndexadosHabilitada(){
		return getIndexador().isEnabled();
	}
		
	/**
	 * Metodo que ao passar um ID de um ProcessoDocumento ira deletar todos os Registros que tenham vinculo com a tabela
	 * tb_processo_documento.
	 * 
	 * @param idProcessoDocumento id do ProcessoDocumento.
	 */
	public void removerDadosVinculados(Integer idProcessoDocumento){
		SessaoProcessoMultDocsVotoManager sessaoProcessoMultDocsVotoManager = ComponentUtil.getComponent(SessaoProcessoMultDocsVotoManager.class);
		SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager = ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class);
		SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager = ComponentUtil.getComponent(SessaoProcessoDocumentoManager.class);
		ProcessoDocumentoAcordaoManager processoDocumentoAcordaoManager = ComponentUtil.getComponent(ProcessoDocumentoAcordaoManager.class);
		
		sessaoProcessoMultDocsVotoManager.remover(idProcessoDocumento);
		sessaoProcessoDocumentoVotoManager.remover(idProcessoDocumento);
		sessaoProcessoDocumentoManager.remover(idProcessoDocumento);
		processoDocumentoAcordaoManager.remover(idProcessoDocumento);
	}
	
	/**
	 * Fornecido o id de um documento juntado a algum processo, retorna a PA (polo ativo), PP (polo passivo), OU (outros participantes) ou E se for usuário externo ou I se for usuário do tribunal
	 * 
	 * @param idProcessoDocumento
	 * @return
	 */
	public TipoOrigemAcaoEnum obterOrigemUsuarioJuntada(Integer idProcessoDocumento) {
		TipoOrigemAcaoEnum retorno = null;
		try {
			if (idProcessoDocumento != null) {
				retorno = obterTipoOrigemJuntada(this.getProcessoDocumentoManager().findById(idProcessoDocumento));
			}
		} catch (PJeBusinessException e) {
			// nothing to do
		}
		return retorno;
	}
	
	public TipoOrigemAcaoEnum obterTipoOrigemJuntada(ProcessoDocumento procDoc) {
		TipoOrigemAcaoEnum retorno = null;
		if(procDoc == null || procDoc.getDataJuntada() == null) {
			return null;
		}
		if(procDoc.getInTipoOrigemJuntada() != null) {
			retorno = procDoc.getInTipoOrigemJuntada();
		}else {
			if(procDoc.getLocalizacaoJuntada() != null && !procDoc.getLocalizacaoJuntada().trim().isEmpty()) {
				if(procDoc.getLocalizacaoJuntada().startsWith(DESCRICAO_POLO_JUNTADA.ATIVO)) {
					retorno = TipoOrigemAcaoEnum.PA;
				}else if(procDoc.getLocalizacaoJuntada().startsWith(DESCRICAO_POLO_JUNTADA.PASSIVO)) {
					retorno = TipoOrigemAcaoEnum.PP;
				}else if(procDoc.getLocalizacaoJuntada().startsWith(DESCRICAO_POLO_JUNTADA.OUTROS_INTERESSADOS)) {
					retorno = TipoOrigemAcaoEnum.OU;
				}
			}
			if(retorno == null) {
				if(procDoc.getPapel() != null){
					if(Authenticator.isUsuarioExterno(procDoc.getPapel())) {
						retorno = TipoOrigemAcaoEnum.E;
					}else {
						retorno = TipoOrigemAcaoEnum.I;
					}
				}
				if(retorno != TipoOrigemAcaoEnum.I) {
					Usuario usuarioPesquisa = null;
					if(procDoc.getUsuarioJuntada() != null) {
						usuarioPesquisa = procDoc.getUsuarioJuntada();
					}else if(procDoc.getUsuarioAlteracao() != null) {
						usuarioPesquisa = procDoc.getUsuarioAlteracao();
					}else if(procDoc.getUsuarioInclusao() != null) {
						usuarioPesquisa = procDoc.getUsuarioInclusao();
					}
					if(usuarioPesquisa != null) {
						ProcessoTrf processoTrf = procDoc.getProcessoTrf();
						if(processoTrf == null && procDoc.getProcesso() != null) {
							processoTrf = ProcessoTrfManager.instance().getProcessoTrfByProcesso(procDoc.getProcesso());
						}
						retorno = this.identificaTipoOrigem(processoTrf, usuarioPesquisa, procDoc.getPapel(), procDoc.getLocalizacao());					
					}
				}
			}
			if(retorno == null) {
				retorno = TipoOrigemAcaoEnum.I;
			}
		}
		return retorno;
	}
	
	/**
	 * Busca o tipo de origem do usuário dados processo, usuário, papel, localizacao
	 * 
	 * @param processoTrf
	 * @param usuario
	 * @param papel
	 * @param localizacao
	 * @return
	 */
	private TipoOrigemAcaoEnum identificaTipoOrigem(ProcessoTrf processoTrf, Usuario usuario, Papel papel, Localizacao localizacao) {
		TipoOrigemAcaoEnum retorno = null;
		Pessoa pessoaJuntada = null;
		try {
			pessoaJuntada = getPessoaService().findById(usuario.getIdUsuario());
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		if(pessoaJuntada != null && (retorno == null || retorno == TipoOrigemAcaoEnum.E)) {
			boolean verificaSeProcurador = Boolean.TRUE;
			if(papel != null) {
				if(!Authenticator.isProcurador(papel)) {
					verificaSeProcurador = Boolean.FALSE;
				}
			}else if(localizacao != null){
				if(ComponentUtil.getComponent(ProcuradoriaManager.class).recuperaPorLocalizacao(localizacao) == null) {
					verificaSeProcurador = Boolean.FALSE;
				}							
			}
			
			switch(ComponentUtil.getComponent(ProcessoParteManager.class).identificaParticipacaoPessoa(processoTrf, pessoaJuntada, verificaSeProcurador)) {
			case A:
				retorno = TipoOrigemAcaoEnum.PA;
				break;
			case P:
				retorno = TipoOrigemAcaoEnum.PP;
				break;
			case T:
				retorno = TipoOrigemAcaoEnum.OU;
				break;
			default:
				retorno = TipoOrigemAcaoEnum.E;
				break;
			}
		}
		return retorno;
	}
	
	private ProcessoDocumentoBinManager getProcessoDocumentoBinManager() {
		return ComponentUtil.getComponent(ProcessoDocumentoBinManager.class);
	}
	
	private ProcessoDocumentoManager getProcessoDocumentoManager() {
		return ComponentUtil.getComponent(ProcessoDocumentoManager.class);
	}
	
	private ModeloDocumentoManager getModeloDocumentoManager() {
		return ComponentUtil.getComponent(ModeloDocumentoManager.class);
	}
	
	private TipoProcessoDocumentoManager getTipoProcessoDocumentoManager() {
		return ComponentUtil.getComponent(TipoProcessoDocumentoManager.class);
	}
	
	private PessoaService getPessoaService() {
		return ComponentUtil.getComponent(PessoaService.class);
	}
	
	private UsuarioService getUsuarioService() {
		return ComponentUtil.getComponent(UsuarioService.class);
	}
	
	private ProcessoJudicialService getProcessoJudicialService() {
		return ComponentUtil.getComponent(ProcessoJudicialService.class);
	}
	
	private ModeloDocumentoLocalManager getModeloDocumentoLocalManager() {
		return ComponentUtil.getComponent(ModeloDocumentoLocalManager.class);
	}
	
	private ProcessoDocumentoDAO getProcessoDocumentoDAO() {
		return ComponentUtil.getComponent(ProcessoDocumentoDAO.class);
	}
	
	private ProcessoDocumentoLidoManager getProcessoDocumentoLidoManager() {
		return ComponentUtil.getComponent(ProcessoDocumentoLidoManager.class);
	}
	
	private LocalizacaoService getLocalizacaoService() {
		return ComponentUtil.getComponent(LocalizacaoService.class, true);
	}
	
	private Indexador getIndexador() {
		return ComponentUtil.getComponent(Indexador.class);
	}

	/**
	 * Recupera o último documento cadastrado do tipo informado como parâmetro
	 * 
	 * @param tipoProcessoDocumento Tipo do Documento
	 * @param processo Processo
	 * @return Documento cadastrado
	 */
	public ProcessoDocumento recuperarUltimoProcessoDocumento(
			TipoProcessoDocumento tipoProcessoDocumento, Processo processo) {
		return getProcessoDocumentoDAO().getUltimoProcessoDocumentoByProcessoTipoProcessoDocumento(tipoProcessoDocumento, processo);
	}

    /**
     * Recupera o <FA>ltimo documento cadastrado do tipo informado como par<E2>metro
     * 
     * @param tipoProcessoDocumento Tipo do Documento
     * @param processo Processo
     * @return Documento cadastrado
     */
    public ProcessoDocumento recuperarUltimoProcessoDocumentoNaoJulgado(Integer idProcesso, Integer idTipoProcessoDocumento) {
        return getProcessoDocumentoDAO().getUltimoDocumentoPorTipoNaoJulgado(idProcesso, idTipoProcessoDocumento);
    }
	
	/**
	 * Recupera os documentos do tipo Relatório, Ementa e Voto Relator para o processo e sessão passados como parâmetro
	 * 
	 * @param sessao
	 * @param processoTrf
	 * @return
	 */
	public Map<TipoProcessoDocumento, SessaoProcessoDocumento> recuperaDocumentosComSessaoJulgamento(Sessao sessao, ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador) {
		Map<TipoProcessoDocumento, SessaoProcessoDocumento> hashDocumentos = new HashMap<>();

		SessaoProcessoDocumentoDAO sessaoProcessoDocumentoDAO = ComponentUtil.getSessaoProcessoDocumentoDAO();
		SessaoPautaProcessoTrfDAO sessaoPautaProcessoTrfDAO = ComponentUtil.getSessaoPautaProcessoTrfDAO();
		
		Date ultimaDataJulgamento = sessaoPautaProcessoTrfDAO.obterUltimaDataSessaoJulgamentoProcesso(processoTrf);		

		// Relatorio
		SessaoProcessoDocumento spdRelatorio = sessaoProcessoDocumentoDAO.recuperarRelatorioAtivoPorSessaoEhProcesso(sessao, processoTrf, orgaoJulgador);
		if (spdRelatorio == null) {
			// Recupera o documento de uma sessão prévia e replica-o, em caso de existência, para a sessão atual
			spdRelatorio = sessaoProcessoDocumentoDAO.recuperarRelatorioAtivoPorProcessoSessaoAnterior(ultimaDataJulgamento, processoTrf, orgaoJulgador);
			if ((sessao!=null) && (spdRelatorio != null)) {
				spdRelatorio.setSessao(sessao);
				sessaoProcessoDocumentoDAO.persist(spdRelatorio);
				sessaoProcessoDocumentoDAO.flush();
			}
			if (spdRelatorio == null) {
				TipoProcessoDocumento tipoDocRelatorio = ParametroUtil.instance().getTipoProcessoDocumentoRelatorio();
				ProcessoDocumento procDocRelatorio = criarProcessoDocumentoVazio(tipoDocRelatorio, processoTrf);
				spdRelatorio = new SessaoProcessoDocumento();
				spdRelatorio.setProcessoDocumento(procDocRelatorio);
				spdRelatorio.setSessao(sessao);
				spdRelatorio.setOrgaoJulgador(orgaoJulgador);
			}
		}
		hashDocumentos.put(ParametroUtil.instance().getTipoProcessoDocumentoRelatorio(), spdRelatorio);

		// Ementa
		SessaoProcessoDocumento spdEmenta = sessaoProcessoDocumentoDAO.recuperarEmentaAtivaPorSessaoEhProcessoEhOrgaoJulgador(sessao, processoTrf, orgaoJulgador);
		if (spdEmenta == null) {
			// Recupera o documento de uma sessão prévia e replica-o, em caso de existência, para a sessão atual
			spdEmenta = sessaoProcessoDocumentoDAO.recuperarEmentaAtivaPorProcessoSessaoAnterior(ultimaDataJulgamento, processoTrf, orgaoJulgador);
			if ((sessao!=null) && (spdEmenta != null)) {
				spdEmenta.setSessao(sessao);
				sessaoProcessoDocumentoDAO.persist(spdEmenta);
				sessaoProcessoDocumentoDAO.flush();
			}
			if (spdEmenta == null) {
				TipoProcessoDocumento tipoDocEmenta = ParametroUtil.instance().getTipoProcessoDocumentoEmenta();
				ProcessoDocumento pdEmenta = criarProcessoDocumentoVazio(tipoDocEmenta, processoTrf);
				spdEmenta = new SessaoProcessoDocumento();
				spdEmenta.setProcessoDocumento(pdEmenta);
				spdEmenta.setSessao(sessao);
				spdEmenta.setOrgaoJulgador(orgaoJulgador);
			}
		}
		hashDocumentos.put(ParametroUtil.instance().getTipoProcessoDocumentoEmenta(), spdEmenta);

		// Voto Relator
		SessaoProcessoDocumentoVotoDAO sessaoProcessoDocumentoVotoDAO = ComponentUtil.getSessaoProcessoDocumentoVotoDAO();
		SessaoProcessoDocumentoVoto spdVoto = sessaoProcessoDocumentoVotoDAO.recuperarVoto(sessao, processoTrf, orgaoJulgador);
		if (spdVoto==null) {
			spdVoto = sessaoProcessoDocumentoVotoDAO.recuperarVoto(ultimaDataJulgamento, processoTrf, orgaoJulgador);
			if (spdVoto!=null) {
				//...verificar se a sessão é diferente. Em caso positivo, atualiza as informações do documento para a sessão atual
				if ((spdVoto.getSessao()==null) || (spdVoto.getSessao().getIdSessao() != sessao.getIdSessao())) {
					spdVoto.setSessao(sessao);
					sessaoProcessoDocumentoVotoDAO.persist(spdVoto);
					sessaoProcessoDocumentoVotoDAO.flush();
				}
			} else {
				TipoProcessoDocumento tipoDocVoto = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
				ProcessoDocumento pdVoto = criarProcessoDocumentoVazio(tipoDocVoto, processoTrf);
				spdVoto = new SessaoProcessoDocumentoVoto();
				spdVoto.setProcessoDocumento(pdVoto);
				spdVoto.setOrgaoJulgador(processoTrf.getOrgaoJulgador());
				spdVoto.setOjAcompanhado(processoTrf.getOrgaoJulgador());
				spdVoto.setProcessoTrf(processoTrf);
			}
		}
		hashDocumentos.put(ParametroUtil.instance().getTipoProcessoDocumentoVoto(), spdVoto);

		return hashDocumentos;
	}

	/**
	 * Recupera os documentos do tipo Relatório, Ementa e Voto Relator para o processo onde este não esteja em sessão
	 * 
	 * @param acordaoCompilacao
	 * @return
	 */
	public Map<TipoProcessoDocumento, SessaoProcessoDocumento> recuperaDocumentosSemSessaoJulgamento(ProcessoTrf processoTrf) {
		Map<TipoProcessoDocumento, SessaoProcessoDocumento> hashDocumentos = new HashMap<>();

		// Recupera Relatório, Ementa e Voto Relator que foram cadastradas antes do julgamento acontecer...
		try {
			TipoProcessoDocumento tipoDocRelatorio = ParametroUtil.instance().getTipoProcessoDocumentoRelatorio();
			TipoProcessoDocumento tipoDocEmenta = ParametroUtil.instance().getTipoProcessoDocumentoEmenta();

			Integer[] tiposDocumento = new Integer[] {tipoDocRelatorio.getIdTipoProcessoDocumento(), tipoDocEmenta.getIdTipoProcessoDocumento()};

			List<ProcessoDocumento> listProcessoDocumento = getDocumentosPorTipos(processoTrf, tiposDocumento);
			for (ProcessoDocumento procDoc : listProcessoDocumento) {
				// Verifica se foi cadastrado antecipadamente à sessão de julgamento
				SessaoProcessoDocumento documentoAntecipado = ComponentUtil.getComponent(SessaoProcessoDocumentoManager.class)
						.recuperaPorProcessoDocumentoSePossivelSemSecao(procDoc);
				if (procDoc.getTipoProcessoDocumento().equals(tipoDocEmenta)) {
					SessaoProcessoDocumento spdEmentaRelatorAcordao = new SessaoProcessoDocumento();
					spdEmentaRelatorAcordao.setProcessoDocumento(procDoc);
					if (documentoAntecipado != null) {
						spdEmentaRelatorAcordao.setIdSessaoProcessoDocumento(documentoAntecipado.getIdSessaoProcessoDocumento());
						spdEmentaRelatorAcordao.setLiberacao(documentoAntecipado.getLiberacao());
					}

					hashDocumentos.put(ParametroUtil.instance().getTipoProcessoDocumentoEmenta(), spdEmentaRelatorAcordao);

				} else if (procDoc.getTipoProcessoDocumento().equals(tipoDocRelatorio)) {
					SessaoProcessoDocumento spdRelatorio = new SessaoProcessoDocumento();
					spdRelatorio.setProcessoDocumento(procDoc);
					if (documentoAntecipado != null) {
						spdRelatorio.setIdSessaoProcessoDocumento(documentoAntecipado.getIdSessaoProcessoDocumento());
						spdRelatorio.setLiberacao(documentoAntecipado.getLiberacao());
					}

					hashDocumentos.put(ParametroUtil.instance().getTipoProcessoDocumentoRelatorio(), spdRelatorio);
				}
			}

			// Recupera os documentos do tipo Voto Relator
			TipoProcessoDocumento tipoDocVoto = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
			listProcessoDocumento = getDocumentosPorTipos(processoTrf, tipoDocVoto.getIdTipoProcessoDocumento());
			if (!CollectionUtilsPje.isEmpty(listProcessoDocumento)) {
				// Verifica se foi cadastrado antecipadamente à sessão de julgamento
				ProcessoDocumento documentoMaisRecente = listProcessoDocumento.get(listProcessoDocumento.size()-1);
				SessaoProcessoDocumento votoAnterior = ComponentUtil.getComponent(SessaoProcessoDocumentoManager.class)
						.recuperaPorProcessoDocumentoSePossivelSemSecao(documentoMaisRecente);
				
				SessaoProcessoDocumentoVoto spdVoto = new SessaoProcessoDocumentoVoto();
				spdVoto.setProcessoDocumento(documentoMaisRecente);
				spdVoto.setOrgaoJulgador(processoTrf.getOrgaoJulgador());
				if (votoAnterior != null) {
					if (!(votoAnterior instanceof SessaoProcessoDocumentoVoto)) 
						throw new PJeRuntimeException("Inconsistência entre a regra de negócio e a base de dados. Não foi possível recuperar o SessaoProcessoDocumentoVoto relativo ao seu respectivo SessaoProcessoDocumento: " + votoAnterior);
					
					if (votoAnterior.getSessao()==null)
						spdVoto.setIdSessaoProcessoDocumento(votoAnterior.getIdSessaoProcessoDocumento());
					spdVoto.setLiberacao(votoAnterior.getLiberacao());
					
					SessaoProcessoDocumentoVoto votoExistente = (SessaoProcessoDocumentoVoto)votoAnterior;
					spdVoto.setCheckAcompanhaRelator(votoExistente.getCheckAcompanhaRelator());
					spdVoto.setDestaqueSessao(votoExistente.getDestaqueSessao());
					spdVoto.setDtVoto(votoExistente.getDtVoto());
					spdVoto.setImpedimentoSuspeicao(votoExistente.getImpedimentoSuspeicao());
					spdVoto.setTextoProclamacaoJulgamento(votoExistente.getTextoProclamacaoJulgamento());
					spdVoto.setTipoVoto(votoExistente.getTipoVoto());
					spdVoto.setOjAcompanhado(votoExistente.getOjAcompanhado());
					spdVoto.setProcessoTrf(processoTrf);
				}
				hashDocumentos.put(ParametroUtil.instance().getTipoProcessoDocumentoVoto(), spdVoto);
			}
		} catch (PJeBusinessException e) {
			logger.error("Não foi possível recuperar os documentos vinculados ao Processo: {0}", e.getLocalizedMessage());
		}

		return hashDocumentos;
	}

	private ProcessoDocumento criarProcessoDocumentoVazio(TipoProcessoDocumento tipoDocVoto, ProcessoTrf processoTrf) {
		ProcessoDocumento procDocVoto = new ProcessoDocumento();
		procDocVoto.setTipoProcessoDocumento(tipoDocVoto);

		ProcessoDocumentoBin documentoBin = new ProcessoDocumentoBin();
		documentoBin.setModeloDocumento("");
		procDocVoto.setProcessoDocumentoBin(documentoBin);
		procDocVoto.setProcessoTrf(processoTrf);
		return procDocVoto;
	}
	
    /**
     * Recupera todos documentos do processo por um mapa de parametros
     * @param idProcesso (opcional)
     * @param processoDocumentoVO
     * @return PaginatedDataModel<ProcessoDocumento>
     */
    public PaginatedDataModel<ProcessoDocumento> recuperarDocumentosParametros(Integer idProcesso, ProcessoDocumentoVO processoDocumentoVO) {
        return getProcessoDocumentoDAO().recuperarDocumentosParametros(idProcesso, processoDocumentoVO);
    }
        
    /**
     * Realiza a associacao de documento Acordao a sessao de julgamento.
     * Se nao conseguir recuperar o processoTrf o metodo lancará RuntimeException.
     * Se atraves do processo nao for possivel recuperar a sessao de julgamento, as associacoes nao sao realizadas.
     * @throws PJeBusinessException 
     */
	public void associarDocumentoAcordaoASessaoViaFluxo() throws PJeBusinessException{
		    	    
		AcordaoCompilacao ac = null;
		SessaoPautaProcessoTrf sppt = null;
		
		ProcessoTrf processoTrf = ProcessoJbpmUtil.getProcessoTrf();
		
		if(processoTrf != null){
			sppt = ComponentUtil.getSessaoPautaProcessoTrfManager().getSessaoPautaProcessoTrfJulgado(processoTrf);
			TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(processoTrf.getIdProcessoTrf());
					
			if(sppt != null){		
				ac = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperarAcordaoCompilacao(sppt, taskInstance);
					
				associarDocumentoAcordaoASessao(ac);
				associarDocumentosAoDocumentoAcordao(ac);
			} else{
				logger.warn("documentoJudicialService.associarDocumentoAcordaoASessaoViaFluxo nao foi realizada pois nao ha sessao para o processo "+processoTrf.getIdProcessoTrf()+"-"+processoTrf.getNumeroProcesso());
			}
			
		}else{
			logger.error("Nao foi possivel recuperar o processo em documentoJudicialService.associarDocumentoAcordaoASessaoViaFluxo");
			throw new RuntimeException("Nao foi possivel recuperar o processo em documentoJudicialService.associarDocumentoAcordaoASessaoViaFluxo");
		}
		
	}
    
	/**
	 * Faz a associacao de documentos vinculados ao acordao a ele mesmo.
	 * @param acordaoCompilacao
	 */
    public void associarDocumentosAoDocumentoAcordao(AcordaoCompilacao acordaoCompilacao){
		List<ProcessoDocumento> processoDocumentos = acordaoCompilacao.getProcessoDocumentosParaAssinatura();
		ProcessoDocumento pdAcordao = acordaoCompilacao.getAcordao();                                

		//Ementa
		if (acordaoCompilacao.getEmentaRelatorDoAcordao() != null && Util.isDocumentoPreenchido(acordaoCompilacao.getEmentaRelatorDoAcordao().getProcessoDocumento())) {
			processoDocumentos.add(acordaoCompilacao.getEmentaRelatorDoAcordao().getProcessoDocumento());
		}

		// Voto                
		if (acordaoCompilacao.getVotoVencedor() != null && Util.isDocumentoPreenchido(acordaoCompilacao.getVotoVencedor().getProcessoDocumento())) {
			processoDocumentos.add(acordaoCompilacao.getVotoVencedor().getProcessoDocumento());
		}

		// Relatorio
		// Se o relatorio nao estiver assinado assinar ele
		if (acordaoCompilacao.getRelatorio() != null && Util.isDocumentoPreenchido(acordaoCompilacao.getRelatorio().getProcessoDocumento())) {
			processoDocumentos.add(acordaoCompilacao.getRelatorio().getProcessoDocumento());
		}

		// Notas orais
		if (acordaoCompilacao.getNotasOrais() != null && Util.isDocumentoPreenchido(acordaoCompilacao.getNotasOrais().getProcessoDocumento())) {
			processoDocumentos.add(acordaoCompilacao.getNotasOrais().getProcessoDocumento());
		}
			
		try{			
						
			ProcessoDocumentoManager processoDocumentoManager = ComponentUtil.getComponent(ProcessoDocumentoManager.class);
			for(ProcessoDocumento pd: processoDocumentos){
	   			if(	pd != null
	   				&& ! (pd.getIdProcessoDocumento() == pdAcordao.getIdProcessoDocumento())){
	   				
	   				if(pd.getDocumentoPrincipal() == null || !(pd.getDocumentoPrincipal().getIdProcessoDocumento() == pdAcordao.getIdProcessoDocumento()) ){
	   					if((!pd.getTipoProcessoDocumento().equals(ParametroUtil.instance().getTipoProcessoDocumentoEmenta()) || 
	   							(pd.getTipoProcessoDocumento().equals(ParametroUtil.instance().getTipoProcessoDocumentoEmenta()) && acordaoCompilacao.getSessaoPautaProcessoTrf() != null 
	   							&& acordaoCompilacao.getSessaoPautaProcessoTrf().getOrgaoJulgadorVencedor() != null && 
	   							acordaoCompilacao.getEmentaRelatorDoAcordao().getOrgaoJulgador().equals(acordaoCompilacao.getSessaoPautaProcessoTrf().getOrgaoJulgadorVencedor())))) {
	   						pd.setDocumentoPrincipal(pdAcordao);
	   						processoDocumentoManager.persist(pd);
	   					}
	   				}
	   				
	   			}
			}
			
			processoDocumentoManager.flush();
			
		}catch(PJeBusinessException ex){
			logger.error(ex.getMessage());
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		
		associarVotoMagistrado(acordaoCompilacao, pdAcordao);
	}

	private void associarVotoMagistrado(AcordaoCompilacao acordaoCompilacao, ProcessoDocumento pdAcordao) {
		// Voto do Magistrado (Relator)             
		if (acordaoCompilacao.getVotoRelatorDoProcesso() != null && Util.isDocumentoPreenchido(acordaoCompilacao.getVotoRelatorDoProcesso().getProcessoDocumento())) {
			try {			
				ProcessoDocumento pd = acordaoCompilacao.getVotoRelatorDoProcesso().getProcessoDocumento();
	   			if(	pd != null
	   				&& ! (pd.getIdProcessoDocumento() == pdAcordao.getIdProcessoDocumento())){
	   				
	   				if(pd.getDocumentoPrincipal() == null
	   				   || !( pd.getDocumentoPrincipal().getIdProcessoDocumento() == pdAcordao.getIdProcessoDocumento()) ){
	   					pd.setDocumentoPrincipal(pdAcordao);
	   					ComponentUtil.getComponent(ProcessoDocumentoManager.class).persist(pd);	   					
	   					ComponentUtil.getComponent(ProcessoDocumentoManager.class).flush();
	   				}
	   			}
			} catch(PJeBusinessException ex) {
				logger.error(ex.getMessage());
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
		}
	}
	
    /**
     * Associa o documento acordao a sessao caso ele ainda nao esteja associado.
     * @param acordaoCompilacao
     */
	public void associarDocumentoAcordaoASessao(AcordaoCompilacao acordaoCompilacao){
		try{
			
			ProcessoDocumento pdAcordao = acordaoCompilacao.getAcordao();
			
			if(ComponentUtil.getSessaoProcessoDocumentoManager().recuperaPorProcessoDocumento(pdAcordao) == null ) {
				SessaoProcessoDocumento spdAcordao = new SessaoProcessoDocumento();
		   		spdAcordao.setProcessoDocumento(pdAcordao);
		   		spdAcordao.setLiberacao(true);
		   		spdAcordao.setSessao(acordaoCompilacao.getSessaoPautaProcessoTrf().getSessao());
		   		spdAcordao.setOrgaoJulgador(acordaoCompilacao.getSessaoPautaProcessoTrf().getOrgaoJulgadorVencedor());
		   		getSessaoProcessoDocumentoManager().persistAndFlush(spdAcordao);
			}
			
		}catch(PJeBusinessException ex){
			logger.error(ex.getMessage());
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}			
	}
	
	/**
	 * Dever mostrar que o documento nao foi lido se:
	 * - o documento nao estiver lido
	 * - E o documento tenha sido juntado ao processo depois da autuao do processo
	 * --- E
	 * --- a localizao do usuario for superior hierarquicamente a localizao do OJ do processo
	 * --- OU se for um processo de colegiado e o usuario estiver lotado exatamente no mesmo colegiado
	 * @param processoDocumento
	 * @param processoTrf
	 * @return
	 */
	public boolean mostrarDocumentoComoNaoLido(ProcessoDocumento processoDocumento, ProcessoTrf processoTrf) {
		Integer idColegiadoProcesso = processoTrf.getOrgaoJulgadorColegiado() != null ? processoTrf.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado() : null;
		Integer idColegiadousuario = Authenticator.getIdOrgaoJulgadorColegiadoAtual();

		return processoDocumento != null && processoTrf != null && !processoDocumento.getLido() &&
			processoDocumento.getDataInclusao().after(processoTrf.getDataAutuacao()) &&
			this.papelManager.isPapelDocumentoNaoLido(processoDocumento.getPapel()) && 
			
			(Authenticator.isServidorExclusivoColegiado() || Authenticator.getLocalizacoesFilhasAtuais().contains(processoTrf.getOrgaoJulgador().getLocalizacao())) &&
			(idColegiadousuario == null || idColegiadousuario.equals(idColegiadoProcesso));
	}
	
	public boolean podeExibirPublicamente(ProcessoDocumento pd, boolean comDataJuntada) {
		boolean retorno = false;
		if ((pd != null && pd.getProcessoDocumentoBin() != null && Boolean.FALSE.equals(pd.getDocumentoSigiloso()) ) && (!comDataJuntada || pd.getDataJuntada() != null)) {
			 retorno = true;
		}
		return retorno;
	}
	
	public String recuperarConteudoBase64(ProcessoDocumento pd) {
		String conteudoHtml = pd.getProcessoDocumentoBin().getModeloDocumento();
		String conteudoSemTagHtml = StringUtil.removeHtmlTags(conteudoHtml);
		
		String encodedText = Base64.getUrlEncoder().encodeToString(conteudoSemTagHtml.getBytes());
		return encodedText;
	}

	/**
	 * Retorna Copia Processo Documento
	 * @param processoDocumentoOrigem
	 * @return ProcessoDocumento
	 */
	public ProcessoDocumento retornaCopiaProcessoDocumento(ProcessoDocumento processoDocumentoOrigem){
		ProcessoDocumento processoDocumentoRetorno = null;
		try {
			processoDocumentoRetorno = EntityUtil.cloneEntity(processoDocumentoOrigem, false);
		} catch (InstantiationException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		}
		return processoDocumentoRetorno;
	}
	
	/**
	 * Método responsável por verificar se o ProcessoDocumento especificado está juntado ao processo
	 * @param idProcessoDocumento Código identificador do ProcessoDocumento
	 * @return Retorna "true" caso o documento esteja juntado ao processo e "false" caso contrário
	 */
	public boolean isDocumentoJuntado(int idProcessoDocumento) {
		return getProcessoDocumentoDAO().isDocumentoJuntado(idProcessoDocumento);
	}
	
	public List<TipoProcessoDocumento> getTiposDocumentosMinutaMiniPac(Papel papel){
		List<Integer> idsList = new ArrayList<>(0);
		idsList.add(ParametroUtil.instance().getTipoProcessoDocumentoCitacao().getIdTipoProcessoDocumento());
		idsList.add(ParametroUtil.instance().getTipoProcessoDocumentoIntimacao().getIdTipoProcessoDocumento());
		idsList.add(ParametroUtil.instance().getTipoProcessoDocumentoNotificacao().getIdTipoProcessoDocumento());
		
		return getTipoProcessoDocumentoManager().findDisponiveis(papel, idsList.toArray(new Integer[]{}));
	}
	
	public List<TipoProcessoDocumento> getTiposDocumentoMinutaMiniPac() {
		Papel papel = Authenticator.getPapelAtual();
		return this.getTiposDocumentosMinutaMiniPac(papel);
	}
	/**
	 * Mtodo responsvel por gerar um QrCode para consultar documentos do processo a partir do tipo de documento
	 * @param processoJudicial Dados do processo
	 * @param idTipoProcessoDocumento Cdigo do tipo de documento
	 * @param exibirApenasUltimoDocumento Informa se o sistema deve exibir apenas o ltimo documento
	 * @return
	 */
	public String gerarQrCodeParaConsultarDocumentos(ProcessoTrf processoJudicial, int idTipoProcessoDocumento, boolean exibirApenasUltimoDocumento) {
		try {
			String parametros = 
					String.valueOf(processoJudicial.getIdProcessoTrf()) + "&" + 
					String.valueOf(idTipoProcessoDocumento) + "&" +
					String.valueOf(exibirApenasUltimoDocumento);
			parametros = EncryptionSecurity.encrypt(parametros);
			parametros = Base64.getEncoder().encodeToString(parametros.getBytes());
			StringBuilder url = new StringBuilder();
			url.append(new br.com.itx.component.Util().getUrlProject() + "/Processo/ConsultaDocumento/listView.seam");
			url.append("?y=" + parametros);
			
			return QrCodeUtil.gerarQrCodeBase64(url.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String gerarQrCodeParaConsultarUltimoAtoProferido(ProcessoTrf processoJudicial, String tipos) {
		try {
		
			List<Integer> idsTiposDoc = Arrays.stream(tipos.split(",")).map(Integer::parseInt).collect(Collectors.toList());
			
			ProcessoDocumento ultimoAtoProferido = ProcessoDocumentoManager.instance().getUltimoProcessoDocumento(
					ComponentUtil.getTipoProcessoDocumentoManager().findTiposIn(idsTiposDoc), processoJudicial.getProcesso());
			
			StringBuilder url = new StringBuilder();
			url.append(new br.com.itx.component.Util().getUrlProject() + "/Processo/ConsultaDocumento/listView.seam");
			
				
			url.append("?x=" + ValidacaoAssinaturaProcessoDocumento.instance().getCodigoValidacaoDocumento(ultimoAtoProferido.getProcessoDocumentoBin()));
			
			return QrCodeUtil.gerarQrCodeBase64(url.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public String gerarQrCodeParaConsultarUltimoDocumento(ProcessoTrf processoJudicial, Integer idPeticaoInicial) {
		try {
		
			
			ProcessoDocumento pd = ProcessoDocumentoManager.instance().getUltimoProcessoDocumento(
					ComponentUtil.getTipoProcessoDocumentoManager().findById(idPeticaoInicial),
					processoJudicial.getProcesso());
			
			StringBuilder url = new StringBuilder();
			url.append(new br.com.itx.component.Util().getUrlProject() + "/Processo/ConsultaDocumento/listView.seam");
			
				
			url.append("?x=" + ValidacaoAssinaturaProcessoDocumento.instance().getCodigoValidacaoDocumento(pd.getProcessoDocumentoBin()));
			
			return QrCodeUtil.gerarQrCodeBase64(url.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
