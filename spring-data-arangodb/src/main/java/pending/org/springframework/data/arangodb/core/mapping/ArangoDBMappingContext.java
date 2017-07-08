package pending.org.springframework.data.arangodb.core.mapping;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

/**
 * {@link BasicArangoDBPersistentEntity} 、 {@link ArangoDBPersistentProperty}
 * を扱う Arango DB の {@link MappingContext} のデフォルト実装です。
 * 
 * @author hs0x01
 *
 */
public class ArangoDBMappingContext
		extends AbstractMappingContext<BasicArangoDBPersistentEntity<?>, ArangoDBPersistentProperty>
		implements ApplicationContextAware {

	/**
	 * アプリケーションを設定するコンテキストのインスタンスです。
	 */
	private ApplicationContext context;

	/**
	 * デフォルトフィールド名ストラテジのインスタンスです。
	 */
	private static final FieldNamingStrategy DEFAULT_NAMING_STRATEGY = PropertyNameFieldNamingStrategy.INSTANCE;

	/**
	 * フィールド名ストラテジのインスタンスです。
	 */
	private FieldNamingStrategy fieldNamingStrategy = DEFAULT_NAMING_STRATEGY;

	/**
	 * フィールド名ストラテジのインスタンスを設定します。
	 * 
	 * @param fieldNamingStrategy フィールド名ストラテジのインスタンス
	 */
	public void setFieldNamingStrategy(final FieldNamingStrategy fieldNamingStrategy) {
		this.fieldNamingStrategy = fieldNamingStrategy == null ? DEFAULT_NAMING_STRATEGY : fieldNamingStrategy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <T> BasicArangoDBPersistentEntity<?> createPersistentEntity(TypeInformation<T> typeInformation) {

		BasicArangoDBPersistentEntity<T> entity = new BasicArangoDBPersistentEntity<T>(typeInformation);
		if (context != null) {
			entity.setEnvironment(context.getEnvironment());
		}
		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ArangoDBPersistentProperty createPersistentProperty(Field field, PropertyDescriptor descriptor,
			BasicArangoDBPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {

		return new BasicArangoDBPersistentProperty(field, descriptor, owner, simpleTypeHolder, fieldNamingStrategy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}
}
