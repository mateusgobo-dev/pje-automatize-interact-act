package br.com.infox.pje.processor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.hibernate.Session;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jbpm.JbpmContext;
import org.jbpm.persistence.db.DbPersistenceService;
import org.quartz.SchedulerException;

import br.com.infox.cliente.home.CaixaFiltroHome;
import br.com.infox.cliente.home.PainelUsuarioHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.TarefaTree;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.service.LogService;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.service.PautaJulgamentoService;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.action.AbaPautaJulgamentoAction;
import br.com.jt.pje.manager.HistoricoSituacaoSessaoManager;
import br.com.jt.pje.manager.PautaSessaoManager;
import br.com.jt.pje.manager.SessaoManager;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.enums.SituacaoSessaoEnum;
import br.jus.pje.nucleo.entidades.CaixaFiltro;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.util.Crypto;
import br.jus.pje.nucleo.util.DateUtil;

@Name(SessaoJTFechamentoPautaProcessor.NAME)
@AutoCreate
public class SessaoJTFechamentoPautaProcessor {
	
	@Logger
	private static Log log;
	public final static String NAME = "sessaoJTFechamentoPautaProcessor";

	@In
	private LogService logService;

	@In
	private GenericManager genericManager;
	@In
	private PautaSessaoManager pautaSessaoManager;
	@In
	private SessaoManager sessaoManager;
	@In
	private HistoricoSituacaoSessaoManager historicoSituacaoSessaoManager;
	@In
	private PautaJulgamentoService pautaJulgamentoService;
	
	@In
	private AbaPautaJulgamentoAction abaPautaJulgamentoAction;

	
	private String numeroProcesso; 
	private String orgaoJulgadorColegiado;
	private String sessaoJudiciaria;
	private Date dataSessao;
	private Date horaSessao;
	private String salaSessao;

	public SessaoJTFechamentoPautaProcessor(){
	}
	
	public static SessaoJTFechamentoPautaProcessor instance() {
		return (SessaoJTFechamentoPautaProcessor) Component.getInstance(NAME);
	}

