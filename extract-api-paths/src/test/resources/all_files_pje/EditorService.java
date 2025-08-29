package br.com.infox.editor.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.editor.exception.EditorServiceException;
import br.com.infox.editor.interpretadorDocumento.LinguagemFormalException;
import br.com.infox.editor.manager.ProcessoDocumentoEstruturadoManager;
import br.com.infox.editor.manager.ProcessoDocumentoEstruturadoTopicoManager;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.manager.DocumentoVotoManager;
import br.com.jt.pje.manager.VotoManager;
import br.jus.pje.jt.entidades.DocumentoVoto;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumento;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumentoTopico;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;
import br.jus.pje.nucleo.entidades.editor.Topico;
import br.jus.pje.nucleo.entidades.editor.topico.ITopicoComConclusao;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoConsideracoes;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoDispositivoAcordao;

@Name(EditorService.NAME)
@AutoCreate
public class EditorService {

	public static final String NAME = "editorService";
	
	@In
	private ProcessoDocumentoEstruturadoManager processoDocumentoEstruturadoManager;
	@In
	private ProcessoDocumentoEstruturadoTopicoManager processoDocumentoEstruturadoTopicoManager;
	@In(value = XmlProcessoDocumentoEstruturadoService.NAME)
	private XmlProcessoDocumentoEstruturadoService xmlDocumentoService;
	@In
	private ProcessaModeloService processaModeloService;
	@In
	private VotoManager votoManager;
	@In
	private DocumentoVotoManager documentoVotoManager;
	@Logger
	private Log log;

	/*
	 * PJE-JT: Ricardo Scholz : PJEII-8220 - 2013-05-23
	 * Carregamento dos tipos de documento, para evitar repetidos acessos à
	 * base de dados.
	 */
	private static final int TIPO_PROCESSO_DOCUMENTO_EMENTA = ParametroUtil.instance()
			.getTipoProcessoDocumentoEmenta().getIdTipoProcessoDocumento();
	private static final int TIPO_PROCESSO_DOCUMENTO_RELATORIO =
			ParametroUtil.instance().getTipoProcessoDocumentoRelatorio().getIdTipoProcessoDocumento();
	private static final int TIPO_PROCESSO_DOCUMENTO_FUNDAMENTACAO =
			ParametroUtil.instance().getTipoProcessoDocumentoFundamentacao().getIdTipoProcessoDocumento();
	private static final int TIPO_PROCESSO_DOCUMENTO_DISPOSITIVO =
			ParametroUtil.instance().getTipoProcessoDocumentoDispositivo().getIdTipoProcessoDocumento();
	private static final int TIPO_PROCESSO_DOCUMENTO_ACORDAO = ParametroUtil.instance()
			.getTipoProcessoDocumentoAcordao().getIdTipoProcessoDocumento();
	/*
	 * PJE-JT: Fim.
	 */
	
	public ProcessoDocumentoEstruturado criarProcessoDocumentoEstruturado(EstruturaDocumento estruturaDocumento, ProcessoTrf processoTrf, TipoProcessoDocumento tipoProcessoDocumento, PessoaMagistrado magistrado) throws LinguagemFormalException {
		ProcessoDocumentoEstruturado pdEstruturado = new ProcessoDocumentoEstruturado();
		pdEstruturado.setEstruturaDocumento(estruturaDocumento);
		pdEstruturado.setMagistrado(magistrado);
		pdEstruturado.setProcessoDocumentoEstruturadoTopicoList(criarProcessoDocumentoEstruturadoTopicoList(pdEstruturado));
		pdEstruturado.setProcessoTrf(processoTrf);
		pdEstruturado.atualizaProcessoDocumento();
		pdEstruturado.getProcessoDocumento().setProcesso(processoTrf.getProcesso());
		pdEstruturado.getProcessoDocumento().setTipoProcessoDocumento(tipoProcessoDocumento);
		return pdEstruturado;
	}

	/**
	 * Metodo chamado para gravar o documento, ele verifica se o documendo já
	 * foi persistido para saber qual metodo deve chamar: -
	 * {@link #persistirDocumentoEstruturado(ProcessoDocumentoEstruturado)} -
	 * {@link #atualizarDocumentoEstruturado(ProcessoDocumentoEstruturado)}
	 *
	 * @param docEstruturado
	 * @throws EditorServiceException
	 *             Se o documento não for válido
	 */
	public void gravarDocumentoEstruturado(ProcessoDocumentoEstruturado docEstruturado) throws EditorServiceException {
		processoDocumentoEstruturadoManager.gravarDocumentoEstruturado(docEstruturado);
	}
	
