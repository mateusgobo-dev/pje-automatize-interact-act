/**
 * ProcessoParteRepresentanteParaRepresentanteProcessualConverter.java
 * 
 * Data de criação: 23/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.StringUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.home.TipoDocumentoIdentificacaoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v222.beans.CadastroOAB;
import br.jus.cnj.intercomunicacao.v222.beans.Endereco;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeRepresentanteProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.RepresentanteProcessual;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.TipoParte;

/**
 * Conversor de ProcessoParteRepresentante para RepresentanteProcessual.
 * 
 * @author Adriano Pamplona
 */
@AutoCreate
@Name(RepresentanteProcessualParaProcessoParteRepresentanteConverter.NAME)
public class RepresentanteProcessualParaProcessoParteRepresentanteConverter
		extends
		IntercomunicacaoConverterAbstrato<RepresentanteProcessual, ProcessoParteRepresentante> {

	public static final String NAME = "v222.RepresentanteProcessualParaProcessoParteRepresentanteConverter";
	
	/**
	 * @return Instância de RepresentanteProcessualParaProcessoParteRepresentanteConverter.
	 */
	public static RepresentanteProcessualParaProcessoParteRepresentanteConverter instance() {
		return ComponentUtil.getComponent(RepresentanteProcessualParaProcessoParteRepresentanteConverter.class);
	}
	
	@Override
	public ProcessoParteRepresentante converter(RepresentanteProcessual representante) {
		ProcessoParteRepresentante resultado = null;
		
		if (isNotNull(representante)) {
			resultado = new ProcessoParteRepresentante();
			resultado.setTipoRepresentante(obterTipoRepresentante(representante));
			resultado.setRepresentante(obterRepresentante(representante));
		}
		return resultado;
	}

	private Pessoa obterRepresentante(RepresentanteProcessual representante) {
		TipoDocumentoIdentificacaoHome tipoIdentificacaoHome = TipoDocumentoIdentificacaoHome.getHome();
		CadastroOAB oab = representante.getInscricao();
		String documento = representante.getNumeroDocumentoPrincipal();
		List<Endereco> enderecos = representante.getEndereco();
		
		PessoaDocumentoIdentificacao identificacao = new PessoaDocumentoIdentificacao();
		identificacao.setDocumentoPrincipal(Boolean.TRUE);
		identificacao.setNumeroDocumento(documento);
		if (InscricaoMFUtil.isCpfValido(documento)) {
			identificacao.setTipoDocumento(tipoIdentificacaoHome.getTipoDocumentoIdentificacao(TipoDocumentoIdentificacaoHome.TIPOCPF));
		} else if (InscricaoMFUtil.isCnpjValido(documento)) {
			identificacao.setTipoDocumento(tipoIdentificacaoHome.getTipoDocumentoIdentificacao(TipoDocumentoIdentificacaoHome.tipoCPJ));
		}
		
		IntercomunicacaoEnderecoParaEnderecoConverter enderecoConverter = new IntercomunicacaoEnderecoParaEnderecoConverter();
		Pessoa resultado = new Pessoa();
		resultado.setNome(representante.getNome());
		resultado.getPessoaDocumentoIdentificacaoList().add(identificacao);
		resultado.getEnderecoList().addAll(enderecoConverter.converterColecao(enderecos, resultado));
		
		if (oab != null) {
			TipoDocumentoIdentificacao tipoOAB = tipoIdentificacaoHome.getTipoDocumentoIdentificacao(TipoDocumentoIdentificacaoHome.TIPOOAB);
			
			PessoaDocumentoIdentificacao identificacaoOAB = new PessoaDocumentoIdentificacao();
			identificacaoOAB.setDocumentoPrincipal(Boolean.TRUE);
			identificacaoOAB.setNumeroDocumento(oab.getValue());
			identificacaoOAB.setTipoDocumento(tipoOAB);
			identificacaoOAB.setDocumentoPrincipal(StringUtils.equals(documento, oab.getValue()));
			if (!identificacaoOAB.getDocumentoPrincipal()) {
				resultado.getPessoaDocumentoIdentificacaoList().add(identificacaoOAB);
			}
		}
		
		return resultado;
	}

	private TipoParte obterTipoRepresentante(RepresentanteProcessual representante) {
		Map<ModalidadeRepresentanteProcessual, TipoParte> mapa = new HashMap<>();
		mapa.put(ModalidadeRepresentanteProcessual.A, ParametroUtil.instance().getTipoParteAdvogado());
		mapa.put(ModalidadeRepresentanteProcessual.E, ParametroUtil.instance().getTipoParteAdvogado());
		mapa.put(ModalidadeRepresentanteProcessual.D, ParametroUtil.instance().getTipoParteProcurador());
		mapa.put(ModalidadeRepresentanteProcessual.M, ParametroUtil.instance().getTipoParteProcurador());
		mapa.put(ModalidadeRepresentanteProcessual.P, ParametroUtil.instance().getTipoParteRepresentante());
		
		return mapa.get(representante.getTipoRepresentante());
	}
	
}
