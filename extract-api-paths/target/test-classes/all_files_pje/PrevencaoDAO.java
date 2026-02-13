package br.com.infox.pje.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

@Name(PrevencaoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class PrevencaoDAO extends GenericDAO {

	public static final String NAME = "PrevencaoDAO";
	
	@In
	private ProcessoTrfDAO processoTrfDAO;
	
	
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> buscarProcessosPrevencaoGeralPorNomeDocumento(
			Set<Integer> assuntos, 
			Set<Integer> idsPessoaPoloAtivo, 
			Set<Integer> idsPessoaPoloPassivo,
			Integer idProcessoTrfAtual) {
		
		if (seNuloOuVazio(assuntos) || seNuloOuVazio(idsPessoaPoloAtivo) || seNuloOuVazio(idsPessoaPoloPassivo)) {
			return new ArrayList<>();
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append("distinct p.id_processo ");
		sql.append("from ");
		sql.append("client.tb_processo_trf pt ");
		sql.append("inner join core.tb_processo p on ");
		sql.append("p.id_processo = pt.id_processo_trf ");
		sql.append("inner join client.tb_processo_assunto a on ");
		sql.append("a.id_processo_trf = pt.id_processo_trf ");
		sql.append("inner join client.tb_processo_parte pa on ");
		sql.append("pa.id_processo_trf = pt.id_processo_trf ");
		sql.append("and pa.in_participacao = 'A' ");
		sql.append("inner join client.tb_tipo_parte tpa on ");
		sql.append("tpa.id_tipo_parte = pa.id_tipo_parte ");
		sql.append("inner join client.tb_processo_parte pp on ");
		sql.append("pp.id_processo_trf = pt.id_processo_trf ");
		sql.append("and pp.in_participacao = 'P' ");
		sql.append("inner join client.tb_tipo_parte tpp on ");
		sql.append("tpp.id_tipo_parte = pp.id_tipo_parte ");
		sql.append("left join client.tb_pessoa_autoridade au on ");
		sql.append("au.id_pessoa_autoridade = pp.id_pessoa ");
		sql.append("where ");
		sql.append("p.nr_processo is not null ");
		sql.append("and pt.dt_distribuicao is not null ");
		sql.append("and pt.cd_processo_status = :processoStatus ");
		sql.append("and a.id_assunto_trf in (:assuntos) ");
		sql.append("and tpa.in_tipo_principal = true ");
		sql.append("and tpp.in_tipo_principal = true ");
		sql.append("and p.id_processo <> :idProcessoTrf ");
		sql.append("and ( (pa.id_pessoa in (:idsPessoaPoloAtivo) ");
		sql.append("and pp.id_pessoa in (:idsPessoaPoloPassivo)) ");
		sql.append("or (pa.id_pessoa in (:idsPessoaPoloPassivo) ");
		sql.append("and pp.id_pessoa in (:idsPessoaPoloAtivo)) ");
		sql.append("or (pa.id_pessoa in (:idsPessoaPoloAtivo) ");
		sql.append("and au.id_orgao_vinculacao in (:idsPessoaPoloPassivo)))");				
		
		Query query = getEntityManager().createNativeQuery(sql.toString());
		query.setParameter("assuntos", assuntos);
		query.setParameter("idProcessoTrf", idProcessoTrfAtual);
		query.setParameter("processoStatus", ProcessoStatusEnum.D.toString());
		query.setParameter("idsPessoaPoloAtivo", idsPessoaPoloAtivo);
		query.setParameter("idsPessoaPoloPassivo", idsPessoaPoloPassivo);

		List<Integer> idsProcessosPreventos = query.getResultList();
		ProcessoTrfDAO processoTrfDAOIn = ComponentUtil.getComponent(ProcessoTrfDAO.NAME); 
		return processoTrfDAOIn.getProcessos(idsProcessosPreventos);
	}


	private boolean seNuloOuVazio(Set<Integer> assuntos) {
		return assuntos == null || assuntos.isEmpty();
	}
	
	
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> buscarProcessosPrevencaoAcoesColetivas(
			Set<Integer> classes, 
			Set<Integer> assuntos, 
			Set<Integer> idsPessoaPoloPassivo,
			Integer idProcessoTrfAtual) {
		
		if (seNuloOuVazio(assuntos) || seNuloOuVazio(idsPessoaPoloPassivo)) {
			return new ArrayList<>();
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append("distinct p.id_processo ");
		sql.append("from ");
		sql.append("client.tb_processo_trf pt ");
		sql.append("inner join core.tb_processo p on ");
		sql.append("p.id_processo = pt.id_processo_trf ");
		sql.append("inner join client.tb_processo_assunto a on ");
		sql.append("a.id_processo_trf = pt.id_processo_trf ");
		sql.append("inner join client.tb_processo_parte pp on ");
		sql.append("pp.id_processo_trf = pt.id_processo_trf ");
		sql.append("and pp.in_participacao = 'P' ");
		sql.append("inner join client.tb_tipo_parte tpp on ");
		sql.append("tpp.id_tipo_parte = pp.id_tipo_parte ");
		sql.append("left join client.tb_pessoa_autoridade au on ");
		sql.append("au.id_pessoa_autoridade = pp.id_pessoa ");
		sql.append("where ");
		sql.append("p.nr_processo is not null ");
		sql.append("and pt.dt_distribuicao is not null ");
		sql.append("and pt.cd_processo_status = :processoStatus ");
		sql.append("and pt.id_classe_judicial in (:classes) ");
		sql.append("and a.id_assunto_trf in (:assuntos) ");
		sql.append("and tpp.in_tipo_principal = true ");
		sql.append("and p.id_processo <> :idProcessoTrf ");
		sql.append("and ( pp.id_pessoa in (:idsPessoaPoloPassivo) ");
		sql.append("or au.id_orgao_vinculacao in (:idsPessoaPoloPassivo))");
		
		Query query = getEntityManager().createNativeQuery(sql.toString());
		query.setParameter("classes", classes);
		query.setParameter("assuntos", assuntos);
		query.setParameter("idProcessoTrf", idProcessoTrfAtual);
		query.setParameter("processoStatus", ProcessoStatusEnum.D.toString());
		query.setParameter("idsPessoaPoloPassivo", idsPessoaPoloPassivo);

		List<Integer> idsProcessosPreventos = query.getResultList();
		ProcessoTrfDAO processoTrfDAOIn = ComponentUtil.getComponent(ProcessoTrfDAO.NAME); 
		return processoTrfDAOIn.getProcessos(idsProcessosPreventos);
		
	}
	
	public String buscarProcessoReferencia(Integer idProcessoTrfAtual) {
		String procRef = null;
		if (idProcessoTrfAtual != null) {
			StringBuilder sql = new StringBuilder();
			sql.append(" SELECT pt.ds_proc_referencia");
			sql.append(" FROM client.tb_processo_trf pt");
			sql.append(" WHERE pt.id_processo_trf = :idProcesso");

			Query query = getEntityManager().createNativeQuery(sql.toString());
			query.setParameter("idProcesso", idProcessoTrfAtual);

			try {
				procRef = (String) query.getSingleResult();
			} catch (NoResultException nre) {
				return null;
			}
			return procRef;
		}
		return null;
	}
}
