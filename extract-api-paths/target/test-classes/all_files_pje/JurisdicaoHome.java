package br.com.infox.cliente.home;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.jus.cnj.pje.nucleo.manager.AplicacaoClasseManager;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;

/**
 * Classe para operações com "Jurisdição"
 * 
 */
@Name("jurisdicaoHome")
public class JurisdicaoHome extends AbstractJurisdicaoHome<Jurisdicao> {

	private static final long serialVersionUID = 1L;

	private String numeroOrigemStr;
	
	private Estado estadoDeSelecaoJurisdicao;

	private OrgaoJulgador orgaoJulgadorPlantao;

	private OrgaoJulgadorColegiado orgaoJulgadorColegiadoPlantao;

	@In
	private AplicacaoClasseManager aplicacaoClasseManager;
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private Map<String, String> messages;

	@Override
	public String remove(Jurisdicao obj) {
		obj.setAtivo(Boolean.FALSE);
		setInstance(obj);
		String ret = super.update();
		return ret;
	}

	public String getNumeroOrigemStr() {
		Integer numeroOrigem = getInstance().getNumeroOrigem();
		String numero = "";
		if (numeroOrigem != null) {
			numero = NumeroProcessoUtil.completaZeros(numeroOrigem, 4);
		}
		return numero;
	}

	public void setNumeroOrigemStr(String numeroOrigemStr) {
		getInstance().setNumeroOrigem(Integer.parseInt(numeroOrigemStr));
		this.numeroOrigemStr = numeroOrigemStr;
	}
	
	public List<AplicacaoClasse> getAplicacaoItems(){
		List<AplicacaoClasse> list = null;
		try {
			list = aplicacaoClasseManager.findAll();
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, messages.get("jurisdicao.recuperaAplicacao.error"));
		}
		if(list == null || list.size() == 0){
			facesMessages.add(Severity.ERROR, messages.get("jurisdicao.recuperaAplicacao.vazio"));
			return Collections.emptyList();
		}
		return list;
	}

	public Estado getEstadoDeSelecaoJurisdicao() {
		if (estadoDeSelecaoJurisdicao == null){
			estadoDeSelecaoJurisdicao = this.getInstance().getEstado();
		}
		return estadoDeSelecaoJurisdicao;
	}

	public void setEstadoDeSelecaoJurisdicao(Estado estadoDeSelecaoJurisdicao) {
		this.estadoDeSelecaoJurisdicao = estadoDeSelecaoJurisdicao;
	}

	public OrgaoJulgador getOrgaoJulgadorPlantao() {
		if (orgaoJulgadorPlantao == null){
			orgaoJulgadorPlantao = this.getInstance().getOrgaoJulgadorPlantao();
		}
		return orgaoJulgadorPlantao;
	}

	public void setOrgaoJulgadorPlantao(OrgaoJulgador orgaoJulgadorPlantao) {
		this.orgaoJulgadorPlantao = orgaoJulgadorPlantao;
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiadoPlantao() {
		if (orgaoJulgadorColegiadoPlantao == null){
			orgaoJulgadorColegiadoPlantao = this.getInstance().getOrgaoJulgadorColegiadoPlantao();
		}
		return orgaoJulgadorColegiadoPlantao;
	}

	public void setOrgaoJulgadorColegiadoPlantao(OrgaoJulgadorColegiado orgaoJulgadorColegiadoPlantao) {
		this.orgaoJulgadorColegiadoPlantao = orgaoJulgadorColegiadoPlantao;
	}
}