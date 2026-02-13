/**
 * UploadProcessoDocumentoResource.java.
 *
 * Data de criação: 11/06/2014
 */
package br.jus.cnj.pje.view.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.jboss.seam.web.AbstractResource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.richfaces.component.html.HtmlFileUpload;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.FileUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.servicos.MimeUtilChecker;
import br.jus.cnj.pje.view.MultipleFileUploadAction;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;

/**
 * Classe responsável em receber os arquivos enviados via upload. Os arquivos
 * são recebidos via stream e disponibilizados em uma variável ne conversação
 * para que um managedbean resolva o que fazer com os arquivos enviados.
 * 
 * @author adriano.pamplona
 */
@Scope(ScopeType.APPLICATION)
@Name("uploadProcessoDocumentoResource")
@BypassInterceptors
public class UploadProcessoDocumentoResource extends AbstractResource implements Serializable {

	/**
	 * Nome da variável de contexto que será usada para armazenar a coleção de
	 * arquivos anexos. A variável poderá ser recuperada no XHTML com a linha de
	 * código contexts.conversationContext.get('anexos').
	 * 
	 */
	private static String VARIAVEL_UPLOAD_EVENT = "uploadEvent";

	/**
	 * @see org.jboss.seam.web.AbstractResource#getResource(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void getResource(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		new ContextualHttpServletRequest(request) {
			@Override
			public void process() throws IOException, JSONException {
				executar(request, response);
			}
		}.run();
	}

	@Override
	public String getResourcePath() {
		return "/upload";
	}

	/**
	 * Resolve a requisição do resource. Os arquivos de upload são recuperados e
	 * armazenados em uma variável do seam para que sejam tratados pelos
	 * managedbean's.
	 * 
	 * Os arquivos são recuperados via streamming conforme documentação abaixo.
	 * http://commons.apache.org/proper/commons-fileupload/streaming.html
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @throws IOException
	 * @throws JSONException 
	 */
	protected void executar(HttpServletRequest request, HttpServletResponse response)
			throws IOException, JSONException {

		JSONObject resultado = new JSONObject();
		response.setStatus(HttpServletResponse.SC_OK);
		if (ServletFileUpload.isMultipartContent(request)) {

			try {
				Collection<UploadItem> colecaoUploadItem = new ArrayList<UploadItem>();

				ServletFileUpload upload = new ServletFileUpload();
				upload.setHeaderEncoding("UTF-8");

				FileItemIterator anexos = upload.getItemIterator(request);

				JSONArray jsonArquivos = new JSONArray();
				while (anexos.hasNext()) {
					FileItemStream arquivo = anexos.next();
					if (!arquivo.isFormField()) {
						JSONObject jsonArquivo = processarFileItemStream(colecaoUploadItem, arquivo);
						jsonArquivos.put(jsonArquivo);
					}
				}

				UploadEvent evento = new UploadEvent(new HtmlFileUpload(),
						(List<UploadItem>) colecaoUploadItem);
				atribuirAnexosNaConversacao(evento);

				resultado = novoRequisicaoJSONObject(jsonArquivos, null);
			} catch (Exception e) {
				resultado = novoRequisicaoJSONObject(null, e.getMessage());
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} finally {
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(resultado.toString());
			}
		}
	}

	/**
	 * Processa o stream do arquivo submetido.
	 * 
	 * @param colecaoArquivo
	 *            Coleção de arquivos que serão adicionados no UploadEvent.
	 * @param arquivo
	 *            Stream do arquivo submetido.
	 * @return Resposta json do processamento.
	 * @throws JSONException 
	 */
	protected JSONObject processarFileItemStream(Collection<UploadItem> colecaoArquivo,
			FileItemStream arquivo) throws JSONException {

		JSONObject resultado = null;

		try (InputStream stream = arquivo.openStream()){

			if (!arquivo.isFormField()) {
				String nome = arquivo.getName();
				String contentType = arquivo.getContentType();
				File file = FileUtil.writeTempFile(stream);

				UploadItem upload = new UploadItem(nome, (int) file.length(), contentType, file);
				novoMultipleFileUploadAction().processItem(upload);

				colecaoArquivo.add(upload);

				resultado = novoArquivoJSONObject(nome, null);
			}
		} catch (PJeBusinessException e) {
			resultado = novoArquivoJSONObject(arquivo.getName(), e.getLocalizedMessage());
		} catch (Exception e) {
			resultado = novoArquivoJSONObject(arquivo.getName(), e.getMessage());
		}

		return resultado;
	}

	/**
	 * Retorna um objeto json resultado da requisição do envio do arquivo.
	 * 
	 * @param nome
	 *            Nome do arquivo.
	 * @param erro
	 *            Mensagem de erro.
	 * @return resposta do upload do arquivo.
	 * @throws JSONException 
	 */
	protected JSONObject novoArquivoJSONObject(String nome, String erro) throws JSONException {
		JSONObject resultado = new JSONObject();

		String mensagem = "Erro ao fazer o upload do arquivo %s. Erro: %s";

		resultado.put("upload", (erro == null));
		resultado.put("nome", nome);
		resultado.put("mensagem", (erro != null ? String.format(mensagem, nome, erro) : null));

		return resultado;
	}

	/**
	 * Retorna um objeto json resultado da requisição.
	 * 
	 * @param arquivos
	 *            Lista de arquivos submetido.
	 * @param erro
	 *            Mensagem de erro.
	 * @return resposta da requisição.
	 * @throws JSONException 
	 */
	protected JSONObject novoRequisicaoJSONObject(JSONArray arquivos, String erro) throws JSONException {
		JSONObject resultado = new JSONObject();

		resultado.put("requisicao", (erro == null));
		resultado.put("arquivos", arquivos);
		resultado.put("mensagem", erro);

		return resultado;
	}

	/**
	 * Atribui os anexos na variável de conversação VARIAVEL_UPLOAD_EVENT.
	 * 
	 * @param evento
	 *            UploadEvent com os arquivos agregados.
	 * @throws PJeException
	 */
	protected void atribuirAnexosNaConversacao(UploadEvent evento) throws PJeException {

		if (Contexts.isConversationContextActive()) {
			Contexts.getConversationContext().set(VARIAVEL_UPLOAD_EVENT, evento);
		} else {
			throw new PJeException(
					"Conversação não encontrada, verifique se o parâmetro 'cid' foi passado na requisição.");
		}
		if (ParametroUtil.instance().isEditorLibreOfficeHabilitado() && ProcessoTrfHome.instance().getInstance().getProcesso() != null) {
			ProtocolarDocumentoBean protocalarDocumento = new ProtocolarDocumentoBean(
					ProcessoTrfHome.instance().getInstance().getProcesso().getIdProcesso());

			protocalarDocumento.gravar(evento);
		}
	}

	/**
	 * @return novo MultipleFileUploadAction.
	 */
	protected MultipleFileUploadAction novoMultipleFileUploadAction() {
		MimeUtilChecker mimeChecker = (MimeUtilChecker) Component.getInstance(
				MimeUtilChecker.class, ScopeType.APPLICATION);
		return new MultipleFileUploadAction(mimeChecker);
	}

}
