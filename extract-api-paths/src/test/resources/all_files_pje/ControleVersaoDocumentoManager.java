package br.jus.cnj.pje.nucleo.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.jus.cnj.certificado.SigningUtilities;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.ControleVersaoDocumentoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.je.pje.business.dto.ControleVersaoDocumentoDTO;
import br.jus.je.pje.business.dto.ControleVersoesDocumentoDTO;
import br.jus.je.pje.business.dto.RespostaDTO;
import br.jus.pje.jt.entidades.ControleVersaoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.util.Crypto;
import br.jus.pje.nucleo.util.StringUtil;

import com.google.gson.Gson;

@Name(ControleVersaoDocumentoManager.NAME)
public class ControleVersaoDocumentoManager extends BaseManager<ControleVersaoDocumento> {
	public final static String NAME = "controleVersaoDocumentoManager";

	@Logger
	private Log logger;
	
	@In
	private ControleVersaoDocumentoDAO controleVersaoDocumentoDAO;

	@In
	private ProcessoDocumentoManager processoDocumentoManager;

	@In
	private transient ParametroUtil parametroUtil;

	/**
	 * Salva uma versão do documento
	 * @param String com o conteúdo do documento
	 */
	public void salvarVersaoDocumento(ProcessoDocumento processoDocumento) throws PJeBusinessException {
		if(processoDocumento == null || processoDocumento.getProcessoDocumentoBin() == null || StringUtil.isEmpty(processoDocumento.getProcessoDocumentoBin().getModeloDocumento())){
			return;
		}
		
		UsuarioService usuarioService = (UsuarioService) Component.getInstance("usuarioService", ScopeType.EVENT);
		
		ControleVersaoDocumento controleVersaoDocumento = new ControleVersaoDocumento();
		controleVersaoDocumento.setAtivo(true);
		controleVersaoDocumento.setDataModificacao(new Date());
		controleVersaoDocumento.setProcessoDocumentoBin(processoDocumento.getProcessoDocumentoBin());
		controleVersaoDocumento.setUsuario(usuarioService.getUsuarioLogado());
		if(usuarioService != null && usuarioService.getLocalizacaoAtual() != null) {
			controleVersaoDocumento.setLocalizacaoAtual(usuarioService.getLocalizacaoAtual().getLocalizacaoFisica().getLocalizacao());
		}
		
		controleVersaoDocumento.setSha1Conteudo(Crypto.encodeSHA256(processoDocumento.getProcessoDocumentoBin().getModeloDocumento()));
		controleVersaoDocumento.setConteudo(processoDocumento.getProcessoDocumentoBin().getModeloDocumento());
		gravarControleVersaoDocumento(controleVersaoDocumento);
	}
	
	/**
	 * Persiste chamando objeto controle de versão documento
	 * @return boolean true ou false dependendo do sucesso na gravação
	 */
	public boolean gravarControleVersaoDocumento(ControleVersaoDocumento controleVersaoDocumento) {
		boolean gravou = Boolean.FALSE;
		
		if(!documentoModificado(controleVersaoDocumento)) {
			logger.error("Documento não foi modificado e por isso não será gravado");
			return gravou;
		}

		
		if(controleVersaoDocumento.getProcessoDocumentoBin() != null){
			if(controleVersaoDocumento.getVersao() == 0){
				controleVersaoDocumento.setDataModificacao(new Date());
			} 

			mergeAndFlush(controleVersaoDocumento);

			gravou = Boolean.TRUE;
			logger.info("Sucesso na gravação de uma versão de um documento");
		}

		if(ultrapassouQuantidadeVersoes(controleVersaoDocumento)) {
			controleVersaoDocumentoDAO.removePrimeiroDocumentoVersionado(controleVersaoDocumento.getProcessoDocumentoBin().getIdProcessoDocumentoBin());
			renumeraVersoesControleVersaoDocumento(controleVersaoDocumento.getProcessoDocumentoBin().getIdProcessoDocumentoBin());
		}
		

		return gravou;
	}

