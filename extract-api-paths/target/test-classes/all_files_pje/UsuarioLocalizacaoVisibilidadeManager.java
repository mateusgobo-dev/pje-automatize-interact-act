/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça.
 *
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.LocalizacaoDAO;
import br.jus.cnj.pje.business.dao.UsuarioLocalizacaoVisibilidadeDAO;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.SubstituicaoMagistrado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoVisibilidade;

/**
 * Componente de gerenciamento da entidade {@link UsuarioLocalizacaoVisibilidade}.
 * 
 * @author cristof
 *
 */
@Name(UsuarioLocalizacaoVisibilidadeManager.NAME)
public class UsuarioLocalizacaoVisibilidadeManager extends BaseManager<UsuarioLocalizacaoVisibilidade> {
	
	public static final String NAME = "usuarioLocalizacaoVisibilidadeManager";
	
	@In
	private UsuarioLocalizacaoVisibilidadeDAO usuarioLocalizacaoVisibilidadeDAO;
	
	@In
	private LocalizacaoDAO localizacaoDAO;

	@Override
	protected UsuarioLocalizacaoVisibilidadeDAO getDAO() {
		return usuarioLocalizacaoVisibilidadeDAO;
	}

	/**
	 * Indica se existe alguma visibilidade de atuação associada à localização interna dada. 
	 * A visibilidade deve estar ativa no momento da verificação.
	 *  
	 * @param loc a localização a respeito da qual se pretende identificar a existência de visibilidade
	 * @return true, se houver pelo menos uma visibilidade ativa associada. 
	 */
	public boolean temVisibilidade(UsuarioLocalizacaoMagistradoServidor loc) {
		if(loc == null){
			return false;
		}
		return usuarioLocalizacaoVisibilidadeDAO.temVisibilidade(loc);
	}
	
	/**
	 * Indica se existe alguma visibilidade vinculada à localização interna dada associada com
	 * pelo menos um cargo judicial.
	 * 
	 * @param loc a localização respeito da qual se pretende identificar a existência de visibilidade
	 * @return true, se houver pelo menos uma visibilidade ativa e vinculada a um cargo
	 */
	public boolean temOrgaoVisivel(UsuarioLocalizacaoMagistradoServidor loc){
		if(loc == null){
			return false;
		}
		return usuarioLocalizacaoVisibilidadeDAO.temOrgaoVisivel(loc);
	}
	
 	/**
 	 * Retorna todas as visibilidades de uma dada localização/lotação. 
 	 *  
 	 * @param localizacao localização em questão
 	 * @return lista de visibilidades 
 	 */
 	public List<UsuarioLocalizacaoVisibilidade> obterVisibilidades(UsuarioLocalizacaoMagistradoServidor localizacao) {
 		return getDAO().obterVisibilidades(localizacao, false);
 	}
 	
 	public List<UsuarioLocalizacaoVisibilidade> obterVisibilidadesAtivas(UsuarioLocalizacaoMagistradoServidor localizacao){
 		return getDAO().obterVisibilidades(localizacao, true);
 	}
 	
	/**
	 * Método responsável por retornar as localizações do servidor, salvo as vinculadas ao papel de magistrado.
	 * 
	 * @param idUsuario idUsuario Identificador do usuário.
	 * @return As localizações do servidor, salvo as vinculadas ao papel de magistrado.
	 */
 	public List<Localizacao> getLocalizacaoServidorItems(Integer idUsuario) {		
 		return localizacaoDAO.getLocalizacaoServidorItems(idUsuario);
 	}
 	
 	/**
 	 * Retorna as localizações do servidor, eliminando as vinculadas ao magistrado, filtrando pelo OJC, OJ e LocalizacaoFisica
 	 * @param idUsuario
 	 * @param orgaoJulgadorColegiado
 	 * @param orgaoJulgador
 	 * @param localizacaoFisica
 	 * @return
 	 */
 	public List<Localizacao> getLocalizacaoServidorItems(Integer idUsuario, OrgaoJulgadorColegiado orgaoJulgadorColegiado, OrgaoJulgador orgaoJulgador, Localizacao localizacaoFisica){
 		return localizacaoDAO.getLocalizacaoServidorItems(idUsuario, orgaoJulgadorColegiado, orgaoJulgador, localizacaoFisica);
 	}

	/**
	 * Remove todas as visibilidades geradas pra substituições que já
	 * se encerraram.
	 */
	public void removerVisibilidadesSubstituicoesEncerradas() {
		usuarioLocalizacaoVisibilidadeDAO.removerVisibilidadesSubstituicoesEncerradas();
	}
	
	/**
	 * Remove todas as visibilidades geradas para uma dada substituição
	 * @param substituicaoMagistrado substituição cujas visibilidades serão removidas
	 */
	public void removerVisibilidadesSubstituicao(SubstituicaoMagistrado substituicaoMagistrado) {
		usuarioLocalizacaoVisibilidadeDAO.removerVisibilidadesSubstituicao(substituicaoMagistrado);
	}
	
	/**
	 * Dada uma visibilidade, verifica se existe alguma outra visibilidade
	 * com periodo conflitante abrangendo o mesmo cargo.
	 * @param visibilidade a visibilidade a ser analisada
	 * @return true caso existam visibilidades conflitantes.
	 */
	public boolean existeVisibilidadeConflitante(UsuarioLocalizacaoVisibilidade visibilidade) {
		return getDAO().existeVisibilidadeConflitante(visibilidade);
	}
	
	/**
	 * Dada uma lotação de origem <code>lotacaoOrigem</code>, replica suas visibilidades para uma lotação de destino <code>lotacaoDestino</code>
	 * @param lotacaoOrigem lotação de origem
	 * @param lotacaoDestino lotação de destino
	 */
	public void replicarVisibilidades(UsuarioLocalizacaoMagistradoServidor lotacaoOrigem,	UsuarioLocalizacaoMagistradoServidor lotacaoDestino) {
		
		for (UsuarioLocalizacaoVisibilidade visibilidadeOrigem :  obterVisibilidades(lotacaoOrigem)){
			UsuarioLocalizacaoVisibilidade novaVisibilidade = new UsuarioLocalizacaoVisibilidade();
			novaVisibilidade.setDtInicio(visibilidadeOrigem.getDtInicio());
			novaVisibilidade.setDtFinal(visibilidadeOrigem.getDtFinal());
			novaVisibilidade.setOrgaoJulgadorCargo(visibilidadeOrigem.getOrgaoJulgadorCargo());
			novaVisibilidade.setSubstituicaoMagistrado(visibilidadeOrigem.getSubstituicaoMagistrado());
			novaVisibilidade.setUsuarioLocalizacaoMagistradoServidor(lotacaoDestino);
			
			try{
				persistAndFlush(novaVisibilidade);
			}catch(Exception e){
				
			}
		}
		
	}
	
	/**
	 * Verifica se a lotaçao dada <code>lotacao</code> possui visibilidade ativa para o cargo judicial dado <code>cargoJudicial</code>
	 * @param lotacao
	 * @param cargoJudicial
	 * @return true caso a lotação dada possua visibilidade ativa para o cargo dado.
	 */
	public Boolean possuiVisibilidadeAtiva(UsuarioLocalizacaoMagistradoServidor lotacao, OrgaoJulgadorCargo cargoJudicial){
		return getDAO().possuiVisibilidadeAtiva(lotacao, cargoJudicial);
	}
	
}
