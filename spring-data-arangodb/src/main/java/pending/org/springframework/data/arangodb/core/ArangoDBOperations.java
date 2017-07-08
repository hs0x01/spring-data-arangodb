package pending.org.springframework.data.arangodb.core;

import java.util.List;
import java.util.Map;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;

import pending.org.springframework.data.arangodb.core.convert.ArangoDBConverter;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBPersistentEntity;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBPersistentProperty;

/**
 * Arango DB データソースの共通オペレーションを定義します。
 * 
 * @author hs0x01
 *
 */
public interface ArangoDBOperations {
	
	/**
	 * {@code key} によりドキュメントを取得し、エンティティで返します。
	 * 
	 * @param key ドキュメントを特定するキー
	 * @param entityClass エンティティクラス
	 * @return エンティティ
	 */
	<R> R read(String key, Class<R> entityClass);
	
	/**
	 * AQL によりエンティティを返します。
	 * 
	 * @param aql AQL
	 * @param bindVars バインド変数
	 * @param entityClass エンティティクラス
	 * @return エンティティのリスト
	 */
	<R> List<R> readByAql(String aql, Map<String, Object> bindVars, Class<R> entityClass);
	
	/**
	 * AQL によりエンティティをカウントします。
	 * 
	 * @param aql AQL
	 * @param bindVars バインド変数
	 * @return エンティティのカウント
	 */
	long countByAql(String aql, Map<String, Object> bindVars);
	
	/**
	 * AQL によりエンティティを更新します。
	 * 
	 * @param aql AQL
	 * @param bindVars バインド変数
	 */
	void updateByAql(String aql, Map<String, Object> bindVars);
	
	/**
	 * エンティティからドキュメントを作成します。
	 * 
	 * @param entity エンティティ
	 */
	void insert(Object entity);
	
	/**
	 * エンティティからドキュメントを更新します。
	 * 
	 * @param entity エンティティ
	 */
	void update(Object entity);
	
	/**
	 * エンティティからドキュメントを削除します。
	 * 
	 * @param entity エンティティ
	 */
	void delete(Object entity);
	
	/**
	 * コレクションを全件削除します。
	 * 
	 * @param collectionName コレクション名
	 */
	void truncate(String collectionName);
	
	/**
	 * {@link ArangoDBConverter} を返します。
	 * 
	 * @return {@link ArangoDBConverter}
	 */
	ArangoDBConverter getConverter();
	
	/**
	 * {@link ConvertingPropertyAccessor} を返します。
	 * 
	 * @param entity エンティティ
	 * @return {@link ConvertingPropertyAccessor}
	 */
	ConvertingPropertyAccessor getPropertyAccessor(Object entity);
	
	/**
	 * {@link MappingContext} を返します。
	 * 
	 * @return {@link MappingContext}
	 */
	MappingContext<? extends ArangoDBPersistentEntity<?>, ArangoDBPersistentProperty> getMappingContext();
}
