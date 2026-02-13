package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.business.dao.TipoProcessoDocumentoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TipoProcessoDocumentoDTO;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.TipoDocumentoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * @author cristof
 * 
 */
@Name(TipoProcessoDocumentoManager.NAME)
public class TipoProcessoDocumentoManager extends BaseManager<TipoProcessoDocumento>{

	public static final String NAME = "tipoProcessoDocumentoManager";

	@In
	private ParametroService parametroService;

	@In
	private TipoProcessoDocumentoDAO tipoProcessoDocumentoDAO;

	@Logger
	private Log logger;

	@Override
	protected TipoProcessoDocumentoDAO getDAO(){
		return this.tipoProcessoDocumentoDAO;
	}

	public List<TipoProcessoDocumento> findDisponiveisInternatemente(Papel papel){
		return this.tipoProcessoDocumentoDAO.findAvailable(papel);
	}

	public List<TipoProcessoDocumento> findDisponiveisExternamente(){
		return this.tipoProcessoDocumentoDAO.findExternallyAvailable(Authenticator.getPapelAtual());
	}

	public List<TipoProcessoDocumento> findDisponiveis(Integer... ids){
		return this.tipoProcessoDocumentoDAO.findByIds(ids);
	}

	public List<TipoProcessoDocumento> findDisponiveis(Papel papel, Integer... ids){
		return this.tipoProcessoDocumentoDAO.filtraTipoProcessoDocumento(papel, ids);
	}

	public List<TipoProcessoDocumento> findDisponiveisAntigos(boolean inicial, boolean modelo){
		return tipoProcessoDocumentoDAO.findTiposDocumentosAntigos(inicial, modelo);
	}

	public List<TipoProcessoDocumento> getTipoProcessoDocumentoExpedienteList(){
		return tipoProcessoDocumentoDAO.getTipoProcessoDocumentoExpedienteList();
	}
	
	/**
	 * Retorna a lista de todos os tipos de documento de ato proferido
	 * @return
	 */
	public List<TipoProcessoDocumento> getTipoDocumentoAtoProferidoList(){
		return tipoProcessoDocumentoDAO.findAllAtoProferido();
	}

	public List<TipoProcessoDocumento> getTipoDocumentoAtoMagistradoList(){
		try{
			List<Integer> ids = new ArrayList<Integer>();
			if(ParametroUtil.instance().isPrimeiroGrau()){
				ids.add(Integer.parseInt(parametroService.valueOf(Parametros.TIPODOCUMENTOSENTENCA)));
			}else{
				ids.add(Integer.parseInt(parametroService.valueOf(Parametros.TIPODOCUMENTOACORDAO)));
			}
			ids.add(Integer.parseInt(parametroService.valueOf(Parametros.TIPODOCUMENTOATOORDINATORIO)));
			ids.add(Integer.parseInt(parametroService.valueOf(Parametros.TIPODOCUMENTODECISAO)));
			ids.add(Integer.parseInt(parametroService.valueOf(Parametros.TIPODOCUMENTODESPACHO)));
			return tipoProcessoDocumentoDAO.findByIds(ids.toArray(new Integer[ids.size()]));
		}catch (NumberFormatException e){
			logger.error("Erro ao tentar recuperar o código de tipo de documento registrado em parâmetro do sistema: {0}. Retornando lista vazia.", e.getLocalizedMessage());
		}
		return Collections.emptyList();
	}

	public TipoProcessoDocumento findByCodigoDocumento(String codigoDocumento, Boolean status) throws PJeBusinessException{
		TipoProcessoDocumento returnValue = getDAO().findByCodigoDocumento(codigoDocumento, status);
		if(returnValue == null){
			throw new PJeBusinessException(String.format("Tipo de documento %s não localizado", codigoDocumento));
		}
		return returnValue;
	}
	
	public TipoProcessoDocumento findByCodigoTipoProcessoDocumento(String codigoTipoProcessoDocumento) throws PJeBusinessException{
		TipoProcessoDocumento returnValue = getDAO().findByCodigoTipoProcessoDocumento(codigoTipoProcessoDocumento);
		if(returnValue == null){
			throw new PJeBusinessException(String.format("Tipo de documento %s não localizado", codigoTipoProcessoDocumento));
		}
		return returnValue;
	}
	
	public TipoProcessoDocumento presuncaoTipoDocumento(String descricao, Papel papel) throws PJeBusinessException {
		String txt = descricao.replaceAll(" ", "%");
		Search search = new Search(TipoProcessoDocumento.class);
		addCriteria(search, 
				Criteria.equals("ativo", true),
				Criteria.startsWith("tipoProcessoDocumento", txt), 
				Criteria.in("papeis.papel", new Papel[]{papel}));
		search.setMax(1);
		List<TipoProcessoDocumento> tipos = list(search);
		return tipos.isEmpty() ? null : tipos.get(0);
	}

	public List<TipoProcessoDocumento> getDisponiveis(Papel papel, TipoDocumentoEnum...tipos) throws PJeBusinessException {
		return getDisponiveis(papel, false, tipos);
	}
	
	public List<TipoProcessoDocumento> getDisponiveis(Papel papel, boolean restringirJuntadaDocumento, TipoDocumentoEnum...tipos) throws PJeBusinessException {
		Search search = new Search(TipoProcessoDocumento.class);
		addCriteria(search, Criteria.equals("ativo", true));
		if (tipos != null && tipos.length > 0) {
			addCriteria(search, Criteria.in("inTipoDocumento", tipos));
		}
		if (papel != null) {
			addCriteria(search, Criteria.equals("papeis.papel", papel));
		}
		if (restringirJuntadaDocumento) {
			addCriteria(search, Criteria.equals("exibeJuntadaDocumento", true));
		}
		search.addOrder("o.tipoProcessoDocumento", Order.ASC);
		return list(search);

	}
	