	/**
	 * Metodo utilizado para fechamento de pauta automatico
	 * @param inicio
	 * @param cron
	 * @return
	 * @throws SchedulerException
	 */
	@Asynchronous
	@Transactional
	public QuartzTriggerHandle fecharPautaAutomatico(@Expiration Date inicio,
													@IntervalCron String cron) {
		
		// PJEII-4881  Tratamento de excecao para evitar que a aplicação não inicie.
		try {
			fecharPautaAutomatico();
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), "fecharPautaAutomatico");
		}
		return null;
	}
	
	public Object fecharPautaAutomatico() {
		Usuario usuarioSistema = ParametroUtil.instance().getUsuarioSistema();
		
		List<SessaoJT> sessoesDoDiaCorrente = new ArrayList<SessaoJT>();
		sessoesDoDiaCorrente.addAll(sessaoManager.getSessoesComDataFechamentoPautaDiaCorrente());
		for (SessaoJT sessaoJT : sessoesDoDiaCorrente) {
			try{
				
				fecharPauta(usuarioSistema, sessaoJT);
				
			} catch (Exception e){
				
				e.printStackTrace();
				//Tem que sequir para o póximo
				if(sessaoJT != null ){
					log.error("[ERRO FECHAR PAUTA] Erro ao Fechar Pauta com Id: " + sessaoJT.getIdSessao());
				}
			}
			
		}
		
		return null;
	}

	public void fecharPauta(Usuario usuario, SessaoJT sessao) {
		sessaoJudiciaria = ParametroUtil.getFromContext(Parametros.NOME_SECAO_JUDICIARIA, true);
		List<PautaSessao> lista = pautaSessaoManager.listaPautaSessaoBySessao(sessao);
		List<PautaSessao> processosPautaSessaoInclusaoPA = pautaSessaoManager.getProcessosPautaSessaoInclusaoPA(sessao);
		List<ProcessoTrf> listaProcessoMovimentado = new ArrayList<ProcessoTrf>();
		Long idTaskInstace = null;
		
		//ISSUE PJEII-6416 e 4255 - Realizada em 28/05/2013 por Rafael Barros
		//Evita erro de lazy initialization que estava ocorrendo
		sessao = sessaoManager.find(SessaoJT.class, sessao.getIdSessao());
		usuario = ParametroUtil.instance().getUsuarioSistema();
		
		//ATUALIZAR SESSAO
		orgaoJulgadorColegiado = sessao.getOrgaoJulgadorColegiado().getOrgaoJulgadorColegiado();
		
		//ISSUE PJEII-6416 e 4255 - Realizada em 28/05/2013 por Rafael Barros
		dataSessao = sessao.getDataSessao();				
		horaSessao = sessao.getSalaHorario().getHoraInicial();		
		salaSessao = sessao.getSalaHorario().getSala().getSala();
				
		for (PautaSessao pautaSessao : lista){
			
			try{
				
				idTaskInstace = pautaJulgamentoService.fecharPauta(usuario, sessao, listaProcessoMovimentado, idTaskInstace, pautaSessao, processosPautaSessaoInclusaoPA);
				
			} catch (Exception e){
				Util.beginTransaction();
				JbpmContext currentJbpmContext = ManagedJbpmContext.instance();
				DbPersistenceService dbPersistenceService = (DbPersistenceService) currentJbpmContext.getServices().getPersistenceService();
				e.printStackTrace();
				
				if(pautaSessao != null){
					pautaSessao = pautaSessaoManager.recuperarProcessosPorId(pautaSessao);
					abaPautaJulgamentoAction.removerProcessoDePauta(pautaSessao);
				}
				
				if(pautaSessao != null && pautaSessao.getProcessoTrf() != null && pautaSessao.getProcessoTrf().getNumeroProcesso() != null){
				
					log.error("[FECHAR PAUTA] Erro ao fechar pauta: processo " + pautaSessao.getProcessoTrf().getNumeroProcesso() + " não incluido", e);
					
					if(FacesContext.getCurrentInstance() != null){
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao movimentar processo " + 
						pautaSessao.getProcessoTrf().getNumeroProcesso(), "Erro"));
					}

				} else {
					
					log.error("[FECHAR PAUTA] Erro ao fechar pauta: processo não incluido", e);
					
					if(FacesMessages.instance() != null){
						FacesMessages.instance().add(Severity.ERROR, "Erro ao movimentar processo ");
					}
				}
				
				Session s = dbPersistenceService.getSession();
				s.flush();
				// Commita a transação do Seam e remove a associação entra a thread corrente e a transação.
				// Por conta disso, antes de qualquer outra consulta a banco, é necessário chamar o Util.beginTransaction();
				Util.commitTransction();
			}
		}
		
		// Iniciar uma transação, se não houver transação ativa.
		Util.beginTransaction();
		
		historicoSituacaoSessaoManager.gravarHistorico(sessao);
		//Atualiza status da sessao
		sessao.setSituacaoSessao(SituacaoSessaoEnum.S);
		sessao.setDataSituacaoSessao(new Date());
		sessao.setUsuarioSituacaoSessao(usuario);
		sessaoManager.update(sessao);
		
		//CRIA CAIXA
		if(idTaskInstace != null && listaProcessoMovimentado != null && listaProcessoMovimentado.size() > 0 && sessao != null){
			
			CaixaFiltro caixa = criarCaixa(sessao, listaProcessoMovimentado, idTaskInstace);
			//ADICIONA A LISTA DE PROCESSOS NA CAIXA
			adicionaProcessoCaixa(listaProcessoMovimentado, caixa);
		}
	}


	private void adicionaProcessoCaixa(List<ProcessoTrf> listaProcessoMovimentado, CaixaFiltro caixa) {
		
		
		//INJETA PROCESSOS NA CAIXA				
		List<Integer> listaIdProcesso = new ArrayList<Integer>();
		for (ProcessoTrf processo : listaProcessoMovimentado){
			if(processo != null){
				listaIdProcesso.add(processo.getIdProcessoTrf());
			}
		}
		
		TarefaTree tree = ComponentUtil.getComponent("tarefaTree");
		
		if(tree != null && tree.getRoots() != null && tree.getRoots().size() > 0 ){
			
			Usuario usuario = (Usuario) Contexts.getSessionContext().get("usuarioLogado");
			
			PainelUsuarioHome painelUsuarioHome = PainelUsuarioHome.instance();
			painelUsuarioHome.setAtualizaGridCaixas(usuario != null);
			painelUsuarioHome.setProcessoCaixa(listaIdProcesso, caixa);
		}
	}

	private CaixaFiltro criarCaixa(SessaoJT sessao,
			List<ProcessoTrf> listaProcessoMovimentado, Long idTaskInstace) {
		pautaJulgamentoService.iniciarHomesProcessos(listaProcessoMovimentado.get(0));
		pautaJulgamentoService.iniciarBusinessProcess(idTaskInstace);
		
		OrgaoJulgadorColegiado colegiado = getOrgaoJulgadorColegiado(listaProcessoMovimentado);

		String defaultTransition = (String) TaskInstanceUtil.instance().getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		Integer idTask = pautaJulgamentoService.obtemIdTaskPorProcesso(listaProcessoMovimentado.get(0), defaultTransition);
		
		String nomeCaixa = obterNomeCaixa(sessao);
		CaixaFiltro caixa = CaixaFiltroHome.instance().addCaixaComRetorno(idTask, nomeCaixa, colegiado);
		return caixa;
	}

	private OrgaoJulgadorColegiado getOrgaoJulgadorColegiado(
			List<ProcessoTrf> listaProcessoMovimentado) {
		OrgaoJulgadorColegiado ojc = null;
		
		for (ProcessoTrf processoTrf : listaProcessoMovimentado) {
			if (processoTrf != null && processoTrf.getOrgaoJulgadorColegiado() != null) {
				ojc = processoTrf.getOrgaoJulgadorColegiado();
			}
		}
		
		if (ojc == null) {
			ojc = Authenticator.getOrgaoJulgadorColegiadoAtual();
		}
		
		return ojc;
	}

	public String obterNomeCaixa(SessaoJT sessao){
		
		String nomeCaixa = null; 
		
		if(sessao.getDataSessao() != null && sessao.getSalaHorario() != null && sessao.getSalaHorario().getSala() != null){
			
			nomeCaixa = DateUtil.dateToString(sessao.getDataSessao()) + " " + DateUtil.dateToHour(sessao.getSalaHorario().getHoraInicial()) + " - " + sessao.getSalaHorario().getSala();
			
		} else {
			
			nomeCaixa = "Caixa criada em " + DateUtil.dateToString(new Date());
		}
		return nomeCaixa;
	}

	public void atualizaSessao(Usuario usuario, SessaoJT sessao) throws Exception{
		lancarMovimentoInclusaoPauta(sessao);
		
		/*
		 * PJEII-1880 : Resolução do problema de "não substituição de expressões/variáveis" corretamente
		 * Obtém os dados utilizados nas variáveis ANTES de intimar as partes.
		 */
		
		orgaoJulgadorColegiado = sessao.getOrgaoJulgadorColegiado().getOrgaoJulgadorColegiado();
		dataSessao = sessao.getDataSessao();
		horaSessao = sessao.getSalaHorario().getHoraInicial();
		salaSessao = sessao.getSalaHorario().getSala().getSala();
		
		intimarPartesProcessosInclusaoPA(usuario, sessao);
		
		historicoSituacaoSessaoManager.gravarHistorico(sessao);
		//Atualiza status da sessao
		sessao.setSituacaoSessao(SituacaoSessaoEnum.S);
		sessao.setDataSituacaoSessao(new Date());
		sessao.setUsuarioSituacaoSessao(usuario);
		sessaoManager.update(sessao);
	}
	
	private void lancarMovimentoInclusaoPauta(SessaoJT sessao){
		List<ProcessoTrf> listProcessosEmPauta = new ArrayList<ProcessoTrf>();
		List<PautaSessao> listPautaSessao = new ArrayList<PautaSessao>();
		listPautaSessao.addAll(pautaSessaoManager.listaPautaSessaoBySessao(sessao));
		for(PautaSessao ps : listPautaSessao){
			listProcessosEmPauta.add(ps.getProcessoTrf());
		}
		
		for (ProcessoTrf processoTrf : listProcessosEmPauta) {
			//Lança movimentos de inclusão de Pauta
			pautaSessaoManager.lancarMovimentoInclusaoPauta(processoTrf.getProcesso(),sessao);
			
			numeroProcesso = processoTrf.getProcesso().getNumeroProcesso(); 
		}
	}
	
	public void lancarMovimentoInclusaoPautaPorProcesso(ProcessoTrf processo,SessaoJT sessao) throws Exception{
			pautaSessaoManager.lancarMovimentoInclusaoPauta(processo.getProcesso(),sessao);
//			throw new Exception("Erro Simulado");
	}

	private void intimarPartesProcessosInclusaoPA(Usuario usuario, SessaoJT sessao)throws Exception{
		List<ProcessoTrf> listProcessosInclusaoPA = new ArrayList<ProcessoTrf>();
		List<PautaSessao> listPautaSessao = new ArrayList<PautaSessao>();
		listPautaSessao.addAll(pautaSessaoManager.getProcessosPautaSessaoInclusaoPA(sessao));
		for(PautaSessao ps : listPautaSessao){
			listProcessosInclusaoPA.add(ps.getProcessoTrf());
		}
		
		for (ProcessoTrf processoTrf : listProcessosInclusaoPA) {
			/*
			 * PJEII-1880 : Resolução problema de "não substituição de expressões/variáveis" corretamente
			 * Atualiza propriedade numeroProcesso antes de intimar.
			 */
			numeroProcesso = processoTrf.getProcesso().getNumeroProcesso(); 
			
			intimacaoDePauta(usuario, sessao, processoTrf);
		}
	}

	public void intimacaoDePauta(Usuario usuario, SessaoJT sessao, ProcessoTrf processoTrf) throws Exception{
		try{
			//Persist Processo Documento
			ProcessoDocumentoBin bin = persistBin(usuario);
			ProcessoDocumento processoDocumento = persistDocumento(processoTrf, bin, usuario);
			
			//Persist Processo Expediente
			ProcessoExpediente processoExpediente =  persistExpediente(processoTrf, sessao, processoDocumento);
			
			//GERA O PROCESSO EXPEDIENTE PARA O DOCUMENTO PROCESSOPARTEEXPEDIENTE
			ProcessoDocumentoExpediente pde = new ProcessoDocumentoExpediente();
			pde.setProcessoDocumento(processoDocumento);
			pde.setProcessoExpediente(processoExpediente);
			pde.setAnexo(false);
			genericManager.persist(pde);
			//---------------------------------
			
			//Intima as Partes do processo
			intimarPartesProcesso(processoTrf, processoExpediente,  sessao.getOrgaoJulgadorColegiado().getDiaCienciaInclusaoPauta());

			if (DomicilioEletronicoService.instance().isIntegracaoHabilitada()) {
				DomicilioEletronicoService.instance().enviarExpedientesAsync(Arrays.asList(processoExpediente));
			}
		}catch(Exception e){
			throw e;
		}
	}
	
	private ProcessoDocumentoBin persistBin(Usuario usuario){
		ProcessoDocumentoBin bin = new ProcessoDocumentoBin();
		bin.setDataInclusao(new Date());
		bin.setModeloDocumento(processarModelo(ParametroUtil.instance().getModeloIntimacaoPauta().getModeloDocumento()));
		bin.setUsuario(usuario);
		
		return genericManager.persist(bin);
	}
	
	private ProcessoDocumento persistDocumento(ProcessoTrf processoTrf, ProcessoDocumentoBin bin, Usuario usuario){
		ProcessoDocumento processoDocumento = new ProcessoDocumento();
		processoDocumento.setProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoIntimacaoPauta().getTipoProcessoDocumento());
		processoDocumento.setProcesso(processoTrf.getProcesso());
		processoDocumento.setUsuarioInclusao(usuario);
		processoDocumento.setDataInclusao(new Date());
		processoDocumento.setAtivo(Boolean.TRUE);
		processoDocumento.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoIntimacaoPauta());
		processoDocumento.setDocumentoSigiloso(Boolean.FALSE);
		processoDocumento.setProcessoDocumentoBin(bin);
		
		return genericManager.persist(processoDocumento);
	}
	
	private ProcessoExpediente persistExpediente(ProcessoTrf processoTrf, SessaoJT sessao, ProcessoDocumento processoDocumento){
		ProcessoExpediente processoExpediente =  new ProcessoExpediente();
		processoExpediente.setProcessoTrf(processoTrf);
		processoExpediente.setDtCriacao(new Date());
		processoExpediente.setInTemporario(true);
		processoExpediente.setProcessoDocumento(processoDocumento);
		processoExpediente.setMeioExpedicaoExpediente(ExpedicaoExpedienteEnum.E);
		processoExpediente.setUrgencia(Boolean.FALSE);
		processoExpediente.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoIntimacaoPauta());
		processoExpediente.setSessaoJT(sessao);
		
		return genericManager.persist(processoExpediente);
	}
	
	private void intimarPartesProcesso(ProcessoTrf processoTrf, ProcessoExpediente processoExpediente, int prazoLegal){
//		throw new BusinessException("simular Erro!");
		ProcessoParteExpediente ppe = null;
		for (ProcessoParte parteAtivo : processoTrf.getListaParteAtivo()) {
			ppe = new ProcessoParteExpediente();
			ppe.setPessoaParte(parteAtivo.getPessoa());
			ppe.setProcessoExpediente(processoExpediente);
			ppe.setProcessoJudicial(processoTrf);
			ppe.setPrazoLegal(prazoLegal);
			genericManager.persist(ppe);
		}
		
		for (ProcessoParte partePassivo : processoTrf.getListaPartePassivo()) {
			ppe = new ProcessoParteExpediente();
			ppe.setPessoaParte(partePassivo.getPessoa());
			ppe.setProcessoExpediente(processoExpediente);
			ppe.setProcessoJudicial(processoTrf);
			ppe.setPrazoLegal(prazoLegal);
			genericManager.persist(ppe);
		}
	}
	
	/**
	 * Processa um modelo avaliando linha a linha.
	 * @param modelo
	 * @return
	 */
	public static String processarModelo(String modelo) {
		if (modelo != null) {
			StringBuilder modeloProcessado = new StringBuilder();		
			String[] linhas = modelo.split("\n");
			for (int i = 0; i < linhas.length; i++) {
				if (modeloProcessado.length() > 0) {
					modeloProcessado.append('\n');
				}
				String linha = linhas[i];
				try {
					linha = (String) Expressions.instance()
						.createValueExpression(linhas[i]).getValue();
				} catch (RuntimeException e) {
					log.warn("Erro ao avalizar expressão na linha: '" + 
							linha + "': " + e.getMessage());
				}
				modeloProcessado.append(linha);
			}
			return modeloProcessado.toString();
		}
		return modelo;
	}
	
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public String getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public String getSessaoJudiciaria() {
		return sessaoJudiciaria;
	}

	public String getDataSessao() {
		//ISSUE PJEII-6416 e 4255 - Realizada em 06/06/2013 por Rafael Barros
		return DateUtil.dateToString(dataSessao);
	}

	public String getHoraSessao() {
		//ISSUE PJEII-6416 e 4255 - Realizada em 06/06/2013 por Rafael Barros
		return DateUtil.dateToHour(horaSessao);
	}

	public String getSalaSessao() {
		return salaSessao;
	}
	
	public String getDataSessaoFormatada() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy");
		return sdf.format(dataSessao);
	}
	
	public String getHoraSessaoFormatada() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		return sdf.format(horaSessao);
	}

	public PautaJulgamentoService getPautaJulgamentoService(){
		return pautaJulgamentoService;
	}

	public void setPautaJulgamentoService(PautaJulgamentoService pautaJulgamentoService){
		this.pautaJulgamentoService = pautaJulgamentoService;
	}
}
