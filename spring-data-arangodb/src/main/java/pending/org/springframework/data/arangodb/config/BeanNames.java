package pending.org.springframework.data.arangodb.config;

import org.springframework.core.convert.converter.Converter;

import pending.org.springframework.data.arangodb.core.ArangoDBClient;
import pending.org.springframework.data.arangodb.core.ArangoDBOperations;

/**
 * Arango DB bean のためのデフォルト bean 名です。
 * 
 * @author hs0x01
 *
 */
public class BeanNames {

	/**
	 * デフォルト {@link ArangoDBClient} bean 名称です。
	 */
	public static final String ARANGO_DB = "arangoDB";

	/**
	 * デフォルト {@link ArangoDBOperations} bean 名称です。
	 */
	public static final String ARANGO_DB_TEMPLATE = "arangoDBTemplate";
	
	/**
	 * リポジトリと Arango DB 操作のカスタムマッピング bean 名称です。
	 */
	public static final String ARANGO_DB_OPERATIONS_MAPPING = "arangoDBRepositoryOperationsMapping";
	
	/**
	 * Arango DB ドライバインタフェースとエンティティ間のデータ変換を実行する bean 名称です。
	 */
	public static final String ARANGO_DB_MAPPING_CONVERTER = "arangoDBMappingConverter";
	
	/**
	 * Arango DB に保存されるエンティティのマッピングメタデータの bean 名称です。
	 */
	public static final String ARANGO_DB_MAPPING_CONTEXT = "arangoDBMappingContext";
	
	/**
	 * エンティティフィールドをエンコード / デコードするカスタム {@link Converter} を登録する bean 名称です。
	 */
	public static final String ARANGO_DB_CUSTOM_CONVERSIONS = "arangoDBCustomConversions";
}