	public List<TipoProcessoDocumento> findByAplicacaoClasse(Integer idAplicacaoClasse, String tipoProcessoDocumento){
		return getDAO().findByAplicacaoClasse(idAplicacaoClasse, tipoProcessoDocumento);
	}

	public List<TipoProcessoDocumento> findTiposExceto(
			Integer idTipoProcessoDocumentoExcecao) {
		List<TipoProcessoDocumento> tipos = tipoProcessoDocumentoDAO.findTiposDocumentos(idTipoProcessoDocumentoExcecao);
		return tipos;
	}

	/** 
	 * Método que retorna a lista de tipos de documentos ativos que estejam dentro da lista informada
	 * @param idsTipoDocumento - a lista de ids dos tipos de documento que deverão ser retornados caso estejam ativos
	 * @return a lista de tipos de documentos ativos dentre os informados no parâmetro
	 */
	public List<TipoProcessoDocumento> findTiposIn(List<Integer> idsTipoDocumento) throws PJeBusinessException{
		List<TipoProcessoDocumento> tipos = tipoProcessoDocumentoDAO.findTiposDocumentosIn(idsTipoDocumento);
		return tipos;
	}
	
	/** 
	 * Método que retorna a lista de tipos de documentos ativos que não estejam dentro da lista informada
	 * @param idsTipoDocumento - a lista de ids dos tipos de documento que não deverão ser retornados.
	 * @return a lista de tipos de documentos ativos exceto os informados no parâmetro
	 */
	public List<TipoProcessoDocumento> findTiposNotIn(List<Integer> idsTipoDocumento) throws PJeBusinessException{
		List<TipoProcessoDocumento> tipos = tipoProcessoDocumentoDAO.findTiposDocumentosNotIn(idsTipoDocumento);
		return tipos;
	}
	
	/**
	 * Recupera uma lista de {@link TipoProcessoDocumento} de acordo com os identificadores informados.
	 * 
	 * @param ids Arrays de identificadores.
	 * @return Uma lista de {@link TipoProcessoDocumento} de acordo com os identificadores informados.
	 */
	public List<TipoProcessoDocumento> findByIds(Integer[] ids) {
		if (ids != null && ids.length > 0) {
			return tipoProcessoDocumentoDAO.findByIds(ids);
		}
		return Collections.emptyList();
	}
	
	/**
	 * Retorna um Map o qual contém os objetos {@link TipoProcessoDocumento} correspondentes aos identificadores informados.
	 * 
	 * @param idsTipoDocumentoVoto Array de indentificadores.
	 * @return Um Map o qual contém os objetos {@link TipoProcessoDocumento} correspondentes aos identificadores informados.
	 */
	public HashMap<String, TipoProcessoDocumento> getMapTipoProcessoDocumento(Integer[] idsTipoDocumentoVoto) {
		HashMap<String, TipoProcessoDocumento> resultMap = new HashMap<String, TipoProcessoDocumento>(0);
		List<TipoProcessoDocumento> tiposProcessoDocumento = this.tipoProcessoDocumentoDAO.findByIds(idsTipoDocumentoVoto);
		for (TipoProcessoDocumento tipoProcessoDocumento : tiposProcessoDocumento) {
			resultMap.put(tipoProcessoDocumento.getTipoProcessoDocumento(), tipoProcessoDocumento);
		}
		return resultMap;
	}

	/**
	 * Consulta os tipos de documento disponíveis.
	 * 
	 * @return Lista de TipoProcessoDocumento.
	 */
	public List<TipoProcessoDocumento> consultarTodosDisponiveis(){
		return tipoProcessoDocumentoDAO.consultarTodosDisponiveis();
	}
	
	/**
	 * Consulta os tipos de documento disponíveis.
	 * 
	 * @return Lista de TipoProcessoDocumentoDTO.
	 */
	public List<TipoProcessoDocumentoDTO> consultarTodosDisponiveisDTO(){
		return tipoProcessoDocumentoDAO.consultarTodosDisponiveisDTO();
	}
	
	public TipoProcessoDocumento findByDescricaoDocumento(String codigoDocumento) throws PJeBusinessException{
		TipoProcessoDocumento returnValue = getDAO().findByDescricaoDocumento(codigoDocumento);
		if(returnValue == null){
			throw new PJeBusinessException(String.format("Tipo de documento %s não localizado", codigoDocumento));
		}
		return returnValue;
	}

	/** Consulta o tipo de documento por sua descrição
	 * 
	 * @param tipoDocumento Descrição do documento
	 * @return objeto TipoProcessoDocumentoCorrespondente
	 */
	public TipoProcessoDocumento findByDescricaoTipoDocumento(String tipoDocumento){
		return tipoProcessoDocumentoDAO.findByDescricaoDocumento(tipoDocumento);
	}

	/**
	 * Metodo responsavel por recuperar os tipos de processo documento de acordo
	 * com a aplicação classe
	 * 
	 * @return List<TipoProcessoDocumento>
	 */
	public List<TipoProcessoDocumento> obterTipoProcessoDocumentoPorAplicacaoClasseAtual() {
		return getDAO().obterTipoProcessoDocumentoPorAplicacaoClasseAtual();
	}
	
	/**
	 * Metodo responsavel por recuperar os tipos de processo documento de acordo
	 * com o tipo de documento informado
	 * 
	 * @param tipoDocumento
	 * @return List<TipoProcessoDocumento>
	 */
	public List<TipoProcessoDocumento> findByTipoDocumento(TipoDocumentoEnum tipoDocumento) {
		return getDAO().findByTipoDocumento(tipoDocumento);
	}
	
}
