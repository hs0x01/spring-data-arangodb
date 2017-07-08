package pending.org.springframework.data.arangodb.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import pending.org.springframework.data.arangodb.core.ArangoDBTemplate;
import pending.org.springframework.data.arangodb.core.convert.CustomConversions;
import pending.org.springframework.data.arangodb.core.convert.MappingArangoDBConverter;
import pending.org.springframework.data.arangodb.core.mapping.ArangoDBMappingContext;
import pending.org.springframework.data.arangodb.core.mapping.Entity;
import pending.org.springframework.data.arangodb.repository.config.RepositoryOperationsMapping;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.mapping.model.CamelCaseAbbreviatingFieldNamingStrategy;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Java による Arango DB 設定の基底となるクラスです。
 * 
 * @author hs0x01
 *
 */
@Configuration
public abstract class AbstractArangoDBDataConfiguration {

	protected abstract ArangoDBConfigurer arangoDBConfigurer();

	/**
	 * {@link ArangoDBTemplate} を生成します。
	 * 
	 * @return {@link ArangoDBTemplate}
	 * @throws Exception
	 *             bean 生成に失敗した場合
	 */
	@Bean(name = BeanNames.ARANGO_DB_TEMPLATE)
	public ArangoDBTemplate arangoDBTemplate() throws Exception {
		ArangoDBTemplate template = 
				new ArangoDBTemplate(arangoDBConfigurer().arangoDBClient(), mappingArangoDBConverter());
		return template;
	}

	/**
	 * {@link RepositoryOperationsMapping} を生成します。
	 * 
	 * @param arangoDBTemplate
	 *            {@link ArangoDBTemplate}
	 * @return {@link RepositoryOperationsMapping}
	 * @throws Exception
	 *             bean 生成に失敗した場合
	 */
	@Bean(name = BeanNames.ARANGO_DB_OPERATIONS_MAPPING)
	public RepositoryOperationsMapping repositoryOperationsMapping(ArangoDBTemplate arangoDBTemplate) throws Exception {
		RepositoryOperationsMapping baseMapping = new RepositoryOperationsMapping(arangoDBTemplate);
		// configureRepositoryOperationsMapping(baseMapping);
		return baseMapping;
	}

	/**
	 * {@link MappingArangoDBConverter} を生成します。
	 * 
	 * @return {@link MappingArangoDBConverter}
	 * @throws Exception
	 *             bean 生成に失敗した場合
	 */
	@Bean(name = BeanNames.ARANGO_DB_MAPPING_CONVERTER)
	public MappingArangoDBConverter mappingArangoDBConverter() throws Exception {
		MappingArangoDBConverter converter = new MappingArangoDBConverter(arangoDBMappingContext());
		converter.setConversions(customConversions());
		return converter;
	}

	/**
	 * {@link ArangoDBMappingContext} を生成します。
	 * 
	 * @return {@link ArangoDBMappingContext}
	 * @throws Exception
	 *             bean 生成に失敗した場合
	 */
	@Bean(name = BeanNames.ARANGO_DB_MAPPING_CONTEXT)
	public ArangoDBMappingContext arangoDBMappingContext() throws Exception {
		ArangoDBMappingContext mappingContext = new ArangoDBMappingContext();
		mappingContext.setInitialEntitySet(getInitialEntitySet());
		mappingContext.setSimpleTypeHolder(customConversions().getSimpleTypeHolder());
		mappingContext.setFieldNamingStrategy(fieldNamingStrategy());
		return mappingContext;
	}

	/**
	 * {@link Entity} アノテーションがつけられたクラスのマッピングベースパッケージを検索します。
	 * 
	 * @throws ClassNotFoundException
	 *             初期エンティティがロードできない場合
	 */
	protected Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {
		String basePackage = getMappingBasePackage();
		Set<Class<?>> initialEntitySet = new HashSet<Class<?>>();

		if (StringUtils.hasText(basePackage)) {
			ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(
					false);
			componentProvider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
			for (BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {
				initialEntitySet.add(ClassUtils.forName(candidate.getBeanClassName(),
						AbstractArangoDBConfiguration.class.getClassLoader()));
			}
		}

		return initialEntitySet;
	}

	/**
	 * {@link Persistent} アノテーションをつけられたクラスを検索するベースパッケージを返します。<br>
	 * デフォルトで、このクラスを継承した具象設定クラスのパッケージを返します。
	 *
	 * @return ベースパッケージ
	 */
	protected String getMappingBasePackage() {
		return getClass().getPackage().getName();
	}

	/**
	 * フィールド名が {@link CamelCaseAbbreviatingFieldNamingStrategy}
	 * で短縮されるべきかどうかを返します。
	 * 
	 * @return フィールド名が短縮されるべきならば{@code true}、そうでなければ{@code false}
	 */
	protected boolean abbreviateFieldNames() {
		return false;
	}

	/**
	 * 生成された {@link ArangoDBMappingContext} インスタンス上に {@link FieldNamingStrategy}
	 * を設定します。
	 * 
	 * @return {@link FieldNamingStrategy}
	 */
	protected FieldNamingStrategy fieldNamingStrategy() {
		return abbreviateFieldNames() ? new CamelCaseAbbreviatingFieldNamingStrategy()
				: PropertyNameFieldNamingStrategy.INSTANCE;
	}

	/**
	 * 必要ならば、 {@link CustomConversions} にカスタムコンバータを登録します。
	 * <p>
	 * カスタムコンバータは、 {@link #mappingArangoDBConverter()} 、
	 * {@link #arangoDBMappingContext()} とともに登録されます。<br>
	 * デフォルトでは、空の {@link CustomConversions} を返します。
	 * </p>
	 * 
	 * @return {@link CustomConversions}
	 */
	@Bean(name = BeanNames.ARANGO_DB_CUSTOM_CONVERSIONS)
	public CustomConversions customConversions() {
		return new CustomConversions(Collections.emptyList(), null);
	}
}
