package pending.org.springframework.data.arangodb.config;

import pending.org.springframework.data.arangodb.core.ArangoDBClient;

//TODO 不要？
public interface ArangoDBConfigurer {

	ArangoDBClient arangoDBClient() throws Exception;
}
