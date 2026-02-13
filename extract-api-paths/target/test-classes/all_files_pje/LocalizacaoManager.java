/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.LocalizacaoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

@Name(LocalizacaoManager.NAME)
public class LocalizacaoManager extends BaseManager<Localizacao> {
	public static final String NAME = "localizacaoManager";
	
	@In
	private LocalizacaoDAO localizacaoDAO;
	
	@Override
	protected LocalizacaoDAO getDAO() {
		return localizacaoDAO;
	}
	
	public static LocalizacaoManager instance() {
		return (LocalizacaoManager)Component.getInstance(LocalizacaoManager.NAME);
	}
	
	/**
	 * Recupera a localização com o identificador indicado, persistindo uma nova localização com esse identificador se ela não existir.
	 * 
	 * @param identificador o identificador esperado
	 * @return a localização, já gerenciada pelo gerente de entidades
	 */
	public Localizacao getLocalizacao(String identificador){
		Localizacao ret = localizacaoDAO.findByName(identificador);
		if(ret == null){
			ret = getLocalizacao();
			ret.setLocalizacao(identificador);
			localizacaoDAO.persist(ret);
		}
		return ret;
	}
	
	public Localizacao getLocalizacao(){
		Localizacao ret = new Localizacao();
		ret.setAtivo(true);
		ret.setEstrutura(false);
		return ret;
	}
	
	/**
	 * Metodo que retorna a query responsavel por retornar as localizacoes de
	 * primeiro nivel apos o no principal.
	 * 
	 * @return query dos filhos de root
	 */
	public String getQueryFilhosPrimeiroNivel() {
		return this.getQueryFilhos();
	}

