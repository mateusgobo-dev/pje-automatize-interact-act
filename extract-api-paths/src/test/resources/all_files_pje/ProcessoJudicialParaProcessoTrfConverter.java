/**
 * ProcessoJudicialParaProcessoTrfConverter.java
 * 
 * Data de criaição: 23/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ProjetoUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v222.beans.AssuntoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.CabecalhoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.MovimentacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.NumeroUnico;
import br.jus.cnj.intercomunicacao.v222.beans.PoloProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.ProcessoJudicial;
import br.jus.cnj.pje.intercomunicacao.v222.util.ConversorUtil;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Conversor de ProcessoJudicial para ProcessoTrf.
 * 
 * @author Adriano Pamplona
 */
@Name(ProcessoJudicialParaProcessoTrfConverter.NOME)
public class ProcessoJudicialParaProcessoTrfConverter extends
		IntercomunicacaoConverterAbstrato<ProcessoJudicial, ProcessoTrf> {

	public static final String NOME = "v222.processoJudicialParaProcessoTrfConverter";
	
	public static ProcessoJudicialParaProcessoTrfConverter instance() {
		return ComponentUtil.getComponent(ProcessoJudicialParaProcessoTrfConverter.class);
	}
	
	/**
	 * Construtor.
	 */
	public ProcessoJudicialParaProcessoTrfConverter() {
		super();
	}

	@Override
	public ProcessoTrf converter(ProcessoJudicial processoJudicial) {
		ProcessoTrf resultado = null;
		if (isNotNull(processoJudicial)) {
			if (isNotNull(processoJudicial.getDadosBasicos())) {
				resultado = novoProcessoTrf(null);
				
				CabecalhoProcessual dadosBasicos = processoJudicial.getDadosBasicos();
				resultado.setProcesso(obterProcesso(processoJudicial));
				resultado.setClasseJudicial(obterClasseJudicial(dadosBasicos));
				resultado.setOrgaoJulgador(obterOrgaoJulgador(dadosBasicos));
				resultado.setSegredoJustica(obterSegredoJustica(dadosBasicos));
				resultado.setJurisdicao(obterJurisdicao(dadosBasicos));
				resultado.setCompetencia(obterCompetencia(dadosBasicos));
				resultado.setValorCausa(dadosBasicos.getValorCausa());
				resultado.setDataAutuacao(ConversorUtil.converterParaDate(dadosBasicos.getDataAjuizamento()));
				resultado.getProcessoAssuntoList().addAll(obterColecaoProcessoAssunto(dadosBasicos.getAssunto()));
				resultado.getProcessoParteList().addAll(obterColecaoProcessoParte(dadosBasicos.getPolo()));
			}
			
			if (isNotNull(processoJudicial.getDocumento())) {
				resultado = novoProcessoTrf(resultado);
				resultado.getProcesso().getProcessoDocumentoList().addAll(obterColecaoProcessoDocumento(processoJudicial.getDocumento(), resultado.getProcesso()));
			}
			
			if (isNotNull(processoJudicial.getMovimento())) {
				resultado = novoProcessoTrf(resultado);
				resultado.getProcesso().getProcessoEventoList().addAll(obterColecaoProcessoEvento(processoJudicial.getMovimento()));
			}
		}
		
		return resultado;
	}
	
	protected ProcessoTrf novoProcessoTrf(ProcessoTrf processoPadrao) {
		ProcessoTrf resultado = processoPadrao;
		if (resultado == null) {
			resultado = new ProcessoTrf();
			resultado.setProcesso(new Processo());
		}
		return resultado;
	}

	private List<ProcessoParte> obterColecaoProcessoParte(List<PoloProcessual> polos) {
		List<ProcessoParte> partes = new ArrayList<>();
		PoloProcessualParaProcessoParteConverter converter = PoloProcessualParaProcessoParteConverter.instance();
		for (PoloProcessual polo : polos) {
			partes.addAll(converter.converter(polo));
		}
		return partes;
	}

	private List<ProcessoAssunto> obterColecaoProcessoAssunto(List<AssuntoProcessual> assunto) {
		AssuntoProcessualParaProcessoAssuntoConverter converter = AssuntoProcessualParaProcessoAssuntoConverter.instance();
		return converter.converterColecao(assunto);
	}

	private Competencia obterCompetencia(CabecalhoProcessual dadosBasicos) {
		Competencia resultado = new Competencia();
		resultado.setIdCompetencia(dadosBasicos.getCompetencia());
		
		return resultado;
	}

	private Jurisdicao obterJurisdicao(CabecalhoProcessual dadosBasicos) {
		Jurisdicao resultado = new Jurisdicao();
		resultado.setNumeroOrigem(converterParaInt(dadosBasicos.getCodigoLocalidade()));
		
		return resultado;
	}

	private Boolean obterSegredoJustica(CabecalhoProcessual dadosBasicos) {
		int nivelSigilo = dadosBasicos.getNivelSigilo();
		return (nivelSigilo > 0);
	}

	private Processo obterProcesso(ProcessoJudicial processo) {
		CabecalhoProcessual dadosBasicos = processo.getDadosBasicos();
		NumeroUnico nu = dadosBasicos.getNumero();
		
		Processo resultado = new Processo();
		resultado.setNumeroProcesso(nu.getValue());
		return resultado;
	}

	private List<ProcessoEvento> obterColecaoProcessoEvento(List<MovimentacaoProcessual> movimentos) {
		MovimentacaoProcessualParaProcessoEventoConverter converter = MovimentacaoProcessualParaProcessoEventoConverter.instance();
		return converter.converterColecao(movimentos);
	}

	private List<ProcessoDocumento> obterColecaoProcessoDocumento(List<DocumentoProcessual> documentos, Processo processo) {
		DocumentoProcessualParaProcessoDocumentoConverter converter = ComponentUtil.getComponent(DocumentoProcessualParaProcessoDocumentoConverter.class);
		return converter.converterColecao(documentos, processo, null, true, true);
	}

	private ClasseJudicial obterClasseJudicial(CabecalhoProcessual dadosBasicos) {
		ClasseJudicial resultado = new ClasseJudicial();
		resultado.setCodClasseJudicial(String.valueOf(dadosBasicos.getClasseProcessual()));
		resultado.setExigeFiscalLei(dadosBasicos.isIntervencaoMP());
		return resultado;
	}
	
	private OrgaoJulgador obterOrgaoJulgador(CabecalhoProcessual dadosBasicos) {
		br.jus.cnj.intercomunicacao.v222.beans.OrgaoJulgador oj = dadosBasicos.getOrgaoJulgador();

		OrgaoJulgador resultado = new OrgaoJulgador();
		resultado.setIdOrgaoJulgador(Integer.valueOf(oj.getCodigoOrgao()));
		resultado.setOrgaoJulgador(oj.getNomeOrgao());
		return resultado;
	}

}
