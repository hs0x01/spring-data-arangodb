package pending.org.springframework.data.arangodb.repository.config;

import org.w3c.dom.Element;

import pending.org.springframework.data.arangodb.config.BeanNames;
import pending.org.springframework.data.arangodb.core.ArangoDBTemplate;
import pending.org.springframework.data.arangodb.repository.support.ArangoDBRepositoryFactoryBean;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.data.config.ParsingUtils;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.XmlRepositoryConfigurationSource;

/**
 * {@link RepositoryConfigurationExtensionSupport} の拡張です。
 * 
 * @author hs0x01
 *
 */
public class ArangoDBRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {

	/**
	 * {@link ArangoDBTemplate} の参照です。XML で設定するときに使用します。
	 */
	private static final String ARANGO_DB_TEMPLATE_REF = "arango-db-template-ref";

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getModulePrefix() {
		return "arango-db";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void postProcess(final BeanDefinitionBuilder builder, final XmlRepositoryConfigurationSource config) {
		Element element = config.getElement();
		ParsingUtils.setPropertyReference(builder, element, ARANGO_DB_TEMPLATE_REF, "arangoDBOperations");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void postProcess(final BeanDefinitionBuilder builder, final AnnotationRepositoryConfigurationSource config) {
		builder.addDependsOn(BeanNames.ARANGO_DB_OPERATIONS_MAPPING);
		builder.addPropertyReference("arangoDBOperationsMapping", BeanNames.ARANGO_DB_OPERATIONS_MAPPING);
	}
	
	/**
	 * リポジトリファクトリビーン名を返します。
	 */
	public String getRepositoryFactoryClassName() {
		return ArangoDBRepositoryFactoryBean.class.getName();
	}
}
