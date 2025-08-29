package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.HashMap;
import java.util.Map;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.com.infox.exceptions.NegocioException;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoIdentificacao;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeDocumentoIdentificador;
import br.jus.cnj.intercomunicacao.v222.beans.TipoQualificacaoPessoa;
import br.jus.cnj.pje.nucleo.manager.TipoDocumentoIdentificacaoManager;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Name(DocumentoIdentificacaoParaTipoDocumentoIdentificacaoConverter.NAME)
@AutoCreate
public class DocumentoIdentificacaoParaTipoDocumentoIdentificacaoConverter
		extends
		IntercomunicacaoConverterAbstrato<DocumentoIdentificacao, TipoDocumentoIdentificacao> {
	
	public static final String NAME = "v222.documentoIdentificacaoParaTipoDocumentoIdentificacaoConverter";
	
	@In
	private TipoDocumentoIdentificacaoManager tipoDocumentoIdentificacaoManager;
	
	public static final String CODIGO_TIPO_DOCUMENTO_CPF = "CPF";
	public static final String CODIGO_TIPO_DOCUMENTO_CNPJ = "CPJ";
	
	private Map<TipoQualificacaoPessoa,TipoPessoaEnum> mapTipoQualificacaoPessoa = 
			new HashMap<TipoQualificacaoPessoa,TipoPessoaEnum>();
	
	private MultipleEntryMap<ModalidadeDocumentoIdentificador, String, TipoQualificacaoPessoa> mapModalidadeDocumentoIdentificador = new 
			MultipleEntryMap<ModalidadeDocumentoIdentificador, String, TipoQualificacaoPessoa>();
	
	{
		mapTipoQualificacaoPessoa.put(TipoQualificacaoPessoa.FISICA, TipoPessoaEnum.F);
		mapTipoQualificacaoPessoa.put(TipoQualificacaoPessoa.JURIDICA, TipoPessoaEnum.J);
		mapTipoQualificacaoPessoa.put(TipoQualificacaoPessoa.AUTORIDADE, TipoPessoaEnum.A);
		mapTipoQualificacaoPessoa.put(TipoQualificacaoPessoa.ORGAOREPRESENTACAO, TipoPessoaEnum.A);

		mapModalidadeDocumentoIdentificador.putMultiple(ModalidadeDocumentoIdentificador.CMF, 
				TipoQualificacaoPessoa.FISICA, CODIGO_TIPO_DOCUMENTO_CPF, 
				TipoQualificacaoPessoa.JURIDICA, CODIGO_TIPO_DOCUMENTO_CNPJ);
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.CI,"RG");
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.PAS,"PAS");
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.TE,"TIT");
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.CNH,"CNH");
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.CN,"CNA");
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.CC,"CCA");
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.CT,"CTR");
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.RIC,"RIC");
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.PIS_PASEP,"PIS");
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.CEI,"CEI");
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.NIT,"NIT");
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.CP,"CCP");
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.IF,"IDF");
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.RGE,"RGE");
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.OAB,"OAB");
		mapModalidadeDocumentoIdentificador.put(ModalidadeDocumentoIdentificador.RJC,"RJC");
	}
	
	public TipoDocumentoIdentificacao converter(
			DocumentoIdentificacao documentoIdentificacao, TipoQualificacaoPessoa tipoQualificacaoPessoa) {

		if (tipoQualificacaoPessoa == null) {
			throw new NegocioException(
					"Não foi definido um tipo de Qualificação de pessoa.");
		}
		if (documentoIdentificacao.getTipoDocumento() == null) {
			throw new NegocioException(
					"Não foi definido uma modalidade identificador de documento.");
		}
		
		TipoDocumentoIdentificacao tipoDocumentoIdentificacao = null;

		String codigo = mapModalidadeDocumentoIdentificador.get(documentoIdentificacao.getTipoDocumento(), tipoQualificacaoPessoa);			

		tipoDocumentoIdentificacao = tipoDocumentoIdentificacaoManager.carregarTipoDocumentoIdentificacao(codigo);

		if (tipoDocumentoIdentificacao == null && codigo != null) {
			throw new NegocioException(String.format(
					"Tipo de documento %s não cadastrado ou inválido para o tipo de pessoa \"%s\"", 
					documentoIdentificacao.getTipoDocumento().value(), tipoQualificacaoPessoa.value()));
						
		}

		return tipoDocumentoIdentificacao;

	}

	
	@Override
	public TipoDocumentoIdentificacao converter(DocumentoIdentificacao documentoIdentificacao) {
		throw new UnsupportedOperationException("Usar converter(DocumentoIdentificacao documentoIdentificacao, "
				+ "TipoQualificacaoPessoa tipoQualificacaoPessoa)");
	}
}
