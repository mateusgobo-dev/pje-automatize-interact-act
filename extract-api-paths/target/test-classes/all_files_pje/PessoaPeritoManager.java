/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;
import java.util.Set;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.business.dao.PessoaPeritoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaServidor;

/**
 * Componente de gerenciamento da entidade {@link PessoaPerito};
 */
@Name(PessoaPeritoManager.NAME)
public class PessoaPeritoManager extends AbstractPessoaFisicaEspecializadaManager<PessoaPerito, PessoaPeritoDAO> {
	
	public static final String NAME = "pessoaPeritoManager";
	
	@In
	private PessoaPeritoDAO pessoaPeritoDAO;
	
	@Override
	protected PessoaPeritoDAO getDAO() {
		return pessoaPeritoDAO;
	}
	
	/**
	 * Atribui a uma pessoa dada, se já não tiver, um perfil de perito.
	 * 
	 * @param pessoa a pessoa física a quem se pretende atribuir o perfil
	 * @return a {@link PessoaServidor} já vinculada à pessoa física.
	 */
	public PessoaPerito especializa(PessoaFisica pessoa) throws PJeBusinessException {
		PessoaPerito perito = pessoa.getPessoaPerito();
		if(perito == null){
			perito = pessoaPeritoDAO.especializa(pessoa);
			pessoa.setPessoaPerito(perito);
		}
		pessoa.setEspecializacoes(pessoa.getEspecializacoes() | PessoaFisica.PER);
		return perito;
	}
	
	@Override
	public PessoaPerito persist(PessoaPerito entity)
			throws PJeBusinessException {
		
		if(entity.getIdUsuario() == null){
			entity.setAtraiCompetencia(false);
		}
		
		entity.setTipoPessoa(ParametroUtil.instance().getTipoPessoaPerito());
		return super.persist(entity);
	}
	
	/**
	 * Suprime de uma pessoa a especialização de perito
	 * @param pessoa a pessoa física a quem se prentende suprimir o perfil
	 * @return a {@link PessoaServidor} já vinculada à pessoa física.
	 * @throws PJeBusinessException
	 */
	public PessoaPerito desespecializa(PessoaFisica pessoa) throws PJeBusinessException{
		PessoaPerito per = pessoa.getPessoaPerito();
		if(per != null){
			per = pessoaPeritoDAO.desespecializa(pessoa);
			pessoa.setPessoaPerito(per);
		}
		return per;
	}
	
	public List<PessoaPerito> recuperar(Especialidade especialidade, OrgaoJulgador orgaoJulgador) {
		return this.pessoaPeritoDAO.recuperar(especialidade, orgaoJulgador);
	}
	
	public List<PessoaPerito> recuperarAtivos(Integer idEspecialidade, Set<Integer> idsOrgaoJulgador) {
		return this.pessoaPeritoDAO.recuperarAtivos(idEspecialidade, idsOrgaoJulgador);
	}
}
