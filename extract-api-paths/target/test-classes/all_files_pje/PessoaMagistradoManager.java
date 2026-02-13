package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.PessoaMagistradoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;

/**
 * Classe manager para UsuarioLocalizacaoMagistradoServidor
 * 
 * @author Allan
 * 
 */
@Name("pessoaMagistradoManager")
public class PessoaMagistradoManager extends AbstractPessoaFisicaEspecializadaManager<PessoaMagistrado, PessoaMagistradoDAO>{
	
	public static final String NAME = "pessoaMagistradoManager";
	
	@In
	private PessoaMagistradoDAO pessoaMagistradoDAO;
	
	/**
     * @return Instância da classe.
     */
    public static PessoaMagistradoManager instance() {
        return ComponentUtil.getComponent(NAME);
    }
    
	@Override
	protected PessoaMagistradoDAO getDAO() {
		return pessoaMagistradoDAO;
	}

	public PessoaMagistrado getMagistradoRecebeDistribuicao(OrgaoJulgador orgaoJulgador) {
		return pessoaMagistradoDAO.getMagistradoRecebeDistribuicao(orgaoJulgador);
	}
	
	public List<PessoaMagistrado> magistradoSubstitutoItems(OrgaoJulgador orgaoJulgador, SessaoJT sessao, ProcessoTrf processoTrf){
		if(orgaoJulgador == null || sessao == null || processoTrf == null){
			return null;
		}
		return pessoaMagistradoDAO.magistradoSubstitutoItems(orgaoJulgador, sessao, processoTrf);
	}
	
	public List<PessoaMagistrado> magistradoSubstitutoSessaoItems(OrgaoJulgador orgaoJulgador, SessaoJT sessao){
		if(orgaoJulgador == null){
			return null;
		}
		return pessoaMagistradoDAO.magistradoSubstitutoSessaoItems(orgaoJulgador, sessao);
	}
	
	public List<PessoaMagistrado> magistradoSubstitutoSessaoItems(OrgaoJulgador orgaoJulgador, SessaoJT sessao, OrgaoJulgadorColegiado orgaoJulgadorColegiado){
	  	if(orgaoJulgador == null){  
	  		return null;  
	  	}  
	  	return pessoaMagistradoDAO.magistradoSubstitutoSessaoItems(orgaoJulgador, sessao, orgaoJulgadorColegiado);  
	} 
	
	public PessoaMagistrado getMagistradoTitular(OrgaoJulgador orgaoJulgador){
		if(orgaoJulgador == null){
			return null;
		}
		return pessoaMagistradoDAO.getMagistradoTitular(orgaoJulgador);
	}
	
	public PessoaMagistrado getMagistradoTitular(OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado){  
		if(orgaoJulgador == null){  
			return null;  
	  	}  
	  	return pessoaMagistradoDAO.getMagistradoTitular(orgaoJulgador, orgaoJulgadorColegiado);  
	}  
	
	public List<PessoaMagistrado> magistradoPorOrgaoJulgador(OrgaoJulgador orgaoJulgador){
		return pessoaMagistradoDAO.magistradoPorOrgaoJulgador(orgaoJulgador, false);
	}
	
	public List<PessoaMagistrado> magistradoPorOrgaoJulgador(OrgaoJulgador orgaoJulgador, Boolean somenteAtivosNoOrgao){
	    return pessoaMagistradoDAO.magistradoPorOrgaoJulgador(orgaoJulgador, somenteAtivosNoOrgao);
	}

	public List<PessoaMagistrado> magistradoList() {
		return pessoaMagistradoDAO.magistradoList();
	}
	
	/**
	 * Atribui a uma pessoa dada, se já não tiver, um perfil de magistrado.
	 * 
	 * @param pessoa a pessoa física a quem se pretende atribuir o perfil
	 * @return a {@link PessoaMagistrado} já vinculada à pessoa física.
	 */
	@Override
	public PessoaMagistrado especializa(PessoaFisica pessoa) throws PJeBusinessException {
		PessoaMagistrado mag = pessoa.getPessoaMagistrado();
		if(mag == null){
			mag = pessoaMagistradoDAO.especializa(pessoa);
			pessoa.setPessoaMagistrado(mag);
		}
		pessoa.setEspecializacoes(pessoa.getEspecializacoes() | PessoaFisica.MAG);
		return mag;
	}
	
