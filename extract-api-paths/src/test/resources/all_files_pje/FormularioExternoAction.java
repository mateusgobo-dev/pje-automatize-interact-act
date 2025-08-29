package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;
import org.json.JSONArray;
import org.json.JSONObject;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.view.fluxo.ProcessoJudicialAction;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(FormularioExternoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class FormularioExternoAction implements Serializable, ArquivoAssinadoUploader{

	public static final String NAME = "formularioExternoAction";
	
	private static final long serialVersionUID = 1L;
	private String perguntasJSON;
	private String respostasJSON;
	
	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	@In
	private TramitacaoProcessualService tramitacaoProcessualService;
	
	@In(required=true)
	private Identity identity;
	
	@Create
	public void init() {
		if(this.protocolarDocumentoBean == null){	
			
			ProcessoJudicialAction action = ComponentUtil.getComponent(ProcessoJudicialAction.NAME);
			
			ProcessoTrf processoJudicial = action.getProcessoJudicial();
			
			if(processoJudicial == null) {
				processoJudicial = ProcessoTrfHome.instance().getInstance();
			}
			
			if(this.identity.hasRole(Papeis.INTERNO)){
				this.protocolarDocumentoBean = new ProtocolarDocumentoBean(processoJudicial.getIdProcessoTrf(), 
						ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL 
						| ProtocolarDocumentoBean.LANCAR_MOVIMENTACAO, NAME);
			}
			else{
				this.protocolarDocumentoBean = new ProtocolarDocumentoBean(processoJudicial.getIdProcessoTrf(), 
						ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL 
						| ProtocolarDocumentoBean.LANCAR_MOVIMENTACAO, NAME);
			}
		}				
	}	
	
	public void criarDocumento() throws JSONException {
		Map<String, String> perguntasRespostas = new LinkedHashMap<>();
		
		JSONArray perguntas = new JSONArray(this.perguntasJSON);
		JSONObject respostas = new JSONObject(this.respostasJSON);
		
		String id;
		
		for (int i = 0; i < perguntas.length(); i++) {
			id = perguntas.getJSONObject(i).getInt("id") + "";
			if (perguntas.getJSONObject(i).getString("type").equals("section") && !perguntas.getJSONObject(i).getString("visibility").equals("hidden")) {
				perguntasRespostas.put(id + "", String.format("{\"type\":\"%s\",\"pergunta\":\"%s\",\"resposta\":\"\"}", 
					perguntas.getJSONObject(i).getString("type"), perguntas.getJSONObject(i).getString("label")));
			} else if (respostas.has(id) && StringUtils.isNotEmpty(respostas.getString(id))) {
				perguntasRespostas.put(id, String.format("{\"type\":\"%s\",\"pergunta\":\"%s\",\"resposta\":\"%s\"}", 
					perguntas.getJSONObject(i).getString("type"), perguntas.getJSONObject(i).getString("label"), respostas.getString(id)));
			}
		}
		String html = this.gerarHtmlFormulario(perguntasRespostas);
		this.protocolarDocumentoBean.getDocumentoPrincipal().getProcessoDocumentoBin().setModeloDocumento(html);
		this.protocolarDocumentoBean.gravarRascunho();
	}
	
	public void abrirFormulario() {
		this.protocolarDocumentoBean.getDocumentoPrincipal().getProcessoDocumentoBin().setModeloDocumento(null);
	}
	
	public String gerarHtmlFormulario(Map<String, String> perguntasRespostas) {
		StringBuilder result = new StringBuilder();
		
		for (String value : perguntasRespostas.values()) {
			result.append(this.format(new JSONObject(value)));
		}
		
		return result.toString();
	}
	
	private String format(JSONObject json) {
		String result;
		if (json.getString("type").equals("section")) {
			result = String.format("<br /><p style='font-size: 12pt;'><b>%s</b></p><hr>", json.getString("pergunta"));
		} else {
			result = String.format("<p><b>%s</b><br />%s</p>", json.getString("pergunta"), json.getString("resposta"));
		}
		return result;
	}
	
	
	public String getSource() {
		return (String)this.tramitacaoProcessualService.recuperaVariavelTarefa("pje:flx:paginaExterna");
	}

	public String getPerguntasJSON() {
		return perguntasJSON;
	}

	public void setPerguntasJSON(String perguntasJSON) {
		this.perguntasJSON = perguntasJSON;
	}

	public String getRespostasJSON() {
		return respostasJSON;
	}

	public void setRespostasJSON(String respostasJSON) {
		this.respostasJSON = respostasJSON;
	}
	
	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}
	
	public void setProtocolarDocumentoBean(ProtocolarDocumentoBean protocolarDocumentoBean) {
		this.protocolarDocumentoBean = protocolarDocumentoBean;
	}
	
	public void concluirInclusaoPeticaoDocumento() {
		this.protocolarDocumentoBean.concluirAssinatura();
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
	}

	@Override
	public String getActionName() {
		return NAME;
	}
	
}
