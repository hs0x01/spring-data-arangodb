package pending.org.springframework.data.arangodb.core.convert;

import org.springframework.data.convert.DefaultTypeMapper;
import org.springframework.data.convert.TypeAliasAccessor;

import com.arangodb.entity.BaseDocument;

/**
 * Arango DB の型マッパーです。
 * 
 * @author hs0x01
 *
 */
public class DefaultArangoDBTypeMapper extends DefaultTypeMapper<BaseDocument> implements ArangoDBTypeMapper {

	/**
	 * 型マッパーを生成します。
	 */
	public DefaultArangoDBTypeMapper() {
		super(new ArangoDBDocumentTypeAliasAccessor());
	}

	/**
	 * {@link TypeAliasAccessor} の実装です。
	 * 
	 * @author hs0x01
	 *
	 */
	public static final class ArangoDBDocumentTypeAliasAccessor implements TypeAliasAccessor<BaseDocument> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object readAliasFrom(final BaseDocument source) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void writeTypeTo(final BaseDocument sink, final Object alias) {
		}
	}
}
