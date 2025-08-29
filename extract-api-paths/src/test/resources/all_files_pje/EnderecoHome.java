/**
 * pje-web
 * Copyright (C) 2009-2014 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.com.infox.ibpm.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.component.suggest.CepSuggestBean;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.manager.EnderecoManager;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.service.CepService;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * Componente de controle de tela da entidade {@link Endereco}.
 * 
 * @author Infox Tecnologia Ltda.
 *
 */
@Name("enderecoHome")
@BypassInterceptors
public class EnderecoHome extends AbstractEnderecoHome<Endereco> {

	private static final long serialVersionUID = 1L;

	/**
	 * Flag indicando se o número do logradouro e o complemento estão vazios.
	 * Assume o valor <code>true</code> apenas se ambos estiverem vazios.
	 */
	private Boolean numeroComplementoVazios = false;

	private boolean bairroCEPVazio = true;
	private boolean logradouroCEPVazio = true;
	private boolean numeroEnderecoCEPVazio = true;
	private boolean complementoCEPVazio = true;

	/**
	 * Flag indicando se o CEP armazenado na instância está duplicado com o CEP
	 * de algum dos endereços já cadastrados.
	 * 
	 * @see processoParteVinculoPessoaEnderecoGrid.component.xml
	 */
	private Boolean cepDuplicado = false;

	private List<Cep> cepsExistentes;

	private Boolean disabled;

	private Endereco instancePesquisar;
	
	/**
	 * Getter para o componente 'cepSuggest'.
	 * 
	 * @return
	 */
	private CepSuggestBean getCepSuggestBean() {
		return getComponent("cepSuggest");
	}

	/**
	 * Indica se a edição de formulário está desabilitada.
	 * 
	 * @return true, se a edição estiver desabilitada
	 */
	public Boolean getDisabled() {
		return disabled;
	}

	/**
	 * Permite definir que o formulário controlado por este componente
	 * está com a edição de seu conteúdo habilitada ou desabilitada.
	 * 
	 * @param disabled true, para definir como desabilitada.
	 */
	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	/**
	 * Recupera a lista de CEPs existentes.
	 * 
	 * @return a lista de ceps.
	 */
	public List<Cep> getCepsExistentes() {
		return cepsExistentes;
	}

	public void setCepsExistentes(List<Cep> cepsExistentes) {
		this.cepsExistentes = cepsExistentes;
	}

	public void obterCEPs(String valor) {
		CepService cepService = (CepService) Component.getInstance(CepService.class);
		Cep cepPesquisa = cepService.findByCodigo(valor);
		if(cepsExistentes == null){
			cepsExistentes = new ArrayList<Cep>();
		}
		if(cepPesquisa != null){
			cepsExistentes.add(cepPesquisa);
		}
		
		if (this.cepsExistentes.isEmpty()) {
			this.disabled = Boolean.FALSE;
		}
	}

	/**
	 * Setter para o objeto <code>Cep</code> no <code>CepSuggestBean</code>.
	 * Apresenta efeito colateral: sempre que um novo CEP for carregado, limpa
	 * os campos de número e complemento.
	 * 
	 * @param cep
	 */
	public void setCep(Cep cep) {
		getCepSuggestBean().setInstance(cep);		
	}

	/**
	 * Carrega um endereço para edição. Faz a busca pelos dados usando o CEP
	 * Suggest e em seguida sobrescreve os valores de bairro, logradouro,
	 * endereço e complemento.
	 * 
	 * @param cep
	 *            CEP do endereço em edição.
	 * @param nomeBairro
	 *            Nome do bairro do endereço em edição.
	 * @param nomeLogradouro
	 *            Nome do logradouro do endereço em edição.
	 * @param numeroEndereco
	 *            Número do endereço em edição.
	 * @param complemento
	 *            Complemento do endereço em edição.
	 */
	public void carregaEndereco(Cep cep, String nomeBairro, String nomeLogradouro, String numeroEndereco, String complemento) {
		// Carrega o CEP e salva nomes do estado e da cidade.
		setCep(cep);
		// Atualiza a instância com as informações recebidas como parâmetro,
		// juntamente com as que foram carregadas pelo CEP.
		Endereco endereco = instance().getInstance();
		endereco.setNomeEstado(getCep().getMunicipio().getEstado().getEstado());
		endereco.setNomeCidade(getCep().getMunicipio().getMunicipio());
		endereco.setNomeBairro(nomeBairro);
		endereco.setNomeLogradouro(nomeLogradouro);
		endereco.setNumeroEndereco(numeroEndereco);
		endereco.setComplemento(complemento);
		
		this.bairroCEPVazio = (cep.getNomeBairro() == null || cep.getNomeBairro().isEmpty());
		this.logradouroCEPVazio = (cep.getNomeLogradouro() == null || cep.getNomeLogradouro().isEmpty());
		this.numeroEnderecoCEPVazio = (cep.getNumeroEndereco() == null || cep.getNumeroEndereco().isEmpty());
		this.complementoCEPVazio = (cep.getComplemento() == null || cep.getComplemento().isEmpty());
	}

