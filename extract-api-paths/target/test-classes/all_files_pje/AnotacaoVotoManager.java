package br.com.jt.pje.manager;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.jt.pje.dao.AnotacaoVotoDAO;
import br.jus.pje.jt.entidades.AnotacaoVoto;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(AnotacaoVotoManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class AnotacaoVotoManager extends GenericManager{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "anotacaoVotoManager";
	
	@In
	private AnotacaoVotoDAO anotacaoVotoDAO;
	
	public AnotacaoVoto getAnotacaoVotoSemSessaoByProcessoOrgaoJulgadorEColegiado(ProcessoTrf processoTrf, 
																		 OrgaoJulgador orgaoJulgador,
																		 OrgaoJulgadorColegiado orgaoJulgadorColegiado){
		if(processoTrf == null || orgaoJulgador == null || orgaoJulgadorColegiado == null){
			return null;
		}
		return anotacaoVotoDAO.getAnotacaoVotoSemSessaoByProcessoOrgaoJulgadorEColegiado(processoTrf, orgaoJulgador, orgaoJulgadorColegiado);
	}
	
	public List<AnotacaoVoto> getAnotacoesSemSessaoByProcesso(ProcessoTrf processoTrf){
		if(processoTrf == null){
			return null;
		}
		return anotacaoVotoDAO.getAnotacoesSemSessaoByProcesso(processoTrf);
	}
	
	public List<AnotacaoVoto> getAnotacoesBySessaoProcesso(SessaoJT sessao, ProcessoTrf processoTrf){
		if(processoTrf == null || sessao == null){
			return null;
		}
		return anotacaoVotoDAO.getAnotacoesBySessaoProcesso(sessao, processoTrf);
	}
	
	public AnotacaoVoto getAnotacaoVotoByProcessoSessaoOrgaoJulgadorEColegiado(ProcessoTrf processoTrf, SessaoJT sessao, 
																				OrgaoJulgador orgaoJulgador,
																				OrgaoJulgadorColegiado orgaoJulgadorColegiado){
		if(processoTrf == null || sessao == null || orgaoJulgador == null || orgaoJulgadorColegiado == null){
			return null;
		}
		return anotacaoVotoDAO.getAnotacaoVotoByProcessoSessaoOrgaoJulgadorEColegiado(processoTrf, sessao, orgaoJulgador, orgaoJulgadorColegiado);
	}
	
	public AnotacaoVoto getAnotacaoVotoSemOJByProcessoSessaoEColegiado(ProcessoTrf processoTrf, SessaoJT sessao, 
																	OrgaoJulgadorColegiado orgaoJulgadorColegiado){
		if(processoTrf == null || sessao == null || orgaoJulgadorColegiado == null){
			return null;
		}
		return anotacaoVotoDAO.getAnotacaoVotoSemOJByProcessoSessaoEColegiado(processoTrf, sessao, orgaoJulgadorColegiado);
	}
	
	public List<AnotacaoVoto> getAnotacoesVotoByProcessoEColegiadoExcluindoSessaoAtual(ProcessoTrf processoTrf, OrgaoJulgadorColegiado orgaoJulgadorColegiado, SessaoJT sessaoAtual){
		if (processoTrf == null || orgaoJulgadorColegiado == null){
			return null;
		}
		return anotacaoVotoDAO.getAnotacoesVotoByProcessoEColegiadoExcluindoSessaoAtual(processoTrf, orgaoJulgadorColegiado, sessaoAtual);
	}

	public List<AnotacaoVoto> getAnotacoesVotoByProcessoSessaoOrgaoJulgadorEColegiado(ProcessoTrf processoTrf, SessaoJT sessao,
			OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado){
		if(processoTrf == null || sessao == null || orgaoJulgador == null || orgaoJulgadorColegiado == null){
			return null;
		}
		return anotacaoVotoDAO.getAnotacoesVotoByProcessoSessaoOrgaoJulgadorEColegiadoExcluindoSessaoAtual(processoTrf, orgaoJulgadorColegiado, orgaoJulgador, sessao);
	}

	public List<AnotacaoVoto> getAnotacoesVotoSemSessaoByProcessoOrgaoJulgadorEColegiado(ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgadorAtual,
			OrgaoJulgadorColegiado orgaoJulgadorColegiadoAtual){
		if(processoTrf == null || orgaoJulgadorAtual == null || orgaoJulgadorColegiadoAtual == null){
			return null;
		}
		return anotacaoVotoDAO.getAnotacoesVotoSemSessaoByProcessoOrgaoJulgadorEColegiado(processoTrf, orgaoJulgadorAtual,
				orgaoJulgadorColegiadoAtual);
	}

	public List<AnotacaoVoto> getAnotacaoVotoSemSessaoByProcessoEOrgaoJulgador(ProcessoTrf processoTrf, SessaoJT sessao, OrgaoJulgador orgaoJulgador){
		if(processoTrf == null || orgaoJulgador == null || sessao == null){
			return null;
		}
		return anotacaoVotoDAO.getAnotacaoVotoSemSessaoByProcessoEOrgaoJulgador(processoTrf, sessao,
				orgaoJulgador);
	}

}
