package pending.org.springframework.data.arangodb.core.convert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.core.GenericTypeResolver;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.util.Assert;

/**
 * カスタムコンバージョンをキャプチャするオブジェクトです。
 * 
 * <p>
 * JSON にマッピングされる型がシンプルなものであるかどうか検討します。<br>
 * それらは、深いインスペクションも入れ子変換も必要ないためです。
 * </p>
 * 
 * @author hs0x01
 *
 */
public class CustomConversions {

	/**
	 * {@link SimpleTypeHolder} のインスタンスです。
	 */
	private final SimpleTypeHolder simpleTypeHolder;

	/**
	 * コンバータインスタンスのリストです。
	 */
	private final List<Object> converters;
	
	/**
	 * 暗号化 / 復号を行うコンバータです。
	 */
	private EncryptConverter encryptConverter;

	private final Set<GenericConverter.ConvertiblePair> readingPairs;
	private final Set<GenericConverter.ConvertiblePair> writingPairs;
	private final Set<Class<?>> customSimpleTypes;
	private final ConcurrentMap<GenericConverter.ConvertiblePair, CacheValue> customReadTargetTypes;

	/**
	 * コンバータなしでインスタンス生成します。
	 */
	CustomConversions() {
		this(new ArrayList<Object>(), null);
	}

	/**
	 * コンバータのリストでインスタンス生成します。
	 *
	 * @param converters
	 *            コンバータのリスト
	 * @param encryptConverter {@link EncryptConverter} の実装。使用しなければ {@code null}。
	 */
	public CustomConversions(final List<?> converters, EncryptConverter encryptConverter) {
		Assert.notNull(converters);

		readingPairs = new LinkedHashSet<GenericConverter.ConvertiblePair>();
		writingPairs = new LinkedHashSet<GenericConverter.ConvertiblePair>();
		customSimpleTypes = new HashSet<Class<?>>();
		customReadTargetTypes = new ConcurrentHashMap<GenericConverter.ConvertiblePair, CacheValue>();

		this.converters = new ArrayList<Object>();
		this.converters.addAll(converters);
		this.converters.addAll(DateConverters.getConvertersToRegister());

		for (Object converter : this.converters) {
			registerConversion(converter);
		}

		simpleTypeHolder = new SimpleTypeHolder(customSimpleTypes, true);
		
		this.encryptConverter = encryptConverter;
	}

	/**
	 * 型が Simple 型かどうかチェックします。
	 *
	 * @param type
	 *            チェック対象の型
	 * @return Simple 型ならば {@code true}、そうでなければ {@code false}
	 */
	public boolean isSimpleType(final Class<?> type) {
		return simpleTypeHolder.isSimpleType(type);
	}

	/**
	 * {@link SimpleTypeHolder} を返します。
	 *
	 * @return {@link SimpleTypeHolder}
	 */
	public SimpleTypeHolder getSimpleTypeHolder() {
		return simpleTypeHolder;
	}

	/**
	 * {@link GenericConversionService} に登録されるコンバータを設定します。
	 *
	 * @param conversionService
	 *            {@link GenericConversionService}
	 */
	public void registerConvertersIn(final GenericConversionService conversionService) {
		for (Object converter : converters) {
			boolean added = false;

			if (converter instanceof Converter) {
				conversionService.addConverter((Converter<?, ?>) converter);
				added = true;
			}

			if (converter instanceof ConverterFactory) {
				conversionService.addConverterFactory((ConverterFactory<?, ?>) converter);
				added = true;
			}

			if (converter instanceof GenericConverter) {
				conversionService.addConverter((GenericConverter) converter);
				added = true;
			}

			if (!added) {
				throw new IllegalArgumentException(
						"Given set contains element that is neither Converter nor ConverterFactory!");
			}
		}
	}

