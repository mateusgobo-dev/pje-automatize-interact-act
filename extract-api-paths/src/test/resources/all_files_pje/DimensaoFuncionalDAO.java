package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.component.Util;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.DimensaoFuncional;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(DimensaoFuncionalDAO.NAME)
public class DimensaoFuncionalDAO extends BaseDAO<DimensaoFuncional> {

	public static final String NAME = "dimensaoFuncionalDAO";
	
	@Override
	public Integer getId(DimensaoFuncional df) {
		return df.getIdDimensaoFuncional();
	}
	
	@SuppressWarnings("unchecked")
	public List<DimensaoFuncional> getDimensoesFuncionais(ProcessoTrf proc, List<Competencia> competencias, Jurisdicao jurisdicao){
		Map<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("aplicacaoClasse", jurisdicao.getAplicacao());
		parametros.put("competencias", competencias);
		parametros.put("jurisdicao", jurisdicao);
		parametros.put("classeJudicial", proc.getClasseJudicial());
		parametros.put("assuntos", proc.getAssuntoTrfList());
		
		// Ao passar null nas listas em que ocorre o "in (:list)" evita-se o erro "unexpected end of subtree"
		if (Util.isEmpty(parametros.get("competencias"))){
			parametros.put("competencias", null);
		}
		if (Util.isEmpty(parametros.get("assuntos"))){
			parametros.put("assuntos", null);
		}
		
		String query = "SELECT DISTINCT df " +
				"FROM Competencia c " + 
				"JOIN c.competenciaClasseAssuntoList cca " +
				"JOIN cca.classeAplicacao ca " + 
				"JOIN c.orgaoJulgadorCompetenciaList ojc " +
				"JOIN ojc.orgaoJulgador.orgaoJulgadorCargoList ojcc " +
				"JOIN c.dimensaoFuncionalList df " + 
				"WHERE ojc.orgaoJulgador.jurisdicao = :jurisdicao " +
				"AND ojc.orgaoJulgador.ativo = true " +
				"AND (ojcc.recebeDistribuicao = true AND ojcc.valorPeso > 0) " +
				"AND ca.classeJudicial = :classeJudicial " + 
				"AND ca.aplicacaoClasse = :aplicacaoClasse " + 
				"AND cca.assuntoTrf IN (:assuntos) " + 
				"AND df.ativo = true " + 
				"AND cca.competencia.ativo = true " + 
				"AND ojc.orgaoJulgador.ativo = true " + 
				"AND c IN (:competencias) " +
				"AND CURRENT_DATE >= ojc.dataInicio " + 
				"AND (ojc.dataFim IS NULL OR CURRENT_DATE <= ojc.dataFim) ";
		String queryColegiado = "SELECT DISTINCT df " +
				"FROM Competencia c " + 
				"JOIN c.competenciaClasseAssuntoList cca " +
				"JOIN cca.classeAplicacao ca " + 
				"JOIN c.orgaoJulgadorColegiadoCompetenciaList ojcol " +
				"JOIN ojcol.orgaoJulgadorColegiado AS oj " +
				"JOIN c.dimensaoFuncionalList df " + 
				"WHERE oj.jurisdicao = :jurisdicao " +
				"AND oj.ativo = true " +
				"AND ca.classeJudicial = :classeJudicial " + 
				"AND ca.aplicacaoClasse = :aplicacaoClasse " + 
				"AND cca.assuntoTrf IN (:assuntos) " + 
				"AND df.ativo = true " + 
				"AND c.ativo = true " + 
				"AND c IN (:competencias) " + 
				"AND CURRENT_DATE >= ojcol.dataInicio " +
				"AND (ojcol.dataFim IS NULL OR CURRENT_DATE <= ojcol.dataFim) ";
		Query q = entityManager.createQuery(query);
		loadParameters(q, parametros);
		Set<DimensaoFuncional> ret = new HashSet<DimensaoFuncional>((List<DimensaoFuncional>) q.getResultList());
		q = entityManager.createQuery(queryColegiado);
		loadParameters(q, parametros);
		ret.addAll((List<DimensaoFuncional>) q.getResultList());
		return new ArrayList<DimensaoFuncional>(ret);
	}
}
