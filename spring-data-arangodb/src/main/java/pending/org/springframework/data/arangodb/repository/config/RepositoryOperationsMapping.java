package pending.org.springframework.data.arangodb.repository.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.util.Assert;

import pending.org.springframework.data.arangodb.core.ArangoDBOperations;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBPersistentEntity;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBPersistentProperty;

/**
 * リポジトリと {@link ArangoDBOperations} のマッピングを設定するユーティリティです。
 * <p>
 * リポジトリの型、デフォルトフォールバックにより、使用される {@link ArangoDBOperations} の設定を行います。
 * </p>
 * 
 * @author hs0x01
 *
 */
public class RepositoryOperationsMapping {

	/**
	 * デフォルトの {@link ArangoDBOperations} インスタンスです。
	 */
	private ArangoDBOperations defaultOperations;

	/**
	 * リポジトリと {@link ArangoDBOperations} のマッピングです。
	 */
	private Map<String, ArangoDBOperations> byRepository = new HashMap<String, ArangoDBOperations>();

	/**
	 * エンティティと {@link ArangoDBOperations} のマッピングです。
	 */
	private Map<String, ArangoDBOperations> byEntity = new HashMap<String, ArangoDBOperations>();

	/**
	 * マッピングを生成し、デフォルトフォールバックに使用される {@link ArangoDBOperations} を設定します。
	 * 
	 * @param defaultOperations
	 *            デフォルトフォールバックに使用される {@link ArangoDBOperations}
	 */
	public RepositoryOperationsMapping(ArangoDBOperations defaultOperations) {
		Assert.notNull(defaultOperations);
		this.defaultOperations = defaultOperations;
	}

	/**
	 * リポジトリで使用される {@link MappingContext} を返します。
	 * <p>
	 * {@link MappingContext} は、デフォルトの {@link ArangoDBOperations} から抽出されます。
	 * </p>
	 * 
	 * @return リポジトリで使用される {@link MappingContext}
	 */
	public MappingContext<? extends ArangoDBPersistentEntity<?>, ArangoDBPersistentProperty> getMappingContext() {
		return defaultOperations.getConverter().getMappingContext();
	}

	/**
	 * リポジトリインタフェースとエンティティタイプから、対応する {@link ArangoDBOperations} を解決します。
	 * 
	 * <p>
	 * リポジトリインタフェースから対応する {@link ArangoDBOperations}
	 * を検索し、エンティティタイプ、デフォルトフォールバックの順に解決します。
	 * </p>
	 * 
	 * @param repositoryInterface
	 *            リポジトリインタフェース
	 * @param domainType
	 *            エンティティタイプ
	 * @return 解決された {@link ArangoDBOperations} インスタンス
	 */
	public ArangoDBOperations resolve(Class<?> repositoryInterface, Class<?> domainType) {
		ArangoDBOperations result = byRepository.get(repositoryInterface.getName());
		if (result != null) {
			return result;
		} else {
			result = byEntity.get(domainType.getName());
			if (result != null) {
				return result;
			} else {
				return defaultOperations;
			}
		}
	}
}
