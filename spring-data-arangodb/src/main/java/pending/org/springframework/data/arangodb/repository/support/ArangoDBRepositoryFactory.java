package pending.org.springframework.data.arangodb.repository.support;

import java.io.Serializable;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import pending.org.springframework.data.arangodb.core.ArangoDBOperations;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBPersistentEntity;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBPersistentProperty;
import pending.org.springframework.data.arangodb.repository.config.RepositoryOperationsMapping;
import pending.org.springframework.data.arangodb.repository.query.ArangoDBEntityInformation;

/**
 * {@link SimpleArangoDBRepository} インスタンスを生成するファクトリです。
 * 
 * @author hs0x01
 *
 */
public class ArangoDBRepositoryFactory extends RepositoryFactorySupport {

	/**
	 * {@link RepositoryOperationsMapping} インスタンスです。
	 */
	private final RepositoryOperationsMapping arangoDBOperationsMapping;

	/**
	 * {@link MappingContext} インスタンスです。
	 */
	private final MappingContext<? extends ArangoDBPersistentEntity<?>, ArangoDBPersistentProperty> mappingContext;

	/**
	 * ファクトリを生成します。
	 * 
	 * @param arangoDBOperationsMapping
	 *            {@link RepositoryOperationsMapping} インスタンス
	 */
	public ArangoDBRepositoryFactory(final RepositoryOperationsMapping arangoDBOperationsMapping) {

		this.arangoDBOperationsMapping = arangoDBOperationsMapping;
		mappingContext = arangoDBOperationsMapping.getMappingContext();
	}

	/**
	 * ドメインクラスに基づくエンティティ情報を返します。
	 *
	 * @param domainClass
	 *            エンティティクラス.
	 * @param <T>
	 *            エンティティ
	 * @param <ID>
	 *            ID
	 *
	 * @return {@link MappingArangoDBEntityInformation}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T, ID extends Serializable> MappingArangoDBEntityInformation<T, ID> getEntityInformation(
			Class<T> domainClass) {

		ArangoDBPersistentEntity<?> entity = mappingContext.getPersistentEntity(domainClass);

		if (entity == null) {
			throw new MappingException(
					String.format("Could not lookup mapping metadata for domain class %s!", domainClass.getName()));
		}

		return new MappingArangoDBEntityInformation<T, ID>((ArangoDBPersistentEntity<T>) entity);
	}

	/**
	 * メタデータに基づきリポジトリを生成します。
	 * 
	 * @param metadata
	 *            メタデータ
	 * @return リポジトリ
	 */
	@Override
	@SuppressWarnings("rawtypes")
	protected Object getTargetRepository(RepositoryInformation metadata) {

		ArangoDBOperations arangoDBOperations = arangoDBOperationsMapping.resolve(metadata.getRepositoryInterface(),
				metadata.getDomainType());

		ArangoDBEntityInformation<?, Serializable> entityInformation = getEntityInformation(metadata.getDomainType());
		SimpleArangoDBRepository repo = getTargetRepositoryViaReflection(metadata, entityInformation,
				arangoDBOperations);

		return repo;
	}

	/**
	 * 生成するリポジトリの基底クラスを返します。
	 * 
	 * @param metadata
	 *            メタデータ
	 * @return リポジトリの基底クラス
	 */
	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		return SimpleArangoDBPagingAndSortingRepository.class;
	}
}
