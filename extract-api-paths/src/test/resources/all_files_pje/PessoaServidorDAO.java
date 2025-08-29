/**
 *  pje
 *  Copyright (C) 2013 Paulo Cristovão de Araújo Silva Filho
 *
 *  Este programa é software livre: você pode redistribuí-lo ou modificá-lo
 *  nos termos da Licença GNU Affero General Public como publicada pela
 *  Free Software Foundation, quer em sua versão 3, quer em versão pos-
 *  terior.
 * 
 *  Este programa é distribuído na esperança de que ele será útil, mas SEM
 *  QUALQUER GARANTIA; especialmente a de que ele tem algum VALOR 
 *  COMERCIAL ou APTIDÃO PARA UM OBJETIVO ESPECÍFICO. Leia a licença
 *  GNU Affero General Public para maiores detalhes.
 *
 *  Você deve ter recebido uma cópia da licença GNU Affero General Public
 *  com este programa.  Se não, acesse em <http://www.gnu.org/licenses/>.
 *
 */
package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaServidor;

/**
 * Componente de acesso a dados da entidade {@link PessoaServidor}.
 * 
 * @author cristof
 *
 */
@Name("pessoaServidorDAO")
public class PessoaServidorDAO extends AbstractPessoaFisicaEspecializadaDAO<PessoaServidor> {

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(PessoaServidor e) {
		return e.getIdUsuario();
	}
	
	/**
	 * Atribui a uma pessoa física dada um perfil de servidor.
	 * 
	 * @param pessoa a pessoa física a quem será atribuído o perfil
	 * @return a {@link PessoaServidor} que foi atribuída à pessoa física.
	 */
	public PessoaServidor especializa(PessoaFisica pessoa){
		if(!entityManager.contains(pessoa)){
			entityManager.persist(pessoa);
		}
		entityManager.flush();
		String query = "INSERT INTO tb_pessoa_servidor (id) VALUES (?1)";
		Query q = EntityUtil.createNativeQuery(entityManager, query, "tb_pessoa_servidor");
		q.setParameter(1, pessoa.getIdUsuario());
		if(q.executeUpdate() > 0) {
			return entityManager.find(PessoaServidor.class, pessoa.getIdUsuario());
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public PessoaServidor retornaByCPF(String cpf, Integer idUsuario){
		StringBuilder sqlPes = new StringBuilder();
		sqlPes.append("select o from PessoaServidor o ");
		sqlPes.append("inner join o.pessoaDocumentoIdentificacaoList p ");
		sqlPes.append("where p.tipoDocumento.codTipo = 'CPF' ");
		sqlPes.append("and p.numeroDocumento = :cpf");

		sqlPes.append("and o.idUsuario in ");
		sqlPes.append("(select n.usuario.idUsuario from UsuarioLocalizacao n ");
		sqlPes.append("where trim(n.papel.identificador) like 'administrador' ");
		sqlPes.append(" or trim(n.papel.identificador) like 'secretaria') ");

		sqlPes.append("and o.idUsuario not in ");
		sqlPes.append("(select l.idUsuario from PessoaOficialJustica l) ");

		if ((idUsuario != null) && (idUsuario != 0)) {
			sqlPes.append(" and o.idUsuario <> :idUsuario");
		}

		Query query = entityManager.createQuery(sqlPes.toString());
		query.setParameter("cpf", cpf);
		if (idUsuario != null) {
			query.setParameter("idUsuario", idUsuario);
		}
		List<PessoaServidor> listAdvogado = query.getResultList();

		if (listAdvogado.size() > 0) {
			FacesMessages.instance().addToControl("numeroCPFCpf", StatusMessage.Severity.ERROR, "CPF já cadastrado!");
			return listAdvogado.get(0);
		} 
		
		return null;		
	}

	public PessoaServidor desespecializa(PessoaFisica pessoa){
		PessoaServidor ser = null;
		ser = (PessoaServidor)entityManager.find(PessoaServidor.class, pessoa.getIdPessoa());
		if(ser != null){
			ser.getPessoa().suprimePessoaEspecializada(ser);
			entityManager.flush();
			return ser;
		}
		
		return null;
	}


@SuppressWarnings("unchecked")
	public List<PessoaServidor> retornaListaPessoaServidor(Integer idOrgaoJulgador, Integer idLocalizacao,
			Integer idPapel, Boolean somenteAtivos) {
		if(idOrgaoJulgador != null || idLocalizacao != null || idPapel != null){
			StringBuilder sqlPes = new StringBuilder();
			sqlPes.append("select distinct ps from PessoaServidor ps ");		
			sqlPes.append("inner join ps.usuarioLocalizacaoList ul ");
			sqlPes.append("inner join ul.usuarioLocalizacaoMagistradoServidor ulms ");
			sqlPes.append("inner join ulms.usuarioLocalizacaoVisibilidadeList ulv ");

			if(idOrgaoJulgador != null && idOrgaoJulgador != 0) {
				sqlPes.append("inner join ulms.orgaoJulgador oj ");
			}
			
			if(idLocalizacao != null && idLocalizacao != 0) {
				sqlPes.append("inner join ul.localizacaoFisica lf ");
				sqlPes.append("inner join lf.estruturaFilho ef ");
			}
			
			if(idPapel != null && idPapel != 0) {
				sqlPes.append("inner join ul.papel p ");
			}
			
			sqlPes.append("where 1=1 ");
			
			
			
			if(idOrgaoJulgador != null && idOrgaoJulgador != 0) {
				sqlPes.append("and oj.idOrgaoJulgador = :idOrgaoJulgador ");		
				if(somenteAtivos) {
					sqlPes.append("and (ulv.dtFinal is null or ulv.dtFinal > :dataAtual) ");
				}
			}
			
			if(idLocalizacao != null && idLocalizacao != 0) {
				sqlPes.append("and ef.idLocalizacao = :idLocalizacao ");
			}
			
			if(idPapel != null && idPapel != 0) {
				sqlPes.append("and p.idPapel = :idPapel ");
			}
			

			
			Query query = entityManager.createQuery(sqlPes.toString());
			
			if (idOrgaoJulgador != null && idOrgaoJulgador != 0) {
				query.setParameter("idOrgaoJulgador", idOrgaoJulgador);
				if(somenteAtivos) {
					query.setParameter("dataAtual", new Date());
				}
			}
			if (idLocalizacao != null && idLocalizacao != 0) {
				query.setParameter("idLocalizacao", idLocalizacao);
			}
			if (idPapel != null && idPapel != 0) {
				query.setParameter("idPapel", idPapel);
			}
			
			List<PessoaServidor> listaPessoaServidor = new ArrayList<PessoaServidor>();
			listaPessoaServidor = query.getResultList();
			
			return listaPessoaServidor;
			
		}
		else {
			return null;
		}
	}	


}
