package pending.org.springframework.data.arangodb.core.convert;

import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair;
import org.springframework.util.Assert;

import pending.org.springframework.data.arangodb.core.mapping.ArangoDBSimpleTypes;

/**
 * コンバージョン登録情報です。
 *
 * @author hs0x01
 */
class ConverterRegistration {

	private final ConvertiblePair convertiblePair;
	private final boolean reading;
	private final boolean writing;

	/**
	 * {@link ConverterRegistration} を生成します。
	 *
	 * @param convertiblePair
	 *            {@link GenericConverter.ConvertiblePair}
	 * @param isReading
	 *            リーディングのコンバータを検討することを強制する場合 {@code true} 、そうでなければ {@code false}
	 * @param isWriting
	 *            ライティングのコンバータを検討することを強制する場合 {@code true} 、そうでなければ {@code false}
	 */
	public ConverterRegistration(ConvertiblePair convertiblePair, boolean isReading, boolean isWriting) {
		Assert.notNull(convertiblePair);

		this.convertiblePair = convertiblePair;
		reading = isReading;
		writing = isWriting;
	}

	/**
	 * 引数を元に {@link ConverterRegistration} を生成します。
	 *
	 * @param source
	 *            コンバートされる元の型
	 * @param target
	 *            コンバートされる対象の型
	 * @param isReading
	 *            リーディングのコンバータを検討することを強制する場合 {@code true} 、そうでなければ {@code false}
	 * @param isWriting
	 *            ライティングのコンバータを検討することを強制する場合 {@code true} 、そうでなければ {@code false}
	 */
	public ConverterRegistration(Class<?> source, Class<?> target, boolean isReading, boolean isWriting) {
		this(new ConvertiblePair(source, target), isReading, isWriting);
	}

	/**
	 * コンバータがライティングに使われるかどうか返します。
	 *
	 * @return ライティングに使われる場合 {@code true} 、そうでなければ {@code false}
	 */
	public boolean isWriting() {
		return writing == true || (!reading && isSimpleTargetType());
	}

	/**
	 * コンバータがリーディングに使われるかどうか返します。
	 *
	 * @return リーディングに使われる場合 {@code true} 、そうでなければ {@code false}
	 */
	public boolean isReading() {
		return reading == true || (!writing && isSimpleSourceType());
	}

	/**
	 * 実際のコンバージョンペアを返します。
	 *
	 * @return 実際のコンバージョンペア
	 */
	public ConvertiblePair getConvertiblePair() {
		return convertiblePair;
	}

	/**
	 * 元の型が Arango DB Simple 型かどうか返します。
	 *
	 * @return Arango DB Simple 型の場合 {@code true} 、そうでなければ {@code false}
	 */
	public boolean isSimpleSourceType() {
		return isArangoDBBasicType(convertiblePair.getSourceType());
	}

	/**
	 * 対象の型が Arango DB Simple 型かどうか返します。
	 *
	 * @return Arango DB Simple 型の場合 {@code true} 、そうでなければ {@code false}
	 */
	public boolean isSimpleTargetType() {
		return isArangoDBBasicType(convertiblePair.getTargetType());
	}

	/**
	 * 与えられた型が Arango DB が基本的にハンドル可能な型かどうか返します。
	 *
	 * @param type
	 *            与えられた型
	 * @return ハンドル可能な型の場合 {@code true} 、そうでなければ {@code false}
	 */
	private static boolean isArangoDBBasicType(Class<?> type) {
		return ArangoDBSimpleTypes.HOLDER.isSimpleType(type);
	}
}
