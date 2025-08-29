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

import java.util.Date;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.business.dao.PessoaAssistenteAdvogadoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.PessoaAssistenteAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;

/**
 * Componente de gerenciamento das entidades {@link PessoaAssistenteAdvogado}.
 * 
 * @author cristof
 *
 */
@Name(PessoaAssistenteAdvogadoManager.NAME)
public class PessoaAssistenteAdvogadoManager
		extends
		AbstractPessoaFisicaEspecializadaManager<PessoaAssistenteAdvogado, PessoaAssistenteAdvogadoDAO> {
	
	public static final String NAME = "pessoaAssistenteAdvogadoManager";
	
	@In
	private PessoaAssistenteAdvogadoDAO pessoaAssistenteAdvogadoDAO;
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected PessoaAssistenteAdvogadoDAO getDAO() {
		return pessoaAssistenteAdvogadoDAO;
	}
	
	/**
	 * Atribui a uma pessoa física dada um perfil de assistente de advogado.
	 * 
	 * @param pessoa a pessoa física a quem será atribuído o perfil
	 * @return a {@link PessoaAssistenteAdvogado} que foi atribuída à pessoa física.
	 */
	public PessoaAssistenteAdvogado especializa(PessoaFisica pessoa){
		PessoaAssistenteAdvogado assistente = pessoa.getPessoaAssistenteAdvogado();
		if(assistente == null){
			assistente = pessoaAssistenteAdvogadoDAO.especializa(pessoa); 
			pessoa.setPessoaAssistenteAdvogado(assistente);
		}
		pessoa.setEspecializacoes(pessoa.getEspecializacoes() | PessoaFisica.ASA);
		return assistente;
	}
	
	@Override
	public PessoaAssistenteAdvogado persist(PessoaAssistenteAdvogado entity)
			throws PJeBusinessException {
		
		if (entity.getDataNascimento() != null && entity.getDataNascimento().after(new Date())) {
			throw new PJeBusinessException("pje.pessoaAssistenteAdvogadoManager.error.dataNascimentoPosteriorAtual");
		}

		if (entity.getDataObito() != null && entity.getDataObito().after(new Date())) {
			throw new PJeBusinessException("pje.pessoaAssistenteAdvogadoManager.error.dataObitoPosteriorAtual");
		}
		
		if (entity.getDataExpedicaoOAB() != null && entity.getDataExpedicaoOAB().after(new Date())) {
			throw new PJeBusinessException("pje.pessoaAssistenteAdvogadoManager.error.dataExpedicaoOABPosteriorAtual");
		}
		
		return super.persist(entity);
	}
	
	/**
	 * Suprime de uma pessoa física a especialização de assistente advogado.
	 * @param pessoa
	 * @return
	 * @throws PJeBusinessException
	 */
	public PessoaAssistenteAdvogado desespecializa(PessoaFisica pessoa) throws PJeBusinessException{
		PessoaAssistenteAdvogado asa = pessoa.getPessoaAssistenteAdvogado();
		if(asa != null){
			asa = pessoaAssistenteAdvogadoDAO.desespecializa(pessoa);
			pessoa.setPessoaAssistenteAdvogado(asa);
		}
		return asa;
	}	

}
