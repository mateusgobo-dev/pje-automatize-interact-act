package br.jus.cnj.pje.webservice.client.bnmp;

public enum TipoProcessoDocumentoBNMP {
	
	MANDADO_PRISAO(
			"pje:bnmpii:tipoDocumento:mandadoPrisao",
			"idPecaMandadoBNMP",
			"tipoMandadoPrisao",
			"pje:bnmpii:modelo:mandadoPrisao"
			),
    ORDEM_DESINTERNACAO(
    		"pje:bnmpii:tipoDocumento:ordemDesinternacao", 
    		"idPecaOrdDesinBNMP",
    		"tipoOrdDesinternacao",
    		"pje:bnmpii:modelo:ordemDesinternacao"
    		),
	MANDADO_INTERNACAO(
			"pje:bnmpii:tipoDocumento:mandadoInternacao", 
			"idPecaMandInterBNMP",
			"tipoMandadoInternacao",
			"pje:bnmpii:modelo:mandadoInternacao"
			),
	ALVARA_SOLTURA(
			"pje:bnmpii:tipoDocumento:alvaraSoltura", 
			"idPecaAlvaraSolturaBNMP",
			"tipoAlvaraSoltura",
			"pje:bnmpii:modelo:alvaraSoltura"
			),
	CERTIDAO_EXTINCAO_PUNIBILIDADE_MORTE(
			"pje:bnmpii:tipoDocumento:certidaoExtincao",
			"idPecaCertidaoPunibiBNMP",
			"tipoCertidaoPunibilidade",
			"pje:bnmpii:modelo:certidaoExtincao"
			),
	CERTIDAO_ARQUIVAMENTO_GUIA(
			"pje:bnmpii:tipoDocumento:certidaoArquivamento", 
			"idPecaCertidaoArquivamentoBNMP",
			"tipoCertidaoArquivamento",
			"pje:bnmpii:modelo:certidaoArquivamento"
			),
	GUIA_INTERNACAO(
			"pje:bnmpii:tipoDocumento:guiaInternacao", 
			"idPecaGuiaInternacaoBNMP",
			"tipoGuiaInternacao",
			"pje:bnmpii:modelo:guiaInternacao"
			),
	GUIA_RECOLHIMENTO(
			"pje:bnmpii:tipoDocumento:guiaRecolhimento", 
			"idPecaGuiaRecolhimentoBNMP",
			"tipoGuiaRecolhimento",
			"pje:bnmpii:modelo:guiaRecolhimento"
			),
	CERTIDAO_CUMPRIMENTO_MANDADO_INTERNACAO(
			"pje:bnmpii:tipoDocumento:certidaoCumprimentoInternacao", 
			"idPecaGuiaCumpriMandInterBNMP",
			"tipoCumprimentoMandadoInternacao",
			"pje:bnmpii:modelo:certidaoCumprimentoInternacao"
			),
	CONTRAMANDADO(
			"pje:bnmpii:tipoDocumento:contramandado", 
			"idPecaContramandadoBNMP",
			"tipoContramandado",
			"pje:bnmpii:modelo:contramandado"
			),
	CERTIDAO_CUMPRIMENTO_MANDADO_PRISAO(
			"pje:bnmpii:tipoDocumento:certidaoCumprimentoPrisao", 
			"idPecaCertiCumpriMandaPrisaoBNMP",
			"tipoCertidaoCumpriMandadoPrisao",
			"pje:bnmpii:modelo:certidaoCumprimentoPrisao"
			);
	
    private String parametroTipoDocumento;
    
    private String variavelIdPeca;
    
    private String variavelTipoPeca;
    
    private String parametroModeloDocumento;
    
	TipoProcessoDocumentoBNMP(String parametroTipoDocumento, String variavelIdPeca, String variavelTipoPeca, String parametroModeloDocumento){
		
		this.setParametroTipoDocumento(parametroTipoDocumento);
        this.setVariavelIdPeca(variavelIdPeca);
        this.setVariavelTipoPeca(variavelTipoPeca);
        this.setParametroModeloDocumento(parametroModeloDocumento);
    }

	public String getParametroTipoDocumento() {
		return parametroTipoDocumento;
	}

	public void setParametroTipoDocumento(String parametroTipoDocumento) {
		this.parametroTipoDocumento = parametroTipoDocumento;
	}

	public String getVariavelIdPeca() {
		return variavelIdPeca;
	}

	public void setVariavelIdPeca(String variavelIdPeca) {
		this.variavelIdPeca = variavelIdPeca;
	}

	public String getVariavelTipoPeca() {
		return variavelTipoPeca;
	}

	public void setVariavelTipoPeca(String variavelTipoPeca) {
		this.variavelTipoPeca = variavelTipoPeca;
	}
	/**
	 * Busca TipoProcessoDocumentoBNMP por codigo
	 * 
	 * 
	 * @param codigo
	 * @return TipoProcessoDocumentoBNMP
	 */
	public static TipoProcessoDocumentoBNMP buscaPor(String parametroTipoDocumento) {
		
		for (TipoProcessoDocumentoBNMP tipoProcessoDocumentoBNMP : TipoProcessoDocumentoBNMP.values()) {
			if (tipoProcessoDocumentoBNMP.getParametroTipoDocumento().equalsIgnoreCase(parametroTipoDocumento)) {
				return tipoProcessoDocumentoBNMP;
			}
		}
		return null;
	}
	
	public String getParametroModeloDocumento() {
		return parametroModeloDocumento;
	}

	public void setParametroModeloDocumento(String parametroModeloDocumento) {
		this.parametroModeloDocumento = parametroModeloDocumento;
	}

	public static String getParametroModeloPor(String parametroTipoDocumento) {
		for (TipoProcessoDocumentoBNMP p : values()) {
			if (p.getParametroTipoDocumento().equals(parametroTipoDocumento)) {
				return p.getParametroModeloDocumento();
			}
		}
		return null;
	}
}
