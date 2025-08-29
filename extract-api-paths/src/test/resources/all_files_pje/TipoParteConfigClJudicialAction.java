package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.pje.manager.TipoParteConfigClJudicialManager;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TipoParteConfiguracaoManager;
import br.jus.cnj.pje.nucleo.manager.TipoParteManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoParteConfigClJudicial;
import br.jus.pje.nucleo.entidades.TipoParteConfiguracao;
import br.jus.pje.nucleo.util.StringUtil;

@Name(TipoParteConfigClJudicialAction.NAME)
@Scope(ScopeType.PAGE)
public class TipoParteConfigClJudicialAction extends BaseAction<TipoParteConfigClJudicial> implements Serializable {
	
	private static final long serialVersionUID = -3191858136367858792L;
	
	public static final String NAME = "tipoParteConfigClJudicialAction";
	
	private List<TipoParte> listaTiposParte = new ArrayList<TipoParte>(0);
	private ClasseJudicial classeJudicialSelecionada;
	private EntityDataModel<TipoParteConfigClJudicial> model;
	private TipoParteConfiguracao tipoParteConfiguracao;
	private TipoParteConfigClJudicial tipoParteConfigClJudicial;
	
	@In
	private TipoParteConfigClJudicialManager tipoParteConfigClJudicialManager;
	
	@In
	private TipoParteConfiguracaoManager tipoParteConfiguracaoManager;
	
	@In
	private ClasseJudicialManager classeJudicialManager;
	
	@In
	private TipoParteManager tipoParteManager;
	
	//Componentes da tela
	private boolean edicao;
	private Boolean tipoPrincipal;
	private Boolean poloAtivo;
	private Boolean poloPassivo;
	private Boolean outrosParticipantes;
	private Boolean pessoaFisica;
	private Boolean pessoaJuridica;
	private Boolean enteAutoridade;
	private Boolean buscaOab;
	private TipoParte tipoParte;
	
	//Flag's de validações
	private boolean principalAtivo;
	private boolean principalPassivo;
	private boolean restauraAtributos = true;
	
	public void init(Integer idClasseJudicialSelecionada){
		try {
			if(idClasseJudicialSelecionada != null) {
				setClasseJudicialSelecionada(classeJudicialManager.findById(idClasseJudicialSelecionada));
			}
			limparMarcacao();
		} catch (PJeBusinessException e) {
			facesMessages.addFromResourceBundle(Severity.ERROR, "classeJudicial.tipoParte.erro.classeJudicialNaoIdentificada");
		}
	}
	
	/**
	 * Método responsável por resetar o valor inicial das variáveis além de
	 * limpar os checkboxes da tela
	 */
	public void limparMarcacao() {
		setTipoPrincipal(null);
		setPoloAtivo(null);
		setPoloPassivo(null);
		setOutrosParticipantes(null);
		setPessoaFisica(null);
		setPessoaJuridica(null);
		setEnteAutoridade(null);
		setTipoParte(null);
	}

	/**
	 * Método responsável por recuperar as partes e adicionar à lista de acordo
	 * com o tipo principal selecionado
	 */
	public void recuperarTipoPartePrincipal() {
		listaTiposParte.clear();
		
		if (isTipoPrincipalSelecionado() || isEdicao()) {
			List<TipoParte> tipoPartePrincipal = tipoParteManager.recuperarPorTipoPrincipal(getTipoPrincipal(),Boolean.TRUE);
			for (TipoParte tipoParte : tipoPartePrincipal) {
				listaTiposParte.add(tipoParte);
			}
		}
	}
	
	/**
	 * Método responsável por recuperar os tipos da parte e suas configurações
	 * de acordo com a classe judicial selecionada
	 * 
	 * @return <code>List</code> de {@link TipoParteConfigClJudicial}
	 */
	public List<TipoParteConfigClJudicial> recuperarTipoParteConfiguracao() {
		if (getClasseJudicialSelecionada() != null) {
			List<TipoParteConfigClJudicial> tiposPartesConfigClsJudiciais = tipoParteConfigClJudicialManager.recuperarTipoParteConfiguracao(getClasseJudicialSelecionada());
			return CollectionUtilsPje.isNotEmpty(tiposPartesConfigClsJudiciais) ? tiposPartesConfigClsJudiciais : new ArrayList<TipoParteConfigClJudicial>();
		}
		return new ArrayList<TipoParteConfigClJudicial>();
	}
	
