package br.com.infox.pje.action;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.TipoInclusaoDocumentoEnum;

@Name("abaVotoRelatorAction")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AbaVotoRelatorAction extends AbstractInteiroTeorProcesso implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final TipoProcessoDocumento TIPO_PROCESSO_DOCUMENTO_VOTO = ParametroUtil.instance()
			.getTipoProcessoDocumentoVoto();
	private SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto;

	public List<ModeloDocumento> getModeloDocumentoList() {
		return getModeloDocumentoList(TIPO_PROCESSO_DOCUMENTO_VOTO);
	}

	public SessaoProcessoDocumentoVoto getSessaoProcessoDocumentoVoto() {
		if (sessaoProcessoDocumentoVoto == null) {

			sessaoProcessoDocumentoVoto = getSessaoProcessoDocumentoVotoByTipoOj(getProcessoTrf().getOrgaoJulgador());

			// Se não existe nenhum processodocumento na sessão persistido
			// cria-se um novo
			if (sessaoProcessoDocumentoVoto != null
					&& sessaoProcessoDocumentoManager.documentoInclusoAposProcessoJulgado(
							sessaoProcessoDocumentoVoto.getDtVoto(), getProcessoTrf().getProcesso())) {
				return sessaoProcessoDocumentoVoto;
			} else {
				criaNovoVoto();
			}
		}
		return sessaoProcessoDocumentoVoto;
	}
	
	public String getModeloDocumentoVoto(){
		if(sessaoProcessoDocumentoVoto != null && sessaoProcessoDocumentoVoto.getProcessoDocumento() != null){
			return sessaoProcessoDocumentoVoto.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento();
		}else{
			return null;
		}
	}

	public void setModeloDocumentoVoto(String txt){
		sessaoProcessoDocumentoVoto.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(txt);
	}
	
	private void criaNovoVoto() {
		sessaoProcessoDocumentoVoto = new SessaoProcessoDocumentoVoto();
		ProcessoDocumento processoDocumento = new ProcessoDocumento();
		processoDocumento.setProcessoDocumentoBin(new ProcessoDocumentoBin());
		sessaoProcessoDocumentoVoto.setProcessoDocumento(processoDocumento);
		sessaoProcessoDocumentoVoto.getProcessoDocumento().setTipoProcessoDocumento(TIPO_PROCESSO_DOCUMENTO_VOTO);
	}

	public void setSessaoProcessoDocumentoVoto(SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto) {
		this.sessaoProcessoDocumentoVoto = sessaoProcessoDocumentoVoto;
	}

	@Override
	public void update() {
		ProcessoDocumento pd = getSessaoProcessoDocumentoVoto().getProcessoDocumento();
		pd.setUsuarioAlteracao(Authenticator.getUsuarioLogado());
		pd.setNomeUsuarioAlteracao(Authenticator.getUsuarioLogado().getNome());
		pd.setDataAlteracao(new Date());
		pd.setPapel(Authenticator.getPapelAtual());
		pd.setLocalizacao(Authenticator.getLocalizacaoAtual());
		sessaoProcessoDocumentoVotoManager.update(getSessaoProcessoDocumentoVoto());

		FacesMessages.instance().add(Severity.INFO, "Documento atualizado com sucesso!");
	}

	@Override
	public SessaoProcessoDocumentoVoto persist() {
		SessaoProcessoDocumentoVoto voto = sessaoProcessoDocumentoVoto;
		ProcessoDocumentoBinManager pdbm = (ProcessoDocumentoBinManager) Component.getInstance("processoDocumentoBinManager");
		Sessao sessao = getSessao();
		ProcessoTrf processoJudicial = getProcessoTrf();
		OrgaoJulgador oj = Authenticator.getOrgaoJulgadorAtual();
		UsuarioLocalizacao role = Authenticator.getUsuarioLocalizacaoAtual();
		ProcessoDocumento pd = voto.getProcessoDocumento();

		pd.setProcessoDocumento("voto_relator");
		pd.setUsuarioInclusao(role.getUsuario());
		pd.setNomeUsuarioInclusao(role.getUsuario().getNome());
		pd.setDataInclusao(new Date());
		pd.setPapel(role.getPapel());
		pd.setLocalizacao(role.getLocalizacaoFisica());
		pd.setProcesso(processoJudicial.getProcesso());

		voto.setSessao(sessao);
		voto.setProcessoTrf(processoJudicial);
		voto.setOjAcompanhado(oj);
		voto.setOrgaoJulgador(oj);
		voto.setTipoInclusao(TipoInclusaoDocumentoEnum.S);

		String modeloDocumentoBin = voto.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento();
		ProcessoDocumentoBin pdb = pdbm.inserirProcessoDocumentoBin(new Date(), modeloDocumentoBin);

		sessaoProcessoDocumentoVoto.getProcessoDocumento().setProcessoDocumentoBin(pdb);
		try {
			pd = processoDocumentoManager.persist(sessaoProcessoDocumentoVoto.getProcessoDocumento());
			voto.setProcessoDocumento(pd);
			voto = sessaoProcessoDocumentoVotoManager.persistirSessaoEAgregados(getSessao(),
				sessaoProcessoDocumentoVoto, getProcessoTrf(), Authenticator.getUsuarioLocalizacaoAtual(),
				Authenticator.getOrgaoJulgadorAtual());
		}catch(PJeBusinessException e){
			FacesMessages.instance().add(Severity.ERROR, "Houve um erro ao tentar gravar o documento!");
			return voto;
		}
		FacesMessages.instance().add(Severity.INFO, "Documento inserido com sucesso!");
		return voto;
	}

	public List<TipoVoto> getTipoVotoList() {
		if (this.tipoVotoList == null) {
			this.tipoVotoList = this.getTipoVotoManager().listTipoVotoAtivoComRelator();
		}
		return tipoVotoList;
	}

	@Override
	public boolean existeDocumento() {
		return (getSessaoProcessoDocumentoVoto() != null && getSessaoProcessoDocumentoVoto().getSessao() != null);
	}

	@Override
	public SessaoProcessoDocumento getSessaoProcessoDocumento() {
		return getSessaoProcessoDocumentoVoto();
	}

	@Override
	public void setSessaoProcessoDocumento(SessaoProcessoDocumento sessaoProcessoDocumento) {
	}

	@Override
	public TipoVotoManager getTipoVotoManager() {
		return tipoVotoManager;
	}
}
