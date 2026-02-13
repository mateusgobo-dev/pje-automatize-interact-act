package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.identidade.Papel;

/**
 * Classe responsavel por manipular no banco de dados as operacoes que envolvem a entidade de Localizacao
 */
@Name("localizacaoDAO")
public class LocalizacaoDAO extends BaseDAO<Localizacao>{

	@Override
	public Integer getId(Localizacao e){
		return e.getIdLocalizacao();
	}

	public Localizacao findByName(String identificador) {
		String query = "SELECT l FROM Localizacao AS l WHERE lower(l.localizacao) = lower(:identificador)";
		Query q = entityManager.createQuery(query);
		q.setParameter("identificador", identificador);
		try{
			return (Localizacao) q.getSingleResult();
		}catch(NoResultException e){
			return null;
		}
	}

	/**
	 * Recupera as localizações vinculadas a uma dada pessoa.
	 * 
	 * @param p a pessoa cujas localizações se pretende obter
	 * @param first o primeiro resultado que se pretende obter, ou null
	 * @param max o máximo de resultados que se pretende obter por chamada ao método, ou null para todos.
	 * @return a lista de localizações.
	 */
	@SuppressWarnings("unchecked")
	public List<Localizacao> getLocalizacoesPessoais(Pessoa p, Integer first, Integer max) {
		String query = "SELECT u.localizacao FROM UsuarioLocalizacao AS u WHERE u.usuario = :usuario";
		Query q = entityManager.createQuery(query);
		q.setParameter("usuario", (Usuario) p);
		if(first != null && first > 0){
			q.setFirstResult(first);
		}
		if(max != null && max > 0){
			q.setMaxResults(max);
		}
		return q.getResultList();
	}

	/**
	 * Recupera todas as localizacoes 'filhas' de uma determinada localizacao.
	 * 
	 * @param idLocalizacaoPai localizacao pai
	 * @return Lista de localizacoes filhas
	 */
	public List<Localizacao> obterFilhas(List<Integer> idsLocaliacoesPais){
		return this.getArvoreDescendente(idsLocaliacoesPais, true);
	}

	/**
	 * Recupera todas as localizacoes 'pais' de uma determinada localizacao.
	 * 
	 * @param idLocalizacaoFilha localizacao filha
	 * @return Lista de ID's de localizacoes ancestrais.
	 */
	public List<Integer> obterIdsAncestrais(List<Integer> idsLocalizacoesFilhas){
		List<Integer> localizacoesAncestraisIds = new ArrayList<Integer>();
		List<Localizacao> localizacoesAncestrais = this.getArvoreAscendente(idsLocalizacoesFilhas, true);
		if(CollectionUtilsPje.isNotEmpty(localizacoesAncestrais)) {
			for (Localizacao localizacao : localizacoesAncestrais) {
				localizacoesAncestraisIds.add(localizacao.getIdLocalizacao());
			}
		}
    	return localizacoesAncestraisIds;
	}

	/**
	 * [PJEII-1571] Obtém a localização de um usuário em um determinado papel.
	 * @param usuario
	 * @param papel
	 * @return Localizacao por usuario e papel
	 */
	public Localizacao getLocalizacaoPorUsuarioPorPapel(Usuario usuario, Papel papel) {
		String query = "SELECT uloc.localizacaoFisica FROM UsuarioLocalizacao uloc WHERE uloc.papel = :papel AND uloc.usuario = :usuario";
		Query q = entityManager.createQuery(query);
		q.setParameter("papel", papel);
		q.setParameter("usuario", usuario);
		return ((Localizacao)q.getSingleResult());
	}
	
	/**
	 * Pesquisa as localizações vinculadas ao endereço passado por parâmetro.
	 * @param	idEndereco
	 * @return	lista de localizações vinculadas ao id_endereco passados por parâmetro.
	 */
	@SuppressWarnings("unchecked")
	public List<Localizacao> obterLocalizacoesComEndereco(Integer idEndereco){
		List<Localizacao> localizacoes = new ArrayList<Localizacao>();
		StringBuilder hql = new StringBuilder();
		
		hql.append("SELECT loc FROM Localizacao loc ");
		hql.append("INNER JOIN loc.endereco ender ");
		hql.append("WHERE ender.idEndereco = :paramIdEndereco ");
		
		Query query = entityManager.createQuery(hql.toString());
		query.setParameter("paramIdEndereco", idEndereco);
		localizacoes = query.getResultList();
		
		return localizacoes;
	}
	
