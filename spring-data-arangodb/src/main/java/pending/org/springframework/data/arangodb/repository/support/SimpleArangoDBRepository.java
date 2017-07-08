package pending.org.springframework.data.arangodb.repository.support;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.arangodb.ArangoDBException;

import lombok.SneakyThrows;
import pending.org.springframework.data.arangodb.core.ArangoDBOperations;
import pending.org.springframework.data.arangodb.core.ArangoDBTemplate;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBPersistentEntity;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBPersistentProperty;
import pending.org.springframework.data.arangodb.core.mapping.Entity;
import pending.org.springframework.data.arangodb.repository.ArangoDBRepository;
import pending.org.springframework.data.arangodb.repository.query.ArangoDBEntityInformation;

/**
 * Arango DB のリポジトリ基底実装です。
 * 
 * @author hs0x01
 *
 * @param <T>
 *            エンティティ
 * @param <ID>
 *            ID
 */
public class SimpleArangoDBRepository<T, ID extends Serializable> implements ArangoDBRepository<T, ID> {

	/**
	 * {@link ArangoDBTemplate} インスタンスです。
	 */
	protected final ArangoDBOperations arangoDBOperations;

	/**
	 * {@link MappingArangoDBEntityInformation} インスタンスです。
	 */
	protected final ArangoDBEntityInformation<T, ID> entityInformation;

	/**
	 * リポジトリを生成します。
	 * 
	 * @param metadata
	 *            {@link MappingArangoDBEntityInformation} インスタンス
	 * @param arangoDBOperations
	 *            {@link ArangoDBTemplate} インスタンス
	 */
	public SimpleArangoDBRepository(final ArangoDBEntityInformation<T, ID> metadata,
			final ArangoDBOperations arangoDBOperations) {

		Assert.notNull(arangoDBOperations);
		Assert.notNull(metadata);

		entityInformation = metadata;
		this.arangoDBOperations = arangoDBOperations;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends T> S save(S entity) {

		Assert.notNull(entity, "Entity must not be null!");

		ID id = entityInformation.getId(entity);

		T one = null;

		if (id != null) {
			one = findOne(id);
		}

		if (one == null) {
			arangoDBOperations.insert(entity);
		} else {
			arangoDBOperations.update(entity);
		}

		return entity;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends T> Iterable<S> save(Iterable<S> entities) {
		
		for (S entity : entities) {
			save(entity);
		}
		
		return entities;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T findOne(ID id) {
		Assert.notNull(id, "ID must not be null!");
		return arangoDBOperations.read(id.toString(), entityInformation.getJavaType());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists(ID id) {

		T one = findOne(id);

		return (one != null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<T> findAll() {
		
		String collectionName = getCollectionName();
		
		Map<String, Object> bindVars = new HashMap<>();
		
		return arangoDBOperations.readByAql(
				"FOR c IN " + collectionName + " RETURN c",
				bindVars,
				entityInformation.getJavaType());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<T> findAll(Iterable<ID> ids) {
		
		if (ids == null || !ids.iterator().hasNext()) {
			throw new IllegalArgumentException("The ids is null or empty.");
		}
		
		String collectionName = getCollectionName();
		Map<String, Object> bindVars = new HashMap<>();
		
		StringBuilder query = new StringBuilder();
		query.append("FOR c IN ");
		query.append(collectionName);
		query.append(" FILTER ");
		
		int i = 0;
		Iterator<ID> idsItr = ids.iterator();
		while (idsItr.hasNext()) {
			ID id = idsItr.next();
			bindVars.put("_key" + i, id);
			query.append(" c._key == @_key").append(i);
			query.append(" ||");
			i++;
		}
		
		query.delete(query.length() - 2, query.length());
		
		query.append(" RETURN c");
		
		return arangoDBOperations.readByAql(
				query.toString(),
				bindVars,
				entityInformation.getJavaType());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long count() {
		
		String collectionName = getCollectionName();
		
		Map<String, Object> bindVars = new HashMap<>();
		
		return arangoDBOperations.countByAql(
				"FOR c IN " + collectionName + " COLLECT WITH COUNT INTO length RETURN length",
				bindVars);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SneakyThrows
	public void delete(ID id) {

		T entity = entityInformation.getJavaType().newInstance();

		ConvertingPropertyAccessor accessor = arangoDBOperations.getPropertyAccessor(entity);

		MappingContext<? extends ArangoDBPersistentEntity<?>, ArangoDBPersistentProperty> mappingContext = arangoDBOperations
				.getMappingContext();

		ArangoDBPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(entity.getClass());

		ArangoDBPersistentProperty idProperty = persistentEntity.getIdProperty();

		accessor.setProperty(idProperty, id);

		delete(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(T entity) {
		Assert.notNull(entity, "The given entity must not be null!");
		
		try {
			arangoDBOperations.delete(entity);
		} catch (ArangoDBException ae) {
			if (!"Response: 404, Error: 1202 - document not found".equals(ae.getMessage())) {
				throw ae;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Iterable<? extends T> entities) {
		
		for (T entity : entities) {
			delete(entity);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteAll() {
		
		String collectionName = getCollectionName();
		
		arangoDBOperations.truncate(collectionName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArangoDBOperations getArangoDBOperations() {
		return arangoDBOperations;
	}
	
	/**
	 * コレクション名を返します。
	 * 
	 * @return コレクション名
	 */
	protected String getCollectionName() {
		
		Class<T> entityClass = entityInformation.getJavaType();
		
		String collectionName = entityClass.getSimpleName();
		Entity entity = entityClass.getAnnotation(Entity.class);
		if (entity != null && !StringUtils.isEmpty(entity.collectionName())) {
			collectionName = entity.collectionName();
		}
		
		return collectionName;
	}
}
