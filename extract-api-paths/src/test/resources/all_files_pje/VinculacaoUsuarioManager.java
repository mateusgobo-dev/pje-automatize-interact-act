package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.VinculacaoUsuarioDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.VinculacaoUsuario;
import br.jus.pje.nucleo.enums.TipoVinculacaoUsuarioEnum;

@Name(VinculacaoUsuarioManager.NAME)
public class VinculacaoUsuarioManager extends BaseManager<VinculacaoUsuario>{

	public static final String NAME = "vinculacaoUsuarioManager";

	@In
	private VinculacaoUsuarioDAO vinculacaoUsuarioDAO;
	
	@In
	private UsuarioLocalizacaoVisibilidadeManager usuarioLocalizacaoVisibilidadeManager;
	
	@In
	private UsuarioLocalizacaoManager usuarioLocalizacaoManager;
	
	@In
	private LocalizacaoManager localizacaoManager;
	
	@In
	private UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;
	
	/**
	 * Dada uma <code>lotacao</code> de usuário que acabou de ser incluída ou alterada, 
	 * efetua a sincronização de lotações de usuários vinculados ao usuário em questão 
	 *  
	 * @param lotacao lotação que usuário que acabou de ser manipulada
	 * @param tipoVinculacao filtra pra sincronizar apenas vinculações referentes ao tipo em questão.
	 */
	public void sincronizarLotacoes(UsuarioLocalizacaoMagistradoServidor lotacao, TipoVinculacaoUsuarioEnum tipoVinculacao) {
		List<VinculacaoUsuario> vinculacoesUsuarios = obterVinculacoesUsuarios(lotacao.getUsuarioLocalizacao().getUsuario(), tipoVinculacao);
		for (VinculacaoUsuario vinculacaoUsuario : vinculacoesUsuarios){
			try{
				sincronizarLotacoes(vinculacaoUsuario, lotacao);
			}catch(Exception e){
			}
		}
	}
	
	/**
	 * Dado um usuário <code>usuario</code> que acabou de ter sua lotação em um dado OJ/OJC excluída, efetua
	 * sincronização para excluir as lotações dos usuários vinculados a esse usuário. 
	 * @param usuario
	 * @param orgaoJulgador
	 * @param orgaoJulgadorColegiado
	 * @param tipoVinculacaoUsuario sincroniza apenas usuários que foram vinculados usando o tipo de vinculação em questão
	 */
	public void sincronizarExclusaoLotacaoUsuario(Usuario usuario, OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado, 
			TipoVinculacaoUsuarioEnum tipoVinculacaoUsuario) {
		List<VinculacaoUsuario> vinculacoesUsuarios = obterVinculacoesUsuarios(usuario, tipoVinculacaoUsuario);
		for (VinculacaoUsuario vinculacaoUsuario : vinculacoesUsuarios){
			removerLotacoes(vinculacaoUsuario, orgaoJulgador, orgaoJulgadorColegiado);
		}
	}
	
	/**
 	 * Dada uma vinculação de usuário que está prestes a ser excluída, efetua exclusão
 	 * de lotações/localizações que foram criadas a partir dessa vinculação 
 	 * @param vinculacaoUsuario vinculação de usuário que está prestes a ser excluída
 	 */
 	public void sincronizarExclusaoVinculacaoUsuario(VinculacaoUsuario vinculacaoUsuario) {
 		removerLotacoes(vinculacaoUsuario, null, null);
 	}
	
	/**
	 * Dada uma vinculação de usuário <code>vinculacaoUsuario</code> que acabou de ser alterada ou incluída, 
	 * efetua a sincronização de lotações do usuário vinculado em relação ao usuário 
	 *  
	 * @param vinculacaoUsuario vinculação de usuário que acabou de ser alterada ou incluída
	 */
	public void sincronizarLotacoes(VinculacaoUsuario vinculacaoUsuario) {
		List<UsuarioLocalizacaoMagistradoServidor> lotacoesUsuario = obterLotacoesUsuario(vinculacaoUsuario.getUsuario());
		for (UsuarioLocalizacaoMagistradoServidor lotacaoUsuario : lotacoesUsuario){
			try{
				sincronizarLotacoes(vinculacaoUsuario, lotacaoUsuario);
			}catch(Exception e){
			}
		}
	}
	
	/**
	 * Dada uma vinculação entre usuários <code>vinculacaoUsuario</code>, esse método garante que
	 * o usuário vinculado <code>vinculacaoUsuario.usuarioVinculado</code> tenha lotação e respectivas visibilidades 
	 * similares à lotação em questão <code>lotacaoUsuario</code>.  
	 * @param vinculacaoUsuarior vinculacao de usuário que gerou a necessidade da sincronização
	 * @param lotacaoUsuario lotacao de usuário a ser utilizada como base para sincronização da lotação do usuário vinculado.
	 * @throws PJeBusinessException 
	 */
	private void sincronizarLotacoes(VinculacaoUsuario vinculacaoUsuario, UsuarioLocalizacaoMagistradoServidor lotacaoUsuario) throws PJeBusinessException {
		removerLotacoes(vinculacaoUsuario, lotacaoUsuario.getOrgaoJulgador(), lotacaoUsuario.getOrgaoJulgadorColegiado());
		incluirLotacoes(vinculacaoUsuario, lotacaoUsuario);
	}
	
