package br.jus.pje.api.converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jboss.util.Strings;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.AssuntoProcessual;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.CabecalhoProcessoSimples;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.CabecalhoProcessual;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.NumeroUnico;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.OrgaoJulgador;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.PoloProcessual;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public class CabecalhoProcessualConverter {

	public CabecalhoProcessual convertCabecalhoProcessualFrom(ProcessoTrf processoTrf) {
		CabecalhoProcessual cabecalhoProcessual = new CabecalhoProcessual();
		
		cabecalhoProcessual.setDadosBasicos(this.convertCabecalhoProcessoSimplesFrom(processoTrf));
		cabecalhoProcessual.setCompetencia(processoTrf.getCompetencia().getIdCompetencia());
		cabecalhoProcessual.setCodigoLocalidade(String.valueOf(processoTrf.getJurisdicao().getNumeroOrigem()));
		cabecalhoProcessual.setNivelSigilo(processoTrf.getSegredoJustica() == true ? 5 : 0);
		cabecalhoProcessual.setDataAjuizamento(processoTrf.getDataAutuacao());
		cabecalhoProcessual.setOrgaoJulgador(cabecalhoProcessual.getOrgaoJulgador());
		cabecalhoProcessual.setProcessoFisico(false);
		cabecalhoProcessual.setPedidoLiminarPendente(processoTrf.getApreciadoTutelaLiminar());
		cabecalhoProcessual.setValorCausa(processoTrf.getValorCausa());
		cabecalhoProcessual.setPolo(this.buildPoloProcessual(processoTrf));
		cabecalhoProcessual.setAssunto(this.buildAssuntosProcessuais(processoTrf.getAssuntoTrfList()));
		cabecalhoProcessual.setAny(buildAny(processoTrf));
		
		return cabecalhoProcessual;
	}
	
	public CabecalhoProcessoSimples convertCabecalhoProcessoSimplesFrom(ProcessoTrf processoTrf) {
		CabecalhoProcessoSimples cabecalhoSimples = new CabecalhoProcessoSimples();
		
		NumeroUnico nu = new NumeroUnico();
		nu.setValue(processoTrf.getProcesso().getNumeroProcesso());
		
		OrgaoJulgador oj = this.buildOrgaoJulgador(processoTrf.getOrgaoJulgador(), processoTrf.getOrgaoJulgadorColegiado());

		cabecalhoSimples.setNumero(nu);
		cabecalhoSimples.setClasseProcessual(Integer.parseInt(processoTrf.getClasseJudicial().getCodClasseJudicial()));
		cabecalhoSimples.setOrgaoJulgador(oj);
		
		return cabecalhoSimples;
	}
	
	private OrgaoJulgador buildOrgaoJulgador(br.jus.pje.nucleo.entidades.OrgaoJulgador orgaoJulgadorLegacy, OrgaoJulgadorColegiado orgaoJulgadorColegiadoLegacy) {
		OrgaoJulgador oj = new OrgaoJulgador();
		
		oj.setCodigo(String.valueOf(orgaoJulgadorLegacy.getIdOrgaoJulgador()));
		oj.setCodigoLocalidade(orgaoJulgadorLegacy.getCodigoOrigem());
		oj.setInstancia(orgaoJulgadorLegacy.getInstancia());
		oj.setNome(orgaoJulgadorLegacy.getOrgaoJulgador());
		
		if(orgaoJulgadorColegiadoLegacy != null) {
			OrgaoJulgador ojc = new OrgaoJulgador();
			
			ojc.setCodigo(String.valueOf(orgaoJulgadorColegiadoLegacy.getIdOrgaoJulgadorColegiado()));
			ojc.setInstancia(orgaoJulgadorColegiadoLegacy.getInstancia());
			ojc.setNome(orgaoJulgadorColegiadoLegacy.getOrgaoJulgadorColegiado());
			oj.setOrgaoColegiado(ojc);
		}
		
		return oj;
	}
	
	private List<PoloProcessual> buildPoloProcessual(ProcessoTrf processoTrf){
		PoloProcessualConverter poloConverter = new PoloProcessualConverter();
		
		List<PoloProcessual> poloProcessual = poloConverter.convertFrom(processoTrf);
		
		return poloProcessual;
	}
	
	private List<Object> buildAny(ProcessoTrf processoTrf){
		List<Object> any = new ArrayList<>();
		Map<String,String> outros = new HashMap<String,String>();
		
		outros.put("nomeClasseProcessual", processoTrf.getClasseJudicial().getClasseJudicial());
		outros.put("siglaEstado", processoTrf.getJurisdicao().getEstado().getCodEstado());
		outros.put("estado", processoTrf.getJurisdicao().getEstado().getEstado());	
		outros.put("jurisdicao", getDocumentoPessoaSecaoJuridica());
		outros.put("numeroVara", processoTrf.getOrgaoJulgador().getNumeroVara().toString());
		outros.putAll(buildCabecaAcao(processoTrf));		 
		any.add(outros);
		
		return any;
	}	

	private String getDocumentoPessoaSecaoJuridica() {
		PessoaJuridica pessoaJuridicaSecao = recuperarPessoaJuridicaSecao();
		if(pessoaJuridicaSecao == null)
			return Strings.EMPTY;
		
		return pessoaJuridicaSecao
				.getPessoaDocumentoIdentificacaoList()
				.stream()
				.filter(x -> x.getTipoDocumento().getCodTipo().equals("CPJ"))
				.map(PessoaDocumentoIdentificacao::getNumeroDocumento)
				.findFirst()
				.orElse(Strings.EMPTY);		
	}

	private PessoaJuridica recuperarPessoaJuridicaSecao() {
		br.jus.pje.nucleo.entidades.OrgaoJulgador orgaoJulgador = Authenticator.getOrgaoJulgadorAtual(); 
		if(orgaoJulgador == null || orgaoJulgador.getJurisdicao().getPessoaJuridicaSecao() == null)
			return null;
		return orgaoJulgador.getJurisdicao().getPessoaJuridicaSecao();
	}	

	private List<AssuntoProcessual> buildAssuntosProcessuais(List<AssuntoTrf> assuntosTrf){
		List<AssuntoProcessual> assuntos = new ArrayList<>();
		AssuntoProcessualConverter converter = new AssuntoProcessualConverter();
		
		for (AssuntoTrf assTrf : assuntosTrf) {
			AssuntoProcessual assuntoProcessual = converter.convertFrom(assTrf);
			assuntos.add(assuntoProcessual);
		}
		
		return assuntos;
	}
	
	private Map<String, String> buildCabecaAcao(ProcessoTrf processoTrf){
		Map<String,String> cabecaAcao = new HashMap<>();
		Optional<ProcessoParte> primeiraParte = retornarPrimeiraParteAtiva(processoTrf);
		
		if(primeiraParte.isPresent()) {
			ProcessoParte processoParte = primeiraParte.get();
			cabecaAcao.put("idPessoaCabecaAcao", processoParte.getPessoa().getDocumentoCpfCnpj(false));
			cabecaAcao.put("nomePessoaCabecaAcao", processoParte.getNomeParte());
		}		
		return cabecaAcao;
	}

	private Optional<ProcessoParte> retornarPrimeiraParteAtiva(ProcessoTrf processoTrf) {
		return processoTrf.getProcessoPartePoloAtivoSemAdvogadoList().stream().findFirst();
	}
}
