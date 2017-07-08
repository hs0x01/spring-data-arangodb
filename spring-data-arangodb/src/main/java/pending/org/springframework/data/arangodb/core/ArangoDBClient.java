package pending.org.springframework.data.arangodb.core;

import com.arangodb.ArangoDB;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Arango DB のクライアントです。
 * 
 * @author hs0x01
 *
 */
@Data
@AllArgsConstructor
public class ArangoDBClient {
	
	/**
	 * {@link ArangoDB} インスタンスです。
	 */
	private ArangoDB arangoDB;
	
	/**
	 * データベース名です。
	 */
	private String dbName;
}
