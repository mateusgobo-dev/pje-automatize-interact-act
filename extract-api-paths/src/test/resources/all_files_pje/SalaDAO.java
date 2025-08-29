package br.com.jt.pje.dao;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.query.SalaQuery;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.TipoAudiencia;

@Name(SalaDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class SalaDAO extends GenericDAO implements SalaQuery, Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "salaDAO";

	@SuppressWarnings("unchecked")
	public List<Sala> getSalaSessaoItems(){
		Query q = getEntityManager().createQuery(SALA_SESSAO_ITEMS_QUERY);
		List<Sala> list = q.getResultList();
		return list;
	}

	@SuppressWarnings("unchecked")
	public List<Sala> getSalaSessaoItemsByOrgaoJulgadorColegiado(OrgaoJulgadorColegiado ojc){
		Query q = getEntityManager().createQuery(SALA_SESSAO_ITEMS_BY_ORGAO_JULGADOR_COLEGIADO_QUERY);
		q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO, ojc);
		List<Sala> list = q.getResultList();
		return list;
	}

	@SuppressWarnings("unchecked")
	public List<Sala> findAll(){
		String queryStr = "SELECT o FROM Sala AS o WHERE o.ativo = true ";
		Query q = EntityUtil.getEntityManager().createQuery(queryStr);
		return q.getResultList();
	}

	public Object getId(Sala e) {
		return e.getIdSala();
	}

	public List<Sala> getSalaListByLocalizacao(Localizacao localizacao) {
		return getSalaListByLocalizacaoAndTipoAudiencia(localizacao, null); 
	}
	
	public List<Sala> getSalaListByLocalizacaoAndTipoAudiencia(Localizacao localizacao, TipoAudiencia tipoAudiencia) {
		String hql = "select s from Sala s ";

		if (tipoAudiencia != null) {
			hql+="  join s.tipoAudienciaList t where t.idTipoAudiencia = :idTipoAudiencia ";
		} else {
			hql+=" where 1=1 ";
		}
		hql+=" and s.ativo = true ";
		if (localizacao!=null){
			hql+=" and s.orgaoJulgador.localizacao.idLocalizacao = :idLocalizacao ";
			hql+=" union ";
			hql+=" select s from Sala s ";
			if (tipoAudiencia != null) {
				hql+="  join s.tipoAudienciaList t where t.idTipoAudiencia = :idTipoAudiencia ";
			} else {
				hql+=" where 1=1 ";
			}
			hql+=" and s.ativo = true ";
			hql+=" and s.orgaoJulgadorColegiado.localizacao.idLocalizacao = :idLocalizacao ";
		}
			
		hql+="  ORDER BY s.sala ";
		
		Query consulta= EntityUtil.getEntityManager().createQuery(hql);
		if (localizacao!=null){
			consulta.setParameter("idLocalizacao", localizacao.getIdLocalizacao());
		}
		if (tipoAudiencia!=null){
			consulta.setParameter("idTipoAudiencia", tipoAudiencia.getIdTipoAudiencia());
		}
		
		@SuppressWarnings("unchecked")
		List<Sala> resultList = consulta.getResultList();
		List<Sala> salasSemTipoAudiencia = getSalasSemTipoAudienciaByLocalizacao(localizacao);
		
		if (salasSemTipoAudiencia != null) {
			for (Sala sala : salasSemTipoAudiencia) {
				if (!resultList.contains(sala)){
					resultList.add(sala);
				}
			}
		}
		
		Collections.sort (resultList, new Comparator() {  
            public int compare(Object o1, Object o2) {  
                Sala p1 = (Sala) o1;  
                Sala p2 = (Sala) o2;  
                return p1.getSala().compareToIgnoreCase(p2.getSala());  
            }  
        });
		
		return resultList;		
	}
	
	public List<Sala> getSalaListByOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		Localizacao localizacao = null;
		if (orgaoJulgador != null) {
			localizacao = orgaoJulgador.getLocalizacao();
		}
		return getSalaListByLocalizacao(localizacao);
	}
	
	public List<Sala> getSalaListByOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		Localizacao localizacao = null;
		if (orgaoJulgadorColegiado != null) {
			localizacao = orgaoJulgadorColegiado.getLocalizacao();
		}
		return getSalaListByLocalizacao(localizacao);
	}
	
	public List<Sala> getSalasSemTipoAudienciaByLocalizacao(Localizacao localizacao) {
		StringBuilder hql = new StringBuilder("SELECT salaN FROM Sala as salaN where  salaN.ativo = true AND salaN.tipoSala = 'A' ");
		
		if (localizacao != null) {
			hql.append(" AND (salaN.orgaoJulgador.localizacao.idLocalizacao = :idLocalizacao) ");
		}
		
		hql.append(" AND salaN not in (SELECT sala FROM Sala as sala INNER JOIN sala.tipoAudienciaList as tipo ")
		.append("WHERE sala.ativo = true AND sala.tipoSala = 'A' ");
		
		if (localizacao != null) {
			hql.append(" AND (sala.orgaoJulgador.localizacao.idLocalizacao = :idLocalizacao )");
		}
		
		hql.append(") ORDER BY salaN.sala");
		
		Query q = EntityUtil.getEntityManager().createQuery(hql.toString());
		
		if (localizacao != null) {
			q.setParameter("idLocalizacao", localizacao.getIdLocalizacao());
		}
		
		@SuppressWarnings("unchecked")
		List<Sala> salasSemTipo = q.getResultList();
		return salasSemTipo;
	}
	
	@SuppressWarnings("unchecked")
	public List<Sala> getSalaByPeriodoAudienciaAndTipoAudiencia(Date dataInicial, Date dataFinal, TipoAudiencia tipoAudiencia, 
			OrgaoJulgador orgaoJulgador){
		//todas as salas de audiencia ativas
		StringBuilder hql = new StringBuilder("SELECT s FROM Sala s join s.salaHorarioList so left join s.tipoAudienciaList ta " +
				"where s.ativo = true AND s.tipoSala = 'A' AND so.ativo = true ");

		//de um determinado dia da semana
		hql.append(" AND so.diaSemana.idDiaSemana = :idDiaSemana");
		//que atendam em um determinado período
		hql.append(" AND :horaInicial between so.horaInicial and so.horaFinal");
		hql.append(" AND :horaFinal between so.horaInicial and so.horaFinal");
		//associadas a nenhum ou mais tipos de audiencia
		hql.append(" AND (ta.idTipoAudiencia = :idTipoAudiencia or ta.idTipoAudiencia is null)");
		
		//de um determinado órgão julgador
		if(orgaoJulgador != null && orgaoJulgador.getIdOrgaoJulgador() >0){
			hql.append(" AND s.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador");
		}
		
		Query q = EntityUtil.getEntityManager().createQuery(hql.toString());
		
		Calendar calendarInicial = Calendar.getInstance();
		calendarInicial.setTime(dataInicial);
		int idDiaSemana = calendarInicial.get(Calendar.DAY_OF_WEEK);
		q.setParameter("idDiaSemana", idDiaSemana);

		calendarInicial.set(0, 0, 0);
		Date horaInicial = calendarInicial.getTime();
		q.setParameter("horaInicial", horaInicial);

		Calendar calendarFinal = Calendar.getInstance();
		calendarFinal.setTime(dataFinal);
		calendarFinal.set(0, 0, 0);
		Date horaFinal= calendarFinal.getTime(); 
		q.setParameter("horaFinal", horaFinal);

		q.setParameter("idTipoAudiencia", tipoAudiencia.getIdTipoAudiencia());
		
		if(orgaoJulgador != null && orgaoJulgador.getIdOrgaoJulgador() >0){
			q.setParameter("idOrgaoJulgador", orgaoJulgador.getIdOrgaoJulgador());
		}
		
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Sala> recuperar(OrgaoJulgador orgaoJulgador, TipoAudiencia tipoAudiencia) {
		StringBuilder jpql = new StringBuilder("SELECT DISTINCT(s) FROM Sala s JOIN s.tipoAudienciaList ta ")
			.append("WHERE s.ativo = true AND s.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador AND ta.idTipoAudiencia = :idTipoAudiencia");
		
		Query q = getEntityManager().createQuery(jpql.toString());
		q.setParameter("idOrgaoJulgador", orgaoJulgador.getIdOrgaoJulgador());
		q.setParameter("idTipoAudiencia", tipoAudiencia.getIdTipoAudiencia());
		
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Sala> recuperarSalasAudienciaAtivas(Integer idTipoAudiencia, Set<Integer> idsOrgaoJulgador) {
		StringBuilder jpql = new StringBuilder("SELECT s FROM Sala s JOIN s.tipoAudienciaList t ")
			.append("WHERE s.ativo = true AND s.tipoSala = 'A' AND t.idTipoAudiencia = :idTipoAudiencia ")
			.append("AND s.orgaoJulgador.id IN (:idsOrgaoJulgador) ORDER BY s.sala");
		
		Query q = getEntityManager().createQuery(jpql.toString());
		q.setParameter("idTipoAudiencia", idTipoAudiencia);
		q.setParameter("idsOrgaoJulgador", idsOrgaoJulgador);
		
		return q.getResultList();
	}
}
