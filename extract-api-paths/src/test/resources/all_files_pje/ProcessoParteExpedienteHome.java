package br.com.infox.cliente.home;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.DAO.EntityList;
import br.com.infox.cliente.actions.PaginatorAction;
import br.com.infox.cliente.component.tree.AssuntoTrfTreeHandler;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.component.tree.EntidadeTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.pje.manager.ProcessoDocumentoTrfLocalManager;
import br.com.itx.component.Util;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.passos.CienciaAutomatizadaDiarioEletronico;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.PublicadorDJE;
import br.jus.cnj.pje.extensao.auxiliar.AvisoRecebimentoECT;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.LocalizacaoService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.pje.nucleo.entidades.CalendarioEvento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaExpediente;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteEndereco;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.enums.TipoResultadoAvisoRecebimentoEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name("processoParteExpedienteHome")
public class ProcessoParteExpedienteHome extends AbstractProcessoParteExpedienteHome<ProcessoParteExpediente> {

	private static final long serialVersionUID = 1L;
	private List<ProcessoParte> partesList = new ArrayList<ProcessoParte>(0);
	private List<ProcessoParte> partesListTodos = new ArrayList<ProcessoParte>(0);
	private Date dataAutuacao;
	private Date dataDistribuicao;
	private TipoPessoa tipoPessoa;
	private String nomeParte;
	private String numeroProcesso;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private OrgaoJulgador orgaoJulgador;
	private Map<Integer, Boolean> semCienciaMap;
	private Boolean allCheckbox = Boolean.FALSE;
	private List<ProcessoParteExpediente> processoParteExpedienteList = new ArrayList<ProcessoParteExpediente>();
	private ProcessoDocumento documentoExpediente = null;
	private ProcessoParteExpediente expedienteSelecionado;
	private Long idProcessoParteExpedienteCiencia;
	private AvisoRecebimentoECT avisoRecebimentoECT;
	private String htmlRastreioCorrespondencia;
	
	@In
	private AtoComunicacaoService atoComunicacaoService;
	
	@In
	private CienciaAutomatizadaDiarioEletronico cienciaAutomatizadaDiarioEletronico;

	@In
	private LocalizacaoService localizacaoService;  

	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	
	@In
	private ProcessoExpedienteManager processoExpedienteManager;
	
	@In
	private ProcessoParteExpedienteManager processoParteExpedienteManager;
	
	@In
	private ProcessoDocumentoExpedienteManager processoDocumentoExpedienteManager;
	
	@In
	private ProcessoDocumentoTrfLocalManager processoDocumentoTrfLocalManager;
	
	@In(create = true, required = false)
	private PublicadorDJE publicadorDJE;
	@In(create = true, required = false)
	private PrazosProcessuaisService prazosProcessuaisService;
	
	@Logger
	private Log log;
	
	public boolean validarUsuarioInterno(ProcessoTrf processoTrf){
		return localizacaoService.validarUsuarioInterno(processoTrf);
	}
	
	public void limparSearchDoAgrupador(){  
		setNumeroProcesso(null);  
		setNomeParte(null);  
		setDataAutuacao(null);  
		setDataDistribuicao(null);  
		setTipoPessoa(null);  
		ConsultaProcessoHome consultaProcessoHome = ComponentUtil.getComponent("consultaProcessoHome");  
		consultaProcessoHome.newInstance();  
	} 
	
	public void limparNumeroProcessoEntidadeClasseJuducialEAssunto() {

		tipoPessoa = null;
		numeroProcesso = null;

		ConsultaProcessoHome consultaProcessoHome = ComponentUtil.getComponent("consultaProcessoHome");
		consultaProcessoHome.limparClasseJuducialEAssunto();

	}

       public void limparNumeroProcesso() {		
		ConsultaProcessoHome instanceCPH = ComponentUtil.getComponent("consultaProcessoHome");
		instanceCPH.setProcessoTrf(null);
		instanceCPH.setNumeroProcesso(null);
    	numeroProcesso = null;
		tipoPessoa = null;
		orgaoJulgadorColegiado = null;
		orgaoJulgador = null;
		ConsultaProcessoHome.instance().getInstance().setClasseJudicial(null);
		ConsultaProcessoHome.instance().getInstance().setAssuntoTrf(null);		
		limparTrees();	
	}

	private void limparTrees() {
		ClasseJudicialTreeHandler classeTree = getComponent("classeJudicialTree");
		classeTree.clearTree();
		AssuntoTrfTreeHandler assuntoTree = getComponent("assuntoTrfProcessoTree");
		assuntoTree.clearTree();
		EntidadeTreeHandler entidadeTree = getComponent("entidadeTree");
		entidadeTree.clearTree();
	}

	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	public PaginatorAction paginatorAction() {
		return ComponentUtil.getComponent(PaginatorAction.NAME);
	}

	@Override
	public void newInstance() {
		setPartesList(new ArrayList<ProcessoParte>(0));
		processoParteExpedienteList = new ArrayList<ProcessoParteExpediente>();
		limparChecks();
		setInstance(new ProcessoParteExpediente());
	}

	public void verificarChecks(ProcessoParte obj) {
		if ((obj.getPrazoLegal() == null) && (obj.getPrazoProcessual() == null)) {
			partesList.remove(obj);
			obj.setCheckado(Boolean.FALSE);
		}
	}

	public void inserirParte(ProcessoParte obj) {
		if ((obj.getPrazoLegal() == null) && (obj.getPrazoProcessual() == null)) {
			FacesMessages.instance().add(Severity.ERROR, "Pelo menos um dos Prazos devem estar Preenchidos.");
			obj.setCheckado(Boolean.FALSE);
		} else {
			if (obj.getCheckado()) {
				partesList.add(obj);
			} else {
				partesList.remove(obj);
			}
		}
	}

	public boolean verificarPrazosNulos(ProcessoParte obj) {
		Boolean resultado = Boolean.TRUE;
		if ((obj.getPrazoLegal() != null) || (obj.getPrazoProcessual() != null)) {
			resultado = Boolean.FALSE;
		}

		if (resultado) {
			obj.setCheckado(Boolean.FALSE);
		}
		return resultado;
	}

	public void limparChecks() {
		for (ProcessoParte partesL : partesList) {
			partesL.setCheckado(Boolean.FALSE);
			partesL.setPrazoLegal(null);
			partesL.setPrazoProcessual(null);
		}
	}

	public static ProcessoParteExpedienteHome instance() {
		return ComponentUtil.getComponent("processoParteExpedienteHome");
	}

	public void atualizar() {
		ProcessoExpediente processoExpediente = ProcessoExpedienteHome.instance().getInstance();

		// Atualizar ou remover partes
		List<ProcessoParteExpediente> processoParteExpedienteList = new ArrayList<ProcessoParteExpediente>(
				processoExpediente.getProcessoParteExpedienteList());
		for (ProcessoParteExpediente processoParteExpediente : processoParteExpedienteList) {
			ProcessoParte processoParte = getParteFromParteList(processoParteExpediente.getPessoaParte());
			if (processoParte != null) {
				processoParteExpediente.setPrazoLegal(processoParte.getPrazoLegal() != null ? processoParte
						.getPrazoLegal() : 0);
				// Quando o prazo legal  é igual a zero, o comportamento tem de ser igual ao tipo sem prazo
				if (processoParteExpediente.getPrazoLegal() != null && processoParteExpediente.getPrazoLegal().equals(0)) {
					processoParteExpediente.setTipoPrazo(TipoPrazoEnum.S);
				}
			} else {
				getEntityManager().remove(processoParteExpediente);
				processoExpediente.getProcessoParteExpedienteList().remove(processoParteExpediente);
			}
		}

		// Inserir novas partes
		for (ProcessoParte processoParte : partesList) {
			if (!contemPessoaComoParteExpediente(processoExpediente.getProcessoParteExpedienteList(),
					processoParte.getPessoa())) {
				ProcessoParteExpediente ppe = new ProcessoParteExpediente();
				ppe.setPessoaParte(processoParte.getPessoa());
				if (processoParte.getPrazoLegal() == null) {
					ppe.setPrazoLegal(0);
					ppe.setTipoPrazo(TipoPrazoEnum.S);
				} else {
					ppe.setPrazoLegal(processoParte.getPrazoLegal());
					//[PJEII-4049] Quando o prazo legal  é igual a zero, o comportamento tem de ser igual ao tipo sem prazo
					if (ppe.getPrazoLegal() != null && ppe.getPrazoLegal().equals(0)) {
						ppe.setTipoPrazo(TipoPrazoEnum.S);
					}
				}
				ppe.setPrazoProcessual(processoParte.getPrazoProcessual());
				ppe.setProcessoExpediente(ProcessoExpedienteHome.instance().getInstance());
				ppe.setPendenteManifestacao(false);

				if (ProcessoExpedienteHome.instance().getInstance().getMeioExpedicaoExpediente()
						.equals(ExpedicaoExpedienteEnum.E)) {
					ppe.setDtPrazoProcessual(calcularPrazoProcessual());
				}
				getEntityManager().persist(ppe);
			}
		}
		getEntityManager().flush();
	}

