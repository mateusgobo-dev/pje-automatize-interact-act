/**
 * ProcessoParteProcuradoriaParaRepresentanteProcessualConverter.java
 * 
 * Data de criação: 14/09/2016
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.intercomunicacao.v222.beans.Endereco;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeRepresentanteProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.RepresentanteProcessual;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;

/**
 * Conversor de ProcessoParte Procuradoria para RepresentanteProcessual.
 * 
 * @author Adriano Pamplona
 */
@AutoCreate
@Name(ProcessoParteProcuradoriaParaRepresentanteProcessualConverter.NAME)
public class ProcessoParteProcuradoriaParaRepresentanteProcessualConverter
		extends
		IntercomunicacaoConverterAbstrato<ProcessoParte, RepresentanteProcessual> {

	public static final String NAME = "v222.processoParteProcuradoriaParaRepresentanteProcessualConverter";
	
	@In (EnderecoParaIntercomunicacaoEnderecoConverter.NAME)
	private EnderecoParaIntercomunicacaoEnderecoConverter enderecoParaIntercomunicacaoEnderecoConverter; 
	
	@Override
	public RepresentanteProcessual converter(ProcessoParte processoParte) {
		RepresentanteProcessual resultado = null;
		processoParte = EntityUtil.refreshEntity(processoParte);
		if (isNotNull(processoParte) && isProcuradoria(processoParte)) {
			Procuradoria procuradoria = processoParte.getProcuradoria();
			
			resultado = new RepresentanteProcessual();
			resultado.setNome(procuradoria.getNome());
			resultado.setTipoRepresentante(obterTipoRepresentante(procuradoria));
			resultado.setNumeroDocumentoPrincipal(obterNumeroDocumentoPrincipal(procuradoria));
			resultado.getEndereco().clear();
			Endereco endereco = obterEndereco(procuradoria);
			if (isNotNull(endereco)) { 
				resultado.getEndereco().add(endereco);
			}
			
		}
		return resultado;
	}
	
	/**
	 * Retorna true se o representante for uma procuradoria.
	 * 
	 * @param processoParte ProcessoParte
	 * @return booleano
	 */
	private boolean isProcuradoria(ProcessoParte processoParte) {
		return (processoParte.getProcuradoria() != null);
	}

	/**
	 * Retorna a ModalidadeRepresentanteProcessual do TipoProcuradoriaEnum.
	 * 
	 * @param procuradoria Procuradoria
	 * @return ModalidadeRepresentanteProcessual
	 */
	private ModalidadeRepresentanteProcessual obterTipoRepresentante(Procuradoria procuradoria) {
		ModalidadeRepresentanteProcessual resultado = null;
		if(procuradoria.getTipo().equals(TipoProcuradoriaEnum.D)){
			resultado = ModalidadeRepresentanteProcessual.D;
		}else{
			resultado = ModalidadeRepresentanteProcessual.P;
		}
		return resultado;
	}
	
	/**
	 * Retorna o número do documento principal da pessoa jurídica vinculada à procuradoria.
	 * 
	 * @param procuradoria Procuradoria
	 * @return Número do documento principal.
	 */
	private String obterNumeroDocumentoPrincipal(Procuradoria procuradoria) {
		String resultado = null;
		
		PessoaJuridica pj = procuradoria.getPessoaJuridica();
		if (pj != null) {
			resultado = InscricaoMFUtil.retiraMascara(pj.getDocumentoCpfCnpj());
		}
		
		return resultado;
	}

	/**
	 * @param procuradoria
	 * @return endereço da procuradoria.
	 */
	protected Endereco obterEndereco(Procuradoria procuradoria) {
		
		Localizacao localizacao = procuradoria.getLocalizacao();
		br.jus.pje.nucleo.entidades.Endereco pjeEndereco = localizacao.getEndereco();
		return getEnderecoParaIntercomunicacaoEnderecoConverter().converter(pjeEndereco);
	}
	
	/**
	 * @return the enderecoParaIntercomunicacaoEnderecoConverter
	 */
	protected EnderecoParaIntercomunicacaoEnderecoConverter getEnderecoParaIntercomunicacaoEnderecoConverter() {
		if (enderecoParaIntercomunicacaoEnderecoConverter == null) {
			enderecoParaIntercomunicacaoEnderecoConverter = ComponentUtil.getComponent(
					EnderecoParaIntercomunicacaoEnderecoConverter.class);
		}
		return enderecoParaIntercomunicacaoEnderecoConverter;
	}
}
