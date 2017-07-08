package pending.org.springframework.data.arangodb.core.mapping;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mapping.model.SimpleTypeHolder;

public abstract class ArangoDBSimpleTypes {

	static {
		Set<Class<?>> simpleTypes = new HashSet<Class<?>>();
		simpleTypes.add(Number.class);
		ARANGO_DB_SIMPLE_TYPES = Collections.unmodifiableSet(simpleTypes);
	}

	private static final Set<Class<?>> ARANGO_DB_SIMPLE_TYPES;
	public static final SimpleTypeHolder HOLDER = new SimpleTypeHolder(ARANGO_DB_SIMPLE_TYPES, true);

	private ArangoDBSimpleTypes() {
	}
}
