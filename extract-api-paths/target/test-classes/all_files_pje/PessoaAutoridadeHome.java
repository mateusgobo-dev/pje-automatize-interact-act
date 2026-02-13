package br.com.infox.cliente.home;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.cliente.bean.PreCadastroPessoaBean;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.PessoaJuridicaManager;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.pje.nucleo.entidades.AutoridadePublica;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.enums.StatusSenhaEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Name("pessoaAutoridadeHome")
@BypassInterceptors
public class PessoaAutoridadeHome extends AbstractHome<PessoaAutoridade> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3205872604343757785L;
	
	private String cnpjPesquisa;
	private PessoaJuridica orgaoVinculadoPesquisa;
	private PessoaJuridica orgaoVinculacao;
	private String cadastro;
	private Boolean buscarPorCnpj = Boolean.FALSE;
	private Boolean ativo = Boolean.TRUE;
	
	@In
	private PessoaJuridicaManager pessoaJuridicaManager;
	
	@Override
	public void setId(Object id) {
		super.setId(id);
		
		if (getInstance().getAtivo() != null) {
			setAtivo(getInstance().getAtivo());
		}
	}

	/**
	 * Metodo persiste os dados da pessoa autoridade
	 */
	@Override
	public String persist() {
		String retorno = null;
		if(verificaSelecaoOrgaoVinculacao()) {
			if(ParametroUtil.instance().getIdTipoPessoaAutoridade() == 0) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add("Não foi possível localizar um tipo de pessoa autoridade!");
				return retorno;
			}
			if ((getInstance().getNome() == null) || (getInstance().getNome().length() <= 0))
				getInstance().setNome("USUÁRIO PADRÃO");
			if (getInstance().getLogin() == null) {
				getInstance().setLogin(UUID.randomUUID().toString());
			}
			getInstance().setBloqueio(false);
			getInstance().setProvisorio(false);

			TipoPessoa tipoPessoa = getEntityManager().find(TipoPessoa.class, ParametroUtil.instance().getIdTipoPessoaAutoridade());
			getInstance().setTipoPessoa(tipoPessoa);
			getInstance().setInTipoPessoa(TipoPessoaEnum.A);
			getInstance().setStatusSenha(StatusSenhaEnum.B);
			getInstance().setAtivo(getAtivo());
			retorno = super.persist();
		}
		return retorno;
	}
	
	/**
	 * metodo responsavel por verificar se o usuario selecionou um orgao de vinculacao
	 * @return true / false se nao houver selecionado
	 */
	private boolean verificaSelecaoOrgaoVinculacao() {
		boolean retorno = true;
		if(getInstance().getOrgaoVinculacao() == null ) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add("Informe o órgão de vinculação");
			limparCamposPesquisa();
			retorno = false;
		}
		return retorno;
	}
	
	/**
	 * metodo responsavel por limpar os dois campos de pesquisa de orgao de vinculacao
	 */
	private void limparCamposPesquisa() {
		limparCampoPesquisaOrgaoVinculadoPesquisa();
		limparCampoPesquisaCNPJOrgaoVinculadoPesquisa();
	}
	
	/**
	 * metodo responsavel por limpar o campo de pesquisa do CNPJ do orgao de vinculacao para pesquisa
	 */
	private void limparCampoPesquisaCNPJOrgaoVinculadoPesquisa() {
		cnpjPesquisa = null;		
	}

	/**
	 * metodo responsavel por limpar o campo de pesquisa da pessoa juridica do orgao de vinculacao para pesquisa
	 */
	private void limparCampoPesquisaOrgaoVinculadoPesquisa() {
		orgaoVinculadoPesquisa = null;		
	}
	
	/**
	 * Método invocando o metodo persist pois a entidade não possui um auto increment
	 * Se chamar o persist acima diretamente, é retornado erro de Entidade persistida não pode ser nula
	 */
	public void persistir(){
		persist();
	}
	
	@Override
	protected String afterPersistOrUpdate(String ret) {
		refreshGrid("pessoaAutoridadeGrid");
		setCnpjPesquisa(null);
		setOrgaoVinculadoPesquisa(null);
		PessoaHome.instance().setInstance(instance);
		PreCadastroPessoaBean preB = (PreCadastroPessoaBean) Component.getInstance("preCadastroPessoaBean");
		preB.setInTipoPessoa(TipoPessoaEnum.A);
		preB.setPessoaAutoridade(instance);		
		FacesMessages.instance().clear();
		adicionarMensagemSucessoCadastro();
		return super.afterPersistOrUpdate(ret);
	}

	/**
	 * Adiciona ao FacesMessages a mensagem de sucesso ao cadastrar uma pessoaAutoridade, através do cadastro de Pessoa.
	 * Sendo assim, a origem do cadastro não deverá ser na funcionalidade de Partes, e sim, na configuração > pessoa.
	 */
	private void adicionarMensagemSucessoCadastro() {
		if (!isCadastroParte(cadastro)) {
			FacesMessages.instance().addFromResourceBundle("pessoaAutoridade.operacao.sucesso");
		}
	}
	
	/**
	 * Verifica se a origem do cadastro é na tela de inserção de partes.
	 * @param	cadastro
	 * @return	verdadeiro se a variável origemCadastro estiver preenchida e com o valor "parte".
	 */
	private boolean isCadastroParte(String cadastro){
		return (StringUtils.isNotBlank(cadastro) && cadastro.equalsIgnoreCase("parte"));
	}

	public void setPessoaAutoridadeIdPessoaAutoridade(Integer id) {
		setId(id);
	}

	public Integer getPessoaAutoridadeIdPessoaAutoridade() {
		return (Integer) getId();
	}

	public static PessoaAutoridadeHome instance() {

		return ComponentUtil.getComponent("pessoaAutoridadeHome");
	}

	@Override
	public void setInstance(PessoaAutoridade instance) {
		if( instance != null ) {
			orgaoVinculacao = instance.getOrgaoVinculacao();
		}
		PreCadastroPessoaBean preB = (PreCadastroPessoaBean) Component.getInstance("preCadastroPessoaBean");
		PessoaHome pessoaHome = (PessoaHome) Component.getInstance("pessoaHome");
		preB.setInTipoPessoa(TipoPessoaEnum.A);
		pessoaHome.setInstance(instance);
		preB.setPessoaAutoridade(instance);		
		super.setInstance(instance);
	}
	
	public String inactiveRecursive(PessoaAutoridade obj) {
		for (AutoridadePublica autoridadePublica : obj.getAutoridadePublicaList()) {
			autoridadePublica.setAtivo(false);
		}

		obj.setAtivo(false);

		getEntityManager().flush();

		GridQuery grid = (GridQuery) Component.getInstance("pessoaAutoridadeGrid");

		grid.refresh();
		FacesMessages.instance().clear();
		FacesMessages.instance().add("Operação realizada com sucesso!");
		return "update";
	}
	/**
	 * Método responsável pesquisar o órgao de de vinculação pelo CNPJ fornecido. Caso o órgão tenha sido recuperado do suggest, o método apenas 
	 * atribui os dados a partir do objeto recuperado do suggest
	 * 
	 */
	public void pesquisarOrgaoVinculacao(){
		PessoaJuridica pessoaJuridica = null;
		if(verificaPreenchimentoCamposPesquisa()) {
			if( getCnpjPesquisa() != null ) {
				pessoaJuridicaManager = (PessoaJuridicaManager)Component.getInstance("pessoaJuridicaManager");
				pessoaJuridica = pessoaJuridicaManager.findByCNPJ(getCnpjPesquisa());
				
				if( pessoaJuridica == null ) {
					if(Authenticator.isLogouComCertificado()) {
						String nrCnpjMatriz = InscricaoMFUtil.getCNPJBase(InscricaoMFUtil.retiraMascara(getCnpjPesquisa()).substring(0, 8));
					
						if((InscricaoMFUtil.retiraMascara(getCnpjPesquisa())).equals(nrCnpjMatriz)){
							// O CNPJ informado é o da matriz
							try{
								pessoaJuridica = pessoaJuridicaManager.recuperaPessoaJuridicaPelaReceita(getCnpjPesquisa(), null);
							} catch(Exception e ) {
								FacesMessages.instance().clear();
								FacesMessages.instance().add(
										StatusMessage.Severity.ERROR, FacesUtil.getMessage("entity_messages", "preCadastroPessoaBean.pessoa_juridica_nao_encontrada"));
								return;
							}					
						} else {
							// O CNPJ informado é o de uma filial
							PessoaJuridica matriz = null;
							try{
								matriz = pessoaJuridicaManager.recuperaPessoaJuridicaPelaReceita(nrCnpjMatriz, null);
							} catch(Exception e ) {
								FacesMessages.instance().clear();
								FacesMessages.instance().add(
										StatusMessage.Severity.ERROR, FacesUtil.getMessage("entity_messages", "preCadastroPessoaBean.pessoa_juridica_matriz_nao_encontrada"));
								return;
							}
							if( matriz != null ) {	
								try{
									pessoaJuridica = pessoaJuridicaManager.recuperaPessoaJuridicaPelaReceita(getCnpjPesquisa(), matriz);
								} catch(Exception e ) {
									FacesMessages.instance().clear();
									FacesMessages.instance().add(
											StatusMessage.Severity.ERROR, FacesUtil.getMessage("entity_messages", "preCadastroPessoaBean.pessoa_juridica_nao_encontrada"));
									return;
								}
							}	
						}
					}else {
						FacesMessages.instance().clear();
						FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Não foi possível realizar a consulta pelo CNPJ {0}. Esta funcionalidade é permitida apenas para usuários que tenham realizado o acesso ao sistema com certificado digital", getCnpjPesquisa());
					}
				}
			} else {
				pessoaJuridica = getOrgaoVinculadoPesquisa();
			}
			
			if(pessoaJuridica != null){
				getInstance().setOrgaoVinculacao(pessoaJuridica);
				setOrgaoVinculacao(pessoaJuridica);
			}else{
				getInstance().setOrgaoVinculacao(null);
				setOrgaoVinculacao(null);
			}
		} else {
			FacesMessages.instance().clear();
			FacesMessages.instance().add("Forneça um nome ou um CNPJ");
		}
	}
	
	/**
	 * metodo responsavel por verificar se os campos de pesquisa estao preenchidos
	 * @return true se pelo menos um dos campos estiver preenchido / false
	 */
	private boolean verificaPreenchimentoCamposPesquisa() {
		boolean retorno = true;
		if((getCnpjPesquisa() == null) && (getOrgaoVinculadoPesquisa() == null)) {
			retorno = false;
		}
		return retorno;
	}

	/**
	 * Metodo responsável para limpar os campos do orgao de vinculacao
	 */
	public void limparPesquisaOrgaoVinculacao(){
		limparCampoPesquisaOrgaoVinculadoPesquisa();
		limparCampoPesquisaCNPJOrgaoVinculadoPesquisa();
		setOrgaoVinculacao(null);
		getInstance().setOrgaoVinculacao(null);
	}
	
	public void desvincularOrgao(){
		getInstance().setOrgaoVinculacao(null);
	}
	
	/**Exibirá o botão Criar ente/autoridade na tela caso a origem do cadastro da pessoa autoridade seja no cadastro de
	 * partes, e não no cadastro de pessoa.
	 * 
	 * @return verdadeiro se o objeto não for gerenciado e a origem do cadastro for no cadastro de partes. 
	 */
	public boolean isExibeBotaoCriarEnteAutoridade(){
		boolean retorno = false;
		if (!isManaged() && isCadastroParte(cadastro)) {
			retorno = true;
		}
		return retorno;
	}
	
	public void buscarPorCnpj(){
		setBuscarPorCnpj(!buscarPorCnpj);
	}
	
	public PessoaJuridica getOrgaoVinculadoPesquisa() {
		return orgaoVinculadoPesquisa;
	}

	public void setOrgaoVinculadoPesquisa(PessoaJuridica orgaoVinculadoPesquisa) {
		this.orgaoVinculadoPesquisa = orgaoVinculadoPesquisa;
	}
	
	public String getCnpjPesquisa() {
		return cnpjPesquisa;
	}
	
	public void setCnpjPesquisa(String cnpjPesquisa) {
		this.cnpjPesquisa = cnpjPesquisa;
	}
	
	public PessoaJuridica getOrgaoVinculacao() {
		return orgaoVinculacao;
	}
	
	public void setOrgaoVinculacao(PessoaJuridica orgaoVinculacao) {
		this.orgaoVinculacao = orgaoVinculacao;
	}

	public String getCadastro() {
		return cadastro;
	}

	public void setCadastro(String cadastro) {
		this.cadastro = cadastro;
	}
	
	public Boolean getBuscarPorCnpj() {
		return buscarPorCnpj;
	}
	
	public void setBuscarPorCnpj(Boolean buscarPorCnpj) {
		this.buscarPorCnpj = buscarPorCnpj;
	}

	public Boolean getAtivo() {
//		return (getInstance().getAtivo() != null ? getInstance().getAtivo() : this.ativo);
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
}
