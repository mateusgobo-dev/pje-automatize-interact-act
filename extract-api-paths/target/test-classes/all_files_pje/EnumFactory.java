package br.com.infox.cliente.type;

import br.jus.pje.nucleo.enums.SimNaoEnum;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.enums.CondicaoPssEnum;
import br.jus.pje.nucleo.enums.EspecieRequisicaoEnum;
import br.jus.pje.nucleo.enums.NaturezaCreditoEnum;
import br.jus.pje.nucleo.enums.ParcialIncontroversoIntegralEnum;
import br.jus.pje.nucleo.enums.RpvPrecatorioEnum;
import br.jus.pje.nucleo.enums.RpvTipoCessaoEnum;
import br.jus.pje.nucleo.enums.RpvTipoFormaHonorarioEnum;
import br.jus.pje.nucleo.enums.RpvTipoRestricaoPagamentoEnum;
import br.jus.pje.nucleo.enums.TipoOrgaoPublicoEnum;

import java.util.Arrays;
import java.util.List;

@Name("enumFactory")
@Scope(ScopeType.APPLICATION)
public class EnumFactory {
	@Factory(scope=ScopeType.APPLICATION)
	public TipoOrgaoPublicoEnum[] getTipoOrgaoPublicoEnumValues() {
		return TipoOrgaoPublicoEnum.values();
	}	
	
	@Factory(scope = ScopeType.APPLICATION)
	public RpvTipoRestricaoPagamentoEnum[] getRpvTipoRestricaoPagamentoEnumValues() {
		return RpvTipoRestricaoPagamentoEnum.values();
	}

	@Factory(scope = ScopeType.APPLICATION)
	public RpvTipoFormaHonorarioEnum[] getRpvTipoFormaHonorarioEnumValues() {
		return RpvTipoFormaHonorarioEnum.values();
	}

	@Factory(scope = ScopeType.APPLICATION)
	public RpvPrecatorioEnum[] getRpvPrecatorioEnumValues() {
		return RpvPrecatorioEnum.values();
	}

	@Factory(scope = ScopeType.APPLICATION)
	public NaturezaCreditoEnum[] getNaturezaCreditoEnumValues() {
		return NaturezaCreditoEnum.values();
	}

	@Factory(scope = ScopeType.APPLICATION)
	public EspecieRequisicaoEnum[] getEspecieRequisicaoEnumValues() {
		return EspecieRequisicaoEnum.values();
	}

	@Factory(scope = ScopeType.APPLICATION)
	public ParcialIncontroversoIntegralEnum[] getParcialIncontroversoIntegralEnumValues() {
		return ParcialIncontroversoIntegralEnum.values();
	}

	@Factory(scope = ScopeType.APPLICATION)
	public RpvTipoCessaoEnum[] getRpvTipoCessaoEnumValues() {
		return RpvTipoCessaoEnum.values();
	}

	@Factory(scope = ScopeType.APPLICATION)
	public CondicaoPssEnum[] getCondicaoPssEnumValues() {
		return CondicaoPssEnum.values();
	}

	@Factory(scope = ScopeType.PAGE)
	public List<SimNaoEnum> getOpcoesSimNao() {
		return Arrays.asList(SimNaoEnum.values());
	}
}
