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
import br.jus.cnj.pje.business.dao.PessoaAssistenteProcuradoriaDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaFisica;

/**
 * Componente de gerenciamento das entidades {@link PessoaAssistenteProcuradoria}.
 * 
 * @author cristof
 *
 */
@Name(PessoaAssistenteProcuradoriaManager.NAME)
public class PessoaAssistenteProcuradoriaManager
		extends
		AbstractPessoaFisicaEspecializadaManager<PessoaAssistenteProcuradoria, PessoaAssistenteProcuradoriaDAO> {
	
	public static final String NAME = "pessoaAssistenteProcuradoriaManager";
	
	@In
	private PessoaAssistenteProcuradoriaDAO pessoaAssistenteProcuradoriaDAO;
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected PessoaAssistenteProcuradoriaDAO getDAO(){
		return pessoaAssistenteProcuradoriaDAO;
	}
	
	/**
	 * Atribui a uma pessoa física dada um perfil de assistente de procuradoria.
	 * 
	 * @param pessoa a pessoa física a quem será atribuído o perfil
	 * @return a {@link PessoaAssistenteProcuradoria} que foi atribuída à pessoa física.
	 */
	public PessoaAssistenteProcuradoria especializa(PessoaFisica pessoa){
		PessoaAssistenteProcuradoria assistente = pessoa.getPessoaAssistenteProcuradoria();
		if(assistente == null){
			assistente = pessoaAssistenteProcuradoriaDAO.especializa(pessoa);
			pessoa.setPessoaAssistenteProcuradoria(assistente);
		}
		pessoa.setEspecializacoes(pessoa.getEspecializacoes() | PessoaFisica.ASP);
		return assistente;
	}
	
	@Override
	public PessoaAssistenteProcuradoria persist(
			PessoaAssistenteProcuradoria entity) throws PJeBusinessException {
		
		entity.setTipoPessoa(ParametroUtil.instance().getTipoPessoaFisica());
		
		if (entity.getDataNascimento() != null && entity.getDataNascimento().after(new Date())) {
			throw new PJeBusinessException("pje.pessoaAssistenteProcuradorManager.error.dataNascimentoPosteriorAtual");
		}

		if (entity.getDataObito() != null && entity.getDataObito().after(new Date())) {
			throw new PJeBusinessException("pje.pessoaAssistenteProcuradorManager.error.dataObitoPosteriorAtual");
		}
		
		if (entity.getDataExpedicaoOab() != null && entity.getDataExpedicaoOab().after(new Date())) {
			throw new PJeBusinessException("pje.pessoaAssistenteProcuradorManager.error.dataExpedicaoOABPosteriorAtual");
		}
		
		return super.persist(entity);
	}
	
	/**
	 * Suprime de uma pessoa física a especialização de PessoaAssistenteProcuradoria.
	 * @param pessoa
	 * @return
	 * @throws PJeBusinessException
	 */
	public PessoaAssistenteProcuradoria desespecializa(PessoaFisica pessoa) throws PJeBusinessException{
		PessoaAssistenteProcuradoria asp = pessoa.getPessoaAssistenteProcuradoria();
		if(asp != null){
			asp = pessoaAssistenteProcuradoriaDAO.desespecializa(pessoa);
			pessoa.setPessoaAssistenteProcuradoria(asp);
		}
		return asp;
	}	

}
