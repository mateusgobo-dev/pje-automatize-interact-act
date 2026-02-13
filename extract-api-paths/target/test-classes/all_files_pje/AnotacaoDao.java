package br.com.infox.editor.dao;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.editor.query.AnotacaoQuery;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.editor.Anotacao;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;
import br.jus.pje.nucleo.enums.editor.TipoAnotacao;

@Name(AnotacaoDao.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class AnotacaoDao extends GenericDAO implements AnotacaoQuery {
	
	public static final String NAME = "anotacaoDao";
	
	@SuppressWarnings("unchecked")
	public List<Anotacao> getAnotacoesDoTopico(ProcessoDocumentoEstruturadoTopico topico) {
		return getEntityManager().createQuery(ANOTACOES_TOPICO).setParameter(TOPICO_PARAM, topico).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Anotacao> getAnotacoesDoDocumento(ProcessoDocumentoEstruturado documento) {
		return getEntityManager().createQuery(ANOTACOES_DOCUMENTO).setParameter(DOCUMENTO_PARAM, documento).getResultList();
	}
	
	public boolean orgaoJulgadorPossuiAnotacoesDoTipo(TipoAnotacao tipoAnotacao, ProcessoDocumentoEstruturado documento, OrgaoJulgador orgaoJulgador) {
		return EntityUtil.getSingleResult(getEntityManager().createQuery(ORGAO_JULGADOR_POSSUI_ANOTACOES_DO_TIPO)
				.setParameter(DOCUMENTO_PARAM, documento)
				.setParameter(TIPO_ANOTACAO_PARAM, tipoAnotacao)
				.setParameter(ORGAO_JULGADOR_PARAM, orgaoJulgador)) != null;
	}
}
