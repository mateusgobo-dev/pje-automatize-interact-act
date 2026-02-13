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
package br.jus.pje.nucleo.entidades.filters;

public interface CaixaFilter extends Filter {

	public static final String FILTER_PARAM_ID_PROCESSO = "idProcesso";
	public static final String FILTER_LETRA_OAB = "letraOAB";
	public static final String FILTER_NUMERO_OAB = "numeroOAB";
	public static final String FILTER_UF_OAB = "ufOAB";
	public static final String FILTER_DATA_NASCIMENTO = "dataNascimento";
	public static final String FILTER_NUMERO_CPF = "numeroCpf";
	public static final String FILTER_NUMERO_CNPJ = "numeroCnpj";
	public static final String FILTER_NOME_PARTE = "nomeParte";
	public static final String FILTER_ASSUNTO = "assunto";

	public static final String CONDITION_NUMERO_CNPJ = "exists (SELECT 1 FROM tb_processo_trf p"
			+ "        INNER JOIN tb_processo_parte pp ON (p.id_processo_trf =	pp.id_processo_trf) "
			+ "		 INNER JOIN tb_pessoa_juridica pj ON	(pp.id_pessoa = pj.id_pessoa_juridica) "
			+ "		 INNER JOIN tb_pess_doc_identificacao pdi ON (pdi.id_pessoa = pj.id_pessoa_juridica)"
			+ "		 WHERE p.id_processo_trf = :idProcesso AND pdi.cd_tp_documento_identificacao = 'CPJ'"
			+ "		 AND (pdi.nr_documento_identificacao = nr_cpf_cnpj_parte"
			+ "			  OR nr_cpf_cnpj_parte is null OR nr_cpf_cnpj_parte = ''))";

	public static final String CONDITION_LETRA_OAB = "exists (SELECT 1 FROM tb_processo_trf p "
			+ "INNER JOIN tb_processo_parte pp ON (p.id_processo_trf = "
			+ "pp.id_processo_trf) INNER JOIN tb_pessoa_advogado pa ON "
			+ "(pp.id_pessoa = pa.id) WHERE p.id_processo_trf "
			+ "= :idProcesso AND (pa.ds_letra_oab = ds_letra_oab_parte OR "
			+ "ds_letra_oab_parte is null OR ds_letra_oab_parte = ''))";

	public static final String CONDITION_NUMERO_OAB = "exists (SELECT 1 FROM tb_processo_trf p "
			+ "INNER JOIN tb_processo_parte pp ON (p.id_processo_trf = "
			+ "pp.id_processo_trf) INNER JOIN tb_pessoa_advogado pa ON "
			+ "(pp.id_pessoa = pa.id) WHERE p.id_processo_trf "
			+ "= :idProcesso AND (pa.nr_oab = nr_oab_parte OR nr_oab_parte is null " + "OR nr_oab_parte = ''))";

	public static final String CONDITION_UF_OAB = "exists (SELECT 1 FROM tb_processo_trf p "
			+ "INNER JOIN tb_processo_parte pp ON (p.id_processo_trf = "
			+ "pp.id_processo_trf) INNER JOIN tb_pessoa_advogado pa ON "
			+ "(pp.id_pessoa = pa.id) WHERE p.id_processo_trf "
			+ "= :idProcesso AND (pa.id_uf_oab = id_uf_oab_parte OR " + "id_uf_oab_parte is null))";

	public static final String CONDITION_DATA_NASCIMENTO = "exists (SELECT 1 FROM tb_processo_trf p "
			+ "INNER JOIN tb_processo_parte pp ON (p.id_processo_trf = "
			+ "pp.id_processo_trf) INNER JOIN tb_pessoa_fisica pf ON "
			+ "(pp.id_pessoa = pf.id_pessoa_fisica) WHERE p.id_processo_trf = "
			+ ":idProcesso AND (pf.dt_nascimento BETWEEN dt_ano_nasc_parte_inicio "
			+ "and dt_ano_nasc_parte_fim OR (dt_ano_nasc_parte_inicio " + "is null OR dt_ano_nasc_parte_fim is null)))";

	public static final String CONDITION_NUMERO_CPF = "exists (SELECT 1 FROM tb_processo_trf p "
			+ "        INNER JOIN tb_processo_parte pp ON (p.id_processo_trf = pp.id_processo_trf) "
			+ "		   INNER JOIN tb_pessoa_fisica pf ON (pp.id_pessoa = pf.id_pessoa_fisica)"
			+ "		   INNER JOIN tb_pess_doc_identificacao pdi ON (pdi.id_pessoa = pf.id_pessoa_fisica)"
			+ "		   WHERE p.id_processo_trf = :idProcesso AND pdi.cd_tp_documento_identificacao = 'CPF'"
			+ "		   AND (pdi.nr_documento_identificacao = nr_cpf_cnpj_parte"
			+ "			    OR nr_cpf_cnpj_parte is null OR nr_cpf_cnpj_parte = ''))";

	/*
	 * [PJEII-2170] PJE-JT: Sérgio Ricardo : PJE-1.4.4 
	 * Adição das condições de filtro para NOME PARTE e ASSUNTO
	 */
	public static final String CONDITION_NOME_PARTE = "exists (SELECT 1 FROM tb_processo_trf p"
			+ "      INNER JOIN tb_processo_parte pp ON (p.id_processo_trf =	pp.id_processo_trf) "
			+ "		 INNER JOIN tb_usuario_login u ON	(pp.id_pessoa = u.id_usuario) "
			+ "		 WHERE p.id_processo_trf = :idProcesso AND u.nome = ds_nome_parte";
	
	public static final String CONDITION_ASSUNTO = "exists (SELECT 1 FROM tb_processo_trf p"
			+ "      INNER JOIN tb_processo_assunto pa ON (p.id_processo_trf =	pa.id_processo_trf) "
			+ "		 WHERE p.id_processo_trf = :idProcesso AND pa.id_assunto = id_assunto_trf";	

}