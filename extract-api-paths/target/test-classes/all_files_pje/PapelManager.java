/**
 *  pje-web
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.business.dao.PapelDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(PapelManager.NAME)
public class PapelManager extends BaseManager<Papel>{
	
	public static final String NAME  = "papelManager";
	
	private List<Papel> papeisParaDocumentosNaoLidos;

	@In
	private PapelDAO papelDAO;

	@Override
	protected PapelDAO getDAO() {
		return papelDAO;
	}
	
	public Papel findByCodeName(String identificador) throws PJeBusinessException {
		return papelDAO.findByCodeName(identificador);
	}

	public boolean criarPapel(String identificador) throws PJeBusinessException {
		Papel p = papelDAO.findByCodeName(identificador);
		if(p != null){
			return false;
		}
		p = new Papel();
		p.setCondicional(false);
		p.setIdentificador(identificador);
		p.setNome(identificador);
		persistAndFlush(p);
		return true;
	}
	
	public List<Papel> getPapeisHerdados(Papel papel){
		return this.getDAO().findAllChildrenByPapel(papel);
	}
	
	public List<Papel> recuperarPapeisHerdeiros(Papel papel) {
		return this.getDAO().findAllHerdeirosByPapel(papel);
	}
	
	/**
	 * Recupera a lista de identificadores dos papeis pretensamente atribuíveis na aplicação,
	 * ou seja, aqueles não marcados como condicionais.
	 *
	 * @return a lista de identificadores
	 * @throws PJeBusinessException 
	 */
	public List<String> recuperaAtribuiveis() throws PJeBusinessException{
		Search s = new Search(Papel.class);
		s.setRetrieveField("identificador");
		addCriteria(s, Criteria.equals("condicional", false));
		return list(s);
	}
	
	/**
	 * Retorna a lista de papéis definida no parâmetro pje:agrupador:docsNaoLidos:papeis
	 * Esta lista indica quais papéis terão seus documentos juntados considerados como não lidos.
	 * 
	 * @return lista de papéis definida no parâmetro pje:agrupador:docsNaoLidos:papeis
	 */
	public List<Papel> getPapeisParaDocumentosNaoLidos() {
		if (this.papeisParaDocumentosNaoLidos == null) {
			String papeis = ParametroUtil.getFromContext(Parametros.PJE_AGRUPADOR_DOCS_NAO_LIDOS_PAPEIS, true);
			StringTokenizer token = new StringTokenizer(papeis, ",");
			papeis = papeis.replace("\n", "").trim();
			Papel papel = null;
			PapelManager p = (PapelManager) Component.getInstance("papelManager");
			List<Papel> papeisPesquisa = new ArrayList<Papel>();
			while (token.hasMoreElements()) {
				String strToken = token.nextToken();
				try {
					papel = p.findByCodeName(strToken);
					if (papel != null) {
						papeisPesquisa.add(papel);
					}
				} catch (PJeBusinessException e) {
					this.logger.error("Não existe papel com identificador ".concat(strToken));
				}
				
			}
			this.papeisParaDocumentosNaoLidos = papeisPesquisa;
		}
		return this.papeisParaDocumentosNaoLidos;
	}
	
	public boolean isPapelDocumentoNaoLido(Papel papel) {
		boolean result = Boolean.FALSE;
		
		if (papel != null) {
			result = this.getPapeisParaDocumentosNaoLidos().stream().filter(p -> p.equals(papel)).findAny().isPresent(); 
		}
		
		return result;
	}
	public boolean possuiPapelAdvogado( UsuarioLocalizacao usuarioLocalizacao ){		
		Papel papelAdvogado = ParametroUtil.instance().getPapelAdvogado();
		
		if( usuarioLocalizacao == null || papelAdvogado == null ){
			return false;
		}
		
		if( usuarioLocalizacao.getPapel().getIdPapel() == papelAdvogado.getIdPapel() ){
			return true;
		}
		
		return false;
	}
}
