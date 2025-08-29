package br.com.itx.util;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jboss.seam.ScopeType;


/**
 * Marca um método getter (Ex: getPropriedade()) para armazenar seu resultado em cache no 
 * {@link #scope()} definido.<br/><br/>
 * 
 * Deve ser usado a EL #{wi:cached('componente.propriedade', context)} para acessar
 * as propriedades, Ex: #{wi:cached('processoHome.modalAberto', eventContext)}. 
 * Ao usar essa EL, somente será executado o getter uma única vez, posteriormente 
 * será buscado do contexto.<br/><br/>
 * 
 * Usar essa annotation tem o mesmo efeito de anotar o método getter com
 * {@code @Factory(value="cached.componente.propriedade", scope=ScopeType.EVENT)} e usar a EL
 * #{empty eventContext.get('cached.componente.propriedade')?cached.componente.propriedade:eventContext.get('cached.componente.propriedade')}
 * para buscar o resultado do getter nas páginas JSFs.<br/><br/>
 * 
 * Essa annotation é varrida pelo {@link CreateCachedFactories} e deve ser anotada em métodos
 * que são usados em atributos JSF "rendered" dentro de estruturas iterativas.
 * 
 * @author David Vieira
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Cached {
   /**
    * {@link ScopeType} que guardará o retorno do método em cache. <br/>Por padrão, é guardado no {@link ScopeType#EVENT}
    */
   ScopeType scope() default ScopeType.EVENT;
}