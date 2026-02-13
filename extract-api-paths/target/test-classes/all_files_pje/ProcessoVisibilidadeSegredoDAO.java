/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoVisibilidadeSegredo;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * @author cristof
 * 
 */
@Name("processoVisibilidadeSegredoDAO")
public class ProcessoVisibilidadeSegredoDAO extends BaseDAO<ProcessoVisibilidadeSegredo>{

	private static final String ID_USUARIO = "idUsuario";
	private static final String ID_PROCESSO = "idProcesso";
	private static final String ID_PROCURADORIA = "idProcuradoria";
	@Override
	public Integer getId(ProcessoVisibilidadeSegredo e){
		return e.getIdProcessoVisibilidadeSegredo();
	}

	public boolean visivel(ProcessoTrf processoJudicial, Usuario u) {
		return visivel(processoJudicial, u, null);
	}
	
	public boolean visivel(ProcessoTrf processoJudicial, Usuario u, Procuradoria procuradoria) {
		StringBuilder str = new StringBuilder("SELECT EXISTS (SELECT 1 FROM ProcessoVisibilidadeSegredo pvs WHERE pvs.processo.idProcesso = :idProcesso");
		
		if (procuradoria == null) {
			str.append(" AND pvs.idPessoa = :idUsuario");
		} else {
			str.append(" AND ((pvs.procuradoria.idProcuradoria = :idProcuradoria");
			str.append(" AND (EXISTS( SELECT 1 FROM PessoaProcuradoria pp WHERE pp.procuradoria = pvs.procuradoria");
			str.append(" AND pp.pessoa.idUsuario = :idUsuario)");
			str.append(" OR EXISTS( SELECT 1 FROM PessoaAssistenteProcuradoriaLocal papl WHERE papl.procuradoria = pvs.procuradoria");
			str.append(" AND papl.usuario.idUsuario = :idUsuario)))");
			str.append(" OR pvs.idPessoa = :idUsuario)");
		}
		str.append(") FROM ProcessoVisibilidadeSegredo");
		
		Query q = entityManager.createQuery(str.toString());
		q.setParameter(ID_PROCESSO, processoJudicial.getIdProcessoTrf());
		q.setParameter(ID_USUARIO, u.getIdUsuario());
		
		if (procuradoria != null) {
			q.setParameter(ID_PROCURADORIA, procuradoria.getIdProcuradoria());
		}
		
		return EntityUtil.getSingleResult(q);
	}


	public void limpaRegistros(ProcessoTrf processo) {
		String query = "DELETE FROM ProcessoVisibilidadeSegredo AS pvs WHERE pvs.processo.idProcesso = :idProcesso";
		Query q = entityManager.createQuery(query);
		q.setParameter(ID_PROCESSO, processo.getProcesso().getIdProcesso());
		q.executeUpdate();
	}
	
