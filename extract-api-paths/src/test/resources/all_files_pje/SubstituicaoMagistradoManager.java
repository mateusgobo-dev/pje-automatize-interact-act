package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.SubstituicaoMagistradoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.SubstituicaoMagistrado;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoVisibilidade;

@Name(SubstituicaoMagistradoManager.NAME)
public class SubstituicaoMagistradoManager extends BaseManager<SubstituicaoMagistrado>{

	public static final String NAME = "substituicaoMagistradoManager";

	@In
	private SubstituicaoMagistradoDAO substituicaoMagistradoDAO;
	
	@In
	private UsuarioLocalizacaoVisibilidadeManager usuarioLocalizacaoVisibilidadeManager;
	
	@In
	private UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;
	
	
	
	private List<UsuarioLocalizacaoVisibilidade> visibilidadesConflitantes = new ArrayList<UsuarioLocalizacaoVisibilidade>();
	
	@Override
	protected SubstituicaoMagistradoDAO getDAO() {
		return substituicaoMagistradoDAO;
	}
	
	/**
	 * Valida se uma dada substituição pode ser persistida
	 * @param substituicaoMagistrado a substituição a ser validada
	 * @throws PJeBusinessException caso a validação aponte algum problema.
	 */
	public void validar(SubstituicaoMagistrado substituicaoMagistrado) throws PJeBusinessException{
		if (substituicaoMagistrado.getDataInicio().after(substituicaoMagistrado.getDataFim())){
			throw new PJeBusinessException("substituicaoMagistrado.validacao.datas");
		}
		
		if (existeSubstituicaoConflitante(substituicaoMagistrado)){
			throw new PJeBusinessException("substituicaoMagistrado.validacao.conflito");
		}
	}
	
	/**
	 * Dada uma substituição, verifica se existe alguma outra substituição
	 * com período conflitante com a substituição em questão, 
	 * para o mesmo magistrado afastado.  
	 * @param substituicaoMagistrado a substituição a ser analisada
	 * @return true caso existam substituições conflitantes.
	 */
	public boolean existeSubstituicaoConflitante(SubstituicaoMagistrado substituicaoMagistrado) {
		return getDAO().existeSubstituicaoConflitante(substituicaoMagistrado);	
	}
	
	/**
	 * Efetua tratamento (manutenção) de visibilidades necessárias a atender uma dada substituição
	 * @param substituicaoMagistrado substituição cujas visibilidades serão tratadas.
	 * @throws PJeBusinessException 
	 */
	public void tratarVisibilidades(SubstituicaoMagistrado substituicaoMagistrado) throws PJeBusinessException{
		removerVisibilidadesSubstituicoesEncerradas();
		removerVisibilidades(substituicaoMagistrado);
		
		visibilidadesConflitantes.clear();
		criarVisibilidades(substituicaoMagistrado);
		
		substituicaoMagistrado.setAvisosVisibilidade("");
		if (!visibilidadesConflitantes.isEmpty()){
			String avisosVisibilidadesConflitantes = criarAvisosVisibilidadesConflitantes(visibilidadesConflitantes); 
			substituicaoMagistrado.setAvisosVisibilidade(avisosVisibilidadesConflitantes);
		}
		mergeAndFlush(substituicaoMagistrado);
	}
	
	/**
	 * Dada uma listagem de visibilidades conflitantes com a substituição atual, retorna uma mensagem
	 * de aviso formatada para apresentação ao usuário.
	 * @param visibilidadesConflitantes
	 * @return String com mensagem formatada.
	 */
	private String criarAvisosVisibilidadesConflitantes(List<UsuarioLocalizacaoVisibilidade> visibilidadesConflitantes) {
		StringBuilder sb = new StringBuilder();
		sb.append("Aviso: houve conflito de visibilidade já existente para os usuários abaixo: ");
		sb.append("<ul>");
		for (UsuarioLocalizacaoVisibilidade ulv : visibilidadesConflitantes){
			sb.append("<li>Usuário: "+ulv.getUsuarioLocalizacaoMagistradoServidor().getUsuarioLocalizacao().getUsuario() + " </li>");
			sb.append("<li>Cargo: "+ulv.getOrgaoJulgadorCargo().getDescricao() + " </li>");
			sb.append("<li></li>");
		}
		sb.append("</ul>");
		sb.append("As visibilidades não foram habilitadas para tais usuários!");
		
		return sb.toString();
	}