	private boolean contemPessoaComoParteExpediente(List<ProcessoParteExpediente> processoParteExpedienteList,
			Pessoa pessoa) {
		for (ProcessoParteExpediente processoParteExpediente : processoParteExpedienteList) {
			if (processoParteExpediente.getPessoaParte().equals(pessoa)) {
				return true;
			}
		}
		return false;
	}

	public void inserir() {
		for (ProcessoParte partesL : partesList) {
			setInstance(new ProcessoParteExpediente());
			getInstance().setPessoaParte(partesL.getPessoa());
			getInstance().setProcessoJudicial(partesL.getProcessoTrf());
			getInstance().setPrazoLegal(partesL.getPrazoLegal());
			getInstance().setPrazoProcessual(partesL.getPrazoProcessual());
			getInstance().setProcessoExpediente(ProcessoExpedienteHome.instance().getInstance());
			getInstance().setProcessoJudicial(ProcessoExpedienteHome.instance().getInstance().getProcessoTrf());
			if (ProcessoExpedienteHome.instance().getInstance().getMeioExpedicaoExpediente() == null) {
				ProcessoExpedienteHome.instance().getInstance().setMeioExpedicaoExpediente(ExpedicaoExpedienteEnum.M);
			}
			if (ProcessoExpedienteHome.instance().getInstance().getMeioExpedicaoExpediente()
					.equals(ExpedicaoExpedienteEnum.E)) {
				getInstance().setDtPrazoProcessual(
						prazosProcessuaisService.obtemDataIntimacaoComunicacaoEletronica(getInstance().getDataDisponibilizacao(),
								prazosProcessuaisService.obtemCalendario(partesL.getProcessoTrf().getOrgaoJulgador()), 
								partesL.getProcessoTrf().getCompetencia().getCategoriaPrazoCiencia(), ContagemPrazoEnum.M));
			}
			if (partesL.getCheckado()) {
				getInstance().setDtCienciaParte(new Date());
				getInstance().setCienciaSistema(Boolean.TRUE);

				if (getInstance().getPrazoLegal() != null && getInstance().getPrazoLegal() > 0
						&& getInstance().getDtPrazoLegal() == null) {
					OrgaoJulgador o = getInstance().getProcessoJudicial().getOrgaoJulgador();
					Calendario calendario = prazosProcessuaisService.obtemCalendario(o);
					Date dataFinal = prazosProcessuaisService.calculaPrazoProcessual(getInstance().getDtCienciaParte(), getInstance()
							.getPrazoLegal(), TipoPrazoEnum.D, calendario, 
							getInstance().getProcessoJudicial().getCompetencia().getCategoriaPrazoProcessual(), ContagemPrazoEnum.C);
					getInstance().setDtPrazoLegal(dataFinal);
				}
			}
			//Quando o prazo legal  é igual a zero, o comportamento tem de ser igual ao tipo sem prazo
			if (getInstance().getPrazoLegal() != null && getInstance().getPrazoLegal().equals(0)) {
				getInstance().setTipoPrazo(TipoPrazoEnum.S);
			}
			persist(getInstance());
			partesL.setCheckado(Boolean.FALSE);
			partesL.setPrazoLegal(null);
			partesL.setPrazoProcessual(null);
		}
		refreshGrid("processoParteExpedienteMenuGrid");
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getProcessoDocumentoAssistenteList() {
		List<ProcessoDocumento> list = ((GridQuery) ComponentUtil
				.getComponent("processoTrfDocumentoPaginatorGrid")).getFullList();
		return paginatorAction().filtrarDocumentosSemPermissaoVisualizacao(list);
	}

	public void cienciaAutomatizada() {
		cienciaAutomatizadaDiarioEletronico();
		
	}
	
	/**
	 * 
	 * Método para atualizar Data de Ciência e Data Prazo Legal para os expedientes enviados para o Diário Eletrônico.
	 * Para isso utiliza uma variável de controle que armazena a ultima data de execução do JOB que obtém as matérias publicadas. 
	 * A partir da última data de execução o JOB obtém, até a data atual, todas as matérias publicadas, atualizando os expedientes relacionados. 
	 * 
	 */
	@Transactional
	public void cienciaAutomatizadaDiarioEletronico() {
		cienciaAutomatizadaDiarioEletronico.cienciaAutomatizadaDiarioEletronico();
	}

	public void cancelaPublicacao() throws PontoExtensaoException, PJeBusinessException, InterruptedException {
		try {
			cancelaPublicacao(this.instance);

			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO,
					"Pedido de cancelamento do expediente enviado ao diário eletrônico com sucesso. "
							+ "Por favor, confira na lista de expedientes dos autos digitais o estado do cancelamento.");
		} catch (PontoExtensaoException e) {
			log.error("Erro ao tentar enviar o pedido de cancelamento do expediente ao diário eletrônico. Excecao: "
					+ e.getLocalizedMessage());

			FacesMessages.instance().add(Severity.INFO,
					"Erro ao tentar enviar o pedido de cancelamento do expediente ao diário eletrônico");
		}
	}

	public void cancelaPublicacao(ProcessoParteExpediente processoParteExpediente)
			throws PontoExtensaoException, PJeBusinessException, InterruptedException {
		if (publicadorDJE != null) {
			processoParteExpediente.setEnviadoCancelamento(true);

			publicadorDJE.cancelarPublicacao(processoParteExpediente.getIdProcessoParteExpediente(),
					Authenticator.getUsuarioLogado().getLogin());

			processoParteExpedienteManager.fecharExpediente(processoParteExpediente);
		}
	}

	public boolean isPodeCancelaPublicacao() throws PontoExtensaoException {
		// Verifica se possui conector para comunicação com Diário Eletrônico
		if (publicadorDJE != null) {
			return isPodeCancelaPublicacao(this.instance);
		}

		return false;
	}

	public boolean isPodeCancelaPublicacao(ProcessoParteExpediente processoParteExpediente)
			throws PontoExtensaoException {
		// Verifica se possui conector para comunicação com Diário Eletrônico
		if (publicadorDJE != null) {
			return publicadorDJE.isPodeCancelarPublicacao(processoParteExpediente.getIdProcessoParteExpediente());
		}

		return false;
	}

	public void consultarMateriasDisponibilizadasNoDia(Date data, boolean atualizarDataExecucaoJob) throws Exception {
		cienciaAutomatizadaDiarioEletronico.consultarMateriasDisponibilizadasNoDia(data, atualizarDataExecucaoJob);
	}

	/*  
 	* Verifica se o papel do usuário é 'Assistente de Procuradoria', 'Gestor Assistente de Procuradoria',  
 	* 'Assistente de Advogado' ou 'Gestor Assistente de Advogado'  
 	*/  
 	public boolean isUsusarioAssistAdvOrAssistProc() {  
 		String identificadorPapelAtual = ComponentUtil.getComponent("identificadorPapelAtual");  
 		return (identificadorPapelAtual.equals("assistAdvogado")   
 				|| identificadorPapelAtual.equals("assistProcuradoria")  
 				|| identificadorPapelAtual.equals("assistGestorAdvogado"));		  
 	}

	public void lancarMovimentosDiario(ProcessoExpediente processoExpediente) {
		cienciaAutomatizadaDiarioEletronico.lancarMovimentosDiario(processoExpediente);
	}

	public void refreshGrids(){
		getEntityManager().clear();
		
		refreshGrid("expedientePendenteGrid");
		refreshGrid("expedienteConfirmadoIntimadoGrid");
		refreshGrid("expedienteConfirmadoSistemaGrid");
		refreshGrid("documentoProcessoGrid");
		refreshGrid("expedienteRespondidoGrid");
	}

	public Date calcularPrazoProcessual() {
		Calendar data = Calendar.getInstance();
		data.add(Calendar.DAY_OF_MONTH, 10);
		getInstance().setDtPrazoProcessual(data.getTime());
		return data.getTime();
	}

