package br.com.infox.editor.action;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.editor.bean.Estilo;
import br.com.infox.editor.manager.CssDocumentoManager;
import br.com.infox.editor.manager.EstruturaDocumentoTopicoMagistradoManager;
import br.com.infox.editor.manager.EstruturaDocumentoTopicoManager;
import br.com.infox.editor.service.NumeracaoDocumentoService;
import br.com.infox.editor.service.ProcessaModeloService;
import br.com.infox.editor.tree.HierarchicalListTree;
import br.com.infox.editor.tree.RichHierarchicalListTree;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.view.GenericCrudAction;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.Variavel;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumento;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumentoTopico;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumentoTopicoMagistrado;


@Name(ModeloMagistradoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ModeloMagistradoAction extends GenericCrudAction<EstruturaDocumento> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "modeloMagistradoAction";

	private HierarchicalListTree<EstruturaDocumentoTopico> estruturaDocumentoTopicoTree = new RichHierarchicalListTree<EstruturaDocumentoTopico>("Tópicos do Documento");
	private PessoaMagistrado magistrado;
	private Variavel variavel;
	private List<Estilo> estilos;

	@In
	private EstruturaDocumentoTopicoManager estruturaDocumentoTopicoManager;
	@In
	private EstruturaDocumentoTopicoMagistradoManager estruturaDocumentoTopicoMagistradoManager;
	@In
	private NumeracaoDocumentoService numeracaoDocumentoService;
	@In
	private PessoaMagistradoManager pessoaMagistradoManager;
	@In
	private ProcessaModeloService processaModeloService;
	@In
	private CssDocumentoManager cssDocumentoManager;

	public HierarchicalListTree<EstruturaDocumentoTopico> getEstruturaDocumentoTopicoTree() {
		return estruturaDocumentoTopicoTree;
	}

	@Override
	public void setId(Integer id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (changed) {
			setIdInstance(id);

			cancelar();
			estruturaDocumentoTopicoTree.clear();
			estruturaDocumentoTopicoTree.setHierarchicalList(estruturaDocumentoTopicoManager.getEstruturaDocumentoTopicoList(getInstance()));
		}
	}

	public void buildEstDocTopMagistradoList() {
		for (EstruturaDocumentoTopico estruturaDocumentoTopico: estruturaDocumentoTopicoTree.getHierarchicalList()) {
			EstruturaDocumentoTopicoMagistrado topicoMagistrado = estruturaDocumentoTopicoMagistradoManager.getEstruturaDocumentoTopicoMagistrado(estruturaDocumentoTopico, magistrado);
			if (topicoMagistrado == null) {
				topicoMagistrado = createEstruturaDocumentoTopicoMagistrado(estruturaDocumentoTopico);
			}
			estruturaDocumentoTopico.setEstruturaDocumentoTopicoMagistrado(topicoMagistrado);
		}
	}

	private EstruturaDocumentoTopicoMagistrado createEstruturaDocumentoTopicoMagistrado(EstruturaDocumentoTopico estruturaDocumentoTopico) {
		EstruturaDocumentoTopicoMagistrado topicoMagistrado = new EstruturaDocumentoTopicoMagistrado();
		topicoMagistrado.setEstruturaDocumentoTopico(estruturaDocumentoTopico);
		topicoMagistrado.setConteudo(estruturaDocumentoTopico.getTopico().getConteudoPadrao());
		topicoMagistrado.setPessoaMagistrado(magistrado);
		return topicoMagistrado;
	}

	public String getNumeracaoFormatada(EstruturaDocumentoTopico edTopico) {
		return numeracaoDocumentoService.getNumeracaoDocumento(edTopico.getNivel(), edTopico.getNumeracao());
	}

	public void gravar() {
		if (estruturaDocumentoTopicoTree.getSelected() == null) {
			FacesMessages.instance().add(Severity.ERROR, "Selecione um tópico.");
			return;
		}
		String erro = validaModelo();
		if (erro != null) {
			FacesMessages.instance().add(Severity.ERROR, "Modelo inválido! " + erro);
			return;
		}

		gravarModelosMagistrado();
	}

	private String validaModelo() {
		for (EstruturaDocumentoTopico estruturaDocumentoTopico: estruturaDocumentoTopicoTree.getHierarchicalList()) {
			EstruturaDocumentoTopicoMagistrado estruturaDocumentoTopicoMagistrado = estruturaDocumentoTopico.getEstruturaDocumentoTopicoMagistrado();
			String erro = processaModeloService.validaModelo(estruturaDocumentoTopicoMagistrado.getConteudo());
			if (erro != null)
				return erro;
		}
		return null;
	}

	private void gravarModelosMagistrado() {
		EstruturaDocumentoTopico estruturaDocumentoTopico = estruturaDocumentoTopicoTree.getSelected();
		EstruturaDocumentoTopicoMagistrado estruturaDocumentoTopicoMagistrado = estruturaDocumentoTopico.getEstruturaDocumentoTopicoMagistrado();

		String conteudoMagistrado = estruturaDocumentoTopicoMagistrado.getConteudo().trim();
		String conteudoPadrao = estruturaDocumentoTopico.getTopico().getConteudoPadrao().trim();
		if (conteudoMagistrado.equals(conteudoPadrao)) {
			if (estruturaDocumentoTopicoMagistrado.getIdEstruturaDocumentoTopicoMagistrado() != 0) {
				estruturaDocumentoTopicoMagistradoManager.remove(estruturaDocumentoTopicoMagistrado);
				estruturaDocumentoTopico.setEstruturaDocumentoTopicoMagistrado(createEstruturaDocumentoTopicoMagistrado(estruturaDocumentoTopico));
			}
		} else {
			if (estruturaDocumentoTopicoMagistrado.getIdEstruturaDocumentoTopicoMagistrado() != 0) {
				estruturaDocumentoTopicoMagistradoManager.update(estruturaDocumentoTopicoMagistrado);
			} else {
				estruturaDocumentoTopicoMagistradoManager.persist(estruturaDocumentoTopicoMagistrado);
			}
		}
	}

	public void utilizarModeloPadrao() {
		EstruturaDocumentoTopico estruturaDocumentoTopico = estruturaDocumentoTopicoTree.getSelected();
		if (estruturaDocumentoTopico != null) {
			genericManager.remove(estruturaDocumentoTopico.getEstruturaDocumentoTopicoMagistrado());
			
			estruturaDocumentoTopico.setEstruturaDocumentoTopicoMagistrado(createEstruturaDocumentoTopicoMagistrado(estruturaDocumentoTopico));
		}
	}

	public void cancelar() {
		magistrado = null;
		estruturaDocumentoTopicoTree.setSelected(null);
	}

	public boolean isPadrao(EstruturaDocumentoTopico estruturaDocumentoTopico) {
		return estruturaDocumentoTopico.getEstruturaDocumentoTopicoMagistrado().getIdEstruturaDocumentoTopicoMagistrado() == 0;
	}

	public List<PessoaMagistrado> getMagistradoItems() {
		OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();
		if (orgaoJulgadorAtual != null)
			return pessoaMagistradoManager.magistradoPorOrgaoJulgador(orgaoJulgadorAtual);
		//Criação da ordenação da lista de magistrado
		List<PessoaMagistrado> resultado = pessoaMagistradoManager.magistradoList();
		Comparator<PessoaMagistrado> comparator = new Comparator<PessoaMagistrado>() {
			@Override
			public int compare(PessoaMagistrado o1, PessoaMagistrado o2) {
				return o1.toString().compareTo(o2.toString());
			}
		};
		Collections.sort(resultado, comparator);
		return resultado;
	}

	public PessoaMagistrado getMagistrado() {
		return magistrado;
	}

	public void setMagistrado(PessoaMagistrado magistrado) {
		this.magistrado = magistrado;
	}

	public Variavel getVariavel() {
		return variavel;
	}

	public void setVariavel(Variavel variavel) {
		this.variavel = variavel;
	}
	
	@WebRemote
	public List<Estilo> getEstilos() {
		if (estilos == null) {
			estilos = cssDocumentoManager.getEstilos();
		}
		return estilos;
	}
	
	/**
	 * [PJEII-6633] Método que encapsula a recuperação do conteúdo, para evitar a exibição de mensagem de "Erro inesperado".
	 * @author fernando.junior (05/04/2013)
	 */
	public String recuperaConteudo() {
		try {
			return getEstruturaDocumentoTopicoTree().getSelected().getEstruturaDocumentoTopicoMagistrado().getConteudo();
		} catch (NullPointerException npe) {
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
