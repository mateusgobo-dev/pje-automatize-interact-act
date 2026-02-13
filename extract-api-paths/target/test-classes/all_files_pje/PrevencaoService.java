







package br.jus.cnj.pje.servicos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.jus.cnj.pje.business.dao.ElasticDAO;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.util.Version;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;

import com.google.common.collect.Iterables;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.PJEBrazilianAnalyzer;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.dao.PrevencaoDAO;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.business.dao.AssuntoTrfDAO;
import br.jus.cnj.pje.business.dao.PessoaDocumentoIdentificacaoDAO;
import br.jus.cnj.pje.business.dao.ProcessoTrfConexaoDAO;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.VerificadorPrevencaoExterna;
import br.jus.cnj.pje.extensao.auxiliar.ProcessoConexao;
import br.jus.cnj.pje.extensao.auxiliar.ProcessoOrigem;
import br.jus.cnj.pje.extensao.auxiliar.ProcessoParteDocumento;
import br.jus.cnj.pje.extensao.auxiliar.ProcessoParteNome;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.PrevencaoException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.pje.indexacao.Indexador;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.AgrupamentoClasseJudicial;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ClasseJudicialAgrupamento;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.ItemsLog;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.entidades.ProcessoTrfLogPrevencao;
import br.jus.pje.nucleo.entidades.ProcessoTrfPreventoLog;
import br.jus.pje.nucleo.enums.CriticidadeEnum;
import br.jus.pje.nucleo.enums.PrevencaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.SegredoEntreProcessosJudiciaisEnum;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;
import br.jus.pje.nucleo.enums.TipoSolicitacaoPrevencaoEnum;
import br.jus.pje.nucleo.search.bridge.NumeroDocumentoBridge;
import org.json.JSONArray;
import org.json.JSONObject;

