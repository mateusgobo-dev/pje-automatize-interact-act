package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.jboss.seam.annotations.Name;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.VotoBlocoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.BlocoComposicao;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoBloco;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.entidades.VotoBloco;
import br.jus.pje.nucleo.enums.ContextoVotoEnum;
import br.jus.pje.nucleo.enums.TipoVotoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("votoBlocoManager")
public class VotoBlocoManager extends BaseManager<VotoBloco>{

	@Override
	protected VotoBlocoDAO getDAO() {
		return ComponentUtil.getVotoBlocoDAO();
	}

	public HashMap<String, Long> recuperarPlacar(BlocoJulgamento bloco) {
		HashMap<String,Long> placar = new HashMap<String, Long>();
		placar.put("procedente", contagemVotos(bloco, "C"));
		placar.put("parcialmente", contagemVotos(bloco, "P"));
		placar.put("contra", contagemVotos(bloco, "D"));
		return placar;
	}
	
	/**
	 * Recupera o número de votos proferidos no bloco, especificamente
	 * com o contexto dado {@link TipoVotoEnum#toString()} e que não se trate de um tipo de voto
	 * do relator.
	 * 
	 * @param bloco
	 * @param contexto
	 * @return
	 */
	private long contagemVotos(BlocoJulgamento bloco, String contexto){
		Search s = new Search(VotoBloco.class);
		addCriteria(s,
				Criteria.equals("bloco", bloco),
				Criteria.equals("tipoVoto.contexto", contexto),
				Criteria.equals("tipoVoto.relator", false));
		return count(s);
	}

	public List<VotoBloco> recuperarVotos(BlocoJulgamento bloco){
		Search s = new Search(VotoBloco.class);
		addCriteria(s,
				Criteria.equals("bloco", bloco));
		return list(s);
	}

	public List<VotoBloco> recuperarVotosVogais(BlocoJulgamento bloco){
		Search s = new Search(VotoBloco.class);
		addCriteria(s,
				Criteria.equals("bloco", bloco),
				Criteria.equals("tipoVoto.relator", false));
		return list(s);
	}
	
	public List<VotoBloco> recuperarVotosDivergentes(BlocoJulgamento bloco){
		Search s = new Search(VotoBloco.class);
		addCriteria(s,
				Criteria.equals("bloco", bloco),
				Criteria.equals("tipoVoto.contexto", (ContextoVotoEnum.D).getContexto()));
		return list(s);
	}

	public List<OrgaoJulgador> recuperarOrgaosDivergentes(BlocoJulgamento bloco){
		return getDAO().recuperarOrgaosDivergentes(bloco);
	}
	
	public VotoBloco recuperarVoto(BlocoJulgamento bloco, OrgaoJulgador orgao) {
		Search s = new Search(VotoBloco.class);
		addCriteria(s,
				Criteria.equals("bloco", bloco),
				Criteria.equals("orgaoJulgador", orgao));
		s.setMax(1);
		List<VotoBloco> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}
	
	public VotoBloco recuperarVoto(BlocoComposicao composicao) {
		return this.recuperarVoto(composicao.getBloco(), composicao.getOrgaoJulgador());
	}
	
	public TipoVoto recuperarVotoDoRelator(BlocoJulgamento bloco) {
		TipoVoto retorno = null;
		ProcessoBloco primeiroProcessoBloco = ComponentUtil.getProcessoBlocoManager().recuperaPrimeiroProcessoBloco(bloco);
		SessaoProcessoDocumentoVoto votoPrimeiroProcessoBloco = ComponentUtil.getSessaoProcessoDocumentoVotoManager().recuperarVoto(bloco.getSessao(), primeiroProcessoBloco.getProcessoTrf(), bloco.getOrgaoJulgadorRelator());
		if(votoPrimeiroProcessoBloco != null) {
			retorno = votoPrimeiroProcessoBloco.getTipoVoto();
		}
		return retorno;
	}
	
	public boolean existeDivergente(BlocoComposicao blocoComposicao) {
		return existeDivergente(blocoComposicao.getBloco());
	}
	
	
	public boolean existeDivergente(BlocoJulgamento bloco) {
		return !(recuperarVotosDivergentes(bloco).isEmpty());
	}
	
	public OrgaoJulgador contagemMaioriaVotacao(BlocoJulgamento bloco) {
		return this.getDAO().contagemMaioriaVotacao(bloco);
	}
	
	public void registrarVotacaoUnanime(BlocoJulgamento bloco, List<BlocoComposicao> listComposicaoBloco) throws PJeBusinessException {
		TipoVoto acompanhaRelator = ComponentUtil.getTipoVotoManager().recuperaAcompanhaRelator();
		bloco.setOrgaoJulgadorVencedor(bloco.getOrgaoJulgadorRelator());
		ComponentUtil.getBlocoJulgamentoManager().mergeAndFlush(bloco);
		for(BlocoComposicao votante: listComposicaoBloco){
			if(votante.getOrgaoJulgador().equals(bloco.getOrgaoJulgadorRelator())){
				continue;
			}
			VotoBloco voto = recuperarVoto(votante);
			if(voto == null){
				voto = new VotoBloco();
				voto.setBloco(bloco);
				voto.setOrgaoJulgador(votante.getOrgaoJulgador());
			}
			voto.setTipoVoto(acompanhaRelator);
			voto.setDataVoto(new Date());
			voto.setOjAcompanhado(bloco.getOrgaoJulgadorRelator());
			ComponentUtil.getVotoBlocoManager().persistAndFlush(voto);
			List<SessaoPautaProcessoTrf> processosPautados = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperar(bloco);
			for (SessaoPautaProcessoTrf processoPautado : processosPautados){
				ComponentUtil.getSessaoProcessoDocumentoVotoManager().registrarVotacao(processoPautado, votante.getOrgaoJulgador(), bloco.getOrgaoJulgadorRelator(), acompanhaRelator, isVotoRelator(voto));
				processoPautado.setOrgaoJulgadorVencedor(bloco.getOrgaoJulgadorRelator());
				ComponentUtil.getSessaoPautaProcessoTrfManager().mergeAndFlush(processoPautado);
			}
		}
	}
	
