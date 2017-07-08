package pending.org.springframework.data.arangodb.core.convert;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.data.mapping.model.MappingException;

import com.arangodb.entity.BaseDocument;

import lombok.SneakyThrows;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBMappingContext;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBPersistentEntity;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBPersistentProperty;
import pending.org.springframework.data.arangodb.core.mapping.BasicArangoDBPersistentEntity;

/**
 * Arango DB のマッピングコンバータです。
 * 
 * <p>
 * マッピングコンバーターは、エンティティと Arango DB ドライバのオブジェクト表現との Read / Write に責務を持ちます。
 * </p>
 * 
 * @author hs0x01
 *
 */
public class MappingArangoDBConverter implements ArangoDBConverter, ApplicationContextAware, InitializingBean {

	/**
	 * {@link ApplicationContext} インスタンスです。
	 */
	protected ApplicationContext applicationContext;

	/**
	 * {@link ArangoDBMappingContext} インスタンスです。
	 */
	protected final MappingContext<? extends ArangoDBPersistentEntity<?>, ArangoDBPersistentProperty> mappingContext;

	/**
	 * {@link DefaultArangoDBTypeMapper} インスタンスです。
	 */
	protected ArangoDBTypeMapper typeMapper;

	/**
	 * {@link CustomConversions} のインスタンスです。
	 */
	protected CustomConversions conversions = new CustomConversions();

	/**
	 * {@link GenericConversionService} インスタンスです。
	 */
	protected GenericConversionService conversionService;

	/**
	 * {@link MappingArangoDBConverter} を生成します。
	 * 
	 * @param mappingContext
	 *            {@link ArangoDBMappingContext} インスタンス
	 */
	public MappingArangoDBConverter(
			final MappingContext<? extends ArangoDBPersistentEntity<?>, ArangoDBPersistentProperty> mappingContext) {

		this.mappingContext = mappingContext;
		this.typeMapper = new DefaultArangoDBTypeMapper();
		this.conversionService = new GenericConversionService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MappingContext<? extends ArangoDBPersistentEntity<?>, ArangoDBPersistentProperty> getMappingContext() {
		return mappingContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConversionService getConversionService() {
		return conversionService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R read(Class<R> type, BaseDocument source) {

		if (source == null) {
			return null;
		}

		ArangoDBPersistentEntity<?> entity = mappingContext.getPersistentEntity(type);

		return readInternal(type, source, entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(Object source, BaseDocument target) {
		if (source == null) {
			return;
		}

		ArangoDBPersistentEntity<?> entity = mappingContext.getPersistentEntity(source.getClass());

		writeInternal(source, target, entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() {
		conversions.registerConvertersIn(conversionService);
	}

	/**
	 * エンティティに Arango DB から取得した {@link BaseDocument} の値を読み込みます。
	 * 
	 * @param type
	 *            エンティティの型
	 * @param source
	 *            {@link BaseDocument}
	 * @param entityInformation
	 *            {@link BasicArangoDBPersistentEntity}
	 * @return 値を読み込んだエンティティのインスタンス
	 */
	@SneakyThrows
	protected <R> R readInternal(Class<R> type, BaseDocument source,
			final ArangoDBPersistentEntity<?> entityInformation) {

		R entity = type.newInstance();

		Map<String, Object> properties = source.getProperties();

		ObjectMapper objectMapper = new ObjectMapper(conversions, conversionService);
		
		objectMapper.mapToEntity(properties, entity);

		return entity;
	}

	/**
	 * {@link BaseDocument} にエンティティの値を書き込みます。
	 * 
	 * @param entity
	 *            エンティティ
	 * @param target
	 *            {@link BaseDocument}
	 * @param entityInformation
	 *            {@link BasicArangoDBPersistentEntity}
	 * @return 値を書き込んだ {@link BasicArangoDBPersistentEntity}
	 */
	@SneakyThrows
	protected void writeInternal(final Object entity, final BaseDocument target,
			final ArangoDBPersistentEntity<?> entityInformation) {

		if (entity == null) {
			return;
		}

		if (entityInformation == null) {
			throw new MappingException("No mapping metadata found for entity of type " + entity.getClass().getName());
		}

		final ConvertingPropertyAccessor accessor = getPropertyAccessor(entity);
		final ArangoDBPersistentProperty idProperty = entityInformation.getIdProperty();
		final ArangoDBPersistentProperty versionProperty = entityInformation.getVersionProperty();

		Map<String, Object> documentMap = new HashMap<>();

		ObjectMapper objectMapper = new ObjectMapper(conversions, conversionService);
		
		objectMapper.entityToMap(entity, documentMap);

		if (idProperty != null) {
			Object key = accessor.getProperty(idProperty);
			target.setKey(key.toString());
			documentMap.remove(idProperty.getName());
		}
		if (versionProperty != null) {
			String version = accessor.getProperty(versionProperty, String.class);
			target.setRevision(version);
			documentMap.remove(versionProperty.getName());
		}

		target.setProperties(documentMap);
	}

	/**
	 * {@link ConvertingPropertyAccessor} を返します。
	 * 
	 * @param entity
	 *            エンティティ
	 * @return {@link ConvertingPropertyAccessor}
	 */
	private ConvertingPropertyAccessor getPropertyAccessor(Object entity) {

		ArangoDBPersistentEntity<?> entityInformation = mappingContext.getPersistentEntity(entity.getClass());
		PersistentPropertyAccessor accessor = entityInformation.getPropertyAccessor(entity);

		return new ConvertingPropertyAccessor(accessor, conversionService);
	}

	/**
	 * {@link CustomConversions} を設定します。
	 * 
	 * @param conversions
	 *            {@link CustomConversions}
	 */
	public void setConversions(CustomConversions conversions) {
		this.conversions = conversions;
	}
}
