/**
 * PessoaParaParteConverter.java
 * 
 * Data de criação: 23/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.intercomunicacao.v222.beans.Parte;
import br.jus.pje.nucleo.entidades.Pessoa;

/**
 * Conversor de Pessoa para Parte.
 * 
 * @author Adriano Pamplona
 */
@AutoCreate
@Name(PessoaParaParteConverter.NAME)
public class PessoaParaParteConverter
		extends
		IntercomunicacaoConverterAbstrato<Pessoa, Parte> {

	public static final String NAME = "v222.pessoaParaParteConverter";
	
	@In (scope = ScopeType.EVENT, required = false)
	@Out (scope = ScopeType.EVENT, required = false)
	private Map<Integer, br.jus.cnj.intercomunicacao.v222.beans.Pessoa> mapaCachePessoa;
	
	/**
	 * Use o método converter(Pessoa, Boolean).
	 * 
	 * @see br.jus.cnj.pje.intercomunicacao.converter.IntercomunicacaoConverterAbstrato#converter(java.lang.Object)
	 */
	@Override
	@Deprecated
	public Parte converter(Pessoa pessoa) {
		return converter(pessoa, Boolean.FALSE);
	}

	public Parte converter(Pessoa pessoa, Boolean justicaGratuita) {
		Parte resultado = null;
		
		if (isNotNull(pessoa)) {
			if (Boolean.FALSE.equals(isServicoConsultarAvisosPendentes()) && Boolean.FALSE.equals(isServicoConsultarTeorComunicacao())
					&& Boolean.FALSE.equals(isServicoConsultarProcesso())) {
				pessoa = refreshPessoa(pessoa);
			}
			
			resultado = new Parte();
			resultado.setPessoa(obterPessoaIntercomunicacao(pessoa));
			resultado.setAssistenciaJudiciaria(justicaGratuita!=null?justicaGratuita:false);
			
			if(isNull(resultado.getPessoa())){
				resultado.setInteressePublico("N"); //TODO (adriano.pamplona): verificar.
			}
			
			resultado.setIntimacaoPendente(0);
			resultado.setRelacionamentoProcessual(null);//TODO (adriano.pamplona): verificar.
			
			resultado.unsetAdvogado();//TODO (adriano.pamplona): verificar.
			resultado.unsetPessoaProcessualRelacionada();//TODO (adriano.pamplona): verificar.
		}
		return resultado;
	}

	/**
	 * Converter o objeto do PJE Pessoa para MNI Pessoa. O objeto recuperado é armazenado em cache para deixar as 
	 * próximas solicitações para a mesma pessoa mais rápidas.
	 * 
	 * @param pessoa
	 * @return pessoa intercomunicacao.
	 */
	protected br.jus.cnj.intercomunicacao.v222.beans.Pessoa obterPessoaIntercomunicacao(
			Pessoa pessoa) {
		br.jus.cnj.intercomunicacao.v222.beans.Pessoa resultado = null;
		
		if (!getMapaCachePessoa().containsKey(pessoa.getIdPessoa())) {
			PessoaParaIntercomunicacaoPessoaConverter converter = new PessoaParaIntercomunicacaoPessoaConverter();
			resultado = converter.converter(pessoa);
			getMapaCachePessoa().put(pessoa.getIdPessoa(), resultado);
		} else {
			resultado = getMapaCachePessoa().get(pessoa.getIdPessoa());
		}
		return resultado;
	}
	
	/**
	 * Atualiza a instância do objeto passado por parâmetro.
	 * 
	 * @param pessoa Pessoa.
	 * @return Pessoa.
	 */
	protected Pessoa refreshPessoa(Pessoa pessoa) {
		try {
			if (EntityUtil.getEntityManager().contains(pessoa)) {
				EntityUtil.getEntityManager().refresh(pessoa);
			} else if (isNotNull(pessoa) && isNotNull(pessoa.getIdPessoa())) {
				pessoa = EntityUtil.getEntityManager().find(pessoa.getClass(), pessoa.getIdPessoa());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return pessoa;
	}

	/**
	 * @return mapaCachePessoa.
	 */
	protected Map<Integer, br.jus.cnj.intercomunicacao.v222.beans.Pessoa> getMapaCachePessoa() {
		if (mapaCachePessoa == null) {
			mapaCachePessoa = new HashMap<Integer, br.jus.cnj.intercomunicacao.v222.beans.Pessoa>();
		}
		return mapaCachePessoa;
	}
}