	/**
	 * @param processoParteExpediente
	 * @return
	 */
	@SuppressWarnings({ "static-access", "unused" })
	private ProcessoParteExpediente calcularPrazoLegalExpedienteEletronico(
			ProcessoParteExpediente processoParteExpediente) {
		GregorianCalendar dataCiencia = new GregorianCalendar();
		dataCiencia.setTime(processoParteExpediente.getDtCienciaParte());
		dataCiencia.add(dataCiencia.DATE, 1);

		while (dataCiencia.get(Calendar.DAY_OF_WEEK) == 1 || dataCiencia.get(Calendar.DAY_OF_WEEK) == 7) {
			dataCiencia.add(dataCiencia.DATE, 1);
		}
		while (verificarFeriado(dataCiencia, processoParteExpediente)) {
			dataCiencia.add(dataCiencia.DATE, 1);
		}
		dataCiencia.add(dataCiencia.DATE, processoParteExpediente.getPrazoLegal());
		while (dataCiencia.get(Calendar.DAY_OF_WEEK) == 1 || dataCiencia.get(Calendar.DAY_OF_WEEK) == 7) {
			dataCiencia.add(dataCiencia.DATE, 1);
		}
		while (verificarFeriado(dataCiencia, processoParteExpediente)) {
			dataCiencia.add(dataCiencia.DATE, 1);
		}
		processoParteExpediente.setDtPrazoLegal(dataCiencia.getTime());

		return processoParteExpediente;
	}

	public void cienciaIntimacaoIntegracaoCNJ(ProcessoParteExpediente processoParteExpediente) {
		if (processoParteExpediente != null && processoParteExpediente.getDtCienciaParte() == null) {
			processoParteExpediente.setDtCienciaParte(new Date());
			processoParteExpediente.setCienciaSistema(false);
			if (processoParteExpediente.getTipoPrazo() != TipoPrazoEnum.C && processoParteExpediente.getTipoPrazo() != TipoPrazoEnum.S) {
				OrgaoJulgador o = processoParteExpediente.getProcessoJudicial().getOrgaoJulgador();
				Calendario calendario = prazosProcessuaisService.obtemCalendario(o);
				Date dataFinalPrazo = prazosProcessuaisService.calculaPrazoProcessual(processoParteExpediente.getDtCienciaParte(),
						processoParteExpediente.getPrazoLegal(), TipoPrazoEnum.D, calendario, 
						processoParteExpediente.getProcessoJudicial().getCompetencia().getCategoriaPrazoProcessual(), ContagemPrazoEnum.M);
				processoParteExpediente.setDtPrazoLegal(dataFinalPrazo);
			} else {
				processoParteExpediente.setPrazoLegal(null);
				processoParteExpediente.setPrazoProcessual(null);
			}
			setInstance(processoParteExpediente);
			if (getInstance().getProcessoExpediente() != null) {
				EntityManager em = getEntityManager();
				em.merge(processoParteExpediente);
				EntityUtil.flush(em);
				refreshGrid("expedientePendenteGrid");
				refreshGrid("expedienteConfirmadoIntimadoGrid");
				refreshGrid("expedienteRespondidoGrid");
				refreshGrid("documentoProcessoGrid");
			}
		}
	}

