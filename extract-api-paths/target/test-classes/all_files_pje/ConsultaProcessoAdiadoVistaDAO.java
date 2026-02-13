package br.jus.cnj.pje.business.dao;


import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;

import br.jus.pje.nucleo.dto.FiltroProcessoSessaoDTO;
import br.jus.pje.nucleo.entidades.ConsultaProcessoAdiadoVista;
import br.jus.pje.nucleo.entidades.ProcessoTrf;



/**
 * Componente de acesso a dados da entidade {@link ConsultaProcessoAdiadoVista}.
 * 
 *
 */
@Name("consultaProcessoAdiadoVistaDAO")
public class ConsultaProcessoAdiadoVistaDAO extends BaseDAO<ConsultaProcessoAdiadoVista> {

	@Override
	public Object getId(ConsultaProcessoAdiadoVista consulta) {
		// TODO Auto-generated method stub
		return consulta.getIdProcessoTrf();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> pesquisarAdiados(FiltroProcessoSessaoDTO filtro) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.processoTrf from ConsultaProcessoAdiadoVista o where o.adiadoVista = 'AD' ");
		sb.append("and o.processoTrf.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ");
		sb.append("and not exists (select s.processoTrf from SessaoPautaProcessoTrf s ");
		sb.append("where s.processoTrf = o.processoTrf and s.dataExclusaoProcessoTrf is null ");
		sb.append("and s.sessao.dataRealizacaoSessao is null ");
		sb.append(" and (s.sessao.dataRegistroEvento is null or s.sessao.dataFechamentoSessao is not null)) ");
		if(filtro.getSessao().getContinua()){
			sb.append("and o.sessaoPautaProcessoTrf.sessao.continua is true ");
		}else{
			sb.append("and (o.sessaoPautaProcessoTrf.sessao.continua is false ");
			sb.append("		or (o.sessaoPautaProcessoTrf.sessao.continua is true ");
			sb.append("			and o.sessaoPautaProcessoTrf.orgaoJulgadorRetiradaJulgamento != o.processoTrf.orgaoJulgador)) ");
		}
		sb.append(" and ( (o.processoTrf.classeJudicial.pauta = true and o.processoTrf.selecionadoJulgamento = false) or " );
		sb.append(" (o.processoTrf.selecionadoPauta = true and o.processoTrf.classeJudicial.pauta = false) )");
		sb.append(" and	o.processoTrf.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ");
		
		if(filtro.getOrgaoJulgador() != null) {
			sb.append("	AND o.processoTrf.orgaoJulgador = :orgaoJulgador ");
		}
		if(filtro.getTipoVoto() != null) {
			sb.append("	AND o.processoTrf.idProcessoTrf in ( select voto.processoTrf.idProcessoTrf from SessaoProcessoDocumentoVoto voto where voto.tipoVoto = :tipoVoto and voto.liberacao = true ) ");
		}
		if(filtro.getNumeroSequencia() != null) {
			sb.append("	AND o.processoTrf.numeroSequencia = :numeroSequencia ");
		}
		if(filtro.getDigitoVerificador() != null) {
			sb.append("		AND o.processoTrf.numeroDigitoVerificador = :numeroDigitoVerificador ");
		}
		if(filtro.getAno() != null) {
			sb.append("		AND o.processoTrf.ano = :ano "); 
		}
		
		if(filtro.getRespectivoTribunal() != null && filtro.getRespectivoTribunal().trim().length() > 0){
			sb.append("		AND o.processoTrf.numeroOrgaoJustica = :numeroOrgaoJustica");
		}
		if (filtro.getNumeroOrigem() != null) {
			sb.append("		AND o.processoTrf.numeroOrigem = :numeroOrigem");
		}
			
		if (filtro.getClasseJudicial() != null && filtro.getClasseJudicial().trim().length() > 0 ) {
			sb.append("		AND LOWER(to_ascii(o.processoTrf.classeJudicial.classeJudicial)) LIKE LOWER(to_ascii( :classeJudicial )) )) ");
		}
		if (filtro.getNomeParte() != null && filtro.getNomeParte().trim().length() > 0 ) {
			sb.append(" AND EXISTS ( ");
			sb.append(" SELECT 1 ");
			sb.append(" FROM ProcessoParte pp " );
			sb.append(" WHERE pp.inSituacao = 'A' ");
			sb.append(" AND pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf ");
			sb.append(" AND LOWER(to_ascii(pp.pessoa.nome)) LIKE LOWER(to_ascii( :nomeParte ))  ) ");
		}
		if(filtro.getAssunto() != null && filtro.getAssunto().trim().length() > 0) {
			sb.append(" AND EXISTS ( ");
			sb.append(" SELECT 1 FROM ProcessoAssunto pa ");
			sb.append(" INNER JOIN pa.assuntoTrf ass ");
			sb.append(" WHERE pa.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf ");
			sb.append(" AND (ass.codAssuntoTrf = :assunto ");
			sb.append(" OR LOWER(to_ascii(ass.assuntoTrf)) LIKE LOWER(to_ascii( :assunto )) )) ");
		}
		
			
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgadorColegiado", Authenticator.getOrgaoJulgadorColegiadoAtual());
		
		if(filtro.getOrgaoJulgador() != null) {
			q.setParameter("orgaoJulgador", filtro.getOrgaoJulgador());
		}

		if(filtro.getTipoVoto() != null) {
			q.setParameter("tipoVoto", filtro.getTipoVoto());
		}
		
		if(filtro.getNumeroSequencia() != null) {
			q.setParameter("numeroSequencia", filtro.getNumeroSequencia());
		}
		
		if(filtro.getDigitoVerificador() != null) {
			q.setParameter("numeroDigitoVerificador", filtro.getDigitoVerificador());
		}
		
		if(filtro.getAno() != null) {
			q.setParameter("ano", filtro.getAno());
		}

		if(StringUtils.isNotBlank(filtro.getRamoJustica()) && StringUtils.isNotBlank(filtro.getRespectivoTribunal())){
			q.setParameter("numeroOrgaoJustica", Integer.parseInt(filtro.getRamoJustica() + filtro.getRespectivoTribunal()));
		}
		
		if (filtro.getNumeroOrigem() != null) { 
			q.setParameter("numeroOrigem", filtro.getNumeroOrigem());
		}

		if (filtro.getClasseJudicial() != null && filtro.getClasseJudicial().trim().length() > 0 ) { 
			q.setParameter("classeJudicial", filtro.getClasseJudicial());
		}

		if (filtro.getNomeParte() != null && filtro.getNomeParte().trim().length() > 0 ) { 
			q.setParameter("nomeParte", filtro.getNomeParte());
		}

		if(filtro.getAssunto() != null && filtro.getAssunto().trim().length() > 0) { 
			q.setParameter("assunto", filtro.getAssunto());
		}
		return q.getResultList();		
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> pesquisarVista(FiltroProcessoSessaoDTO filtro) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.processoTrf from ConsultaProcessoAdiadoVista o where o.sessaoPautaProcessoTrf.orgaoJulgadorPedidoVista IS NOT NULL AND o.adiadoVista = 'PV' ");
		sb.append("and o.processoTrf.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ");
		sb.append("AND EXISTS (SELECT s.processoTrf FROM SessaoPautaProcessoTrf s  ");
		sb.append("WHERE s.processoTrf = o.processoTrf AND s.dataExclusaoProcessoTrf IS NULL ");
		sb.append("AND s.sessao.dataRealizacaoSessao IS NOT NULL  ");
		sb.append("AND (s.sessao.dataRegistroEvento is not null OR s.sessao.dataFechamentoSessao is not null)) ");
		
		if(filtro.getSessao().getContinua()){
			sb.append("AND o.sessaoPautaProcessoTrf.processoTrf.pautaVirtual IS true ");
		}else{
			sb.append("AND o.sessaoPautaProcessoTrf.processoTrf.pautaVirtual IS false ");
		}
		
		if(filtro.getOrgaoJulgador() != null) {
			sb.append("	AND o.processoTrf.orgaoJulgador = :orgaoJulgador ");
		}
		if(filtro.getTipoVoto() != null) {
			sb.append("	AND o.processoTrf.idProcessoTrf in ( select voto.processoTrf.idProcessoTrf from SessaoProcessoDocumentoVoto voto where voto.tipoVoto = :tipoVoto and voto.liberacao = true ) ");
		}
		if(filtro.getNumeroSequencia() != null) {
			sb.append("	AND o.processoTrf.numeroSequencia = :numeroSequencia ");
		}
		if(filtro.getDigitoVerificador() != null) {
			sb.append("		AND o.processoTrf.numeroDigitoVerificador = :numeroDigitoVerificador ");
		}
		if(filtro.getAno() != null) {
			sb.append("		AND o.processoTrf.ano = :ano "); 
		}
		if(filtro.getRespectivoTribunal() != null && filtro.getRespectivoTribunal().trim().length() > 0){
			sb.append("		AND o.processoTrf.numeroOrgaoJustica = :numeroOrgaoJustica");
		}
		if (filtro.getNumeroOrigem() != null) {
			sb.append("		AND o.processoTrf.numeroOrigem = :numeroOrigem");
		}
			
		if (filtro.getClasseJudicial() != null && filtro.getClasseJudicial().trim().length() > 0 ) {
			sb.append("		AND LOWER(to_ascii(o.processoTrf.classeJudicial.classeJudicial)) LIKE LOWER(to_ascii( :classeJudicial )) )) ");
		}
		if (filtro.getNomeParte() != null && filtro.getNomeParte().trim().length() > 0 ) {
			sb.append(" AND EXISTS ( ");
			sb.append(" SELECT 1 ");
			sb.append(" FROM ProcessoParte pp " );
			sb.append(" WHERE pp.inSituacao = 'A' ");
			sb.append(" AND pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf ");
			sb.append(" AND LOWER(to_ascii(pp.pessoa.nome)) LIKE LOWER(to_ascii( :nomeParte ))  ) ");
		}
		if(filtro.getAssunto() != null && filtro.getAssunto().trim().length() > 0) {
			sb.append(" AND EXISTS ( ");
			sb.append(" SELECT 1 FROM ProcessoAssunto pa ");
			sb.append(" INNER JOIN pa.assuntoTrf ass ");
			sb.append(" WHERE pa.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf ");
			sb.append(" AND (ass.codAssuntoTrf = :assunto ");
			sb.append(" OR LOWER(to_ascii(ass.assuntoTrf)) LIKE LOWER(to_ascii( :assunto )) )) ");
		}
		
			
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgadorColegiado", Authenticator.getOrgaoJulgadorColegiadoAtual());
		
		if(filtro.getOrgaoJulgador() != null) {
			q.setParameter("orgaoJulgador", filtro.getOrgaoJulgador());
		}

		if(filtro.getTipoVoto() != null) {
			q.setParameter("tipoVoto", filtro.getTipoVoto());
		}
		
		if(filtro.getNumeroSequencia() != null) {
			q.setParameter("numeroSequencia", filtro.getNumeroSequencia());
		}
		
		if(filtro.getDigitoVerificador() != null) {
			q.setParameter("numeroDigitoVerificador", filtro.getDigitoVerificador());
		}
		
		if(filtro.getAno() != null) {
			q.setParameter("ano", filtro.getAno());
		}
		
		if(StringUtils.isNotBlank(filtro.getRamoJustica()) && StringUtils.isNotBlank(filtro.getRespectivoTribunal())){
			q.setParameter("numeroOrgaoJustica", Integer.parseInt(filtro.getRamoJustica() + filtro.getRespectivoTribunal()));
		}
		
		if (filtro.getNumeroOrigem() != null) { 
			q.setParameter("numeroOrigem", filtro.getNumeroOrigem());
		}

		if (filtro.getClasseJudicial() != null && filtro.getClasseJudicial().trim().length() > 0 ) { 
			q.setParameter("classeJudicial", filtro.getClasseJudicial());
		}

		if (filtro.getNomeParte() != null && filtro.getNomeParte().trim().length() > 0 ) { 
			q.setParameter("nomeParte", filtro.getNomeParte());
		}

		if(filtro.getAssunto() != null && filtro.getAssunto().trim().length() > 0) { 
			q.setParameter("assunto", filtro.getAssunto());
		}


		return q.getResultList();		
	}

}
