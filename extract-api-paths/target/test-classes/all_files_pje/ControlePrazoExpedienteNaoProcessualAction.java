package br.jus.cnj.pje.view;

import java.util.Date;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.ScopeType;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.util.ControlePrazoExpedientesNaoProcessuaisManager;
import br.jus.cnj.pje.view.fluxo.ProcessoJudicialAction;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.util.DateUtil;

/**
 * Classe de armazenamento e transformaçao de dados utilizados no frame de controle de prazo de expedientes nao processuais
 * @author luiz.mendes
 *
 */
@Name("controlePrazoExpedienteNaoProcessualAction")
@Scope(ScopeType.CONVERSATION)
public class ControlePrazoExpedienteNaoProcessualAction {
	@Logger
	private Log log;
	
	@In(create=true)
	private ControlePrazoExpedientesNaoProcessuaisManager controlePrazoExpedientesNaoProcessuaisManager;
	
	private ProcessoJudicialAction processoJudicialAction = ComponentUtil.getComponent("processoJudicialAction");
	
	public static final String TIPO_VAZIO = "V";
	public static final String TIPO_DATA_CERTA = "C";
	public static final String TIPO_DIA = "D";
	public static final String TIPO_MES = "M";
	public static final String TIPO_ANO = "A";
	
	private Date dataSelecionada = null;
	private int prazoSelecionado = 0;
	private String tipoDePrazo = TIPO_VAZIO;
	private Date dataCalculada = null;//será calculada após inserçao de um @prazoSelecionado
	private boolean fechaTela = false;//booleano para evitar que a janela se feche após um erro inesperado
	
	
	
	//GETTERS
	public Date getDataSelecionada() {
		if(dataSelecionada == null) {
			return DateUtil.defineData(1, 0, 0, 0, 0);//retorna 0 horas do dia atual + 1 dia
		}else {
			return dataSelecionada;
		}
	}
	public int getPrazoSelecionado() {
		return prazoSelecionado;
	}
	public String getTipoDePrazo() {
		return tipoDePrazo;
	}
	public Date getDataCalculada() {
		return dataCalculada;
	}
	public boolean isFechaTela() {
		return fechaTela;
	}
	//SETTERS
	public void setDataSelecionada(Date dataSelecionada) {
		this.dataSelecionada = dataSelecionada;
	}
	public void setPrazoSelecionado(int prazoSelecionado) {
		this.prazoSelecionado = prazoSelecionado;
	}
	public void setTipoDePrazo(String tipoDePrazo) {
		this.tipoDePrazo = tipoDePrazo;
	}
	public void setDataCalculada(Date dataCalculada) {
		this.dataCalculada = dataCalculada;
	}
	public void setFechaTela(boolean fechaTela) {
		this.fechaTela = fechaTela;
	}
	
	/**
	 * Metodo responsavel por verificar a possibilidade de mostrar na tela o campo para inserção do prazo
	 * @return true se o tipo de prazo selecionado for dia, mes ou ano
	 */
	public boolean mostraBoxPrazoBoolean() {
		boolean retorno = false;
		if(tipoDePrazo.equalsIgnoreCase(TIPO_DIA)||tipoDePrazo.equalsIgnoreCase(TIPO_MES)||tipoDePrazo.equalsIgnoreCase(TIPO_ANO)) {
			retorno = true;
		}
		return retorno;
	}
	
	/**
	 * Metodo criado devido à intolerancia do componente com 'rendered' e 'disabled', retorna o CSS do tipo de display (none/box)
	 * ao utilizar o componente com rendered e disabled, os mesmos nao funcionaram como esperado.
	 * @return String com o CSS
	 */
	public String mostraBoxDataPrazoCSS() {
		String retorno = "none";
		if(prazoSelecionado > 0 && (!tipoDePrazo.equalsIgnoreCase(TIPO_VAZIO) && !tipoDePrazo.equalsIgnoreCase(TIPO_DATA_CERTA) )) {
			calculaDataPrazo();
			retorno = "box";
		}
		return retorno;
	}
	/**
	 * Metodo a ser utilizado para configuraçao do fluxo para salvar a data do prazo sem necessidade do frame, por EL.
	 * @param tipoDePrazoM - deve ser "D", "M" ou "A" para dias, meses ou anos
	 * @param prazoM - deverá ser um inteiro positivo, que será adicionado a data atual, de acordo com o @tipoDePrazoM
	 */
	public void setDataAguardarPrazoExpNaoProcessual(String tipoDePrazoM, int prazoM) {
		if((tipoDePrazoM.equalsIgnoreCase(TIPO_DIA)||tipoDePrazoM.equalsIgnoreCase(TIPO_MES)||tipoDePrazoM.equalsIgnoreCase(TIPO_ANO)) && prazoM > 0) {
			setTipoDePrazo(tipoDePrazoM);
			setPrazoSelecionado(prazoM);
			calculaDataPrazo();
			salvaVariaveisETramitaProcesso();
		}else {
			log.error("########################");
			log.error("CONFIGURAÇAO DO FLUXO DE AGUARDAR PRAZO EXPEDIENTES NAO PROCESSUAIS");
			log.error("METODO PARA SALVAR A DATA DIRETAMENTE A PARTIR DO FLUXO RECEBEU PARAMETROS INCORRETOS");
			log.error("########################");
		}
	}
	
