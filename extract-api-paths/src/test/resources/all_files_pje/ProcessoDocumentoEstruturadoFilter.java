/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades.editor.filters;

import br.jus.pje.nucleo.entidades.filters.Filter;

public class ProcessoDocumentoEstruturadoFilter implements Filter {

	public static final String FILTER_PROCESSO_TOPICO_ATIVO = "processoDocumentoEstruturadoTopicoList";
	public static final String CONDITION_PROCESSO_TOPICO_ATIVO = "in_ativo = 'true'";
}
