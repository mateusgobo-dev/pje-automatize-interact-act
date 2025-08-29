package br.jus.cnj.pje.business.dao;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.webservice.controller.competencia.dto.CompetenciaClasseAssuntoDTO;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.CompetenciaClasseAssunto;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(CompetenciaClasseAssuntoDAO.NAME)
public class CompetenciaClasseAssuntoDAO extends BaseDAO<CompetenciaClasseAssunto>{
	
	public static final String NAME = "competenciaClasseAssuntoDAO";

	@Override
	public Object getId(CompetenciaClasseAssunto e) {
		return e.getIdCompClassAssu();
	}
	
	public int recuperarNivelAcesso(ProcessoTrf processo, Competencia c) {
		List<Integer> listIdAssunto = ComponentUtil.getProcessoJudicialManager().recuperarAssuntos(processo);
		StringBuilder queryStr = new StringBuilder("select max(cd_nivel_acesso) from client.tb_competencia_cl_assunto cca inner join client.tb_classe_aplicacao ca on cca.id_classe_aplicacao = ca.id_classe_aplicacao ");
		queryStr.append("inner join client.tb_classe_judicial c on ca.id_classe_judicial = c.id_classe_judicial inner join client.tb_aplicacao_classe ac on ca.id_aplicacao_classe = ac.id_aplicacao_classe ");
		queryStr.append(" where id_competencia = :competencia and id_assunto in (:idsAssuntos) ");		
		queryStr.append("and ac.id_aplicacao_classe = :idAplicacaoClasse ");
		queryStr.append("and c.id_classe_judicial = :idClasse ");
		Query q = this.entityManager.createNativeQuery(queryStr.toString());
		q.setParameter("competencia", c.getIdCompetencia());
		q.setParameter("idsAssuntos", Util.isEmpty(listIdAssunto) ? null : listIdAssunto);
		q.setParameter("idClasse", processo.getClasseJudicial().getIdClasseJudicial());
		q.setParameter("idAplicacaoClasse", ParametroUtil.instance().getAplicacaoSistema().getIdAplicacaoClasse());
		return (Integer) q.getSingleResult();

	}
	
	public boolean recuperarConfiguracaoSegredo(ProcessoTrf processo, Competencia c) {
		List<Integer> listIdAssunto = ComponentUtil.getProcessoJudicialManager().recuperarAssuntos(processo);
		StringBuilder queryStr = new StringBuilder("select count(1) from client.tb_competencia_cl_assunto cca inner join client.tb_classe_aplicacao ca on cca.id_classe_aplicacao = ca.id_classe_aplicacao ");
		queryStr.append("inner join client.tb_classe_judicial c on ca.id_classe_judicial = c.id_classe_judicial inner join client.tb_aplicacao_classe ac on ca.id_aplicacao_classe = ac.id_aplicacao_classe ");
		queryStr.append(" where in_segredo_sigilo = 't' and id_competencia = :competencia and id_assunto in (:idsAssuntos) ");		
		queryStr.append("and ac.id_aplicacao_classe = :idAplicacaoClasse ");
		queryStr.append("and c.id_classe_judicial = :idClasse ");
		Query q = this.entityManager.createNativeQuery(queryStr.toString());
		q.setParameter("competencia", c.getIdCompetencia());
		q.setParameter("idsAssuntos", Util.isEmpty(listIdAssunto) ? null : listIdAssunto);
		q.setParameter("idClasse", processo.getClasseJudicial().getIdClasseJudicial());
		q.setParameter("idAplicacaoClasse", ParametroUtil.instance().getAplicacaoSistema().getIdAplicacaoClasse());
		BigInteger qtdSigilo =  (BigInteger) q.getSingleResult();
		return qtdSigilo != null && qtdSigilo.doubleValue() > 0;
	}
	
	@SuppressWarnings("unchecked")
	public List<CompetenciaClasseAssuntoDTO> recuperarCompetenciaClasseAssunto(CompetenciaClasseAssuntoDTO competenciaClasseAssuntoDto) {
		StringBuilder queryStr = new StringBuilder("select ds_competencia, id_comp_class_assu, cd_assunto_trf || ' - ' || ds_assunto_trf as assunto, " );
		queryStr.append( " cd_classe_judicial || ' - ' || ds_classe_judicial_sigla || ' - ' || ds_classe_judicial as classe, in_segredo_sigilo, cd_nivel_acesso from client.tb_competencia_cl_assunto cca " );
		queryStr.append( "inner join client.tb_competencia comp on cca.id_competencia = comp.id_competencia inner join client.tb_classe_aplicacao ca on cca.id_classe_aplicacao = ca.id_classe_aplicacao " );  
		queryStr.append( "inner join client.tb_classe_judicial c on ca.id_classe_judicial = c.id_classe_judicial ");
		queryStr.append( "inner join client.tb_assunto_trf a on cca.id_assunto = a.id_assunto_trf where comp.in_ativo = 't' and ca.id_aplicacao_classe = ");
		queryStr.append(ParametroUtil.instance().getAplicacaoSistema().getIdAplicacaoClasse());
		if( competenciaClasseAssuntoDto != null ) {
			if(competenciaClasseAssuntoDto.getNomeCompetencia() != null && !competenciaClasseAssuntoDto.getNomeCompetencia().isEmpty()) {
				queryStr.append( " and ds_competencia ilike '%" );
				queryStr.append( competenciaClasseAssuntoDto.getNomeCompetencia() );
				queryStr.append( "%'" ); 
			}
			if(competenciaClasseAssuntoDto.getNomeAssunto() != null && !competenciaClasseAssuntoDto.getNomeAssunto().isEmpty()) {
				queryStr.append( " and ds_assunto_trf ilike '%" );
				queryStr.append( competenciaClasseAssuntoDto.getNomeAssunto() );
				queryStr.append( "%'" ); 
			}
			if(competenciaClasseAssuntoDto.getNomeClasse() != null && !competenciaClasseAssuntoDto.getNomeClasse().isEmpty()) {
				queryStr.append( " and c.ds_classe_judicial ilike '%" );
				queryStr.append( competenciaClasseAssuntoDto.getNomeClasse() );
				queryStr.append( "%'" ); 
			}
		}
		queryStr.append( " order by ds_competencia " );
		Query query  = getEntityManager().createNativeQuery(queryStr.toString());
		List<Object[]> rs = query.getResultList();
		List<CompetenciaClasseAssuntoDTO> retorno = new ArrayList<CompetenciaClasseAssuntoDTO>();
		for (Object[] r: rs) {
			CompetenciaClasseAssuntoDTO dto = new CompetenciaClasseAssuntoDTO();
			dto.setNomeCompetencia((String)r[0]);
			dto.setIdCompetenciaClasseAssunto((Integer)r[1]);
			dto.setNomeAssunto((String)r[2]);
			dto.setNomeClasse((String)r[3]);
			dto.setSigiloSegredo((Boolean)r[4]);
			dto.setNivelAcesso((Integer)r[5]);
			retorno.add(dto);
			
		}
		return retorno;
	}
}