	/**
	 * Metodo usado pelo frame para verificar se as condições para salvar as variaveis de data existem e solicitar o transito do processo
	 */
	public void salvaVariaveisETramitaProcesso () {
		if(!tipoDePrazo.equalsIgnoreCase(TIPO_VAZIO)) {
			Date dataPretendida = dataPretendida();
			ProcessoTrf processoAtual = processoJudicialAction.getProcessoJudicial();
			if(verificaDataEPrazo(dataPretendida)) {
				if(!controlePrazoExpedientesNaoProcessuaisManager.salvaVariaveisETramitaProcesso(dataPretendida, processoAtual)) {
					FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("entity_messages", "pje.aguardarPrazoExpedientesNaoProcessuais.erroAoSalvarETramitarProcesso"), processoAtual.getProcesso().getNumeroProcesso());
				} else {
					fechaTela = true;
				}
			}			
		}else {
			FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("entity_messages", "pje.aguardarPrazoExpedientesNaoProcessuais.erroSelecionarTipoPrazo"));
		}
	}
	
	/**
	 * Metodo que verifica se o tipo de prazo está corretamente setado e com a @dataPretendida maior que a data atual, às 23:59
	 * @param dataPretendida
	 * @return true se valores estiverem corretos / false e mensagem para usuario se houver algum erro
	 */
	private boolean verificaDataEPrazo(Date dataPretendida) {
		boolean retorno = false;
		Date dataAtual = DateUtil.defineData(0, 0, 0, 23, 59);//retorna a data de hoje, às 23:59
		if((tipoDePrazo.equalsIgnoreCase(TIPO_DIA)||tipoDePrazo.equalsIgnoreCase(TIPO_MES)||tipoDePrazo.equalsIgnoreCase(TIPO_ANO)) && dataCalculada == null) {
			FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("entity_messages", "pje.aguardarPrazoExpedientesNaoProcessuais.erroPrazoInvalido"));
			FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "pje.aguardarPrazoExpedientesNaoProcessuais.infoPrazoMaiorZero"));
		}
		if(dataPretendida != null && dataPretendida.after(dataAtual)) {//a data pretendida tem que ser maior que hoje 
			retorno = true;
		}else {
			FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("entity_messages", "pje.aguardarPrazoExpedientesNaoProcessuais.erroDataInvalida"));
			FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "pje.aguardarPrazoExpedientesNaoProcessuais.infoDataMaiorAtual"));
		}
		return retorno;
	}
	
	/**
	 * Metodo para retornar a data selecionada ou calculada, dependendo do @tipoDePrazo selecionado
	 * @return data
	 */
	private Date dataPretendida() {
		Date retorno = dataCalculada;
		if(tipoDePrazo.equalsIgnoreCase(TIPO_VAZIO)) {
			retorno = null;	
		} else if (tipoDePrazo.equalsIgnoreCase(TIPO_DATA_CERTA)) {
			retorno = dataSelecionada;	
		} 
		return retorno;
	}
	
	/**
	 * Metodo para calcular a data do prazo à partir do valor do @prazoSelecionado e do @tipoDePrazo
	 */
	public void calculaDataPrazo() {
		if(tipoDePrazo.equalsIgnoreCase(TIPO_DIA)) {//DIAS
			dataCalculada = DateUtil.defineData(prazoSelecionado, null, null, 0, 0);
		} else if(tipoDePrazo.equalsIgnoreCase(TIPO_MES)) {//MESES
			dataCalculada = DateUtil.defineData(null, prazoSelecionado, null, 0, 0);
		} else {//ANOS
			dataCalculada = DateUtil.defineData(null, null, prazoSelecionado, 0, 0);
		}
	}
}