package pending.org.springframework.data.arangodb.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.util.StringUtils;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;

import lombok.SneakyThrows;
import pending.org.springframework.data.arangodb.core.convert.ArangoDBConverter;
import pending.org.springframework.data.arangodb.core.convert.MappingArangoDBConverter;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBMappingContext;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBPersistentEntity;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBPersistentProperty;
import pending.org.springframework.data.arangodb.core.mapping.Entity;

/**
 * {@link ArangoDBOperations} の実装です。
 * 
 * @author hs0x01
 *
 */
public class ArangoDBTemplate implements ArangoDBOperations, ApplicationEventPublisherAware {

	/**
	 * {@link Iterable} クラスの定義です。
	 */
	private static final Collection<String> ITERABLE_CLASSES;

	/**
	 * {@link ArangoDBClient} のインスタンスです。
	 */
	private final ArangoDBClient arangoDBClient;

	/**
	 * {@link MappingArangoDBConverter} のインスタンスです。
	 */
	private ArangoDBConverter converter;

	/**
	 * {@link ArangoDBMappingContext} のインスタンスです。
	 */
	protected final MappingContext<? extends ArangoDBPersistentEntity<?>, ArangoDBPersistentProperty> mappingContext;

	/**
	 * static 初期化子です。
	 * 
	 * <p>定数を初期化します。</p>
	 */
	static {
		final Set<String> iterableClasses = new HashSet<String>();
		iterableClasses.add(List.class.getName());
		iterableClasses.add(Collection.class.getName());
		iterableClasses.add(Iterator.class.getName());
		ITERABLE_CLASSES = Collections.unmodifiableCollection(iterableClasses);
	}