	private void renumeraVersoesControleVersaoDocumento(int idProcessoDocumentoBin) {
		List<ControleVersaoDocumento> list = controleVersaoDocumentoDAO.obterTodasVersoesPorIdDocumentoOrdemCrescenteDataModificao(idProcessoDocumentoBin);
		int versao = 1;
		for(ControleVersaoDocumento controleVersaoDocumento : list) {
			controleVersaoDocumento.setVersao(versao);
			versao ++ ;
			mergeAndFlush(controleVersaoDocumento);
		}
	}

	/**
	 * Consulta das versões de um documento em formato json
	 * @param processoDocumentoBin
	 * @return {@link String} representando {@link JSONObject} com as informações de resposta.
	 * @throws PJeBusinessException 
	 */
	public String obterVersoesDocumentoJSON(ProcessoDocumentoBin processoDocumentoBin) throws PJeBusinessException {
		List<ControleVersaoDocumentoDTO> listControleVersaoDocumentoDTO = obterVersoesDocumentoDTO(processoDocumentoBin);
		return obterVersoesDocumentoJSON(listControleVersaoDocumentoDTO);
	}

	/**
	 * Consulta das versões de um documento
	 * O objetivo deste método é evitar sobrecarga quando houver json com muitas informações. A idéia é paginar estas informações
	 * @param processoDocumentoBin
	 * @param versaoInicial - indica a versão que iniciou a pesquisa
	 * @param quantidadeProximasVersoes - indica as próximas versões que serão pesquisadas
	 * @return {@link String} representando {@link JSONObject} com as informações de resposta.
	 * @throws PJeBusinessException 
	 */
	public String obterVersoesDocumentoJSONPaginada(ProcessoDocumentoBin processoDocumentoBin, int limit, int offset) throws PJeBusinessException {
		List<ControleVersaoDocumentoDTO> listControleVersaoDocumentoDTO = obterVersoesDocumentoDTO(processoDocumentoBin, limit, offset);
		return obterVersoesDocumentoJSON(listControleVersaoDocumentoDTO);
	}

	/**
	 * Recupera uma versão do documento
	 * 
	 * @param ControleVersaoDocumento
	 * @return ControleVersaoDocumento
	 */
	public ControleVersaoDocumento obterControleVersaoDocumento(ControleVersaoDocumento controleVersaoDocumento){
		return controleVersaoDocumentoDAO.find(controleVersaoDocumento.getIdControleVersaoDocumento());
	}

	/**
	 * Deleta todas as versões de um documento.
	 * 
	 * @param idProcessoDocumentoBin
	 */
	public void deletarTodasVersoesIdDocumento(Integer idProcessoDocumentoBin) {
		logger.info("Remove as versões do documento na data de juntada");
		controleVersaoDocumentoDAO.deletarTodasVersoesIdDocumento(idProcessoDocumentoBin);
	}

	/**
	 * Deleta uma versão de um documento
	 * 
	 * @param ControleVersaoDocumento
	 */
	public void deletarControleVersaoDocumento(ControleVersaoDocumento controleVersaoDocumento) {
		controleVersaoDocumentoDAO.remove(controleVersaoDocumento);
	}

	/**
	 * Verifica se o documento foi modificado
	 * 
	 * @param ControleVersaoDocumento
	 * @return boolean
	 */
	private boolean documentoModificado(ControleVersaoDocumento controleVersaoDocumento) {
		ControleVersaoDocumento ultimoControleVersaoDocumento = obtemUltimoDocumentoVersionado(controleVersaoDocumento);
		
		if(ultimoControleVersaoDocumento==null) {
			controleVersaoDocumento.setVersao(1);
			return true; 
		} else if(ultimoControleVersaoDocumento.getSha1Conteudo().equals(controleVersaoDocumento.getSha1Conteudo())) {
			return false;
		} else {
			controleVersaoDocumento.setVersao(ultimoControleVersaoDocumento.getVersao()+1);
			return true;
		}
	}

