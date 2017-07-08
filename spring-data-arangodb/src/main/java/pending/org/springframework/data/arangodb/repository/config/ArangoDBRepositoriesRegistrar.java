package pending.org.springframework.data.arangodb.repository.config;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * Arango DB リポジトリレジストラです。
 * 
 * @author hs0x01
 *
 */
public class ArangoDBRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableArangoDBRepositories.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected RepositoryConfigurationExtension getExtension() {
		return new ArangoDBRepositoryConfigurationExtension();
	}
}
