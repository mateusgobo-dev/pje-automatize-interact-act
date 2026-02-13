/**
 * EnderecoParaIntercomunicacaoEnderecoConverter.java
 * 
 * Data de criação: 23/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Conversor de Endereço para Endereço da intercomunicação.
 * 
 * @author Adriano Pamplona
 */
@AutoCreate
@Name(EnderecoParaIntercomunicacaoEnderecoConverter.NAME)
public class EnderecoParaIntercomunicacaoEnderecoConverter
		extends
		IntercomunicacaoConverterAbstrato<Endereco, br.jus.cnj.intercomunicacao.v222.beans.Endereco> {

	public static final String NAME = "v222.enderecoParaIntercomunicacaoEnderecoConverter";
	
	@Override
	public br.jus.cnj.intercomunicacao.v222.beans.Endereco converter(Endereco endereco) {
		br.jus.cnj.intercomunicacao.v222.beans.Endereco resultado = null;
		
		if (isNotNull(endereco)) {
			resultado = new br.jus.cnj.intercomunicacao.v222.beans.Endereco();
			resultado.setBairro(endereco.getNomeBairro());
			resultado.setCep(StringUtil.removeNaoNumericos(endereco.getCep().getNumeroCep()));
			resultado.setCidade(endereco.getNomeCidade());
			resultado.setComplemento(endereco.getComplemento());
			resultado.setEstado(endereco.getCep().getMunicipio().getEstado().getCodEstado());
			resultado.setLogradouro(endereco.getNomeLogradouro());
			resultado.setNumero(endereco.getNumeroEndereco());
		}
		return resultado;
	}

}
