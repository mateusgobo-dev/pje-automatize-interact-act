package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.QuadroAviso;
import br.jus.pje.nucleo.entidades.QuadroAvisoPapel;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name("quadroAvisoPapelHome")
@BypassInterceptors
public class QuadroAvisoPapelHome extends AbstractHome<QuadroAviso> {

	private static final long serialVersionUID = 1L;
	private int idPapel;
	private List<QuadroAvisoPapel> listaQuadroAvisoPapel;
	private List<String> papeisDisponiveis;
	private Map<String, Papel> mapPapeisDisponiveis;
	private List<String> papeis;
	private QuadroAviso quadroAviso;

	public static QuadroAvisoPapelHome instance() {
		return ComponentUtil.getComponent("quadroAvisoPapelHome");
	}

	@Override
	public void newInstance() {
		super.newInstance();
		listaQuadroAvisoPapel = null;
		papeis = null;
		papeisDisponiveis = null;
		mapPapeisDisponiveis = new HashMap<>();
		getListaQuadroAvisoPapel();
		getPapeisDisponiveis();
	}

	public int getIdPapel() {
		return idPapel;
	}

	public void setIdPapel(int idPapel) {
		this.idPapel = idPapel;
	}
	
	@SuppressWarnings("unchecked")
	public List<QuadroAvisoPapel> getListaQuadroAvisoPapel() {
		if (listaQuadroAvisoPapel == null) {
			String s = "select o from QuadroAvisoPapel o where o.quadroAviso =  #{quadroAvisoHome.instance} order by o.papel.nome";
			listaQuadroAvisoPapel = getEntityManager().createQuery(s).getResultList();
		}
		return listaQuadroAvisoPapel;
	}

	public void setListaQuadroAvisoPapel(List<QuadroAvisoPapel> listaQuadroAvisoPapel) {
		this.listaQuadroAvisoPapel = listaQuadroAvisoPapel;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getPapeisDisponiveis() {
		if (papeisDisponiveis == null) {
			papeisDisponiveis = new ArrayList<>();
			String s = "select o from Papel o where identificador not like '/%' order by nome asc";
			List<Papel> listaPapeis = getEntityManager().createQuery(s).getResultList();
			for (Papel papel : listaPapeis) {
				mapPapeisDisponiveis.put(papel.getIdentificador(), papel);
				papeisDisponiveis.add(papel.getIdentificador());
			}
		}
		return papeisDisponiveis;
	}

	public void setPapeisDisponiveis(List<String> papeisDisponiveis) {
		this.papeisDisponiveis = papeisDisponiveis;
	}
	
	public List<String> getPapeis() {
		if (papeis == null) {
			if(listaQuadroAvisoPapel != null) {
				papeis = new ArrayList<>();
				for (QuadroAvisoPapel quadroAvisoPapel : listaQuadroAvisoPapel) {
					papeis.add(quadroAvisoPapel.getPapel().getIdentificador());
				}
			}
		}
		return papeis;
	}

	public void setPapeis(List<String> papeis) {
		this.papeis = papeis;
	}
	
	public Map<String, Papel> getMapPapeisDisponiveis() {
		return mapPapeisDisponiveis;
	}

	public void setMapPapeisDisponiveis(Map<String, Papel> mapPapeisDisponiveis) {
		this.mapPapeisDisponiveis = mapPapeisDisponiveis;
	}
	
	public void salvarPapeisVinculados() {
		this.update();
		newInstance();
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		quadroAviso = QuadroAvisoHome.instance().getInstance();
		List<QuadroAvisoPapel> quadroAvisoPapelListAux = new ArrayList<>();
		for (String identificadorPapel : papeis) {
			Boolean achouPapelAssociado = false;
			for (QuadroAvisoPapel quadroAvisoPapel : quadroAviso.getQuadroAvisoPapelList()) {
				if(quadroAvisoPapel.getPapel().getIdentificador().equals(identificadorPapel)) {
					quadroAvisoPapelListAux.add(quadroAvisoPapel);
					achouPapelAssociado = true;
					break;
				}
			}
			if(achouPapelAssociado == false) {
				QuadroAvisoPapel quadroAvisoPapel = new QuadroAvisoPapel();
				Papel papel = mapPapeisDisponiveis.get(identificadorPapel);
				quadroAvisoPapel.setPapel(papel);
				quadroAvisoPapel.setQuadroAviso(quadroAviso);
				quadroAvisoPapelListAux.add(quadroAvisoPapel);
			}
		}
		quadroAviso.getQuadroAvisoPapelList().clear();
		quadroAviso.getQuadroAvisoPapelList().addAll(quadroAvisoPapelListAux);
		setInstance(quadroAviso);
		return super.beforePersistOrUpdate();
	}

	@Override
	public void setId(Object id) {
		super.setId(id);
	}

}