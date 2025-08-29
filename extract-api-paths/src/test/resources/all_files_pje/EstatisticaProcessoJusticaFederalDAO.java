package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.EstatisticaProcessoJusticaFederalQuery;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;

/**
 * Classe com as consultas a entidade de Competencia.
 * 
 * @author Daniel
 * 
 */
@Name(EstatisticaProcessoJusticaFederalDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class EstatisticaProcessoJusticaFederalDAO extends GenericDAO implements Serializable,
		EstatisticaProcessoJusticaFederalQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estatisticaProcessoJusticaFederalDAO";

	/**
	 * Pega uma seção, e devolve a lista de orgaoJulgadores.
	 * 
	 * @param secao
	 *            , dataInicio, dataFim
	 * @return Lista de orgaoJulgadores daquela seção
	 */
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> listOrgaoJulgadoresSecao(String secao) {
		List<OrgaoJulgador> resultList = null;
		Query q = getEntityManager().createQuery(LIST_ORGAO_JULGADOR_SECAO_QUERY);
		q.setParameter(QUERY_PARAMETER_SECAO, secao);
		resultList = q.getResultList();
		return resultList;
	}

	/**
	 * Pega uma seção, e devolve a lista de orgaoJulgadores.
	 * 
	 * @param secao
	 *            , dataInicio, dataFim
	 * @return Lista de orgaoJulgadores daquela seção
	 */
	@SuppressWarnings("unchecked")
	public List<Competencia> listCompetenciasOrgaoJulgador(OrgaoJulgador oj) {
		List<Competencia> resultList = null;
		Query q = getEntityManager().createQuery(LIST_COMPETENCIA_ORGAO_JULGADOR_QUERY);
		q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, oj);

		resultList = q.getResultList();
		return resultList;
	}

	public long qtdProcessosVara(String sj, OrgaoJulgador oj, Pessoa p) {
		Long resultList;
		Query q = getEntityManager().createQuery(QTD_PROCESSOS_VARA_QUERY);
		q.setParameter(QUERY_PARAMETER_SECAO, sj);
		q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, oj);
		q.setParameter(QUERY_PARAMETER_PESSOA, p);
		q.setParameter(QUERY_PARAMETER_EVENTO1, ParametroUtil.instance().getEventoArquivamentoDefinitivoProcessual()
				.getCodEvento());
		q.setParameter(QUERY_PARAMETER_EVENTO2, ParametroUtil.instance().getEventoBaixaDefinitivaProcessual()
				.getCodEvento());
		q.setParameter(QUERY_PARAMETER_EVENTO3, ParametroUtil.instance().getEventoReativacaoProcessual().getCodEvento());
		q.setParameter(QUERY_PARAMETER_EVENTO4, ParametroUtil.instance().getEventoRemetidoTrfProcessual()
				.getCodEvento());
		q.setParameter(QUERY_PARAMETER_EVENTO5, ParametroUtil.instance().getEventoRecebimentoProcessual()
				.getCodEvento());

		resultList = EntityUtil.getSingleResult(q);
		return resultList;
	}

	public Processo buscaProcessoSegredoJustica(ProcessoTrf processo, int idUsuario) {
		Processo proc = null;
		Query q = getEntityManager().createQuery(PROCESSO_SEGREDO_JUSTICA_QUERY);
		q.setParameter(QUERY_PARAMETER_PROCESSO, processo.getIdProcessoTrf());
		q.setParameter(QUERY_PARAMETER_PESSOA, idUsuario);

		proc = EntityUtil.getSingleResult(q);
		return proc;
	}

	public Processo buscaProcessoTextoSigiloso(ProcessoTrf processo, int idUsuario) {
		Processo proc = null;
		Query q = getEntityManager().createQuery(PROCESSO_TEXTO_SIGILOSO_QUERY);
		q.setParameter(QUERY_PARAMETER_PROCESSO, processo.getIdProcessoTrf());
		q.setParameter(QUERY_PARAMETER_PESSOA, idUsuario);

		proc = EntityUtil.getSingleResult(q);
		return proc;
	}

	@SuppressWarnings("unchecked")
	public List<UsuarioLogin> listUsuariosVisibilidadeSegredo(ProcessoTrf processo) {
		Query q = getEntityManager().createQuery(LIST_USUARIO_VISIBILIDADE_SEGREDO_QUERY);
		q.setParameter(QUERY_PARAMETER_PROCESSO, processo.getIdProcessoTrf());
		List<UsuarioLogin> resultList = q.getResultList();
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<Usuario> listJuizesPorOJ(OrgaoJulgador orgaoJulgador) {
		Query q = getEntityManager().createQuery(LIST_JUIZ_FEDERAL_POR_ORGAO_JULGADOR_QUERY);
		q.setParameter(QUERY_PARAMETER_ORGAO_JULGADOR, orgaoJulgador);

		List<Usuario> result = q.getResultList();
		return result;
	}
}