	/**
	 * Remove todas as visibilidades geradas pra substituições que já se encerraram.
	 */
	private void removerVisibilidadesSubstituicoesEncerradas() {
		usuarioLocalizacaoVisibilidadeManager.removerVisibilidadesSubstituicoesEncerradas();
	}	
	
	/**
	 * Remove todas as visibilidades geradas para uma dada substituição
	 * @param substituicaoMagistrado substituição cujas visibilidades serão removidas
	 */
	private void removerVisibilidades(SubstituicaoMagistrado substituicaoMagistrado){
		usuarioLocalizacaoVisibilidadeManager.removerVisibilidadesSubstituicao(substituicaoMagistrado);
	}
	
	/**
	 * Cria visibilidades para atender a substituição em questão.
	 * @param substituicaoMagistrado substituição ao qual serão geradas as visibilidades
	 * @throws PJeBusinessException 
	 */
	private void criarVisibilidades(SubstituicaoMagistrado substituicaoMagistrado) throws PJeBusinessException {
		
		UsuarioLocalizacaoMagistradoServidor lotacaoMagistradoSubstituto = usuarioLocalizacaoMagistradoServidorManager.obterLocalizacaoMagistrado(
			substituicaoMagistrado.getCargoMagistradoSubstituto(), substituicaoMagistrado.getOrgaoJulgadorColegiado(), false);		
		
		criarVisibilidadeMagistradoSubstituto(substituicaoMagistrado, lotacaoMagistradoSubstituto);
		criarVisibilidadesEstruturaGabineteMagistradoSubstituto(substituicaoMagistrado, lotacaoMagistradoSubstituto);
				
		if (substituicaoMagistrado.getEstruturaGabineteCedida() && substituicaoMagistrado.getMagistradoAfastado() != null){
			criarVisibilidadesEstruturaGabineteMagistradoAfastado(substituicaoMagistrado);			
		}
	}
	
	/**
	 * Dado uma substituição, cria visibilidade necessária para que o magistrado substituto consiga
	 * atender a substituição em questão. 
	 * @param substituicaoMagistrado substituição em questão
	 * @throws PJeBusinessException
	 */
	private void criarVisibilidadeMagistradoSubstituto(SubstituicaoMagistrado substituicaoMagistrado, UsuarioLocalizacaoMagistradoServidor lotacaoMagistradoSubstituto) throws PJeBusinessException{
		criarVisibilidade(substituicaoMagistrado, lotacaoMagistradoSubstituto,	substituicaoMagistrado.getCargoDistribuicao());
	}
	
	/**
	 * Dado uma substituição, cria visibilidades necessárias para que a estrutura de gabinete do magistrado substituto 
	 * consiga atender a substituição em questão. 
	 * @param substituicaoMagistrado substituição em questão
	 * @throws PJeBusinessException
	 */
	private void criarVisibilidadesEstruturaGabineteMagistradoSubstituto(SubstituicaoMagistrado substituicaoMagistrado, UsuarioLocalizacaoMagistradoServidor lotacaoMagistradoSubstituto) throws PJeBusinessException{
		List<UsuarioLocalizacaoMagistradoServidor> lotacoesEstruturaGabinete = obterEstruturaGabineteMagistrado(lotacaoMagistradoSubstituto);
		for (UsuarioLocalizacaoMagistradoServidor lotacaoEstruturaGabinete : lotacoesEstruturaGabinete){
			criarVisibilidade(substituicaoMagistrado, lotacaoEstruturaGabinete, substituicaoMagistrado.getCargoDistribuicao());
		}
	}

	/**
	 * Dado uma substituição, cria visibilidades necessárias para que a estrutura de gabinete do magistrado afastado 
	 * consiga atender a substituição em questão. 
	 * @param substituicaoMagistrado substituição em questão
	 * @throws PJeBusinessException
	 */
	private void criarVisibilidadesEstruturaGabineteMagistradoAfastado(SubstituicaoMagistrado substituicaoMagistrado) throws PJeBusinessException{
		UsuarioLocalizacaoMagistradoServidor lotacaoMagistradoAfastado = usuarioLocalizacaoMagistradoServidorManager.obterLocalizacaoMagistrado(
				substituicaoMagistrado.getCargoDistribuicao(), substituicaoMagistrado.getOrgaoJulgadorColegiado(), true);		
		
		List<UsuarioLocalizacaoMagistradoServidor> lotacoesEstruturaGabinete = obterEstruturaGabineteMagistrado(lotacaoMagistradoAfastado);
		for (UsuarioLocalizacaoMagistradoServidor lotacaoEstruturaGabinete : lotacoesEstruturaGabinete){
			criarVisibilidade(substituicaoMagistrado, lotacaoEstruturaGabinete, substituicaoMagistrado.getCargoMagistradoSubstituto());
		}
	}