	/**
	 * Método responsável por converter o valor <code>true</code> em "SIM" e o
	 * valor <code>false</code> em "NÃO"
	 * 
	 * @param valor
	 *            parâmetro que se deseja fazer a conversão
	 * @return <code>String</code>, "SIM" para valores <code>true</code> e "NÃO"
	 *         para valores <code>false</code>
	 */
	public String booleanToString(Boolean valor) {
		return StringUtil.booleanToString(valor, "SIM", "NÃO");
	}
	
	/**
	 * Método responsável por modificar o label do combobox se o tipo principal
	 * estiver sido selecionado
	 * 
	 * @return <code>String</code>, caso o tipo principal tenha sido selecionado
	 *         "Selecione", caso não, "Selecione o tipo primeiro"
	 */
	public String comboLabelSemSelecao() {
		return (isTipoPrincipalSelecionado() ? "Selecione" : "Selecione o tipo primeiro");
	}
	
	/**
	 * Método responsável por verificar se o tipo principal foi selecionado na
	 * tela
	 * 
	 * @return <code>Boolean</code>, <code>true</code> se ele for selecionado
	 *         (diferente de null)
	 */
	public boolean isTipoPrincipalSelecionado() {
		return (getTipoPrincipal() != null);
	}
	
	/**
	 * Método responsável por validar e salvar a configuracao da classe judicial
	 */
	public void salvar(){
		try {
			if(!validarTipoPrincipal()){
				incluir();
			}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Método responsável por incluir a configuração da parte à classe judicial
	 */
	public void incluir() {
		facesMessages.clear();
		try {
			validarVinculacaoTipoParte();
			validarVinculacaoPolos();
			validarVinculacaoPessoa();
			
			
			TipoParteConfiguracao configuracao = criarConfiguracao();
			TipoParteConfigClJudicial tipoParteConfigClJudicialNova = criarVinculacaoEntidades(configuracao);
			if (!isParteExistente(tipoParteConfigClJudicialNova) && !isEdicao()) {
				getManager().persistAndFlush(tipoParteConfigClJudicialNova);
				EntityUtil.flush();
				facesMessages.addFromResourceBundle(Severity.INFO, "pje.message.createRecord");	
			} else {
				facesMessages.addFromResourceBundle(Severity.ERROR,
						"classeJudicial.tipoParte.erro.configuracaoExistente", getTipoParte(),
						getClasseJudicialSelecionada());
			}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Método responsável por excluir a configuração
	 * 
	 * @param tipoParteConfigClJudicial
	 *            a configuração da parte associada à classe judicial que se
	 *            deseja excluir
	 */
	public void excluir(TipoParteConfigClJudicial tipoParteConfigClJudicial) {
		try {
			getManager().remove(tipoParteConfigClJudicial);
			getManager().flush();
			facesMessages.addFromResourceBundle(Severity.INFO, "pje.message.deleteRecord");
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Método responsável por recuperar uma configuração e colocá-la para ser
	 * editada
	 * 
	 * @param tipoParteConfigClJudicial
	 *            configuração a ser editada
	 */
	public void editar(TipoParteConfigClJudicial tipoParteConfigClJudicial) {
		setEdicao(true);
		setTipoParteConfigClJudicial(tipoParteConfigClJudicial);
		setPoloAtivo(tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloAtivo());
		setPoloPassivo(tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloPassivo());
		setPessoaFisica(tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoPessoaFisica());
		setPessoaJuridica(tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoPessoaJuridica());
		setEnteAutoridade(tipoParteConfigClJudicial.getTipoParteConfiguracao().getEnteAutoridade());
		setOutrosParticipantes(tipoParteConfigClJudicial.getTipoParteConfiguracao().getOutrosParticipantes());
		setTipoParte(tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte());
		setTipoPrincipal(tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte().getTipoPrincipal());
	}
	
	/**
	 * Método responsável por limpar a tela e permitir uma nova inclusão de
	 * configurações
	 */
	public void novo() {
		super.newInstance();
		limparMarcacao();
		setEdicao(false);
	}

	/**
	 * Método responsável por alterar a configuração existente
	 */
	public void alterar() {
		if (getTipoParte()  != null) {
			try {
				TipoParteConfigClJudicial configClJudicial = getManager().merge(tipoParteConfigClJudicial);
				if(configClJudicial.getTipoParteConfiguracao().getPadrao() != null && !configClJudicial.getTipoParteConfiguracao().getPadrao()){
					configClJudicial.getTipoParteConfiguracao().setPoloAtivo(getPoloAtivo());
					configClJudicial.getTipoParteConfiguracao().setPoloPassivo(getPoloPassivo());
					configClJudicial.getTipoParteConfiguracao().setTipoPessoaFisica(getPessoaFisica());
					configClJudicial.getTipoParteConfiguracao().setTipoPessoaJuridica(getPessoaJuridica());
					configClJudicial.getTipoParteConfiguracao().setEnteAutoridade(getEnteAutoridade());
					configClJudicial.getTipoParteConfiguracao().setOutrosParticipantes(getOutrosParticipantes());
					configClJudicial.getTipoParteConfiguracao().setTipoParte(getTipoParte());
					persistirAtualizar(configClJudicial);
				}else{
					TipoParteConfiguracao configuracao = criarConfiguracao();
					configClJudicial.setTipoParteConfiguracao(configuracao);
					persistirAtualizar(configClJudicial);
				}
			} catch (PJeBusinessException e) {
				facesMessages.addFromResourceBundle(Severity.ERROR, "classeJudicial.tipoParte.erro.alterarConfiguracao");
				e.printStackTrace();
			}
		}else{
			facesMessages.addFromResourceBundle(Severity.ERROR, "classeJudicial.tipoParte.erro.validarVinculacaoTipoParte");
		}
	}
	
	/**
	 * Método responsável por persistir e atualizar da entidade
	 * 
	 * @param configClJudicial
	 *            {@link TipoParteConfigClJudicial}
	 * @throws PJeBusinessException
	 */
	private void persistirAtualizar(TipoParteConfigClJudicial configClJudicial) throws PJeBusinessException {
		getManager().mergeAndFlush(configClJudicial);
		getManager().refresh(configClJudicial);
		facesMessages.addFromResourceBundle(Severity.INFO, "pje.message.updateRecord");
	}
	
	/**
	 * Método responsável por verificar se já existe uma {@link TipoParte} já
	 * existe na configuração da {@link ClasseJudicial}
	 * 
	 * @param tipoParteConfigClJudicialNova
	 *            parâmetro que se deseja obter o tipo parte para verificar a
	 *            existência
	 * @return <code>Boolean</code>, <code>true</code> se existir
	 */
	private boolean isParteExistente(TipoParteConfigClJudicial tipoParteConfigClJudicialNova) {
		List<TipoParteConfigClJudicial> configuracao = recuperarTipoParteConfiguracao();
		for (TipoParteConfigClJudicial tipoParteConfigClJudicial : configuracao) {
			if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte()
					.equals(tipoParteConfigClJudicialNova.getTipoParteConfiguracao().getTipoParte())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Método responsável por verificar se a configuração é nova ou já existente
	 * 
	 * @param tipoParteConfigClJudicialNova
	 *            a configuração da parte associada à classe judicial que se
	 *            deseja verificar se existe
	 */
	public boolean isNovaConfiguracao(TipoParteConfigClJudicial tipoParteConfigClJudicialNova) {
		return !tipoParteConfigClJudicialManager.existeTipoParteConfigClJudicial(tipoParteConfigClJudicialNova);
	}
	
	/**
	 * Método responsável por validar a vinculação de pessoa à classe judicial
	 * 
	 * @throws PJeBusinessException
	 *             se o usuário tentar incluir um tipo de parte sem marcar pelo
	 *             menos um tipo de pessoa: "Pessoa física?", "Pessoa jurídica?"
	 *             ou "Ente/autoridade?"
	 */
	
	/**
	 * Método responsável por validar a exibição da modal na tela.
	 */
	public boolean isExibeModalMensagens(){
		return principalAtivo || principalPassivo;
	}
	
	private void validarVinculacaoPessoa() throws PJeBusinessException {
		if ( (getPessoaFisica() == null || getPessoaFisica() == Boolean.FALSE) && (getPessoaJuridica() == null || getPessoaJuridica() == Boolean.FALSE) && (getEnteAutoridade() == null || getEnteAutoridade() == Boolean.FALSE)) {
			throw new PJeBusinessException("classeJudicial.tipoParte.erro.validarVinculacaoPessoa");
		}
	}

	/**
	 * Método responsável por validar a vinculação de pólos à classe judicial
	 * 
	 * @throws PJeBusinessException
	 *             se o usuário tentar incluir um tipo de parte sem marcar pelo
	 *             menos uma das opções: "Polo ativo?", "Polo passivo?" ou
	 *             "Outros participantes?"
	 */
	private void validarVinculacaoPolos() throws PJeBusinessException {
		if ((getPoloAtivo() == null && getPoloAtivo() == Boolean.FALSE) && (getPoloPassivo() == null || getOutrosParticipantes() == Boolean.FALSE) && (getOutrosParticipantes() == null || getOutrosParticipantes() == Boolean.FALSE)) {
			throw new PJeBusinessException("classeJudicial.tipoParte.erro.validarVinculacaoPolos");
		}
	}
	
	/**
	 * Método responsável por validar a vinculação de tipo parte à classe
	 * judicial
	 * 
	 * @throws PJeBusinessException
	 *             se o usuário tentar incluir uma configuração sem selecionar
	 *             ao menos um tipo de parte
	 */
	private void validarVinculacaoTipoParte() throws PJeBusinessException {
		if (getTipoParte()  == null) {
			throw new PJeBusinessException("classeJudicial.tipoParte.erro.validarVinculacaoTipoParte");	
		}
	}
	
	/**
	 * Método responsável por validar o tipoPrincipal 
	 * @return <code>Boolean</code> 
	 * @throws PJeBusinessException
	 */
	private boolean validarTipoPrincipal() throws PJeBusinessException{
		boolean invalido = false;
		resetarFlagPrincipal();
		List<TipoParteConfigClJudicial> listTiposPartesConfigs = recuperarTipoParteConfiguracao();
		if(tipoPrincipal != null && tipoPrincipal){
			validarVinculacaoTipoParte();
			invalido =  verificarTipoPrincipaisPolo(listTiposPartesConfigs,poloAtivo,poloPassivo);
		}
		if(invalido){
			restauraAtributos = false;
		}
		return invalido;
	}

	/**
	 * Método responsável por resetar as flags principais, para evitar que a
	 * modal seja exibida na inclusão de um tipo não principal
	 */
	private void resetarFlagPrincipal() {
		principalAtivo = false;
		principalPassivo = false;
	}

	/**
	 * Método responsável por validar o tipoPrincipal de acordo com os polos
	 * @param listTiposPartesConfigs
	 * @param ativo
	 * @param passivo
	 * @return <code>Boolean</code>
	 */
	private boolean verificarTipoPrincipaisPolo(List<TipoParteConfigClJudicial> listTiposPartesConfigs,boolean ativo, boolean passivo) {
		boolean invalido = false;
		resetarFlagPrincipal();
		if(ativo){
			if(CollectionUtilsPje.isNotEmpty(listTiposPartesConfigs)){
				for (TipoParteConfigClJudicial tipoParteConfigClJudicial : listTiposPartesConfigs) {
					if(tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte().getTipoPrincipal() && tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloAtivo()){
						principalAtivo = Boolean.TRUE;
						invalido = true;
						break;
					}
				}
			}
		}
		if(passivo){
			if(CollectionUtilsPje.isNotEmpty(listTiposPartesConfigs)){
				for (TipoParteConfigClJudicial tipoParteConfigClJudicial : listTiposPartesConfigs) {
					if(tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte().getTipoPrincipal() && tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloPassivo()){
						principalPassivo = Boolean.TRUE;
						invalido = true;
						break;
					}
				}
			}
		}
		return invalido;
	}
	

	/**
	 * Método responsável por criar um objeto {@link TipoParteConfigClJudicial}
	 * com a vinculação entre as entidades {@link TipoParteConfiguracao} e
	 * {@link ClasseJudicial}
	 * 
	 * @param tipoParteConfiguracao
	 *            configuracao para se fazer a vinculação
	 * 
	 * @return <code>TipoParteConfigClJudicial</code>, uma entidade nova já
	 *         vinculada e preenchida
	 */
	private TipoParteConfigClJudicial criarVinculacaoEntidades(TipoParteConfiguracao tipoParteConfiguracao) {
		tipoParteConfigClJudicial = new TipoParteConfigClJudicial();
		tipoParteConfigClJudicial.setClasseJudicial(getClasseJudicialSelecionada());
		tipoParteConfigClJudicial.setTipoParteConfiguracao(tipoParteConfiguracao);
		return tipoParteConfigClJudicial;
	}

	/**
	 * Método responsável por criar e persistir a entidade
	 * {@link TipoParteConfiguracao}
	 * 
	 * @return <code>TipoParteConfiguracao</code>, uma entidade nova já
	 *         vinculada, preenchida e persistida
	 */
	private TipoParteConfiguracao criarConfiguracao() {
		tipoParteConfiguracao = new TipoParteConfiguracao();
		tipoParteConfiguracao.setTipoParte(getTipoParte());
		tipoParteConfiguracao.setPoloAtivo(getPoloAtivo());
		tipoParteConfiguracao.setPoloPassivo(getPoloPassivo());
		tipoParteConfiguracao.setTipoPessoaFisica(getPessoaFisica());
		tipoParteConfiguracao.setTipoPessoaJuridica(getPessoaJuridica());
		tipoParteConfiguracao.setEnteAutoridade(getEnteAutoridade());
		tipoParteConfiguracao.setOutrosParticipantes(getOutrosParticipantes());
		tipoParteConfiguracao.setPadrao(Boolean.FALSE);
		tipoParteConfiguracao.setOab(validarOab());
		try {
			return tipoParteConfiguracaoManager.persist(tipoParteConfiguracao);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return tipoParteConfiguracao;
	}

	/**
	 * Método que valida a Oab para exibição na tela de partes 
	 * de acordo com a configuração padrão
	 */
	private boolean validarOab() {
		boolean validarOab = false;
		if(getPessoaFisica() && getBuscaOab() != null){
			validarOab = getBuscaOab();
		}
		return validarOab;
	}
	
	
	/**
	 * Método responsável por recuperar a configuração padrão do tipo da parte
	 * selecionado
	 */
	public void recuperarTipoParteConfiguracaoPadrao() {
		if (getTipoParte() != null && restauraAtributos) {
			List<TipoParteConfiguracao> tipoParteConfiguracao = tipoParteConfiguracaoManager.recuperarPorTipoPartePadrao(getTipoParte(), true);
			
			for (TipoParteConfiguracao configuracao : tipoParteConfiguracao) {
				setPoloAtivo(configuracao.getPoloAtivo());
				setPoloPassivo(configuracao.getPoloPassivo());
				setPessoaFisica(configuracao.getTipoPessoaFisica());
				setPessoaJuridica(configuracao.getTipoPessoaJuridica());
				setEnteAutoridade(configuracao.getEnteAutoridade());
				setOutrosParticipantes(configuracao.getOutrosParticipantes());
				setBuscaOab(configuracao.getOab());
			}	
		}
	}
	
	public boolean desabilitarCampo(Boolean campo) {
		if (campo == null) {
			return false;
		}
		
		return !campo;
	}
	
	/**
	 * Método responsável por retornar a mensagem para a modal de exibição
	 * @return <code>String</code>
	 */
	public String msgValidacao(){
		StringBuilder msg = new StringBuilder();
		boolean unica = false;
		if(principalAtivo && principalPassivo){
			msg.append("Já existe um tipo principal para o pólo ativo e passivo, tem certeza que deseja continuar?");
			unica = true;
		}
		if(!unica && principalPassivo){
			msg.append("Já existe um tipo principal para o pólo passivo, tem certeza que deseja continuar?");
		}
		if(!unica && principalAtivo){
			msg.append("Já existe um tipo principal para o pólo ativo, tem certeza que deseja continuar?");
		}
		return msg.toString();
	}
	
	public void restaurarAtributos(){
		restauraAtributos = Boolean.TRUE;
	}
	public ClasseJudicial getClasseJudicialSelecionada() {
		return classeJudicialSelecionada;
	}
	
	public void setClasseJudicialSelecionada(ClasseJudicial classeJudicialSelecionada) {
		this.classeJudicialSelecionada = classeJudicialSelecionada;
	}
	
	public Boolean getTipoPrincipal() {
		return tipoPrincipal;
	}
	
	
	public void setTipoPrincipal(Boolean tipoPrincipal) {
		this.tipoPrincipal = tipoPrincipal;
	}
	
	
	public Boolean getPoloAtivo() {
		return poloAtivo;
	}
	
	
	public void setPoloAtivo(Boolean poloAtivo) {
		this.poloAtivo = poloAtivo;
	}
	
	
	public Boolean getPoloPassivo() {
		return poloPassivo;
	}
	
	
	public void setPoloPassivo(Boolean poloPassivo) {
		this.poloPassivo = poloPassivo;
	}
	
	
	public Boolean getOutrosParticipantes() {
		return outrosParticipantes;
	}
	
	
	public void setOutrosParticipantes(Boolean outrosParticipantes) {
		this.outrosParticipantes = outrosParticipantes;
	}
	
	
	public Boolean getPessoaFisica() {
		return pessoaFisica;
	}
	
	
	public void setPessoaFisica(Boolean pessoaFisica) {
		this.pessoaFisica = pessoaFisica;
	}
	
	
	public Boolean getPessoaJuridica() {
		return pessoaJuridica;
	}
	
	
	public void setPessoaJuridica(Boolean pessoaJuridica) {
		this.pessoaJuridica = pessoaJuridica;
	}
	
	
	public Boolean getEnteAutoridade() {
		return enteAutoridade;
	}
	
	
	public void setEnteAutoridade(Boolean enteAutoridade) {
		this.enteAutoridade = enteAutoridade;
	}
	
	
	public TipoParte getTipoParte() {
		return tipoParte;
	}
	
	
	public void setTipoParte(TipoParte tipoParte) {
		this.tipoParte = tipoParte;
	}
	
	public List<TipoParte> getListaTiposParte() {
		return listaTiposParte;
	}

	public void setListaTiposParte(List<TipoParte> listaTiposParte) {
		this.listaTiposParte = listaTiposParte;
	}

	public TipoParteConfigClJudicial getTipoParteConfigClJudicial() {
		return tipoParteConfigClJudicial;
	}

	public void setTipoParteConfigClJudicial(TipoParteConfigClJudicial tipoParteConfigClJudicial) {
		this.tipoParteConfigClJudicial = tipoParteConfigClJudicial;
	}

	@Override
	protected BaseManager<TipoParteConfigClJudicial> getManager() {
		return tipoParteConfigClJudicialManager;
	}

	@Override
	public EntityDataModel<TipoParteConfigClJudicial> getModel() {
		return model;
	}

	public boolean isEdicao() {
		return edicao;
	}

	public void setEdicao(boolean edicao) {
		this.edicao = edicao;
	}

	public Boolean getBuscaOab() {
		return buscaOab;
	}

	public void setBuscaOab(Boolean buscaOab) {
		this.buscaOab = buscaOab;
	}

	public boolean isRestauraAtributos() {
		return restauraAtributos;
	}

	public void setRestauraAtributos(boolean restauraAtributos) {
		this.restauraAtributos = restauraAtributos;
	}
	
}