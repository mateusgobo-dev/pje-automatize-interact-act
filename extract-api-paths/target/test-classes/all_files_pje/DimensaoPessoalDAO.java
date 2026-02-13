package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.DimensaoPessoal;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(DimensaoPessoalDAO.NAME)
public class DimensaoPessoalDAO extends BaseDAO<DimensaoPessoal> {
	
	public static final String NAME = "dimensaoPessoalDAO";
	
	@Override
	public Integer getId(DimensaoPessoal dp) {
		return dp.getIdDimensaoPessoal();
	}
	
	@SuppressWarnings("unchecked")
	public List<DimensaoPessoal> getDimensoesPessoais(ProcessoTrf proc, List<Competencia> competencias, Jurisdicao jurisdicao){
		Map<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("aplicacaoClasse", jurisdicao.getAplicacao());
		parametros.put("competencias", competencias);
		parametros.put("jurisdicao", jurisdicao);
		parametros.put("classeJudicial", proc.getClasseJudicial());
		parametros.put("assuntos", proc.getAssuntoTrfList());
		
		// Passar listas vazias ou nulas para trechos hql ' IN (:lista)' retorna exceção do hibernate
		if (competencias == null || competencias.size() == 0
				|| proc.getAssuntoTrfList() == null || proc.getAssuntoTrfList().size() == 0){
			return Collections.EMPTY_LIST;
		}
		
		String query = "SELECT DISTINCT dp " +
				"FROM Competencia c " + 
				"JOIN c.competenciaClasseAssuntoList cca " +
				"JOIN cca.classeAplicacao ca " + 
				"JOIN c.orgaoJulgadorCompetenciaList ojc " +
				"JOIN ojc.orgaoJulgador.orgaoJulgadorCargoList ojcc " +
				"JOIN c.dimensaoPessoalList dp " +
				"WHERE ojc.orgaoJulgador.jurisdicao = :jurisdicao " +
				"AND ojc.orgaoJulgador.ativo = true " +
				"AND (ojcc.recebeDistribuicao = true AND ojcc.valorPeso > 0) " +
				"AND ca.classeJudicial = :classeJudicial " + 
				"AND ca.aplicacaoClasse = :aplicacaoClasse " + 
				"AND cca.assuntoTrf IN (:assuntos) " + 
				"AND dp.ativo = true " + 
				"AND cca.competencia.ativo = true " + 
				"AND ojc.orgaoJulgador.ativo = true " + 
				"AND c IN (:competencias) " +
				"AND CURRENT_DATE >= ojc.dataInicio " + 
				"AND (ojc.dataFim IS NULL OR CURRENT_DATE <= ojc.dataFim) "+
				"AND (exists(select 1 from DimensaoPessoalPessoa o where o.dimensaoPessoal.idDimensaoPessoal = dp.idDimensaoPessoal) "+
				"OR   exists(select 1 from DimensaoPessoalTipoPessoa t where t.dimensaoPessoal.idDimensaoPessoal = dp.idDimensaoPessoal)) ";
		String queryColegiado = "SELECT DISTINCT dp " +
				"FROM Competencia c " + 
				"JOIN c.competenciaClasseAssuntoList cca " +
				"JOIN cca.classeAplicacao ca " + 
				"JOIN c.orgaoJulgadorColegiadoCompetenciaList ojcol " +
				"JOIN ojcol.orgaoJulgadorColegiado AS oj " +
				"JOIN c.dimensaoPessoalList dp " +
				"JOIN dp.pessoasAfetadasList pa " +
				"WHERE oj.jurisdicao = :jurisdicao " +
				"AND oj.ativo = true " +
				"AND ca.classeJudicial = :classeJudicial " + 
				"AND ca.aplicacaoClasse = :aplicacaoClasse " + 
				"AND cca.assuntoTrf IN (:assuntos) " + 
				"AND dp.ativo = true " + 
				"AND c.ativo = true " + 
				"AND c IN (:competencias) " + 
				"AND CURRENT_DATE >= ojcol.dataInicio " +
				"AND (ojcol.dataFim IS NULL OR CURRENT_DATE <= ojcol.dataFim) "+
				"AND (exists(select 1 from DimensaoPessoalPessoa o where o.dimensaoPessoal.idDimensaoPessoal = dp.idDimensaoPessoal) "+
				"OR   exists(select 1 from DimensaoPessoalTipoPessoa t where t.dimensaoPessoal.idDimensaoPessoal = dp.idDimensaoPessoal)) ";
		Query q = entityManager.createQuery(query);
		loadParameters(q, parametros);
		Set<DimensaoPessoal> ret = new HashSet<DimensaoPessoal>((List<DimensaoPessoal>) q.getResultList());
		q = entityManager.createQuery(queryColegiado);
		loadParameters(q, parametros);
		ret.addAll((List<DimensaoPessoal>) q.getResultList());
		return new ArrayList<DimensaoPessoal>(ret);
	}

}
