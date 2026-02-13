package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.FrequenciaComparecimentoEmJuizo;

public class FrequenciaComparecimentoEmJuizoType extends EnumType<FrequenciaComparecimentoEmJuizo> {
	private static final long serialVersionUID = 6739871300519919665L;

	public FrequenciaComparecimentoEmJuizoType() {
		super(FrequenciaComparecimentoEmJuizo.QUINZENAL);
	}

	public FrequenciaComparecimentoEmJuizoType(FrequenciaComparecimentoEmJuizo f) {
		super(f);
	}
}
