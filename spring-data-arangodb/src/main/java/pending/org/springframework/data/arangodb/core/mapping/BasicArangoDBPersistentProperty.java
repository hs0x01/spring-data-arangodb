package pending.org.springframework.data.arangodb.core.mapping;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mapping.model.SimpleTypeHolder;

/**
 * エンティティのプロパティを表します。
 * 
 * @author hs0x01
 *
 */
public class BasicArangoDBPersistentProperty extends AnnotationBasedPersistentProperty<ArangoDBPersistentProperty>
		implements ArangoDBPersistentProperty {

	/**
	 * {@link FieldNamingStrategy} のインスタンスです。
	 */
	private final FieldNamingStrategy fieldNamingStrategy;

	/**
	 * インスタンスを生成します。
	 * 
	 * @param field
	 *            フィールド
	 * @param propertyDescriptor
	 *            {@link PropertyDescriptor}
	 * @param owner
	 *            エンティティ
	 * @param simpleTypeHolder
	 *            {@link SimpleTypeHolder}
	 * @param fieldNamingStrategy
	 *            {@link FieldNamingStrategy}
	 */
	public BasicArangoDBPersistentProperty(final Field field, final PropertyDescriptor propertyDescriptor,
			final ArangoDBPersistentEntity<?> owner, final SimpleTypeHolder simpleTypeHolder,
			final FieldNamingStrategy fieldNamingStrategy) {

		super(field, propertyDescriptor, owner, simpleTypeHolder);
		this.fieldNamingStrategy = fieldNamingStrategy == null ? PropertyNameFieldNamingStrategy.INSTANCE
				: fieldNamingStrategy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Association<ArangoDBPersistentProperty> createAssociation() {
		return new Association<ArangoDBPersistentProperty>(this, null);
	}
}
