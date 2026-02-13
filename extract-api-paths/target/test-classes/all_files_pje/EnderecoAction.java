package br.jus.cnj.pje.view;
 
 import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.infox.ibpm.component.suggest.CepSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.EnderecoManager;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.service.CepService;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.Usuario;
 
/**
 * 
 * @author luiz.mendes
 *
 */
@Name("enderecoAction")
@Scope(ScopeType.CONVERSATION)
public class EnderecoAction implements Serializable {
 
	private static final long serialVersionUID = -3527537500356632826L;
 
	@In
	private EnderecoManager enderecoManager;
	 	
	@In
	private LocalizacaoManager localizacaoManager;
	
	@In
	private ParametroService parametroService;
	
	@In
	private CepService cepService;
	 	
	private Procuradoria procDefProprietariaEndereco;
	private Usuario usuarioProprietarioEndereco;
	private boolean procDefTemEndereco = false;
	private boolean edicaoEndereco = false;
	private boolean novoEndereco = false;
	private boolean numeroEnderecoVazio = false;
	private boolean bairroEnderecoVazio = false;
	private boolean logradouroEnderecoVazio = false;
	private Endereco endereco;
	private final String[] PAPEIS_ADMINISTRADORES_PROCURADORIA = new String[]{
	 	Papeis.ADMINISTRADOR,
	 	Papeis.PJE_ADMINISTRADOR_PROCURADORIA, Papeis.REPRESENTANTE_PROCESSUAL_GESTOR};
	
	private List<Endereco> enderecosCadastrados = new ArrayList<Endereco>(0);
	
	CepSuggestBean cepSuggestBean = ComponentUtil.getComponent("cepSuggest");
	 	
	public EnderecoAction() {}
	
	public boolean isEnderecoProcuradoria() {
		return (procDefProprietariaEndereco != null && !enderecosCadastrados.isEmpty());
	}
	
	public List<Endereco> getEnderecosCadastrados(){
		return enderecosCadastrados;
	}
	
	public Endereco getEndereco() {
		return endereco;
	}
	
	public String getNomeEstado() {
		if(endereco != null) {
			return endereco.getNomeEstado();
		}else {
			return "";
		}
	}
	
	public void setNomeEstado(String nomeEstado) {
		endereco.setNomeEstado(nomeEstado);
	}
	
	public String getNomeCidade() {
		if(endereco != null) {
			return endereco.getNomeCidade();
		}else {
			return "";
		}
	}
	
	public void setNomeCidade(String nomeCidade) {
		endereco.setNomeCidade(nomeCidade);
	}
	
	public String getNomeBairro() {
		if(endereco != null) {
			return endereco.getNomeBairro();
		}else {
			return "";
		}
	}
	
	public void setNomeBairro(String nomeBairro) {
		endereco.setNomeBairro(nomeBairro);
	}
	
	public String getLogradouro() {
		if(endereco != null) {
			return endereco.getNomeLogradouro();
		}else {
			return "";
		}
	}
	
	public void setLogradouro(String logradouro) {
		endereco.setNomeLogradouro(logradouro);
	}
	
	public String getNumeroEndereco() {
		if(endereco != null) {
			return endereco.getNumeroEndereco();
		}else {
			return "";
		}
	}
	
	public void setNumeroEndereco(String numero) {
		endereco.setNumeroEndereco(numero);
	}
	
	public String getComplemento() {
		if(endereco != null) {
			return endereco.getComplemento();
		}else {
			return "";
		}
	}
	
	public void setComplemento(String complemento) {
		endereco.setComplemento(complemento);
	}
	 
	/**
 	 * metodo responsavel por aplicar as regras de exibiçao do botao gravar (botao que grava as alteraoes realizadas no endereco previamente cadastrado)
 	 * @return true/false
 	 */
 	public boolean exibeBotaoGravar() {
 	 	return (endereco != null && endereco.getCep() != null && (novoEndereco || edicaoEndereco));
 	}
 	
 	/**
 	 * metodo responsavel por persistir as alteracoes realizadas no objeto Endereco.
 	 * caso o endereo nao exista no banco de dados, o mesmo será criado.
 	 * caso exista:
 	 * 		se for de procuradoria, é persisitido o endereco e atualizada a localizacao da mesma, com o endereco recem criado.
 	 * @param ignoraAvisoFaltaNumeroEndereco
 	 */
 	public void salvarEndereco() {
		insereInformacoesComplementaresEnderecoParaPersistencia();
		try {
			enderecoManager.salvarEndereco(endereco);
			if(procDefProprietariaEndereco != null) {
				alteraLocalizacaoProcuradoria();
			}
			contrutorMensagem(Severity.INFO, "Endereço salvo!");
		} catch (Exception e) {
			contrutorMensagem(Severity.INFO, "Erro ao salvar o Endereço!");
			e.printStackTrace();
		}
		if(procDefProprietariaEndereco != null) {
			populaObjetosEnderecos(procDefProprietariaEndereco);
		}else {
			populaObjetosEnderecos(usuarioProprietarioEndereco);
		}
		edicaoEndereco = Boolean.FALSE;
		if(novoEndereco) {
			novoEndereco = Boolean.FALSE;
		}	
 	}
 
