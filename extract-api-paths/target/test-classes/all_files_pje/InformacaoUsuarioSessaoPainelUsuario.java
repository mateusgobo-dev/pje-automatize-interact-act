package br.jus.cnj.pje.webservice.json;

public class InformacaoUsuarioSessaoPainelUsuario extends InformacaoUsuarioSessao{

    private Boolean podeEditarTags;
    private Boolean podeVisualizarPainelMagistradoSessao = Boolean.FALSE;

    public InformacaoUsuarioSessaoPainelUsuario(InformacaoUsuarioSessao usuario){
        this.setIdOrgaoJulgador(usuario.getIdOrgaoJulgador());
        this.setIdOrgaoJulgadorColegiado(usuario.getIdOrgaoJulgadorColegiado());
        this.setIdOrgaoJulgadorCargo(usuario.getIdOrgaoJulgadorCargo());
        this.setIdUsuario(usuario.getIdUsuario());
        this.setIdsLocalizacoesFisicasFilhas(usuario.getIdsLocalizacoesFisicasFilhas());
        this.setIdLocalizacaoFisica(usuario.getIdLocalizacaoFisica());
        this.setIdLocalizacaoModelo(usuario.getIdLocalizacaoModelo());
        this.setIdPapel(usuario.getIdPapel());
        this.setVisualizaSigiloso(usuario.getVisualizaSigiloso());
        this.setNivelAcessoSigilo(usuario.getNivelAcessoSigilo());
        this.setLogin(usuario.getLogin());
    }

    public Boolean getPodeEditarTags() {
        return podeEditarTags;
    }

    public void setPodeEditarTags(Boolean podeEditarTags) {
        this.podeEditarTags = podeEditarTags;
    }

    public Boolean getPodeVisualizarPainelMagistradoSessao() {
        return podeVisualizarPainelMagistradoSessao;
    }

    public void setPodeVisualizarPainelMagistradoSessao(Boolean podeVisualizarPainelMagistradoSessao) {
        this.podeVisualizarPainelMagistradoSessao = podeVisualizarPainelMagistradoSessao;
    }
}