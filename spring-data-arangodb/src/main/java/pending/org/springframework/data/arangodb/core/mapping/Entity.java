package pending.org.springframework.data.arangodb.core.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.data.annotation.Persistent;

/**
 * Arango DB に永続化するオブジェクトを識別します。
 * 
 * @author hs0x01
 *
 */
@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Entity {
	
	/**
	 * コレクション名です。
	 */
	String collectionName() default "";
}