	private List<VotoBloco> getVotosAcompanhantes(BlocoJulgamento bloco, OrgaoJulgador orgao) {
		Search s = new Search(VotoBloco.class);
		addCriteria(s,
				Criteria.equals("bloco", bloco),
				Criteria.equals("ojAcompanhado", orgao));
		return list(s);
	}
	
	public void removerVoto(BlocoComposicao votante) throws Exception {
		VotoBloco votoremover = ComponentUtil.getVotoBlocoManager().recuperarVoto(votante);
		this.removerVoto(votoremover);
	}
	
	public void removerVoto(VotoBloco voto) throws Exception {
		this.remove(voto);
		this.flush();
		List<VotoBloco> votosAcompanhantes = getVotosAcompanhantes(voto.getBloco(), voto.getOrgaoJulgador());
		for(VotoBloco vot : votosAcompanhantes){
			vot.setOjAcompanhado(vot.getOrgaoJulgador());
			this.mergeAndFlush(vot);
		}
		List<SessaoPautaProcessoTrf> processosPautados = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperar(voto.getBloco());
		for (SessaoPautaProcessoTrf processoPautado : processosPautados){
			ComponentUtil.getSessaoProcessoDocumentoVotoManager().removerVoto(processoPautado, voto.getOrgaoJulgador());
		}
		ComponentUtil.getBlocoJulgamentoManager().setaMaioriaVotacao(voto.getBloco());
	}
	
	public void registrarVoto(VotoBloco votoBloco) throws Exception {
		this.mergeAndFlush(votoBloco);
		List<SessaoPautaProcessoTrf> processosPautados = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperar(votoBloco.getBloco());
		for (SessaoPautaProcessoTrf processoPautado : processosPautados){
			ComponentUtil.getSessaoProcessoDocumentoVotoManager().registrarVotacao(processoPautado, votoBloco.getOrgaoJulgador(), votoBloco.getOjAcompanhado(), votoBloco.getTipoVoto(), isVotoRelator(votoBloco));
		}
		ComponentUtil.getBlocoJulgamentoManager().setaMaioriaVotacao(votoBloco.getBloco());
	}
	
	public boolean isVotoRelator(VotoBloco voto) {
		OrgaoJulgador ojBloco = voto.getBloco().getOrgaoJulgadorRelator(); 
		OrgaoJulgador ojVoto = voto.getOrgaoJulgador();
		return (ojVoto.equals(ojBloco));
	}
	
	public void registrarVotoBloco(OrgaoJulgador orgao, OrgaoJulgador orgaoAcompanhado, BlocoJulgamento bloco) throws Exception {
		VotoBloco votoBloco = new VotoBloco();
		votoBloco.setOrgaoJulgador(orgao);
		votoBloco.setBloco(bloco);
		votoBloco.setOjAcompanhado(orgaoAcompanhado);
		votoBloco.setTipoVoto(bloco.getVotoRelator());
		this.persistAndFlush(votoBloco);
	}
	
	public void registrarVotoAgrupadoOrgaoJulgador(TipoVoto tipoVoto, BlocoJulgamento bloco, OrgaoJulgador orgao, OrgaoJulgador ojAcompanhado) throws Exception {
		if(ojAcompanhado == null) {
			ojAcompanhado = orgao;
		}
		VotoBloco votoBloco = this.recuperarVoto(bloco, orgao);
		if(votoBloco == null) {
			votoBloco = new VotoBloco();
			votoBloco.setOrgaoJulgador(orgao);
			votoBloco.setBloco(bloco);
			votoBloco.setOjAcompanhado(ojAcompanhado);
		}
		votoBloco.setTipoVoto(tipoVoto);
		if(bloco.getOrgaoJulgadorRelator() == orgao) {
				votoBloco.getBloco().setVotoRelator(votoBloco.getTipoVoto());
				bloco.setVotoRelator(tipoVoto);
				ComponentUtil.getBlocoJulgamentoManager().mergeAndFlush(votoBloco.getBloco());
		}
		if(ComponentUtil.getTipoVotoManager().isDivergencia(votoBloco.getTipoVoto())) {
			votoBloco.setOjAcompanhado(ojAcompanhado);
		} else {
			votoBloco.setOjAcompanhado(votoBloco.getBloco().getOrgaoJulgadorRelator());
		}
		ComponentUtil.getVotoBlocoManager().registrarVoto(votoBloco);
	}
}
