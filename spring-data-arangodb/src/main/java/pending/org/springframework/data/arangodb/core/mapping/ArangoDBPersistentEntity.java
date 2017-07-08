package pending.org.springframework.data.arangodb.core.mapping;

import org.springframework.data.mapping.PersistentEntity;

/**
 * 0以上のプロパティを含む永続化されるエンティティを表します。
 * 
 * @author hs0x01
 *
 * @param <T> エンティティ
 */
public interface ArangoDBPersistentEntity<T> extends PersistentEntity<T, ArangoDBPersistentProperty> {
}
