/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informao Ltda.

 Este programa  software livre; voc pode redistribu-lo e/ou modific-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; verso 2 da Licena.
 Este programa  distribudo na expectativa de que seja til, porm, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implcita de COMERCIABILIDADE OU 
 ADEQUAO A UMA FINALIDADE ESPECFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Voc deve ter recebido uma cpia da GNU GPL junto com este programa; se no, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.cliente.component.tree;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;

import br.com.infox.component.tree.EntityNode;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.CentralMandadoLocalizacao;
import br.jus.pje.nucleo.entidades.Localizacao;

@Name("localizacaoCMTree")
@BypassInterceptors
public class LocalizacaoCentralMandadoTreeHandler extends AbstractTreeHandlerCachedRoots<Localizacao> {

	private static final long serialVersionUID = 1L;

		/*
		 * [PJEII-4869] PJE-JT: Ronny Paterson : PJE-1.4.7
		 * Incluso de verificao que traz somente as localizaes vinculadas  estrutura
	 *  qual pertence o papel do usurio logado ou  central de mandados do usurio logado,
	 * caso este esteja vinculado a uma.
		 */
	@Override
	protected String getQueryRoots() {
		Localizacao localizacao = getLocalizacaoAtual();
		CentralMandadoLocalizacao centralMandadoLocalizacaoUsuarioLogado = null;
		CentralMandado centralMandadoUsuarioLogado = null;
		boolean usuarioVinculadoCentral = false;
		StringBuilder sb = new StringBuilder();
		if (localizacao != null) {
			centralMandadoLocalizacaoUsuarioLogado = getCentralMandadoLocalizacaoAtual(localizacao);
			if(centralMandadoLocalizacaoUsuarioLogado != null) {
				centralMandadoUsuarioLogado = centralMandadoLocalizacaoUsuarioLogado.getCentralMandado();				
				if(centralMandadoUsuarioLogado != null) {
					usuarioVinculadoCentral = true;
				}
			}

			if (usuarioVinculadoCentral) {
			
				sb.append("select cml.localizacao ");
				sb.append("from CentralMandadoLocalizacao cml "); 
				sb.append("where cml.centralMandado.idCentralMandado = ");
				sb.append(centralMandadoUsuarioLogado.getIdCentralMandado());
				sb.append(" and cml.localizacao.ativo=true");
				sb.append(" order by cml.localizacao");
			} else {
				String localizacaoListaId = (String)Contexts.getSessionContext().get(Authenticator.ID_LOCALIZACOES_FILHAS_ATUAIS);
				if(localizacaoListaId.trim().isEmpty()) {
					localizacaoListaId = "0";
				}

				sb.append("select distinct l from CentralMandadoLocalizacao cml, Localizacao l");			
				sb.append(" where cml.localizacao.idLocalizacao in ("+localizacaoListaId+")");
				sb.append(" and l.idLocalizacao = cml.localizacao.idLocalizacao");
				sb.append(" and l.ativo=true");
				sb.append(" order by l.localizacao");			
			}
		}
		
		return sb.toString();
	}

	@Override
	protected String getQueryChildren() {
		return "select l from Localizacao l where l.localizacaoPai = :" + EntityNode.PARENT_NODE;
	}
	
	@Override
	protected List<Localizacao> getChildren(Localizacao entity){
		return entity.getLocalizacaoList();
	}

	@Override
	protected String getEventSelected() {
		return "evtSelectLocalizacao";
	}

	@Override
	protected Localizacao getEntityToIgnore() {
		return ComponentUtil.getInstance("localizacaoHome");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void selectListener(NodeSelectedEvent ev) {
		HtmlTree tree = (HtmlTree) ev.getSource();
		treeId = tree.getId();
		EntityNode<Localizacao> en = (EntityNode<Localizacao>) tree.getData();
		if (en != null)
			setSelected(en.getEntity());
		Events.instance().raiseEvent("evtSelectLocalizacaoEstrutura", getSelected(), getEstrutura(en));
	}
	
	private Localizacao getEstrutura(EntityNode<Localizacao> en) {
		EntityNode<Localizacao> parent = en.getParent();
		while (parent != null) {
			if (parent.getEntity().getEstruturaFilho() != null) {
				return parent.getEntity();
			}
			parent = parent.getParent();
		}
		return null;
	}
		
	private Localizacao getLocalizacaoAtual() {
		return Authenticator.getLocalizacaoAtual();
	}
	
	private CentralMandadoLocalizacao getCentralMandadoLocalizacaoAtual(Localizacao localizacao) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(CentralMandadoLocalizacao.class);
		criteria.add(Restrictions.eq("localizacao", localizacao));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		CentralMandadoLocalizacao centralMandadoLocalizacao = (CentralMandadoLocalizacao) criteria.uniqueResult();
		return centralMandadoLocalizacao;
	}
}