	/**
	 * Metodo que retorna a query responsavel por retornar as localizacoes
	 * filhas do no pai.
	 * 
	 * @return quer dos filhos de root ou de filhos dos demais nos
	 */
	public String getQueryFilhos() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT l FROM Localizacao l WHERE l.localizacaoPai = :");
		query.append(EntityNode.PARENT_NODE);
		query.append(" ORDER BY l.faixaInferior ");
		return query.toString();
	}
	
	/**
	 * Retorna a query que busca os nos roots (Principais) para a localizacao e o magistrado
	 * 
	 * @param idLocalizacao Id da Localizacao
	 * @param usuarioLocalizacaoMagistrado usuario localizacao do magistrado
	 * @return query gerada
	 */
	public String getQueryRootsLocalizacao(
			UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor, Integer idLocalizacao, Integer idLocalizacaoPai) {
		
		StringBuilder query = new StringBuilder("SELECT l FROM Localizacao l WHERE l.ativo = true");
		query.append(" AND l.faixaInferior IS NOT NULL ");
		if(idLocalizacao != null) {
			query.append(" AND l.idLocalizacao = ");
			query.append(idLocalizacao);
		}
		
		if (idLocalizacaoPai != null) {
			query.append(" AND l.localizacaoPai.idLocalizacao = ");
			query.append(idLocalizacaoPai);
		}
		if(idLocalizacao == null && idLocalizacaoPai == null) {
			query.append(" AND 1 != 1");
		}
		
		query.append(" ORDER BY l.faixaInferior ");
		return query.toString();
	}

	public String getQueryRootsLocalizacaoSegundoGrau(
			UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor, Integer idLocalizacao, Integer idLocalizacaoPai) {
		return this.getQueryRootsLocalizacao(usuarioLocalizacaoMagistradoServidor, idLocalizacao, idLocalizacaoPai);
	}
	/**
	 * Recupera as localizações vinculadas a uma dada pessoa.
	 * 
	 * @param p a pessoa cujas localizações se pretende obter
	 * @param first o primeiro resultado que se pretende obter, ou null
	 * @param max o máximo de resultados que se pretende obter por chamada ao método, ou null para todos.
	 * @return a lista de localizações.
	 */
	public List<Localizacao> getLocalizacoesPessoais(Pessoa p, Integer first, Integer max){
		return localizacaoDAO.getLocalizacoesPessoais(p, first, max);
	}
	
	public Localizacao getLocalizacaoExistente(String identificador){
		return localizacaoDAO.findByName(identificador);
	}

	public List<Localizacao> getLocalizacoesFisicasFolhaCompativeis (List<Integer> idsLocalizacoesSuperiores){
		return localizacaoDAO.getLocalizacoesFolha(idsLocalizacoesSuperiores);
	}

	public List<Localizacao> getLocalizacoesFisicasFolhaTribunal() throws PJeBusinessException {
		Integer idLocalizacaTribunal = ParametroUtil.instance().getLocalizacaoTribunal() != null ? ParametroUtil.instance().getLocalizacaoTribunal().getIdLocalizacao() : null;
		List<Integer> idsLocalizacaoTribunal = new ArrayList<>();
		idsLocalizacaoTribunal.add(idLocalizacaTribunal);
		return this.getLocalizacoesFisicasFolhaCompativeis(idsLocalizacaoTribunal);
 	}
	
	/**
	 * Dada uma lista de localizações inferiores, deve-se retornar uma lista de localizações superiores çompatíveis e que são 
	 * superiores comuns às localizações dadas, caso não encontre, retorna a localização física do tribunal
	 * 
	 * @param idsLocalizacoesSuperiores
	 * @return
	 */
	public List<Localizacao> getLocalizacoesFisicasSuperioresCompativeis (List<Integer> idsLocalizacoesInferiores){
		List<Localizacao> localizacoesList = localizacaoDAO.getArvoreAscendente(idsLocalizacoesInferiores, true);
		if(CollectionUtilsPje.isEmpty(localizacoesList) && ParametroUtil.instance().getLocalizacaoTribunal() != null) {
			localizacoesList.add(ParametroUtil.instance().getLocalizacaoTribunal());
		}

		return localizacoesList;
	}

	public List<Localizacao> getLocalizacoesFisicasExcetoFolhaTribunal() throws PJeBusinessException {
		Integer idLocalizacaTribunal = ParametroUtil.instance().getLocalizacaoTribunal() != null ? ParametroUtil.instance().getLocalizacaoTribunal().getIdLocalizacao() : null;
		List<Integer> idsLocalizacaoTribunal = new ArrayList<>();
		idsLocalizacaoTribunal.add(idLocalizacaTribunal);
		return localizacaoDAO.getLocalizacoesExcetoFolhas(idsLocalizacaoTribunal);
 	}
	
	/**
	 * Retorna as localizacoes filhas de uma localizacao dada
	 * @param idLocalizacaoPai
	 * @return
	 * @throws PJeBusinessException
	 */
	public List<Localizacao> obterFilhas(int idLocalizacaoPai) throws PJeBusinessException {
		List<Integer> idsLocalizacao = new ArrayList<>();
		idsLocalizacao.add(idLocalizacaoPai);

		return localizacaoDAO.obterFilhas(idsLocalizacao); 	
	}
	
	/**
  	 * Verifica se a localizacao possui OJ ou OJC.
  	 * 
  	 * @param Localizacao localizacao
  	 * @return Verdadeiro caso a localizacao possua OJ ou OJC. Falso, caso contrrio.
  	 */
  	public boolean isLocalizacaoPossuiOJouOJC(Localizacao localizacao){
  		return this.isLocalizacaoPossuiOJ(localizacao) 
  				|| this.isLocalizacaoPossuiOJC(localizacao);
 	}
  	
  	public boolean isLocalizacaoPossuiOJ(Localizacao localizacao){
  		OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent("orgaoJulgadorManager");
  		
  		return orgaoJulgadorManager.getOrgaoJulgadorByLocalizacaoExata(localizacao) != null; 
  	}
	
  	public boolean isLocalizacaoPossuiOJC(Localizacao localizacao){
  		OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager = ComponentUtil.getComponent("orgaoJulgadorColegiadoManager");
  		
  		return CollectionUtilsPje.isNotEmpty(orgaoJulgadorColegiadoManager.getColegiadosByLocalizacaoExata(localizacao)); 
  	}
	
	/**
	 * Verifica se a localização é descendente da localização candidata
	 * 
	 * @param Localizacao inferior
	 * @param Localizacao superior
	 * @return Boolean: verdadeiro ou falso
	 */
	public boolean isLocalizacaoDescendente(Localizacao inferior, Localizacao superior){
		for(Localizacao ancestral: getArvoreAscendente(inferior.getIdLocalizacao(), true)){
			if(ancestral.equals(superior)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Retorna as localizações baseadas na lista de id do endereço
	 * @param idEndereco
	 * @return	retorna uma lista com as localizações que estão relacionadas ao id passado por parametro.
	 */
	public List<Localizacao> obterLocalizacoes(Integer idEndereco){
		List<Localizacao> localizacoes = new ArrayList<Localizacao>();
		if (idEndereco != null) {
			localizacoes = localizacaoDAO.obterLocalizacoesComEndereco(idEndereco);
		}
		return localizacoes;
	}
	
	/**
 	 * metodo responsavel por encaminhar o objeto passado em parametro para persistencia
 	 * @param localizacao
 	 * @throws Exception 
 	 */
 	public void salvarLocalizacao(Localizacao localizacao) throws Exception {
 		localizacaoDAO.salvarLocalizacao(localizacao);		
 	}
	
	public List<Localizacao> getArvoreDescendente(Integer idLocalizacao, boolean incluirNoRaiz) {
		List<Integer> idsLocalizacao = new ArrayList<>();
		idsLocalizacao.add(idLocalizacao);

		return localizacaoDAO.getArvoreDescendente(idsLocalizacao, incluirNoRaiz);
	}

	public List<Localizacao> getArvoreAscendente(Integer idLocalizacao, boolean incluirNoRaiz) {
		List<Integer> idsLocalizacao = new ArrayList<>();
		idsLocalizacao.add(idLocalizacao);
		
		return localizacaoDAO.getArvoreAscendente(idsLocalizacao, incluirNoRaiz);
	}
}
