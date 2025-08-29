/**
 * ExpedienteTransform.java.
 *
 * Data: 16/02/2018
 */
package br.jus.cnj.pje.business.dao.transform;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.converters.StringConverter;

import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrfSemFiltro;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.RegistroIntimacao;
import br.jus.pje.nucleo.entidades.RespostaExpediente;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.enums.TipoResultadoAvisoRecebimentoEnum;

/**
 * Classe responsável em transformar o resultado da execução de uma query nativa
 * de expedientes para o objeto ProcessoParteExpediente.
 * 
 * @author Adriano Pamplona
 *
 */
public class ProcessoParteExpedienteTransform extends AbstractTransform<ProcessoParteExpediente> {

	//@formatter:off
	/**
	 * -------------------------------
	 * Ordem dos campos no resultList:
	 * -------------------------------
	 * 0 - id_processo_trf
	 * 1 - nr_processo
	 * 2 - nr_sequencia
	 * 3 - nr_ano
	 * 4 - dt_distribuicao
	 * 5 - in_prioridade
	 * 6 - nm_pessoa_autor
	 * 7 - qt_autor
	 * 8 - nm_pessoa_reu
	 * 9 - qt_reu
	 * 10 - id_orgao_julgador_colegiado
	 * 11 - ds_orgao_julgador_colegiado
	 * 12 - id_orgao_julgador
	 * 13 - ds_orgao_julgador
	 * 14 - id_classe_judicial
	 * 15 - ds_classe_judicial_sigla
	 * 16 - ds_classe_judicial
	 * 17 - ppe.id_processo_parte_expediente
	 * 18 - ppe.dt_ciencia_parte
	 * 19 - ppe.dt_prazo_legal
	 * 20 - ppe.in_fechado
	 * 21 - ppe.nm_pessoa_ciencia
	 * 22 - ppe.nm_pessoa_parte
	 * 23 - ppe.qt_prazo_legal_parte
	 * 24 - ppe.in_tipo_prazo
	 * 25 - pe.id_processo_expediente
	 * 26 - pe.dt_criacao_expediente
	 * 27 - pe.in_meio_expediente 
	 * 28 - rgi.id
	 * 29 - rgi.dt_registro
	 * 30 - rgi.in_resultado
	 * 31 - rex.id
	 * 32 - rex.dt_registro
	 * 33 - tpd.id_tipo_processo_documento
	 * 34 - tpd.ds_tipo_processo_documento
	 * 35 - ptf.in_segredo_justica
	 * 36 - ppe.id_pessoa_parte 
	 * 37 - ptf.cd_classe_judicial
	 * 38 - ptf.nr_identificacao_orgao_justica 
	 * 39 - ptf.id_jurisdicao 
	 * 40 - ptf2.id_competencia 
	 * 41 - p.in_tipo_pessoa 
	 * 42 - ul.ds_nome 
	 * 43 - nr_documento_identificacao 
	 * 44 - ptf.cd_assunto_principal 
	 * 45 - ptf.ds_assunto_principal
	 * 46 - ptf.vl_causa
	 * 47 - ptf.dt_autuacao
	 * 48 - ptf.in_instancia_orgao_julgador
	 * 49 - ptf.cd_ibge_orgao_julgador
	 * 50 - ptf2.in_bloqueia_peticao
	 * 51 - cpt.cd_nivel_acesso
	 * 52 - ptf.ds_ultimo_movimento
	 * 53 - ptf.dt_ultimo_movimento
	 * 54 - ppe.dt_encerrado_manualmente
	 * 55 - ppe.in_enviado_cancelamento
	 * 56 - ppe.in_cancelado
	 * 58 - ppe.in_enviado_domicilio
	 */
	//@formatter:on