	/**
	 * Obtém último documento versionado de determinado processo documento
	 * 
	 * @param ControleVersaoDocumento
	 * @return ControleVersaoDocumento
	 */
	private ControleVersaoDocumento obtemUltimoDocumentoVersionado(ControleVersaoDocumento controleVersaoDocumento) {
		return controleVersaoDocumentoDAO.obtemUltimoDocumentoVersionado(controleVersaoDocumento);
	}
	
	/**
	 * Retorna quantidade de versões de documento
	 * 
	 * @param ControleVersaoDocumento
	 * @return boolean
	 */
	private boolean ultrapassouQuantidadeVersoes(ControleVersaoDocumento controleVersaoDocumento) {
		int quantidadeVersoes = obterTodasVersoesPorIdDocumento(controleVersaoDocumento.getProcessoDocumentoBin().getIdProcessoDocumentoBin()).size();
		int quantidadeMaximaVersoesDocumento = parametroUtil.getQuantidadeMaximaVersoesDocumento();

		if(quantidadeMaximaVersoesDocumento<0) {
			return false; // indica que não há limite para versões a serem persistidas. Para definir limite deverá ser cadastrado o parâmetro Parametros.QUANTIDADE_MAXIMA_VERSOES_DOCUMENTO
		}
			
		if(quantidadeVersoes>quantidadeMaximaVersoesDocumento) {
			return true;
		} 
		
		return false;
	}

	/**
	 * Obtém todas as versões de um documento.
	 * 
	 * @param idDocumento
	 * @return List<ControleVersaoDocumento>
	 */
	private List<ControleVersaoDocumento> obterTodasVersoesPorIdDocumento(Integer idDocumento){
		return controleVersaoDocumentoDAO.obterTodasVersoesPorIdDocumento(idDocumento);
	}

	/**
	 * Obtém uma lista de versões do documento a partir do idDocumento da tb_processo_documento_bin
	 *  
	 * @Param Integer idDocumento (id br.jus.pje.nucleo.entidades.ProcessoDocumentoBin.idProcessoDocumentoBin)
	 * 
	 * @return List<ControleVersaoDocumento>
	 */
	private List<ControleVersaoDocumento> obterVersoesDocumento(ProcessoDocumentoBin processoDocumentoBin) throws PJeBusinessException {
		return obterTodasVersoesPorIdDocumento(processoDocumentoBin.getIdProcessoDocumentoBin());
	}
	
	/**
	 * Obtém um json com base na List<ControleVersaoDocumentoDTO> 
	 *  
	 * @Param List<ControleVersaoDocumentoDTO>
	 * @return string contendo o json  
	 */
	private String obterVersoesDocumentoJSON(List<ControleVersaoDocumentoDTO> listControleVersaoDocumentoDTO) {
		RespostaDTO respostaControleVersaoDocumentoDTO = new RespostaDTO();
		
		try {
			respostaControleVersaoDocumentoDTO.setSucesso(Boolean.TRUE);
			
			ControleVersoesDocumentoDTO versoes = new ControleVersoesDocumentoDTO();
			versoes.setVersoes(listControleVersaoDocumentoDTO);
			
			respostaControleVersaoDocumentoDTO.setResposta(versoes);
		} catch (Exception e) {
			e.printStackTrace();
			respostaControleVersaoDocumentoDTO.setSucesso(Boolean.FALSE);
			respostaControleVersaoDocumentoDTO.setMensagem(e.getLocalizedMessage());
		}

		String strRetornoControleVersaoDocumentoJSON = new Gson().toJson(respostaControleVersaoDocumentoDTO, RespostaDTO.class);
		
		return strRetornoControleVersaoDocumentoJSON;
	}
	
	private List<ControleVersaoDocumentoDTO> obterVersoesDocumentoDTO(ProcessoDocumentoBin processoDocumentoBin, int limit, int offset) throws PJeBusinessException {
		return mapperVersoesDocumentoDTO(controleVersaoDocumentoDAO.obterVersoesPorIdDocumentoPaginada(processoDocumentoBin.getIdProcessoDocumentoBin(), limit, offset));
	}

