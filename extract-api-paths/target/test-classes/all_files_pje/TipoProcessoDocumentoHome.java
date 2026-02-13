/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.ibpm.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.TipoComunicacaoEnum;
import br.jus.pje.nucleo.enums.TipoDocumentoEnum;
import br.jus.pje.nucleo.enums.TipoExpedienteEnum;
import br.jus.pje.nucleo.enums.VisibilidadeEnum;

@Name(TipoProcessoDocumentoHome.NAME)
@BypassInterceptors
public class TipoProcessoDocumentoHome extends AbstractTipoProcessoDocumentoHome<TipoProcessoDocumento> {

	public static final String NAME = "tipoProcessoDocumentoHome";
	private static final long serialVersionUID = 1L;

	public static TipoProcessoDocumentoHome instance() {
		return ComponentUtil.getComponent("tipoProcessoDocumentoHome");
	}

	// PJE-JT:Desenvolvedor Haroldo Arouca :PJE-439 2011-09-01:Alteracoes feitas
	// pela JT
	@Override
	protected boolean beforePersistOrUpdate() {
		if (!this.instance.getInTipoDocumento().equals(TipoDocumentoEnum.E)) {
			this.instance.setInTipoComunicacao(null);
			this.instance.setInTipoExpediente(null);
		}
		return super.beforePersistOrUpdate();
	}

	// PJE-JT:Fim

	@Override
	public String persist() {
		String ret = null;
		try {
			ret = super.persist();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	@Override
	public String remove(TipoProcessoDocumento obj) {
		obj.setAtivo(Boolean.FALSE);
		return super.remove(obj);
	}

	public TipoDocumentoEnum[] getTipoDocumentoEnumValues() {
		return TipoDocumentoEnum.values();
	}

	public TipoComunicacaoEnum[] getTipoComunicacaoEnumValues() {
		return TipoComunicacaoEnum.values();
	}

	public TipoExpedienteEnum[] getTipoExpedienteEnumValues() {
		return TipoExpedienteEnum.values();
	}

	public VisibilidadeEnum[] getVisibilidadeEnumValues() {
		return VisibilidadeEnum.values();
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

}