	private List<ProcessoDocumentoEstruturadoTopico> criarProcessoDocumentoEstruturadoTopicoList(ProcessoDocumentoEstruturado documentoEstruturado) throws LinguagemFormalException {
		List<ProcessoDocumentoEstruturadoTopico> topicoList = new ArrayList<ProcessoDocumentoEstruturadoTopico>();
		for (EstruturaDocumentoTopico modeloTopico : documentoEstruturado.getEstruturaDocumento().getEstruturaDocumentoTopicoList()) {
			ProcessoDocumentoEstruturadoTopico processoTopico = processoDocumentoEstruturadoTopicoManager.criarProcessoDocumentoEstruturadoTopico(modeloTopico, documentoEstruturado);
			processoTopico.setTopico((Topico)EntityUtil.removeProxy(processoTopico.getTopico()));
			topicoList.add(processoTopico);
			criarProcessoDocumentoEstruturadoTopicoListRecursivo(topicoList, processoTopico);
		}
		return topicoList;
	}
	
	private void criarProcessoDocumentoEstruturadoTopicoListRecursivo(List<ProcessoDocumentoEstruturadoTopico> topicoList, ProcessoDocumentoEstruturadoTopico processoTopico) throws LinguagemFormalException{
		if (processoTopico.getTopico().getItemTopico() != null && !(processoTopico.getTopico() instanceof TopicoConsideracoes)) {
			ProcessoDocumentoEstruturadoTopico processoDocumentoEstruturadoTopico = processoDocumentoEstruturadoTopicoManager.criarProcessoDocumentoEstruturadoItemTopico(processoTopico);
			topicoList.add(processoDocumentoEstruturadoTopico);
			if(processoDocumentoEstruturadoTopico.getTopico().getItemTopico() != null){
				criarProcessoDocumentoEstruturadoTopicoListRecursivo(topicoList, processoDocumentoEstruturadoTopico);
			}
		}
		if(processoTopico.getTopico() instanceof ITopicoComConclusao){
			topicoList.add(processoDocumentoEstruturadoTopicoManager.criarProcessoDocumentoEstruturadoConclusaoTopico(processoTopico));
		}
		if(processoTopico.getTopico() instanceof TopicoDispositivoAcordao){
			TopicoDispositivoAcordao topicoDispositivoAcordao = (TopicoDispositivoAcordao)processoTopico.getTopico();
			topicoList.add(processoDocumentoEstruturadoTopicoManager.criarProcessoDocumentoEstruturadoItemTopico(processoTopico,topicoDispositivoAcordao.getDispositivoSessao()));
			topicoList.add(processoDocumentoEstruturadoTopicoManager.criarProcessoDocumentoEstruturadoItemTopico(processoTopico,topicoDispositivoAcordao.getDispositivoVoto()));
		}
	}

