package br.com.jt.pje.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.pje.bean.SubstitutoProcessoSessaoBean;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.dao.ComposicaoProcessoSessaoDAO;
import br.jus.pje.jt.entidades.ComposicaoProcessoSessao;
import br.jus.pje.jt.entidades.ComposicaoSessao;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ComposicaoProcessoSessaoManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class ComposicaoProcessoSessaoManager extends GenericManager{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "composicaoProcessoSessaoManager";
	
	@In
	ComposicaoProcessoSessaoDAO composicaoProcessoSessaoDAO;
	
	public List<ComposicaoProcessoSessao> getComposicaoProcessoByProcessoSessao(ProcessoTrf processoTrf, SessaoJT sessao){
		if(processoTrf == null || sessao == null){
			return null;
		}
		return composicaoProcessoSessaoDAO.getComposicaoProcessoByProcessoSessao(processoTrf, sessao);
	}
	
	public ComposicaoProcessoSessao getComposicaoProcessoSessao(ProcessoTrf processoTrf, SessaoJT sessao, OrgaoJulgador orgaoJulgador){
		if(processoTrf == null || sessao == null || orgaoJulgador == null){
			return null;
		}
		return composicaoProcessoSessaoDAO.getComposicaoProcessoSessao(processoTrf, sessao, orgaoJulgador);
	}
	
	public boolean existeComposicaoProcessoByProcessoSessao(ProcessoTrf processoTrf, SessaoJT sessao){
		if(processoTrf == null || sessao == null){
			return false;
		}
		return composicaoProcessoSessaoDAO.existeComposicaoProcessoByProcessoSessao(processoTrf, sessao);
	}
	
	public List<ComposicaoProcessoSessao> removerComposicaoProcesso(List<ComposicaoProcessoSessao> listComposicaoProcesso, 
																	ComposicaoProcessoSessao cps,
																	SessaoJT sessao){
		if(listComposicaoProcesso.size() <= sessao.getOrgaoJulgadorColegiado().getMinimoParticipante()){
			throw new NegocioException("Número de participantes é igual ao mínimo");
		}
		listComposicaoProcesso.remove(cps);
		if(cps.getIdComposicaoProcessoSessao() != null){
			cps = EntityUtil.find(ComposicaoProcessoSessao.class, cps.getIdComposicaoProcessoSessao());
			remove(cps);
		}
		return listComposicaoProcesso;
	}
	
	public void atualizarComposicaoProcesso(ComposicaoProcessoSessao presidente, 
											List<ComposicaoProcessoSessao> listComposicaoProcesso,
											SessaoJT sessao){
		
		if(listComposicaoProcesso.size() < sessao.getOrgaoJulgadorColegiado().getMinimoParticipante()){
			throw new NegocioException("Número de participantes menor que o mínimo");
		}
		
		if(presidente != null){
			try {
				listComposicaoProcesso.get(listComposicaoProcesso.indexOf(presidente)).setPresidente(true);
			} catch (ArrayIndexOutOfBoundsException e) {
				presidente = null;
			}
		}
		
		for(ComposicaoProcessoSessao cps: listComposicaoProcesso){
			if(presidente == null || (cps.getPresidente() && !cps.equals(presidente))){
				cps.setPresidente(false);
			}
			update(cps);
		}
	}
	
	public void inserirComposicaoProcesso(PautaSessao pautaSessao, ComposicaoSessao presidente,
											List<ComposicaoSessao> listComposicaoSessao){
		if(listComposicaoSessao.size() < pautaSessao.getSessao().getOrgaoJulgadorColegiado().getMinimoParticipante()){
			throw new NegocioException("Número de participantes menor que o mínimo");
		}
		
		if(presidente != null){
			persist(criarComposicaoProcessoSessao(pautaSessao, presidente, true));
			listComposicaoSessao.remove(presidente);
		}
		
		for(ComposicaoSessao cs: listComposicaoSessao){
			persist(criarComposicaoProcessoSessao(pautaSessao, cs, false));
		}
	}
	
	public void inserirComposicaoProcesso(List<PautaSessao> pautaSessaoList, List<ComposicaoSessao> listComposicaoSessao){
		for(PautaSessao ps: pautaSessaoList){
			for(ComposicaoSessao cs: listComposicaoSessao){
				persist(criarComposicaoProcessoSessao(ps, cs, false));
			}
		}
	}
	
	
	public void inserirComposicaoProcesso(PautaSessao pautaSessao, List<ComposicaoSessao> listComposicaoSessao){
		for(ComposicaoSessao cs: listComposicaoSessao){
			persist(criarComposicaoProcessoSessao(pautaSessao, cs, false));
		}
	}
	
	public ComposicaoProcessoSessao criarComposicaoProcessoSessao(PautaSessao pautaSessao, 
																	ComposicaoSessao composicaoSessao,
																	boolean presidente){
		composicaoSessao = find(ComposicaoSessao.class, composicaoSessao.getIdComposicaoSessao());
		ComposicaoProcessoSessao cps = new ComposicaoProcessoSessao();
		cps.setComposicaoSessao(composicaoSessao);
		cps.setPautaSessao(pautaSessao);
		cps.setPresidente(presidente);
		
		if(pautaSessao.getProcessoTrf().getOrgaoJulgador().equals(composicaoSessao.getOrgaoJulgador())){
			cps.setMagistradoRelator(pautaSessao.getProcessoTrf().getPessoaRelator());
		}

		return cps;
	}
	
	public void alterarSubstitutoProcessoLote(List<PautaSessao> pautaSessaoList, List<SubstitutoProcessoSessaoBean> listBean){
		List<SubstitutoProcessoSessaoBean> beanRemove = new ArrayList<SubstitutoProcessoSessaoBean>(0);
		List<OrgaoJulgador> ojList = new ArrayList<OrgaoJulgador>(0);
		for(SubstitutoProcessoSessaoBean bean: listBean){
			if(bean.getMagistradoSubstituto() == null){
				beanRemove.add(bean);
			}else{
				ojList.add(bean.getOrgaoJulgador());
			}
		}
		
		listBean.removeAll(beanRemove);
		
		for(PautaSessao ps: pautaSessaoList){
			for(ComposicaoProcessoSessao cps: getComposicaoProcessoByProcessoSessaoOJList(ps.getProcessoTrf(), ps.getSessao(), ojList)){
				for(SubstitutoProcessoSessaoBean bean: listBean){
					if(cps.getComposicaoSessao().getOrgaoJulgador().equals(bean.getOrgaoJulgador())
						&& !cps.getPautaSessao().getProcessoTrf().getOrgaoJulgador().equals(bean.getOrgaoJulgador())){
						cps.setMagistradoSubstituto(bean.getMagistradoSubstituto());
						update(cps);
						break;
					}
				}
			}
		}
	}
	
	private List<ComposicaoProcessoSessao> getComposicaoProcessoByProcessoSessaoOJList(ProcessoTrf processoTrf, 
																						SessaoJT sessao, 
																						List<OrgaoJulgador> orgaoJulgadorList){
		if(processoTrf == null || sessao == null || orgaoJulgadorList == null || orgaoJulgadorList.isEmpty()){
			return null;
		}
		return composicaoProcessoSessaoDAO.getComposicaoProcessoByProcessoSessaoOJList(processoTrf, sessao, orgaoJulgadorList);
	}
	
}