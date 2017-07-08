package pending.org.springframework.data.arangodb.repository.support;

import java.io.Serializable;

import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.Assert;

import pending.org.springframework.data.arangodb.core.ArangoDBOperations;
import pending.org.springframework.data.arangodb.repository.config.RepositoryOperationsMapping;

/**
 * リポジトリを生成するファクトリビーンです。
 * 
 * @author hs0x01
 *
 * @param <T> リポジトリ
 * @param <S> エンティティ
 * @param <ID> ID
 */
public class ArangoDBRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
		extends RepositoryFactoryBeanSupport<T, S, ID> {

	/**
	 * {@link RepositoryOperationsMapping} インスタンスです。
	 */
	private RepositoryOperationsMapping operationsMapping;

	/**
	 * 与えられたリポジトリインターフェースのためのファクトリビーンを生成します。
	 * 
	 * @param repositoryInterface リポジトリインターフェース
	 */
	public ArangoDBRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	/**
	 * {@link ArangoDBOperations} を設定します。
	 * 
	 * @param operations {@link ArangoDBOperations}
	 */
	public void setArangoDBOperations(final ArangoDBOperations operations) {
		setArangoDBOperationsMapping(new RepositoryOperationsMapping(operations));
	}

	/**
	 * {@link RepositoryOperationsMapping} を設定します。
	 * 
	 * @param mapping {@link RepositoryOperationsMapping}
	 */
	public void setArangoDBOperationsMapping(final RepositoryOperationsMapping mapping) {
		this.operationsMapping = mapping;
		setMappingContext(operationsMapping.getMappingContext());
	}

	/**
	 * リポジトリファクトリのインスタンスを生成します。
	 * 
	 * @return リポジトリファクトリのインスタンス
	 */
	@Override
	protected RepositoryFactorySupport createRepositoryFactory() {
		return getFactoryInstance(operationsMapping);
	}

	/**
	 * {@link RepositoryOperationsMapping} を使って、リポジトリファクトリのインスタンスを生成します。
	 * 
	 * @return リポジトリファクトリのインスタンス
	 */
	protected ArangoDBRepositoryFactory getFactoryInstance(final RepositoryOperationsMapping operationsMapping) {
		return new ArangoDBRepositoryFactory(operationsMapping);
	}

	/**
	 * 依存関係が解決されていることを確実にします。
	 */
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		Assert.notNull(operationsMapping, "operationsMapping must not be null!");
	}
}
