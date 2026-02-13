package br.jus.cnj.pje.nucleo.manager;

import java.util.Collections;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.business.dao.CompetenciaDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.AssociacaoDimensaoPessoalEnum;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.TipoVinculacaoUsuarioEnum;

@Name(CompetenciaManager.NAME)
public class CompetenciaManager extends BaseManager<Competencia> {
	
	public static final String NAME = "competenciaManager";
	
	@In
	private CompetenciaDAO competenciaDAO;
	
	@Logger
	private Log logger;

	@Override
	protected CompetenciaDAO getDAO() {
		return competenciaDAO;
	}

	public Competencia getCompetenciaByProcessoTrf(ProcessoTrf proc) {
		return competenciaDAO.getCompetenciaByProcessoTrf(proc);
	}

	public List<Competencia> getCompetenciasPossiveisUsuario(Jurisdicao jurisdicao, OrgaoJulgador orgaoJulgador, 
			OrgaoJulgadorColegiado orgaoJulgadorColegiado, boolean somenteIncidental){
		
		boolean isUsuarioInterno = Authenticator.isUsuarioInterno();
		TipoVinculacaoUsuarioEnum tipoUsuarioInterno = Authenticator.getTipoUsuarioInternoAtual();
		Integer idOrgaoJulgadorUsuario = Authenticator.getIdOrgaoJulgadorAtual();
		Integer idOrgaoJulgadorColegiadoUsuario = Authenticator.getIdOrgaoJulgadorColegiadoAtual();
		
		return competenciaDAO.getCompetenciasDisponiveis(
				jurisdicao, orgaoJulgador, orgaoJulgadorColegiado, somenteIncidental, 
				isUsuarioInterno, tipoUsuarioInterno, idOrgaoJulgadorUsuario, idOrgaoJulgadorColegiadoUsuario);
	}
	
	public List<Competencia> getCompetenciasPossiveis(Jurisdicao jurisdicao){
		return competenciaDAO.getCompetenciasPorJurisdicao(jurisdicao);
	}
	
	public List<Competencia> getCompetenciasBasicas(ProcessoTrf proc, Jurisdicao jurisdicao){
		List<AssuntoTrf> assuntoTrfList = ProcessoJudicialManager.instance().recuperaAssuntosNaoComplementares(proc);
		if(jurisdicao != null && proc.getClasseJudicial() == null && CollectionUtilsPje.isEmpty(assuntoTrfList)){
			return getCompetenciasPossiveis(jurisdicao);
		}else{
			return competenciaDAO.getCompetenciasBasicas(proc, jurisdicao, assuntoTrfList);
		}
	}
	
	public List<Competencia> competenciaItemsByOrgaoJulgador(OrgaoJulgador oj){
		if(oj == null){
			return Collections.emptyList();
		}
		return competenciaDAO.competenciaItemsByOrgaoJulgador(oj);
	}
	
	public List<Competencia> getCompetenciasPessoaisNecessarias(ProcessoTrf proc, List<Competencia> essenciais){
		if(proc.getPessoaList(ProcessoParteParticipacaoEnum.A).size() == 0 || proc.getPessoaList(ProcessoParteParticipacaoEnum.P).size() == 0){
			return essenciais;
		}else{
			return competenciaDAO.getCompetenciasDimensaoPessoal(proc, essenciais, AssociacaoDimensaoPessoalEnum.A);
		}
	}
	
	public Competencia getCompetencia(Integer idJurisdicao, Integer idClasseJudicial,
			List<AssuntoTrf> assuntos, Integer idCompetencia) throws PJeBusinessException{
		List<Competencia> competenciaList = competenciaDAO.getCompetencia(idJurisdicao, idClasseJudicial, assuntos, idCompetencia);
		if (competenciaList == null || competenciaList.isEmpty() || competenciaList.size() == 0) {
			throw new PJeBusinessException("Não existe competência cadastrada para a classe e assuntos informados.");
		} else {
			if (competenciaList.size() > 1) {
				StringBuilder msg = new StringBuilder();
				msg.append("Há mais de uma competência possível para o processo: ");
				for(int i = 0; i < competenciaList.size(); i++){
					msg.append(String.format("%d %s", competenciaList.get(i).getIdCompetencia(), competenciaList.get(i).getCompetencia()));
					if(i != competenciaList.size() - 1){
						msg.append(", ");
					}
				}
				throw new PJeBusinessException(msg.toString());
			} else {
				return competenciaList.get(0);
			}
		}
	}
	
	public void toggleHabilitacaoClasseAtendimentoPlantao(Competencia competencia, ClasseJudicial classeJudicial) {
		if (isClasseAtendimentoPlantao(competencia,classeJudicial)) {
			competencia.getClasseJudicialAtendimentoPlantaoList().remove(classeJudicial);
		} else {
			competencia.getClasseJudicialAtendimentoPlantaoList().add(classeJudicial);
		}
		competenciaDAO.persist(competencia);
		competenciaDAO.flush();
					
	}	
	
	public boolean isClasseAtendimentoPlantao(Competencia competencia,ClasseJudicial classeJudicial) {
        return competenciaDAO.isClasseAtendimentoPlantao(competencia, classeJudicial);
	}
}
