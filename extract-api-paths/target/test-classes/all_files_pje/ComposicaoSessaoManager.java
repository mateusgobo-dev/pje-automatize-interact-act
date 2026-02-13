package br.com.jt.pje.manager;

import java.util.Collections;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.jt.pje.dao.ComposicaoSessaoDAO;
import br.jus.pje.jt.entidades.ComposicaoSessao;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ComposicaoSessaoManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class ComposicaoSessaoManager extends GenericManager{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "composicaoSessaoManager";
	
	@In
	private ComposicaoSessaoDAO composicaoSessaoDAO;
	
	public List<OrgaoJulgador> getOrgaoJulgadorBySessao(SessaoJT sessao){
		if(sessao == null){
			return null;
		}
		return composicaoSessaoDAO.getOrgaoJulgadorBySessao(sessao);
	}
	
	public ComposicaoSessao getComposicaoSessao(SessaoJT sessao, OrgaoJulgador orgaoJulgador){
		if(sessao == null || orgaoJulgador == null){
			return null;
		}
		return composicaoSessaoDAO.getComposicaoSessao(sessao, orgaoJulgador);
	}
	
	public List<ComposicaoSessao> composicaoSessaoListBySessao(SessaoJT sessao){
		if(sessao == null){
			return null;
		}
		return composicaoSessaoDAO.composicaoSessaoListBySessao(sessao);
	}
	
	public List<ComposicaoSessao> composicaoSessaoSemComposicaoProcessoBySessaoProcesso(SessaoJT sessao, ProcessoTrf processoTrf){
		if(sessao == null || processoTrf == null){
			return Collections.emptyList();
		}
		return composicaoSessaoDAO.composicaoSessaoSemComposicaoProcessoBySessaoProcesso(sessao, processoTrf);
	}
}