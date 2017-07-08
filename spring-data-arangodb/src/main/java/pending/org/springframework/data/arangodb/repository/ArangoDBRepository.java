package pending.org.springframework.data.arangodb.repository;

import java.io.Serializable;

import org.springframework.data.repository.CrudRepository;

import pending.org.springframework.data.arangodb.core.ArangoDBOperations;

/**
 * Arango DB の {@link org.springframework.data.repository.Repository} インタフェースです。
 * 
 * @author hs0x01
 *
 * @param <T> エンティティ
 * @param <ID> ID
 */
public interface ArangoDBRepository<T, ID extends Serializable> extends CrudRepository<T, ID> {

	/**
	 * {@link ArangoDBOperations} の実装インスタンスを返します。
	 * 
	 * @return {@link ArangoDBOperations} の実装インスタンス
	 */
	ArangoDBOperations getArangoDBOperations();
}
