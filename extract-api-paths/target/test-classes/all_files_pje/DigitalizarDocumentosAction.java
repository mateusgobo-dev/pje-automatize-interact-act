package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.view.DocumentoJudicialDataModel;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

import javax.servlet.http.HttpServletRequest;

@Name("digitalizarDocumentosAction")
@Scope(ScopeType.CONVERSATION)
public class DigitalizarDocumentosAction implements Serializable,ArquivoAssinadoUploader {

	private static final long serialVersionUID = 8802876019051984423L;

	private static final String VINCULA_DOCUMENTO_PRINCIPAL = "pje:fluxo:digitalizacao:vinculaDocumentoPrincipal";
	private static final String MINUTA = "pje:fluxo:digitalizacao:minuta";
	private static final String DOCUMENTO_PRINCIPAL = "pje:fluxo:digitalizacao:documentoPrincipal";
	private static final String DOCUMENTOS_ANEXOS = "pje:fluxo:digitalizacao:documentosAnexos";
	public static final String DOCUMENTO_PRINCIPAL_FINALIZADO = "pje:fluxo:digitalizacao:documentoPrincipalFinalizado";
	public static final String DOCUMENTOS_ANEXOS_FINALIZADOS = "pje:fluxo:digitalizacao:documentosAnexosFinalizados";
	
	private transient ProtocolarDocumentoBean protocolarDocumentoBean;
	private transient DocumentoJudicialDataModel documentoJudicialDataModel;
	private ProcessoTrf processoJudicial;
	private Boolean vinculaDocumentoPrincipal = true;
	private Boolean minutaDigitalizacao = false;
	private Integer idDocumentoPrincipal = null;
	
	@In
	private transient FacesMessages facesMessages;
	
	@In
	private transient TramitacaoProcessualService tramitacaoProcessualService;

	@In
	private transient DocumentoJudicialService documentoJudicialService;
	
	@In
	private transient ProcessoDocumentoManager processoDocumentoManager;
	
