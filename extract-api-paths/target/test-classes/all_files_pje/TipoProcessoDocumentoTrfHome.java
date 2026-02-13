package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.csjt.pje.business.manager.TipoMateriaDiarioEletronicoManager;
import br.jus.pje.jt.entidades.TipoMateriaDiarioEletronico;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoTrf;
import br.jus.pje.nucleo.enums.TipoDocumentoEnum;

@Name(TipoProcessoDocumentoTrfHome.NAME)
@BypassInterceptors
public class TipoProcessoDocumentoTrfHome extends AbstractHome<TipoProcessoDocumentoTrf> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "tipoProcessoDocumentoTrfHome";

	public static TipoProcessoDocumentoTrfHome instance() {
		return ComponentUtil.getComponent(TipoProcessoDocumentoTrfHome.NAME);
	}

	@Override
	public void newInstance() {
		refreshGrid("tipoProcessoDocumentoTrfGrid");
		super.clearInstance();
	}
	
	/**
	 * Este método inativa um Tipo de Documento do Processo
	 */
	@Override
	public String inactive(TipoProcessoDocumentoTrf instance) {
		String ret = "";
		if(beforePersistOrUpdate()) {
			setInstance(instance);
			instance.setAtivo(false);
		}
		ret = super.update();
		if (ret != null && ret != "") {
		  	FacesMessages.instance().clear();
		  	FacesMessages.instance().add(Severity.INFO, super.getInactiveSuccess());
		}
		return ret;
	}

	@Override
	public boolean isEditable() {
		return ParametroUtil.instance().getPermitirCadastrosBasicos();
	}
	
	public void setNotificaPartes(Integer notificaPartes) {
		switch (notificaPartes) {
		case 0: // Nem advogado nem parte
			this.instance.setNotificaParte(false);
			this.instance.setNotificaAdvogado(false);
			break;
		case 1: // Só advogados
			this.instance.setNotificaParte(false);
			this.instance.setNotificaAdvogado(true);
			break;
		case 2: // Só partes
			this.instance.setNotificaParte(true);
			this.instance.setNotificaAdvogado(false);
			break;
		}
	}

	public Integer getNotificaPartes() {
		if (this.instance.getNotificaAdvogado() == Boolean.FALSE && this.instance.getNotificaParte() == Boolean.FALSE)
			return 0;
		if (this.instance.getNotificaAdvogado() == Boolean.FALSE && this.instance.getNotificaParte() == Boolean.TRUE)
			return 2;
		if (this.instance.getNotificaAdvogado() == Boolean.TRUE && this.instance.getNotificaParte() == Boolean.FALSE)
			return 1;
		return 0;
	}
	
	public List<String> obtemNumeroDescricao(){
		List<String> listaTipoMateria = new ArrayList<String>();
		try{
			TipoMateriaDiarioEletronicoManager tipoMateriaDiarioEletronicoManager = ComponentUtil.getComponent("tipoMateriaDiarioEletronicoManager");
			List<TipoMateriaDiarioEletronico> tipoMateriaList = tipoMateriaDiarioEletronicoManager.findAll();
			for (TipoMateriaDiarioEletronico tipoMateriaDiarioEletronico : tipoMateriaList){
				listaTipoMateria.add(tipoMateriaDiarioEletronico.getIdTipoMateria() + "-" + tipoMateriaDiarioEletronico.getDescricaoTipoMateria());
			}
		} catch (PJeBusinessException e){
			e.printStackTrace();
		}
		return listaTipoMateria;
	}
	
	public static String completaZeros(long l, int tamanho){
		StringBuilder sb = new StringBuilder();
		String lSrt = Long.toString(l);
		for (int i = 0; i < tamanho - lSrt.length(); i++){
			sb.append('0');
		}
		sb.append(lSrt);
		return sb.toString();
	}
	
	@Override
	protected boolean beforePersistOrUpdate() {
		if(getInstance().getInTipoDocumento() != null) {
			if (!(getInstance().getInTipoDocumento().equals(TipoDocumentoEnum.D) || 
				  getInstance().getInTipoDocumento().equals(TipoDocumentoEnum.T))) {
				getInstance().setTamanhoMaximoPagina(null);
			}
		}
		return super.beforePersistOrUpdate();
	}
}
