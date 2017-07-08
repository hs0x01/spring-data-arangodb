package pending.org.springframework.data.arangodb.core.convert;

import org.springframework.data.convert.EntityConverter;

import com.arangodb.entity.BaseDocument;

import pending.org.springframework.data.arangodb.core.mapping.ArangoDBPersistentEntity;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBPersistentProperty;

/**
 * 変換される型を識別するコンバータのマーカーインタフェースです。
 * 
 * @author hs0x01
 *
 */
public interface ArangoDBConverter
		extends EntityConverter<ArangoDBPersistentEntity<?>, ArangoDBPersistentProperty, Object, BaseDocument> {
}
