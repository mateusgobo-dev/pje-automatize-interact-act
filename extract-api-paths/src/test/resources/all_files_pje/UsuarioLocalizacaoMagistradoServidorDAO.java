/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Cargo;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.VinculacaoUsuario;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.TipoVinculacaoUsuarioEnum;
import br.jus.pje.nucleo.util.DateUtil;

/**
 * @author cristof
 * 
 */
@Name("usuarioLocalizacaoMagistradoServidorDAO")
public class UsuarioLocalizacaoMagistradoServidorDAO extends BaseDAO<UsuarioLocalizacaoMagistradoServidor> {

	
	@Override
	public Integer getId(UsuarioLocalizacaoMagistradoServidor e){
		return e.getIdUsuarioLocalizacaoMagistradoServidor();
	}

	@SuppressWarnings("unchecked")
	public List<Usuario> listJuizesPorOJ(OrgaoJulgador orgaoJulgador){
		Query q = getEntityManager().createQuery(
				"select distinct o.pessoaMagistrado from EstatisticaProcessoJusticaFederal "
					+ "o where o.orgaoJulgador =:orgaoJulgador");
		q.setParameter("orgaoJulgador", orgaoJulgador);
		List<Usuario> result = q.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Usuario> listJuizes(){
		String query = "SELECT DISTINCT(ulms.usuarioLocalizacao.usuario) FROM UsuarioLocalizacaoMagistradoServidor ulms " +
				"	WHERE ulms.usuarioLocalizacao.usuario.nome IN " +
				"		(SELECT pm.nome FROM PessoaMagistrado pm WHERE ulms.usuarioLocalizacao.usuario.nome = pm.nome) " +
				"	ORDER BY ulms.usuarioLocalizacao.usuario.nome";
		Query q = getEntityManager().createQuery(query);
		return q.getResultList();
	}

	public String getRelator(OrgaoJulgador orgaoJulgador){
		String query = "SELECT o.usuarioLocalizacao.usuario.nome FROM UsuarioLocalizacaoMagistradoServidor o " +
				"	WHERE o.orgaoJulgadorCargo.orgaoJulgador = :orgao " +
				"		AND o.orgaoJulgadorCargo.recebeDistribuicao = true ";

		Query q = entityManager.createQuery(query);
		q.setParameter("orgao", orgaoJulgador);
		q.setMaxResults(1);
		try{
			return (String) q.getSingleResult();
		}catch(NoResultException e){
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Cargo> getCargoVisibilidadeList(UsuarioLocalizacaoMagistradoServidor locAtual) {
		String query = "SELECT cargo FROM UsuarioLocalizacaoVisibilidade o " +
				"		LEFT JOIN o.orgaoJulgadorCargo ojc " +
				"		LEFT JOIN ojc.cargo cargo " +
				"	WHERE o.usuarioLocalizacaoMagistradoServidor = :localizacaoAtual";
		Query q = entityManager.createQuery(query);
		q.setParameter("localizacaoAtual", locAtual);
		return q.getResultList();
	}
	
	public List<UsuarioLocalizacaoMagistradoServidor> getMagistrados(
			OrgaoJulgador oj, OrgaoJulgadorColegiado ojColegiado, Date dataReferencia) {
		
		return recuperarLocalizacaoMagistrados(oj, ojColegiado, dataReferencia, null);
	}
	
	public List<UsuarioLocalizacaoMagistradoServidor> getMagistradosAuxiliares(
			OrgaoJulgador oj, OrgaoJulgadorColegiado ojColegiado, Date dataReferencia) {
		
		return recuperarLocalizacaoMagistrados(oj, ojColegiado, dataReferencia, true);
	}
	
	
	/**
	 * Verifica se há um registro de usuario localizacao com os dados informados
	 * @param usuario
	 * @param ojc
	 * @param oj
	 * @param localizacaoFisica
	 * @param localizacaoModelo
	 * @param papel
	 * @param dataInicio
	 * @param idUsuarioLocalizacaoMagistradoServidor
	 * @return
	 */
	public boolean verificaLocalizacaoInformada(Usuario usuario, OrgaoJulgadorColegiado ojc, OrgaoJulgador oj, Localizacao localizacaoFisica, Localizacao localizacaoModelo, 
    		Papel papel, Date dataInicio, Integer idUsuarioLocalizacaoMagistradoServidor) {
        StringBuilder sb = new StringBuilder();
        sb.append("select o from UsuarioLocalizacaoMagistradoServidor o where 1=1 ");
        sb.append(" and o.usuarioLocalizacao.usuario = :usuario ");
        sb.append(" and o.usuarioLocalizacao.localizacaoFisica = :localizacaoFisica ");
        sb.append(" and o.usuarioLocalizacao.papel = :papel ");

        if(dataInicio != null) {
        	sb.append(" and (o.dtFinal is null or o.dtFinal >= :dataInicio) ");
        }
        
        if (oj != null) {
        	sb.append(" and o.orgaoJulgador = :orgaoJulgador ");
        }else {
        	sb.append(" and o.orgaoJulgador IS NULL ");
        }

        if (ojc != null) {
        	sb.append(" and o.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ");
        }else {
        	sb.append(" and o.orgaoJulgadorColegiado IS NULL ");
        }
        
        if(localizacaoModelo != null) {
        	sb.append(" and o.usuarioLocalizacao.localizacaoModelo = :localizacaoModelo ");
        }else {
        	sb.append(" and o.usuarioLocalizacao.localizacaoModelo IS NULL ");
        }
        
        if (idUsuarioLocalizacaoMagistradoServidor != null) {
        	sb.append(" and o.idUsuarioLocalizacaoMagistradoServidor != :id ");
        }
        
        Query q = getEntityManager().createQuery(sb.toString());
        
        q.setParameter("usuario", usuario);
        q.setParameter("localizacaoFisica", localizacaoFisica);
        q.setParameter("papel", papel);

        if(oj != null) {
            q.setParameter("orgaoJulgador", oj);
        }
        if (ojc != null) {
        	q.setParameter("orgaoJulgadorColegiado", ojc);
        }

        if(localizacaoModelo != null) {
        	q.setParameter("localizacaoModelo", localizacaoModelo);
        }
        if(dataInicio != null) {
        	q.setParameter("dataInicio", dataInicio);
        }

        if (idUsuarioLocalizacaoMagistradoServidor != null) {
            q.setParameter("id", idUsuarioLocalizacaoMagistradoServidor);
        }

        @SuppressWarnings("unchecked")
		List<UsuarioLocalizacaoMagistradoServidor> result = (List<UsuarioLocalizacaoMagistradoServidor>) q.getResultList();
        return CollectionUtilsPje.isNotEmpty(result);
    }
	
	@SuppressWarnings("unchecked")
	private List<UsuarioLocalizacaoMagistradoServidor> recuperarLocalizacaoMagistrados(
			OrgaoJulgador oj, OrgaoJulgadorColegiado ojColegiado, Date dataReferencia, Boolean auxiliar) {
		
		StringBuilder jpql = new StringBuilder("SELECT DISTINCT ulms FROM UsuarioLocalizacaoMagistradoServidor AS ulms ")
				.append("JOIN FETCH ulms.orgaoJulgadorCargo c ")
				.append("JOIN FETCH ulms.usuarioLocalizacao ul ")
				.append("JOIN FETCH ul.usuario u ")
				.append("WHERE ulms.orgaoJulgador = :orgao ");
				
		if (ojColegiado != null) {
			jpql.append("AND ulms.orgaoJulgadorColegiado = :ojColegiado ");
		}
		if (auxiliar != null) {
			jpql.append("AND ulms.orgaoJulgadorCargo.auxiliar = :auxiliar ");
		}
		
		jpql.append("AND CAST(ulms.dtInicio as date) <= :dataCorrente ")
				.append(" AND (ulms.dtFinal IS NULL OR CAST(ulms.dtFinal as date) >= :dataCorrente)")
				.append(" ORDER BY u.nome ASC, c.descricao ");
		
		Query query = entityManager.createQuery(jpql.toString());
		query.setParameter("orgao", oj);
		if (ojColegiado != null) {
			query.setParameter("ojColegiado", ojColegiado);
		}
		if (auxiliar != null) {
			query.setParameter("auxiliar", auxiliar);
		}
		query.setParameter("dataCorrente", DateUtil.getDataSemHora(dataReferencia));

		return query.getResultList();
	}

	/**
	 * Método responsável por recuperar os dados do magistrado por cargo
	 * 
	 * @param orgaoJulgadorCargo
	 *            Dados do cargo
	 * @return Dados do magistrado
	 * 
	 * @see #obterLocalizacaoMagistrado(OrgaoJulgadorCargo, OrgaoJulgadorColegiado, Boolean)
	 */
	public UsuarioLocalizacaoMagistradoServidor getMagistradoPorCargo(
			OrgaoJulgadorCargo orgaoJulgadorCargo) {
		return obterLocalizacaoMagistrado(orgaoJulgadorCargo, null, null);
	}
	
	/**
	 * Método responsável por recuperar os dados de localização do magistrado por
	 * cargo, indicando se deseja o titular ou de determinado colegiado
	 * 
	 * Este método faz a pesquisa para recuperar apenas uma localização de acordo as chaves do cargo
	 * 
	 * @param orgaoJulgadorCargo
	 *            Cargo dentro do OJ para pesquisa.
	 * 
	 * @param orgaoJulgadorColegiado
	 *            Colegiado dentro da localização do OJ (opcional, pode ser
	 *            nulo)
	 * 
	 * @param magistradoTitular
	 *            <code>true</code> para trazer somente o titular
	 *            <code>false</code> para trazer o qual não é titular
	 *            <code>null</code> para não filtrar por este parâmetro
	 * 
	 * @return
	 * 		A localização do magistrador/servidor de acordo com os parâmetros informados.
	 * 
	 */
	public UsuarioLocalizacaoMagistradoServidor obterLocalizacaoMagistrado(
			OrgaoJulgadorCargo orgaoJulgadorCargo,
			OrgaoJulgadorColegiado orgaoJulgadorColegiado,
			Boolean magistradoTitular) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("select o from UsuarioLocalizacaoMagistradoServidor o ");
		sb.append("where o.orgaoJulgadorCargo = :orgaoJulgadorCargo ");
		
		if (orgaoJulgadorColegiado != null) {
			sb.append("and o.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ");
		}
		if (magistradoTitular != null) {
			sb.append("and o.magistradoTitular = :magistradoTitular ");
		}
		
		sb.append("and CAST(o.dtInicio as date) <= current_date ");
		sb.append("and (o.dtFinal is null or o.dtFinal >= current_date) ");	
		
		sb.append("order by o.dtInicio desc ");
				
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgadorCargo", orgaoJulgadorCargo);
		if (orgaoJulgadorColegiado != null) {
			q.setParameter("orgaoJulgadorColegiado", orgaoJulgadorColegiado);
		}
		if (magistradoTitular != null) {
			q.setParameter("magistradoTitular", magistradoTitular);
		}		
		
		try {
			q.setMaxResults(1);
			return (UsuarioLocalizacaoMagistradoServidor) q.getSingleResult();
			
		} catch (NoResultException e) {
			return null;
		}
	}
	
	/**
	 * Dado um órgão julgador <code>orgaoJulgador</code>, retorna localizações de magistrados 
	 * que constam lotados nesse orgão julgador.
	 * 
	 * @param orgaoJulgador
	 *            {@link OrgaoJulgador} para pesquisa
	 * 
	 * @param ojColegiado
	 *            {@link OrgaoJulgadorColegiado} para pesquisa (pode ser
	 *            nulo caso se queira localizações em qualquer colegiado)
	 * 
	 * @param isCargoDistribuivel
	 *            Aplica filtro relacionado ao fato do cargo receber distribuição ou não.
	 *            Pode ser nulo caso não queira aplicar esse filtro
	 * 
	 * @param isCargoAuxiliar
	 *            Aplica filtro relacionado ao fato do cargo ser auxiliar ou não.
	 *            Pode ser nulo caso não queira aplicar esse filtro
	 *
	 * @param isMagistradoTitular
	 *            Aplica filtro relacionado ao fato do cargo ser auxiliar ou não.
	 *            Pode ser nulo caso não queira aplicar esse filtro
	 *            
	 * @return Lotações dos magistrados que atuam no órgão julgador {@link OrgaoJulgador}
	 *         
	 */
	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacoesMagistrados(
			OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado ojColegiado, 
			Boolean isCargoDistribuivel, Boolean isCargoAuxiliar, Boolean isMagistradoTitular){
			
		StringBuilder sb = new StringBuilder();
		
		sb.append("select o from UsuarioLocalizacaoMagistradoServidor o ");
		sb.append("where o.orgaoJulgadorCargo.orgaoJulgador = :orgaoJulgador ");
		sb.append("and CAST(o.dtInicio as date) <= current_date ");
		sb.append("and (o.dtFinal is null or o.dtFinal >= current_date) ");
		
		if (ojColegiado != null) {
			sb.append("and o.orgaoJulgadorColegiado = :ojColegiado ");
		}
		if (isCargoDistribuivel != null){
			sb.append("and o.orgaoJulgadorCargo.recebeDistribuicao = :recebeDistribuicao ");
		}
		if (isCargoAuxiliar != null){
			sb.append("and o.orgaoJulgadorCargo.auxiliar = :auxiliar ");
		}
		if (isMagistradoTitular != null){
			sb.append("and o.magistradoTitular = :magistradoTitular ");	
		}
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgador", orgaoJulgador);
		
		if (ojColegiado != null) {
			q.setParameter("ojColegiado", ojColegiado);
		}
		if (isCargoDistribuivel != null){
			q.setParameter("recebeDistribuicao", isCargoDistribuivel);			
		}
		if (isCargoAuxiliar != null){
			q.setParameter("auxiliar", isCargoAuxiliar);			
		}
		if (isMagistradoTitular != null){
			q.setParameter("magistradoTitular", isMagistradoTitular);	
		}
		
		@SuppressWarnings("unchecked")
		List<UsuarioLocalizacaoMagistradoServidor> resultList = q.getResultList();		
		return resultList;
		
	}

	/**
	 * Dada a localização de um magistrado, utiliza a estrutura de vinculação de usuários
	 * para inferir a localização dos servidores que compõem a estrutura de gabinete 
	 * desse magistrado no respectivo órgão julgador.
	 * @param localizacaoMagistrado
	 * @return lotações dos servidores vinculados ao magistrado
	 */
	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacoesServidores(
			UsuarioLocalizacaoMagistradoServidor localizacaoMagistrado){
			
		StringBuilder sb = new StringBuilder();
		
		sb.append("select o from UsuarioLocalizacaoMagistradoServidor o 		");
		sb.append("where o.orgaoJulgador = :orgaoJulgador 						");
		sb.append("and o.orgaoJulgadorColegiado = :orgaoJulgadorColegiado 		");
		sb.append("and o.orgaoJulgadorCargo is null 							");
		sb.append("and o.usuarioLocalizacao.usuario in ( 						");
		sb.append("		select vu.usuarioVinculado from VinculacaoUsuario vu 	");
		sb.append("		where vu.usuario = :usuarioMagistrado 					");
		sb.append("		and vu.tipoVinculacaoUsuario = :tipoVinculacaoUsuario)	");
		sb.append("and CAST(o.dtInicio as date) <= current_date					");
		sb.append("and (o.dtFinal is null or o.dtFinal >= current_date) 		");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgador", localizacaoMagistrado.getOrgaoJulgador());
		q.setParameter("orgaoJulgadorColegiado", localizacaoMagistrado.getOrgaoJulgadorColegiado());
		q.setParameter("usuarioMagistrado", localizacaoMagistrado.getUsuarioLocalizacao().getUsuario());
		q.setParameter("tipoVinculacaoUsuario", TipoVinculacaoUsuarioEnum.EGA);
		
		@SuppressWarnings("unchecked")
		List<UsuarioLocalizacaoMagistradoServidor> resultList = q.getResultList();		
		return resultList;
		
	}
	
	/**
	 * Exclui lotações/localizações originadas de vinculação de usuário.
	 * @param vinculacaoUsuario vinculação de usuários que originou as lotações a serem excluídas
	 * @param orgaoJulgador orgao julgador das lotações a serem excluídas 
	 * @param orgaoJulgadorColegiado orgao julgador colegiado das lotações a serem excluídas
	 */
	public void removerLotacoes(VinculacaoUsuario vinculacaoUsuario, OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado){	
		List<UsuarioLocalizacaoMagistradoServidor> localizacoes = obterLocalizacoes(vinculacaoUsuario,	orgaoJulgador, orgaoJulgadorColegiado);
		
		for (UsuarioLocalizacaoMagistradoServidor ulms : localizacoes){
			UsuarioLocalizacao ul = ulms.getUsuarioLocalizacao();
			ulms.setUsuarioLocalizacao(null);
			getEntityManager().remove(ulms);
			getEntityManager().remove(ul);
			getEntityManager().flush();
		}
	}
	
	/**
	 * Retorna lotações/localizações de usuários que obedeçam os critérios parametrizados. 
	 * @param vinculacaoUsuario filtra somente lotações geradas a partir dessa vinculação
	 * @param orgaoJulgador filtra somente lotações nesse OJ. Informar nulo, caso não desejar filtrar por esse parametro.
	 * @param orgaoJulgadorColegiado filtra somente lotações nesse OJ Colegiado. Informar nulo, caso não desejar filtrar por esse parametro.
	 * @return Lista com os IDs das lotações encontradas
	 */
	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacoes(VinculacaoUsuario vinculacaoUsuario, OrgaoJulgador orgaoJulgador,
			OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT o ");
		sb.append("FROM UsuarioLocalizacaoMagistradoServidor o ");
		sb.append("WHERE o.vinculacaoUsuario = :vinculacaoUsuario ");
		
		if (orgaoJulgador != null){
			sb.append("AND o.orgaoJulgador = :orgaoJulgador ");
		}
		
		if (orgaoJulgadorColegiado != null){
			sb.append("AND o.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ");
		}	
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("vinculacaoUsuario", vinculacaoUsuario);
		
		if (orgaoJulgador != null){
			q.setParameter("orgaoJulgador", orgaoJulgador);
		}	
		if (orgaoJulgadorColegiado != null){
			q.setParameter("orgaoJulgadorColegiado", orgaoJulgadorColegiado);
		}	
		
		@SuppressWarnings("unchecked")
		List<UsuarioLocalizacaoMagistradoServidor> localizacoes = q.getResultList();
		return localizacoes;
	}	
	
 	/**
 	 * Obtem todas as localizações de um dado usuário
 	 * @param usuario usuário em questão.
 	 * @return lista de localizações desse usuário
 	 */
 	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacoesUsuario(Usuario usuario) {
 		return this.obterLocalizacoesUsuario(usuario, null, null);
 	}
 	
 	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacoesUsuario(Usuario usuario, Boolean apenasMagistrados, Boolean apenasServidores) {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("select o from UsuarioLocalizacaoMagistradoServidor o 	 ");
 		sb.append("where o.usuarioLocalizacao.usuario.idUsuario = :idUsuario ");
 		
 		if(apenasMagistrados != null && apenasMagistrados) {
 			sb.append(" AND o.usuarioLocalizacao.papel.identificador = :papelMagistrado ");
 		}else if(apenasServidores != null && apenasServidores) {
 			sb.append(" AND o.usuarioLocalizacao.papel.identificador != :papelMagistrado ");
 		}
 		
 		Query q = getEntityManager().createQuery(sb.toString());
 		q.setParameter("idUsuario", usuario.getIdUsuario());
 		if((apenasMagistrados != null && apenasMagistrados) || (apenasServidores != null && apenasServidores)) {
 			q.setParameter("papelMagistrado", Papeis.MAGISTRADO);
 		}
	
 		@SuppressWarnings("unchecked")
 		List<UsuarioLocalizacaoMagistradoServidor> resultList = q.getResultList();
 
 		return resultList; 		
 	}
 	 	
	/**
	 * Dada a localização de um assessor, utiliza a estrutura de vinculação de
	 * usuários para inferir a localização dos magistrados que ele assessora no
	 * respectivo órgão julgador da localização
	 * 
	 * @param localizacaoMagistradoSubstituto lotações do assessor
	 * @return lotações dos magistrados que o servidor assessora
	 */
	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacoesMagistradosAssessorado(
			UsuarioLocalizacaoMagistradoServidor localizacaoMagistradoSubstituto){
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("select o from UsuarioLocalizacaoMagistradoServidor o 		");
		sb.append("where o.orgaoJulgador = :orgaoJulgador 						");
		sb.append("and o.orgaoJulgadorColegiado = :orgaoJulgadorColegiado 		");
		sb.append("and o.orgaoJulgadorCargo is not null							");
		sb.append("and o.usuarioLocalizacao.usuario in ( 						");
		sb.append("		select vu.usuario from VinculacaoUsuario vu 			");
		sb.append("		where vu.usuarioVinculado = :usuarioMagistradoSubst		");
		sb.append("		and vu.tipoVinculacaoUsuario = :tipoVinculacaoUsuario)	");
		sb.append("and CAST(o.dtInicio as date) <= current_date					");
		sb.append("and (o.dtFinal is null or o.dtFinal >= current_date) 		");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgador", localizacaoMagistradoSubstituto.getOrgaoJulgador());
		q.setParameter("orgaoJulgadorColegiado", localizacaoMagistradoSubstituto.getOrgaoJulgadorColegiado());
		q.setParameter("usuarioMagistradoSubst", localizacaoMagistradoSubstituto.getUsuarioLocalizacao().getUsuario());
		q.setParameter("tipoVinculacaoUsuario", TipoVinculacaoUsuarioEnum.EGA);
		
		@SuppressWarnings("unchecked")
		List<UsuarioLocalizacaoMagistradoServidor> localizacaoAssessorado = q.getResultList();		
		return localizacaoAssessorado;
	}
	
	/**
	 * Dada a localização de um magistrado, utiliza a estrutura de vinculação de
	 * usuários para inferir a localização dos assessores dele no respectivo 
	 * órgão julgador da localização
	 * 
	 * @param localizacaoMagistrado lotação do magistrado
	 * @return lotações dos acessores do magistrado
	 */
	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacoesAcessoresMagistrado(
			UsuarioLocalizacaoMagistradoServidor localizacaoMagistrado){
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(" select o from UsuarioLocalizacaoMagistradoServidor o 		");
		sb.append(" where o.orgaoJulgador = :orgaoJulgador 						");
		sb.append(" and o.orgaoJulgadorColegiado = :orgaoJulgadorColegiado 		");
		sb.append(" and o.usuarioLocalizacao.usuario in ( 						");
		sb.append("		select vu.usuarioVinculado from VinculacaoUsuario vu 	");
		sb.append("		where vu.usuario = :usuarioMagistrado					");
		sb.append("		and vu.tipoVinculacaoUsuario = :tipoVinculacaoUsuario)	");
		sb.append(" and CAST(o.dtInicio as date) <= current_date				");
		sb.append(" and (o.dtFinal is null or o.dtFinal >= current_date)		");
		
		
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgador", localizacaoMagistrado.getOrgaoJulgador());
		q.setParameter("orgaoJulgadorColegiado", localizacaoMagistrado.getOrgaoJulgadorColegiado());
		q.setParameter("usuarioMagistrado", localizacaoMagistrado.getUsuarioLocalizacao().getUsuario());
		q.setParameter("tipoVinculacaoUsuario", TipoVinculacaoUsuarioEnum.EGA);
		
		@SuppressWarnings("unchecked")
		List<UsuarioLocalizacaoMagistradoServidor> localizacaoAssessorado = q.getResultList();		
		return localizacaoAssessorado;
	}
	
	public Long countUsuarioLocalizacaoMagistradoServidor(Integer idUsuario, Integer idOrgaoJulgador, Integer idUsuLocMagistrado) {
		StringBuilder query = new StringBuilder("select count(o) from UsuarioLocalizacaoMagistradoServidor o ");
		query.append("where o.idUsuarioLocalizacaoMagistradoServidor <> :idUsuLocMagistrado ");
		query.append("and o.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador ");
		query.append("and o.usuarioLocalizacao.usuario.idUsuario = :idUsuario ");
		Query q = getEntityManager().createQuery(query.toString());
		q.setParameter("idUsuLocMagistrado", idUsuLocMagistrado);
		q.setParameter("idOrgaoJulgador", idOrgaoJulgador);
		q.setParameter("idUsuario", idUsuario);
		return (Long) q.getSingleResult();
	}
}

