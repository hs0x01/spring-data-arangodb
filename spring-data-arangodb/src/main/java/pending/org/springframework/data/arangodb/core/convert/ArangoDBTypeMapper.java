package pending.org.springframework.data.arangodb.core.convert;

import org.springframework.data.convert.TypeMapper;

import com.arangodb.entity.BaseDocument;

/**
 * 型マッパーのマーカーインタフェースです。
 * 
 * @author hs0x01
 *
 */
public interface ArangoDBTypeMapper extends TypeMapper<BaseDocument> {
}
