package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Localizacao;

@Name("localizacaoSemVerificarAutenticacaoSuggestBean")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class LocalizacaoSemVerificarAutenticacaoSuggestBean extends AbstractSuggestBean<Localizacao> {	

    private static final long serialVersionUID = 4376637547740125114L;

    @Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT localizacao FROM Localizacao localizacao ");
		sb.append("WHERE lower(localizacao.localizacao) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) ");
		sb.append("AND localizacao.ativo = true ");		        
		if (isPapelPossuiVinculoAdvogado()) {
			sb.append("AND (localizacao = #{authenticator.getLocalizacaoAtual()} ");
			sb.append("OR localizacao IN( ");
				sb.append("SELECT localizacaoPai FROM Localizacao localizacaoPai ");
				sb.append("WHERE localizacaoPai.localizacaoPai = #{authenticator.getLocalizacaoAtual()}) ");
			sb.append(") ");
		}else{
			sb.append("AND localizacao.localizacaoPai IS NOT NULL ");			
		}
		sb.append("ORDER BY localizacao.localizacao");
		return sb.toString();
	}

    /**
     * Verfica se o papel atual esta vinculado ao papel de advogado.
     * 
     * @return True se estiver vinculo com advogado.
     */
	private boolean isPapelPossuiVinculoAdvogado() {
		return Authenticator.isAdvogado() || Authenticator.isAssistenteAdvogado();  
	}
}