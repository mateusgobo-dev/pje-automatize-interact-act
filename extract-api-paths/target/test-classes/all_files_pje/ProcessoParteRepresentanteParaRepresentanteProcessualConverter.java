/**
 * ProcessoParteRepresentanteParaRepresentanteProcessualConverter.java
 * 
 * Data de criação: 23/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.trf.webservice.ConsultaClienteOAB;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.intercomunicacao.v222.beans.CadastroOAB;
import br.jus.cnj.intercomunicacao.v222.beans.Endereco;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeRepresentanteProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.RepresentanteProcessual;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.EstadoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAdvogadoManager;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.PessoaAdvogadoTipoInscricaoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.ws.externo.cna.entidades.DadosAdvogadoOAB;

/**
 * Conversor de ProcessoParteRepresentante para RepresentanteProcessual.
 * 
 * @author Adriano Pamplona
 */
@AutoCreate
@Name(ProcessoParteRepresentanteParaRepresentanteProcessualConverter.NAME)
public class ProcessoParteRepresentanteParaRepresentanteProcessualConverter
		extends
		IntercomunicacaoConverterAbstrato<ProcessoParteRepresentante, RepresentanteProcessual> {

	public static final String NAME = "v222.processoParteRepresentanteParaRepresentanteProcessualConverter";
	
	@In (EnderecoParaIntercomunicacaoEnderecoConverter.NAME)
	private EnderecoParaIntercomunicacaoEnderecoConverter enderecoParaIntercomunicacaoEnderecoConverter; 
	
	@Logger
	private Log log;
	
	@Override
	public RepresentanteProcessual converter(ProcessoParteRepresentante representante) {
		RepresentanteProcessual resultado = null;
		
		if (isNotNull(representante)) {
			representante = refreshRepresentante(representante);
			if (isAdvogado(representante)) {
				PessoaAdvogado advogado = ((PessoaFisica) representante.getRepresentante()).getPessoaAdvogado();
			
				if (advogado != null) {
					resultado = new RepresentanteProcessual();
					resultado.setNome(advogado.getNome());
					resultado.setTipoRepresentante(ModalidadeRepresentanteProcessual.A);
					resultado.setInscricao(obterInscricao(advogado));
					resultado.setNumeroDocumentoPrincipal(InscricaoMFUtil.retiraMascara(advogado.getNumeroCPF()));
					resultado.getEndereco().clear();
					resultado.getEndereco().addAll(consultarColecaoEndereco(representante));
				}
			}
		}
		return resultado;
	}
	
	protected ProcessoParteRepresentante refreshRepresentante(ProcessoParteRepresentante representante) {
		try {
			if (EntityUtil.getEntityManager().contains(representante)) {
				EntityUtil.getEntityManager().refresh(representante);
			} else if (isNotNull(representante) && isNotNull(representante.getIdProcessoParteRepresentante())) {
				representante = EntityUtil.getEntityManager().find(representante.getClass(), representante.getIdProcessoParteRepresentante());
			}
		} catch (Exception e) {
			log.error("Erro ao realizar refresh nos dados do representante: " + e.getLocalizedMessage() + ".", e);
		}
		return representante;
	}
	
	/**
	 * @param advogado
	 * @return inscrição (oab)
	 */
	protected CadastroOAB obterInscricao(PessoaAdvogado advogado) {
		CadastroOAB oab = new CadastroOAB();
		oab.setValue(formatarNumeroOAB(advogado));
		return oab;
	}
	
	/**
	 * @param representante
	 * @return true se o representante for do tipo advogado.
	 */
	protected boolean isAdvogado(ProcessoParteRepresentante representante) {
		TipoParte tipo = representante.getTipoRepresentante();
		TipoParte tipoAdvogado = ParametroUtil.instance().getTipoParteAdvogado();
		
		return tipo.equals(tipoAdvogado);
	}
	
	/**
	 * Formatad o número da oab do advogado.
	 * 
	 * @param advogado
	 * @return número oab formatado.
	 */
	protected String formatarNumeroOAB(PessoaAdvogado advogado) {
		StringBuilder resultado = new StringBuilder();
		
		String numeroOAB = null;
		if (advogado != null) {
			numeroOAB = advogado.getNumeroOAB();
			
			if (StringUtils.isBlank(numeroOAB)) {
				DadosAdvogadoOAB dadosOAB = obterDadosAdvogadoOAB(advogado);
				if (isNotNull(dadosOAB)) {
					EstadoManager estadoManager = ComponentUtil.getComponent(EstadoManager.NAME);
					
					advogado.setNumeroOAB(dadosOAB.getNumInscricao());
					advogado.setUfOAB(estadoManager.findBySigla(dadosOAB.getUf()));
					advogado.setTipoInscricao(
							PessoaAdvogadoTipoInscricaoEnum.obter(
									dadosOAB.getTipoInscricao(), 
									PessoaAdvogadoTipoInscricaoEnum.A));
					
					atualizarPessoaAdvogado(advogado);
					
					numeroOAB = advogado.getNumeroOAB();
				}
				
			}
			
			numeroOAB = StringUtil.removeNaoNumericos(numeroOAB);
			
			if (StringUtils.isNotBlank(numeroOAB)) {
				advogado = EntityUtil.refreshEntity(advogado);
				resultado.append((advogado.getUfOAB() != null ? advogado.getUfOAB().getCodEstado() : ""));
				resultado.append(StringUtils.leftPad(numeroOAB, 7, "0"));
				resultado.append(advogado.getTipoInscricao());
			}
		}

		return resultado.toString();
	}
	
	/**
	 * Retorna os dados do advogado através do CPF do advogado.
	 * 
	 * @param advogado Advogado com CPF.
	 * @return Dados do advogado.
	 */
	protected DadosAdvogadoOAB obterDadosAdvogadoOAB(PessoaAdvogado advogado) {
		DadosAdvogadoOAB resultado = null;
		
		if (isNotNull(advogado) && isNotVazio(advogado.getDocumentoCpfCnpj())) {
			String cpf = advogado.getDocumentoCpfCnpj();
			List<DadosAdvogadoOAB> advogados = ConsultaClienteOAB.instance().consultaDadosBase(cpf);
			
			if (isVazio(advogados)) {
				return null;
			} else if (advogados.size() > 1) {
				for (int indice = 0; indice < advogados.size() && isNull(resultado); indice++) {
					DadosAdvogadoOAB adv = advogados.get(indice);
					if (adv != null & adv.getTipoInscricao() != null && adv.getTipoInscricao().equalsIgnoreCase("advogado")) {
						resultado = adv;
					}
				}
			} else {
				resultado = advogados.get(0);
			}
		}
		
		
		return resultado;
	}
	
	/**
	 * Atualiza os dados do advogado.
	 * 
	 * @param advogado
	 */
	protected void atualizarPessoaAdvogado(PessoaAdvogado advogado) {
		PessoaAdvogadoManager pessoaAdvogadoManager = ComponentUtil.getComponent(PessoaAdvogadoManager.NAME);
		
		try {
			pessoaAdvogadoManager.merge(advogado);
			pessoaAdvogadoManager.flush();
		} catch (PJeBusinessException e) {
			String mensagem = e.getLocalizedMessage();
			throw new NegocioException((mensagem != null ? mensagem : e.getMessage()));
		}
	}

	/**
	 * @param representante
	 * @return coleção de endereços da parte do processo.
	 */
	protected Collection<Endereco> consultarColecaoEndereco(ProcessoParteRepresentante representante) {
		representante = EntityUtil.refreshEntity(representante);
		List<br.jus.pje.nucleo.entidades.Endereco> enderecos = representante.getParteRepresentante().getEnderecos();
		//Código abaixo inserido para evitar listas de endereço com objetos de mesma instância.
		List<br.jus.pje.nucleo.entidades.Endereco> enderecosNaoDuplicados =
			    new ArrayList<br.jus.pje.nucleo.entidades.Endereco>(new LinkedHashSet<br.jus.pje.nucleo.entidades.Endereco>(enderecos));
		return getEnderecoParaIntercomunicacaoEnderecoConverter().converterColecao(enderecosNaoDuplicados);
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