	/**
	 * Recupera o {@link Cep} atualmente selecionado no componente {@link CepSuggestBean}.
	 * 
	 * @return o {@link Cep} selecionado
	 */
	public Cep getCep() {
		return getCepSuggestBean().getInstance();
	}

	/**
	 * Observador do evento de seleção do {@link Cep} no {@link CepSuggestBean}.
	 * O evento é disparado por {@link CepSuggestBean#setInstance(Cep)} com o tipo de evento
	 * obtido em {@link CepSuggestBean#getEventSelected()}.
	 * 
	 */
	@Observer("cepChangedEvent")
	public void setEndereco(Cep cep) {
		if (cep == null) {
			Contexts.removeFromAllContexts("cepSuggest");
			this.setInstance(new Endereco());
		} else {
			CepSuggestBean cepSuggest = ComponentUtil.getComponent("cepSuggest");
			cepSuggest.setDefaultValue(cep.getNumeroCep());
			disabled = true;
			Endereco end = getInstance();
			end.setCep(cep);
			if(cep.getMunicipio() != null) {
				end.setNomeEstado(cep.getMunicipio().getEstado().getEstado());
				end.setNomeCidade(cep.getMunicipio().getMunicipio());
			}
			if(cep.getNomeBairro() == null || cep.getNomeBairro().isEmpty()){
				end.setNomeBairro(null);
				this.bairroCEPVazio = true;
			}else{
				end.setNomeBairro(cep.getNomeBairro());
				this.bairroCEPVazio = false;
			}
			if(cep.getNomeLogradouro() == null || cep.getNomeLogradouro().isEmpty()){
				end.setNomeLogradouro(null);
				this.logradouroCEPVazio = true;
			}else{
				end.setNomeLogradouro(cep.getNomeLogradouro());
				this.logradouroCEPVazio = false;
			}
			if(cep.getNumeroEndereco() == null || cep.getNumeroEndereco().isEmpty()) {
				end.setNumeroEndereco(null);
				this.getInstance().setNumeroEndereco("");
				this.numeroEnderecoCEPVazio = true;
			}else {
				end.setNumeroEndereco(cep.getNumeroEndereco());
				this.numeroEnderecoCEPVazio = false;
			}
			if(cep.getComplemento() == null || cep.getComplemento().isEmpty()) {
				end.setComplemento(null);
				this.getInstance().setComplemento("");
				this.complementoCEPVazio = true;
			}else {
				this.complementoCEPVazio = false;
				end.setComplemento(cep.getComplemento());
			}			
		}
	}

	/**
	 * Indica se há complementos de número vazios.
	 * 
	 * @return true, se houver
	 * @see {@link EnderecoHome#numeroComplementoVazios}
	 */
	public Boolean getNumeroComplementoVazios() {
		return this.numeroComplementoVazios;
	}

	/**
	 * Define se há complementos negocialmente necessários vazios.
	 * 
	 * @param numeroComplementoVazios
	 * @see {@link EnderecoHome#numeroComplementoVazios}
	 */
	public void setNumeroComplementoVazios(Boolean numeroComplementoVazios) {
		this.numeroComplementoVazios = numeroComplementoVazios;
	}

	/**
	 * Getter padrão para <code>cepDuplicado</code>.
	 * 
	 * @return
	 * @see {@link EnderecoHome#cepDuplicado}
	 */
	public Boolean getCepDuplicado() {
		return cepDuplicado;
	}

	/**
	 * Setter padrão para <code>cepDuplicado</code>.
	 * 
	 * @param cepDuplicado
	 * @see {@link EnderecoHome#cepDuplicado}
	 */
	public void setCepDuplicado(Boolean cepDuplicado) {
		this.cepDuplicado = cepDuplicado;
	}

