package br.jus.cnj.pje.view;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.TipoParteHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.TipoParteConfiguracaoManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoParteConfiguracao;

@Name(TipoParteConfiguracaoAction.NAME)
@Scope(ScopeType.PAGE)
public class TipoParteConfiguracaoAction {

	public static final String NAME = "tipoParteConfiguracaoAction";
	private TipoParteHome tipoParteHome = ComponentUtil.getComponent(TipoParteHome.class);
	private TipoParteConfiguracao tipoParteConfiguracao = new TipoParteConfiguracao();
	
	@In
	(create = true, required = false)
	TipoParteConfiguracaoManager tipoParteConfiguracaoManager;
	
	/**
	 * Retorna se a aba dever ser exiba já que é necessário
	 * que o {@link TipoParte} tela que ser selecionado no form da tela.
	 * @return 
	 */
	public boolean isExibeAba(){
		return getTipoParte().getTipoParte() != null;
	}

	/**
	 * Recupera {@link TipoParte} gerenciado pela Home. 
	 * @return
	 */
	private TipoParte getTipoParte() {
		return tipoParteHome.instance().getInstance();
	}

	/**
	 * carrega a lista quando o usuário clicar na aba de configurações. 
	 */
	public void carregarListaParteConfig(){
		limpar();
		List<TipoParteConfiguracao> tipoParteConfigPadrao = tipoParteConfiguracaoManager.recuperarPorTipoPartePadrao(getTipoParte(),Boolean.TRUE);
		if(CollectionUtilsPje.isNotEmpty(tipoParteConfigPadrao)){
			this.tipoParteConfiguracao = tipoParteConfigPadrao.get(0);
		}
	}

	/**
	 * Ação para criar um novo {@link TipoParteConfiguracao}.
	 */
	public void novo(){
		limpar();
	}
	
	/**
	 * Grava {@link TipoParteConfiguracao}.
	 */
	public void gravar(){
		if(isCamposValidos()){
			try {
				tipoParteConfiguracao.setPadrao(Boolean.TRUE);
				tipoParteConfiguracaoManager.persistAndFlush(tipoParteConfiguracao);
				FacesMessages.instance().addFromResourceBundle(Severity.INFO, "TipoParte_created");
				limpar();
				carregarListaParteConfig();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Altera {@link TipoParteConfiguracao}.
	 */
	public void alterar(){
		if(isCamposValidos()){
			try {
				tipoParteConfiguracaoManager.mergeAndFlush(tipoParteConfiguracao);
				FacesMessages.instance().addFromResourceBundle(Severity.INFO, "TipoParte_updated");
				limpar();
				carregarListaParteConfig();
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * Limpa {@link TipoParteConfiguracao} e sua lista.
	 */
	private void limpar(){
		tipoParteConfiguracao = new TipoParteConfiguracao();
		tipoParteConfiguracao.setTipoParte(getTipoParte());
		tipoParteConfiguracao.setOab(Boolean.FALSE);
	}
	
	/**
	 * Conforme exibição na tela transforma o resultado <b>Boolean</b>
	 * em String.
	 * @param valor
	 * @return
	 */
	public String resultado(Boolean valor){
		return (valor == null || valor.equals(Boolean.FALSE)) ? "NÃO" : "SIM";
	}
	
	/**
	 * Retorna a descrição da {@link TipoParte} para
	 * exibição em tela.
	 * @return
	 */
	public String getNomeTipoParte(){
		return getTipoParte().getTipoParte();
	}
	
	/**
	 * Verificar de acordo com a regra de negócio
	 * se o usuário pode gravar uma nova {@link TipoParteConfiguracao}
	 * @return
	 */
	public boolean isPodeIncluir(){
		return !isTipoParteConfiguracaoEdit() && !isContemConfiguracao();
	}
	
	/**
	 * de acordo com a {@link TipoParte} verifica se contem
	 * uma {@link TipoParteConfiguracao}
	 * @return
	 */
	private boolean isContemConfiguracao(){
		return tipoParteConfiguracaoManager.isContemConfiguracao(getTipoParte());
	}
	
	
	/**
	 * Verifica se a {@link TipoParteConfiguracao} está em edição
	 * para exibição correta do botão na tela.
	 * @return
	 */
	public boolean isTipoParteConfiguracaoEdit(){
		return tipoParteConfiguracao != null && tipoParteConfiguracao.getIdTipoParteConfiguracao() != 0;
	}
	
	/**
	 * Verifica se os campos inseridos na tela são validos para inserção ou alteração
	 * de acordo com as regras.
	 * @return
	 */
	private boolean isCamposValidos() {
		boolean valido = true;
		if(!isPossuiPolo()){
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "TipoParte.configuracao.msg.possuiPolo");
			valido = false;
		}
		if(!isPossuiTipoPessoa()){
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "TipoParte.configuracao.msg.possuiTipoParte");
			valido = false;
		}
		return valido;
	}
	
	/**
	 * Conforme regra para inserção ou alteração é necessário
	 * que pelo menos um dos Polos sejá informado.
	 * @return
	 */
	private boolean isPossuiPolo() {
		return isPoloAtivo() || isPoloPassivo() || isOutrosParticipantes();
	}
	
	/**
	 * Conforme regra para inserção ou alteração é necessário
	 * que pelo menos um dos Tipos da pessoa seja informado.
	 * @return
	 */
	private boolean isPossuiTipoPessoa() {
		return isPessoaFisica() || isPessoaJuridica() || isEnteAutoridade();
	}
	
	/**
	 * Conforme regra de negócio verifica se o campo pode estar habilitado
	 * e caso não possa também insere a busca como false 
	 * @return
	 */
	public boolean isHabilitaBuscaOab(){
		boolean habilita = Boolean.FALSE;
		if(!isPessoaFisica()){
			getTipoParteConfiguracao().setOab(Boolean.FALSE);
		}else{
			habilita = Boolean.TRUE;
		}
		return habilita;
	}
	
	/**
	 * resetar os valores do tipoPoloMNI.
	 */
	public void resetarPoloMNI(){
		if(!isOutrosParticipantes()){
			getTipoParteConfiguracao().getTipoParte().setTipoPoloMNI(null);
		}
	}
	
	private Boolean isEnteAutoridade() {
		return tipoParteConfiguracao.getEnteAutoridade() != null && tipoParteConfiguracao.getEnteAutoridade();
	}
	private Boolean isPessoaJuridica() {
		return tipoParteConfiguracao.getTipoPessoaJuridica() != null && tipoParteConfiguracao.getTipoPessoaJuridica();
	}
	
	private Boolean isPessoaFisica() {
		return tipoParteConfiguracao.getTipoPessoaFisica() != null && tipoParteConfiguracao.getTipoPessoaFisica();
	}
	
	public Boolean isOutrosParticipantes() {
		return tipoParteConfiguracao.getOutrosParticipantes() != null && tipoParteConfiguracao.getOutrosParticipantes();
	}
	private Boolean isPoloPassivo() {
		return tipoParteConfiguracao.getPoloPassivo() != null && tipoParteConfiguracao.getPoloPassivo();
	}
	private Boolean isPoloAtivo() {
		return tipoParteConfiguracao.getPoloAtivo() != null && tipoParteConfiguracao.getPoloAtivo();
	}
	
	public TipoParteConfiguracao getTipoParteConfiguracao() {
		return tipoParteConfiguracao;
	}

	public void setTipoParteConfiguracao(TipoParteConfiguracao tipoParteConfiguracao) {
		this.tipoParteConfiguracao = tipoParteConfiguracao;
	}
	
	
}
