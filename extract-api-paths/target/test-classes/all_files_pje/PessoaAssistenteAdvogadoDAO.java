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

import br.com.itx.util.EntityUtil;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.PessoaAssistenteAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;

/**
 * Componente de acesso a dados da entidade {@link PessoaAssistenteAdvogado}.
 * 
 * @author cristof
 *
 */
@Name("pessoaAssistenteAdvogadoDAO")
public class PessoaAssistenteAdvogadoDAO extends AbstractPessoaFisicaEspecializadaDAO<PessoaAssistenteAdvogado> {
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(PessoaAssistenteAdvogado e) {
		return e.getIdUsuario();
	}
	
	/**
	 * Atribui a uma pessoa física dada um perfil de assistente de advogado.
	 * 
	 * @param pessoa a pessoa física a quem será atribuído o perfil
	 * @return a {@link PessoaAssistenteAdvogado} que foi atribuída à pessoa física.
	 */
	public PessoaAssistenteAdvogado especializa(PessoaFisica pessoa){
		if(!entityManager.contains(pessoa)){
			entityManager.persist(pessoa);
		}
		entityManager.flush();
		String query = "INSERT INTO tb_pessoa_assistente_adv (id) VALUES (?1)";
		Query q = EntityUtil.createNativeQuery(entityManager, query, "tb_pessoa_assistente_adv");
		q.setParameter(1, pessoa.getIdUsuario());
		if(q.executeUpdate() > 0) {
			return entityManager.find(PessoaAssistenteAdvogado.class, pessoa.getIdUsuario());
		} else {
			return null;
		}
	}
	
	public PessoaAssistenteAdvogado desespecializa(PessoaFisica pessoa){
		PessoaAssistenteAdvogado asa = null;
		asa = (PessoaAssistenteAdvogado)entityManager.find(PessoaAssistenteAdvogado.class, pessoa.getIdPessoa());
		if(asa != null){
			asa.getPessoa().suprimePessoaEspecializada(asa);
			entityManager.flush();
			return asa;
		}
		
		return null;
	}	

}
