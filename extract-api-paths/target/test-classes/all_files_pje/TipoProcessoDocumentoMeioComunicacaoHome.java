package br.jus.csjt.pje.view.action;

import java.util.Arrays;
import java.util.List;

import br.com.infox.cliente.home.TipoProcessoDocumentoTrfHome;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoMeioComunicacao;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.com.itx.component.AbstractHome;
import br.jus.cnj.pje.business.dao.TipoProcessoDocumentoMeioComunicacaoDAO;

@Name(TipoProcessoDocumentoMeioComunicacaoHome.NAME)
public class TipoProcessoDocumentoMeioComunicacaoHome extends
	AbstractHome<TipoProcessoDocumentoMeioComunicacao> {
	
	public static final String NAME = "tipoProcessoDocumentoMeioComunicacaoHome";
	public static final String MSG02 = "Não foi possível salvar o registro. Já existe, para o tipo de documento  o meio de comunicação selecionado.";

	private static final long serialVersionUID = 1L;
	
	@In(create=true)
	private TipoProcessoDocumentoMeioComunicacaoDAO tipoProcessoDocumentoMeioComunicacaoDAO;

	@Override
	public String update() {
		String ret = null;
		try {
			if (tipoProcessoDocumentoMeioComunicacaoDAO.verificaSeJaTemCadastrado(TipoProcessoDocumentoTrfHome.instance().getInstance(), getInstance().getMeioComunicacao())){
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, MSG02);
			}
			else{			
				getInstance().setTipoProcessoDocumento(TipoProcessoDocumentoTrfHome.instance().getInstance());
				ret = super.persist();
				if (ret != null) {
					newInstance();
				}
			}
		} catch (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Erro ao atualizar!");
		}
		refreshGrid("tipoProcessoDocumentoMeioComunicacaoGrid");
		return ret;
	}
	
	
	@Override
	public String persist() {
		String ret = null;
		try {
			if (tipoProcessoDocumentoMeioComunicacaoDAO.verificaSeJaTemCadastrado(TipoProcessoDocumentoTrfHome.instance().getInstance(), getInstance().getMeioComunicacao())){
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, MSG02);
			}
			else{			
				getInstance().setTipoProcessoDocumento(TipoProcessoDocumentoTrfHome.instance().getInstance());
				ret = super.persist();
				if (ret != null) {
					newInstance();
				}
			}
		} catch (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
		}
		refreshGrid("tipoProcessoDocumentoMeioComunicacaoGrid");
		return ret;
	}

	@Override
	public void newInstance() {
		super.newInstance();
	}

	@Override
	public String remove(TipoProcessoDocumentoMeioComunicacao obj) {
		setInstance(obj);
		refreshGrid("tipoProcessoDocumentoMeioComunicacaoGrid");
		return super.remove(obj);
	}
	
	public List<ExpedicaoExpedienteEnum> meioComunicacaoList(){
		return Arrays.asList(ExpedicaoExpedienteEnum.values());
	}
	/**
	 * Verifica se tem o Meio de Comunicação Diário Eletrônico cadastrado para o TipoProcessoDocumento.
	 * Se tiver a aba de configuração do Diário Eletrônico deve ser exibida
	 * @return true se o TipoProcessoDocumento tiver o Meio de Comunicação Diário Eletrônico cadastrado 
	 */
	public boolean exibeAbaDiarioEletronico() {
		return tipoProcessoDocumentoMeioComunicacaoDAO.verificaSeJaTemCadastrado(TipoProcessoDocumentoTrfHome.instance().getInstance(), ExpedicaoExpedienteEnum.P);
	}
}