 	/**
 	 * caso o usuario esteja criando um NOVO endereco para uma procuradoria/defensoria, 
 	 * a localizacao desta deverá ser alterada, acrescentando o endereço novo.
 	 * quando a procuradoria já dispor de um endereço, o mesmo ser sempre editado,
 	 * nao sendo criado um novo.
 	 * @throws Exception 
 	 */
 	private void alteraLocalizacaoProcuradoria() throws Exception {
 		if(novoEndereco && procDefProprietariaEndereco != null) {
 			procDefProprietariaEndereco.getLocalizacao().setEndereco(endereco);
 			localizacaoManager.salvarLocalizacao(procDefProprietariaEndereco.getLocalizacao());
 		}
 	}
 
 	/**
 	 * metodo auxiliar para inserir, caso nao existam, informaoes complementares no endereco,
 	 * como criador, autualizado em, etc.
 	 */
 	private void insereInformacoesComplementaresEnderecoParaPersistencia() {
 		if(novoEndereco) {
 			endereco.setUsuarioCadastrador(Authenticator.getUsuarioLogado());
 			insereProprietarioEndereco();
 		}
 		endereco.setDataAlteracao(new Date());
 	}
 	
 	/**
 	 * metodo auxiliar responsavel por inserir, caso nao exista, o proprietario do endereco.
 	 * se o endereco pertencer a uma procuradoria, a mesma dever ser inserida no atributo procuradoria. o atributo usuario dever ser null
 	 * se o endereco pertencer a uma pessoa, a mesma devera ser inserida no atributo usuario. o atributo procuradoria dever ser null.
 	 */
 	private void insereProprietarioEndereco() {
 		endereco.setProcuradoria(null);
 		endereco.setUsuario(null);
 		
 		if(procDefProprietariaEndereco != null ) {
 			if(endereco.getProcuradoria() == null) {
 				endereco.setProcuradoria(procDefProprietariaEndereco);
 			}
 		}else {
 			if(endereco.getUsuario() == null) {
 				endereco.setUsuario(usuarioProprietarioEndereco);
 			}
 		}
 	}
 
 	/**
 	 * metodo auxiliar, responsavel por criar as mensagens a serem exibidas em tela
 	 * @param severity
 	 * @param mensagem
 	 */
 	private void contrutorMensagem(Severity severity, String mensagem) {
 		FacesMessages.instance().clear();
 		FacesMessages.instance().add(severity, mensagem);
 	}
 
 	/**
 	 * metodo responsavel por popular os objetos necessarios para exibir o endereco da pessoa/procuradoria
 	 * @param object
 	 */
 	public void populaObjetosEnderecos(Object object) {
 		if(object == null) {
 			return;
 		} 
 		resetObjetosEnderecos();
 		if(object instanceof Procuradoria) {
 			this.procDefProprietariaEndereco = (Procuradoria)object;
 			if(procDefProprietariaEndereco.getLocalizacao() != null) {
 				if(procuradoriaTemEndereco()) {
 					this.procDefTemEndereco = true;
 					this.enderecosCadastrados = new ArrayList<Endereco>(0);
 					this.enderecosCadastrados.add(procDefProprietariaEndereco.getLocalizacao().getEndereco());
 				} else {
 					this.novoEndereco = Boolean.TRUE;
 					cepSuggestBean.setInstance(new Cep());
 				}
 			}
 		}
 	}
 	
 	private boolean procuradoriaTemEndereco() {
		return (procDefProprietariaEndereco.getLocalizacao().getEndereco() != null && procDefProprietariaEndereco.getLocalizacao().getEndereco().getCep() != null);
	}
 	
 	/**
 	 * metodo auxiliar responsavel por retornar os objetos ao estado inicial.
 	 */
 	private void resetObjetosEnderecos() {
 		procDefProprietariaEndereco = null;
 		usuarioProprietarioEndereco = null;
 		procDefTemEndereco = false;
 		edicaoEndereco = false;
 		novoEndereco = false;
 		numeroEnderecoVazio = false;
 		endereco = null;
 		enderecosCadastrados = new ArrayList<Endereco>(0);
 	}
 	
 	/**
 	 * metodo auxiliar que verifica se:
 	 * -> o numero do endereco foi preenchido(true/false)
 	 * -> o logradouro foi preenchido (true/false)
 	 * -> o bairro foi preenchido (true/false)
 	 * 
 	 * ao verificar, seta as variaveis ('numeroEnderecoVazio', 'logradouroEnderecoVazio', 'bairroEnderecoVazio')  
 	 * com o resultado obtido.
 	 */
 	public void verificaFaltaInformacoesEndereco() {
 		if(endereco != null) {
	 		bairroEnderecoVazio = (endereco.getNomeBairro() == null || StringUtils.isBlank(endereco.getNomeBairro()));
	 		logradouroEnderecoVazio = (endereco.getNomeLogradouro() == null || StringUtils.isBlank(endereco.getNomeLogradouro()));
	 		numeroEnderecoVazio = (endereco.getNumeroEndereco() == null || StringUtils.isBlank(endereco.getNumeroEndereco()));
 		}
 	}
 	