	/**
 	 * metodo responsavel por persistir o objeto Localizacao.
 	 * caso o mesmo ja esteja carregado na sessao, realiza update.
 	 * caso nao exista, salva uma nova instancia.
 	 * @param localizacao
 	 * @throws Exception
 	 */
 	public void salvarLocalizacao(Localizacao localizacao) throws Exception{
 		EntityManager em = EntityUtil.getEntityManager();
 		if(em.contains(localizacao)) {
 			em.merge(localizacao);
 		} else {
 			em.persist(localizacao);
 		}
 		em.flush();
 	}
	
	/**
	 * Método responsável por retornar as localizações do servidor, salvo as vinculadas ao papel de magistrado.
	 * Dadas as restrições de ojc, oj e localizacao física dadas
	 * 
	 * @param idUsuario idUsuario Identificador do usuário.
	 * @return As localizações do servidor, salvo as vinculadas ao papel de magistrado.
	 */
	@SuppressWarnings("unchecked")
 	public List<Localizacao> getLocalizacaoServidorItems(Integer idUsuario, OrgaoJulgadorColegiado orgaoJulgadorColegiado, OrgaoJulgador orgaoJulgador, Localizacao localizacaoFisica) {	
 		StringBuilder sb = new StringBuilder("SELECT o FROM UsuarioLocalizacaoMagistradoServidor o ")
 				.append("WHERE o.usuarioLocalizacao.usuario.idUsuario = :idUsuario ")
 				.append("AND o.vinculacaoUsuario IS NULL ")
 				.append("AND o.usuarioLocalizacao.papel != :papelMagistrado");
 		
		if(orgaoJulgadorColegiado != null) {
			sb.append(" AND o.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = :idOrgaoJulgadorColegiado ");
		}
		if(orgaoJulgador != null) {
			sb.append(" AND o.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador ");
		}

		if(localizacaoFisica != null && localizacaoFisica.getFaixaInferior() != null) {
			sb.append(" AND o.idUsuarioLocalizacaoMagistradoServidor IN (")
				.append(" SELECT ul.idUsuarioLocalizacao FROM UsuarioLocalizacao AS ul ")
				.append(" JOIN ul.localizacaoFisica loc ")
				.append(" WHERE loc.faixaInferior IS NOT NULL ")
				.append(" AND loc.faixaInferior >= :faixaInferiorLocalizacaoFisica ")
				.append(" AND loc.faixaSuperior IS NOT NULL ")
				.append(" AND loc.faixaSuperior <=  :faixaSuperiorLocalizacaoFisica")
				.append(" ) ");
		}
 		
		sb.append(" ORDER BY o.usuarioLocalizacao.localizacaoFisica.faixaInferior, o.usuarioLocalizacao.papel.nome, o.usuarioLocalizacao.idUsuarioLocalizacao");

 		Query query = getEntityManager().createQuery(sb.toString());
 		query.setParameter("idUsuario", idUsuario);
 		query.setParameter("papelMagistrado", ParametroUtil.instance().getPapelMagistrado());
 		if(orgaoJulgadorColegiado != null) {
 			query.setParameter("idOrgaoJulgadorColegiado", orgaoJulgadorColegiado.getIdOrgaoJulgadorColegiado());
 		}
 		if(orgaoJulgador != null) {
 			query.setParameter("idOrgaoJulgador", orgaoJulgador.getIdOrgaoJulgador());
 		}
 		if(localizacaoFisica != null && localizacaoFisica.getFaixaInferior() != null) {
 			query.setParameter("faixaInferiorLocalizacaoFisica", localizacaoFisica.getFaixaInferior());
 			query.setParameter("faixaSuperiorLocalizacaoFisica", localizacaoFisica.getFaixaSuperior());
 		}
		
		return (List<Localizacao>) query.getResultList();
 	}
	
	public List<Localizacao> getLocalizacaoServidorItems(Integer idUsuario) {
		return this.getLocalizacaoServidorItems(idUsuario, null, null, null);
	}
	
	
	public List<Localizacao> getArvoreDescendente(List<Integer> idsLocalizacoes, boolean incluirNoRaiz) {
		return getArvore(idsLocalizacoes, incluirNoRaiz, false, false);
	}

	public List<Localizacao> getArvoreAscendente(List<Integer> idsLocalizacoes, boolean incluirNoRaiz) {
		return getArvore(idsLocalizacoes, incluirNoRaiz, true, false);
	}