	/**
	 * Indica se o modal para confirmação de inclusão deve ser mostrado.
	 * 
	 * @return <code>true</code> caso qualquer das flags
	 *         {@link EnderecoHome#cepDuplicado} ou
	 *         {@link EnderecoHome#numeroComplementoVazios} tenha valor
	 *         <code>true</code>.
	 */
	public Boolean getMostraModal() {
		return (getCepDuplicado() || getNumeroComplementoVazios());
	}

	/**
	 * Checa se o CEP é null.
	 * 
	 * @return <code>true</code> caso o CEP seja nulo.
	 */
	public boolean checkCep() {
		return (this.getInstance().getCep() != null);
	}

	/**
	 * Função que checa se existe algum campo de endereço preenchido. Se algum
	 * campo for diferente de vazio o campo CEP passa a ser obrigatório.
	 * 
	 * @return Caso todos os campos estiverem vazios ou <code>null</code>
	 *         retorna <code>true</code> e o CEP não será obrigatório. Se os
	 *         campos de endereço forem iguais a <code>null</code> retorna
	 *         <code>false</code>.
	 */
	public boolean checkEndereco() {
		return checkEndereco(getInstance());
	}

	/**
	 * Checa se o endereço tem os campos mínimos preenchidos.
	 * 
	 * @param endereco
	 *            endereço a ser verificado.
	 * 
	 * @return <code>true</code> caso o endereço satisfaça as restrições
	 *         mínimas.
	 */
	public boolean checkEndereco(Endereco endereco) {
		if ((!Strings.isEmpty(endereco.getNomeLogradouro())) 
				|| (!Strings.isEmpty(endereco.getNomeBairro())) 
				|| (!Strings.isEmpty(endereco.getComplemento()))) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isNumeroEnderecoCEPVazio() {
		return numeroEnderecoCEPVazio;
	}

	public boolean isComplementoCEPVazio() {
		return complementoCEPVazio;
	}

	public boolean isBairroCEPVazio() {
		return bairroCEPVazio;
	}

	public boolean isLogradouroCEPVazio() {
		return logradouroCEPVazio;
	}

	/* (non-Javadoc)
	 * @see br.com.itx.component.AbstractHome#setId(java.lang.Object)
	 */
	@Override
	public void setId(Object id) {
		super.setId(id);
		if (isManaged() && getInstance().getCep() != null) {
			Endereco e = getInstance();
			EntityUtil.getEntityManager().refresh(e);
			carregaEndereco(getInstance().getCep(), e.getNomeBairro(), e.getNomeLogradouro(), e.getNumeroEndereco(), e.getComplemento());
		}
	}

	/* (non-Javadoc)
	 * @see br.com.infox.ibpm.home.AbstractEnderecoHome#createInstance()
	 */
	@Override
	protected Endereco createInstance() {
		setInstance(super.createInstance());
		getInstance().setCep(new Cep());
		this.setDisabled(Boolean.FALSE);
		limparPesquisa();
		return instance;
	}

	/* (non-Javadoc)
	 * @see br.com.infox.ibpm.home.AbstractEnderecoHome#remove(br.jus.pje.nucleo.entidades.Endereco)
	 */
	@Override
	public String remove(Endereco obj) {
		String ret = "";
		if (obj != null && permiteRemocao(obj) && permiteRemoverEnderecoComLocalizacao(obj.getIdEndereco())) {
			ret = super.remove(obj);
			refreshGrid("enderecoGrid");
			refreshGrid("processoParteVinculoPessoaEnderecoGrid");
		} else {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Não é possível remover este endereço. Ele ainda está sendo utilizado por uma parte ou foi utilizado em um expediente.");
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see br.com.infox.ibpm.home.AbstractEnderecoHome#persist()
	 */
	@Override
	public String persist() {
		Usuario pessoaLogada = (Usuario) Contexts.getSessionContext().get("usuarioLogado");
		getInstance().setDataAlteracao(new Date());
		getInstance().setUsuarioCadastrador(pessoaLogada);
		String persist = "";
		if (getInstance().getCorrespondencia() == null) {
			getInstance().setCorrespondencia(Boolean.FALSE);
		}
		if (checkCep()) {
			persist = super.persist();
			refreshGrid("enderecoGrid");
			refreshGrid("processoParteVinculoPessoaEnderecoGrid");
			Contexts.removeFromAllContexts("cepSuggest");
		}
		return persist;
	}

	/**
	 * Método para checar se há mensagens a serem mostradas antes de persistir
	 * um endereço. Se o parâmetro <code>ignoraAvisos</code> tiver valor
	 * <code>false<code>, atualiza as flags 
	 * {@link EnderecoHome#cepDuplicado} e {@link EnderecoHome#numeroComplementoVazios}.
	 * Se houver mensagens a mostrar, não persiste o endereço, permitindo que a  interface mostre
	 * as mensagens.
	 * 
	 * [PJEII-2659] - [PJEII-2766]
	 * Retirando a condição que verificava se a pessoa do PreCadastroPessoa é null, pois inibia a verificação de cep
	 * duplicado (Solimar Alves dos Santos Banco do Brasil)
	 * 
	 * @param ignoraAvisos
	 *            flag indicando se a persistência deve ser realizada
	 *            independente da existência de mensagens a serem mostradas.
	 *            Caso tenha valor <code>true</code>, persiste o endereço sem
	 *            atualizar as mensagens.
	 * @return
	 * @see EnderecoHome#persist()
	 */
	@SuppressWarnings("unchecked")
	public String persist(boolean ignoraAvisos) {
		String resultado = "";

		if (this.getCepSuggestBean().getInstance() == null) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "CEP não selecionado ou inexistente.");
		} else {
			if (!ignoraAvisos) {

				GridQuery gridParteEndereco = ((GridQuery) getComponent("processoParteVinculoPessoaEnderecoGrid"));
				gridParteEndereco.refresh();

				List<Object> obj = gridParteEndereco.getResultList();
				if (obj.size() > 0){
					cepDuplicado = enderecoExistente();
				}
				numeroComplementoVazios = verificaNumeroComplementoVazios();
			}
			if (ignoraAvisos || !getMostraModal()) {
				resultado = persist();
				cepDuplicado = false;
				numeroComplementoVazios = false;
			}
		}
		return resultado;
	}

	/* (non-Javadoc)
	 * @see br.com.itx.component.AbstractHome#update()
	 */
	@Override
	public String update() {
		String update = "";
		if (checkCep()) {
			getInstance().setDataAlteracao(new Date());
			Usuario pessoaLogada = (Usuario) Contexts.getSessionContext().get("usuarioLogado");
			getInstance().setUsuarioCadastrador(pessoaLogada);
			update = super.update();
			refreshGrid("enderecoGrid");
			refreshGrid("processoParteVinculoPessoaEnderecoGrid");
		}
		return update;
	}

	/**
	 * Sobrecarga do método update(), visando checar se há mensagens a serem
	 * mostradas, antes de realizar a atualização.
	 * 
	 * @param ignoraAvisos
	 * @return
	 */
	public String update(boolean ignoraAvisos) {
		String resultado = "";
		if (!ignoraAvisos) {
			cepDuplicado = enderecoExistente();
			numeroComplementoVazios = verificaNumeroComplementoVazios();
		}
		if (ignoraAvisos || !getMostraModal()) {
			resultado = update();
			cepDuplicado = false;
			numeroComplementoVazios = false;
			newInstance();
		}
		return resultado;
	}

	/**
	 * Recupera a instância atualmente existente no contexto de conversação deste componente.
	 * 
	 * @return a instância atualmente existente
	 */
	public static EnderecoHome instance() {
		return ComponentUtil.getComponent("enderecoHome");
	}

	/* (non-Javadoc)
	 * @see br.com.itx.component.AbstractHome#newInstance()
	 */
	@Override
	public void newInstance() {
		Contexts.removeFromAllContexts("cepSuggest");
		Contexts.removeFromAllContexts("logradouroSuggest");
		super.newInstance();
	}

	/**
	 * Indica se o CEP está nulo.
	 * 
	 * @return <code>true</code> se <code>instance.geCep()</code> ou
	 *         <code>instance.getCep().getNumeroCep()</code> retornarem um valor
	 *         nulo.
	 */
	public boolean isCepNulo() {
		return (instance.getCep() == null || instance.getCep().getNumeroCep() == null);
	}

	/**
	 * Verifica se o novo endereço a ser inserido já existe entre os endereços
	 * retornados pela grid 'processoParteVinculoPessoaEnderecoGrid'.
	 * EndereçoscheckCep são considerados iguais se os CEPs forem idênticos.
	 * 
	 * @return <code>true</code> caso um endereçoo com o mesmo CEP esteja na
	 *         grid.
	 */
	private boolean enderecoExistente() {
		Usuario cadastrador = getEntityManager().find(Usuario.class,Authenticator.getPessoaLogada().getIdUsuario());
		return enderecoExistente(cadastrador);
	}
	
	@SuppressWarnings("unchecked")
	private boolean enderecoExistente(Usuario cadastrador) {
		List<Endereco> enderecoList;
		if (cadastrador == null) {
			enderecoList = EntityUtil.getEntityManager().createQuery("SELECT o FROM Endereco o WHERE o.usuario = :usuario")
					.setParameter("usuario", getInstance().getUsuario())
					.getResultList();
		} else {
			enderecoList = EntityUtil.getEntityManager().createQuery("SELECT o FROM Endereco o WHERE o.usuario = :usuario and o.usuarioCadastrador = :cadastrador")
					.setParameter("usuario", getInstance().getUsuario())
					.setParameter("cadastrador", cadastrador)
					.getResultList();
		}

		if (enderecoList != null && enderecoList.size() > 0) {
			for (Endereco endereco : enderecoList) {
				if (endereco.getCep().getNumeroCep().equalsIgnoreCase(instance().getCep().getNumeroCep())
						&& (!this.isManaged() || instance().getInstance().getIdEndereco() != endereco.getIdEndereco())) {
					return true;
				}
			}
		}
		return false;
	}
 
	/**
	 * Verifica se o número do logradouro e o complemento estão vazios.
	 * 
	 * @return <code>true</code> caso ambos estejam vazios.
	 */
	private Boolean verificaNumeroComplementoVazios() {
		return (this.instance.getNumeroEndereco() == null || this.instance.getNumeroEndereco().trim().length() == 0) && (this.instance.getComplemento() == null || this.instance.getComplemento().trim().length() == 0);
	}

	/**
	 * Verifica se um determinado endereço está sendo utilizado em algum
	 * processo.
	 * 
	 * @param endereco
	 *            endereco a ser verificado.
	 * @return <code>true</code> se o endereço não estiver sendo utilizado em
	 *         nenhum processo.
	 */
	private Boolean permiteRemocao(Endereco endereco) {
		EnderecoManager enderecoManager = (EnderecoManager) Component.getInstance("enderecoManager");
		return !enderecoManager.estaEmUso(endereco) && !enderecoManager.isUsadoEmExpediente(endereco);
	}

	/**
	 * Verifica se um endereço não está vinculado a alguma localização. É necessário essa verificação porque no cadastro de procuradorias,
	 * o endereço da procuradoria é o endereço de uma pessoa, o que acaba gerando esse vínculo do endereço com uma localização. 
	 * @param	idEndereco
	 * @return	verdadeiro se não existir vinculação do endereço passado por parâmetro com alguma localização, falso se houver.
	 */
	private boolean permiteRemoverEnderecoComLocalizacao(Integer idEndereco){
		boolean permite = false;
		LocalizacaoManager localizacaoManager = (LocalizacaoManager) Component.getInstance("localizacaoManager");
		List<Localizacao> localizacoesDoEndereco = localizacaoManager.obterLocalizacoes(idEndereco);
		
		if (ProjetoUtil.isVazio(localizacoesDoEndereco)) {
			permite = true;
		}
		
		return permite;
	}

	/**
	 * Executa a pesquisa dos endereços.
	 */
	public void pesquisar() {
		refreshGrid("processoParteVinculoPessoaEnderecoGrid");
	}
	
	/**
	 * Limpa a pesquisa dos endereços.
	 */
	public void limparPesquisa() {
		setInstancePesquisar(null);
		refreshGrid("processoParteVinculoPessoaEnderecoGrid");
	}

	/**
	 * @return instancePesquisar.
	 */
	public Endereco getInstancePesquisar() {
		if (instancePesquisar == null) {
			instancePesquisar = new Endereco();
			instancePesquisar.setCep(new Cep());
		}
		return instancePesquisar;
	}

	/**
	 * @param instancePesquisar Atribui instancePesquisar.
	 */
	public void setInstancePesquisar(Endereco instancePesquisar) {
		this.instancePesquisar = instancePesquisar;
	}
}
