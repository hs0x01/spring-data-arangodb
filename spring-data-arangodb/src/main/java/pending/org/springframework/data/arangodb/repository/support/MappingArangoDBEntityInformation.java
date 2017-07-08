package pending.org.springframework.data.arangodb.repository.support;

import java.io.Serializable;

import org.springframework.data.repository.core.support.PersistentEntityInformation;

import pending.org.springframework.data.arangodb.core.mapping.ArangoDBPersistentEntity;
import pending.org.springframework.data.arangodb.repository.query.ArangoDBEntityInformation;

/**
 * エンティティ情報のコンテナです。
 * 
 * @author hs0x01
 *
 * @param <T> エンティティ
 * @param <ID> ID
 */
public class MappingArangoDBEntityInformation<T, ID extends Serializable> extends PersistentEntityInformation<T, ID>
		implements ArangoDBEntityInformation<T, ID> {

	/**
	 * エンティティ情報コンテナを生成します。
	 * 
	 * @param entity エンティティ
	 */
	public MappingArangoDBEntityInformation(final ArangoDBPersistentEntity<T> entity) {
		super(entity);
	}
}
