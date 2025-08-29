package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.Identity;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ConsultaProcessoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ConsultaProcessoAction implements Serializable, ArquivoAssinadoUploader {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "consultaProcessoAction";

	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	@In(create = true)
	private DocumentoJudicialService documentoJudicialService;
	
	@In(required=true)
	private Identity identity;
			
	@In(create=false, required=true)
	private FacesMessages facesMessages;
	
	@Create
	public void iniciar() {
		if(this.protocolarDocumentoBean == null){			
			
			ProcessoJudicialAction action = ComponentUtil.getComponent(ProcessoJudicialAction.NAME);
			
			ProcessoTrf processoJudicial = action.getProcessoJudicial();
			
			if(this.identity.hasRole(Papeis.INTERNO)){
				this.protocolarDocumentoBean = new ProtocolarDocumentoBean(processoJudicial.getIdProcessoTrf(), 
						ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL 
						| ProtocolarDocumentoBean.LANCAR_MOVIMENTACAO
						| ProtocolarDocumentoBean.PERMITE_SELECIONAR_MOVIMENTACAO
						| ProtocolarDocumentoBean.PERMITIR_VINCULACAO_RESPOSTA
						| ProtocolarDocumentoBean.UTILIZAR_MODELOS, getActionName());
			}
			else{
				this.protocolarDocumentoBean = new ProtocolarDocumentoBean(processoJudicial.getIdProcessoTrf(), 
						ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL 
						| ProtocolarDocumentoBean.LANCAR_MOVIMENTACAO
						| ProtocolarDocumentoBean.PERMITIR_VINCULACAO_RESPOSTA
						| ProtocolarDocumentoBean.MOSTRAR_ESCOLHA_PARTES, getActionName());
			}
		}				
	}

	public void concluirInclusaoPeticaoDocumento() {
		this.protocolarDocumentoBean.concluirAssinatura();
	}
	
	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
	}

	@Override
	public String getActionName() {
		return NAME;
	}

	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}	
}