	/**
	 * {@link ArangoDBTemplate} を生成します。
	 * 
	 * @param arangoDBClient {@link ArangoDBClient} インスタンス
	 * @param converter {@link ArangoDBConverter}
	 */
	public ArangoDBTemplate(final ArangoDBClient arangoDBClient, ArangoDBConverter converter) {
		this.arangoDBClient = arangoDBClient;
		this.converter = converter == null ? getDefaultConverter() : converter;
		this.mappingContext = this.converter.getMappingContext();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R read(String key, Class<R> entityClass) {

		ArangoCollection arangoCollection = getArangoCollection(entityClass);
		
		BaseDocument baseDocument = arangoCollection.getDocument(key, BaseDocument.class);
		
		if (baseDocument == null) {
			return null;
		}
		
		R entity = converter.read(entityClass, baseDocument);
		
		setSpecialProperties(entity, baseDocument);
		
		return entity;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@SneakyThrows
	public <R> List<R> readByAql(String aql, Map<String, Object> bindVars, Class<R> entityClass) {
		
		ArangoDB arangoDB = arangoDBClient.getArangoDB();
		String dbName = arangoDBClient.getDbName();
		
		List<R> list = new ArrayList<>();
		
		ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(aql, bindVars, null, BaseDocument.class);
			
		while (cursor.hasNext()) {
			
			BaseDocument document = cursor.next();
			
			R entity = converter.read(entityClass, document);
			
			setSpecialProperties(entity, document);
			
			list.add(entity);
		}
		
		return list;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@SneakyThrows
	public long countByAql(String aql, Map<String, Object> bindVars) {
		
		ArangoDB arangoDB = arangoDBClient.getArangoDB();
		String dbName = arangoDBClient.getDbName();
		
		ArangoCursor<Long> cursor = arangoDB.db(dbName).query(aql, bindVars, null, Long.class);
		long count = cursor.next();
		
		return count;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@SneakyThrows
	public void updateByAql(String aql, Map<String, Object> bindVars) {
		
		ArangoDB arangoDB = arangoDBClient.getArangoDB();
		String dbName = arangoDBClient.getDbName();
		
		arangoDB.db(dbName).query(aql, bindVars, null, Void.class);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void truncate(String collectionName) {
		
		ArangoDB arangoDB = arangoDBClient.getArangoDB();
		String dbName = arangoDBClient.getDbName();
		
		arangoDB.db(dbName).collection(collectionName).truncate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void insert(Object entity) {
		
		ensureNotIterable(entity);

		BaseDocument document = new BaseDocument();
		converter.write(entity, document);

		ArangoCollection arangoCollection = getArangoCollection(entity.getClass());
		
		arangoCollection.insertDocument(document);
		
		setSpecialProperties(entity, document);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(Object entity) {
		
		ensureNotIterable(entity);

		BaseDocument document = new BaseDocument();
		converter.write(entity, document);

		ArangoCollection arangoCollection = getArangoCollection(entity.getClass());
		
		if (StringUtils.isEmpty(document.getKey())) {
			throw new IllegalArgumentException("The key is null or empty.");
		}
		arangoCollection.updateDocument(document.getKey(), document);
		
		setSpecialProperties(entity, document);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Object entity) {
		
		ensureNotIterable(entity);
		
		ConvertingPropertyAccessor accessor = getPropertyAccessor(entity);
		ArangoDBPersistentEntity<?> persistentEntity = mappingContext
				.getPersistentEntity(entity.getClass());
		
		ArangoDBPersistentProperty idProperty = persistentEntity.getIdProperty();
		
		Object key = null;
		if (idProperty != null) {
			key = accessor.getProperty(idProperty);
		}
		
		if (StringUtils.isEmpty(key)) {
			throw new IllegalArgumentException("The key is null or empty.");
		}
		
		ArangoCollection arangoCollection = getArangoCollection(entity.getClass());
		
		arangoCollection.deleteDocument(key.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArangoDBConverter getConverter() {
		return this.converter;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConvertingPropertyAccessor getPropertyAccessor(Object source) {
		ArangoDBPersistentEntity<?> entity = mappingContext.getPersistentEntity(source.getClass());
		PersistentPropertyAccessor accessor = entity.getPropertyAccessor(source);

		return new ConvertingPropertyAccessor(accessor, converter.getConversionService());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public MappingContext<? extends ArangoDBPersistentEntity<?>, ArangoDBPersistentProperty> getMappingContext() {
		return mappingContext;
	}

	/**
	 * オブジェクトが {@link Iterable} でないことをチェックします。
	 * 
	 * @param o オブジェクト
	 * @throws IllegalArgumentException オブジェクトが {@link Iterable} である場合
	 */
	protected static void ensureNotIterable(Object o) {
		if (null != o) {
			if (o.getClass().isArray() || ITERABLE_CLASSES.contains(o.getClass().getName())) {
				throw new IllegalArgumentException("Cannot use a collection here.");
			}
		}
	}
	
	/**
	 * {@link MappingArangoDBConverter} を返します。
	 * 
	 * @return {@link MappingArangoDBConverter}
	 */
	private ArangoDBConverter getDefaultConverter() {
		MappingArangoDBConverter c = new MappingArangoDBConverter(new ArangoDBMappingContext());
		c.afterPropertiesSet();
		return c;
	}
	
	/**
	 * {@link ArangoCollection} を返します。
	 * 
	 * @param entityClass エンティティクラス
	 * @return {@link ArangoCollection}
	 */
	private ArangoCollection getArangoCollection(Class<?> entityClass) {
		
		ArangoDB arangoDB = arangoDBClient.getArangoDB();
		String dbName = arangoDBClient.getDbName();

		String collectionName = entityClass.getSimpleName();
		Entity entity = entityClass.getAnnotation(Entity.class);
		if (entity != null && !StringUtils.isEmpty(entity.collectionName())) {
			collectionName = entity.collectionName();
		}
		
		ArangoCollection arangoCollection = arangoDB.db(dbName).collection(collectionName);
		
		return arangoCollection;
	}
	
	/**
	 * エンティティに {@link @Id} 、 {@link @Version} の値を設定します。
	 * 
	 * @param entity エンティティ
	 * @param document {@link BaseDocument}
	 */
	private <E> void setSpecialProperties(E entity, BaseDocument document) {
		
		ConvertingPropertyAccessor accessor = getPropertyAccessor(entity);
		ArangoDBPersistentEntity<?> persistentEntity = mappingContext
				.getPersistentEntity(entity.getClass());
		
		ArangoDBPersistentProperty idProperty = persistentEntity.getIdProperty();
		ArangoDBPersistentProperty versionProperty = persistentEntity.getVersionProperty();
		
		if (idProperty != null) {
			accessor.setProperty(idProperty, document.getKey());
		}
		if (versionProperty != null) {
			accessor.setProperty(versionProperty, document.getRevision());
		}
	}
}
