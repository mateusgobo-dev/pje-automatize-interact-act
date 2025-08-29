/**
 * RelacaoPessoalParaRelacionamentoPessoalConverter.java
 * 
 * Data de criação: 23/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import br.jus.cnj.intercomunicacao.v222.beans.ModalidadesRelacionamentoPessoal;
import br.jus.cnj.intercomunicacao.v222.beans.Pessoa;
import br.jus.cnj.intercomunicacao.v222.beans.RelacionamentoPessoal;
import br.jus.pje.nucleo.entidades.RelacaoPessoal;
import br.jus.pje.nucleo.entidades.TipoRelacaoPessoal;

/**
 * Conversor de RelacaoPessoal para RelacionamentoPessoal.
 * 
 * @author Adriano Pamplona
 */
public class RelacaoPessoalParaRelacionamentoPessoalConverter
		extends
		IntercomunicacaoConverterAbstrato<RelacaoPessoal, RelacionamentoPessoal> {

	private static String TUTOR = "TUT";
	private static String CURADOR = "CUR";

	@Override
	public RelacionamentoPessoal converter(RelacaoPessoal relacao) {
		RelacionamentoPessoal resultado = null;

		if (isNotNull(relacao) && isRelacaoValida(relacao)) {
			resultado = new RelacionamentoPessoal();
			resultado.setPessoa(obterPessoa(relacao.getPessoaRepresentante()));
			resultado.setModalidadeRelacionamento(obterModalidadeRelacionemanto(relacao
							.getTipoRelacaoPessoal()));
		}
		return resultado;
	}

	/**
	 * @param tipoRelacaoPessoal
	 * @return modalidade do relacionamento (tutor ou curador)
	 */
	private ModalidadesRelacionamentoPessoal obterModalidadeRelacionemanto(
			TipoRelacaoPessoal tipoRelacaoPessoal) {
		TipoRelacaoPessoalParaModalidadesRelacionamentoPessoalConverter converter = new TipoRelacaoPessoalParaModalidadesRelacionamentoPessoalConverter();
		return converter.converter(tipoRelacaoPessoal);
	}

	/**
	 * @param pessoaRepresentante
	 * @return pessoa do representante.
	 */
	protected Pessoa obterPessoa(
			br.jus.pje.nucleo.entidades.Pessoa pessoaRepresentante) {
		PessoaParaIntercomunicacaoPessoaConverter converter = new PessoaParaIntercomunicacaoPessoaConverter();
		return converter.converter(pessoaRepresentante);
	}

	/**
	 * @param relacao
	 * @return true se a relação não for TUTOR e nem CURADOR.
	 */
	protected boolean isRelacaoValida(RelacaoPessoal relacao) {
		TipoRelacaoPessoal tipoRelacaoPessoal = relacao.getTipoRelacaoPessoal();
		String codigo = tipoRelacaoPessoal.getCodigo();
		return !(codigo.equals(TUTOR) || codigo.equals(CURADOR));
	}
}
