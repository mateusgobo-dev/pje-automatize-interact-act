/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.CepDAO;
import br.jus.cnj.pje.business.dao.UsuarioDAO;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.cnj.pje.webservice.client.keycloak.KeycloakServiceClient;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.StatusSenhaEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoa;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaJuridica;;

/**
 * @author cristof
 * 
 */
@Name(UsuarioManager.NAME)
public class UsuarioManager extends AbstractUsuarioManager<Usuario, UsuarioDAO> {
	
	public static final String NAME = "usuarioManager";

	@In
	private UsuarioDAO usuarioDAO;
	
  	@In
  	private CepDAO cepDAO;

	@Override
	protected UsuarioDAO getDAO() {
		return usuarioDAO;
	}
	
	public Usuario findUsuarioHashAtivacao(String login, String hashAtivacaoSenha){
		return getDAO().findUsuarioHashAtivacao(login, hashAtivacaoSenha);
	}
	
	/**
	 * Altera a senha do usuario
	 * @param login do usuario
	 * @param senha do usuario em claro
	 * @return Usuario
	 */
	public Usuario alterarSenha(String login, String senha) throws PJeBusinessException{
		if(!PjeUtil.instance().validarSenha(senha)){
			throw new PJeBusinessException("pje.error.usuarioManager.senhaInvalida");
		}
		
		Usuario usuario = findByLogin(login);
		if(usuario == null){
			throw new PJeBusinessException("pje.error.usuarioManager.error.usuarioNaoEncontrado", null, login);
		}
		
		if(usuario.getStatusSenha() != StatusSenhaEnum.A){
			throw new PJeBusinessException("pje.error.usuarioManager.error.statusSenhaNaoAtivada", null, usuario.getStatusSenha().getLabel());
		}
		
		String old = usuario.getSenha();
		if(old.equals(senha)){
			throw new PJeBusinessException("pje.error.usuarioManager.error.senhaIgualAnterior");
		}
		
		usuario.setSenha(PjeUtil.instance().hashSenha(senha));
		persistAndFlush(usuario);
		return usuario;			
	}
	
	@Override
	public Usuario findById(Object id) throws PJeBusinessException {
		if(id != null){
			return super.findById(id);
		}else{
			return null;
		}
	}
	
	/**
	 * Ativa a senha cadastrada pelo usuario, localizando-o atraves do login e do hash de ativacao da senha enviado por email ao usuario
	 * presente no link de ativacao da senha.
	 * @param login do usuario
	 * @param hasCodigoAtivacao enviado para o email do usuario
	 * @param senha em claro que o usuario informou como sendo sua nova senha
	 * @return E
	 * @throws PJeBusinessException
	 */
	public Usuario ativarSenha(String login, String hasCodigoAtivacao, String senha) throws PJeBusinessException{
		Usuario usuario = findUsuarioHashAtivacao(login, hasCodigoAtivacao);
		if(usuario == null){
			throw new PJeBusinessException(
					"pje.abstractUsuarioManager.warn.erroAtivarSenha", null,
					"Usuario ou codigo de ativacao nao encontrado");
		}
		
		if(!PjeUtil.instance().validarSenha(senha.trim())){
			throw new PJeBusinessException(
					"pje.abstractUsuarioManager.warn.erroAtivarSenha", null,
					"A senha deve conter letras e numeros e ter de 8 a 64 caracteres");			
		}
		
		usuario.setSenha(PjeUtil.instance().hashSenha(senha.trim()));
		usuario.setStatusSenha(StatusSenhaEnum.A);
		usuario.setDataValidadeSenha(gerarDataValidadeSenha());
		usuario.setHashAtivacaoSenha(null);
		usuario.setFalhasSucessivas(0);
		persistAndFlush(usuario);
		ativarContaNoSSO(usuario);
		reiniciarSenha(usuario, senha.trim());
		return usuario;
	}	
	
	public List<Integer> getIdsUsuariosLocalizacao(Localizacao localizacao){
		Search s = new Search(UsuarioLocalizacao.class);
		s.setRetrieveField("usuario.idUsuario");
		addCriteria(s, Criteria.equals("localizacaoFisica.idLocalizacao", localizacao.getIdLocalizacao()));
		return list(s);
	}
	