	@Override
	public PessoaMagistrado persist(PessoaMagistrado entity)
			throws PJeBusinessException {
		
		if (entity.getDataObito() != null && entity.getDataNascimento() != null
				&& entity.getDataObito().before(entity.getDataNascimento())) {
			throw new PJeBusinessException("pje.pessoaMagistradoManager.erro.dataObitoMenorDataNascimento");
		}
		
		if(entity.getIdUsuario() != null){
			entity.setTipoPessoa(ParametroUtil.instance().getTipoPessoaMagistrado());
		}
		
		return super.persist(entity);
	}
	
	/**
	 * Suprime de uma pessoa a especialização de Magistrado
	 * @param pessoa a pessoa física a quem se prentende suprimir o perfil
	 * @return a {@link PessoaServidor} já vinculada à pessoa física.
	 * @throws PJeBusinessException
	 */
	@Override
	public PessoaMagistrado desespecializa(PessoaFisica pessoa) throws PJeBusinessException{
		PessoaMagistrado mag = pessoa.getPessoaMagistrado();
		if(mag != null){
			mag = pessoaMagistradoDAO.desespecializa(pessoa);
			pessoa.setPessoaMagistrado(mag);
		}
		return mag;
	}
	
	/**
	 * Método responsável por recuperar o magistrado marcado como presente na sessão de julgamento e que seja representante do órgão julgador.
	 * 
	 * @param idSessao Identificador da sessão de julgamento.
	 * @param idOrgaoJulgador Identificador do órgão julgador.
	 * @return Magistrado marcado como presente na sessão de julgamento e que seja representante do órgão julgador.
	 */
	public PessoaMagistrado recuperarMagistrado(Integer idSessao, Integer idOrgaoJulgador) {
		return this.pessoaMagistradoDAO.recuperarMagistrado(idSessao, idOrgaoJulgador);
	}

	/**
	 * Retorna os órgãos julgadores onde o magistrado é o titular.
	 * 
	 * @param magistrado PessoaMagistrado
	 * @return órgãos julgadores onde o magistrado é o titular.
	 */
	public List<OrgaoJulgador> consultarOrgaoJulgadorMagistradoTitular(PessoaMagistrado magistrado) {
		return this.pessoaMagistradoDAO.consultarOrgaoJulgadorMagistradoTitular(magistrado);
	}
	
	/**
	 * Metodo que obtem os magistrados substitutos para uma determinada sessao e orgao julgador.
	 * 
	 * @param idSessao Id da Sessao
	 * @param idOrgaoJulgador Id do Orgao Julgador
	 * @return Lista de Magistrados substitutos.
	 */
	public List<PessoaMagistrado> obterSubstitutos(Integer idSessao, Integer idOrgaoJulgador){
		return pessoaMagistradoDAO.obterSubstitutos(idSessao, idOrgaoJulgador);
	}
	
	/**
	 * Metodo que obtem os magistrados aptos para uma sessao.
	 *  + Verifica se o magistrado pode compor a sessao, e se ele é apto de acordo com os magistrados vinculados a um
	 *  	Orgao Julgador
	 *  + Verifica qual o magistrado mais apto mais antigo para um Orgao Julgador e coloca ele como primeiro da lista 
	 *  
	 * @param idOrgaoJulgador Id do Orgao Julgador
	 * @param idSessao Id da Sessao
	 * @param dataAberturaSessao Data de Inicio da Sessao
	 * @return Magistrados aptos para uma sessao.
	 */
	public List<PessoaMagistrado> obterAptos(Integer idOrgaoJulgador, Integer idSessao, Date dataAberturaSessao){
		return pessoaMagistradoDAO.obterAptos(idOrgaoJulgador, idSessao, dataAberturaSessao);
	}
	
	/**
	 * Obtem os magistrados em orderm ativos para o orgao julgador
	 * 
	 * @param idOrgaoJulgador 
	 * @return Lista de magistrados para o OJ
	 */
	public List<PessoaMagistrado> obterAptos(Integer idOrgaoJulgador){
		return pessoaMagistradoDAO.obterAptos(idOrgaoJulgador);
	}
	
	/**
	 * Recupera o presidente da sessão de julgamento
	 * @param OrdensDeComposicao
	 * @return presidente da sessão de julgamento
	 */
	public PessoaMagistrado getMagistradoPresidenteSessao(List<SessaoComposicaoOrdem> OrdensDeComposicao) {
		for (SessaoComposicaoOrdem sco : OrdensDeComposicao) {			
			if (sco.getPresidente()) {
				if (sco.getMagistradoSubstitutoSessao() != null) {
					return sco.getMagistradoSubstitutoSessao();
				}				
				return sco.getMagistradoPresenteSessao();
			}
		}		
		return null;
	}	
	
	
}