package br.jus.cnj.pje.nucleo.manager;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.component.UrlUtil;
import br.com.jt.pje.manager.SessaoManager;
import br.jus.cnj.pje.business.dao.LiberacaoPublicacaoDecisaoDAO;
import br.jus.cnj.pje.business.dao.ProcessoDocumentoDAO;
import br.jus.cnj.pje.nucleo.MuralException;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.BaseService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.util.IndexEvent;
import br.jus.cnj.pje.vo.ConsultaPublicacaoSessaoVO;
import br.jus.csjt.pje.commons.util.FileUtil;
import br.jus.pje.nucleo.entidades.LiberacaoPublicacaoDecisao;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.SituacaoPublicacaoLiberacaoEnum;
import br.jus.pje.nucleo.enums.TipoDecisaoPublicacaoEnum;
import br.jus.pje.nucleo.enums.TipoPublicacaoEnum;
import br.jus.pje.nucleo.util.Cronometro;
import br.jus.pje.nucleo.util.DateUtil;
import java.util.Calendar;
import java.util.GregorianCalendar;

@Name(LiberacaoPublicacaoDecisaoService.NAME)
public class LiberacaoPublicacaoDecisaoService extends BaseService {
	public final static String NAME = "liberacaoPublicacaoDecisaoService";
	@Logger
	private Log logger;
	
    @In
    private ProcessoDocumentoDAO processoDocumentoDAO;

	@In
	private LiberacaoPublicacaoDecisaoDAO liberacaoPublicacaoDecisaoDAO;

	@In
	private TramitacaoProcessualService tramitacaoProcessualService; 
	
	@In
	private AtoComunicacaoService atoComunicacaoService;

	@In
	private PessoaMagistradoManager pessoaMagistradoManager;
	
	@In
	private ProcessoJudicialService processoJudicialService;

	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	
	@In
	private ParametroUtil parametroUtil;

	@In(required = false)
	private TaskInstanceHome taskInstanceHome;

	@In
	private SessaoManager sessaoManager;

	@In
	private ProcessoExpedienteManager processoExpedienteManager;

	@In
	private MuralService muralService;

	/**
	 * Responsvel por liberar as publicações considerando os documentos selecionados
	 * Caso o tipo de publicacao seja em MURAl libera para a publicacao.
	 * @param listaDocumentoProcessoDespachoDecisao 
	 */
	public void liberarPublicacao(LiberacaoPublicacaoDecisao liberacaoPublicacaoDecisao, ProcessoDocumento processoDocumento) throws MuralException {
		liberacaoPublicacaoDecisao.setProcessoDocumento(processoDocumento);
		liberacaoPublicacaoDecisao.setDataCriacao(new Date());
		liberacaoPublicacaoDecisaoDAO.merge(liberacaoPublicacaoDecisao);
		liberacaoPublicacaoDecisaoDAO.flush();
		executaTransicaoProximaTarefa();
	}

	
	/**
	 * @return recupera a varivel de fluxo pje:fluxo:horaLimitePublicacao -
	 *         Varivel com contedo de formato de hora "hh:mm:ss".
	 */
	public String recuperarVariavelFluxoDefineHoraLimitePublicacao() {
		String variavel = (String) this.tramitacaoProcessualService.recuperaVariavelTarefa(br.jus.cnj.pje.nucleo.Variaveis.VARIAVEL_FLUXO_DEFINE_HORA_LIMITE_PUBLICACAO);
		return variavel;
	}