	/**
	 * Recupera a localização inicial do usuário.
	 * 
	 * @param idUsuario Identificador do usuário.
	 * @return A localização inicial do usuário.
	 */
	public String recuperarLocalizacaoInicial(Integer idUsuario) {
		return usuarioDAO.recuperarLocalizacaoInicial(idUsuario);
	}
	
  	/**
	 * Método responsável por preencher o CEP do usuário através do
	 * {@link DadosReceitaPessoa}.
	 * 
	 * @param dadosReceita
	 *            os dados do usuário de acordo com a Receita Federal
	 * @param usuario
	 *            o usuário que se deseja preencher o endereço
	 */
  	public void preencherEndereco(DadosReceitaPessoa dadosReceita, Usuario usuario) {
  		boolean isPJ = dadosReceita instanceof DadosReceitaPessoaJuridica;
  		Cep cep = cepDAO.findByCodigo(StringUtil.formatCep(isPJ ? ((DadosReceitaPessoaJuridica) dadosReceita).getNumCep() : 
  			((DadosReceitaPessoaFisica) dadosReceita).getNumCEP()));
  		
  		if (cep != null) {
  			Endereco endereco = new Endereco();
  			endereco.setCep(cep);
  			endereco.setNomeBairro(isPJ ? ((DadosReceitaPessoaJuridica) dadosReceita).getDescricaoBairro() : ((DadosReceitaPessoaFisica) dadosReceita).getBairro());
  			endereco.setNomeLogradouro(isPJ ? ((DadosReceitaPessoaJuridica) dadosReceita).getDescricaoLogradouro() : ((DadosReceitaPessoaFisica) dadosReceita).getLogradouro());
  			endereco.setNumeroEndereco(isPJ ? ((DadosReceitaPessoaJuridica) dadosReceita).getNumLogradouro() : ((DadosReceitaPessoaFisica) dadosReceita).getNumLogradouro());
  			endereco.setComplemento(isPJ ? ((DadosReceitaPessoaJuridica) dadosReceita).getDescricaoComplemento() : ((DadosReceitaPessoaFisica) dadosReceita).getComplemento());
  			endereco.setUsuario(usuario);
  
  			usuario.getEnderecoList().add(endereco);
  		}
  	}

  	/**
  	 * metodo responsavel por recuperar o usuario da pessoa passada em parametro.
  	 * @param _pessoa
  	 * @return Usuario / null
  	 */
	public Usuario encontrarPorPessoa(Pessoa _pessoa) {
		return usuarioDAO.find(_pessoa.getIdPessoa());
	}
	
	public List<Integer> consultarIdsUsuariosPorPapelHerdado(Papel papel){
		return this.getDAO().consultarIdsUsuariosPorPapelHerdado(papel);
	}
	
	public Integer marcarFlagAtualizaSSOPorPapelHerdado(Papel papel) {
		return this.getDAO().marcarFlagAtualizaSSOPorPapelHerdado(papel);
	}
	
	public void marcarFlagAtualizaSSO(Integer idUsuario) {
		this.getDAO().marcarFlagAtualizaSSO(idUsuario);
	}
	
	public void ativarContaNoSSO(Usuario usuario) throws PJeBusinessException {
		if (ConfiguracaoIntegracaoCloud.getSSOAuthenticationEnabled()) {
			KeycloakServiceClient keycloakServiceClient = ComponentUtil.getComponent(KeycloakServiceClient.NAME);
			keycloakServiceClient.enableUser(usuario.getLogin());
		}
	}

	public void reiniciarSenha(Usuario usuario, String senha) throws PJeBusinessException {
		if (ConfiguracaoIntegracaoCloud.getSSOAuthenticationEnabled()) {
			KeycloakServiceClient keycloakServiceClient = ComponentUtil.getComponent(KeycloakServiceClient.NAME);
			keycloakServiceClient.resetPassword(usuario, senha);
		}
	}

	/**
 	 * Metodo responsavel por verificar se ha algum usuario ativo, associado ao CPF ou ao CNPJ
 	 * que tambem devera estar ativo e nao usuario falsamente, bem como existir uma localizacao
 	 * vinculada ao usuario.
 	 * @param cpfCnpj
 	 * @return
 	 */
	public boolean isUsuarioAtivoPje(String cpfCnpj) {
		return usuarioDAO.isUsuarioAtivoPje(cpfCnpj);
	}
}
