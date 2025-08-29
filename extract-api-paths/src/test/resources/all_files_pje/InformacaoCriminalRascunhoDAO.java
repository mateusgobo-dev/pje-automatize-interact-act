package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRascunho;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(InformacaoCriminalRascunhoDAO.NAME)
public class InformacaoCriminalRascunhoDAO extends BaseDAO<InformacaoCriminalRascunho>{

	public static final String NAME = "informacaoCriminalRascunhoDAO";
	
	@Override
	public Object getId(InformacaoCriminalRascunho e) {
		return e.getId();
	}
	
	/**
	 * Recupera a informacao criminal de um {@link ProcessoParte} para um {@link ProcessoTrf}
	 * @param idProcessoJudicial
	 * @param idProcessoParte
	 * @return a {@link InformacaoCriminalRascunho} ou null se não for encontrada
	 */
	@SuppressWarnings("unchecked")
	public InformacaoCriminalRascunho findByIdProcessoTrfAndIdProcessoParte(Integer idProcessoJudicial, Long idProcessoParte){
		List<InformacaoCriminalRascunho> lista = new ArrayList<InformacaoCriminalRascunho>(0);
		InformacaoCriminalRascunho ret = null;
		
		StringBuilder queryStr = new StringBuilder("");
		queryStr.append("SELECT ic FROM InformacaoCriminalRascunho ic ");
		queryStr.append("WHERE ic.processoRascunho.processo.idProcessoTrf = :idProcessoJudicial ");
		queryStr.append("AND ic.processoParte.id = :idProcessoParte ");
		
		Query query = this.entityManager.createQuery(queryStr.toString());
		query.setParameter("idProcessoJudicial", idProcessoJudicial);
		query.setParameter("idProcessoParte", idProcessoParte);
		
		lista = query.getResultList();
		
		if(!CollectionUtilsPje.isEmpty(lista)){
			ret = lista.get(0);
		}
		
		return ret;
	}
	
	/**
	 * Recupera as informacoes criminais de um {@link ProcessoTrf}
	 * @param idProcessoJudicial
	 * @return a {@link InformacaoCriminalRascunho} ou null se não for encontrada
	 */
	@SuppressWarnings("unchecked")
	public List<InformacaoCriminalRascunho> findAllByIdProcessoTrf(Integer idProcessoJudicial){
		List<InformacaoCriminalRascunho> lista = null;
		
		StringBuilder queryStr = new StringBuilder("");
		queryStr.append("SELECT ic FROM InformacaoCriminalRascunho ic ");
		queryStr.append("WHERE ic.processoRascunho.processo.idProcessoTrf = :idProcessoJudicial ");
		
		Query query = this.entityManager.createQuery(queryStr.toString());
		query.setParameter("idProcessoJudicial", idProcessoJudicial);
		
		lista = query.getResultList();
		
		if(CollectionUtilsPje.isEmpty(lista)){
			lista = null;
		}
		
		return lista;
	}
}
