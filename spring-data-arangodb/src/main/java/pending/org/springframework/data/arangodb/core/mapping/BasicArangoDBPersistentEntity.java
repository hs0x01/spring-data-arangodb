package pending.org.springframework.data.arangodb.core.mapping;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;

/**
 * エンティティを表します。
 * 
 * @author hs0x01
 *
 * @param <T>
 *            エンティティ
 */
public class BasicArangoDBPersistentEntity<T> extends BasicPersistentEntity<T, ArangoDBPersistentProperty>
		implements ArangoDBPersistentEntity<T>, EnvironmentAware {

	/**
	 * {@link Environment} のインスタンスです。
	 */
	private Environment environment;

	/**
	 * インスタンスを生成します。
	 * 
	 * @param typeInformation
	 *            エンティティの型情報
	 */
	public BasicArangoDBPersistentEntity(final TypeInformation<T> typeInformation) {
		super(typeInformation);
	}

	/**
	 * {@link Environment} のインスタンスを設定します。
	 */
	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
