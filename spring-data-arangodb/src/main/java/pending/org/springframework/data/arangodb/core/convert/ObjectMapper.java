package pending.org.springframework.data.arangodb.core.convert;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.support.GenericConversionService;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import pending.org.springframework.data.arangodb.core.mapping.Encrypt;

/**
 * エンティティと Arango DB オブジェクトを変換します。
 * 
 * @author hs0x01
 *
 */
@AllArgsConstructor
public class ObjectMapper {

	/**
	 * {@link CustomConversions} のインスタンスです。
	 */
	private CustomConversions conversions;

	/**
	 * {@link GenericConversionService} インスタンスです。
	 */
	private GenericConversionService conversionService;

	/**
	 * エンティティの集合を {@link List} で返します。
	 * 
	 * @param entities
	 *            エンティティの集合
	 * @return {@link List}
	 */
	@SneakyThrows
	protected List<Object> entitiesToList(Iterable<?> entities) {

		List<Object> list = new ArrayList<>();

		for (Object entity : entities) {

			Map<String, Object> map = new HashMap<>();

			entityToMap(entity, map);

			if (map.isEmpty()) {
				list.add(entity);
			} else {
				list.add(map);
			}
		}

		return list;
	}

	/**
	 * {@link Map} の集合をエンティティの {@link List} で返します。
	 * 
	 * @param maps
	 *            {@link Map} の集合
	 * @param entityClass
	 *            エンティティクラス
	 * @return エンティティの {@link List}
	 */
	@SneakyThrows
	@SuppressWarnings("unchecked")
	protected List<Object> listToEntities(Iterable<Object> objects, Class<?> entityClass) {

		List<Object> list = new ArrayList<>();

		for (Object object : objects) {

			if (objects instanceof Map) {

				Object entity = entityClass.newInstance();

				mapToEntity((Map<String, Object>) object, entity);

				list.add(entity);

			} else {

				list.add(object);
			}
		}

		return list;
	}

	/**
	 * エンティティの値を {@link Map} に設定します。
	 * 
	 * @param entity
	 *            エンティティ
	 * @param map
	 *            {@link Map}
	 */
	@SneakyThrows
	protected void entityToMap(Object entity, Map<String, Object> map) {

		Class<?> originalEntityClass = entity.getClass();
		Class<?> entityClass = entity.getClass();

		while (entityClass != null) {

			Field[] fields = entityClass.getDeclaredFields();

			for (Field field : fields) {

				PropertyDescriptor propertyDescriptor;
				try {
					propertyDescriptor = new PropertyDescriptor(field.getName(), originalEntityClass);
				} catch (IntrospectionException e) {
					continue;
				}

				Method getter = propertyDescriptor.getReadMethod();

				Object value = getter.invoke(entity);

				if (value != null) {
					value = convertForWriteIfNeeded(value, field);
					if (value instanceof Iterable) {
						value = entitiesToList((Iterable<?>) value);
					} else {
						Map<String, Object> mapIfEntity = new HashMap<>();
						entityToMap(value, mapIfEntity);
						if (!mapIfEntity.isEmpty()) {
							value = mapIfEntity;
						}
					}
				}

				map.put(field.getName(), value);
			}

			entityClass = entityClass.getSuperclass();
		}
	}

	/**
	 * {@link Map} の値を エンティティに設定します。
	 * 
	 * @param map
	 *            {@link Map}
	 * @param entity
	 *            エンティティ
	 */
	@SuppressWarnings("unchecked")
	@SneakyThrows
	protected void mapToEntity(Map<String, Object> map, Object entity) {

		Class<?> originalEntityClass = entity.getClass();
		Class<?> entityClass = entity.getClass();

		while (entityClass != null) {

			Field[] fields = entityClass.getDeclaredFields();

			for (Field field : fields) {

				PropertyDescriptor propertyDescriptor;
				try {
					propertyDescriptor = new PropertyDescriptor(field.getName(), originalEntityClass);
				} catch (IntrospectionException e) {
					continue;
				}

				Method setter = propertyDescriptor.getWriteMethod();

				if (!map.containsKey(field.getName())) {
					continue;
				}

				Object value = map.get(field.getName());

				if (value != null) {
					value = convertForReadIfNeeded(value, setter.getParameterTypes()[0], field);
					if (value instanceof Iterable) {
						Class<?> arg0Class = (Class<?>) ((ParameterizedType) setter.getGenericParameterTypes()[0])
								.getActualTypeArguments()[0];
						value = listToEntities((Iterable<Object>) value, arg0Class);
					} else if (value instanceof Map) {
						Object arg0Object = setter.getParameterTypes()[0].newInstance();
						mapToEntity((Map<String, Object>) value, arg0Object);
						value = arg0Object;
					}
				}

				setter.invoke(entity, value);
			}

			entityClass = entityClass.getSuperclass();
		}
	}

	/**
	 * 実際に保存されるクラスに必要ならば値をコンバートします。<br>
	 * 不要ならば値をそのまま返します。
	 * 
	 * @param value
	 *            実際に保存されるクラスにコンバートされる値
	 * @param field
	 *            フィールド
	 * @return コンバートされた値 (コンバート不要ならば同じ値)
	 */
	private Object convertForWriteIfNeeded(Object value, Field field) {
		if (value == null) {
			return null;
		}

		Class<?> targetType = this.conversions.getCustomWriteTarget(value.getClass());
		if (targetType != null) {
			value = this.conversionService.convert(value, targetType);
		}

		Encrypt encrypt = field.getAnnotation(Encrypt.class);
		
		EncryptConverter encryptConverter = conversions.getEncryptConverter();
		
		if (encrypt != null && encryptConverter != null) {
			value = encryptConverter.encrypt(value.toString());
		}

		return value;
	}

	/**
	 * 実際に保存されるクラスに必要ならば値をコンバートします。<br>
	 * 不要ならば値をそのまま返します。
	 * 
	 * @param source
	 *            変換元の値
	 * @param target
	 *            変換先のクラス
	 * @param targetField
	 *            変換先フィールド
	 * 
	 * @return コンバートされた値 (コンバート不要ならば同じ値)
	 */
	private Object convertForReadIfNeeded(Object source, Class<?> target, Field targetField) {
		if (source == null) {
			return null;
		}
		Object value = source;
		
		Encrypt encrypt = targetField.getAnnotation(Encrypt.class);
		
		EncryptConverter encryptConverter = conversions.getEncryptConverter();
		
		if (encrypt != null && encryptConverter != null && value instanceof String) {
			value = encryptConverter.decrypt((String) value);
		}
		
		if (conversions.hasCustomReadTarget(value.getClass(), target)) {
			value = conversionService.convert(value, target);
		}
		
		return value;
	}
}