	/**
	 * @return Date com base na varivel de fluxo pje:fluxo:horaLimitePublicacao
	 *         - Varivel com contedo de formato de hora "hh:mm:ss".
	 */
	public Date horaLimitePublicacao() {
		try {
			String variavel = recuperarVariavelFluxoDefineHoraLimitePublicacao();
			Calendar dataHoje = new GregorianCalendar();
			dataHoje.setTime(new Date());
			
			Calendar dataHoraLimite = Calendar.getInstance();
			dataHoraLimite.setTime(DateUtil.stringToDate(variavel, "hh:mm:ss"));
			dataHoraLimite.set(Calendar.YEAR, dataHoje.get(Calendar.YEAR));
			dataHoraLimite.set(Calendar.MONTH, dataHoje.get(Calendar.MONTH));
			dataHoraLimite.set(Calendar.DATE, dataHoje.get(Calendar.DATE));
			return dataHoraLimite.getTime();
		} catch (Exception e) {
			return new Date();
		}
	}

	/**
	 * Verifica se ultrapassou a hora limite para publicao
	 */
	public void verificaUltrapassouHoraLimitePublicacao(TipoPublicacaoEnum tipoPublicacaoEnum) throws Exception {
		Date horaLimitePublicar = horaLimitePublicacao();
		Date dataHoraAtual = new Date();

		if (tipoPublicacaoEnum != null && tipoPublicacaoEnum == TipoPublicacaoEnum.SESSAO && dataHoraAtual.after(horaLimitePublicar)) {
			throw new Exception("Ultrapassou a hora limite para publicao");
		}
	}

	/**
	 * Obtem uma liberacao por determinado id de documento.
	 * 
	 * @param idDocumento
	 * @return Liberacao
	 */
	public LiberacaoPublicacaoDecisao obterPorIdDocumento(Integer idDocumento) {
		return liberacaoPublicacaoDecisaoDAO.obterPorIdDocumento(idDocumento);
	}

	/**
	 * Aps registrar as informaes prestadas pelo usurio, executa, caso exista, o evento
	 * taskInstanceUtil.setFrameDefaultTransition('NOME_DA_PROX_TAREFA')}.
	 */
	private void executaTransicaoProximaTarefa() throws MuralException {
		String transicaoSaida = (String) TaskInstanceUtil.instance().getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		if ((transicaoSaida == null) || (transicaoSaida.isEmpty())) {
			logger.info("Não houve transição de saída padrão configurada");
		} else {
			taskInstanceHome.end(transicaoSaida);
		}
	}
	
	/**
	 * Persiste chamando a camada DAO
	 */
	private boolean gravarLiberarPublicacaoSessao(LiberacaoPublicacaoDecisao liberacao) {
		boolean gravou = Boolean.FALSE;
		if(liberacao.getDataPublicacao() != null && isPrazoValido(liberacao)){
			System.out.println("Nr: "+ liberacao.getNumeroProcesso() + "Data Criacao: "+liberacao.getDataCriacao());
			if(liberacao.getIdLiberacaoPublicacaoDecisao() == 0){
				liberacao.setDataCriacao(new Date());
				liberacaoPublicacaoDecisaoDAO.persist(liberacao);
			} else {
				liberacaoPublicacaoDecisaoDAO.merge(liberacao);
			}
			liberacaoPublicacaoDecisaoDAO.flush();
			gravou = Boolean.TRUE;
		}
		return gravou;
	}
	
	private boolean isPrazoValido(LiberacaoPublicacaoDecisao liberacao){
		boolean retorno = Boolean.FALSE;
		if(liberacao.getTipoPrazo() != null){
			if(liberacao.getTipoPrazo().isPrazoDataCerta() && liberacao.getDataPrazoLegal() != null){
				retorno = Boolean.TRUE;
			} else if (!liberacao.getTipoPrazo().isPrazoDataCerta() && liberacao.getPrazoLegal() != null){
				retorno = Boolean.TRUE;
			} else if(liberacao.getTipoPrazo().isSemPrazo()){
				retorno = Boolean.TRUE;
			}
		}
		return retorno;
	}