	public String getHtmlEditor(ProcessoDocumentoEstruturado documento, boolean exibirAnotacoes) {
		try {
			xmlDocumentoService.setExibirAnotacoes(exibirAnotacoes);
			String xml = xmlDocumentoService.criarXmlDocumento(documento);
			String modelo = processaModeloService.processaModelo(xml, documento.getEstruturaDocumento().getXslDocumento().getConteudo());
			return modelo;
		} catch (LinguagemFormalException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getHtmlEditor(ProcessoDocumentoEstruturado documento, boolean exibirAnotacoes, List<ProcessoDocumentoEstruturadoTopico> whiteListTopicos) {
		try {
			xmlDocumentoService.setExibirAnotacoes(exibirAnotacoes);
			String xml = xmlDocumentoService.criarXmlDocumento(documento, whiteListTopicos);
			String modelo = processaModeloService.processaModelo(xml, documento.getEstruturaDocumento().getXslDocumento().getConteudo());
			return modelo;
		} catch (LinguagemFormalException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getHtmlEditor(ProcessoDocumentoEstruturado documento) {
		return getHtmlEditor(documento, false);
	}
	
	public String getHtmlEditor(Integer idProcessoDocumentoEstruturado) {
		ProcessoDocumentoEstruturado processoDocumentoEstruturado = EntityUtil.getEntityManager().find(ProcessoDocumentoEstruturado.class, idProcessoDocumentoEstruturado);
		return getHtmlEditor(processoDocumentoEstruturado);
	}
	
	public String getHtmlEditor(Integer idProcessoDocumentoEstruturado, boolean exibirAnotacoes) {
		ProcessoDocumentoEstruturado processoDocumentoEstruturado = EntityUtil.getEntityManager().find(ProcessoDocumentoEstruturado.class, idProcessoDocumentoEstruturado);
		return getHtmlEditor(processoDocumentoEstruturado, exibirAnotacoes);
	}
	
	public List<String> getVotoAntigosRelator (ProcessoTrf processoTrf){
		List<String> textoTopicos = new ArrayList<String>(); 
		List<Voto> votos = votoManager.getVotosByProcesso(processoTrf);
		OrgaoJulgador relator = processoTrf.getOrgaoJulgador();
		Voto votoDoDocumento = null;
		List<DocumentoVoto> documentosVoto;
		int idVotoMaior = 0;
		boolean encontrou = false;
		String textoEmenta = "";
		String textoRelatorio = "";
		String textoFundamentacao = "";
		String textoDispositivo = "";
		String textoAlert = "";
		
		//verificar qual é o voto com maior id para o relator (voto mais recente)
		for(Voto voto:votos){
			if(voto.getOrgaoJulgador().equals(relator)){
				if(voto.getIdVoto() > idVotoMaior) {
					idVotoMaior = voto.getIdVoto();
					votoDoDocumento = voto;
					encontrou = true;
					//log.info("getVotoAntigosRelator");
				}
				
			}
		}
		if(!encontrou) {
			log.error("Importação: Voto não encontrado para o processo do documento em edição");
			textoAlert = "Importação não pode ser realizada porque não foi encontrado documento não estruturado para este processo";
			return null;
		}
		
		//Busca os documentos correspondentes ao voto
		documentosVoto = documentoVotoManager.getDocumentoVotoByVoto(votoDoDocumento);
		
		//Para cada documento, procurar o processoDocumento correspondente
		for(DocumentoVoto documentoVoto : documentosVoto) {
			/*
			 * PJE-JT: Ricardo Scholz : PJEII-8220 - 2013-05-23
			 * Troca da estrutura 'switch' pela estrutura 'if/else', utilização
			 * das constantes de tipo de documento, visando maior robustez. 
			 * Substituição dos textos carregados por Strings vazias, quando 
			 * fossem nulos, de forma a evitar erros no 'editorview.js'. 
			 */
			int tipoProcessoDocumento = documentoVoto.getTipoProcessoDocumento().getIdTipoProcessoDocumento();
			if(tipoProcessoDocumento == TIPO_PROCESSO_DOCUMENTO_EMENTA) {//ementa
				textoEmenta = documentoVoto.getProcessoDocumentoBin().getDocumentoOriginal();
				if(textoEmenta==null) {textoEmenta = "";}
				log.info("getVotoAntigosRelator: ementa: " + textoEmenta);
			} else if (tipoProcessoDocumento == TIPO_PROCESSO_DOCUMENTO_RELATORIO) {//relatorio
				textoRelatorio = documentoVoto.getProcessoDocumentoBin().getDocumentoOriginal();
				if(textoRelatorio==null) {textoRelatorio = "";}
				log.info("getVotoAntigosRelator: relatorio: " + textoRelatorio);
			} else if (tipoProcessoDocumento == TIPO_PROCESSO_DOCUMENTO_FUNDAMENTACAO) {//fundamentacao
				textoFundamentacao = documentoVoto.getProcessoDocumentoBin().getDocumentoOriginal();
				if(textoFundamentacao==null) {textoFundamentacao = "";}
				log.info("getVotoAntigosRelator: fundamentacao: " + textoFundamentacao);
			} else if (tipoProcessoDocumento == TIPO_PROCESSO_DOCUMENTO_DISPOSITIVO) {//dispositivo
				textoDispositivo = documentoVoto.getProcessoDocumentoBin().getDocumentoOriginal();
				if(textoDispositivo==null) {textoDispositivo = "";}
				log.info("getVotoAntigosRelator: dispositivo: " + textoDispositivo);
			} else if (tipoProcessoDocumento == TIPO_PROCESSO_DOCUMENTO_ACORDAO) {//acordao assinado
				log.info("Importacao: ultimo documento é um acordao assinado. Importacao abortada ");
				textoAlert = "Importação não pode ser realizada porque não existe documento em edição após assinatura do acórdão.";
			} else {
				log.info("getVotoAntigosRelator: tipo de tópico não esperado: " + documentoVoto.getTipoProcessoDocumento().getIdTipoProcessoDocumento());
			}
			/*
			 * PJE-JT: Fim.
			 */
		}
		
		//acrescenta os topicos encontrados em ordem fixa (ementa, relatorio, fundamentacao e dispositivo) 
		textoTopicos.add(textoEmenta);
		textoTopicos.add(textoRelatorio);
		textoTopicos.add(textoFundamentacao);
		textoTopicos.add(textoDispositivo);
		textoTopicos.add(textoAlert); 
		
		return textoTopicos;
		
	}
	
	public List<Map<String, Object>> getVotosAntigosRevisores (ProcessoTrf processoTrf){
		List<Voto> votos = votoManager.getVotosByProcesso(processoTrf);
		OrgaoJulgador relator = processoTrf.getOrgaoJulgador();
		Voto votoDoDocumento = null;
		int idVotoMaior = 0;
		boolean encontrou = false;
		String textoEmenta = "";
		String textoRelatorio = "";
		String textoFundamentacao = "";
		String textoDispositivo = "";
		String textoAlert = "";
		List<Map<String, Object>> retornoVotosRevisores = new ArrayList<Map<String, Object>>(); 
		
		//verificar qual é o voto com maior id para o relator (voto mais recente)
		for(Voto voto:votos){
			if(voto.getOrgaoJulgador().equals(relator)){
				if(voto.getIdVoto() > idVotoMaior) {
					idVotoMaior = voto.getIdVoto();
					votoDoDocumento = voto;
					encontrou = true; 
					break;
					//log.info("getVotosAntigosRevisores");
				}
				
			}
		}
		if(!encontrou) {
			log.error("getVotosAntigosRevisores: Voto não encontrado para o processo do documento em edição");
			textoAlert = "Importação não pode ser realizada porque não foi encontrado documento não estruturado para este processo";
			return null;
		}
		
		//Busca os votos dos revisores da sessao de julgamento do processo em analise
		List<Voto> votosRevisores = votoManager.getVotosByProcessoSessao(processoTrf, votoDoDocumento.getSessao());
		
		if (votosRevisores != null) {
			for(Voto votoRevisor: votosRevisores) {
				textoEmenta = "";
				textoRelatorio = "";
				textoFundamentacao = "";
				textoDispositivo = "";
				textoAlert = "";
				Map<String, Object> retornoVotoRevisor = new HashMap<String, Object>();
				
				if(!votoRevisor.getOrgaoJulgador().equals(relator)) { //excluir relator
					//retorna os documentos de cada revisor
					List<DocumentoVoto> documentosVotoRevisor = documentoVotoManager.getDocumentoVotoByVoto(votoRevisor);
					
					String textoVotoRevisor = "";
					
					//Para cada documento, procurar o processoDocumento correspondente - texto do voto
					for(DocumentoVoto documentoVoto : documentosVotoRevisor) {
						/*
						 * PJE-JT: Ricardo Scholz : PJEII-8220 - 2013-05-23
						 * Troca da estrutura 'switch' pela estrutura 'if/else', utilização
						 * das constantes de tipo de documento, visando maior robustez. 
						 * Substituição dos textos carregados por Strings vazias, quando 
						 * fossem nulos, de forma a evitar erros no 'editorview.js'. 
						 */
						int tipoProcessoDocumento = documentoVoto.getTipoProcessoDocumento().getIdTipoProcessoDocumento();
						
						if(tipoProcessoDocumento == TIPO_PROCESSO_DOCUMENTO_EMENTA) {//ementa
							textoEmenta = documentoVoto.getProcessoDocumentoBin().getDocumentoOriginal();
							if(textoEmenta == null) {textoEmenta = "";}
						} else if (tipoProcessoDocumento == TIPO_PROCESSO_DOCUMENTO_RELATORIO) {//relatorio
							textoRelatorio = documentoVoto.getProcessoDocumentoBin().getDocumentoOriginal();
							if(textoRelatorio == null) {textoRelatorio = "";}
						} else if (tipoProcessoDocumento == TIPO_PROCESSO_DOCUMENTO_FUNDAMENTACAO) {//fundamentacao
							textoFundamentacao = documentoVoto.getProcessoDocumentoBin().getDocumentoOriginal();
							if(textoFundamentacao == null) {textoFundamentacao = "";}
						} else if (tipoProcessoDocumento == TIPO_PROCESSO_DOCUMENTO_DISPOSITIVO) {//dispositivo
							textoDispositivo = documentoVoto.getProcessoDocumentoBin().getDocumentoOriginal();
							if(textoDispositivo == null) {textoDispositivo = "";}
						} else {
							log.info("getVotosAntigosRevisores: tipo de tópico revisor não esperado: " + documentoVoto.getTipoProcessoDocumento().getIdTipoProcessoDocumento());
						}
						/*
						 * PJE-JT: Fim.
						 */
					}
					
					//informacoes de voto para cada revisor
					textoVotoRevisor = textoEmenta + textoRelatorio + textoFundamentacao + textoDispositivo;
					retornoVotoRevisor.put("voto", textoVotoRevisor);
					retornoVotoRevisor.put("revisor", votoRevisor.getOrgaoJulgador().getOrgaoJulgador());
					
					retornoVotosRevisores.add(retornoVotoRevisor);
					
				}
			}
		}
		
		int i=1;
		for(Map<String, Object> testeRetorno : retornoVotosRevisores) {
			log.info("getVotosAntigosRevisores: Voto Revisor " + i +" : " + testeRetorno.get("voto"));
			log.info("getVotosAntigosRevisores: Revisor " + i++ +": " + testeRetorno.get("revisor"));
		}
		
		return retornoVotosRevisores;
		
	}

}
