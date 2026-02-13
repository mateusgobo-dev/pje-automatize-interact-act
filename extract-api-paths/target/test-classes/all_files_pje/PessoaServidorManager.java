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
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.business.dao.PessoaServidorDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaServidor;

/**
 * @author cristof
 *
 */
@Name("pessoaServidorManager")
@Scope(ScopeType.EVENT)
@AutoCreate
public class PessoaServidorManager extends AbstractPessoaFisicaEspecializadaManager<PessoaServidor, PessoaServidorDAO> {
	
	public static final String NAME = "pessoaServidorManager";
	
	@In
	private PessoaServidorDAO pessoaServidorDAO;
	
	@Override
	protected PessoaServidorDAO getDAO() {
		return pessoaServidorDAO;
	}

	/**
	 * Atribui a uma pessoa dada, se já não tiver, um perfil de servidor.
	 * 
	 * @param pessoa a pessoa física a quem se pretende atribuir o perfil
	 * @return a {@link PessoaServidor} já vinculada à pessoa física.
	 */
	public PessoaServidor especializa(PessoaFisica pessoa) throws PJeBusinessException {
		PessoaServidor serv = pessoa.getPessoaServidor();
		if(serv == null){
			serv = pessoaServidorDAO.especializa(pessoa);
			pessoa.setPessoaServidor(serv);
		}
		pessoa.setEspecializacoes(pessoa.getEspecializacoes() | PessoaFisica.SER);
		return serv;
	}
	
	@Override
	public PessoaServidor persist(PessoaServidor entity)
			throws PJeBusinessException {
		if(entity.getIdUsuario() == null){
			entity.setTipoPessoa(ParametroUtil.instance().getTipoPessoaServidor());
		}
		
		return super.persist(entity);
	}
	
	public PessoaServidor retornaByCPF(String cpf, Integer idUsuario){
		return getDAO().retornaByCPF(cpf, idUsuario);
	}
	
	/**
	 * Suprime de uma pessoa a especialização de servidor
	 * @param pessoa a pessoa física a quem se prentende suprimir o perfil
	 * @return a {@link PessoaServidor} já vinculada à pessoa física.
	 * @throws PJeBusinessException
	 */
	public PessoaServidor desespecializa(PessoaFisica pessoa) throws PJeBusinessException{
		PessoaServidor ser = pessoa.getPessoaServidor();
		if(ser != null){
			ser = pessoaServidorDAO.desespecializa(pessoa);
			pessoa.setPessoaServidor(ser);
		}
		return ser;
	}
	
	public List<PessoaServidor> retornaListaPessoaServidor(Integer idOrgaoJulgador, Integer idLocalizacao, Integer idPapel, Boolean somenteAtivos) {	
		List<PessoaServidor> listaPessoaServidor = pessoaServidorDAO.retornaListaPessoaServidor(idOrgaoJulgador, idLocalizacao, idPapel, somenteAtivos);	
		return listaPessoaServidor;
		
	}
	
}
