package br.com.infox.pje.action;	

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.manager.BaseCalculoIrManager;
import br.jus.pje.nucleo.entidades.BaseCalculoIr;

/**
 * Classe action para a página /PJE2/RpvSimulacao/listView.xhtml
 * @author silasjesus
 *
 */
@Name(RpvSimulacaoAction.NAME)
@Scope(ScopeType.PAGE)
public class RpvSimulacaoAction implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String NAME = "rpvSimulacaoAction";
	private static final Double TETO_MINIMO_BASE_IR = 1637.11; //Aguardando criação de parametro.
	
	private Boolean tipoBeneficiario = true;
	private Double valorBeneficiario;
	private Double pss;
	private Boolean isencaoIr = false;
	private Integer numeroMeses;
	private Double valorDeducoes;
	private Double valorContratual;
	private Double limiteValorCompensar;
	
	@In
	private BaseCalculoIrManager baseCalculoIrManager;
	
	public void calcularValorLimiteCompensar() {
		BaseCalculoIr bCI = baseCalculoIrManager.getBaseCalculoIr();
		if(bCI == null){
			FacesMessages.instance().add(Severity.ERROR, "Não existe base de cálculo cadastrada, favor cadastrar.");
			return; 
		}
		if (!tipoBeneficiario) {
			if (pss == null) {
				pss = 0.0;
			}
			if (valorDeducoes == null) {
				valorDeducoes = 0.0;
			}
			if(valorContratual - pss - valorDeducoes > TETO_MINIMO_BASE_IR){
				bCI = null;
				bCI = baseCalculoIrManager.getBaseCalculoIrByValor(valorContratual);
				if(bCI == null){
					FacesMessages.instance().add(Severity.ERROR, "Favor cadastrar a base de cálculo de IR no Menu: Cadastros Básicos/Base de Cálculos IR");
					return;
				}
			}
			if (isencaoIr) {
				limiteValorCompensar = valorContratual;
			} else {
				Double percLimiteValorCompensar = Double.parseDouble(ParametroUtil.instance().getPercLimiteValorCompsar());
				limiteValorCompensar = valorContratual - (percLimiteValorCompensar * valorContratual);
			}
		}
		
		if (tipoBeneficiario) {
			if (pss == null) {
				pss = 0.00;
			}
			
			if (valorDeducoes == null) {
				valorDeducoes = 0.00;
			}
			if(getBaseIr() > TETO_MINIMO_BASE_IR){
				if(numeroMeses == null || numeroMeses == 0){
					bCI = null;
					bCI = baseCalculoIrManager.getBaseCalculoIrByValor(getBaseIr());
				}else{
					bCI = null;
					bCI = baseCalculoIrManager.getBaseCalculoIrByValor(getBaseIr()/numeroMeses);
				}
				if(bCI == null){
					FacesMessages.instance().add(Severity.ERROR, "Favor cadastrar a base de cálculo de IR no Menu: Cadastros Básicos/Base de Cálculos IR");
					return; 
				}
			}
			if (isencaoIr) {
				limiteValorCompensar = valorBeneficiario - pss;
			} else {
				if (numeroMeses == null || numeroMeses == 0) {
					Double percLimiteValorCompensar = Double.parseDouble(ParametroUtil.instance().getPercLimiteValorCompsar());
					limiteValorCompensar = (valorBeneficiario - pss) - (percLimiteValorCompensar * (valorBeneficiario - pss));
				} else {
					limiteValorCompensar = getValorAliquota();
				}
			}
		}
	}
	
	private Double getValorAliquota() {
		Double baseIr = getBaseIr()/numeroMeses;
		BaseCalculoIr baseCalculoIr = baseCalculoIrManager.getBaseCalculoIrByValor(baseIr);
		if (baseCalculoIr != null) { 
			return valorBeneficiario -( pss + getIr(getBaseIr(), baseCalculoIr));
		} else {
			return null;
		}
	}
	
	private Double getBaseIr() {
		return valorBeneficiario - pss - valorDeducoes;
	}
	
	private Double getIr(Double valor, BaseCalculoIr baseCalculoIr) {
		return ((valor * baseCalculoIr.getVlAliquota()/100) - (baseCalculoIr.getVlParcelaADeduzir() * numeroMeses));
	}
	
	public void limparBeneficiario() {
		tipoBeneficiario = true;
		valorBeneficiario = null;
		valorContratual = null;
		pss = null;
		numeroMeses = null;
		valorDeducoes = null;
		isencaoIr = false;
		limiteValorCompensar = null;
	}
	
	public void limparValores() {
		tipoBeneficiario = false;
		limparBeneficiario();
	}

	public Boolean getTipoBeneficiario() {
		return tipoBeneficiario;
	}
	
	public void setTipoBeneficiario(Boolean tipoBeneficiario) {
		this.tipoBeneficiario = tipoBeneficiario;
	}
	public Double getValorBeneficiario() {
		return valorBeneficiario;
	}
	public void setValorBeneficiario(Double valorBeneficiario) {
		this.valorBeneficiario = valorBeneficiario;
	}
	public Double getPss() {
		return pss;
	}
	public void setPss(Double pss) {
		this.pss = pss;
	}
	public Boolean getIsencaoIr() {
		return isencaoIr;
	}
	public void setIsencaoIr(Boolean isencaoIr) {
		this.isencaoIr = isencaoIr;
	}
	public Integer getNumeroMeses() {
		return numeroMeses;
	}
	public void setNumeroMeses(Integer numeroMeses) {
		this.numeroMeses = numeroMeses;
	}
	public Double getValorDeducoes() {
		return valorDeducoes;
	}
	public void setValorDeducoes(Double valorDeducoes) {
		this.valorDeducoes = valorDeducoes;
	}
	public Double getValorContratual() {
		return valorContratual;
	}
	public void setValorContratual(Double valorContratual) {
		this.valorContratual = valorContratual;
	}
	public Double getLimiteValorCompensar() {
		return limiteValorCompensar;
	}
	public void setLimiteValorCompensar(Double limiteValorCompensar) {
		this.limiteValorCompensar = limiteValorCompensar;
	}

	public String getVlDecimalFormat(Double valor) {
		String vl = "";
		if (valor != null && valor != 0){
			NumberFormat formatter =  DecimalFormat.getNumberInstance(new Locale("pt","BR"));
			formatter.setMinimumIntegerDigits(1);
			formatter.setMaximumFractionDigits(2);
			formatter.setMinimumFractionDigits(2);
			vl = formatter.format(valor);
			return "R$ "+vl;
		}else{
			return vl;
		}
	}	
}