package pending.org.springframework.data.arangodb.repository.query;

import java.io.Serializable;

import org.springframework.data.repository.core.EntityInformation;

/**
 * Arango DB エンティティ情報のマーカインタフェースです。
 * 
 * @author hs0x01
 *
 * @param <T> エンティティ
 * @param <ID> ID
 */
public interface ArangoDBEntityInformation<T, ID extends Serializable> extends EntityInformation<T, ID> {
}