	/**
	 * コンバータのコンバージョンを登録します。
	 * <p>
	 * {@link GenericConverter} によって返されたジェネリクスあるいは
	 * {@link GenericConverter.ConvertiblePair} かどうか調べます。
	 * </p>
	 * 
	 * @param converter
	 *            登録されるコンバータ
	 */
	private void registerConversion(final Object converter) {
		Class<?> type = converter.getClass();
		boolean isWriting = type.isAnnotationPresent(WritingConverter.class);
		boolean isReading = type.isAnnotationPresent(ReadingConverter.class);

		if (converter instanceof GenericConverter) {
			GenericConverter genericConverter = (GenericConverter) converter;
			for (GenericConverter.ConvertiblePair pair : genericConverter.getConvertibleTypes()) {
				register(new ConverterRegistration(pair, isReading, isWriting));
			}
		} else if (converter instanceof Converter) {
			Class<?>[] arguments = GenericTypeResolver.resolveTypeArguments(converter.getClass(), Converter.class);
			register(new ConverterRegistration(arguments[0], arguments[1], isReading, isWriting));
		} else {
			throw new IllegalArgumentException("Unsupported Converter type!");
		}
	}

	/**
	 * 基本 Arango DB 型側に依存するリーディング、ライティングのペアとして、{@link ConverterRegistration}
	 * を登録します。
	 * 
	 * @param registration
	 *            {@link ConverterRegistration}
	 */
	private void register(final ConverterRegistration registration) {
		GenericConverter.ConvertiblePair pair = registration.getConvertiblePair();

		if (registration.isReading()) {
			readingPairs.add(pair);
		}

		if (registration.isWriting()) {
			writingPairs.add(pair);
			customSimpleTypes.add(pair.getSourceType());
		}
	}

	/**
	 * 与えられた型を Arango DB ネイティブな型に変換するためのカスタムコンバージョンを持つ場合、コンバートするための型を返します。
	 * 
	 * @param sourceType
	 *            与えられた型
	 * @return コンバートするための型
	 */
	public Class<?> getCustomWriteTarget(Class<?> sourceType) {
		return getCustomWriteTarget(sourceType, null);
	}

	/**
	 * 与えられた型のオブジェクトを書くことができる型を返します。
	 * <p>
	 * 返される型は、与えられた期待された型のサブクラスである可能性があります。<br>
	 * {@code requestedTargetType} が {@literal null} ならば、最初にマッチした型を返します。<br>
	 * 型が見つからなければ、 {@literal null} を返します。
	 * </p>
	 * 
	 * @param sourceType
	 *            与えられた型
	 * @param requestedTargetType
	 *            与えられた期待された型
	 * @return 与えられた型のオブジェクトを書くことができる型
	 */
	public Class<?> getCustomWriteTarget(Class<?> sourceType, Class<?> requestedTargetType) {
		Assert.notNull(sourceType);
		return getCustomTarget(sourceType, requestedTargetType, writingPairs);
	}

	/**
	 * Arango DB ネイティブ型に書くために登録されるカスタムコンバージョンがあるかどうかを返します。
	 *
	 * @param sourceType
	 *            与えられた型
	 * @return カスタムコンバージョンがあれば {@code true} 、そうでなければ {@code false}
	 */
	public boolean hasCustomWriteTarget(Class<?> sourceType) {
		Assert.notNull(sourceType);
		return hasCustomWriteTarget(sourceType, null);
	}

	/**
	 * {@code sourceType} のオブジェクトを {@code requestedTargetType}
	 * のオブジェクトに書くために登録されたカスタムコンバージョンがあるかどうかを返します。
	 * 
	 * @param sourceType
	 *            与えられた型
	 * @param requestedTargetType
	 *            与えられた期待された型
	 * @return カスタムコンバージョンがあるならば {@code true} 、そうでなければ {@code false}
	 */
	public boolean hasCustomWriteTarget(Class<?> sourceType, Class<?> requestedTargetType) {
		Assert.notNull(sourceType);
		return getCustomWriteTarget(sourceType, requestedTargetType) != null;
	}

