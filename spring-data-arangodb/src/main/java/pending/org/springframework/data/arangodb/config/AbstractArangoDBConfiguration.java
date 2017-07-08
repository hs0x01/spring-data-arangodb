package pending.org.springframework.data.arangodb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.arangodb.ArangoDB;

import pending.org.springframework.data.arangodb.core.ArangoDBClient;

/**
 * Java による Arango DB 設定の基底となるクラスです。
 * 
 * @author hs0x01
 *
 */
@Configuration
public abstract class AbstractArangoDBConfiguration extends AbstractArangoDBDataConfiguration
		implements ArangoDBConfigurer {

	/**
	 * Arango DB のデータベース名を返します。
	 * 
	 * @return データベース名
	 */
	protected abstract String getDBName();
	
	/**
	 * Arango DB の接続ホスト名または IP アドレスを返します。
	 * 
	 * @return 接続ホスト名または IP アドレス
	 */
	protected abstract String getHost();
	
	/**
	 * Arango DB の接続ポート番号を返します。
	 * 
	 * @return 接続ポート番号
	 */
	protected abstract int getPort();
	
	/**
	 * Arango DB の接続ユーザ名を返します。
	 *
	 * @return 接続ユーザー名
	 */
	protected abstract String getUserName();
	
	/**
	 * Arango DB の接続パスワードを返します。
	 * 
	 * @return 接続パスワード
	 */
	protected abstract String getPassword();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Bean(name = BeanNames.ARANGO_DB)
	public ArangoDBClient arangoDBClient() throws Exception {
		ArangoDB arangoDB = new ArangoDB.Builder()
					.host(getHost())
					.port(getPort())
					.user(getUserName())
					.password(getPassword())
					.build();
		
		ArangoDBClient arangoDBClient = new ArangoDBClient(arangoDB, getDBName());
		
		return arangoDBClient;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ArangoDBConfigurer arangoDBConfigurer() {
		return this;
	}
}