	/**
	 * Dada uma vinculação entre usuários <code>vinculacaoUsuario</code>, esse método efetua a inclusão 
	 * de lotação e respectivas visibilidades para o usuário vinculado <code>vinculacaoUsuario.usuarioVinculado</code> 
	 * similares à lotação de usuário dada <code>lotacaoUsuario</code>.
	 * @param vinculacaoUsuario vinculacao de usuário a ser considerada
	 * @param lotacaoUsuario lotacao de usuário a ser replicada para o usuário vinculado.
	 * @throws PJeBusinessException 
	 */
	private void incluirLotacoes(VinculacaoUsuario vinculacaoUsuario, UsuarioLocalizacaoMagistradoServidor lotacaoUsuario) throws PJeBusinessException {
		Localizacao localizacaoModeloDestino = null;
		Localizacao localizacaoModeloDestinoRoot = lotacaoUsuario.getLocalizacaoFisica().getEstruturaFilho();
		// Se a localização da vinculação previamente cadastrada não fizer parte da árvore de localizações modelo da nova lotação do magistrado,
		// como solução de contorno, utiliza a localização modelo do próprio magistrado
		if(localizacaoModeloDestinoRoot != null 
				&& localizacaoManager.isLocalizacaoDescendente(vinculacaoUsuario.getLocalizacao(), localizacaoModeloDestinoRoot)) {
			localizacaoModeloDestino = vinculacaoUsuario.getLocalizacao();
		}else {
			localizacaoModeloDestino = lotacaoUsuario.getLocalizacaoModelo();
		}
		
		UsuarioLocalizacao novaLocalizacaoUsuarioVinculado = new UsuarioLocalizacao();
		novaLocalizacaoUsuarioVinculado.setUsuario(vinculacaoUsuario.getUsuarioVinculado());
		novaLocalizacaoUsuarioVinculado.setLocalizacaoFisica(lotacaoUsuario.getLocalizacaoFisica());
		novaLocalizacaoUsuarioVinculado.setLocalizacaoModelo(localizacaoModeloDestino);
		novaLocalizacaoUsuarioVinculado.setPapel(vinculacaoUsuario.getPapel());
		novaLocalizacaoUsuarioVinculado.setResponsavelLocalizacao(false);
		usuarioLocalizacaoManager.persistAndFlush(novaLocalizacaoUsuarioVinculado);

		UsuarioLocalizacaoMagistradoServidor novaLotacaoUsuarioVinculado = new UsuarioLocalizacaoMagistradoServidor();
		novaLotacaoUsuarioVinculado.setIdUsuarioLocalizacaoMagistradoServidor(novaLocalizacaoUsuarioVinculado.getIdUsuarioLocalizacao());
		novaLotacaoUsuarioVinculado.setDtInicio(lotacaoUsuario.getDtInicio());
		novaLotacaoUsuarioVinculado.setDtFinal(lotacaoUsuario.getDtFinal());
		novaLotacaoUsuarioVinculado.setMagistradoTitular(false);
		novaLotacaoUsuarioVinculado.setOrgaoJulgador(lotacaoUsuario.getOrgaoJulgador());
		novaLotacaoUsuarioVinculado.setOrgaoJulgadorColegiado(lotacaoUsuario.getOrgaoJulgadorColegiado());
		novaLotacaoUsuarioVinculado.setUsuarioLocalizacao(novaLocalizacaoUsuarioVinculado);
		novaLotacaoUsuarioVinculado.setVinculacaoUsuario(vinculacaoUsuario);
		usuarioLocalizacaoMagistradoServidorManager.persistAndFlush(novaLotacaoUsuarioVinculado);
		
		usuarioLocalizacaoVisibilidadeManager.replicarVisibilidades(lotacaoUsuario, novaLotacaoUsuarioVinculado);
	}

	/**
	 * Remove lotações originadas da vinculação <vinculacaoUsuario> em questão.
	 * @param vinculacaoUsuario vinculação a ser considerada para a remoção.
	 * @param orgaoJulgador remove somente lotações do órgão julgador em questão
	 * @param orgaoJulgadorColegiado remove somente lotações do órgão julgador colegiado em questão
	 */
	private void removerLotacoes(VinculacaoUsuario vinculacaoUsuario, OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		usuarioLocalizacaoMagistradoServidorManager.removerLotacoes(vinculacaoUsuario, orgaoJulgador, orgaoJulgadorColegiado);
	}

	/**
 	 * Método que obtem vinculações de usuários relacionadas a um dado usuário <code>usuario</code>
	 * @param usuario usuário a ser consultado
	 * @param tipoVinculacaoUsuario filtra apenas vinculações do tipo em questão
	 * @return lista de vinculações de usuários
	 */
	public List<VinculacaoUsuario> obterVinculacoesUsuarios(Usuario usuario, TipoVinculacaoUsuarioEnum tipoVinculacaoUsuario) {
		return getDAO().obterVinculacoesUsuarios(usuario, tipoVinculacaoUsuario);
	}

	/**
	 * Retorna todas as lotações de um dado usuário <code>usuario</code>
	 * @param usuario em questão
	 * @return lista de lotações do usuário
	 */
	private List<UsuarioLocalizacaoMagistradoServidor> obterLotacoesUsuario(Usuario usuario) {
		return usuarioLocalizacaoMagistradoServidorManager.obterLocalizacoesUsuario(usuario);
	}

	@Override
	protected VinculacaoUsuarioDAO getDAO() {
		return vinculacaoUsuarioDAO;
	}
	
}