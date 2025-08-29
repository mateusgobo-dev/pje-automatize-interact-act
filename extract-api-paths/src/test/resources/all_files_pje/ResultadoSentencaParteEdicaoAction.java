package br.jus.csjt.pje.view.action;

import java.io.Serializable;
import java.math.BigDecimal;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.util.ComponentUtil;
import br.jus.csjt.pje.business.service.ResultadoSentencaService;
import br.jus.pje.nucleo.entidades.ResultadoSentencaParte;
import br.jus.pje.nucleo.entidades.SolucaoSentenca;

/**
 * Componente Action usado para interface entre a View e o
 * LancadorMovimentosService.
 * 
 * @author David, Borges
 */
@Name(ResultadoSentencaParteEdicaoAction.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class ResultadoSentencaParteEdicaoAction implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4570131134428133796L;
	public static final String NAME = "resultadoSentencaParteEdicaoAction";
	private ResultadoSentencaService resultadoSentencaService = ComponentUtil
			.getComponent(ResultadoSentencaService.NAME);

	private SolucaoSentenca solucaoSentencaSelecionada;
	private BigDecimal valorCondenacao = new BigDecimal("0");
	private BigDecimal custasDispensadas = new BigDecimal("0");
	private BigDecimal custasArrecadar = new BigDecimal("0");
	private Boolean assistenciaJudiciariaGratuita;

	private ResultadoSentencaParte resultadoSentencaParteSelecionado;

	public void setResultadoSentencaParteSelecionado(ResultadoSentencaParte resultadoSentencaParteSelecionado) {
		this.resultadoSentencaParteSelecionado = resultadoSentencaParteSelecionado;
		this.solucaoSentencaSelecionada = resultadoSentencaParteSelecionado.getSolucaoSentenca();
		this.valorCondenacao = resultadoSentencaParteSelecionado.getValorCondenacao();
		this.custasDispensadas = resultadoSentencaParteSelecionado.getValorCustasDispensadas();
		this.custasArrecadar = resultadoSentencaParteSelecionado.getValorCustasArrecadar();
		this.assistenciaJudiciariaGratuita = resultadoSentencaParteSelecionado.getAssistenciaJudicialGratuita();
	}

	public ResultadoSentencaParte getResultadoSentencaParteSelecionado() {
		return resultadoSentencaParteSelecionado;
	}

	public void atualizarResultadoSentencaParte() {
		if (!validarCustas()) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR,
					"Por favor informar ao menos uma das custas (dispensadas ou a arrecadar).");
			return;
		}
		setarBigDecimalNuloComoZero();
		resultadoSentencaParteSelecionado.setSolucaoSentenca(solucaoSentencaSelecionada);
		resultadoSentencaParteSelecionado.setValorCondenacao(valorCondenacao);
		resultadoSentencaParteSelecionado.setValorCustasArrecadar(custasArrecadar);
		resultadoSentencaParteSelecionado.setValorCustasDispensadas(custasDispensadas);
		resultadoSentencaService.gravarResultadoSentencaParteDiferenciado(resultadoSentencaParteSelecionado);
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro de Solução Diferenciada atualizado com sucesso!");
	}

	public void setSolucaoSentencaSelecionada(SolucaoSentenca solucaoSentencaSelecionada) {
		this.solucaoSentencaSelecionada = solucaoSentencaSelecionada;
	}

	public SolucaoSentenca getSolucaoSentencaSelecionada() {
		return solucaoSentencaSelecionada;
	}

	public void setAssistenciaJudiciariaGratuita(Boolean assistenciaJudiciariaGratuita) {
		this.assistenciaJudiciariaGratuita = assistenciaJudiciariaGratuita;
	}

	public Boolean getAssistenciaJudiciariaGratuita() {
		return assistenciaJudiciariaGratuita;
	}

	public void setValorCondenacao(BigDecimal valorCondenacao) {
		this.valorCondenacao = valorCondenacao;
	}

	public BigDecimal getValorCondenacao() {
		return valorCondenacao;
	}

	public void setCustasDispensadas(BigDecimal custasDispensadas) {
		this.custasDispensadas = custasDispensadas;
	}

	public BigDecimal getCustasDispensadas() {
		return custasDispensadas;
	}

	public void setCustasArrecadar(BigDecimal custasArrecadar) {
		this.custasArrecadar = custasArrecadar;
	}

	public BigDecimal getCustasArrecadar() {
		return custasArrecadar;
	}

	private void setarBigDecimalNuloComoZero() {
		if (valorCondenacao == null) {
			valorCondenacao = new BigDecimal("0");
		}
		if (custasDispensadas == null) {
			custasDispensadas = new BigDecimal("0");
		}
		if (custasArrecadar == null) {
			custasArrecadar = new BigDecimal("0");
		}
	}

	private boolean validarCustas() {
		if ((custasArrecadar == null || custasArrecadar.compareTo(new BigDecimal("0")) == 0)
				&& (custasDispensadas == null || custasDispensadas.compareTo(new BigDecimal("0")) == 0)) {
			return false;
		}
		return true;
	}
}