	private List<ControleVersaoDocumentoDTO> obterVersoesDocumentoDTO(ProcessoDocumentoBin processoDocumentoBin) throws PJeBusinessException {
		return mapperVersoesDocumentoDTO(obterVersoesDocumento(processoDocumentoBin));
	}
	
	/**
	 * Mapeia a lista ControleVersaoDocumento para uma lista ControleVersaoDocumentoDTO
	 * 
	 * @param List<ControleVersaoDocumento>
	 * @return List<ControleVersaoDocumentoDTO>
	 */
	private List<ControleVersaoDocumentoDTO> mapperVersoesDocumentoDTO(List<ControleVersaoDocumento> listControleVersaoDocumento) throws PJeBusinessException {
		List<ControleVersaoDocumentoDTO> listControleVersaoDocumentoDTO = new ArrayList<ControleVersaoDocumentoDTO>();
		
		for(ControleVersaoDocumento controleVersaoDocumento : listControleVersaoDocumento) {
			ControleVersaoDocumentoDTO controleVersaoDocumentoDTO = new ControleVersaoDocumentoDTO();
			
			controleVersaoDocumentoDTO.setIdControleVersaoDocumento(controleVersaoDocumento.getIdControleVersaoDocumento());
			controleVersaoDocumentoDTO.setLocalizacaoUsuario(controleVersaoDocumento.getLocalizacaoAtual());			
			controleVersaoDocumentoDTO.setDataModificacao(controleVersaoDocumento.getDataModificacao());
			try {
				byte[] encoded = SigningUtilities.base64Encode(controleVersaoDocumento.getConteudo().getBytes());
				controleVersaoDocumentoDTO.setConteudo(new String(encoded, 0, 0, encoded.length));
			} catch (IOException e) {
				throw new PJeBusinessException("Erro ao transformar o conteúdo da versão do documento em base64");
			}
			controleVersaoDocumentoDTO.setSha1Conteudo(controleVersaoDocumento.getSha1Conteudo());
			controleVersaoDocumentoDTO.setNomeUsuario(controleVersaoDocumento.getUsuario().getNome());
			controleVersaoDocumentoDTO.setAtivo(controleVersaoDocumento.isAtivo());
			controleVersaoDocumentoDTO.setVersao(controleVersaoDocumento.getVersao());
			controleVersaoDocumentoDTO.setObservacao(controleVersaoDocumento.getObservacao());
			
			listControleVersaoDocumentoDTO.add(controleVersaoDocumentoDTO);
		}
		
		return listControleVersaoDocumentoDTO;
	}

	/**
	 * Retorna o conteúdo do documento em formato JSON
	 * @param conteudo
	 * @return
	 * @throws JSONException 
	 */
	public String obterConteudoDocumentoJSON(String conteudo) throws JSONException {
		JSONObject retorno = new JSONObject();
		
		if(conteudo != null) {
			try {
				retorno.put("sucesso", Boolean.TRUE);
				
				retorno.put("resposta", java.util.Base64.getEncoder().encodeToString(conteudo.getBytes()));
			} catch (Exception e) {
				e.printStackTrace();
				retorno.put("sucesso", Boolean.FALSE);
				retorno.put("mensagem", e.getLocalizedMessage());
			}
		} else {
			retorno.put("sucesso", Boolean.FALSE);
			retorno.put("mensagem", "Documento vazio");
		}
		
		return retorno.toString();
	}

	public ControleVersaoDocumentoDAO getControleVersaoDocumentoDAO() {
		return controleVersaoDocumentoDAO;
	}

	public void setControleVersaoDocumentoDAO(
			ControleVersaoDocumentoDAO controleVersaoDocumentoDAO) {
		this.controleVersaoDocumentoDAO = controleVersaoDocumentoDAO;
	}

	@Override
	protected BaseDAO<ControleVersaoDocumento> getDAO() {
		return this.controleVersaoDocumentoDAO;
	}

	public ControleVersaoDocumento obterVersaoDocumento(int versaoDocumento, ProcessoDocumentoBin processoDocumentoBin) {
		return controleVersaoDocumentoDAO.obterVersaoDocumento(versaoDocumento, processoDocumentoBin);
	}
}
