package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

@Name("pendenciaSessaoJulgamentoPessoaMagistradoSuggest")
@BypassInterceptors
public class PendenciaSessaoJulgamentoPessoaMagistradoSuggestBean extends
		AbstractSuggestBean<UsuarioLocalizacaoMagistradoServidor> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select pm from PessoaMagistrado pm where pm.idUsuario in( ");
		sb.append("select o.usuarioLocalizacao.usuario.idUsuario from UsuarioLocalizacaoMagistradoServidor o ");
		sb.append("where o.usuarioLocalizacao.papel.identificador like 'magistrado' ");
		sb.append("and  o.orgaoJulgador in ( ");
		sb.append("select scp.orgaoJulgador from SessaoComposicaoOrdem scp )");
		//sb.append("where scp.sessao = #{pendenciaSessaoJulgamentoList.sessao}) ");
		sb.append("and lower(TO_ASCII(o.usuarioLocalizacao.usuario.nome)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%'))) order by pm.nome");
		return sb.toString();
	}

}