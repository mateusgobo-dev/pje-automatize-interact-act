/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoComposicao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;

@Name(SessaoPautaProcessoComposicaoDAO.NAME)
public class SessaoPautaProcessoComposicaoDAO extends BaseDAO<SessaoPautaProcessoComposicao> {

	public static final String NAME = "sessaoPautaProcessoComposicaoDAO";

	@Override
	public Integer getId(SessaoPautaProcessoComposicao e) {
		return e.getIdSessaoPautaProcessoComposicao();
	}

	@SuppressWarnings("unchecked")
	public List<SessaoPautaProcessoComposicao> findBySessaoPautaProcessoTrf(SessaoPautaProcessoTrf sessaoPautaProcTrf, Boolean somentePresentes){
		
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(" select sppc from SessaoPautaProcessoComposicao sppc ");
		sbQuery.append(" where sppc.sessaoPautaProcessoTrf.idSessaoPautaProcessoTrf = :sessao ");
		if (somentePresentes){
			sbQuery.append(" and sppc.presente = true "); 
		}
		sbQuery.append(" order by sppc.tipoAtuacaoMagistrado, sppc.magistradoPresente.nome ");
		
		Query query = getEntityManager().createQuery(sbQuery.toString());   
		query.setParameter("sessao", sessaoPautaProcTrf.getIdSessaoPautaProcessoTrf());
		return query.getResultList();
	}

	/**
	 * Altera a presenca do magistrado na sessao para todas as composicoes dos processos da sessao
	 * 
	 * @param sessaoComposicaoOrdem
	 */
    public void atualizarPresencaDoMagistradoNasComposicoesDosProcessosDaSessao(SessaoComposicaoOrdem sessaoComposicaoOrdem) {
   	 
    	StringBuilder cmd = new StringBuilder()
			.append(" update SessaoPautaProcessoComposicao sppc ")
			.append("    set sppc.presente = :presente ")
			.append("  where sppc.id in ( ")
			.append("             select sppc2.id ")
			.append("               from SessaoPautaProcessoComposicao sppc2 ")
			.append("               join sppc2.sessaoPautaProcessoTrf sppt ")
			.append("              where sppc2.definidoPorUsuario = false ")
			.append("                and sppc2.orgaoJulgador = :orgaoJulgador ")
			.append("                and sppt.sessao = :sessao ")
			.append("        )");
		 
		Query query = getEntityManager().createQuery(cmd.toString());   
		query.setParameter("sessao", sessaoComposicaoOrdem.getSessao());
		query.setParameter("orgaoJulgador", sessaoComposicaoOrdem.getOrgaoJulgador());
		query.setParameter("presente", sessaoComposicaoOrdem.getPresenteSessao());
		
		query.executeUpdate();
    }
	
    /**
     * Atualiza o magistrado 'presente' das composicoes dos processos da sessão passada por parâmetro.
     *
     * @param sessaoComposicaoOrdem SessaoComposicaoOrdem
     */
    public void atualizarMagistradoPresenteNasComposicoesDosProcessosDaSessao(SessaoComposicaoOrdem sessaoComposicaoOrdem) {
      	 
    	StringBuilder cmd = new StringBuilder()
			.append(" update SessaoPautaProcessoComposicao sppc ")
			.append("    set sppc.magistradoPresente = :magistradoPresente ")
			.append("  where sppc.id in ( ")
			.append("             select sppc2.id ")
			.append("               from SessaoPautaProcessoComposicao sppc2 ")
			.append("               join sppc2.sessaoPautaProcessoTrf sppt ")
			.append("              where sppc2.definidoPorUsuario = false ")
			.append("                and sppc2.orgaoJulgador = :orgaoJulgador ")
			.append("                and sppt.sessao = :sessao ")
			.append("        )");
		 
		Query query = getEntityManager().createQuery(cmd.toString());   
		query.setParameter("sessao", sessaoComposicaoOrdem.getSessao());
		query.setParameter("orgaoJulgador", sessaoComposicaoOrdem.getOrgaoJulgador());
		query.setParameter("magistradoPresente", sessaoComposicaoOrdem.getMagistradoPresenteSessao());
		
		query.executeUpdate();
    }