	/**
	 * Dado um perfil de magistrado, retorna os perfis dos servidores
	 * que compõem a estrutura de gabinete desse magistrado 
	 * @param perfilMagistradoSubstituto perfil do magistrado em questão
	 * @return
	 */
	private List<UsuarioLocalizacaoMagistradoServidor> obterEstruturaGabineteMagistrado(
			UsuarioLocalizacaoMagistradoServidor perfilMagistrado) {
		return usuarioLocalizacaoMagistradoServidorManager.obterLocalizacoesServidores(perfilMagistrado);
	}

	/**
	 * Dada uma substituição de magistrado, concede visibilidade para um perfil específico, 
	 * em relação a um cargo alvo específico, de forma a atender a substituição em questão.
	 * @param substituicaoMagistrado substituição a ser atendida
	 * @param ojCargoAlvo cargo que se tornará visivel ao perfil
	 * @param perfil perfil que está ganhando visibilidade para o cargo
	 * @throws PJeBusinessException
	 */
	private void criarVisibilidade(SubstituicaoMagistrado substituicaoMagistrado, UsuarioLocalizacaoMagistradoServidor perfil, OrgaoJulgadorCargo ojCargoAlvo) throws PJeBusinessException {
		UsuarioLocalizacaoVisibilidade ulv = new UsuarioLocalizacaoVisibilidade();
		ulv.setSubstituicaoMagistrado(substituicaoMagistrado);
		ulv.setDtInicio(substituicaoMagistrado.getDataInicio());
		ulv.setDtFinal(substituicaoMagistrado.getDataFim());
		ulv.setOrgaoJulgadorCargo(ojCargoAlvo);
		ulv.setUsuarioLocalizacaoMagistradoServidor(perfil);
		if (usuarioLocalizacaoVisibilidadeManager.existeVisibilidadeConflitante(ulv)){
			visibilidadesConflitantes.add(ulv);
			return;
		}
		
		usuarioLocalizacaoVisibilidadeManager.persist(ulv);
	}
	
	/**
	 * Recupera a listagem de visibilidades conflitantes
	 * @return listagem de visibilidades conflitantes
	 */
	public List<UsuarioLocalizacaoVisibilidade> getVisibilidadesConflitantes(){
		return visibilidadesConflitantes;
	}
	
	/**
	 * Verifica se determinado magistrado possui um período de substituição cadastrado.
	 * @param orgaoJulgador orgão julgador a ser consultado
	 * @param magistrado usuário que deveria estar substituindo alguém, um magistrado
	 */
	public boolean existeSubstituicaoMagistrado(OrgaoJulgadorColegiado orgaoJulgadorColegiado, OrgaoJulgador orgaoJulgador, Usuario magistrado) {		
		return !substituicaoMagistradoDAO.obterSubstituicoesMagistrado(orgaoJulgadorColegiado, orgaoJulgador, magistrado).isEmpty();
	}
	
	/**
	 * Método que carrega as substituições cadastradas para determinado magistrado
	 */
	public List<SubstituicaoMagistrado> obterSubstituicaoMagistrado(OrgaoJulgadorColegiado orgaoJulgadorColegiado, OrgaoJulgador orgaoJulgador, Usuario magistrado) {
		return substituicaoMagistradoDAO.obterSubstituicoesMagistrado(orgaoJulgadorColegiado, orgaoJulgador, magistrado);
	}
	
	/**
	 * @see SubstituicaoMagistradoDAO#obterSubstituicaoMagistradoVigente(UsuarioLocalizacaoMagistradoServidor) 	
	 */
	public SubstituicaoMagistrado obterSubstituicaoMagistradoVigente(OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado,
			UsuarioLocalizacaoMagistradoServidor perfilMagistradoSubstituto) {
		return substituicaoMagistradoDAO.obterSubstituicaoMagistradoVigente(orgaoJulgador, orgaoJulgadorColegiado, perfilMagistradoSubstituto);
	}
	
	
	public SubstituicaoMagistrado obterSubstituicaoVigentePorMagistradoAfastado(OrgaoJulgador orgaoJulgador,
			OrgaoJulgadorColegiado orgaoJulgadorColegiado, Date dataReferencia) {
		
		return getDAO().obterSubstituicaoVigentePorMagistradoAfastado(orgaoJulgador, orgaoJulgadorColegiado, dataReferencia);
	}

}