 	/**
	 * Método responsável por verificar e encaminhar para atualizacao das informações de endereço.
	 */
	public void atualizarDadosEndereco(){
		if(endereco == null) {
			this.endereco = new Endereco();
		}
		if(cepSuggestBean.getInstance() != null) {
			atualizaEndereco(cepSuggestBean.getInstance());
		}else {
			List<Cep> ceps = cepService.findByNumero(cepSuggestBean.getDefaultValue());
			if(ceps != null && !ceps.isEmpty()) {
				atualizaEndereco(ceps.get(0));
				cepSuggestBean.setInstance(ceps.get(0));
			} else {
				cepSuggestBean.setDefaultValue("");
			}
		}
	}
	
	/**
	 * metodo responsavel por atualizar as informacoes do endereco com as informacoes do CEP 
	 * passado em parametro.
	 * @param cep
	 */
	private void atualizaEndereco(Cep cep) {
		this.endereco.setCep(cepSuggestBean.getInstance());
		this.endereco.setNomeEstado(cepSuggestBean.getInstance().getMunicipio().getEstado().getEstado());
		this.endereco.setNomeCidade(cepSuggestBean.getInstance().getMunicipio().getMunicipio());
		this.endereco.setNomeBairro(cepSuggestBean.getInstance().getNomeBairro());		
		this.endereco.setNomeLogradouro(cepSuggestBean.getInstance().getNomeLogradouro());
		this.endereco.setComplemento(cepSuggestBean.getInstance().getComplemento());
		this.endereco.setNumeroEndereco(cepSuggestBean.getInstance().getNumeroEndereco());
	}

	/**
	 * metodo responsavel por verificar se o cep já está inserido
	 * @return
	 */
	public boolean isCepNuloOuVazio() {
		return (cepSuggestBean.getInstance() == null || StringUtils.isBlank(cepSuggestBean.getInstance().getNumeroCep()));
	}
	
	public boolean permissaoPadraoVisualizacaoFormularioEndereco() {
		return (Authenticator.isPermissaoCadastroTodosPapeis() || Authenticator.isRepresentanteGestor());
	}

	public boolean isNumeroEnderecoVazio() {
		return numeroEnderecoVazio;
	}
	
	public boolean isBairroEnderecoVazio() {
		return bairroEnderecoVazio;
	}
	
	public boolean isLogradouroEnderecoVazio() {
		return logradouroEnderecoVazio;
	}
	
	public boolean isEnderecoCompleto() {
		verificaFaltaInformacoesEndereco();
		if(numeroEnderecoVazio || bairroEnderecoVazio || logradouroEnderecoVazio) {
			return false;
		}else {
			return true;
		}
	}
	
	public void editarEndereco(Endereco endereco) {
		this.edicaoEndereco = Boolean.TRUE;
		this.endereco = endereco;
		cepSuggestBean.setInstance(endereco.getCep());
	}

	public String getUrlCorreiosBuscaCep() {
		String retorno = "";
		try{
			retorno = parametroService.findByName("urlBuscaCepCorreios").getValorVariavel() ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retorno;
	}
	
	public boolean exibeFormularioEndereco() {
		//clique direto no botao de edicao de endereco
		if(this.edicaoEndereco) {
			return true;
		}
 		//procuradoria/defensoria
 		if(
 				this.procDefProprietariaEndereco != null && 
 				this.procDefTemEndereco == false && 
 				verificaPermissaoVisualizacaoFormulario() && 
 				enderecosCadastrados.isEmpty()) {
 			return true;
 		}
 		return false;
	}
	
	public boolean exibeBotaoEdicaoEndereco() {
 		if(verificaPermissaoVisualizacaoFormulario()) {
			
			/* PROCURADORIA / DEFENSORIA
			 * LOGICA:
			 * se a pessoa que esta sendo editada for uma procuradoria e o usuario atual tiver permissao para ver o formulario de
			 * endereco, exibe o botao de edicao */
	 		if(this.procDefProprietariaEndereco != null) {
	 			return true;
	 		}
 		}
 		return false;
	}
	
 	/**
 	 * metodo responsavel por verificar se o usuario atual tem os papeis necessrios para acessar funcionalidades
 	 * @return true se o usuario estiver com o papel atual permitido pelas regras de visualizacao
 	 */
 	private boolean verificaPermissaoVisualizacaoFormulario() {
 		if(Authenticator.isPapelAdministrador() || Authenticator.hasRole(PAPEIS_ADMINISTRADORES_PROCURADORIA)) {
 			return true;
 		}
 		return false;
 	}
 }