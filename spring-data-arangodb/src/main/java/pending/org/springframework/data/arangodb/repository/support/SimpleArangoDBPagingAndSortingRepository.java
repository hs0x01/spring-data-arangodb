package pending.org.springframework.data.arangodb.repository.support;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

import pending.org.springframework.data.arangodb.core.ArangoDBOperations;
import pending.org.springframework.data.arangodb.repository.ArangoDBPagingAndSortingRepository;
import pending.org.springframework.data.arangodb.repository.query.ArangoDBEntityInformation;

/**
 * Arango DB の {@link PagingAndSortingRepository} の実装です。
 * 
 * @author hs0x01
 *
 * @param <T>
 *            エンティティ
 * @param <ID>
 *            ID
 */
public class SimpleArangoDBPagingAndSortingRepository<T, ID extends Serializable>
		extends SimpleArangoDBRepository<T, ID> implements ArangoDBPagingAndSortingRepository<T, ID> {

	/**
	 * リポジトリを生成します。
	 *
	 * @param metadata
	 *            {@link ArangoDBEntityInformation} インスタンス
	 * @param couchbaseOperations
	 *            {@link ArangoDBOperations} インスタンス
	 */
	public SimpleArangoDBPagingAndSortingRepository(ArangoDBEntityInformation<T, ID> metadata,
			ArangoDBOperations arangoDBOperations) {
		super(metadata, arangoDBOperations);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<T> findAll(Sort sort) {

		String collectionName = getCollectionName();

		Map<String, Object> bindVars = new HashMap<>();

		String aqlSort = getAqlSort(sort, "c");

		return arangoDBOperations.readByAql("FOR c IN " + collectionName + " " + aqlSort + " RETURN c", bindVars,
				entityInformation.getJavaType());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Page<T> findAll(Pageable pageable) {
		
		String aqlSort = "";
		if (pageable.getSort() != null) {
			aqlSort = getAqlSort(pageable.getSort(), "c");
		}

		String collectionName = getCollectionName();

		Map<String, Object> bindVars = new HashMap<>();

		StringBuilder aql = new StringBuilder();
		
		aql.append("FOR c IN ");
		aql.append(collectionName).append(" ");
		aql.append(aqlSort).append(" ");
		aql.append("LIMIT ").append(pageable.getOffset()).append(", ").append(pageable.getPageSize()).append(" ");
		aql.append("RETURN c");
		
		List<T> list = arangoDBOperations.readByAql(aql.toString(), bindVars,
				entityInformation.getJavaType());
		
		long total = count();
		
		return new PageImpl<>(list, pageable, total);
	}

	/**
	 * AQL のソート条件文字列を返します。
	 * 
	 * @param sort
	 *            {@link Sort}
	 * @param collectionAlias
	 *            コレクションのエイリアス
	 * @return AQL のソート条件文字列
	 */
	protected String getAqlSort(Sort sort, String collectionAlias) {

		StringBuilder aqlSort = new StringBuilder();

		aqlSort.append("SORT");

		for (Sort.Order order : sort) {

			String orderProperty = order.getProperty();

			if (order.isAscending()) {
				aqlSort.append(" ").append(collectionAlias).append(".").append(orderProperty).append(" ASC")
						.append(",");
			} else {
				aqlSort.append(" ").append(collectionAlias).append(".").append(orderProperty).append(" DESC")
						.append(",");
			}
		}

		aqlSort.delete(aqlSort.length() - 1, aqlSort.length());

		return aqlSort.toString();
	}
}
