/**
 * pje-web
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.TipoVotoDAO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componete de controle negocial da entidade {@link TipoVoto}.
 * 
 * @author cristof
 *
 */
@Name("tipoVotoManager")
public class TipoVotoManager extends BaseManager<TipoVoto> {
	
	@In
	private TipoVotoDAO tipoVotoDAO;

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected TipoVotoDAO getDAO() {
		return tipoVotoDAO;
	}
	
	//************ Importação de métodos de br.com.infox.pje.manager.TipoVotoManager ************//
	
	/**
	 * Verifica qual é o contexto do voto e para cada tipo retorna uma letra que
	 * pela regra, representa esse contexto.
	 * 
	 * @param tv tipo de voto a se obter a letra
	 * @return letra referente ao tipo de voto
	 */
	public String getLetraTipoVoto(TipoVoto tv) {
		if (tv != null && tv.getContexto() != null) {
			if (tv.getContexto().equals("C")) {
				return "A";
			} else {
				return tv.getContexto();
			}
		}
		return null;
	}

	/**
	 * Recupera a lista de tipos ativos de voto que um relator de um processo pode proferir.
	 * 
	 * @return a lista
	 */
	public List<TipoVoto> listTipoVotoAtivoComRelator() {
		Search s = new Search(TipoVoto.class);
		addCriteria(s, 
				Criteria.equals("ativo", true),
				Criteria.equals("relator", true));
		s.addOrder("tipoVoto", Order.ASC);
		return list(s);
	}

	//******** Fim da importação de métodos de br.com.infox.pje.manager.TipoVotoManager *********//
	//************** Importação de métodos de br.com.infox.pje.manager.TipoVotoDAO **************//
	
	/**
	 * Recupera os tipos de voto que podem ser proferidos por não relatores.
	 * 
	 * @return a lista de tipos
	 */
	public List<TipoVoto> tiposVotosVogais(){
		Search s = new Search(TipoVoto.class);
		addCriteria(s, 
				Criteria.equals("ativo", true),
				Criteria.equals("relator", false));
		s.addOrder("tipoVoto", Order.ASC);
		return list(s);
	}
	
	/**
	 * Recupera os tipos de voto que podem ser proferidos por relatores.
	 * 
	 * @return a lista de tipos
	 */
	public List<TipoVoto> tiposVotosRelator(){
		Search s = new Search(TipoVoto.class);
		addCriteria(s, 
				Criteria.equals("ativo", true),
				Criteria.equals("relator", true));
		s.addOrder("tipoVoto", Order.ASC);
		return list(s);
	}
	
	/**
	 * Recupera o tipo do voto de um dado documento produzido por um órgão julgador na sessão.
	 * 
	 * @param sessao a sessão de julgamento
	 * @param idProcessoTrf identificador do processo no qual teria sido produzido o documento
	 * @param tipoDocumento o tipo de documento voto 
	 * @param oj o órgão julgador do prolator do documento
	 * @return tipo de voto do relator
	 */
	public TipoVoto recuperaTipoVoto(Sessao sessao, int idProcessoTrf, TipoProcessoDocumento tipoDocumento, OrgaoJulgador oj) {
		Search s = new Search(SessaoProcessoDocumentoVoto.class);
		s.setRetrieveField("tipoVoto");
		addCriteria(s, 
				Criteria.equals("sessao", sessao),
				Criteria.equals("processoDocumento.processo.idProcesso", idProcessoTrf),
				Criteria.equals("processoDocumento.tipoProcessoDocumento", tipoDocumento),
				Criteria.equals("orgaoJulgador", oj),
				Criteria.isNull("processoDocumento.dataExclusao"));
		s.setMax(1);
		List<TipoVoto> result = list(s);
		return result.isEmpty() ? null : result.get(0);
	}
	
	//********** Fim da importação de métodos de br.com.infox.pje.manager.TipoVotoDAO ***********//
	
	public TipoVoto recuperaAcompanhaRelator(){
		return recuperaTipo(false, "C");
	}

	public TipoVoto recuperaAcompanhaParteRelator(){
		return recuperaTipo(false, "P");
	}
	
	public TipoVoto recuperaTipoDivergente(){
		return recuperaTipo(false, "D");
	}
	
	public TipoVoto recuperaNaoConhece(){
		return recuperaTipo(false, "N");
	}
	
	public TipoVoto recuperaSuspeito(){
		return recuperaTipo(false, "S");
	}
	
	public TipoVoto recuperaImpedido(){
		return recuperaTipo(false, "I");
	}		
	
	private TipoVoto recuperaTipo(boolean relator, String contexto){
		Search s = new Search(TipoVoto.class);
		addCriteria(s, 
				Criteria.equals("ativo", true),
				Criteria.equals("relator", relator),
				Criteria.equals("contexto", contexto));
		s.setMax(1);
		List<TipoVoto> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}
	
	public List<TipoVoto> recuperaTipos(boolean relator){
		Search s = new Search(TipoVoto.class);
		addCriteria(s, 
				Criteria.equals("ativo", true),
				Criteria.equals("relator", relator));
		return list(s);
	}
	
	public boolean isDivergencia(TipoVoto tipoVoto) {
		return tipoVoto.getContexto().equals("D");
	}
}