	/**
	 * Metodo usado no agendamento
	 * @throws PJeBusinessException 
	 */
	public void verificarControlePublicacaoAgendamento() {
		logger.info("Iniciando publicacao de decioes via agendamento");
		List<LiberacaoPublicacaoDecisao> pendentes = liberacaoPublicacaoDecisaoDAO.obterDecisoesPendentesPublicacao();
		logger.info("Iniciando publicacao de decisoes via agendamento de [{0}]", pendentes.size());
		
		try {
			for (LiberacaoPublicacaoDecisao liberacao : pendentes) {
				if(liberacao.getProcessoDocumento().getAtivo()){
					liberacao.setSituacaoPublicacaoLiberacao(SituacaoPublicacaoLiberacaoEnum.PUBLICADA);
					liberacao.setDataDocPublicado(new Date());
					liberacaoPublicacaoDecisaoDAO.merge(liberacao);
					publicarLiberacaoSessaoMural(liberacao);
				} else {
					excluirInativarDocumentoLiberacao(liberacao);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logger.info("Finalizando publicacao de decisoes via agendamento de [{0}]", pendentes.size());
	}
	
	/**
	 * Metodo que publica as Decisoes em mural ou em sessao.
	 * 
	 * @param publicacoes
	 * @throws PJeBusinessException 
	 */
	@Transactional
	public List<LiberacaoPublicacaoDecisao> publicar(List<LiberacaoPublicacaoDecisao> publicacoes) throws PJeBusinessException {
		logger.info("Iniciando publicacao de [{0}]", publicacoes.size());
		List<LiberacaoPublicacaoDecisao> nrsProcessoNaoGravados = new ArrayList<LiberacaoPublicacaoDecisao>();
		for (LiberacaoPublicacaoDecisao libPublicacao : publicacoes) {
			if(libPublicacao.getDataPublicacao() == null){
				nrsProcessoNaoGravados.add(libPublicacao);
			} else if(DateUtil.isDataMaior(libPublicacao.getDataPublicacao(), new Date())){
				gravarPublicacaoPendente(libPublicacao);
			} else if (DateUtil.isDataMenorIgual(libPublicacao.getDataPublicacao(), new Date())){
				if( libPublicacao.getProcessoDocumento().getAtivo()){
					libPublicacao.setSituacaoPublicacaoLiberacao(SituacaoPublicacaoLiberacaoEnum.PUBLICADA);
					libPublicacao.setDataDocPublicado(new Date());
					
					if(gravarLiberarPublicacaoSessao(libPublicacao)){
						publicarLiberacaoSessaoMural(libPublicacao);
					} else {
						libPublicacao.setSituacaoPublicacaoLiberacao(SituacaoPublicacaoLiberacaoEnum.LIBERADO_PARA_PUBLICACAO);
						nrsProcessoNaoGravados.add(libPublicacao);
					}
				} else {
					excluirInativarDocumentoLiberacao(libPublicacao);
				}
			}
		}
		return nrsProcessoNaoGravados;
	}
	
	private void excluirInativarDocumentoLiberacao(LiberacaoPublicacaoDecisao libPublicacao) {
		logger.info("Documento [{0}] retirado da publicacao", libPublicacao.getProcessoDocumento().getIdProcessoDocumento());
		libPublicacao.setSituacaoPublicacaoLiberacao(SituacaoPublicacaoLiberacaoEnum.INATIVA);
		liberacaoPublicacaoDecisaoDAO.merge(libPublicacao);
		liberacaoPublicacaoDecisaoDAO.flush();
	}

	private void gravarPublicacaoPendente(LiberacaoPublicacaoDecisao libPublicacao){
		libPublicacao.setSituacaoPublicacaoLiberacao(SituacaoPublicacaoLiberacaoEnum.PENDENTE_DE_PUBLICACAO);
		
		if(gravarLiberarPublicacaoSessao(libPublicacao)){
			logger.info("Publicacao para o processo [{0}] agendada pois a data de publicacao e uma data futura", libPublicacao.getNumeroProcesso());
		} else {
			libPublicacao.setSituacaoPublicacaoLiberacao(SituacaoPublicacaoLiberacaoEnum.CRIADA);
		}
	}
	
	private void publicarLiberacaoSessaoMural(LiberacaoPublicacaoDecisao libPublicacao) throws PJeBusinessException {
		PJeException excecao = null;
		try {
			if(libPublicacao.getTipoPublicacao().isMural()) {
				muralService.enviarDadosMural(libPublicacao.getNumeroProcesso(), libPublicacao.getProcessoDocumento().getIdProcessoDocumento());
			}
			logger.info("Criando Expediente para o processo [{0}]", libPublicacao.getNumeroProcesso());
			if(!atoComunicacaoService.intimarDestinatariosPublicacao(libPublicacao.getProcessoDocumento().getProcessoTrf(), libPublicacao.getProcessoDocumento(), libPublicacao)) {
				excecao = new MuralException("Houve problemas na criação dos expedientes e os prazos não serão contados");
			} else {
				logger.info("Sinalizando fluxo de controle de prazo para o processo [{0}]", libPublicacao.getProcessoDocumento().getProcessoTrf().getIdProcessoTrf());
				processoJudicialService.sinalizarFluxo(libPublicacao.getProcessoDocumento().getProcessoTrf(), Variaveis.CONTROLA_PRAZO_PUBLICACAO_SESSAO, true, true, false);
				logger.info("Partes intimadas para o processo [{0}]", libPublicacao.getNumeroProcesso());
			}
		} catch (PJeBusinessException e) {
			logger.info("Houve um erro na intimacao para o processo [{0}]", libPublicacao.getNumeroProcesso());
			excecao = e;
		} catch( MuralException e ) {
			excecao = e;
		}
		if( excecao != null ) {
			excecao.printStackTrace();
			libPublicacao.setSituacaoPublicacaoLiberacao(SituacaoPublicacaoLiberacaoEnum.PENDENTE_DE_PUBLICACAO);
			libPublicacao.setDataDocPublicado(null);
			liberacaoPublicacaoDecisaoDAO.merge(libPublicacao);
			liberacaoPublicacaoDecisaoDAO.flush();
			logger.error(excecao);
			throw new PJeBusinessException(excecao.getCode());
		}

	}
	 
	/**
	 * Obtem uma liberacao por determinado id de documento.
	 * 
	 * @param idDocumento
	 * @return Liberacao
	 */
	public LiberacaoPublicacaoDecisao recuperar(Integer idDocumento){
		return liberacaoPublicacaoDecisaoDAO.recuperar(idDocumento);
	}

	/**
	 * Metodo que grava as publicacoes mas nao publica.
	 * 
	 * @param publicacoes
	 */
	public List<String> gravar(List<LiberacaoPublicacaoDecisao> publicacoes) {
		List<String> nrsProcessoNaoGravados = new ArrayList<String>();
		for (LiberacaoPublicacaoDecisao liberacaoPublicacaoDecisao : publicacoes) {
			boolean gravou = gravarLiberarPublicacaoSessao(liberacaoPublicacaoDecisao);
			if(!gravou){
				nrsProcessoNaoGravados.add(liberacaoPublicacaoDecisao.getNumeroProcesso());
			}
		}
		return nrsProcessoNaoGravados;
	}
	
	private List<LiberacaoPublicacaoDecisao> obterDecisoesJulgadasEmSessao(Date dataSessao){
		List<LiberacaoPublicacaoDecisao> decisoesRetorno = new ArrayList<LiberacaoPublicacaoDecisao>();
		List<LiberacaoPublicacaoDecisao> decisoesEmSessaoComPossiveisExpedientes = liberacaoPublicacaoDecisaoDAO.obterDecisoesJulgadasEmSessao(dataSessao);
		
		decisoesRetorno = recuperaLiberacoesSemExpedienteParaPublicacao(decisoesEmSessaoComPossiveisExpedientes);
		
		for (LiberacaoPublicacaoDecisao liberacao : decisoesRetorno) {
			liberacao.setDataCriacao(new Date());
			liberacao.setTipoDecisaoPublicacao(TipoDecisaoPublicacaoEnum.COLEGIADA);
			liberacao.setTipoPublicacao(TipoPublicacaoEnum.SESSAO);
		}
		return decisoesRetorno;
	}

	/**
	 * Metodo que verifica se tem documento ou documento vinculado para as decioes
	 * 
	 * @param decisoesEmSessaoComPossiveisExpedientes
	 * @return
	 */
	private List<LiberacaoPublicacaoDecisao> recuperaLiberacoesSemExpedienteParaPublicacao(List<LiberacaoPublicacaoDecisao> decisoesEmSessaoComPossiveisExpedientes) {
		List<LiberacaoPublicacaoDecisao> retorno = new ArrayList<LiberacaoPublicacaoDecisao>();
		Integer idDocumento = 0;
		for (LiberacaoPublicacaoDecisao liberacaoPublicacaoDecisao : decisoesEmSessaoComPossiveisExpedientes) {
			idDocumento = liberacaoPublicacaoDecisao.getProcessoDocumento().getIdProcessoDocumento();
			
			boolean existeExpendienteVinculado = processoExpedienteManager.verificarExistenciaExpedientePublicacao(idDocumento, ExpedicaoExpedienteEnum.R,ExpedicaoExpedienteEnum.A);
			
			if(!existeExpendienteVinculado){
				retorno.add(liberacaoPublicacaoDecisao);
			}
		}
		return retorno;
	}
	
	/**
	 * Metodo que pesquisa as Publicacoes obedecendo as seguintes regras:
	 * 	<ul>
	 * 		<li>Verifica se na sessao existem processos ainda nao cadastrados para publicacao para cadastra-los</li>
	 * 	    <li>Faz a pesquisa normalmente</li>
	 *  </ul>
	 * 
	 * @param vo de consulta
	 * @param gravaDecisaoSessao quando o usuario tem este perfil pode trazer os processos (documentos) que ainda nao foram gravados na tabela de liberacao
	 * 	ou seja, pode buscar para dada sessao os processos (documentos)
	 * @return Lista de LiberacaoPublicacaoDecisao
	 */
	public List<LiberacaoPublicacaoDecisao> pesquisar(ConsultaPublicacaoSessaoVO vo, Boolean gravaDecisaoSessao){
		List<LiberacaoPublicacaoDecisao> pesqCadastro = new ArrayList<LiberacaoPublicacaoDecisao>();
		if(gravaDecisaoSessao){
			pesqCadastro.addAll(obterDecisoesJulgadasEmSessao(vo.getDataSessao()));
		}
		pesqCadastro.addAll(liberacaoPublicacaoDecisaoDAO.pesquisar(vo));
		return pesqCadastro;
	}
	
	/**
	 * Metodo que retorna as Decisoes s aptas para serem publicadas. 
	 * 
	 * @return Relatorio de Publicacoes.
	 */
	public List<LiberacaoPublicacaoDecisao> pesquisarRelatorioDecisoesMonocraticasEmSessao(ConsultaPublicacaoSessaoVO consultaVO){
		Cronometro tempo = new Cronometro(); 
		logger.info("Iniciando consulta relatorio decisoes s");
		
		List<LiberacaoPublicacaoDecisao> relatorio = liberacaoPublicacaoDecisaoDAO.pesquisarRelatorioDecisoesMonocraticasEmSessao(consultaVO);
		
		logger.info("Consulta relatorio decisoes s finalizada. Tempo total [{0}/seg]",tempo.getAtual());
		return relatorio;
	}
	
	/**
	 * Metodo que imprime (baixa) o relatorio de decisoes s aptas para serem publicadas em sessao ou em mural.
	 * 
	 * @param publicacoes
	 * @param out
	 */
	public void imprimirRelatorioPublicacao(List<LiberacaoPublicacaoDecisao> publicacoes, OutputStream out){
		Cronometro cronometro = new Cronometro();
		logger.info("Iniciando a construo do pdf de publicacoes com [{0}] liberacoes", publicacoes.size());
		
		Document document = new Document(); 
		try {
			PdfWriter writer = PdfWriter.getInstance(document, out);
			
			IndexEvent event = new IndexEvent();
			writer.setPageEvent(event);
			writer.open();
			
			document.setPageSize(PageSize.A4);
			
			PdfPTable table = criarTabelaLiberacaoPublicacao(publicacoes, document, writer, event);
			document.add(table);
			
			PdfContentByte cb = writer.getDirectContent();
			writeFooter(document, writer, cb, String.valueOf(publicacoes.size()));
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		document.close();
		
		logger.info("Relatorio pdf gerado. Tempo total [{0}/seg]",cronometro.getAtual());
	}
	
	/**
	 * Mtodo que retorna a lista de Datas do tipo de publicacao "Em Sessao"
	 * @return Lista de datas da tabela
	 */
	public List<Date> obterDatasSessoesLiberacao() {
		List<Date> datasSessoesNaoFinalizadas = sessaoManager.getDatasSessoesNaoFinalizadas();
		datasSessoesNaoFinalizadas.addAll(liberacaoPublicacaoDecisaoDAO.obterDatasSessoesLiberacao());
		Set<Date> datasNaoDuplicadas = new HashSet<Date>(datasSessoesNaoFinalizadas);
		return new ArrayList<Date>(datasNaoDuplicadas);
	}
	
	/**
	 * Metodo responsavel por tratar do processo de gravacao de liberacao para quem tem o perfil de gravacao.
	 * 
	 * @param publicacoes
	 * @param publicacoesSelecionadas
	 */
	public void gravarLiberacoes(List<LiberacaoPublicacaoDecisao> publicacoes, List<LiberacaoPublicacaoDecisao> publicacoesSelecionadas) {
		processarLiberacaoParaPublicacao(publicacoesSelecionadas);
		processarAlteracoesTodasPublicacoes(publicacoes, publicacoesSelecionadas);
	}

	private void processarAlteracoesTodasPublicacoes(List<LiberacaoPublicacaoDecisao> publicacoes, List<LiberacaoPublicacaoDecisao> publicacoesSelecionadas) {
		for (LiberacaoPublicacaoDecisao publicacao : publicacoes) {
			if(publicacoesSelecionadas.contains(publicacao)){
				publicacao.setSituacaoPublicacaoLiberacao(SituacaoPublicacaoLiberacaoEnum.LIBERADO_PARA_PUBLICACAO);
				liberacaoPublicacaoDecisaoDAO.persist(publicacao);
			} else if(publicacao.getSituacaoPublicacaoLiberacao() != null && publicacao.getSituacaoPublicacaoLiberacao().isLiberadaPublicacao()){
				publicacao.setSituacaoPublicacaoLiberacao(SituacaoPublicacaoLiberacaoEnum.CRIADA);
				liberacaoPublicacaoDecisaoDAO.persist(publicacao);
			}
			liberacaoPublicacaoDecisaoDAO.flush();
		}
	}

	/**
	 * Metodo que processa as liberacoes colocando elas para serem 'Liberadas para Publicicacao'. 
	 * 
	 * @param publicacoesSelecionadas
	 */
	private void processarLiberacaoParaPublicacao(List<LiberacaoPublicacaoDecisao> publicacoesSelecionadas) {
		for (LiberacaoPublicacaoDecisao selecionada : publicacoesSelecionadas) {
			selecionada.setSituacaoPublicacaoLiberacao(SituacaoPublicacaoLiberacaoEnum.LIBERADO_PARA_PUBLICACAO);
			liberacaoPublicacaoDecisaoDAO.persist(selecionada);
			liberacaoPublicacaoDecisaoDAO.flush();
		}
	}
	
	/**
	 * Metodo que verifica a disponibilidade dos servicos Mural e PjeServicos
	 * 
	 * @return true se os dois servicos estiverem disponiveis
	 */
	public boolean verificarServicosDisponiveisParaPublicacao(){
		boolean muralDisponivel = false;
		boolean pjeServicoDisponivel = false;
		try {
			String urlMural = parametroUtil.recuperarUrlServicoMural();
			String urlPjeServicos = parametroUtil.recuperarUrlPjeServico();
			
			muralDisponivel = muralService.verificarServicoDisponivel(2500, urlMural);
			pjeServicoDisponivel = UrlUtil.isUrlValida(urlPjeServicos, 2500);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pjeServicoDisponivel && muralDisponivel;
	}

	private PdfPTable criarTabelaLiberacaoPublicacao(List<LiberacaoPublicacaoDecisao> publicacoes, Document document, PdfWriter writer, IndexEvent event)
			throws MalformedURLException, IOException, DocumentException {
		
		PdfPTable table = new PdfPTable(new float[] { 0.2f, 0.2f, 0.2f, 0.3f, 0.1f });
		table.setWidthPercentage(100);
		table.setHorizontalAlignment(Element.ALIGN_CENTER);
		
		document.open();
		
		final int tamanhoFontCabecalho = 9;
		final int tamanhoFontCelula = 8;
		
		PdfContentByte cb = writer.getDirectContent();
		
		writeCabecalho(document, writer, cb, ParametroUtil.getParametro(Parametros.NOME_SECAO_JUDICIARIA));
		
		table.addCell(getHeaderCell("Processo", tamanhoFontCabecalho));
		table.addCell(getHeaderCell("Classe Judicial", tamanhoFontCabecalho));
		table.addCell(getHeaderCell("Origem", tamanhoFontCabecalho));
		table.addCell(getHeaderCell("Ministro(a) Relator(a)", tamanhoFontCabecalho));
		table.addCell(getHeaderCell("Decisão", tamanhoFontCabecalho));
		
		for (LiberacaoPublicacaoDecisao libPubDec : publicacoes) {
			ProcessoTrf trf = libPubDec.getProcessoDocumento().getProcessoTrf();
			List<PessoaMagistrado> magistradosAptos = pessoaMagistradoManager.obterAptos(trf.getOrgaoJulgador().getIdOrgaoJulgador());
			
			table.addCell(getNormalCell(libPubDec.getNumeroProcesso(), null, tamanhoFontCelula));
			table.addCell(getNormalCell(trf.getClasseJudicialStr(), null, tamanhoFontCelula));
			table.addCell(getNormalCell(trf.getComplementoJEUfMunicipioExtenso(), null, tamanhoFontCelula));
			table.addCell(getNormalCell(magistradosAptos.get(0).getNome(), null, tamanhoFontCelula));
			table.addCell(getNormalCell(libPubDec.getTipoDecisaoPublicacao().getLabel(), null, tamanhoFontCelula));
			
			event.body = true;
		}
		return table;
	}
	
	private void writeCabecalho(Document document, PdfWriter writer, PdfContentByte cb, String justica) throws MalformedURLException, IOException, DocumentException{
		PdfOutline root = cb.getRootOutline();

		document.add(new Chunk(" ").setLocalDestination("-1"));

		new PdfOutline(root, PdfAction.gotoLocalPage("-1", false), "Cabeçalho");

		float[] larguras = new float[]{12f, 88f};
		PdfPTable tabTitulo = new PdfPTable(larguras);
		tabTitulo.setWidthPercentage(100);

		Image brasao = Image.getInstance(FileUtil.getContent(getClass().getResourceAsStream("/META-INF/images/brasaoMiniPDF.png")));

		brasao.scalePercent(60f);
		brasao.setAlignment(Image.LEFT | Image.TEXTWRAP);
		brasao.setIndentationRight(5f);
		brasao.setSpacingAfter(140);
		PdfPCell ce = new PdfPCell(brasao);
		ce.setBorderColor(Color.WHITE);
		ce.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		tabTitulo.addCell(ce);

		Font fontCabec = new Font(Font.HELVETICA, 11, Font.NORMAL);
		Font fontCorpo = new Font(Font.HELVETICA, 10, Font.NORMAL);
		
		Paragraph tit = new Paragraph(justica, fontCabec);
		tit.add(new Phrase("\nPJe - Processo Judicial Eletrônico", fontCabec));
		PdfPCell cd = new PdfPCell(tit);
		cd.setBorderColor(Color.WHITE);
		tabTitulo.addCell(cd);
		
		Paragraph localData = new Paragraph(DateUtil.dateToString(new Date()) + "\n", fontCorpo);
		localData.setAlignment(Paragraph.ALIGN_RIGHT);
		document.add(tabTitulo);

		Paragraph textoHeader = new Paragraph(new Phrase("Relatório de Decisões Monocráticas",fontCabec));
		textoHeader.setAlignment(Paragraph.ALIGN_CENTER);
		textoHeader.setSpacingAfter(2.0f);
		
		document.add(textoHeader);
	}
	
	private void writeFooter(Document document, PdfWriter writer, PdfContentByte cb, String valueFooter) throws DocumentException{
		PdfOutline root = cb.getRootOutline();
		document.add(new Chunk(" ").setLocalDestination("-1"));
		new PdfOutline(root, PdfAction.gotoLocalPage("-1", false), "Cabeçalho");
		
		Font fontCabec = new Font(Font.HELVETICA, 11, Font.BOLD);
		
		Paragraph paragrafoRodape = new Paragraph("Total de registros:", fontCabec);
		paragrafoRodape.add(new Phrase(valueFooter, fontCabec));
		paragrafoRodape.setAlignment("Center");
		document.add(paragrafoRodape);
	}
	
	public static PdfPCell getNormalCell(String string, String language, float size) throws DocumentException, IOException {
        if(string != null && "".equals(string)){
            return new PdfPCell();
        }
        Font f  = getFontForThisLanguage(language);
        f.setSize(size);
        PdfPCell cell = new PdfPCell(new Phrase(string, f));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }
	
	public static PdfPCell getHeaderCell(String string, float size) throws DocumentException, IOException {
        if(string != null && "".equals(string)){
            return new PdfPCell();
        }
        Font f  = getFonteHeader();
        f.setSize(size);
        PdfPCell cell = new PdfPCell(new Phrase(string, f));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

	public static Font getFonteHeader(){
		return FontFactory.getFont(BaseFont.TIMES_BOLD, null, true);
	}
	
	public static Font getFontForThisLanguage(String language) {
        if ("czech".equals(language)) {
            return FontFactory.getFont(BaseFont.TIMES_ROMAN, "Cp1250", true);
        }
        if ("greek".equals(language)) {
            return FontFactory.getFont(BaseFont.TIMES_ROMAN, "Cp1253", true);
        }
        return FontFactory.getFont(BaseFont.TIMES_ROMAN, null, true);
    }
	
	public LiberacaoPublicacaoDecisaoDAO getLiberacaoPublicacaoDecisaoDAO() {
		return liberacaoPublicacaoDecisaoDAO;
	}

	public void setLiberacaoPublicacaoDecisaoDAO(LiberacaoPublicacaoDecisaoDAO liberacaoPublicacaoDecisaoDAO) {
		this.liberacaoPublicacaoDecisaoDAO = liberacaoPublicacaoDecisaoDAO;
	}

	public TaskInstanceHome getTaskInstanceHome() {
		return taskInstanceHome;
	}

	public void setTaskInstanceHome(TaskInstanceHome taskInstanceHome) {
		this.taskInstanceHome = taskInstanceHome;
	}

	public SessaoManager getSessaoManager() {
		return sessaoManager;
	}

	public void setSessaoManager(SessaoManager sessaoManager) {
		this.sessaoManager = sessaoManager;
	}

	public boolean verificarExistenciaLiberacao(ProcessoDocumento processoDocumento) {
		return liberacaoPublicacaoDecisaoDAO.verificarExistenciaLiberacao(processoDocumento);
	}
}