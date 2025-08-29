package br.com.infox.cliente.home;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.BooleanUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.infox.cliente.component.ValidacaoAssinaturaProcessoDocumento;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.certificado.util.VerificaCertificadoPessoa;
import br.com.infox.ibpm.component.tree.AutomaticEventsTreeHandler;
import br.com.infox.ibpm.component.tree.EventsEditorTreeHandler;
import br.com.infox.ibpm.component.tree.EventsHomologarMovimentosTreeHandler;
import br.com.infox.ibpm.component.tree.EventsTreeHandler;
import br.com.infox.ibpm.home.AbstractProcessoDocumentoBinHome;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.home.api.IProcessoDocumentoBinHome;
import br.com.itx.component.FileHome;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaOficialJustica;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoVisibilidadeSegredo;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.filters.ConsultaProcessoTrfFilter;

@Install(precedence = Install.APPLICATION)
@Name("processoDocumentoBinHome")
//@BypassInterceptors
public class ProcessoDocumentoBinHome extends AbstractProcessoDocumentoBinHome<ProcessoDocumentoBin> implements
		IProcessoDocumentoBinHome {

	private static final LogProvider log = Logging.getLogProvider(ProcessoDocumentoBinHome.class);
	private static final long serialVersionUID = 1L;

	private String signature;
	private String certChain;
	private Boolean isAssinarDocumento = Boolean.FALSE;
	private Boolean mostrarMinuta = Boolean.FALSE;
	private ProcessoDocumento processoDocumento;
	private Boolean peticaoAvulsaAnexada = Boolean.FALSE;
	private boolean assinado = false;
	private boolean exibeModalRemetido = false;
	private Long taskInstanceId;

	@In
	private DocumentoJudicialService documentoJudicialService;
		
	/**
	 * PJEII-758:
	 * Atributo para verificar autorização por meio de flag, buscando eliminar 
	 * sucessivas chamadas de rotinas. 
	 * @author Athos Reiser
	 * @since 1.4.1
	 * @see #loadInstance()
	 * @see #getInstance() 
	 */
	private Boolean verificaAutorizacao = null;

	public void setSignature(String signature) {
		getInstance().setSignature(signature);
		this.signature = signature;
	}

	public String setDownloadInstance(Integer id) {
		if(id == 0){
			Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		    Object param = params.get("idBin");
		    try {
		    	if (param != null) {
		    		id = new Integer(param.toString());
		    	}
		    } catch (NumberFormatException ex) {
		    	log.error(ex);
		    }
		}
		setProcessoDocumentoBinIdProcessoDocumentoBin(id);
		return super.setDownloadInstance();
	}
	
	public String getSignature() {
		return signature;
	}

	public void setCertChain(String certChain) {
		getInstance().setCertChain(certChain);
		this.certChain = certChain;
	}

	public String getCertChain() {
		return certChain;
	}

	public Boolean getIsAssinarDocumento() {
		return isAssinarDocumento;
	}

	public void setIsAssinarDocumento(Boolean isAssinarDocumento) {
		this.isAssinarDocumento = isAssinarDocumento;
	}

	public static ProcessoDocumentoBinHome instance() {
		return ComponentUtil.getComponent("processoDocumentoBinHome");
	}

	@Override
	public String update() {
		ProcessoDocumentoManager processoDocumentoManager = (ProcessoDocumentoManager) Component.getInstance(ProcessoDocumentoManager.class);
		String ret = super.update();
		isAssinarDocumento();
		return ret;
	}

	@Override
	public String persist() {
		String ret = super.persist();
		isAssinarDocumento();
		return ret;
	}

	private void isAssinarDocumento() {
		if (isModelo() && isAssinarDocumento) {
			assinarDocumento();
		}
	}

	public boolean assinarDocumento() {
		/*
		 * PJE-JT: Athos Reiser : PJEII-965 - 2012-04-24 Alteracoes feitas
		 * pela JT. Verifica se existe valor na instance, caso contrario deve-se buscar no componente 
		 * do processoHome. Estava lancando NullPointerException no momento da assinatura do documento.
		 */
		if (this.isManaged()) {
			return assinarDocumento(getInstance());
		} 
		
		return assinarDocumento(ProcessoHome.instance().getProcessoDocumentoBin());	
		/*
		 * PJE-JT: Fim
		 */
	}

	@Observer(ProcessoHome.EVENT_ATUALIZAR_PROCESSO_DOCUMENTO_FLUXO)
	protected boolean assinarDocumento(ProcessoDocumentoBin procDocBin) {
		try {
			
			if( (procDocBin == null || procDocBin.getIdProcessoDocumentoBin() == 0) 
					&& ProcessoHome.instance() != null 
					&& ProcessoHome.instance().getIdProcessoDocumento() != 0){
				ProcessoDocumento processoDocumento = EntityUtil.find(ProcessoDocumento.class,ProcessoHome.instance().getIdProcessoDocumento());
				ProcessoDocumentoHome.instance().setInstance(processoDocumento);
				procDocBin = processoDocumento.getProcessoDocumentoBin();
			}
			
			
			if (certChain != null && signature != null) {
				procDocBin.setCertChain(certChain);
				procDocBin.setSignature(signature);
			}
			VerificaCertificadoPessoa.verificaCertificadoPessoaLogada(getInstance().getCertChain());
		} catch (Exception e) {
			Util.setMessage(Severity.ERROR, "Erro ao assinar o documento: " + e.getMessage());
			return false;
		}
		ProcessoDocumentoBinPessoaAssinatura assinatura = new ProcessoDocumentoBinPessoaAssinatura();
		assinatura.setProcessoDocumentoBin(procDocBin);
		assinatura.setPessoa(Authenticator.getPessoaLogada());
		assinatura.setAssinatura(procDocBin.getSignature());
		assinatura.setCertChain(procDocBin.getCertChain());
		getEntityManager().persist(assinatura);
		setValido();
		getEntityManager().flush();
		if (procDocBin.getDataAssinatura() == null) {
			procDocBin.setDataAssinatura(new Date());
		}

		if (ProcessoDocumentoHome.instance().getInstance() != null) {
			ProcessoHome.instance().setIdProcessoDocumento(
					ProcessoDocumentoHome.instance().getInstance().getIdProcessoDocumento());
		} else {
			ProcessoHome.instance().setIdProcessoDocumento(0);
		}
		
		ProcessInstance pi = org.jboss.seam.bpm.ProcessInstance.instance();
		
		
		if(pi != null){
			EventsTreeHandler.instance().registraEventos();
			EventsEditorTreeHandler.instance().registraEventos();
			EventsHomologarMovimentosTreeHandler.instance().registraEventos();
		}
		else{
			EventsTreeHandler.instance().registraEventosSemFluxo(ProcessoHome.instance().getInstance());
			EventsEditorTreeHandler.instance().registraEventosSemFluxo(ProcessoHome.instance().getInstance());
		}
		refreshGrid("assinaturasGrid");
		getEntityManager().merge(procDocBin);
		getEntityManager().flush();

		// define o papel do documento como o do responssavel pela assinatura
		for (ProcessoDocumento pd : procDocBin.getProcessoDocumentoList()) {
			pd.setPapel(Authenticator.getPapelAtual());
			getEntityManager().merge(pd);
			getEntityManager().flush();
		}

		if (!procDocBin.getValido()) {
			//procDocBin.setValido(ProcessoDocumentoBinHome.instance().estaValidado(procDocBin));
//			procDocBin.setValido(ProcessoDocumentoHome.instance().estaValido());

			// [PJEII-3785] Valida o documento em função da nova assinatura
			procDocBin.setValido(ProcessoDocumentoHome.instance().estaValido(procDocBin, assinatura));
			
			// se agora é válido, vamos gravá-lo na base de dados
			if (procDocBin.getValido()) {
				EntityUtil.getEntityManager().merge(procDocBin);
				EntityUtil.getEntityManager().flush();
			}
		}
		
		FacesMessages.instance().clear();
		Util.setMessage(Severity.INFO, "Documento assinado com sucesso.");
		return true;
	}
	
	public boolean isDocumentoAssinado() {
		return isDocumentoAssinado(getInstance());
	}

	protected boolean isDocumentoAssinado(ProcessoDocumentoBin procDocBin) {
		if (!procDocBin.getSignatarios().isEmpty()) {
			return true;
		} else {
			Util.setMessage(Severity.ERROR, "O documento não está assinado.");
			return false;
		}
	}

	public String getModeloDocumento(){
		if (!isModeloVazio()){
			return getInstance().getModeloDocumento();
		}else{
			return "";
		}
	}

	public void setMostrarMinuta(Boolean mostrarMinuta) {
		this.mostrarMinuta = mostrarMinuta;
	}

	public Boolean getMostrarMinuta() {
		return mostrarMinuta;
	}

	public void alteraMostraMinuta(Boolean val) {
		mostrarMinuta = val;
	}

	public void setMinuta(ProcessoDocumento obj) {
		setMostrarMinuta(Boolean.TRUE);
		setProcessoDocumento(obj);
		setInstance(obj.getProcessoDocumentoBin());
		ProcessoDocumentoHome.instance().setInstance(obj);
		// Linha recolocada, pois, na análise da minuta, a sua ausência provocava o bug PJEII-457
		ProcessoHome.instance().setProcessoDocumentoBin(obj.getProcessoDocumentoBin());
		ProcessoHome.instance().setTipoProcessoDocumento(obj.getTipoProcessoDocumento());
		ProcessoHome.instance().onSelectProcessoDocumento();
	}

	public String atualizarMinuta() {
		String update = super.update();
		assinarDocumento();
		refreshGrid("minutaGrid");
		setMostrarMinuta(Boolean.FALSE);
		Events.instance().raiseEvent(EventsTreeHandler.REGISTRA_EVENTO_PD_EVENT, processoDocumento);
		ProcessoDocumento pd = ProcessoDocumentoHome.instance().getInstance();
		pd.setProcessoDocumento(ProcessoHome.instance().getTipoProcessoDocumento().toString());
		ProcessoHome.instance().setIdProcessoDocumento(pd.getIdProcessoDocumento());
		getEntityManager().merge(pd);
		EntityUtil.flush();
		newInstance();
		setAssinado(true);
		return update;
	}

	public void atualizaMinuta() {
		update();
		ProcessoDocumento pd = ProcessoDocumentoHome.instance().getInstance();
		pd.setTipoProcessoDocumento(ProcessoHome.instance().getTipoProcessoDocumento());
		ProcessoHome.instance().setIdProcessoDocumento(pd.getIdProcessoDocumento());
		getEntityManager().merge(pd);
		EntityUtil.flush();
		AutomaticEventsTreeHandler.instance().registraEventos();
		ProcessoDocumentoTrfLocalHome.instance().updateProcessoDocumentoTrf(pd.getIdProcessoDocumento());
	}

	public void apagarListaEventos() {
		ProcessoDocumento pd = ProcessoDocumentoHome.instance().getInstance();
		pd.setTipoProcessoDocumento(ProcessoHome.instance().getTipoProcessoDocumento());
		getEntityManager().merge(pd);
		EntityUtil.flush();
		AutomaticEventsTreeHandler.instance().registraEventos();
	}

	public Boolean getPeticaoAvulsaAnexada() {
		return peticaoAvulsaAnexada;
	}

	public void setPeticaoAvulsaAnexada(Boolean peticaoAvulsaAnexada) {
		this.peticaoAvulsaAnexada = peticaoAvulsaAnexada;
	}

	public Boolean foiAnexada() {
		return getPeticaoAvulsaAnexada();
	}

	public Boolean getVerificaPeticaoNaoLida() {
		StringBuilder query = new StringBuilder();

		query.append("select count(o) from ProcessoDocumentoPeticaoNaoLida a ");
		query.append("inner join a.processoDocumento o ");
		query.append("where o not in (select pl.processoDocumento from ProcessoDocumentoLido pl) ");
		query.append("and o.idProcessoDocumento = :idProcessoDocumento");
		Query q = getEntityManager().createQuery(query.toString());
		q.setParameter("idProcessoDocumento", ProcessoTrfHome.instance().getInstance().getIdProcessoTrf());

		try {
			Long resultado = (Long) q.getSingleResult();
			return resultado.compareTo(0L) > 0;
		} catch (NoResultException e) {
			return false;
		}
		
	}

	@Override
	public void newInstance() {
		FileHome.instance().clear();
		setAssinado(false);
		certChain = null;
		signature = null;
		super.newInstance();
	}
	
	/*
	 * PJE-JT: Athos Reiser : PJE-979 - 2012-01-16 Alteracoes feitas pela JT.
	 * Tratar problema de autorizacao na visualizacao de documentos.
	 * PJE-JT: Athos Reiser e Ricardo Scholz: PJE-1244 - 2012-01-24 Alteracoes feitas pela JT.
	 * Inserção de checagem se processoDocumento e processoDocumentoBin são consistentes.
	 */
	/**
	 * Sobrescrito metodo de carregamento para tratar as autorizacoes da visualizacao dos documentos do processo. Aos usuarios externos do tribunal
	 * somente sera possivel visualizar documentos que eles proprios criaram. Usuarios internos podem visualizar todos.
	 * 
	 * @return ProcessoDocumentoBin dados do documento do processo
	 * 
	 * @author Athos Reiser
	 * @since 1.4.0.3
	 * @category PJE-JT
	 */
	@Override
	protected ProcessoDocumentoBin loadInstance(){

		ProcessoDocumentoBin processoDocumentoBin = super.loadInstance();
		Usuario usuarioLogado = (Usuario) Contexts.getSessionContext().get("usuarioLogado");

		// Devido alguns casos a pesquisa por documento nao passar o id do processo documento
		String idProcessoDoc = null;
		if ( FacesContext.getCurrentInstance()!=null ) {
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			idProcessoDoc = (String) request.getParameter("idProcessoDoc");
		}

		// Devido existir algumas pesquisas q nao exige a autenticacao do usuario
		if (usuarioLogado == null
				|| idProcessoDoc == null){
			if (idProcessoDoc != null) {
				ProcessoDocumento processoDoc = getProcessoDocumento(idProcessoDoc);
				if (ProcessoDocumentoHome.isUsuarioExterno() && ProcessoDocumentoHome.instance().existePendenciaCienciaSemCache(processoDoc)) {
					
					FacesMessages.instance().clear();
					FacesMessages.instance().add(Severity.ERROR, "Visualização indisponível. Pendente de ciência pelo destinatário.");
					log.warn("Tentativa de visualizacao de documento pendente de ciência id "
						+ processoDoc.getIdProcessoDocumento()
						+ " sem usuário (externo).");
					
					verificaAutorizacao = false;
					
					return null;
				} else {
					return processoDocumentoBin;
				}
			} else {
				if (processoDocumentoBin == null || processoDocumentoBin.getProcessoDocumentoList() == null) {
					return processoDocumentoBin;
				}
				for (ProcessoDocumento processoDoc : processoDocumentoBin.getProcessoDocumentoList()){
					if (ProcessoDocumentoHome.isUsuarioExterno() && ProcessoDocumentoHome.instance().existePendenciaCienciaSemCache(processoDoc)) {
						
						FacesMessages.instance().clear();
						FacesMessages.instance().add(Severity.ERROR, "Visualização indisponível. Pendente de ciência pelo destinatário.");
						log.warn("Tentativa de visualizacao de documento pendente de ciência id "
							+ processoDoc.getIdProcessoDocumento()
							+ " sem usuário (externo).");
						
						verificaAutorizacao = false;
						
						return null;
					}
				}
				return processoDocumentoBin;
			}
		}
		
		ProcessoDocumento processoDocumento = getProcessoDocumento(idProcessoDoc);

		// Somente os usuarios internos e quem criou o documento, no caso de usuario externo, podem visualizar
		//Verifica também se há inconsistências entre as informações do processoDocumentoBin e do processoDocumento
	 	if (processoDocumento.getProcessoDocumentoBin().getIdProcessoDocumentoBin() !=
	 	 			processoDocumentoBin.getIdProcessoDocumentoBin() ||
	 	 			!verificarAutorizacaoVisualizacaoDocumento(processoDocumento)){

			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Usuário sem permissão para visualizar documento.");
			log.warn("Tentativa de visualizacao de documento id "
				+ processoDocumento.getIdProcessoDocumento()
				+ " pelo usuário: " + Authenticator.getUsuarioLogado().getLogin()
				+ " - id: " + Authenticator.getUsuarioLogado().getIdUsuario());
			
			verificaAutorizacao = false;
			
			return null;
		} else {
			if (
					!Pessoa.instanceOf(Authenticator.getPessoaLogada(),PessoaOficialJustica.class) && (
						(
							Authenticator.isUsuarioExterno() && 
							!Pessoa.instanceOf(Authenticator.getPessoaLogada(),PessoaProcurador.class) &&
							ProcessoDocumentoHome.instance().existePendenciaCiencia(processoDocumento)
						) ||
						(
							Authenticator.isProcurador() &&
							ProcessoDocumentoHome.instance().existePendenciaCiencia(processoDocumento)
						) 
					)
				) {
				
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR, "Visualização indisponível. Pendente de ciência pelo destinatário.");
				log.warn("Tentativa de visualizacao de documento pendente de ciência id "
					+ processoDocumento.getIdProcessoDocumento()
					+ " pelo usuário: " + Authenticator.getUsuarioLogado().getLogin()
					+ " - id: " + Authenticator.getUsuarioLogado().getIdUsuario());
				
				verificaAutorizacao = false;
				
				return null;
			}
		}
	 	
		return processoDocumentoBin;
	}

	private ProcessoDocumento getProcessoDocumento(String idProcessoDoc){
		/*
		 * PJE-JT: David Vieira : PJE-1244 - 2012-01-26 Alteracoes feitas pela JT.
		 * Correção de bug encontrado pela homologação do PJE-1260, ao abrir documento na aba Processo da Petição Inicial.
		 */
		if (ProcessoDocumentoHome.instance().getId() == null){
			ProcessoDocumentoHome.instance().setId(Integer.parseInt(idProcessoDoc));
		}

		ProcessoDocumento processoDocumento = ProcessoDocumentoHome.instance().getInstance();
		return processoDocumento;
	}
	
	/**
	 * Verifica se usuario logado tem autorizacao para visualizar o documento.
	 * 
	 * @param processoDocumento documento a ser verificado a autorizacao.
	 * @return true caso o usuario autenticado pelo sistema tem autorizacao para visualizar o documento, false caso contrario.
	 * 
	 * @author Athos Reiser
	 * @since 1.4.0.3
	 * @category PJE-JT
	 */
	public Boolean verificarAutorizacaoVisualizacaoDocumento(ProcessoDocumento processoDocumento){
		ProcessoDocumento doc = documentoJudicialService.getDocumento(processoDocumento.getIdProcessoDocumento(), processoDocumento.getProcessoTrf());
		if(doc != null){
			return true;
		}else{
			return verificarAutorizacaoVisualizacaoDocumento_(processoDocumento);
		}
	}

	/**
	 * Permite verificar a autorização:
	 * - se usuário do tribunal
	 * - ou se usuário que incluiu
	 * @param processoDocumento
	 * @return
	 */
	private Boolean verificarAutorizacaoVisualizacaoDocumento_(ProcessoDocumento processoDocumento){
		if (Authenticator.isUsuarioInterno()) {
			return true;
		}
		Usuario usuarioLogado = (Usuario) Contexts.getSessionContext().get("usuarioLogado");
		if(processoDocumento.getUsuarioInclusao().getIdUsuario().intValue() == usuarioLogado.getIdUsuario().intValue()){
			return true;
		}		

		// Caso documento seja sigiloso verificar se usuario logado tem permissao de visualizar
		if (processoDocumento.getDocumentoSigiloso()){
			String hql = "FROM " + ProcessoDocumentoVisibilidadeSegredo.class.getSimpleName() + " o "
				+ " WHERE o.pessoa.idUsuario = :idUsuario "
				+ " AND o.processoDocumento.idProcessoDocumento = :idProcessoDocumento";

			Query query = getEntityManager().createQuery(hql);
			query.setParameter("idUsuario", usuarioLogado.getIdUsuario());
			query.setParameter("idProcessoDocumento", processoDocumento.getIdProcessoDocumento());

			@SuppressWarnings("rawtypes")
			List resultado = query.getResultList();

			if (resultado != null && !resultado.isEmpty()){
				return true;
			}
		}

		ProcessoTrf processoTrf = getEntityManager().find(ProcessoTrf.class, processoDocumento.getProcesso().getIdProcesso());
		// Caso o processo tenha segredo eh verificado se usuario logado tem permissao de visualizar
		if (processoTrf.getSegredoJustica()){
			String hql = "FROM ProcessoVisibilidadeSegredo o "
				+ " WHERE o.pessoa.idUsuario = :idUsuario "
				+ " AND o.processo.idProcesso = :idProcesso";

			Query query = getEntityManager().createQuery(hql);
			query.setParameter("idUsuario", usuarioLogado.getIdUsuario());
			query.setParameter("idProcesso", processoTrf.getIdProcessoTrf());

			@SuppressWarnings("rawtypes")
			List resultado = query.getResultList();

			if (resultado != null && !resultado.isEmpty()){
				return true;
			} else {
				
				//Caso o documento seja sigiloso verificar se usuário logado é Procurador da Entidade que possui autorização de visibilidade
				hql = "select pdvs from ProcessoDocumentoVisibilidadeSegredo pdvs, PessoaProcuradoriaEntidade ppe, PessoaProcuradorProcuradoria ppp" +
						" where pdvs.processoDocumento.idProcessoDocumento = :idProcessoDocumento" +
						" and pdvs.pessoa = ppe.pessoa" +
						" and ppe = ppp.pessoaProcuradoriaEntidade" +
						" and ppp.pessoaProcurador.idUsuario = :idUsuario";
				
				query = getEntityManager().createQuery(hql);
				query.setParameter("idUsuario", usuarioLogado.getIdUsuario());
				query.setParameter("idProcessoDocumento", processoDocumento.getIdProcessoDocumento());
				
				resultado = query.getResultList();				
				
				if (resultado != null && !resultado.isEmpty()){
					return true;
				}				
			}
		}
		
		// Caso o processo tenha segredo verificar se usuário é procurador da procuradoria que gerou o expediente
				if(processoTrf.getSegredoJustica()){
					Query query = getEntityManager().createNativeQuery("select distinct 1 from tb_processo_trf o where o." + ConsultaProcessoTrfFilter.CONDITION_PROCURADOR);
					query.setParameter("idUsuario", usuarioLogado.getIdUsuario());
					
					@SuppressWarnings("rawtypes")
					List resultado = query.getResultList();

					if (resultado != null && !resultado.isEmpty()){
						return true;
					}
				}

		// Caso o doc. nao seja sigiloso e o processo nao tenha segredo, entao eh permitida a visualizacao
		if (!processoDocumento.getDocumentoSigiloso() && !processoTrf.getSegredoJustica()){
			return true;
		}

		return false;
	}

	/*
	 * PJE-JT: Fim.
	 */

	public void setAssinado(boolean assinado) {
		this.assinado = assinado;
	}

	public boolean isAssinado() {
		return assinado;
	}

	public void setExibeModalRemetido(boolean exibeModalRemetido) {
		this.exibeModalRemetido = exibeModalRemetido;
	}

	public boolean isExibeModalRemetido() {
		return exibeModalRemetido;
	}

	public void verificaRemetido2Grau() {
		// verifica se o processo foi remetido para o segundo grau
		if (ProcessoTrfHome.instance().verificaRemetido2Grau(
				EntityUtil.find(ProcessoTrf.class, ProcessoDocumentoHome.instance().getInstance().getProcesso()
						.getIdProcesso()))) {
			exibeModalRemetido = true;
			return;
		}
		assinarDocumento();
	}

	public String getSize(Integer sizeBytes) {
		if (sizeBytes != null && sizeBytes > 0) {
			NumberFormat formatter = new DecimalFormat("###,##0.00");
			float size = sizeBytes / 1024f;
			return formatter.format(size) + " Kb";
		} else {
			return null;
		}
	}
	
	public String verificaSegredo() {
		return verificaSegredo(false, false);
	}

	/*
     * PJE-JT: Ricardo Scholz : PJE-1056 - 2011-12-07 Alteracoes feitas pela JT.
     * Modificação para utilizar métodos encapsulados 'isProcessoDocumentoSigiloso' e
     * 'pertenceAProcessoSigiloso', de forma a evitar redundância na implementação na regra de
     * negócio que checa se um documento é sigiloso ou pertence a um processo sigiloso.
     * 
     * PJEII-758: Sérgio Pacheco : verificaSegredo() separado para usar a lógica em setDownloadInstanceVerificado()
     * que valida documento do tipo PDF.
     *
     * TJRJ: Maio/2024
     * Caso aplicarRegraDocSigiloso = true, passa a verificar o parâmetro: pje:consultaDocumento:permiteDocumentoSigiloso
     */
	public String verificaSegredo(boolean aplicarRegraDocSigiloso, boolean aplicarRegraDeNivelAcesso) {
		String codigo = ValidacaoAssinaturaProcessoDocumento.instance().getCodigoValidacaoDocumento(getInstance());
		String msgDocumentoSigiloso = "O Documento '" + codigo + "' é VÁLIDO mas sua visualização está indisponível no momento, pois está sob sigilo.";
		String msgProcessoSigiloso = "O Documento '" + codigo + "' é VÁLIDO mas sua visualização está indisponível no momento, pois ele pertence a um processo que está sob segredo de justiça.";
		String retorno = "";

		if (this.isProcessoDocumentoSigiloso()) {
			retorno = msgDocumentoSigiloso;
			if (aplicarRegraDocSigiloso) {
				boolean permiteVisualizarDocSigiloso = BooleanUtils.toBoolean(ParametroUtil.getParametro(Parametros.PJE_CONSULTA_DOCUMENTO_PERMITE_DOCUMENTO_SIGILOSO));
				retorno = !permiteVisualizarDocSigiloso ? msgDocumentoSigiloso : "";
			}
		}

		if (retorno.isEmpty() && this.pertenceAProcessoSigiloso(aplicarRegraDeNivelAcesso)) {
			retorno = msgProcessoSigiloso;
		}

		return retorno;
	}
    
    public String getVerificacaoSegredo() {
    	String retorno = verificaSegredo();
    	return retorno.isEmpty() ? getInstance().getModeloDocumento() : retorno;  
    }
    
    public String setDownloadInstanceVerificado() {
    	String retorno = verificaSegredo();
    	if ( retorno.isEmpty() ) {
    		return setDownloadInstance();
    	}
    	return retorno;  
    }
    
    /*
     * PJE-JT: Fim.
     */


	
	
    /*
     * PJE-JT: Ricardo Scholz : PJE-1056 - 2011-12-07 Alteracoes feitas pela JT.
     * Criação de método para checar se a instância do ProcessoDocumentoBin é sigilosa ou pertence 
     * a um processo sigiloso. Dois métodos auxiliares foram implementados para melhorar o 
     * encapsulamento.
     */
    /**
     * Verifica se o <code>ProcessoDocumento</code> referenciado por <code>instance</code> é
     * sigiloso ou encontra-se associado a um processo sigiloso. Por questões de otimização e
     * encapsulamento, executa a tarefa em dois passos. No primeiro, verifica se os documentos 
     * associados ao binário são sigilosos (não necessita acesso ao banco de dados). Caso nenhum
     * documento seja sigiloso, verifica se algum dos processos a que pertence cada documento é 
     * sigiloso (necessita acesso ao banco de dados).
     * 
     * @return true         caso algum dos documentos ou processos associados ao binário seja sigiloso
     *                 false        caso contrário.
     * @author Ricardo Scholz
     */
    public boolean isProcessoDocumentoPublicamenteVisivel(){
            //Primeiro passo - não acessa o banco de dados
            if(this.isProcessoDocumentoSigiloso() || 
                            //Segundo passo - acessa o banco de dados
                            this.pertenceAProcessoSigiloso()){
                    return true;
            }
            return false;
    }
    
    /**
     * Verifica se algum dos <code>ProcessoDocumento</code> relacionados a <code>instance</code> é
     * sigiloso.
     * 
     * @return      true    caso qualquer dos processos-documento associados é instância do binário
     *                                      seja sigiloso.
     * @author      Ricardo Scholz
     */
    private boolean isProcessoDocumentoSigiloso(){
            List<ProcessoDocumento> documentos = getInstance().getProcessoDocumentoList();
            for(ProcessoDocumento documento : documentos){
                    if(documento.getDocumentoSigiloso()){
                            return true;
                    }
            }
            return false;
    }
    
    /**
     * Verifica se algum <code>ProcessoTrf</code> associado a qualquer <code>ProcessoDocumento</code>
     * é sigiloso. Necessita acessar o banco de dados para recuperar a lista de <code>ProcessoTrf</code>
     * associada a cada <code>ProcessoDocumento</code>, uma vez que a partir da instância de 
     * <code>ProcessoDocumento</code> só é possível chegar ao objeto pai (<code>Processo</code>).
     * 
     * @return      true    caso qualquer <code>ProcessoTrf</code> associado a qualquer dos
     *                                      <code>ProcessoDocumento</code> relacionados a <code>instance</code> seja
     *                                      sigiloso.
     *                      false   caso contrário.
     *
     * @author      Ricardo Scholz
     *
     * TJRJ: Maio/2024
     * Caso aplicarRegraDeNivelAcesso = true, passa a verificar o parâmetro pje:consultaDocumento:niveisAcessoNaoPermitido
     */
    @SuppressWarnings("unchecked")
	private boolean pertenceAProcessoSigiloso(boolean aplicarRegraDeNivelAcesso) {
		int nivelAcessoProcesso = 0;
		String niveisDeAcessoNaoPermitido = "";
		if (aplicarRegraDeNivelAcesso) {
			niveisDeAcessoNaoPermitido = ParametroUtil.getParametro(Parametros.PJE_CONSULTA_DOCUMENTO_NIVEIS_ACESSO_NAO_PERMITIDO);
		}

		List<ProcessoDocumento> documentos = getInstance().getProcessoDocumentoList();
		for (ProcessoDocumento documento : documentos) {
			String query = "select o from ProcessoTrf o where o.processo.idProcesso = "
					+ documento.getProcesso().getIdProcesso();
			List<ProcessoTrf> processos = getEntityManager().createQuery(query).getResultList();
			for (ProcessoTrf processo : processos) {
				if (aplicarRegraDeNivelAcesso) {
					if (!niveisDeAcessoNaoPermitido.equals("-1")) {
						nivelAcessoProcesso = Math.max(nivelAcessoProcesso, processo.getNivelAcesso());
						List<Integer> niveis = CollectionUtilsPje.convertStringToIntegerList(niveisDeAcessoNaoPermitido);
						if (niveis.contains(nivelAcessoProcesso) && processo.getSegredoJustica()) {
							return true;
						}
					}
				} else if (processo.getSegredoJustica()) {
					return true;
				}
			}
		}
		return false;
	}
    /*
     * PJE-JT: Fim.
     */
    
    private boolean pertenceAProcessoSigiloso(){
        return pertenceAProcessoSigiloso(false);
    }

	public Date getDataInclusaoBin(Integer idBin) {
		ProcessoDocumentoBin pdBin;
		if (idBin != 0) {
			pdBin = getEntityManager().find(ProcessoDocumentoBin.class, idBin);
			return pdBin.getDataInclusao();
		}
		return null;

	}

	@SuppressWarnings("unchecked")
	private List<ProcessoParteExpediente> obtemExpedientesVinculados(Integer documento) {
		List<ProcessoParteExpediente> ret = new ArrayList<ProcessoParteExpediente>();
		if (this.getInstance() != null) {
			Pessoa pessoaLogada = (Pessoa) ProcessoHome.instance().getUsuarioLogado();
			String consultaStatusLeitura = "SELECT DISTINCT p " + "	FROM ProcessoParteExpediente AS p "
					+ "		JOIN p.processoParte.processoParteRepresentanteList reps " + "		JOIN p.processoExpediente pe "
					+ "		JOIN pe.processoDocumentoExpedienteList docs " + "	WHERE p.processoExpediente = pe "
					+ "		AND docs.anexo = false" + "		AND p.dtCienciaParte IS NULL "
					+ "		AND docs.processoDocumento.idProcessoDocumento = :processoDocumento "
					+ "		AND (p.processoParte.pessoa = :pessoa "
					+ "			OR (reps.tipoRepresentante = :tipoParteAdvogado AND reps.representante = :pessoa))";
			Query q = EntityUtil.getEntityManager().createQuery(consultaStatusLeitura);
			q.setParameter("pessoa", pessoaLogada);
			q.setParameter("tipoParteAdvogado", ParametroUtil.instance().getTipoParteAdvogado());
			q.setParameter("processoDocumento", documento);
			ret.addAll(q.getResultList());
		}
		return ret;
	}

	public void tomarCiencia(Integer documento) {
		List<ProcessoParteExpediente> expedientes = obtemExpedientesVinculados(documento);
		for (ProcessoParteExpediente ppe : expedientes) {
			ProcessoParteExpedienteHome ppeh = (ProcessoParteExpedienteHome) Component
					.getInstance("processoParteExpedienteHome");
			ppeh.cienciaIntimacao(ppe);
		}
	}

	@Override
	public ProcessoDocumentoBin getInstance() {
		if (verificaAutorizacao != null && !verificaAutorizacao) {
			return null;
		}
		
		ProcessoDocumentoBin instance = super.getInstance();
		instance.setContext(new Util().getContextPath());
		return instance;
	}

	public void setTaskInstanceId(Long taskInstanceId) {
		this.taskInstanceId = taskInstanceId;
	}

	public Long getTaskInstanceId() {
		return taskInstanceId;
	}

}