	@SuppressWarnings("unchecked")
	/**
	 * Método responsável por recuperar a árvore de Localizações a partir do código identificador da Localização pai
	 * @param idsLocalizacoes Códigos identificadores das Localizações paradigma
	 * @param incluirNoRaiz Informa se a Localização pai deve ser incluída no retorno
	 * @param ascendente Informa se a pesquisa na árvore é de forma ascendente (recuperar os pais) ou descendente (recuperar os filhos) 
	 * @param apenasFolhas Informa se a pesquisa deve recuperar apenas as Localizações folhas
	 * @return
	 */
	private List<Localizacao> getArvore(List<Integer> idsLocalizacoes, boolean incluirNoRaiz, boolean ascendente, boolean apenasFolhas, boolean removeFolhas) {
		String operadorInicial = (ascendente ? "<" : ">") + (incluirNoRaiz ? "=" : "");
		String operadorFinal = (ascendente ? ">" : "<") + (incluirNoRaiz ? "=" : "");;
		List<Localizacao> localizacoes = new ArrayList<Localizacao>(0);
		StringBuilder sql = new StringBuilder();

		sql.append("select f.id_localizacao, f.id_localizacao_pai, f.ds_localizacao, f.nr_faixa_inferior, f.nr_faixa_superior ");
		sql.append("from ( ");
		sql.append(" SELECT MIN(l1.nr_faixa_inferior) nr_faixa_inferior_consolidado, MAX(l1.nr_faixa_superior) nr_faixa_superior_consolidado ");
		sql.append("FROM core.tb_localizacao l1 WHERE id_localizacao IN (:idsLocalizacoes)");
		sql.append(" ) loc_consolidadas ");
		sql.append("inner join core.tb_localizacao f on ( ");
		sql.append(String.format(" (f.nr_faixa_inferior %s loc_consolidadas.nr_faixa_inferior_consolidado "
				+ " and f.nr_faixa_superior %s loc_consolidadas.nr_faixa_superior_consolidado)" , operadorInicial, operadorFinal));
		if(incluirNoRaiz) {
			sql.append(" OR (id_localizacao IN (:idsLocalizacoes))");
		}
		sql.append(")");

		if (apenasFolhas) {
			sql.append(" and f.nr_faixa_inferior + 1 = f.nr_faixa_superior ");	
		}else if(removeFolhas) {
			sql.append(" and f.nr_faixa_inferior + 1 != f.nr_faixa_superior ");				
		}
		
		sql.append("where f.in_ativo = true ");
		sql.append("order by f.nr_faixa_inferior");
		Query q = entityManager.createNativeQuery(sql.toString());
		q.setParameter("idsLocalizacoes", idsLocalizacoes);
		
		List<Object[]> result = q.getResultList();
		for (Object[] r: result) {
			Localizacao loc = preencherLocalizacao(r);
			localizacoes.add(loc);
		}
		return localizacoes;
	}

	private List<Localizacao> getArvore(List<Integer> idsLocalizacoes, boolean incluirNoRaiz, boolean ascendente, boolean apenasFolhas) {
		return this.getArvore(idsLocalizacoes, incluirNoRaiz, ascendente, apenasFolhas, false);
	}
	
	private Localizacao preencherLocalizacao(Object[] r) {
		Localizacao loc = new Localizacao();
		loc.setIdLocalizacao((Integer)r[0]);
		if(r[1] != null) {
			Localizacao locPai = new Localizacao();
			locPai.setIdLocalizacao((Integer)r[1]);
			loc.setLocalizacaoPai(locPai);
		}
		loc.setLocalizacao((String)r[2]);
		loc.setFaixaInferior((Integer)r[3]);
		loc.setFaixaSuperior((Integer)r[4]);
		loc.setAtivo(true);
		return loc;
	}
	
	/**
	 * Método responsável por recuperar as Localizações folha
	 * @param idLocalizacao Código identificador da Localização pai
	 * @return Lista de Localizações folha
	 */
	public List<Localizacao> getLocalizacoesFolha(List<Integer> idsLocalizacoes) {
		return this.getArvore(idsLocalizacoes, false, false, true);
	}
	
	/**
	 * Método responsável por recuperar as Localizações descendentes da lista dada, removendo os nós folha da árvore
	 * @param idsLocalizacoes Código identificador das Localizações pai
	 * @return Lista de Localizações folha
	 */
	public List<Localizacao> getLocalizacoesExcetoFolhas(List<Integer> idsLocalizacoes) {
		return this.getArvore(idsLocalizacoes, true, false, false, true);
	}
}