    /**
     * Atualiza o magistrado 'substituto' das composicoes dos processos da sessão passada por parâmetro.
     *
     * @param sessaoComposicaoOrdem SessaoComposicaoOrdem
     */
    public void atualizarMagistradoSubstitutoNasComposicoesDosProcessosDaSessao(SessaoComposicaoOrdem sessaoComposicaoOrdem) {
      	 
    	StringBuilder cmd = new StringBuilder()
			.append(" update SessaoPautaProcessoComposicao sppc ")
			.append("    set sppc.magistradoPresente = :magistradoSubstituto ")
			.append("  where sppc.id in ( ")
			.append("             select sppc2.id ")
			.append("               from SessaoPautaProcessoComposicao sppc2 ")
			.append("               join sppc2.sessaoPautaProcessoTrf sppt ")
			.append("              where sppc2.definidoPorUsuario = false ")
			.append("                and sppc2.orgaoJulgador = :orgaoJulgador ")
			.append("                and sppt.sessao = :sessao ")
			.append("        )");
		 
		Query query = getEntityManager().createQuery(cmd.toString());   
		query.setParameter("sessao", sessaoComposicaoOrdem.getSessao());
		query.setParameter("orgaoJulgador", sessaoComposicaoOrdem.getOrgaoJulgador());
		query.setParameter("magistradoSubstituto", sessaoComposicaoOrdem.getMagistradoSubstitutoSessao());
		
		query.executeUpdate();
    }
    
    /**
	 * Metodo que retorna lista dos Órgãos Julgadores Impedido/Suspeicao por Sessao Pauta Processo
	 * @param sessaoPautaProcessoTrf
	 */
	public Set<Integer> recuperarOrgaosJulgadoresDaComposicaoJulgamentoImpedidoSuspeicaoPorSessaoPautaProcessoTrf(
			SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		StringBuilder sql = new StringBuilder()
				.append(" select sppc.orgaoJulgador.idOrgaoJulgador from SessaoPautaProcessoComposicao sppc ")
				.append(" where sppc.sessaoPautaProcessoTrf = :sessaoPautaProcessoTrf ")
				.append(" and sppc.impedidoSuspeicao = true ");
		
		Query query = getEntityManager().createQuery(sql.toString());
		query.setParameter("sessaoPautaProcessoTrf", sessaoPautaProcessoTrf);
		List<Integer> lista = query.getResultList();
		Set<Integer> retorno = new HashSet<Integer>();
		retorno.addAll(lista);
		return retorno;
	}
	
	public List<SessaoPautaProcessoComposicao> obterParticipacoesComposicaoProcessual(Sessao sessao, OrgaoJulgador orgaoJulgador) {
    	StringBuilder sql = new StringBuilder()
			.append(" SELECT sppc FROM SessaoPautaProcessoComposicao sppc ")
			.append(" WHERE sppc.sessaoPautaProcessoTrf.sessao = :sessao ")
			.append(" AND sppc.orgaoJulgador = :orgaoJulgador ");

		Query query = getEntityManager().createQuery(sql.toString());
		query.setParameter("sessao", sessao);
		query.setParameter("orgaoJulgador", orgaoJulgador);
		
		@SuppressWarnings("unchecked")
		List <SessaoPautaProcessoComposicao> resultList = query.getResultList();
		
		return resultList;
	}
	
	/**
	 * Metodo que atualiza os votos impedidos/suspeicao no painel do secretario da sessao realizado pelo
	 * magistrado na votacao vogal
	 * @param idsOrgaoJulgadoresImpedidos
	 * @param sessaoPautaProcessoTrf
	 */
	public void atualizaVotosImpedidos(List<Integer> idsOrgaoJulgadoresImpedidos,
			SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		StringBuilder cmd = new StringBuilder()
				.append(" UPDATE  SessaoPautaProcessoComposicao sppc ")
				.append(" SET sppc.impedidoSuspeicao = true ")
				.append(" WHERE sppc.sessaoPautaProcessoTrf = :sessaoPautaProcessoTrf ")
				.append(" AND sppc.orgaoJulgador.idOrgaoJulgador IN (:idsOrgaoJulgadoresImpedidos) ");
		
		Query query = getEntityManager().createQuery(cmd.toString());   
		query.setParameter("sessaoPautaProcessoTrf", sessaoPautaProcessoTrf);
		query.setParameter("idsOrgaoJulgadoresImpedidos", idsOrgaoJulgadoresImpedidos);
		query.executeUpdate();
	}
	
}