	public long contagemVisualizadores(ProcessoTrf processo){
		String query = "SELECT COUNT(pvs.idProcessoVisibilidadeSegredo) FROM ProcessoVisibilidadeSegredo AS pvs " +
				"	WHERE pvs.processo.idProcesso = :idProcesso ";
		Query q = entityManager.createQuery(query);
		q.setParameter(ID_PROCESSO, processo.getProcesso().getIdProcesso());
		q.setMaxResults(1);
		Number cont = (Number) q.getSingleResult();
		return cont.longValue();
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoVisibilidadeSegredo> recuperaVisualizadores(ProcessoTrf processo, Integer first, Integer max) {
		Query q = entityManager.createQuery("SELECT pvs FROM ProcessoVisibilidadeSegredo AS pvs WHERE pvs.processo.idProcesso = :idProcesso");
		q.setParameter(ID_PROCESSO, processo.getProcesso().getIdProcesso());
		if(first != null && first.intValue() >= 0){
			q.setFirstResult(first);
		}
		if(max != null && max.intValue() > 1){
			q.setMaxResults(max);
		}
		return q.getResultList();
	}
	
	public  List <ProcessoVisibilidadeSegredo> recuperaProcessoVisibilidadeSegredoPessoaProcessoSemProcuradoria(Pessoa pessoa, ProcessoTrf processo) {
		String query = "SELECT pvs FROM ProcessoVisibilidadeSegredo AS pvs " +
				"	WHERE pvs.procuradoria.idProcuradoria is null "
				+ "		AND pvs.processo.idProcesso = :idProcesso " +
				"		AND pvs.pessoa.idUsuario = :idUsuario ";
		Query q = entityManager.createQuery(query);
		q.setParameter(ID_USUARIO, pessoa.getIdUsuario());
		q.setParameter(ID_PROCESSO, processo.getIdProcessoTrf());
		
		try{
			return q.getResultList();
		}catch (NoResultException e){
			return null;
		}
	}

	/**
	 * Recupera todas as visibilidade segredo de processos, dado processo-procuradoria-pessoa
	 * 
	 * @param pessoa
	 * @param procuradoria
	 * @param processo
	 * @return
	 */
	public  List <ProcessoVisibilidadeSegredo> recuperaProcessoVisibilidadeSegredoPessoaProcuradoriaProcesso(Pessoa pessoa, Procuradoria procuradoria, ProcessoTrf processo) {
		String query = "SELECT pvs FROM ProcessoVisibilidadeSegredo AS pvs " +
				"	WHERE "
				+ "		pvs.processo.idProcesso = :idProcesso "
				+ "		AND pvs.procuradoria.idProcuradoria = :idProcuradoria " +
				"		AND pvs.pessoa.idUsuario = :idUsuario ";
		Query q = entityManager.createQuery(query);
		q.setParameter(ID_USUARIO, pessoa.getIdUsuario());
		q.setParameter(ID_PROCESSO, processo.getProcesso().getIdProcesso());
		q.setParameter(ID_PROCURADORIA, procuradoria.getIdProcuradoria());
		
		try{
			return q.getResultList();
		}catch (NoResultException e){
			return null;
		}
	}
	
	/**
	 * Recupera todas as visibilidades segredo de processos, dado processo-pessoa que não têm procuradoria relacionada
	 * 
	 * @param pessoa
	 * @param processo
	 * @return
	 */
	public ProcessoVisibilidadeSegredo recuperaProcessoVisibilidadeSegredo(Pessoa pessoa, ProcessoTrf processo) {
		String query = "SELECT pvs FROM ProcessoVisibilidadeSegredo AS pvs " +
				"	WHERE pvs.processo.idProcesso = :idProcesso " +
				"		AND pvs.pessoa.idUsuario = :idUsuario ";
		Query q = entityManager.createQuery(query);
		q.setParameter(ID_USUARIO, pessoa.getIdUsuario());
		q.setParameter(ID_PROCESSO, processo.getProcesso().getIdProcesso());
		try{
			return (ProcessoVisibilidadeSegredo) q.getSingleResult();
		}catch (NoResultException e){
			return null;
		}
	}
	
	public List <ProcessoVisibilidadeSegredo> recuperaProcessoVisibilidadeSegredoPessoaSemProcuradoria(Pessoa pessoa) {
		String query = "SELECT pvs FROM ProcessoVisibilidadeSegredo AS pvs " +
				"	WHERE pvs.procuradoria.idProcuradoria is null " +
				"		AND pvs.pessoa.idUsuario = :idUsuario ";
		Query q = entityManager.createQuery(query);
		q.setParameter(ID_USUARIO, pessoa.getIdUsuario());
		try{
			return q.getResultList();
		}catch (NoResultException e){
			return null;
		}
	}

	
	public List <ProcessoVisibilidadeSegredo> recuperaProcessoVisibilidadeSegredoPessoaProcuradoria(Pessoa pessoa, Procuradoria procuradoria) {
		String query = "SELECT pvs FROM ProcessoVisibilidadeSegredo AS pvs " +
				"	WHERE pvs.procuradoria.idProcuradoria = :idProcuradoria " +
				"		AND pvs.pessoa.idUsuario = :idUsuario ";
		Query q = entityManager.createQuery(query);
		q.setParameter(ID_USUARIO, pessoa.getIdUsuario());
		q.setParameter(ID_PROCURADORIA, procuradoria.getIdProcuradoria());
		try{
			return q.getResultList();
		}catch (NoResultException e){
			return null;
		}
	}
	/** 
	 * Resgata os processos que o usuário logado tem acesso de visualização
	 * Ex.: idProcessoTrf in (659, 660, 998)
	 * 
	 * @return uma <b>Criteria</b> contendo os ID's dos processos que o usuário faz parte.
	 */
	@SuppressWarnings("unchecked") 
	public List<Integer> recuperaVisibilidadeAtribuidaProcessoUsuarioLogado() {
		String query = "select o.processo.idProcesso from ProcessoVisibilidadeSegredo o where o.pessoa.idUsuario = :idPessoa";
		
		Query q = entityManager.createQuery(query);
		q.setParameter("idPessoa", Authenticator.getUsuarioLogado().getIdUsuario());
		return q.getResultList();
	}

	public void removerVisualizadorProcessoSigilosoNoOrgaoJulgador(Integer idUsuario, Integer idOrgaoJulgador) {
		StringBuilder sql = new StringBuilder();
		sql.append("delete from tb_proc_visibilida_segredo where id_proc_visibilidade_segredo in ( ");
		sql.append("select visualizador.id_proc_visibilidade_segredo from tb_proc_visibilida_segredo visualizador ");
		sql.append("inner join tb_processo_trf processo on visualizador.id_processo_trf = processo.id_processo_trf ");
		sql.append("where processo.cd_nivel_acesso = 5 ");
		sql.append("and processo.id_orgao_julgador = :idOrgaoJulgador ");
		sql.append("and visualizador.id_pessoa = :idUsuario ");
		sql.append(") ");
		Query query = getEntityManager().createNativeQuery(sql.toString());
		query.setParameter("idOrgaoJulgador", idOrgaoJulgador);
		query.setParameter("idUsuario", idUsuario);
		query.executeUpdate();
	}
}