@Name(PrevencaoService.NAME)
@Scope(ScopeType.EVENT)
public class PrevencaoService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public final static String NAME = "prevencaoService";
	private ProcessoTrfLogPrevencao logPrev = new ProcessoTrfLogPrevencao();
	private static Object lock = new Object();
	
	@Logger
	private Log logger;
	
	@In(required=false, create=true)
	private VerificadorPrevencaoExterna verificadorPrevencaoExterna;
	
	@In(create=true)
	private ParametroService parametroService;
	
	@In(create=true)
	private FluxoManager fluxoManager;

	@In
	private Indexador indexador;

	@In(create=true)
	private ProcessoJudicialService processoJudicialService;
	
	@In(create=true)
	private ProcessoTrfConexaoDAO processoTrfConexaoDAO;	
	
	@In(create=true)
	private PessoaDocumentoIdentificacaoDAO pessoaDocumentoIdentificacaoDAO;
	
	public static PrevencaoService instance() {
		return ComponentUtil.getComponent(PrevencaoService.NAME);
	}
	
	public void limparService(){
	 	logPrev = new ProcessoTrfLogPrevencao();
	 }

	public void verificarPrevencao(List<ProcessoTrf> processos, TipoSolicitacaoPrevencaoEnum tipo) throws Exception {
		for (ProcessoTrf p : processos) {
			if (p.getDataDistribuicao() != null) {
				logPrev = new ProcessoTrfLogPrevencao();
				verificarPrevencao(p, tipo);
			}
		}
	}

	public void verificarPrevencao(ProcessoTrf processoTrf) throws PontoExtensaoException, PrevencaoException {
		verificarPrevencao(processoTrf, TipoSolicitacaoPrevencaoEnum.D);
		if(verificadorPrevencaoExterna != null){
			try{
				verificarPrevencaoExterna(processoTrf);
			}catch (PontoExtensaoException e) {
				logger.error("Erro ao tentar verificar a prevenção por meio do conector [{0}].", verificadorPrevencaoExterna.getClass());
				throw e;
			}
		}else{
			logger.warn("Não há verificador de prevenção externa disponível.");
		}
	}
	
	/**
	 * Funcionalidade criada para desconsiderar prevenção para documentos (CNPJ ou CPF) cadastrados no parâmetro de lista branca de nao preventos.
	 * @param processoTrf - Objeto a ser analisado com 
	 * @return
	 */
	private boolean verificarIsencaoDePrevencao(ProcessoTrf processoTrf) {
		String whiteList = parametroService.valueOf(Parametros.PJE_WHITE_LIST_PREVENCAO);
	    if (whiteList != null && !whiteList.isEmpty()) { //Verifica somenta caso tenha dados cadastrados no parâmetro.
	    	List<Pessoa> pessoasPolos = new ArrayList<>(processoTrf.getPessoaPoloAtivoList());
	    	pessoasPolos.addAll(processoTrf.getPessoaPoloPassivoList());
	        List<String> listaCpfCnpjNaoPrevencao = Arrays.asList(whiteList.split(","));
	        //Se houver uma correspondência, o método anyMatch retornará true, indicando que pelo menos um CPF/CNPJ está na lista de isenção.
	        return pessoasPolos.stream()
	            .map(Pessoa::getDocumentoCpfCnpj)
	            .anyMatch(cnpj -> listaCpfCnpjNaoPrevencao.contains(cnpj));
	    }
	    
	    return false;
	}

	public void verificarPrevencao(ProcessoTrf processoTrf, boolean apagarHistorico) throws PontoExtensaoException, PrevencaoException {
		List<ProcessoConexao> processosPreventosExternos = new ArrayList<>();
		if (verificadorPrevencaoExterna != null) {
			try {
				//Primeiro tenta recuperar do legado
				ProcessoOrigem processoOrigem = getProcessoOrigem(processoTrf);
				processosPreventosExternos = verificadorPrevencaoExterna.verificaPrevencao(processoOrigem);
			} catch (PontoExtensaoException e) {
				//Qualquer falha com o legado, no apaga o histrico
				logger.error("Erro ao tentar verificar a preveno por meio do conector [{0}].", verificadorPrevencaoExterna.getClass());
				throw e;
			}
		} else {
			logger.warn("No h verificador de preveno externa disponvel.");
		}
		
		//Se houve sucesso na recuperao do legado, verifica se  para apagar o histrico antigo.
		//obs.: se houve falha com o legado, no apaga o histrico antigo.
		if (apagarHistorico) {
			processoTrfConexaoDAO.apagarHistoricoDaPrevencao(processoTrf);
		}
		
		//Grava os novos processos preventos externos
		for (ProcessoConexao processoPrevento : processosPreventosExternos) {
			ProcessoTrfConexao conexao = getProcessoTrfConexaoDetalhado(processoPrevento);
			conexao.setProcessoTrf(processoTrf);
			EntityUtil.getEntityManager().persist(conexao);
		}
		verificarPrevencao(processoTrf, TipoSolicitacaoPrevencaoEnum.D);
	}
	
	
	private void verificarPrevencaoExterna(ProcessoTrf processoTrf) throws PontoExtensaoException {
		ProcessoOrigem processoOrigem = getProcessoOrigem(processoTrf);

		// Por questões de compatibilidade com tribunais que possuem o
		// conector antigo (sem a implementação do novo método de prevenção externa),
		// primeiro executa o método antigo (deprecated).
		String[] processosPreventos = executaPontoDeExtensaoAntigoDaPrevencaoExterna(processoOrigem);

		if (processosPreventos != null) { // Implementação antiga válida.
			for (String processoPrevento : processosPreventos) {
				ProcessoTrfConexao conexao = getProcessoTrfConexao(processoPrevento);
				conexao.setProcessoTrf(processoTrf);
				EntityUtil.getEntityManager().persist(conexao);
			}
		} else {
			// O retorno nulo do ponto de extensão antigo indica a utilização do novo método de prevenção.
			List<ProcessoConexao> processosPreventosDetalhados = verificadorPrevencaoExterna.verificaPrevencao(processoOrigem);

			for (ProcessoConexao processoPrevento : processosPreventosDetalhados) {
				ProcessoTrfConexao conexao = getProcessoTrfConexaoDetalhado(processoPrevento);
				conexao.setProcessoTrf(processoTrf);
				EntityUtil.getEntityManager().persist(conexao);
			}
		}
	}

	private static ProcessoOrigem getProcessoOrigem(ProcessoTrf processoTrf) {
		ProcessoOrigem processoOrigem = new ProcessoOrigem();
		processoOrigem.setIdProcessoOrigem(processoTrf.getProcesso().getIdProcesso());
		processoOrigem.setNumeroProcessoOrigem(processoTrf.getJurisdicao().getNumeroOrigem()+"");
		processoOrigem.setCodClasse(Integer.parseInt(processoTrf.getClasseJudicial().getCodClasseJudicial()));

		atribuiAssuntos(processoTrf.getProcessoAssuntoList(), processoOrigem);
		atribuiAutores(processoTrf.getPessoaPoloAtivoList(), processoOrigem);
		atribuiReus(processoTrf.getPessoaPoloPassivoList(), processoOrigem);
		atribuiProcessosVinculados(processoTrf, processoOrigem);

		return processoOrigem;
	}
	
	private static void atribuiAssuntos(List<ProcessoAssunto> assuntos, ProcessoOrigem processoOrigem) {
		int[] codAssuntos = new int[assuntos.size()];

		for (int i = 0; i < codAssuntos.length; i++) {
			ProcessoAssunto assunto = assuntos.get(i);
			codAssuntos[i] = Integer.parseInt(assunto.getAssuntoTrf().getCodAssuntoTrf());

			if (assunto.getAssuntoPrincipal().booleanValue()) {
				processoOrigem.setCodAssuntoPrincipal(Integer.parseInt(assunto.getAssuntoTrf().getCodAssuntoTrf()));
			}
		}
		processoOrigem.setCodAssuntos(codAssuntos);
	}
	
	private static void atribuiAutores(List<Pessoa> autores, ProcessoOrigem processoOrigem) {
		for (Pessoa autor : autores) {
			ProcessoParteNome nomeAutor = new ProcessoParteNome();
			if (autor.getIdPessoa() != null) {
				nomeAutor.setIdPessoa(autor.getIdPessoa().intValue());	
			}			
			nomeAutor.setNome(autor.getNome());
			processoOrigem.addNomeAutor(nomeAutor);

			for (PessoaDocumentoIdentificacao pdi : autor.getPessoaDocumentoIdentificacaoList()) {
				if (pdi.getAtivo().booleanValue() && pdi.getDocumentoPrincipal().booleanValue()) {
					// Obtém o nome do documento e acrescenta na lista de nomes
					ProcessoParteNome nomeAutorDoc = new ProcessoParteNome();
					if (autor.getIdPessoa() != null) {
						nomeAutorDoc.setIdPessoa(autor.getIdPessoa().intValue());
					}
					nomeAutorDoc.setNome(pdi.getNome());
					processoOrigem.addNomeAutor(nomeAutorDoc);

					// Obtém o documento e acrescenta na lista de documentos.
					ProcessoParteDocumento doc = new ProcessoParteDocumento();
					doc.setIdDocumento(pdi.getIdDocumentoIdentificacao());
					doc.setDocumento(pdi.getTipoDocumento().getCodTipo() + ":" + pdi.getNumeroDocumento());
					processoOrigem.addDocumentoAutor(doc);
				}
			}

			for (PessoaNomeAlternativo pna : autor.getPessoaNomeAlternativoList()) {
				// Obtém o nome alternativo e acrescenta na lista de nomes
				ProcessoParteNome nomeAutorAlternativo = new ProcessoParteNome();
				if (autor.getIdPessoa() != null) {
					nomeAutorAlternativo.setIdPessoa(autor.getIdPessoa().intValue());
				}
				nomeAutorAlternativo.setNome(pna.getPessoaNomeAlternativo());
				processoOrigem.addNomeAutor(nomeAutorAlternativo);
			}
		}
	}
	
	private static void atribuiReus(List<Pessoa> reus, ProcessoOrigem processoOrigem) {
		for (Pessoa reu : reus) {
			ProcessoParteNome nomeReu = new ProcessoParteNome();
			if (reu.getIdPessoa() != null) {
				nomeReu.setIdPessoa(reu.getIdPessoa().intValue());	
			}			
			nomeReu.setNome(reu.getNome());
			processoOrigem.addNomeReu(nomeReu);

			for (PessoaDocumentoIdentificacao pdi : reu.getPessoaDocumentoIdentificacaoList()) {
				if (pdi.getAtivo().booleanValue() && pdi.getDocumentoPrincipal().booleanValue()) {
					// Obtém o nome do documento e acrescenta na lista de nomes
					ProcessoParteNome nomeReuDoc = new ProcessoParteNome();
					if (reu.getIdPessoa() != null) {					
					    nomeReuDoc.setIdPessoa(reu.getIdPessoa().intValue());
					}    
					nomeReuDoc.setNome(pdi.getNome());
					processoOrigem.addNomeReu(nomeReuDoc);

					// Obtém o documento e acrescenta na lista de documentos.
					ProcessoParteDocumento doc = new ProcessoParteDocumento();
					doc.setIdDocumento(pdi.getIdDocumentoIdentificacao());
					doc.setDocumento(pdi.getTipoDocumento().getCodTipo() + ":" + pdi.getNumeroDocumento());
					processoOrigem.addDocumentoReu(doc);
				}
			}

			for (PessoaNomeAlternativo pna : reu.getPessoaNomeAlternativoList()) {
				// Obtém o nome alternativo e acrescenta na lista de nomes
				ProcessoParteNome nomeReuAlternativo = new ProcessoParteNome();
				if (reu.getIdPessoa() != null) {
					nomeReuAlternativo.setIdPessoa(reu.getIdPessoa().intValue());	
				}
				nomeReuAlternativo.setNome(pna.getPessoaNomeAlternativo());
				processoOrigem.addNomeReu(nomeReuAlternativo);
			}
		}
	}
	
	private static void atribuiProcessosVinculados(ProcessoTrf processoTrf, ProcessoOrigem processoOrigem) {
		if (processoTrf.getDesProcReferencia() != null) {
			processoOrigem.addProcessoVinculado(processoTrf.getDesProcReferencia());
		}

		for (ProcessoTrfConexao conexo : processoTrf.getProcessoTrfConexaoList()) {
			if (conexo.getProcessoTrfConexo() != null) {
				processoOrigem.addProcessoVinculado(conexo.getProcessoTrfConexo().getNumeroProcesso());
			} else if (conexo.getNumeroProcesso() != null) {
				processoOrigem.addProcessoVinculado(conexo.getNumeroProcesso());
			}
		}
	}
	
	private String[] executaPontoDeExtensaoAntigoDaPrevencaoExterna(ProcessoOrigem processoOrigem) throws PontoExtensaoException {
		//TODO: as informações de "procedimentosOriginarios" eh vazia.
		String[] procedimentosOriginarios = new String[]{};
		
		return verificadorPrevencaoExterna.verificaPrevencao(processoOrigem.getNumeroProcessoOrigem(),
				processoOrigem.getCodigoOrgao(), processoOrigem.getCodClasse(), processoOrigem.getCodAssuntoPrincipal(), processoOrigem.getCodAssuntos(),
				processoOrigem.getNomesAutoresAsArray(), processoOrigem.getDocumentosAutoresAsArray(), processoOrigem.getNomesReusAsArray(),
				processoOrigem.getDocumentosReusAsArray(), processoOrigem.getProcessosVinculadosAsArray(), procedimentosOriginarios);
	}	

	private static ProcessoTrfConexao getProcessoTrfConexao(String processoPrevento) {
		ProcessoTrfConexao conexao = new ProcessoTrfConexao();
		conexao.setNumeroProcesso(processoPrevento);
		conexao.setTipoConexao(TipoConexaoEnum.PR);
		conexao.setPrevencao(PrevencaoEnum.PE);
		conexao.setDtPossivelPrevencao(new Date());
		return conexao;
	}	
	
	private static ProcessoTrfConexao getProcessoTrfConexaoDetalhado(ProcessoConexao processoPrevento) {
		ProcessoTrfConexao conexao = new ProcessoTrfConexao();
		conexao.setTipoConexao(TipoConexaoEnum.PR);
		conexao.setPrevencao(PrevencaoEnum.PE);
		conexao.setDtPossivelPrevencao(new Date());
		conexao.setNumeroProcesso(processoPrevento.getNumeroProcesso());
		conexao.setClasseJudicial(processoPrevento.getClasseJudicial());
		conexao.setAssunto(processoPrevento.getAssunto());
		conexao.setSessaoJudiciaria(processoPrevento.getSessaoJudiciaria());
		conexao.setOrgaoJulgador(processoPrevento.getOrgaoJulgador());
		conexao.setLinkSessaoJudiciaria(processoPrevento.getLinkSessaoJudiciaria());
		conexao.setListaPoloAtivo(processoPrevento.getPartesPoloAtivo());
		conexao.setListaPoloPassivo(processoPrevento.getPartesPoloPassivo());
		return conexao;
	}
	
	public void verificarPrevencao(ProcessoTrf processoTrf, TipoSolicitacaoPrevencaoEnum tipo) throws PrevencaoException {
		logPrev.setProcessoTrf(processoTrf);
		logPrev.setInTipoSolicitacaoPrevencao(tipo);
		
		try {
			ProcessoTrfHome.instance().validarAutuacao(processoTrf);
		} catch (Exception e) {
			throw new PrevencaoException(e);
		}

		if (isProcessoComNumeroReferencia(processoTrf)) {
			verificaPrevencaoPorProcessoReferencia(processoTrf);
		}
		
		//Regra de prevenção extra, incluída pelo TRF-1.
		realizaPrevencaoDeProcessoBuscandoOutrosProcessosQueFazemReferenciaAhEle(processoTrf);
		
		if (isProcessoIncidentalComNumeroOriginario(processoTrf)) {
			avaliaProcessoIncidentalComNumeroOriginario(processoTrf);
		} else {
			avaliaProcessoNaoIncidentalComNumeroOriginario(processoTrf);
		}
	}
	
	private boolean isProcessoIncidentalComNumeroOriginario(ProcessoTrf processoTrf) {
		return processoTrf.getIsIncidente()  && processoTrf.getProcessoOriginario() != null;
	}	
	
	private void avaliaProcessoIncidentalComNumeroOriginario(ProcessoTrf processoTrf) {
		processoTrf.setProcessoOriginario((ProcessoTrf)HibernateUtil.getSession().merge(processoTrf.getProcessoOriginario()));
		SegredoEntreProcessosJudiciaisEnum tipoSegredo = processoJudicialService.verificaSegredo(processoTrf.getProcessoOriginario(), processoTrf);
		
		if (tipoSegredo != SegredoEntreProcessosJudiciaisEnum.SEGREDO_NAO_IDENTIFICADO) {
			if (naoExisteNoProcessoOriginarioReferenciaDePrevencaoAoProcessoIncidental(processoTrf)) {
				adicionaNoProcessoOriginarioReferenciaAoProcessoIncidental(processoTrf);
			}
			if (naoExisteNoProcessoIncidentalReferenciaDePrevencaoAoProcessoOriginario(processoTrf)) {
				adicionaNoProcessoIncidentalReferenciaAoProcessoOriginario(processoTrf);
			}
		}
	}
	
	private boolean naoExisteNoProcessoOriginarioReferenciaDePrevencaoAoProcessoIncidental(ProcessoTrf processoTrf) {
		return !verificaExistenciaPossivelPrevencao(processoTrf.getProcessoOriginario(), processoTrf);
	}
	
	private boolean naoExisteNoProcessoIncidentalReferenciaDePrevencaoAoProcessoOriginario(ProcessoTrf processoTrf) {
		return !verificaExistenciaPossivelPrevencao(processoTrf, processoTrf.getProcessoOriginario());
	}
	
	private void avaliaProcessoNaoIncidentalComNumeroOriginario(ProcessoTrf processoTrf) {
		List<ProcessoTrf> possivelProcessosPreventos = new ArrayList<>(0);
		List<ClasseJudicial> classeJudiciaisColetivas = buscarClassesAcoesColetivas(processoTrf);
		
		if (!classeJudiciaisColetivas.isEmpty()) {
			possivelProcessosPreventos.addAll(buscarProcessosAcoesColetivas(processoTrf, classeJudiciaisColetivas));
		} else {
				possivelProcessosPreventos.addAll(buscarProcessosPrevencaoGeralPorNomeDocumento(processoTrf));
		}
		incluirPossiveisPreventos(processoTrf, possivelProcessosPreventos);
		EntityUtil.getEntityManager().persist(logPrev);
	}
	
	private boolean isProcessoComNumeroReferencia(ProcessoTrf processoTrf) {
		//Em retificação de processos, o objeto processoTrf pode estar desatualizado, sendo
		//necessário fazer consulta em banco de dados.
		
		PrevencaoDAO prevencaoDAO = ComponentUtil.getComponent(PrevencaoDAO.NAME);
		String procRef = prevencaoDAO.buscarProcessoReferencia(processoTrf.getIdProcessoTrf());
		
		if (procRef != null && !procRef.trim().isEmpty()) {
			processoTrf.setDesProcReferencia(procRef);
			return true;
		}
		
		return false;
	}	
	
	private void verificaPrevencaoPorProcessoReferencia(ProcessoTrf processoTrf) {
		List<ProcessoTrf> processosComMesmoNumeroOriginario = processoTrfConexaoDAO.verificaExistenciaPrevencaoPorReferencia(processoTrf, processoTrf.getDesProcReferencia());
		
		if (!isListaVazia(processosComMesmoNumeroOriginario)) {
			//Inclui no processo "C" todos aqueles processos preventos identificados.
			incluirPossiveisPreventos(processoTrf, processosComMesmoNumeroOriginario);
			
			//Para cada processo prevento identificado, inclui uma referncia cruzada ao processo que est sendo atualmente protocolado (processo "C").
			for (ProcessoTrf processoConexao : processosComMesmoNumeroOriginario) {
				if (naoExisteNoProcessoConexaoReferenciaDePrevencaoAoProcessoAtual(processoConexao, processoTrf)) {
					insereProcessoConexao(processoConexao, processoTrf, TipoConexaoEnum.PR, PrevencaoEnum.PE);
				}
			}
		}
	}
	
	public void realizaPrevencaoDeProcessoBuscandoOutrosProcessosQueFazemReferenciaAhEle(ProcessoTrf processoTrf) {
		List<ProcessoTrf> processosComMesmoNumeroOriginario = processoTrfConexaoDAO.verificaExistenciaPrevencaoPorReferencia(processoTrf, processoTrf.getNumeroProcesso());
		
		if (!isListaVazia(processosComMesmoNumeroOriginario)) {
			//Inclui no processo "B" todos aqueles processos identificados.
			incluirPossiveisPreventos(processoTrf, processosComMesmoNumeroOriginario);
		}		
	}	
	
	private void incluirPossiveisPreventos(ProcessoTrf processoJudicial, List<ProcessoTrf> possivelProcessosPreventos) {
		List<ProcessoTrfConexao> listaToPersist = new ArrayList<>();
		for (ProcessoTrf processoTrfConexo : possivelProcessosPreventos) {
			SegredoEntreProcessosJudiciaisEnum tipoSegredo = processoJudicialService.verificaSegredo(processoJudicial, processoTrfConexo);
			
			if (!verificaExistenciaPossivelPrevencao(processoJudicial, processoTrfConexo) && (
					tipoSegredo == SegredoEntreProcessosJudiciaisEnum.SOMENTE_P1_TRAMITA_EM_SEGREDO || 
					tipoSegredo == SegredoEntreProcessosJudiciaisEnum.NENHUM_DOS_PROCESSOS_TRAMITAM_EM_SEGREDO || 
					tipoSegredo == SegredoEntreProcessosJudiciaisEnum.OS_DOIS_PROCESSOS_TRAMITAM_EM_SEGREDO_SENDO_P1_MAIS_ANTIGO_QUE_P2)) {
				listaToPersist.add(toProcessoConexao(processoJudicial, processoTrfConexo, TipoConexaoEnum.PR, PrevencaoEnum.PE));
			}

			// Inclui a conexão cruzada
			if (!verificaExistenciaPossivelPrevencao(processoTrfConexo, processoJudicial) && (
					tipoSegredo == SegredoEntreProcessosJudiciaisEnum.SOMENTE_P2_TRAMITA_EM_SEGREDO || 
					tipoSegredo == SegredoEntreProcessosJudiciaisEnum.NENHUM_DOS_PROCESSOS_TRAMITAM_EM_SEGREDO || 
					tipoSegredo == SegredoEntreProcessosJudiciaisEnum.OS_DOIS_PROCESSOS_TRAMITAM_EM_SEGREDO_SENDO_P2_MAIS_ANTIGO_QUE_P1)) {
				listaToPersist.add(toProcessoConexao(processoTrfConexo, processoJudicial, TipoConexaoEnum.PR, PrevencaoEnum.PE));
			}
		}
		processoTrfConexaoDAO.insereProcessoConexaoNativeQuery(listaToPersist);
		
	}
	
	private void insereProcessoConexao(ProcessoTrf processoPrincipal, ProcessoTrf processoSuspeio, TipoConexaoEnum tipoConexao, PrevencaoEnum tipoPrevencao) {
		insereProcessoConexao(processoPrincipal, processoSuspeio, tipoConexao, tipoPrevencao, Boolean.TRUE);
	}
	private void insereProcessoConexao(ProcessoTrf processoPrincipal, ProcessoTrf processoSuspeio, TipoConexaoEnum tipoConexao, PrevencaoEnum tipoPrevencao, Boolean flagFlush) {
		ProcessoTrfConexao processoTrfConexao = toProcessoConexao(processoPrincipal, processoSuspeio, tipoConexao,
				tipoPrevencao);
		processoTrfConexaoDAO.insereProcessoConexao(processoTrfConexao, flagFlush);
	}

	private ProcessoTrfConexao toProcessoConexao(ProcessoTrf processoPrincipal, ProcessoTrf processoSuspeio,
			TipoConexaoEnum tipoConexao, PrevencaoEnum tipoPrevencao) {
		ProcessoTrfConexao processoTrfConexao = new ProcessoTrfConexao();
		processoTrfConexao.setProcessoTrf(processoPrincipal);
		processoTrfConexao.setProcessoTrfConexo(processoSuspeio);
		processoTrfConexao.setTipoConexao(tipoConexao);
		processoTrfConexao.setPrevencao(tipoPrevencao);
		processoTrfConexao.setDtPossivelPrevencao(new Date());
		return processoTrfConexao;
	}
	
	private boolean naoExisteNoProcessoConexaoReferenciaDePrevencaoAoProcessoAtual(ProcessoTrf processoConexao, ProcessoTrf processoAtual) {
		return !verificaExistenciaPossivelPrevencao(processoConexao, processoAtual);
	}

	private void adicionaNoProcessoOriginarioReferenciaAoProcessoIncidental(ProcessoTrf processoTrf) {
		insereProcessoConexao(processoTrf.getProcessoOriginario(), processoTrf, TipoConexaoEnum.DP, PrevencaoEnum.PR);
	}
	
	private void adicionaNoProcessoIncidentalReferenciaAoProcessoOriginario(ProcessoTrf processoTrf) {
		insereProcessoConexao(processoTrf, processoTrf.getProcessoOriginario(), TipoConexaoEnum.DP, PrevencaoEnum.PR);
	}
	
	
	
	/**
	 * Verifica se o processo dado é uma ação coletiva e, se for, retorna a lista de classes judiciais coletivas
	 * que, eventualmente, podem ser preventas com o dado processo.
	 *
	 * @param processoJudicial o processo paradigma
	 * @return a lista de classes coletivas, se a classe do processo paradigma for coletiva, ou uma lista vazia.
	 */
	private List<ClasseJudicial> buscarClassesAcoesColetivas(ProcessoTrf processoJudicial) {
		AgrupamentoClasseJudicial agrupamentoClasseJudicial = AgrupamentoClasseJudicialService.getAgrupamentoClasseJudicial("ACL");
		
		if (agrupamentoClasseJudicial != null) {
			ClasseJudicial classeDoProcesso = processoJudicial.getClasseJudicial();
			List<ClasseJudicialAgrupamento> classesJudiciaisAgrupamento =  agrupamentoClasseJudicial.getClasseJudicialAgrupamentoList();
			
			for (ClasseJudicialAgrupamento classeJudicialAgrupamento : classesJudiciaisAgrupamento) {
				if (classeJudicialAgrupamento.getClasse().equals(classeDoProcesso)) {
					return AgrupamentoClasseJudicialService.getClassesJudiciais(classesJudiciaisAgrupamento);
				}
			}
		}
		ItemsLog item = new ItemsLog();
		item.setProcessoTrfLog(this.logPrev);
		item.setInCriticidade(CriticidadeEnum.I);
		item.setItem("O processo não se enquadra em ações coletivas.");
		this.logPrev.getItemsLogList().add(item);
		return new ArrayList<ClasseJudicial>(0);
	}

	/**
	 * Recupera a lista de processos coletivos possivelmente preventos com um dado processo
	 * 
	 * @param processoJudicial o processo paradigma
	 * @param classeJudicialList a lista de classes coletivas
	 * @return a lista de processos potencialmente preventos.
	 */
	private List<ProcessoTrf> buscarProcessosAcoesColetivas(ProcessoTrf processoJudicial, List<ClasseJudicial> classeJudicialList) {
		Set<ProcessoTrf> preventos = new HashSet<>();
		
		Set<Integer> assuntos = getIdsColecaoAssuntoTrf(processoJudicial);
		Set<Integer> idsPessoaPoloAtivo = obterIDsPessoaPorListaDePartes(processoJudicial.getProcessoPartePoloAtivoSemAdvogadoList());
		Set<Integer> idsPessoaPoloPassivo = obterIDsPessoaPorListaDePartes(processoJudicial.getProcessoPartePoloPassivoSemAdvogadoList());
		List<br.jus.pje.nucleo.entidades.PessoaJuridica> orgaosVinculados = processoJudicial.getProcessoPartePoloPassivoOrgaoVinculacao();
		
		if (!isListaVazia(orgaosVinculados)) {
			List<Pessoa> pessoasOrgaosVinculados = new ArrayList<>();
			
			//Busca também em mandados de segurança.
			for (br.jus.pje.nucleo.entidades.PessoaJuridica pessoaJuridica : orgaosVinculados) {
				pessoasOrgaosVinculados.add((Pessoa)pessoaJuridica);
			}
			
			idsPessoaPoloPassivo.addAll(obterIDsPessoa(pessoasOrgaosVinculados));
		}
		PrevencaoDAO prevencaoDAO = ComponentUtil.getComponent(PrevencaoDAO.NAME);
		
		//Item i e ii da RN304
		List<ProcessoTrf> preventosComuns = prevencaoDAO.buscarProcessosPrevencaoGeralPorNomeDocumento(assuntos, idsPessoaPoloAtivo,
				idsPessoaPoloPassivo, processoJudicial.getIdProcessoTrf());
		
		if (isListaVazia(preventosComuns)) {
			preventosComuns = new ArrayList<>();
		}
		
		Set<Integer> classes = new HashSet<>();
		for (ClasseJudicial classe : classeJudicialList) {
			classes.add(Integer.valueOf(classe.getIdClasseJudicial()));
		}

		//Item iii da RN304 
		List<ProcessoTrf> preventosColetivos = prevencaoDAO.buscarProcessosPrevencaoAcoesColetivas(classes, assuntos, idsPessoaPoloPassivo,
				processoJudicial.getIdProcessoTrf());
		
		if (!isListaVazia(preventosColetivos)) {
			preventosComuns.addAll(preventosColetivos);
		}
		
		if (!isListaVazia(preventosComuns)) {
			for (ProcessoTrf prevento : preventosComuns) {
				//Elimina duplicidades devido a duas consultas
				preventos.add(prevento);		
			}
			
			ItemsLog item = new ItemsLog();
			item.setProcessoTrfLog(this.logPrev);
			item.setInCriticidade(CriticidadeEnum.I);
			item.setItem("Para regra de ações coletivas foram encontrados os seguintes processos preventos:\n"
					+ listarProcessos(preventosComuns));
			this.logPrev.getItemsLogList().add(item);
			adicionarPossiveisPreventos(preventosComuns);
		} else {
			ItemsLog item = new ItemsLog();
			item.setProcessoTrfLog(this.logPrev);
			item.setInCriticidade(CriticidadeEnum.I);
			item.setItem("Para regra de ações coletivas não foram encontrados possíveis preventos.");
			this.logPrev.getItemsLogList().add(item);
		}
		return new ArrayList<>(preventos);
	}

	/**
	 * Verifica se o processo dado é uma ação constitucional e, se for, retorna a lista de classes judiciais constitucionais
	 * que, eventualmente, podem ser preventas com o dado processo.
	 * 
	 * @param processoJudicial o processo paradigma
	 * @return a lista de classes constitucionais, se a classe do processo paradigma for constitucional, ou uma lista vazia. 
	 */
	private List<ClasseJudicial> buscarClassesAcoesConstitucionais(ProcessoTrf processoJudicial) {
		AgrupamentoClasseJudicial agrupamentoClasseJudicial = AgrupamentoClasseJudicialService
				.getAgrupamentoClasseJudicial("ACO");
		if (agrupamentoClasseJudicial != null) {
			for (ClasseJudicialAgrupamento classeJudicialAgrupamento : agrupamentoClasseJudicial
					.getClasseJudicialAgrupamentoList()) {
				if (classeJudicialAgrupamento.getClasse().equals(processoJudicial.getClasseJudicial())) {
					return AgrupamentoClasseJudicialService.getClassesJudiciais(agrupamentoClasseJudicial
							.getClasseJudicialAgrupamentoList());
				}
			}
		}
		ItemsLog item = new ItemsLog();
		item.setProcessoTrfLog(this.logPrev);
		item.setInCriticidade(CriticidadeEnum.I);
		item.setItem("O processo não se enquadra em ações constitucionais.");
		this.logPrev.getItemsLogList().add(item);
		return new ArrayList<ClasseJudicial>(0);
	}

	@SuppressWarnings("unchecked")
	private List<ProcessoTrf> buscarProcessosAcoesConstitucionais(ProcessoTrf processoJudicial, List<ClasseJudicial> classeJudicialList) {
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct pro from ProcessoTrf pro ").append("inner join pro.processoAssuntoList pal ")
				.append("inner join pro.processoParteList ppa ")
				.append("inner join pro.processoParteList ppAutor ")
				.append("where pro.classeJudicial in (:classes) ")
				.append("and pal.assuntoTrf in (:assuntos) ").append("and ppa.pessoa.orgaoVinculacao in (:orgaos) ")
				.append("and ppa.inSituacao = 'A' ").append("and pro.numeroSequencia != null ")
				.append("and ppAutor.inParticipacao = 'A' ").append("and ppAutor.inSituacao = 'A' ")
				.append("and ppAutor.partePrincipal = 'true' ").append("and ppAutor.pessoa in (:pessoas) ")
				.append("and pro != :processoTrf ").append("and pro.processoStatus = :processoStatus ");
		Query query = em.createQuery(sql.toString());
		query.setParameter("classes", classeJudicialList);
		/*
		 * PJE-JT: David Vieira : PJE-763 - 2011-10-27 Alteracoes feitas pela
		 * JT. this.processoTrf.getAssuntoTrfList() estava retornando uma lista
		 * vazia..
		 */
		// antes: query.setParameter("assuntos",
		// this.processoTrf.getAssuntoTrfList());
		List<AssuntoTrf> assuntos = getColecaoAssuntoTrf(processoJudicial);
		query.setParameter("assuntos", assuntos);
		/*
		 * PJE-JT: Fim
		 */

		query.setParameter("orgaos", processoJudicial.getProcessoPartePoloPassivoOrgaoVinculacao());
		query.setParameter("processoTrf", processoJudicial);
		query.setParameter("processoStatus", ProcessoStatusEnum.D);

		List<Pessoa> pessoas = new ArrayList<Pessoa>(0);
		for(ProcessoParte p : processoJudicial.getListaPartePrincipalAtivo()){
			pessoas.add(p.getPessoa());
		}

		query.setParameter("pessoas", pessoas);

		List<ProcessoTrf> lista = null;
		/*
		 * PJE-JT: David Vieira : PJE-763 - 2011-10-27 Alteracoes feitas pela
		 * JT. Nos casos em que alguma dessas lista está vazia, o hql estava
		 * retornando exception 'unexpected end of subtree'
		 */
		if (isListaVazia(processoJudicial.getAssuntoTrfList())
				|| isListaVazia(processoJudicial.getProcessoPartePoloPassivoOrgaoVinculacao())) {
			lista = new ArrayList<ProcessoTrf>();
		} else {
			lista = query.getResultList();
		}
		// antes: List<ProcessoTrf> lista = query.getResultList();
		/*
		 * PJE-JT: Fim
		 */

		if (lista.size() > 0) {
			ItemsLog item = new ItemsLog();
			item.setProcessoTrfLog(this.logPrev);
			item.setInCriticidade(CriticidadeEnum.I);
			item.setItem("Para regra de ações constitucionais foram encontrados os seguintes processos preventos:\n"
					+ listarProcessos(lista));
			this.logPrev.getItemsLogList().add(item);
			adicionarPossiveisPreventos(lista);
		} else {
			ItemsLog item = new ItemsLog();
			item.setProcessoTrfLog(this.logPrev);
			item.setInCriticidade(CriticidadeEnum.I);
			item.setItem("Para regra de ações constitucionais não foram encontrados possíveis preventos.");
			this.logPrev.getItemsLogList().add(item);
		}
		return lista;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private List<ProcessoTrf> buscarProcessosPrevencaoGeral(ProcessoTrf processoJudicial) {
		List<Pessoa> pessoaAtivoList = new ArrayList<Pessoa>(0);
		for (ProcessoParte processoParte : processoJudicial.getProcessoPartePoloAtivoSemAdvogadoList()) {
			pessoaAtivoList.add(processoParte.getPessoa());
		}
		List<Pessoa> pessoaPassivoList = new ArrayList<Pessoa>(0);
		for (ProcessoParte processoParte : processoJudicial.getProcessoPartePoloPassivoSemAdvogadoList()) {
			pessoaPassivoList.add(processoParte.getPessoa());
		}

		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct pro from ProcessoTrf pro ")
				.append("inner join pro.processoAssuntoList pal ")
				.append("inner join pro.processoParteList ppa ")
				.append("inner join pro.processoParteList ppb ")
				.append("where pal.assuntoTrf in (:assuntos) ")
				.append("and ppa.inParticipacao = 'A' ")
				.append("and ppb.inParticipacao = 'P' ")
				.append("and pro != :processoTrf ")
				.append("and ppa.tipoParte.idTipoParte != "
						+ ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte() + " ")
				.append("and ppb.tipoParte.idTipoParte != "
						+ ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte() + " ")
				.append("and ((ppa.pessoa in (:partesAtivo) and ppb.pessoa in (:partesPassivo)) ")
				.append("or   (ppb.pessoa in (:partesAtivo) and ppa.pessoa in (:partesPassivo))) ")
				.append("and pro.dataDistribuicao != null ").append("and pro.processoStatus = :processoStatus ");

		Query query = em.createQuery(sql.toString());
		/*
		 * PJE-JT: David Vieira : PJE-763 - 2011-10-27 Alteracoes feitas pela
		 * JT. this.processoTrf.getAssuntoTrfList() estava retornando uma lista
		 * vazia..
		 */
		// antes: query.setParameter("assuntos",
		// this.processoTrf.getAssuntoTrfList());
		query.setParameter("assuntos", ProcessoTrfHome.instance().getListaAssunto());
		/*
		 * PJE-JT: Fim
		 */
		query.setParameter("partesAtivo", pessoaAtivoList);
		query.setParameter("partesPassivo", pessoaPassivoList);
		query.setParameter("processoTrf", processoJudicial);
		query.setParameter("processoStatus", ProcessoStatusEnum.D);

		List<ProcessoTrf> lista = null;

		/*
		 * PJE-JT: David Vieira : PJE-763 - 2011-10-27 Alteracoes feitas pela
		 * JT. Nos casos em que alguma dessas lista está vazia, o hql estava
		 * retornando exception 'unexpected end of subtree'
		 */
		if (isListaVazia(ProcessoTrfHome.instance().getListaAssunto()) || isListaVazia(pessoaAtivoList)
				|| isListaVazia(pessoaPassivoList)) {
			lista = new ArrayList<ProcessoTrf>();
		} else {
			lista = query.getResultList();
		}
		// antes: lista = query.getResultList();
		/*
		 * PJE-JT: Fim
		 */

		if (lista.size() > 0) {
			ItemsLog item = new ItemsLog();
			item.setProcessoTrfLog(this.logPrev);
			item.setInCriticidade(CriticidadeEnum.I);
			item.setItem("Para regra geral foram encontrados os seguintes possíveis preventos:\n"
					+ listarProcessos(lista));
			this.logPrev.getItemsLogList().add(item);
			adicionarPossiveisPreventos(lista);
		} else {
			ItemsLog item = new ItemsLog();
			item.setProcessoTrfLog(this.logPrev);
			item.setInCriticidade(CriticidadeEnum.I);
			item.setItem("Para regra geral não foram encontrados possíveis preventos.");
			this.logPrev.getItemsLogList().add(item);
		}
		return lista;
	}

	@SuppressWarnings("unchecked")
	private List<ProcessoTrf> buscarProcessosPrevencaoGeralPorNomeDocumento(ProcessoTrf processoJudicial) {
		String msg = null;
		List<ProcessoTrf> preventos = new ArrayList<>();
		
		if (!Boolean.TRUE.equals(processoJudicial.getClasseJudicial().getIgnoraPrevencao())) {
			Set<Integer> assuntos = getIdsColecaoAssuntoTrf(processoJudicial);
			Set<Integer> idsPessoaPoloAtivo = obterIDsPessoaPorListaDePartes(processoJudicial.getProcessoPartePoloAtivoSemAdvogadoList());
			Set<Integer> idsPessoaPoloPassivo = obterIDsPessoaPorListaDePartes(processoJudicial.getProcessoPartePoloPassivoSemAdvogadoList());
			List<br.jus.pje.nucleo.entidades.PessoaJuridica> orgaosVinculados = processoJudicial.getProcessoPartePoloPassivoOrgaoVinculacao();
			
			if (!isListaVazia(orgaosVinculados)) {
				List<Pessoa> pessoasOrgaosVinculados = new ArrayList<>();
				
				//Busca também em mandados de segurança.
				for (br.jus.pje.nucleo.entidades.PessoaJuridica pessoaJuridica : orgaosVinculados) {
					pessoasOrgaosVinculados.add((Pessoa)pessoaJuridica);
				}
				
				idsPessoaPoloPassivo.addAll(obterIDsPessoa(pessoasOrgaosVinculados));
			}
			PrevencaoDAO prevencaoDAO = ComponentUtil.getComponent(PrevencaoDAO.NAME);
			preventos = prevencaoDAO.buscarProcessosPrevencaoGeralPorNomeDocumento(assuntos, idsPessoaPoloAtivo,
					idsPessoaPoloPassivo, processoJudicial.getIdProcessoTrf());
			
			if (!isListaVazia(preventos)) {
				msg = "Foram encontrados os seguintes possíveis preventos:\n" + listarProcessos(preventos);
			} else {
				preventos = new ArrayList<>();
				msg = "Não foram encontrados possíveis preventos.";
			}
		} else {
			msg = "A classe judicial do processo está configurada para ignorar a prevenção.";
		}
		
		ItemsLog item = new ItemsLog();
		item.setProcessoTrfLog(this.logPrev);
		item.setInCriticidade(CriticidadeEnum.I);
		item.setItem(msg);
		this.logPrev.getItemsLogList().add(item);
		
		adicionarPossiveisPreventos(preventos);
		
		return preventos;
	}

	private Set<Integer> obterIDsPessoa(List<Pessoa> pessoas) {
		Set<String> nomesPessoa = new HashSet<String>(0);
		Set<String> documentosPessoa = new HashSet<String>(0);
		
		for (Pessoa pessoa : pessoas) {
			nomesPessoa.add(pessoa.getNome());
			for (PessoaDocumentoIdentificacao pdi : pessoa.getPessoaDocumentoIdentificacaoList()) {
				documentosPessoa.add(pdi.getTipoDocumento().getCodTipo().trim() + "+" + alterarDocumento(pdi.getNumeroDocumento()));
			}
		}
		Set<Integer> idsPessoa = new HashSet<Integer>(0);
		try {
			idsPessoa = buscaElasticSearchIdsPessoas(nomesPessoa, documentosPessoa);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return idsPessoa;
	}	
	
	private Set<Integer> obterIDsPessoaPorListaDePartes(List<ProcessoParte> partes) {
		List<Pessoa> pessoas = new ArrayList<>();
		
		for (ProcessoParte parte : partes) {
			pessoas.add(parte.getPessoa());
		}
		return obterIDsPessoa(pessoas);
	}

	private boolean isListaVazia(List<?> lista) {
		return lista == null || lista.isEmpty();
	}

	private String alterarDocumento(String numeroDocumento) {
		List<String> lista = Arrays.asList(NumeroDocumentoBridge.DOCUMENTO_STOP_WORDS);
		for (String t : lista) {
			numeroDocumento = numeroDocumento.replace(t, "");
		}
		return numeroDocumento;
	}

	private Set<Integer> buscaElasticSearchIdsPessoas(Set<String> nomesPessoa, Set<String> documentosPessoa) {
		Set<Integer> idsPessoa = new HashSet<Integer>(0);
		ElasticDAO<PessoaDocumentoIdentificacao> processoElasticDAO = new ElasticDAO<PessoaDocumentoIdentificacao>() {
		};
		processoElasticDAO.setIndexador(indexador);
		br.jus.pje.search.Search search = new br.jus.pje.search.Search(PessoaDocumentoIdentificacao.class);
		try {
			if (indexador.isEnabled()) {
				carregarCriterioNumeroDocumentoOuNomePessoa(search, nomesPessoa, documentosPessoa);
				if (search.getCriterias().isEmpty()) {
					logger.warn("Ao menos um dos critrios deve estar definido!");
					return idsPessoa;
				}
				JSONObject resultadoPesquisa = processoElasticDAO.search(search);
				if (resultadoPesquisa != null && resultadoPesquisa.keySet().size() > 0) {
					//int total = ((JSONObject) resultadoPesquisa.get("hits")).getInt("total");
					int total = ((JSONObject) ((JSONObject) resultadoPesquisa.get("hits")).get("total")).getInt("value");
					if (total == 0) {
						logger.info("No foram encontrados resultados para a pesquisa.");
					} else {
						JSONArray arrayDocumentos_ = ((JSONArray) ((JSONObject) resultadoPesquisa.get("hits"))
								.get("hits"));
						for (int i = 0; i < arrayDocumentos_.length(); i++) {
							// verifica se é a mesme pessoa comparando o CPF
							String cpfElastic = alterarDocumento(
									((JSONObject) ((JSONObject) arrayDocumentos_.get(i)).get("_source"))
											.getString("numero"));

							if ("CPF"
									.equals(((JSONObject) ((JSONObject) arrayDocumentos_.get(i)).get("_source"))
											.getString("codigo"))
									&& documentosPessoa.stream().filter(d -> d.contains("CPF"))
											.map(d -> d.substring(d.indexOf("+") + 1))
											.anyMatch(cpf -> cpf.equals(cpfElastic))) {
								idsPessoa.add(((JSONObject) ((JSONObject) arrayDocumentos_.get(i)).get("_source"))
										.getInt("pessoa"));
							}
						}
					}
				}
			} else {
				logger.error("Indexacao nao habilitada.");
			}
		} catch (Throwable e) {
			logger.error("Houve um erro ao realizar a consulta.");
			e.printStackTrace();
		}

		return idsPessoa;
	}

	private void carregarCriterioNumeroDocumentoOuNomePessoa(br.jus.pje.search.Search s, Set<String> nomesPessoa,
															 Set<String> documentosPessoa) throws Exception {

		boolean isNomesPessoaVazio = (nomesPessoa == null || nomesPessoa.isEmpty());
		boolean isDocumentosPessoaVazio = (documentosPessoa == null || documentosPessoa.isEmpty());

		if (isNomesPessoaVazio && isDocumentosPessoaVazio) {
			return;
		}

		List<Criteria> todosCriterios = new ArrayList<>();

		if (!isNomesPessoaVazio) {
			for (String nomePessoa : nomesPessoa) {
				if (StringUtil.isEmpty(nomePessoa)) {
					continue;
				}
				Criteria criterioNome = Criteria.equals("pessoa.nome", nomePessoa);
				todosCriterios.add(criterioNome);
			}
		}

		if (!isDocumentosPessoaVazio) {
			for (String documentoPessoa : documentosPessoa) {
				if (StringUtil.isEmpty(documentoPessoa)) {
					continue;
				}
				Criteria numeroDocumento = (Criteria.equals("numeroDocumento",
						documentoPessoa.substring(documentoPessoa.indexOf("+") + 1)));
				Criteria codigoDocumento = (Criteria.equals("tipoDocumento.codTipo",
						documentoPessoa.substring(0, documentoPessoa.indexOf("+"))));
				Criteria numeroAndCodigo = Criteria.and(numeroDocumento, codigoDocumento);
				todosCriterios.add(numeroAndCodigo);
			}
		}

		if (!todosCriterios.isEmpty()) {
			Criteria[] arrayTodosCriterios = new Criteria[todosCriterios.size()];
			arrayTodosCriterios = todosCriterios.toArray(arrayTodosCriterios);
			s.addCriteria(Criteria.or(arrayTodosCriterios));
		}

		return;
	}

	interface Function {
		void call();
	}
	private void executarComTratamentoParaLimiteDeClausulas(Function function) {
		try {
			function.call();
		} catch (BooleanQuery.TooManyClauses e) {
			BooleanQuery.setMaxClauseCount(BooleanQuery.getMaxClauseCount() + (BooleanQuery.getMaxClauseCount() / 2));
			function.call();
		}
	}

	private Set<Integer> realizaBuscaParceladaDeIdsViaPKs(Set<Integer> ids, int defautMaxClauseCount) {
		Set<Integer> idsPessoa = new HashSet<>();
		List<List<Integer>> roundTripsBanco = particionaIds(ids, defautMaxClauseCount);
	
		for (List<Integer> idsParcial : roundTripsBanco) {
			List<Integer> idsTemp = pessoaDocumentoIdentificacaoDAO.obterIdsPessoasPorIdsDocs(new HashSet<>(idsParcial));
			
			if (idsTemp != null && !idsTemp.isEmpty()) {
				idsPessoa.addAll(idsTemp);
			}
		}
		return idsPessoa;
	}
	
	
	private Set<Integer> realizaBuscaParceladaDeIdsViaCnpjs(Set<String> docs, int defautMaxClauseCount) {
		Set<Integer> idsPessoa = new HashSet<>();
		List<List<String>> roundTripsBanco = particionaDocs(docs, defautMaxClauseCount);
	
		for (List<String> docsParcial : roundTripsBanco) {
			idsPessoa.addAll(pessoaDocumentoIdentificacaoDAO.obterIdsPessoasViaRaizCnpj(new HashSet<>(docsParcial)));
		}
		return idsPessoa;
	}	

	private Set<Integer> realizaBuscaParceladaDeIdsViaCpfs(Set<String> docs, int defautMaxClauseCount) {
		Set<Integer> idsPessoa = new HashSet<>();
		List<List<String>> roundTripsBanco = particionaDocs(docs, defautMaxClauseCount);
	
		for (List<String> docsParcial : roundTripsBanco) {
			idsPessoa.addAll(pessoaDocumentoIdentificacaoDAO.obterIdsPessoasViaCpf(new HashSet<>(docsParcial)));
		}
		return idsPessoa;
	}	
	
	protected List<List<Integer>> particionaIds(Set<Integer> ids, int tamanhoParticao) {
		List<List<Integer>> roundTripsBanco = new ArrayList<>();
		
		for (List<Integer> particao : Iterables.partition(ids, tamanhoParticao)) {
			roundTripsBanco.add(particao);
		}
		
		return roundTripsBanco;
	}
	
	protected List<List<String>> particionaDocs(Set<String> ids, int tamanhoParticao) {
		List<List<String>> roundTripsBanco = new ArrayList<>();
		
		for (List<String> particao : Iterables.partition(ids, tamanhoParticao)) {
			roundTripsBanco.add(particao);
		}
		
		return roundTripsBanco;
	}	
	
	public boolean verificaExistenciaPossivelPrevencao(ProcessoTrf processoTrf, ProcessoTrf processoTrfConexo) {
		return this.verificaExistenciaPossivelPrevencao(processoTrf, processoTrfConexo, null);
	}
	
	public boolean verificaExistenciaPossivelPrevencao(ProcessoTrf processoTrf, ProcessoTrf processoTrfConexo, String numeroProcessoTrfConexo) {
		if(processoTrfConexo == null && numeroProcessoTrfConexo == null) {
			return Boolean.FALSE;
		}
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select count(p) from ProcessoTrfConexao p ");
		sql.append("where p.processoTrf = :processoTrf ");
		sql.append("and p.tipoConexao = :tipoConexaoPrevencao ");
		sql.append("and p.ativo = true ");
		if(processoTrfConexo != null) {
			sql.append("and p.processoTrfConexo = :processoTrfConexo ");
		}
		if(numeroProcessoTrfConexo != null) {
			sql.append("and p.numeroProcesso = :numeroProcessoTrfConexo ");
		}
		
		Query query = em.createQuery(sql.toString());
		query.setParameter("processoTrf", processoTrf);
		query.setParameter("tipoConexaoPrevencao", TipoConexaoEnum.PR);
		
		if(processoTrfConexo != null) {
			query.setParameter("processoTrfConexo", processoTrfConexo);
		}
		if(numeroProcessoTrfConexo != null) {
			query.setParameter("numeroProcessoTrfConexo", numeroProcessoTrfConexo);
		}
		
		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	private String listarProcessos(List<ProcessoTrf> lista) {
		StringBuilder texto = new StringBuilder();
		for (ProcessoTrf p : lista) {
			texto.append("[" + p.toString() + "] ");
		}
		return texto.toString();
	}

	private void adicionarPossiveisPreventos(List<ProcessoTrf> lista) {
		for (ProcessoTrf p : lista) {
			ProcessoTrfPreventoLog pp = new ProcessoTrfPreventoLog();
			pp.setProcessoTrf(p);
			pp.setProcessoTrfLogPrevencao(logPrev);
			this.logPrev.getPossiveisPreventos().add(pp);
		}
	}

	/**
	 * Método para verificar o segredo entre dois processos (p1 e p2).
	 * Retornos possíveis:
	 * <li>1: somente p1 tramita em segredo</li>
	 * <li>2: somente p2 tramita em segredo</li>
	 * <li>3: nenhum dos processos tramita em segredo</li>
	 * <li>4: os dois processos tramitam em segredo, sendo p1 mais antigo que p2</li>
	 * <li>5: os dois processos tramitam em segredo, sendo p2 mais antigo que p1</li>
	 * <li>6: erro na verificação</li>
	 * @deprecated  Ver {@link ProcessoJudicialService.verificaSegredo(ProcessoTrf p1, ProcessoTrf p2)}
	 */
	@Deprecated	
	public int verificaSegredo(ProcessoTrf p1, ProcessoTrf p2) {
		SegredoEntreProcessosJudiciaisEnum tipoSegredo = processoJudicialService.verificaSegredo(p1, p2);
		return tipoSegredo.getCodigoSegredo();
	}
	
	// Usaremos a versao sincronizada pois essa, mesmo modificada, causa inconsistencia devico as commits
	// nao serem ordenados
	 @Observer(Eventos.CONEXAO_PROCESSUAL_CRIADA)
	@Transactional
	public void iniciarFluxoPrevencao(ProcessoTrfConexao processoTrfConexao) {

		//Verifica se o objeto de conexao está instanciado, bem como os processos conexos
		if(processoTrfConexao == null || processoTrfConexao.getProcessoTrf() == null || processoTrfConexao.getProcessoTrfConexo() == null) {
			return;
		}

		//Verifica se a conexão é do tipo prevenção e se a análise está pendente
		if(!processoTrfConexao.getTipoConexao().equals(TipoConexaoEnum.PR) || !processoTrfConexao.getPrevencao().equals(PrevencaoEnum.PE)) {
			return;
		}
		
		String codigoFluxoPrevencao = parametroService.valueOf(Parametros.CODIGO_FLUXO_PREVENCAO);

		//Verifica se o código do fluxo de prevenção foi configurado
		if(codigoFluxoPrevencao == null || codigoFluxoPrevencao.trim().equals("")) {
			return;
		}
		
		try {
			//Instancia o fluxo de prevenção
			Fluxo fluxoPrevencao = fluxoManager.findByCodigo(codigoFluxoPrevencao);
			//Se o fluxo foi instanciado...
			if(fluxoPrevencao != null) {
				synchronized (lock) {
					//Verifica se já existe fluxo de prevenção iniciado para o processo
					boolean existeFluxoPrevencaoAtivo = fluxoManager.existeFluxoComVariavel(processoTrfConexao.getProcessoTrf(), Variaveis.VARIAVEL_FLUXO_PREVENCAO);
					//Se não há fluxo de prevenção instanciado para o processo, inicia um novo fluxo
					if(!existeFluxoPrevencaoAtivo) {
						processoJudicialService.incluirNovoFluxo(processoTrfConexao.getProcessoTrf(), fluxoPrevencao.getCodFluxo());
					}
				}
			}
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
//	@Observer(Eventos.CONEXAO_PROCESSUAL_CRIADA)
	public void iniciarFluxoPrevencaoSynchronized(ProcessoTrfConexao processoTrfConexao) throws Exception {
		PrevencaoServiceSynchronized.iniciarFluxoPrevencao(processoTrfConexao, this.logger, this.parametroService, this.fluxoManager);
	}

	public boolean analisePrevencaoEmFluxo() {
 		String codigoFluxoPrevencao = this.parametroService.valueOf(Parametros.CODIGO_FLUXO_PREVENCAO);
 
 		if (codigoFluxoPrevencao == null || codigoFluxoPrevencao.trim().equals("")) {
 			return false;
 		}
 
 		Fluxo fluxoPrevencao = this.fluxoManager.findByCodigo(codigoFluxoPrevencao);
 		if (fluxoPrevencao == null) {
 			return false;
 		}
 		
 		return true;
 	}

	/**
	 * Retorna a coleção de AssuntoTrf para consultar processos preventos.
	 *
	 * @param processo ProcessoTrf
	 * @return Lista de AssuntoTrf.
	 */
	private List<AssuntoTrf> getColecaoAssuntoTrf(ProcessoTrf processo) {
		Set<Integer> idsAssuntos = getIdsColecaoAssuntoTrf(processo);
		
		if (!idsAssuntos.isEmpty()) {
			AssuntoTrfDAO assuntoTrfDAO = ComponentUtil.getComponent(AssuntoTrfDAO.NAME);
			return assuntoTrfDAO.obtemAssuntosJudiciais(idsAssuntos);
		}
		return new ArrayList<>();
	}
	
	private Set<Integer> getIdsColecaoAssuntoTrf(ProcessoTrf processo) {
		List<ProcessoAssunto> listaProcessoAssunto = processo.getProcessoAssuntoList();
		if (isListaVazia(listaProcessoAssunto)) {
			return new HashSet<>();
		}
		
		Set<Integer> ids = new HashSet<>();

		for (ProcessoAssunto assunto : listaProcessoAssunto) {
			ids.add(Integer.valueOf(assunto.getAssuntoTrf().getIdAssuntoTrf()));
		}
		return ids;
	}	
}