	/**
	 * {@code sourceType} のオブジェクトを {@code requestedTargetType}
	 * のオブジェクトに読むために登録されたカスタムコンバージョンがあるかどうかを返します。
	 * 
	 * @param sourceType
	 *            与えられた型
	 * @param requestedTargetType
	 *            与えられた期待された型
	 * @return カスタムコンバージョンがあるならば {@code true} 、そうでなければ {@code false}
	 */
	public boolean hasCustomReadTarget(Class<?> sourceType, Class<?> requestedTargetType) {
		Assert.notNull(sourceType);
		Assert.notNull(requestedTargetType);
		return getCustomReadTarget(sourceType, requestedTargetType) != null;
	}

	/**
	 * 与えられた {@code sourceType} 、 {@code requestedTargetType} の実際のコンバージョン型を返します。
	 * <p>
	 * 返される型は、 {@code requestedTargetType} に割り当て可能であることに留意してください。
	 * </p>
	 * 
	 * @param sourceType
	 *            与えられた型
	 * @param requestedTargetType
	 *            与えられた期待された型
	 * @return 実際のコンバージョン型
	 */
	private Class<?> getCustomReadTarget(Class<?> sourceType, Class<?> requestedTargetType) {
		Assert.notNull(sourceType);
		if (requestedTargetType == null) {
			return null;
		}

		GenericConverter.ConvertiblePair lookupKey = new GenericConverter.ConvertiblePair(sourceType,
				requestedTargetType);
		CacheValue readTargetTypeValue = customReadTargetTypes.get(lookupKey);

		if (readTargetTypeValue != null) {
			return readTargetTypeValue.getType();
		}

		readTargetTypeValue = CacheValue.of(getCustomTarget(sourceType, requestedTargetType, readingPairs));
		CacheValue cacheValue = customReadTargetTypes.putIfAbsent(lookupKey, readTargetTypeValue);

		return cacheValue != null ? cacheValue.getType() : readTargetTypeValue.getType();
	}

	/**
	 * {@code sourceType} の {@link GenericConverter.ConvertiblePair} を調べます。<br>
	 * 加えて、型の割り当て可能をチェックします。
	 * 
	 * @param sourceType
	 *            与えられた型
	 * @param requestedTargetType
	 *            与えられた期待された型
	 * @param pairs
	 *            {@link GenericConverter.ConvertiblePair} の {@link Iterable}
	 *            オブジェクト
	 * @return カスタムコンバージョン
	 */
	private static Class<?> getCustomTarget(Class<?> sourceType, Class<?> requestedTargetType,
			Iterable<GenericConverter.ConvertiblePair> pairs) {
		Assert.notNull(sourceType);
		Assert.notNull(pairs);

		for (GenericConverter.ConvertiblePair typePair : pairs) {
			if (typePair.getSourceType().isAssignableFrom(sourceType)) {
				Class<?> targetType = typePair.getTargetType();
				if (requestedTargetType == null || targetType.isAssignableFrom(requestedTargetType)) {
					return targetType;
				}
			}
		}

		return null;
	}

	/**
	 * 型キャッシュで {@literal null} を安全に保存するためのラッパーです。
	 *
	 * @author hs0x01
	 */
	private static class CacheValue {

		private static final CacheValue ABSENT = new CacheValue(null);

		private final Class<?> type;

		public CacheValue(Class<?> type) {
			this.type = type;
		}

		public Class<?> getType() {
			return type;
		}

		static CacheValue of(Class<?> type) {
			return type == null ? ABSENT : new CacheValue(type);
		}
	}

	/**
	 * {@link EncryptConverter} を返します。
	 * 
	 * @return {@link EncryptConverter}
	 */
	public EncryptConverter getEncryptConverter() {
		return encryptConverter;
	}
}
