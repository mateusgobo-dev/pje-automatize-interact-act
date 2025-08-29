package br.com.infox.bpm.taskPage.FGPJE;

import java.io.Serializable;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.component.tree.EventosTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.pje.manager.ProcessoDocumentoTrfLocalManager;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrfLocal;

@Name(AtoMagistradoTaskPageAction.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class AtoMagistradoTaskPageAction extends DarCienciaPartesTaskAction implements Serializable{

	private static final String NOME_TRANSICAO_MINUTAR = "Voltar para Assessoria";
	private static final long serialVersionUID = 1L;
	public static final String NAME = "atoMagistradoTaskPageAction";

	@In
	protected transient ProcessoDocumentoManager processoDocumentoManager;
	@In
	private ProcessoDocumentoTrfLocalManager processoDocumentoTrfLocalManager;
	@In
	private AssinaturaDocumentoService assinaturaDocumentoService;

	private ProcessoDocumento processoDocumento;
	private ProcessoDocumentoBin processoDocumentoBin;
	private ProcessoDocumentoTrfLocal processoDocumentoTrfLocal;
	private String certChain;
	private String signature;
	private boolean minutaNaoEcontrada = false;

	private MinutarTaskPageAction minutarTaskPageAction;
	private boolean liberarConsultaPublica;

	public ProcessoDocumento getUltimaMinuta(){
		StringBuilder sqlPes = new StringBuilder();
		sqlPes.append("select o from ProcessoDocumento o where ");
		sqlPes.append(" o.tipoProcessoDocumento in (select tpd from TipoProcessoDocumentoTrf tpd ");
		sqlPes.append("where tpd.visivelAnaliseMinuta = true) and ");
		sqlPes.append("o.processo.idProcesso = :idProcesso  and ");
		sqlPes.append("o.tipoProcessoDocumento in (select u.tipoProcessoDocumento from TipoProcessoDocumentoPapel u where u.papel.idPapel in (:idPapeis)) and ");
		sqlPes.append("o.ativo = true");
		sqlPes.append("order by o.dataInclusao desc");

		Query query = EntityUtil.createQuery(sqlPes.toString());
		query.setParameter("idProcesso", ProcessoHome.instance().getId());
		query.setParameter("idPapeis", ParametroUtil.instance().getIdsPapeisEditarMinuta());
		return EntityUtil.getSingleResult(query);
	}

	public void initPage(){
		if (processoDocumento != null){
			return;
		}

		minutaNaoEcontrada = false;
		processoDocumento = getUltimaMinuta();

		if (processoDocumento == null){
			minutaNaoEcontrada = true;
			return;
		}
		processoDocumentoBin = processoDocumento.getProcessoDocumentoBin();
		getOrCreateProcessoDocumentoTrfLocal();
		System.out.println(processoDocumentoTrfLocal);
		onSelectProcessoDocumento();
		EventosTreeHandler instance = (EventosTreeHandler) EventosTreeHandler.instance();
		instance.carregaEventos(processoDocumento, processoDocumento.getTipoProcessoDocumento());
	}

	private void getOrCreateProcessoDocumentoTrfLocal(){
		processoDocumentoTrfLocal = EntityUtil.find(ProcessoDocumentoTrfLocal.class,
				processoDocumento.getIdProcessoDocumento());
		if (processoDocumentoTrfLocal == null){
			processoDocumentoTrfLocal = new ProcessoDocumentoTrfLocal();
			processoDocumentoTrfLocal.setDecisaoTerminativa(false);
			processoDocumentoTrfLocal.setProcessoDocumento(processoDocumento);
			processoDocumentoTrfLocal.setIdProcessoDocumentoTrf(processoDocumento.getIdProcessoDocumento());
			processoDocumentoTrfLocal.setExibirDocMinuta(false);
			processoDocumentoTrfLocalManager.persist(processoDocumentoTrfLocal);
		}
	}

	public void onSelectProcessoDocumento(){
		if (null != processoDocumento){
			ProcessoHome.instance().onSelectProcessoDocumento(processoDocumento.getTipoProcessoDocumento(), getEventosTreeHandler());
		}
	}

	public boolean isAssinaturaLiberada(){
		ProcessoDocumentoManager documentoManager = new ProcessoDocumentoManager();
		boolean possuiVisibilidadeMinuta = documentoManager.possuiVisibilidadeParaTipoProcessoDocumento(Authenticator
				.getPapelAtual(), processoDocumento.getTipoProcessoDocumento().getIdTipoProcessoDocumento());
		return possuiVisibilidadeMinuta && !isMinutaAssinada();
	}

	public boolean isMinutaAssinada(){
		AssinaturaDocumentoService assinaturaDocumentoService = ComponentUtil
				.getComponent(AssinaturaDocumentoService.NAME);
		return assinaturaDocumentoService.isDocumentoAssinado(processoDocumento);
	}

	public void gravarMinuta(){
		try{
			updateMinuta();
		} catch (Exception e){
			FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar minuta: " + e.getMessage(), e);
		}
	}

	public void assinarMinuta(){
		try{
			updateMinuta();

			if (!EventosTreeHandler.instance().possuiEventoBeanSelecionado()){
				FacesMessages.instance().add(Severity.ERROR, "Nenhum evento selecionado");
				return;
			}

			processoDocumento.setPapel(Authenticator.getPapelAtual());
			assinaturaDocumentoService.assinarDocumento(processoDocumento.getProcessoDocumentoBin(), signature, certChain);
			processoDocumentoManager.persist(processoDocumento);

			EventosTreeHandler.instance().registraEventosProcessoDocumento(processoDocumento);

			if (ParametroUtil.instance().isPrimeiroGrau()){
				end(TaskNamesPrimeiroGrau.CONHECIMENTO_SECRETARIA);
				if (processoDocumento.getTipoProcessoDocumento().equals(
						ParametroUtil.instance().getTipoProcessoDocumentoSentenca())){
					intimarPartesAutomaticamente(processoDocumento);
				}
				else{
					setAvisoIntimacao("Processo remetido para ''Secretaria''.");
				}
			}
			else{
				end(MinutarSegundoGrauTaskPageAction.instance().getNomeTarefaSecretaria());
				setAvisoIntimacao("Processo remetido para ''Secretaria''.");
			}
			FacesMessages.instance().clear();

			processoDocumentoTrfLocalManager.criarDocumentoPublico(processoDocumento, liberarConsultaPublica);
			Util.setToEventContext("canClosePanel", true);

		} catch (Exception e){
			FacesMessages.instance().add(Severity.ERROR, "Erro ao assinar minuta: " + e.getMessage(), e);
		}
	}

	@Override
	public void end(String transitionName){
		getEventosTreeHandler().clearList();
		// TODO Rebeca vai verificar se essa regra vai ser somente para o
		// segundo grau
		if (!ParametroUtil.instance().isPrimeiroGrau()){
			if (isTarefaConhecimentoSecretaria(transitionName) && !isMinutaAssinada()){
				inativarMinuta();
			}
			if (isTarefaMinutar(transitionName)){
				Util.setToEventContext("canClosePanel", true);
			}
		}
		super.end(transitionName);
	}

	private boolean isTarefaMinutar(String transitionName){
		return NOME_TRANSICAO_MINUTAR.equals(transitionName)
			|| TaskNamesSegundoGrau.MINUTAR_SREEO.equals(transitionName);
	}

	private EventosTreeHandler getEventosTreeHandler(){
		return (EventosTreeHandler) EventosTreeHandler.instance();
	}

	private void inativarMinuta(){
		getMinutarTaskAction().inativarMinuta(processoDocumento);
	}

	public boolean isTarefaConhecimentoSecretaria(String transitionName){
		return getMinutarTaskAction().isTarefaConhecimentoSecretaria(transitionName);
	}

	private void updateMinuta(){
		EntityManager em = EntityUtil.getEntityManager();
		em.merge(processoDocumentoBin);
		em.merge(processoDocumento);
		em.merge(processoDocumentoTrfLocal);
		EventosTreeHandler.instance().registrarEventosJbpm(processoDocumento);
		em.flush();
	}

	public ProcessoDocumento getProcessoDocumento(){
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento){
		this.processoDocumento = processoDocumento;
	}

	public ProcessoDocumentoBin getProcessoDocumentoBin(){
		return processoDocumentoBin;
	}

	public void setProcessoDocumentoBin(ProcessoDocumentoBin processoDocumentoBin){
		this.processoDocumentoBin = processoDocumentoBin;
	}

	public ProcessoDocumentoTrfLocal getProcessoDocumentoTrfLocal(){
		return processoDocumentoTrfLocal;
	}

	public void setProcessoDocumentoTrfLocal(ProcessoDocumentoTrfLocal processoDocumentoTrfLocal){
		this.processoDocumentoTrfLocal = processoDocumentoTrfLocal;
	}

	public String getCertChain(){
		return certChain;
	}

	public void setCertChain(String certChain){
		this.certChain = certChain;
	}

	public String getSignature(){
		return signature;
	}

	public void setSignature(String signature){
		this.signature = signature;
	}

	public boolean isMinutaNaoEcontrada(){
		return minutaNaoEcontrada;
	}

	public static AtoMagistradoTaskPageAction instance(){
		return ComponentUtil.getComponent(NAME);
	}

	private MinutarTaskPageAction getMinutarTaskAction(){
		if (minutarTaskPageAction == null){
			if (ParametroUtil.instance().isPrimeiroGrau()){
				minutarTaskPageAction = MinutarPrimeiroGrauTaskPageAction.instance();
			}
			else{
				minutarTaskPageAction = MinutarSegundoGrauTaskPageAction.instance();
			}
		}
		return minutarTaskPageAction;
	}

	public void setLiberarConsultaPublica(boolean liberarConsultaPublica){
		this.liberarConsultaPublica = liberarConsultaPublica;
	}

	public boolean getLiberarConsultaPublica(){
		return liberarConsultaPublica;
	}
}