	public void cienciaIntimacao(ProcessoParteExpediente processoParteExpediente) {
		if (processoParteExpediente != null && processoParteExpediente.getDtCienciaParte() == null) {
			atoComunicacaoService.registraCienciaPessoal(processoParteExpediente);
			setInstance(processoParteExpediente);
			if (getInstance().getProcessoExpediente() != null) {
				EntityManager em = getEntityManager();
				em.merge(processoParteExpediente);
				EntityUtil.flush(em);
				refreshGrid("expedientePendenteGrid");
				refreshGrid("expedienteConfirmadoIntimadoGrid");
				refreshGrid("expedienteRespondidoGrid");
				refreshGrid("documentoProcessoGrid");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void cienciaIntimacao(ProcessoDocumento pd) {
		String sql = "select o from ProcessoDocumentoExpediente o where o.processoDocumento = :processoDocumento";
		Query query = EntityUtil.createQuery(sql);
		query.setParameter("processoDocumento", pd);
		List<ProcessoDocumentoExpediente> documentosExpediente = query.getResultList();
		Pessoa pessoaLogada = Authenticator.getPessoaLogada();
		
		if (documentosExpediente.size() > 0) {
			for(ProcessoDocumentoExpediente pde : documentosExpediente) {
				String sql2 = "select o from ProcessoParteExpediente o where " +
						"o.processoExpediente = :processoExpediente and o.pessoaParte = :pessoaParte";
				
				Query query2 = EntityUtil.createQuery(sql2);
				query2.setParameter("processoExpediente", pde.getProcessoExpediente());
				query2.setParameter("pessoaParte", pessoaLogada);
				List<ProcessoParteExpediente> processoParteExpedienteList = query2.getResultList();
				
				for (ProcessoParteExpediente listpParte : processoParteExpedienteList) {
					if (listpParte.getDtCienciaParte() == null) {
						cienciaIntimacao(listpParte);
						
						if(semCienciaMap != null) {
							semCienciaMap.clear();
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Boolean podeTomarCiencia(ProcessoDocumento processoDocumento) {
		if (processoDocumento == null || processoDocumento.getTipoProcessoDocumento() == null) {
			return false;
		}
		
		String idTipoProcessoDocumentoIntimacaoPauta = (String) Util.instance().eval("idTipoProcessoDocumentoIntimacaoPauta");
		
		if (processoDocumento.getTipoProcessoDocumento().toString().equals("Expediente")
				|| processoDocumento.getTipoProcessoDocumento().getIdTipoProcessoDocumento() == ParametroUtil
						.instance().getTipoProcessoDocumentoIntimacaoPauta().getIdTipoProcessoDocumento()
				|| processoDocumento.getTipoProcessoDocumento().getIdTipoProcessoDocumento() == 
				(idTipoProcessoDocumentoIntimacaoPauta != null ? Integer.valueOf(idTipoProcessoDocumentoIntimacaoPauta) : 0)) {

			if (processoDocumento.getUsuarioInclusao() == null) {
				return true; // Processo veio do 1º Grau, portanto não tem usuárioInclusao.
			}

			String sql = "select o from ProcessoDocumentoExpediente o "
					+ "where o.processoDocumento = :processoDocumento";
			Query query = EntityUtil.createQuery(sql);
			query.setParameter("processoDocumento", processoDocumento);
			List<ProcessoDocumentoExpediente> processoDocumentoExpedienteList = query
					.getResultList();

			if (processoDocumentoExpedienteList != null
					&& processoDocumentoExpedienteList.size() > 0) {
				ProcessoDocumentoExpediente processoDocumentoExpediente = (ProcessoDocumentoExpediente) 
																		  processoDocumentoExpedienteList.get(0);
				String sql2 = "select o from ProcessoParteExpediente o "
						+ "where o.processoExpediente = :processoExpediente";
				Query query2 = EntityUtil.createQuery(sql2);
				query2.setParameter("processoExpediente", processoDocumentoExpediente.getProcessoExpediente());
				List<ProcessoParteExpediente> processoParteExpedienteList = query2
						.getResultList();
				ProcessoParteExpediente processoParteExpediente = new ProcessoParteExpediente();
				if (processoParteExpedienteList.size() > 1) {
					Usuario usuarioLogado = Authenticator.getUsuarioLogado();
					List<Pessoa> entidadesRepresentadas = null;
					if (Pessoa.instanceOf(usuarioLogado, PessoaProcurador.class)) {
						entidadesRepresentadas = PessoaProcuradorHome.instance().getEntidadesRepresentadas(((PessoaFisica) usuarioLogado).getPessoaProcurador());
					}
					for (ProcessoParteExpediente ppExpedienteList : processoParteExpedienteList) {
						if (entidadesRepresentadas != null) {
							for (Pessoa pessoa : entidadesRepresentadas) {
								if (pessoa.getIdUsuario() == ppExpedienteList.getPessoaParte().getIdUsuario()) {
									processoParteExpediente = ppExpedienteList;
									break;
								}
							}
						} else if (usuarioLogado == ppExpedienteList.getPessoaParte()) {
							processoParteExpediente = ppExpedienteList;
							break;
						}
					}
				}
				if (processoParteExpediente.getIdProcessoParteExpediente() == 0
						&& processoParteExpedienteList.size() > 0) {
					processoParteExpediente = processoParteExpedienteList.get(0);
				} else {
					return false;
				}
				return (podeTomarCiencia(processoParteExpediente));
			}
		}
		return true;
	}

	/**
	 * Teste se um ProcessoParteExpediente pode ser marcado como visto pelo
	 * usuário logado. Pela regra se a intimação é para um usuário ele pode
	 * tomar ciência. Caso o usuário seja um procurador ele pode tomar ciência
	 * se a intimação for para alguma das entidades que ele representa. Caso o
	 * usuário seja advogado ele pode tomar ciência se for o advogado da parte
	 * intimada.
	 * 
	 * @param processoParteExpediente
	 * @return Se tem permissão para dar ciência na intimação
	 */
	public boolean podeTomarCiencia(ProcessoParteExpediente processoParteExpediente) {
		if (processoParteExpediente.getProcessoDocumento() != null
				&& processoParteExpediente.getProcessoDocumento().getUsuarioInclusao() == null) {
			return true; // Processo veio do 1º Grau, portanto não tem
							// usuárioInclusao
		}
		Usuario usuarioLogado = Authenticator.getUsuarioLogado();
		Pessoa intimado = processoParteExpediente.getPessoaParte();
		ProcessoTrf processoTrf = processoParteExpediente.getProcessoExpediente().getProcessoTrf();
		if (Authenticator.isMagistrado() || Authenticator.isDiretorSecretaria()
				|| Authenticator.isDiretorDistribuicao()) {
			return true;
		}
		List<ProcessoParteRepresentante> representantes = new ArrayList<ProcessoParteRepresentante>();
		ProcessoParte pP = new ProcessoParte();

		if (processoParteExpediente.getPessoaParte() != null) {
			StringBuffer sb = new StringBuffer();
			sb.append("select o from ProcessoParte o ");
			sb.append("where o.processoTrf = :processo ");
			sb.append("and o.pessoa.idUsuario = :pessoaParte ");

			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("pessoaParte", processoParteExpediente.getPessoaParte().getIdUsuario());
			q.setParameter("processo", processoTrf);
			q.setMaxResults(1);
			try {
				pP = (ProcessoParte) q.getSingleResult();
				if (pP != null) {
					representantes = pP.getProcessoParteRepresentanteList();
				}
			} catch (NoResultException no) {
			}
		}

		for (int i = 0; i < representantes.size(); i++) {
			if (representantes.get(i).getRepresentante().getIdUsuario().equals(usuarioLogado.getIdUsuario())) {
				return true;
			}
		}
		if (!confirmacaoExpediente(processoParteExpediente).equals("--")) {
			return true;
		}

		ProcessoDocumento processoDocumento = obterProcessoDocumento();
		if (intimado != null
				&& processoDocumento != null
				&& (usuarioLogado.getIdUsuario() == intimado.getIdUsuario() || usuarioLogado.getIdUsuario() == processoDocumento
						.getUsuarioInclusao().getIdUsuario())) {
			return true;
		} else if (Pessoa.instanceOf(usuarioLogado, PessoaProcurador.class)) {
			PessoaProcurador procurador = ((PessoaFisica) usuarioLogado).getPessoaProcurador();
			List<Pessoa> entidadesRepresentadas = PessoaProcuradorHome.instance().getEntidadesRepresentadas(procurador);
			for (Pessoa entidade : entidadesRepresentadas) {
				if (intimado != null && entidade.getIdUsuario() == intimado.getIdUsuario()) {
					return true;
				}
			}
		} else if (pP != null && pP.getInParticipacao() != null) {
			if (Pessoa.instanceOf(usuarioLogado, PessoaAdvogado.class)) {
				String sql = "select count(o) from ProcessoParte o " + "where o.inParticipacao = :part and "
						+ "o.pessoa.idUsuario in (:usuario) and " + "o.processoTrf = :processo";
				Query query = EntityUtil.createQuery(sql);
				query.setParameter("usuario", usuarioLogado.getIdUsuario());
				query.setParameter("processo", processoTrf);
				query.setParameter("part", pP.getInParticipacao());
				try {
					Long retorno = (Long) query.getSingleResult();
					return retorno > 0;
				} catch (NoResultException no) {
					return Boolean.FALSE;
				}
			}
		}
		return false;
	}

	private Date calcularPrazoLegalExpedienteEletronico() {
		Calendar dataCiencia = Calendar.getInstance();

		Calendar proximoDia = dataCiencia;
		proximoDia.add(Calendar.DAY_OF_MONTH, 1);
		if (!isUtilDay(proximoDia)) {
			while (!isUtilDay(proximoDia)) {
				proximoDia.add(Calendar.DAY_OF_MONTH, 1);
			}
			dataCiencia = proximoDia;
		} else {
			if(instance.getDtCienciaParte() != null){
				dataCiencia.setTime(instance.getDtCienciaParte());
			}
		}
		Integer prazoLegal = instance.getPrazoLegal();

		for (int i = 0; i < prazoLegal; i++) {
			dataCiencia.add(Calendar.DAY_OF_MONTH, 1);
			if (verificarSuspensaoDePrazo(dataCiencia)) {
				prazoLegal++;
			}
		}

		while (!isUtilDay(dataCiencia)) {
			dataCiencia.add(dataCiencia.DAY_OF_MONTH, 1);
		}

		return dataCiencia.getTime();
	}

	/**
	 * Metodo que verifica se a data é dia util ou não, caso seja dia util,
	 * verifica se é feriado ou não.
	 */
	private Boolean isUtilDay(Calendar data) {
		if ((data.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
				|| (data.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
			return false;
		} else if (verificarFeriado(data)) {
			return false;
		}
		return true;
	}

	/**
	 * @param data
	 * @return
	 */
	private Boolean verificarFeriado(Calendar data) {
		ProcessoTrf processoTrf = instance.getProcessoExpediente().getProcessoTrf();
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sb = new StringBuilder();
		sb.append("select count(ce) from CalendarioEvento ce ");
		sb.append("where ce.inJudiciario = true and ");
		sb.append("		 ce.inFeriado = true and ");
		sb.append("		 ce.dtDia = :dia and ");
		sb.append("		 ce.dtMes = :mes and ");
		sb.append("		(ce.dtAno = :ano or ce.dtAno is null) and ");
		sb.append("		(ce.inAbrangencia in ('C','N','E','O')) and ");
		sb.append("		(ce.orgaoJulgador = :orgao or ce.orgaoJulgador is null) and ");
		sb.append("		(ce.estado in (select list.municipio.estado from ProcessoTrf p ");
		sb.append("					   inner join p.jurisdicao.municipioList list ");
		sb.append("					   where p.processo.numeroProcesso = :nrProcesso) or ce.estado is null) and ");
		sb.append("		(ce.municipio in (select list.municipio from ProcessoTrf p ");
		sb.append("						  inner join p.jurisdicao.municipioList list ");
		sb.append("						  where p.processo.numeroProcesso = :nrProcesso) or ce.municipio is null) ");
		Query query = em.createQuery(sb.toString());
		query.setParameter("dia", data.get(Calendar.DAY_OF_MONTH));
		query.setParameter("mes", data.get(Calendar.MONTH) + 1);
		query.setParameter("ano", data.get(Calendar.YEAR));
		query.setParameter("orgao", processoTrf.getOrgaoJulgador());
		query.setParameter("nrProcesso", processoTrf.getProcesso().getNumeroProcesso());
		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno > 0 || verificarSuspensaoDePrazo(data);
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	/**
	 * 
	 * @param data
	 * @param processoParteExpediente
	 * @return
	 */
	private Boolean verificarFeriado(GregorianCalendar data, ProcessoParteExpediente processoParteExpediente) {
		EntityManager em = EntityUtil.getEntityManager();

		StringBuilder sb = new StringBuilder();
		sb.append("select count(ce) from CalendarioEvento ce ");
		sb.append("where ce.inJudiciario = true and ");
		sb.append("		ce.inFeriado = true and ");
		sb.append("		ce.dtDia = :dia and ");
		sb.append("		ce.dtMes = :mes and ");
		sb.append("		(ce.dtAno = :ano or ce.dtAno is null) and ");
		sb.append("		(ce.inAbrangencia in ('N','E','M','O') or ce.orgaoJulgador = :orgao))");
		Query query = em.createQuery(sb.toString());
		query.setParameter("dia", data.get(GregorianCalendar.DAY_OF_MONTH));
		query.setParameter("mes", data.get(GregorianCalendar.MONTH) + 1);
		query.setParameter("ano", data.get(GregorianCalendar.YEAR));
		if (processoParteExpediente != null) {
			OrgaoJulgador oj = processoParteExpediente.getProcessoJudicial().getOrgaoJulgador();
			query.setParameter("orgao", oj);
		} else {
			query.setParameter("orgao", null);
		}
		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
		
	}

	/**
	 * @param data
	 * @return
	 */
	private Boolean verificarSuspensaoDePrazo(Calendar data) {
		ProcessoTrf processoTrf = instance.getProcessoExpediente().getProcessoTrf();
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sb = new StringBuilder();
		sb.append("select count(ce) from CalendarioEvento ce ");
		sb.append("where ce.inSuspendePrazo = true and ");
		sb.append("		 ce.dtDia = :dia and ");
		sb.append("		 ce.dtMes = :mes and ");
		sb.append("		(ce.dtAno = :ano or ce.dtAno is null) and ");
		sb.append("		(ce.inAbrangencia in ('N','E','C','O')) and ");
		sb.append("		(ce.orgaoJulgador.idOrgaoJulgador = :orgao or ce.orgaoJulgador is null) and ");
		sb.append("		(ce.estado in (select list.municipio.estado from ProcessoTrf p ");
		sb.append("					   inner join p.jurisdicao.municipioList list ");
		sb.append("					   where p.processo.numeroProcesso = :nrProcesso) or ce.estado is null) and ");
		sb.append("		(ce.municipio in (select list.municipio from ProcessoTrf p ");
		sb.append("						  inner join p.jurisdicao.municipioList list ");
		sb.append("						  where p.processo.numeroProcesso = :nrProcesso) or ce.municipio is null) ");
		Query query = em.createQuery(sb.toString());
		query.setParameter("dia", data.get(Calendar.DAY_OF_MONTH));
		query.setParameter("mes", data.get(Calendar.MONTH) + 1);
		query.setParameter("ano", data.get(Calendar.YEAR));
		query.setParameter("orgao", processoTrf.getOrgaoJulgador().getIdOrgaoJulgador());
		query.setParameter("nrProcesso", processoTrf.getProcesso().getNumeroProcesso());
		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno > 0 || verificarPeriodoSuspensaoDePrazo(data);
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	/**
	 * @param data
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Boolean verificarPeriodoSuspensaoDePrazo(Calendar data) {
		boolean suspendePrazo = false;
		ProcessoTrf processoTrf = instance.getProcessoExpediente().getProcessoTrf();
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sb = new StringBuilder();
		sb.append("select ce from CalendarioEvento ce ");
		sb.append("where ce.inSuspendePrazo = true ");
		sb.append("and  (ce.dtDia is not null and ce.dtMes is not null ) ");
		sb.append("and  (ce.dtDiaFinal is not null and ce.dtMesFinal is not null and ce.dtAnoFinal is not null) ");
		sb.append("and  (ce.inAbrangencia in ('N','E','C','O')) ");
		sb.append("and  (ce.orgaoJulgador.idOrgaoJulgador = :orgao or ce.orgaoJulgador is null) ");
		sb.append("and	(ce.estado in (select list.municipio.estado from ProcessoTrf p ");
		sb.append("					   inner join p.jurisdicao.municipioList list ");
		sb.append("					   where p.processo.numeroProcesso = :nrProcesso) or ce.estado is null) ");
		sb.append("and  (ce.municipio in (select list.municipio from ProcessoTrf p ");
		sb.append("						  inner join p.jurisdicao.municipioList list ");
		sb.append("						  where p.processo.numeroProcesso = :nrProcesso) or ce.municipio is null) ");
		Query query = em.createQuery(sb.toString());
		query.setParameter("orgao", processoTrf.getOrgaoJulgador().getIdOrgaoJulgador());
		query.setParameter("nrProcesso", processoTrf.getProcesso().getNumeroProcesso());

		List<CalendarioEvento> list = query.getResultList();

		Calendar dataInicio = Calendar.getInstance();
		Calendar dataFim = Calendar.getInstance();
		Calendar dataAux = Calendar.getInstance();
		Calendar dataAtual = Calendar.getInstance();

		for (CalendarioEvento calendarioEvento : list) {
			// Limpa as datas para não acumular as datas anteriores
			dataInicio.clear();
			dataFim.clear();
			dataAux.clear();

			// Verifica se o evento repete anualmente
			boolean repeteAnualmente = (calendarioEvento.getDtAno() == null);
			if (repeteAnualmente) {
				calendarioEvento.setDtAno(dataAtual.get(Calendar.YEAR));
				if (calendarioEvento.getDtMes() > calendarioEvento.getDtMesFinal()) {
					dataAtual.add(Calendar.YEAR, 1);
				}
				calendarioEvento.setDtAnoFinal(dataAtual.get(Calendar.YEAR));
			}

			// Seta as datas
			dataInicio.set(calendarioEvento.getDtAno(), calendarioEvento.getDtMes() - 1, calendarioEvento.getDtDia());
			dataFim.set(calendarioEvento.getDtAnoFinal(), calendarioEvento.getDtMesFinal() - 1,
					calendarioEvento.getDtDiaFinal());
			dataAux.set(data.get(Calendar.YEAR), data.get(Calendar.MONTH), data.get(Calendar.DAY_OF_MONTH));

			if (repeteAnualmente) {
				calendarioEvento.setDtAno(null);
				calendarioEvento.setDtAnoFinal(null);
				getEntityManager().merge(calendarioEvento);
				getEntityManager().flush();
			}

			if (DateUtil.isBetweenDates(dataAux.getTime(), dataInicio.getTime(), dataFim.getTime())) {
				suspendePrazo = true;
			}
			dataAtual.clear();
		}

		return suspendePrazo;
	}

	@SuppressWarnings("unchecked")
	public void setarDocumentoPrincipal() {
		String sql = "select o from ProcessoDocumentoExpediente o "
				+ "where o.processoExpediente = :processoExpediente " + "and o.anexo = false";
		Query q = getEntityManager().createQuery(sql);
		q.setParameter("processoExpediente", getInstance().getProcessoExpediente());
		List<ProcessoDocumentoExpediente> pdeList = q.getResultList();
		if (pdeList.size() > 0) {
			ProcessoDocumento processoDocumentoAto = pdeList.get(0).getProcessoDocumentoAto();
			ProcessoDocumentoBin processoDocumentoBin = pdeList.get(0).getProcessoDocumento().getProcessoDocumentoBin();
			if (processoDocumentoAto != null) {
				processoDocumentoBin = processoDocumentoAto.getProcessoDocumentoBin();
			}
			ProcessoDocumentoBinHome.instance().setInstance(processoDocumentoBin);
		}
	}

	public void setarProcessoDocumentoAto() {
		Criteria criteria = HibernateUtil.getSession().createCriteria(ProcessoDocumentoExpediente.class);
		criteria.add(Restrictions.eq("processoExpediente", getInstance().getProcessoExpediente()));
		criteria.add(Restrictions.eq("anexo", false));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		ProcessoDocumentoExpediente classe = (ProcessoDocumentoExpediente)criteria.uniqueResult();
		if (classe != null) {
			ProcessoDocumentoBinHome.instance().setInstance(classe.getProcessoDocumentoAto().getProcessoDocumentoBin());
		}		
	}

	public String confirmacaoExpediente(ProcessoParteExpediente processoParteExpediente) {
		if (processoParteExpediente.getCienciaSistema() != null)
			if (processoParteExpediente.getCienciaSistema()) {
				return "Sistema";
			} else if (processoParteExpediente.getPessoaCiencia() != null) {
				return processoParteExpediente.getPessoaCiencia().getNome();
			} else if (processoParteExpediente.getNomePessoaCiencia() != null) {
				return processoParteExpediente.getNomePessoaCiencia();
			} else {
				return "--";
			}
		else
			return "--";
	}
	
	/**
	 * Retorna o texto final externo do último evento do processo.
	 * 
	 * @param idProcesso
	 *            id do processo
	 * @return texto final externo do último evento do processo, como retornado
	 *         pelo método {@link ProcessoEvento#getTextoFinalExterno()}.
	 */
	@SuppressWarnings("unchecked")
	public String getUltimoEventoTextoFinalExterno(Integer idProcesso) {
		String query = "from ProcessoEvento mp where " + "mp.processo.idProcesso = :idProcesso and "
				+ "mp.visibilidadeExterna = true " + "order by mp.dataAtualizacao desc, mp.idProcessoEvento desc ";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("idProcesso", idProcesso);
		q.setMaxResults(1);

		List<ProcessoEvento> list = q.getResultList();
		if (list.size() > 0) {
			return list.get(0).getTextoFinalExterno();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public String getUltimoEvento(Integer idProcesso) {
		String query = "select pe from ProcessoEvento pe "
				+ "where pe.evento in (select ep from Evento ep " + "					where pe.evento = ep and "
				+ "						 (ep.segredoJustica = false or ep.segredoJustica is null))  "
				+ "and pe.processo.idProcesso = :idProcesso "
				+ "order by pe.dataAtualizacao desc, pe.evento.idEvento desc";
		Query q = getEntityManager().createQuery(query).setMaxResults(1);
		q.setParameter("idProcesso", idProcesso);

		List<ProcessoEvento> list = q.getResultList();
		if (list.size() > 0) {
			return list.get(0).getTextoFinal();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public String getListaPoloAtivo(ProcessoExpediente processoExpediente) {
		if (processoExpediente != null) {
			ProcessoTrf processoTrf = processoExpediente.getProcessoTrf();
			StringBuilder sb = new StringBuilder();
			sb.append("select o from ProcessoParte o ");
			sb.append("where o.processoTrf = :processoTrf and inParticipacao='A'");

			Query query = getEntityManager().createQuery(sb.toString());
			query.setParameter("processoTrf", processoTrf);

			List<ProcessoParte> listSemOrdenacao = query.getResultList();
			List<ProcessoParte> listPP = new ArrayList<ProcessoParte>(0);
			
			for (ProcessoParte processoParte : listSemOrdenacao) {
				if (ParametroUtil.instance().getTipoParteAdvogado() != processoParte.getTipoParte())
					listPP.add(processoParte);
			}

			if (listPP.size() == 1) {
				return listPP.get(0).getPessoa().getNome() + " - " + listPP.get(0).getPessoa().getDocumentoCpfCnpj();
			} else {
				if (listPP.size() > 1) {
					return listPP.get(0).getPessoa().getNome() + " e outros - "
							+ listPP.get(0).getPessoa().getDocumentoCpfCnpj();
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public String getListaPoloPassivo(ProcessoExpediente processoExpediente) {
		if (processoExpediente != null) {
			ProcessoTrf processoTrf = processoExpediente.getProcessoTrf();
			StringBuilder sb = new StringBuilder();
			sb.append("select o from ProcessoParte o ");
			sb.append("where o.processoTrf = :processoTrf and inParticipacao='P'");

			Query query = getEntityManager().createQuery(sb.toString());
			query.setParameter("processoTrf", processoTrf);

			List<ProcessoParte> listSemOrdenacao = query.getResultList();
			List<ProcessoParte> listPP = new ArrayList<ProcessoParte>(0);
			
			for (ProcessoParte processoParte : listSemOrdenacao) {
				if (ParametroUtil.instance().getTipoParteAdvogado() != processoParte.getTipoParte())
					listPP.add(processoParte);
			}
			
			String retorno = null;  
			if (listPP.size() == 1) {
				retorno = listPP.get(0).getPessoa().getDocumentoCpfCnpj() != null ?  listPP.get(0).getPessoa().getNome() + " - " + listPP.get(0).getPessoa().getDocumentoCpfCnpj() :  
					  	 listPP.get(0).getPessoa().getNome();  
				return retorno;
			} else {
				if (listPP.size() > 1) {
					
					retorno = listPP.get(0).getPessoa().getDocumentoCpfCnpj() != null ?  
						  	 listPP.get(0).getPessoa().getNome() + " e outros - " + listPP.get(0).getPessoa().getDocumentoCpfCnpj() :  
						  		listPP.get(0).getPessoa().getNome() + " e outros "; 
					
					return retorno;
				}
			}
		}
		return null;
	}

	public void setPartesListTodos(List<ProcessoParte> partesListTodos) {
		this.partesListTodos = partesListTodos;
	}

	public List<ProcessoParte> getPartesListTodos() {
		if (partesListTodos == null || partesListTodos.isEmpty()) {
			Object idProcesso = ProcessoHome.instance().getId();
			if (idProcesso != null) {
				ProcessoTrf processoTrf = getEntityManager().find(ProcessoTrf.class, idProcesso);
				partesListTodos = processoTrf.getProcessoParteList();
			} 
		}
		ProcessoExpediente pe = ProcessoExpedienteHome.instance().getInstance();
		if (pe != null && pe.getIdProcessoExpediente() != 0) {
			addPessoaExpediente(pe);
		}
		return partesListTodos;
	}

	public String getProcessoParteExpedienteList(ProcessoExpediente pe) {
		List<Object> list = new ArrayList<Object>(pe.getProcessoParteExpedienteList());
		return StringUtil.concatList(list, ", ");
	}	
	
	public List<ProcessoParte> getPartesList() {
		return partesList;
	}

	public void setPartesList(List<ProcessoParte> partesList) {
		this.partesList = partesList;
	}

	public void setDataAutuacao(Date dataAutuacao) {
		this.dataAutuacao = dataAutuacao;
	}

	public Date getDataAutuacao() {
		return dataAutuacao;
	}

	public void setDataDistribuicao(Date dataDistribuicao) {
		this.dataDistribuicao = dataDistribuicao;
	}

	public Date getDataDistribuicao() {
		return dataDistribuicao;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}	
	
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@SuppressWarnings("unchecked")
	public ProcessoDocumento obterProcessoDocumento() {
		if (getInstance().getProcessoExpediente() != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o from ProcessoDocumentoExpediente o ");
			sb.append("where o.processoExpediente.idProcessoExpediente = :idPE");
			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("idPE", getInstance().getProcessoExpediente().getIdProcessoExpediente());
			List<ProcessoDocumentoExpediente> documentoExpedienteList = q.getResultList();
			if (documentoExpedienteList != null && !documentoExpedienteList.isEmpty()) {
				for (ProcessoDocumentoExpediente processoDocumentoExpediente : documentoExpedienteList) {
					if (processoDocumentoExpediente.getAnexo() != null && !processoDocumentoExpediente.getAnexo()) {
						return processoDocumentoExpediente.getProcessoDocumento();
					}
				}
			}
		}
		return null;
	}

	public boolean semCiencia(ProcessoDocumento processoDocumento) {
		if(semCienciaMap == null) {
			semCienciaMap = new HashMap<Integer, Boolean>();
		}
		int idProcessoExpediente = processoDocumento.getIdProcessoDocumento();
		Boolean value = semCienciaMap.get(idProcessoExpediente);
		if(value == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("select count(o) from ProcessoDocumentoExpediente o ");
			sb.append("where o.processoDocumento.idProcessoDocumento = :idDoc ");
			sb.append("and exists (select ppe from ProcessoParteExpediente ppe ");
			sb.append("			   where ppe.dtCienciaParte is null ");
			sb.append("              and ppe.processoExpediente.meioExpedicaoExpediente = 'E' ");
			sb.append("				 and ppe.prazoLegal >= 0 ");
			sb.append("				 and o.processoExpediente = ppe.processoExpediente ");
			sb.append("				 and ppe.pessoaParte in (:list))");
			
			Query q = getEntityManager().createQuery(sb.toString())
						.setParameter("idDoc", processoDocumento.getIdProcessoDocumento())
						.setParameter("list", PessoaAdvogadoHome.instance().getPessoaAdvogadoProcurador());
			
			Long count = 0L;
			try {
				count = (Long) q.getSingleResult();
			} catch (Exception e) {
			}

			value = count.compareTo(0L) > 0;
			semCienciaMap.put(processoDocumento.getIdProcessoDocumento(), value);
		}
		return value;
	}

	/**
	 * Método que atualiza os prazos dos expedientes quando é cadastrado um novo
	 * evento no calendário.
	 * 
	 * @param ce
	 */
	@SuppressWarnings("unchecked")
	public void atualizarExpedientes(CalendarioEvento ce) {
		StringBuilder sb = new StringBuilder();
		sb.append("Select o from ProcessoParteExpediente o ");
		sb.append("where  ((o.dtPrazoLegal  >= :dataInicial  ");
		if (ce.getDataEventoFim() != null) {
			sb.append("and o.dtPrazoLegal  <= :dataFinal  ");
			sb.append(") or (o.dtCienciaParte >= :dataInicial and o.dtCienciaParte <= :dataFinal  ");
			sb.append(") or (o.dtCienciaParte  < :dataInicial and o.dtPrazoLegal > :dataFinal ");
		}
		sb.append(") and o.processoExpediente.meioExpedicaoExpediente = :meioExpedicao ");
		sb.append("and o.prazoLegal > 0) ");
		sb.append("and o.dtCienciaParte <> null ");
		Query q = getEntityManager().createQuery(sb.toString());
		Calendar dataInicial = Calendar.getInstance();
		Calendar dataFinal = Calendar.getInstance();
		
		Calendar agora = Calendar.getInstance();
		int anoAtual = agora.get(Calendar.YEAR);
		boolean haRepeticaoDoEvento = (ce.getDtAno() == null);

		if(haRepeticaoDoEvento) {
			dataInicial.set(Calendar.YEAR, anoAtual);
		}
		
		dataInicial.setTime(ce.getDataEvento());
		q.setParameter("meioExpedicao", ExpedicaoExpedienteEnum.E);
		q.setParameter("dataInicial", dataInicial.getTime());
		if (ce.getDataEventoFim() != null) {
			dataFinal.setTime(ce.getDataEventoFim());
			if(haRepeticaoDoEvento) {
				dataFinal.set(Calendar.YEAR, anoAtual);
				if(dataFinal.compareTo(dataInicial) < 0 ) {
					dataFinal.set(Calendar.YEAR, anoAtual + 1);
				}
			}
			q.setParameter("dataFinal", dataFinal.getTime());
		}
		List<ProcessoParteExpediente> ppe = q.getResultList();
		for (ProcessoParteExpediente processoParteExpediente : ppe) {
			setId(processoParteExpediente.getIdProcessoParteExpediente());
			processoParteExpediente.setDtPrazoLegal(calcularPrazoLegalExpedienteEletronico());
			getEntityManager().merge(processoParteExpediente);
			getEntityManager().flush();

		}
		dataInicial.clear();
		dataFinal.clear();
	}

	/**
	 * Método que verifica se o md5 do documento do ato do magistrado é igual ao
	 * md5 do documento do expediente
	 * 
	 * @param processoExpediente
	 * @return true se o MD5 dos dois documentos forem iguais
	 */
	public boolean regraUm(ProcessoExpediente processoExpediente) {
		StringBuilder sql = new StringBuilder("");
		sql.append("select count(o) from ProcessoDocumentoExpediente o ");
		sql.append(" where o.anexo = false and o.processoExpediente = :processoExpediente ");
		sql.append(" and o.processoDocumento.processoDocumentoBin.md5Documento = o.processoDocumentoAto.processoDocumentoBin.md5Documento ");
		Query query = EntityUtil.createQuery(sql.toString());
		query.setParameter("processoExpediente", processoExpediente);
		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	/**
	 * Método que verifica se o md5 do documento do ato do magistrado é
	 * diferente ao md5 do documento do expediente
	 * 
	 * @param processoExpediente
	 * @return true se o MD5 dos dois documentos forem diferentes
	 */
	public boolean regraDois(ProcessoExpediente processoExpediente) {
		String sql = "select count(o) from ProcessoDocumentoExpediente o "
				+ "where o.anexo = false and o.processoExpediente = :processoExpediente and o.processoDocumento.processoDocumentoBin.md5Documento != o.processoDocumentoAto.processoDocumentoBin.md5Documento ";
		Query query = EntityUtil.createQuery(sql);
		query.setParameter("processoExpediente", processoExpediente);
		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	/**
	 * Método que verifica se o documento do ato do magistrado existe
	 * 
	 * @param processoExpediente
	 * @return true se o documento do ato do magistrado existe
	 */
	public boolean regraTres(ProcessoExpediente processoExpediente) {
		String sql = "select count(o) from ProcessoDocumentoExpediente o "
				+ "where o.processoDocumentoAto = null and o.anexo = false and o.processoExpediente = :processoExpediente ";
		Query query = EntityUtil.createQuery(sql);
		query.setParameter("processoExpediente", processoExpediente);
		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	@SuppressWarnings("unused")
	private boolean verificarRegra(ProcessoExpediente processoExpediente, String sql, Map<Integer, Boolean> regraMap) {
		int idProcessoExpediente = processoExpediente.getIdProcessoExpediente();
		Query query = EntityUtil.createQuery(sql);
		query.setParameter("processoExpediente", processoExpediente);
		Long result = EntityUtil.getSingleResult(query);
		boolean ret = result > 0 ? true : false;
		regraMap.put(idProcessoExpediente, ret);
		return ret;
	}

	public void removerPessoaExpedienteNaoVinculada(ProcessoExpediente processoExpediente) {
		return;
	}

	public List<ProcessoParteExpediente> processoParteExpedienteComDocumentoList() {
		ProcessoTrf instance = ProcessoTrfHome.instance().getInstance();
		if (instance.getIdProcessoTrf() == 0) {
			instance = ProcessoExpedienteHome.instance().getInstance().getProcessoTrf();
		}		
		processoParteExpedienteList = processoParteExpedienteManager
				.processoParteExpedienteComDocumentoList(ProcessoTrfHome.instance().getInstance());
		processoParteExpedienteList = processoParteExpedienteManager.processoParteExpedienteComDocumentoList(instance); 
		return processoParteExpedienteList;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoParteExpediente> processoParteExpedienteList() {
		EntityList el = ComponentUtil.getComponent("detalheProcessoParteExpedienteList");
		processoParteExpedienteList = el.list();
		return processoParteExpedienteList;
	}

	public Boolean getAllCheckbox() {
		return allCheckbox;
	}

	public void setAllCheckbox(Boolean allCheckbox) {
		this.allCheckbox = allCheckbox;
	}

	public void setCheckboxOnList(int key, Boolean checkbox) {
		processoParteExpedienteList.get(key).setFechado(checkbox);
		if (!checkbox) {
			setAllCheckbox(false);
		}
	}

	public void setAllCheckBoxOnList() {
		for (ProcessoParteExpediente ppe : processoParteExpedienteList) {
			ppe.setFechado(getAllCheckbox());
		}
	}
	
	/**
	 * Método responsável por fechar o expediente apontado pela instância da classe.  
	 * 
	 */
	public void fecharProcessoParteExpediente() {
		try {
			processoParteExpedienteManager.fecharExpediente(this.instance);
			FacesMessages.instance().add(Severity.INFO,"Expediente fechado com sucesso");
		} catch( PJeBusinessException excecao ) {
			FacesMessages.instance().add(Severity.INFO,"Erro ao tentar fechar o expediente");
		}
	}

	public void persistAllCheckbox() {
		for (ProcessoParteExpediente ppe : processoParteExpedienteList) {
			try {
				processoParteExpedienteManager.persist(ppe);
			} catch (PJeBusinessException e) {				
				e.printStackTrace();
				FacesMessages.instance().add(Severity.ERROR,e.getMessage(),e.getParams());
				return;
			}
		}		
		
		FacesMessages.instance().add(Severity.INFO,"Operação concluída com sucesso");
	}

	@SuppressWarnings({ "unused", "unchecked" })
	private List<PessoaExpediente> getPessoaExpedienteList() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaExpediente o ");
		sb.append("where o.processoExpediente = :pe");

		Query q = EntityUtil.createQuery(sb.toString());
		q.setParameter("pe", ProcessoExpedienteHome.instance().getInstance());
		return q.getResultList();
	}
	
	private void addPessoaExpediente(ProcessoExpediente pe) {
		return;
	}	
	
	public boolean parteListTodosContemPessoa(Pessoa pessoa) {
		for (ProcessoParte pp: partesListTodos) {
			if (pp.getPessoa().equals(pessoa)) {
				return true;
			}
		}
		return false;
	}	
	
	public boolean parteListContemPessoa(Pessoa pessoa) {
		return getParteFromParteList(pessoa) != null;
	}

	public ProcessoParte getParteFromParteList(Pessoa pessoa) {
		for (ProcessoParte pp : partesList) {
			if (pp.getPessoa().equals(pessoa)) {
				return pp;
			}
		}
		return null;
	}

	public ProcessoDocumento getDocumentoExpediente() {
		return documentoExpediente;
	}

	public void setDocumentoExpediente(ProcessoDocumento documentoExpediente) {
		this.documentoExpediente = documentoExpediente;
	}

	public String getLabelOrgalJulgador(){
		return ParametroUtil.instance().isPrimeiroGrau() ? "Órgão Julgador" : "Relator";
	}
	
	/**
	 * Verifica se o usuario logado eh um advogado do processo com o expediente passado como
	 * parametro.
	 * 
	 * @param processoParteExpediente
	 * @return true caso usuario logado seja advogado do processo contendo o expediente
	 */
	public boolean ehProcessoParteAdvogadoProcesso(ProcessoParteExpediente processoParteExpediente) {
		List<String> listaPartes = PessoaAdvogadoHome.instance().getParticipacoesProcessoString();
		if(listaPartes.contains(processoParteExpediente.getPessoaParte().getIdUsuario().toString() + "_"+processoParteExpediente.getProcessoJudicial().getIdProcessoTrf())){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Verifica se o usuario logado eh um advogado do processo com o expediente.
	 * 
	 * @param processoParteExpediente
	 * @return true caso usuario logado seja advogado do processo contendo o expediente
	 */
	public boolean ehProcessoParteAdvogadoProcesso() {
		return ehProcessoParteAdvogadoProcesso(getInstance());
	}
	
	public ProcessoParteExpediente getExpedienteSelecionado(){
		return expedienteSelecionado;
	}

	public void setExpedienteSelecionado(ProcessoParteExpediente expedienteSelecionado){
		this.expedienteSelecionado = expedienteSelecionado;
	}
	
	public void registraCienciaPessoal() {
		atoComunicacaoService.registraCienciaPessoal(expedienteSelecionado);
	}

	public Long getIdProcessoParteExpedienteCiencia() {
		return idProcessoParteExpedienteCiencia;
	}

	public void setIdProcessoParteExpedienteCiencia(
			Long idProcessoParteExpedienteCiencia) {
		this.idProcessoParteExpedienteCiencia = idProcessoParteExpedienteCiencia;
	}
	
	public AvisoRecebimentoECT getAvisoRecebimentoECT() {
		return avisoRecebimentoECT;
	}
	
	public void setAvisoRecebimentoECT(AvisoRecebimentoECT avisoRecebimentoECT) {
		this.avisoRecebimentoECT = avisoRecebimentoECT;
	}
	
	public String getHtmlRastreioCorrespondencia() {
		return htmlRastreioCorrespondencia;
	}
	
	public void setHtmlRastreioCorrespondencia(
			String htmlRastreioCorrespondencia) {
		this.htmlRastreioCorrespondencia = htmlRastreioCorrespondencia;
	}

	public String recuperaHTMLRastreioCorrespondencia(ProcessoParteExpedienteEndereco ppee){
			String urlCorreios = ParametroUtil.getParametro("urlCorreios");
			
			StringBuilder sb = new StringBuilder();
			sb.append("<table style=\"border:1px solid black;border-collapse:collapse; cellpadding:5; width:100%; text-align:center; \">");
			sb.append("<thead>");
			sb.append("    <tr>");
			sb.append("        <th colspan=\"5\" style=\"border:1px solid black; text-align:left; font-weight:bold; background-color:blue; color:white; \">Codigo de rastreio: "
					+ ppee.getNumeroAr() + "</th>");
			sb.append("    </tr>");
			sb.append("</thead>");
			sb.append("    <tr>");
			String montarLink = "<td>\n"
					+ "  <a href=\"" + urlCorreios + "\"" + "target=\"" + "_blank" + "\">"
					+       urlCorreios
					+ "  </a>\n"
					+ "</td>";
			sb.append("        <td>Acompanhe o andamento da entrega do seu pedido.</td>");
			sb.append("    </tr>");
			sb.append("    <tr>");
			sb.append(montarLink);
			sb.append("    </tr>");
			sb.append("</tbody>");
			sb.append("</table>");
			
			 return sb.toString();
	}
	
	public void recuperaHTMLRastreioCorrespondencia(
			ProcessoParteExpediente processoParteExpediente){
		
		setInstance(processoParteExpediente);
		htmlRastreioCorrespondencia = "";
		for(ProcessoParteExpedienteEndereco ppee : getInstance().getProcessoParteExpedienteEnderecoList()){
			if(ppee.getNumeroAr() != null){
				htmlRastreioCorrespondencia += recuperaHTMLRastreioCorrespondencia(ppee)+"<br/>";
			}
		}
		
	}
	
	/**
	 * Método responsável por verificar se existe algum expediente pendente com
	 * prazo em aberto para o processo.
	 * 
	 * Adiciona também um alerta ao processo caso seja requisitado pelo usuário.
	 * 
	 * @see ProcessoParteExpedienteManager#getAtosComunicacaoPendentes(ProcessoTrf...)
	 * 
	 * @param processoTrf
	 *            {@link ProcessoTrf} a ser pesquisado
	 * 
	 * @param adicionarAlerta
	 *            Indicador para adicionar mensagem de alerta ao processo caso
	 *            exista algum expediente pendente com prazo aberto.
	 * @see {@link AlertaHome#inserirAlerta(ProcessoTrf, String, CriticidadeAlertaEnum)}
	 * 
	 * @return <code>true</code> caso exista expediente pendente com prazo em
	 *         aberto
	 * 
	 *         <code>false</code> caso não exista expediente pendente com prazo
	 *         em aberto
	 */
	public boolean existeExpedientePendenteComPrazoAberto(ProcessoTrf processoTrf, boolean adicionarAlerta) {
				
		List<ProcessoParteExpediente> expedientesPendentes = processoParteExpedienteManager
				.getAtosComunicacaoPendentes(processoTrf);
		
		if (expedientesPendentes != null && !expedientesPendentes.isEmpty()) {
			
			// possui expendiente pendente com prazo aberto			
			if (adicionarAlerta) {
				StringBuilder msg = new StringBuilder("Existe(m) expediente(s) pendente(s) com prazo em curso para a(s) parte(s): ");
				List<String> nomePartes = new ArrayList<String>();
				
				for (ProcessoParteExpediente processoParteExpediente : expedientesPendentes) {				
					nomePartes.add(processoParteExpediente.getNomePessoaParte());
				}
				
				msg.append(StringUtil.concatList(nomePartes, ", ", " e "));
				
				AlertaHome.instance().inserirAlerta(processoTrf, msg.toString(), CriticidadeAlertaEnum.A);
				EntityUtil.getEntityManager().flush();				
			}
			
			return true;
		} 
		
		// não possui expendiente aberto com prazo em aberto
		return false;
		
	}
	
	/**
	 * Método responsável por retornar as informações de todos os expedientes de um processo.
	 * Estas informações são as mesmas apresentadas na funcionalidade "Expedientes" dos Autos digitais.
	 * 
	 * @param processoTrf {@link ProcessoTrf}
	 * @return As informações de todos os expedientes de um processo.
	 */
	public String recuperarDescricaoTodosExpedientes(ProcessoTrf processoTrf) {
		StringBuilder result = new StringBuilder();
		
		if (processoTrf != null) {
			ProcessoExpediente pe;
			List<ProcessoParteExpediente> processoParteExpedientes = processoParteExpedienteManager.getParteExpedienteFromProcesso(processoTrf);
			for (ProcessoParteExpediente ppe : processoParteExpedientes) {
				pe = ppe.getProcessoExpediente();
				
				result.append(String.format("<b>Identificador do expediente:</b> %d <br>", ppe.getIdProcessoParteExpediente()))
					.append(String.format("<b>Tipo de documento utilizado:</b> %s <br>", pe.getTipoProcessoDocumento()))
					.append(String.format("<b>Destinatário:</b> %s <br>", ppe.getNomePessoaParte()));
				
				if (!ppe.getIntimacaoPessoal() && ppe.getProcuradoria() != null) {
					result.append(String.format("<b>Representante:</b> %s <br>", ppe.getProcuradoria()));
				}
				
				result.append(String.format("%s (%s) <br>", ExpedicaoExpedienteEnum.E.equals(pe.getMeioExpedicaoExpediente()) ? 
					"Expedição eletrônica" : pe.getMeioExpedicaoExpediente().getLabel(), DateUtil.dateToString(pe.getDtCriacao(), "dd/MM/yyyy HH:mm:ss")));
				
				if (ppe.getDtCienciaParte() != null) {
					result.append(String.format("%s registrou ciência em %s <br>", ppe.getPessoaCiencia() == null ? 
						"O sistema" : (ppe.getPessoaCiencia().equals(Authenticator.getPessoaLogada()) ? "Você" : ppe.getPessoaCiencia()), ppe.getDtCienciaParte()));
				}
				
				if (ppe.getDtCienciaParte() == null && ppe.getRegistroIntimacaoList() != null && ppe.getRegistroIntimacaoList().size() > 0 
						&& TipoResultadoAvisoRecebimentoEnum.R.equals(ppe.getRegistroIntimacaoList().get(0).getResultado())) {
					
					result.append(String.format("Comunicação frustrada constatada em %s - %s <br>", 
						DateUtil.dateToString(ppe.getRegistroIntimacaoList().get(0).getData(), "dd/MM/yyyy HH:mm:ss"), 
						ppe.getRegistroIntimacaoList().get(0).getResultado().getLabel()));
				}
				
				if (ExpedicaoExpedienteEnum.M.equals(pe.getMeioExpedicaoExpediente()) && ppe.getDtCienciaParte() == null && ppe.getFechado()) {
					result.append("Comunicação frustrada <br>");
				}
				
				result.append(String.format("<b>Prazo:</b> %s %s <br>", ppe.getPrazoLegal() == null ? StringUtils.EMPTY : ppe.getPrazoLegal(), 
					TipoPrazoEnum.D.equals(ppe.getTipoPrazo()) ? "dias" : ppe.getTipoPrazo().getLabel()));
				
				if (ppe.getDtPrazoLegal() != null) {
					result.append(String.format("<b>Data limite prevista %s:</b> %s <br>", ppe.getDtCienciaParte() == null 
							&& ExpedicaoExpedienteEnum.E.equals(pe.getMeioExpedicaoExpediente()) ? "para ciência expressa" : "para manifestação",
						DateUtil.dateToString(ppe.getDtPrazoLegal(), "dd/MM/yyyy HH:mm:ss")));
				}
				
				result.append(String.format("<b>Expediente %s</b> <br> <hr>", ppe.getFechado() ? "fechado" : "aberto"));
			}
		}
		return result.toString();
	}
	
	/**
	 * Dado um processoParteExpediente, retorna a informação de qual a partipação da pessoa intimada no processo
	 * 
	 * @param idProcessoParteExpediente
	 * @return
	 */
	public ProcessoParteParticipacaoEnum obterParticipacaoParteExpediente(Integer idProcessoParteExpediente) {
		ProcessoParteParticipacaoEnum resultado = null;
		
		if (idProcessoParteExpediente != null) {
			try {
				ProcessoParteExpediente parteExpediente = processoParteExpedienteManager.findById(idProcessoParteExpediente);
				if (parteExpediente != null) {
					resultado = ComponentUtil.getComponent(ProcessoParteManager.class).identificaParticipacaoPessoa(parteExpediente.getProcessoJudicial(),parteExpediente.getPessoaParte(), false);
				}
			} catch (PJeBusinessException e) {
				// Nothing to do
			}
		}
		
		return resultado;
	}

}