	@Override
	public ProcessoParteExpediente transform(Object[] objeto) {
		ProcessoParteExpediente ppe = new ProcessoParteExpediente();
		ProcessoExpediente pe = new ProcessoExpediente();
		RespostaExpediente rex = new RespostaExpediente();
		RegistroIntimacao rgi = new RegistroIntimacao();
		TipoProcessoDocumento tpd = new TipoProcessoDocumento();
		ProcessoTrf ptf = new ProcessoTrf();
		Processo pro = new Processo();
		ConsultaProcessoTrfSemFiltro cpt = new ConsultaProcessoTrfSemFiltro();
		ClasseJudicial cla = new ClasseJudicial();
		OrgaoJulgador oj = null;
		OrgaoJulgadorColegiado ojc = null;

		ptf.setIdProcessoTrf((Integer) objeto[0]);
		pro.setNumeroProcesso((String) objeto[1]);
		ptf.setDataDistribuicao((java.util.Date) objeto[4]);
		cpt.setPrioridade((Boolean) objeto[5]);
		cpt.setAutor((String) objeto[6]);
		cpt.setQtAutor(((BigInteger) objeto[7]).longValue());
		cpt.setReu((String) objeto[8]);
		cpt.setQtReu(((BigInteger) objeto[9]).longValue());
		cpt.setCodigoAssuntoPrincipal((String) objeto[44]);
		cpt.setAssuntoPrincipal((String) objeto[45]);
		cpt.setUltimoMovimento(objeto[52].toString());
		try {
			if (objeto[53] != null && objeto[53].toString().isEmpty() == false
					&& "0".equals(objeto[53].toString()) == false) {
				cpt.setDataUltimoMovimento(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(objeto[53].toString()));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if(objeto[10] != null) {
			ojc = new OrgaoJulgadorColegiado();
			ojc.setIdOrgaoJulgadorColegiado((Integer) objeto[10]);
			ojc.setOrgaoJulgadorColegiado((String) objeto[11]);
		}
		if(objeto[12] != null) {
			oj = new OrgaoJulgador();
			oj.setIdOrgaoJulgador((Integer) objeto[12]);
			oj.setOrgaoJulgador((String) objeto[13]);
			oj.setInstancia(objeto[48] == null ? null : ((java.lang.Character) objeto[48]).toString());
			if(objeto[49] != null) {
				Localizacao loc = new Localizacao();
				Endereco end = new Endereco();
				Cep cep = new Cep();
				Municipio mun = new Municipio();
				mun.setCodigoIbge((String) objeto[49]);
				cep.setMunicipio(mun);
				end.setCep(cep);
				loc.setEndereco(end);
				oj.setLocalizacao(loc);
			}
		}
		
		cpt.setIdOrgaoJulgadorColegiado((Integer) objeto[10]);
		cpt.setOrgaoJulgadorColegiado((String) objeto[11]);
		cpt.setIdOrgaoJulgador((Integer) objeto[12]);
		cpt.setOrgaoJulgador((String) objeto[13]);
		
		cla.setIdClasseJudicial((Integer) objeto[14]);
		cla.setClasseJudicialSigla((String) objeto[15]);
		cla.setClasseJudicial((String) objeto[16]);

		ppe.setIdProcessoParteExpediente((Integer) objeto[17]);
		ppe.setDtCienciaParte((java.util.Date) objeto[18]);
		ppe.setDtPrazoLegal((java.util.Date) objeto[19]);
		ppe.setFechado((Boolean) objeto[20]);
		ppe.setNomePessoaCiencia((String) objeto[21]);
		ppe.setNomePessoaParte((String) objeto[22]);
		ppe.setPrazoLegal((Integer) objeto[23]);

		Character tipoPrazo = (Character) objeto[24];
		if (tipoPrazo != null) {
			ppe.setTipoPrazo(TipoPrazoEnum.valueOf(tipoPrazo.toString()));
		}

		pe.setIdProcessoExpediente((Integer) objeto[25]);
		pe.setDtCriacao((java.util.Date) objeto[26]);

		Character meioExpedicaoExpediente = (Character) objeto[27];
		if (meioExpedicaoExpediente != null) {
			pe.setMeioExpedicaoExpediente(ExpedicaoExpedienteEnum.valueOf(meioExpedicaoExpediente.toString()));
		}

		Integer idRegistroIntimacao = (Integer) objeto[28];
		if (idRegistroIntimacao != null) {
			rgi.setId((Integer) objeto[28]);
			rgi.setData((java.util.Date) objeto[29]);

			Character tipoResultadoAvisoRecebimento = (Character) objeto[30];
			if (tipoResultadoAvisoRecebimento != null) {
				rgi.setResultado(TipoResultadoAvisoRecebimentoEnum.valueOf(tipoResultadoAvisoRecebimento.toString()));
			}

			List<RegistroIntimacao> rgiList = new ArrayList<RegistroIntimacao>(0);
			rgiList.add(rgi);
			ppe.setRegistroIntimacaoList(rgiList);
		}

		Integer idRespostaExpediente = (Integer) objeto[31];
		if (idRespostaExpediente != null) {
			rex.setId((Integer) objeto[31]);
			rex.setData((java.util.Date) objeto[32]);
			ppe.setResposta(rex);
		}

		if(objeto[33] != null) {
			tpd.setIdTipoProcessoDocumento((Integer) objeto[33]);
			tpd.setTipoProcessoDocumento((String) objeto[34]);
		}

		ptf.setSegredoJustica((Boolean) objeto[35]);

		Pessoa pessoaParte = null;
		String tipoPessoa = (String) new StringConverter().convert(null, (Character) objeto[41]);
		if (TipoPessoaEnum.A.toString().equals(tipoPessoa)) {
			pessoaParte = new PessoaAutoridade();
		} else if (TipoPessoaEnum.F.toString().equals(tipoPessoa)) {
			pessoaParte = new PessoaFisica();
			((PessoaFisica) pessoaParte).setNumeroCPF((String) objeto[43]);
		} else if (TipoPessoaEnum.J.toString().equals(tipoPessoa)) {
			pessoaParte = new PessoaJuridica();
			((PessoaJuridica) pessoaParte).setNumeroCNPJ((String) objeto[43]);
		} else {
			pessoaParte = new Pessoa();
		}
		pessoaParte.setIdPessoa((Integer) objeto[36]);
		pessoaParte.setNome((String) objeto[42]);
		ppe.setPessoaParte(pessoaParte);

		cla.setCodClasseJudicial((String) objeto[37]);

		BigDecimal numeroOrgaoJustica = (BigDecimal) objeto[38];
		ptf.setNumeroOrgaoJustica(numeroOrgaoJustica != null ? numeroOrgaoJustica.intValue() : null);

		Jurisdicao jurisdicao = new Jurisdicao();
		jurisdicao.setIdJurisdicao((Integer) objeto[39]);
		ptf.setJurisdicao(jurisdicao);

		Competencia competencia = new Competencia();
		competencia.setIdCompetencia((Integer) objeto[40]);
		ptf.setCompetencia(competencia);

		ptf.setProcesso(pro);
		ptf.setConsultaProcessoTrf(cpt);
		ptf.setClasseJudicial(cla);
		ptf.setOrgaoJulgadorColegiado(ojc);
		ptf.setOrgaoJulgador(oj);

		if (objeto[44] != null) {
			AssuntoTrf assunto = new AssuntoTrf();
			assunto.setCodAssuntoTrf((String) objeto[44]);
			assunto.setAssuntoTrf((String) objeto[45]);

			ProcessoAssunto processoAssunto = new ProcessoAssunto();
			processoAssunto.setAssuntoPrincipal(Boolean.TRUE);
			processoAssunto.setAssuntoTrf(assunto);
			ptf.setProcessoAssuntoList(Arrays.asList(processoAssunto));
		}
		ptf.setInBloqueiaPeticao((Boolean)objeto[50]);
		pe.setTipoProcessoDocumento(tpd);
		ptf.setValorCausa((objeto[46] != null ? ((BigDecimal) objeto[46]).doubleValue() : null ));
		ptf.setDataAutuacao((java.util.Date) objeto[47]);
		ppe.setProcessoJudicial(ptf);
		ppe.setProcessoExpediente(pe);
		ptf.setNivelAcesso((Integer) objeto[51]);
		ppe.setDtEncerramentoManual((java.util.Date) objeto[54]);
		ppe.setEnviadoCancelamento((Boolean) objeto[55]);
		ppe.setCancelado((Boolean) objeto[56]);
		ptf.setInBloqueioMigracao((Boolean)objeto[57]);
		ppe.setEnviadoDomicilio((Boolean)objeto[58]);
		return ppe;
	}
}