	@In(create = true, required = true)
	private transient TaskInstanceUtil taskInstanceUtil;
	
	
	@SuppressWarnings("unchecked")
	@Create
	public void init() {
		if(processoJudicial == null){
			processoJudicial = tramitacaoProcessualService.recuperaProcesso();
		}
		
		if(protocolarDocumentoBean == null){
			protocolarDocumentoBean = new ProtocolarDocumentoBean(processoJudicial.getIdProcessoTrf(),
					false,true,false,false,false,true,false,getActionName());
		}
		if(tramitacaoProcessualService != null){
			String recupera = (String)tramitacaoProcessualService.recuperaVariavelTarefa(VINCULA_DOCUMENTO_PRINCIPAL);
			if(recupera != null){
				setVinculaDocumentoPrincipal(recupera.toLowerCase().equals("true"));
			}
			
			String recuperaMinuta = (String)tramitacaoProcessualService.recuperaVariavelTarefa(MINUTA);
			if(recuperaMinuta != null){
				setMinutaDigitalizacao(recuperaMinuta.toLowerCase().equals("true"));
			}
			
			Integer valor = (Integer)tramitacaoProcessualService.recuperaVariavel(DOCUMENTO_PRINCIPAL);
			if(valor != null && valor.equals(0)){
				valor = null;
				tramitacaoProcessualService.apagaVariavel(DOCUMENTO_PRINCIPAL);
			}

			if(valor != null){
				setIdDocumentoPrincipal(valor);
				Map<Integer,Integer> arquivosaux = (Map<Integer,Integer>) tramitacaoProcessualService.recuperaVariavel(DOCUMENTOS_ANEXOS);
				if(arquivosaux != null){
					Map<Integer, Integer> arquivosAnexos = new TreeMap<Integer, Integer>();
					arquivosAnexos.putAll(arquivosaux);
					try {
						protocolarDocumentoBean.setDocumentoPrincipal(processoDocumentoManager.findById(getIdDocumentoPrincipal()));
						ArrayList<ProcessoDocumento> documentosAnexos = new ArrayList<ProcessoDocumento>(arquivosAnexos.size());
						List<Integer> remocoes  = new ArrayList<Integer>();
						for (Map.Entry<Integer, Integer> entry : arquivosAnexos.entrySet()) { 
							ProcessoDocumento pd = processoDocumentoManager.findById(entry.getValue());
							if(pd == null){
								remocoes.add(entry.getKey());
								continue;
							}
							documentosAnexos.add(processoDocumentoManager.findById(entry.getValue()));
						}
						if(!remocoes.isEmpty()){	
							for(Integer j : remocoes){
								arquivosAnexos.remove(j);
							}
							tramitacaoProcessualService.gravaVariavel(DOCUMENTOS_ANEXOS, arquivosAnexos);
						}
						
						protocolarDocumentoBean.setArquivos(documentosAnexos);
						protocolarDocumentoBean.setContador(documentosAnexos.size());
						
					} catch (PJeBusinessException e) {
						protocolarDocumentoBean.setDocumentoPrincipal(null);
					}
				}else{
					protocolarDocumentoBean.setDocumentoPrincipal(null);
					setIdDocumentoPrincipal(null);
				}
			}
		
		}
		
		if(documentoJudicialDataModel == null  && vinculaDocumentoPrincipal && getIdDocumentoPrincipal() == null){
			documentoJudicialDataModel = new DocumentoJudicialDataModel();
			documentoJudicialDataModel.setProcessoJudicial(this.processoJudicial);
			documentoJudicialDataModel.setDocumentoJudicialService(this.documentoJudicialService);
			documentoJudicialDataModel.setOrdemDecrescente(true);
			documentoJudicialDataModel.setMostrarPdf(false);
			documentoJudicialDataModel.setIncluirDocumentoPeticaoInicial(true);
			documentoJudicialDataModel.setIncluirComAssinaturaInvalidada(true);
			
		}
		
		
	}

	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}

	public void setProtocolarDocumentoBean(ProtocolarDocumentoBean protocolarDocumentoBean) {
		this.protocolarDocumentoBean = protocolarDocumentoBean;
	}

	public DocumentoJudicialDataModel getDocumentoJudicialDataModel() {
		return documentoJudicialDataModel;
	}

	public void setDocumentoJudicialDataModel(DocumentoJudicialDataModel documentoJudicialDataModel) {
		this.documentoJudicialDataModel = documentoJudicialDataModel;
	}
	
	public void setSelected() {
        ProcessoDocumento documentoPrincipal = ((ProcessoDocumento)documentoJudicialDataModel.getRowData());
        if(documentoPrincipal != null){
        	protocolarDocumentoBean.setDocumentoPrincipal(documentoPrincipal);
        }
    }
	
	public String getDownloadLinks(){
		List<ProcessoDocumento> docs = new ArrayList<ProcessoDocumento>();
		docs.addAll(protocolarDocumentoBean.getArquivos());
		return documentoJudicialService.getDownloadLinks(docs);
	}

	public Boolean getVinculaDocumentoPrincipal() {
		return vinculaDocumentoPrincipal;
	}

	public void setVinculaDocumentoPrincipal(Boolean vinculaDocumentoPrincipal) {
		this.vinculaDocumentoPrincipal = vinculaDocumentoPrincipal;
	}
	
	public void gravar(ProcessoDocumento processoDocumento){
		getProtocolarDocumentoBean().gravar(processoDocumento);
		gravarVariavelFluxo();
		facesMessages.add(Severity.INFO,"Minuta de digitalização realizada com sucesso");
	}
	
	public void alterarOrdem(int posicaoAtual, int novaPosicao){
		protocolarDocumentoBean.alterarOrdem(posicaoAtual, novaPosicao);
		gravarVariavelFluxo();
	}
	
	public void gravarTodos(){
		getProtocolarDocumentoBean().gravarTodos();
		gravarVariavelFluxo();
		facesMessages.add(Severity.INFO,"Minuta de digitalização realizada com sucesso");
	}
	
	private void gravarVariavelFluxo(){
		Map<Integer,Integer> ids = new HashMap<Integer,Integer>(0);
		Integer idPrincipal = null;
		if(protocolarDocumentoBean.getDocumentoPrincipal() != null){
				idPrincipal = protocolarDocumentoBean.getDocumentoPrincipal().getIdProcessoDocumento();
			
		}
		if(protocolarDocumentoBean.getArquivos() != null ){
			for(int i = 0; i < protocolarDocumentoBean.getArquivos().size(); i++){
					ids.put(i,protocolarDocumentoBean.getArquivos().get(i).getIdProcessoDocumento());
			}	
				
		}
		tramitacaoProcessualService.gravaVariavel(DOCUMENTO_PRINCIPAL, idPrincipal);
		tramitacaoProcessualService.gravaVariavel(DOCUMENTOS_ANEXOS, ids);
	}

	public Integer getIdDocumentoPrincipal() {
		return idDocumentoPrincipal;
	}

	public void setIdDocumentoPrincipal(Integer idDocumentoPrincipal) {
		this.idDocumentoPrincipal = idDocumentoPrincipal;
	}

	public Boolean getMinutaDigitalizacao() {
		return minutaDigitalizacao;
	}

	public void setMinutaDigitalizacao(Boolean minutaDigitalizacao) {
		this.minutaDigitalizacao = minutaDigitalizacao;
	}
	
	public void remove(Integer docId) throws PJeBusinessException{
		protocolarDocumentoBean.remove(docId);
		Map<Integer,Integer> ids = new HashMap<Integer,Integer>(0);
		if(protocolarDocumentoBean.getArquivos() != null ){
			int i =0;
			for (ProcessoDocumento pd: protocolarDocumentoBean.getArquivos()) { 
					ids.put(i, pd.getIdProcessoDocumento());
					i++;
			}
			protocolarDocumentoBean.setContador(i);
		}
		
		tramitacaoProcessualService.gravaVariavel(DOCUMENTOS_ANEXOS,ids);
		init();
	}
	
	@SuppressWarnings("unchecked")
	public void concluir(){
		protocolarDocumentoBean.concluirAssinatura();
		Map<Integer,Integer> mapa = (Map<Integer, Integer>) tramitacaoProcessualService.recuperaVariavel(DOCUMENTOS_ANEXOS);

		if(mapa.size() > 0){
			String complemento = "documento";

			if(mapa.size() > 1){
				complemento = "documentos";
			}

			MovimentoAutomaticoService.preencherMovimento().
					deCodigo(581).
					comProximoComplementoVazio().
					doTipoLivre().
					preencherComTexto(complemento).
					lancarMovimento();
		}


		tramitacaoProcessualService.gravaVariavel(DOCUMENTO_PRINCIPAL_FINALIZADO,
					tramitacaoProcessualService.recuperaVariavel(DOCUMENTO_PRINCIPAL));
		tramitacaoProcessualService.gravaVariavel(DOCUMENTOS_ANEXOS_FINALIZADOS, mapa);
		tramitacaoProcessualService.apagaVariavel(DOCUMENTO_PRINCIPAL);
		tramitacaoProcessualService.apagaVariavel(DOCUMENTOS_ANEXOS);
		String transicaoSaida = (String)taskInstanceUtil.getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);

		if(transicaoSaida != null){
			TaskInstanceHome.instance().end(transicaoSaida);
		}

	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
	}

	@Override
	public String getActionName() {
		return "digitalizarDocumentosAction";
	}
}