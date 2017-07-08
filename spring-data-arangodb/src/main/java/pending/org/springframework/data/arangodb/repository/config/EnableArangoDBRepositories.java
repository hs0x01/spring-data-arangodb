package pending.org.springframework.data.arangodb.repository.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import pending.org.springframework.data.arangodb.config.BeanNames;
import pending.org.springframework.data.arangodb.core.ArangoDBTemplate;
import pending.org.springframework.data.arangodb.repository.support.ArangoDBRepositoryFactoryBean;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;

import java.lang.annotation.*;

/**
 * Arango DB リポジトリを有効にするアノテーションです。
 * 
 * <p>
 * 基底パッケージが {@link #value()} 、 {@link #basePackages()} 、
 * {@link #basePackageClasses()} で設定されていない場合、
 * このアノテーションの付いたクラスのパッケージを基底パッケージとします。
 * </p>
 *
 * @author hs0x01
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(ArangoDBRepositoriesRegistrar.class)
public @interface EnableArangoDBRepositories {

	/**
	 * {@link #basePackages()} 属性のエイリアスです。簡潔にアノテーション宣言できるようにします。
	 * <p>
	 * E.g. {@code @EnableArangoDBRepositories(basePackages="org.my.pkg")} の代わりに
	 * {@code @EnableArangoDBRepositories("org.my.pkg")} のように。
	 * </p>
	 */
	String[] value() default {};

	/**
	 * エンティティを検索する基底パッケージです。
	 * <p>
	 * 型安全のためには、 {@link #basePackageClasses()} を使用します。
	 * </p>
	 */
	String[] basePackages() default {};

	/**
	 * エンティティを検索する型安全な基底パッケージです。
	 * <p>
	 * 指定されたそれぞれのクラスのパッケージが検索されます。<br>
	 * エンティティクラスを指定するよりもマーカーインタフェースやクラスを作成して指定することを検討してください。
	 * </p>
	 */
	Class<?>[] basePackageClasses() default {};

	/**
	 * エンティティを検索する対象を絞り込みます。
	 * <p>
	 * 基底パッケージから指定されたフィルタにマッチするように絞り込みます。
	 * </p>
	 */
	Filter[] includeFilters() default {};

	/**
	 * 検索から除外するエンティティを指定します。
	 */
	Filter[] excludeFilters() default {};

	/**
	 * カスタムリポジトリ実装を探すときに使われる接尾辞です。
	 * <p>
	 * デフォルトは、 {@literal Impl} です。<br>
	 * E.g {@code PersonRepository} の実装は、 {@code PersonRepositoryImpl}
	 * として検索されます。
	 * </p>
	 */
	String repositoryImplementationPostfix() default "";

	/**
	 * Spring Data 名前付きクエリプロパティファイルを検索するロケーションを設定します。
	 * <p>
	 * デフォルトは、 {@code META-INFO/arangodb-named-queries.properties} が使われます。
	 * </p>
	 * 
	 * @return
	 */
	String namedQueriesLocation() default "";

	/**
	 * リポジトリプロキシを生成するために使われるリポジトリベースクラスを設定します。
	 */
	Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

	/**
	 * リポジトリインスタンス生成に使われる {@link FactoryBean} クラスを返します。
	 * <p>
	 * デフォルトは、 {@link ArangoDBRepositoryFactoryBean} です。
	 * </p>
	 */
	Class<?> repositoryFactoryBeanClass() default ArangoDBRepositoryFactoryBean.class;

	/**
	 * 内部クラスのように、入れ子になったリポジトリインタフェースを、リポジトリ基盤が発見できるようにすべきかどうかを設定します。
	 */
	boolean considerNestedRepositories() default false;

	/**
	 * リポジトリにデフォルトで使われる {@link ArangoDBTemplate} bean の名称を設定します。
	 * 
	 * @return
	 */
	String arangoDBTemplateRef() default BeanNames.ARANGO_DB_TEMPLATE;
}
