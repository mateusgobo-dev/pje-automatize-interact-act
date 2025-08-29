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

import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaFisica;

/**
 * Componente de acesso a dados da entidade {@link PessoaAssistenteProcuradoria}.
 *  
 * @author cristof
 *
 */
@Name("pessoaAssistenteProcuradoriaDAO")
public class PessoaAssistenteProcuradoriaDAO extends AbstractPessoaFisicaEspecializadaDAO<PessoaAssistenteProcuradoria> {
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(PessoaAssistenteProcuradoria e) {
		return e.getIdUsuario();
	}
	
	/**
	 * Atribui a uma pessoa física dada um perfil de assistente de procuradoria.
	 * 
	 * @param pessoa a pessoa física a quem será atribuído o perfil
	 * @return a {@link PessoaAssistenteProcuradoria} que foi atribuída à pessoa física.
	 */
	public PessoaAssistenteProcuradoria especializa(PessoaFisica pessoa){
		if(!entityManager.contains(pessoa)){
			entityManager.persist(pessoa);
		}
		entityManager.flush();
		String query = "INSERT INTO tb_pess_assistente_procurd (id, dt_cadastro) VALUES (?1, NOW())";
		Query q = EntityUtil.createNativeQuery(entityManager, query, "tb_pess_assistente_procurd");
		q.setParameter(1, pessoa.getIdUsuario());
		if(q.executeUpdate() > 0) {
			return entityManager.find(PessoaAssistenteProcuradoria.class, pessoa.getIdUsuario());
		} else {
			return null;
		}
	}
	
	/**
	 * Efetua a supressão do papel de Assistente de Procuradoria para a pessoa física indicada
	 * @param pessoa
	 * @return
	 */
	public PessoaAssistenteProcuradoria desespecializa(PessoaFisica pessoa){
		PessoaAssistenteProcuradoria asp = null;
		asp = (PessoaAssistenteProcuradoria)entityManager.find(PessoaAssistenteProcuradoria.class, pessoa.getIdPessoa());
		if(asp != null){
			asp.getPessoa().suprimePessoaEspecializada(asp);
			entityManager.flush();
			return asp;
		}
		
		return null;
	}	

}
