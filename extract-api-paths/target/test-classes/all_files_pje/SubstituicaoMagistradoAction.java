package br.com.infox.pje.action;

import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.view.GenericCrudAction;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorCargoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.SubstituicaoMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.SubstituicaoMagistrado;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

@Name(SubstituicaoMagistradoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class SubstituicaoMagistradoAction extends GenericCrudAction<SubstituicaoMagistrado> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "substituicaoMagistradoAction";
	
	private List<OrgaoJulgadorColegiado> orgaosJulgadoresColegiados;
	
	private UsuarioLocalizacaoMagistradoServidor perfilMagistradoSubstituto;	
	

	@Override
	public void persist() {
		salvarSubstituicaoCorrente(true);
	}


	@Override
	public void update() {
		salvarSubstituicaoCorrente(false);
	}
	
	@Override
	public void setInstance(SubstituicaoMagistrado substituicaoMagistrado) {
		super.setInstance(substituicaoMagistrado);
		
		if (substituicaoMagistrado != null){
			UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager = ComponentUtil.getComponent(UsuarioLocalizacaoMagistradoServidorManager.class);
			this.perfilMagistradoSubstituto = usuarioLocalizacaoMagistradoServidorManager.obterLocalizacaoAtivaPriorizandoColegiado(
				substituicaoMagistrado.getMagistradoSubstituto().getIdUsuario(),
				substituicaoMagistrado.getOrgaoJulgador(),
				substituicaoMagistrado.getOrgaoJulgadorColegiado());
		}
	}

	/**
	 * Retorna uma string mais amigável para seleção do cargo a ser
	 * substituídos, trazendo o nome do magistrado lotado no cargo quando
	 * houver.
	 */
	public String getLabelItemComboCargoPrincipal(OrgaoJulgadorCargo ojc){
		String retorno = ojc.getDescricao();
		UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager = ComponentUtil.getComponent(UsuarioLocalizacaoMagistradoServidorManager.class);
		UsuarioLocalizacaoMagistradoServidor magistrado = usuarioLocalizacaoMagistradoServidorManager.obterLocalizacaoMagistrado(ojc,null,true);
		if (magistrado != null) {
			retorno = magistrado.getUsuarioLocalizacao().getUsuario().getNome() + " / " + retorno;
		} else {
			retorno = "Cargo Vago / "+retorno;
		}		
		return retorno;
	}
	
	
	/**
	 * Atualiza ou persiste entidade de substituiçaõ de magistrado no banco de dados. 
	 * @param isNovaSubstituicao Se <code>true</code>, indica que é uma nova substituição. 
	 */
	public void salvarSubstituicaoCorrente(Boolean isNovaSubstituicao){
		SubstituicaoMagistrado substMag = getInstance();
		try{
			preencherInformacoesMagistradoSubstituto(substMag);
			preencherInformacoesMagistradoAfastado(substMag);
			SubstituicaoMagistradoManager substituicaoMagistradoManager = ComponentUtil.getComponent(SubstituicaoMagistradoManager.class);
			substituicaoMagistradoManager.validar(substMag);
			Date dataAtualizacao = new Date();
			substMag.setDataUltimaAtualizacao(dataAtualizacao);
			
			if (isNovaSubstituicao){
				substMag.setDataCriacao(dataAtualizacao);
				super.persist();
			}
			else{
				super.update();
			}
			
			substituicaoMagistradoManager.tratarVisibilidades(substMag);
			
		}catch(PJeBusinessException e){
			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
		}
		
	}

	private void preencherInformacoesMagistradoSubstituto(SubstituicaoMagistrado substMag) throws PJeBusinessException {
		substMag.setCargoMagistradoSubstituto(perfilMagistradoSubstituto.getOrgaoJulgadorCargo());
		PessoaMagistradoManager pessoaMagistradoManager = ComponentUtil.getComponent(PessoaMagistradoManager.class);
		PessoaMagistrado magistradoSubstituto = pessoaMagistradoManager
				.findById(perfilMagistradoSubstituto.getUsuarioLocalizacao().getUsuario().getIdUsuario());
		substMag.setMagistradoSubstituto(magistradoSubstituto);
	}
	
	private void preencherInformacoesMagistradoAfastado(SubstituicaoMagistrado substMag) throws PJeBusinessException {
		UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager = ComponentUtil.getComponent(UsuarioLocalizacaoMagistradoServidorManager.class);
		UsuarioLocalizacaoMagistradoServidor lotacaoTitular = usuarioLocalizacaoMagistradoServidorManager.obterLocalizacaoMagistrado(substMag.getCargoDistribuicao(), substMag.getOrgaoJulgadorColegiado(), true);
		
		if (lotacaoTitular != null){
			PessoaMagistradoManager pessoaMagistradoManager = ComponentUtil.getComponent(PessoaMagistradoManager.class);
			PessoaMagistrado magistradoAfastado = pessoaMagistradoManager
					.findById(lotacaoTitular.getUsuarioLocalizacao().getUsuario().getIdUsuario());
			substMag.setMagistradoAfastado(magistradoAfastado);
		}
	}
	
	
	
	

	/**
	 * Obtém lista de órgãos julgadores colegiados ativos para escolha no combo.
	 * @return lista de órgãos julgadores colegiados. 
	 */
	public List<OrgaoJulgadorColegiado> getOrgaosJulgadoresColegiados(){
		if (orgaosJulgadoresColegiados == null){
			OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager = ComponentUtil.getComponent(OrgaoJulgadorColegiadoManager.class);
			orgaosJulgadoresColegiados = orgaoJulgadorColegiadoManager.getOrgaoJulgadorColegiadoItems();
		}
		return orgaosJulgadoresColegiados;
	}
	
	/**
	 * Baseado num possível órgão julgador colegiado previamente escolhido,
	 * retorna uma lista de órgãos julgadores singulares.
	 * @return lista de órgãos julgadores singulares
	 */
	public List<OrgaoJulgador> getOrgaosJulgadores(){ 		
		if (getInstance().getOrgaoJulgadorColegiado() != null || ParametroUtil.instance().isPrimeiroGrau()){
			OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent(OrgaoJulgadorManager.class);
			return orgaoJulgadorManager.orgaosPorColegiado(getInstance().getOrgaoJulgadorColegiado());
		}
		return null;
	}
	
	/**
	 * Retorna possíveis cargos distribuíveis no órgão julgador escolhido   
	 * @return lista com cargos distribuíveis.
	 */
	public List<OrgaoJulgadorCargo> getCargosDistribuiveis(){
		if (getInstance().getOrgaoJulgador() != null){
			OrgaoJulgadorCargoManager orgaoJulgadorCargoManager = ComponentUtil.getComponent(OrgaoJulgadorCargoManager.class);
			return orgaoJulgadorCargoManager.recuperaAtivos(getInstance().getOrgaoJulgador(), true);	
		}
		return null;
	}
	
	/**
	 * Retorna possíveis perfis (localizações) para escolha de magistrado substituto.   
	 * @return lista com localizações de magistrado.
	 */
	public List<UsuarioLocalizacaoMagistradoServidor> getPerfisPossiveisMagistradoSubstituto(){
		UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager = ComponentUtil.getComponent(UsuarioLocalizacaoMagistradoServidorManager.class);
		return usuarioLocalizacaoMagistradoServidorManager.obterLocalizacoesMagistrados(getInstance().getOrgaoJulgador(), getInstance().getOrgaoJulgadorColegiado(), 
				false, true, false);
	}
	
	public UsuarioLocalizacaoMagistradoServidor getPerfilMagistradoSubstituto() {
		return perfilMagistradoSubstituto;
	}


	public void setPerfilMagistradoSubstituto(UsuarioLocalizacaoMagistradoServidor perfilMagistradoSubstituto) {
		this.perfilMagistradoSubstituto = perfilMagistradoSubstituto;
	}
	

	@Override
	public void onClickSearchTab() {
		newInstance();
		ReservaProcessoMagistradoSubstitutoAction reservaProcessoMagistradoSubstitutoAction = ComponentUtil.getComponent(ReservaProcessoMagistradoSubstitutoAction.class);
		reservaProcessoMagistradoSubstitutoAction.setExibeAbaReservaVinculacao(Boolean.FALSE);
	}
	
	@Override
	public void onClickFormTab() {
		ReservaProcessoMagistradoSubstitutoAction reservaProcessoMagistradoSubstitutoAction = ComponentUtil.getComponent(ReservaProcessoMagistradoSubstitutoAction.class);
		reservaProcessoMagistradoSubstitutoAction.setExibeAbaReservaVinculacao(Boolean.FALSE);
	}
}
