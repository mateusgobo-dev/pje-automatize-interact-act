/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.business.dao.PessoaOficialJusticaDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.GrupoOficialJustica;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaOficialJustica;
import br.jus.pje.nucleo.entidades.PessoaServidor;

/**
 * Componente gerenciador da entidade {@link PessoaOficialJustica}.
 * @author cristof
 *
 */
@Name(PessoaOficialJusticaManager.NAME)
public class PessoaOficialJusticaManager extends AbstractPessoaFisicaEspecializadaManager<PessoaOficialJustica, PessoaOficialJusticaDAO> {
	
	public static final String NAME = "pessoaOficialJusticaManager";
	
	@In
	private PessoaOficialJusticaDAO pessoaOficialJusticaDAO;
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected PessoaOficialJusticaDAO getDAO() {
		return pessoaOficialJusticaDAO;
	}

	/**
	 * Atribui a uma pessoa dada, se já não tiver, um perfil de oficial de justiça.
	 * 
	 * @param pessoa a pessoa física a quem se pretende atribuir o perfil
	 * @return a {@link PessoaServidor} já vinculada à pessoa física.
	 */
	public PessoaOficialJustica especializa(PessoaFisica pessoa) throws PJeBusinessException {
		PessoaOficialJustica ofj = pessoa.getPessoaOficialJustica(); 
		if(ofj == null){
			ofj = pessoaOficialJusticaDAO.especializa(pessoa);
			pessoa.setPessoaOficialJustica(ofj);
		}
		pessoa.setEspecializacoes(pessoa.getEspecializacoes() | PessoaFisica.OFJ);
		return ofj;
	}
	
	@Override
	public PessoaOficialJustica persist(PessoaOficialJustica entity)
			throws PJeBusinessException {
		
		if (entity.getDataObito() != null && entity.getDataNascimento() != null
				&& entity.getDataObito().before(entity.getDataNascimento())) {
			throw new PJeBusinessException("pje.pessoaOficialJusticaManager.erro.dataObitoMenorDataNascimento");
		}
		
		entity.setTipoPessoa(ParametroUtil.instance().getTipoPessoaOficialJustica());
		
		return super.persist(entity);
	}
	
	/**
	 * Suprime de uma pessoa a especialização de oficial de justiça
	 * @param pessoa a pessoa física a quem se prentende suprimir o perfil
	 * @return a {@link PessoaServidor} já vinculada à pessoa física.
	 * @throws PJeBusinessException
	 */
	public PessoaOficialJustica desespecializa(PessoaFisica pessoa) throws PJeBusinessException{
		PessoaOficialJustica ofj = pessoa.getPessoaOficialJustica();
		if(ofj != null){
			ofj = pessoaOficialJusticaDAO.desespecializa(pessoa);
			pessoa.setPessoaOficialJustica(ofj);
		}
		return ofj;
	}		
	
	public List<PessoaOficialJustica> listPessoaOficialJusticaByGrupoOficialJustica(GrupoOficialJustica grupoOficialJustica){
		return pessoaOficialJusticaDAO.listPessoaOficialJusticaByGrupoOficialJustica(grupoOficialJustica);
	}
	
}
