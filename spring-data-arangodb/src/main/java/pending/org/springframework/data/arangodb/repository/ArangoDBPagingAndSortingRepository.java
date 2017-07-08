package pending.org.springframework.data.arangodb.repository;

import java.io.Serializable;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Arango DB の {@link PagingAndSortingRepository} インタフェースです。
 *
 * @author hs0x01
 */
public interface ArangoDBPagingAndSortingRepository<T, ID extends Serializable>
    extends ArangoDBRepository<T, ID>, PagingAndSortingRepository<T, ID> {
}
