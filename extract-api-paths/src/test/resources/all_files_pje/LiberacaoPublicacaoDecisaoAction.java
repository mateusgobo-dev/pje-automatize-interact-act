package br.com.infox.cliente.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.dao.GenericDAO;
import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.manager.SessaoManager;
import br.jus.cnj.pje.nucleo.MuralException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.LiberacaoPublicacaoDecisaoService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualImpl;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.view.BaseAction;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.pje.nucleo.entidades.LiberacaoPublicacaoDecisao;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.SituacaoPublicacaoLiberacaoEnum;
import br.jus.pje.nucleo.enums.TipoDecisaoPublicacaoEnum;
import br.jus.pje.nucleo.enums.TipoPublicacaoEnum;

@Name(LiberacaoPublicacaoDecisaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class LiberacaoPublicacaoDecisaoAction extends BaseAction<LiberacaoPublicacaoDecisao> {
	private static final long serialVersionUID = -4788450557624621775L;
	public final static String NAME = "liberacaoPublicacaoDecisaoAction";
	private String horaLimitePublicacao = null;
	private TipoProcessoDocumento tipoDocumentoEscolhido;
	private ProcessoDocumento processoDocumento;

	@In
	private TramitacaoProcessualService tramitacaoProcessualService;
	
	@In
	private SessaoManager sessaoManager;

	@In
 	private GenericDAO genericDAO;
	
	@In
	private LiberacaoPublicacaoDecisaoService liberacaoPublicacaoDecisaoService; 

	/**
	 * Documentos: Lista dos documentos do processo que será vinculado ao expediente.
	 */
	private List<ProcessoDocumento> listaDocumentoProcesso = null;

	/**
	 * Data da Sessão: Lista das sessões criadas pelo Assessor de Plenário.
	 */
	List<Sessao> listaSessaoNaoFinalizada;

	Sessao sessao;
	/**
	 * Data da sessão de julgamento escolhida
	 */
	private Date dataSessoesJulgamento;
	
	private TipoPublicacaoEnum tipoPublicacaoEnum;

	private boolean sucessoGravacao=false;
	

	/**
	 * Efetua a liberação da sessão
	 */
	public void liberarPublicacao() throws MuralException {
		TramitacaoProcessualService tramitacaoProcessualService = ComponentUtil.getComponent(TramitacaoProcessualImpl.class);
		LiberacaoPublicacaoDecisao liberacaoPublicacaoDecisao = null;
		if(validarLiberacao()) {
			try {
				liberacaoPublicacaoDecisao = new LiberacaoPublicacaoDecisao();
				if(tipoPublicacaoEnum.isSessao()) {
					liberacaoPublicacaoDecisao.setDataSessao(sessao.getDataSessao());
					liberacaoPublicacaoDecisao.setSessao(sessao);
				}
				liberacaoPublicacaoDecisao.setNumeroProcesso(tramitacaoProcessualService.recuperaProcesso().getNumeroProcesso());
				liberacaoPublicacaoDecisao.setTipoPublicacao(tipoPublicacaoEnum);
				liberacaoPublicacaoDecisao.setTipoDecisaoPublicacao(TipoDecisaoPublicacaoEnum.MONOCRATICA);
				liberacaoPublicacaoDecisao.setProcessoDocumento(this.processoDocumento);
				liberacaoPublicacaoDecisao.setSituacaoPublicacaoLiberacao(SituacaoPublicacaoLiberacaoEnum.CRIADA);
				liberacaoPublicacaoDecisaoService.liberarPublicacao(liberacaoPublicacaoDecisao, this.processoDocumento);
				sucessoGravacao=true;
				facesMessages.addFromResourceBundle("Liberado para publicação");
			} catch (MuralException e) {
				sucessoGravacao=false;
				facesMessages.addFromResourceBundle(e.getCode());
			}
		} 
	}
	
	
	boolean validarLiberacao() {
		boolean retorno = true;
		if(this.processoDocumento == null) {
			facesMessages.addFromResourceBundle("Selecione um documento");
			retorno = false;
		}
		if(this.tipoPublicacaoEnum == null ) {
			facesMessages.addFromResourceBundle("Selecione um tipo de publicação");
			retorno = false;
		} else {
			if( this.tipoPublicacaoEnum.isSessao() && this.sessao == null ) {
				facesMessages.addFromResourceBundle("Selecione uma sessão");
				retorno = false;
			}
		}
		return retorno;
	}

	
	public boolean verificarDocumentoSelecionado(ProcessoDocumento processoDocumentoLinha) {
		boolean retorno = false;
		if(this.processoDocumento != null && processoDocumentoLinha != null) {
			retorno = processoDocumentoLinha.equals(this.processoDocumento);
		}
		return retorno;
	}

	/**
	 * Tipo de documento a ser publicado
	 * 
	 * @return List<TipoProcessoDocumento>
	 */
	public List<TipoProcessoDocumento> getTiposDocumentosTexto(){
		List<TipoProcessoDocumento> tipos =  new ArrayList<TipoProcessoDocumento>();
		try {
			tipos.add(ParametroUtil.instance().getTipoProcessoDocumentoDecisao());
			tipos.add(ParametroUtil.instance().getTipoProcessoDocumentoDespacho());	
		} catch (Exception e) {
			logger.error("Não foi possível recuperar a lista de tipos de documentos disponíveis: {0}", e.getLocalizedMessage());
			tipos = Collections.emptyList();
		}
		return tipos;
	}
	
	public List<ProcessoDocumento> listarDocumentosGrid() {
	    if(this.listaDocumentoProcesso == null && tipoDocumentoEscolhido != null) {
	    	ProcessoDocumentoManager processoDocumentoManager = ComponentUtil.getComponent(ProcessoDocumentoManager.NAME);
	    	TramitacaoProcessualService tramitacaoProcessualService = ComponentUtil.getComponent(TramitacaoProcessualImpl.class);
	    	this.listaDocumentoProcesso = processoDocumentoManager.listarDocumentosPrincipais(tramitacaoProcessualService.recuperaProcesso(), tipoDocumentoEscolhido.getIdTipoProcessoDocumento());
	    	if(this.listaDocumentoProcesso != null && !this.listaDocumentoProcesso.isEmpty()) {
	    		List<ProcessoDocumento> listaCaminhar = new ArrayList<ProcessoDocumento>(this.listaDocumentoProcesso);
	    		for (ProcessoDocumento documento : listaCaminhar) {
	    			ProcessoExpedienteManager processoExpedienteManager = ComponentUtil.getComponent(ProcessoExpedienteManager.class);
	    			if(liberacaoPublicacaoDecisaoService.verificarExistenciaLiberacao(documento) || processoExpedienteManager.verificarExistenciaExpedientePublicacao(documento.getIdProcessoDocumento(), ExpedicaoExpedienteEnum.values())) {
	    				this.listaDocumentoProcesso.remove(documento);
	    			}
	    		}
	    		if(this.listaDocumentoProcesso.size()==1) {
		    		this.processoDocumento = this.listaDocumentoProcesso.get(0);
		    	}
	    	}
	    	
	    }
	    return this.listaDocumentoProcesso;
	}

	public void limparListaDocumentos() {
	    this.listaDocumentoProcesso = null;
	}
	
	/**
	 * Limpa os documentos selecionados
	 */
	public void limparSelecaoDocumentos() {
		this.processoDocumento = null;				
	}


	public List<ProcessoDocumento> getListaDocumentoProcesso() {
		return listaDocumentoProcesso;
	}

	public void setListaDocumentoProcesso(List<ProcessoDocumento> listaDocumentoProcesso) {
		this.listaDocumentoProcesso = listaDocumentoProcesso;
	}

	public LiberacaoPublicacaoDecisaoService getLiberacaoPublicacaoDecisaoService() {
		return liberacaoPublicacaoDecisaoService;
	}

	public void setLiberacaoPublicacaoDecisaoService(
			LiberacaoPublicacaoDecisaoService liberacaoPublicacaoDecisaoService) {
		this.liberacaoPublicacaoDecisaoService = liberacaoPublicacaoDecisaoService;
	}

	public TipoPublicacaoEnum getTipoPublicacaoEnum() {
		return tipoPublicacaoEnum;
	}

	public void setTipoPublicacaoEnum(TipoPublicacaoEnum tipoPublicacaoEnum) {
		this.tipoPublicacaoEnum = tipoPublicacaoEnum;
	}

	public Date getDataSessoesJulgamento() {
		return dataSessoesJulgamento;
	}

	public void setDataSessoesJulgamento(Date dataSessoesJulgamento) {
		this.dataSessoesJulgamento = dataSessoesJulgamento;
	}

	public List<Sessao> getListaSessaoNaoFinalizada() {
		listaSessaoNaoFinalizada = sessaoManager.getSessoesNaoFinalizadas(liberacaoPublicacaoDecisaoService.horaLimitePublicacao());
		return listaSessaoNaoFinalizada;
	}

	public void setListaSessaoNaoFinalizada (
			List<Sessao> listaSessaoNaoFinalizada) {
		this.listaSessaoNaoFinalizada = listaSessaoNaoFinalizada;
	}

	public Sessao getSessao() {
		return sessao;
	}

	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}
	
	public String getHoraLimitePublicacao() {
		if(horaLimitePublicacao == null) {
			horaLimitePublicacao = liberacaoPublicacaoDecisaoService.recuperarVariavelFluxoDefineHoraLimitePublicacao();
		}
		
		return horaLimitePublicacao;
	}

	public void setHoraLimitePublicacao(String horaLimitePublicacao) {
		this.horaLimitePublicacao = horaLimitePublicacao;
	}

	public boolean isSucessoGravacao() {
		return sucessoGravacao;
	}

	public void setSucessoGravacao(boolean sucessoGravacao) {
		this.sucessoGravacao = sucessoGravacao;
	}

	@Override
	protected BaseManager<LiberacaoPublicacaoDecisao> getManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityDataModel<LiberacaoPublicacaoDecisao> getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	public TipoProcessoDocumento getTipoDocumentoEscolhido() {
		return tipoDocumentoEscolhido;
	}

	public void setTipoDocumentoEscolhido(TipoProcessoDocumento tipoDocumentoEscolhido) {
		this.tipoDocumentoEscolhido = tipoDocumentoEscolhido;
	}
	
	public TipoPublicacaoEnum[] obterAllTipoPublicacaoEnum() {
		return TipoPublicacaoEnum.values();
	}

	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

}
